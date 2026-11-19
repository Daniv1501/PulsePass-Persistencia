import { type FieldValues, type Path, type UseFormSetError } from 'react-hook-form'
import { ApiError } from './http'
export function erroresCampos<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
  campos: readonly string[],
) {
  if (error instanceof ApiError && error.status === 400)
    for (const [campo, message] of Object.entries(error.details))
      if (campos.includes(campo)) setError(campo as Path<T>, { type: 'server', message })
}
