import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import { getToken } from '@/utils/auth'

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
  timeout: 5000
})

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
