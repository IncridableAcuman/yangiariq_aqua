package com.aquaai.risk.infrastructure.adapter;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.risk.domain.port.RiskAnomalyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RiskAnomalyAdapter implements RiskAnomalyPort {

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

    @Override
    public int countActiveAnomalies(UUID fieldId) {
        if (fieldId == null) return 0;
        List<Anomaly> list = anomalyRepository.findAll(
                fieldId, null, AnomalyStatus.DETECTED, null, null, null, null, null, 0, 50
        );
        return list != null ? list.size() : 0;
    }
}
