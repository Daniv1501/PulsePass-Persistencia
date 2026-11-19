# Verificación del PRD de frontend PulsePass

Documento revisado: `PRD_PulsePass_Frontend_React_TypeScript_Tailwind (1).pdf`, versión 1.0, 63 páginas. Auditoría del 16 de noviembre de 2026. Las instrucciones del PDF se usan como criterios del taller; las decisiones posteriores del usuario prevalecen sobre sus ejemplos visuales.

Estados: **Cumple** = implementación revisada y evidencia indicada; **Adaptado** = cambio solicitado por el usuario; **Pendiente de comprobar** = no se afirma una verificación que no se ejecutó.

## Las once etapas del taller (sección 29)

| Etapa | Estado | Implementación y evidencia |
| --- | --- | --- |
| 1. Contrato | Cumple | `src/features/{eventos,artistas,sedes,usuarios,tickets,auth,comun}/tipos.ts`; esquemas Zod contrastados con los DTO Java. `features/tipos.ts` conserva exportaciones compatibles. Colección de referencia: `../../tests/e2e/postman/PulsePass-Manual-E2E.postman_collection.json`. |
| 2. Proyecto base | Cumple | React 19, TS strict, Vite, Tailwind 4, Router 8, ESLint y Prettier con orden de clases Tailwind; proxy `/api`, `.env.example`, tokens `@theme`. |
| 3. UI genérica | Cumple | `componentes/ui.tsx`: Boton, Campo, Selector, Badge, TarjetaEvento, Modal nativo, Tabla genérica, Datos, Cargando, Aviso. Pruebas de renderizado e interacción. |
| 4. Router y layouts | Cumple | Un `createBrowserRouter` y `RouterProvider` desde `react-router/dom`; configuración separada en `rutasRouter.tsx`, rutas en `lib/rutas.ts`; layout, guardas, 403, 404 y errorElement. `rutas.test.tsx` cuenta las 20 páginas y comprueba lazy. |
| 5. Cliente HTTP | Cumple | `lib/http.ts`, ApiError, Bearer, Zod, errores seguros; `http.test.ts`. |
| 6. Catálogo público | Cumple | Inicio, Cartelera, DetalleEvento y Directorio; filtros URL, artista por nombre, perfil y sedes. `catalogo`, `diseno`, `flujos` y `estados.test.tsx`. |
| 7. Autenticación | Cumple | Registro/login/cuenta, restauración, vencimiento y salida; guardas por rol; `auth.test.tsx` y `rutas.test.tsx`. JWT dura 15 minutos según la configuración del backend solicitada por el usuario. |
| 8. Tickets de usuario | Cumple | Compra con email del principal, confirmación, lista/filtros, detalle, copiar código y cancelación modal. `flujos`, `paginas` y `auditoria.test.tsx`. |
| 9. STAFF | Cumple | Buscar ticket, registrar ingreso, repetir y mostrar 409; tickets pagados con tabla que se desplaza dentro de su contenedor. |
| 10. ADMIN | Cumple | Buscar por código, crear, sedes activas, publicar con confirmación y asociar artista. Pruebas de 400, 409 y actualización de datos. |
| 11. Calidad | Ver resultados | Informes en `auditoria/`, comandos de comprobación y límites de verificación al final. |

## Principios FE-001 a FE-014

