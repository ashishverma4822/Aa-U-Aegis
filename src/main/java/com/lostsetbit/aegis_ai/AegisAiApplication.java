package com.lostsetbit.aegis_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AegisAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AegisAiApplication.class, args);
	}

}
