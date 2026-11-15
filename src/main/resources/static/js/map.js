
document.addEventListener("DOMContentLoaded", () => {

    // Initialize map centered on Brno
    const map = L.map("map").setView(
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

    // Example sighting (temporary test data)
    const sighting = L.marker([
        49.1951,
        16.6068
    ]).addTo(map);

    sighting.bindPopup("Red Fox Sighting");

    sighting.on("click", () => {
        document.getElementById("detailsPanel").innerHTML = `
            <h5>Red Fox</h5>
            <p>Location: Brno</p>
            <p>Type: Animal Sighting</p>
            <button class="btn btn-primary btn-sm">
                Edit
            </button>
            <button class="btn btn-danger btn-sm">
                Delete
            </button>
        `;
    });

    // Example habitat polygon
    const habitat = L.polygon([
        [49.205, 16.590],
        [49.215, 16.615],
        [49.195, 16.625],
        [49.190, 16.600]
    ], {
        color: "green",
        fillOpacity: 0.25
    }).addTo(map);

    habitat.bindPopup("Example Habitat");

    habitat.on("click", () => {
        document.getElementById("detailsPanel").innerHTML = `
            <h5>Example Habitat</h5>
            <p>Type: Habitat Polygon</p>
            <button class="btn btn-primary btn-sm">
                Edit
            </button>
            <button class="btn btn-danger btn-sm">
                Delete
            </button>
        `;
    });

});
