package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.ResilienceScenarioResponse;
import com.aquaai.network.application.service.WaterNetworkDtoMapper;
import com.aquaai.network.domain.model.ResilienceScenario;
import com.aquaai.network.domain.repository.ResilienceScenarioRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetResilienceScenarioDetailUseCase {

    private final ResilienceScenarioRepository scenarioRepository;
    private final WaterNetworkDtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public ResilienceScenarioResponse execute(UUID id) {
        ResilienceScenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resilience scenario", id));
        return dtoMapper.toScenarioResponse(scenario);
    }
}
