package com.example.findHataServerDiscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class FindHataServerDiscoveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(FindHataServerDiscoveryApplication.class, args);
	}

}
