import { expect, it, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from './setup'
import { renderPage } from './render'
import { artista, principal, sede, ticket } from './fixtures'
import { ListaArtistas, ListaSedes } from '../pages/Directorio'
import { MisTickets, TarjetaTicket } from '../pages/Tickets'
import { AdminCrearEvento } from '../pages/Admin'
import { ValidacionIngreso } from '../pages/Staff'
import { NoEncontrada, SinPermisos } from '../pages/Errores'
function sesion() {
  sessionStorage.setItem(
    'pulsepass.sesion',
    JSON.stringify({ token: 'token', expiraEn: Date.now() + 900000 }),
  )
  server.use(http.get('/api/auth/me', () => HttpResponse.json(principal)))
}
it('lista artistas activos', async () => {
  server.use(http.get('/api/artists/active', () => HttpResponse.json([artista])))
  renderPage(<ListaArtistas />)
  expect(await screen.findByRole('link', { name: /Solar Beat/ })).toBeInTheDocument()
})
it('lista sedes con ciudad y capacidad', async () => {
  server.use(http.get('/api/venues/active', () => HttpResponse.json([sede])))
  renderPage(<ListaSedes />)
  expect(await screen.findByText('Santa Marta')).toBeInTheDocument()
  expect(screen.getByText('3 personas')).toBeInTheDocument()
})
it('mis tickets usa email de sesión y filtra por estado', async () => {
  sesion()
  server.use(
    http.get('/api/tickets/by-user', ({ request }) => {
      expect(new URL(request.url).searchParams.get('email')).toBe(principal.email)
      return HttpResponse.json([ticket])
    }),
  )
  renderPage(<MisTickets />)
  await screen.findByText(ticket.ticketCode)
  await userEvent.click(screen.getByRole('button', { name: 'Usados' }))
  expect(screen.getByText('No hay tickets con este estado.')).toBeInTheDocument()
})
it('mis tickets vacío', async () => {
  sesion()
  server.use(http.get('/api/tickets/by-user', () => HttpResponse.json([])))
  renderPage(<MisTickets />)
  expect(await screen.findByText('Aún no tienes tickets')).toBeInTheDocument()
})
it('no ofrece cancelar un ticket usado', () => {
  renderPage(<TarjetaTicket ticket={{ ...ticket, status: 'USED', type: 'VIP' }} />)
  expect(screen.queryByRole('button', { name: 'Cancelar ticket' })).not.toBeInTheDocument()
})
it('cancelación 409 conserva modal y muestra mensaje', async () => {
  server.use(
    http.patch('/api/tickets/:code/cancel', () =>
      HttpResponse.json(
        {
          timestamp: '2026-11-16',
          status: 409,
          error: 'Conflict',
          message: 'El ticket ya fue utilizado.',
          details: {},
        },
        { status: 409 },
      ),
    ),
  )
  renderPage(<TarjetaTicket ticket={{ ...ticket, status: 'PAID', type: 'VIP' }} />)
  await userEvent.click(screen.getByRole('button', { name: 'Cancelar ticket' }))
  await userEvent.click(screen.getByRole('button', { name: 'Confirmar cancelación' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('El ticket ya fue utilizado.')
  expect(screen.getByRole('dialog')).toBeInTheDocument()
})
it('cancelación exitosa invalida la caché y cierra modal', async () => {
  server.use(
    http.patch('/api/tickets/:code/cancel', () =>
      HttpResponse.json({ ...ticket, status: 'CANCELLED' }),
    ),
  )
  const { client } = renderPage(
    <TarjetaTicket ticket={{ ...ticket, status: 'PAID', type: 'VIP' }} />,
  )
  client.setQueryData(['ticket', ticket.ticketCode], ticket)
  await userEvent.click(screen.getByRole('button', { name: 'Cancelar ticket' }))
  await userEvent.click(screen.getByRole('button', { name: 'Confirmar cancelación' }))
  await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
  expect(client.getQueryState(['ticket', ticket.ticketCode])?.isInvalidated).toBe(true)
})
it('crear evento vacío valida sin enviar HTTP', async () => {
  const enviar = vi.fn()
  server.use(
    http.post('/api/events', () => {
      enviar()
      return HttpResponse.json({})
    }),
  )
  server.use(http.get('/api/venues/active', () => HttpResponse.json([sede])))
  renderPage(<AdminCrearEvento />)
  await screen.findByLabelText('Código del evento')
  await userEvent.click(screen.getByRole('button', { name: 'Crear evento' }))
  expect(await screen.findAllByRole('alert')).not.toHaveLength(0)
  expect(enviar).not.toHaveBeenCalled()
})
it('staff busca ticket y registra ingreso', async () => {
  server.use(
    http.get('/api/tickets/:code', () => HttpResponse.json(ticket)),
    http.patch('/api/tickets/:code/use', () => HttpResponse.json({ ...ticket, status: 'USED' })),
  )
  renderPage(<ValidacionIngreso />)
  await userEvent.type(screen.getByLabelText('Código de ticket'), ticket.ticketCode)
  await userEvent.click(screen.getAllByRole('button', { name: 'Buscar' })[0])
  await screen.findByText(ticket.eventName)
  await userEvent.click(screen.getByRole('button', { name: 'Marcar como usado' }))
  expect(await screen.findByText('Ingreso registrado.')).toBeInTheDocument()
})
it('página 404 es navegable', () => {
  renderPage(<NoEncontrada />)
  expect(screen.getByRole('heading', { name: 'No encontramos esta página' })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: 'Volver al inicio' })).toHaveAttribute('href', '/')
})
it('página 403 explica permisos', () => {
  renderPage(<SinPermisos />)
  expect(screen.getByRole('heading', { name: 'No tienes permisos' })).toBeInTheDocument()
})
