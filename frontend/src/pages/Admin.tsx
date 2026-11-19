import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Buscar, Categoria, Codigo, CrearEvento, Evento } from '../features/tipos'
import { api } from '../features/api'
import { useAccion, useArtistas, useEvento, useSedes } from '../features/hooks'
import { erroresCampos } from '../lib/formulario'
import { ApiError, mensaje } from '../lib/http'
import { categorias, fecha } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import { Aviso, Badge, Boton, Cabecera, Campo, Datos, Modal, Selector } from '../componentes/ui'
const sessionKey = 'pulsepass.creados'
function creados() {
  try {
    return z.array(Evento).parse(JSON.parse(sessionStorage.getItem(sessionKey) || '[]'))
  } catch {
    return []
  }
}
export function AdminEventos() {
  const navigate = useNavigate()
  const form = useForm<z.infer<typeof Buscar>>({ resolver: zodResolver(Buscar) })
  return (
    <div className="narrow">
      <Cabecera titulo="Administración de eventos">
        <Link className="btn bg-marca-600 text-white" to={RUTAS.crear}>
          + Nuevo evento
        </Link>
      </Cabecera>
      <form
        className="card search-form p-6"
        onSubmit={form.handleSubmit((v) => navigate(RUTAS.gestion(v.code)))}
      >
        <Campo
          etiqueta="Buscar evento por código"
          {...form.register('code')}
          error={form.formState.errors.code?.message}
        />
        <Boton>Abrir</Boton>
      </form>
      <h2 className="mt-10 mb-5">Creados en esta sesión</h2>
      {creados().length ? (
        creados().map((e) => (
          <Link
            className="card mb-3 flex flex-wrap items-center gap-4 p-5"
            key={e.eventCode}
            to={RUTAS.gestion(e.eventCode)}
          >
            <span className="font-mono">{e.eventCode}</span>
            <strong>{e.name}</strong>
            <span className="link ml-auto">Gestionar →</span>
          </Link>
        ))
      ) : (
        <p className="muted">Aún no has creado eventos en esta sesión.</p>
      )}
      <p className="muted mt-8 text-sm">
        Consulta eventos por código. El catálogo público muestra únicamente los publicados.
      </p>
    </div>
  )
}
export function AdminCrearEvento() {
  const navigate = useNavigate()
  const sedes = useSedes()
  const mutation = useAccion(api.crear)
  const form = useForm<z.infer<typeof CrearEvento>>({
    resolver: zodResolver(CrearEvento),
    defaultValues: { category: 'MUSIC', minimumAge: 0, description: '', venueCode: '' },
  })
  return (
    <div className="narrow">
      <Link className="back-link" to={RUTAS.admin}>
        ← Eventos
      </Link>
      <Cabecera titulo="Nuevo evento" descripcion="Dale forma a la próxima gran experiencia." />
      <Datos query={sedes} vacio="Necesitas una sede activa para crear eventos">
        {(data) => (
          <form
            className="card p-7"
            noValidate
            onSubmit={form.handleSubmit(async (value) => {
              try {
                const result = await mutation.mutateAsync(value)
                sessionStorage.setItem(
                  sessionKey,
                  JSON.stringify([
                    ...creados().filter((e) => e.eventCode !== result.eventCode),
                    result,
                  ]),
                )
                navigate(RUTAS.gestion(result.eventCode))
              } catch (error) {
                if (error instanceof ApiError && error.status === 403) {
                  navigate(RUTAS.sinPermisos)
                  return
                }
                erroresCampos(error, form.setError, Object.keys(value))
              }
            })}
          >
            {mutation.isError && <Aviso>{mensaje(mutation.error)}</Aviso>}
            <Campo
              etiqueta="Código del evento"
              {...form.register('eventCode')}
              error={form.formState.errors.eventCode?.message}
            />
            <Campo
              etiqueta="Nombre"
              {...form.register('name')}
              error={form.formState.errors.name?.message}
            />
            <div className="field">
              <label htmlFor="description">Descripción</label>
              <textarea
                id="description"
                rows={4}
                maxLength={500}
                {...form.register('description')}
                aria-invalid={!!form.formState.errors.description}
                aria-describedby={
                  form.formState.errors.description ? 'description-error' : undefined
                }
              />
              {form.formState.errors.description && (
                <span id="description-error" role="alert">
                  {form.formState.errors.description.message}
                </span>
              )}
              <span className="muted text-sm">{form.watch('description').length}/500</span>
            </div>
            <div className="grid gap-5 sm:grid-cols-2">
              <Selector etiqueta="Categoría" {...form.register('category')}>
                {Categoria.options.map((c) => (
                  <option key={c} value={c}>
                    {categorias[c]}
                  </option>
                ))}
              </Selector>
              <Campo
                etiqueta="Edad mínima"
                type="number"
                min={0}
                {...form.register('minimumAge', { valueAsNumber: true })}
                error={form.formState.errors.minimumAge?.message}
              />
            </div>
            <Campo
              etiqueta="Fecha y hora"
              type="datetime-local"
              {...form.register('eventDate')}
              error={form.formState.errors.eventDate?.message}
            />
            <Selector
              etiqueta="Sede"
              {...form.register('venueCode')}
              error={form.formState.errors.venueCode?.message}
            >
              <option value="">Selecciona una sede</option>
              {data.map((s) => (
                <option key={s.code} value={s.code}>
                  {s.name} ({s.code})
                </option>
              ))}
            </Selector>
            <div className="modal-actions">
              <Link className="btn border border-slate-300" to={RUTAS.admin}>
                Cancelar
              </Link>
              <Boton cargando={mutation.isPending}>Crear evento</Boton>
            </div>
          </form>
        )}
      </Datos>
    </div>
  )
}
const Asociacion = z.object({ artistId: z.string().regex(/^\d+$/, 'Selecciona un artista') })
export function AdminGestionEvento() {
  const { eventCode } = useParams()
  const parsed = Codigo.safeParse(eventCode)
  const query = useEvento(parsed.success ? parsed.data : '')
  const artistas = useArtistas()
  const publicar = useAccion(api.publicar)
  const asociar = useAccion((id: number) => api.asociar(parsed.success ? parsed.data : '', id))
  const [modal, setModal] = useState(false)
  const form = useForm<z.infer<typeof Asociacion>>({ resolver: zodResolver(Asociacion) })
  if (!parsed.success) return <Cabecera titulo="Evento no disponible" />
  return (
    <div className="narrow">
      <Link className="back-link" to={RUTAS.admin}>
        ← Administración
      </Link>
      <Datos query={query} noDisponible="Evento no disponible">
        {(e) => (
          <>
            <Cabecera titulo={e.name}>
              <Badge estado={e.status} />
            </Cabecera>
            <section className="card p-7">
              <p className="mb-4 font-mono text-marca-600 dark:text-marca-300">{e.eventCode}</p>
              <p className="muted mb-3">
                {fecha(e.eventDate)} · {e.venueName}
              </p>
              <p className="muted mb-5">{e.description}</p>
              {publicar.isSuccess && <Aviso exito>Evento publicado.</Aviso>}
              <div className="flex flex-wrap gap-3">
                {e.status === 'DRAFT' && (
                  <Boton
                    onClick={() => {
                      publicar.reset()
                      setModal(true)
                    }}
                  >
                    Publicar evento
                  </Boton>
                )}
                <Link className="btn border border-slate-300" to={RUTAS.evento(e.eventCode)}>
                  Ver evento
                </Link>
                <Link className="btn border border-slate-300" to={RUTAS.pagados(e.eventCode)}>
                  Tickets pagados
                </Link>
              </div>
            </section>
            <section className="card mt-6 p-7">
              <h2 className="mb-5">Artistas del evento</h2>
              <div className="mb-6 flex flex-wrap gap-3">
                {e.artists.map((a) => (
                  <span className="chip" key={a}>
                    {a}
                  </span>
                ))}
                {!e.artists.length && <p className="muted">Aún no hay artistas asociados.</p>}
              </div>
              <Datos query={artistas} vacio="No hay artistas activos">
                {(data) => (
                  <form
                    onSubmit={form.handleSubmit((v) =>
                      asociar.mutate(Number(v.artistId), {
                        onError: (error) => erroresCampos(error, form.setError, ['artistId']),
                      }),
                    )}
                  >
                    <Selector
                      etiqueta="Asociar artista"
                      {...form.register('artistId')}
                      error={form.formState.errors.artistId?.message}
                    >
                      <option value="">Selecciona un artista</option>
                      {data.map((a) => (
                        <option key={a.id} value={a.id}>
                          {a.stageName}
                        </option>
                      ))}
                    </Selector>
                    {asociar.isError && <Aviso>{mensaje(asociar.error)}</Aviso>}
                    {asociar.isSuccess && <Aviso exito>Artista asociado.</Aviso>}
                    <Boton cargando={asociar.isPending}>Asociar artista</Boton>
                  </form>
                )}
              </Datos>
            </section>
            {modal && (
              <Modal titulo="¿Publicar este evento?" onCerrar={() => setModal(false)}>
                <p className="muted my-5">{e.name} será visible en la cartelera pública.</p>
                {publicar.isError && <Aviso>{mensaje(publicar.error)}</Aviso>}
                <div className="modal-actions">
                  <Boton variante="secundario" onClick={() => setModal(false)}>
                    Volver
                  </Boton>
                  <Boton
                    cargando={publicar.isPending}
                    onClick={() =>
                      publicar.mutate(e.eventCode, { onSuccess: () => setModal(false) })
                    }
                  >
                    Confirmar publicación
                  </Boton>
                </div>
              </Modal>
            )}
          </>
        )}
      </Datos>
    </div>
  )
}
