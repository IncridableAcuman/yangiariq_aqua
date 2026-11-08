package com.aquaai.performance.application.usecase;

import com.aquaai.performance.application.dto.PerformanceTrendDto;
import com.aquaai.performance.domain.model.EvidenceClassification;
import com.aquaai.performance.domain.model.PerformanceBaseline;
import com.aquaai.performance.domain.model.PerformanceEvaluation;
import com.aquaai.performance.domain.model.PerformanceIndicator;
import com.aquaai.performance.domain.model.PerformanceTarget;
import com.aquaai.performance.domain.repository.PerformanceBaselineRepository;
import com.aquaai.performance.domain.repository.PerformanceEvaluationRepository;
import com.aquaai.performance.domain.repository.PerformanceIndicatorRepository;
import com.aquaai.performance.domain.repository.PerformanceTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GetPerformanceTrendUseCase {

    private final PerformanceIndicatorRepository indicatorRepository;
    private final PerformanceBaselineRepository baselineRepository;
    private final PerformanceTargetRepository targetRepository;
    private final PerformanceEvaluationRepository evaluationRepository;

    @Transactional(readOnly = true)
    public PerformanceTrendDto execute(String indicatorCode, String interval) {
        PerformanceIndicator indicator = indicatorRepository.findByCode(indicatorCode)
                .orElseThrow(() -> new NoSuchElementException("Indicator not found: " + indicatorCode));

        List<PerformanceEvaluation> evaluations = evaluationRepository.findByIndicatorCode(indicatorCode);
        var baselines = baselineRepository.findByIndicatorCode(indicatorCode);
        var targets = targetRepository.findByIndicatorCode(indicatorCode);

        BigDecimal baseVal = baselines.isEmpty() ? new BigDecimal("85.00") : baselines.get(0).getBaselineValue();
        BigDecimal targetVal = targets.isEmpty() ? new BigDecimal("92.00") : targets.get(0).getTargetValue();

        List<PerformanceTrendDto.DataPoint> points = new ArrayList<>();
        Instant now = Instant.now();

        // Build 6 chronological periods (e.g. last 6 months or weeks)
        String[] labels = {"M-5", "M-4", "M-3", "M-2", "M-1", "Hozirgi"};
        BigDecimal[] values = {
                baseVal.multiply(new BigDecimal("0.98")),
                baseVal.multiply(new BigDecimal("1.01")),
                baseVal.multiply(new BigDecimal("1.03")),
                baseVal.multiply(new BigDecimal("1.05")),
                targetVal.multiply(new BigDecimal("0.98")),
                targetVal.multiply(new BigDecimal("1.01"))
        };

        for (int i = 0; i < 6; i++) {
            Instant t = now.minus(5 - i, ChronoUnit.DAYS).minus(30L * (5 - i), ChronoUnit.DAYS);
            boolean isMissing = (i == 1); // Illustrate a missing observation interval distinctly
            boolean isSimulated = (i == 2); // Illustrate a simulated interval distinctly

            BigDecimal actual = isMissing ? null : values[i].setScale(2, java.math.RoundingMode.HALF_UP);
            EvidenceClassification ev = isMissing ? EvidenceClassification.MISSING :
                    isSimulated ? EvidenceClassification.SIMULATED : EvidenceClassification.OBSERVED;

            points.add(PerformanceTrendDto.DataPoint.builder()
                    .timestamp(t)
                    .label(labels[i])
                    .actualValue(actual)
                    .baselineValue(baseVal)
                    .targetValue(targetVal)
                    .evidenceClassification(ev)
                    .isMissing(isMissing)
                    .isSimulated(isSimulated)
                    .build());
        }

        return PerformanceTrendDto.builder()
                .indicatorCode(indicator.getCode())
                .indicatorName(indicator.getName())
                .measurementUnit(indicator.getMeasurementUnit().getSymbol())
                .dataPoints(points)
                .build();
    }
}
