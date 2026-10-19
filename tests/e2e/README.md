# PulsePass — Fase 5: pruebas end-to-end manuales con Postman

Colección de pruebas de caja negra HTTP contra la API **real** de PulsePass (Spring Boot + PostgreSQL), ejecutada **a mano** desde Postman: una petición a la vez, con **Send**, inspeccionando la respuesta y anotando el resultado.

- **Cobertura:** 22/22 rutas (20 de negocio de la Fase 3 + `POST /api/auth/login` y `GET /api/auth/me` de la Fase 4), con **131 peticiones** y 5 pasos fuera de Postman. Detalle por ruta y actor en [`matriz-cobertura.md`](matriz-cobertura.md).
- **Actores:** anónimo, `USER_A`, `USER_B` (ajeno), `STAFF`, `ADMIN`, `INACTIVE_USER` y una cuenta que intenta inyectar `role=ADMIN`.
- **Sin automatización:** la colección no tiene scripts de Pre-request ni Post-response/Tests, no usa `pm.*`, y no se ejecuta con Runner, Newman ni Postman CLI.
- **Sin cambios a la API:** la colección se ajustó al código y a los DTO reales de las Fases 3 y 4.

> Esta carpeta especifica e implementa las pruebas. **No** es un reporte de resultados: la bitácora se entrega vacía (`NOT RUN`) y la completa quien ejecuta.

## Contenido

| Archivo | Para qué |
|---|---|
| `postman/PulsePass-Manual-E2E.postman_collection.json` | Colección Postman v2.1, carpetas 00–09 |
| `postman/PulsePass-local.postman_environment.json` | Plantilla de variables; contraseñas y tokens vacíos |
| `checklist-e2e-manual.md` | **Orden obligatorio** de ejecución, qué copiar y cuándo correr SQL |
| `registro-ejecucion-manual.md` | Bitácora a completar (esperado, observado, PASS/FAIL/BLOCKED) |
| `matriz-cobertura.md` | 22 rutas × actores, con el caso y el HTTP esperado de cada celda |
| `fixtures/` | SQL de datos de prueba y de cambios de estado, solo para `pulsepass_e2e` |

---

## 1. Requisitos

- Java 21 y el proyecto compilando (`./mvnw -q -DskipTests package`).
- Docker Desktop encendido (para la base E2E) **o** un PostgreSQL local donde pueda crear la base `pulsepass_e2e`.
- Postman Desktop (o Web con el Desktop Agent).

## 2. Base de datos dedicada `pulsepass_e2e`

Las pruebas nunca se ejecutan sobre la base de desarrollo. La forma más simple es un contenedor desechable en el puerto **5433** (no choca con un PostgreSQL local en 5432):

```powershell
docker run -d --name pulsepass-e2e-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=pulsepass_e2e -p 5433:5432 postgres:18-alpine
```

Alternativa sin Docker, en un PostgreSQL local: `CREATE DATABASE pulsepass_e2e;` y use `DB_URL=jdbc:postgresql://localhost:5432/pulsepass_e2e` en el paso 3.

## 3. Arrancar la API y cargar los fixtures

### 3.1 Variables de entorno y arranque (PowerShell)

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5433/pulsepass_e2e"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "postgres"
# Clave JWT exclusiva de E2E: 32 bytes aleatorios. No la guarde en archivos ni la comparta.
$b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
$env:PULSEPASS_JWT_SECRET_BASE64 = [Convert]::ToBase64String($b)
# Solo si el puerto 8080 está ocupado (p. ej. Apache/XAMPP):
$env:SERVER_PORT = "8081"

.\mvnw.cmd spring-boot:run
```

En Git Bash / Linux: `export` de las mismas variables y `PULSEPASS_JWT_SECRET_BASE64=$(openssl rand -base64 32)`.

Al arrancar, Flyway aplica V1–V4 sobre la base vacía. Espere la línea `Started PulsepassApplication`.

### 3.2 Catálogo de prueba

En otra terminal, desde la raíz del repositorio:

```powershell
Get-Content tests\e2e\fixtures\seed-e2e.sql | docker exec -i pulsepass-e2e-db psql -U postgres -d pulsepass_e2e -v ON_ERROR_STOP=1
```

La consulta final debe listar `E2E-DRAFT`, `E2E-MINI` y `E2E-PUB`. Qué crea cada fixture: [`fixtures/README.md`](fixtures/README.md).

### 3.3 Roles privilegiados (se hace durante la ejecución, paso SQL-1)

STAFF y ADMIN **no** se obtienen por HTTP. Primero se registran `e2e_staff` y `e2e_admin` con `POST /api/users` (carpeta 03; nacen como USER) y después se ejecuta:

```powershell
Get-Content tests\e2e\fixtures\grant-e2e-roles.sql | docker exec -i pulsepass-e2e-db psql -U postgres -d pulsepass_e2e -v ON_ERROR_STOP=1
```

Los scripts del bloque de cierre (`estado-desactivar-inactivo.sql`, `estado-revocar-staff.sql`) se ejecutan igual.

### 3.4 Postman

1. **Import** → los dos archivos de `tests/e2e/postman/`.
2. Seleccione el ambiente **PulsePass-local (E2E)** (selector arriba a la derecha).
3. Ajuste `baseUrl` si usó otro puerto (por ejemplo `http://localhost:8081`).
4. En la columna **Current value**, escriba las seis contraseñas (12 a 72 caracteres, ficticias): `userAPassword`, `userBPassword`, `roleInjPassword`, `staffPassword`, `adminPassword`, `inactivePassword`.
5. Ejecute **TC-PREP-001**. Si no responde 200, todo queda BLOCKED hasta resolverlo.

