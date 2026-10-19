-- =====================================================================
-- PulsePass - Fase 5 - Provision de identidades privilegiadas (E2E)
-- EXCLUSIVO de pulsepass_e2e.
--
-- Precondicion: las cuentas e2e_staff y e2e_admin ya fueron registradas
-- por POST /api/users (carpeta 03 de la coleccion). El registro publico
-- solo otorga USER; los privilegios se conceden aqui, fuera de HTTP
-- (PRD Fase 4 sec. 9.2 / PRD Fase 5 sec. 5). Las contrasenas las eligio
-- quien ejecuta la prueba: este archivo no contiene ninguna.
-- Idempotente.
-- =====================================================================

DO $$
BEGIN
    IF current_database() <> 'pulsepass_e2e' THEN
        RAISE EXCEPTION 'grant-e2e-roles.sql solo puede ejecutarse sobre pulsepass_e2e (base actual: %)',
            current_database();
    END IF;
    IF (SELECT count(*) FROM users WHERE username IN ('e2e_staff', 'e2e_admin')) <> 2 THEN
        RAISE EXCEPTION 'Faltan e2e_staff o e2e_admin: registrelos primero con POST /api/users';
    END IF;
END $$;

INSERT INTO user_roles (user_id, role)
SELECT id, 'STAFF' FROM users WHERE username = 'e2e_staff'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'e2e_admin'
ON CONFLICT DO NOTHING;

-- Verificacion: roles efectivos de las cuentas E2E
SELECT u.username, string_agg(r.role, ',' ORDER BY r.role) AS roles, u.active
FROM users u JOIN user_roles r ON r.user_id = u.id
WHERE u.username LIKE 'e2e_%'
GROUP BY u.username, u.active
ORDER BY u.username;
