import { useSearchParams } from 'react-router'
import { z } from 'zod'
import { useQuery } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Search } from 'lucide-react'
import { Categoria } from '../features/tipos'
import { api } from '../features/api'
import { useEventos, useSedes } from '../features/hooks'
import { categorias } from '../lib/formato'
import { Aviso, Boton, Cabecera, Campo, Datos, Selector, TarjetaEvento } from '../componentes/ui'
import { mensaje } from '../lib/http'
const Filtro = z.object({ artista: z.string().max(255) })
export default function Catalogo() {
  const [params, setParams] = useSearchParams()
  const parsed = z
    .object({
      q: z.string().max(255).catch(''),
      categoria: Categoria.or(z.literal('')).catch(''),
      artista: z.string().max(255).catch(''),
    })
    .parse(Object.fromEntries(params))
  const { q = '', categoria = '', artista = '' } = parsed
  const eventos = useEventos(artista)
  const sedes = useSedes()
  const form = useForm<z.infer<typeof Filtro>>({
    resolver: zodResolver(Filtro),
    defaultValues: { artista },
  })
  const perfil = useQuery({
    queryKey: ['artista', 'nombre', artista],
    queryFn: () => api.artistaNombre(artista),
    enabled: !!artista,
    staleTime: 300000,
  })
  function update(key: string, value: string) {
    const next = new URLSearchParams(params)
    if (value) next.set(key, value)
    else next.delete(key)
    setParams(next, { replace: true })
  }
  return (
    <>
      <Cabecera
        titulo="Cartelera de eventos"
        descripcion="Encuentra esa experiencia que vas a recordar."
      />
      <div className="card filters">
        <Campo
          etiqueta="Buscar evento"
          placeholder="¿Qué quieres vivir?"
          value={q}
          onChange={(e) => update('q', e.target.value)}
        />
        <Selector
          etiqueta="Categoría"
          value={categoria}
          onChange={(e) => update('categoria', e.target.value)}
        >
          <option value="">Todas las categorías</option>
          {Categoria.options.map((c) => (
            <option key={c} value={c}>
              {categorias[c]}
            </option>
          ))}
        </Selector>
        <form
          onSubmit={form.handleSubmit((v) => update('artista', v.artista))}
          className="flex items-end gap-2"
        >
          <Campo
            etiqueta="Artista"
            placeholder="Nombre artístico"
            error={form.formState.errors.artista?.message}
            {...form.register('artista')}
          />
          <Boton aria-label="Buscar por artista">
            <Search size={18} />
          </Boton>
        </form>
      </div>
      {perfil.data && <p className="muted mb-5">Eventos de {perfil.data.stageName}</p>}
      {artista && perfil.isPending && <p role="status">Buscando artista…</p>}
      {perfil.isError && <Aviso>{mensaje(perfil.error)}</Aviso>}
      <Datos query={eventos} vacio="No hay eventos publicados">
        {(data) => {
          const filtered = data.filter(
            (e) =>
              e.name.toLocaleLowerCase('es').includes(q.toLocaleLowerCase('es')) &&
              (!categoria || e.category === categoria),
          )
          return (
            <>
              <p className="muted mb-5">{filtered.length} experiencias para descubrir</p>
              {filtered.length ? (
                <div className="cards-grid">
                  {filtered.map((e) => (
                    <TarjetaEvento
                      key={e.eventCode}
                      evento={e}
                      ciudad={sedes.data?.find((s) => s.name === e.venueName)?.city}
                    />
                  ))}
                </div>
              ) : (
                <div className="empty">
                  <h2>No encontramos eventos con esos filtros</h2>
                  <Boton
                    variante="secundario"
                    onClick={() => {
                      setParams({})
                      form.reset({ artista: '' })
                    }}
                  >
                    Limpiar filtros
                  </Boton>
                </div>
              )}
            </>
          )
        }}
      </Datos>
    </>
  )
}
