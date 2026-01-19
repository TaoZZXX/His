package com.his;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class HisGatewayApplication {
    public static void main(String[] args) {
        // 启动时打印关键信息，确认应用启动
        System.out.println("===== 网关启动开始 =====");
        SpringApplication.run(HisGatewayApplication.class, args);
        System.out.println("===== 网关启动完成 =====");
    }
}