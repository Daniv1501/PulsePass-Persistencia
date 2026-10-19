# PulsePass — Capa de Controladores: trazabilidad, Definition of Done y sustentación

## 1. Matriz de trazabilidad (PRD sección 29, completa)

| Requisito | Endpoint | Service de origen | Regla / comportamiento | Tests |
|---|---|---|---|---|
| FR-CTRL-VEN-001 | `GET /api/venues/{code}` | `VenueService.findByCode` | Recurso existente / inexistente | TEST-CTRL-VEN-001, 002 |
| FR-CTRL-VEN-002 | `GET /api/venues/active` | `VenueService.findActiveVenues` | Solo activos | TEST-CTRL-VEN-003 + ruta literal |
| FR-CTRL-EVT-001 | `POST /api/events` | `EventService.create` | Reglas de creación heredadas | TEST-CTRL-EVT-001, 002 + campos faltantes, longitud, JSON mal formado, enum inválido, 409, 404 |
| FR-CTRL-EVT-002 | `GET /api/events/{eventCode}` | `EventService.findByCode` | `ResourceNotFoundException` | TEST-CTRL-EVT-003, 004 |
| FR-CTRL-EVT-003 | `GET /api/events/published` | `EventService.findPublishedEvents` | Solo publicados | TEST-CTRL-EVT-005 + 500 |
| FR-CTRL-EVT-004 | `PATCH /api/events/{eventCode}/publish` | `EventService.publish` | Transición de estado | TEST-CTRL-EVT-006, 007 |
| FR-CTRL-EVT-005 | `POST /api/events/{eventCode}/artists/{artistId}` | `EventService.addArtist` | Asociación no duplicada | TEST-CTRL-EVT-008 + 409 duplicado, `artistId` no numérico |
| FR-CTRL-EVT-006 | `GET /api/events/by-artist?stageName=` | `EventService.findByArtist` | Búsqueda por artista | TEST-CTRL-EVT-009 + `stageName` en blanco |
| FR-CTRL-ART-001 | `GET /api/artists/{id}` | `ArtistService.findById` | Consulta de artistas | TEST-CTRL-ART-001, 002 + `id` no numérico |
| FR-CTRL-ART-002 | `GET /api/artists/by-stage-name?stageName=` | `ArtistService.findByStageName` | Consulta de artistas | TEST-CTRL-ART-003 + 404, parámetro faltante |
| FR-CTRL-ART-003 | `GET /api/artists/active` | `ArtistService.findActiveArtists` | Consulta de artistas | TEST-CTRL-ART-004 |
| FR-CTRL-USR-001 | `POST /api/users` | `UserService.register` | Validaciones + duplicados | TEST-CTRL-USR-001, 002, 003 + body vacío, email duplicado |
| FR-CTRL-USR-002 | `GET /api/users/by-email?email=` | `UserService.findByEmail` | Consulta usuario | TEST-CTRL-USR-004 + 404, email inválido |
| FR-CTRL-USR-003 | `GET /api/users/by-username?username=` | `UserService.findByUsername` | Consulta usuario | TEST-CTRL-USR-005 + 404 |
| FR-CTRL-TKT-001 | `POST /api/tickets` | `TicketService.purchase` | Compra, edad, capacidad, estado | TEST-CTRL-TKT-001..004 + tipo inválido |
| FR-CTRL-TKT-002 | `GET /api/tickets/{ticketCode}` | `TicketService.findByCode` | Consulta | TEST-CTRL-TKT-005 + 404 |
| FR-CTRL-TKT-003 | `GET /api/tickets/by-user?email=` | `TicketService.findByUserEmail` | Consulta por usuario | TEST-CTRL-TKT-006 + parámetro faltante |
| FR-CTRL-TKT-004 | `GET /api/events/{eventCode}/tickets/paid` | `TicketService.findPaidTicketsByEvent` | Solo PAID | TEST-CTRL-TKT-007 |
| FR-CTRL-TKT-005 | `PATCH /api/tickets/{ticketCode}/cancel` | `TicketService.cancel` | PAID → CANCELLED | TEST-CTRL-TKT-008, 009 |
| FR-CTRL-TKT-006 | `PATCH /api/tickets/{ticketCode}/use` | `TicketService.markAsUsed` | PAID → USED | TEST-CTRL-TKT-010, 011 |

### Cobertura Service → HTTP (sección 9)

| Service | Métodos | Endpoints |
|---|---|---|
| `VenueService` | 2 | 2 |
| `EventService` | 6 | 6 |
| `ArtistService` | 3 | 3 |
| `UserService` | 3 | 3 |
| `TicketService` | 6 | 6 (5 en `TicketController` + `/api/events/{eventCode}/tickets/paid`) |
| **Total** | **20** | **20** |

