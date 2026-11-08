package com.aquaai.accountability.application.dto;

import com.aquaai.accountability.domain.model.EvidenceConfidence;

import java.util.UUID;

public record AttachEvidenceCommand(
        UUID incidentId,
        String evidenceType,
        String title,
        String description,
        String evidenceUri,
        EvidenceConfidence confidence,
        boolean isSimulated,
        String sourceReference,
        UUID recordedBy,
        String recorderName,
        String recorderRole
) {}
