package com.tong.fpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
//@EnableScheduling
@SpringBootApplication
public class FplApplication {

	public static void main(String[] args) {
		SpringApplication.run(FplApplication.class, args);
	}

}
