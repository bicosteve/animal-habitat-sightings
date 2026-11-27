
// map.js — dashboard overview map.
//
// Two responsibilities:
//   1. Render the existing habitats (passed from Spring as the `habitats`
//      global) as polygons with tooltips + click-to-load details.
//   2. Let the user draw/inspect geometries; drawn shapes are stored in
//      sessionStorage for any legacy consumers.
//
// Geometry conversion (convertToSdo) lives in geometry-draw.js and is reused
// here, so index.html must load geometry-draw.js before map.js. Primary
// geometry creation now happens in the self-contained create forms (each
// embeds its own map).

document.addEventListener("DOMContentLoaded", () => {
    sessionStorage.removeItem("drawnGeometries");

    // Initialize map centered on Brno
    window.map = L.map("map", {
        doubleClickZoom: false
    }).setView(
        [49.1951, 16.6068],
        12
    );

    // OpenStreetMap basemap
    L.tileLayer(
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
        {
            maxZoom: 19,
            attribution: "&copy; OpenStreetMap contributors"
        }
    ).addTo(map);

    // ---- Existing habitats layer ------------------------------------------

    const habitatLayers = L.featureGroup().addTo(map);

    function displayHabitats() {
        habitatLayers.clearLayers();

        habitats.forEach(habitat => {
            const geometry = habitat.geometry;

            if (!geometry || geometry.gtype !== 2003) {
                return;
            }

            const elemInfo = geometry.elemInfo;
            const ordinates = geometry.ordinates;

            if (!elemInfo || !ordinates) {
                return;
            }

            const rings = [];

            // Read each polygon ring from SDO_ELEM_INFO
            for (let i = 0; i < elemInfo.length; i += 3) {
                const start = elemInfo[i] - 1;
                const end = i + 3 < elemInfo.length
                    ? elemInfo[i + 3] - 1
                    : ordinates.length;

                const ring = [];

                for (let j = start; j < end; j += 2) {
                    ring.push([
                        ordinates[j + 1],
                        ordinates[j]
                    ]);
                }

                rings.push(ring);
            }

            const polygon = L.polygon(rings, {
                color: "#3388ff",
                weight: 2,
                fillColor: "#3388ff",
                fillOpacity: 0.25,
                pmIgnore: true
            });

            polygon.bindTooltip(habitat.name, {
                permanent: true,
                direction: "center",
                className: "habitat-label"
            });

            polygon.on("click", () => {
                fetch(`/habitats/${habitat.id}/details`)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error("Failed to load habitat details");
                        }
                        return response.json();
                    })
                    .then(data => {
                        displayHabitatDetails(data);
                    })
                    .catch(error => console.error(error));
            });

            habitatLayers.addLayer(polygon);
        });

        if (habitatLayers.getLayers().length > 0) {
            map.fitBounds(habitatLayers.getBounds());
        }
    }

    function displayHabitatDetails(data) {
        const panel = document.getElementById("detailsPanel");

        panel.innerHTML = `
            <div class="card-header">
                <strong>${data.habitatName}</strong>
            </div>
            <div class="card-body">
                <p><strong>Habitat ID:</strong> ${data.habitatId}</p>
                <p><strong>Animal:</strong> ${data.animalName}</p>
                <p><strong>Area:</strong> ${Number(data.area).toFixed(2)} m²</p>
                <p><strong>Perimeter:</strong> ${Number(data.perimeter).toFixed(2)} m</p>
                <p><strong>Created:</strong> ${data.createdAt ?? "Unknown"}</p>
                <p><strong>Centroid:</strong> ${
                    data.centroidLongitude != null
                        ? `${Number(data.centroidLongitude).toFixed(6)}, ${Number(data.centroidLatitude).toFixed(6)}`
                        : "Unavailable"
                }</p>
            </div>
        `;
    }

    // ---- User-drawn geometries --------------------------------------------
    // convertToSdo is provided by geometry-draw.js (loaded before this file).

    function saveGeometries() {
        const geometries = [];

        map.eachLayer(layer => {
            // Handle every drawable geometry type, not just polygons, and skip
            // the read-only habitat polygons rendered above.
            const isDrawable =
                layer instanceof L.Marker ||
                layer instanceof L.Polyline; // Polygon extends Polyline

            if (!isDrawable || habitatLayers.hasLayer(layer)) {
                return;
            }

            try {
                const geometry = convertToSdo(layer);

                geometries.push({
                    id: L.Util.stamp(layer),
                    ...geometry
                });
            } catch (error) {
                console.error("Geometry conversion failed:", error);
            }
        });

        sessionStorage.setItem(
            "drawnGeometries",
            JSON.stringify(geometries)
        );

        console.log("Saved geometries:", geometries);
    }

    // Enable Leaflet-Geoman drawing controls
    map.pm.addControls({
        position: "topleft",
        drawMarker: true,
        drawPolyline: true,
        drawPolygon: true,
        drawRectangle: false,
        drawCircle: false,
        drawCircleMarker: false,
        editMode: true,
        dragMode: true,
        removalMode: true,
        cutPolygon: true
    });

    // Handle newly created geometry
    map.on("pm:create", event => {
        const layer = event.layer;

        saveGeometries();

        // Update stored geometry when edited
        layer.on("pm:edit", saveGeometries);
    });

    // Handle removed geometries
    map.on("pm:remove", event => {
        saveGeometries();
    });

    map.on("pm:cut", saveGeometries);

    displayHabitats();
});
