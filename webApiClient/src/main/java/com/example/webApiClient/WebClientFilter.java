package com.example.webApiClient;

import com.example.tokenService.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WebClientFilter implements ExchangeFilterFunction {

    @Value("${network_auth_jwt_secret}")
    String authJwtSecret;

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {

        TokenService tokenService = new TokenService(authJwtSecret);

        CustomInserter<Map<String, ?>> inserter = (CustomInserter<Map<String, ?>>) request.body();

        TokenService.Token token = tokenService.generateToken(inserter.getBody());

        ClientRequest newRequest = ClientRequest.from(request)
                .header("internal_jwt_header", token.getHeader())
                .header("internal_jwt_signature", token.getSignature())
                .header("internal_jwt_expires", Integer.toString(token.getExpires()))
                .build();


        return next.exchange(newRequest);
    }
}
