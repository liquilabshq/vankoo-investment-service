package com.liquilabs.vankoo.investment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class VankooInvestmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VankooInvestmentServiceApplication.class, args);
    }
}