package com.aquaai.performance.application.usecase;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.PerformanceBaseline;
import com.aquaai.performance.domain.repository.PerformanceBaselineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListPerformanceBaselinesUseCase {

    private final PerformanceBaselineRepository baselineRepository;

    @Transactional(readOnly = true)
    public List<PerformanceBaseline> execute(String indicatorCode, GeographicScopeType scopeType) {
        if (indicatorCode != null && scopeType != null) {
            return baselineRepository.findByIndicatorCodeAndScopeType(indicatorCode, scopeType);
        }
        if (indicatorCode != null) {
            return baselineRepository.findByIndicatorCode(indicatorCode);
        }
        return baselineRepository.findAll();
    }
}
