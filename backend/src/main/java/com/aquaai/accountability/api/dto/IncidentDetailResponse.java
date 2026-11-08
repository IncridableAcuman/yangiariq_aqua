package com.aquaai.accountability.api.dto;

import com.aquaai.accountability.application.dto.IncidentDetailsDto;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public record IncidentDetailResponse(
        IncidentResponse incident,
        List<IncidentEvidenceResponse> evidence,
        List<CorrectiveActionResponse> actions,
        List<IncidentVerificationResponse> verifications,
        List<IncidentTimelineResponse> timeline,
        String canalName,
        String wuaName,
        String fieldName,
        boolean isOverdue
) {
    public static IncidentDetailResponse from(IncidentDetailsDto dto, Instant now) {
        if (dto == null) return null;
        return new IncidentDetailResponse(
                IncidentResponse.from(dto.incident(), now),
                dto.evidence().stream().map(IncidentEvidenceResponse::from).collect(Collectors.toList()),
                dto.actions().stream().map(CorrectiveActionResponse::from).collect(Collectors.toList()),
                dto.verifications().stream().map(IncidentVerificationResponse::from).collect(Collectors.toList()),
                dto.timeline().stream().map(IncidentTimelineResponse::from).collect(Collectors.toList()),
                dto.canalName(),
                dto.wuaName(),
                dto.fieldName(),
                dto.isOverdue()
        );
    }
}
