# Validación de la continuación — 16 de noviembre de 2026

## Implementación y referencias

Se implementaron los PRD suministrados de Controladores v2, Seguridad Spring Security/JJWT y Postman E2E Manual. Se consultaron los controladores y el manejador de excepciones de `deepblue-rescue` como referencia local; la autorización y los contratos pertenecen al dominio PulsePass.

| Alcance | Evidencia |
| --- | --- |
| 20 operaciones de negocio + login + me | Seis controladores y matriz `tests/e2e/matriz-cobertura.md` |
| Validación antes de delegar | Bean Validation, contraseña por bytes UTF-8 y pruebas que verifican ausencia de llamadas al servicio |
| Errores uniformes | `GlobalExceptionHandler`, `SecurityErrorHandler`; pruebas 400/401/403/404/409/500 |
| Registro atómico | `AuthServiceImpl`, BCrypt y USER; prueba de rollback de usuario, credencial y roles |
| Seguridad del token | HS256, issuer, exp, iat, sub y jti; tests criptográficos y HTTP para expiración/firma alterada |
| Roles y usuarios inactivos | Consulta de identidad persistida en cada Bearer; denegaciones de rol y usuario desactivado |
| Propiedad | Comparación de ID persistente; prueba de compra ajena con cero tickets adicionales y cancelación ajena sin mutación |
| Visibilidad del catálogo | Borradores 404 al público y filtrado servidor por artista; STAFF/ADMIN autorizados |
| Migración | V4 aditiva probada contra PostgreSQL 18; V1–V3 sin cambios |
| Fixtures dedicadas | Perfil e2e restringido a `pulsepass_e2e`; pruebas de provisión e idempotencia |
| Frontend | Contratos alineados, lint/build y 60 pruebas aprobadas |
| Artefacto Postman | 93 solicitudes, diez carpetas, 22 rutas nominales; validado contra esquema oficial v2.1; sin scripts y secretos exportados vacíos |

## Pruebas automatizadas

Suite de backend: **222 pruebas, cero fallos, cero errores y cero omitidas**, incluyendo tres clases de integración con PostgreSQL real mediante Testcontainers. Suite frontend: **60 pruebas aprobadas**; ESLint y compilación de producción aprobados. La compilación limpia del backend se comprueba con el comando exigido por el taller:

```powershell
.\mvnw.cmd clean test
cd frontend
npm.cmd run lint
npm.cmd run build
npm.cmd test
```

Los resultados detallados de Java se regeneran en `target/surefire-reports/`. Los secretos de tests son ficticios y no se usan en runtime. No se deshabilitan los filtros de seguridad en MockMvc ni se sustituye PostgreSQL por H2.

## Aceptación manual pendiente

Las **93 solicitudes Postman siguen NOT RUN** hasta enviarlas una por una desde la aplicación Postman y revisar las respuestas visualmente. El formato JSON y la cobertura están verificados, pero eso no constituye ejecución manual. Completar la bitácora con los HTTP observados y la evidencia; no atribuir resultados de los tests Java a Postman.

La entrega inicial del backend no incluyó auditoría visual, Lighthouse ni axe. La revisión posterior del frontend incorpora esos informes en [CUMPLIMIENTO_FRONTEND.md](../frontend/docs/CUMPLIMIENTO_FRONTEND.md). No se implementan pagos, QR, refresh tokens ni despliegue productivo. Se conservan los precios y transiciones de negocio anteriores; STAFF global y ausencia de revocación instantánea por jti son límites documentados del MVP.
