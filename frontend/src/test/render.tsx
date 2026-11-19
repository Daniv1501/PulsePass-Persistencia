import type { ReactNode } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createMemoryRouter, type RouteObject } from 'react-router'
import { RouterProvider } from 'react-router/dom'
import { render } from '@testing-library/react'
import { AuthProvider } from '../auth/AuthContext'
import { NotificacionesProvider } from '../componentes/Notificaciones'
export function renderPage(node: ReactNode, path = '/', pattern = '*') {
  return renderRoutes(
    [
      { path: pattern, element: node },
      { path: '*', element: <p>Destino de navegación</p> },
    ],
    path,
  )
}
export function renderRoutes(routes: RouteObject[], path = '/', state?: unknown) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  const router = createMemoryRouter(routes, { initialEntries: [{ pathname: path, state }] })
  const result = render(
    <QueryClientProvider client={client}>
      <AuthProvider>
        <NotificacionesProvider>
          <RouterProvider router={router} />
        </NotificacionesProvider>
      </AuthProvider>
    </QueryClientProvider>,
  )
  return { ...result, client, router }
}