`ControllerLayerRulesTest.everyServiceOperationIsExposedOverHttp` hace fallar el build si se agrega un método a un Service sin su endpoint, o si cambia una ruta del contrato.

### Criterios de aceptación (sección 20)

| Criterio | Dónde se verifica |
|---|---|
| AC-CTRL-001 venue existente → 200 + `VenueResponse` | `VenueControllerTest.shouldReturn200WhenVenueExists` |
| AC-CTRL-002 venue inexistente → 404 con `ErrorResponse` | `VenueControllerTest.shouldReturn404WithErrorResponseWhenVenueDoesNotExist` |
| AC-CTRL-003 `CreateEventRequest` válido → 201 | `EventControllerTest.shouldReturn201WhenCreatingValidEvent` |
| AC-CTRL-004 request inválido → 400 con `details` | `EventControllerTest.shouldReturn400WithInvalidFieldsWhenRequestIsInvalid` |
| AC-CTRL-005 `CMF-2026` en DRAFT → `PATCH /publish` 200 | `EventControllerTest.shouldReturn200WhenPublishingDraftEvent` |
| AC-CTRL-006 `BusinessRuleException` → 409 | `EventControllerTest.shouldReturn409WhenPublishingIsNotAllowed` |
| AC-CTRL-007 `RegisterUserRequest` válido → 201 | `UserControllerTest.shouldReturn201WhenRegisteringValidUser` |
| AC-CTRL-008 email duplicado → 409 sin detalles internos | `UserControllerTest.shouldReturn409WithoutInternalDetailsWhenEmailIsDuplicated` |
| AC-CTRL-009 compra válida → 201 + `TicketResponse` | `TicketControllerTest.shouldReturn201WhenPurchaseIsValid` |
| AC-CTRL-010 menor de edad → 409 | `TicketControllerTest.shouldReturn409WhenBusinessRuleRejectsPurchase` |
| AC-CTRL-011 ticket PAID, cancelar → 200 | `TicketControllerTest.shouldReturn200WhenCancellingPaidTicket` |
| AC-CTRL-012 ticket USED, cancelar → 409 | `TicketControllerTest.shouldReturn409WhenCancellingUsedTicket` |
| AC-CTRL-013 ticket PAID, usar → 200 | `TicketControllerTest.shouldReturn200WhenMarkingPaidTicketAsUsed` |
| AC-CTRL-014 ticket CANCELLED, usar → 409 | `TicketControllerTest.shouldReturn409WhenMarkingCancelledTicketAsUsed` |
| AC-CTRL-015 ningún Controller accede a Repository | `ControllerLayerRulesTest.controllersDependOnlyOnServiceInterfaces` |
| AC-CTRL-016 ningún endpoint retorna entidades JPA | `ControllerLayerRulesTest.endpointsReturnResponseEntityOfDtosOnly` |

### Criterios de calidad (sección 21.1)

| Criterio | Cómo se cumple |
|---|---|
| QT-CTRL-001 una clase de pruebas por Controller | `VenueControllerTest`, `EventControllerTest`, `ArtistControllerTest`, `UserControllerTest`, `TicketControllerTest` |
| QT-CTRL-002 Services como mocks | `@MockitoBean` en todas las clases |
| QT-CTRL-003 status HTTP | `status().isOk()/isCreated()/isBadRequest()/isNotFound()/isConflict()/...` en cada test |
| QT-CTRL-004 `Content-Type` | `content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)` |
| QT-CTRL-005 campos JSON con `jsonPath` | todos los tests de éxito y de error |
| QT-CTRL-006 interacción con Service | `verify(service).metodo(...)` |
| QT-CTRL-007 request inválido no invoca Service | `verify(..., never())` y `verifyNoInteractions(...)` |
| QT-CTRL-008 respuestas 404 | Venue, Event, Artist, User y Ticket |
| QT-CTRL-009 respuestas 409 | Event (publicar, duplicado, artista), User (username/email), Ticket (compra, cancelar, usar) |
| QT-CTRL-010 Bean Validation con 400 | Event, User y Ticket (body) + query params |
| QT-CTRL-011 `ErrorResponse` | `timestamp`, `status`, `error`, `message`, `details` verificados con `jsonPath`; estructura en `ControllerLayerRulesTest` |
| QT-CTRL-012 `mvn clean test` → `BUILD SUCCESS` | ver la Definition of Done |

## 2. Definition of Done (sección 30)

