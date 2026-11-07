package com.aquaai.network.api.dto;

import com.aquaai.network.domain.model.DispatchAssignment;
import com.aquaai.network.domain.model.DispatchConflict;
import com.aquaai.network.domain.model.FairnessPolicy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DispatchScheduleResponse(
        UUID id,
        String title,
        Instant periodStart,
        Instant periodEnd,
        String status,
        UUID targetWuaId,
        String targetWuaName,
        BigDecimal totalRequestedM3,
        BigDecimal totalAllocatedM3,
        BigDecimal totalUnmetM3,
        List<DispatchAssignment> assignments,
        List<DispatchConflict> conflicts,
        Map<String, Object> metrics,
        FairnessPolicy fairnessPolicy,
        String notes,
        UUID approvedBy,
        Instant approvedAt,
        String rejectionReason,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {}
