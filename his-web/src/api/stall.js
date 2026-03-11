import request from '@/utils/request'

// 用户登录
export function login(data) {
  return request({
    url: '/sms/stall/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/sms/stall/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getInfo(token) {
  return request({
    url: '/sms/stall/info',
    method: 'get',
    params: { token }
  })
}

// 用户登出
export function logout() {
  return request({
    url: '/stall/logout',
    method: 'post'
  })
}