Use siempre **Current value**, no *Initial value*: Postman solo comparte o exporta *Initial value*, así los secretos se quedan en su máquina.

## 4. Ejecución

Siga [`checklist-e2e-manual.md`](checklist-e2e-manual.md) de arriba abajo. Las carpetas de la colección agrupan por módulo; **el checklist define el orden**, porque hay dependencias (tokens, códigos de tickets, cambios de estado). Cada petición trae en su pestaña **Docs** el ID, objetivo, actor, precondiciones, token usado, HTTP esperado y qué revisar.

| Flujo | Qué cubre | Secciones del checklist |
|---|---|---|
| A | Descubrimiento público sin token; borradores ocultos | 2 |
| B | Registro, roles por SQL, login de los 4 actores, `/me`, tokens inválidos | 3, 4, 5, 11 |
| C | ADMIN crea, asocia artista y publica; USER y STAFF son rechazados | 7 |
| D | Compra, anti-IDOR, cancelación (propietario y ADMIN) y uso por STAFF | 6, 8 |
| E | Errores 400/404/409, reglas de compra (borrador, edad, aforo) | 9, 10 |

### 4.1 Variables que se copian a mano

Tras pulsar Send, seleccione el valor en el Body de la respuesta, cópielo y péguelo en el **Current value** de la variable (icono del ojo → *Edit*).

| Caso | Copiar el campo… | …en la variable |
|---|---|---|
| E2E-ART-03 | `id` del artista "Solar Beat" | `artistId` |
| TC-AUTH-002 | `accessToken` | `userAToken` **y** `expiredToken` (anote la hora) |
| E2E-AUTH-01b … 01f | `accessToken` | `userBToken`, `staffToken`, `adminToken`, `inactiveToken`, `roleInjToken` |
| TC-AUTH-009 (antes de enviarlo) | valor de `userAToken` con **un** carácter central cambiado | `tamperedToken` |
| TC-BIZ-007a / 007b / 007c | `ticketCode` | `ticketCancelableCode` (T1) / `ticketUsableCode` (T2) / `ticketAdminCancelCode` (T3) |

Los tokens duran **15 minutos**. Si una petición protegida que debería funcionar devuelve 401, repita el login de ese actor y actualice su variable (nunca `expiredToken`).

### 4.2 Tokens de prueba precargados

El ambiente trae precargados valores que **no son secretos**:

- `malformedToken`: un texto que no es un JWT (TC-AUTH-008 y TC-AUTH-015).
- `testJwtHeaderHs256`, `testJwtHeaderNone`, `testJwtPayload`, `testJwtUnsignedPayload` y `testJwtForeignSignature`: las partes de dos JWT de prueba. TC-AUTH-011a envía `{{testJwtHeaderHs256}}.{{testJwtPayload}}.{{testJwtForeignSignature}}`, firmado HS256 con una clave aleatoria que se generó y se descartó (no es la clave de la API). TC-AUTH-011b envía `{{testJwtHeaderNone}}.{{testJwtUnsignedPayload}}`, sin firma (`alg=none`; esa variable es el contenido terminado en punto).

Ambos JWT tienen `exp` en 2099, así que se rechazan por la firma, no por la fecha. Van partidos en tres variables a propósito: así el detector de secretos de Postman no los confunde con credenciales reales al importar. Postman los vuelve a unir al enviar.

Si al importar aparece igualmente un aviso **Secrets Detected**, no lo cierre con la X (eso cancela la importación): en cada fila elija **Action → Ignore** y continúe.

## 5. Cómo verificar cada respuesta

**Respuesta exitosa (200/201).** El código exacto, no "cualquier 2xx"; `Content-Type: application/json`; la forma correcta (objeto o arreglo); los campos del DTO real. Para POST, el encabezado `Location`. Cuando el caso cambia estado, el siguiente caso es una relectura que confirma lo persistido.

**Error.** El Body es un `ErrorResponse` con exactamente cinco campos:

```json
{ "timestamp": "…", "status": 403, "error": "Forbidden", "message": "…", "details": {} }
```

