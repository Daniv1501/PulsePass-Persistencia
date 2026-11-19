# PRD PulsePass - Pruebas E2E Postman Manual

Transcripcion textual del PDF suministrado por el usuario. La extraccion conserva el texto; no reproduce la maquetacion del PDF. Los requisitos del documento se contrastan con el codigo real y con README.md.

PulsePass — Product Requirements Document
Plataforma de eventos, artistas y entradas
Caso de estudio académico — Fase 5: Colección Postman para pruebas end-to-end manuales de Controladores
REST y Seguridad
Campo Detalle
Documento PRD — PulsePass — Pruebas HTTP end-to-end (Postman manual)
Versión 1.1 — ejecución manual
Estado Propuesto para implementación académica
Fecha 10 de octubre de 2026
Tecnologías
objetivo
Java 21, Spring Boot 4.x, Spring Security 7.x, JJWT 0.13.x, PostgreSQL, Flyway,
Postman Desktop / Web Agent, Postman Collection v2.1
Alcance técnico Validar 20 endpoints de negocio heredados + 2 endpoints de autenticación, flujos integrados, roles, propiedad de recursos, JWT, errores y regresión
Predecesores Fase 1 Persistencia → Fase 2 Servicios → Fase 3 Controladores REST → Fase 4 Seguridad
Naturaleza del
entregable
PRD para implementar colección y guía de ejecución manual; no es un reporte de
ejecución ni asegura que la API esté funcionando
Control del documento
Campo Definición
Propósito Especificar el conjunto reproducible de pruebas de caja negra HTTP de PulsePass, ejecutable contra una aplicación real conectada a su PostgreSQL.
Audiencia Estudiantes, docente, backend, QA y revisores.
Dependencias
normativas
PRD de Persistencia; PRD de Servicios; PRD_PulsePass_Capa_Controladores_v2.md;
PRD_PulsePass_Capa_Seguridad_Spring_Security_JJWT.md.
Estrategia
Ejecutar solicitudes manualmente desde Postman → inspeccionar código HTTP, headers,
JSON y estado observable → verificar roles y propiedad → documentar evidencia en una
bitácora.
Decisión MVP Postman Collection v2.1, ejecutada manualmente desde Postman con botón Send, sin scripts de pruebas ni herramientas CLI.
Regla de
precedencia
Ante divergencias, prevalecen los contratos de las Fases 3 y 4; este PRD no autoriza
rutas ni comportamientos nuevos.
1. Resumen ejecutivo
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
1 / 26

PulsePass ya dispone de entidades y persistencia, reglas de negocio, 20 operaciones REST y una capa de
seguridad mediante Spring Security y JWT con JJWT. Esta Fase 5 debe comprobar que todas funcionan juntas
desde un cliente HTTP real: registro, autenticación, autorización, publicación, consultas, compra,
consulta/cancelación/uso de tickets y tratamiento de errores.
La solución a entregar será una colección importable en Postman, con solicitudes listas para usar, variables de
entorno, ejemplos de cuerpos, orden documentado de ejecución y una guía de comparación manual de
respuestas esperadas y observadas. Los tokens e identificadores se copiarán manualmente a variables locales
de Postman. No se utilizarán scripts en las pestañas Pre-request o Post-response/Tests, ni ejecuciones
automáticas de colecciones. Un HTTP 2xx por sí solo no implica aprobación: el evaluador debe inspeccionar su
contenido y efecto de negocio.
Principio de producto: no basta con probar que una URL responde: se debe demostrar que las 22
operaciones previstas, los tres roles y las reglas de propiedad producen resultados correctos con
datos controlados.
1.1 Evolución por fases
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
2 / 26

Fase 1: 
Persistencia
JPA + Flyway + 
PostgreSQL
Fase 2: Servicios
Reglas + DTOs
Fase 3: 
Controladores
20 endpoints 
REST
Fase 4: 
Seguridad
Spring Security 
+ JJWT
Fase 5: E2E 
HTTP
Postman manual
2. Visión y problema
2.1 Problema
Las pruebas aisladas con MockMvc, Mockito y Testcontainers no sustituyen la verificación externa del producto
desplegado. Es posible que un controlador pase sus pruebas unitarias y que la API real falle por filtros HTTP,
migraciones, roles, serialización, inconsistencias de DTO, datos iniciales o configuraciones de entorno. Se
requiere una prueba reproducible, trazable y segura de todo el flujo.
2.2 Visión
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
3 / 26

Un evaluador debe poder levantar PulsePass, provisionar datos de prueba, importar la colección y ejecutar
cada solicitud manualmente, siguiendo instrucciones de preparación, secuencia, código HTTP esperado,
evidencia que debe inspeccionar y registro de resultados.
2.3 Objetivos medibles
1. Cubrir 20/20 operaciones de negocio y 2/2 rutas de autenticación especificadas.
2. Validar por cada operación al menos un caso exitoso cuando exista una precondición realizable; en rutas
de acceso protegido, incluir casos 401/403 o política 404 anti-enumeración aplicable.
3. Cubrir roles USER, STAFF, ADMIN, visitante anónimo y usuario USER ajeno.
4. Comprobar flujos de extremo a extremo con dependencias reales en PostgreSQL, sin mocks en la
ejecución E2E.
5. Identificar y registrar manualmente respuestas incorrectas, exposición de secretos, filtración de
borradores o acceso horizontal indebido.
6. Entregar una plantilla de resultados manuales con caso, rol, datos, respuesta esperada, observada,
estado y evidencia.
7. Garantizar aislamiento/repetibilidad con fixtures deterministas o identificadores únicos y una estrategia
explícita de limpieza.
2.4 No objetivos
No implementar nuevos Controllers, Services, Repositories ni entidades; no agregar endpoints de
administración para facilitar tests; no implementar refresh tokens, revocación instantánea, pagos reales, interfaz
web, pruebas de carga, pentesting exhaustivo, OAuth2, Keycloak ni SSO. No generar tokens con claves
conocidas de producción ni inyectar roles privilegiados a través del registro público. No declarar cobertura de
rutas que no existan en los PRD previos.
3. Dependencias, supuestos y reglas de compatibilidad
Referencia Contrato heredado que se preserva
Persistencia Venue, Event, Artist, User, UserProfile, Ticket; PostgreSQL y Flyway; relaciones y restricciones de dominio.
Servicios VenueService (2), EventService (6), ArtistService (3), UserService (3), TicketService (6), reglas de capacidad, estados y propiedad.
Controladores Rutas y verbos de los 20 métodos; DTO record; ErrorResponse(timestamp, status, error, message, details); códigos HTTP.
Seguridad Registro con contraseña, POST /api/auth/login, GET /api/auth/me, Bearer JWT de 15 min, roles USER/STAFF/ADMIN, verificación de propiedad y estado activo.
Supuestos controlados — NO inventar contratos: nombres precisos de campos de VenueResponse,
EventResponse, TicketResponse, valores concretos de enums, esquema completo de creación de evento y
datos de migraciones deberán extraerse del código/DTO definitivos. Los fragmentos de ejemplo de este PRD
son plantillas, no afirmaciones de que esos valores ya estén disponibles. La colección final debe alinearse
exactamente con la implementación, sin cambiar sus contratos por conveniencia.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
4 / 26

