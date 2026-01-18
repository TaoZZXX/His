import Vue from 'vue'
import App from './App.vue'

// 1. 引入 Element UI 核心库和样式
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

// 2. 全局注册 Element UI
Vue.use(ElementUI)

// 导入全局样式
import '@/style/css/reset.css'

import router from './router'
import store from './store'

Vue.config.productionTip = false

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
