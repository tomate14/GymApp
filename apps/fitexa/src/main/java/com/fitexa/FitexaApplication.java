package com.fitexa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FitexaApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitexaApplication.class, args);
    }
}
