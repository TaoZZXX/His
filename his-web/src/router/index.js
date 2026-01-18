import Vue from 'vue'
import VueRouter from 'vue-router'

import DashboardVue from "@/layout/dashboard/Dashboard.Vue.vue";

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

export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
