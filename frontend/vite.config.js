import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发代理:前端 5173 → 后端 8080,免 CORS 烦恼
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
