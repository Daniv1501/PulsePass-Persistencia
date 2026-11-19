import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { z } from 'zod'
import { api } from '../features/api'
import { configurarSesion } from '../lib/http'
import type { Principal, Rol } from '../features/tipos'
const Stored = z.object({ token: z.string().min(1), expiraEn: z.number() })
type Sesion =
  | { estado: 'cargando' }
  | { estado: 'anonimo'; expirada?: boolean }
  | { estado: 'autenticado'; token: string; expiraEn: number; usuario: Principal }
interface Auth {
  sesion: Sesion
  iniciarSesion: (identifier: string, password: string) => Promise<void>
  cerrarSesion: () => void
  tieneRol: (...roles: Rol[]) => boolean
}
const Context = createContext<Auth | null>(null)
export function AuthProvider({ children }: { children: ReactNode }) {
  const [sesion, setSesion] = useState<Sesion>({ estado: 'cargando' })
  const client = useQueryClient()
  function limpiar(expirada = false) {
    sessionStorage.removeItem('pulsepass.sesion')
    sessionStorage.removeItem('pulsepass.creados')
    configurarSesion(null)
    client.clear()
    setSesion({ estado: 'anonimo', expirada })
  }
  useEffect(() => {
    let active = true
    configurarSesion(null, () => {
      if (active) limpiar(true)
    })
    let stored: unknown
    try {
      stored = JSON.parse(sessionStorage.getItem('pulsepass.sesion') || 'null')
    } catch {
      stored = null
    }
    const parsed = Stored.safeParse(stored)
    if (!parsed.success || parsed.data.expiraEn <= Date.now()) {
      sessionStorage.removeItem('pulsepass.sesion')
      if (parsed.success) limpiar(true)
      else {
        sessionStorage.removeItem('pulsepass.creados')
        setSesion({ estado: 'anonimo' })
      }
      return () => {
        active = false
      }
    }
    const value = parsed.data
    configurarSesion(value.token)
    void client
      .fetchQuery({ queryKey: ['sesion'], queryFn: api.me, staleTime: 0 })
      .then((usuario) => {
        if (active) setSesion({ estado: 'autenticado', ...value, usuario })
      })
      .catch(() => {
        if (active) limpiar(true)
      })
    return () => {
      active = false
    }
    // Restore once on mounting; all subsequent changes use iniciarSesion/limpiar.
  }, [])
  useEffect(() => {
    if (sesion.estado !== 'autenticado') return
    const timer = setTimeout(() => limpiar(true), Math.max(0, sesion.expiraEn - Date.now()))
    return () => clearTimeout(timer)
  }, [sesion])
  async function iniciarSesion(identifier: string, password: string) {
    const response = await api.login({ identifier, password })
    const value = { token: response.accessToken, expiraEn: Date.now() + response.expiresIn * 1000 }
    configurarSesion(value.token)
    try {
      const usuario = await client.fetchQuery({
        queryKey: ['sesion'],
        queryFn: api.me,
        staleTime: 0,
      })
      sessionStorage.setItem('pulsepass.sesion', JSON.stringify(value))
      setSesion({ estado: 'autenticado', ...value, usuario })
    } catch (error) {
      limpiar()
      throw error
    }
  }
  const tieneRol = (...roles: Rol[]) =>
    sesion.estado === 'autenticado' && roles.some((r) => sesion.usuario.roles.includes(r))
  return (
    <Context.Provider value={{ sesion, iniciarSesion, cerrarSesion: () => limpiar(), tieneRol }}>
      {children}
    </Context.Provider>
  )
}
export function useAuth() {
  const context = useContext(Context)
  if (!context) throw new Error('AuthProvider requerido')
  return context
}
