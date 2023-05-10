package com.example.findHataMessagingServer.configs;

import com.example.externalRequestFilter.security.PreFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ExternalFilter implements PreFilter {


    @SneakyThrows
    @Override
    public boolean rejectFiltering(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        List<String> block = List.of("/ws");

        return block.stream()
                .anyMatch((v) -> httpRequest.getRequestURI().startsWith(v));

    }
}
