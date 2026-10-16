import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5174,
    proxy: {
      // In dev, proxy /api to the Spring Boot backend
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
      }
    }
  }
})
