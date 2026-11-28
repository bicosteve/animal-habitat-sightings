# Plan: Sighting create form, controller, list wiring + image upload

> STATUS: APPROVED — implementing.
>
> Final UX decisions:
> - Image failure (best-effort): redirect to /sightings with a dismissible
>   warning flash ("Sighting saved, but the image could not be attached.").
> - Upload limits: 5MB max-file-size, accept="image/*" in the form.
> - List "Location" column: "lat, long" text rounded to ~5 decimals.

## Goal
Build the Sighting create flow (point geometry + optional image upload) and wire
the sightings list page to real data, consistent with the other entities.

## Backend contract (already on branch, do not change the signatures)
`sightings` table columns → `Sighting` model / `SightingRepository.insertSighting`:
- `animal_id`  NOT NULL, FK → animals        → **dropdown** (like Habitat)
- `sighted_at` NOT NULL (DB default SYSTIMESTAMP) → **datetime-local**, default now
- `notes`      VARCHAR2(1000), nullable       → **textarea**
- `geom`       SDO_GEOMETRY NOT NULL, **POINT (2001)** → map, `data-shape="point"`
- image        separate `sighting_images` (ORDImage), optional → **file input**

Existing service methods (reused):
- `addSighting(Sighting)` → returns generated `sighting_id`
- `getAllSightings()`, `getSightingDetails(id)`, `getSightingImage(id)`

## Decisions (from brainstorming)
1. sighted_at: `datetime-local` input, pre-filled with now; user may backdate.
2. Image: optional upload on the same page, single submission.
3. Image save: add a combined **best-effort** service method
   `addSighting(Sighting, byte[] imageBytes)` — always save the sighting; if an
   image is present, attempt to attach it; on image failure, the sighting still
   exists and we surface a warning (do not fail the whole request).
4. List page: fully wired — rows from a joined query, image thumbnails via an
   endpoint, enable the "+ Add Sighting" button.
5. Animal name in list: add a joined list query returning animal_name.

## Work items

### A. Model — `SightingForm` (new, in `model.forms`)
Fields + validation, mirroring HabitatForm:
```
@NotNull  Long animalId
@NotNull  LocalDateTime sightedAt            // bound from datetime-local
          String notes                        // optional (maxlength 1000 in UI)
          int sdo_gtype, int sdo_srid
@NotBlank String sdo_elem_info, sdo_ordinates // point drawn on the map
```
(Image is NOT a form field — it arrives as a separate `MultipartFile` param.)

### B. Service — `SightingService`
Add:
```
long addSighting(Sighting sighting, byte[] imageBytes) throws Exception
    // inserts sighting; if imageBytes != null && length > 0, attach image
    // (best-effort: log + rethrow-as-warning strategy TBD in controller)
List<Map<String,Object>> getAllSightingsForList() throws Exception
    // JOIN animals; returns sightingId, animalName, sightedAt, longitude,
    // latitude, hasImage (boolean)
```
Add matching repo method `getAllSightingsForList(Connection)` in SightingRepository
(JOIN animals; LEFT JOIN/EXISTS sighting_images for hasImage; extract point
coords via SDO ordinates or getPoint()).

### C. Controller — `SightingController` (new)
- `@RequestMapping("/sightings")`
- `GET /sightings`        → list (move this off HomeController)
- `GET /sightings/new`    → form (feed `animals` dropdown + empty form w/ sightedAt=now)
- `POST /sightings`       → `@Valid SightingForm` + `BindingResult`
                            + `@RequestParam(required=false) MultipartFile image`
                            parse geometry via GeometryBinder, build Sighting,
                            call combined service method, redirect on success
- `GET /sightings/{id}/image` → `ResponseEntity<byte[]>` with the stored MIME type
                               (for thumbnails + full view); 404 if no image
Remove the `/sightings` mapping from `HomeController`.

### D. Templates
- `sighting-form.html` (new): name dropdown, datetime-local, notes textarea,
  **enctype="multipart/form-data"** file input, embedded point map
  (reuse geometry-draw.js + geometry-form-init.js, data-shape="point"),
  hidden sdo_* fields, inline validation errors (same pattern as other forms).
- `sightings.html` (rewire): populate rows from the joined list, show
  `<img src="/sightings/{id}/image">` thumbnail when hasImage, enable the
  "+ Add Sighting" button → `/sightings/new`.

### E. Config
- `application.yaml`: add multipart limits, e.g.
  `spring.servlet.multipart.max-file-size: 5MB` and `max-request-size: 10MB`.

## Open questions / risks
- GeometryBinder currently targets polygons/lines too; for a point the elem_info
  is `[1,1,1]` and ordinates length 2 — the existing odd/short guards already
  allow length==2. Verify point path.
- ORDImage requires the image bytes be a real decodable image; a bogus upload
  will throw in `setProperties()`. Best-effort strategy handles this gracefully.
- `sightedAt` binding: Spring binds `datetime-local` (yyyy-MM-ddTHH:mm) to
  LocalDateTime out of the box; confirm no explicit `@DateTimeFormat` needed.

## Verification
- `mvn compile` after each milestone.
- Manual run against Oracle: create a sighting with and without an image;
  confirm list shows the row + thumbnail; confirm point renders.
```
