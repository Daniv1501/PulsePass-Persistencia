import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './api'
import { useNotificar } from '../componentes/Notificaciones'
export const useEventos = (artista = '') =>
  useQuery({
    queryKey: artista ? ['eventos', 'artista', artista] : ['eventos', 'publicados'],
    queryFn: () => (artista ? api.porArtista(artista) : api.eventos()),
    staleTime: 60000,
  })
export const useEvento = (code: string) =>
  useQuery({
    queryKey: ['evento', code],
    queryFn: () => api.evento(code),
    enabled: !!code,
    staleTime: 30000,
  })
export const useArtistas = () =>
  useQuery({ queryKey: ['artistas', 'activos'], queryFn: api.artistas, staleTime: 300000 })
export const useSedes = () =>
  useQuery({ queryKey: ['sedes', 'activas'], queryFn: api.sedes, staleTime: 300000 })
export function useAccion<T, V>(fn: (value: V) => Promise<T>) {
  const client = useQueryClient()
  const notificar = useNotificar()
  return useMutation({
    mutationFn: fn,
    onSuccess: async (resultado) => {
      const evento = resultado as { eventCode?: unknown; status?: unknown; venueCode?: unknown }
      if (typeof evento?.eventCode === 'string' && typeof evento.venueCode === 'string') {
        client.setQueryData(['evento', evento.eventCode], resultado)
      }
      await Promise.all(
        ['tickets', 'ticket', 'evento', 'eventos'].map((key) =>
          client.invalidateQueries({ queryKey: [key] }),
        ),
      )
      notificar('Operación completada correctamente.')
    },
  })
}
