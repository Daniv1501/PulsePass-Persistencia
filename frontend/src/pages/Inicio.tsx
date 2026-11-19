import { Link } from 'react-router'
import { lazy, Suspense } from 'react'
import {
  ArrowRight,
  ArrowUpRight,
  CalendarDays,
  Headphones,
  MapPin,
  Music2,
  Sparkles,
  Ticket,
} from 'lucide-react'
import { RUTAS } from '../lib/rutas'
import { categorias, fecha } from '../lib/formato'
import { useArtistas, useEventos, useSedes } from '../features/hooks'
import { Datos, TarjetaEvento } from '../componentes/ui'
import { ImagenEvento } from '../componentes/ImagenEvento'
const BuscadorInicio = lazy(() => import('../componentes/BuscadorInicio'))
import PortadaInicio from '../componentes/PortadaInicio'
export default function Inicio() {
  const eventos = useEventos()
  const artistas = useArtistas()
  const sedes = useSedes()
  const destacado = eventos.data?.[0]
  return (
    <>
      <PortadaInicio />
      <Suspense
        fallback={
          <div className="home-search" aria-busy="true" style={{ minHeight: 120 }}>
            Preparando búsqueda…
          </div>
        }
      >
        <BuscadorInicio />
      </Suspense>
      <nav className="experience-strip" aria-label="Explorar por experiencia">
        <span>Un plan para cada versión de ti</span>
        <Link to={`${RUTAS.eventos}?categoria=MUSIC`}>
          <Music2 size={17} /> Música en vivo
        </Link>
        <Link to={`${RUTAS.eventos}?categoria=CULTURE`}>
          <Sparkles size={17} /> Cultura
        </Link>
        <Link to={`${RUTAS.eventos}?categoria=SPORTS`}>
          <Ticket size={17} /> Deportes
        </Link>
        <Link to={RUTAS.eventos}>
          Descubrir todo <ArrowRight size={17} />
        </Link>
      </nav>
      <section className="section home-section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">FUERA DE LA RUTINA</p>
            <h2>El próximo «yo estuve ahí».</h2>
            <p className="muted section-intro">
              Elige el evento. Nosotros te guardamos la emoción.
            </p>
          </div>
          <Link className="link section-link" to={RUTAS.eventos}>
            Ver cartelera <ArrowUpRight size={18} />
          </Link>
        </div>
        <Datos query={eventos} vacio="Pronto habrá nuevas experiencias">
          {(data) => (
            <div className="cards-grid home-events-grid">
              {data.slice(0, 3).map((e) => (
                <TarjetaEvento
                  key={e.eventCode}
                  evento={e}
                  ciudad={sedes.data?.find((s) => s.name === e.venueName)?.city}
                />
              ))}
            </div>
          )}
        </Datos>
      </section>
      {destacado && (
        <section className="spotlight">
          <div className="spotlight-visual">
            <ImagenEvento evento={destacado} />
            <span className="spotlight-sticker">
              <Headphones size={23} /> EN PRIMERA FILA
            </span>
          </div>
          <div className="spotlight-copy">
            <p className="eyebrow">UN EVENTO PARA TU AGENDA</p>
            <span className="spotlight-category">{categorias[destacado.category]}</span>
            <h2>{destacado.name}</h2>
            <p className="meta">
              <CalendarDays size={18} />
              {fecha(destacado.eventDate)}
            </p>
            <p className="meta">
              <MapPin size={18} />
              {destacado.venueName}
            </p>
            <Link className="btn spotlight-button" to={RUTAS.evento(destacado.eventCode)}>
              Quiero vivirlo <ArrowUpRight size={20} />
            </Link>
          </div>
        </section>
      )}
      <section className="section home-section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">DETRÁS DE CADA GRAN NOCHE</p>
            <h2>Talento que se siente.</h2>
          </div>
          <Link className="link section-link" to={RUTAS.artistas}>
            Todos los artistas <ArrowUpRight size={18} />
          </Link>
        </div>
        <Datos query={artistas} vacio="Pronto conocerás a nuestros artistas">
          {(data) => (
            <div className="artist-grid">
              {data.slice(0, 3).map((a, index) => (
                <Link className="artist-editorial" key={a.id} to={RUTAS.artista(a.id)}>
                  <span className="artist-number">0{index + 1}</span>
                  <div className="artist-mark" aria-hidden="true">
                    <Music2 size={38} />
                  </div>
                  <div className="artist-editorial-copy">
                    <p>
                      {a.genre} · {a.country}
                    </p>
                    <h3>{a.stageName}</h3>
                  </div>
                  <ArrowUpRight className="artist-arrow" size={24} />
                </Link>
              ))}
            </div>
          )}
        </Datos>
      </section>
      <section className="festival-cta">
        <div className="festival-cta-symbol" aria-hidden="true">
          <Ticket size={65} />
        </div>
        <div>
          <p className="eyebrow">ENTRA. DESCUBRE. REPITE.</p>
          <h2>
            Las buenas historias
            <br />
            empiezan con un «vamos».
          </h2>
          <p>Tu próxima entrada puede ser tu próximo gran recuerdo.</p>
        </div>
        <Link className="btn" to={RUTAS.eventos}>
          Encontrar mi próximo plan <ArrowUpRight size={20} />
        </Link>
      </section>
    </>
  )
}
