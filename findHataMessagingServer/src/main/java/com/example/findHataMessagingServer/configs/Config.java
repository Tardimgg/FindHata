package com.example.findHataMessagingServer.configs;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.example.externalRequestFilter.security")
@ComponentScan("com.example.webApiClient")
public class Config { }
