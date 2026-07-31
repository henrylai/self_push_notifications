package com.pushpal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PushpalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PushpalApplication.class, args);
    }
}
