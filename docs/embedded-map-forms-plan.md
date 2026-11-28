# Plan: Embedded-Map Create Forms

> STATUS: IMPLEMENTED for Animal, Habitat, Biome, Checkpoint, Trail.
> Sighting deferred (backend lives on `main`). `mvn compile` passes.
> Not yet run against a live Oracle DB.


Branch: `feat/controllers`
Goal: Replace the fragile "draw on dashboard -> sessionStorage -> separate form" flow
with self-contained create forms that embed their own map. Add Animal management so
the Animal foreign key can be satisfied via a dropdown instead of a raw ID.

## Decisions (agreed)

1. Each Add form embeds its own Leaflet + Geoman map; draw + fill + save on one page.
2. Add an Animal create form; replace raw "Animal ID" input with a dropdown of animals.
3. Entities are independent (no forced habitat-before-sighting order). DB only requires
   an Animal to exist before a Habitat or Sighting (FK `fk_habitats_animal`,
   `fk_sightings_animal`).
4. Keep improving the existing dashboard; no new welcome page.
5. Sighting form is DEFERRED (its repository/service/form live only on `main`).

## Scope of THIS change

Build the embedded-map + dropdown pattern for: **Animal, Habitat, Biome, Checkpoint, Trail.**
Sighting is out of scope until we reconcile with `main`.

## Key facts discovered

- `map.js` already has a correct `convertToSdo(layer)` for point (2001), line (2002),
  polygon (2003), including polygon ring-orientation handling. This logic is REUSED.
- `map.js` `saveGeometries()` has a bug: it only stores polygons (loops
  `if (layer instanceof L.Polygon)`), so points/lines were never persisted to
  sessionStorage. Embedding the map in the form removes this dependency entirely.
- Spring (verified): a single comma-joined string does NOT bind to `int[]`/`double[]`.
  Forms therefore keep `sdo_elem_info`/`sdo_ordinates` as `String`, and controllers
  split-and-parse (same pattern as the existing `HabitatController`).

## The shared JS contract

New file: `static/js/geometry-draw.js`

Exposes a single initializer invoked per form. Reads config from the loading
`<script data-*>` tag:

- `data-map-id`        : id of the map `<div>` (default "map")
- `data-shape`         : one of `point` | `line` | `polygon`
- optional `data-center-lat` / `data-center-lng` / `data-zoom`

Behaviour:

1. Build a Leaflet map on the given div, OSM basemap, centered on Brno by default.
2. Enable ONLY the relevant Geoman draw tool for `data-shape`
   (drawMarker | drawPolyline | drawPolygon). Enable edit/drag/remove.
3. Enforce a single active geometry: drawing a new one replaces the previous.
4. On create/edit/remove, call `convertToSdo(layer)` and write into hidden fields:
   `#sdo_gtype`, `#sdo_srid`, `#sdo_elem_info` (comma-joined), `#sdo_ordinates`
   (comma-joined). Clear them on remove.
5. Update `#geometryStatus` text; disable submit until a valid geometry exists.

`convertToSdo` is factored out of `map.js` into `geometry-draw.js` and reused by
both the dashboard and the forms (single source of truth).

## Files

### New Java
- `controller/AnimalController.java`  (GET list, GET /new, POST)
  (AnimalService + AnimalRepository already exist; insertAnimal already fixed.)

### New templates
- `animal-form.html`, `animals.html`

### New static JS
- `static/js/geometry-draw.js`  (shared map+draw+SDO, replaces per-form scripts)

### Modified templates (embed map, drop sessionStorage script)
- `habitat-form.html`   : add map div; Animal ID -> Animal dropdown; use geometry-draw.js
- `biome-form.html`     : add map div; use geometry-draw.js
- `checkpoint-form.html`: add map div; use geometry-draw.js
- `trail-form.html`     : add map div; use geometry-draw.js
- `fragments/navbar.html`: add "Animals" link

### Modified Java (dropdown data)
- `HabitatController`: add `@ModelAttribute("animals")` feed (getAllAnimals) so the
  form can render the dropdown. Controller already splits sdo_* strings.

### Modified JS
- `static/js/map.js`: import/reuse the shared `convertToSdo`; fix `saveGeometries`
  to handle all shape types (or leave dashboard display-only — see Open Question).

### Possibly removed
- `static/js/habitat-form.js` and `static/js/geometry-form.js` become obsolete once
  forms use `geometry-draw.js`. Remove after the forms are switched over.

## Build / verification

- `mvn compile` after Java changes.
- Manual reasoning for JS (no DB available here); cannot run the live app (needs Oracle).
- Re-verify Spring binding assumption already done; keep String sdo_* fields.

## Open questions for review

1. Dashboard role: should the dashboard map stay (display existing data) and the drawing
   move entirely into forms? (Proposed: yes — dashboard becomes display/overview; all
   drawing happens in forms.)
2. Animal dropdown empty state: if no animals exist yet, the habitat/sighting form
   dropdown is empty. Add an inline "No animals yet - add one first" link to the
   Animal form? (Proposed: yes.)
3. Should `animal-form.html` also carry a map? No — animals have no geometry. Plain form.

## Implementation order

1. `geometry-draw.js` (the shared contract) + factor `convertToSdo` out of `map.js`.
2. Animal: controller + `animals.html` + `animal-form.html` + navbar link.
3. Rework `habitat-form.html` (map embed + animal dropdown) + HabitatController feed.
4. Rework biome / checkpoint / trail forms to embed the map.
5. Clean up obsolete per-form scripts.
6. `mvn compile`; review.
