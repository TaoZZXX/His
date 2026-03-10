package com.his.controller;

import com.his.domain.Result;
import com.his.dto.SmsStaffLoginDTO;
import com.his.dto.SmsStaffRegisterDTO;
import com.his.service.ISmsStaffService;
import com.his.vo.SmsStaffLoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/stall")
public class SmsStaffController {

    @Autowired
    private ISmsStaffService smsStallService;

    @PostMapping("/login")
    public Result<SmsStaffLoginVo> login(@RequestBody SmsStaffLoginDTO userLoginDTO) {
        SmsStaffLoginVo smsStaffLoginVo = smsStallService.login(userLoginDTO);
        return Result.success("登录成功", smsStaffLoginVo);
    }

    @PostMapping("/register")
    public Result<Object> register(@RequestBody SmsStaffRegisterDTO smsStaffRegisterDTO) {
        smsStallService.register(smsStaffRegisterDTO);
        return Result.success("注册成功", null);
    }

    @RequestMapping("/info")
    public Result<SmsStaffLoginVo> info(String token) {
        SmsStaffLoginVo smsStaffInfo = smsStallService.getInfo(token);
        return Result.success("获取用户信息成功", smsStaffInfo);
    }

    @PostMapping("/logout")
    public Result<Object> logout() {
        return Result.success("退出成功", null);
    }
}