Matices importantes: (a) 20 rutas de negocio incluyen POST /api/users; no contarlo dos veces; (b) GET
/api/auth/me se define como requisito Should en Fase 4, pero esta Fase 5 lo exige para cerrar la cobertura
de 22; si aún no se implementó, señalarlo como dependencia bloqueante, no simularlo; (c) el rol STAFF global
para operaciones de entradas es una limitación documentada del MVP, no un diseño recomendado para
producción.
4. Arquitectura de ejecución
Estudiante / 
Docente / QA
Postman: envío 
manual de 
requests
HTTP real :8080
Spring Security: 
JWT, roles, 
ownership
Controllers REST
 Services
 Repositories JPA
PostgreSQL de 
pruebas
Inspección 
visual de 
status, headers 
y JSON
Bitácora 
manual: PASS / 
FAIL / BLOCKED
Fixtures / 
Flyway
Frontera E2E: cliente Postman → servidor Spring Boot real → filtros de seguridad → controllers → services →
PostgreSQL. Se prohíbe reemplazar capas con mocks durante estas verificaciones. La preparación de datos
puede usar fixtures de prueba documentados, sin añadir endpoints al producto.
Modo de ejecución: abrir una petición, seleccionar el ambiente, configurar el token del actor cuando
corresponda, pulsar Send, inspeccionar resultado, anotarlo y continuar. No se exige Collection Runner,
Newman, Postman CLI, scripts de pruebas, JavaScript ni cURL.
5. Actores, identidades y datos de prueba
Actor Origen Expectativa
ANONYMOUS Sin Authorization Solo endpoints explícitamente públicos.
USER_A Registro público con contraseña, posteriormente login Compra y administra sus tickets; consulta su usuario.
USER_B Segundo registro público independiente No ve, modifica ni cancela recursos de USER_A.
STAFF Fixture local privilegiado gestionado fuera de HTTP Accede a consultas operativas autorizadas y marca tickets como usados.
ADMIN Fixture local privilegiado gestionado fuera de HTTP Crea y publica eventos, asocia artistas y ejecuta las acciones autorizadas.
INACTIVE_USER Fixture local de cuenta inactiva No recibe JWT o deja de poder acceder tras desactivación.
No incluir contraseñas ni tokens reales en el repositorio; credenciales de ejemplo solo en plantilla no sensible.
STAFF y ADMIN se provisionan con semilla SQL/migración de test o un proceso externo solo en el ambiente
E2E y con BCrypt; no se registran con un campo role en el cuerpo de POST /api/users.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
5 / 26

5.1 Fixtures mínimos
Fixture Requisito
Venue activo venueActiveCode Consultable y seleccionable para eventos.
Artista activo artistId,
artistStageName Recuperable por ID/nombre y asociable a eventos.
Evento borrador draftEventCode Visible por operador permitido; invisible para público.
Evento publicado publishedEventCode Fecha futura, capacidad disponible, edad mínima coherente.
Evento por publicar
eventToPublishCode Preparado para PATCH /publish.
Dos usuarios USER_A/USER_B Identidades distintas; correo, username y password controlados.
STAFF, ADMIN, inactivo Preprovisionados de modo verificable y separado del registro público.
Ticket cancelable / usable Tickets distintos para operaciones de estado irreversibles.
Se recomienda prefijar códigos/correos con identificador de ejecución (runId) y usar dominio reservado
example.test. Evitar colisiones y datos personales reales. Conservar los IDs y códigos retornados por la API;
no suponer IDs autoincrementales ni códigos generados de forma fija.
6. Requisitos funcionales de la solución de pruebas
ID Requisito Prioridad Criterio de aceptación manual
FR-E2E-
001 Colección Postman v Must Importable; contiene las 22 rutas, organizadas por módulo.
FR-E2E-
002
Entorno
parametrizable Must Base URL, datos, códigos y tokens editables desde Postman, sin editar URL de cada petición.
FR-E2E-
003
Autenticación
manual Must Registrar usuario, enviar login, copiar accessToken a la variable correspondiente.
FR-E2E-
004 Token por actor Must Variables separadas userAToken, userBToken, staffToken, adminToken.
FR-E2E-
005
Comprobación
manual Must Cada caso especifica respuesta HTTP y JSON esperado para comparar visualmente.
FR-E2E-
006
Encadenamiento
manual Must La guía indica qué códigos/IDs copiar y dónde usarlos en siguientes peticiones.
FR-E2E-
007
Matriz de
autorización Must Ejecutar los casos público, 401, 403 y propiedad según contrato.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
6 / 26