| ID | Estado | Comprobación |
| --- | --- | --- |
| FE-001 | Cumple | No se calcula edad del comprador, disponibilidad, capacidad ni transiciones de negocio. Los rechazos 409 vienen del servidor. Ocultar/deshabilitar acciones por estado es presentación. |
| FE-002 | Cumple | Zod valida respuestas, formularios, sesión persistida y parámetros externos; tipos de DTO derivados con `z.infer`. |
| FE-003 | Cumple | Navegación de producto centralizada en RUTAS y PATRONES; parámetros codificados con encodeURIComponent. |
| FE-004 | Cumple | RutaProtegida y RutaPorRol; el backend mantiene la autorización real. |
| FE-005 | Cumple | TanStack Query controla datos, carga, errores, caché e invalidación. No se copia `query.data` a useState. El historial ADMIN de la pestaña es auxiliar, no un listado completo del servidor. |
| FE-006 | Cumple | Sesión con unión discriminada `estado`; estados de consultas/mutaciones de TanStack Query. Booleanos locales se limitan a UI como abrir un modal. |
| FE-007 | Cumple | Un router de datos; sin BrowserRouter ni dependencia react-router-dom. |
| FE-008 | Cumple | Clases completas, variantes declaradas y clsx; sin interpolar nombres parciales de clases. |
| FE-009 | Cumple | Verificación a 360 y 1440 px, ambos temas, 20 páginas; sin overflow horizontal de documento. |
| FE-010 | Cumple en auditoría automática | Labels, alt, foco visible, skip link, diálogo nativo y regiones de aviso; axe en navegador. Esto no sustituye una evaluación humana completa WCAG. |
| FE-011 | Cumple | Componentes funcionales, props tipadas, sin React.FC ni any; errores unknown con narrowing. |
| FE-012 | Cumple | 19 páginas lazy; Inicio y Layout inmediatos. Prueba de la configuración real. |
| FE-013 | Cumple | JWT solo en memoria/sessionStorage y encabezado Authorization; nunca en query string o consola. |
| FE-014 | Cumple | Datos cubre carga/error/vacío/éxito de pantallas de datos. Formularios estáticos no inventan estados de lista vacía; muestran validación, envío y resultado. |

## Requisitos funcionales de la sección 7

Todos los siguientes están implementados; cada fila identifica su evidencia principal.

| IDs | Comportamiento | Evidencia |
| --- | --- | --- |
| FR-FE-EVT-001 | Lista de publicados y vacío | Catalogo; catalogo/estados |
| FR-FE-EVT-002 | Texto/categoría conservados en URL | Catalogo; catalogo |
| FR-FE-EVT-003 | Detalle y recurso no disponible | DetalleEvento; flujos/estados |
| FR-FE-EVT-004 | Eventos por artista | Catalogo y DetalleArtista; flujos |
| FR-FE-EVT-005 | Badge y compra solo habilitada en PUBLISHED | DetalleEvento; flujos |
| FR-FE-ART-001 | Artistas activos | ListaArtistas; paginas/estados |
| FR-FE-ART-002 | Perfil y eventos | DetalleArtista; flujos/estados |
| FR-FE-ART-003 | Buscar por nombre artístico | Catalogo, resolución `by-stage-name`, con carga y error visibles |
| FR-FE-VEN-001 | Sedes activas | ListaSedes; paginas/estados |
| FR-FE-VEN-002 | Detalle por código | DetalleSede; flujos/estados |
| FR-FE-AUTH-001 | Registro, 201, duplicado genérico | Acceso; auth |
| FR-FE-AUTH-002 | Login y retorno al destino | Acceso, Guardas; auth/flujos/rutas |
| FR-FE-AUTH-003 | 401 de credenciales genérico | Acceso, http; auth |
| FR-FE-AUTH-004 | Restaurar sesión en pestaña | AuthContext; auth |
| FR-FE-AUTH-005 | 401 elimina sesión/caché y lleva a login con aviso | AuthContext/Layout; rutas y http |
| FR-FE-AUTH-006 | Salida elimina datos privados | AuthContext; auth |
| FR-FE-AUTH-007 | Cuenta propia, consulta por email y username | Cuenta; estados y revisión de API |
| FR-FE-TKT-001 | Compra y tipos, precio del servidor | DetalleEvento; flujos |
| FR-FE-TKT-002 | Anónimo vuelve tras login | DetalleEvento/Guardas; flujos |
| FR-FE-TKT-003 | Tickets propios y filtros | Tickets; paginas/estados |
| FR-FE-TKT-004 | Detalle/código/fecha/COP | Tickets; flujos/estados |
| FR-FE-TKT-005 | Cancelación confirmada y actualización | Tickets; paginas/auditoria |
| FR-FE-STF-001 | Buscar y usar; segundo ingreso 409 | Staff; paginas/auditoria |
| FR-FE-STF-002 | Tabla de pagados y uso | Staff; flujos/estados |
| FR-FE-ADM-001 | Crear, validar, mapear 400, mostrar 409 | Admin; flujos/paginas |
| FR-FE-ADM-002 | Selector de sedes reales | Admin; flujos/estados |
| FR-FE-ADM-003 | Buscar/gestionar borrador por código | Admin; revisión de formulario y detalle |
| FR-FE-ADM-004 | Publicar con confirmación | Admin; flujos |
| FR-FE-ADM-005 | Asociar y duplicado 409 | Admin; auditoria |
| FR-FE-GEN-001 | Navbar por roles, menú móvil y footer | Layout; auditoria |
| FR-FE-GEN-002 | 403/404/errorElement | Errores/ErrorPagina; paginas/rutas |
| FR-FE-GEN-003 | Avisos accesibles, éxito 4 s, error descartable | Notificaciones/ui; auditoria |
| FR-FE-GEN-004 | Preferencia del sistema y alternancia persistida | main/Layout/CSS; axe en ambos temas |

