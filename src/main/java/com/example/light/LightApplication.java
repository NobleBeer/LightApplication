package com.example.light;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
public class LightApplication {

	public static void main(String[] args) {
		SpringApplication.run(LightApplication.class, args);
	}

}