ID Requisito Prioridad Criterio de aceptación manual
FR-E2E-
008 Integridad funcional Must Reconsultar eventos/tickets tras cada transición para verificar estado persistido.
FR-E2E-
009 ErrorResponse Must Verificar visualmente sus cinco campos y ausencia de datos sensibles.
FR-E2E-
010 Datos controlados Must Fixtures y restauración manual documentados solo para base de prueba.
FR-E2E-
011
Instrucciones paso a
paso Must Para cada escenario: precondición, actor, petición, acción, esperado y comprobación.
FR-E2E-
012 Bitácora manual Must Tabla con ID, fecha, actor, HTTP esperado/real, resultado y observación.
FR-E2E-
013 Confidencialidad Must No exportar tokens o contraseñas reales ni adjuntarlos como evidencia.
FR-E2E-
014 Regresión Must Registrar si las pruebas revelan incompatibilidad con las Fases 1–4.
FR-E2E-
015 Bloqueos Must Si falla un prerrequisito, marcar casos dependientes BLOCKED, no PASS.
FR-E2E-
016 Repetibilidad Must La guía describe cómo partir de datos limpios y repetir verificaciones.
FR-E2E-
017 Contratos heredados Must Ajustar el request al código y DTO reales; no cambiar la API para acomodar la colección.
Restricción expresa: la colección no incluirá código en event, script, Pre-request Scripts ni Post-
response/Tests; los resultados serán evaluados por una persona y anotados en la bitácora.
7. Matriz completa de cobertura HTTP y seguridad
Leyenda: P público, A requiere autenticación y evaluación adicional, ADM ADMIN, OPS STAFF/ADMIN, OWN
propietario validado contra ID persistente, PUB solo información publicada; 403/404* significa denegación
según política anti-enumeración consistente. Todos los permisos que dependen del recurso se validan contra
persistencia, no por el query string.
Caso
base Método Endpoint Éxito Política a demostrar
E2E-
VEN-
01
GET /api/venues/{code} 200 P; 404 código ausente.
E2E-
VEN-
02
GET /api/venues/active 200 P; conjunto de venues activos.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
7 / 26

Caso
base Método Endpoint Éxito Política a demostrar
E2E-
EVT-
01
POST /api/events 201 ADM; visitante 401; USER 403.
E2E-
EVT-
02
GET /api/events/{eventCode} 200
PUB para visitante;
borrador solo OPS;
visitante denegado
sin filtración.
E2E-
EVT-
03
GET /api/events/published 200 P, nunca incluye borradores.
E2E-
EVT-
04
PATCH /api/events/{eventCode}/publish 200
ADM; verificar
persistencia de
estado.
E2E-
EVT-
05
POST /api/events/{eventCode}/artists/{artistId} 200
ADM; relación
verificable; asociación
repetida según regla
del Service.
E2E-
EVT-
06
GET /api/events/by-artist?stageName=... 200
Solo eventos
publicados al público;
operador autorizado
puede acceder a
otros.
E2E-
ART-
01
GET /api/artists/{id} 200 P, 404 ausente.
E2E-
ART-
02
GET /api/artists/by-stage-name?stageName=... 200 P, consulta consistente.
E2E-
ART-
03
GET /api/artists/active 200 P, solo activos.
E2E-
USR-
01
POST /api/users 201
P, asigna solo USER,
password no
expuesto; 400
inválido y 409
duplicado.
E2E-
USR-
02
GET /api/users/by-email?email=... 200
OWN o ADMIN
autorizado; usuario
ajeno 403/404*.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
8 / 26

Caso
base Método Endpoint Éxito Política a demostrar
E2E-
USR-
03
GET /api/users/by-username?username=... 200
OWN o ADMIN
autorizado; usuario
ajeno 403/404*.
E2E-
TKT-
01
POST /api/tickets 201
USER, userEmail del
principal; usuario
ajeno denegado;
reglas negocio.
E2E-
TKT-
02
GET /api/tickets/{ticketCode} 200
OWN o OPS autorizado;
usuario ajeno
403/404*.
E2E-
TKT-
03
GET /api/tickets/by-user?email=... 200
OWN o ADMIN
autorizado; usuario
ajeno 403/404*.
E2E-
TKT-
04
GET /api/events/{eventCode}/tickets/paid 200 OPS; USER 403.
E2E-
TKT-
05
PATCH /api/tickets/{ticketCode}/cancel 200
OWN (USER) o ADMIN
autorizado;
repetición/estado
inválido 409.
E2E-
TKT-
06
PATCH /api/tickets/{ticketCode}/use 200 OPS; USER 403; estado inválido 409.
E2E-
AUTH-
01
POST /api/auth/login 200
Público, credenciales
correctas → JWT;
inválidas → 401.
E2E-
AUTH-
02
GET /api/auth/me 200
A, principal actual;
ausente/invalidado →
401.
Total: 22/22 rutas previstas (20 de negocio + 2 de autenticación). No confundir cantidad de rutas con cantidad
de tests: cada ruta requiere varios escenarios según riesgos. Cualquier 404 utilizado para ocultar un recurso
ajeno debe seguir una política formal consistente; no aceptar silenciosamente cualquier 404 como sustituto de
un 403 sin justificarlo en la matriz de tests.
8. Requisitos de casos de prueba — mínimos por
categoría
8.1 HTTP y contratos (TC-HTTP-*)
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
9 / 26

Método y path exactos; query/path variables codificadas correctamente.
Content-Type: application/json en cuerpos JSON; Accept: application/json en requests
aplicables.
Verificar código de éxito exacto (200 o 201), no coincidencia genérica 2xx.
Comprobar JSON deserializable y DTO esperado, incluyendo campos mínimos definidos en el código.
Validar mensajes y códigos de error (400, 401, 403, 404, 409) contra contrato.
Probar campos obligatorios ausentes, email mal formado, JSON inválido y enum inválido donde
corresponda.
No se exponen campos password, passwordHash, secret, credenciales ni entidades JPA internas.
8.2 Autenticación / JWT (TC-AUTH-*)
ID Caso Resultado
TC-AUTH-
001 Registrar USER_A válido 201, sin JWT implícito, rol inicial USER.
TC-AUTH-
002 Login con username 200, accessToken no vacío, tokenType=Bearer, expiresIn configurado.
TC-AUTH-
003 Login con email 200, mismo usuario identificado correctamente.
TC-AUTH-
004 Login password erróneo 401 genérico, sin enumeración.
TC-AUTH-
005 Login identificador inexistente 401 genérico, sin distinguir del error de password.
TC-AUTH-
006
Consultar /api/auth/me con
token de USER_A 200, identidad A, sin credenciales.
TC-AUTH-
007 Ruta protegida sin header 401 + ErrorResponse.
TC-AUTH-
008 Bearer inventado o malformado 401 + ErrorResponse.
TC-AUTH-
009
JWT alterado (cambiar un
carácter) 401, nunca ingresa a controller.
TC-AUTH-
010 JWT expirado 401; fixture token de corta duración generado por mecanismo de test controlado.
TC-AUTH-
011
JWT firmado por otra clave /
issuer distinto
401; token construido offline con clave exclusiva de
test, nunca con secreto de app real.
TC-AUTH-
012
Token entregado por query string
en lugar de Bearer 401 en recurso protegido.
TC-AUTH-
013 Usuario inactivo con token previo 401/denegación configurada, sin acceso al recurso.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
10 / 26

