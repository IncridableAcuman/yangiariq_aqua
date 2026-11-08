package com.aquaai.performance.application.usecase;

import com.aquaai.performance.domain.model.PerformanceIndicator;
import com.aquaai.performance.domain.repository.PerformanceIndicatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPerformanceIndicatorDetailUseCase {

    private final PerformanceIndicatorRepository indicatorRepository;

    @Transactional(readOnly = true)
    public PerformanceIndicator execute(String codeOrId) {
        try {
            UUID id = UUID.fromString(codeOrId);
            Optional<PerformanceIndicator> byId = indicatorRepository.findById(id);
            if (byId.isPresent()) return byId.get();
        } catch (IllegalArgumentException ignored) {
            // Not a UUID, treat as code
        }

        return indicatorRepository.findByCode(codeOrId)
                .orElseThrow(() -> new NoSuchElementException("Performance Indicator not found: " + codeOrId));
    }
}
