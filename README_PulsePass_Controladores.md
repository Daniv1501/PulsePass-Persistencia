# PulsePass — Capa de Controladores REST (Fase 3)

Tercera capa del caso de estudio. Encima de Persistencia (Fase 1) y Servicios (Fase 2) se agrega la frontera HTTP: cinco `@RestController`, validación de requests con Bean Validation, un `GlobalExceptionHandler` que traduce excepciones a un contrato JSON único, y pruebas aisladas de cada endpoint con `@WebMvcTest` + MockMvc.

La regla de la fase es simple: **el Controller no decide nada de negocio**. Recibe el request, valida su forma, llama al Service y devuelve el DTO con el código HTTP correcto. Si el Service lanza una excepción, el handler global la convierte en `ErrorResponse`.

```text
HTTP Request → Controller (@Valid) → Service (reglas) → Repository → PostgreSQL
                    ↑                       │
                    └── Response DTO ───────┘
              Exception → GlobalExceptionHandler → ResponseEntity<ErrorResponse>
```

## Qué se agregó

```text
src/main/java/com/pulsepass
├── controller/
│   ├── VenueController.java
│   ├── EventController.java
│   ├── ArtistController.java
│   ├── UserController.java
│   └── TicketController.java
├── dto/request/            → ahora con anotaciones de Bean Validation
│   ├── CreateEventRequest.java
│   ├── RegisterUserRequest.java
│   └── PurchaseTicketRequest.java
├── dto/response/
│   └── ErrorResponse.java  → nuevo (contrato de errores)
└── exception/
    └── GlobalExceptionHandler.java   → nuevo (@RestControllerAdvice)

src/test/java/com/pulsepass
├── controller/
│   ├── VenueControllerTest.java
│   ├── EventControllerTest.java
│   ├── ArtistControllerTest.java
│   ├── UserControllerTest.java
│   └── TicketControllerTest.java
└── architecture/
    └── ControllerLayerRulesTest.java
```

Dependencias nuevas en `pom.xml`:

| Dependencia | Para qué |
|---|---|
| `spring-boot-starter-webmvc` | Spring MVC, Jackson y Tomcat embebido |
| `spring-boot-starter-validation` | Bean Validation (Hibernate Validator) |
| `spring-boot-starter-webmvc-test` (test) | `@WebMvcTest`, MockMvc, `jsonPath` |

También se activó `<parameters>true</parameters>` en el compilador. Igual, todos los `@PathVariable` y `@RequestParam` declaran su nombre explícitamente, así que no dependen de esa opción.

No se tocaron entidades, migraciones, repositories, mappers ni implementaciones de Service. Los DTOs de request son los mismos records de la Fase 2; solo se les agregaron anotaciones de validación, sin cambiar sus componentes.

## Endpoints

Los 20 métodos públicos de los Services de la Fase 2 tienen exactamente un endpoint.

### Venues — `VenueController`

