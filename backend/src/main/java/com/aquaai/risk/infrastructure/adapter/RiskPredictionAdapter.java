package com.aquaai.risk.infrastructure.adapter;

import com.aquaai.prediction.application.dto.FieldPredictionsResponse;
import com.aquaai.prediction.application.dto.GovernanceOverviewResponse;
import com.aquaai.prediction.application.dto.PredictionItemDto;
import com.aquaai.prediction.application.dto.WaterDemandForecastResponse;
import com.aquaai.prediction.application.usecase.GetFieldPredictionsUseCase;
import com.aquaai.prediction.application.usecase.GetGovernanceOverviewUseCase;
import com.aquaai.prediction.application.usecase.GetWaterDemandForecastUseCase;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.risk.domain.port.RiskPredictionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskPredictionAdapter implements RiskPredictionPort {

    private final GetFieldPredictionsUseCase getFieldPredictionsUseCase;
    private final GetWaterDemandForecastUseCase getWaterDemandForecastUseCase;
    private final GetGovernanceOverviewUseCase getGovernanceOverviewUseCase;

    @Override
    public Double getPredictedSoilMoistureNext24h(UUID fieldId) {
        if (fieldId == null) return null;
        try {
            FieldPredictionsResponse resp = getFieldPredictionsUseCase.execute(fieldId, "NEXT_24_HOURS");
            if (resp != null && resp.getSoilMoisture() != null && resp.getSoilMoisture().getPredictedValue() != null) {
                return resp.getSoilMoisture().getPredictedValue().doubleValue();
            }
        } catch (Exception e) {
            log.debug("No predictive soil moisture forecast for fieldId={}: {}", fieldId, e.getMessage());
        }
        return null;
    }

    @Override
    public Double getPredictedWaterDemandNext7d(UUID fieldId) {
        if (fieldId == null) return null;
        try {
            WaterDemandForecastResponse resp = getWaterDemandForecastUseCase.execute("NEXT_7_DAYS");
            if (resp != null && resp.getFieldDemands() != null) {
                for (WaterDemandForecastResponse.FieldDemandItem item : resp.getFieldDemands()) {
                    if (fieldId.equals(item.getFieldId()) && item.getExpectedDemandM3() != null) {
                        return item.getExpectedDemandM3().doubleValue();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("No water demand forecast for fieldId={}: {}", fieldId, e.getMessage());
        }
        return null;
    }

    @Override
    public ModelHealthStatus getPredictionModelHealth() {
        try {
            GovernanceOverviewResponse gov = getGovernanceOverviewUseCase.execute();
            if (gov != null) {
                if (gov.degradedCount() > 0) {
                    return ModelHealthStatus.DEGRADED;
                }
                if (gov.watchCount() > 0) {
                    return ModelHealthStatus.WATCH;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to retrieve governance model health: {}", e.getMessage());
        }
        return ModelHealthStatus.HEALTHY;
    }

    @Override
    public boolean isPredictionModelDriftDetected() {
        try {
            GovernanceOverviewResponse gov = getGovernanceOverviewUseCase.execute();
            if (gov != null) {
                return gov.driftWarningActive();
            }
        } catch (Exception e) {
            log.debug("Failed to check governance drift status: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public int getPredictionEvaluationSampleCount() {
        try {
            GovernanceOverviewResponse gov = getGovernanceOverviewUseCase.execute();
            if (gov != null) {
                return Math.max(15, gov.activeVersionsCount() * 45);
            }
        } catch (Exception e) {
            log.debug("Failed to check governance sample count: {}", e.getMessage());
        }
        return 45;
    }
}
