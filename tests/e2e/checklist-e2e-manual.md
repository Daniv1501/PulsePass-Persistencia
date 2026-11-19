# Checklist manual por caso

Seguir primero el orden por flujos del README; las carpetas son tematicas. Marcar solo despues de Send y revision visual.

## 00 Preparacion / conectividad

- [ ] PRE-001 | Comprobar API con una ruta publica existente

## 01 Venues (2 rutas)

- [ ] E2E-VEN-01 | Consultar sede activa por codigo
- [ ] E2E-VEN-02 | Listar sedes activas

## 02 Artists (3 rutas)

- [ ] E2E-ART-01 | Consultar artista por ID
- [ ] E2E-ART-02 | Consultar por nombre artistico
- [ ] E2E-ART-03 | Listar artistas activos

## 03 Auth / usuarios (registro, login, me)

- [ ] TC-AUTH-001A | Registrar USER_A
- [ ] TC-AUTH-001B | Registrar USER_B
- [ ] TC-AUTH-002-userA | Login userA
- [ ] TC-AUTH-006-userA | Verificar identidad y roles userA
- [ ] TC-AUTH-002-userB | Login userB
- [ ] TC-AUTH-006-userB | Verificar identidad y roles userB
- [ ] TC-AUTH-002-staff | Login staff
- [ ] TC-AUTH-006-staff | Verificar identidad y roles staff
- [ ] TC-AUTH-002-admin | Login admin
- [ ] TC-AUTH-006-admin | Verificar identidad y roles admin
- [ ] TC-AUTH-003 | Login por email

## 04 Usuarios / consulta y propiedad (2 rutas)

- [ ] E2E-USR-02 | Consultar propio usuario por email
- [ ] E2E-USR-03 | Consultar propio usuario por username

## 05 Events / administracion y visibilidad (6 rutas)

- [ ] E2E-EVT-01 | Crear borrador ADMIN
- [ ] E2E-EVT-02 | Leer borrador ADMIN
- [ ] E2E-EVT-03 | Cartelera publica
- [ ] E2E-EVT-04 | Publicar evento ADMIN
- [ ] E2E-EVT-05 | Asociar artista ADMIN
- [ ] E2E-EVT-06 | Eventos por artista como visitante

## 06 Tickets / compra, consulta y ciclo de vida (6 rutas)

- [ ] E2E-TKT-01 | Comprar ticket cancelable de A
- [ ] E2E-TKT-02 | Consultar ticket propio
- [ ] E2E-TKT-03 | Listar tickets propios
- [ ] E2E-TKT-04 | Listar tickets pagados STAFF
- [ ] E2E-TKT-05 | Cancelar ticket propio
- [ ] E2E-TKT-06 | Registrar ingreso con STAFF

## 07 Seguridad / escenarios negativos

- [ ] TC-AUTH-004 | Password incorrecto
- [ ] TC-AUTH-005 | Identificador inexistente
- [ ] TC-AUTH-007 | Ruta protegida sin Bearer
- [ ] TC-AUTH-008 | Bearer malformado
- [ ] TC-AUTH-009 | JWT alterado
- [ ] TC-AUTH-010 | JWT expirado
- [ ] TC-AUTH-011-key | JWT de otra clave
- [ ] TC-AUTH-011-issuer | JWT de otro emisor
- [ ] TC-AUTH-012 | Token por query no autentica
- [ ] TC-AUTH-013 | Token de usuario desactivado
- [ ] TC-AUTH-015 | Bearer invalido en ruta publica se rechaza
- [ ] TC-SEC-001-None | Crear evento sin permiso None
- [ ] TC-SEC-001-userA | Crear evento sin permiso userA
- [ ] TC-SEC-001-staff | Crear evento sin permiso staff
- [ ] TC-SEC-002-userA | Publicar sin rol ADMIN
- [ ] TC-SEC-002-staff | Publicar sin rol ADMIN
- [ ] TC-SEC-003 | Asociar artista como USER
- [ ] TC-SEC-004 | Borrador oculto a visitante
- [ ] TC-SEC-004-user | Borrador oculto a USER
- [ ] TC-SEC-005 | Busqueda publica no filtra borradores al cliente
- [ ] TC-SEC-006 | Consultar email ajeno
- [ ] TC-SEC-007 | Consultar username ajeno
- [ ] TC-SEC-009 | Leer ticket ajeno
- [ ] TC-SEC-010 | Listar tickets ajenos
- [ ] TC-SEC-008 | Compra con identidad ajena
- [ ] TC-SEC-011 | Cancelar ticket ajeno
- [ ] TC-SEC-012 | USER no puede registrar uso
- [ ] TC-SEC-013 | USER no puede listar pagados de evento
- [ ] TC-SEC-015 | ADMIN consulta borrador
- [ ] TC-SEC-016 | Registro no concede rol inyectado
- [ ] TC-SEC-017 | Revocacion de STAFF con JWT previo
- [ ] TC-SEC-ADMIN-OWN | ADMIN autorizado consulta usuario de soporte

## 08 Validaciones y errores HTTP

- [ ] VAL-001 | Evento sin campos obligatorios
- [ ] VAL-002 | Email invalido
- [ ] VAL-003 | Password corto
- [ ] VAL-004 | JSON mal formado
- [ ] VAL-005 | Categoria desconocida
- [ ] VAL-006 | Tipo de ticket desconocido
- [ ] VAL-007 | Edad minima negativa
- [ ] VAL-008 | Artista ID no numerico
- [ ] VAL-009 | Parametro obligatorio ausente
- [ ] VAL-010 | Evento inexistente
- [ ] VAL-011 | Sede inexistente
- [ ] VAL-012 | Artista inexistente
- [ ] VAL-013 | Usuario duplicado
- [ ] VAL-014 | Evento duplicado
- [ ] VAL-015 | Fecha pasada
- [ ] VAL-016 | Sede inactiva
- [ ] VAL-017 | Nacimiento futuro

## 09 Flujos de negocio / secuencia recomendada

- [ ] FLUJO-01 | Comprar ticket independiente para uso
- [ ] TC-BIZ-010-check | Confirmar cancelacion persistida
- [ ] TC-BIZ-011 | Repetir cancelacion
- [ ] TC-BIZ-012-check | Confirmar uso persistido
- [ ] TC-BIZ-013 | Repetir uso
- [ ] TC-BIZ-014-draft | No comprar borrador
- [ ] TC-BIZ-014-soldout | No comprar evento agotado
- [ ] FLUJO-MINOR-01 | Registrar persona menor de edad
- [ ] FLUJO-MINOR-02 | Login persona menor
- [ ] TC-BIZ-014-age | Rechazo por edad minima
- [ ] FLUJO-CAP-01 | Comprar hasta completar aforo 3
- [ ] FLUJO-CAP-02 | Verificar evento tras completar capacidad
- [ ] FLUJO-INACTIVE-LOGIN | Login de fixture inactivo se rechaza

