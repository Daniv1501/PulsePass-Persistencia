import { http } from '../../lib/http'
import * as t from '../tipos'
const e = encodeURIComponent
export const usuariosApi = {
  usuario: (email: string) => http(`/users/by-email?email=${e(email)}`, t.Usuario),
  usuarioNombre: (username: string) =>
    http(`/users/by-username?username=${e(username)}`, t.Usuario),
}
