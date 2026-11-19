import { expect, it, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import type { RouteObject } from 'react-router'
import { server } from './setup'
import { renderRoutes } from './render'
import { principal } from './fixtures'
import { rutas } from '../rutasRouter'
import { ErrorPagina } from '../pages/ErrorPagina'
import { destinoTrasLogin } from '../lib/navegacion'

it.each(['https://externo.test', '//externo.test', '/\\externo.test', '/login', '/registro'])(
  'rechaza destino de retorno inseguro o circular %s',
  (desde) => {
    expect(destinoTrasLogin({ desde })).toBe('/cuenta')
  },
)

function paginas(config: RouteObject[]): RouteObject[] {
  return config.flatMap((route) => (route.children ? paginas(route.children) : [route]))
}
it('registra las 20 páginas y difiere todas salvo Inicio', () => {
  const pages = paginas(rutas)
  expect(pages).toHaveLength(20)
  expect(pages.filter((page) => !page.index).every((page) => typeof page.lazy === 'function')).toBe(
    true,
  )
  expect(pages.find((page) => page.index)?.Component).toBeDefined()
})
it('una URL desconocida llega a la página 404 del router real', async () => {
  renderRoutes(rutas, '/esta-ruta-no-existe')
  expect(
    await screen.findByRole('heading', { name: 'No encontramos esta página' }),
  ).toBeInTheDocument()
})
it('errorElement oculta mensajes internos y ofrece reintentar', async () => {
  vi.spyOn(console, 'error').mockImplementation(() => {})
  renderRoutes([
    {
      path: '/',
      element: <p>Página de prueba</p>,
      loader: () => {
        throw new Error('SQL_PASSWORD_SECRETO')
      },
      errorElement: <ErrorPagina />,
    },
  ])
  expect(await screen.findByRole('alert')).toHaveTextContent('No pudimos cargar esta página')
  expect(screen.queryByText(/SQL_PASSWORD_SECRETO/)).not.toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument()
})
it('401 durante una consulta limpia caché y conserva el destino para login', async () => {
  sessionStorage.setItem(
    'pulsepass.sesion',
    JSON.stringify({ token: 'fixture', expiraEn: Date.now() + 900000 }),
  )
  server.use(
    http.get('/api/auth/me', () => HttpResponse.json(principal)),
    http.get('/api/users/by-email', () => HttpResponse.json({}, { status: 401 })),
  )
  const { client, router } = renderRoutes(rutas, '/cuenta')
  client.setQueryData(['privado'], { dato: 'privado' })
  expect(await screen.findByRole('alert')).toHaveTextContent('Tu sesión expiró')
  await waitFor(() => expect(router.state.location.pathname).toBe('/login'))
  expect(router.state.location.state.desde).toBe('/cuenta')
  expect(client.getQueryData(['privado'])).toBeUndefined()
  expect(sessionStorage.getItem('pulsepass.sesion')).toBeNull()
})
