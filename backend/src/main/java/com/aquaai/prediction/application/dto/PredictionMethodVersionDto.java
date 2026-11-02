package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.PredictionMethodVersion;

import java.time.Instant;
import java.util.UUID;

public record PredictionMethodVersionDto(
        UUID id,
        String predictionType,
        String versionTag,
        String status,
        String description,
        String configurationJson,
        boolean isImmutable,
        Instant createdAt,
        Instant activatedAt,
        Instant retiredAt,
        String createdBy,
        String activatedBy
) {
    public static PredictionMethodVersionDto fromDomain(PredictionMethodVersion v) {
        if (v == null) return null;
        return new PredictionMethodVersionDto(
                v.getId(),
                v.getPredictionType().name(),
                v.getVersionTag(),
                v.getStatus().name(),
                v.getDescription(),
                v.getConfigurationJson(),
                v.isImmutable(),
                v.getCreatedAt(),
                v.getActivatedAt(),
                v.getRetiredAt(),
                v.getCreatedBy(),
                v.getActivatedBy()
        );
    }
}
