import { z } from 'zod'
export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public details: Record<string, string> = {},
  ) {
    super(message)
  }
}
const ErrorResponse = z.object({
  timestamp: z.string(),
  status: z.number(),
  error: z.string(),
  message: z.string(),
  details: z.record(z.string(), z.string()).nullish(),
})
let token: string | null = null
let onUnauthorized: () => void = () => {}
export function configurarSesion(value: string | null, callback?: () => void) {
  token = value
  if (callback) onUnauthorized = callback
}
export const mensaje = (error: unknown) => {
  if (!(error instanceof ApiError)) return 'Ocurrió un error inesperado. Inténtalo más tarde.'
  return error.status === 400 && Object.keys(error.details).length
    ? `${error.message} ${Object.values(error.details).join('. ')}`
    : error.message
}
export async function http<T>(
  path: string,
  schema: z.ZodType<T>,
  options: RequestInit = {},
): Promise<T> {
  const headers = new Headers(options.headers)
  headers.set('Accept', 'application/json')
  if (options.body) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  let response: Response
  try {
    response = await fetch(`${import.meta.env.VITE_API_BASE_URL || '/api'}${path}`, {
      ...options,
      headers,
    })
  } catch {
    throw new ApiError(0, 'No pudimos conectar con el servidor.')
  }
  const body: unknown = await response.json().catch(() => null)
  if (!response.ok) {
    const parsed = ErrorResponse.safeParse(body)
    if (response.status === 401 && path !== '/auth/login') onUnauthorized()
    const fallback: Record<number, string> = {
      400: 'Revisa los datos del formulario.',
      401: path === '/auth/login' ? 'Credenciales inválidas' : 'Tu sesión expiró',
      403: 'No tienes permisos para esta acción.',
      404: 'Recurso no disponible',
      409: 'No se pudo completar la operación.',
      500: 'Ocurrió un error inesperado. Inténtalo más tarde.',
    }
    const safeMessage =
      parsed.success && [400, 409].includes(response.status)
        ? parsed.data.message
        : fallback[response.status] || fallback[500]
    throw new ApiError(
      response.status,
      safeMessage,
      parsed.success ? parsed.data.details || {} : {},
    )
  }
  const parsed = schema.safeParse(body)
  if (!parsed.success)
    throw new ApiError(502, 'La respuesta del servidor no tiene el formato esperado.')
  return parsed.data
}
