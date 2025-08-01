package com.example.farm4u;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Farm4uApplication {

	public static void main(String[] args) {
		SpringApplication.run(Farm4uApplication.class, args);
	}

}