## Las 20 páginas y los contratos HTTP (secciones 8 a 17)

El mapa completo de rutas y roles está en [README](../README.md#4-rutas-y-roles). `rutas.test.tsx` verifica que son 20 y que las 19 distintas de Inicio tienen carga diferida. La auditoría de navegador visita las 20 con datos ficticios y roles conocidos; no usa credenciales ni JWT reales.

La tabla 10.3 enumera 20 operaciones de negocio y 3 entradas de autenticación, pero **registro (`POST /api/users`) aparece en ambos grupos**. Son 22 combinaciones distintas de método/ruta, todas alcanzables desde la UI. No se creó un segundo endpoint de registro para forzar un total de 23.

Las llamadas y sus esquemas se organizan por funcionalidad en `features/*/api.ts` y `tipos.ts`. `features/api.ts` conserva una fachada común. `Cuenta` permite actualizar mediante email o username propios. ADMIN consulta por código y recuerda lo creado en la pestaña porque la API no ofrece listado general de borradores; no se inventa ese endpoint.

Validaciones de formularios: RHF + Zod; campos requeridos, formatos, límites de longitud, fecha válida y contraseña de al menos 12 puntos de código y máximo 72 bytes UTF-8. No se bloquea la compra por edad o capacidad calculada en cliente. Los detalles 400 se mapean a los campos conocidos y también quedan disponibles en el aviso general para no perder campos desconocidos.

Errores: 400 por campo/general; 401 de login genérico; 401 de sesión limpia y redirige; 403 de carga redirige a permisos, salvo recursos privados que muestran “no disponible”; 404 contextual; 409 conserva el mensaje de negocio; red/5xx usan mensajes seguros y reintento manual. Una consulta reintenta automáticamente una vez en red/5xx; nunca reintenta mutaciones ni 4xx.

Tiempos de caché: publicados/por artista 60 s, evento 30 s, artistas/sedes 300 s, usuario 60 s, ticket/pagados 15 s. Las seis mutaciones de negocio invalidan las familias `tickets`, `ticket`, `evento`, `eventos`; respuestas de evento también se incorporan a su detalle. Salir o expirar limpia la caché. Los tests de auditoría ejercitan cada API de mutación contra MSW.

## Diseño de la sección 9

| Requisito | Estado | Observación |
| --- | --- | --- |
| DES-01: iconos SVG | Cumple | Lucide y logo SVG de onda de sonido. |
| DES-02: portadas sin fotografías | Adaptado | El usuario pidió el rediseño de festival con imágenes de ambiente; no se revierte. Créditos en `public/images/CREDITOS.md`. |
| DES-03: navegación por rol e iniciales | Cumple | Enlaces públicos/USER/STAFF/ADMIN, cuenta con iniciales, acceso a consulta de pagados. |
| DES-04: administración estrecha | Cumple | Contenedor `narrow`, max-w-3xl. |
| DES-05: códigos monoespaciados | Cumple | Eventos y tickets con font-mono. |
| DES-06: filtros de tickets | Cumple | Chips por estado. |
| DES-07: compra móvil fija | Cumple | Barra mobile-purchase. |
| DES-08: índigo/rosa y Poppins | Adaptado | Se conserva azul océano/turquesa y tipografía del rediseño autorizado. |
| DES-09: botones táctiles | Cumple | Boton con min-h-11 (44 px), cierre de avisos 44 px. |
| DES-10: error 409 en modal | Cumple | Aviso con role=alert y diálogo abierto. |
| DES-11: portada de referencia | Adaptado | Portada de festival y buscador solicitados por el usuario; navegación y flujos del PRD conservados. |

No se afirma coincidencia píxel a píxel con las 32 capturas del PDF: sería incompatible con el rediseño solicitado después. La equivalencia funcional, móvil y oscuro sí se comprueba.

## Criterios de aceptación AC-FE-001 a 022

| IDs | Resultado y evidencia |
| --- | --- |
| 001–004 | Cartelera anónima, detalle, 404 y compra anónima: catalogo/flujos. |
| 005–009 | Registro inválido sin HTTP, registro exitoso, duplicado, login 401 y sesión: auth. |
| 010–011 | Compra con identidad de sesión y 409 de negocio dentro del modal: flujos. |
| 012–014 | Cancelación actualiza listado, USED sin cancelación y ticket ajeno no disponible: paginas/auditoria/flujos. |
| 015–016 | USER no accede a ADMIN; STAFF registra ingreso y repetir da 409: auth/auditoria. |
| 017–018 | Creación vacía valida, publicación actualiza estado e invalida cartelera: paginas/flujos/auditoria. |
| 019–020 | 401 durante consulta limpia y conserva destino; URL desconocida muestra 404: rutas. |
| 021 | Revisión de componentes: reglas de negocio permanecen en backend. |
| 022 | Cobertura de todas las operaciones; véase aclaración del registro duplicado en el conteo. |

## Calidad QT-FE-001 a 011 y NFR-FE-001 a 010

| ID | Resultado |
| --- | --- |
| QT-001 | `estados.test.tsx` parametriza pantallas de datos con carga, éxito, error y vacío donde hay listas; formularios/operación y páginas estáticas tienen pruebas de interacción o render en los demás archivos. |
| QT-002 | Testing Library usa roles/labels y texto visible; no selecciona por clases CSS. |
| QT-003 / 006 | Validación y envío de formularios; MSW detecta peticiones inesperadas. Casos inválidos de login/registro verifican cero solicitudes. |
| QT-004 | Guardas anónimo/USER/STAFF/ADMIN en auth; navbar por rol en auditoria. |
| QT-005 / 007 | http prueba 400/401/403/404/409/500 y Authorization; flujos comprueban su presentación. |
| QT-008 | Las seis mutaciones ejercitadas con API/MSW y caché; actualización visible de cancelación, uso, publicación y asociación. |
| QT-009 | MSW sin red real, fixtures y fecha fija de prueba; temporizadores controlados en prueba de 4 s. |
| QT-010 | Ejecutar lint/build/test; resultados finales en `auditoria/RESULTADOS.md`. |
| QT-011 | Axe en 20 páginas × 2 anchos × 2 temas, más 12 estados de formularios inválidos, cero violaciones detectadas; informe JSON adjunto. |
| NFR-001 | Auditoría automática y pruebas de foco/Escape/diálogo. WCAG completo requiere evaluación humana con tecnologías de asistencia. |
| NFR-002 | 360/1440 px sin overflow de documento en 92 comprobaciones. |
| NFR-003 | Lazy confirmado por test y chunks de producción; informe Lighthouse de Inicio adjunto. |
| NFR-004 | Sin secretos del servidor en frontend; JWT fuera de URL/logs/localStorage; renderizado de errores como texto. |
| NFR-005 / 006 | TS strict, sin any, contratos Zod y ApiError central. |
| NFR-007 | Contratos/API por funcionalidad, UI compartida, portada y pie separados. Revisión AST de funciones de componentes en producción: ninguna supera 150 líneas. Los módulos que agrupan varios componentes pueden tener más líneas. |
| NFR-008 | `es-CO`, Intl.DateTimeFormat e Intl.NumberFormat COP. |
| NFR-009 | Fecha fija, MSW y pruebas deterministas. |
| NFR-010 | **Pendiente de comprobar** las últimas dos versiones de Chrome, Edge, Firefox y Safari. La auditoría ejecutada usa Chrome instalado; no prueba esa matriz completa ni Safari en Windows. |

## Cambios realizados durante esta revisión

- Avisos de éxito de 4 segundos y botón de cierre; notificaciones accesibles.
- Campo de contraseña de registro ajustado a la imagen de la sección 9.9: ojo para mostrar/ocultar, ayuda de 12 caracteres a 72 bytes y barra de ocho segmentos con fortaleza orientativa. Conserva la validación existente y la paleta elegida por el usuario.
- Detalles 400 visibles y mapeados en login, compra y asociación; errores de búsqueda de artista visibles.
- Reintento de consultas y tiempos de caché del documento; invalidación e incorporación de respuestas de eventos.
- Contratos y API organizados por funcionalidad; teléfono/fecha de nacimiento opcionales compatibles con respuestas reales.
- Límites estructurales de formularios alineados con DTO; contraseña medida en puntos de código y bytes UTF-8.
- RutaPorRol explícita, enlace a consulta de pagados, iniciales de cuenta y limpieza de datos al restaurar una sesión vencida.
- Corregida una carrera entre Login y SoloAnonimo: ambos respetan el mismo destino de retorno validado; se rechazan destinos externos o circulares. El test usa el router real.
- Contraste AA corregido en pie, tablas y acciones oscuras; atributo ARIA de notificaciones corregido.
- Imagen principal precargada, fuentes servidas localmente y portada móvil con un recurso existente más ligero.
- Pruebas ampliadas de estados, rutas reales, expiración durante una consulta, caché y operaciones.

## Alcance de la evidencia

Las pruebas Vitest/MSW y axe con fixtures verifican el frontend, no sustituyen la ejecución manual Postman de otro PDF ni certifican la autorización del backend. Lighthouse mide la compilación de producción local conectada al proxy configurado; sus puntuaciones dependen del equipo y de la carga.

La implementación de los flujos del taller está disponible. Quedan explícitamente separados la adaptación visual autorizada, la evaluación humana completa de accesibilidad y la matriz de versiones de navegadores que no se ha ejecutado. No se etiqueta el proyecto como “100 % certificado” ocultando esos límites.

## Ready y Done (sección 30)

Ready: API y seguridad disponibles localmente en el puerto 8081 mediante el proxy configurado; cuentas de demostración documentadas por el backend; Node compatible instalado; contratos y política de recursos ajenos contrastados. Las tres guías Markdown citadas por el PDF no se adjuntaron a esta revisión; no se declara haberlas leído.

Done: las 20 páginas, router único/lazy, cobertura de operaciones, contratos Zod, RHF, almacenamiento y envío del JWT, guardas, errores, identidad de compra, estados, Tailwind completo, modo oscuro, responsive, pruebas y README se verifican en las tablas anteriores. La comparación visual literal con PNG se marca **adaptada por petición del usuario**. La revisión completa de navegadores y tecnologías de asistencia queda **pendiente de comprobar**, sin confundirla con el resultado automático de axe.

## Respuestas de repaso (sección 31)

1. Ocultar un botón facilita la navegación; una petición puede enviarse directamente. Spring Security y el servicio deben validar el permiso.
2. Zod valida estructura, formato y límites del contrato. El backend determina edad permitida, disponibilidad y reglas de negocio.
3. Un 400 señala datos inválidos; 404 indica un recurso no disponible; 409 comunica un conflicto de negocio. El frontend presenta cada caso en su contexto.
4. El email viene del principal para evitar que el formulario permita comprar en nombre de otra persona. El servidor comprueba también esa identidad.
5. TanStack Query centraliza caché, carga, errores, deduplicación, reintentos e invalidación, reduciendo efectos manuales y datos desactualizados.
6. `z.infer` deriva el tipo de la misma definición que valida en ejecución y evita mantener dos contratos distintos.
7. Un XSS puede leer sessionStorage. Se reduce la exposición al no inyectar HTML de la API, mantener dependencias, no registrar tokens y limitar su duración; el token se borra al expirar o salir.
8. Un 401 cierra la sesión, limpia caché y dirige a login con el destino original. La compra no se reenvía automáticamente después de autenticarse.
9. El router de datos organiza layouts, errores y guardas; lazy evita descargar páginas administrativas en la primera carga pública.
10. Refresh con cookie HttpOnly exigiría cambios de backend, CORS/credenciales según origen, protección CSRF, rotación/revocación y coordinación de renovación en el cliente. Está fuera del taller actual.
11. En móvil la tabla muestra etiquetas por celda y su contenedor gestiona el desplazamiento; el documento completo no desborda.
12. La ausencia de listado completo limita ADMIN y la falta de disponibilidad dificulta anticipar un 409. Pediría primero un listado paginado autorizado de eventos, conservando la seguridad de los borradores.


