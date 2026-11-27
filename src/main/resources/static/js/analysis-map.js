// analysis-map.js — renders the analysis results on a Leaflet map.
//
// Inputs (set as globals by analysis.html before this file loads):
//   analysisPolygons: [{ type, name, area, perimeter, geometry, centroid }]
//       every habitat/biome polygon, drawn with an area/perimeter label.
//   distanceData: { distanceKm, fromName, toName, from, to } where from/to are
//       { type, id, name, geometry, point }; when a distance was calculated the
//       two features are drawn plus a line between their representative points,
//       labelled "Distance from {fromName} to {toName} is {X} km".
//
// Geometry objects use Oracle's gtype/srid/elemInfo/ordinates shape, where
// ordinates are flat [lng, lat, lng, lat, ...] pairs.

document.addEventListener("DOMContentLoaded", () => {

    const map = L.map("analysisMap").setView([49.1951, 16.6068], 12);

    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
        maxZoom: 19,
        attribution: "&copy; OpenStreetMap contributors"
    }).addTo(map);

    const allLayers = L.featureGroup().addTo(map);

    // --- geometry helpers --------------------------------------------------

    // Read polygon rings (gtype 2003) into Leaflet [lat,lng] arrays.
    function polygonRings(geometry) {
        const elemInfo = geometry.elemInfo;
        const ordinates = geometry.ordinates;
        if (!elemInfo || !ordinates) {
            return null;
        }

        const rings = [];
        for (let i = 0; i < elemInfo.length; i += 3) {
            const start = elemInfo[i] - 1;
            const end = i + 3 < elemInfo.length ? elemInfo[i + 3] - 1 : ordinates.length;

            const ring = [];
            for (let j = start; j < end; j += 2) {
                ring.push([ordinates[j + 1], ordinates[j]]);
            }
            rings.push(ring);
        }
        return rings;
    }

    // Read a line (gtype 2002) into a Leaflet [lat,lng] array.
    function lineLatLngs(geometry) {
        const ordinates = geometry.ordinates;
        if (!ordinates) {
            return null;
        }
        const points = [];
        for (let j = 0; j + 1 < ordinates.length; j += 2) {
            points.push([ordinates[j + 1], ordinates[j]]);
        }
        return points;
    }

    // A single [lat,lng] from a point geometry (gtype 2001).
    function pointLatLng(geometry) {
        const o = geometry.ordinates;
        if (!o || o.length < 2) {
            return null;
        }
        return [o[1], o[0]];
    }

    // Draw any feature geometry (polygon/line/point) and return its layer.
    function drawGeometry(geometry, style) {
        if (!geometry || typeof geometry.gtype === "undefined") {
            return null;
        }
        if (geometry.gtype === 2003) {
            const rings = polygonRings(geometry);
            return rings ? L.polygon(rings, style) : null;
        }
        if (geometry.gtype === 2002) {
            const pts = lineLatLngs(geometry);
            return pts ? L.polyline(pts, style) : null;
        }
        if (geometry.gtype === 2001) {
            const p = pointLatLng(geometry);
            return p ? L.marker(p) : null;
        }
        return null;
    }

    // --- 1. area/perimeter polygons ---------------------------------------

    analysisPolygons.forEach(row => {
        const layer = drawGeometry(row.geometry, {
            color: row.type === "Biome" ? "#2e7d32" : "#3388ff",
            weight: 2,
            fillColor: row.type === "Biome" ? "#2e7d32" : "#3388ff",
            fillOpacity: 0.2,
            pmIgnore: true
        });
        if (!layer) {
            return;
        }

        const area = Number(row.area).toFixed(1);
        const perimeter = Number(row.perimeter).toFixed(1);
        const label = `${row.name}<br>${area} m² · ${perimeter} m`;

        layer.bindTooltip(label, {
            permanent: true,
            direction: "center",
            className: "analysis-label"
        });

        allLayers.addLayer(layer);
    });

    // --- 2. distance line --------------------------------------------------

    if (distanceData && distanceData.distanceKm != null
        && distanceData.from && distanceData.to) {

        const fromPoint = distanceData.from.point
            ? pointLatLng(distanceData.from.point) : null;
        const toPoint = distanceData.to.point
            ? pointLatLng(distanceData.to.point) : null;

        // Draw each selected feature's own shape (highlighted).
        [distanceData.from, distanceData.to].forEach(feature => {
            const shape = drawGeometry(feature.geometry, {
                color: "#d32f2f",
                weight: 3,
                fillColor: "#d32f2f",
                fillOpacity: 0.15,
                pmIgnore: true
            });
            if (shape) {
                allLayers.addLayer(shape);
            }
        });

        // Draw the connecting line between representative points.
        if (fromPoint && toPoint) {
            const line = L.polyline([fromPoint, toPoint], {
                color: "#d32f2f",
                weight: 2,
                dashArray: "6,6",
                pmIgnore: true
            });

            const midpoint = [
                (fromPoint[0] + toPoint[0]) / 2,
                (fromPoint[1] + toPoint[1]) / 2
            ];

            const km = Number(distanceData.distanceKm).toFixed(3);
            const label = `Distance from ${distanceData.fromName} `
                + `to ${distanceData.toName} is ${km} km`;

            line.bindTooltip(label, {
                permanent: true,
                direction: "center",
                className: "analysis-label"
            });

            allLayers.addLayer(line);
            // Keep a visible label even for a very short line.
            L.circleMarker(midpoint, { radius: 0, opacity: 0 }).addTo(map);
        }
    }

    // --- fit to everything drawn ------------------------------------------

    if (allLayers.getLayers().length > 0) {
        map.fitBounds(allLayers.getBounds(), { padding: [30, 30] });
    }
});
