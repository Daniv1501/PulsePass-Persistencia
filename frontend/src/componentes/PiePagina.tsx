import { Link } from 'react-router'
import { RUTAS } from '../lib/rutas'
import Logo from './Logo'
export default function PiePagina() {
  return (
    <footer>
      <div className="footer-shell">
        <Link className="brand" to={RUTAS.inicio} aria-label="PulsePass, inicio">
          <Logo />
        </Link>
        <p>Los mejores momentos empiezan con una entrada.</p>
        <span>© 2026 PulsePass · Hecho para vivirlo</span>
      </div>
    </footer>
  )
}
