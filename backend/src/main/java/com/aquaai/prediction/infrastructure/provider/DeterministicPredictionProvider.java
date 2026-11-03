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
            return versioned;
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
