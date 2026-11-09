package com.aquaai.integration.application.dto;

public record UpdateDataSourceCommand(
        String name,
        String description,
        String endpointUrl,
        String configurationRef
) {}