- [x] Existen `VenueController`, `EventController`, `ArtistController`, `UserController` y `TicketController`
- [x] Los cinco Controllers dependen exclusivamente de Services (`ControllerLayerRulesTest`)
- [x] Todos los Controllers responden un objeto `ResponseEntity` (`ControllerLayerRulesTest`)
- [x] Ningún Controller depende directamente de Repository (`ControllerLayerRulesTest`)
- [x] Ningún Controller implementa reglas de negocio: cada método es una línea de delegación, más el armado del `Location` en los POST de creación
- [x] Ningún endpoint retorna entidades JPA (`ControllerLayerRulesTest`)
- [x] Se reutilizan los DTOs de la capa Service (mismos records, con anotaciones de validación agregadas)
- [x] Se cubren los 20 métodos públicos de Service (`ControllerLayerRulesTest`)
- [x] POST de creación retorna 201 (eventos, usuarios, tickets, con cabecera `Location`)
- [x] GET exitoso retorna 200
- [x] PATCH exitoso retorna 200
- [x] Bean Validation retorna 400
- [x] `ResourceNotFoundException` retorna 404
- [x] `DuplicateResourceException` retorna 409
- [x] `BusinessRuleException` retorna 409
- [x] Error inesperado retorna 500
- [x] Todos los errores utilizan `ErrorResponse`
- [x] Todos los handlers retornan `ResponseEntity<ErrorResponse>` (`ControllerLayerRulesTest`)
- [x] Existe `GlobalExceptionHandler` con `@RestControllerAdvice`
- [x] Las pruebas utilizan `@WebMvcTest`
- [x] Los Services se reemplazan con `@MockitoBean`
- [x] Las pruebas utilizan MockMvc
- [x] Las pruebas validan status, content type y JSON
- [x] Se utiliza `verify(...)` para validar llamadas a Service
- [x] Se utiliza `verify(..., never())` cuando un request inválido no debe llegar a Service
- [ ] `mvn clean test` finaliza con `BUILD SUCCESS`: **hay que correrlo en tu máquina**, porque necesita descargar las dependencias nuevas. Para `PersistenceIntegrationTest` además se necesita Docker activo; el resto de la suite no lo necesita.
- [x] El README documenta los endpoints principales y cómo ejecutar las pruebas (`README_PulsePass_Controladores.md`)

## 3. Respuestas para el equipo (sección 31)

1. **¿Qué responsabilidad pertenece al Controller y cuál al Service?**
   El Controller se ocupa de lo HTTP: mapear URL y verbo a un caso de uso, convertir JSON en un DTO, validar la forma del request, elegir el código de respuesta y convertir el resultado en JSON. El Service se ocupa del negocio: decidir si la operación está permitida (usuario activo, evento publicado, edad, capacidad, transiciones de estado), coordinar repositories, manejar la transacción y mapear entidades a DTOs. En PulsePass, cada método de Controller es una línea que delega al Service.

2. **¿Por qué un Controller no debería acceder directamente a Repository?**
   Porque se saltaría las reglas de negocio y las transacciones que viven en el Service. Por ejemplo, un `ticketRepository.save(...)` desde el Controller crearía un ticket sin validar edad ni capacidad, y sin pasar el evento a `SOLD_OUT`. Además acoplaría la API al modelo JPA, obligaría a duplicar lógica en cada endpoint, y el Controller ya no podría probarse con un Service mockeado.

3. **¿Qué diferencia existe entre una validación estructural y una regla de negocio?**
   La estructural se puede decidir mirando solo el request: si el campo viene, si el email tiene formato válido, si la edad mínima es ≥ 0, si el JSON está bien formado. Pertenece a Bean Validation y produce 400. La regla de negocio necesita el estado del sistema: si el usuario existe y está activo, si el evento está `PUBLISHED`, si queda capacidad, si el ticket está `PAID`. Pertenece al Service y produce 404 o 409. Por ejemplo, `"userEmail": "abc"` es estructural (400); Laura, con 17 años, comprando para un evento +18 es una regla de negocio (409).

4. **¿Cuándo debe utilizarse 400, 404 y 409?**
   **400**: el request en sí está mal (falta un campo, formato inválido, JSON roto, tipo incorrecto en la URL); repetirlo igual siempre va a fallar. **404**: el request está bien formado pero el recurso no existe (venue, evento, usuario, artista o ticket inexistente). **409**: el request es válido y el recurso existe, pero la operación choca con el estado actual (código o email duplicado, publicar un evento que no está en DRAFT, cancelar un ticket USED, comprar sin capacidad o sin la edad mínima).

5. **¿Por qué una creación debería retornar 201 en lugar de 200?**
   Porque 201 Created le dice al cliente que se creó un recurso nuevo, no solo que la operación salió bien, y lo acompaña de la cabecera `Location` con la URL del recurso (`/api/events/CMF-2026`). Así un frontend, un proxy o una herramienta de pruebas distinguen una creación de una consulta o de una actualización sin leer el body. Usar 200 para todo es el anti-patrón 25.5 del PRD.

