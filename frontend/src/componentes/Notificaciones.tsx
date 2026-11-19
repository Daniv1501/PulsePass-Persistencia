import { createContext, useContext, useRef, useState, type ReactNode } from 'react'
import { Aviso } from './ui'

const Context = createContext<(texto: string) => void>(() => {})
export function NotificacionesProvider({ children }: { children: ReactNode }) {
  const [avisos, setAvisos] = useState<{ id: number; texto: string }[]>([])
  const siguiente = useRef(0)
  function notificar(texto: string) {
    const id = ++siguiente.current
    setAvisos((actuales) => [...actuales.slice(-3), { id, texto }])
  }
  return (
    <Context.Provider value={notificar}>
      {children}
      <div className="toast-region" role="region" aria-live="polite" aria-label="Notificaciones">
        {avisos.map((aviso) => (
          <Aviso
            key={aviso.id}
            exito
            onCerrar={() => setAvisos((actuales) => actuales.filter((a) => a.id !== aviso.id))}
          >
            {aviso.texto}
          </Aviso>
        ))}
      </div>
    </Context.Provider>
  )
}
export const useNotificar = () => useContext(Context)
