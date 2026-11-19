import { describe, it, expect, vi, beforeEach } from 'vitest'
import { http as mockHttp, HttpResponse } from 'msw'
import { z } from 'zod'
import { server } from './setup'
import { ApiError, configurarSesion, http } from '../lib/http'
import { precioCOP } from '../lib/formato'
const errorBody = (status: number) => ({
  timestamp: '2026-11-16T12:00:00',
  status,
  error: 'Error',
  message: 'Mensaje del backend',
  details: { name: 'Campo requerido' },
})
beforeEach(() => configurarSesion(null, () => {}))
describe('cliente HTTP', () => {
  it('envía token solo por Authorization', async () => {
    configurarSesion('token-prueba')
    server.use(
      mockHttp.get('/api/test', ({ request }) => {
        expect(request.headers.get('Authorization')).toBe('Bearer token-prueba')
        expect(request.url).not.toContain('token-prueba')
        return HttpResponse.json({ ok: true })
      }),
    )
    expect(await http('/test', z.object({ ok: z.boolean() }))).toEqual({ ok: true })
  })
  it('conserva details y message de 400', async () => {
    server.use(mockHttp.get('/api/test', () => HttpResponse.json(errorBody(400), { status: 400 })))
    await expect(http('/test', z.unknown())).rejects.toMatchObject({
      status: 400,
      details: { name: 'Campo requerido' },
      message: 'Mensaje del backend',
    })
  })
  it.each([401, 403, 404, 409, 500])(
    'traduce estado %s sin revelar errores internos',
    async (status) => {
      server.use(mockHttp.get('/api/test', () => HttpResponse.json(errorBody(status), { status })))
      const error = await http('/test', z.unknown()).catch((e: unknown) => e)
      expect(error).toBeInstanceOf(ApiError)
      expect((error as ApiError).status).toBe(status)
      if (status !== 409) expect((error as ApiError).message).not.toBe('Mensaje del backend')
    },
  )
  it('maneja cuerpos inválidos con un error seguro', async () => {
    server.use(
      mockHttp.get('/api/test', () => new HttpResponse('<html>stack</html>', { status: 500 })),
    )
    await expect(http('/test', z.unknown())).rejects.toThrow('Ocurrió un error inesperado')
  })
  it('cierra sesión al recibir 401', async () => {
    const salir = vi.fn()
    configurarSesion('token', salir)
    server.use(mockHttp.get('/api/test', () => HttpResponse.json(errorBody(401), { status: 401 })))
    await http('/test', z.unknown()).catch(() => {})
    expect(salir).toHaveBeenCalledOnce()
  })
  it('401 en login muestra credenciales inválidas sin expirar otra sesión', async () => {
    const salir = vi.fn()
    configurarSesion(null, salir)
    server.use(
      mockHttp.post('/api/auth/login', () => HttpResponse.json(errorBody(401), { status: 401 })),
    )
    await expect(http('/auth/login', z.unknown(), { method: 'POST' })).rejects.toThrow(
      'Credenciales inválidas',
    )
    expect(salir).not.toHaveBeenCalled()
  })
  it('valida la respuesta exitosa con zod', async () => {
    server.use(mockHttp.get('/api/test', () => HttpResponse.json({ ok: 'no' })))
    await expect(http('/test', z.object({ ok: z.boolean() }))).rejects.toMatchObject({
      status: 502,
    })
  })
  it('maneja fallo de red', async () => {
    server.use(mockHttp.get('/api/test', () => HttpResponse.error()))
    await expect(http('/test', z.unknown())).rejects.toThrow('No pudimos conectar con el servidor.')
  })
  it('formatea COP', () => expect(precioCOP(250000)).toMatch(/250[.,]000/))
})
