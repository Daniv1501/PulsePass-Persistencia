export const precioCOP = (value: number) =>
  new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(value)
export const fecha = (value: string) =>
  new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'long',
    timeStyle: value.includes('T') ? 'short' : undefined,
  }).format(new Date(value.includes('T') ? value : `${value}T12:00:00`))
export const categorias: Record<string, string> = {
  MUSIC: 'Música',
  SPORTS: 'Deportes',
  TECHNOLOGY: 'Tecnología',
  EDUCATION: 'Educación',
  CULTURE: 'Cultura',
  ENTERTAINMENT: 'Entretenimiento',
}
