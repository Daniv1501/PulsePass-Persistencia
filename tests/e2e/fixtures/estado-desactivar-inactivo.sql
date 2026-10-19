-- =====================================================================
-- PulsePass - Fase 5 - Cambio de estado: desactivar la cuenta e2e_inactive
-- EXCLUSIVO de pulsepass_e2e. Se ejecuta en el bloque de cierre
-- (TC-AUTH-013), DESPUES de haber copiado su token en {{inactiveToken}}.
-- =====================================================================

DO $$
BEGIN
    IF current_database() <> 'pulsepass_e2e' THEN
        RAISE EXCEPTION 'Este script solo puede ejecutarse sobre pulsepass_e2e (base actual: %)',
            current_database();
    END IF;
END $$;

UPDATE users SET active = FALSE WHERE username = 'e2e_inactive';

SELECT username, active FROM users WHERE username = 'e2e_inactive';
