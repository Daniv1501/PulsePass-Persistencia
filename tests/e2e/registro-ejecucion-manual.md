# Registro de ejecución manual — PulsePass E2E

Complete una fila por caso **después** de pulsar Send e inspeccionar la respuesta. No pegue tokens, contraseñas ni datos personales reales.

| Campo | Valor |
|---|---|
| Ejecutado por | |
| Fecha | |
| Commit / rama de la API | |
| `baseUrl` | |
| `runId` | |
| Base de datos | `pulsepass_e2e` |

**Resultado:** `PASS` (HTTP, JSON y condición de negocio/seguridad coinciden) · `FAIL` (algo no coincide o hubo un 500) · `BLOCKED` (falta fixture, actor, token o respuesta previa) · `NOT RUN` (aún no ejecutado).


## 1. Preparación

| Caso | Fecha/hora | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| PREP-1 | | — | Base E2E, API y fixtures | — | — | | NOT RUN | |
| TC-PREP-001 | | ANONYMOUS | `GET /api/venues/active` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-PREP-002 | | ANONYMOUS | `GET /api/auth/me` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |

## 2. Flujo A — Descubrimiento público (anónimo)

| Caso | Fecha/hora | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| E2E-VEN-02 | | ANONYMOUS | `GET /api/venues/active` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-VEN-01 | | ANONYMOUS | `GET /api/venues/{{venueActiveCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-VEN-01b | | ANONYMOUS | `GET /api/venues/{{venueInactiveCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-VEN-01c | | ANONYMOUS | `GET /api/venues/VEN-NO-EXISTE` | 404 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-ART-03 | | ANONYMOUS | `GET /api/artists/active` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-ART-01 | | ANONYMOUS | `GET /api/artists/{{artistId}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-ART-01b | | ANONYMOUS | `GET /api/artists/999999` | 404 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-ART-02 | | ANONYMOUS | `GET /api/artists/by-stage-name?stageName={{artistStageName}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-ART-02b | | ANONYMOUS | `GET /api/artists/by-stage-name?stageName=Artista Fantasma` | 404 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-EVT-03 | | ANONYMOUS | `GET /api/events/published` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-EVT-02 | | ANONYMOUS | `GET /api/events/{{publishedEventCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-004 | | ANONYMOUS | `GET /api/events/{{draftEventCode}}` | 404 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-005 | | ANONYMOUS | `GET /api/events/by-artist?stageName={{artistStageName}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |

## 3. Flujo B — Registro

| Caso | Fecha/hora | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| TC-AUTH-001 | | ANONYMOUS | `POST /api/users` | 201 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-USR-01b | | ANONYMOUS | `POST /api/users` | 201 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-USR-01c | | ANONYMOUS | `POST /api/users` | 201 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-USR-01d | | ANONYMOUS | `POST /api/users` | 201 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-USR-01e | | ANONYMOUS | `POST /api/users` | 201 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-016 | | ANONYMOUS | `POST /api/users` | 201 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-006a | | ANONYMOUS | `POST /api/users` | 409 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-006b | | ANONYMOUS | `POST /api/users` | 409 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |

## 4. Flujo B — Roles privilegiados, login y /me

| Caso | Fecha/hora | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| SQL-1 | | — | Conceder STAFF y ADMIN (fuera de HTTP) | — | — | | NOT RUN | |
| TC-AUTH-002 | | ANONYMOUS | `POST /api/auth/login` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-AUTH-003 | | ANONYMOUS | `POST /api/auth/login` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-AUTH-01b | | ANONYMOUS | `POST /api/auth/login` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-AUTH-01c | | ANONYMOUS | `POST /api/auth/login` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-AUTH-01d | | ANONYMOUS | `POST /api/auth/login` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-AUTH-01e | | ANONYMOUS | `POST /api/auth/login` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-AUTH-01f | | ANONYMOUS | `POST /api/auth/login` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-AUTH-004 | | ANONYMOUS | `POST /api/auth/login` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-AUTH-005 | | ANONYMOUS | `POST /api/auth/login` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-AUTH-006 | | USER_A | `GET /api/auth/me` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-AUTH-02b | | USER_B | `GET /api/auth/me` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-AUTH-02c | | STAFF | `GET /api/auth/me` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-AUTH-02d | | ADMIN | `GET /api/auth/me` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-AUTH-02f | | ROLE_INJ | `GET /api/auth/me` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |

## 5. Flujo B — Tokens inválidos y rol insuficiente

| Caso | Fecha/hora | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| TC-AUTH-008 | | ANONYMOUS | `GET /api/auth/me` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-AUTH-009 | | ANONYMOUS | `GET /api/auth/me` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-AUTH-011a | | ANONYMOUS | `GET /api/auth/me` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-AUTH-011b | | ANONYMOUS | `GET /api/auth/me` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-AUTH-012 | | ANONYMOUS | `GET /api/auth/me?access_token={{userAToken}}` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-AUTH-015 | | ANONYMOUS | `GET /api/events/published` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-AUTH-014 | | USER_A | `PATCH /api/events/{{draftEventCode}}/publish` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |

## 6. Usuarios — propiedad de datos personales

| Caso | Fecha/hora | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| E2E-USR-02 | | USER_A | `GET /api/users/by-email?email={{userAEmail}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-USR-03 | | USER_A | `GET /api/users/by-username?username={{userAUsername}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-006 | | USER_B | `GET /api/users/by-email?email={{userAEmail}}` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-007 | | USER_B | `GET /api/users/by-username?username={{userAUsername}}` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-USR-03b | | STAFF | `GET /api/users/by-username?username={{userAUsername}}` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-USR-02b | | ADMIN | `GET /api/users/by-email?email={{userAEmail}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-USR-03c | | ADMIN | `GET /api/users/by-username?username=nadie_{{runId}}` | 404 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-USR-02c | | ANONYMOUS | `GET /api/users/by-email?email={{userAEmail}}` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |

## 7. Flujo C — Catálogo (ADMIN)

| Caso | Fecha/hora | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| TC-SEC-001a | | ANONYMOUS | `POST /api/events` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-001b | | USER_A | `POST /api/events` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-001c | | STAFF | `POST /api/events` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-003 | | ADMIN | `POST /api/events` | 201 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-EVT-02b | | ANONYMOUS | `GET /api/events/{{eventToPublishCode}}` | 404 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-EVT-03b | | ANONYMOUS | `GET /api/events/published` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-003a | | USER_A | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-003b | | STAFF | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-002a | | USER_A | `PATCH /api/events/{{eventToPublishCode}}/publish` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-002b | | STAFF | `PATCH /api/events/{{eventToPublishCode}}/publish` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-EVT-02c | | ADMIN | `GET /api/events/{{eventToPublishCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-015 | | ADMIN | `GET /api/events/{{draftEventCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-EVT-02d | | STAFF | `GET /api/events/{{draftEventCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-EVT-05 | | ADMIN | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-EVT-05b | | ADMIN | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 409 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-EVT-04 | | ADMIN | `PATCH /api/events/{{eventToPublishCode}}/publish` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-EVT-04b | | ADMIN | `PATCH /api/events/{{eventToPublishCode}}/publish` | 409 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-004 | | ANONYMOUS | `GET /api/events/{{eventToPublishCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-EVT-03c | | ANONYMOUS | `GET /api/events/published` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-005 | | ANONYMOUS | `GET /api/events/by-artist?stageName={{artistStageName}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-EVT-06b | | STAFF | `GET /api/events/by-artist?stageName={{artistStageName}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |

## 8. Flujo D — Compra, propiedad, cancelación y uso

| Caso | Fecha/hora | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| TC-BIZ-007a | | USER_A | `POST /api/tickets` | 201 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-007b | | USER_A | `POST /api/tickets` | 201 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-007c | | USER_A | `POST /api/tickets` | 201 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-008 | | USER_A | `POST /api/tickets` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-TKT-03b | | USER_B | `GET /api/tickets/by-user?email={{userBEmail}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-TKT-02 | | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-008 | | USER_A | `GET /api/tickets/by-user?email={{userAEmail}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-009 | | USER_B | `GET /api/tickets/{{ticketCancelableCode}}` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-010 | | USER_B | `GET /api/tickets/by-user?email={{userAEmail}}` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-011 | | USER_B | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-TKT-05c | | STAFF | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-TKT-02b | | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-010 | | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-TKT-02c | | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-011 | | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 409 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-TKT-05b | | ADMIN | `PATCH /api/tickets/{{ticketAdminCancelCode}}/cancel` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-013 | | USER_A | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-TKT-04c | | ANONYMOUS | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-009 | | STAFF | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-TKT-04b | | ADMIN | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-012a | | USER_A | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-012b | | USER_B | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-TKT-02d | | STAFF | `GET /api/tickets/{{ticketUsableCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-014 | | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-TKT-02e | | USER_A | `GET /api/tickets/{{ticketUsableCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-013 | | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 409 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| E2E-TKT-03c | | ADMIN | `GET /api/tickets/by-user?email={{userAEmail}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |

## 9. Reglas de compra (carpeta 09)

| Caso | Fecha/hora | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| TC-BIZ-014a | | USER_A | `POST /api/tickets` | 409 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-014b | | USER_B | `POST /api/tickets` | 409 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-014c | | USER_A | `POST /api/tickets` | 201 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-014d | | ADMIN | `GET /api/events/{{soldOutEventCode}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-014e | | USER_A | `POST /api/tickets` | 409 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-014f | | USER_B | `GET /api/tickets/by-user?email={{userBEmail}}` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |

## 10. Flujo E — Validaciones y errores (carpeta 08)

| Caso | Fecha/hora | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| TC-HTTP-001 | | ANONYMOUS | `POST /api/users` | 400 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-002 | | ANONYMOUS | `POST /api/users` | 400 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-003 | | ANONYMOUS | `POST /api/users` | 400 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-004 | | ANONYMOUS | `POST /api/auth/login` | 400 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-005 | | ANONYMOUS | `POST /api/auth/login` | 400 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-006 | | ADMIN | `POST /api/events` | 400 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-007 | | ADMIN | `POST /api/events` | 400 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-015a | | ADMIN | `POST /api/events` | 409 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-015b | | ADMIN | `POST /api/events` | 409 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-015c | | ADMIN | `POST /api/events` | 404 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-BIZ-015d | | ADMIN | `POST /api/events` | 409 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-008 | | ADMIN | `PATCH /api/events/E2E-NO-EXISTE/publish` | 404 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-009 | | ADMIN | `POST /api/events/{{eventToPublishCode}}/artists/abc` | 400 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-010 | | ADMIN | `POST /api/events/{{eventToPublishCode}}/artists/999999` | 404 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-011 | | USER_A | `POST /api/tickets` | 400 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-012 | | USER_A | `POST /api/tickets` | 404 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-013 | | STAFF | `GET /api/tickets/TCK-NOEXISTE` | 404 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-014 | | USER_A | `GET /api/tickets/TCK-NOEXISTE` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-015 | | USER_A | `GET /api/users/by-email` | 400 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-HTTP-016 | | USER_A | `GET /api/users/by-email?email=no-es-un-email` | 400 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |

## 11. Bloque de cierre — estado revalidado y expiración

| Caso | Fecha/hora | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|---|---|---|---|---|---|---|---|---|
| SQL-2 | | — | Desactivar e2e_inactive | — | — | | NOT RUN | |
| TC-AUTH-013a | | INACTIVE_USER | `GET /api/auth/me` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-AUTH-013b | | ANONYMOUS | `POST /api/auth/login` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| SQL-3 | | — | Revocar STAFF a e2e_staff | — | — | | NOT RUN | |
| TC-SEC-017a | | STAFF | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 403 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| TC-SEC-017b | | STAFF | `GET /api/auth/me` | 200 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |
| WAIT-1 | | — | Esperar la expiración del primer token | — | — | | NOT RUN | |
| TC-AUTH-010 | | ANONYMOUS | `GET /api/auth/me` | 401 | *pendiente* | *pendiente* | NOT RUN | *pendiente* |

## Consolidación

| Módulo | PASS | FAIL | BLOCKED | NOT RUN |
|---|---|---|---|---|
| 00 Preparación / conectividad | | | | |
| 01 Venues (2 rutas) | | | | |
| 02 Artists (3 rutas) | | | | |
| 03 Auth / usuarios (registro, login, me) | | | | |
| 04 Usuarios / consulta y propiedad (2 rutas) | | | | |
| 05 Events / administración y visibilidad (6 rutas) | | | | |
| 06 Tickets / compra, consulta y ciclo de vida (6 rutas) | | | | |
| 07 Seguridad / escenarios negativos | | | | |
| 08 Validaciones y errores HTTP | | | | |
| 09 Flujos de negocio / secuencia recomendada | | | | |
| **Total** | | | | |

Rutas con caso nominal ejecutado: ___ / 22

## Defectos y regresiones

| ID defecto | Caso | Precondiciones | Pasos | HTTP esperado | HTTP observado | Fase afectada (1–4) | Estado |
|---|---|---|---|---|---|---|---|
| | | | | | | | |
