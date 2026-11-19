import { expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from './setup'
import { renderPage } from './render'
import { evento } from './fixtures'
import Catalogo from '../pages/Catalogo'
it('muestra cartelera publicada', async () => {
  server.use(http.get('/api/events/published', () => HttpResponse.json([evento])))
  renderPage(<Catalogo />)
  expect(await screen.findByRole('heading', { name: evento.name })).toBeInTheDocument()
})
it('muestra estado vacío', async () => {
  server.use(http.get('/api/events/published', () => HttpResponse.json([])))
  renderPage(<Catalogo />)
  expect(
    await screen.findByRole('heading', { name: 'No hay eventos publicados' }),
  ).toBeInTheDocument()
})
it('conserva filtros de texto y categoría en URL', async () => {
  server.use(http.get('/api/events/published', () => HttpResponse.json([evento])))
  const { router } = renderPage(<Catalogo />)
  await screen.findByRole('heading', { name: evento.name })
  await userEvent.type(screen.getByLabelText('Buscar evento'), 'Caribbean')
  await userEvent.selectOptions(screen.getByLabelText('Categoría'), 'MUSIC')
  expect(router.state.location.search).toContain('q=Caribbean')
  expect(router.state.location.search).toContain('categoria=MUSIC')
})
it('muestra error de conexión y reintento', async () => {
  server.use(http.get('/api/events/published', () => HttpResponse.error()))
  renderPage(<Catalogo />)
  expect(await screen.findByRole('alert')).toHaveTextContent('No pudimos conectar')
  expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument()
})
it('muestra estado de carga', () => {
  server.use(http.get('/api/events/published', () => new Promise(() => {})))
  renderPage(<Catalogo />)
  expect(screen.getByRole('status', { name: 'Cargando' })).toBeInTheDocument()
})
