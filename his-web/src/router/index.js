import Vue from 'vue'
import VueRouter from 'vue-router'

import LoginVue from '@/views/login'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'login',
    component: LoginVue,
    meta: { title: '登录' }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }

  next()
})

export default router
