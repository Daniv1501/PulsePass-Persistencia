import { z } from 'zod'
export const Categoria = z.enum([
  'MUSIC',
  'SPORTS',
  'TECHNOLOGY',
  'EDUCATION',
  'CULTURE',
  'ENTERTAINMENT',
])
export const EstadoEvento = z.enum(['DRAFT', 'PUBLISHED', 'SOLD_OUT', 'CANCELLED', 'FINISHED'])
export const ResumenEvento = z.object({
  id: z.number(),
  eventCode: z.string(),
  name: z.string(),
  category: Categoria,
  status: EstadoEvento,
  eventDate: z.iso.datetime({ local: true }),
  venueName: z.string(),
})
export const Evento = ResumenEvento.extend({
  description: z.string().nullable(),
  minimumAge: z.number().int(),
  venueCode: z.string(),
  artists: z.array(z.string()),
})
export const CrearEvento = z.object({
  eventCode: z.string().trim().min(1, 'Código obligatorio').max(30),
  name: z.string().trim().min(1, 'Nombre obligatorio').max(150),
  description: z.string().max(500, 'Máximo 500 caracteres'),
  category: Categoria,
  eventDate: z.iso.datetime({ local: true, error: 'Fecha y hora incorrectas' }),
  minimumAge: z.number().int().min(0, 'La edad debe ser positiva o cero'),
  venueCode: z.string().min(1, 'Selecciona una sede').max(30),
})
export type Evento = z.infer<typeof Evento>
export type ResumenEvento = z.infer<typeof ResumenEvento>
