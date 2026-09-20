package com.his.controller;

import com.his.domain.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/job")
public class JobController {

    @GetMapping("/ping")
    public Result<Object> ping() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "his-job-service");
        data.put("time", LocalDateTime.now().toString());
        return Result.success(data);
    }
}