6. **¿Por qué los errores deben utilizar una estructura uniforme?**
   Porque el cliente puede tratar cualquier error con un solo código: leer `status` y `message`, y si hay `details` marcar los campos del formulario. Sin un contrato único, cada endpoint inventaría su formato (texto plano, la página de error por defecto de Spring, un stack trace) y el frontend necesitaría casos especiales. Además, el contrato controla qué información sale: nunca stack traces ni nombres de clases internas (NFR-CTRL-007).

7. **¿Qué ventaja aporta `@RestControllerAdvice` frente a capturar excepciones en cada endpoint?**
   Centraliza la traducción excepción → HTTP en un solo lugar. Los controllers quedan delgados y legibles, sin `try/catch` repetidos. El mapeo (`BusinessRuleException` → 409) se define una sola vez y es consistente en los 20 endpoints. Un endpoint nuevo hereda el manejo de errores sin escribir nada, y cambiar el formato de error es tocar una sola clase. También captura excepciones que ocurren antes de llegar al método del controller (JSON mal formado, validación, parámetros faltantes), que un `try/catch` dentro del método nunca vería.

8. **¿Por qué `@WebMvcTest` es más apropiado que `@SpringBootTest` para estas pruebas?**
   `@WebMvcTest` levanta solo la capa web (controllers, advice, conversión JSON, validación) y deja los Services como mocks. Así se prueba exactamente el contrato HTTP, sin base de datos, sin Flyway, sin Testcontainers y sin ejecutar reglas de negocio. Es más rápido, determinista (NFR-CTRL-008) y, cuando falla, el problema está en el controller o en el handler. `@SpringBootTest` levanta toda la aplicación, incluida la conexión a PostgreSQL; sirve para pruebas de integración, que esta fase deja fuera de alcance.

9. **¿Cómo verificarías que un request inválido nunca invoca el Service?**
   Enviando el request inválido con MockMvc, verificando el 400 y luego `verify(service, never()).metodo(any())`, o `verifyNoInteractions(service)` cuando el mock no debe tocarse en absoluto. Por ejemplo, en `EventControllerTest.shouldReturn400WithInvalidFieldsWhenRequestIsInvalid` se comprueba `verify(eventService, never()).create(any())`. Lo mismo se hace para email inválido, JSON mal formado, enum inválido, `artistId` no numérico y parámetros faltantes.

10. **¿Qué cambios serían necesarios para agregar autenticación JWT en una fase posterior?**
    - Agregar `spring-boot-starter-security` y un resource server o filtro JWT que valide firma, expiración y emisor del token.
    - Definir un `SecurityFilterChain`: endpoints públicos (`GET /api/events/published`, `GET /api/venues/**`, `POST /api/users`) y protegidos con roles `USER`, `ORGANIZER` y `ADMIN` (crear y publicar eventos para organizadores, `use` para personal de acceso). La sesión sería stateless.
    - Agregar un endpoint de login o integrar un proveedor de identidad.
    - Tomar la identidad del token en lugar de que el cliente envíe `userEmail` en `PurchaseTicketRequest`: hoy cualquiera puede comprar a nombre de otro.
    - Responder 401 y 403 también con `ErrorResponse`, usando un `AuthenticationEntryPoint` y un `AccessDeniedHandler`.
    - Actualizar los `@WebMvcTest` con `spring-security-test` (`@WithMockUser` o `jwt()`) y agregar tests de 401 y 403.

    Ni los Services ni el modelo de dominio tendrían que cambiar.

## 4. Decisiones y límites

- **Tickets PAID por evento en `EventController`.** El PRD ubica el endpoint en `/api/events/{eventCode}/tickets/paid` (sección 8.5, "TicketController / EventController"). Por eso `EventController` depende de `EventService` y `TicketService`. `TicketControllerTest` carga ambos controllers para probar TEST-CTRL-TKT-007 donde lo pide la sección 22.5.
- **`Location` en los 201.** `/api/events/{eventCode}`, `/api/tickets/{ticketCode}` y `/api/users/by-username?username=...`. No existe un `GET /api/users/{id}` porque `UserService` no ofrece búsqueda por id, y el PRD no permite agregar operaciones nuevas.
- **405 y 415 explícitos.** No están en la tabla mínima del PRD, pero sin ellos un verbo o un `Content-Type` incorrectos caerían en el handler genérico como 500, lo que contradice la sección 16. Por la misma razón, cualquier otra excepción de Spring MVC que ya traiga un código 4xx conserva ese código.
- **`purchase` sigue validando `type == null` en el Service.** Ahora el Controller lo rechaza antes con 400, pero el chequeo del Service no se toca: la Fase 3 no modifica la Fase 2 y el Service puede tener otros llamadores.
- **Sin paginación, versionado (`/api/v1`) ni autenticación.** Están fuera de alcance según las secciones 26 y 27.
