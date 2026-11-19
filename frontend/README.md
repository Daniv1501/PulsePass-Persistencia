# PulsePass · Frontend — Fase 5

Continuación de las capas de persistencia y servicios existentes. Implementa la SPA del PRD con React 19, TypeScript estricto, Vite, Tailwind CSS 4, React Router 8, TanStack Query, React Hook Form, Zod y Vitest/Testing Library/MSW.

## 1. Ejecutar en Windows

Desde la raíz del repositorio:

```powershell
cd frontend
npm.cmd ci
Copy-Item .env.example .env
npm.cmd run dev
```

Abrir la dirección que imprime Vite (normalmente `http://localhost:5173`). Se usa `npm.cmd` porque PowerShell puede bloquear `npm.ps1` por su política de ejecución. Requiere Node 22.22+; el entorno de implementación tiene Node 26.

## 2. Conectar la API

`VITE_API_BASE_URL=/api` es la URL pública que usa el navegador. `API_PROXY_TARGET=http://localhost:8080` es el destino del proxy de desarrollo; reiniciar Vite después de cambiarlo. No se necesitan secretos en el frontend. En una futura publicación, servir API y SPA bajo el mismo origen o configurar CORS en el servidor; configurar fallback de las rutas de SPA a `index.html`.

El repositorio ya incluye controladores REST y seguridad JWT. Iniciar primero el backend siguiendo [README_BACKEND.md](../README_BACKEND.md); luego iniciar Vite. MSW se usa exclusivamente en pruebas; los datos de la aplicación vienen de la API real.

Los esquemas de eventos, artistas, sedes, usuarios y tickets se contrastaron con los records Java actuales. El ticket incluye `eventName`, `eventCode` y `userEmail`. El registro recibe `password`; `/auth/me` devuelve `userId`, `username`, `email` y `roles`; login devuelve `accessToken`, `tokenType: 'Bearer'` y `expiresIn`.

Para demostración, el perfil E2E del backend crea `e2e-admin` y `e2e-staff` con contraseñas elegidas al arrancar. Los asistentes USER se registran desde la aplicación. Ver [fixtures E2E](../tests/e2e/fixtures/README.md).

## 3. Recorrido de implementación

1. **Contratos** — `src/features/tipos.ts`: enums y esquemas Zod alineados con los DTO; tipos derivados, sin `any`.
2. **Proyecto y diseño** — `vite.config.ts`, `src/index.css`: proxy, Tailwind 4, azul océano/turquesa, Inter/Space Grotesk y tema claro/oscuro. El diseño de festival incluye portada fotográfica, búsqueda por texto/categoría, evento destacado real, tarjetas con fecha y ciudad, navegación con iconos y tickets digitales. Las fotografías de ambiente se sirven desde `public/images/`; sus fuentes están en [CREDITOS.md](public/images/CREDITOS.md).
3. **UI compartida** — `src/componentes/ui.tsx`: botones, campos, selectores, badges, modal nativo con foco/Escape, tabla genérica y estados de carga/vacío/error.
4. **Navegación** — `src/router.tsx`, `src/lib/rutas.ts`: las 20 rutas del PRD, carga diferida, layout, menú móvil y errores 403/404.
5. **HTTP** — `src/lib/http.ts`: Bearer, validación de respuestas, errores tipados y mensajes seguros para red/400/401/403/404/409/500.
6. **Catálogo** — `src/pages/Inicio.tsx`, `Catalogo.tsx`, `DetalleEvento.tsx`, `Directorio.tsx`: eventos, filtros en URL, búsqueda por artista, detalles y sedes.
7. **Sesión** — `src/auth/`: restauración desde sessionStorage, consulta de identidad, expiración, salida y guardas por rol. El tema usa localStorage; el token nunca lo usa.
8. **Compra y tickets** — `src/pages/Tickets.tsx`: compra desde detalle, confirmación, filtros, código copiable y cancelación confirmada. El email sale de la sesión; el precio sale del servidor. Los 409 conservan abierto el modal.
9. **Operación** — `src/pages/Staff.tsx`: búsqueda de tickets, registro de ingreso y tickets pagados por evento.
10. **Administración** — `src/pages/Admin.tsx`: consulta por código, creación, publicación confirmada y asociación de artistas. Los creados se recuerdan en la pestaña; no se inventa un endpoint para listar todos.
11. **Validación** — `src/test/`: pruebas de HTTP, formularios, roles, catálogo, tickets y operación con MSW, sin API real.

## 4. Rutas y roles

| Acceso | Rutas |
| --- | --- |
| Público | `/`, `/eventos`, `/eventos/:eventCode`, `/artistas`, `/artistas/:id`, `/sedes`, `/sedes/:code`, `/403`, ruta 404 |
| Solo anónimo | `/login`, `/registro` |
| Autenticado | `/cuenta` |
| USER | `/mis-tickets`, `/mis-tickets/:ticketCode`, `/compra/confirmacion/:ticketCode` |
| STAFF / ADMIN | `/staff/validacion`, `/staff/eventos/:eventCode/tickets` |
| ADMIN | `/admin/eventos`, `/admin/eventos/nuevo`, `/admin/eventos/:eventCode` |

## 5. Cobertura de operaciones

| Operaciones REST | Acceso desde la interfaz |
| --- | --- |
| Sedes: activas y código | Lista/detalle de sedes; selector de creación |
| Eventos: publicados, código, por artista | Inicio, cartelera, detalle y administración |
| Eventos: crear, publicar, asociar artista | Formularios y acciones ADMIN |
| Artistas: activos, ID, nombre artístico | Directorio, detalle y búsqueda de artista de cartelera |
| Usuarios: registrar, por email, por username | Registro y cuenta (botón de actualización alterna método de consulta de identidad propia) |
| Tickets: comprar, código, por usuario | Detalle de evento, detalle de ticket y mis tickets |
| Tickets: pagados de evento, cancelar, usar | STAFF, cancelación e ingreso |
| Autenticación: login, me, registro con password | Acceso, restauración y registro |

## 6. Comprobar

La revisión paso a paso del PDF, las correcciones y las excepciones de diseño están en [CUMPLIMIENTO_FRONTEND.md](docs/CUMPLIMIENTO_FRONTEND.md). Los resultados medidos están en [RESULTADOS.md](docs/auditoria/RESULTADOS.md).

Las fuentes Inter y Space Grotesk se sirven localmente mediante los paquetes Fontsource Variable (licencia OFL-1.1 incluida en sus paquetes); no se necesita consultar Google Fonts al abrir la aplicación.

```powershell
npm.cmd run lint
npm.cmd run build
npm.cmd test
npm.cmd run preview
```

La compilación valida TypeScript y genera `dist/`. Los tests interceptan HTTP mediante MSW. Para formatear: `npm.cmd exec prettier -- --write src vite.config.ts eslint.config.js`.

## 7. Límites de validación

Las pruebas del backend incluyen integración HTTP con PostgreSQL real; las del frontend usan MSW. Se ejecutaron Lighthouse en la compilación local y axe sobre las 20 pantallas con fixtures, en ambos temas y a 360/1440 px. Los informes están en `docs/auditoria/`. La dirección visual de festival responde a los cambios pedidos por el usuario. El taller excluye pagos reales, QR, refresh tokens y despliegue productivo.

La matriz de las últimas dos versiones de Chrome/Edge/Firefox/Safari y una evaluación humana completa con tecnologías de asistencia quedan pendientes de comprobar. La aceptación E2E manual de Postman tiene su propia bitácora y se completa desde Postman.
