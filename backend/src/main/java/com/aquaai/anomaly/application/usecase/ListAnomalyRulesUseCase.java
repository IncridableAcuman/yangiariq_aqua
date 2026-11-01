package com.aquaai.anomaly.application.usecase;

import com.aquaai.anomaly.domain.model.AnomalyRuleConfig;
import com.aquaai.anomaly.domain.repository.AnomalyRuleConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAnomalyRulesUseCase {

    private final AnomalyRuleConfigRepository repository;

    @Transactional(readOnly = true)
    public List<AnomalyRuleConfig> execute() {
        return repository.findAll();
    }
}
