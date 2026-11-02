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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "backtest_runs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestRunJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "proposal_id", nullable = false)
    private UUID proposalId;

    @Column(name = "current_version_tag", nullable = false, length = 64)
    private String currentVersionTag;

    @Column(name = "candidate_version_tag", nullable = false, length = 64)
    private String candidateVersionTag;

    @Column(name = "date_range_from", nullable = false)
    private Instant dateRangeFrom;

    @Column(name = "date_range_to", nullable = false)
    private Instant dateRangeTo;

    @Column(name = "sample_size", nullable = false)
    private int sampleSize;

    @Column(name = "current_mae", precision = 12, scale = 4)
    private BigDecimal currentMae;

    @Column(name = "candidate_mae", precision = 12, scale = 4)
    private BigDecimal candidateMae;

    @Column(name = "mae_improvement_percent", precision = 8, scale = 2)
    private BigDecimal maeImprovementPercent;

    @Column(name = "current_rmse", precision = 12, scale = 4)
    private BigDecimal currentRmse;

    @Column(name = "candidate_rmse", precision = 12, scale = 4)
    private BigDecimal candidateRmse;

    @Column(name = "current_mbe", precision = 12, scale = 4)
    private BigDecimal currentMbe;

    @Column(name = "candidate_mbe", precision = 12, scale = 4)
    private BigDecimal candidateMbe;

    @Column(name = "current_accuracy_percent", precision = 8, scale = 2)
    private BigDecimal currentAccuracyPercent;

    @Column(name = "candidate_accuracy_percent", precision = 8, scale = 2)
    private BigDecimal candidateAccuracyPercent;

    @Column(name = "current_f1", precision = 6, scale = 4)
    private BigDecimal currentF1;

    @Column(name = "candidate_f1", precision = 6, scale = 4)
    private BigDecimal candidateF1;

    @Column(name = "regression_detected", nullable = false)
    private boolean regressionDetected;

    @Column(name = "regression_details", columnDefinition = "TEXT")
    private String regressionDetails;

    @Column(name = "data_quality", nullable = false, length = 32)
    private String dataQuality;

    @Column(name = "is_comparable", nullable = false)
    private boolean isComparable;

    @Column(name = "has_sufficient_sample", nullable = false)
    private boolean hasSufficientSample;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    @Column(name = "executed_by", length = 64)
    private String executedBy;
}