| Método | Endpoint | Service | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/venues/{code}` | `findByCode(code)` | 200 | 404 |
| GET | `/api/venues/active` | `findActiveVenues()` | 200 | — |

### Eventos — `EventController`

| Método | Endpoint | Service | Éxito | Errores |
|---|---|---|---|---|
| POST | `/api/events` | `create(request)` | 201 + `Location` | 400, 404 (venue), 409 |
| GET | `/api/events/{eventCode}` | `findByCode(eventCode)` | 200 | 404 |
| GET | `/api/events/published` | `findPublishedEvents()` | 200 | — |
| PATCH | `/api/events/{eventCode}/publish` | `publish(eventCode)` | 200 | 404, 409 |
| POST | `/api/events/{eventCode}/artists/{artistId}` | `addArtist(eventCode, artistId)` | 200 | 400, 404, 409 |
| GET | `/api/events/by-artist?stageName=...` | `findByArtist(stageName)` | 200 | 400 |
| GET | `/api/events/{eventCode}/tickets/paid` | `TicketService.findPaidTicketsByEvent(eventCode)` | 200 | — |

### Artistas — `ArtistController`

| Método | Endpoint | Service | Éxito | Errores |
|---|---|---|---|---|
| GET | `/api/artists/{id}` | `findById(id)` | 200 | 400, 404 |
| GET | `/api/artists/by-stage-name?stageName=...` | `findByStageName(stageName)` | 200 | 400, 404 |
| GET | `/api/artists/active` | `findActiveArtists()` | 200 | — |

### Usuarios — `UserController`

| Método | Endpoint | Service | Éxito | Errores |
|---|---|---|---|---|
| POST | `/api/users` | `register(request)` | 201 + `Location` | 400, 409 |
| GET | `/api/users/by-email?email=...` | `findByEmail(email)` | 200 | 400, 404 |
| GET | `/api/users/by-username?username=...` | `findByUsername(username)` | 200 | 400, 404 |

### Tickets — `TicketController`

| Método | Endpoint | Service | Éxito | Errores |
|---|---|---|---|---|
| POST | `/api/tickets` | `purchase(request)` | 201 + `Location` | 400, 404, 409 |
| GET | `/api/tickets/{ticketCode}` | `findByCode(ticketCode)` | 200 | 404 |
| GET | `/api/tickets/by-user?email=...` | `findByUserEmail(email)` | 200 | 400 |
| PATCH | `/api/tickets/{ticketCode}/cancel` | `cancel(ticketCode)` | 200 | 404, 409 |
| PATCH | `/api/tickets/{ticketCode}/use` | `markAsUsed(ticketCode)` | 200 | 404, 409 |

`GET /api/events/{eventCode}/tickets/paid` está en `EventController`, porque su recurso raíz es el evento (la sección 8.5 del PRD lo agrupa como "TicketController / EventController"). El controller solo delega en `TicketService`.

Las rutas literales (`/active`, `/published`, `/by-artist`) conviven con las variables (`/{code}`, `/{eventCode}`). Spring MVC siempre prefiere el patrón más específico, así que `GET /api/venues/active` nunca llega a `findByCode("active")`; hay un test que lo comprueba.

## Ejemplos

Crear un evento:

```http
POST /api/events
Content-Type: application/json

{
  "eventCode": "CMF-2026",
  "name": "Caribbean Music Fest 2026",
  "description": "Festival de música del Caribe",
  "category": "MUSIC",
  "eventDate": "2026-12-05T20:00:00",
  "minimumAge": 18,
  "venueCode": "VEN-SMR-01"
}
```

```http
HTTP/1.1 201 Created
Location: /api/events/CMF-2026
Content-Type: application/json

{ "eventCode": "CMF-2026", "status": "DRAFT", "venueCode": "VEN-SMR-01", "artists": [], ... }
```

Comprar un ticket:

```http
POST /api/tickets
Content-Type: application/json

{ "userEmail": "andrea@pulsepass.com", "eventCode": "CMF-2026", "type": "VIP" }
```

Flujo completo del escenario (sección 19 del PRD), con `curl`:

```bash
curl http://localhost:8080/api/venues/VEN-SMR-01
curl -X POST http://localhost:8080/api/events -H "Content-Type: application/json" -d @evento.json
curl -X POST http://localhost:8080/api/events/CMF-2026/artists/1
curl -X PATCH http://localhost:8080/api/events/CMF-2026/publish
curl http://localhost:8080/api/events/published
curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d @andrea.json
curl -X POST http://localhost:8080/api/tickets -H "Content-Type: application/json" \
     -d '{"userEmail":"andrea@pulsepass.com","eventCode":"CMF-2026","type":"VIP"}'
