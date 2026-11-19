import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import Logo from '../componentes/Logo'
import ClaveRegistro from '../componentes/ClaveRegistro'
import { Login as LoginSchema, Registro as RegistroSchema } from '../features/tipos'
import { api } from '../features/api'
import { useAuth } from '../auth/AuthContext'
import { RUTAS } from '../lib/rutas'
import { ApiError, mensaje } from '../lib/http'
import { erroresCampos } from '../lib/formulario'
import { Aviso, Boton, Campo } from '../componentes/ui'
import { EstadoNavegacion, destinoTrasLogin } from '../lib/navegacion'
export function Login() {
  const { iniciarSesion } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const state = EstadoNavegacion.safeParse(location.state)
  const data = state.success ? state.data : {}
  const form = useForm<z.infer<typeof LoginSchema>>({ resolver: zodResolver(LoginSchema) })
  return (
    <section className="auth-card card">
      <span className="auth-icon">
        <Logo soloIcono />
      </span>
      <h1>Qué bueno verte</h1>
      <p className="muted mb-7">Inicia sesión y sigue viviendo experiencias.</p>
      {data.registrado && <Aviso exito>Cuenta creada, inicia sesión</Aviso>}
      {data.motivo === 'expirada' && <Aviso>Tu sesión expiró</Aviso>}
      <form
        noValidate
        onSubmit={form.handleSubmit(async (value) => {
          setError('')
          try {
            await iniciarSesion(value.identifier, value.password)
            navigate(destinoTrasLogin(location.state), { replace: true })
          } catch (e) {
            erroresCampos(e, form.setError, ['identifier', 'password'])
            setError(mensaje(e))
          }
        })}
      >
        <Campo
          etiqueta="Usuario o correo electrónico"
          autoComplete="username"
          {...form.register('identifier')}
          error={form.formState.errors.identifier?.message}
        />
        <Campo
          etiqueta="Contraseña"
          type="password"
          autoComplete="current-password"
          {...form.register('password')}
          error={form.formState.errors.password?.message}
        />
        {error && <Aviso>{error}</Aviso>}
        <Boton className="mt-3 w-full" cargando={form.formState.isSubmitting}>
          Iniciar sesión
        </Boton>
      </form>
      <p className="muted mt-6 text-center">
        ¿Primera vez aquí?{' '}
        <Link className="link" to={RUTAS.registro}>
          Crea una cuenta
        </Link>
      </p>
    </section>
  )
}
export function Registro() {
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const form = useForm<z.infer<typeof RegistroSchema>>({ resolver: zodResolver(RegistroSchema) })
  const campos = [
    { name: 'firstName', label: 'Nombres', type: 'text', auto: 'given-name' },
    { name: 'lastName', label: 'Apellidos', type: 'text', auto: 'family-name' },
    { name: 'username', label: 'Usuario', type: 'text', auto: 'username' },
    { name: 'email', label: 'Correo electrónico', type: 'email', auto: 'email' },
    { name: 'birthDate', label: 'Fecha de nacimiento', type: 'date', auto: 'bday' },
    { name: 'phone', label: 'Teléfono (opcional)', type: 'tel', auto: 'tel' },
    { name: 'password', label: 'Contraseña', type: 'password', auto: 'new-password' },
  ] as const
  return (
    <section className="auth-card register-card card">
      <span className="auth-icon">
        <Logo soloIcono />
      </span>
      <h1>Tu próxima historia</h1>
      <p className="muted mb-7">Crea tu cuenta y no te pierdas ningún momento.</p>
      <form
        noValidate
        onSubmit={form.handleSubmit(async (value) => {
          setError('')
          try {
            await api.registrar(value)
            navigate(RUTAS.login, { state: { registrado: true } })
          } catch (e) {
            erroresCampos(
              e,
              form.setError,
              campos.map((c) => c.name),
            )
            setError(
              e instanceof ApiError && e.status === 409
                ? 'El usuario o correo ya está registrado'
                : mensaje(e),
            )
          }
        })}
      >
        <div className="grid gap-x-5 sm:grid-cols-2">
          {campos.map((c) =>
            c.name === 'password' ? (
              <ClaveRegistro
                key={c.name}
                valor={form.watch('password') || ''}
                {...form.register('password')}
                error={form.formState.errors.password?.message}
              />
            ) : (
              <Campo
                key={c.name}
                etiqueta={c.label}
                type={c.type}
                autoComplete={c.auto}
                {...form.register(c.name)}
                error={form.formState.errors[c.name]?.message}
              />
            ),
          )}
        </div>
        {error && <Aviso>{error}</Aviso>}
        <Boton className="w-full" cargando={form.formState.isSubmitting}>
          Crear cuenta
        </Boton>
      </form>
      <p className="muted mt-6 text-center">
        ¿Ya tienes cuenta?{' '}
        <Link className="link" to={RUTAS.login}>
          Inicia sesión
        </Link>
      </p>
    </section>
  )
}
