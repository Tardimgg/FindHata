package com.example.findHataGateway;

import com.auth0.jwt.interfaces.Claim;
import com.example.Result;
import com.example.tokenService.TokenService;
import com.example.webApiClient.CustomInserter;
import com.example.webApiClient.WebApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR;


@Component
public class TokenizeFilter implements GlobalFilter {

    @Value("${network_auth_jwt_secret}")
    String networkAuthJwtSecret;

    @Autowired
    private WebApiClient webApiClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


        String body = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);

        LinkedHashSet<URI> path0 = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        String path = path0.stream().findAny().get().getPath();

        String accessToken = exchange.getRequest().getHeaders().getFirst("accessToken");


        TokenService tokenService = new TokenService(networkAuthJwtSecret);
        TokenService.Token bodyToken = tokenService.generateToken(body, 2000);

        exchange.getRequest().mutate().header("internal_jwt_header", bodyToken.getHeader())
                .header("internal_jwt_signature", bodyToken.getSignature())
                .header("internal_jwt_expires", Integer.toString(bodyToken.getExpires())).build();


        if (path.startsWith("/auth/") || accessToken == null) {
            System.out.println("gateway: without check token" );

            exchange.getRequest().mutate()
                    .header("userId", "-1")
                    .header("roles", "[ANONYMOUS]")
                    .header("hasAlternativeConnection", "false")
                    .build();

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                exchange.getResponse().getHeaders().remove("internal_jwt_header");
                exchange.getResponse().getHeaders().remove("internal_jwt_signature");
                exchange.getResponse().getHeaders().remove("internal_jwt_expires");
            }));
        }

        return webApiClient.getWebClient("http://auth-server/api/roles/")
                .flatMap(client -> {

                    Map<String, String> getAllRequest = new HashMap<>();
                    getAllRequest.put("userToken", accessToken);
                    getAllRequest.put("accessToken", webApiClient.getServiceUserToken());

                    return client.post()
                            .uri("get-all-with-token")
                            .body(CustomInserter.fromValue(getAllRequest))
                            .retrieve()
                            .bodyToMono(String.class)
                            .map((responseBody) -> {
                                ObjectMapper mapper = new ObjectMapper();

                                JsonNode responseJson = null;
                                try {
                                    responseJson = mapper.readTree(responseBody);
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }

                                Result<String, String> res;

                                if (responseJson != null && responseJson.has("status") && responseJson.get("status").asText().equals("ok")) {

                                    String stringRoles = "";
                                    try {
                                        ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {});
                                        List<String> roles = reader.readValue(responseJson.get("roles"));

                                        stringRoles = roles.stream().collect(Collectors.joining(", ", "[", "]"));
                                        res = Result.ok("");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        res = Result.err(e.toString());
                                    }

                                    exchange.getRequest().mutate().header("accessToken", null, null)
                                            .header("userId", responseJson.get("userId").asText())
                                            .header("roles", stringRoles)
                                            .header("hasAlternativeConnection", responseJson.get("hasAlternativeConnection").asText())
                                            .build();
                                } else {
                                    res = Result.err(responseBody);
                                }

                                return res;
                            })
                            .flatMap((v) -> {
                                if (v.type == Result.Val.Err) {
                                    exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                                    exchange.getResponse().getHeaders().add("Content-Type", "application/json");

                                    byte[] bytes = v.err.getBytes(StandardCharsets.UTF_8);
                                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

                                    return exchange.getResponse().writeWith(Flux.just(buffer));
                                }

                                System.out.println(exchange.getRequest().getHeaders());
                                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                                    HttpHeaders headers = exchange.getResponse().getHeaders();
                                    headers.remove("internal_jwt_header");
                                    headers.remove("internal_jwt_signature");
                                    headers.remove("internal_jwt_expires");

                                }));
                            });
                });
    }
}
