package com.his.controller;

import com.his.domain.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/notify")
public class NotifyController {

    @GetMapping("/ping")
    public Result<Object> ping() {
        return Result.success(Collections.singletonMap("service", "his-notify-service"));
    }
}
