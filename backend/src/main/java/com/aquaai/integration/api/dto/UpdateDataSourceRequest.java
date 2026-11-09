package com.aquaai.integration.api.dto;

import com.aquaai.integration.application.dto.UpdateDataSourceCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UpdateDataSourceRequest(
        @Size(max = 128, message = "Name must not exceed 128 characters")
        @Schema(description = "Updated display name")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        @Schema(description = "Updated description")
        String description,

        @Size(max = 255, message = "Endpoint URL must not exceed 255 characters")
        @Schema(description = "Updated endpoint URL")
        String endpointUrl,

        @Size(max = 128, message = "Configuration reference must not exceed 128 characters")
        @Schema(description = "Updated configuration reference")
        String configurationRef
) {
    public UpdateDataSourceCommand toCommand() {
        return new UpdateDataSourceCommand(name, description, endpointUrl, configurationRef);
    }
}
