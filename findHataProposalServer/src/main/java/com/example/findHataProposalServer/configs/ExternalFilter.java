package com.example.findHataProposalServer.configs;

import com.example.externalRequestFilter.security.PreFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class ExternalFilter implements PreFilter {

    @SneakyThrows
    @Override
    public boolean rejectFiltering(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return httpRequest.getRequestURI().startsWith("/images")
                || httpRequest.getRequestURI().startsWith("/api/proposal/save-image");
    }
}
