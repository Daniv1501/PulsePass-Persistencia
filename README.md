INTEGRANTES: _(completar nombres y códigos)_

# PulsePass — Fase 4: Seguridad con Spring Security y JWT

PulsePass es el caso de estudio de una plataforma de eventos y venta de entradas. Este repositorio ya traía las tres fases anteriores (persistencia, servicios y 20 endpoints REST). En esta fase se les pone un candado: ahora la API sabe **quién** hace cada petición y **qué** puede hacer, sin tocar las reglas de negocio que ya existían.

| Fase | Qué aporta |
|---|---|
| 1 · Persistencia | Entidades JPA, PostgreSQL, Flyway (V1–V3), repositories |
| 2 · Servicios | Reglas de negocio, DTOs `record`, MapStruct, transacciones |
| 3 · Controladores | 20 operaciones HTTP, Bean Validation, `GlobalExceptionHandler`, `ErrorResponse` |
| **4 · Seguridad** | Registro con contraseña BCrypt, login, JWT firmado (HS256), roles, propiedad de tickets, errores 401/403 en JSON |

## Cómo está armada la seguridad

```text
Cliente ── Authorization: Bearer <jwt> ──▶ Spring Security (filtros)
                                            ├─ JwtAuthenticationFilter  → valida el token y recarga usuario + roles desde la BD
                                            ├─ reglas por ruta y rol    → authorizeHttpRequests
                                            ├─ sin identidad            → RestAuthenticationEntryPoint → 401
                                            └─ sin permiso              → RestAccessDeniedHandler      → 403
                                         ▼
                                    Controller (delgado)
                                         ▼
                                    Service (negocio + @PreAuthorize de propiedad)
                                         ▼
                                    Repository / PostgreSQL
```

Piezas nuevas (todo bajo `com.pulsepass`):

- `security/`: `SecurityConfig`, `JwtProperties`, `JwtService`, `JwtAuthenticationFilter`, `CustomUserDetailsService`, `SecurityPrincipal`, `PasswordEncoderConfig`, `AuthenticationManagerConfig`, `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`, `SecurityErrorWriter`, `InvalidJwtException`.
- `service/`: `AuthService` (registro atómico y login) y `AuthorizationService` (compara al usuario autenticado con el dueño del recurso).
- `controller/AuthController`: `POST /api/auth/login` y `GET /api/auth/me`.
- `domain/`: `UserCredential`, `UserRole`, `UserRoleId`, `Role`. `repository/`: `UserCredentialRepository`, `UserRoleRepository`.
- `dto/`: `LoginRequest`, `AuthResponse`, `AuthenticatedUserResponse`. `RegisterUserRequest` ahora exige `password`.
- Migración `V4__security_credentials_roles.sql`.

## Variables de entorno

| Variable | ¿Obligatoria? | Para qué sirve |
|---|---|---|
| `PULSEPASS_JWT_SECRET_BASE64` | **Sí** | Clave HMAC en Base64 con **mínimo 32 bytes**. Si falta o es corta, la aplicación no arranca. |
| `PULSEPASS_JWT_ISSUER` | No | Emisor del token (por defecto `pulsepass-api`). |
| `DB_URL` | No | Por defecto `jdbc:postgresql://localhost:5432/pulsepass`. |
| `DB_USER` / `DB_PASSWORD` | No | Por defecto `postgres` / `postgres`. |

El tiempo de vida del token es `PT15M` (900 s), configurable en `application.yml` (`pulsepass.security.jwt.access-token-ttl`).

Generar una clave:

```bash
openssl rand -base64 32
```

```powershell
$b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

`.env.example` es solo una plantilla sin secretos. `.env` y `dev-env.ps1` están en `.gitignore`: nunca se sube una clave real. Los tests usan una clave ficticia propia en `src/test/resources/config/application.yml`.

## Levantar la aplicación

Necesitas JDK 21, PostgreSQL con una base `pulsepass` creada y Maven (o el wrapper incluido).

```bash
export DB_URL=jdbc:postgresql://localhost:5432/pulsepass
export DB_USER=postgres
export DB_PASSWORD=<tu clave>
export PULSEPASS_JWT_SECRET_BASE64=$(openssl rand -base64 32)

mvn spring-boot:run        # o ./mvnw spring-boot:run
```

En Windows (PowerShell) se definen igual con `$env:NOMBRE = "valor"` y se usa `.\mvnw.cmd spring-boot:run`. Al iniciar, Flyway aplica V1–V4 y Hibernate valida el esquema (`ddl-auto: validate`). La API queda en `http://localhost:8080`.

## Base de datos (migración V4)

```text
user_credentials (1:1 con users)          user_roles (N:1 con users)
 ├─ user_id  PK + FK                        ├─ user_id FK
 ├─ password_hash  NOT NULL (BCrypt)        └─ role  USER | STAFF | ADMIN      PK(user_id, role)
 └─ password_changed_at
```

