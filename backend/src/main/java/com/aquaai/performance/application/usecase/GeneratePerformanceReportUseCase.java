package com.aquaai.performance.application.usecase;

import com.aquaai.performance.application.dto.PerformanceOverviewDto;
import com.aquaai.performance.application.dto.PerformanceReportDto;
import com.aquaai.performance.domain.repository.ImpactAssessmentRepository;
import com.aquaai.performance.domain.repository.PerformanceEvaluationRepository;
import com.aquaai.performance.domain.repository.PerformanceIndicatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GeneratePerformanceReportUseCase {

    private final GetPerformanceOverviewUseCase overviewUseCase;
    private final PerformanceIndicatorRepository indicatorRepository;
    private final PerformanceEvaluationRepository evaluationRepository;
    private final ImpactAssessmentRepository impactAssessmentRepository;

    @Transactional(readOnly = true)
    public PerformanceReportDto execute(Instant periodStart, Instant periodEnd, String scope) {
        Instant pStart = periodStart != null ? periodStart : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant pEnd = periodEnd != null ? periodEnd : Instant.now();
        String targetScope = scope != null ? scope : "Yangiariq Tumani (Regional District)";

        PerformanceOverviewDto overview = overviewUseCase.execute();
        var indicators = indicatorRepository.findAll();
        var evaluations = evaluationRepository.findAll();
        var impacts = impactAssessmentRepository.findAll();

        return PerformanceReportDto.builder()
                .reportId("REP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .reportTitle("Yangiariq Suv Resurslari Operatsiyalari Samaradorligi va Taʼsir Baholash Hisoboti")
                .generatedAt(Instant.now())
                .generatedBy("YANGIARIQ AQUA AI Performance Intelligence Engine")
                .reportingPeriodStart(pStart)
                .reportingPeriodEnd(pEnd)
                .geographicScope(targetScope)
                .legalDisclaimer("Ushbu hisobot operatsion monitoring va qiyosiy taʼsir baholash uchun moʻljallangan. Bu rasmiy davlat sertifikati yoki yuridik akkreditatsiya hujjati hisoblanmaydi.")
                .overview(overview)
                .indicators(indicators)
                .evaluations(evaluations)
                .impactAssessments(impacts)
                .build();
    }
}
