import { QueryClient } from '@tanstack/react-query'
import { ApiError } from './http'

export const reintentarConsulta = (intentos: number, error: Error) =>
  intentos < 1 && error instanceof ApiError && (error.status === 0 || error.status >= 500)

export function crearQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: reintentarConsulta, retryDelay: 300, refetchOnWindowFocus: false },
      mutations: { retry: false },
    },
  })
}