- V1–V3 no se tocaron; la fase agrega una migración nueva.
- Los usuarios que ya existían reciben el rol `USER` pero **no** una credencial: no pueden iniciar sesión hasta una activación segura. No se inventa ninguna contraseña por defecto.
- El registro público siempre asigna `USER`. `STAFF` y `ADMIN` solo se otorgan por SQL controlado (ver `scripts/demo-seed.sql`).

## Autenticación

1. **Registro** — `POST /api/users` crea usuario, hash BCrypt y rol `USER` en una sola transacción (`AuthService.register`). Contraseña: 12 a 72 caracteres (y máximo 72 bytes); el username no admite `@` ni espacios. La respuesta no trae contraseña ni hash. El registro **no** devuelve token: el flujo es registro → login.
2. **Login** — `POST /api/auth/login`:
   ```json
   { "identifier": "andrea", "password": "UnaFraseSeguraLarga2026!" }
   ```
   responde `200`:
   ```json
   { "accessToken": "<jwt>", "tokenType": "Bearer", "expiresIn": 900 }
   ```
   Contraseña mala, usuario inexistente, inactivo o sin credencial dan siempre el mismo `401` genérico.
3. **Quién soy** — `GET /api/auth/me` con Bearer devuelve `id`, `username`, `email` y `roles`.
4. El resto de rutas protegidas se llaman con `Authorization: Bearer <accessToken>`. El token solo se acepta por ese encabezado (un JWT en la query string se ignora).

### Sobre el JWT

- Claims: `sub` (ID del usuario), `iss`, `iat`, `exp`, `jti`. No lleva contraseña ni roles.
- Los **roles y el estado activo se consultan a la base en cada petición**: si desactivas a un usuario o le quitas un rol, el cambio rige desde la siguiente petición aunque su token siga vigente.
- `JwtService` exige HS256, verifica la firma antes de confiar en los claims y rechaza tokens vencidos, manipulados, sin firma (`alg=none`), de otro emisor o sin `sub`/`exp`.
- API *stateless*: sin sesión ni `JSESSIONID`. CSRF está desactivado porque no se usan cookies de autenticación.

## Quién puede qué (las 20 operaciones)

| Operación | Anónimo | USER | STAFF | ADMIN |
|---|---|---|---|---|
| GET `/api/venues/{code}`, `/api/venues/active` | ✔ | ✔ | ✔ | ✔ |
| GET `/api/artists/{id}`, `/by-stage-name`, `/active` | ✔ | ✔ | ✔ | ✔ |
| GET `/api/events/published` | ✔ | ✔ | ✔ | ✔ |
| GET `/api/events/{eventCode}`, `/api/events/by-artist` | solo publicados | solo publicados | todos | todos |
| POST `/api/events` | 401 | 403 | 403 | ✔ |
| PATCH `/api/events/{eventCode}/publish` | 401 | 403 | 403 | ✔ |
| POST `/api/events/{eventCode}/artists/{artistId}` | 401 | 403 | 403 | ✔ |
| POST `/api/users` | ✔ (rol inicial `USER`) | ✔ | ✔ | ✔ |
| GET `/api/users/by-email`, `/by-username` | 401 | solo el propio | solo el propio | cualquiera |
| POST `/api/tickets` | 401 | ✔ con su propio email | 403 | 403 |
| GET `/api/tickets/{ticketCode}` | 401 | solo propios | cualquiera | cualquiera |
| GET `/api/tickets/by-user?email=` | 401 | solo el propio | solo el propio | cualquiera |
| GET `/api/events/{eventCode}/tickets/paid` | 401 | 403 | ✔ | ✔ |
| PATCH `/api/tickets/{ticketCode}/cancel` | 401 | solo propios | solo propios | cualquiera |
| PATCH `/api/tickets/{ticketCode}/use` | 401 | 403 | ✔ | ✔ |

### Protección contra IDOR

- La propiedad se decide con el **ID persistente** del usuario autenticado frente al dueño leído de la base. Un `userEmail` o `userId` que llegue en la petición nunca cuenta como identidad.
- En `POST /api/tickets` el `userEmail` del cuerpo debe ser el del usuario autenticado; tampoco el ADMIN puede comprar a nombre de otro.
- Un `USER` recibe **403** al consultar un ticket ajeno o uno que no existe, para que no pueda adivinar qué códigos existen.
- Los eventos que no están publicados responden **404** al público, así no se revelan borradores.
- Las reglas viven en `AuthorizationService` y se aplican con `@PreAuthorize` en los Services, no solo en los Controllers.

## Formato de errores

Todos los errores usan el mismo `ErrorResponse` (`timestamp`, `status`, `error`, `message`, `details`). Los fallos de seguridad ocurren antes del `DispatcherServlet`, por eso el *entry point* y el *access denied handler* escriben el mismo JSON.

