import { expect, it, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from './setup'
import { renderPage } from './render'
import { artista, evento, principal, sede, ticket } from './fixtures'
import DetalleEvento from '../pages/DetalleEvento'
import { DetalleArtista, DetalleSede } from '../pages/Directorio'
import { AdminCrearEvento, AdminGestionEvento } from '../pages/Admin'
import { DetalleTicket, ConfirmacionCompra } from '../pages/Tickets'
import { TicketsPagadosEvento } from '../pages/Staff'
const error = (status: number, message: string) =>
  HttpResponse.json(
    { timestamp: '2026-11-16', status, error: 'Error', message, details: {} },
    { status },
  )
function login() {
  sessionStorage.setItem(
    'pulsepass.sesion',
    JSON.stringify({ token: 'token', expiraEn: Date.now() + 900000 }),
  )
  server.use(http.get('/api/auth/me', () => HttpResponse.json(principal)))
}
function eventoAPI(status = 'PUBLISHED') {
  server.use(http.get('/api/events/CMF-2026', () => HttpResponse.json({ ...evento, status })))
}
it('detalle muestra evento, sede y artistas', async () => {
  eventoAPI()
  renderPage(<DetalleEvento />, '/eventos/CMF-2026', '/eventos/:eventCode')
  expect(await screen.findByRole('heading', { name: evento.name })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: sede.name })).toBeInTheDocument()
  expect(screen.getByRole('link', { name: artista.stageName })).toBeInTheDocument()
})
it('evento inexistente muestra no disponible', async () => {
  server.use(http.get('/api/events/CMF-2026', () => error(404, 'No existe')))
  renderPage(<DetalleEvento />, '/eventos/CMF-2026', '/eventos/:eventCode')
  expect(await screen.findByRole('alert')).toHaveTextContent('Evento no disponible')
})
it.each(['SOLD_OUT', 'CANCELLED', 'FINISHED'])('evento %s deshabilita compra', async (status) => {
  eventoAPI(status)
  renderPage(<DetalleEvento />, '/eventos/CMF-2026', '/eventos/:eventCode')
  expect(await screen.findByRole('button', { name: 'Comprar entrada' })).toBeDisabled()
})
it('comprador anónimo vuelve al evento después del login', async () => {
  eventoAPI()
  const { router } = renderPage(<DetalleEvento />, '/eventos/CMF-2026', '/eventos/:eventCode')
  await userEvent.click(await screen.findByRole('button', { name: 'Comprar entrada' }))
  await waitFor(() => expect(router.state.location.pathname).toBe('/login'))
  expect(router.state.location.state.desde).toBe('/eventos/CMF-2026')
})
it('compra envía identidad de sesión y navega a confirmación', async () => {
  login()
  eventoAPI()
  server.use(
    http.post('/api/tickets', async ({ request }) => {
      expect(await request.json()).toEqual({
        userEmail: principal.email,
        eventCode: evento.eventCode,
        type: 'VIP',
      })
      return HttpResponse.json(ticket, { status: 201 })
    }),
  )
  const { router } = renderPage(<DetalleEvento />, '/eventos/CMF-2026', '/eventos/:eventCode')
  await userEvent.click(await screen.findByRole('button', { name: 'Comprar entrada' }))
  await userEvent.selectOptions(screen.getByLabelText('Tipo de entrada'), 'VIP')
  expect(screen.queryByLabelText('Email del comprador')).not.toBeInTheDocument()
  await userEvent.click(screen.getByRole('button', { name: 'Confirmar compra' }))
  await waitFor(() =>
    expect(router.state.location.pathname).toBe('/compra/confirmacion/TKT-8F2A1C'),
  )
})
it('409 de compra se muestra dentro del modal', async () => {
  login()
  eventoAPI()
  server.use(http.post('/api/tickets', () => error(409, 'No cumple la edad mínima.')))
  renderPage(<DetalleEvento />, '/eventos/CMF-2026', '/eventos/:eventCode')
  await userEvent.click(await screen.findByRole('button', { name: 'Comprar entrada' }))
  await userEvent.click(screen.getByRole('button', { name: 'Confirmar compra' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('No cumple la edad mínima.')
  expect(screen.getByRole('dialog')).toBeInTheDocument()
})
it('artista de ID inválido no consulta API', () => {
  const request = vi.fn()
  server.use(
    http.get('/api/artists/:id', () => {
      request()
      return HttpResponse.json(artista)
    }),
  )
  renderPage(<DetalleArtista />, '/artistas/no-numero', '/artistas/:id')
  expect(screen.getByRole('heading', { name: '404 · Artista no encontrado' })).toBeInTheDocument()
  expect(request).not.toHaveBeenCalled()
})
it('detalle de artista carga sus eventos', async () => {
  server.use(
    http.get('/api/artists/1', () => HttpResponse.json(artista)),
    http.get('/api/events/by-artist', () => HttpResponse.json([evento])),
  )
  renderPage(<DetalleArtista />, '/artistas/1', '/artistas/:id')
  expect(await screen.findByRole('heading', { name: artista.stageName })).toBeInTheDocument()
  expect(await screen.findByRole('heading', { name: evento.name })).toBeInTheDocument()
})
it('detalle de sede carga su capacidad', async () => {
  server.use(http.get('/api/venues/VEN-SMR-01', () => HttpResponse.json(sede)))
  renderPage(<DetalleSede />, '/sedes/VEN-SMR-01', '/sedes/:code')
  expect(await screen.findByRole('heading', { name: sede.name })).toBeInTheDocument()
  expect(screen.getByText('3 personas de capacidad')).toBeInTheDocument()
})
it('sede inexistente presenta un mensaje seguro', async () => {
  server.use(http.get('/api/venues/NO-EXISTE', () => error(404, 'Interno')))
  renderPage(<DetalleSede />, '/sedes/NO-EXISTE', '/sedes/:code')
  expect(await screen.findByRole('alert')).toHaveTextContent('Sede no encontrada')
  expect(screen.queryByText('Interno')).not.toBeInTheDocument()
})
it.each([403, 404])('ticket %s no revela existencia', async (status) => {
  server.use(http.get('/api/tickets/:code', () => error(status, 'Privado')))
  renderPage(<DetalleTicket />, '/mis-tickets/TKT-8F2A1C', '/mis-tickets/:ticketCode')
  expect(await screen.findByRole('alert')).toHaveTextContent('Ticket no disponible')
  expect(screen.queryByText('Privado')).not.toBeInTheDocument()
})
it('confirmación obtiene precio del servidor', async () => {
  server.use(http.get('/api/tickets/:code', () => HttpResponse.json(ticket)))
  renderPage(
    <ConfirmacionCompra />,
    '/compra/confirmacion/TKT-8F2A1C',
    '/compra/confirmacion/:ticketCode',
  )
  expect(
    await screen.findByRole('heading', { name: '¡Compra realizada con éxito!' }),
  ).toBeInTheDocument()
  expect(screen.getByText(/250\.000/)).toBeInTheDocument()
})
it('STAFF lista tickets pagados de evento', async () => {
  server.use(http.get('/api/events/:code/tickets/paid', () => HttpResponse.json([ticket])))
  renderPage(
    <TicketsPagadosEvento />,
    '/staff/eventos/CMF-2026/tickets',
    '/staff/eventos/:eventCode/tickets',
  )
  expect(await screen.findByRole('cell', { name: ticket.ticketCode })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Usar' })).toBeInTheDocument()
})
async function rellenarEvento() {
  await userEvent.type(await screen.findByLabelText('Código del evento'), 'CMF-2026')
  await userEvent.type(screen.getByLabelText('Nombre'), 'Caribbean Music Fest 2026')
  await userEvent.type(screen.getByLabelText('Fecha y hora'), '2026-12-12T20:00')
  await userEvent.selectOptions(screen.getByLabelText('Sede'), sede.code)
  await userEvent.click(screen.getByRole('button', { name: 'Crear evento' }))
}
it('ADMIN crea evento y navega a gestión', async () => {
  server.use(
    http.get('/api/venues/active', () => HttpResponse.json([sede])),
    http.post('/api/events', () =>
      HttpResponse.json({ ...evento, status: 'DRAFT' }, { status: 201 }),
    ),
  )
  const { router } = renderPage(
    <AdminCrearEvento />,
    '/admin/eventos/nuevo',
    '/admin/eventos/nuevo',
  )
  await rellenarEvento()
  await waitFor(() => expect(router.state.location.pathname).toBe('/admin/eventos/CMF-2026'))
  expect(sessionStorage.getItem('pulsepass.creados')).toContain('CMF-2026')
})
it('ADMIN mapea errores 400 a campos', async () => {
  server.use(
    http.get('/api/venues/active', () => HttpResponse.json([sede])),
    http.post('/api/events', () =>
      HttpResponse.json(
        {
          timestamp: '2026-11-16',
          status: 400,
          error: 'Bad Request',
          message: 'Revisa los campos',
          details: { name: 'Nombre rechazado' },
        },
        { status: 400 },
      ),
    ),
  )
  renderPage(<AdminCrearEvento />)
  await rellenarEvento()
  expect(await screen.findByText('Nombre rechazado')).toBeInTheDocument()
  expect(screen.getByLabelText('Nombre')).toHaveAttribute('aria-invalid', 'true')
})
it('ADMIN publica y actualiza estado', async () => {
  let status = 'DRAFT'
  server.use(
    http.get('/api/events/CMF-2026', () => HttpResponse.json({ ...evento, status })),
    http.get('/api/artists/active', () => HttpResponse.json([artista])),
    http.patch('/api/events/CMF-2026/publish', () => {
      status = 'PUBLISHED'
      return HttpResponse.json({ ...evento, status })
    }),
  )
  renderPage(<AdminGestionEvento />, '/admin/eventos/CMF-2026', '/admin/eventos/:eventCode')
  await userEvent.click(await screen.findByRole('button', { name: 'Publicar evento' }))
  await userEvent.click(screen.getByRole('button', { name: 'Confirmar publicación' }))
  expect(await screen.findByText('PUBLISHED')).toBeInTheDocument()
})
it('ADMIN publicación 409 conserva confirmación', async () => {
  eventoAPI('DRAFT')
  server.use(
    http.get('/api/artists/active', () => HttpResponse.json([artista])),
    http.patch('/api/events/CMF-2026/publish', () => error(409, 'No se puede publicar.')),
  )
  renderPage(<AdminGestionEvento />, '/admin/eventos/CMF-2026', '/admin/eventos/:eventCode')
  await userEvent.click(await screen.findByRole('button', { name: 'Publicar evento' }))
  await userEvent.click(screen.getByRole('button', { name: 'Confirmar publicación' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('No se puede publicar.')
  expect(screen.getByRole('dialog')).toBeInTheDocument()
})
