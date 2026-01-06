import Vue from 'vue'
import App from './App.vue'

// 1. 引入 Element UI 核心库和样式
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

// 2. 全局注册 Element UI
Vue.use(ElementUI)

import router from './router'

Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
