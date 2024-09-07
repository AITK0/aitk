const {defineConfig} = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8081,
    proxy: {
      '^/': {
        target: "http://localhost:8080",
        ws: false,
        changeOrigin: true
      }
    }
  }
})
