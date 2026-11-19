import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { useQuery } from '@tanstack/react-query'
import { CheckCircle2, Copy } from 'lucide-react'
import Logo from '../componentes/Logo'
import { useAuth } from '../auth/AuthContext'
import { api } from '../features/api'
import { useAccion } from '../features/hooks'
import { Codigo, EstadoTicket, type Ticket } from '../features/tipos'
import { RUTAS } from '../lib/rutas'
import { fecha, precioCOP } from '../lib/formato'
import { mensaje } from '../lib/http'
import { Aviso, Badge, Boton, Cabecera, Datos, Modal } from '../componentes/ui'
function Cancelar({ ticket, onCerrar }: { ticket: Ticket; onCerrar: () => void }) {
  const mutation = useAccion(api.cancelar)
  return (
    <Modal titulo="¿Cancelar este ticket?" onCerrar={onCerrar}>
      <p className="muted my-5">
        Vas a cancelar el ticket <span className="font-mono">{ticket.ticketCode}</span>. Confirma
        para continuar.
      </p>
      {mutation.isError && <Aviso>{mensaje(mutation.error)}</Aviso>}
      <div className="modal-actions">
        <Boton variante="secundario" onClick={onCerrar}>
          Conservar ticket
        </Boton>
        <Boton
          variante="peligro"
          cargando={mutation.isPending}
          onClick={() => mutation.mutate(ticket.ticketCode, { onSuccess: onCerrar })}
        >
          Confirmar cancelación
        </Boton>
      </div>
    </Modal>
  )
}
export function TarjetaTicket({ ticket, detalle = false }: { ticket: Ticket; detalle?: boolean }) {
  const [cancelar, setCancelar] = useState(false)
  const [copiado, setCopiado] = useState(false)
  return (
    <article className="card ticket-card">
      <div className="ticket-side">
        <Logo soloIcono />
        <span>PULSEPASS</span>
        <span className="ticket-side-type">{ticket.type}</span>
      </div>
      <div className="ticket-body">
        <div className="flex flex-wrap justify-between gap-3">
          <h2>{ticket.eventName}</h2>
          <Badge estado={ticket.status} />
        </div>
        <div className="ticket-code-panel">
          <span className="ticket-code-label">CÓDIGO DE ENTRADA</span>
          <p className="font-mono">{ticket.ticketCode}</p>
        </div>
        <p className="muted mt-2">
          {ticket.type} · {precioCOP(ticket.price)}
        </p>
        <p className="muted mt-2 text-sm">Comprado el {fecha(ticket.purchaseDate)}</p>
        {detalle && (
          <>
            <p className="muted mt-4">Titular: {ticket.userEmail}</p>
            <p className="mt-4">Presenta este código en el ingreso.</p>
            <Boton
              variante="secundario"
              className="mt-4"
              onClick={() => {
                void navigator.clipboard
                  .writeText(ticket.ticketCode)
                  .then(() => setCopiado(true))
                  .catch(() => setCopiado(false))
              }}
            >
              <Copy size={16} />
              {copiado ? 'Copiado' : 'Copiar código'}
            </Boton>
          </>
        )}
        <div className="mt-5 flex flex-wrap gap-3">
          {!detalle && (
            <Link className="btn border border-slate-300" to={RUTAS.ticket(ticket.ticketCode)}>
              Ver detalle
            </Link>
          )}
          {ticket.status === 'PAID' && (
            <Boton
              variante="fantasma"
              className="text-red-600 dark:text-red-300"
              onClick={() => setCancelar(true)}
            >
              Cancelar ticket
            </Boton>
          )}
        </div>
      </div>
      {cancelar && <Cancelar ticket={ticket} onCerrar={() => setCancelar(false)} />}
    </article>
  )
}
export function MisTickets() {
  const { sesion } = useAuth()
  const email = sesion.estado === 'autenticado' ? sesion.usuario.email : ''
  const query = useQuery({
    queryKey: ['tickets', 'usuario', email],
    queryFn: () => api.tickets(email),
    enabled: !!email,
    staleTime: 15000,
  })
  const [filtro, setFiltro] = useState('')
  return (
    <>
      <Cabecera titulo="Mis tickets" descripcion="Tus próximos recuerdos están aquí." />
      <div className="mb-8 flex flex-wrap gap-2" aria-label="Filtrar tickets">
        {[
          { value: '', label: 'Todos' },
          ...EstadoTicket.options.map((s) => ({
            value: s,
            label: {
              PAID: 'Pagados',
              USED: 'Usados',
              CANCELLED: 'Cancelados',
              RESERVED: 'Reservados',
            }[s],
          })),
        ].map((f) => (
          <button
            key={f.value}
            className="chip"
            aria-pressed={filtro === f.value}
            onClick={() => setFiltro(f.value)}
          >
            {f.label}
          </button>
        ))}
      </div>
      <Datos query={query} vacio="Aún no tienes tickets">
        {(data) => {
          const visible = data.filter((t) => !filtro || t.status === filtro)
          return visible.length ? (
            <div className="grid gap-5">
              {visible.map((t) => (
                <TarjetaTicket key={t.ticketCode} ticket={t} />
              ))}
            </div>
          ) : (
            <div className="empty">No hay tickets con este estado.</div>
          )
        }}
      </Datos>
    </>
  )
}
export function DetalleTicket() {
  const { ticketCode } = useParams()
  const valid = Codigo.safeParse(ticketCode)
  const query = useQuery({
    queryKey: ['ticket', ticketCode],
    queryFn: () => api.ticket(valid.success ? valid.data : ''),
    enabled: valid.success,
    staleTime: 15000,
  })
  if (!valid.success) return <Cabecera titulo="Ticket no disponible" />
  return (
    <>
      <Link className="back-link" to={RUTAS.tickets}>
        ← Mis tickets
      </Link>
      <Cabecera titulo="Tu entrada" />
      <Datos query={query} noDisponible="Ticket no disponible">
        {(t) => <TarjetaTicket ticket={t} detalle />}
      </Datos>
    </>
  )
}
export function ConfirmacionCompra() {
  const { ticketCode } = useParams()
  const parsed = Codigo.safeParse(ticketCode)
  const query = useQuery({
    queryKey: ['ticket', ticketCode],
    queryFn: () => api.ticket(parsed.success ? parsed.data : ''),
    enabled: parsed.success,
    staleTime: 15000,
  })
  if (!parsed.success) return <Cabecera titulo="Ticket no disponible" />
  return (
    <div className="mx-auto max-w-2xl">
      <Datos query={query} noDisponible="Ticket no disponible">
        {(t) => (
          <>
            <div className="my-10 text-center">
              <CheckCircle2 className="mx-auto mb-5 text-emerald-600" size={60} />
              <h1>¡Compra realizada con éxito!</h1>
              <p className="muted mt-4">Prepárate para vivir una gran experiencia.</p>
            </div>
            <TarjetaTicket ticket={t} detalle />
            <div className="mt-8 flex flex-wrap justify-center gap-4">
              <Link className="btn bg-marca-600 text-white" to={RUTAS.tickets}>
                Ver mis tickets
              </Link>
              <Link className="btn border border-slate-300" to={RUTAS.eventos}>
                Seguir explorando
              </Link>
            </div>
          </>
        )}
      </Datos>
    </div>
  )
}
