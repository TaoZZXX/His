package com.his;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.his.mapper")
@SpringBootApplication
public class HisSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HisSystemApplication.class, args);
    }

}
