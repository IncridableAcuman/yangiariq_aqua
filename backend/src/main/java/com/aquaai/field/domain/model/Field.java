package com.aquaai.field.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Field Aggregate Root representing an agricultural parcel managed by a farmer in Yangiariq.
 * <p>
 * Business Invariants:
 * <ul>
 *   <li>Owner ID must not be null (references User identity).</li>
 *   <li>Name must not be blank and cannot exceed 128 characters.</li>
 *   <li>Area in hectares must be strictly greater than zero.</li>
 *   <li>CropType, SoilType, and IrrigationMethod must not be null.</li>
 *   <li>Field ID and Owner ID are immutable once created.</li>
 * </ul>
 */
@Getter
public class Field {

    public static final int MAX_NAME_LENGTH = 128;
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    private final UUID id;
    private final UUID ownerId;
    private String name;
    private String description;
    private BigDecimal areaHectares;
    private CropType cropType;
    private SoilType soilType;
    private IrrigationMethod irrigationMethod;
    private FieldLocation location;
    private FieldBoundary boundary;
    private FieldStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    /**
     * Domain factory method for creating a brand-new agricultural Field aggregate.
     */
    public static Field create(
            UUID ownerId,
            String name,
            String description,
            BigDecimal areaHectares,
            CropType cropType,
            SoilType soilType,
            IrrigationMethod irrigationMethod,
            FieldLocation location,
            FieldBoundary boundary
    ) {
        return create(UUID.randomUUID(), ownerId, name, description, areaHectares, cropType, soilType, irrigationMethod, location, boundary);
    }

    /**
     * Domain factory method with explicit ID (e.g. for testing or client-assigned identifiers).
     */
    public static Field create(
            UUID id,
            UUID ownerId,
            String name,
            String description,
            BigDecimal areaHectares,
            CropType cropType,
            SoilType soilType,
            IrrigationMethod irrigationMethod,
            FieldLocation location,
            FieldBoundary boundary
    ) {
        Instant now = Instant.now();
        return new Field(
                id,
                ownerId,
                name,
                description,
                areaHectares,
                cropType,
                soilType,
                irrigationMethod,
                location,
                boundary,
                FieldStatus.ACTIVE,
                now,
                now,
                null
        );
    }

    /**
     * Reconstitution constructor (used by repository/persistence mapper).
     */
    public Field(
            UUID id,
            UUID ownerId,
            String name,
            String description,
            BigDecimal areaHectares,
            CropType cropType,
            SoilType soilType,
            IrrigationMethod irrigationMethod,
            FieldLocation location,
            FieldBoundary boundary,
            FieldStatus status,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        if (ownerId == null) {
            throw new BusinessRuleException("Field owner ID must not be null");
        }
        this.id = id;
        this.ownerId = ownerId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version;

        setNameInternal(name);
        setDescriptionInternal(description);
        setAreaHectaresInternal(areaHectares);
        setCropTypeInternal(cropType);
        setSoilTypeInternal(soilType);
        setIrrigationMethodInternal(irrigationMethod);
        this.location = location;
        this.boundary = boundary;
        this.status = status != null ? status : FieldStatus.ACTIVE;
    }

    /**
     * Updates mutable field properties while maintaining domain invariants.
     */
    public void updateDetails(
            String name,
            String description,
            BigDecimal areaHectares,
            CropType cropType,
            SoilType soilType,
            IrrigationMethod irrigationMethod,
            FieldLocation location,
            FieldBoundary boundary
    ) {
        setNameInternal(name);
        setDescriptionInternal(description);
        setAreaHectaresInternal(areaHectares);
        setCropTypeInternal(cropType);
        setSoilTypeInternal(soilType);
        setIrrigationMethodInternal(irrigationMethod);
        this.location = location;
        this.boundary = boundary;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = FieldStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = FieldStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public boolean isOwnedBy(UUID userId) {
        if (userId == null) {
            return false;
        }
        return this.ownerId.equals(userId);
    }

    public boolean isActive() {
        return this.status == FieldStatus.ACTIVE;
    }

    public boolean isInactive() {
        return this.status == FieldStatus.INACTIVE;
    }

    private void setNameInternal(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Field name cannot be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new BusinessRuleException("Field name cannot exceed " + MAX_NAME_LENGTH + " characters");
        }
        this.name = trimmed;
    }

    private void setDescriptionInternal(String description) {
        if (description != null) {
            String trimmed = description.trim();
            if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
                throw new BusinessRuleException("Field description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
            }
            this.description = trimmed.isEmpty() ? null : trimmed;
        } else {
            this.description = null;
        }
    }

    private void setAreaHectaresInternal(BigDecimal areaHectares) {
        if (areaHectares == null) {
            throw new BusinessRuleException("Field area in hectares must not be null");
        }
        if (areaHectares.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Field area must be greater than zero hectares: " + areaHectares);
        }
        this.areaHectares = areaHectares;
    }

    private void setCropTypeInternal(CropType cropType) {
        if (cropType == null) {
            throw new BusinessRuleException("Field crop type must not be null");
        }
        this.cropType = cropType;
    }

    private void setSoilTypeInternal(SoilType soilType) {
        if (soilType == null) {
            throw new BusinessRuleException("Field soil type must not be null");
        }
        this.soilType = soilType;
    }

    private void setIrrigationMethodInternal(IrrigationMethod irrigationMethod) {
        if (irrigationMethod == null) {
            throw new BusinessRuleException("Field irrigation method must not be null");
        }
        this.irrigationMethod = irrigationMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return Objects.equals(id, field.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
