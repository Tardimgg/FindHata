package com.example.findHataProposalServer.configs;

import akka.actor.ActorSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan("com.example.externalRequestFilter.security")
public class Config {

    @Bean
    ActorSystem system() {
        return ActorSystem.create("system");
    }
}
