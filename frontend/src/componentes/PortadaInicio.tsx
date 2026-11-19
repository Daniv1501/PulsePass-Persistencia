import { Link } from 'react-router'
import { ArrowUpRight, Waves } from 'lucide-react'
import { RUTAS } from '../lib/rutas'
export default function PortadaInicio() {
  return (
    <section className="festival-hero" aria-labelledby="festival-title">
      <picture>
        <source media="(max-width: 767px)" srcSet="/images/festival.webp" />
        <img
          className="festival-hero-photo"
          src="/images/festival-hero.webp"
          alt=""
          fetchPriority="high"
          width={1800}
          height={1000}
        />
      </picture>
      <div className="festival-hero-overlay" />
      <div className="festival-hero-copy">
        <span className="live-label">
          <span /> LA VIDA SUENA MEJOR EN VIVO
        </span>
        <h1 id="festival-title">
          Menos rutina.
          <br />
          Más <span>momentos.</span>
        </h1>
        <p>
          La primera canción. Las manos arriba. Esa noche que no se olvida.
          <br className="hidden sm:block" /> Encuentra tu próxima gran historia.
        </p>
        <Link className="hero-explore" to={RUTAS.eventos}>
          Explorar experiencias <ArrowUpRight size={22} />
        </Link>
      </div>
      <div className="hero-side-note" aria-hidden="true">
        <Waves size={30} />
        <span>
          Siente el ritmo.
          <br />
          Vive el momento.
        </span>
      </div>
      <span className="hero-photo-caption">Música en vivo · Imagen de ambiente</span>
    </section>
  )
}
