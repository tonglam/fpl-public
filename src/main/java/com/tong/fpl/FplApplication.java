package com.tong.fpl;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableEncryptableProperties
@SpringBootApplication
public class FplApplication {

    public static void main(String[] args) {
        SpringApplication.run(FplApplication.class, args);
    }

}
