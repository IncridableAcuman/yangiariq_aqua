package com.aquaai.irrigation.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CompletePlanRequest {
    private BigDecimal actualVolumeDeliveredM3;
}
