package com.aquaai.prediction.infrastructure.provider;

import com.aquaai.prediction.application.usecase.SelectAdaptiveMethodUseCase;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.HydraulicAnomalyInfo;
import com.aquaai.prediction.domain.port.PredictionDataProvider.SensorHealthSummary;
import com.aquaai.prediction.domain.port.PredictionDataProvider.TelemetryObservation;
import com.aquaai.prediction.domain.port.PredictionDataProvider.UpcomingPlanInfo;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WaterBalance;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WeatherContext;
import com.aquaai.prediction.domain.port.PredictionMethodVersionRepository;
import com.aquaai.prediction.domain.port.PredictionProvider;
import com.aquaai.prediction.domain.service.DataSufficiencyEvaluator;
import com.aquaai.prediction.domain.service.DataSufficiencyEvaluator.SufficiencyEvaluation;
import com.aquaai.prediction.domain.service.IrrigationNeedForecaster;
import com.aquaai.prediction.domain.service.IrrigationPlanRiskForecaster;
import com.aquaai.prediction.domain.service.SensorReliabilityForecaster;
import com.aquaai.prediction.domain.service.SoilMoistureForecaster;
import com.aquaai.prediction.domain.service.WaterAvailabilityForecaster;
import com.aquaai.prediction.domain.service.WaterDemandForecaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Authoritative deterministic and explainable predictive model provider.
 * <p>
 * Combines statistical trend regression, FAO-56 volumetric crop water requirement models,
 * water budget headroom comparisons, and hydraulic safety interlock enforcement.
 */
@Component
public class DeterministicPredictionProvider implements PredictionProvider {

    private final DataSufficiencyEvaluator sufficiencyEvaluator;
    private final SoilMoistureForecaster soilMoistureForecaster;
    private final IrrigationNeedForecaster irrigationNeedForecaster;
    private final WaterDemandForecaster waterDemandForecaster;
    private final WaterAvailabilityForecaster waterAvailabilityForecaster;
    private final IrrigationPlanRiskForecaster irrigationPlanRiskForecaster;
    private final SensorReliabilityForecaster sensorReliabilityForecaster;

    @Autowired(required = false)
    private PredictionMethodVersionRepository versionRepository;

    @Autowired(required = false)
    private SelectAdaptiveMethodUseCase adaptiveMethodSelector;

    @Autowired(required = false)
    private com.aquaai.prediction.domain.port.PredictionModelProvider mlModelProvider;

    private final com.aquaai.prediction.domain.service.HybridPredictionEngine hybridEngine =
            new com.aquaai.prediction.domain.service.HybridPredictionEngine();

    public DeterministicPredictionProvider() {
        this.sufficiencyEvaluator = new DataSufficiencyEvaluator();
        this.soilMoistureForecaster = new SoilMoistureForecaster();
        this.irrigationNeedForecaster = new IrrigationNeedForecaster();
        this.waterDemandForecaster = new WaterDemandForecaster();
        this.waterAvailabilityForecaster = new WaterAvailabilityForecaster();
        this.irrigationPlanRiskForecaster = new IrrigationPlanRiskForecaster();
        this.sensorReliabilityForecaster = new SensorReliabilityForecaster();
    }

    public DeterministicPredictionProvider(PredictionMethodVersionRepository versionRepository) {
        this();
        this.versionRepository = versionRepository;
    }

    public DeterministicPredictionProvider(PredictionMethodVersionRepository versionRepository,
                                           SelectAdaptiveMethodUseCase adaptiveMethodSelector) {
        this();
        this.versionRepository = versionRepository;
        this.adaptiveMethodSelector = adaptiveMethodSelector;
    }

    @Override
    public String getProviderName() {
        return "DeterministicStatisticalProvider";
    }

