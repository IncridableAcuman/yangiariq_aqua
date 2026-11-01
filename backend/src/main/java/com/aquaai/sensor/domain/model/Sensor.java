package com.aquaai.sensor.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Sensor Aggregate Root representing a physical or simulated telemetry device installed on an agricultural field.
 */
@Getter
public class Sensor {

    public static final int MAX_NAME_LENGTH = 128;
    public static final int MAX_DEVICE_IDENTIFIER_LENGTH = 64;
    public static final int MAX_PROVIDER_LENGTH = 64;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final Pattern DEVICE_IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.:]+$");

    private final UUID id;
    private final UUID fieldId;
    private String name;
    private final String deviceIdentifier;
    private final SensorType type;
    private SensorStatus status;
    private String provider;
    private String description;
    private Instant installedAt;
    private Instant lastSeenAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    /**
     * Domain factory method for creating a new sensor aggregate.
     */
    public static Sensor create(
            UUID fieldId,
            String name,
            String deviceIdentifier,
            SensorType type,
            String provider,
            String description
    ) {
        return create(UUID.randomUUID(), fieldId, name, deviceIdentifier, type, provider, description);
    }

    /**
     * Domain factory method with caller-assigned UUID.
     */
    public static Sensor create(
            UUID id,
            UUID fieldId,
            String name,
            String deviceIdentifier,
            SensorType type,
            String provider,
            String description
    ) {
        Instant now = Instant.now();
        return new Sensor(
                id,
                fieldId,
                name,
                deviceIdentifier,
                type,
                SensorStatus.ACTIVE,
                provider != null ? provider : "SIMULATED",
                description,
                now,
                null,
                now,
                now,
                null
        );
    }

    /**
     * Full reconstitution constructor (used by repository persistence mappers).
     */
    public Sensor(
            UUID id,
            UUID fieldId,
            String name,
            String deviceIdentifier,
            SensorType type,
            SensorStatus status,
            String provider,
            String description,
            Instant installedAt,
            Instant lastSeenAt,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = Objects.requireNonNull(id, "Sensor ID must not be null");
        this.fieldId = Objects.requireNonNull(fieldId, "Field ID must not be null");
        this.type = Objects.requireNonNull(type, "Sensor type must not be null");
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.installedAt = installedAt != null ? installedAt : this.createdAt;
        this.lastSeenAt = lastSeenAt;
        this.version = version;

        setNameInternal(name);
        this.deviceIdentifier = validateDeviceIdentifier(deviceIdentifier);
        this.status = status != null ? status : SensorStatus.ACTIVE;
        setProviderInternal(provider);
        setDescriptionInternal(description);
    }

    public void updateDetails(String name, String description) {
        setNameInternal(name);
        setDescriptionInternal(description);
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status.validateTransition(SensorStatus.ACTIVE);
        this.status = SensorStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status.validateTransition(SensorStatus.INACTIVE);
        this.status = SensorStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public void setMaintenance() {
        this.status.validateTransition(SensorStatus.MAINTENANCE);
        this.status = SensorStatus.MAINTENANCE;
        this.updatedAt = Instant.now();
    }

    public void markOffline() {
        this.status.validateTransition(SensorStatus.OFFLINE);
        this.status = SensorStatus.OFFLINE;
        this.updatedAt = Instant.now();
    }

    public void recordHeartbeat(Instant timestamp) {
        this.lastSeenAt = timestamp != null ? timestamp : Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return this.status == SensorStatus.ACTIVE;
    }

    private void setNameInternal(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Sensor name cannot be blank", "BLANK_SENSOR_NAME");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new BusinessRuleException(
                    "Sensor name cannot exceed " + MAX_NAME_LENGTH + " characters",
                    "SENSOR_NAME_TOO_LONG"
            );
        }
        this.name = trimmed;
    }

    private String validateDeviceIdentifier(String deviceIdentifier) {
        if (deviceIdentifier == null || deviceIdentifier.isBlank()) {
            throw new BusinessRuleException("Device identifier cannot be blank", "BLANK_DEVICE_IDENTIFIER");
        }
        String trimmed = deviceIdentifier.trim();
        if (trimmed.length() > MAX_DEVICE_IDENTIFIER_LENGTH) {
            throw new BusinessRuleException(
                    "Device identifier cannot exceed " + MAX_DEVICE_IDENTIFIER_LENGTH + " characters",
                    "DEVICE_IDENTIFIER_TOO_LONG"
            );
        }
        if (!DEVICE_IDENTIFIER_PATTERN.matcher(trimmed).matches()) {
            throw new BusinessRuleException(
                    "Device identifier contains invalid characters: " + trimmed,
                    "INVALID_DEVICE_IDENTIFIER"
            );
        }
        return trimmed;
    }

    private void setProviderInternal(String provider) {
        if (provider != null) {
            String trimmed = provider.trim();
            if (trimmed.length() > MAX_PROVIDER_LENGTH) {
                throw new BusinessRuleException(
                        "Provider identifier cannot exceed " + MAX_PROVIDER_LENGTH + " characters",
                        "PROVIDER_TOO_LONG"
                );
            }
            this.provider = trimmed.isEmpty() ? "SIMULATED" : trimmed;
        } else {
            this.provider = "SIMULATED";
        }
    }

    private void setDescriptionInternal(String description) {
        if (description != null) {
            String trimmed = description.trim();
            if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
                throw new BusinessRuleException(
                        "Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters",
                        "DESCRIPTION_TOO_LONG"
                );
            }
            this.description = trimmed.isEmpty() ? null : trimmed;
        } else {
            this.description = null;
        }
    }
}
