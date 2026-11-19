import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { server } from './setup'
import { renderPage } from './render'
import { artista, evento, principal, sede, ticket, usuario } from './fixtures'
import Catalogo from '../pages/Catalogo'
import Inicio from '../pages/Inicio'
import DetalleEvento from '../pages/DetalleEvento'
import { DetalleArtista, DetalleSede, ListaArtistas, ListaSedes } from '../pages/Directorio'
import Cuenta from '../pages/Cuenta'
import { ConfirmacionCompra, DetalleTicket, MisTickets } from '../pages/Tickets'
import { AdminCrearEvento, AdminGestionEvento } from '../pages/Admin'
import { TicketsPagadosEvento } from '../pages/Staff'

const paginas = [
  {
    nombre: 'Inicio',
    node: <Inicio />,
    path: '/',
    pattern: '/',
    endpoint: '/api/events/published',
    data: [evento],
    texto: evento.name,
    lista: true,
  },
  {
    nombre: 'Cartelera',
    node: <Catalogo />,
    path: '/eventos',
    pattern: '/eventos',
    endpoint: '/api/events/published',
    data: [evento],
    texto: evento.name,
    lista: true,
  },
  {
    nombre: 'Evento',
    node: <DetalleEvento />,
    path: '/eventos/CMF-2026',
    pattern: '/eventos/:eventCode',
    endpoint: '/api/events/CMF-2026',
    data: evento,
    texto: evento.name,
  },
  {
    nombre: 'Artistas',
    node: <ListaArtistas />,
    path: '/artistas',
    pattern: '/artistas',
    endpoint: '/api/artists/active',
    data: [artista],
    texto: artista.stageName,
    lista: true,
  },
  {
    nombre: 'Artista',
    node: <DetalleArtista />,
    path: '/artistas/1',
    pattern: '/artistas/:id',
    endpoint: '/api/artists/1',
    data: artista,
    texto: artista.stageName,
  },
  {
    nombre: 'Sedes',
    node: <ListaSedes />,
    path: '/sedes',
    pattern: '/sedes',
    endpoint: '/api/venues/active',
    data: [sede],
    texto: sede.name,
    lista: true,
  },
  {
    nombre: 'Sede',
    node: <DetalleSede />,
    path: '/sedes/VEN-SMR-01',
    pattern: '/sedes/:code',
    endpoint: '/api/venues/VEN-SMR-01',
    data: sede,
    texto: sede.name,
  },
  {
    nombre: 'Cuenta',
    node: <Cuenta />,
    path: '/cuenta',
    pattern: '/cuenta',
    endpoint: '/api/users/by-email',
    data: usuario,
    texto: 'Andrea Ruiz',
  },
  {
    nombre: 'Mis tickets',
    node: <MisTickets />,
    path: '/mis-tickets',
    pattern: '/mis-tickets',
    endpoint: '/api/tickets/by-user',
    data: [ticket],
    texto: ticket.ticketCode,
    lista: true,
  },
  {
    nombre: 'Ticket',
    node: <DetalleTicket />,
    path: '/mis-tickets/TKT-A',
    pattern: '/mis-tickets/:ticketCode',
    endpoint: '/api/tickets/TKT-A',
    data: ticket,
    texto: ticket.ticketCode,
  },
  {
    nombre: 'Confirmación',
    node: <ConfirmacionCompra />,
    path: '/compra/confirmacion/TKT-A',
    pattern: '/compra/confirmacion/:ticketCode',
    endpoint: '/api/tickets/TKT-A',
    data: ticket,
    texto: ticket.ticketCode,
  },
  {
    nombre: 'Pagados',
    node: <TicketsPagadosEvento />,
    path: '/staff/eventos/CMF-2026/tickets',
    pattern: '/staff/eventos/:eventCode/tickets',
    endpoint: '/api/events/CMF-2026/tickets/paid',
    data: [ticket],
    texto: ticket.ticketCode,
    lista: true,
  },
  {
    nombre: 'Crear evento',
    node: <AdminCrearEvento />,
    path: '/admin/eventos/nuevo',
    pattern: '/admin/eventos/nuevo',
    endpoint: '/api/venues/active',
    data: [sede],
    texto: 'Código del evento',
    formulario: true,
    lista: true,
  },
  {
    nombre: 'Gestión',
    node: <AdminGestionEvento />,
    path: '/admin/eventos/CMF-2026',
    pattern: '/admin/eventos/:eventCode',
    endpoint: '/api/events/CMF-2026',
    data: evento,
    texto: evento.name,
  },
]
function preparar() {
  sessionStorage.setItem(
    'pulsepass.sesion',
    JSON.stringify({ token: 'fixture', expiraEn: Date.now() + 900000 }),
  )
  server.use(
    http.get('/api/auth/me', () => HttpResponse.json(principal)),
    http.get('/api/artists/active', () => HttpResponse.json([artista])),
    http.get('/api/events/by-artist', () => HttpResponse.json([])),
  )
}
describe.each(paginas)('Estados de $nombre', (pagina) => {
  it('cargando', async () => {
    preparar()
    server.use(http.get(pagina.endpoint, () => new Promise(() => {})))
    renderPage(pagina.node, pagina.path, pagina.pattern)
    expect((await screen.findAllByRole('status', { name: 'Cargando' })).length).toBeGreaterThan(0)
  })
  it('éxito', async () => {
    preparar()
    server.use(http.get(pagina.endpoint, () => HttpResponse.json(pagina.data)))
    renderPage(pagina.node, pagina.path, pagina.pattern)
    if ('formulario' in pagina)
      expect(await screen.findByLabelText(pagina.texto)).toBeInTheDocument()
    else expect((await screen.findAllByText(pagina.texto)).length).toBeGreaterThan(0)
  })
  it('error con reintento', async () => {
    preparar()
    server.use(http.get(pagina.endpoint, () => HttpResponse.json({}, { status: 500 })))
    renderPage(pagina.node, pagina.path, pagina.pattern)
    expect(await screen.findByRole('alert')).toHaveTextContent('Ocurrió un error inesperado')
    expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument()
  })
  if ('lista' in pagina)
    it('vacío', async () => {
      preparar()
      server.use(http.get(pagina.endpoint, () => HttpResponse.json([])))
      renderPage(pagina.node, pagina.path, pagina.pattern)
      expect(await screen.findByRole('link', { name: 'Explorar cartelera →' })).toBeInTheDocument()
    })
})
