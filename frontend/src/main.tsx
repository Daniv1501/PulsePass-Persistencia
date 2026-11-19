import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router/dom'
import { QueryClientProvider } from '@tanstack/react-query'
import { crearQueryClient } from './lib/queryClient'
import { NotificacionesProvider } from './componentes/Notificaciones'
import { AuthProvider } from './auth/AuthContext'
import { router } from './router'
import './index.css'
const theme = localStorage.getItem('pulsepass.tema')
document.documentElement.classList.toggle(
  'dark',
  theme === 'dark' || (!theme && matchMedia('(prefers-color-scheme: dark)').matches),
)
const client = crearQueryClient()
createRoot(document.getElementById('root')!).render(
  <QueryClientProvider client={client}>
    <AuthProvider>
      <NotificacionesProvider>
        <RouterProvider router={router} />
      </NotificacionesProvider>
    </AuthProvider>
  </QueryClientProvider>,
)
