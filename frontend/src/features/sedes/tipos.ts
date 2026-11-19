import { z } from 'zod'
export const Sede = z.object({
  id: z.number().optional(),
  code: z.string(),
  name: z.string(),
  city: z.string(),
  address: z.string(),
  capacity: z.number(),
  active: z.boolean(),
})
