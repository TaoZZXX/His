package com.his.controller;

import com.his.domain.PageResult;
import com.his.domain.Result;
import com.his.domain.SmsStaff;
import com.his.dto.SmsStaffLoginDTO;
import com.his.dto.SmsStaffRegisterDTO;
import com.his.service.ISmsStaffService;
import com.his.vo.SmsStaffLoginVo;
import com.his.vo.StaffPageVo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff")
public class SmsStaffController {

    private final ISmsStaffService smsStaffService;

    public SmsStaffController(ISmsStaffService smsStaffService) {
        this.smsStaffService = smsStaffService;
    }

    @PostMapping("/login")
    public Result<SmsStaffLoginVo> login(@RequestBody SmsStaffLoginDTO dto) {
        return Result.success("登录成功", smsStaffService.login(dto));
    }

    @PostMapping("/register")
    public Result<Object> register(@RequestBody SmsStaffRegisterDTO dto) {
        smsStaffService.register(dto);
        return Result.success("注册成功", null);
    }

    @RequestMapping("/info")
    public Result<SmsStaffLoginVo> info(@RequestParam("token") String token) {
        return Result.success("获取用户信息成功", smsStaffService.getInfo(token));
    }

    @PostMapping("/logout")
    public Result<Object> logout() {
        return Result.success("退出成功", null);
    }

    @GetMapping("staffs")
    public Result<PageResult<StaffPageVo>> getStaffs(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                     @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                     @RequestParam(value = "deptId", required = false) Integer deptId,
                                                     @RequestParam(value = "roleId", required = false) Integer roleId) {
        return Result.success("获取员工列表成功", smsStaffService.getStaffByPage(page, size, deptId, roleId));
    }

    @PostMapping("createStaff")
    public Result<Object> createStaff(@RequestBody SmsStaff smsStaff) {
        smsStaffService.createStaff(smsStaff);
        return Result.success("新增员工成功", null);
    }

    @PutMapping("staffs/{id}")
    public Result<Object> updateStaff(@PathVariable("id") Long id, @RequestBody SmsStaff smsStaff) {
        smsStaffService.updateStaff(id, smsStaff);
        return Result.success("更新员工成功", null);
    }

    @DeleteMapping("staffs/{username}")
    public Result<Object> deleteStaff(@PathVariable String username) {
        smsStaffService.deleteStaff(username);
        return Result.success("删除员工成功", null);
    }
}
