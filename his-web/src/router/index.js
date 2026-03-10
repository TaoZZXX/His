import Vue from 'vue'
import VueRouter from 'vue-router'

import DashboardVue from "@/layout/dashboard/Dashboard.Vue.vue";
import { getToken } from '@/utils/auth'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'home',
    component: DashboardVue,
    meta: { title: '主页' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/LoginVue.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/register/RegisterVue.vue'),
    meta: { title: '注册' }
  }
]

function createRouter() {
  return new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
  })
}

const router = createRouter()

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }

  // 登录白名单
  const whiteList = ['/login', '/register']
  const hasToken = getToken()

  if (hasToken) {
    // 已登录，访问登录页则跳转主页
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      next()
    }
  } else {
    // 未登录，访问白名单以外页面则跳转登录页
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next({ path: '/login', query: { redirect: to.fullPath } })
    }
  }
})

export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
