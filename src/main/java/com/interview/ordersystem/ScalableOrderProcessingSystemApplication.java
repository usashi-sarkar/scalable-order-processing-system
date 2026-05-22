package com.interview.ordersystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ScalableOrderProcessingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScalableOrderProcessingSystemApplication.class, args);
    }
}
