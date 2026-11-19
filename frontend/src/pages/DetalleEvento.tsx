import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { CalendarDays, MapPin } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'
import { useAccion, useEvento } from '../features/hooks'
import { api } from '../features/api'
import { Codigo, Compra, TipoTicket, type Evento } from '../features/tipos'
import { Aviso, Badge, Boton, Cabecera, Datos, Modal, Selector } from '../componentes/ui'
import { categorias, fecha } from '../lib/formato'
import { mensaje } from '../lib/http'
import { RUTAS } from '../lib/rutas'
import { ImagenEvento } from '../componentes/ImagenEvento'
import { erroresCampos } from '../lib/formulario'
function Comprar({ evento, onCerrar }: { evento: Evento; onCerrar: () => void }) {
  const { sesion } = useAuth()
  const navigate = useNavigate()
  const mutation = useAccion(api.comprar)
  const form = useForm<z.infer<typeof Compra>>({
    resolver: zodResolver(Compra),
    defaultValues: { type: 'GENERAL' },
  })
  return (
    <Modal titulo="Confirmar compra" onCerrar={onCerrar}>
      <p className="muted my-4">{evento.name}</p>
      <form
        onSubmit={form.handleSubmit(async (value) => {
          if (sesion.estado !== 'autenticado') return
          try {
            const ticket = await mutation.mutateAsync({
              ...value,
              eventCode: evento.eventCode,
              userEmail: sesion.usuario.email,
            })
            navigate(RUTAS.confirmacion(ticket.ticketCode))
          } catch (error) {
            erroresCampos(error, form.setError, ['type'])
          }
        })}
      >
        <Selector
          etiqueta="Tipo de entrada"
          error={form.formState.errors.type?.message}
          {...form.register('type')}
        >
          {TipoTicket.options.map((t) => (
            <option key={t}>{t}</option>
          ))}
        </Selector>
        <p className="muted my-4 text-sm">
          El precio definitivo se informa en la confirmación de compra.
        </p>
        {mutation.isError && <Aviso>{mensaje(mutation.error)}</Aviso>}
        <div className="modal-actions">
          <Boton type="button" variante="secundario" onClick={onCerrar}>
            Volver
          </Boton>
          <Boton cargando={mutation.isPending}>Confirmar compra</Boton>
        </div>
      </form>
    </Modal>
  )
}
export default function DetalleEvento() {
  const { eventCode } = useParams()
  const parsed = Codigo.safeParse(eventCode)
  const query = useEvento(parsed.success ? parsed.data : '')
  const [modal, setModal] = useState(false)
  const { sesion, tieneRol } = useAuth()
  const navigate = useNavigate()
  if (!parsed.success) return <Cabecera titulo="Evento no disponible" />
  return (
    <>
      <Link className="back-link" to={RUTAS.eventos}>
        ← Volver a la cartelera
      </Link>
      <Datos query={query} noDisponible="Evento no disponible">
        {(e) => (
          <>
            <div className="detail-banner">
              <ImagenEvento evento={e} />
              <span>{categorias[e.category]} · PULSEPASS LIVE</span>
            </div>
            <div className="detail-grid">
              <section>
                <div className="mb-4 flex items-center gap-3">
                  <span className="category">{categorias[e.category]}</span>
                  <Badge estado={e.status} />
                </div>
                <Cabecera titulo={e.name} />
                <p className="meta mb-3">
                  <CalendarDays size={20} />
                  {fecha(e.eventDate)}
                </p>
                <p className="meta">
                  <MapPin size={20} />
                  <Link className="link" to={RUTAS.sede(e.venueCode)}>
                    {e.venueName}
                  </Link>
                </p>
                <h2 className="mt-10 mb-4">Acerca del evento</h2>
                <p className="muted whitespace-pre-line">
                  {e.description || 'Prepárate para vivir una experiencia inolvidable.'}
                </p>
                <h2 className="mt-10 mb-4">Artistas</h2>
                <div className="flex flex-wrap gap-3">
                  {e.artists.length ? (
                    e.artists.map((a) => (
                      <Link
                        key={a}
                        className="chip"
                        to={`${RUTAS.eventos}?artista=${encodeURIComponent(a)}`}
                      >
                        {a}
                      </Link>
                    ))
                  ) : (
                    <p className="muted">Artistas por anunciar.</p>
                  )}
                </div>
              </section>
              <aside className="card purchase-box">
                <h2>Tu lugar está aquí</h2>
                <p className="muted mt-3 mb-6">Vive la experiencia en {e.venueName}.</p>
                <p className="mb-6">Edad mínima: {e.minimumAge} años</p>
                {(sesion.estado !== 'autenticado' || tieneRol('USER')) && (
                  <Boton
                    className="mobile-purchase w-full"
                    disabled={e.status !== 'PUBLISHED'}
                    onClick={() =>
                      sesion.estado === 'autenticado'
                        ? setModal(true)
                        : navigate(RUTAS.login, { state: { desde: RUTAS.evento(e.eventCode) } })
                    }
                  >
                    Comprar entrada
                  </Boton>
                )}
                <p className="muted mt-4 text-sm">
                  {e.status === 'PUBLISHED'
                    ? 'Selecciona tu tipo de entrada al continuar.'
                    : 'La compra no está disponible para este evento.'}
                </p>
              </aside>
            </div>
            {modal && <Comprar evento={e} onCerrar={() => setModal(false)} />}
          </>
        )}
      </Datos>
    </>
  )
}
