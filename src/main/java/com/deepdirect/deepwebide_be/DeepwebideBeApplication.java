package com.deepdirect.deepwebide_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeepwebideBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeepwebideBeApplication.class, args);
	}

}
