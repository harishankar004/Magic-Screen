package com.example.MagicScreenBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MagicScreenBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MagicScreenBackendApplication.class, args);
	}
}
