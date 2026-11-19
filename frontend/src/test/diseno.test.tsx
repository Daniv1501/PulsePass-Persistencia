import { expect, it } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from './setup'
import { renderPage } from './render'
import { artista, evento } from './fixtures'
import BuscadorInicio from '../componentes/BuscadorInicio'
import Inicio from '../pages/Inicio'

it('el buscador de portada lleva el texto y la categoría a la cartelera', async () => {
  const { router } = renderPage(<BuscadorInicio />)
  await userEvent.type(screen.getByLabelText('¿Qué quieres vivir?'), 'Caribbean')
  await userEvent.selectOptions(screen.getByLabelText('Tu tipo de plan'), 'MUSIC')
  await userEvent.click(screen.getByRole('button', { name: 'Encontrar mi plan' }))
  await waitFor(() => expect(router.state.location.pathname).toBe('/eventos'))
  const params = new URLSearchParams(router.state.location.search)
  expect(params.get('q')).toBe('Caribbean')
  expect(params.get('categoria')).toBe('MUSIC')
})

it('una búsqueda vacía abre toda la cartelera', async () => {
  const { router } = renderPage(<BuscadorInicio />)
  await userEvent.click(screen.getByRole('button', { name: 'Encontrar mi plan' }))
  await waitFor(() => expect(router.state.location.pathname).toBe('/eventos'))
  expect(router.state.location.search).toBe('')
})

it('el evento destacado corresponde al catálogo real y enlaza a su detalle', async () => {
  server.use(
    http.get('/api/events/published', () => HttpResponse.json([evento])),
    http.get('/api/artists/active', () => HttpResponse.json([artista])),
  )
  renderPage(<Inicio />)
  expect(await screen.findByRole('link', { name: 'Quiero vivirlo' })).toHaveAttribute(
    'href',
    '/eventos/CMF-2026',
  )
  expect(screen.getByRole('search', { name: 'Encuentra tu próximo evento' })).toBeInTheDocument()
  expect(await screen.findByText('Santa Marta · Marina Convention Center')).toBeInTheDocument()
})