ID Caso Resultado
TC-AUTH-
014
Token de USER para endpoint
ADMIN 403 + ErrorResponse.
TC-AUTH-
015
JWT entregado en endpoint
público pero inválido
No autenticar nunca; verificar política definida para
token inválido presente.
Las pruebas de JWT requieren comprobar existencia de claims obligatorios (sub, iss, iat, exp, jti) y
coherencia temporal como inspección no autoritativa desde el cliente; la verdadera validación criptográfica
corresponde al servidor. No incluir secreto privado en la colección para firmar tokens, ni comparar tokens JWT
textualmente.
8.3 Autorización / IDOR (TC-SEC-*)
ID Request Actor Esperado
TC-SEC-
001 Crear evento anónimo / USER_A 401 / 403.
TC-SEC-
002 Publicar evento USER_A / STAFF 403 / 403.
TC-SEC-
003 Asociar artista USER_A 403.
TC-SEC-
004 Consultar draft por código anónimo No retornar datos del draft; estatus definido de denegación.
TC-SEC-
005
Buscar eventos de artista con
draft anónimo Draft no incluido.
TC-SEC-
006 Consultar usuario A por email USER_B 403/404*; no información personal.
TC-SEC-
007
Consultar usuario A por
username USER_B 403/404*; no información personal.
TC-SEC-
008
Comprar ticket usando
userEmail de B USER_A Denegado, sin ticket nuevo.
TC-SEC-
009 Leer ticket de A USER_B 403/404*; no datos del ticket.
TC-SEC-
010 Listar tickets del email A USER_B 403/404*; no información.
TC-SEC-
011 Cancelar ticket de A USER_B 403/404*, ticket sin cambios.
TC-SEC-
012 Marcar uso de ticket A USER_B 403, ticket sin cambios.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
11 / 26

ID Request Actor Esperado
TC-SEC-
013 Ver tickets pagados USER_A 403.
TC-SEC-
014 Marcar uso de ticket STAFF 200 cuando la transición sea válida.
TC-SEC-
015
Administrador consulta
borrador ADMIN 200, conforme a política Fase 4.
TC-SEC-
016
Registrar usuario con
role=ADMIN inyectado anónimo Nuevo usuario no obtiene ADMIN.
TC-SEC-
017
Revocar rol de fixture
STAFF/ADMIN
usuario con JWT
antiguo
Privilegio pierde vigencia en siguiente
petición si servidor recarga roles.
Para las denegaciones en recursos mutables, volver a leerlos con un actor autorizado y verificar que el estado
no cambió. Un 403 sin prueba posterior de integridad es insuficiente para los escenarios críticos.
8.4 Negocio y transiciones (TC-BIZ-*)
ID Flujo Evidencia
TC-BIZ-
001 Consultar venues activos y venue por código Identidad y estado coinciden con fixture.
TC-BIZ-
002 Consultar artistas activos, por ID y stage name Registros esperados.
TC-BIZ-
003 Crear evento válido como ADMIN 201, eventCode disponible para GET posterior.
TC-BIZ-
004 Publicar evento Su estado cambia y aparece en /published.
TC-BIZ-
005 Añadir artista al evento Consulta por artista devuelve evento publicado.
TC-BIZ-
006 Registrar dos usuarios Identidad y unicidad verificables; duplicados 409.
TC-BIZ-
007 Comprar ticket para evento publicado 201, ticket vinculado al usuario y evento correctos.
TC-BIZ-
008 Consultar ticket por código y usuario Estado y propietario correctos.
TC-BIZ-
009 Listar tickets pagados con STAFF Incluye el ticket elegible; no otros estados.
TC-BIZ-
010 Cancelar ticket elegible por propietario 200 y nueva lectura refleja estado cancelado.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
12 / 26

ID Flujo Evidencia
TC-BIZ-
011 Cancelar un ticket otra vez 409 o regla definida en Service (sin mutación).
TC-BIZ-
012 Marcar como usado otro ticket elegible con STAFF 200, estado usado verificable.
TC-BIZ-
013 Marcar ticket usado una segunda vez 409 o regla de Service, no doble transición.
TC-BIZ-
014
Compra con evento no publicado / aforo agotado /
edad insuficiente
Error de negocio según contrato; no
ticket persistido.
TC-BIZ-
015 Datos de evento inválidos / códigos inexistentes 400/404/409 según semántica.
Importante: TicketStatus y TicketType deberán utilizar literales reales del enum implementado; PAID,
USED o CANCELLED solo deben emplearse si los DTO de PulsePass los declaran. Si el Servicio realiza compra con
otra transición, adaptar verificaciones sin falsear expectativas.
9. Flujos E2E obligatorios y orden de ejecución
9.1 Flujo A — Descubrimiento público (sin autenticación)
1. GET /api/venues/active → guardar venueActiveCode del fixture/verificarlo.
2. GET /api/venues/{code} → 200.
3. GET /api/artists/active → identificar artistId.
4. GET /api/artists/{id} y GET /api/artists/by-stage-name → 200.
5. GET /api/events/published → no expone draft.
6. GET /api/events/{eventCode} para publicado → 200; para draft → no exponer datos.
7. GET /api/events/by-artist?stageName=... → solo publicados públicamente.
9.2 Flujo B — Registro y JWT
1. Registrar USER_A y USER_B con emails distintos y passwords fuertes.
2. Verificar que el registro no admite escalación de rol, ni revela secretos.
3. Ejecutar login de A, B, STAFF y ADMIN por mecanismos previstos.
4. Guardar tokens en variables de ejecución locales, no persistirlos en ficheros compartidos.
5. GET /api/auth/me con cada token y validar identidad y privilegios efectivos.
6. Probar contraseña incorrecta, token inválido, rol insuficiente y ausencia de token.
9.3 Flujo C — Gestión del catálogo (ADMIN)
1. Crear un evento con POST /api/events usando venue activo y fecha futura.
2. Leer evento como ADMIN y comprobar que aún no aparece en listado público si está en borrador.
3. Asociar artista con POST /api/events/{eventCode}/artists/{artistId}.
4. Publicar vía PATCH /api/events/{eventCode}/publish.
5. Leer el evento sin Bearer; validar que ahora es público.
6. Consultar /api/events/published y /api/events/by-artist para comprobar propagación.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
13 / 26

