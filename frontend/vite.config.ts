import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '')
  return {
    plugins: [react(), tailwindcss()],
    build: {
      rollupOptions: {
        output: {
          manualChunks(id) {
            if (!id.includes('/node_modules/')) return
            if (id.includes('/zod/')) return 'validacion'
            if (id.includes('/@tanstack/')) return 'consultas'
            if (id.includes('/react-router/')) return 'navegacion'
            if (id.includes('/react-hook-form/') || id.includes('/@hookform/')) return 'formularios'
            if (['/react/', '/react-dom/', '/scheduler/'].some((name) => id.includes(name)))
              return 'react'
          },
        },
      },
    },
    server: {
      proxy: {
        '/api': { target: env.API_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true },
      },
    },
    test: { environment: 'jsdom', setupFiles: './src/test/setup.ts', restoreMocks: true },
  }
})
