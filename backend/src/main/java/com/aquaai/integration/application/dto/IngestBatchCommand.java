package com.aquaai.integration.application.dto;

import java.util.List;

public record IngestBatchCommand(
        String sourceCode,
        List<IngestTelemetryItemCommand> items
) {}
