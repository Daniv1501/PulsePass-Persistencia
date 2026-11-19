import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router'
import {
  CalendarDays,
  ClipboardList,
  MapPin,
  Menu,
  Mic2,
  Moon,
  ScanLine,
  Settings2,
  Sun,
  Ticket,
  X,
} from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { RUTAS } from '../lib/rutas'
import { Boton } from './ui'
import Logo from './Logo'
import PiePagina from './PiePagina'
export default function Layout() {
  const { sesion, tieneRol, cerrarSesion } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  useEffect(() => {
    if (sesion.estado === 'anonimo' && sesion.expirada && location.pathname !== RUTAS.login) {
      void navigate(RUTAS.login, {
        replace: true,
        state: { desde: location.pathname + location.search, motivo: 'expirada' },
      })
    }
  }, [sesion, location.pathname, location.search, navigate])
  const [menu, setMenu] = useState(false)
  const [oscuro, setOscuro] = useState(() => document.documentElement.classList.contains('dark'))
  function tema() {
    document.documentElement.classList.toggle('dark')
    setOscuro(!oscuro)
    localStorage.setItem('pulsepass.tema', !oscuro ? 'dark' : 'light')
  }
  function salir() {
    cerrarSesion()
    navigate(RUTAS.inicio)
  }
  const enlaces = [
    { to: RUTAS.eventos, label: 'Eventos', icon: CalendarDays },
    { to: RUTAS.artistas, label: 'Artistas', icon: Mic2 },
    { to: RUTAS.sedes, label: 'Sedes', icon: MapPin },
    ...(tieneRol('USER') ? [{ to: RUTAS.tickets, label: 'Mis tickets', icon: Ticket }] : []),
    ...(tieneRol('STAFF', 'ADMIN')
      ? [
          { to: RUTAS.validacion, label: 'Validar ingreso', icon: ScanLine },
          { to: RUTAS.consultaPagados, label: 'Tickets pagados', icon: ClipboardList },
        ]
      : []),
    ...(tieneRol('ADMIN') ? [{ to: RUTAS.admin, label: 'Administrar', icon: Settings2 }] : []),
  ]
  return (
    <>
      <a className="skip" href="#contenido">
        Saltar al contenido
      </a>
      <header className="site-header">
        <div className="nav-shell">
          <Link className="brand" to={RUTAS.inicio} aria-label="PulsePass, inicio">
            <Logo />
          </Link>
          <Boton
            id="menu-button"
            variante="fantasma"
            className="md:hidden"
            aria-expanded={menu}
            aria-controls="navegacion"
            aria-label={menu ? 'Cerrar menú' : 'Abrir menú'}
            onClick={() => setMenu(!menu)}
          >
            {menu ? <X /> : <Menu />}
          </Boton>
          <nav
            id="navegacion"
            aria-label="Principal"
            className={menu ? 'nav-links nav-open' : 'nav-links'}
            onClick={() => setMenu(false)}
            onKeyDown={(event) => {
              if (event.key === 'Escape') {
                setMenu(false)
                document.getElementById('menu-button')?.focus()
              }
            }}
          >
            {enlaces.map((e) => {
              const Icon = e.icon
              return (
                <NavLink key={e.to} to={e.to}>
                  <Icon size={16} aria-hidden="true" />
                  {e.label}
                </NavLink>
              )
            })}
            {sesion.estado !== 'autenticado' && (
              <>
                <Link className="mobile-account" to={RUTAS.login}>
                  Iniciar sesión
                </Link>
                <Link className="mobile-account" to={RUTAS.registro}>
                  Crear cuenta
                </Link>
              </>
            )}
            {sesion.estado === 'autenticado' && (
              <>
                <Link className="mobile-account" to={RUTAS.cuenta}>
                  Mi cuenta
                </Link>
                <Boton className="mobile-account" variante="fantasma" onClick={salir}>
                  Salir
                </Boton>
              </>
            )}
          </nav>
          <div className="nav-actions">
            <Boton
              variante="fantasma"
              aria-label={oscuro ? 'Activar modo claro' : 'Activar modo oscuro'}
              onClick={tema}
            >
              {oscuro ? <Sun size={19} /> : <Moon size={19} />}
            </Boton>
            {sesion.estado === 'autenticado' ? (
              <>
                <Link className="account-link" to={RUTAS.cuenta}>
                  <span className="account-avatar" aria-hidden="true">
                    {sesion.usuario.username.slice(0, 2).toLocaleUpperCase('es-CO')}
                  </span>
                  {sesion.usuario.username}
                </Link>
                <Boton variante="fantasma" className="logout-button" onClick={salir}>
                  Salir
                </Boton>
              </>
            ) : (
              <>
                <Link className="login-link" to={RUTAS.login}>
                  Ingresar
                </Link>
                <Link className="btn header-register bg-marca-600 text-white" to={RUTAS.registro}>
                  Registrarse
                </Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main id="contenido" className="main-shell" tabIndex={-1}>
        <Outlet />
      </main>
      <PiePagina />
    </>
  )
}
