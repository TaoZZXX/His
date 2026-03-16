import request from '@/utils/request'

// 获取角色列表（分页）
export function getRoleList(params) {
  return request({
    url: '/sms/role/roles',
    method: 'get',
    params
  })
}

// 获取所有权限（以树形结构返回）
export function getPermissions() {
  return request({
    url: '/sms/role/permissions',
    method: 'get'
  })
}

// 获取单个权限
export function getPermission(id) {
  return request({
    url: `/sms/role/permissions/${id}`,
    method: 'get'
  })
}

// 获取角色的权限 id 列表
export function getRolePermissions(roleId) {
  return request({
    url: `/sms/role/roles/${roleId}/permissions`,
    method: 'get'
  })
}

// 创建权限
export function createPermission(data) {
  return request({
    url: '/sms/role/permissions',
    method: 'post',
    data
  })
}

// 修改权限
export function updatePermission(id, data) {
  return request({
    url: `/sms/role/permissions/${id}`,
    method: 'put',
    data
  })
}

// 删除权限
export function deletePermission(id) {
  return request({
    url: `/sms/role/permissions/${id}`,
    method: 'delete'
  })
}

// 创建角色
export function createRole(data) {
  return request({
    url: '/sms/role/roles',
    method: 'post',
    data
  })
}

// 修改角色
export function updateRole(id, data) {
  return request({
    url: `/sms/role/roles/${id}`,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/sms/role/roles/${id}`,
    method: 'delete'
  })
}

// 更新角色权限（传入权限 id 数组）
export function updateRolePermissions(roleId, permissionIds) {
  return request({
    url: `/sms/role/roles/${roleId}/permissions`,
    method: 'put',
    data: permissionIds
  })
}
