package com.aquaai.performance.application.usecase;

import com.aquaai.performance.application.dto.CreateBaselineCommand;
import com.aquaai.performance.domain.model.PerformanceBaseline;
import com.aquaai.performance.domain.repository.PerformanceBaselineRepository;
import com.aquaai.performance.domain.repository.PerformanceIndicatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatePerformanceBaselineUseCase {

    private final PerformanceBaselineRepository baselineRepository;
    private final PerformanceIndicatorRepository indicatorRepository;

    @Transactional
    public PerformanceBaseline execute(CreateBaselineCommand command) {
        if (!indicatorRepository.existsByCode(command.getIndicatorCode())) {
            throw new NoSuchElementException("Indicator not found for code: " + command.getIndicatorCode());
        }

        Instant now = Instant.now();
        PerformanceBaseline baseline = PerformanceBaseline.builder()
                .id(UUID.randomUUID())
                .indicatorCode(command.getIndicatorCode())
                .name(command.getName())
                .description(command.getDescription())
                .scopeType(command.getScopeType())
                .scopeId(command.getScopeId())
                .scopeName(command.getScopeName())
                .periodStart(command.getPeriodStart())
                .periodEnd(command.getPeriodEnd())
                .baselineValue(command.getBaselineValue())
                .measurementUnit(command.getMeasurementUnit())
                .dataCompletenessPct(command.getDataCompletenessPct())
                .evidenceClassification(command.getEvidenceClassification())
                .baselineQuality(command.getBaselineQuality())
                .calculationMethod(command.getCalculationMethod())
                .seasonContext(command.getSeasonContext())
                .approvalStatus("APPROVED")
                .approvedBy(command.getApprovedBy())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return baselineRepository.save(baseline);
    }
}
