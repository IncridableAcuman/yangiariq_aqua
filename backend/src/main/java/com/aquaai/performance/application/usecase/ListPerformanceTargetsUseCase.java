package com.aquaai.performance.application.usecase;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.PerformanceTarget;
import com.aquaai.performance.domain.repository.PerformanceTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListPerformanceTargetsUseCase {

    private final PerformanceTargetRepository targetRepository;

    @Transactional(readOnly = true)
    public List<PerformanceTarget> execute(String indicatorCode, GeographicScopeType scopeType) {
        if (indicatorCode != null && scopeType != null) {
            return targetRepository.findByIndicatorCodeAndScopeType(indicatorCode, scopeType);
        }
        if (indicatorCode != null) {
            return targetRepository.findByIndicatorCode(indicatorCode);
        }
        return targetRepository.findAll();
    }
}
