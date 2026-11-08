package com.aquaai.performance.application.usecase;

import com.aquaai.performance.application.dto.CreateTargetCommand;
import com.aquaai.performance.domain.model.PerformanceTarget;
import com.aquaai.performance.domain.model.TargetStatus;
import com.aquaai.performance.domain.repository.PerformanceIndicatorRepository;
import com.aquaai.performance.domain.repository.PerformanceTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatePerformanceTargetUseCase {

    private final PerformanceTargetRepository targetRepository;
    private final PerformanceIndicatorRepository indicatorRepository;

    @Transactional
    public PerformanceTarget execute(CreateTargetCommand command) {
        if (!indicatorRepository.existsByCode(command.getIndicatorCode())) {
            throw new NoSuchElementException("Indicator not found for code: " + command.getIndicatorCode());
        }

        Instant now = Instant.now();
        PerformanceTarget target = PerformanceTarget.builder()
                .id(UUID.randomUUID())
                .indicatorCode(command.getIndicatorCode())
                .title(command.getTitle())
                .scopeType(command.getScopeType())
                .scopeId(command.getScopeId())
                .scopeName(command.getScopeName())
                .targetType(command.getTargetType())
                .targetCategory(command.getTargetCategory())
                .targetValue(command.getTargetValue())
                .rangeMin(command.getRangeMin())
                .rangeMax(command.getRangeMax())
                .measurementUnit(command.getMeasurementUnit())
                .effectiveStart(command.getEffectiveStart())
                .effectiveEnd(command.getEffectiveEnd())
                .targetOwner(command.getTargetOwner())
                .status(TargetStatus.APPROVED)
                .rationale(command.getRationale())
                .targetVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build();

        return targetRepository.save(target);
    }
}
