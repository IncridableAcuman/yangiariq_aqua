package com.aquaai.irrigation.api;

import com.aquaai.irrigation.api.dto.WaterBudgetResponse;
import com.aquaai.irrigation.application.usecase.GetFieldWaterBudgetUseCase;
import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WaterBudgetController {

    private final GetFieldWaterBudgetUseCase getFieldWaterBudgetUseCase;

    @GetMapping("/water-budget")
    public ResponseEntity<ApiResponse<WaterBudgetResponse>> getWaterBudget(
            @RequestParam(required = false) UUID fieldId
    ) {
        if (fieldId != null) {
            WaterBudget budget = getFieldWaterBudgetUseCase.execute(fieldId);
            return ResponseEntity.ok(ApiResponse.ok(WaterBudgetResponse.fromDomain(budget)));
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/fields/{fieldId}/water-budget")
    public ResponseEntity<ApiResponse<WaterBudgetResponse>> getFieldWaterBudget(
            @PathVariable UUID fieldId
    ) {
        WaterBudget budget = getFieldWaterBudgetUseCase.execute(fieldId);
        return ResponseEntity.ok(ApiResponse.ok(WaterBudgetResponse.fromDomain(budget)));
    }
}
