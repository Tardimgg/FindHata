package com.example.externalRequestFilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ExternalRequestFilterApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(ExternalRequestFilterApplication.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}

}
