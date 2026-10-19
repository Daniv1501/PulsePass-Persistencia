# Checklist de ejecución manual — PulsePass E2E

Orden obligatorio de ejecución. Cada fila es **una** petición de la colección `PulsePass — E2E Manual` (busque el ID en el nombre) o un paso fuera de Postman (SQL / espera).

- Envíe cada petición con **Send**, una por una. No use Collection Runner, Newman ni Postman CLI.
- Antes de cada petición protegida, revise en **Authorization** qué variable de token usa.
- Si un token vence (15 min), repita el login del actor y actualice su variable; **no** toque `expiredToken`.
- Si un prerrequisito falla, marque como **BLOCKED** los casos que dependen de él (no PASS).
- Registre cada resultado en `registro-ejecucion-manual.md`.


## 1. Preparación

| ✓ | # | ID | Actor | Petición | Esperado | Copiar / nota |
|---|---|---|---|---|---|---|
| ☐ | 1 | **PREP-1** | — | **Fuera de Postman:** Base E2E, API y fixtures | — | Siga las secciones 2 y 3 del README: base `pulsepass_e2e` limpia, API arrancada contra ella, `fixtures/seed-e2e.sql` ejecutado y ambiente `PulsePass-local` seleccionado con `baseUrl` y contraseñas definidas. |
| ☐ | 2 | TC-PREP-001 | ANONYMOUS | `GET /api/venues/active` | 200 |  |
| ☐ | 3 | TC-PREP-002 | ANONYMOUS | `GET /api/auth/me` | 401 |  |

## 2. Flujo A — Descubrimiento público (anónimo)

| ✓ | # | ID | Actor | Petición | Esperado | Copiar / nota |
|---|---|---|---|---|---|---|
| ☐ | 4 | E2E-VEN-02 | ANONYMOUS | `GET /api/venues/active` | 200 |  |
| ☐ | 5 | E2E-VEN-01 | ANONYMOUS | `GET /api/venues/{{venueActiveCode}}` | 200 |  |
| ☐ | 6 | E2E-VEN-01b | ANONYMOUS | `GET /api/venues/{{venueInactiveCode}}` | 200 |  |
| ☐ | 7 | E2E-VEN-01c | ANONYMOUS | `GET /api/venues/VEN-NO-EXISTE` | 404 |  |
| ☐ | 8 | E2E-ART-03 | ANONYMOUS | `GET /api/artists/active` | 200 | Copie el `id` del artista con `stageName` = "Solar Beat" en la variable `artistId`. |
| ☐ | 9 | E2E-ART-01 | ANONYMOUS | `GET /api/artists/{{artistId}}` | 200 | `artistId` copiado en E2E-ART-03. |
| ☐ | 10 | E2E-ART-01b | ANONYMOUS | `GET /api/artists/999999` | 404 |  |
| ☐ | 11 | E2E-ART-02 | ANONYMOUS | `GET /api/artists/by-stage-name?stageName={{artistStageName}}` | 200 |  |
| ☐ | 12 | E2E-ART-02b | ANONYMOUS | `GET /api/artists/by-stage-name?stageName=Artista Fantasma` | 404 |  |
| ☐ | 13 | E2E-EVT-03 | ANONYMOUS | `GET /api/events/published` | 200 |  |
| ☐ | 14 | E2E-EVT-02 | ANONYMOUS | `GET /api/events/{{publishedEventCode}}` | 200 |  |
| ☐ | 15 | TC-SEC-004 | ANONYMOUS | `GET /api/events/{{draftEventCode}}` | 404 |  |
| ☐ | 16 | TC-SEC-005 | ANONYMOUS | `GET /api/events/by-artist?stageName={{artistStageName}}` | 200 |  |

## 3. Flujo B — Registro

| ✓ | # | ID | Actor | Petición | Esperado | Copiar / nota |
|---|---|---|---|---|---|---|
| ☐ | 17 | TC-AUTH-001 | ANONYMOUS | `POST /api/users` | 201 | `userAPassword` definido en el ambiente (12 a 72 caracteres). |
| ☐ | 18 | E2E-USR-01b | ANONYMOUS | `POST /api/users` | 201 |  |
| ☐ | 19 | E2E-USR-01c | ANONYMOUS | `POST /api/users` | 201 |  |
| ☐ | 20 | E2E-USR-01d | ANONYMOUS | `POST /api/users` | 201 |  |
| ☐ | 21 | E2E-USR-01e | ANONYMOUS | `POST /api/users` | 201 |  |
| ☐ | 22 | TC-SEC-016 | ANONYMOUS | `POST /api/users` | 201 |  |
| ☐ | 23 | TC-BIZ-006a | ANONYMOUS | `POST /api/users` | 409 |  |
| ☐ | 24 | TC-BIZ-006b | ANONYMOUS | `POST /api/users` | 409 |  |

