import { z } from 'zod'
export const Artista = z.object({
  id: z.number(),
  stageName: z.string(),
  country: z.string().nullable(),
  genre: z.string().nullable(),
  active: z.boolean(),
})
