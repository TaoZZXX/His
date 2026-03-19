package com.his.mapper;

import com.his.domain.SmsPermission;
import com.his.domain.SmsRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SmsRoleMapper {

    List<SmsRole> selectRoleList(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("keyword") String keyword);

    Long selectRoleCount(@Param("keyword") String keyword);

    SmsRole selectRoleById(@Param("id") Long id);

    Integer insertRole(SmsRole role);

    Integer updateRole(SmsRole role);

    Integer deleteRoleById(@Param("id") Long id);

    // permission
    List<SmsPermission> selectAllPermissions();

    SmsPermission selectPermissionById(@Param("id") Long id);

    Integer insertPermission(SmsPermission permission);

    Integer updatePermission(SmsPermission permission);

    Integer deletePermissionById(@Param("id") Long id);

    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    Integer deleteRolePermissions(@Param("roleId") Long roleId);

    Integer insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

}
