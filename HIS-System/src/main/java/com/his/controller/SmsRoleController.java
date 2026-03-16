package com.his.controller;

import com.his.domain.PageResult;
import com.his.domain.SmsPermission;
import com.his.domain.SmsRole;
import com.his.domain.Result;
import com.his.service.IRoleService;
import com.his.vo.PermissionNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
public class SmsRoleController {

    @Autowired
    private IRoleService roleService;

    @GetMapping("/roles")
    public Result<PageResult<SmsRole>> getRoles(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                @RequestParam(value = "size", defaultValue = "20") Integer size,
                                                @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success("获取角色列表成功", roleService.getRoleByPage(page, size, keyword));
    }

    @GetMapping("/permissions")
    public Result<List<PermissionNode>> getPermissions() {
        return Result.success("获取权限成功", roleService.getAllPermissions());
    }

    @GetMapping("/roles/{id}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable("id") Long id) {
        return Result.success("获取角色权限成功", roleService.getPermissionIdsByRole(id));
    }

    @PostMapping("/roles")
    public Result<Object> createRole(@RequestBody SmsRole role) {
        roleService.createRole(role);
        return Result.success("创建成功", null);
    }

    @PutMapping("/roles/{id}")
    public Result<Object> updateRole(@PathVariable("id") Long id, @RequestBody SmsRole role) {
        roleService.updateRole(id, role);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/roles/{id}")
    public Result<Object> deleteRole(@PathVariable("id") Long id) {
        roleService.deleteRole(id);
        return Result.success("删除成功", null);
    }

    @PutMapping("/roles/{id}/permissions")
    public Result<Object> updateRolePermissions(@PathVariable("id") Long id, @RequestBody List<Long> permissionIds) {
        roleService.updateRolePermissions(id, permissionIds);
        return Result.success("更新权限成功", null);
    }

    // permission CRUD endpoints
    @PostMapping("/permissions")
    public Result<Object> createPermission(@RequestBody SmsPermission permission) {
        roleService.createPermission(permission);
        return Result.success("创建权限成功", null);
    }

    @PutMapping("/permissions/{id}")
    public Result<Object> updatePermission(@PathVariable("id") Long id, @RequestBody SmsPermission permission) {
        roleService.updatePermission(id, permission);
        return Result.success("更新权限成功", null);
    }

    @DeleteMapping("/permissions/{id}")
    public Result<Object> deletePermission(@PathVariable("id") Long id) {
        roleService.deletePermission(id);
        return Result.success("删除权限成功", null);
    }
}
