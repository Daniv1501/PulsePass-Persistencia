import { useEffect, useId, useRef, useState, type ComponentProps, type ReactNode } from 'react'
import { Link, Navigate } from 'react-router'
import { AlertCircle, ArrowRight, CalendarDays, MapPin, Ticket, X } from 'lucide-react'
import clsx from 'clsx'
import type { UseQueryResult } from '@tanstack/react-query'
import { ApiError, mensaje } from '../lib/http'
import { categorias, fecha } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import type { ResumenEvento } from '../features/tipos'
import { ImagenEvento } from './ImagenEvento'
export function Boton({
  variante = 'primario',
  cargando,
  children,
  className,
  ...props
}: ComponentProps<'button'> & {
  variante?: 'primario' | 'secundario' | 'peligro' | 'fantasma'
  cargando?: boolean
}) {
  const tonos = {
    primario: 'bg-marca-600 text-white hover:bg-marca-700',
    secundario:
      'border border-slate-300 bg-white text-slate-800 dark:bg-slate-800 dark:text-slate-100 dark:border-slate-600',
    peligro: 'bg-red-600 text-white hover:bg-red-700',
    fantasma: 'hover:bg-slate-100 dark:hover:bg-slate-800',
  }
  return (
    <button
      {...props}
      disabled={props.disabled || cargando}
      className={clsx('btn', tonos[variante], className)}
    >
      {cargando ? 'Cargando…' : children}
    </button>
  )
}
export function Campo({
  etiqueta,
  error,
  ...props
}: ComponentProps<'input'> & { etiqueta: string; error?: string }) {
  const id = useId()
  return (
    <div className="field">
      <label htmlFor={id}>{etiqueta}</label>
      <input
        {...props}
        id={id}
        aria-invalid={!!error}
        aria-describedby={error ? `${id}-error` : undefined}
      />
      {error && (
        <span id={`${id}-error`} className="text-sm text-red-600 dark:text-red-300" role="alert">
          {error}
        </span>
      )}
    </div>
  )
}
export function Selector({
  etiqueta,
  error,
  children,
  ...props
}: ComponentProps<'select'> & { etiqueta: string; error?: string }) {
  const id = useId()
  return (
    <div className="field">
      <label htmlFor={id}>{etiqueta}</label>
      <select
        {...props}
        id={id}
        aria-invalid={!!error}
        aria-describedby={error ? `${id}-error` : undefined}
      >
        {children}
      </select>
      {error && (
        <span role="alert" id={`${id}-error`}>
          {error}
        </span>
      )}
    </div>
  )
}
export function Aviso({
  children,
  exito = false,
  onCerrar,
}: {
  children: ReactNode
  exito?: boolean
  onCerrar?: () => void
}) {
  const [visible, setVisible] = useState(true)
  const alCerrar = useRef(onCerrar)
  alCerrar.current = onCerrar
  useEffect(() => {
    setVisible(true)
  }, [children])
  useEffect(() => {
    if (!exito) return
    const timer = setTimeout(() => {
      setVisible(false)
      alCerrar.current?.()
    }, 4000)
    return () => clearTimeout(timer)
  }, [exito, children])
  if (!visible) return null
  return (
    <div
      className={clsx('notice', exito ? 'notice-success' : 'notice-error')}
      role={exito ? 'status' : 'alert'}
    >
      <AlertCircle size={19} />
      <div className="flex-1">{children}</div>
      <button
        type="button"
        className="notice-close"
        aria-label="Cerrar aviso"
        onClick={() => {
          setVisible(false)
          onCerrar?.()
        }}
      >
        <X size={16} aria-hidden="true" />
      </button>
    </div>
  )
}
export function Badge({ estado }: { estado: string }) {
  const tonos: Record<string, string> = {
    PUBLISHED: 'bg-emerald-100 text-emerald-800',
    PAID: 'bg-emerald-100 text-emerald-800',
    DRAFT: 'bg-slate-200 text-slate-700',
    SOLD_OUT: 'bg-amber-100 text-amber-800',
    CANCELLED: 'bg-red-100 text-red-800',
    FINISHED: 'bg-slate-200 text-slate-700',
    USED: 'bg-slate-200 text-slate-700',
    RESERVED: 'bg-sky-100 text-sky-800',
  }
  return <span className={clsx('badge', tonos[estado] || tonos.DRAFT)}>{estado}</span>
}
export function Cargando() {
  return (
    <div aria-busy="true" role="status" aria-label="Cargando" className="grid gap-5 sm:grid-cols-3">
      {[1, 2, 3].map((i) => (
        <div key={i} className="h-64 animate-pulse rounded-2xl bg-slate-200 dark:bg-slate-800" />
      ))}
      <span className="sr-only">Cargando…</span>
    </div>
  )
}
export function Datos<T>({
  query,
  children,
  vacio = 'No hay resultados',
  noDisponible,
}: {
  query: UseQueryResult<T, Error>
  children: (data: T) => ReactNode
  vacio?: string
  noDisponible?: string
}) {
  if (query.isPending) return <Cargando />
  if (
    query.isError &&
    !noDisponible &&
    query.error instanceof ApiError &&
    query.error.status === 403
  )
    return <Navigate to={RUTAS.sinPermisos} replace />
  if (query.isError)
    return (
      <div className="empty">
        <Aviso>
          {noDisponible &&
          query.error instanceof ApiError &&
          [403, 404].includes(query.error.status)
            ? noDisponible
            : mensaje(query.error)}
        </Aviso>
        <Boton variante="secundario" onClick={() => void query.refetch()}>
          Reintentar
        </Boton>
      </div>
    )
  if (Array.isArray(query.data) && query.data.length === 0)
    return (
      <div className="empty">
        <Ticket size={40} />
        <h2>{vacio}</h2>
        <Link className="link" to={RUTAS.eventos}>
          Explorar cartelera →
        </Link>
      </div>
    )
  return children(query.data)
}
export function Modal({
  titulo,
  children,
  onCerrar,
}: {
  titulo: string
  children: ReactNode
  onCerrar: () => void
}) {
  const ref = useRef<HTMLDialogElement>(null)
  const id = useId()
  useEffect(() => {
    const element = ref.current
    const focused = document.activeElement
    element?.showModal()
    return () => {
      element?.close()
      if (focused instanceof HTMLElement) focused.focus()
    }
  }, [])
  return (
    <dialog
      ref={ref}
      className="modal"
      aria-labelledby={id}
      onCancel={onCerrar}
      onClick={(e) => {
        if (e.target === e.currentTarget) onCerrar()
      }}
    >
      <div className="flex items-center justify-between gap-6">
        <h2 id={id}>{titulo}</h2>
        <Boton variante="fantasma" aria-label="Cerrar" onClick={onCerrar}>
          <X size={20} />
        </Boton>
      </div>
      {children}
    </dialog>
  )
}
export function Cabecera({
  titulo,
  descripcion,
  children,
}: {
  titulo: string
  descripcion?: string
  children?: ReactNode
}) {
  return (
    <div className="page-heading">
      <div>
        <p className="eyebrow">DESCUBRE · CONECTA · VIVE</p>
        <h1>{titulo}</h1>
        {descripcion && <p className="muted mt-3">{descripcion}</p>}
      </div>
      {children}
    </div>
  )
}
interface TarjetaEventoProps {
  evento: ResumenEvento
  ciudad?: string
}
export function TarjetaEvento({ evento, ciudad }: TarjetaEventoProps) {
  const date = new Date(evento.eventDate)
  const day = new Intl.DateTimeFormat('es-CO', { day: '2-digit' }).format(date)
  const month = new Intl.DateTimeFormat('es-CO', { month: 'short' })
    .format(date)
    .replace('.', '')
    .toUpperCase()
  return (
    <article className="card event-card">
      <Link
        className="event-card-cover"
        to={RUTAS.evento(evento.eventCode)}
        tabIndex={-1}
        aria-hidden="true"
      >
        <ImagenEvento evento={evento} />
      </Link>
      <div className="event-cover-status">
        <Badge estado={evento.status} />
      </div>
      <time
        className="event-date-badge"
        dateTime={evento.eventDate}
        aria-label={fecha(evento.eventDate)}
      >
        <strong>{day}</strong>
        <span>{month}</span>
      </time>
      <div className="event-card-body">
        <span className="event-card-category">{categorias[evento.category]}</span>
        <h3>
          <Link to={RUTAS.evento(evento.eventCode)}>{evento.name}</Link>
        </h3>
        <p className="meta">
          <MapPin size={16} aria-hidden="true" />
          {ciudad ? `${ciudad} · ${evento.venueName}` : evento.venueName}
        </p>
        <p className="meta">
          <CalendarDays size={16} aria-hidden="true" />
          {fecha(evento.eventDate)}
        </p>
        <Link className="event-card-action" to={RUTAS.evento(evento.eventCode)}>
          Ver evento <ArrowRight size={18} aria-hidden="true" />
        </Link>
      </div>
    </article>
  )
}
export function Tabla<T>({
  filas,
  columnas,
}: {
  filas: T[]
  columnas: { encabezado: string; celda: (fila: T) => ReactNode }[]
}) {
  return (
    <div className="grid gap-4 md:block">
      <table className="responsive-table">
        <thead>
          <tr>
            {columnas.map((c) => (
              <th key={c.encabezado}>{c.encabezado}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {filas.map((fila, i) => (
            <tr key={i}>
              {columnas.map((c) => (
                <td key={c.encabezado} data-label={c.encabezado}>
                  {c.celda(fila)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
