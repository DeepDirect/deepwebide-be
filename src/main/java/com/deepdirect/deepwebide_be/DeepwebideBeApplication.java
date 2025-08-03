package com.deepdirect.deepwebide_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Async
public class DeepwebideBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeepwebideBeApplication.class, args);
	}

}
