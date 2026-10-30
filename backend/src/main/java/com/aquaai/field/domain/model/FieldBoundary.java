package com.aquaai.field.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value Object representing a GeoJSON Polygon field boundary conforming to RFC 7946.
 * <p>
 * Structure:
 * <pre>
 * {
 *   "type": "Polygon",
 *   "coordinates": [
 *     [
 *       [longitude, latitude],
 *       [longitude, latitude],
 *       [longitude, latitude],
 *       [longitude, latitude]
 *     ]
 *   ]
 * }
 * </pre>
 * <p>
 * IMPORTANT: GeoJSON uses [longitude, latitude] order (X, Y).
 */
public record FieldBoundary(
        @JsonProperty("type") String type,
        @JsonProperty("coordinates") List<List<List<Double>>> coordinates
) {

    public static final String POLYGON_TYPE = "Polygon";
    private static final double EPSILON = 1e-7;

    @JsonCreator
    public FieldBoundary(
            @JsonProperty("type") String type,
            @JsonProperty("coordinates") List<List<List<Double>>> coordinates
    ) {
        if (type == null || !POLYGON_TYPE.equalsIgnoreCase(type)) {
            throw new BusinessRuleException("Field boundary type must be 'Polygon'");
        }
        this.type = POLYGON_TYPE;

        if (coordinates == null || coordinates.isEmpty()) {
            throw new BusinessRuleException("Field boundary coordinates cannot be empty");
        }

        List<List<Double>> exteriorRing = coordinates.getFirst();
        if (exteriorRing == null || exteriorRing.size() < 4) {
            throw new BusinessRuleException(
                    "Field boundary polygon exterior ring must contain at least 4 coordinates (closed ring)");
        }

        for (int i = 0; i < exteriorRing.size(); i++) {
            List<Double> point = exteriorRing.get(i);
            if (point == null || point.size() < 2) {
                throw new BusinessRuleException(
                        "Boundary coordinate at index " + i + " must contain at least [longitude, latitude]");
            }
            double lon = point.get(0);
            double lat = point.get(1);

            if (Double.isNaN(lon) || Double.isInfinite(lon) || lon < -180.0 || lon > 180.0) {
                throw new BusinessRuleException("Boundary longitude must be between -180.0 and 180.0 degrees: " + lon);
            }
            if (Double.isNaN(lat) || Double.isInfinite(lat) || lat < -90.0 || lat > 90.0) {
                throw new BusinessRuleException("Boundary latitude must be between -90.0 and 90.0 degrees: " + lat);
            }
        }

        // Validate closure: first point must match last point
        List<Double> first = exteriorRing.getFirst();
        List<Double> last = exteriorRing.getLast();
        boolean closed = Math.abs(first.get(0) - last.get(0)) < EPSILON && Math.abs(first.get(1) - last.get(1)) < EPSILON;
        if (!closed) {
            throw new BusinessRuleException(
                    "Field boundary polygon exterior ring must be closed: first coordinate " + first +
                    " does not match last coordinate " + last);
        }

        List<List<List<Double>>> copiedCoordinates = new ArrayList<>();
        for (List<List<Double>> ring : coordinates) {
            List<List<Double>> copiedRing = new ArrayList<>();
            for (List<Double> pt : ring) {
                copiedRing.add(new ArrayList<>(pt));
            }
            copiedCoordinates.add(copiedRing);
        }
        this.coordinates = copiedCoordinates;
    }

    /**
     * Builds a Polygon FieldBoundary from a list of CoordinatePoint instances representing the exterior ring.
     * Automatically ensures closure by appending the first point if the caller did not close it.
     */
    public static FieldBoundary fromExteriorRing(List<CoordinatePoint> points) {
        if (points == null || points.size() < 3) {
            throw new BusinessRuleException("At least 3 distinct coordinate points are required to define a boundary");
        }

        List<List<Double>> ring = new ArrayList<>();
        for (CoordinatePoint pt : points) {
            ring.add(new ArrayList<>(List.of(pt.longitude(), pt.latitude())));
        }

        // Auto-close ring if not already closed
        CoordinatePoint first = points.getFirst();
        CoordinatePoint last = points.getLast();
        if (Math.abs(first.longitude() - last.longitude()) >= EPSILON || Math.abs(first.latitude() - last.latitude()) >= EPSILON) {
            ring.add(new ArrayList<>(List.of(first.longitude(), first.latitude())));
        }

        return new FieldBoundary(POLYGON_TYPE, List.of(ring));
    }

    /**
     * Returns the exterior ring as a typed list of CoordinatePoint.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<CoordinatePoint> getExteriorRingPoints() {
        if (coordinates.isEmpty()) {
            return Collections.emptyList();
        }
        List<CoordinatePoint> points = new ArrayList<>();
        for (List<Double> pt : coordinates.getFirst()) {
            points.add(CoordinatePoint.of(pt.get(0), pt.get(1)));
        }
        return Collections.unmodifiableList(points);
    }
}
