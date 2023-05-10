package com.example.webApiClient;

import com.example.tokenService.TokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
@Slf4j
public class WebApiClient {

    @Autowired
    private WebClientFilter webClientFilter;

    @Value("${service_login}")
    String serviceLogin;

    @Value("${service_password}")
    String servicePassword;

    @Value("${internal_registration_jwt_secret}")
    private String internalJwtSecret;

    @Value("${auth_url}")
    private String authUrl;

    @Getter
    private String serviceUserToken;

    private Mono<String> createToken() {
        WebClient client = WebClient.builder()
                .filter(webClientFilter)
                .baseUrl(authUrl + "/api/")
                .build();

        Map<String, String> rawJson = new HashMap<>();
        rawJson.put("serviceId", serviceLogin);
        rawJson.put("password", servicePassword);

        TokenService tokenService = new TokenService(internalJwtSecret);
        String registrationToken = tokenService.generateRawToken(rawJson, 5000);

        Map<String, String> json = new HashMap<>();
        json.put("registrationToken", registrationToken);


        return client.post()
                .uri("internal-registration")
                .body(CustomInserter.fromValue(json))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap((response) -> {
                    ObjectMapper mapper = new ObjectMapper();

                    JsonNode tokenJson = null;
                    try {
                        tokenJson = mapper.readTree(response);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    if (tokenJson != null) {
                        if (tokenJson.has("accessToken")) {
                            serviceUserToken = tokenJson.get("accessToken").asText();
                            return Mono.just(serviceUserToken);
                        } else if (tokenJson.has("error")) {
                            if (tokenJson.get("error").asText().equals("The user already exists")){
                                return updateToken();
                            }
                        }
                    }
                    log.error("Failed to registration");
                    return Mono.error(new RuntimeException("Failed to registration"));
                });
    }


    private Mono<String> updateToken() {
        WebClient client = WebClient.builder()
                .filter(webClientFilter)
                .baseUrl("http://gateway-server/auth/api/")
                .baseUrl(authUrl + "/api/")

                .build();

        Map<String, String> json = new HashMap<>();
        json.put("login", serviceLogin);
        json.put("password", servicePassword);

        return client.post()
                .uri("login")
                .body(CustomInserter.fromValue(json))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap((response) -> {
                    ObjectMapper mapper = new ObjectMapper();

                    JsonNode tokenJson = null;
                    try {
                        tokenJson = mapper.readTree(response);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    if (tokenJson != null) {
                        if (tokenJson.has("accessToken")) {
                            serviceUserToken = tokenJson.get("accessToken").asText();
                            return Mono.just(serviceUserToken);
                        } else if (tokenJson.has("error")) {
                            if (tokenJson.get("error").asText().equals("The user does not exist")) {
                                return createToken();
                            }
                        }
                    }
                    System.out.println("Failed to update token");
                    return Mono.just("");
                });
    }

    public Mono<WebClient.Builder> getWebClientBuilder() {
        WebClient client = WebClient.builder()
                .filter(webClientFilter)
                .baseUrl(authUrl + "/api/")

                .build();

        Supplier<Mono<WebClient.Builder>> supplier = () -> client.post()
                .uri("check-token")
                .body(CustomInserter.fromValue(Map.of("accessToken", serviceUserToken)))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap((responseBody) -> {
                    ObjectMapper mapper = new ObjectMapper();

                    JsonNode responseJson = null;
                    try {
                        responseJson = mapper.readTree(responseBody);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    if (responseJson != null && responseJson.has("status") && responseJson.get("status").asText().equals("ok")) {
                        return Mono.just(serviceUserToken);
                    } else if (responseJson != null && responseJson.has("error")){
                        String err = responseJson.get("error").asText();
                        if (err.equals("The token has expired")) {
                            return updateToken();

                        } else if (err.equals("The token is incorrect")) {
                            return createToken();
                        }
                    }
                    return Mono.just("null");

                }).map((accessToken) -> WebClient.builder()
                        .filter(webClientFilter)
                        .defaultHeader("accessToken", accessToken));

        if (serviceUserToken == null) {
            return createToken()
                    .flatMap((v) -> supplier.get());
        }

        return supplier.get();
    }



    public Mono<WebClient> getWebClient(String baseUrl) {
        return getWebClientBuilder().map((v) -> v.baseUrl(baseUrl).build());

    }
}
