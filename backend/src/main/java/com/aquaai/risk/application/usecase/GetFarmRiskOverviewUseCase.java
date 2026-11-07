package com.aquaai.risk.application.usecase;

import com.aquaai.risk.api.dto.FarmRiskOverviewResponse;
import com.aquaai.risk.api.dto.FieldRiskProfileResponse;
import com.aquaai.risk.api.dto.RiskRecommendationResponse;
import com.aquaai.risk.application.service.RiskAssessmentCoordinatorService;
import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskCategory;
import com.aquaai.risk.domain.model.RiskConfidence;
import com.aquaai.risk.domain.model.RiskRecommendation;
import com.aquaai.risk.domain.model.RiskSeverity;
import com.aquaai.risk.domain.port.RiskFieldPort;
import com.aquaai.risk.domain.repository.RiskAssessmentRepository;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFarmRiskOverviewUseCase {

    private final RiskFieldPort fieldPort;
    private final RiskAssessmentRepository assessmentRepository;
    private final RiskAssessmentCoordinatorService coordinatorService;
    private final CurrentUserProvider currentUserProvider;

    public FarmRiskOverviewResponse execute() {
        UUID currentUserId = currentUserProvider.getCurrentUserId().orElse(null);
        UserRole currentRole = currentUserProvider.getCurrentUserRole().orElse(UserRole.FARMER);
        boolean isDistrictScope = (currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR);

        List<RiskFieldPort.FieldInfo> fields = fieldPort.getAllAccessibleFieldInfos(currentUserId, isDistrictScope);
        if (fields.isEmpty()) {
            return FarmRiskOverviewResponse.builder()
                    .totalAssessedFields(0)
                    .criticalRiskCount(0)
                    .highRiskCount(0)
                    .mediumRiskCount(0)
                    .lowRiskCount(0)
                    .insufficientDataCount(0)
                    .hydraulicEmergencyCount(0)
                    .waterShortageCount(0)
                    .severeMoistureRiskCount(0)
                    .telemetryIssuesCount(0)
                    .modelReliabilityWarningsCount(0)
                    .activeRecommendationsCount(0)
                    .categoryDistribution(Collections.emptyMap())
                    .criticalEmergencyFields(Collections.emptyList())
                    .topPrioritizedRecommendations(Collections.emptyList())
                    .evaluatedAt(Instant.now())
                    .build();
        }

        List<UUID> fieldIds = fields.stream().map(RiskFieldPort.FieldInfo::id).toList();
        List<FarmRiskAssessment> latestAssessments = assessmentRepository.findAllLatestByFieldIds(fieldIds);

        Map<UUID, FarmRiskAssessment> assessmentMap = new java.util.HashMap<>();
        for (FarmRiskAssessment a : latestAssessments) {
            assessmentMap.put(a.getFieldId(), a);
        }

        Instant now = Instant.now();
        List<FarmRiskAssessment> activeAssessments = new ArrayList<>();
        Map<UUID, RiskFieldPort.FieldInfo> fieldInfoMap = new java.util.HashMap<>();
        for (RiskFieldPort.FieldInfo f : fields) {
            fieldInfoMap.put(f.id(), f);
            FarmRiskAssessment a = assessmentMap.get(f.id());
            // If missing or older than 30 minutes, evaluate fresh
            if (a == null || Duration.between(a.getEvaluatedAt(), now).toMinutes() >= 30) {
                try {
                    a = coordinatorService.evaluateAndPersistFieldRisk(f.id());
                } catch (Exception ignored) {}
            }
            if (a != null) {
                activeAssessments.add(a);
            }
        }

        int critical = 0;
        int high = 0;
        int medium = 0;
        int low = 0;
        int insufficientData = 0;
        int hydraulicEmergency = 0;
        int waterShortage = 0;
        int severeMoisture = 0;
        int telemetryIssues = 0;
        int modelWarnings = 0;

        Map<RiskCategory, Integer> categoryDistribution = new EnumMap<>(RiskCategory.class);
        for (RiskCategory cat : RiskCategory.values()) {
            categoryDistribution.put(cat, 0);
        }

        List<FieldRiskProfileResponse> criticalEmergencyFields = new ArrayList<>();
        List<RiskRecommendation> allRecommendations = new ArrayList<>();

        for (FarmRiskAssessment a : activeAssessments) {
            if (a.getOverallSeverity() == RiskSeverity.CRITICAL) critical++;
            else if (a.getOverallSeverity() == RiskSeverity.HIGH) high++;
            else if (a.getOverallSeverity() == RiskSeverity.MEDIUM) medium++;
            else if (a.getOverallSeverity() == RiskSeverity.LOW) low++;

            if (a.getOverallConfidence() == RiskConfidence.INSUFFICIENT_DATA) {
                insufficientData++;
            }

            if (a.isHydraulicBlocked()) {
                hydraulicEmergency++;
            }

            if (a.getPrimaryRiskCategory() != null) {
                categoryDistribution.put(a.getPrimaryRiskCategory(),
                        categoryDistribution.getOrDefault(a.getPrimaryRiskCategory(), 0) + 1);
            }

            // Inspect factors
            for (var f : a.getRiskFactors()) {
                if (f.category() == RiskCategory.WATER_AVAILABILITY && f.severity() != RiskSeverity.LOW) waterShortage++;
                if (f.category() == RiskCategory.SOIL_MOISTURE && f.severity() != RiskSeverity.LOW) severeMoisture++;
                if (f.category() == RiskCategory.SENSOR_DATA_RELIABILITY && f.severity() != RiskSeverity.LOW) telemetryIssues++;
                if (f.category() == RiskCategory.PREDICTIVE_MODEL_RELIABILITY && f.severity() != RiskSeverity.LOW) modelWarnings++;
            }

            RiskFieldPort.FieldInfo fInfo = fieldInfoMap.get(a.getFieldId());
            if (a.getOverallSeverity() == RiskSeverity.CRITICAL || a.isHydraulicBlocked()) {
                criticalEmergencyFields.add(FieldRiskProfileResponse.fromDomain(
                        a,
                        fInfo != null ? fInfo.areaHectares() : null,
                        fInfo != null ? fInfo.cropType() : null,
                        fInfo != null ? fInfo.soilType() : null,
                        fInfo != null ? fInfo.irrigationMethod() : null
                ));
            }

            if (a.getRecommendations() != null) {
                allRecommendations.addAll(a.getRecommendations());
            }
        }

        // Sort critical emergency fields by priority rank
        criticalEmergencyFields.sort(Comparator.comparingInt(FieldRiskProfileResponse::priorityRank));

        // Sort recommendations by priority rank ascending, limit top 10
        List<RiskRecommendationResponse> topRecommendations = allRecommendations.stream()
                .sorted(Comparator.comparingInt(RiskRecommendation::priorityRank))
                .limit(10)
                .map(RiskRecommendationResponse::fromDomain)
                .toList();

        return FarmRiskOverviewResponse.builder()
                .totalAssessedFields(activeAssessments.size())
                .criticalRiskCount(critical)
                .highRiskCount(high)
                .mediumRiskCount(medium)
                .lowRiskCount(low)
                .insufficientDataCount(insufficientData)
                .hydraulicEmergencyCount(hydraulicEmergency)
                .waterShortageCount(waterShortage)
                .severeMoistureRiskCount(severeMoisture)
                .telemetryIssuesCount(telemetryIssues)
                .modelReliabilityWarningsCount(modelWarnings)
                .activeRecommendationsCount(allRecommendations.size())
                .categoryDistribution(categoryDistribution)
                .criticalEmergencyFields(criticalEmergencyFields)
                .topPrioritizedRecommendations(topRecommendations)
                .evaluatedAt(now)
                .build();
    }
}
