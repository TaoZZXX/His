import request from '@/utils/request'

// 用户登录
export function login(data) {
  return request({
    url: '/sms/staff/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/sms/staff/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getInfo(token) {
  return request({
    url: '/sms/staff/info',
    method: 'get',
    params: { token }
  })
}

// 用户登出
export function logout() {
  return request({
    url: '/staff/logout',
    method: 'post'
  })
}
