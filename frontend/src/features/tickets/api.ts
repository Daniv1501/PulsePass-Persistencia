import { http } from '../../lib/http'
import * as t from '../tipos'
import { z } from 'zod'
const e = encodeURIComponent
const body = (data: unknown, method = 'POST') => ({ method, body: JSON.stringify(data) })
export const ticketsApi = {
  tickets: (email: string) => http(`/tickets/by-user?email=${e(email)}`, t.Ticket.array()),
  ticket: (code: string) => http(`/tickets/${e(code)}`, t.Ticket),
  comprar: (data: { userEmail: string; eventCode: string; type: z.infer<typeof t.TipoTicket> }) =>
    http('/tickets', t.Ticket, body(data)),
  cancelar: (code: string) => http(`/tickets/${e(code)}/cancel`, t.Ticket, { method: 'PATCH' }),
  usar: (code: string) => http(`/tickets/${e(code)}/use`, t.Ticket, { method: 'PATCH' }),
  pagados: (code: string) => http(`/events/${e(code)}/tickets/paid`, t.Ticket.array()),
}
