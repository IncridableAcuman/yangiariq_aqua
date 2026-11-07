package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.ResilienceScenarioResponse;
import com.aquaai.network.application.service.WaterNetworkDtoMapper;
import com.aquaai.network.domain.model.ResilienceScenario;
import com.aquaai.network.domain.repository.ResilienceScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListResilienceScenariosUseCase {

    private final ResilienceScenarioRepository scenarioRepository;
    private final WaterNetworkDtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public List<ResilienceScenarioResponse> execute() {
        return scenarioRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(dtoMapper::toScenarioResponse)
                .collect(Collectors.toList());
    }
}
