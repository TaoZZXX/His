package com.his;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class HisGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(HisGatewayApplication.class, args);
    }
}