package com.example.findHataNotificationServer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.company.DatabaseCreator;

@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class FindHataNotificationServerApplication {

	public static void main(String[] args) {
		DatabaseCreator.init();
		SpringApplication.run(FindHataNotificationServerApplication.class, args);
	}
}
