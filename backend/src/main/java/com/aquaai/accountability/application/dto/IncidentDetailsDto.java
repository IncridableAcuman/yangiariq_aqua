package com.aquaai.accountability.application.dto;

import com.aquaai.accountability.domain.model.CorrectiveAction;
import com.aquaai.accountability.domain.model.IncidentEvidence;
import com.aquaai.accountability.domain.model.IncidentTimelineEntry;
import com.aquaai.accountability.domain.model.IncidentVerification;
import com.aquaai.accountability.domain.model.WaterIncident;

import java.util.List;

public record IncidentDetailsDto(
        WaterIncident incident,
        List<IncidentEvidence> evidence,
        List<CorrectiveAction> actions,
        List<IncidentVerification> verifications,
        List<IncidentTimelineEntry> timeline,
        String canalName,
        String wuaName,
        String fieldName,
        boolean isOverdue
) {}
