package com.aquaai.irrigation.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class GeneratePlanRequest {
    private LocalDate targetDate;
}
