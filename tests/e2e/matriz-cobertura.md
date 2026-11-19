# Matriz de rutas y permisos

Todas las filas empiezan **NOT RUN** para Postman. Los tests Java de contrato/seguridad son una evidencia separada.

| # | Método y ruta | Permitido | Anónimo / rol prohibido | Propiedad / visibilidad | Caso nominal | Resultado manual |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | GET /api/venues/{code} | Público | Público para todos | N/A | E2E-VEN-01 | NOT RUN |
| 2 | GET /api/venues/active | Público | Público para todos | Solo activos | E2E-VEN-02 | NOT RUN |
| 3 | POST /api/events | ADMIN | 401 / USER,STAFF:403 | N/A | E2E-EVT-01 | NOT RUN |
| 4 | GET /api/events/{eventCode} | Público PUBLISHED; STAFF/ADMIN todos | No publicados:404 para visitante/USER | No revela borradores | E2E-EVT-02 | NOT RUN |
| 5 | GET /api/events/published | Público | Público para todos | Solo PUBLISHED | E2E-EVT-03 | NOT RUN |
| 6 | PATCH /api/events/{eventCode}/publish | ADMIN | 401 / USER,STAFF:403 | Conserva reglas del servicio | E2E-EVT-04 | NOT RUN |
| 7 | POST /api/events/{eventCode}/artists/{artistId} | ADMIN | 401 / USER,STAFF:403 | N/A | E2E-EVT-05 | NOT RUN |
| 8 | GET /api/events/by-artist | Público; STAFF/ADMIN todos | Público para todos | Solo PUBLISHED para visitante/USER | E2E-EVT-06 | NOT RUN |
| 9 | GET /api/artists/{id} | Público | Público para todos | N/A | E2E-ART-01 | NOT RUN |
| 10 | GET /api/artists/by-stage-name | Público | Público para todos | N/A | E2E-ART-02 | NOT RUN |
| 11 | GET /api/artists/active | Público | Público para todos | Solo activos | E2E-ART-03 | NOT RUN |
| 12 | POST /api/users | Público | Público para todos | Crea únicamente USER | TC-AUTH-001A | NOT RUN |
| 13 | GET /api/users/by-email | Propietario / ADMIN | 401 / tercero:403 | ID persistido | E2E-USR-02 | NOT RUN |
| 14 | GET /api/users/by-username | Propietario / ADMIN | 401 / tercero:403 | ID persistido | E2E-USR-03 | NOT RUN |
| 15 | POST /api/tickets | USER propietario | 401 / STAFF,ADMIN sin USER:403 | Email resuelve ID del principal; ajeno:403 | E2E-TKT-01 | NOT RUN |
| 16 | GET /api/tickets/{ticketCode} | Propietario / STAFF / ADMIN | 401 / tercero:403 | Propietario desde BD | E2E-TKT-02 | NOT RUN |
| 17 | GET /api/tickets/by-user | Propietario / ADMIN | 401 / tercero,STAFF ajeno:403 | ID persistido | E2E-TKT-03 | NOT RUN |
| 18 | GET /api/events/{eventCode}/tickets/paid | STAFF / ADMIN | 401 / USER:403 | STAFF global en MVP | E2E-TKT-04 | NOT RUN |
| 19 | PATCH /api/tickets/{ticketCode}/cancel | USER propietario / ADMIN | 401 / tercero,STAFF:403 | Autorización antes de mutar | E2E-TKT-05 | NOT RUN |
| 20 | PATCH /api/tickets/{ticketCode}/use | STAFF / ADMIN | 401 / USER:403 | STAFF global en MVP | E2E-TKT-06 | NOT RUN |
| 21 | POST /api/auth/login | Público | Credencial errónea/inactivo:401 | Identidad por usuario o email | TC-AUTH-002-* | NOT RUN |
| 22 | GET /api/auth/me | Autenticado activo | 401 | Solo principal actual; sin secretos | TC-AUTH-006-* | NOT RUN |
