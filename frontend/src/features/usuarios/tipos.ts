import { z } from 'zod'
export const Usuario = z.object({
  id: z.number(),
  username: z.string(),
  email: z.email(),
  active: z.boolean(),
  firstName: z.string(),
  lastName: z.string(),
  phone: z.string().nullable(),
  city: z.string().nullable(),
  birthDate: z.iso.date().nullable(),
})
