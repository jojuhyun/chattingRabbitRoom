import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      '/stomp': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true
      }
    },
    cors: true
  },
  optimizeDeps: {
    include: ['sockjs-client']
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    commonjsOptions: {
      include: [/sockjs-client/, /node_modules/]
    }
  }
})
