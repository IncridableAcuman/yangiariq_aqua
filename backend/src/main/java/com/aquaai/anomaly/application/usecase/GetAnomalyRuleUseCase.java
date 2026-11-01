package com.aquaai.anomaly.application.usecase;

import com.aquaai.anomaly.domain.model.AnomalyRuleConfig;
import com.aquaai.anomaly.domain.repository.AnomalyRuleConfigRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAnomalyRuleUseCase {

    private final AnomalyRuleConfigRepository repository;

    @Transactional(readOnly = true)
    public AnomalyRuleConfig execute(UUID id) {
        if (id == null) {
            throw new ResourceNotFoundException("AnomalyRuleConfig", "null");
        }
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AnomalyRuleConfig", id));
    }
}
