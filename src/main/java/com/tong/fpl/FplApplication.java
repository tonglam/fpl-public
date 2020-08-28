package com.tong.fpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FplApplication {

	public static void main(String[] args) {
		SpringApplication.run(FplApplication.class, args);
	}

}
