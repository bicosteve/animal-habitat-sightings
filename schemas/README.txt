Animal Habitats and Sightings Management System — Oracle Spatial SQL
=======================================================================

Files and execution order
-------------------------
1. 01_cleanup.sql  - removes previous objects; missing-object DROP errors on
                     a first run are expected.
2. 02_schema.sql   - creates ANIMALS, HABITATS, SIGHTINGS, ordinary indexes,
                     spatial metadata, and R-tree spatial indexes.
3. 03_data.sql     - inserts demonstration rows and validates all geometries.
4. 04_queries.sql  - sample application and spatial/analytical queries.
5. 05_plsql.sql    - geometry-validation triggers for subsequent writes.

Environment assumptions
-----------------------
- Oracle Database 19c, 21c, or 23c with Oracle Spatial enabled/available.
- Run as the schema owner in SQL Developer or SQL*Plus.
- Run each script in the order above.
- 04_queries.sql contains bind variables such as :query_geom and :habitat_id.
  Supply those values in the SQL client, or adapt them in the Java application.
- Demonstration coordinates are illustrative WGS 84 longitude/latitude values
  near Brno; they are not surveyed wildlife records.
- Photographs are stored as BLOBs. Demo rows omit images so image upload can
  be implemented and tested separately.
- This first schema version associates one animal species with each habitat,
  following the supplied specification's single Animal_Name foreign key.
  If habitats need to support multiple species, add a HABITAT_ANIMALS junction
  table in a later schema revision.

Important review notes
----------------------
- The schema uses ANIMAL_ID as a stable foreign key and keeps ANIMAL_NAME UNIQUE.
  This is a normalized implementation of the specification's Animal_Name FK.
- USER_SDO_GEOM_METADATA rows are inserted before creating spatial indexes.
- WGS 84 SRID 4326 is used. For geodetic data, the tolerance value is in metres.
- The scripts have been authored against Oracle syntax and documentation, but
  they have not been executed on your campus Oracle server. Server-side
  privileges and installed Spatial components must be verified there.
