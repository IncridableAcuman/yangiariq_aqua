package com.aquaai.analytics.application.service;

import com.aquaai.analytics.api.dto.FarmInsightDto;
import com.aquaai.analytics.api.dto.FieldPerformanceDto;
import com.aquaai.analytics.domain.model.InsightCategory;
import com.aquaai.analytics.domain.model.InsightSeverity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Deterministic agricultural insight generation engine.
 * Converts historical metrics into actionable operational next-steps without fake AI aesthetics.
 */
@Service
public class FarmInsightEngine {

    public List<FarmInsightDto> generateInsights(List<FieldPerformanceDto> fields) {
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }

        List<FarmInsightDto> insights = new ArrayList<>();

        for (FieldPerformanceDto f : fields) {
            // Rule 1: Repeated Safety Incidents (CRITICAL)
            if (f.getCriticalAnomalyCount() >= 2) {
                insights.add(FarmInsightDto.builder()
                        .id("ins-safety-" + f.getFieldId())
                        .category(InsightCategory.SAFETY)
                        .severity(InsightSeverity.CRITICAL)
                        .fieldId(f.getFieldId())
                        .fieldName(f.getFieldName())
                        .title("Takroriy gidravlik xavf hodisalari (Repeated Safety Incidents)")
                        .explanation("Ushbu dalada tanlangan davr davomida takroriy kritik gidravlik anomaliyalar aniqlangan. Quvur yorilishi yoki bosim tushishi xavfi mavjud.")
                        .evidence(List.of(
                                "Kritik anomaliyalar soni: " + f.getCriticalAnomalyCount(),
                                "Jami xavfsizlik hodisalari: " + f.getSafetyEventCount(),
                                "Gidro-xavfsizlik holati: ATTENTION"
                        ))
                        .recommendedAction("Xavfni tekshirish")
                        .targetSection("anomalies")
                        .targetPayload(Map.of("fieldId", f.getFieldId().toString()))
                        .createdAt(Instant.now())
                        .build());
            }

            // Rule 2: Water Over-Consumption (HIGH)
            if (f.getUtilizationPercent() != null && f.getUtilizationPercent().compareTo(BigDecimal.valueOf(100.0)) > 0) {
                BigDecimal excessM3 = f.getConsumedM3().subtract(f.getAllocatedM3());
                insights.add(FarmInsightDto.builder()
                        .id("ins-water-over-" + f.getFieldId())
                        .category(InsightCategory.WATER)
                        .severity(InsightSeverity.HIGH)
                        .fieldId(f.getFieldId())
                        .fieldName(f.getFieldName())
                        .title("Mavsumiy suv limiti oshirilgan (Water Over-Consumption)")
                        .explanation("Fermer xoʻjaligi tomonidan ushbu dala uchun belgilangan mavsumiy suv limiti toʻliq tugatilgan va ortiqcha suv sarflangan.")
                        .evidence(List.of(
                                "Ajratilgan limit: " + Math.round(f.getAllocatedM3().doubleValue()) + " m³",
                                "Amalda sarflangan: " + Math.round(f.getConsumedM3().doubleValue()) + " m³",
                                "Ortiqcha sarf: +" + Math.round(excessM3.doubleValue()) + " m³ (" + f.getUtilizationPercent() + "%)"
                        ))
                        .recommendedAction("Suv kvotasini qayta koʻrib chiqish")
                        .targetSection("water")
                        .targetPayload(Map.of("fieldId", f.getFieldId().toString()))
                        .createdAt(Instant.now())
                        .build());
            }

            // Rule 3: Low Telemetry Quality / Coverage (WARNING)
            if (f.getTelemetryCoveragePercent() != null && f.getTelemetryCoveragePercent().compareTo(BigDecimal.valueOf(70.0)) < 0) {
                insights.add(FarmInsightDto.builder()
                        .id("ins-telemetry-low-" + f.getFieldId())
                        .category(InsightCategory.TELEMETRY)
                        .severity(InsightSeverity.WARNING)
                        .fieldId(f.getFieldId())
                        .fieldName(f.getFieldName())
                        .title("Datchiklar telemetriya qamrovi past (Low Telemetry Coverage)")
                        .explanation("Dala datchiklaridan olinayotgan maʼlumotlar hajmi ishonchli agronomik tahlil uchun yetarli emas. Datchiklar signal uzatmayotgan yoki eskirgan boʻlishi mumkin.")
                        .evidence(List.of(
                                "Telemetriya qamrovi: " + f.getTelemetryCoveragePercent() + "% (< 70% ostonasi)",
                                "Datchiklar holati: " + f.getSensorHealth()
                        ))
                        .recommendedAction("Datchiklar holatini tekshirish")
                        .targetSection("sensors")
                        .targetPayload(Map.of("fieldId", f.getFieldId().toString()))
                        .createdAt(Instant.now())
                        .build());
            }

            // Rule 4: Verification Gap (WARNING)
            if (f.getCompletedOperationsCount() > 0 &&
                    f.getVerificationCoveragePercent() != null &&
                    f.getVerificationCoveragePercent().compareTo(BigDecimal.valueOf(75.0)) < 0) {
                insights.add(FarmInsightDto.builder()
                        .id("ins-verification-gap-" + f.getFieldId())
                        .category(InsightCategory.VERIFICATION)
                        .severity(InsightSeverity.WARNING)
                        .fieldId(f.getFieldId())
                        .fieldName(f.getFieldName())
                        .title("Sugʻorish natijalarini tekshirishda uzilish (Verification Gap)")
                        .explanation("Bajarilgan sugʻorish amaliyotlarining 25% dan ortigʻi post-operatsion tuproq namligi qayd etilmaganligi sababli tekshirilmagan.")
                        .evidence(List.of(
                                "Bajarilgan operatsiyalar: " + f.getCompletedOperationsCount(),
                                "Natijasi tekshirilgan qamrov: " + f.getVerificationCoveragePercent() + "%",
                                "Tekshirilmagan operatsiyalar mavjud"
                        ))
                        .recommendedAction("Operatsiya natijalarini tasdiqlash")
                        .targetSection("operations")
                        .targetPayload(Map.of("fieldId", f.getFieldId().toString()))
                        .createdAt(Instant.now())
                        .build());
            }

            // Rule 5: Low Plan Adherence (WARNING)
            if (f.getCompletedOperationsCount() > 0 &&
                    f.getPlanAdherencePercent() != null &&
                    f.getPlanAdherencePercent().compareTo(BigDecimal.valueOf(70.0)) < 0) {
                insights.add(FarmInsightDto.builder()
                        .id("ins-adherence-low-" + f.getFieldId())
                        .category(InsightCategory.IRRIGATION)
                        .severity(InsightSeverity.WARNING)
                        .fieldId(f.getFieldId())
                        .fieldName(f.getFieldName())
                        .title("Sugʻorish rejasiga rioya qilish past (Low Plan Adherence)")
                        .explanation("Rejalashtirilgan va amaldagi sugʻorish parametrlari oʻrtasida tafovut yuqori. Hajm yoki vaqt chegaralari buzilgan boʻlishi mumkin.")
                        .evidence(List.of(
                                "Rejaga rioya koʻrsatkichi: " + f.getPlanAdherencePercent() + "% (< 70% ostonasi)",
                                "Bajarilgan amallar soni: " + f.getCompletedOperationsCount()
                        ))
                        .recommendedAction("Operatsiyalar markazini koʻrish")
                        .targetSection("operations")
                        .targetPayload(Map.of("fieldId", f.getFieldId().toString()))
                        .createdAt(Instant.now())
                        .build());
            }
        }

        // Sort strictly by severity hierarchy: CRITICAL -> HIGH -> WARNING -> INFO
        insights.sort(Comparator.comparing(FarmInsightDto::getSeverity, Comparator.comparingInt(this::getSeverityWeight)));

        return insights;
    }

    private int getSeverityWeight(InsightSeverity severity) {
        return switch (severity) {
            case CRITICAL -> 1;
            case HIGH -> 2;
            case WARNING -> 3;
            case INFO -> 4;
        };
    }
}
