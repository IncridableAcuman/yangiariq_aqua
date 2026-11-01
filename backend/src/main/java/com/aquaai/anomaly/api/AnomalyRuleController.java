package com.aquaai.anomaly.api;

import com.aquaai.anomaly.api.dto.AnomalyRuleResponse;
import com.aquaai.anomaly.api.dto.UpdateAnomalyRuleRequest;
import com.aquaai.anomaly.application.usecase.GetAnomalyRuleUseCase;
import com.aquaai.anomaly.application.usecase.ListAnomalyRulesUseCase;
import com.aquaai.anomaly.application.usecase.ToggleAnomalyRuleUseCase;
import com.aquaai.anomaly.application.usecase.UpdateAnomalyRuleUseCase;
import com.aquaai.anomaly.domain.model.AnomalyRuleConfig;
import com.aquaai.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/anomaly-rules")
@RequiredArgsConstructor
public class AnomalyRuleController {

    private final ListAnomalyRulesUseCase listAnomalyRulesUseCase;
    private final GetAnomalyRuleUseCase getAnomalyRuleUseCase;
    private final UpdateAnomalyRuleUseCase updateAnomalyRuleUseCase;
    private final ToggleAnomalyRuleUseCase toggleAnomalyRuleUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnomalyRuleResponse>>> listRules() {
        List<AnomalyRuleConfig> rules = listAnomalyRulesUseCase.execute();
        List<AnomalyRuleResponse> responses = rules.stream()
                .map(AnomalyRuleResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnomalyRuleResponse>> getRule(@PathVariable UUID id) {
        AnomalyRuleConfig rule = getAnomalyRuleUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(AnomalyRuleResponse.fromDomain(rule)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AnomalyRuleResponse>> updateRule(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAnomalyRuleRequest request
    ) {
        AnomalyRuleConfig updated = updateAnomalyRuleUseCase.execute(new UpdateAnomalyRuleUseCase.Command(
                id,
                request.name(),
                request.description(),
                request.severity(),
                request.threshold(),
                request.secondaryThreshold(),
                request.timeWindowMinutes() != null ? request.timeWindowMinutes() : 60,
                request.minConsecutiveReadings() != null ? request.minConsecutiveReadings() : 1
        ));
        return ResponseEntity.ok(ApiResponse.ok(AnomalyRuleResponse.fromDomain(updated)));
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<AnomalyRuleResponse>> enableRule(@PathVariable UUID id) {
        AnomalyRuleConfig updated = toggleAnomalyRuleUseCase.execute(id, true);
        return ResponseEntity.ok(ApiResponse.ok(AnomalyRuleResponse.fromDomain(updated)));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<AnomalyRuleResponse>> disableRule(@PathVariable UUID id) {
        AnomalyRuleConfig updated = toggleAnomalyRuleUseCase.execute(id, false);
        return ResponseEntity.ok(ApiResponse.ok(AnomalyRuleResponse.fromDomain(updated)));
    }
}
