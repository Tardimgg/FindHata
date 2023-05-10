package com.example.findHataProposalServer;

import com.company.DatabaseCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class FindHataProposalServerApplication {

	public static void main(String[] args) {
		DatabaseCreator.init();
		SpringApplication.run(FindHataProposalServerApplication.class, args);
	}
}
