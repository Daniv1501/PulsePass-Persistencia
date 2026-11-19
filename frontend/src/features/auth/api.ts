import { http } from '../../lib/http'
import * as t from '../tipos'
import { z } from 'zod'
const body = (data: unknown, method = 'POST') => ({ method, body: JSON.stringify(data) })
export const authApi = {
  login: (data: z.infer<typeof t.Login>) => http('/auth/login', t.RespuestaAuth, body(data)),
  me: () => http('/auth/me', t.Principal),
  registrar: (data: z.infer<typeof t.Registro>) => http('/users', t.Usuario, body(data)),
}
