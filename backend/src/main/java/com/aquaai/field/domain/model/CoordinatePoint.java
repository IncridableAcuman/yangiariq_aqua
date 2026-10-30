package com.aquaai.field.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;

/**
 * Geographical coordinate point following GeoJSON RFC 7946 specification: [longitude, latitude].
 * <p>
 * Longitude: X-axis coordinate in range [-180.0, 180.0]
 * Latitude: Y-axis coordinate in range [-90.0, 90.0]
 */
public record CoordinatePoint(double longitude, double latitude) {

    public CoordinatePoint {
        if (Double.isNaN(longitude) || Double.isInfinite(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new BusinessRuleException("Longitude must be between -180.0 and 180.0 degrees: " + longitude);
        }
        if (Double.isNaN(latitude) || Double.isInfinite(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new BusinessRuleException("Latitude must be between -90.0 and 90.0 degrees: " + latitude);
        }
    }

    public static CoordinatePoint of(double longitude, double latitude) {
        return new CoordinatePoint(longitude, latitude);
    }
}
