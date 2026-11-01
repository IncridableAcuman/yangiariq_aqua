package com.aquaai.water.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * WaterSource Aggregate Root representing shared/administrative water delivery infrastructure
 * (e.g. main canal, secondary ditch, reservoir, pumping well) supplying water to Yangiariq parcels.
 * <p>
 * Business Invariants:
 * <ul>
 *   <li>Name must not be blank and cannot exceed 128 characters.</li>
 *   <li>Type must not be null.</li>
 *   <li>Description cannot exceed 500 characters.</li>
 *   <li>Status defaults to ACTIVE.</li>
 *   <li>Coordinates (latitude, longitude) are optional; if provided, both must be present and valid.</li>
 * </ul>
 */
@Getter
public class WaterSource {

    public static final int MAX_NAME_LENGTH = 128;
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    public static final BigDecimal MIN_LATITUDE = new BigDecimal("-90.0");
    public static final BigDecimal MAX_LATITUDE = new BigDecimal("90.0");
    public static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180.0");
    public static final BigDecimal MAX_LONGITUDE = new BigDecimal("180.0");

    private final UUID id;
    private String name;
    private WaterSourceType type;
    private String description;
    private WaterSourceStatus status;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    /**
     * Domain factory method for creating a brand-new water source with coordinates.
     */
    public static WaterSource create(String name, WaterSourceType type, String description, BigDecimal latitude, BigDecimal longitude) {
        return create(UUID.randomUUID(), name, type, description, latitude, longitude);
    }

    /**
     * Domain factory method for creating a brand-new water source without coordinates.
     */
    public static WaterSource create(String name, WaterSourceType type, String description) {
        return create(UUID.randomUUID(), name, type, description, null, null);
    }

    /**
     * Domain factory method with explicit ID and coordinates.
     */
    public static WaterSource create(UUID id, String name, WaterSourceType type, String description, BigDecimal latitude, BigDecimal longitude) {
        Instant now = Instant.now();
        return new WaterSource(
                id,
                name,
                type,
                description,
                WaterSourceStatus.ACTIVE,
                latitude,
                longitude,
                now,
                now,
                null
        );
    }

    /**
     * Domain factory method with explicit ID (for testing or client-assigned identifiers).
     */
    public static WaterSource create(UUID id, String name, WaterSourceType type, String description) {
        return create(id, name, type, description, null, null);
    }

    /**
     * Full reconstitution constructor (used by repository/persistence mapper).
     */
    public WaterSource(
            UUID id,
            String name,
            WaterSourceType type,
            String description,
            WaterSourceStatus status,
            BigDecimal latitude,
            BigDecimal longitude,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = Objects.requireNonNull(id, "WaterSource ID must not be null");
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version;

        setNameInternal(name);
        setTypeInternal(type);
        setDescriptionInternal(description);
        setCoordinatesInternal(latitude, longitude);
        this.status = status != null ? status : WaterSourceStatus.ACTIVE;
    }

    /**
     * Backward-compatible reconstitution constructor without coordinates.
     */
    public WaterSource(
            UUID id,
            String name,
            WaterSourceType type,
            String description,
            WaterSourceStatus status,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this(id, name, type, description, status, null, null, createdAt, updatedAt, version);
    }

    /**
     * Updates mutable water source details including coordinates while enforcing domain invariants.
     */
    public void updateDetails(String name, WaterSourceType type, String description, BigDecimal latitude, BigDecimal longitude) {
        setNameInternal(name);
        setTypeInternal(type);
        setDescriptionInternal(description);
        setCoordinatesInternal(latitude, longitude);
        this.updatedAt = Instant.now();
    }

    /**
     * Updates mutable water source details while preserving existing coordinates.
     */
    public void updateDetails(String name, WaterSourceType type, String description) {
        updateDetails(name, type, description, this.latitude, this.longitude);
    }

    public void activate() {
        this.status = WaterSourceStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = WaterSourceStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return this.status == WaterSourceStatus.ACTIVE;
    }

    public boolean isInactive() {
        return this.status == WaterSourceStatus.INACTIVE;
    }

    private void setNameInternal(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Water source name cannot be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new BusinessRuleException("Water source name cannot exceed " + MAX_NAME_LENGTH + " characters");
        }
        this.name = trimmed;
    }

    private void setTypeInternal(WaterSourceType type) {
        if (type == null) {
            throw new BusinessRuleException("Water source type must not be null");
        }
        this.type = type;
    }

    private void setDescriptionInternal(String description) {
        if (description != null) {
            String trimmed = description.trim();
            if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
                throw new BusinessRuleException("Water source description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
            }
            this.description = trimmed.isEmpty() ? null : trimmed;
        } else {
            this.description = null;
        }
    }

    private void setCoordinatesInternal(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null && longitude == null) {
            this.latitude = null;
            this.longitude = null;
            return;
        }
        if (latitude == null || longitude == null) {
            throw new BusinessRuleException("Both latitude and longitude must be provided together");
        }
        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new BusinessRuleException("Water source latitude must be between -90.0 and 90.0 degrees: " + latitude);
        }
        if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new BusinessRuleException("Water source longitude must be between -180.0 and 180.0 degrees: " + longitude);
        }
        this.latitude = latitude.setScale(6, RoundingMode.HALF_UP);
        this.longitude = longitude.setScale(6, RoundingMode.HALF_UP);
    }
}
