package com.his.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/staff")
public class StaffController {

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");

        if ("admin".equals(username) && "123456".equals(password)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 20000);

            // JDK8 兼容：不要用 Map.of
            Map<String, Object> data = new HashMap<>();
            data.put("token", "admin-token");
            response.put("data", data);

            return response;
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 50008);
            response.put("message", "Login failed, please check your username and password.");
            return response;
        }
    }
}
