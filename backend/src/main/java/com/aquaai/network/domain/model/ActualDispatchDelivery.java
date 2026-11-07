package com.aquaai.network.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Audit record of actual physical water delivery recorded by the canal operator,
 * enabling planned-vs-actual variance auditing.
 */
@Getter
@Builder(toBuilder = true)
public class ActualDispatchDelivery {

    private final UUID id;
    private final UUID dispatchScheduleId;
    private final UUID fieldId;
    private final String fieldName;
    private final UUID edgeId;
    private final String canalName;
    private final Instant actualStartAt;
    private final Instant actualEndAt;
    private final BigDecimal plannedVolumeM3;
    private final BigDecimal actualVolumeM3;
    private final BigDecimal varianceVolumeM3;
    private final BigDecimal variancePercent;
    private final BigDecimal measuredFlowM3S;
    private final UUID operatorId;
    private final String operatorNotes;
    private final Instant createdAt;
    private Instant updatedAt;

    public ActualDispatchDelivery(
            UUID id,
            UUID dispatchScheduleId,
            UUID fieldId,
            String fieldName,
            UUID edgeId,
            String canalName,
            Instant actualStartAt,
            Instant actualEndAt,
            BigDecimal plannedVolumeM3,
            BigDecimal actualVolumeM3,
            BigDecimal varianceVolumeM3,
            BigDecimal variancePercent,
            BigDecimal measuredFlowM3S,
            UUID operatorId,
            String operatorNotes,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.dispatchScheduleId = dispatchScheduleId;
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId must not be null");
        this.fieldName = fieldName != null ? fieldName : "Field";
        this.edgeId = edgeId;
        this.canalName = canalName != null ? canalName : "Canal";
        this.actualStartAt = Objects.requireNonNull(actualStartAt, "actualStartAt must not be null");
        this.actualEndAt = Objects.requireNonNull(actualEndAt, "actualEndAt must not be null");
        this.plannedVolumeM3 = plannedVolumeM3 != null ? plannedVolumeM3 : BigDecimal.ZERO;
        this.actualVolumeM3 = actualVolumeM3 != null ? actualVolumeM3 : BigDecimal.ZERO;
        this.varianceVolumeM3 = this.actualVolumeM3.subtract(this.plannedVolumeM3);

        if (this.plannedVolumeM3.compareTo(BigDecimal.ZERO) > 0) {
            this.variancePercent = this.varianceVolumeM3.multiply(new BigDecimal("100.00"))
                    .divide(this.plannedVolumeM3, 2, RoundingMode.HALF_UP);
        } else {
            this.variancePercent = BigDecimal.ZERO;
        }

        this.measuredFlowM3S = measuredFlowM3S != null ? measuredFlowM3S : BigDecimal.ZERO;
        this.operatorId = operatorId;
        this.operatorNotes = operatorNotes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }
}
