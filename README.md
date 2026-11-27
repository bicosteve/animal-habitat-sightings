# Animal Habitat Sightings

A web application for recording and spatially analysing wildlife data in a
nature park. It lets you manage animals, the habitats and biomes they live in,
the checkpoints and trails that make up the park, and the individual sightings
of animals in the field — and then run spatial analysis (area, perimeter, and
distance) over all of it using **Oracle Spatial**.

The project is built as a study project for the Faculty of Information
Technology, Brno University of Technology (BUT FIT), and is designed to run
against an Oracle database with the Spatial (SDO) option enabled.

---

## Table of contents

- [What the app does](#what-the-app-does)
- [Data model](#data-model)
- [Spatial analysis](#spatial-analysis)
- [Tech stack and tools](#tech-stack-and-tools)
- [Project structure](#project-structure)
- [How to run it](#how-to-run-it)
- [Routes overview](#routes-overview)
- [Future improvements](#future-improvements)

---

## What the app does

The application is organised around six domain entities. Each has its own
browsable list, and most support full create / view / edit / delete.

| Entity | Geometry | What it represents |
|---|---|---|
| **Animal** | — | A species or animal recorded in the park (name, description). |
| **Habitat** | Polygon | An area where a given animal lives. Linked to one animal. |
| **Biome** | Polygon | A larger ecological region (e.g. forest, wetland). |
| **Sighting** | Point | A single observation of an animal at a location and time, optionally with a photo. |
| **Checkpoint** | Point | A fixed point of interest (e.g. a viewing building or gate). |
| **Wildlife trail** | Line | A path or route through the park. |

Key capabilities:

- **A dashboard home page** that lists the animals, sightings, and habitats
  added so far, with quick-action buttons to add each, and links to the
  analysis tools.
- **Interactive geometry drawing.** Habitats and biomes are drawn as polygons,
  trails as lines, checkpoints and sightings as points, directly on a
  **Leaflet** map using the **Leaflet-Geoman** drawing controls. The drawn
  shape is converted to an Oracle `SDO_GEOMETRY` and stored in the database.
- **Sighting photos.** A sighting can carry an image, stored in the database
  and served back on the sighting detail page.
- **Spatial analysis** over the stored geometries (see below).

All geometries use **SRID 4326** (WGS84 geographic latitude/longitude), so
spatial measurements are computed geodetically in real-world metres.

---

## Data model

The schema lives in [`schemas/01_schema.sql`](schemas/01_schema.sql). The core
tables are:

- `animals` — `animal_id`, `animal_name`, `description`.
- `biomes` — `biome_id`, `biome_name`, `description`, `geom` (polygon).
- `habitats` — `habitat_id`, `habitat_name`, `animal_id` (FK → animals),
  `geom` (polygon), `created_at`.
- `sightings` — `sighting_id`, `animal_id` (FK → animals), `sighted_at`,
  `notes`, `geom` (point).
- `sighting_images` — image bytes linked to a sighting.
- `checkpoints` — `checkpoint_id`, `checkpoint_name`, `checkpoint_type`,
  `description`, `geom` (point), `created_at`.
- `wildlife_trails` — `trail_id`, `trail_name`, `description`, `geom` (line),
  `created_at`.

Each spatial table has a **`BEFORE INSERT OR UPDATE` trigger** that enforces its
geometry type and SRID, so invalid geometries are rejected at the database
level:

- Habitats and biomes must be 2D polygons (`SDO_GTYPE = 2003`).
- Sightings and checkpoints must be 2D points (`SDO_GTYPE = 2001`).
- Wildlife trails must be 2D lines (`SDO_GTYPE = 2002`).
- All geometries must use `SDO_SRID = 4326`.

Supporting SQL scripts:

- [`schemas/02_data.sql`](schemas/02_data.sql) — a small demonstration dataset.
- [`schemas/03_queries.sql`](schemas/03_queries.sql) — demonstration and
  verification queries.

---

## Spatial analysis

The **Analysis** page (`/analysis`) performs all spatial calculations **in the
database**, using Oracle Spatial's `SDO_GEOM` package. Java only converts units
and formats the results for display.

- **Area & perimeter** — for every habitat and biome polygon, computed with
  `SDO_GEOM.SDO_AREA` (m²) and `SDO_GEOM.SDO_LENGTH` (m). Shown both as a table
  and as labelled polygons on a map.
- **Distance between features** — pick any two features (habitat, biome,
  checkpoint, or trail) and get the shortest edge-to-edge distance between them,
  computed with `SDO_GEOM.SDO_DISTANCE`. Both features are drawn on the map with
  a connecting line between their representative points
  (`SDO_GEOM.SDO_CENTROID` / `SDO_GEOM.SDO_POINTONSURFACE`), labelled
  *"Distance from A to B is X km"*.

Because geometries use SRID 4326, the `'unit=M'` / `'unit=SQ_M'` arguments make
Oracle return results in geodetic metres rather than naïve planar units.

---

## Tech stack and tools

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 (Spring MVC, Spring JDBC, Validation) |
| Views | Thymeleaf server-side templates |
| Build | Maven (with the bundled `mvnw` wrapper) |
| Database | Oracle Database with the **Spatial (SDO)** option |
| JDBC / Spatial | Oracle JDBC (`ojdbc`) + Oracle Spatial `sdoapi` (`JGeometry`) |
| Boilerplate | Lombok |
| Config | `spring-dotenv` (reads credentials from a `.env` file) |
| Front-end map | Leaflet + Leaflet-Geoman (drawing), Bootstrap 5 (layout) |

Notes on the design:

- The app does **not** use Spring's auto-configured datasource. The
  `DataSourceAutoConfiguration` is explicitly excluded in
  `application.yaml`; instead, `AnimalHabitatSightingsApplication.getConnection()`
  opens a plain `OracleDataSource` connection per call, and repositories receive
  a `Connection`. This keeps the Oracle Spatial `JGeometry` round-trips explicit.
- Geometries are converted between the browser and the database with the Oracle
  Spatial `JGeometry` API (`storeJS` / `loadJS`) and a small front-end helper
  (`geometry-draw.js`) that builds `SDO_GEOMETRY` ordinate arrays from the
  shapes drawn on the map.

---

## Project structure

```
animal-habitat-sightings/
├── pom.xml                       # Maven build + dependencies
├── mvnw, mvnw.cmd                # Maven wrapper
├── .env-example                  # Template for the required DB credentials
├── schemas/
│   ├── 01_schema.sql             # Tables, triggers, spatial metadata
│   ├── 02_data.sql               # Demonstration data
│   └── 03_queries.sql            # Demonstration / verification queries
└── src/main/
    ├── java/cz/fit/mit/Animal_Habitat_Sightings/
    │   ├── AnimalHabitatSightingsApplication.java   # Entry point + DB connection
    │   ├── controller/           # Web controllers (one per entity + Analysis, Home)
    │   ├── service/              # Business logic; opens one DB connection per call
    │   ├── repository/          # SQL + JGeometry conversion
    │   └── model/               # Domain + form objects
    └── resources/
        ├── application.yaml      # Spring + Oracle datasource config
        ├── templates/           # Thymeleaf pages (lists, forms, detail, analysis)
        └── static/js/           # Leaflet map + geometry drawing helpers
```

---

## How to run it

### Prerequisites

- **Java 21** (JDK).
- An **Oracle Database** with the **Spatial (SDO)** option, and credentials you
  can connect with (for BUT FIT students, your faculty Oracle account).
- No local Maven install is required — use the bundled `mvnw` wrapper.

### 1. Create the database schema

Connect to your Oracle database (e.g. with SQL Developer or SQL*Plus) and run,
in order:

```sql
@schemas/01_schema.sql   -- tables, triggers, spatial metadata
@schemas/02_data.sql     -- optional: demonstration data
```

### 2. Configure credentials

Copy the example environment file and fill in your own Oracle connection
details:

```bash
cp .env-example .env
```

Then edit `.env`:

```
DB_URL=jdbc:oracle:thin:@//your-oracle-host:1521/your-service-name
DB_USERNAME=your-username
DB_PASSWORD=your-password
```

The `.env` file is git-ignored and must **not** be committed. Its values are
read into `application.yaml` via `spring-dotenv`.

### 3. Run the application

```bash
./mvnw spring-boot:run
```

On startup the app prints `Oracle database connection successful!` if it can
reach the database. Then open:

```
http://localhost:8080/
```

(The server port is configured in `application.yaml` and defaults to `8080`.)

### Building a runnable JAR

```bash
./mvnw clean package
java -jar target/Animal-Habitat-Sightings-0.0.1-SNAPSHOT.jar
```

---

## Routes overview

| Area | Route | Purpose |
|---|---|---|
| Home | `GET /` | Dashboard: lists animals, sightings, habitats |
| Analysis | `GET /analysis` | Area/perimeter + distance analysis page |
| Analysis | `POST /analysis/distance` | Compute distance between two features |
| Animals | `GET /animals`, `/animals/new`, `POST /animals`, `GET /animals/{id}/edit`, `POST /animals/{id}`, `POST /animals/{id}/delete` | Manage animals |
| Habitats | `GET /habitats`, `/habitats/new`, `POST /habitats`, `GET /habitats/{id}/details`, `GET /habitats/map` | Manage habitats |
| Biomes | `GET /biomes`, `/biomes/new`, `POST /biomes` | Manage biomes |
| Sightings | `GET /sightings`, `/sightings/new`, `POST /sightings`, `GET /sightings/{id}`, `GET /sightings/{id}/image`, `POST /sightings/{id}/delete` | Manage sightings + photos |
| Checkpoints | `GET /checkpoints`, `/checkpoints/new`, `POST /checkpoints`, `GET /checkpoints/{id}`, `GET /checkpoints/{id}/edit`, `POST /checkpoints/{id}`, `POST /checkpoints/{id}/delete` | Manage checkpoints |
| Trails | `GET /trails`, `/trails/new`, `POST /trails`, `GET /trails/{id}`, `GET /trails/{id}/edit`, `POST /trails/{id}`, `POST /trails/{id}/delete` | Manage wildlife trails |

---

## Future improvements

The following are known gaps and natural next steps rather than committed work:

- **Complete the CRUD coverage.** Coverage is uneven across entities:
  - *Checkpoints, trails, and animals* have the full create / view (or edit) /
    edit / delete flow.
  - *Biomes* currently support only list and create.
  - *Habitats* support list, create, and a detail view, but not edit/delete.
  - *Sightings* support list, create, detail, and delete, but not edit.

  Bringing every entity up to the same create/view/edit/delete standard would
  make the app consistent.
- **Restore the standalone map.** After the home-page redesign, `/habitats/map`
  still renders the (now map-less) dashboard view and should be pointed at a
  dedicated map page again.
- **Spatial relationship queries in the UI.** The repositories already contain
  useful spatial queries that aren't yet surfaced — e.g. trails intersecting a
  habitat, sightings near a trail, biomes intersecting a habitat, and sightings
  within a habitat. These could become their own analysis views.
- **Closest-point distance lines.** The distance line currently connects
  representative points; drawing the true closest points (via
  `SDO_CLOSEST_POINTS`) would make the on-screen line match the measured number.
- **Connection pooling.** The app opens a new `OracleDataSource` connection per
  call. A proper connection pool (e.g. HikariCP or Oracle UCP) would improve
  performance and robustness under load.
- **Authentication and authorisation.** There is currently no login; adding
  Spring Security would allow per-user data and protect the mutating endpoints.
- **Automated tests.** The project is validated by compilation and manual
  testing against a live Oracle instance. Repository/controller tests (e.g.
  against a test schema or a spatial-capable test container) would catch
  regressions earlier.
- **Input and UX polish.** Richer validation messages, pagination for large
  lists, and sortable/filterable tables.
