package com.aquaai.integration.application.dto;

import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.model.DataSourceType;

public record RegisterDataSourceCommand(
        String sourceCode,
        String name,
        DataSourceType sourceType,
        String providerName,
        String description,
        String endpointUrl,
        String configurationRef,
        DataSourceStatus initialStatus
) {}
