-- =====================================================================
-- PulsePass - Fase 5 - Cambio de estado: revocar el rol STAFF de e2e_staff
-- EXCLUSIVO de pulsepass_e2e. Se ejecuta en el bloque de cierre
-- (TC-SEC-017). El token {{staffToken}} emitido antes sigue siendo un JWT
-- valido, pero el servidor recarga los roles en cada peticion.
-- Para restaurar el rol, vuelva a ejecutar grant-e2e-roles.sql.
-- =====================================================================

DO $$
BEGIN
    IF current_database() <> 'pulsepass_e2e' THEN
        RAISE EXCEPTION 'Este script solo puede ejecutarse sobre pulsepass_e2e (base actual: %)',
            current_database();
    END IF;
END $$;

DELETE FROM user_roles
WHERE role = 'STAFF'
  AND user_id = (SELECT id FROM users WHERE username = 'e2e_staff');

SELECT u.username, string_agg(r.role, ',' ORDER BY r.role) AS roles
FROM users u JOIN user_roles r ON r.user_id = u.id
WHERE u.username = 'e2e_staff'
GROUP BY u.username;
