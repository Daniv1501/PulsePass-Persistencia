# Fixtures exclusivos de E2E

El seeder `E2eFixtureSeeder` solo funciona con perfil `e2e` y URL PostgreSQL cuya base sea exactamente `pulsepass_e2e`. Flyway V1–V4 corre antes. No se agregan rutas administrativas de fixtures ni se modifica un historial Flyway aplicado.

| Fixture | Valor / comportamiento |
| --- | --- |
| ADMIN | `e2e-admin`, contraseña tomada de `PULSEPASS_E2E_ADMIN_PASSWORD` |
| STAFF | `e2e-staff`, contraseña tomada de `PULSEPASS_E2E_STAFF_PASSWORD` |
| Inactivo | `e2e-inactive`, contraseña tomada de `PULSEPASS_E2E_INACTIVE_PASSWORD`; active=false |
| USER A/B | Registro público manual con identificadores de cada ejecución |
| Sede aforo 3 | `VEN-SMR-01`, Marina Convention Center, Santa Marta |
| Sede amplia | `VEN-E2E-LARGE`, aforo 100 |
| Sede inactiva | `VEN-E2E-INACTIVE` |
| Artistas | Catálogo V2; consultar ID de Solar Beat por API |
| Publicado, sin edad mínima | `E2E-PUBLISHED`, sede amplia |
| Publicado, edad mínima 18 | `CMF-2026`, sede aforo 3 |
| Borrador | `E2E-DRAFT` |
| Agotado | `E2E-SOLD-OUT` |
| Fechas | Al crear fixtures: seis meses después del arranque; no se sobrescriben al reiniciar |

La semilla es idempotente: no cambia registros existentes, ni restablece contraseñas/roles/estados. Si se han consumido fixtures, usar nuevos recursos o una base E2E nueva de forma controlada. El aforo agotado es un fixture explícito de estado, no tres tickets preinsertados.

## Inactividad con token anterior

Desde una herramienta SQL administrativa conectada exclusivamente a `pulsepass_e2e`, comprobar primero `SELECT current_database();`. Activar temporalmente `e2e-inactive` con un UPDATE por username, hacer login desde Postman y copiar el token local. Volver a desactivarlo por username y enviar TC-AUTH-013 con el token previo: 401. Restaurar active=false al terminar. No se incorpora una ruta HTTP de activación.

## Revocación de rol

En esa misma base, con un ticket PAID independiente y el token STAFF ya emitido, eliminar **solo** la fila de `user_roles` correspondiente a `e2e-staff` y rol STAFF. La siguiente operación privilegiada debe dar 403 aunque el token no haya expirado. Reconsultar el ticket como propietario: sigue PAID. Restaurar la fila `(user_id, 'STAFF')` al terminar. No ejecutar esta prueba sobre cuentas reales ni retirar roles al único administrador.

## Tokens negativos

Preparar localmente `malformedToken` con texto inventado; `tamperedToken` cambiando un carácter de la firma de una copia; `expiredToken` conservando un token hasta después de su expiración. Para clave distinta, un JWT firmado con una clave aleatoria offline de pruebas debe fallar. Para emisor distinto aislado de la firma, usar un arranque E2E controlado con emisor temporal y la misma clave E2E, emitir token, volver al emisor esperado y comprobar rechazo. Nunca pegar claves de producción ni claves del servidor en Postman. Los tests Java cubren criptográficamente estos casos; las solicitudes manuales siguen NOT RUN hasta su Send real.

## Activación segura de usuarios legados

V4 no inventa una contraseña para usuarios preexistentes. Un administrador debe verificar la identidad del titular por un canal externo, obtener un hash BCrypt de una contraseña nueva con el mismo `PasswordEncoder`, y asociarlo en una transacción a un `user_id` verificado que aún no tenga credencial. Insertar la credencial y rol USER; usar parámetros SQL vinculados y nunca concatenar passwords ni imprimir el hash. No modificar credenciales existentes, no usar una contraseña compartida y no conceder roles privilegiados como parte de esta activación. La interfaz pública no ofrece este procedimiento ni recuperación de contraseña.
