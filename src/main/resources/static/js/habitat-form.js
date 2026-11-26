
document.addEventListener("DOMContentLoaded", () => {

    const storedData = sessionStorage.getItem("drawnGeometries");

    const status = document.getElementById("geometryStatus");
    const submitButton = document.querySelector(
        '#habitatForm button[type="submit"]'
    );
    const geometrySelect = document.getElementById("geometrySelect");

    if (!storedData) {
        showError("No geometries found. Return to the dashboard.");
        return;
    }

    try {
        const geometries = JSON.parse(storedData);

        const polygons = geometries.filter(
            geometry => geometry.sdo_gtype === 2003
        );

        if (polygons.length === 0) {
            showError("No polygon geometries found.");
            return;
        }

        polygons.forEach((polygon, index) => {
            const option = document.createElement("option");

            option.value = index;
            option.textContent = `Polygon ${index + 1}`;

            geometrySelect.appendChild(option);
        });

        function loadPolygon() {

            const selectedIndex = geometrySelect.value;

            if (selectedIndex === "") {
                clearGeometryFields();
                return;
            }

            const polygon = polygons[Number(selectedIndex)];

            document.getElementById("sdo_gtype").value =
                polygon.sdo_gtype;

            document.getElementById("sdo_srid").value =
                polygon.sdo_srid;

            document.getElementById("sdo_elem_info").value =
                polygon.sdo_elem_info.join(",");

            document.getElementById("sdo_ordinates").value =
                polygon.sdo_ordinates.join(",");

            status.textContent =
                "Selected polygon loaded successfully.";

            status.classList.replace("alert-info", "alert-success");

            console.log("Selected habitat geometry:", polygon);
        }

        geometrySelect.addEventListener("change", loadPolygon);

        geometrySelect.value = "0";
        loadPolygon();

    } catch (error) {
        console.error("Geometry loading failed:", error);
        showError("Could not load geometry.");
    }

    function clearGeometryFields() {
        document.getElementById("sdo_gtype").value = "";
        document.getElementById("sdo_srid").value = "";
        document.getElementById("sdo_elem_info").value = "";
        document.getElementById("sdo_ordinates").value = "";
    }

    function showError(message) {
        status.textContent = message;
        status.classList.replace("alert-info", "alert-danger");
        submitButton.disabled = true;
    }
});
