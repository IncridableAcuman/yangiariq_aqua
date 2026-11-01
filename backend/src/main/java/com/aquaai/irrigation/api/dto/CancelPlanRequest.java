package com.aquaai.irrigation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CancelPlanRequest {

    @NotBlank(message = "Cancellation reason must not be blank")
    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    private String reason;
}
