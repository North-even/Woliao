package com.woliao.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WoLiaoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WoLiaoBackendApplication.class, args);
    }
} 