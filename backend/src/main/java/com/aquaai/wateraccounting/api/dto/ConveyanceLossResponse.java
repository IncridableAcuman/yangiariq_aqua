package com.aquaai.wateraccounting.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ConveyanceLossResponse(
        UUID edgeId,
        String canalName,
        String edgeType,
        String startNodeName,
        String endNodeName,
        Instant periodStart,
        Instant periodEnd,
        BigDecimal upstreamVolumeM3,
        BigDecimal downstreamVolumeM3,
        BigDecimal lossVolumeM3,
        BigDecimal lossPercentage,
        String classification,
        String evidenceClassification,
        String confidence,
        String explanation
) {}