## 4. Flujo B — Roles privilegiados, login y /me

| ✓ | # | ID | Actor | Petición | Esperado | Copiar / nota |
|---|---|---|---|---|---|---|
| ☐ | 25 | **SQL-1** | — | **Fuera de Postman:** Conceder STAFF y ADMIN (fuera de HTTP) | — | Ejecute `fixtures/grant-e2e-roles.sql` sobre `pulsepass_e2e` (README sección 3.3). La consulta final debe mostrar `e2e_admin` con ADMIN,USER y `e2e_staff` con STAFF,USER. |
| ☐ | 26 | TC-AUTH-002 | ANONYMOUS | `POST /api/auth/login` | 200 | Copie `accessToken` en **dos** variables: `userAToken` y `expiredToken` (este último se usará en TC-AUTH-010 cuando hayan pasado al menos 15 minutos). Anote la hora. |
| ☐ | 27 | TC-AUTH-003 | ANONYMOUS | `POST /api/auth/login` | 200 |  |
| ☐ | 28 | E2E-AUTH-01b | ANONYMOUS | `POST /api/auth/login` | 200 | Copie `accessToken` en `userBToken`. |
| ☐ | 29 | E2E-AUTH-01c | ANONYMOUS | `POST /api/auth/login` | 200 | Copie `accessToken` en `staffToken`. |
| ☐ | 30 | E2E-AUTH-01d | ANONYMOUS | `POST /api/auth/login` | 200 | Copie `accessToken` en `adminToken`. |
| ☐ | 31 | E2E-AUTH-01e | ANONYMOUS | `POST /api/auth/login` | 200 | Copie `accessToken` en `inactiveToken`. |
| ☐ | 32 | E2E-AUTH-01f | ANONYMOUS | `POST /api/auth/login` | 200 | Copie `accessToken` en `roleInjToken`. |
| ☐ | 33 | TC-AUTH-004 | ANONYMOUS | `POST /api/auth/login` | 401 |  |
| ☐ | 34 | TC-AUTH-005 | ANONYMOUS | `POST /api/auth/login` | 401 |  |
| ☐ | 35 | TC-AUTH-006 | USER_A | `GET /api/auth/me` | 200 |  |
| ☐ | 36 | E2E-AUTH-02b | USER_B | `GET /api/auth/me` | 200 |  |
| ☐ | 37 | E2E-AUTH-02c | STAFF | `GET /api/auth/me` | 200 |  |
| ☐ | 38 | E2E-AUTH-02d | ADMIN | `GET /api/auth/me` | 200 |  |
| ☐ | 39 | E2E-AUTH-02f | ROLE_INJ | `GET /api/auth/me` | 200 |  |

## 5. Flujo B — Tokens inválidos y rol insuficiente

| ✓ | # | ID | Actor | Petición | Esperado | Copiar / nota |
|---|---|---|---|---|---|---|
| ☐ | 40 | TC-AUTH-008 | ANONYMOUS | `GET /api/auth/me` | 401 | `malformedToken` viene precargado en el ambiente con un texto que no es un JWT. |
| ☐ | 41 | TC-AUTH-009 | ANONYMOUS | `GET /api/auth/me` | 401 | Copie el valor de `userAToken` en `tamperedToken` y cambie **un** carácter en el centro (no el último). |
| ☐ | 42 | TC-AUTH-011a | ANONYMOUS | `GET /api/auth/me` | 401 | Las tres partes del JWT vienen precargadas en el ambiente (no son sensibles); Postman las une al enviar. |
| ☐ | 43 | TC-AUTH-011b | ANONYMOUS | `GET /api/auth/me` | 401 | Cabecera `alg=none` y contenido precargados en el ambiente; `testJwtUnsignedPayload` termina en punto (sin firma). |
| ☐ | 44 | TC-AUTH-012 | ANONYMOUS | `GET /api/auth/me?access_token={{userAToken}}` | 401 |  |
| ☐ | 45 | TC-AUTH-015 | ANONYMOUS | `GET /api/events/published` | 401 | Usa `malformedToken` (precargado). |
| ☐ | 46 | TC-AUTH-014 | USER_A | `PATCH /api/events/{{draftEventCode}}/publish` | 403 |  |