| Caso | HTTP |
|---|---|
| JSON o Bean Validation inválido | 400 |
| Sin Bearer, token vencido/alterado o login incorrecto | 401 |
| Usuario válido sin rol o sin ser dueño | 403 |
| Recurso inexistente (o borrador para el público) | 404 |
| Duplicado o regla de negocio | 409 |
| Error inesperado (sin stacktrace) | 500 |

```json
{
  "timestamp": "2026-10-16T15:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "No tiene permisos para realizar esta operación",
  "details": {}
}
```

## Cuentas de prueba

El registro público no crea `STAFF` ni `ADMIN`, así que para la demostración:

1. Registra por la API `andrea`, `carlos`, `laura` y `miguel` con `POST /api/users`.
2. Ejecuta `scripts/demo-seed.sql` en la base (pgAdmin o `psql`): crea el venue `VEN-SMR-01`, el evento `CMF-2026` en borrador, y marca a `laura` como `STAFF` y a `miguel` como `ADMIN`. Se puede correr varias veces.

| Usuario | Rol | Contraseña de demo (solo desarrollo) |
|---|---|---|
| andrea | USER | `UnaFraseSegura2026!` |
| carlos | USER | `UnaFraseSegura2026!` |
| laura | STAFF | `UnaFraseSegura2026!` |
| miguel | ADMIN | `UnaFraseSegura2026!` |

## Demostración con Postman

Importa `docs/postman/PulsePass-Fase4.postman_collection.json`. La variable `baseUrl` apunta a `http://localhost:8080` y los tokens se guardan solos.

1. Carpeta **A. Registro** (crea los cuatro usuarios, prueba duplicado → 409 y contraseña corta → 400).
2. Ejecuta `scripts/demo-seed.sql`.
3. Carpeta **B. Login y /me** (`expiresIn = 900`, login incorrecto → 401).
4. Carpeta **C. Escenario de demostración** (rutas públicas, 401/403, tickets ajenos, uso por STAFF, token alterado).
5. `UPDATE users SET active = false WHERE username = 'carlos';`
6. Carpeta **D. Usuario desactivado** (su token aún vigente y su login dan 401).

Los tokens duran 15 minutos; si tardas más entre B y C, repite B. El evento debe estar publicado antes de que Andrea compre, por eso en el escenario el ADMIN lo publica y le asocia a Solar Beat primero.

## Pruebas y comandos de verificación

```bash
mvn test                              # todo (las de integración necesitan Docker)
mvn test -Dtest='!*IntegrationTest'   # sin Docker: unitarias y MockMvc
mvn -q -DskipTests package            # compila y empaqueta
```

| Tipo | Clases | Requiere |
|---|---|---|
| Unitarias de seguridad | `JwtServiceTest`, `JwtPropertiesTest`, `JwtAuthenticationFilterTest`, `CustomUserDetailsServiceTest`, `PasswordEncoderConfigTest`, `AuthServiceTest`, `AuthorizationServiceTest` | — |
| MockMvc con la cadena de filtros real | `SecurityMatrixTest` (ruta × rol) y `controller/*ControllerTest` | — |
| Negocio (Fase 2) | `*ServiceImplTest`, `PulsePassAcceptanceScenarioTest`, `ServiceLayerRulesTest`, `MapperTest` | — |
| Integración con PostgreSQL real | `PersistenceIntegrationTest`, `SecurityPersistenceIntegrationTest`, `AuthServiceIntegrationTest`, `OwnershipSecurityIntegrationTest` | Docker |

Los tests de seguridad no apagan los filtros (`addFilters = false`): prueban la cadena de verdad. Las pruebas de negocio anteriores siguen vigentes; solo se les agregó una identidad (o la contraseña) donde hacía falta.

## Límites conocidos

Esto es un MVP académico; queda fuera a propósito:

- Refresh tokens, revocación por `jti`, logout del lado servidor, MFA y recuperación o cambio de contraseña. Un token robado vale hasta que vence (15 min).
- Límite de intentos de login y protección contra fuerza bruta.
- Rotación de la clave HMAC y gestor de secretos (en producción se necesitan).
- `STAFF` es global: el dominio todavía no asigna personal a eventos, así que cualquier STAFF puede ver y marcar como usado cualquier ticket. Debe restringirse (por ejemplo con una entidad `EventStaffAssignment`).
- Solo los eventos `PUBLISHED` son públicos; `SOLD_OUT`, `CANCELLED` y `FINISHED` se tratan como no publicados.
- HTTPS y CORS no se configuran aquí: HTTPS es obligatorio fuera de localhost.

## Documentación de fases anteriores

`README_PulsePass_Servicio.md`, `README_PulsePass_Controladores.md` y la carpeta `docs/` conservan la documentación de las fases 2 y 3.
