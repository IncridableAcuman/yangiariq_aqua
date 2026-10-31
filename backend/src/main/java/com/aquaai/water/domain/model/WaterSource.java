package com.aquaai.water.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Getter;

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
 * </ul>
 */
@Getter
public class WaterSource {

    public static final int MAX_NAME_LENGTH = 128;
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    private final UUID id;
    private String name;
    private WaterSourceType type;
    private String description;
    private WaterSourceStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    /**
     * Domain factory method for creating a brand-new water source.
     */
    public static WaterSource create(String name, WaterSourceType type, String description) {
        return create(UUID.randomUUID(), name, type, description);
    }

    /**
     * Domain factory method with explicit ID (for testing or client-assigned identifiers).
     */
    public static WaterSource create(UUID id, String name, WaterSourceType type, String description) {
        Instant now = Instant.now();
        return new WaterSource(
                id,
                name,
                type,
                description,
                WaterSourceStatus.ACTIVE,
                now,
                now,
                null
        );
    }

    /**
     * Reconstitution constructor (used by repository/persistence mapper).
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
        this.id = Objects.requireNonNull(id, "WaterSource ID must not be null");
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version;

        setNameInternal(name);
        setTypeInternal(type);
        setDescriptionInternal(description);
        this.status = status != null ? status : WaterSourceStatus.ACTIVE;
    }

    /**
     * Updates mutable water source details while enforcing domain invariants.
     */
    public void updateDetails(String name, WaterSourceType type, String description) {
        setNameInternal(name);
        setTypeInternal(type);
        setDescriptionInternal(description);
        this.updatedAt = Instant.now();
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
}
