import { z } from 'zod'
export const Rol = z.enum(['USER', 'STAFF', 'ADMIN'])
export type Rol = z.infer<typeof Rol>
export const Principal = z.object({
  userId: z.number().int().positive(),
  username: z.string(),
  email: z.email(),
  roles: z.array(Rol),
})
export const RespuestaAuth = z.object({
  accessToken: z.string().min(1),
  tokenType: z.literal('Bearer'),
  expiresIn: z.number().positive(),
})
export const Login = z.object({
  identifier: z.string().trim().min(1, 'Ingresa tu usuario o correo'),
  password: z.string().min(1, 'Ingresa tu contraseña'),
})
export const Registro = z.object({
  username: z.string().trim().min(1, 'Ingresa un usuario').max(50),
  email: z.email('Correo inválido').max(150),
  firstName: z.string().trim().min(1, 'Ingresa tus nombres').max(80),
  lastName: z.string().trim().min(1, 'Ingresa tus apellidos').max(80),
  birthDate: z.iso
    .date('Fecha inválida')
    .refine((v) => v <= new Date().toLocaleDateString('en-CA'), 'La fecha no puede ser futura'),
  phone: z.string().max(30).optional(),
  password: z
    .string()
    .refine((v) => [...v].length >= 12, 'Usa al menos 12 caracteres')
    .refine((v) => new TextEncoder().encode(v).length <= 72, 'Máximo 72 bytes UTF-8'),
})
export type Principal = z.infer<typeof Principal>
