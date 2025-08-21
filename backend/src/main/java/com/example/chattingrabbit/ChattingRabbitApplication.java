package com.example.chattingrabbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChattingRabbitApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChattingRabbitApplication.class, args);
    }
}
