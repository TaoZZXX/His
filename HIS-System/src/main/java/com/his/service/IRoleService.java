package com.his.service;

import com.his.domain.PageResult;
import com.his.domain.SmsPermission;
import com.his.domain.SmsRole;
import com.his.vo.PermissionNode;

import java.util.List;

public interface IRoleService {

    PageResult<SmsRole> getRoleByPage(Integer page, Integer size, String keyword);

    List<PermissionNode> getAllPermissions();

    List<Long> getPermissionIdsByRole(Long roleId);

    void createRole(SmsRole role);

    void updateRole(Long id, SmsRole role);

    void deleteRole(Long id);

    void updateRolePermissions(Long roleId, List<Long> permissionIds);

    // permission CRUD
    void createPermission(SmsPermission permission);

    void updatePermission(Long id, SmsPermission permission);

    void deletePermission(Long id);
}
