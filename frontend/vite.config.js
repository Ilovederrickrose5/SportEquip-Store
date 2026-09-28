import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 将所有以/api开头的请求代理到后端服务
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        // 按依赖体积拆分 chunk：大依赖独立分包后可被浏览器长期缓存，
        // 业务代码迭代时用户无需重新下载 element-plus / vue 等稳定依赖
        manualChunks(id) {
          if (!id.includes('node_modules')) return
          if (id.includes('element-plus')) return 'vendor-element'
          // 预留：当前项目未引入 echarts，若后续安装图表库会自动归入独立 chunk
          if (id.includes('echarts')) return 'vendor-echarts'
          if (/[\\/]node_modules[\\/](vue|@vue|vue-router|vuex|pinia)[\\/]/.test(id)) return 'vendor-vue'
          if (id.includes('axios')) return 'vendor-axios'
          return 'vendor-others'
        }
      }
    }
  }
})
