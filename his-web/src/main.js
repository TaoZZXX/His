import Vue from 'vue'
import App from './App.vue'

import md5 from 'js-md5'
Vue.prototype.$md5 = md5

import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
Vue.use(ElementUI)

// 导入全局样式
import '@/style/css/reset.css'
import '@/style/css/theme.css'

import router from './router'
import store from './store'

Vue.config.productionTip = false

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
