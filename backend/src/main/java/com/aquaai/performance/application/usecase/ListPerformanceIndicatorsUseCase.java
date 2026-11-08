package com.aquaai.performance.application.usecase;

import com.aquaai.performance.domain.model.IndicatorCategory;
import com.aquaai.performance.domain.model.PerformanceIndicator;
import com.aquaai.performance.domain.repository.PerformanceIndicatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListPerformanceIndicatorsUseCase {

    private final PerformanceIndicatorRepository indicatorRepository;

    @Transactional(readOnly = true)
    public List<PerformanceIndicator> execute(IndicatorCategory category) {
        List<PerformanceIndicator> all = indicatorRepository.findAll();
        if (category == null) {
            return all;
        }
        return all.stream()
                .filter(i -> i.getCategory() == category)
                .collect(Collectors.toList());
    }
}
