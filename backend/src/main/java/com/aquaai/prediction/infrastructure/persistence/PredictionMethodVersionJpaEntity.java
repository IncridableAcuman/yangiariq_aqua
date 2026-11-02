package com.aquaai.prediction.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prediction_method_versions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionMethodVersionJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "prediction_type", nullable = false, length = 64)
    private String predictionType;

    @Column(name = "version_tag", nullable = false, unique = true, length = 64)
    private String versionTag;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "configuration_json", nullable = false, columnDefinition = "TEXT")
    private String configurationJson;

    @Column(name = "is_immutable", nullable = false)
    private boolean isImmutable;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "retired_at")
    private Instant retiredAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "activated_by", length = 64)
    private String activatedBy;

    @Version
    @Column(name = "opt_lock_version", nullable = false)
    private Long optLockVersion;
}
