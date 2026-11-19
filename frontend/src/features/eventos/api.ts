import { http } from '../../lib/http'
import * as t from '../tipos'
import { z } from 'zod'
const e = encodeURIComponent
const body = (data: unknown, method = 'POST') => ({ method, body: JSON.stringify(data) })
export const eventosApi = {
  eventos: () => http('/events/published', t.ResumenEvento.array()),
  evento: (c: string) => http(`/events/${e(c)}`, t.Evento),
  porArtista: (name: string) =>
    http(`/events/by-artist?stageName=${e(name)}`, t.ResumenEvento.array()),
  crear: (data: z.infer<typeof t.CrearEvento>) => http('/events', t.Evento, body(data)),
  publicar: (code: string) => http(`/events/${e(code)}/publish`, t.Evento, { method: 'PATCH' }),
  asociar: (code: string, id: number) =>
    http(`/events/${e(code)}/artists/${id}`, t.Evento, { method: 'POST' }),
}
