import { z } from 'zod'
export const Codigo = z.string().trim().min(1).max(255)
export const Buscar = z.object({ code: Codigo })
