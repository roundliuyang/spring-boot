package com.yly.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class MVCApplication {

	public static void main(String[] args) {
		SpringApplication.run(MVCApplication.class, args);
	}

}