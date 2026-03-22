import request from '@/utils/request'
import { getToken } from '@/utils/auth'

export function getHomeDashboard() {
  return request({
    url: '/sms/home/dashboard',
    method: 'get',
    params: { token: getToken() }
  })
}
