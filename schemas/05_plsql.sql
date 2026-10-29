-- =============================================================================
-- 05_plsql.sql
-- Basic integrity triggers for direct SQL and application writes.
-- Spatial geometry types are checked here because they are application-level
-- rules: HABITATS must contain 2D polygons (2003), SIGHTINGS 2D points (2001).
-- Both layers use SRID 4326 and require valid geometries.
-- =============================================================================

CREATE OR REPLACE TRIGGER trg_habitats_validate_geom
BEFORE INSERT OR UPDATE OF geom ON habitats
FOR EACH ROW
DECLARE
    v_result VARCHAR2(2000);
BEGIN
    IF :NEW.geom IS NULL THEN
        RAISE_APPLICATION_ERROR(-20011, 'Habitat geometry cannot be NULL.');
    END IF;

    IF :NEW.geom.SDO_GTYPE <> 2003 THEN
        RAISE_APPLICATION_ERROR(-20012, 'Habitat geometry must be a 2D polygon (SDO_GTYPE 2003).');
    END IF;

    IF :NEW.geom.SDO_SRID <> 4326 THEN
        RAISE_APPLICATION_ERROR(-20013, 'Habitat geometry must use SRID 4326.');
    END IF;

    v_result := SDO_GEOM.VALIDATE_GEOMETRY_WITH_CONTEXT(:NEW.geom, 0.05);

    IF v_result <> 'TRUE' THEN
        RAISE_APPLICATION_ERROR(-20014, 'Invalid habitat geometry: ' || v_result);
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_sightings_validate_geom
BEFORE INSERT OR UPDATE OF geom ON sightings
FOR EACH ROW
DECLARE
    v_result VARCHAR2(2000);
BEGIN
    IF :NEW.geom IS NULL THEN
        RAISE_APPLICATION_ERROR(-20021, 'Sighting geometry cannot be NULL.');
    END IF;

    IF :NEW.geom.SDO_GTYPE <> 2001 THEN
        RAISE_APPLICATION_ERROR(-20022, 'Sighting geometry must be a 2D point (SDO_GTYPE 2001).');
    END IF;

    IF :NEW.geom.SDO_SRID <> 4326 THEN
        RAISE_APPLICATION_ERROR(-20023, 'Sighting geometry must use SRID 4326.');
    END IF;

    v_result := SDO_GEOM.VALIDATE_GEOMETRY_WITH_CONTEXT(:NEW.geom, 0.05);

    IF v_result <> 'TRUE' THEN
        RAISE_APPLICATION_ERROR(-20024, 'Invalid sighting geometry: ' || v_result);
    END IF;
END;
/

-- Confirm that the triggers are valid after creation.
SELECT trigger_name, status, table_name
FROM user_triggers
WHERE trigger_name IN ('TRG_HABITATS_VALIDATE_GEOM', 'TRG_SIGHTINGS_VALIDATE_GEOM')
ORDER BY trigger_name;
