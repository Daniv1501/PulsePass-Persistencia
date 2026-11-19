import { z } from 'zod'
import { RUTAS } from './rutas'

export const EstadoNavegacion = z.object({
  desde: z
    .string()
    .refine(
      (value) =>
        value.startsWith('/') &&
        !value.startsWith('//') &&
        !value.includes('\\') &&
        [...value].every((char) => char.charCodeAt(0) >= 32),
    )
    .refine((value) => {
      const path = value.split(/[?#]/)[0]
      return path !== RUTAS.login && path !== RUTAS.registro
    })
    .optional(),
  motivo: z.string().optional(),
  registrado: z.boolean().optional(),
})

export function destinoTrasLogin(state: unknown) {
  const parsed = EstadoNavegacion.safeParse(state)
  return parsed.success ? parsed.data.desde || RUTAS.cuenta : RUTAS.cuenta
}
