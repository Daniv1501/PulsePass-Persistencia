import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from './AuthContext'
import { RUTAS } from '../lib/rutas'
import { Cargando } from '../componentes/ui'
import type { Rol } from '../features/tipos'
import { destinoTrasLogin } from '../lib/navegacion'
export function RutaProtegida({ roles }: { roles?: Rol[] }) {
  const { sesion, tieneRol } = useAuth()
  const location = useLocation()
  if (sesion.estado === 'cargando') return <Cargando />
  if (sesion.estado === 'anonimo')
    return (
      <Navigate
        to={RUTAS.login}
        replace
        state={{
          desde: location.pathname + location.search,
          motivo: sesion.expirada ? 'expirada' : undefined,
        }}
      />
    )
  if (roles && !tieneRol(...roles)) return <Navigate to={RUTAS.sinPermisos} replace />
  return <Outlet />
}
export function SoloAnonimo() {
  const { sesion } = useAuth()
  const location = useLocation()
  if (sesion.estado === 'cargando') return <Cargando />
  return sesion.estado === 'autenticado' ? (
    <Navigate to={destinoTrasLogin(location.state)} replace />
  ) : (
    <Outlet />
  )
}
export function RutaPorRol({ roles }: { roles: Rol[] }) {
  return <RutaProtegida roles={roles} />
}