7. Comprobar que USER y STAFF no pueden crear/publicar/asociar.
9.4 Flujo D — Compra, propiedad, cancelación y uso
PulsePass HTTP
PulsePass HTTP
USER_A USER_B STAFF
POST /api/tickets (email A, evento publicado)
201 ticketCode T1
GET /api/tickets/T1
200 ticket de A
GET /api/tickets/T1
403 o 404 política anti-IDOR
PATCH /api/tickets/T1/cancel
Denegado (sin mutación)
PATCH /api/tickets/T1/cancel
200 cancelado
POST /api/tickets (email A, evento publicado)
201 ticketCode T2
GET /api/events/{code}/tickets/paid
200 tickets elegibles
PATCH /api/tickets/T2/use
200 usado
PATCH /api/tickets/T2/use
403
USER_A USER_B STAFF
No reutilizar T1 para use después de cancelarlo. Las operaciones irreversibles requieren tickets separados y
precondiciones explícitas. Una denegación por ownership debe comprobarse antes de inducir una transición
de estado que haga la prueba ambigua.
9.5 Flujo E — Regresión de errores
Ejecutar validaciones 400, recursos ausentes 404, duplicados y transiciones no admitidas 409, accesos no
autenticados 401, privilegios insuficientes 403; validar contrato uniforme y ausencia de datos internos. Los 500
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
14 / 26

no son un resultado esperado; cualquier 500 en los escenarios nominales falla toda la recorrido.
10. Estructura obligatoria de entregables
pulsepass/
└── tests/
    └── e2e/
        ├── README.md                         # preparación y pasos manuales
        ├── postman/
        │   ├── PulsePass-Manual-E2E.postman_collection.json
        │   └── PulsePass-local.postman_environment.json
        ├── fixtures/
        │   ├── README.md
        │   └── seed-e2e.sql                 # opcional, exclusivamente pruebas
        ├── checklist-e2e-manual.md
        ├── registro-ejecucion-manual.md
        └── .gitignore
10.1 Carpetas de colección
PulsePass — E2E Manual
├── 00 Preparación / conectividad
├── 01 Venues (2 rutas)
├── 02 Artists (3 rutas)
├── 03 Auth / usuarios (registro, login, me)
├── 04 Usuarios / consulta y propiedad (2 rutas)
├── 05 Events / administración y visibilidad (6 rutas)
├── 06 Tickets / compra, consulta y ciclo de vida (6 rutas)
├── 07 Seguridad / escenarios negativos
├── 08 Validaciones y errores HTTP
└── 09 Flujos de negocio / secuencia recomendada
Cada solicitud debe llevar nombre claro, descripción con precondiciones, actor, pasos y resultado esperado.
Los escenarios negativos pueden duplicar una petición base con tokens/datos distintos. No crear endpoint
/health si no está definido.
10.2 Convención descriptiva de cada solicitud
ID: TC-SEC-XX
Objetivo: comprobar denegación de un USER en ruta de ADMIN
Precondiciones: USER_A registrado y con token válido
Autorización: Bearer {{userAToken}}
Ejecución: abrir request y pulsar Send
HTTP esperado: 403
Revisión manual: ErrorResponse + ningún cambio persistido
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
15 / 26

Evidencia: anotar HTTP real, resultado y observación en registro-ejecucion-
manual.md
La pestaña de scripts debe permanecer vacía.
11. Variables de entorno y política de alcance
Variable Ámbito Valor / origen
baseUrl environment http://localhost:8080 (o URL del despliegue E2E).
runId environment Identificador alfanumérico elegido manualmente antes de comenzar.
userAEmail, userAUsername environment Datos de registro A.
userBEmail, userBUsername environment Datos de registro B.
userAPassword, userBPassword secreto local Solo runtime, nunca repositorio.
adminIdentifier, staffIdentifier secreto local Identidades de fixtures privilegiados.
adminPassword, staffPassword secreto local Solo runtime.
userAToken, userBToken, adminToken,
staffToken local de Postman
Copiar manualmente desde
accessToken de cada login; nunca
versionar.
venueActiveCode, artistId,
artistStageName fixture/environment Datos obtenidos/validados.
publishedEventCode, draftEventCode,
eventToPublishCode environment Fixtures o respuestas de creación.
ticketCancelableCode,
ticketUsableCode environment Respuestas independientes de compra.
expectedIssuer env pulsepass-api (según configuración).
expectedTokenTtlSeconds env 900 (configuración MVP).
expectedForeignResourceStatus env
403 o 404, decisión explícita
documentada, no aceptar ambos de
forma indiscriminada.
Uso manual: establecer baseUrl y datos en un ambiente local; tras cada login copiar accessToken a la
variable de token del actor usando valores locales/secretos de Postman; copiar también los códigos nuevos
retornados por el servidor. No guardar valores sensibles en la colección ni exportarlos en el JSON compartido.
Antes de enviar una petición protegida verificar qué token utiliza.
12. Contratos de peticiones — ejemplos orientativos
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
16 / 26