curl "http://localhost:8080/api/tickets/by-user?email=andrea@pulsepass.com"
curl -X PATCH http://localhost:8080/api/tickets/TCK-XXXXXXXX/use
```

## Validación de entrada

La validación **estructural** se hace en el Controller con `@Valid` y anotaciones en los records, antes de llamar al Service. Las reglas **de negocio** siguen en el Service.

| Request | Campo | Validación |
|---|---|---|
| `CreateEventRequest` | `eventCode` | `@NotBlank`, `@Size(max = 30)` |
| | `name` | `@NotBlank`, `@Size(max = 150)` |
| | `description` | `@Size(max = 2000)` (opcional) |
| | `category` | `@NotNull` |
| | `eventDate` | `@NotNull` |
| | `minimumAge` | `@NotNull`, `@Min(0)` |
| | `venueCode` | `@NotBlank`, `@Size(max = 30)` |
| `RegisterUserRequest` | `username` | `@NotBlank`, `@Size(max = 50)` |
| | `email` | `@NotBlank`, `@Email`, `@Size(max = 150)` |
| | `firstName`, `lastName` | `@NotBlank`, `@Size(max = 80)` |
| | `phone`, `city` | `@Size` (opcionales) |
| | `birthDate` | `@NotNull` |
| `PurchaseTicketRequest` | `userEmail` | `@NotBlank`, `@Email` |
| | `eventCode` | `@NotBlank` |
| | `type` | `@NotNull` |

Los máximos de longitud coinciden con las columnas de `V1__create_schema.sql`, así que un texto demasiado largo se rechaza con 400 en lugar de explotar en la base como 500. `description` es `TEXT` en la base; el límite de 2000 caracteres es una decisión de contrato.

Los query params obligatorios (`stageName`, `email`, `username`) también se validan: si faltan, o vienen en blanco o con un email mal formado, responden 400 sin llegar al Service.

| Lo valida el Controller | Lo valida el Service |
|---|---|
| campo obligatorio | usuario activo |
| formato de email | evento existente |
| longitud | evento publicado |
| valor mínimo | edad mínima |
| JSON mal formado, enum o fecha inválidos | capacidad |
| tipo de path variable (`artistId` numérico) | transiciones de estado / duplicados de negocio |

## Contrato de errores

Todos los errores, incluidos los de validación, responden con el mismo record:

```java
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> details) { }
```

```json
{
  "timestamp": "2026-10-04T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": {
    "eventCode": "Event code is required",
    "venueCode": "Venue code is required"
  }
}
```

`GlobalExceptionHandler` (`@RestControllerAdvice`) es el único lugar donde se traducen excepciones; ningún controller tiene `try/catch`. Todos sus métodos devuelven `ResponseEntity<ErrorResponse>`.

| Excepción / condición | HTTP | `message` | `details` |
|---|---|---|---|
| `MethodArgumentNotValidException` (`@Valid @RequestBody`) | 400 | `Validation failed` | campo → mensaje |
| `HandlerMethodValidationException` (`@RequestParam` con restricciones) | 400 | `Validation failed` | parámetro → mensaje |
| `HttpMessageNotReadableException` (JSON mal formado, enum o fecha inválidos) | 400 | `Malformed JSON request` | vacío |
| `MissingServletRequestParameterException` | 400 | `Validation failed` | parámetro → `Parameter is required` |
| `MethodArgumentTypeMismatchException` (`/api/artists/abc`) | 400 | `Validation failed` | parámetro → valor inválido |
| `ResourceNotFoundException` | 404 | mensaje del Service | vacío |
| `NoResourceFoundException` (ruta inexistente) | 404 | `Resource not found: ...` | vacío |
| `HttpRequestMethodNotSupportedException` | 405 | `HTTP method not supported: ...` | vacío |
| `HttpMediaTypeNotSupportedException` | 415 | `Content type not supported: ...` | vacío |
| `DuplicateResourceException` | 409 | mensaje del Service | vacío |
| `BusinessRuleException` | 409 | mensaje del Service | vacío |
| Otra excepción de Spring MVC con código 4xx | su código | frase estándar | vacío |
| Cualquier otra `Exception` | 500 | `An unexpected error occurred` | vacío |

En el 500, el detalle real (mensaje y stack trace) se registra en el log del servidor y nunca se envía al cliente (NFR-CTRL-007). Los 405 y 415 no están en la tabla mínima del PRD, pero sin ellos esos casos caerían en el handler genérico y responderían un 500 falso.

## Cómo correr las pruebas

```bash
mvnw.cmd clean test          # Windows
./mvnw clean test            # Linux / macOS
```

Solo las pruebas de Controller (no necesitan Docker ni PostgreSQL):

```bash
mvnw.cmd test -Dtest="*ControllerTest,ControllerLayerRulesTest"
```

`PersistenceIntegrationTest` sigue necesitando Docker activo (Testcontainers). El resto de la suite, incluidas todas las pruebas de esta fase, corre sin infraestructura externa.

Para levantar la API contra un PostgreSQL local:

```bash
set DB_URL=jdbc:postgresql://localhost:5432/pulsepass
set DB_USER=postgres
set DB_PASSWORD=postgres
mvnw.cmd spring-boot:run
```

La API queda en `http://localhost:8080`. Flyway crea el esquema y carga los artistas del catálogo; venues y usuarios se crean con los endpoints o con SQL.

