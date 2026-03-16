package com.his.controller;

import com.his.domain.PageResult;
import com.his.domain.Result;
import com.his.domain.SmsStaff;
import com.his.dto.SmsStaffLoginDTO;
import com.his.dto.SmsStaffRegisterDTO;
import com.his.service.ISmsStaffService;
import com.his.vo.SmsStaffLoginVo;
import com.his.vo.StaffPageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/staff")
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

    /**
     * 分页获取员工列表
     */
    @GetMapping("staffs")
    public Result<PageResult<StaffPageVo>> getStaffs(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "deptId", required = false) Integer deptId,
            @RequestParam(value = "roleId", required = false) Integer roleId
    ) {
        return Result.success("获取员工列表成功", smsStallService.getStaffByPage(page, size, deptId, roleId));
    }

    /**
     * 新增员工 - matches POST /staff/staffs
     */
    @PostMapping("createStaff")
    public Result<Object> createStaff(@RequestBody SmsStaff smsStaff) {
        smsStallService.createStaff(smsStaff);
        return Result.success("新增员工成功", null);
    }

    /**
     * 修改员工信息 - matches PUT /staff/staffs/{id}
     */
    @PutMapping("staffs/{id}")
    public Result<Object> updateStaff(@PathVariable("id") Long id, @RequestBody SmsStaff smsStaff) {
        smsStaff.setId(id);
        smsStallService.updateStaff(id, smsStaff);
        return Result.success("更新员工成功", null);
    }

    /**
     * 删除员工 - matches DELETE /staff/staffs/{id}
     */
    @DeleteMapping("staffs/{username}")
    public Result<Object> deleteStaff(@PathVariable String username) {
        smsStallService.deleteStaff(username);
        return Result.success("删除员工成功", null);
    }
}
