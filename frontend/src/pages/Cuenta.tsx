import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router'
import { useAuth } from '../auth/AuthContext'
import { api } from '../features/api'
import { RUTAS } from '../lib/rutas'
import { fecha } from '../lib/formato'
import { Boton, Cabecera, Datos } from '../componentes/ui'
export default function Cuenta() {
  const { sesion, cerrarSesion, tieneRol } = useAuth()
  const navigate = useNavigate()
  const [metodo, setMetodo] = useState<'email' | 'username'>('email')
  const user = sesion.estado === 'autenticado' ? sesion.usuario : null
  const query = useQuery({
    queryKey: ['usuario', user?.email, metodo],
    queryFn: () =>
      metodo === 'email' ? api.usuario(user?.email || '') : api.usuarioNombre(user?.username || ''),
    enabled: !!user,
    staleTime: 60000,
  })
  return (
    <>
      <Cabecera titulo="Mi cuenta" descripcion="Tu perfil, tus experiencias." />
      <Datos query={query}>
        {(data) => (
          <section className="card p-8">
            <div className="profile-banner">
              <span className="avatar">
                {data.firstName.slice(0, 1)}
                {data.lastName.slice(0, 1)}
              </span>
              <div>
                <h2>
                  {data.firstName} {data.lastName}
                </h2>
                <p className="muted mt-2">
                  @{data.username} · {data.email}
                </p>
                <p className="muted mt-2">
                  Nacimiento: {data.birthDate ? fecha(data.birthDate) : 'Sin registrar'}
                </p>
                <p className="mt-3">
                  {user?.roles.join(' · ')} · {data.active ? 'Activa' : 'Inactiva'}
                </p>
              </div>
            </div>
            <div className="mt-8 flex flex-wrap gap-3">
              {tieneRol('USER') && (
                <Link className="btn bg-marca-600 text-white" to={RUTAS.tickets}>
                  Ver mis tickets
                </Link>
              )}
              <Boton
                variante="secundario"
                onClick={() => setMetodo(metodo === 'email' ? 'username' : 'email')}
              >
                Actualizar por {metodo === 'email' ? 'usuario' : 'correo'}
              </Boton>
              <Boton
                variante="fantasma"
                onClick={() => {
                  cerrarSesion()
                  navigate(RUTAS.inicio)
                }}
              >
                Cerrar sesión
              </Boton>
            </div>
          </section>
        )}
      </Datos>
    </>
  )
}
