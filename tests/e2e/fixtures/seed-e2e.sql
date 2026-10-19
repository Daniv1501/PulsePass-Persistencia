-- =====================================================================
-- PulsePass - Fase 5 - Fixtures del catalogo para pruebas E2E manuales
-- EXCLUSIVO de la base pulsepass_e2e. Ejecutar DESPUES de que Flyway
-- haya aplicado V1-V4 (es decir, despues de arrancar la API una vez).
-- Idempotente: se puede ejecutar varias veces sin duplicar datos.
-- No crea usuarios ni contrasenas (ver grant-e2e-roles.sql).
-- =====================================================================

DO $$
BEGIN
    IF current_database() <> 'pulsepass_e2e' THEN
        RAISE EXCEPTION 'seed-e2e.sql solo puede ejecutarse sobre pulsepass_e2e (base actual: %)',
            current_database();
    END IF;
END $$;

-- ---------- Venues ----------
-- VEN-E2E-01  : activo, capacidad amplia (eventos principales)
-- VEN-E2E-MINI: activo, capacidad 1 (escenario de aforo agotado)
-- VEN-E2E-OFF : inactivo (no debe aparecer en /api/venues/active ni admitir eventos)
INSERT INTO venues (code, name, city, address, capacity, active) VALUES
    ('VEN-E2E-01',   'E2E Arena Principal', 'Santa Marta', 'Calle Prueba 1', 500, TRUE),
    ('VEN-E2E-MINI', 'E2E Sala Mini',       'Santa Marta', 'Calle Prueba 2', 1,   TRUE),
    ('VEN-E2E-OFF',  'E2E Sala Cerrada',    'Santa Marta', 'Calle Prueba 3', 100, FALSE)
ON CONFLICT (code) DO NOTHING;

-- ---------- Eventos ----------
-- Fechas relativas a NOW() para que nunca queden en el pasado.
-- E2E-PUB  : PUBLISHED, edad minima 18 (compras, propiedad, cancelacion, uso)
-- E2E-DRAFT: DRAFT, nunca debe ser visible para el publico
-- E2E-MINI : PUBLISHED en un venue de capacidad 1 (aforo agotado)
INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id)
SELECT 'E2E-PUB', 'E2E Festival Publicado', 'Evento publicado de prueba', 'MUSIC', 'PUBLISHED',
       date_trunc('minute', NOW()) + INTERVAL '180 days', 18, v.id
FROM venues v WHERE v.code = 'VEN-E2E-01'
ON CONFLICT (event_code) DO NOTHING;

INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id)
SELECT 'E2E-DRAFT', 'E2E Borrador Secreto', 'Borrador: no debe filtrarse', 'MUSIC', 'DRAFT',
       date_trunc('minute', NOW()) + INTERVAL '200 days', 0, v.id
FROM venues v WHERE v.code = 'VEN-E2E-01'
ON CONFLICT (event_code) DO NOTHING;

INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id)
SELECT 'E2E-MINI', 'E2E Concierto Intimo', 'Aforo de una persona', 'CULTURE', 'PUBLISHED',
       date_trunc('minute', NOW()) + INTERVAL '150 days', 0, v.id
FROM venues v WHERE v.code = 'VEN-E2E-MINI'
ON CONFLICT (event_code) DO NOTHING;

-- ---------- Artistas ----------
-- 'Solar Beat' viene de la migracion V2. Se asocia al publicado y al borrador
-- para comprobar que /api/events/by-artist no filtra el borrador al publico.
INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e, artists a
WHERE e.event_code IN ('E2E-PUB', 'E2E-DRAFT')
  AND a.stage_name = 'Solar Beat'
ON CONFLICT DO NOTHING;

-- Verificacion rapida (debe listar los tres eventos)
SELECT event_code, status, minimum_age, event_date FROM events
WHERE event_code LIKE 'E2E-%' ORDER BY event_code;
