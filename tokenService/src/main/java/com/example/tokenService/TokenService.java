package com.example.tokenService;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class TokenService {

    private final String jwtSecret;

    public TokenService(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @SneakyThrows
    public String generateRawToken(Map<String, ?> json, long durationInMillis) throws IllegalArgumentException, JWTCreationException {
        return JWT.create()
                .withPayload(json)
                .withExpiresAt(new Date(System.currentTimeMillis() + durationInMillis))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    @SneakyThrows
    public String generateRawToken(String json, long durationInMillis) throws IllegalArgumentException, JWTCreationException {
        return JWT.create()
                .withPayload(json)
                .withExpiresAt(new Date(System.currentTimeMillis() + durationInMillis))
                .sign(Algorithm.HMAC256(jwtSecret));

    }

    @SneakyThrows
    private Token createToken(String rawToken) {
        String[] tokenArr = rawToken.split("\\.");
        String header = tokenArr[0];

        String payload64 = tokenArr[1];
        String payload = new String(Base64.getUrlDecoder().decode(payload64.getBytes()));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(payload);
        int expires = actualObj.get("exp").asInt();

        ((ObjectNode) actualObj).remove("exp");
        String newPayload = actualObj.toString();

        String signature = tokenArr[2];

        return Token.builder()
                .header(header)
                .payload(newPayload)
                .signature(signature)
                .expires(expires)
                .build();
    }

    @SneakyThrows
    public Token generateToken(Map<String, ?> json) throws IllegalArgumentException, JWTCreationException {
        return generateToken(json, 2000);
    }

    @SneakyThrows
    public Token generateToken(Map<String, ?> json, long durationInMillis) throws IllegalArgumentException, JWTCreationException {
        String token = generateRawToken(json, durationInMillis);
        return createToken(token);
    }

    @SneakyThrows
    public Token generateToken(String json) throws IllegalArgumentException, JWTCreationException {
        return generateToken(json, 2000);
    }

    @SneakyThrows
    public Token generateToken(String json, long durationInMillis) throws IllegalArgumentException, JWTCreationException {
        String token = generateRawToken(json, durationInMillis);
        return createToken(token);
    }

    public Map<String, Claim> validateToken(Token token) throws JWTVerificationException {
        String payloadJsonBase64 = "";

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(token.payload);

            if (token.expires != null) {
                ((ObjectNode) actualObj).put("exp", token.expires);
            }

            String json = actualObj.toString();
            payloadJsonBase64 = Base64.getUrlEncoder().encodeToString(json.getBytes()).replace("=", "");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return validateRawToken(token.header + "." + payloadJsonBase64 + "." + token.signature);
    }

    public Map<String, Claim> validateRawToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
                .build();

        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaims();
    }


    @Builder
    @Data
    public static class Token {
        String header;
        String payload;
        String signature;

        //        @Nullable
        Integer expires;

        public static TokenBuilder withBase64Payload(String base64Payload) {
            String payload = new String(Base64.getUrlDecoder().decode(base64Payload.getBytes()));

            return Token.builder()
                    .payload(payload);
        }
    }
}
