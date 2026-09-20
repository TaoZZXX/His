package com.his;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.his.mapper")
public class HisDoctorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HisDoctorServiceApplication.class, args);
    }
}