## Pruebas de la capa Controller

Cada controller tiene su clase con `@WebMvcTest`, que levanta solo la capa web: controllers, `@RestControllerAdvice`, Jackson y validación. No hay `@SpringBootTest`, PostgreSQL ni Testcontainers. Los Services se reemplazan con `@MockitoBean`, así que no se ejecuta lógica de negocio real.

```java
@WebMvcTest(EventController.class)
class EventControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private EventService eventService;
    @MockitoBean private TicketService ticketService;
}
```

Cada test valida el status HTTP, el `Content-Type`, los campos relevantes con `jsonPath`, y la interacción con el Service mediante `verify(...)`. Cuando el request es inválido, se confirma que nunca llegó al Service:

```java
verify(eventService, never()).create(any());
```

Los bodies JSON están escritos como text blocks. Así el test ve exactamente lo que enviaría un cliente real y no depende del `ObjectMapper` (en Spring Boot 4 Jackson pasó a la versión 3, con otro paquete).

| Clase | Tests | Cubre |
|---|---|---|
| `VenueControllerTest` | 4 | TEST-CTRL-VEN-001..003, ruta literal `/active` |
| `EventControllerTest` | 19 | TEST-CTRL-EVT-001..009, JSON mal formado, enum inválido, `description` larga, duplicado, venue inexistente, artista duplicado, `artistId` no numérico, `stageName` en blanco, 500 sin detalles internos |
| `ArtistControllerTest` | 7 | TEST-CTRL-ART-001..004, `stageName` inexistente, `id` no numérico, parámetro faltante |
| `UserControllerTest` | 10 | TEST-CTRL-USR-001..005, body vacío, email duplicado sin detalles internos, 404 por email/username, email inválido en query |
| `TicketControllerTest` | 15 | TEST-CTRL-TKT-001..011, tipo inválido, ticket inexistente, parámetro faltante, 405 |
| `ControllerLayerRulesTest` | 7 | Reglas de arquitectura por reflexión (ver abajo) |

`ControllerLayerRulesTest` comprueba por reflexión, sin Spring:

- los cinco controllers son `@RestController` bajo `/api/`;
- solo dependen de interfaces Service, con campos `final` e inyección por constructor (AC-CTRL-015);
- todo endpoint devuelve `ResponseEntity` de DTOs `response` y nunca entidades JPA (AC-CTRL-016);
- todo `@RequestBody` lleva `@Valid`;
- existen exactamente los 20 endpoints del contrato y hay 20 métodos de Service;
- `GlobalExceptionHandler` es `@RestControllerAdvice` y todos sus handlers devuelven `ResponseEntity<ErrorResponse>`;
- `ErrorResponse` es un record con los cinco componentes del contrato.

La matriz de trazabilidad, la Definition of Done y las respuestas a las preguntas para el equipo están en `docs/PulsePass_Controladores_Trazabilidad.md`.
