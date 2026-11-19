import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { useQuery } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Buscar, Codigo, type Ticket } from '../features/tipos'
import { api } from '../features/api'
import { useAccion } from '../features/hooks'
import { RUTAS } from '../lib/rutas'
import { precioCOP } from '../lib/formato'
import { mensaje } from '../lib/http'
import { Aviso, Badge, Boton, Cabecera, Campo, Datos, Tabla } from '../componentes/ui'
function Buscador({ label, onBuscar }: { label: string; onBuscar: (value: string) => void }) {
  const form = useForm<z.infer<typeof Buscar>>({ resolver: zodResolver(Buscar) })
  return (
    <form
      className="search-form"
      noValidate
      onSubmit={form.handleSubmit((value) => onBuscar(value.code))}
    >
      <Campo
        etiqueta={label}
        autoFocus={label === 'Código de ticket'}
        {...form.register('code')}
        error={form.formState.errors.code?.message}
      />
      <Boton>Buscar</Boton>
    </form>
  )
}
export function ValidacionIngreso() {
  const [code, setCode] = useState('')
  const [exito, setExito] = useState(false)
  const query = useQuery({
    queryKey: ['ticket', code],
    queryFn: () => api.ticket(code),
    enabled: !!code,
    staleTime: 15000,
  })
  const mutation = useAccion(api.usar)
  const navigate = useNavigate()
  return (
    <>
      <Cabecera
        titulo="Validación de ingreso"
        descripcion="Busca una entrada y registra el acceso al evento."
      />
      <section className="card max-w-3xl p-6">
        <Buscador
          key={exito ? 'ingreso-registrado' : 'buscar-ticket'}
          label="Código de ticket"
          onBuscar={(value) => {
            setCode(value)
            setExito(false)
            mutation.reset()
            if (value === code) void query.refetch()
          }}
        />
        {exito && <Aviso exito>Ingreso registrado.</Aviso>}
        {code && (
          <Datos query={query} noDisponible="Ticket no encontrado">
            {(t) => (
              <div className="mt-7">
                <div className="flex justify-between gap-4">
                  <h2>{t.eventName}</h2>
                  <Badge estado={t.status} />
                </div>
                <p className="my-4 font-mono">{t.ticketCode}</p>
                <p className="muted mb-5">
                  {t.type} · Titular: {t.userEmail}
                </p>
                {mutation.isError && <Aviso>{mensaje(mutation.error)}</Aviso>}
                <div className="flex flex-wrap gap-3">
                  <Boton
                    cargando={mutation.isPending}
                    onClick={() =>
                      mutation.mutate(t.ticketCode, { onSuccess: () => setExito(true) })
                    }
                  >
                    Marcar como usado
                  </Boton>
                  <Boton variante="secundario" onClick={() => navigate(RUTAS.pagados(t.eventCode))}>
                    Tickets pagados del evento
                  </Boton>
                </div>
              </div>
            )}
          </Datos>
        )}
        <div id="tickets-pagados" className="mt-8 border-t border-slate-200 pt-6">
          <h3>Consultar tickets pagados</h3>
          <Buscador
            label="Código del evento"
            onBuscar={(value) => navigate(RUTAS.pagados(value))}
          />
        </div>
      </section>
    </>
  )
}
export function TicketsPagadosEvento() {
  const { eventCode } = useParams()
  const parsed = Codigo.safeParse(eventCode)
  const navigate = useNavigate()
  const query = useQuery({
    queryKey: ['tickets', 'pagados', eventCode],
    queryFn: () => api.pagados(parsed.success ? parsed.data : ''),
    enabled: parsed.success,
    staleTime: 15000,
  })
  const mutation = useAccion(api.usar)
  if (!parsed.success) return <Cabecera titulo="Evento no disponible" />
  return (
    <>
      <Cabecera titulo={`Tickets pagados · ${eventCode}`} />
      <Buscador label="Código del evento" onBuscar={(value) => navigate(RUTAS.pagados(value))} />
      {mutation.isError && <Aviso>{mensaje(mutation.error)}</Aviso>}
      {mutation.isSuccess && <Aviso exito>Ingreso registrado.</Aviso>}
      <Datos query={query} vacio="No hay tickets pagados">
        {(data) => (
          <section className="card p-5">
            <p className="muted mb-5">Total: {data.length}</p>
            <Tabla<Ticket>
              filas={data}
              columnas={[
                {
                  encabezado: 'Código',
                  celda: (t) => <span className="font-mono">{t.ticketCode}</span>,
                },
                { encabezado: 'Tipo', celda: (t) => t.type },
                { encabezado: 'Titular', celda: (t) => t.userEmail },
                { encabezado: 'Precio', celda: (t) => precioCOP(t.price) },
                { encabezado: 'Estado', celda: (t) => <Badge estado={t.status} /> },
                {
                  encabezado: 'Acción',
                  celda: (t) => (
                    <Boton
                      variante="secundario"
                      cargando={mutation.isPending}
                      onClick={() => mutation.mutate(t.ticketCode)}
                    >
                      Usar
                    </Boton>
                  ),
                },
              ]}
            />
          </section>
        )}
      </Datos>
    </>
  )
}
