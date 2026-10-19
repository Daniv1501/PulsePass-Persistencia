# Fixtures E2E

Datos controlados para la base **`pulsepass_e2e`**. Todos los scripts comprueban `current_database()` y se niegan a correr sobre cualquier otra base (no pueden tocar datos de desarrollo).

| Script | Cuándo | Qué hace |
|---|---|---|
| `seed-e2e.sql` | Tras el primer arranque de la API (Flyway ya aplicó V1–V4), antes de abrir Postman | Crea el catálogo de prueba. Idempotente. |
| `grant-e2e-roles.sql` | Después de registrar `e2e_staff` y `e2e_admin` por `POST /api/users` (paso SQL-1 del checklist) | Concede `STAFF` y `ADMIN`. Falla con mensaje claro si las cuentas aún no existen. Idempotente. |
| `estado-desactivar-inactivo.sql` | Bloque de cierre (SQL-2) | `e2e_inactive.active = false` (TC-AUTH-013). |
| `estado-revocar-staff.sql` | Bloque de cierre (SQL-3) | Quita `STAFF` a `e2e_staff` (TC-SEC-017). Para restaurarlo, vuelva a ejecutar `grant-e2e-roles.sql`. |

## Datos que crea `seed-e2e.sql`

| Tipo | Código | Estado | Para qué |
|---|---|---|---|
| Venue | `VEN-E2E-01` | activo, capacidad 500 | Venue principal (`venueActiveCode`) |
| Venue | `VEN-E2E-MINI` | activo, capacidad 1 | Aforo agotado |
| Venue | `VEN-E2E-OFF` | **inactivo** | No aparece en `/api/venues/active`; no admite eventos (`venueInactiveCode`) |
| Evento | `E2E-PUB` | PUBLISHED, edad mínima 18, artista Solar Beat | Compras, propiedad, cancelación y uso (`publishedEventCode`) |
| Evento | `E2E-DRAFT` | DRAFT, artista Solar Beat | Nunca debe ser visible al público (`draftEventCode`) |
| Evento | `E2E-MINI` | PUBLISHED en `VEN-E2E-MINI` | Pasa a SOLD_OUT con la primera compra (`soldOutEventCode`) |

Las fechas de los eventos son relativas a `NOW()` (150–200 días en el futuro), así que nunca quedan en el pasado. El artista **Solar Beat** lo crea la migración V2; su `id` no se supone, se lee en el caso E2E-ART-03.

## Por qué no hay usuarios ni contraseñas aquí

El PRD prohíbe contraseñas en el repositorio y prohíbe conceder roles privilegiados por el registro público. Por eso:

1. Las cuentas `e2e_staff`, `e2e_admin` y `e2e_inactive` se registran por HTTP (`POST /api/users`) con contraseñas que **usted elige** en el ambiente de Postman (variables de tipo *secret*, vacías en el archivo versionado). El servidor las guarda con BCrypt.
2. El registro solo otorga `USER`. Los privilegios se conceden después con `grant-e2e-roles.sql`, fuera de HTTP.

Si cambia `staffIdentifier`, `adminIdentifier` o `inactiveIdentifier` en el ambiente, cambie también los nombres en los scripts SQL.

## Cómo ejecutar un script

```powershell
# PowerShell, desde la raíz del repositorio (base en Docker, ver tests/e2e/README.md)
Get-Content tests\e2e\fixtures\seed-e2e.sql | docker exec -i pulsepass-e2e-db psql -U postgres -d pulsepass_e2e -v ON_ERROR_STOP=1
```

```bash
# Git Bash / Linux / macOS
docker exec -i pulsepass-e2e-db psql -U postgres -d pulsepass_e2e -v ON_ERROR_STOP=1 < tests/e2e/fixtures/seed-e2e.sql
```

Con pgAdmin: abra el Query Tool **conectado a `pulsepass_e2e`**, pegue el contenido del script y ejecútelo.
