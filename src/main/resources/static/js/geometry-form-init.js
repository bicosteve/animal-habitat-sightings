
// geometry-form-init.js
// Reads data-* attributes from its own <script> tag and boots the embedded-map
// drawing helper defined in geometry-draw.js. Load order:
//   leaflet -> leaflet-geoman -> geometry-draw.js -> geometry-form-init.js
//
// Supported data-* attributes:
//   data-map-id      (default "map")
//   data-shape       point | line | polygon  (default "polygon")
//   data-center-lat  (default 49.1951)
//   data-center-lng  (default 16.6068)
//   data-zoom        (default 12)

(function () {
    "use strict";

    // Captured at evaluation time, while this script is the current script.
    const self = document.currentScript;

    function num(attr, fallback) {
        const raw = self.getAttribute(attr);
        if (raw === null || raw === "") return fallback;
        const value = Number(raw);
        return Number.isNaN(value) ? fallback : value;
    }

    const options = {
        mapId: self.getAttribute("data-map-id") || "map",
        shape: self.getAttribute("data-shape") || "polygon",
        centerLat: num("data-center-lat", 49.1951),
        centerLng: num("data-center-lng", 16.6068),
        zoom: num("data-zoom", 12)
    };

    // Optional initial point to seed on edit forms (data-initial-lat/-lng).
    const initialLat = num("data-initial-lat", NaN);
    const initialLng = num("data-initial-lng", NaN);
    if (!Number.isNaN(initialLat) && !Number.isNaN(initialLng)) {
        options.initialPoint = { lat: initialLat, lng: initialLng };
    }

    // Optional initial line to seed on edit forms: data-initial-line holds a
    // JSON array of [lat, lng] vertices.
    const rawLine = self.getAttribute("data-initial-line");
    if (rawLine) {
        try {
            const parsed = JSON.parse(rawLine);
            if (Array.isArray(parsed) && parsed.length >= 2) {
                options.initialLine = parsed;
            }
        } catch (error) {
            console.error("geometry-form-init: invalid data-initial-line JSON.", error);
        }
    }

    function start() {
        if (typeof window.initGeometryForm !== "function") {
            console.error("geometry-form-init: initGeometryForm not loaded.");
            return;
        }
        window.initGeometryForm(options);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", start);
    } else {
        start();
    }
})();
