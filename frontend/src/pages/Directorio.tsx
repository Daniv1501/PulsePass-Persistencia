import { Link, useParams } from 'react-router'
import { useQuery } from '@tanstack/react-query'
import { z } from 'zod'
import { MapPin, Music2, Users } from 'lucide-react'
import { api } from '../features/api'
import { useArtistas, useEventos, useSedes } from '../features/hooks'
import { Codigo } from '../features/tipos'
import { RUTAS } from '../lib/rutas'
import { Cabecera, Datos, TarjetaEvento } from '../componentes/ui'
export function ListaArtistas() {
  const query = useArtistas()
  return (
    <>
      <Cabecera
        titulo="Artistas"
        descripcion="Conoce el talento detrás de tus mejores experiencias."
      />
      <Datos query={query} vacio="No hay artistas disponibles">
        {(data) => (
          <div className="cards-grid">
            {data.map((a) => (
              <Link className="card overflow-hidden" to={RUTAS.artista(a.id)} key={a.id}>
                <div className="artist-art">
                  <Music2 size={66} />
                  <span>{a.stageName.slice(0, 2).toUpperCase()}</span>
                </div>
                <div className="p-6">
                  <h2>{a.stageName}</h2>
                  <p className="muted mt-2">
                    {a.genre} · {a.country}
                  </p>
                  <span className="link mt-5 block">Conocer artista →</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </Datos>
    </>
  )
}
function EventosArtista({ nombre }: { nombre: string }) {
  const query = useEventos(nombre)
  return (
    <Datos query={query} vacio="Este artista aún no tiene eventos publicados">
      {(data) => (
        <div className="cards-grid">
          {data.map((e) => (
            <TarjetaEvento key={e.eventCode} evento={e} />
          ))}
        </div>
      )}
    </Datos>
  )
}
export function DetalleArtista() {
  const { id } = useParams()
  const parsed = z.coerce.number().int().positive().safeParse(id)
  const query = useQuery({
    queryKey: ['artista', id],
    queryFn: () => api.artista(parsed.success ? parsed.data : 0),
    enabled: parsed.success,
    staleTime: 300000,
  })
  if (!parsed.success) return <Cabecera titulo="404 · Artista no encontrado" />
  return (
    <>
      <Link className="back-link" to={RUTAS.artistas}>
        ← Todos los artistas
      </Link>
      <Datos query={query} noDisponible="Artista no encontrado">
        {(a) => (
          <>
            <section className="card profile-banner">
              <span className="avatar">
                <Music2 size={36} />
              </span>
              <div>
                <p className="eyebrow">ARTISTA PULSEPASS</p>
                <h1>{a.stageName}</h1>
                <p className="muted mt-3">
                  {a.genre} · {a.country}
                </p>
              </div>
            </section>
            <h2 className="my-8">Próximos eventos</h2>
            <EventosArtista nombre={a.stageName} />
          </>
        )}
      </Datos>
    </>
  )
}
export function ListaSedes() {
  const query = useSedes()
  return (
    <>
      <Cabecera titulo="Sedes" descripcion="Los lugares donde todo sucede." />
      <Datos query={query} vacio="No hay sedes disponibles">
        {(data) => (
          <div className="cards-grid">
            {data.map((s) => (
              <article className="card overflow-hidden" key={s.code}>
                <div className="venue-art">
                  <MapPin size={60} />
                </div>
                <div className="p-6">
                  <h2>{s.name}</h2>
                  <p className="meta mt-4">
                    <MapPin size={17} />
                    {s.city}
                  </p>
                  <p className="meta mt-3">
                    <Users size={17} />
                    {s.capacity} personas
                  </p>
                  <Link className="link mt-6 block" to={RUTAS.sede(s.code)}>
                    Ver sede →
                  </Link>
                </div>
              </article>
            ))}
          </div>
        )}
      </Datos>
    </>
  )
}
export function DetalleSede() {
  const { code } = useParams()
  const parsed = Codigo.safeParse(code)
  const query = useQuery({
    queryKey: ['sede', code],
    queryFn: () => api.sede(parsed.success ? parsed.data : ''),
    enabled: parsed.success,
    staleTime: 300000,
  })
  if (!parsed.success) return <Cabecera titulo="404 · Sede no encontrada" />
  return (
    <>
      <Link className="back-link" to={RUTAS.sedes}>
        ← Todas las sedes
      </Link>
      <Datos query={query} noDisponible="Sede no encontrada">
        {(s) => (
          <section className="card overflow-hidden">
            <div className="venue-art h-64">
              <MapPin size={80} />
            </div>
            <div className="p-8">
              <Cabecera titulo={s.name} descripcion={s.city} />
              <div className="flex flex-wrap gap-8">
                <p className="meta">
                  <Users />
                  {s.capacity} personas de capacidad
                </p>
                <p className="font-mono">{s.code}</p>
                <span className="badge bg-emerald-100 text-emerald-800">
                  {s.active ? 'Activa' : 'Inactiva'}
                </span>
              </div>
            </div>
          </section>
        )}
      </Datos>
    </>
  )
}