`status` debe coincidir con el HTTP, y no debe haber stack trace, hash (`$2a$…`), token ni secreto. Los mensajes que produce esta API:

| HTTP | `message` |
|---|---|
| 400 | `Validation failed` (campos en `details`) o `Malformed JSON request` (JSON roto o enum inválido) |
| 401 | `Authentication is required to access this resource` (sin token / token inválido o vencido / usuario inactivo) · `Invalid credentials` (login fallido, mismo texto exista o no el usuario) |
| 403 | `You do not have permission to perform this operation` |
| 404 | `… not found: <código>` |
| 409 | El mensaje de la regla de negocio (p. ej. `Cannot cancel ticket with status CANCELLED`) |

**Seguridad e integridad.** Tras toda mutación denegada (403), el checklist incluye una relectura con un actor autorizado para comprobar que nada cambió. Un 403 sin esa relectura no basta.

**Resultado:**
- **PASS:** HTTP, JSON y efecto de negocio o seguridad coinciden con lo esperado.
- **FAIL:** algo no coincide, o aparece un 500 (un 500 nunca es esperado).
- **BLOCKED:** falta un fixture, un actor, un token o una respuesta previa indispensable. Los casos que dependen de un paso fallido van como BLOCKED, nunca como PASS.

## 6. Repetir desde cero

Lo recomendado es descartar la base y crearla de nuevo:

```powershell
docker rm -f pulsepass-e2e-db
docker run -d --name pulsepass-e2e-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=pulsepass_e2e -p 5433:5432 postgres:18-alpine
```

Después, reinicie la API (Flyway vuelve a migrar), ejecute `seed-e2e.sql` y vacíe en Postman las variables de tokens y tickets.

Si prefiere no reiniciar, cambie `runId` y los valores que lo llevan (`userAUsername`, `userAEmail`, `userBUsername`, `userBEmail`, `roleInjUsername`, `roleInjEmail`, `eventToPublishCode`, por ejemplo `r1` → `r2`). Los fixtures E2E (`E2E-MINI` agotado, `e2e_inactive` desactivado, STAFF revocado) **no** vuelven solos a su estado inicial, así que repetir sin reiniciar la base solo sirve para casos parciales.

Nunca ejecute `docker rm` ni `DROP DATABASE` sobre otra base. Los scripts SQL ya se niegan a correr fuera de `pulsepass_e2e`.

## 7. Políticas y decisiones registradas

| Tema | Decisión (verificada contra la Fase 4) |
|---|---|
| Ticket o usuario **ajeno** | **403** (`expectedForeignResourceStatus`). Para USER, un ticket **inexistente** también es 403, así no se revela si el código existe (TC-HTTP-014). Para STAFF/ADMIN, un inexistente es 404 (TC-HTTP-013). |
| Evento **borrador** visto por anónimo o USER | **404**, igual que un código inexistente (TC-SEC-004). STAFF y ADMIN lo ven. |
| Bearer inválido en una ruta **pública** | **401**: si se envía un token, se valida; nunca se ignora en silencio (TC-AUTH-015). |
| Token en query string | Se ignora: la petición queda anónima y la ruta protegida responde 401 (TC-AUTH-012). |
| Campo `role` en el registro | Se ignora; la cuenta nace solo con USER (TC-SEC-016, verificado con `/me` en E2E-AUTH-02f). |
| Cancelar un ticket ajeno | Solo propietario o ADMIN; STAFF recibe 403 (E2E-TKT-05c). |
| STAFF global | STAFF puede ver y usar tickets de **cualquier** evento: el dominio no modela asignación de personal a eventos. Es una **limitación del MVP** heredada de la Fase 4, no un control por evento. |

## 8. Límites conocidos de la prueba manual

- **Token con issuer distinto (parte de TC-AUTH-011):** fabricarlo exige la clave HMAC de la API, y la colección no debe contenerla. Lo cubre la prueba automatizada `JwtServiceTest` de la Fase 4. Manualmente se prueba la firma con otra clave y `alg=none`.
- **TC-AUTH-010 (JWT expirado)** usa el token real copiado al principio, ya vencido (≥ 15 min). No hace falta una clave de prueba, pero sí que la sesión dure más de 15 minutos; el checklist lo pone al final.
- **TC-SEC-017** deja a `e2e_staff` sin STAFF. Para seguir usándolo, ejecute de nuevo `grant-e2e-roles.sql`.

## 9. Confidencialidad

- Contraseñas y tokens solo en **Current value**; el archivo de ambiente versionado los lleva vacíos.
- No exporte el ambiente con valores, no pegue tokens en la bitácora ni en capturas, y no use servicios web de terceros para decodificar JWT.
- Al terminar, vacíe las variables `*Token` y `*Password`.
- `tests/e2e/.gitignore` impide versionar ambientes exportados y capturas.
