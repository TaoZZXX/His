package com.his.service.impl;

import com.his.domain.PageResult;
import com.his.domain.SmsPermission;
import com.his.domain.SmsRole;
import com.his.enums.ResultCode;
import com.his.exception.BusinessException;
import com.his.mapper.SmsRoleMapper;
import com.his.service.IRoleService;
import com.his.vo.PermissionNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleService implements IRoleService {

    private static final String PERM_VERSION_KEY = "his:permission:cache:ver";

    @Autowired
    private SmsRoleMapper roleMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    private void bumpPermissionCacheVersion() {
        try {
            if (stringRedisTemplate == null) return;
            stringRedisTemplate.opsForValue().increment(PERM_VERSION_KEY);
        } catch (Exception e) {
            // redis 不可用不影响主流程
        }
    }

    @Override
    public PageResult<SmsRole> getRoleByPage(Integer page, Integer size, String keyword) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        int offset = (page - 1) * size;
        Long total = roleMapper.selectRoleCount(keyword);
        List<SmsRole> list = roleMapper.selectRoleList(offset, size, keyword);
        PageResult<SmsRole> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total == null ? 0L : total);
        result.setPageNo(page);
        result.setPageSize(size);
        result.setPages((int)((result.getTotal() + size -1)/size));
        return result;
    }

    @Override
    public List<PermissionNode> getAllPermissions() {
        List<SmsPermission> perms = roleMapper.selectAllPermissions();
        if (perms == null) return new ArrayList<>();
        // map SmsPermission -> PermissionNode
        List<PermissionNode> nodes = new ArrayList<>();
        for (SmsPermission p : perms) {
            PermissionNode n = new PermissionNode();
            n.setId(p.getId());
            n.setPid(p.getPid());
            n.setName(p.getName());
            n.setValue(p.getValue());
            n.setUrl(p.getUrl());
            n.setStatus(p.getStatus());
            n.setCreateTime(p.getCreateTime());
            n.setSort(p.getSort());
            n.setType(p.getType());
            nodes.add(n);
        }
        return buildTree(nodes);
    }

    private List<PermissionNode> buildTree(List<PermissionNode> flat) {
        Map<Long, PermissionNode> map = new HashMap<>();
        List<PermissionNode> roots = new ArrayList<>();
        for (PermissionNode n : flat) {
            map.put(n.getId(), n);
        }
        for (PermissionNode n : flat) {
            Long pid = n.getPid();
            if (pid == null || pid == 0) {
                roots.add(n);
            } else {
                PermissionNode parent = map.get(pid);
                if (parent != null) {
                    parent.getChildren().add(n);
                } else {
                    roots.add(n); // orphan, treat as root
                }
            }
        }
        return roots;
    }

    @Override
    public List<Long> getPermissionIdsByRole(Long roleId) {
        List<Long> ids = roleMapper.selectPermissionIdsByRoleId(roleId);
        return ids == null ? new ArrayList<>() : ids;
    }

    @Override
    public void createRole(SmsRole role) {
        if (role == null || role.getName() == null) throw new BusinessException(ResultCode.PARAM_ERROR, "角色名不能为空");
        role.setCreateTime(LocalDateTime.now());
        if (roleMapper.insertRole(role) < 1) throw new BusinessException(ResultCode.SERVER_ERROR, "新增角色失败");
    }

    @Override
    public void updateRole(Long id, SmsRole role) {
        if (id == null || role == null) throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        role.setId(id);
        if (roleMapper.updateRole(role) < 1) throw new BusinessException(ResultCode.SERVER_ERROR, "更新角色失败");
    }

    @Override
    public void deleteRole(Long id) {
        if (id == null) throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        SmsRole role = roleMapper.selectRoleById(id);
        if (role != null && role.getName() != null) {
            String rn = role.getName().toLowerCase();
            // 约束：管理员角色不可删除
            if (rn.contains("管理员") || rn.contains("admin")) {
                throw new BusinessException(ResultCode.PERMISSION_DENIED, "管理员角色不可删除");
            }
        }
        // delete role permissions too
        roleMapper.deleteRolePermissions(id);
        if (roleMapper.deleteRoleById(id) < 1) throw new BusinessException(ResultCode.SERVER_ERROR, "删除角色失败");
        bumpPermissionCacheVersion();
    }

    @Override
    public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        if (roleId == null) throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        // delete existing
        roleMapper.deleteRolePermissions(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long pid : permissionIds) {
                roleMapper.insertRolePermission(roleId, pid);
            }
        }
        bumpPermissionCacheVersion();
    }

    @Override
    public void createPermission(SmsPermission permission) {
        if (permission == null || permission.getName() == null) throw new BusinessException(ResultCode.PARAM_ERROR, "权限名不能为空");
        // 前端新增权限弹窗未提供“状态”字段时，status 可能为 null
        // 鉴权 SQL 只取 p.status=1，因此这里给默认值避免权限取不到
        if (permission.getStatus() == null) {
            permission.setStatus(1);
        }
        permission.setCreateTime(LocalDateTime.now());
        if (roleMapper.insertPermission(permission) < 1) throw new BusinessException(ResultCode.SERVER_ERROR, "新增权限失败");
        bumpPermissionCacheVersion();
    }

    @Override
    public void updatePermission(Long id, SmsPermission permission) {
        if (id == null || permission == null) throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        permission.setId(id);
        // 同理：如果更新时前端没传 status，则给默认值
        if (permission.getStatus() == null) {
            permission.setStatus(1);
        }
        if (roleMapper.updatePermission(permission) < 1) throw new BusinessException(ResultCode.SERVER_ERROR, "更新权限失败");
        bumpPermissionCacheVersion();
    }

    @Override
    public void deletePermission(Long id) {
        if (id == null) throw new BusinessException(ResultCode.PARAM_ERROR, "参数错误");
        if (roleMapper.deletePermissionById(id) < 1) throw new BusinessException(ResultCode.SERVER_ERROR, "删除权限失败");
        bumpPermissionCacheVersion();
    }
}
