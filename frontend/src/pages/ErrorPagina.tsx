import { Link, useRouteError } from 'react-router'
import { RUTAS } from '../lib/rutas'
import { ApiError, mensaje } from '../lib/http'
export function ErrorPagina() {
  const error = useRouteError()
  return (
    <div className="empty error-page">
      <h1>¡Ups! Algo salió mal.</h1>
      <p role="alert">
        {error instanceof ApiError
          ? mensaje(error)
          : 'No pudimos cargar esta página. Inténtalo de nuevo.'}
      </p>
      <button className="btn bg-marca-600 text-white" onClick={() => window.location.reload()}>
        Reintentar
      </button>
      <Link className="link" to={RUTAS.inicio}>
        Volver al inicio
      </Link>
    </div>
  )
}
