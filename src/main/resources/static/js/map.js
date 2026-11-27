
// map.js — dashboard overview map.
//
// Geometry conversion (convertToSdo) lives in geometry-draw.js and is reused here.
// The dashboard lets the user draw/inspect geometries; drawn shapes are stored in
// sessionStorage for any legacy consumers. Primary creation now happens in the
// self-contained create forms (each embeds its own map).

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

    function saveGeometries() {
        const geometries = [];

        map.eachLayer(layer => {
            // Handle every drawable geometry type, not just polygons.
            const isDrawable =
                layer instanceof L.Marker ||
                layer instanceof L.Polyline; // Polygon extends Polyline

            if (!isDrawable) {
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
});
