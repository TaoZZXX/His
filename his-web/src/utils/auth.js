import Cookies from 'js-cookie'

const TokenKey = 'vue_admin_template_token'

const UserInfoKey = 'vue_admin_template_user_info'

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}

export function getUserInfo() {
  const userInfo = Cookies.get(UserInfoKey)
  return userInfo ? JSON.parse(userInfo) : {}
}

export function setUserInfo(info) {
  return Cookies.set(UserInfoKey, JSON.stringify(info))
}

export function removeUserInfo() {
  return Cookies.remove(UserInfoKey)
}