    @Override
    public List<Prediction> generateFieldPredictions(
            FieldMetadata field,
            PredictionHorizon horizon,
            PredictionDataProvider dataProvider
    ) {
        Instant now = Instant.now();
        Instant telemetrySince = now.minus(Duration.ofHours(72));
        Instant horizonTarget = now.plus(horizon.getDuration());
        Instant sensorHistorySince = now.minus(Duration.ofDays(14));

        // 1. Fetch contextual operational data from data provider
        List<TelemetryObservation> moistureObs = dataProvider.getRecentMoistureObservations(field.fieldId(), telemetrySince);
        WaterBalance waterBalance = dataProvider.getWaterBalance(field.fieldId()).orElse(null);
        List<UpcomingPlanInfo> upcomingPlans = dataProvider.getUpcomingPlans(field.fieldId(), horizonTarget);
        List<HydraulicAnomalyInfo> anomalies = dataProvider.getActiveHydraulicAnomalies(field.fieldId());
        List<SensorHealthSummary> sensorSummaries = dataProvider.getSensorHealthHistory(field.fieldId(), sensorHistorySince);
        WeatherContext weather = dataProvider.getWeatherContext(field.fieldId());

        // 2. Evaluate data sufficiency on soil moisture
        SufficiencyEvaluation sufficiency = sufficiencyEvaluator.evaluate(moistureObs, now);

        List<Prediction> predictions = new ArrayList<>();

        // 3. Soil Moisture Forecast
        Prediction soilMoisturePred = soilMoistureForecaster.forecast(
                field, horizon, moistureObs, weather, sufficiency, now
        );
        predictions.add(soilMoisturePred);

        // 4. Irrigation Need Forecast
        Prediction irrigationNeedPred = irrigationNeedForecaster.forecast(
                field, horizon, soilMoisturePred, weather, now
        );
        predictions.add(irrigationNeedPred);

        // 5. Water Demand Forecast
        Prediction waterDemandPred = waterDemandForecaster.forecast(
                field, horizon, irrigationNeedPred, upcomingPlans, now
        );
        predictions.add(waterDemandPred);

        // 6. Water Availability Risk
        Prediction waterRiskPred = waterAvailabilityForecaster.forecast(
                field, horizon, waterDemandPred, waterBalance, now
        );
        predictions.add(waterRiskPred);

        // 7. Irrigation Plan Risk (Safety Interlock Enforced)
        Prediction planRiskPred = irrigationPlanRiskForecaster.forecast(
                field, horizon, upcomingPlans, anomalies, waterBalance, sufficiency, now
        );
        predictions.add(planRiskPred);

        // 8. Sensor Reliability Forecast
        Prediction sensorReliabilityPred = sensorReliabilityForecaster.forecast(
                field, horizon, sensorSummaries, now
        );
        predictions.add(sensorReliabilityPred);

        // Apply governed method version tags and configuration snapshots (adaptive selection if enabled)
        if (adaptiveMethodSelector != null || versionRepository != null) {
            List<Prediction> versioned = new ArrayList<>(predictions.size());
            for (Prediction p : predictions) {
                boolean resolved = false;
                if (adaptiveMethodSelector != null) {
                    try {
                        var adaptiveResult = adaptiveMethodSelector.selectForPrediction(field.fieldId(), p.getPredictionType(), p.getId());
                        if (adaptiveResult != null && adaptiveResult.selectedVersionTag() != null) {
                            versioned.add(p.toBuilder()
                                    .methodVersionTag(adaptiveResult.selectedVersionTag())
                                    .configurationSnapshot(adaptiveResult.configurationSnapshot())
                                    .build());
                            resolved = true;
                        }
                    } catch (Exception ignored) {
                        // Safe fallback to baseline version
                    }
                }
                if (!resolved && versionRepository != null) {
                    var activeOpt = versionRepository.findActiveByPredictionType(p.getPredictionType());
                    if (activeOpt.isPresent()) {
                        var ver = activeOpt.get();
                        versioned.add(p.toBuilder()
                                .methodVersionTag(ver.getVersionTag())
                                .configurationSnapshot(ver.getConfigurationJson())
                                .build());
                        resolved = true;
                    }
                }
                if (!resolved) {
                    versioned.add(p);
                }
            }

            // Milestone 08.11: Hybrid ML Residual Adjustment for SOIL_MOISTURE_FORECAST
            List<Prediction> finalPredictions = new ArrayList<>(versioned.size());
            boolean criticalSafetyActive = (anomalies != null && anomalies.stream().anyMatch(a -> "CRITICAL".equalsIgnoreCase(a.severity())));

            for (Prediction p : versioned) {
                if (p.getPredictionType() == PredictionType.SOIL_MOISTURE_FORECAST
                        && mlModelProvider != null
                        && p.getConfigurationSnapshot() != null
                        && p.getConfigurationSnapshot().contains("RIDGE_REGRESSION")) {

                    double baselineVal = p.getPredictedValue() != null ? p.getPredictedValue().doubleValue() : 25.0;
                    double currentVal = !moistureObs.isEmpty() ? moistureObs.get(moistureObs.size() - 1).value().doubleValue() : baselineVal;
                    double slope = -0.15;
                    if (moistureObs.size() >= 2) {
                        double firstSec = moistureObs.get(0).measuredAt().getEpochSecond();
                        double sumX = 0, sumY = 0, sumXX = 0, sumXY = 0;
                        int n = moistureObs.size();
                        for (TelemetryObservation o : moistureObs) {
                            double xH = (o.measuredAt().getEpochSecond() - firstSec) / 3600.0;
                            double yV = o.value().doubleValue();
                            sumX += xH; sumY += yV; sumXX += xH * xH; sumXY += xH * yV;
                        }
                        double denom = (n * sumXX - sumX * sumX);
                        if (Math.abs(denom) > 1e-6) slope = (n * sumXY - sumX * sumY) / denom;
                    }

                    double recentIrrig = 0.0;
                    double headroom = 100.0;
                    if (waterBalance != null && waterBalance.allocatedVolumeM3() != null && waterBalance.allocatedVolumeM3().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal consumed = waterBalance.consumedVolumeM3() != null ? waterBalance.consumedVolumeM3() : BigDecimal.ZERO;
                        headroom = waterBalance.allocatedVolumeM3().subtract(consumed).max(BigDecimal.ZERO).doubleValue() / waterBalance.allocatedVolumeM3().doubleValue() * 100.0;
                    }

                    double qualityScore = sufficiency.sufficiency() == com.aquaai.prediction.domain.model.DataSufficiency.SUFFICIENT ? 1.0 : 0.7;

                    var mlPredOpt = mlModelProvider.predictSoilMoisture(
                            field, horizon, currentVal, slope, weather, recentIrrig, headroom, qualityScore, p.getConfigurationSnapshot()
                    );

                    var hybridRes = hybridEngine.computeHybridPrediction(baselineVal, mlPredOpt.orElse(null), 5.0, criticalSafetyActive);

                    if (!hybridRes.fallbackUsed()) {
                        finalPredictions.add(p.toBuilder()
                                .predictedValue(hybridRes.toBigDecimalFinal())
                                .evidence(p.getEvidence() + " | Hybrid ML residual: " + hybridRes.rationale())
                                .build());
                    } else {
                        finalPredictions.add(p);
                    }
                } else {
                    finalPredictions.add(p);
                }
            }

            return finalPredictions;
        }

        return predictions;
    }

    @Override
    public Prediction generateSpecificPrediction(
            FieldMetadata field,
            PredictionType type,
            PredictionHorizon horizon,
            PredictionDataProvider dataProvider
    ) {
        List<Prediction> all = generateFieldPredictions(field, horizon, dataProvider);
        return all.stream()
                .filter(p -> p.getPredictionType() == type)
                .findFirst()
                .orElseGet(() -> Prediction.createUnavailable(field.fieldId(), type, horizon, "Prediction type not supported"));
    }
}
