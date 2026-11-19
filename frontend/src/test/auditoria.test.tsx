import { describe, expect, it, vi } from 'vitest'
import { act, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from './setup'
import { renderPage } from './render'
import { artista, evento, principal, ticket } from './fixtures'
import { reintentarConsulta } from '../lib/queryClient'
import { ApiError, mensaje } from '../lib/http'
import { Aviso, Boton } from '../componentes/ui'
import { useAccion } from '../features/hooks'
import { api } from '../features/api'
import { ValidacionIngreso } from '../pages/Staff'
import { AdminGestionEvento } from '../pages/Admin'
import { MisTickets } from '../pages/Tickets'
import Layout from '../componentes/Layout'

const conflicto = (message: string) =>
  HttpResponse.json(
    { timestamp: '2026-11-16', status: 409, error: 'Conflict', message, details: {} },
    { status: 409 },
  )
function autenticar(roles = ['USER']) {
  sessionStorage.setItem(
    'pulsepass.sesion',
    JSON.stringify({ token: 'fixture', expiraEn: Date.now() + 900000 }),
  )
  server.use(http.get('/api/auth/me', () => HttpResponse.json({ ...principal, roles })))
}
it.each([0, 500, 502, 503])('reintenta una vez error %s', (status) => {
  expect(reintentarConsulta(0, new ApiError(status, 'error'))).toBe(true)
  expect(reintentarConsulta(1, new ApiError(status, 'error'))).toBe(false)
})
it.each([400, 401, 403, 404, 409])('no reintenta error %s', (status) =>
  expect(reintentarConsulta(0, new ApiError(status, 'error'))).toBe(false),
)
it('no pierde detalles 400 desconocidos para el formulario', () =>
  expect(mensaje(new ApiError(400, 'Revisa', { otro: 'Detalle importante' }))).toContain(
    'Detalle importante',
  ))
it('éxito desaparece después de cuatro segundos', async () => {
  vi.useRealTimers()
  vi.useFakeTimers({ toFake: ['Date', 'setTimeout', 'clearTimeout'] })
  renderPage(<Aviso exito>Guardado</Aviso>)
  expect(screen.getByRole('status')).toHaveTextContent('Guardado')
  await act(async () => {
    vi.advanceTimersByTime(4000)
  })
  expect(screen.queryByText('Guardado')).not.toBeInTheDocument()
})
it('error persiste hasta que se cierra', async () => {
  renderPage(<Aviso>Rechazado</Aviso>)
  await userEvent.click(screen.getByRole('button', { name: 'Cerrar aviso' }))
  expect(screen.queryByRole('alert')).not.toBeInTheDocument()
})
describe.each([
  {
    nombre: 'comprar',
    ejecutar: () =>
      api.comprar({ userEmail: principal.email, eventCode: evento.eventCode, type: 'VIP' }),
    metodo: 'post' as const,
    url: '/api/tickets',
    respuesta: ticket,
  },
  {
    nombre: 'cancelar',
    ejecutar: () => api.cancelar(ticket.ticketCode),
    metodo: 'patch' as const,
    url: `/api/tickets/${ticket.ticketCode}/cancel`,
    respuesta: { ...ticket, status: 'CANCELLED' },
  },
  {
    nombre: 'usar',
    ejecutar: () => api.usar(ticket.ticketCode),
    metodo: 'patch' as const,
    url: `/api/tickets/${ticket.ticketCode}/use`,
    respuesta: { ...ticket, status: 'USED' },
  },
  {
    nombre: 'crear',
    ejecutar: () => api.crear({ ...evento, category: 'MUSIC' }),
    metodo: 'post' as const,
    url: '/api/events',
    respuesta: evento,
  },
  {
    nombre: 'publicar',
    ejecutar: () => api.publicar(evento.eventCode),
    metodo: 'patch' as const,
    url: `/api/events/${evento.eventCode}/publish`,
    respuesta: evento,
  },
  {
    nombre: 'asociar',
    ejecutar: () => api.asociar(evento.eventCode, 1),
    metodo: 'post' as const,
    url: `/api/events/${evento.eventCode}/artists/1`,
    respuesta: evento,
  },
])('Caché de $nombre', ({ nombre: operacion, ejecutar, metodo, url, respuesta }) => {
  it('invalida tickets, detalle y eventos después del éxito', async () => {
    server.use(http[metodo](url, () => HttpResponse.json(respuesta)))
    function Accion() {
      const action = useAccion(async () => ejecutar())
      return <Boton onClick={() => action.mutate(undefined)}>{operacion}</Boton>
    }
    const { client } = renderPage(<Accion />)
    for (const key of ['tickets', 'ticket', 'evento', 'eventos'])
      client.setQueryData([key, 'prueba'], [])
    await userEvent.click(screen.getByRole('button', { name: operacion }))
    await waitFor(() =>
      expect(client.getQueryState(['eventos', 'prueba'])?.isInvalidated).toBe(true),
    )
    for (const key of ['tickets', 'ticket', 'evento'])
      expect(client.getQueryState([key, 'prueba'])?.isInvalidated).toBe(true)
    if ('venueCode' in respuesta)
      expect(client.getQueryData(['evento', evento.eventCode])).toEqual(evento)
  })
})
it('cancelación actualiza la lista con el estado del servidor', async () => {
  autenticar()
  let status = 'PAID'
  server.use(
    http.get('/api/tickets/by-user', () => HttpResponse.json([{ ...ticket, status }])),
    http.patch('/api/tickets/:code/cancel', () => {
      status = 'CANCELLED'
      return HttpResponse.json({ ...ticket, status })
    }),
  )
  renderPage(<MisTickets />)
  await userEvent.click(await screen.findByRole('button', { name: 'Cancelar ticket' }))
  await userEvent.click(screen.getByRole('button', { name: 'Confirmar cancelación' }))
  expect(await screen.findByText('CANCELLED')).toBeInTheDocument()
  expect(screen.queryByRole('button', { name: 'Cancelar ticket' })).not.toBeInTheDocument()
})
it('STAFF recibe 409 al repetir ingreso y conserva el mensaje', async () => {
  let usado = false
  server.use(
    http.get('/api/tickets/:code', () =>
      HttpResponse.json({ ...ticket, status: usado ? 'USED' : 'PAID' }),
    ),
    http.patch('/api/tickets/:code/use', () => {
      if (usado) return conflicto('El ticket ya fue utilizado.')
      usado = true
      return HttpResponse.json({ ...ticket, status: 'USED' })
    }),
  )
  renderPage(<ValidacionIngreso />)
  await userEvent.type(screen.getByLabelText('Código de ticket'), ticket.ticketCode)
  await userEvent.click(screen.getAllByRole('button', { name: 'Buscar' })[0])
  await userEvent.click(await screen.findByRole('button', { name: 'Marcar como usado' }))
  await screen.findByText('Ingreso registrado.')
  await userEvent.click(screen.getByRole('button', { name: 'Marcar como usado' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('El ticket ya fue utilizado.')
})
it('STAFF no revela datos de ticket inexistente', async () => {
  server.use(http.get('/api/tickets/:code', () => HttpResponse.json({}, { status: 404 })))
  renderPage(<ValidacionIngreso />)
  await userEvent.type(screen.getByLabelText('Código de ticket'), 'NO-EXISTE')
  await userEvent.click(screen.getAllByRole('button', { name: 'Buscar' })[0])
  expect(await screen.findByRole('alert')).toHaveTextContent('Ticket no encontrado')
})
it('asociación actualiza artistas y muestra 409 si se repite', async () => {
  let asociado = false
  server.use(
    http.get('/api/events/CMF-2026', () =>
      HttpResponse.json({ ...evento, artists: asociado ? [artista.stageName] : [] }),
    ),
    http.get('/api/artists/active', () => HttpResponse.json([artista])),
    http.post('/api/events/CMF-2026/artists/1', () => {
      if (asociado) return conflicto('El artista ya está asociado.')
      asociado = true
      return HttpResponse.json(evento)
    }),
  )
  renderPage(<AdminGestionEvento />, '/admin/eventos/CMF-2026', '/admin/eventos/:eventCode')
  await userEvent.selectOptions(await screen.findByLabelText('Asociar artista'), '1')
  await userEvent.click(screen.getByRole('button', { name: 'Asociar artista' }))
  expect(await screen.findByText('Artista asociado.')).toBeInTheDocument()
  expect(screen.queryByText('Aún no hay artistas asociados.')).not.toBeInTheDocument()
  await userEvent.click(screen.getByRole('button', { name: 'Asociar artista' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('El artista ya está asociado.')
})
it.each(['USER', 'STAFF', 'ADMIN'])('navegación muestra enlaces del rol %s', async (rol) => {
  autenticar([rol])
  renderPage(<Layout />)
  await screen.findByRole('link', { name: principal.username })
  expect(screen.queryByRole('link', { name: 'Administrar' })).toBe(
    rol === 'ADMIN' ? screen.getByRole('link', { name: 'Administrar' }) : null,
  )
  expect(!!screen.queryByRole('link', { name: 'Validar ingreso' })).toBe(rol !== 'USER')
  expect(!!screen.queryByRole('link', { name: 'Mis tickets' })).toBe(rol === 'USER')
})
it('menú responde a Escape y devuelve el foco al botón', async () => {
  renderPage(<Layout />)
  const boton = screen.getByRole('button', { name: 'Abrir menú' })
  boton.focus()
  await userEvent.keyboard('{Enter}')
  expect(boton).toHaveAttribute('aria-expanded', 'true')
  screen.getByRole('link', { name: 'Eventos' }).focus()
  await userEvent.keyboard('{Escape}')
  expect(boton).toHaveFocus()
  expect(boton).toHaveAttribute('aria-expanded', 'false')
})
