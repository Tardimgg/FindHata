package com.example.externalRequestFilter.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface PreFilter {

    boolean rejectFiltering(HttpServletRequest httpRequest, HttpServletResponse httpResponse);
}
