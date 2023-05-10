package com.example.findHataMessagingServer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.company.DatabaseCreator;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class FindHataMessagingServerApplication {

	public static void main(String[] args) {
		DatabaseCreator.init();
		SpringApplication.run(FindHataMessagingServerApplication.class, args);
	}
}
