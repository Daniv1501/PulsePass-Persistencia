import { z } from 'zod'
export const TipoTicket = z.enum(['GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT'])
export const EstadoTicket = z.enum(['RESERVED', 'PAID', 'CANCELLED', 'USED'])
export const Ticket = z.object({
  id: z.number(),
  ticketCode: z.string(),
  type: TipoTicket,
  price: z.number(),
  status: EstadoTicket,
  purchaseDate: z.iso.datetime({ local: true }),
  userEmail: z.email(),
  eventCode: z.string(),
  eventName: z.string(),
})
export const Compra = z.object({ type: TipoTicket })
export type Ticket = z.infer<typeof Ticket>