## 6. Usuarios — propiedad de datos personales

| ✓ | # | ID | Actor | Petición | Esperado | Copiar / nota |
|---|---|---|---|---|---|---|
| ☐ | 47 | E2E-USR-02 | USER_A | `GET /api/users/by-email?email={{userAEmail}}` | 200 |  |
| ☐ | 48 | E2E-USR-03 | USER_A | `GET /api/users/by-username?username={{userAUsername}}` | 200 |  |
| ☐ | 49 | TC-SEC-006 | USER_B | `GET /api/users/by-email?email={{userAEmail}}` | 403 |  |
| ☐ | 50 | TC-SEC-007 | USER_B | `GET /api/users/by-username?username={{userAUsername}}` | 403 |  |
| ☐ | 51 | E2E-USR-03b | STAFF | `GET /api/users/by-username?username={{userAUsername}}` | 403 |  |
| ☐ | 52 | E2E-USR-02b | ADMIN | `GET /api/users/by-email?email={{userAEmail}}` | 200 |  |
| ☐ | 53 | E2E-USR-03c | ADMIN | `GET /api/users/by-username?username=nadie_{{runId}}` | 404 |  |
| ☐ | 54 | E2E-USR-02c | ANONYMOUS | `GET /api/users/by-email?email={{userAEmail}}` | 401 |  |

## 7. Flujo C — Catálogo (ADMIN)

| ✓ | # | ID | Actor | Petición | Esperado | Copiar / nota |
|---|---|---|---|---|---|---|
| ☐ | 55 | TC-SEC-001a | ANONYMOUS | `POST /api/events` | 401 |  |
| ☐ | 56 | TC-SEC-001b | USER_A | `POST /api/events` | 403 |  |
| ☐ | 57 | TC-SEC-001c | STAFF | `POST /api/events` | 403 |  |
| ☐ | 58 | TC-BIZ-003 | ADMIN | `POST /api/events` | 201 | `eventToPublishCode` no usado antes en esta base (cámbielo junto con `runId` si repite). |
| ☐ | 59 | E2E-EVT-02b | ANONYMOUS | `GET /api/events/{{eventToPublishCode}}` | 404 |  |
| ☐ | 60 | E2E-EVT-03b | ANONYMOUS | `GET /api/events/published` | 200 |  |
| ☐ | 61 | TC-SEC-003a | USER_A | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 403 |  |
| ☐ | 62 | TC-SEC-003b | STAFF | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 403 |  |
| ☐ | 63 | TC-SEC-002a | USER_A | `PATCH /api/events/{{eventToPublishCode}}/publish` | 403 |  |
| ☐ | 64 | TC-SEC-002b | STAFF | `PATCH /api/events/{{eventToPublishCode}}/publish` | 403 |  |
| ☐ | 65 | E2E-EVT-02c | ADMIN | `GET /api/events/{{eventToPublishCode}}` | 200 |  |
| ☐ | 66 | TC-SEC-015 | ADMIN | `GET /api/events/{{draftEventCode}}` | 200 |  |
| ☐ | 67 | E2E-EVT-02d | STAFF | `GET /api/events/{{draftEventCode}}` | 200 |  |
| ☐ | 68 | E2E-EVT-05 | ADMIN | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 200 |  |
| ☐ | 69 | E2E-EVT-05b | ADMIN | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 409 |  |
| ☐ | 70 | E2E-EVT-04 | ADMIN | `PATCH /api/events/{{eventToPublishCode}}/publish` | 200 |  |
| ☐ | 71 | E2E-EVT-04b | ADMIN | `PATCH /api/events/{{eventToPublishCode}}/publish` | 409 |  |
| ☐ | 72 | TC-BIZ-004 | ANONYMOUS | `GET /api/events/{{eventToPublishCode}}` | 200 |  |
| ☐ | 73 | E2E-EVT-03c | ANONYMOUS | `GET /api/events/published` | 200 |  |
| ☐ | 74 | TC-BIZ-005 | ANONYMOUS | `GET /api/events/by-artist?stageName={{artistStageName}}` | 200 |  |
| ☐ | 75 | E2E-EVT-06b | STAFF | `GET /api/events/by-artist?stageName={{artistStageName}}` | 200 |  |

## 8. Flujo D — Compra, propiedad, cancelación y uso

