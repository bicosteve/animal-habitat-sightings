
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

    // Convert Leaflet geometry into Oracle SDO format
    function convertToSdo(layer) {

        // POINT
        if (layer instanceof L.Marker) {
            const point = layer.getLatLng();

            return {
                sdo_gtype: 2001,
                sdo_srid: 4326,
                sdo_elem_info: null,
                sdo_ordinates: [point.lng, point.lat]
            };
        }

        // POLYGON
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

    function saveGeometries() {
        const geometries = [];

        map.eachLayer(layer => {
            if (layer instanceof L.Polygon) {
                try {
                    const geometry = convertToSdo(layer);

                    geometries.push({
                        id: L.Util.stamp(layer),
                        ...geometry
                    });

                } catch (error) {
                    console.error("Geometry conversion failed:", error);
                }
            }
        });

        sessionStorage.setItem(
            "drawnGeometries",
            JSON.stringify(geometries)
        );

        console.log("Saved polygons:", geometries);
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
