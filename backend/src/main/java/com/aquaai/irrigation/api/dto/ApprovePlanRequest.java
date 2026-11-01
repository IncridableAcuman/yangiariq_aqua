package com.aquaai.irrigation.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApprovePlanRequest {
    private boolean acknowledgeWarnings = false;
}
