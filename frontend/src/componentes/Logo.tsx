interface LogoProps {
  soloIcono?: boolean
  className?: string
}

/** La P y la onda comparten un único SVG en todas las variantes de la marca. */
export default function Logo({ soloIcono = false, className = '' }: LogoProps) {
  return (
    <span className={`pulsepass-logo ${className}`} aria-hidden="true">
      <img
        className="pulsepass-logo-mark"
        src="/pulsepass-wave.svg"
        alt=""
        width={40}
        height={40}
      />
      {!soloIcono && (
        <span className="pulsepass-logo-name">
          Pulse<span>Pass</span>
        </span>
      )}
    </span>
  )
}
