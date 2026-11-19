import { useState } from 'react'
import { Ticket } from 'lucide-react'
import type { ResumenEvento } from '../features/tipos'

interface Props {
  evento: Pick<ResumenEvento, 'eventCode' | 'category'>
  className?: string
}
export function ImagenEvento({ evento, className = '' }: Props) {
  const [fallo, setFallo] = useState(false)
  const musica = evento.category === 'MUSIC' || evento.category === 'ENTERTAINMENT'
  const indice =
    Math.floor([...evento.eventCode].reduce((sum, letter) => sum + letter.charCodeAt(0), 0) / 4) % 2
  return (
    <div className={`event-photo ${className}`} aria-hidden="true">
      {musica && !fallo ? (
        <img
          src={indice === 0 ? '/images/concert.webp' : '/images/festival.webp'}
          alt=""
          loading="lazy"
          decoding="async"
          width={1000}
          height={667}
          onError={() => setFallo(true)}
        />
      ) : (
        <div className="event-photo-placeholder">
          <Ticket size={68} />
        </div>
      )}
      <div className="event-photo-shade" />
      {musica && !fallo && <span className="ambient-label">Imagen de ambiente</span>}
    </div>
  )
}
