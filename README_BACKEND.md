# PulsePass — continuación REST, seguridad y aceptación manual

Se conserva la persistencia y la lógica de servicios existente. La fase REST agrega cinco controladores de negocio, validación estructural de DTO y un manejador uniforme de errores. La seguridad agrega registro atómico, login JWT, `/me`, roles y propiedad; los controladores solo delegan y aplican declaraciones de autorización.

## Ejecutar paso a paso

1. Instalar Java 21 y tener Docker Desktop activo.
2. Desde la raíz, ejecutar `powershell -ExecutionPolicy Bypass -File .\scripts\Start-PulsePassE2E.ps1`.
3. Elegir contraseñas locales cuando se soliciten: no se imprimen ni versionan. El script genera una clave de firma aleatoria si falta, arranca la base dedicada y luego la API en `http://localhost:8080`.
4. En otra terminal: `cd frontend`, `npm.cmd ci`, `npm.cmd run dev`; abrir la URL local de Vite. El proxy `/api` usa el puerto 8080 sin necesitar CORS para desarrollo.
5. Para la aceptación manual, importar los dos JSON de `tests/e2e/postman/` y seguir [la secuencia](tests/e2e/README.md).

Para una base propia, definir `DB_URL`, `DB_USER`, `DB_PASSWORD` y `PULSEPASS_JWT_SECRET_BASE64` antes de `mvnw.cmd spring-boot:run`. La clave debe ser Base64 de al menos 32 bytes aleatorios; sin clave válida el arranque falla. `PULSEPASS_JWT_ISSUER` por defecto es `pulsepass-api`, y `PULSEPASS_JWT_TTL` por defecto `PT15M`. `.env.example` contiene únicamente nombres y valores no secretos, no se carga automáticamente.

El perfil normal no inserta usuarios privilegiados ni fixtures de eventos/sedes. El perfil `e2e` verifica que la base sea `pulsepass_e2e` y provisiona las cuentas y datos descritos en [fixtures](tests/e2e/fixtures/README.md). No reutilizar ese perfil para producción.

## Contratos y arquitectura

- Controladores: `src/main/java/com/pulsepass/controller`; 20 operaciones previas más `POST /api/auth/login` y `GET /api/auth/me`. El registro se cuenta una sola vez.
- Validación: `@Valid`, Bean Validation y validador de contraseña de 12 caracteres / máximo 72 bytes UTF-8. Las fechas pasadas de eventos y las reglas de edad/capacidad/transiciones permanecen en servicios y responden 409.
- Registro: `AuthServiceImpl.register` orquesta `UserService.register`, BCrypt y rol USER bajo una transacción. Ignora cualquier rol inyectado por el cliente. El registro no emite token.
- Persistencia de seguridad: V4 crea `user_credentials` y `user_roles`; no toca V1–V3 ni asigna contraseñas a usuarios legados. No se agregan hashes a `UserResponse` ni al mapeo de `User`.
- Autenticación: Spring Security stateless, sin login de formulario, Basic ni sesión por cookie. Token solo por `Authorization: Bearer`; el filtro no acepta query como autenticación. CSRF se deshabilita únicamente porque esta API usa Bearer sin cookies automáticas.
- JWT: JJWT 0.13.0, HS256 explícito y validación de firma, emisor, sub como ID positivo, iat, exp y jti. Los roles y active se recargan desde persistencia en cada request, no desde claims de rol.
- Autorización: `AuthorizationService` compara el ID persistido con el principal; no confía en el email enviado. `SecuredEventService` aplica la visibilidad del catálogo sin cambiar sus reglas comerciales.
- Errores: MVC y filtros devuelven `ErrorResponse(timestamp,status,error,message,details)`. Un Bearer inválido presente se rechaza con 401 incluso en rutas públicas. Recurso ajeno existente:403; inexistente:404; borrador público:404.

La [matriz de 22 rutas](tests/e2e/matriz-cobertura.md) detalla permisos. STAFF tiene alcance global para consultar tickets y registrar ingreso porque el dominio no modela asignación de personal a eventos. ADMIN tiene autorización explícita de soporte sobre usuarios y tickets; comprar sigue requiriendo USER y la identidad propia. Logout del frontend elimina su copia; no revoca un JWT ya emitido. No hay refresh token ni denylist en este MVP.

## Verificar

```powershell
.\mvnw.cmd clean test
cd frontend
npm.cmd run lint
npm.cmd run build
npm.cmd test
```

Las pruebas de persistencia y seguridad usan PostgreSQL 18 mediante Testcontainers; Docker debe estar activo. Los controladores usan `@WebMvcTest`, servicios `@MockitoBean`, MockMvc y filtros activos. Se prueban JSON, validación previa a servicios, visibilidad de borradores, permisos, IDOR, claims JWT, registro BCrypt y transiciones persistidas. Los tests previos de servicios siguen presentes.

La colección Postman es un entregable **manual separado**, sin scripts ni ejecución automatizada. Su bitácora inicia NOT RUN; debe completarla un evaluador tras cada Send. No se presenta como una ejecución manual aprobada.

## Referencias utilizadas

Se siguieron los tres PRD suministrados y se consultó el patrón de `deepblue-rescue` para controladores delgados, ResponseEntity y manejo centralizado de errores. Las diferencias de dominio, DTO y seguridad pertenecen a PulsePass. Referencias técnicas: [JJWT](https://github.com/jwtk/jjwt#readme) y [WebMvcTest de Spring Boot](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/webmvc/test/autoconfigure/WebMvcTest.html).
