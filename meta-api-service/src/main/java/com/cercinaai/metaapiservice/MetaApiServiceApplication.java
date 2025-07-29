package com.cercinaai.metaapiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MetaApiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetaApiServiceApplication.class, args);
    }

}
