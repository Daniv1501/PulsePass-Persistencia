import { expect, it, vi } from 'vitest'
import { screen, waitFor, render } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createMemoryRouter } from 'react-router'
import { RouterProvider } from 'react-router/dom'
import { server } from './setup'
import { renderPage, renderRoutes } from './render'
import { principal, usuario, evento } from './fixtures'
import { rutas } from '../rutasRouter'
import { Login, Registro } from '../pages/Acceso'
import { AuthProvider, useAuth } from '../auth/AuthContext'
import { RutaProtegida } from '../auth/Guardas'
import type { Rol } from '../features/tipos'
function guardar() {
  sessionStorage.setItem(
    'pulsepass.sesion',
    JSON.stringify({ token: 'token-test', expiraEn: Date.now() + 900000 }),
  )
}
it('contraseña de registro muestra fortaleza y permite revelar sin enviar el formulario', async () => {
  const registrar = vi.fn()
  server.use(
    http.post('/api/users', () => {
      registrar()
      return HttpResponse.json(usuario)
    }),
  )
  renderPage(<Registro />)
  const campo = screen.getByLabelText('Contraseña')
  expect(campo).toHaveAttribute('type', 'password')
  await userEvent.type(campo, 'UnaClave1234')
  expect(screen.getByText('Fortaleza: media')).toBeInTheDocument()
  await userEvent.click(screen.getByRole('button', { name: 'Mostrar contraseña' }))
  expect(campo).toHaveAttribute('type', 'text')
  expect(campo).toHaveValue('UnaClave1234')
  await userEvent.click(screen.getByRole('button', { name: 'Ocultar contraseña' }))
  expect(campo).toHaveAttribute('type', 'password')
  expect(registrar).not.toHaveBeenCalled()
})
it('login vacío no envía una petición', async () => {
  const request = vi.fn()
  server.use(
    http.post('/api/auth/login', () => {
      request()
      return HttpResponse.json({})
    }),
  )
  renderPage(<Login />)
  await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }))
  expect(await screen.findAllByRole('alert')).toHaveLength(2)
  expect(request).not.toHaveBeenCalled()
})
it('login 401 muestra credenciales inválidas', async () => {
  server.use(http.post('/api/auth/login', () => HttpResponse.json({}, { status: 401 })))
  renderPage(<Login />)
  await userEvent.type(screen.getByLabelText('Usuario o correo electrónico'), 'andrea')
  await userEvent.type(screen.getByLabelText('Contraseña'), 'incorrecta')
  await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('Credenciales inválidas')
})
it('login válido guarda sesión y token solo en sessionStorage', async () => {
  server.use(
    http.post('/api/auth/login', () =>
      HttpResponse.json({ accessToken: 'token-test', tokenType: 'Bearer', expiresIn: 900 }),
    ),
    http.get('/api/auth/me', () => HttpResponse.json(principal)),
    http.get('/api/events/CMF-2026', () => HttpResponse.json(evento)),
  )
  const { router } = renderRoutes(rutas, '/login', { desde: '/eventos/CMF-2026' })
  await userEvent.type(await screen.findByLabelText('Usuario o correo electrónico'), 'andrea')
  await userEvent.type(screen.getByLabelText('Contraseña'), 'UnaContraseña123')
  await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }))
  await waitFor(() => expect(sessionStorage.getItem('pulsepass.sesion')).toContain('token-test'))
  expect(localStorage.getItem('pulsepass.sesion')).toBeNull()
  await waitFor(() => expect(router.state.location.pathname).toBe('/eventos/CMF-2026'))
})
async function rellenarRegistro(password = 'UnaContraseña123') {
  await userEvent.type(screen.getByLabelText('Nombres'), 'Andrea')
  await userEvent.type(screen.getByLabelText('Apellidos'), 'Ruiz')
  await userEvent.type(screen.getByLabelText('Usuario'), 'andrea')
  await userEvent.type(screen.getByLabelText('Correo electrónico'), 'andrea@pulsepass.test')
  await userEvent.type(screen.getByLabelText('Fecha de nacimiento'), '1998-04-12')
  await userEvent.type(screen.getByLabelText('Contraseña'), password)
  await userEvent.click(screen.getByRole('button', { name: 'Crear cuenta' }))
}
it('registro rechaza contraseña corta sin HTTP', async () => {
  const request = vi.fn()
  server.use(
    http.post('/api/users', () => {
      request()
      return HttpResponse.json(usuario)
    }),
  )
  renderPage(<Registro />)
  await rellenarRegistro('corta')
  expect(await screen.findByRole('alert')).toHaveTextContent('12 caracteres')
  expect(request).not.toHaveBeenCalled()
})
it('registro exitoso navega a login', async () => {
  server.use(http.post('/api/users', () => HttpResponse.json(usuario, { status: 201 })))
  const { router } = renderPage(<Registro />)
  await rellenarRegistro()
  await waitFor(() => expect(router.state.location.pathname).toBe('/login'))
})
it('registro duplicado no revela detalles internos', async () => {
  server.use(
    http.post('/api/users', () =>
      HttpResponse.json(
        {
          timestamp: '2026-11-16',
          status: 409,
          error: 'Conflict',
          message: 'INTERNAL',
          details: {},
        },
        { status: 409 },
      ),
    ),
  )
  renderPage(<Registro />)
  await rellenarRegistro()
  expect(await screen.findByRole('alert')).toHaveTextContent(
    'El usuario o correo ya está registrado',
  )
  expect(screen.queryByText('INTERNAL')).not.toBeInTheDocument()
})
function renderGuarda(roles: Rol[], rol?: Rol) {
  if (rol) {
    guardar()
    server.use(http.get('/api/auth/me', () => HttpResponse.json({ ...principal, roles: [rol] })))
  }
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  const router = createMemoryRouter(
    [
      {
        element: <RutaProtegida roles={roles} />,
        children: [{ path: '/admin', element: <h1>Panel admin</h1> }],
      },
      { path: '/login', element: <h1>Ingresar</h1> },
      { path: '/403', element: <h1>Sin permisos</h1> },
    ],
    { initialEntries: ['/admin'] },
  )
  render(
    <QueryClientProvider client={client}>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </QueryClientProvider>,
  )
  return router
}
it('anónimo se redirige a login conservando origen', async () => {
  const router = renderGuarda(['ADMIN'])
  await screen.findByRole('heading', { name: 'Ingresar' })
  expect(router.state.location.state.desde).toBe('/admin')
})
it.each(['USER', 'STAFF'] as const)('%s no entra a admin', async (rol) => {
  renderGuarda(['ADMIN'], rol)
  expect(await screen.findByRole('heading', { name: 'Sin permisos' })).toBeInTheDocument()
})
it('ADMIN entra al panel', async () => {
  renderGuarda(['ADMIN'], 'ADMIN')
  expect(await screen.findByRole('heading', { name: 'Panel admin' })).toBeInTheDocument()
})
it('token expirado no restaura sesión', async () => {
  sessionStorage.setItem('pulsepass.sesion', JSON.stringify({ token: 'expirado', expiraEn: 0 }))
  renderGuarda(['ADMIN'])
  expect(await screen.findByRole('heading', { name: 'Ingresar' })).toBeInTheDocument()
  expect(sessionStorage.getItem('pulsepass.sesion')).toBeNull()
})
it('cerrar sesión elimina token y caché', async () => {
  guardar()
  server.use(http.get('/api/auth/me', () => HttpResponse.json(principal)))
  function Salir() {
    const { sesion, cerrarSesion } = useAuth()
    return sesion.estado === 'autenticado' ? (
      <button onClick={cerrarSesion}>Salir</button>
    ) : (
      <p>Sin sesión</p>
    )
  }
  const { client } = renderPage(<Salir />)
  await screen.findByRole('button', { name: 'Salir' })
  client.setQueryData(['privado'], { dato: 'secreto' })
  await userEvent.click(screen.getByRole('button', { name: 'Salir' }))
  expect(sessionStorage.getItem('pulsepass.sesion')).toBeNull()
  expect(client.getQueryData(['privado'])).toBeUndefined()
})
