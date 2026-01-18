const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 80, // 把端口改成 80
    open: true, // 可选：启动项目后自动打开浏览器
    host: '0.0.0.0', // 可选：允许外部访问（如同一局域网的其他设备）
    proxy: {
      '/dev-api': {
        target: 'http://localhost:9999', // 后端接口的真实地址+端口（8080就是后端端口）
        changeOrigin: true,
        pathRewrite: { '^/dev-api': '' }
      }
    }
  }
})
