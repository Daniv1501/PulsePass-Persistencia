# Registro de ejecucion manual

Estado inicial: NOT RUN. Las pruebas Java automatizadas son evidencia independiente; no acreditan ejecucion manual desde Postman. No incluir passwords, JWT ni capturas sin redactar.

Fecha/hora (America/Bogota): pendiente. Ejecutor: pendiente. Version/commit: pendiente. Ambiente: localhost / pulsepass_e2e.

| Caso | Actor | Metodo / ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observacion |
| --- | --- | --- | --- | --- | --- | --- | --- |
| PRE-001 | anonimo | `GET /api/venues/active` | 200 | pendiente | pendiente | NOT RUN | Comprobar API con una ruta publica existente |
| E2E-VEN-01 | anonimo | `GET /api/venues/{{venueActiveCode}}` | 200 | pendiente | pendiente | NOT RUN | Consultar sede activa por codigo |
| E2E-VEN-02 | anonimo | `GET /api/venues/active` | 200 | pendiente | pendiente | NOT RUN | Listar sedes activas |
| E2E-ART-01 | anonimo | `GET /api/artists/{{artistId}}` | 200 | pendiente | pendiente | NOT RUN | Consultar artista por ID |
| E2E-ART-02 | anonimo | `GET /api/artists/by-stage-name?stageName={{artistStageName}}` | 200 | pendiente | pendiente | NOT RUN | Consultar por nombre artistico |
| E2E-ART-03 | anonimo | `GET /api/artists/active` | 200 | pendiente | pendiente | NOT RUN | Listar artistas activos |
| TC-AUTH-001A | anonimo | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | Registrar USER_A |
| TC-AUTH-001B | anonimo | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | Registrar USER_B |
| TC-AUTH-002-userA | anonimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | Login userA |
| TC-AUTH-006-userA | userA | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | Verificar identidad y roles userA |
| TC-AUTH-002-userB | anonimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | Login userB |
| TC-AUTH-006-userB | userB | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | Verificar identidad y roles userB |
| TC-AUTH-002-staff | anonimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | Login staff |
| TC-AUTH-006-staff | staff | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | Verificar identidad y roles staff |
| TC-AUTH-002-admin | anonimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | Login admin |
| TC-AUTH-006-admin | admin | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | Verificar identidad y roles admin |
| TC-AUTH-003 | anonimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | Login por email |
| E2E-USR-02 | userA | `GET /api/users/by-email?email={{userAEmail}}` | 200 | pendiente | pendiente | NOT RUN | Consultar propio usuario por email |
| E2E-USR-03 | userA | `GET /api/users/by-username?username={{userAUsername}}` | 200 | pendiente | pendiente | NOT RUN | Consultar propio usuario por username |
| E2E-EVT-01 | admin | `POST /api/events` | 201 | pendiente | pendiente | NOT RUN | Crear borrador ADMIN |
| E2E-EVT-02 | admin | `GET /api/events/{{eventToPublishCode}}` | 200 | pendiente | pendiente | NOT RUN | Leer borrador ADMIN |
| E2E-EVT-03 | anonimo | `GET /api/events/published` | 200 | pendiente | pendiente | NOT RUN | Cartelera publica |
| E2E-EVT-04 | admin | `PATCH /api/events/{{eventToPublishCode}}/publish` | 200 | pendiente | pendiente | NOT RUN | Publicar evento ADMIN |
| E2E-EVT-05 | admin | `POST /api/events/{{eventToPublishCode}}/artists/{{artistId}}` | 200 | pendiente | pendiente | NOT RUN | Asociar artista ADMIN |
| E2E-EVT-06 | anonimo | `GET /api/events/by-artist?stageName={{artistStageName}}` | 200 | pendiente | pendiente | NOT RUN | Eventos por artista como visitante |
| E2E-TKT-01 | userA | `POST /api/tickets` | 201 | pendiente | pendiente | NOT RUN | Comprar ticket cancelable de A |
| E2E-TKT-02 | userA | `GET /api/tickets/{{ticketCancelableCode}}` | 200 | pendiente | pendiente | NOT RUN | Consultar ticket propio |
| E2E-TKT-03 | userA | `GET /api/tickets/by-user?email={{userAEmail}}` | 200 | pendiente | pendiente | NOT RUN | Listar tickets propios |
| E2E-TKT-04 | staff | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 200 | pendiente | pendiente | NOT RUN | Listar tickets pagados STAFF |
| E2E-TKT-05 | userA | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 200 | pendiente | pendiente | NOT RUN | Cancelar ticket propio |
| E2E-TKT-06 | staff | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 200 | pendiente | pendiente | NOT RUN | Registrar ingreso con STAFF |
| TC-AUTH-004 | anonimo | `POST /api/auth/login` | 401 | pendiente | pendiente | NOT RUN | Password incorrecto |
| TC-AUTH-005 | anonimo | `POST /api/auth/login` | 401 | pendiente | pendiente | NOT RUN | Identificador inexistente |
| TC-AUTH-007 | anonimo | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | Ruta protegida sin Bearer |
| TC-AUTH-008 | malformed | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | Bearer malformado |
| TC-AUTH-009 | tampered | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | JWT alterado |
| TC-AUTH-010 | expired | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | JWT expirado |
| TC-AUTH-011-key | wrongKey | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | JWT de otra clave |
| TC-AUTH-011-issuer | wrongIssuer | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | JWT de otro emisor |
| TC-AUTH-012 | anonimo | `GET /api/auth/me?access_token=[REDACTADO]` | 401 | pendiente | pendiente | NOT RUN | Token por query no autentica |
| TC-AUTH-013 | inactive | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | Token de usuario desactivado |
| TC-AUTH-015 | malformed | `GET /api/venues/active` | 401 | pendiente | pendiente | NOT RUN | Bearer invalido en ruta publica se rechaza |
| TC-SEC-001-None | anonimo | `POST /api/events` | 401 | pendiente | pendiente | NOT RUN | Crear evento sin permiso None |
| TC-SEC-001-userA | userA | `POST /api/events` | 403 | pendiente | pendiente | NOT RUN | Crear evento sin permiso userA |
| TC-SEC-001-staff | staff | `POST /api/events` | 403 | pendiente | pendiente | NOT RUN | Crear evento sin permiso staff |
| TC-SEC-002-userA | userA | `PATCH /api/events/{{draftEventCode}}/publish` | 403 | pendiente | pendiente | NOT RUN | Publicar sin rol ADMIN |
| TC-SEC-002-staff | staff | `PATCH /api/events/{{draftEventCode}}/publish` | 403 | pendiente | pendiente | NOT RUN | Publicar sin rol ADMIN |
| TC-SEC-003 | userA | `POST /api/events/{{draftEventCode}}/artists/{{artistId}}` | 403 | pendiente | pendiente | NOT RUN | Asociar artista como USER |
| TC-SEC-004 | anonimo | `GET /api/events/{{draftEventCode}}` | 404 | pendiente | pendiente | NOT RUN | Borrador oculto a visitante |
| TC-SEC-004-user | userA | `GET /api/events/{{draftEventCode}}` | 404 | pendiente | pendiente | NOT RUN | Borrador oculto a USER |
| TC-SEC-005 | anonimo | `GET /api/events/by-artist?stageName={{artistStageName}}` | 200 | pendiente | pendiente | NOT RUN | Busqueda publica no filtra borradores al cliente |
| TC-SEC-006 | userB | `GET /api/users/by-email?email={{userAEmail}}` | 403 | pendiente | pendiente | NOT RUN | Consultar email ajeno |
| TC-SEC-007 | userB | `GET /api/users/by-username?username={{userAUsername}}` | 403 | pendiente | pendiente | NOT RUN | Consultar username ajeno |
| TC-SEC-009 | userB | `GET /api/tickets/{{ticketCancelableCode}}` | 403 | pendiente | pendiente | NOT RUN | Leer ticket ajeno |
| TC-SEC-010 | userB | `GET /api/tickets/by-user?email={{userAEmail}}` | 403 | pendiente | pendiente | NOT RUN | Listar tickets ajenos |
| TC-SEC-008 | userA | `POST /api/tickets` | 403 | pendiente | pendiente | NOT RUN | Compra con identidad ajena |
| TC-SEC-011 | userB | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 403 | pendiente | pendiente | NOT RUN | Cancelar ticket ajeno |
| TC-SEC-012 | userA | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 403 | pendiente | pendiente | NOT RUN | USER no puede registrar uso |
| TC-SEC-013 | userA | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 403 | pendiente | pendiente | NOT RUN | USER no puede listar pagados de evento |
| TC-SEC-015 | admin | `GET /api/events/{{draftEventCode}}` | 200 | pendiente | pendiente | NOT RUN | ADMIN consulta borrador |
| TC-SEC-016 | anonimo | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | Registro no concede rol inyectado |
| TC-SEC-017 | staff | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 403 | pendiente | pendiente | NOT RUN | Revocacion de STAFF con JWT previo |
| TC-SEC-ADMIN-OWN | admin | `GET /api/users/by-email?email={{userAEmail}}` | 200 | pendiente | pendiente | NOT RUN | ADMIN autorizado consulta usuario de soporte |
| VAL-001 | admin | `POST /api/events` | 400 | pendiente | pendiente | NOT RUN | Evento sin campos obligatorios |
| VAL-002 | anonimo | `POST /api/users` | 400 | pendiente | pendiente | NOT RUN | Email invalido |
| VAL-003 | anonimo | `POST /api/users` | 400 | pendiente | pendiente | NOT RUN | Password corto |
| VAL-004 | admin | `POST /api/events` | 400 | pendiente | pendiente | NOT RUN | JSON mal formado |
| VAL-005 | admin | `POST /api/events` | 400 | pendiente | pendiente | NOT RUN | Categoria desconocida |
| VAL-006 | userA | `POST /api/tickets` | 400 | pendiente | pendiente | NOT RUN | Tipo de ticket desconocido |
| VAL-007 | admin | `POST /api/events` | 400 | pendiente | pendiente | NOT RUN | Edad minima negativa |
| VAL-008 | anonimo | `GET /api/artists/no-numerico` | 400 | pendiente | pendiente | NOT RUN | Artista ID no numerico |
| VAL-009 | anonimo | `GET /api/artists/by-stage-name` | 400 | pendiente | pendiente | NOT RUN | Parametro obligatorio ausente |
| VAL-010 | anonimo | `GET /api/events/E2E-NOT-FOUND` | 404 | pendiente | pendiente | NOT RUN | Evento inexistente |
| VAL-011 | anonimo | `GET /api/venues/E2E-NOT-FOUND` | 404 | pendiente | pendiente | NOT RUN | Sede inexistente |
| VAL-012 | anonimo | `GET /api/artists/9223372036854775807` | 404 | pendiente | pendiente | NOT RUN | Artista inexistente |
| VAL-013 | anonimo | `POST /api/users` | 409 | pendiente | pendiente | NOT RUN | Usuario duplicado |
| VAL-014 | admin | `POST /api/events` | 409 | pendiente | pendiente | NOT RUN | Evento duplicado |
| VAL-015 | admin | `POST /api/events` | 409 | pendiente | pendiente | NOT RUN | Fecha pasada |
| VAL-016 | admin | `POST /api/events` | 409 | pendiente | pendiente | NOT RUN | Sede inactiva |
| VAL-017 | anonimo | `POST /api/users` | 409 | pendiente | pendiente | NOT RUN | Nacimiento futuro |
| FLUJO-01 | userA | `POST /api/tickets` | 201 | pendiente | pendiente | NOT RUN | Comprar ticket independiente para uso |
| TC-BIZ-010-check | userA | `GET /api/tickets/{{ticketCancelableCode}}` | 200 | pendiente | pendiente | NOT RUN | Confirmar cancelacion persistida |
| TC-BIZ-011 | userA | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 409 | pendiente | pendiente | NOT RUN | Repetir cancelacion |
| TC-BIZ-012-check | userA | `GET /api/tickets/{{ticketUsableCode}}` | 200 | pendiente | pendiente | NOT RUN | Confirmar uso persistido |
| TC-BIZ-013 | staff | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 409 | pendiente | pendiente | NOT RUN | Repetir uso |
| TC-BIZ-014-draft | userA | `POST /api/tickets` | 409 | pendiente | pendiente | NOT RUN | No comprar borrador |
| TC-BIZ-014-soldout | userA | `POST /api/tickets` | 409 | pendiente | pendiente | NOT RUN | No comprar evento agotado |
| FLUJO-MINOR-01 | anonimo | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | Registrar persona menor de edad |
| FLUJO-MINOR-02 | anonimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | Login persona menor |
| TC-BIZ-014-age | minor | `POST /api/tickets` | 409 | pendiente | pendiente | NOT RUN | Rechazo por edad minima |
| FLUJO-CAP-01 | userA | `POST /api/tickets` | 201 | pendiente | pendiente | NOT RUN | Comprar hasta completar aforo 3 |
| FLUJO-CAP-02 | admin | `GET /api/events/{{ageEventCode}}` | 200 | pendiente | pendiente | NOT RUN | Verificar evento tras completar capacidad |
| FLUJO-INACTIVE-LOGIN | anonimo | `POST /api/auth/login` | 401 | pendiente | pendiente | NOT RUN | Login de fixture inactivo se rechaza |

Consolidacion manual: PASS __ / FAIL __ / BLOCKED __ / NOT RUN __. Cobertura nominal ejecutada __/22.

Defectos: registrar caso, precondiciones, pasos, esperado/observado y evidencia sin secretos.
