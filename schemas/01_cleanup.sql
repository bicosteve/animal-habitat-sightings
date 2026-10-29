-- =============================================================================
-- 01_cleanup.sql
-- Animal Habitats and Sightings Management System
-- Purpose: remove objects from a previous run of this project.
-- Run as the schema owner in SQL Developer or any compatible SQL GUI Tool.
-- Missing-object DROP errors are expected on the first run.
-- =============================================================================

SET DEFINE OFF;

-- Drop spatial indexes before deleting their metadata or base tables.
DROP INDEX idx_habitats_geom;
DROP INDEX idx_sightings_geom;

-- Drop ordinary indexes explicitly, as requested by the project script standard.
DROP INDEX idx_habitats_animal_id;
DROP INDEX idx_sightings_animal_id;
DROP INDEX idx_sightings_sighted_at;

-- Remove spatial metadata for the two geometry layers.
-- USER_SDO_GEOM_METADATA contains metadata owned by the current schema.
DELETE FROM user_sdo_geom_metadata
WHERE table_name IN ('HABITATS', 'SIGHTINGS');

COMMIT;

-- Drop dependent tables before the referenced ANIMALS table.
DROP TABLE sightings CASCADE CONSTRAINTS PURGE;
DROP TABLE habitats CASCADE CONSTRAINTS PURGE;
DROP TABLE animals CASCADE CONSTRAINTS PURGE;

-- End of cleanup. DROP errors for objects that do not exist are expected.
