
// geometry-draw.js
// -----------------------------------------------------------------------------
// Shared geometry utilities + an embedded-map drawing helper for create forms.
//
// Exposes two globals (no module bundler is used in this project):
//
//   window.convertToSdo(layer)
//       Convert a Leaflet layer (Marker / Polyline / Polygon) into Oracle SDO
//       components: { sdo_gtype, sdo_srid, sdo_elem_info, sdo_ordinates }.
//
//   window.initGeometryForm()
//       Read configuration from the loading <script data-*> tag, build a Leaflet
//       map with Geoman drawing restricted to a single shape type, and write the
//       drawn geometry's SDO values into the form's hidden fields.
//
// Hidden fields expected in the form (by id):
//   #sdo_gtype  #sdo_srid  #sdo_elem_info  #sdo_ordinates
// Optional status element: #geometryStatus
// Optional submit button is auto-detected from the enclosing <form>.
// -----------------------------------------------------------------------------

(function () {
    "use strict";

    // ---- Geometry conversion (single source of truth) -----------------------

    function convertToSdo(layer) {

        // POINT
        if (layer instanceof L.Marker) {
            const point = layer.getLatLng();

            return {
                sdo_gtype: 2001,
                sdo_srid: 4326,
                sdo_elem_info: [1, 1, 1],
                sdo_ordinates: [point.lng, point.lat]
            };
        }

        // POLYGON (checked before Polyline: L.Polygon extends L.Polyline)
        if (layer instanceof L.Polygon) {
            const rings = layer.getLatLngs();

            const ordinates = [];
            const elemInfo = [];

            rings.forEach((ring, index) => {
                const points = [...ring];

                if (points.length < 3) {
                    throw new Error("A polygon ring needs at least 3 points.");
                }

                const first = points[0];
                const last = points[points.length - 1];

                if (first.lat !== last.lat || first.lng !== last.lng) {
                    points.push(first);
                }

                let area = 0;

                for (let i = 0; i < points.length - 1; i++) {
                    area +=
                        points[i].lng * points[i + 1].lat -
                        points[i + 1].lng * points[i].lat;
                }

                const shouldBeCounterClockwise = index === 0;
                const isCounterClockwise = area > 0;

                if (shouldBeCounterClockwise !== isCounterClockwise) {
                    points.reverse();
                }

                const offset = ordinates.length + 1;
                const elementType = index === 0 ? 1003 : 2003;

                elemInfo.push(offset, elementType, 1);

                points.forEach(point => {
                    ordinates.push(point.lng, point.lat);
                });
            });

            return {
                sdo_gtype: 2003,
                sdo_srid: 4326,
                sdo_elem_info: elemInfo,
                sdo_ordinates: ordinates
            };
        }

        // LINE
        if (layer instanceof L.Polyline) {
            const points = layer.getLatLngs();

            if (points.length < 2) {
                throw new Error("A line needs at least 2 points.");
            }

            const ordinates = [];

            points.forEach(point => {
                ordinates.push(point.lng, point.lat);
            });

            return {
                sdo_gtype: 2002,
                sdo_srid: 4326,
                sdo_elem_info: [1, 2, 1],
                sdo_ordinates: ordinates
            };
        }

        throw new Error("Unsupported Leaflet geometry type.");
    }

    // ---- Embedded-map form helper -------------------------------------------

    function initGeometryForm(options) {
        const config = options || {};

        const mapId = config.mapId || "map";
        const shape = (config.shape || "polygon").toLowerCase();

        const centerLat = typeof config.centerLat === "number" ? config.centerLat : 49.1951;
        const centerLng = typeof config.centerLng === "number" ? config.centerLng : 16.6068;
        const zoom = typeof config.zoom === "number" ? config.zoom : 12;

        const mapEl = document.getElementById(mapId);
        if (!mapEl) {
            console.error(`geometry-draw: map element #${mapId} not found.`);
            return;
        }

        const status = document.getElementById("geometryStatus");
        const gtypeEl = document.getElementById("sdo_gtype");
        const sridEl = document.getElementById("sdo_srid");
        const elemEl = document.getElementById("sdo_elem_info");
        const ordEl = document.getElementById("sdo_ordinates");
        const submitButton = mapEl.closest("form")
            ? mapEl.closest("form").querySelector('button[type="submit"]')
            : null;

        const map = L.map(mapId, { doubleClickZoom: false })
            .setView([centerLat, centerLng], zoom);

        L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19,
            attribution: "&copy; OpenStreetMap contributors"
        }).addTo(map);

        // Enable only the draw tool matching the required shape.
        map.pm.addControls({
            position: "topleft",
            drawMarker: shape === "point",
            drawPolyline: shape === "line",
            drawPolygon: shape === "polygon",
            drawRectangle: false,
            drawCircle: false,
            drawCircleMarker: false,
            drawText: false,
            editMode: true,
            dragMode: true,
            removalMode: true,
            cutPolygon: false
        });

        let currentLayer = null;

        function setStatus(message, type) {
            if (!status) return;
            status.textContent = message;
            status.classList.remove("alert-info", "alert-success", "alert-danger");
            status.classList.add(type);
        }

        function clearFields() {
            if (gtypeEl) gtypeEl.value = "";
            if (sridEl) sridEl.value = "";
            if (elemEl) elemEl.value = "";
            if (ordEl) ordEl.value = "";
            if (submitButton) submitButton.disabled = true;
        }

        function writeFields(layer) {
            try {
                const sdo = convertToSdo(layer);

                if (gtypeEl) gtypeEl.value = sdo.sdo_gtype;
                if (sridEl) sridEl.value = sdo.sdo_srid;
                if (elemEl) elemEl.value = sdo.sdo_elem_info.join(",");
                if (ordEl) ordEl.value = sdo.sdo_ordinates.join(",");

                if (submitButton) submitButton.disabled = false;
                setStatus("Geometry captured. You can edit it or save.", "alert-success");
            } catch (error) {
                console.error("Geometry conversion failed:", error);
                clearFields();
                setStatus(error.message || "Invalid geometry.", "alert-danger");
            }
        }

        // Start disabled until a geometry is drawn.
        clearFields();
        setStatus("Draw the geometry on the map to continue.", "alert-info");

        // On an edit form we seed the map with the existing geometry so the
        // user sees it and can reshape/redraw it. Point seeds (checkpoints,
        // sightings) and line seeds (trails) are supported. A seeded layer
        // behaves exactly like a freshly drawn one: it writes the hidden fields
        // and updates them when edited/dragged.
        function attachSeed(layer, view) {
            currentLayer = layer;
            if (layer.pm && typeof layer.pm.enable === "function") {
                layer.pm.enable();
            }
            if (view) {
                map.setView(view, zoom);
            }
            writeFields(layer);
            layer.on("pm:edit", () => writeFields(layer));
            layer.on("dragend", () => writeFields(layer));
        }

        const pointSeed = config.initialPoint;
        if (shape === "point" && pointSeed
                && typeof pointSeed.lat === "number" && typeof pointSeed.lng === "number") {

            const marker = L.marker([pointSeed.lat, pointSeed.lng], {
                draggable: true,
                pmIgnore: false
            }).addTo(map);
            attachSeed(marker, [pointSeed.lat, pointSeed.lng]);
        }

        const lineSeed = config.initialLine;
        if (shape === "line" && Array.isArray(lineSeed) && lineSeed.length >= 2) {

            const polyline = L.polyline(lineSeed, { pmIgnore: false }).addTo(map);
            attachSeed(polyline, null);
            // Fit the map to the whole line rather than a single zoom level.
            try {
                map.fitBounds(polyline.getBounds(), { padding: [20, 20] });
            } catch (ignored) {
                // Keep the default view if bounds can't be computed.
            }
        }

        map.on("pm:create", event => {
            // Enforce a single geometry: drop any previous one.
            if (currentLayer) {
                map.removeLayer(currentLayer);
            }
            currentLayer = event.layer;

            writeFields(currentLayer);

            currentLayer.on("pm:edit", () => writeFields(currentLayer));
        });

        map.on("pm:remove", event => {
            if (event.layer === currentLayer) {
                currentLayer = null;
                clearFields();
                setStatus("Geometry removed. Draw a new one.", "alert-info");
            }
        });
    }

    window.convertToSdo = convertToSdo;
    window.initGeometryForm = initGeometryForm;
})();
