-- =============================================================================
-- 04_queries.sql
-- Demonstration and verification queries for the application.
-- Bind variables are shown where a geometry or ID should be supplied by Java.
-- =============================================================================

-- 1. List habitats with their associated animal and geometry.
SELECT h.habitat_id, h.habitat_name, a.animal_name, h.geom
FROM habitats h
JOIN animals a ON a.animal_id = h.animal_id
ORDER BY h.habitat_name;

-- 2. Sightings with animal details and optional image metadata.
SELECT s.sighting_id, s.sighted_at, a.animal_name,
       a.scientific_name, s.notes, s.image_file_name, s.image_mime_type,
       s.geom
FROM sightings s
JOIN animals a ON a.animal_id = s.animal_id
ORDER BY s.sighted_at DESC;

-- 3. SDO_FILTER: primary filter for candidates whose spatial index bounding
--    rectangles interact with the query geometry. This is not the exact
--    topological test. Bind :query_geom as an SDO_GEOMETRY.
SELECT h.habitat_id, h.habitat_name
FROM habitats h
WHERE SDO_FILTER(h.geom, :query_geom) = 'TRUE';

-- 4. SDO_RELATE: exact spatial relationship test using ANYINTERACT.
--    Returns sightings that interact spatially with the selected habitat.
SELECT s.sighting_id, a.animal_name, s.sighted_at, s.notes
FROM sightings s
JOIN animals a ON a.animal_id = s.animal_id
JOIN habitats h ON SDO_RELATE(s.geom, h.geom, 'mask=ANYINTERACT') = 'TRUE'
WHERE h.habitat_id = :habitat_id
ORDER BY s.sighted_at DESC;

-- 5. SDO_NN: find the five nearest sightings to a supplied point.
--    :query_geom must be an SDO_GEOMETRY point in SRID 4326.
SELECT s.sighting_id,
       a.animal_name,
       s.sighted_at,
       SDO_NN_DISTANCE(1) AS distance_value
FROM sightings s
JOIN animals a ON a.animal_id = s.animal_id
WHERE SDO_NN(
    s.geom,
    :query_geom,
    'sdo_num_res=5',
    1
) = 'TRUE'
ORDER BY distance_value;

-- 6. SDO_WITHIN_DISTANCE: sightings within 500 metres of a supplied point.
SELECT s.sighting_id, a.animal_name, s.sighted_at
FROM sightings s
JOIN animals a ON a.animal_id = s.animal_id
WHERE SDO_WITHIN_DISTANCE(
    s.geom,
    :query_geom,
    'distance=500 unit=M'
) = 'TRUE'
ORDER BY s.sighted_at DESC;

-- 7. Count sightings per animal, including animals with no sightings.
SELECT a.animal_id,
       a.animal_name,
       COUNT(s.sighting_id) AS sighting_count
FROM animals a
LEFT JOIN sightings s ON s.animal_id = a.animal_id
GROUP BY a.animal_id, a.animal_name
ORDER BY sighting_count DESC, a.animal_name;

-- 8. Rank animals by number of sightings using a window function.
--    Aggregation is performed first; RANK then ranks the grouped results.
SELECT animal_id,
       animal_name,
       sighting_count,
       RANK() OVER (ORDER BY sighting_count DESC) AS sighting_rank,
       DENSE_RANK() OVER (ORDER BY sighting_count DESC) AS dense_sighting_rank
FROM (
    SELECT a.animal_id,
           a.animal_name,
           COUNT(s.sighting_id) AS sighting_count
    FROM animals a
    LEFT JOIN sightings s ON s.animal_id = a.animal_id
    GROUP BY a.animal_id, a.animal_name
)
ORDER BY sighting_rank, animal_name;

-- 9. Count sightings that fall inside each habitat.
--    SDO_RELATE uses the spatial index and tests the specified relationship.
SELECT h.habitat_id,
       h.habitat_name,
       COUNT(s.sighting_id) AS sightings_interacting
FROM habitats h
LEFT JOIN sightings s
    ON SDO_RELATE(s.geom, h.geom, 'mask=ANYINTERACT') = 'TRUE'
GROUP BY h.habitat_id, h.habitat_name
ORDER BY sightings_interacting DESC, h.habitat_name;

-- 10. Check spatial index state. For domain indexes, DOMIDX_OPSTATUS is
--     important in addition to STATUS.
SELECT index_name, table_name, status, domidx_status, domidx_opstatus
FROM user_indexes
WHERE index_name IN ('IDX_HABITATS_GEOM', 'IDX_SIGHTINGS_GEOM')
ORDER BY index_name;
