package com.aquaai.prediction.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "adaptive_selection_decisions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdaptiveSelectionDecisionJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "prediction_id")
    private UUID predictionId;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "prediction_type", nullable = false, length = 64)
    private String predictionType;

    @Column(name = "selected_version_tag", nullable = false, length = 64)
    private String selectedVersionTag;

    @Column(name = "selection_mode", nullable = false, length = 32)
    private String selectionMode;

    @Column(name = "selection_reason", nullable = false, columnDefinition = "TEXT")
    private String selectionReason;

    @Column(name = "evidence_summary", columnDefinition = "TEXT")
    private String evidenceSummary;

    @Column(name = "context_snapshot_json", nullable = false, columnDefinition = "TEXT")
    private String contextSnapshotJson;

    @Column(name = "alternatives_json", columnDefinition = "TEXT")
    private String alternativesJson;

    @Column(name = "confidence_percent", nullable = false)
    private int confidencePercent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
