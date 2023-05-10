package com.example.findHataGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FindHataGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(FindHataGatewayApplication.class, args);
	}

}
