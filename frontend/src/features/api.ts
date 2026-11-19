import { eventosApi } from './eventos/api'
import { artistasApi } from './artistas/api'
import { sedesApi } from './sedes/api'
import { usuariosApi } from './usuarios/api'
import { authApi } from './auth/api'
import { ticketsApi } from './tickets/api'
export const api = {
  ...eventosApi,
  ...artistasApi,
  ...sedesApi,
  ...usuariosApi,
  ...authApi,
  ...ticketsApi,
}
