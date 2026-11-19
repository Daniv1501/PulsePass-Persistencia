import { useId, useState, type ComponentProps } from 'react'
import { Eye, EyeOff } from 'lucide-react'

interface Props extends Omit<ComponentProps<'input'>, 'type' | 'value'> {
  valor: string
  error?: string
}

export default function ClaveRegistro({ valor, error, ...props }: Props) {
  const id = useId()
  const [visible, setVisible] = useState(false)
  // Orientación visual: no añade condiciones a la validación del registro.
  const longitud = [...valor].length
  const variedad = [/\p{Ll}/u, /\p{Lu}/u, /\p{N}/u, /[^\p{L}\p{N}\s]/u].filter((regla) =>
    regla.test(valor),
  ).length
  const nivel = !valor
    ? 0
    : longitud < 12 || variedad < 2
      ? 1
      : longitud >= 16 && variedad >= 3
        ? 3
        : 2
  const etiquetas = ['sin evaluar', 'débil', 'media', 'fuerte']
  const segmentos = [0, 2, 5, 8][nivel]
  return (
    <div className="field sm:col-span-2">
      <div className="text-sm font-semibold">
        <label htmlFor={id}>Contraseña</label>
        <span aria-hidden="true"> (mín. 12 caracteres)</span>
      </div>
      <div className="relative">
        <input
          {...props}
          id={id}
          value={valor}
          type={visible ? 'text' : 'password'}
          autoComplete="new-password"
          className="pr-14"
          aria-invalid={!!error}
          aria-describedby={`${id}-ayuda ${id}-fortaleza${error ? ` ${id}-error` : ''}`}
        />
        <button
          type="button"
          className="absolute inset-y-0 right-1 flex w-11 items-center justify-center rounded-lg text-slate-600 dark:text-slate-300"
          aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
          aria-controls={id}
          aria-pressed={visible}
          onClick={() => setVisible(!visible)}
        >
          {visible ? <EyeOff size={18} aria-hidden="true" /> : <Eye size={18} aria-hidden="true" />}
        </button>
      </div>
      <p id={`${id}-ayuda`} className="muted text-xs">
        Entre 12 caracteres y 72 bytes.
      </p>
      <div className="flex gap-1" aria-hidden="true">
        {Array.from({ length: 8 }, (_, indice) => (
          <span
            key={indice}
            className={
              indice < segmentos
                ? 'h-1.5 flex-1 rounded-full bg-marca-600 dark:bg-marca-300'
                : 'h-1.5 flex-1 rounded-full bg-slate-300 dark:bg-slate-600'
            }
          />
        ))}
      </div>
      <p id={`${id}-fortaleza`} className="muted text-xs" role="status" aria-live="polite">
        Fortaleza: {etiquetas[nivel]}
      </p>
      {error && (
        <span id={`${id}-error`} role="alert" className="text-sm text-red-600 dark:text-red-300">
          {error}
        </span>
      )}
    </div>
  )
}
