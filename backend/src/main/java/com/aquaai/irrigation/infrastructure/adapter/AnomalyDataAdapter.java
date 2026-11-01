package com.aquaai.irrigation.infrastructure.adapter;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.irrigation.domain.port.AnomalyDataPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnomalyDataAdapter implements AnomalyDataPort {

    private final AnomalyRepository anomalyRepository;

    @Override
    public ParcelAnomalyStatus getAnomalyStatus(UUID fieldId) {
        if (fieldId == null) {
            return new ParcelAnomalyStatus(false, false, false, false);
        }

        List<Anomaly> anomalies = anomalyRepository.findAll(
                fieldId,
                null,
                AnomalyStatus.DETECTED,
                null,
                null,
                null,
                null,
                null,
                0,
                50
        );

        boolean hasPipeBurst = false;
        boolean hasPressureDrop = false;
        boolean hasMoistureCritical = false;
        boolean hasSensorDrift = false;

        for (Anomaly a : anomalies) {
            if (a.getAnomalyType() == AnomalyType.SUSPECTED_PIPE_BURST) {
                hasPipeBurst = true;
            } else if (a.getAnomalyType() == AnomalyType.WATER_PRESSURE_DROP) {
                hasPressureDrop = true;
            } else if (a.getAnomalyType() == AnomalyType.SOIL_MOISTURE_CRITICAL) {
                hasMoistureCritical = true;
            } else if (a.getAnomalyType() == AnomalyType.POSSIBLE_SENSOR_DRIFT) {
                hasSensorDrift = true;
            }
        }

        return new ParcelAnomalyStatus(
                hasPipeBurst,
                hasPressureDrop,
                hasMoistureCritical,
                hasSensorDrift
        );
    }
}
