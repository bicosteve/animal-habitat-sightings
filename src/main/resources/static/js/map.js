
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
                console.log("Parsed rings for", habitat.name, rings);
            }

            const polygon = L.polygon(rings, {
                color: "#3388ff",
                weight: 2,
                fillColor: "#3388ff",
                fillOpacity: 0.25,
                pmIgnore: true
            });

            // Safely display the habitat name
            const popup = document.createElement("div");
            popup.textContent = habitat.name;
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
            if (layer instanceof L.Polygon && !habitatLayers.hasLayer(layer)) {
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
