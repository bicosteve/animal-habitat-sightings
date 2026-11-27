package cz.fit.mit.Animal_Habitat_Sightings.controller;

import oracle.spatial.geometry.JGeometry;
import org.springframework.validation.BindingResult;

import java.util.Arrays;

/**
 * Converts the comma-separated SDO string fields submitted by the create forms
 * into a {@link JGeometry}.
 *
 * <p>The forms send {@code sdo_elem_info} and {@code sdo_ordinates} as single
 * comma-joined strings (Spring will not bind one value into an {@code int[]} /
 * {@code double[]}). The browser populates them from the drawn geometry, so a
 * malformed or empty value means the user submitted without drawing, or the
 * payload was tampered with.
 *
 * <p>Rather than throwing a {@link NumberFormatException} (which would surface
 * as an HTTP 500), parsing problems are registered on the supplied
 * {@link BindingResult} so the controller can simply re-render the form with a
 * readable message. {@code @NotBlank} on the form already covers the empty
 * case; this helper guards against non-numeric or structurally invalid values.
 *
 * <p>Returns {@code null} when parsing fails; callers must check
 * {@code result.hasErrors()} before using the result.
 */
final class GeometryBinder {

    private GeometryBinder() {
    }

    static JGeometry parse(int gtype,
                           int srid,
                           String elemInfoCsv,
                           String ordinatesCsv,
                           BindingResult result) {

        // @NotBlank already reports blank values; skip to avoid duplicate errors.
        if (isBlank(elemInfoCsv) || isBlank(ordinatesCsv)) {
            return null;
        }

        int[] elemInfo;
        double[] ordinates;

        try {
            elemInfo = Arrays.stream(elemInfoCsv.split(","))
                    .map(String::trim)
                    .mapToInt(Integer::parseInt)
                    .toArray();
        } catch (NumberFormatException ex) {
            result.rejectValue("sdo_elem_info", "geometry.invalid",
                    "The drawn geometry is invalid. Please redraw it on the map.");
            return null;
        }

        try {
            ordinates = Arrays.stream(ordinatesCsv.split(","))
                    .map(String::trim)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
        } catch (NumberFormatException ex) {
            result.rejectValue("sdo_ordinates", "geometry.invalid",
                    "The drawn geometry is invalid. Please redraw it on the map.");
            return null;
        }

        if (ordinates.length < 2 || ordinates.length % 2 != 0) {
            result.rejectValue("sdo_ordinates", "geometry.invalid",
                    "The drawn geometry is incomplete. Please redraw it on the map.");
            return null;
        }

        return new JGeometry(gtype, srid, elemInfo, ordinates);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
