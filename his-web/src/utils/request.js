import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import { getToken } from '@/utils/auth'

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
  timeout: 5000
})

// 短时间内同类错误可能会触发多次请求（例如页面初始化），做一次节流避免连弹多条提示
let lastPermissionDeniedAt = 0
const PERMISSION_DENIED_COOLDOWN_MS = 1500

service.interceptors.request.use(
  config => {
    // 兼容后端常见的 Token 传递方式：X-Token / Authorization
    const token = getToken()
    if (token) {
      config.headers['X-Token'] = token
      // 如果后端后续切到标准 Bearer，也能直接用
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// response interceptor
service.interceptors.response.use(
  response => {
    const res = response.data
    // 统一拦截：无权限提示（解决部分页面不弹消息问题）
    if (res && res.code === 50015) {
      const now = Date.now()
      if (now - lastPermissionDeniedAt > PERMISSION_DENIED_COOLDOWN_MS) {
        lastPermissionDeniedAt = now
        Message.error(res.message || '无权限访问')
      }
    }
      if (res.code === 50008 || res.code === 50012 || res.code === 50014) {
        // to re-login
        store.dispatch('user/resetToken').then(() => {
          location.reload()
        })
      }
      return res
  }
)

export default service
