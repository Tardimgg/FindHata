package com.example.findHataAuth.configs;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.example.webApiClient")
@ComponentScan("com.example.externalRequestFilter.security")
public class Config { }
