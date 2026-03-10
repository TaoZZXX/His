import { login, logout, getInfo } from '@/api/stall'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import { resetRouter } from '@/router'

const getDefaultState = () => {
  const userInfo = getUserInfo()
  return {
    token: getToken(),
    name: userInfo.name || '',
    username: userInfo.username || '',
    avatar: userInfo.avatar || ''
  }
}

const state = getDefaultState()

const mutations = {
  RESET_STATE: (state) => {
    Object.assign(state, getDefaultState())
  },
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_NAME: (state, name) => {
    state.name = name
    setUserInfo({ ...getUserInfo(), name })
  },
  SET_USERNAME: (state, username) => {
    state.username = username
    setUserInfo({ ...getUserInfo(), username })
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
    setUserInfo({ ...getUserInfo(), avatar })
  }
}

const actions = {
  // user login
  login({ commit }, userInfo) {
    const { username, password } = userInfo
    return new Promise((resolve, reject) => {
      login({ username: username.trim(), password: this._vm.$md5(password) }).then(response => {
        const { data } = response
        commit('SET_TOKEN', data.token)
        setToken(data.token)

        // 存储登录返回的用户信息
        if (data.username) {
          commit('SET_USERNAME', data.username)
        }
        if (data.name) {
          commit('SET_NAME', data.name)
        }

        resolve()
      }).catch(error => {
        console.log(error)
        reject(error)
      })
    })
  },

  // get user info
  getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      getInfo(state.token).then(response => {
        const { data } = response

        if (!data) {
          return reject('Verification failed, please Login again.')
        }

        const { name, username, avatar } = data

        commit('SET_NAME', name)
        if (username) {
          commit('SET_USERNAME', username)
        }
        commit('SET_AVATAR', avatar)
        resolve(data)
      }).catch(error => {
        console.log(error)
        // 关键修复：调用reject
        reject(error)
      })
    })
  },

  // user logout
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      logout(state.token).then(() => {
        removeToken() // must remove  token  first
        removeUserInfo() // 清除用户信息
        resetRouter()
        commit('RESET_STATE')
        resolve()
      }).catch(error => {
        console.log(error)
        // 关键修复：调用reject
        reject(error)
      })
    })
  },

  // remove token
  resetToken({ commit }) {
    return new Promise(resolve => {
      removeToken() // must remove  token  first
      removeUserInfo() // 清除用户信息
      commit('RESET_STATE')
      resolve()
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}