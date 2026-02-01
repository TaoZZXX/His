package com.his.controller;

import com.his.domain.Result;
import com.his.dto.UserLoginDTO;
import com.his.dto.UserRegisterDTO;
import com.his.service.impl.SmsStallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private SmsStallService smsStallService;

    @PostMapping("/login")
    public Result<Object> login(@RequestBody UserLoginDTO userLoginDTO) {


        return Result.success("登录成功", "sidhasklhdlsahjk");
    }

    @PostMapping("/register")
    public Result<Object> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        smsStallService.register(userRegisterDTO);
        return Result.success("注册成功", null);
    }
}
