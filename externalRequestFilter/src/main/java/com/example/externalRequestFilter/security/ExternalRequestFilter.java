package com.example.externalRequestFilter.security;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.externalRequestFilter.cachedWrappers.CachedBodyRequestWrapper;
import com.example.tokenService.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ExternalRequestFilter extends GenericFilterBean {

    @Value("${network_auth_jwt_secret}")
    String authJwtSecret;

    @Autowired(required = false)
    PreFilter preFilter;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        log.warn("start filter");

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (preFilter != null && preFilter.rejectFiltering(httpRequest, httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        CachedBodyRequestWrapper requestWrapper = new CachedBodyRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        BufferedReader reader = requestWrapper.getReader();
        String body = reader.lines().collect(Collectors.joining());

        for (String headParam: List.of("internal_jwt_header", "internal_jwt_signature", "internal_jwt_expires")) {
            if (!requestWrapper.getHeaders(headParam).hasMoreElements()) {
                httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{ \"secure error\": \"bad request\"}");
                return;
            }
        }
        String tokenHeader = requestWrapper.getHeaders("internal_jwt_header").nextElement();
        String tokenSignature = requestWrapper.getHeaders("internal_jwt_signature").nextElement();
        int tokenExpires = Integer.parseInt(requestWrapper.getHeaders("internal_jwt_expires").nextElement());

        TokenService tokenService = new TokenService(authJwtSecret);

        if (!body.isEmpty()) {
            try {
                tokenService.validateToken(TokenService.Token.builder()
                        .header(tokenHeader)
                        .payload(body)
                        .signature(tokenSignature)
                        .expires(tokenExpires)
                        .build());
            } catch (TokenExpiredException e) {
                httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{ \"err\": \"" + e + "\"}");
                return;
            } catch (RuntimeException e) {
                httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{ \"err\": \"" + e + "\"}");
                return;
            }
        }


        chain.doFilter(requestWrapper, responseWrapper);


        if (responseWrapper.getStatus() == HttpStatus.OK.value()) {

            String ans = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());

            String header = "";
            String payload = "";
            String signature = "";

            if (!ans.isEmpty()) {
                TokenService.Token responseToken = tokenService.generateToken(ans, 1000L);
                header = responseToken.getHeader();
                payload = responseToken.getPayload();
                signature = responseToken.getSignature();
            }

            responseWrapper.setHeader("internal_jwt_header", header);
            responseWrapper.setHeader("internal_jwt_signature", signature);

            log.info("payload response: " + payload);

        }
        responseWrapper.copyBodyToResponse();
    }
}
