package com.devlovecode.aiperm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class AipermApplication {

	public static void main(String[] args) {
		SpringApplication.run(AipermApplication.class, args);
	}

}
