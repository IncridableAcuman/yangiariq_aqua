package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.PredictionContextDto;
import com.aquaai.prediction.domain.model.PredictionContext;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.HydraulicAnomalyInfo;
import com.aquaai.prediction.domain.port.PredictionDataProvider.SensorHealthSummary;
import com.aquaai.prediction.domain.port.PredictionDataProvider.TelemetryObservation;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WaterBalance;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WeatherContext;
import com.aquaai.prediction.domain.service.ContextNormalizer;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFieldPredictionContextUseCase {

    private final PredictionDataProvider dataProvider;
    private final ContextNormalizer normalizer = new ContextNormalizer();

    @Transactional(readOnly = true)
    public PredictionContextDto execute(UUID fieldId) {
        FieldMetadata field = dataProvider.getFieldMetadata(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with id: " + fieldId));

        PredictionContext domainContext = resolveDomainContext(field);
        return PredictionContextDto.fromDomain(domainContext);
    }

    public PredictionContext resolveDomainContext(FieldMetadata field) {
        Instant now = Instant.now();
        Instant telemetrySince = now.minus(Duration.ofHours(72));
        Instant sensorHistorySince = now.minus(Duration.ofDays(14));

        List<TelemetryObservation> moistureObs = dataProvider.getRecentMoistureObservations(field.fieldId(), telemetrySince);
        WaterBalance waterBalance = dataProvider.getWaterBalance(field.fieldId()).orElse(null);
        List<HydraulicAnomalyInfo> anomalies = dataProvider.getActiveHydraulicAnomalies(field.fieldId());
        List<SensorHealthSummary> sensorSummaries = dataProvider.getSensorHealthHistory(field.fieldId(), sensorHistorySince);
        WeatherContext weather = dataProvider.getWeatherContext(field.fieldId());

        return normalizer.normalize(
                field, moistureObs, sensorSummaries, waterBalance, anomalies, weather, now
        );
    }
}
