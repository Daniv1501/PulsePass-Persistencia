# Matriz de cobertura — 22 rutas × actores

Generada a partir de la colección. Cada celda lista los casos que ejercen esa ruta con ese actor y su HTTP esperado.

| Caso base | Ruta | Anónimo | USER_A (propio) | USER_B (ajeno) | STAFF | ADMIN | Otros |
|---|---|---|---|---|---|---|---|
| E2E-VEN-01 | `GET /api/venues/{code}` | E2E-VEN-01 (200)<br>E2E-VEN-01b (200)<br>E2E-VEN-01c (404) | — | — | — | — | — |
| E2E-VEN-02 | `GET /api/venues/active` | TC-PREP-001 (200)<br>E2E-VEN-02 (200) | — | — | — | — | — |
| E2E-EVT-01 | `POST /api/events` | TC-SEC-001a (401) | TC-SEC-001b (403) | — | TC-SEC-001c (403) | TC-BIZ-003 (201)<br>TC-HTTP-006 (400)<br>TC-HTTP-007 (400)<br>TC-BIZ-015a (409)<br>TC-BIZ-015b (409)<br>TC-BIZ-015c (404)<br>TC-BIZ-015d (409) | — |
| E2E-EVT-02 | `GET /api/events/{eventCode}` | E2E-EVT-02 (200)<br>TC-SEC-004 (404)<br>E2E-EVT-02b (404)<br>TC-BIZ-004 (200) | — | — | E2E-EVT-02d (200) | E2E-EVT-02c (200)<br>TC-SEC-015 (200)<br>TC-BIZ-014d (200) | — |
| E2E-EVT-03 | `GET /api/events/published` | E2E-EVT-03 (200)<br>E2E-EVT-03b (200)<br>E2E-EVT-03c (200) | — | — | — | — | TC-AUTH-015 (401) |
| E2E-EVT-04 | `PATCH /api/events/{eventCode}/publish` | — | TC-AUTH-014 (403)<br>TC-SEC-002a (403) | — | TC-SEC-002b (403) | E2E-EVT-04 (200)<br>E2E-EVT-04b (409)<br>TC-HTTP-008 (404) | — |
| E2E-EVT-05 | `POST /api/events/{eventCode}/artists/{artistId}` | — | TC-SEC-003a (403) | — | TC-SEC-003b (403) | E2E-EVT-05 (200)<br>E2E-EVT-05b (409)<br>TC-HTTP-009 (400)<br>TC-HTTP-010 (404) | — |
| E2E-EVT-06 | `GET /api/events/by-artist` | TC-SEC-005 (200)<br>TC-BIZ-005 (200) | — | — | E2E-EVT-06b (200) | — | — |
| E2E-ART-01 | `GET /api/artists/{id}` | E2E-ART-01 (200)<br>E2E-ART-01b (404) | — | — | — | — | — |
| E2E-ART-02 | `GET /api/artists/by-stage-name` | E2E-ART-02 (200)<br>E2E-ART-02b (404) | — | — | — | — | — |
| E2E-ART-03 | `GET /api/artists/active` | E2E-ART-03 (200) | — | — | — | — | — |
| E2E-USR-01 | `POST /api/users` | TC-AUTH-001 (201)<br>E2E-USR-01b (201)<br>E2E-USR-01c (201)<br>E2E-USR-01d (201)<br>E2E-USR-01e (201)<br>TC-SEC-016 (201)<br>TC-BIZ-006a (409)<br>TC-BIZ-006b (409)<br>TC-HTTP-001 (400)<br>TC-HTTP-002 (400)<br>TC-HTTP-003 (400) | — | — | — | — | — |
| E2E-USR-02 | `GET /api/users/by-email` | E2E-USR-02c (401) | E2E-USR-02 (200)<br>TC-HTTP-015 (400)<br>TC-HTTP-016 (400) | TC-SEC-006 (403) | — | E2E-USR-02b (200) | — |
| E2E-USR-03 | `GET /api/users/by-username` | — | E2E-USR-03 (200) | TC-SEC-007 (403) | E2E-USR-03b (403) | E2E-USR-03c (404) | — |
| E2E-TKT-01 | `POST /api/tickets` | — | TC-BIZ-007a (201)<br>TC-BIZ-007b (201)<br>TC-BIZ-007c (201)<br>TC-SEC-008 (403)<br>TC-BIZ-014a (409)<br>TC-BIZ-014c (201)<br>TC-BIZ-014e (409)<br>TC-HTTP-011 (400)<br>TC-HTTP-012 (404) | TC-BIZ-014b (409) | — | — | — |
| E2E-TKT-02 | `GET /api/tickets/{ticketCode}` | — | E2E-TKT-02 (200)<br>E2E-TKT-02b (200)<br>E2E-TKT-02c (200)<br>E2E-TKT-02e (200)<br>TC-HTTP-014 (403) | TC-SEC-009 (403) | E2E-TKT-02d (200)<br>TC-HTTP-013 (404) | — | — |
| E2E-TKT-03 | `GET /api/tickets/by-user` | — | TC-BIZ-008 (200) | E2E-TKT-03b (200)<br>TC-SEC-010 (403)<br>TC-BIZ-014f (200) | — | E2E-TKT-03c (200) | — |
| E2E-TKT-04 | `GET /api/events/{eventCode}/tickets/paid` | E2E-TKT-04c (401) | TC-SEC-013 (403) | — | TC-BIZ-009 (200)<br>TC-SEC-017a (403) | E2E-TKT-04b (200) | — |
| E2E-TKT-05 | `PATCH /api/tickets/{ticketCode}/cancel` | — | TC-BIZ-010 (200)<br>TC-BIZ-011 (409) | TC-SEC-011 (403) | E2E-TKT-05c (403) | E2E-TKT-05b (200) | — |
| E2E-TKT-06 | `PATCH /api/tickets/{ticketCode}/use` | — | TC-SEC-012a (403) | TC-SEC-012b (403) | TC-SEC-014 (200)<br>TC-BIZ-013 (409) | — | — |
| E2E-AUTH-01 | `POST /api/auth/login` | TC-AUTH-002 (200)<br>TC-AUTH-003 (200)<br>E2E-AUTH-01b (200)<br>E2E-AUTH-01c (200)<br>E2E-AUTH-01d (200)<br>E2E-AUTH-01e (200)<br>E2E-AUTH-01f (200)<br>TC-AUTH-004 (401)<br>TC-AUTH-005 (401)<br>TC-HTTP-004 (400)<br>TC-HTTP-005 (400)<br>TC-AUTH-013b (401) | — | — | — | — | — |
| E2E-AUTH-02 | `GET /api/auth/me` | TC-PREP-002 (401)<br>TC-AUTH-012 (401) | TC-AUTH-006 (200) | E2E-AUTH-02b (200) | E2E-AUTH-02c (200)<br>TC-SEC-017b (200) | E2E-AUTH-02d (200) | E2E-AUTH-02f (200)<br>TC-AUTH-008 (401)<br>TC-AUTH-009 (401)<br>TC-AUTH-011a (401)<br>TC-AUTH-011b (401)<br>TC-AUTH-013a (401)<br>TC-AUTH-010 (401) |

**Total:** 22/22 rutas, 131 casos de petición y 5 pasos fuera de Postman.