Estos ejemplos son esqueletos para la implementación. Los request/response DTO existentes son la única
fuente válida para completar los cuerpos definitivos.
12.1 Registro (nuevo campo password en Fase 4)
POST {{baseUrl}}/api/users
Content-Type: application/json
{
  "username": "{{userAUsername}}",
  "email": "{{userAEmail}}",
  "firstName": "Andrea",
  "lastName": "Pruebas",
  "birthDate": "1998-05-15",
  "password": "{{userAPassword}}"
}
Verificar manualmente: 201; campos esperados de UserResponse; no contraseña/hash; rol efectivo no mayor
a USER; duplicación de email/username devuelve conflicto 409.
12.2 Login y captura de token
POST {{baseUrl}}/api/auth/login
Content-Type: application/json
{"identifier":"{{userAUsername}}","password":"{{userAPassword}}"}
Respuesta Fase 4:
{
  "accessToken": "<JWT_FIRMADO>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
Pasos manuales de login:
1. Seleccionar el ambiente local en Postman y enviar POST /api/auth/login con credenciales del actor.
2. Verificar visualmente 200 OK, tokenType = Bearer, accessToken no vacío y expiresIn = 900 (o TTL
configurado).
3. Copiar únicamente el valor accessToken desde el JSON de respuesta a la variable local correspondiente
(userAToken, userBToken, staffToken o adminToken).
4. Abrir GET /api/auth/me, usar el mismo token en Authorization → Bearer Token (o {{userAToken}}) y
pulsar Send.
5. Confirmar que /me muestra la identidad correcta sin secretos. Repetir para cada actor y volver a iniciar
sesión si el token expira.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
17 / 26

No usar scripts Postman ni captura automática del token.
12.3 Crear evento
POST {{baseUrl}}/api/events
Authorization: Bearer {{adminToken}}
Content-Type: application/json
{
  "eventCode": "{{newEventCode}}",
  "name": "PulsePass E2E {{runId}}",
  "description": "Evento de prueba manual",
  "category": "<EVENT_CATEGORY_REAL>",
  "eventDate": "<FECHA_FUTURA_ISO_ADMITIDA>",
  "minimumAge": 0,
  "venueCode": "{{venueActiveCode}}"
}
<EVENT_CATEGORY_REAL> y fecha exacta son marcadores documentales: la colección final debe tener valores
ejecutables admitidos, obtenidos del enum/DTO y congelados por test. No mantener marcadores <> en el
JSON final.
12.4 Compra y verificación de propiedad
POST {{baseUrl}}/api/tickets
Authorization: Bearer {{userAToken}}
Content-Type: application/json
{
  "userEmail": "{{userAEmail}}",
  "eventCode": "{{publishedEventCode}}",
  "type": "<TICKET_TYPE_REAL>"
}
En prueba negativa enviar deliberadamente userBEmail con token de A y comprobar: denegación,
ErrorResponse y ausencia de ticket creado. El código de denegación concreto debe ser el convenido por los
PRD Fase 4 y Controller; no asumir que siempre será 403 si una validación de negocio mapea consistentemente
otro código documentado.
13. Guía de verificación visual manual en Postman
13.1 Respuestas exitosas
Para cada request, inspeccionar en Postman: Status, Headers, Body y, cuando aplique, cambios observables
mediante un GET posterior. Comprobar el HTTP esperado (200/201), encabezado Content-Type:
application/json, forma JSON (objeto o arreglo según el contrato) y campos representativos del DTO real.
Para POST, guardar manualmente el código/ID retornado para usos posteriores.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
18 / 26

13.2 ErrorResponse
En una solicitud negativa comprobar manualmente que el HTTP real coincide con el esperado (400, 401, 403,
404 o 409) y que el Body tiene los campos:
{
  "timestamp": "<fecha-hora>",
  "status": 403,
  "error": "<descripción del estado>",
  "message": "<mensaje seguro>",
  "details": {}
}
El campo status debe coincidir con el HTTP. No debe contener stack traces, hashes de contraseña, tokens ni
secretos de configuración. El ejemplo anterior es ilustrativo, no la respuesta real del servidor.
13.3 Verificación de seguridad y propiedad
Comparar manualmente resultados de la misma ruta sin token, con USER_A, con USER_B y con rol autorizado;
para consultas de tickets ajenos, el resultado debe seguir la política fija 403/404 adoptada. Para mutaciones
denegadas, repetir un GET autorizado y verificar que el recurso no cambió.
13.4 Evidencia de ejecución
Registrar por caso: ID, fecha/hora, actor, método/ruta, datos principales no sensibles, HTTP esperado, HTTP
observado, validación del JSON, estado PASS/FAIL/BLOCKED, observación y evidencia opcional. No incluir JWT,
passwords ni información privada en capturas.
13.5 Criterios manuales PASS / FAIL / BLOCKED
PASS: HTTP, JSON y condición de negocio/seguridad observados coinciden con lo esperado.
FAIL: cualquiera de las comprobaciones no coincide, o existe error 500 no esperado.
BLOCKED: no se puede ejecutar por falta de fixture, actor, token o respuesta previa indispensable.
Está prohibido incluir pm.test, pm.expect, scripts en Pre-request/Post-response o cualquier evaluación
programática.
14. Procedimiento operativo para ejecución manual
1. Abrir Postman, importar la colección y el ambiente de trabajo y seleccionar este último.
2. Comprobar conectividad con una petición pública existente (GET /api/venues/active).
3. Completar manualmente las variables de fixtures: venues, artists, eventos y usuarios de prueba.
4. Ejecutar registro para USER_A y USER_B; luego los cuatro login y copiar sus tokens a variables locales.
5. Enviar las solicitudes en el orden de los Flujos A–E del capítulo 9.
6. Antes de cada petición protegida comprobar el token elegido en Authorization y su vigencia.
7. Para cada caso, inspeccionar Status, Body y, si aplica, una consulta posterior de estado.
8. Registrar PASS, FAIL o BLOCKED en la bitácora y agregar una observación.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
19 / 26

9. En operaciones irreversibles (cancelar/usar), utilizar un ticket diferente por escenario.
10. Al terminar, consolidar cobertura de 22 rutas y defectos hallados; no marcar como aprobados los casos
omitidos.
No ejecutar colección mediante Runner. Cada request se envía individualmente con Send para permitir al
estudiante comprender la respuesta.
15. Preparación del ambiente y datos de prueba
15.1 Ambiente dedicado
Base de datos dedicada (pulsepass_e2e) y aplicación configurada exclusivamente para E2E.
Aplicar migraciones Flyway existentes antes de seed; nunca reescribir historial.
Comprobar que el esquema de seguridad, usuarios privilegiados y contraseñas BCrypt estén activos.
Configurar PULSEPASS_JWT_SECRET_BASE64 en el runtime con clave aleatoria de al menos 32 bytes,
exclusiva de E2E; no incluirla en colección, scripts ni reportes.
Definir forma segura de seed de STAFF y ADMIN; verificar que el registro público solo produce USER.
Asegurar tiempo/zonas coherentes y fechas de eventos futuras; no usar una fecha que se vuelva pasada
en pocos días.
Ejecutar sobre localhost o HTTPS; no transmitir credenciales reales por HTTP público.
15.2 Idempotencia y limpieza
Antes de cada sesión manual se recomienda restaurar la base exclusiva pulsepass_e2e y aplicar las
migraciones Flyway y fixtures aprobados. Alternativamente, asignar un runId distinto y preparar nuevos
recursos. Si el docente utiliza una herramienta administrativa para restaurar datos, debe verificar explícitamente
el nombre de la base E2E. Nunca eliminar registros de desarrollo, QA o producción para repetir escenarios.
15.3 Precondiciones bloqueantes
App arranca y BD disponible.
Migraciones aplicadas y fixtures públicos consultables.
Credenciales de ADMIN/STAFF válidas.
DTOs request/response finales documentados.
GET /api/auth/me implementado conforme a Fase 4.
Tokens válidos antes de cualquier caso que los requiera.
Reportar precondición no satisfecha como BLOCKED, no como éxito ni como producto defectuoso sin
evidencia.
16. Ejecución manual, registro y revisión docente
16.1 Ejecución individual
Todas las pruebas se ejecutarán exclusivamente desde la interfaz de Postman, con envíos individuales y revisión
visual del contenido. La colección no incorporará scripts ni automatizaciones. La secuencia obligatoria queda
documentada en el capítulo 9 y en checklist-e2e-manual.md.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
20 / 26

16.2 Formato de bitácora
Caso Actor Ruta HTTP esperado HTTP observado JSON / estado observado Resultado Evidencia / observación
TC-
AUTH-
001
USER_A POST /api/auth/login 200 pendiente pendiente NOT RUN pendiente
TC-
SEC-
001
USER_A POST /api/events 403 pendiente pendiente NOT RUN pendiente
16.3 Consolidación manual
Al finalizar, contar PASS, FAIL, BLOCKED y NOT RUN por módulo y verificar que cada una de las 22 rutas tiene al
menos un caso nominal ejecutado. Las validaciones de seguridad se contabilizan aparte. Registrar defectos con
precondiciones, pasos, HTTP esperado/observado y reproducción manual.
16.4 Restricciones
No incluir Newman, Postman CLI, scripts de Postman, pipelines de ejecución de estas pruebas, reportes
generados por runners ni códigos de salida de automatización.
17. Estrategia de pruebas y métricas de calidad
Indicador Regla de revisión manual
Cobertura de rutas 22/22 rutas nominales incluidas y comprobadas individualmente.
Acceso público Rutas públicas funcionan sin token y no exponen datos restringidos.
Login y JWT Registro, login, /me, Bearer válido e inválido comprobados manualmente.
Roles USER, STAFF, ADMIN y anónimo contrastados en rutas relevantes.
Propiedad / IDOR Los datos de USER_A no son accesibles ni modificables por USER_B.
Reglas de negocio Crear/publicar, comprar/cancelar/usar y conflictos comprobados con GET posterior.
Contrato de errores ErrorResponse y códigos HTTP verificados visualmente.
Bitácora Cada caso tiene resultado explícito, incluso BLOCKED y NOT RUN.
Los casos dependientes bloqueados nunca se consideran aprobados. El docente verifica tanto cobertura como
calidad de evidencia, no solo recuentos de PASS.
18. Matriz de trazabilidad requisito → evidencia
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
21 / 26

Requisitos Evidencia manual
FR-E2E-001/002 Importación de colección, variables y 22 rutas identificables.
FR-E2E-003/004 Logins y /me con cada actor, tokens almacenados localmente.
FR-E2E-005/006 Estado HTTP/JSON e identificadores copiados entre solicitudes.
FR-E2E-007/008 Matriz de acceso y consultas de estados antes/después.
FR-E2E-009 Inspección de ErrorResponse.
FR-E2E-010/016 Instrucciones de fixtures y repetición sin colisiones.
FR-E2E-011/012 Checklist de ejecución y bitácora diligenciada.
FR-E2E-013 Archivos exportados sin tokens ni contraseñas.
FR-E2E-014/015 Defectos/regresiones registrados; casos bloqueados señalados.
FR-E2E-017 Coherencia explícita con contratos de Fase 3 y Fase 4.
19. Criterios de aceptación en formato Given / When /
Then
AC-01 — Registro y login
Given la API E2E accesible y un usuario nuevo; When se registra con datos válidos y luego hace login; Then
obtiene 201, después 200 con JWT y /api/auth/me devuelve su identidad sin exponer contraseña.
AC-02 — ADMIN publica
Given un ADMIN autenticado, venue activo y un evento en borrador; When crea/asocia/publica; Then los
endpoints responden con códigos contractuales y el evento queda visible públicamente con su artista.
AC-03 — USER sin privilegio
Given USER_A autenticado; When intenta crear o publicar evento; Then recibe 403 con ErrorResponse y
ningún recurso muta.
AC-04 — Protección de propiedad
Given un ticket de USER_A; When USER_B lo consulta o cancela; Then se deniega según política documentada
y el ticket no cambia de propietario ni de estado.
AC-05 — Compra ajena
Given USER_A; When envía userEmail de USER_B al comprar; Then la operación se rechaza, sin generar ticket
a nombre de USER_B.
AC-06 — Uso por STAFF
Given un ticket elegible; When STAFF lo marca como usado; Then la respuesta es 200, la lectura posterior
muestra transición válida y una segunda transición se rechaza según regla de Service.
AC-07 — JWT inválido
Given un recurso protegido; When se consulta con Bearer manipulado, malformado o expirado; Then retorna
401 uniforme, sin ejecución de negocio.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
22 / 26

AC-08 — Borradores
Given un evento no publicado; When el visitante consulta por código/listado/artista; Then no recibe
información del borrador; ADMIN/STAFF sí pueden consultarlo según Fase 4.
AC-09 — ErrorResponse
Given una request malformada o denegada; When se ejecuta; Then timestamp, status, error, message,
details cumplen el contrato y no revelan datos internos.
AC-10 — Verificación manual completa
Given ambiente y fixtures preparados; When el estudiante envía manualmente las 22 rutas desde Postman e
inspecciona las respuestas; Then cada caso queda marcado PASS, FAIL o BLOCKED, con HTTP
esperado/observado y evidencia sin datos sensibles.
20. Plan incremental de implementación (taller)
Etapa Tarea Resultado verificable
1.
Inventario
Contrastar 22 rutas, DTOs y matriz de seguridad con
código actual. Inventario revisado.
2. Datos Preparar PostgreSQL, Flyway, fixtures y cuentas privilegiadas de test. Ambiente listo.
3.
Colección Crear 22 requests, carpetas, descripciones y variables. Colección importable sin scripts.
4. Login Probar manualmente registro, login y /me; copiar tokens. Identidad de cuatro actores comprobada.
5. Funcional Ejecutar manualmente catálogos, eventos y tickets. Flujos A–D documentados.
6.
Seguridad
Ejecutar sin token, token inválido, rol insuficiente e
IDOR. Matriz negativa documentada.
7. Evidencia Completar bitácora con HTTP real y estado observado. Registro manual trazable.
8. Cierre Revisar 22/22 y fallos o bloqueos. DoD y acta de resultados.
21. Riesgos y mitigaciones
Riesgo Impacto Mitigación
Casos marcados PASS solo por
200 Falsa confianza Revisión humana del DTO, relación y persistencia observable.
Orden de pruebas depende de
datos previos
Confusión en
resultados
Orden manual documentado, datos preparados
y restablecimiento seguro.
Token de 15 minutos expira
durante revisión Falsos fallos Repetir login y actualizar manualmente la variable local del actor.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
23 / 26

Riesgo Impacto Mitigación
Fixtures ADMIN/STAFF
inexistentes Cobertura incompleta Seed seguro exclusivo para E2E y preflight.
Diferencias entre DTO y JSON
ejemplo Errores de 400 Inspeccionar fuente Java real; actualizar colección, no API arbitrariamente.
Prueba 403 recibe 400 por
payload inválido Diagnóstico erróneo Enviar payload válido salvo test de validación dedicado.
GET público expone drafts Filtración Casos por código y artista; negativas explícitas.
IDOR por email manipulable Exposición cruzada Dos usuarios reales y consultas/mutaciones cruzadas.
Estado de ticket irreversible Colisiones de datos Tickets distintos por caso y reseteo base.
Secretos exportados con el
ambiente o capturas Compromiso No compartir tokens; limpiar variables y ocultar secretos en evidencias.
404 permite encubrir defectos Falsos positivos Elegir explícitamente política 403 o 404 por recurso y verificar.
STAFF de alcance global Acceso excesivo para producción Registrar limitación heredada; nunca interpretarla como control por evento.
Reset apunta a BD no-E2E Pérdida de datos Validación de entorno, whitelist y prohibición de reset en otros perfiles.
22. Requisitos no funcionales (NFR)
ID Requisito Criterio
NFR-E2E-
001 Portabilidad Postman Collection v2.1 importable y utilizable con la interfaz de Postman.
NFR-E2E-
002
Seguridad de
secretos
Secretos no versionados, no impresos y no publicados como
artefactos.
NFR-E2E-
003 Determinismo Fixtures repetibles y datos propios de cada corrida.
NFR-E2E-
004 Legibilidad Carpetas por dominio, IDs rastreables, nombres de casos expresivos.
NFR-E2E-
005 Diagnóstico Bitácora con actor y escenario, sin datos sensibles.
NFR-E2E-
006 Integridad Pruebas negativas verifican ausencia de mutación persistida.
NFR-E2E-
007 Compatibilidad Respeta Fases 1–4 y Spring Security stateless.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
24 / 26

ID Requisito Criterio
NFR-E2E-
008 Eficiencia Secuencia clara para reducir pasos repetidos; revalidación manual de precondiciones.
23. Definition of Ready (DoR)
API PulsePass construye y arranca sobre PostgreSQL E2E.
Los 20 endpoints de Fase 3 coinciden con el código fuente.
Login, /auth/me, roles y políticas de Fase 4 implementados.
DTOs, enums y errores completos disponibles para parametrización.
Entorno local de Postman y estrategia de fixtures aprobados.
Datos de ADMIN/STAFF e INACTIVE_USER pueden provisionarse de forma segura.
Política 403 versus 404 para propiedad formalizada por endpoint.
24. Definition of Done (DoD)
Colección Postman v2.1 válida e importable, sin scripts de Pre-request o Post-response/Tests.
22/22 rutas base incluidas y probadas manualmente (20 heredadas + login + me).
Logins de A, B, STAFF y ADMIN; JWT copiados a variables locales sin hardcodear.
Casos 400, 401, 403, 404, 409 ejecutados cuando aplique.
Visibilidad de borradores y propiedad de usuarios/tickets verificada.
Compra ajena bloqueada y ausencia de mutación comprobada.
Estados de eventos/tickets comprobados tras las operaciones.
Estructura ErrorResponse comprobada manualmente.
Bitácora diligenciada con esperado, observado, PASS/FAIL/BLOCKED y observación.
Variables y evidencias exportables no contienen contraseñas ni tokens.
README describe preparación, secuencia, copia de variables y comparación de respuestas.
Se registran los defectos y bloqueos; no se declara aprobado lo no ejecutado.
Matriz requisito → caso → ruta → resultado disponible para revisión docente.
25. Entregables de aceptación
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
25 / 26

1. PRD_PulsePass_Pruebas_E2E_Postman_Manual.md (este documento).
2. PulsePass-Manual-E2E.postman_collection.json: solicitudes organizadas, descripción y ejemplos,
sin scripts.
3. PulsePass-local.postman_environment.json: plantilla de variables, sin valores sensibles.
4. tests/e2e/README.md: configuración y guía de ejecución solicitud por solicitud.
5. tests/e2e/checklist-e2e-manual.md: orden de rutas y escenarios.
6. tests/e2e/registro-ejecucion-manual.md: resultados rellenables con esperado/observado.
7. Matriz de cobertura de 22 rutas y roles.
26. Decisiones pendientes y reglas para resolverlas
Decisión Criterio de resolución
¿Se responde 403 o 404 en recurso ajeno? Seguir política anti-enumeración explícita acordada en Fase 4; registrar por caso.
¿Qué campos exactos devuelven los DTO? Copiar solo la definición verdadera de los record del proyecto; no deducir por nombre.
¿Cuáles son valores concretos de
EventCategory, TicketType, TicketStatus? Usar enums y reglas reales del código fuente.
¿Cómo se crean venues/artistas? Fixtures preprovisionados: la Fase 3 solo contiene GET para estos dominios.
¿Cómo se provisionan roles privilegiados? Seed/proceso administrativo de test fuera de registro público; contraseña BCrypt.
¿Cómo forzar expiración/revocación? Ambiente y fixtures controlados para prueba; jamás incluir clave real ni modificar API solo por la recorrido.
¿Debe /api/auth/me ser obligatorio? Sí para entrega Fase 5; si falta, registrar dependencia e implementarlo conforme al PRD de Seguridad.
¿Cómo registrar los resultados? Bitácora Markdown diligenciada manualmente tras cada Send.
Resultado esperado
Al completar la Fase 5, un estudiante podrá recorrer manualmente todo PulsePass desde Postman —registro,
JWT, publicación, compra, cancelación y uso de tickets—, detectar y documentar errores de contrato HTTP,
negocio, permisos y propiedad. La colección y la bitácora servirán como criterio de aceptación manual del
backend.
PRD_PulsePass_Pruebas_E2E_Postman_Manual.md 2026-10-11
26 / 26