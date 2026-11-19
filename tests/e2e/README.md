# PulsePass — Postman E2E manual

Entrega del PRD de pruebas E2E: colección v2.1 con **93 solicitudes**, diez carpetas, las **22 rutas base** (20 de negocio + login + me), ambiente sin secretos, checklist, fixtures, matriz y bitácora. Las solicitudes se envían individualmente con **Send**, sin scripts, Runner, Newman ni Postman CLI.

La colección está preparada, no ejecutada manualmente. La bitácora empieza en **NOT RUN**: los tests automatizados de Java no sustituyen las observaciones desde Postman.

## 1. Preparar el backend

Requisitos: Java 21, Docker Desktop activo, Maven wrapper y Postman. Desde la raíz del repositorio:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Start-PulsePassE2E.ps1
```

El script solicita las contraseñas sin mostrarlas y genera una clave JWT aleatoria en memoria si no hay una configurada. Levanta PostgreSQL 18 en `localhost:5433` y la API en `localhost:8080`. Solo usa la base `pulsepass_e2e`. No cambia la política permanente de PowerShell. El frontend puede correr en otra terminal con `cd frontend; npm.cmd run dev`.

Conservar localmente la contraseña de PostgreSQL elegida: el volumen conserva su contraseña inicial y los siguientes arranques deben usarla. Las contraseñas de fixtures deben tener al menos 12 caracteres y como máximo 72 bytes UTF-8. Si ya existen las cuentas, el seeder no modifica sus contraseñas ni roles.

No se guardan contraseñas, claves ni tokens en archivos del repositorio. `.env.example` es documental: Spring no lo carga automáticamente. También se puede configurar el entorno del proceso y ejecutar `mvnw.cmd spring-boot:run`; consultar [guía del backend](../../README_BACKEND.md).

## 2. Importar y completar el ambiente

Importar ambos archivos de `postman/` y seleccionar **PulsePass-local**. Antes de enviar:

1. Cambiar `runId`, usernames/emails de A y B, `newEventCode` y `eventToPublishCode` por valores únicos y coherentes. Los códigos de evento admiten máximo 30 caracteres.
2. Introducir las contraseñas de A/B y fixtures como valores **locales/secretos**, sin sincronizarlos ni exportarlos. La plantilla compartida mantiene todos estos valores vacíos.
3. Confirmar `baseUrl`, `expectedIssuer`, TTL y fixtures. Obtener `artistId` consultando los artistas activos; no asumir IDs de secuencia.
4. `futureEventDate` usa una fecha futura lejana válida. Para la prueba de edad, revisar `minorBirthDate` frente a la fecha real de `CMF-2026`.
5. No introducir la clave JWT del servidor en Postman.

Después de cada login, comprobar 200, `accessToken`, `tokenType=Bearer`, `expiresIn=900` (o el TTL configurado); copiar únicamente `accessToken` al valor **local** de `userAToken`, `userBToken`, `staffToken` o `adminToken`. Enviar `/api/auth/me` con ese actor para verificar identidad y roles. Al expirar, repetir login y actualizar manualmente.

## 3. Secuencia por flujos

Las carpetas son temáticas; no enviarlas ciegamente de arriba abajo: algunas acciones dependen de datos obtenidos en otra carpeta.

**A — Descubrimiento:** ejecutar conectividad, dos solicitudes de sedes, tres de artistas y cartelera. Guardar `venueActiveCode`, `artistId` y `artistStageName`. Consultar el borrador como anónimo: 404. Buscar por artista: únicamente PUBLISHED.

**B — Identidad:** registrar A/B, hacer los cuatro login y las cuatro consultas `/me`. Consultar los datos propios por email/username. Comparar contraseña incorrecta e identificador inexistente: mismo 401 genérico. Realizar los negativos de token, sin publicar tokens en evidencias.

**C — Administración:** crear evento → guardar código → consultar como ADMIN (DRAFT) → asociar artista → publicar → consultar públicamente y por artista (PUBLISHED). Antes de publicar, probar denegaciones USER/STAFF y reconsultar con ADMIN para demostrar ausencia de mutación. El borrador `E2E-DRAFT` queda reservado para negativos y no se publica.

**D — Tickets:** comprar como A y copiar `ticketCancelableCode`. Ejecutar `FLUJO-01` para otra compra y copiar `ticketUsableCode`: deben ser distintos. Leer ambos como A; consultar lista propia y pagados con STAFF. Ejecutar los IDOR con B **antes** de cancelar/usar; tras cada rechazo, releer con A y confirmar PAID. Cancelar el primero con A → GET CANCELLED → repetir cancelación 409. Usar el segundo con STAFF → GET USED → repetir uso 409. Los pagados ya no deben incluirlos.

**E — Errores y negocio:** ejecutar negativos de estructura y códigos inexistentes, duplicados, fecha pasada, sede inactiva, borrador, agotado y edad. Hacer la prueba de edad antes de llenar el aforo de CMF-2026 para que falle por edad y no por SOLD_OUT. Las pruebas de capacidad se envían individualmente contando tickets PAID hasta tres; luego ADMIN observa SOLD_OUT.

## 4. Políticas que deben verificarse

| Situación | HTTP fijo |
| --- | --- |
| Falta token, token inválido/expirado/inactivo | 401 |
| Bearer inválido presente incluso en ruta pública | 401 |
| Usuario autenticado sin rol | 403 |
| Recurso existente de otro usuario | 403 |
| Recurso inexistente | 404 |
| Evento no PUBLISHED consultado por visitante/USER | 404 |
| Evento no PUBLISHED consultado por STAFF/ADMIN | 200 |
| Estructura/enum/JSON inválidos | 400 |
| Duplicado o regla comercial | 409 |

No aceptar 403 y 404 indistintamente. La compra ajena debe responder 403 y no crear tickets. ADMIN tiene acceso explícito de soporte a usuarios y tickets y puede cancelar; STAFF solo consulta tickets y registra ingreso, con alcance global por la limitación del dominio.

La UI no calcula precios. La política existente del servicio es GENERAL 100.00, STUDENT 50.0000, VIP 200.0000 y BACKSTAGE 300.0000 (persistidos con escala 2). Los montos ilustrativos 120000/250000 de otros documentos no reemplazan el código real. Cancelar no vuelve automáticamente a publicar un evento SOLD_OUT; esa es la conducta heredada, no una nueva regla de esta entrega.

## 5. Evidencia y cierre

En cada Send revisar HTTP exacto, Content-Type, JSON y GET posterior cuando cambie estado. ErrorResponse tiene `timestamp`, `status`, `error`, `message`, `details`; ningún error debe revelar SQL, stack, hash, contraseña ni clave.

Completar [bitácora](registro-ejecucion-manual.md) con fecha/hora, esperado/observado, PASS/FAIL/BLOCKED y evidencia sin secretos. Usar BLOCKED si falta un token/fixture/paso previo; no confundirlo con PASS ni con un defecto probado. Consolidar 22/22 rutas nominales efectivamente ejecutadas usando la [matriz](matriz-cobertura.md), aparte de los negativos. La colección y la plantilla no se ejecutan ni rellenan automáticamente.

Para repetir, preferir nuevos códigos y `runId`. No borrar bases o volúmenes de otros ambientes. La limpieza destructiva de `pulsepass_e2e` debe hacerse conscientemente sobre esa base exclusiva. Detener el contenedor sin borrar datos con `docker compose -f compose.e2e.yml stop`.
