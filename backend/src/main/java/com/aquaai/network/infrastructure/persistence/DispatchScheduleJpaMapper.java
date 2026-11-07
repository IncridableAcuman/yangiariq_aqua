package com.aquaai.network.infrastructure.persistence;

import com.aquaai.network.domain.model.DispatchAssignment;
import com.aquaai.network.domain.model.DispatchConflict;
import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.model.DispatchStatus;
import com.aquaai.network.domain.model.FairnessPolicy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchScheduleJpaMapper {

    private final ObjectMapper objectMapper;

    public DispatchScheduleJpaEntity toJpaEntity(DispatchSchedule domain) {
        if (domain == null) return null;

        String assignmentsJson = "[]";
        String conflictsJson = "[]";
        String metricsJson = "{}";
        String policyJson = "{}";

        try {
            if (domain.getAssignments() != null) {
                assignmentsJson = objectMapper.writeValueAsString(domain.getAssignments());
            }
            if (domain.getConflicts() != null) {
                conflictsJson = objectMapper.writeValueAsString(domain.getConflicts());
            }
            if (domain.getMetrics() != null) {
                metricsJson = objectMapper.writeValueAsString(domain.getMetrics());
            }
            if (domain.getFairnessPolicy() != null) {
                policyJson = objectMapper.writeValueAsString(domain.getFairnessPolicy());
            }
        } catch (Exception e) {
            log.error("Failed to serialize dispatch schedule details to JSON: id={}", domain.getId(), e);
        }

        return DispatchScheduleJpaEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .periodStart(domain.getPeriodStart())
                .periodEnd(domain.getPeriodEnd())
                .status(domain.getStatus().name())
                .targetWuaId(domain.getTargetWuaId())
                .targetWuaName(domain.getTargetWuaName())
                .totalRequestedM3(domain.getTotalRequestedM3())
                .totalAllocatedM3(domain.getTotalAllocatedM3())
                .totalUnmetM3(domain.getTotalUnmetM3())
                .assignmentsJson(assignmentsJson)
                .conflictsJson(conflictsJson)
                .metricsJson(metricsJson)
                .fairnessPolicyJson(policyJson)
                .notes(domain.getNotes())
                .approvedBy(domain.getApprovedBy())
                .approvedAt(domain.getApprovedAt())
                .rejectionReason(domain.getRejectionReason())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .version(domain.getVersion())
                .build();
    }

    public DispatchSchedule toDomain(DispatchScheduleJpaEntity entity) {
        if (entity == null) return null;

        List<DispatchAssignment> assignments = Collections.emptyList();
        List<DispatchConflict> conflicts = Collections.emptyList();
        Map<String, Object> metrics = Collections.emptyMap();
        FairnessPolicy fairnessPolicy = FairnessPolicy.defaultPolicy();

        try {
            if (entity.getAssignmentsJson() != null && !entity.getAssignmentsJson().isBlank()) {
                assignments = objectMapper.readValue(entity.getAssignmentsJson(), new TypeReference<List<DispatchAssignment>>() {});
            }
            if (entity.getConflictsJson() != null && !entity.getConflictsJson().isBlank()) {
                conflicts = objectMapper.readValue(entity.getConflictsJson(), new TypeReference<List<DispatchConflict>>() {});
            }
            if (entity.getMetricsJson() != null && !entity.getMetricsJson().isBlank()) {
                metrics = objectMapper.readValue(entity.getMetricsJson(), new TypeReference<Map<String, Object>>() {});
            }
            if (entity.getFairnessPolicyJson() != null && !entity.getFairnessPolicyJson().isBlank()) {
                fairnessPolicy = objectMapper.readValue(entity.getFairnessPolicyJson(), FairnessPolicy.class);
            }
        } catch (Exception e) {
            log.error("Failed to deserialize dispatch schedule JSON: id={}", entity.getId(), e);
        }

        DispatchStatus status;
        try {
            status = DispatchStatus.valueOf(entity.getStatus());
        } catch (Exception e) {
            status = DispatchStatus.DRAFT;
        }

        return DispatchSchedule.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .status(status)
                .targetWuaId(entity.getTargetWuaId())
                .targetWuaName(entity.getTargetWuaName())
                .totalRequestedM3(entity.getTotalRequestedM3())
                .totalAllocatedM3(entity.getTotalAllocatedM3())
                .totalUnmetM3(entity.getTotalUnmetM3())
                .assignments(assignments)
                .conflicts(conflicts)
                .metrics(metrics)
                .fairnessPolicy(fairnessPolicy)
                .notes(entity.getNotes())
                .approvedBy(entity.getApprovedBy())
                .approvedAt(entity.getApprovedAt())
                .rejectionReason(entity.getRejectionReason())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .version(entity.getVersion())
                .build();
    }
}
