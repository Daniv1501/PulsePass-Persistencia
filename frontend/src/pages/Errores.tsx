import { Link } from 'react-router'
import { ShieldX, Compass } from 'lucide-react'
import { RUTAS } from '../lib/rutas'
export function SinPermisos() {
  return (
    <div className="empty error-page">
      <ShieldX size={60} />
      <p className="error-code">403</p>
      <h1>No tienes permisos</h1>
      <p className="muted">No tienes permisos para acceder a esta sección.</p>
      <Link className="btn bg-marca-600 text-white" to={RUTAS.inicio}>
        Volver al inicio
      </Link>
    </div>
  )
}
export function NoEncontrada() {
  return (
    <div className="empty error-page">
      <Compass size={60} />
      <p className="error-code">404</p>
      <h1>No encontramos esta página</h1>
      <p className="muted">Tu próxima experiencia te está esperando en otro lugar.</p>
      <Link className="btn bg-marca-600 text-white" to={RUTAS.inicio}>
        Volver al inicio
      </Link>
    </div>
  )
}
