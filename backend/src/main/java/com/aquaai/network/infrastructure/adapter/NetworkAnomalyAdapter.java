package com.aquaai.network.infrastructure.adapter;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.network.domain.port.NetworkAnomalyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NetworkAnomalyAdapter implements NetworkAnomalyPort {

    private final AnomalyRepository anomalyRepository;

    @Override
    public boolean hasSuspectedPipeBurst(UUID fieldId) {
        if (fieldId == null) return false;
        List<Anomaly> list = anomalyRepository.findAll(
                fieldId, null, AnomalyStatus.DETECTED, null, AnomalyType.SUSPECTED_PIPE_BURST, null, null, null, 0, 5
        );
        return list != null && !list.isEmpty();
    }

    @Override
    public boolean hasCriticalPressureDrop(UUID fieldId) {
        if (fieldId == null) return false;
        List<Anomaly> list = anomalyRepository.findAll(
                fieldId, null, AnomalyStatus.DETECTED, null, AnomalyType.WATER_PRESSURE_DROP, null, null, null, 0, 5
        );
        return list != null && !list.isEmpty();
    }
}
