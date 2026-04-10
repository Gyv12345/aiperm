package com.devlovecode.aiperm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author shichenyang
 */
@EnableAsync
@SpringBootApplication
@Modulith(systemName = "AIPerm", additionalPackages = "com.devlovecode.aiperm.modules")
public class AipermApplication {

	public static void main(String[] args) {
		SpringApplication.run(AipermApplication.class, args);
	}

}
