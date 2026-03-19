import request from '@/utils/request'

// 获取用户列表（分页与筛选）
export function getStaffList(params) {
  return request({
    url: '/sms/staff/staffs',
    method: 'get',
    params
  })
}

// 获取科室列表（若后端提供）
export function getDepartments() {
  return request({
    url: '/sms/registration/departments',
    method: 'get'
  })
}

// 获取角色列表（若后端提供）
export function getRoles() {
  return request({
    url: '/sms/role/roles',
    method: 'get',
    params: {
      page: 1,
      size: 2000
    }
  })
}

// 创建用户
export function createStaff(data) {
  return request({
    url: '/sms/staff/createStaff',
    method: 'post',
    data
  })
}

// 修改用户
export function updateStaff(id, data) {
  return request({
    url: `/sms/staff/staffs/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteStaff(username) {
  return request({
    url: `/sms/staff/staffs/${username}`,
    method: 'delete'
  })
}
