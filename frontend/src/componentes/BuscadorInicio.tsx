import { useNavigate } from 'react-router'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Search, SlidersHorizontal, ArrowUpRight } from 'lucide-react'
import { Categoria } from '../features/tipos'
import { categorias } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
const Busqueda = z.object({ q: z.string().trim().max(255), categoria: Categoria.or(z.literal('')) })
export default function BuscadorInicio() {
  const navigate = useNavigate()
  const form = useForm<z.infer<typeof Busqueda>>({
    resolver: zodResolver(Busqueda),
    defaultValues: { q: '', categoria: '' },
  })
  return (
    <form
      className="home-search"
      role="search"
      aria-label="Encuentra tu próximo evento"
      onSubmit={form.handleSubmit((values) => {
        const query = new URLSearchParams()
        if (values.q) query.set('q', values.q)
        if (values.categoria) query.set('categoria', values.categoria)
        navigate(`${RUTAS.eventos}${query.size ? `?${query.toString()}` : ''}`)
      })}
    >
      <div className="home-search-field">
        <Search size={21} aria-hidden="true" />
        <div>
          <label htmlFor="home-event">¿Qué quieres vivir?</label>
          <input
            id="home-event"
            placeholder="Busca un evento…"
            maxLength={255}
            {...form.register('q')}
          />
        </div>
      </div>
      <div className="home-search-field search-category">
        <SlidersHorizontal size={20} aria-hidden="true" />
        <div>
          <label htmlFor="home-category">Tu tipo de plan</label>
          <select id="home-category" {...form.register('categoria')}>
            <option value="">Todas las experiencias</option>
            {Categoria.options.map((category) => (
              <option key={category} value={category}>
                {categorias[category]}
              </option>
            ))}
          </select>
        </div>
      </div>
      <button className="btn search-submit" type="submit">
        Encontrar mi plan <ArrowUpRight size={20} aria-hidden="true" />
      </button>
    </form>
  )
}
