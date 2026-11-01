package com.aquaai.sensor.infrastructure.persistence;

import com.aquaai.shared.domain.BaseAuditableEntity;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.sensor.domain.model.SensorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing the "sensor_devices" table.
 * Infrastructure persistence representation; never exposed directly to domain or API layers.
 */
@Getter
@Setter
@Entity
@Table(name = "sensor_devices")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorDeviceJpaEntity extends BaseAuditableEntity {

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "device_identifier", nullable = false, length = 64, unique = true)
    private String deviceIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private SensorType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private SensorStatus status = SensorStatus.ACTIVE;

    @Column(name = "provider", nullable = false, length = 64)
    @Builder.Default
    private String provider = "SIMULATED";

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "installed_at", nullable = false)
    private Instant installedAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;
}
