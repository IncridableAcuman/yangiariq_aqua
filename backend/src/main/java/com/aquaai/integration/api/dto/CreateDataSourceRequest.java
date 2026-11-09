package com.aquaai.integration.api.dto;

import com.aquaai.integration.application.dto.RegisterDataSourceCommand;
import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.model.DataSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDataSourceRequest(
        @NotBlank(message = "Source code is required")
        @Size(min = 3, max = 64, message = "Source code must be between 3 and 64 characters")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Source code must contain only alphanumeric characters, underscores, or hyphens")
        @Schema(description = "Unique immutable code identifier for the data source", example = "KHOREZM_METEO_API")
        String sourceCode,

        @NotBlank(message = "Name is required")
        @Size(max = 128, message = "Name must not exceed 128 characters")
        @Schema(description = "Display name of the data source", example = "Khorezm Meteorological Agency API")
        String name,

        @NotNull(message = "Source type is required")
        @Schema(description = "Category of the data source", example = "WEATHER_PROVIDER")
        DataSourceType sourceType,

        @NotBlank(message = "Provider name is required")
        @Size(max = 64, message = "Provider name must not exceed 64 characters")
        @Schema(description = "Underlying technical adapter or vendor name", example = "NationalAgroMeteoAdapter")
        String providerName,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(description = "Detailed purpose or description")
        String description,

        @Size(max = 255, message = "Endpoint URL must not exceed 255 characters")
        @Schema(description = "Sanitized endpoint URL (without credentials)")
        String endpointUrl,

        @Size(max = 128, message = "Configuration reference must not exceed 128 characters")
        @Schema(description = "Reference to environment variable or secure config")
        String configurationRef,

        @Schema(description = "Initial status (default ACTIVE)")
        DataSourceStatus initialStatus
) {
    public RegisterDataSourceCommand toCommand() {
        return new RegisterDataSourceCommand(
                sourceCode,
                name,
                sourceType,
                providerName,
                description,
                endpointUrl,
                configurationRef,
                initialStatus != null ? initialStatus : DataSourceStatus.ACTIVE
        );
    }
}
