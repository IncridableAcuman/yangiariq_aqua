package com.aquaai.field.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object representing the geographical center / representative coordinate of a field parcel.
 * Validates coordinate boundaries: latitude ∈ [-90.0, 90.0], longitude ∈ [-180.0, 180.0].
 */
public record FieldLocation(
        BigDecimal latitude,
        BigDecimal longitude
) {

    public static final BigDecimal MIN_LATITUDE = new BigDecimal("-90.0");
    public static final BigDecimal MAX_LATITUDE = new BigDecimal("90.0");
    public static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180.0");
    public static final BigDecimal MAX_LONGITUDE = new BigDecimal("180.0");

    public FieldLocation {
        if (latitude == null && longitude == null) {
            // Null coordinate representation
        } else if (latitude == null || longitude == null) {
            throw new BusinessRuleException("Both latitude and longitude must be provided together");
        } else {
            if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
                throw new BusinessRuleException("Field latitude must be between -90.0 and 90.0 degrees: " + latitude);
            }
            if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
                throw new BusinessRuleException("Field longitude must be between -180.0 and 180.0 degrees: " + longitude);
            }
            latitude = latitude.setScale(6, RoundingMode.HALF_UP);
            longitude = longitude.setScale(6, RoundingMode.HALF_UP);
        }
    }

    public static FieldLocation of(BigDecimal latitude, BigDecimal longitude) {
        return new FieldLocation(latitude, longitude);
    }

    public static FieldLocation of(double latitude, double longitude) {
        return new FieldLocation(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
    }
}
