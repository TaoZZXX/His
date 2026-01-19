const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 80, // 把端口改成 80
    host: '0.0.0.0', // 可选：允许外部访问（如同一局域网的其他设备）
    proxy: {
      '/dev-api': {
        target: 'http://localhost:9999',
        changeOrigin: true,
        pathRewrite: { '^/dev-api': '' }
      }
    }
  }
})