| ✓ | # | ID | Actor | Petición | Esperado | Copiar / nota |
|---|---|---|---|---|---|---|
| ☐ | 76 | TC-BIZ-007a | USER_A | `POST /api/tickets` | 201 | Copie `ticketCode` en `ticketCancelableCode` (T1). |
| ☐ | 77 | TC-BIZ-007b | USER_A | `POST /api/tickets` | 201 | Copie `ticketCode` en `ticketUsableCode` (T2). |
| ☐ | 78 | TC-BIZ-007c | USER_A | `POST /api/tickets` | 201 | Copie `ticketCode` en `ticketAdminCancelCode` (T3). |
| ☐ | 79 | TC-SEC-008 | USER_A | `POST /api/tickets` | 403 |  |
| ☐ | 80 | E2E-TKT-03b | USER_B | `GET /api/tickets/by-user?email={{userBEmail}}` | 200 |  |
| ☐ | 81 | E2E-TKT-02 | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 200 |  |
| ☐ | 82 | TC-BIZ-008 | USER_A | `GET /api/tickets/by-user?email={{userAEmail}}` | 200 |  |
| ☐ | 83 | TC-SEC-009 | USER_B | `GET /api/tickets/{{ticketCancelableCode}}` | 403 |  |
| ☐ | 84 | TC-SEC-010 | USER_B | `GET /api/tickets/by-user?email={{userAEmail}}` | 403 |  |
| ☐ | 85 | TC-SEC-011 | USER_B | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 403 |  |
| ☐ | 86 | E2E-TKT-05c | STAFF | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 403 |  |
| ☐ | 87 | E2E-TKT-02b | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 200 |  |
| ☐ | 88 | TC-BIZ-010 | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 200 |  |
| ☐ | 89 | E2E-TKT-02c | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 200 |  |
| ☐ | 90 | TC-BIZ-011 | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 409 |  |
| ☐ | 91 | E2E-TKT-05b | ADMIN | `PATCH /api/tickets/{{ticketAdminCancelCode}}/cancel` | 200 |  |
| ☐ | 92 | TC-SEC-013 | USER_A | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 403 |  |
| ☐ | 93 | E2E-TKT-04c | ANONYMOUS | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 401 |  |
| ☐ | 94 | TC-BIZ-009 | STAFF | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 200 |  |
| ☐ | 95 | E2E-TKT-04b | ADMIN | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 200 |  |
| ☐ | 96 | TC-SEC-012a | USER_A | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 403 |  |
| ☐ | 97 | TC-SEC-012b | USER_B | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 403 |  |
| ☐ | 98 | E2E-TKT-02d | STAFF | `GET /api/tickets/{{ticketUsableCode}}` | 200 |  |
| ☐ | 99 | TC-SEC-014 | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 200 |  |
| ☐ | 100 | E2E-TKT-02e | USER_A | `GET /api/tickets/{{ticketUsableCode}}` | 200 |  |
| ☐ | 101 | TC-BIZ-013 | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 409 |  |
| ☐ | 102 | E2E-TKT-03c | ADMIN | `GET /api/tickets/by-user?email={{userAEmail}}` | 200 |  |

## 9. Reglas de compra (carpeta 09)

| ✓ | # | ID | Actor | Petición | Esperado | Copiar / nota |
|---|---|---|---|---|---|---|
| ☐ | 103 | TC-BIZ-014a | USER_A | `POST /api/tickets` | 409 |  |
| ☐ | 104 | TC-BIZ-014b | USER_B | `POST /api/tickets` | 409 |  |
| ☐ | 105 | TC-BIZ-014c | USER_A | `POST /api/tickets` | 201 |  |
| ☐ | 106 | TC-BIZ-014d | ADMIN | `GET /api/events/{{soldOutEventCode}}` | 200 |  |
| ☐ | 107 | TC-BIZ-014e | USER_A | `POST /api/tickets` | 409 |  |
| ☐ | 108 | TC-BIZ-014f | USER_B | `GET /api/tickets/by-user?email={{userBEmail}}` | 200 |  |

## 10. Flujo E — Validaciones y errores (carpeta 08)

