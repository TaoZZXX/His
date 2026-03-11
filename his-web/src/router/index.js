import Vue from 'vue'
import VueRouter from 'vue-router'

import DashboardVue from "@/layout/dashboard/Dashboard.Vue.vue";
import { getToken } from '@/utils/auth'
import Home from '@/views/home/Home.vue';
import Registration from '@/views/registration/Registration.vue';

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'dashboard',
    component: DashboardVue,
    redirect: '/home',
    meta: { title: '主页' },
    children: [
      {
        path: 'home',
        name: 'home',
        component: Home,
        meta: { title: '主页' }
      },
      {
        path: 'charge',
        name: 'charge',
        component: { render: h => h('router-view') },
        meta: { title: '门诊收费挂号', breadcrumb: ['首页', '门诊收费挂号'] },
        children: [
          {
            path: 'registration',
            name: 'registration',
            component: Registration,
            meta: { title: '门诊挂号工作台', breadcrumb: ['首页', '门诊收费挂号', '门诊挂号工作台'] }
          }
        ]
      }
    ]
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
