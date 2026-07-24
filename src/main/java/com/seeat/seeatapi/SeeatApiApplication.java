package com.seeat.seeatapi;

import com.seeat.seeatapi.global.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class SeeatApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeeatApiApplication.class, args);
    }
}