| ✓ | # | ID | Actor | Petición | Esperado | Copiar / nota |
|---|---|---|---|---|---|---|
| ☐ | 109 | TC-HTTP-001 | ANONYMOUS | `POST /api/users` | 400 |  |
| ☐ | 110 | TC-HTTP-002 | ANONYMOUS | `POST /api/users` | 400 |  |
| ☐ | 111 | TC-HTTP-003 | ANONYMOUS | `POST /api/users` | 400 |  |
| ☐ | 112 | TC-HTTP-004 | ANONYMOUS | `POST /api/auth/login` | 400 |  |
| ☐ | 113 | TC-HTTP-005 | ANONYMOUS | `POST /api/auth/login` | 400 |  |
| ☐ | 114 | TC-HTTP-006 | ADMIN | `POST /api/events` | 400 |  |
| ☐ | 115 | TC-HTTP-007 | ADMIN | `POST /api/events` | 400 |  |
| ☐ | 116 | TC-BIZ-015a | ADMIN | `POST /api/events` | 409 |  |
| ☐ | 117 | TC-BIZ-015b | ADMIN | `POST /api/events` | 409 |  |
| ☐ | 118 | TC-BIZ-015c | ADMIN | `POST /api/events` | 404 |  |
| ☐ | 119 | TC-BIZ-015d | ADMIN | `POST /api/events` | 409 |  |
| ☐ | 120 | TC-HTTP-008 | ADMIN | `PATCH /api/events/E2E-NO-EXISTE/publish` | 404 |  |
| ☐ | 121 | TC-HTTP-009 | ADMIN | `POST /api/events/{{eventToPublishCode}}/artists/abc` | 400 |  |
| ☐ | 122 | TC-HTTP-010 | ADMIN | `POST /api/events/{{eventToPublishCode}}/artists/999999` | 404 |  |
| ☐ | 123 | TC-HTTP-011 | USER_A | `POST /api/tickets` | 400 |  |
| ☐ | 124 | TC-HTTP-012 | USER_A | `POST /api/tickets` | 404 |  |
| ☐ | 125 | TC-HTTP-013 | STAFF | `GET /api/tickets/TCK-NOEXISTE` | 404 |  |
| ☐ | 126 | TC-HTTP-014 | USER_A | `GET /api/tickets/TCK-NOEXISTE` | 403 |  |
| ☐ | 127 | TC-HTTP-015 | USER_A | `GET /api/users/by-email` | 400 |  |
| ☐ | 128 | TC-HTTP-016 | USER_A | `GET /api/users/by-email?email=no-es-un-email` | 400 |  |

## 11. Bloque de cierre — estado revalidado y expiración

| ✓ | # | ID | Actor | Petición | Esperado | Copiar / nota |
|---|---|---|---|---|---|---|
| ☐ | 129 | **SQL-2** | — | **Fuera de Postman:** Desactivar e2e_inactive | — | Ejecute `fixtures/estado-desactivar-inactivo.sql`. La consulta final debe mostrar `active = f`. |
| ☐ | 130 | TC-AUTH-013a | INACTIVE_USER | `GET /api/auth/me` | 401 | SQL-2 ejecutado. `inactiveToken` obtenido en E2E-AUTH-01e (si ya venció, el caso sigue siendo válido pero anótelo). |
| ☐ | 131 | TC-AUTH-013b | ANONYMOUS | `POST /api/auth/login` | 401 |  |
| ☐ | 132 | **SQL-3** | — | **Fuera de Postman:** Revocar STAFF a e2e_staff | — | Ejecute `fixtures/estado-revocar-staff.sql`. La consulta final debe mostrar `e2e_staff` solo con USER. **No** vuelva a iniciar sesión: se usa el `staffToken` antiguo. |
| ☐ | 133 | TC-SEC-017a | STAFF | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 403 |  |
| ☐ | 134 | TC-SEC-017b | STAFF | `GET /api/auth/me` | 200 |  |
| ☐ | 135 | **WAIT-1** | — | **Fuera de Postman:** Esperar la expiración del primer token | — | Compruebe que pasaron **al menos 15 minutos** desde TC-AUTH-002 (la hora que anotó). Si no, espere. |
| ☐ | 136 | TC-AUTH-010 | ANONYMOUS | `GET /api/auth/me` | 401 | WAIT-1: han pasado ≥ 15 min desde que se copió `expiredToken`. |

## Cierre

- [ ] Las 22 rutas tienen al menos un caso nominal ejecutado (ver `matriz-cobertura.md`).
- [ ] Bitácora completa: ningún caso queda sin resultado (PASS, FAIL, BLOCKED o NOT RUN justificado).
- [ ] Defectos y bloqueos registrados en la sección de defectos de la bitácora.
- [ ] Variables `*Token` y contraseñas vaciadas en Postman; nada sensible en capturas ni en archivos exportados.
- [ ] Para repetir: restablecer la base `pulsepass_e2e` (README, sección 6).
