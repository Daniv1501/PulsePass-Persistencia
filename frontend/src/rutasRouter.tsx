import type { RouteObject } from 'react-router'
import Layout from './componentes/Layout'
import { Cargando } from './componentes/ui'
import Inicio from './pages/Inicio'
import { RutaProtegida, RutaPorRol, SoloAnonimo } from './auth/Guardas'
import { ErrorPagina } from './pages/ErrorPagina'
import { PATRONES, RUTAS } from './lib/rutas'
export const rutas: RouteObject[] = [
  {
    path: RUTAS.inicio,
    Component: Layout,
    HydrateFallback: Cargando,
    errorElement: <ErrorPagina />,
    children: [
      { index: true, Component: Inicio },
      {
        path: RUTAS.eventos,
        lazy: async () => ({ Component: (await import('./pages/Catalogo')).default }),
      },
      {
        path: PATRONES.evento,
        lazy: async () => ({ Component: (await import('./pages/DetalleEvento')).default }),
      },
      {
        path: RUTAS.artistas,
        lazy: async () => ({ Component: (await import('./pages/Directorio')).ListaArtistas }),
      },
      {
        path: PATRONES.artista,
        lazy: async () => ({ Component: (await import('./pages/Directorio')).DetalleArtista }),
      },
      {
        path: RUTAS.sedes,
        lazy: async () => ({ Component: (await import('./pages/Directorio')).ListaSedes }),
      },
      {
        path: PATRONES.sede,
        lazy: async () => ({ Component: (await import('./pages/Directorio')).DetalleSede }),
      },
      {
        Component: SoloAnonimo,
        children: [
          {
            path: RUTAS.login,
            lazy: async () => ({ Component: (await import('./pages/Acceso')).Login }),
          },
          {
            path: RUTAS.registro,
            lazy: async () => ({ Component: (await import('./pages/Acceso')).Registro }),
          },
        ],
      },
      {
        element: <RutaProtegida />,
        children: [
          {
            path: RUTAS.cuenta,
            lazy: async () => ({ Component: (await import('./pages/Cuenta')).default }),
          },
        ],
      },
      {
        element: <RutaPorRol roles={['USER']} />,
        children: [
          {
            path: RUTAS.tickets,
            lazy: async () => ({ Component: (await import('./pages/Tickets')).MisTickets }),
          },
          {
            path: PATRONES.ticket,
            lazy: async () => ({ Component: (await import('./pages/Tickets')).DetalleTicket }),
          },
          {
            path: PATRONES.confirmacion,
            lazy: async () => ({ Component: (await import('./pages/Tickets')).ConfirmacionCompra }),
          },
        ],
      },
      {
        element: <RutaPorRol roles={['STAFF', 'ADMIN']} />,
        children: [
          {
            path: RUTAS.validacion,
            lazy: async () => ({ Component: (await import('./pages/Staff')).ValidacionIngreso }),
          },
          {
            path: PATRONES.pagados,
            lazy: async () => ({ Component: (await import('./pages/Staff')).TicketsPagadosEvento }),
          },
        ],
      },
      {
        element: <RutaPorRol roles={['ADMIN']} />,
        children: [
          {
            path: RUTAS.admin,
            lazy: async () => ({ Component: (await import('./pages/Admin')).AdminEventos }),
          },
          {
            path: RUTAS.crear,
            lazy: async () => ({ Component: (await import('./pages/Admin')).AdminCrearEvento }),
          },
          {
            path: PATRONES.gestion,
            lazy: async () => ({ Component: (await import('./pages/Admin')).AdminGestionEvento }),
          },
        ],
      },
      {
        path: RUTAS.sinPermisos,
        lazy: async () => ({ Component: (await import('./pages/Errores')).SinPermisos }),
      },
      {
        path: PATRONES.noEncontrada,
        lazy: async () => ({ Component: (await import('./pages/Errores')).NoEncontrada }),
      },
    ],
  },
]
