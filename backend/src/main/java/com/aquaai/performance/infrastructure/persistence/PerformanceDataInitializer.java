package com.aquaai.performance.infrastructure.persistence;

import com.aquaai.performance.domain.model.AggregationMethod;
import com.aquaai.performance.domain.model.AssessmentReviewStatus;
import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.BaselineQuality;
import com.aquaai.performance.domain.model.ChangeDirection;
import com.aquaai.performance.domain.model.EvidenceClassification;
import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.ImpactAssessment;
import com.aquaai.performance.domain.model.IndicatorCategory;
import com.aquaai.performance.domain.model.MeasurementUnit;
import com.aquaai.performance.domain.model.PerformanceBaseline;
import com.aquaai.performance.domain.model.PerformanceEvaluation;
import com.aquaai.performance.domain.model.PerformanceIndicator;
import com.aquaai.performance.domain.model.PerformanceTarget;
import com.aquaai.performance.domain.model.PerformanceTrend;
import com.aquaai.performance.domain.model.TargetCategory;
import com.aquaai.performance.domain.model.TargetComplianceStatus;
import com.aquaai.performance.domain.model.TargetStatus;
import com.aquaai.performance.domain.model.TargetType;
import com.aquaai.performance.domain.repository.ImpactAssessmentRepository;
import com.aquaai.performance.domain.repository.PerformanceBaselineRepository;
import com.aquaai.performance.domain.repository.PerformanceEvaluationRepository;
import com.aquaai.performance.domain.repository.PerformanceIndicatorRepository;
import com.aquaai.performance.domain.repository.PerformanceTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceDataInitializer implements CommandLineRunner {

    private final PerformanceIndicatorRepository indicatorRepository;
    private final PerformanceBaselineRepository baselineRepository;
    private final PerformanceTargetRepository targetRepository;
    private final PerformanceEvaluationRepository evaluationRepository;
    private final ImpactAssessmentRepository impactAssessmentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (indicatorRepository.findAll().isEmpty()) {
            log.info("Initializing Milestone 08.17 Performance Indicator Catalog and Baseline Data...");
            initIndicators();
            initBaselines();
            initTargets();
            initEvaluations();
            initImpactAssessments();
            log.info("Performance Domain Data initialization complete.");
        }
    }

    private void initIndicators() {
        List<PerformanceIndicator> list = new ArrayList<>();
        Instant now = Instant.now();

        list.add(PerformanceIndicator.builder()
                .id(UUID.randomUUID())
                .code("WDP_DELIVERY_FULFILLMENT")
                .name("Suv yetkazib berish rejasi ijrosi")
                .description("Amalda yetkazilgan suv hajmining rejalashtirilgan taqsimot hajmiga nisbati (Actual / Planned × 100).")
                .category(IndicatorCategory.DELIVERY_RELIABILITY)
                .measurementUnit(MeasurementUnit.PERCENT)
                .calculationMethod("(actualVolumeM3 / plannedVolumeM3) * 100")
                .aggregationMethod(AggregationMethod.WEIGHTED_AVERAGE)
                .supportedGeographicScope(GeographicScopeType.DISTRICT)
                .evidenceRequirement("OBSERVED_DELIVERY_RECORDS")
                .minimumDataQualityPct(new BigDecimal("85.00"))
                .applicableTimePeriod("MONTHLY")
                .sourceModule("NETWORK")
                .calculationVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceIndicator.builder()
                .id(UUID.randomUUID())
                .code("CCP_CONVEYANCE_EFFICIENCY")
                .name("Magistral kanal uzatish samaradorligi")
                .description("Quyi gidropostdan oʻtgan foydali suv hajmining yuqori gidropostdagi umumiy oqimga nisbati.")
                .category(IndicatorCategory.CONVEYANCE_EFFICIENCY)
                .measurementUnit(MeasurementUnit.PERCENT)
                .calculationMethod("(downstreamVolumeM3 / upstreamVolumeM3) * 100")
                .aggregationMethod(AggregationMethod.AVERAGE)
                .supportedGeographicScope(GeographicScopeType.CANAL_REACH)
                .evidenceRequirement("CALIBRATED_ACOUSTIC_GAUGES")
                .minimumDataQualityPct(new BigDecimal("90.00"))
                .applicableTimePeriod("MONTHLY")
                .sourceModule("WATER_ACCOUNTING")
                .calculationVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceIndicator.builder()
                .id(UUID.randomUUID())
                .code("CCP_LOSS_PERCENTAGE")
                .name("Kanal uchastkasi filtrlash va yoʻqotish ulushi")
                .description("Kanal reachida oqim boʻyicha yoʻqotilgan suv hajmi ulushi ((Upstream - Downstream) / Upstream × 100).")
                .category(IndicatorCategory.CONVEYANCE_EFFICIENCY)
                .measurementUnit(MeasurementUnit.PERCENT)
                .calculationMethod("((upstreamVolumeM3 - downstreamVolumeM3) / upstreamVolumeM3) * 100")
                .aggregationMethod(AggregationMethod.AVERAGE)
                .supportedGeographicScope(GeographicScopeType.CANAL_REACH)
                .evidenceRequirement("OBSERVED_OR_CALCULATED")
                .minimumDataQualityPct(new BigDecimal("85.00"))
                .applicableTimePeriod("MONTHLY")
                .sourceModule("WATER_ACCOUNTING")
                .calculationVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceIndicator.builder()
                .id(UUID.randomUUID())
                .code("WAC_QUOTA_UTILIZATION")
                .name("Mavsumiy suv limiti intizomi va sarf nisbati")
                .description("Tasdiqlangan kvota limitidan amalda foydalanish foizi va limitdan oshib ketish xavfi darajasi.")
                .category(IndicatorCategory.ALLOCATION_COMPLIANCE)
                .measurementUnit(MeasurementUnit.PERCENT)
                .calculationMethod("(actualConsumptionM3 / allocatedVolumeM3) * 100")
                .aggregationMethod(AggregationMethod.AVERAGE)
                .supportedGeographicScope(GeographicScopeType.WUA)
                .evidenceRequirement("CALCULATED_LEDGER_ENTRIES")
                .minimumDataQualityPct(new BigDecimal("80.00"))
                .applicableTimePeriod("SEASONAL")
                .sourceModule("WATER_ACCOUNTING")
                .calculationVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceIndicator.builder()
                .id(UUID.randomUUID())
                .code("DEO_EQUITY_CV")
                .name("Taqsimot tengligi variatsiya koeffitsiyenti (CV)")
                .description("Suv yetkazib berish intensivligi boʻyicha bosh va etak qismlar orasidagi dispersiya (CV = σ / μ).")
                .category(IndicatorCategory.DISTRIBUTION_EQUITY)
                .measurementUnit(MeasurementUnit.RATIO)
                .calculationMethod("standardDeviationDeliveryPerHa / meanDeliveryPerHa")
                .aggregationMethod(AggregationMethod.LATEST)
                .supportedGeographicScope(GeographicScopeType.DISTRICT)
                .evidenceRequirement("CLUSTER_EQUITY_EVALUATION")
                .minimumDataQualityPct(new BigDecimal("85.00"))
                .applicableTimePeriod("MONTHLY")
                .sourceModule("WATER_ACCOUNTING")
                .calculationVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceIndicator.builder()
                .id(UUID.randomUUID())
                .code("IOP_SCHEDULE_ADHERENCE")
                .name("Sugʻorish jadvaliga rioya qilish darajasi")
                .description("Rejalashtirilgan tavsiyaviy sugʻorish vaqtiga amalda rioya qilingan parvarishlar ulushi.")
                .category(IndicatorCategory.IRRIGATION_OPERATIONS)
                .measurementUnit(MeasurementUnit.PERCENT)
                .calculationMethod("(completedIrrigationPlans / plannedIrrigationPlans) * 100")
                .aggregationMethod(AggregationMethod.AVERAGE)
                .supportedGeographicScope(GeographicScopeType.FIELD)
                .evidenceRequirement("FIELD_OPERATION_LOGS")
                .minimumDataQualityPct(new BigDecimal("80.00"))
                .applicableTimePeriod("MONTHLY")
                .sourceModule("IRRIGATION")
                .calculationVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceIndicator.builder()
                .id(UUID.randomUUID())
                .code("CAE_RESOLUTION_RATE")
                .name("Mustaqil tekshiruvdan oʻtgan tuzatish choralari samaradorligi")
                .description("Aniqlangan nomuvofiqliklar va kamchiliklarning toʻrt koʻz prinsipi asosida mustaqil hal etilish darajasi.")
                .category(IndicatorCategory.ACCOUNTABILITY_EFFECTIVENESS)
                .measurementUnit(MeasurementUnit.PERCENT)
                .calculationMethod("(independentlyVerifiedResolutions / totalClosedIncidents) * 100")
                .aggregationMethod(AggregationMethod.AVERAGE)
                .supportedGeographicScope(GeographicScopeType.DISTRICT)
                .evidenceRequirement("INDEPENDENT_VERIFICATION_AUDIT")
                .minimumDataQualityPct(new BigDecimal("95.00"))
                .applicableTimePeriod("MONTHLY")
                .sourceModule("ACCOUNTABILITY")
                .calculationVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceIndicator.builder()
                .id(UUID.randomUUID())
                .code("CAE_RECURRENCE_RATE")
                .name("Gidrotexnik hodisalarning qaytalanish koʻrsatkichi")
                .description("Hal etilgandan soʻng qayta ochilgan yoki takroriy aniqlangan operatsion buzilishlar nisbati.")
                .category(IndicatorCategory.ACCOUNTABILITY_EFFECTIVENESS)
                .measurementUnit(MeasurementUnit.PERCENT)
                .calculationMethod("(recurrentIncidentsCount / totalIncidentsCount) * 100")
                .aggregationMethod(AggregationMethod.AVERAGE)
                .supportedGeographicScope(GeographicScopeType.DISTRICT)
                .evidenceRequirement("AUDIT_TIMELINE_LOGS")
                .minimumDataQualityPct(new BigDecimal("90.00"))
                .applicableTimePeriod("SEASONAL")
                .sourceModule("ACCOUNTABILITY")
                .calculationVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceIndicator.builder()
                .id(UUID.randomUUID())
                .code("SDR_OBSERVED_COVERAGE")
                .name("Datchik telemetriyasining ishonchliligi va empirik qamrovi")
                .description("Haqiqiy kuzatilgan (OBSERVED) oʻlchovlarning umumiy telemetriya oqimidagi ulushi.")
                .category(IndicatorCategory.DATA_QUALITY)
                .measurementUnit(MeasurementUnit.PERCENT)
                .calculationMethod("(observedTelemetryPoints / totalTelemetryExpected) * 100")
                .aggregationMethod(AggregationMethod.AVERAGE)
                .supportedGeographicScope(GeographicScopeType.DISTRICT)
                .evidenceRequirement("TELEMETRY_LINEAGE_GATE")
                .minimumDataQualityPct(new BigDecimal("90.00"))
                .applicableTimePeriod("MONTHLY")
                .sourceModule("SENSOR")
                .calculationVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceIndicator.builder()
                .id(UUID.randomUUID())
                .code("PIU_FORECAST_ACCURACY")
                .name("Prediktiv suv talabi prognozining aniqlik darajasi")
                .description("ML va gibrid gidravlik model prognozlarining amaldagi suv oqimiga mos kelish aniqligi (100 - WAPE).")
                .category(IndicatorCategory.PREDICTIVE_INTELLIGENCE)
                .measurementUnit(MeasurementUnit.PERCENT)
                .calculationMethod("100 - (sum(|actual - forecast|) / sum(actual) * 100)")
                .aggregationMethod(AggregationMethod.AVERAGE)
                .supportedGeographicScope(GeographicScopeType.DISTRICT)
                .evidenceRequirement("MODEL_EVALUATION_BACKTEST")
                .minimumDataQualityPct(new BigDecimal("85.00"))
                .applicableTimePeriod("MONTHLY")
                .sourceModule("PREDICTION")
                .calculationVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceIndicator.builder()
                .id(UUID.randomUUID())
                .code("COV_VERIFIED_SAVINGS")
                .name("Tasdiqlangan amaliy suv tejamkorligi hajmi")
                .description("Kanal betonlashtirish, quvurli oʻzanlar va agrotexnik choralar natijasida erishilgan aniq tejalgan suv hajmi.")
                .category(IndicatorCategory.CONSERVATION_OUTCOME)
                .measurementUnit(MeasurementUnit.M3)
                .calculationMethod("baselineLossVolumeM3 - postInterventionLossVolumeM3")
                .aggregationMethod(AggregationMethod.SUM)
                .supportedGeographicScope(GeographicScopeType.DISTRICT)
                .evidenceRequirement("VERIFIED_BASELINE_COMPARISON")
                .minimumDataQualityPct(new BigDecimal("90.00"))
                .applicableTimePeriod("SEASONAL")
                .sourceModule("WATER_ACCOUNTING")
                .calculationVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        indicatorRepository.saveAll(list);
    }

    private void initBaselines() {
        List<PerformanceBaseline> list = new ArrayList<>();
        Instant start = Instant.now().minus(60, ChronoUnit.DAYS);
        Instant end = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant now = Instant.now();

        list.add(PerformanceBaseline.builder()
                .id(UUID.randomUUID())
                .indicatorCode("WDP_DELIVERY_FULFILLMENT")
                .name("Tuman boʻyicha 2025-yil oʻrtacha yetkazib berish bazasi")
                .description("2025-yilgi vegetatsiya davridagi dispecherlik rejasiga nisbatan amaliy yetkazish koʻrsatkichi.")
                .scopeType(GeographicScopeType.DISTRICT)
                .scopeId(null)
                .scopeName("Yangiariq Tumani")
                .periodStart(start)
                .periodEnd(end)
                .baselineValue(new BigDecimal("84.50"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .dataCompletenessPct(new BigDecimal("94.20"))
                .evidenceClassification(EvidenceClassification.CALCULATED)
                .baselineQuality(BaselineQuality.VERIFIED_BASELINE)
                .calculationMethod("HISTORICAL_WEIGHTED_AVERAGE")
                .seasonContext("MAIN_IRRIGATION_SEASON")
                .approvalStatus("APPROVED")
                .approvedBy(null)
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceBaseline.builder()
                .id(UUID.randomUUID())
                .indicatorCode("CCP_LOSS_PERCENTAGE")
                .name("Boʻzyop bosh kanali taʼmirdan oldingi yoʻqotish bazasi")
                .description("PK0 dan PK12 gacha boʻlgan tuproq oʻzanli kanaldagi oʻrtacha oqim yoʻqotish hajmi ulushi.")
                .scopeType(GeographicScopeType.CANAL_REACH)
                .scopeId(null)
                .scopeName("Boʻzyop Bosh Kanali (Reach PK0-PK12)")
                .periodStart(start)
                .periodEnd(end)
                .baselineValue(new BigDecimal("12.40"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .dataCompletenessPct(new BigDecimal("91.00"))
                .evidenceClassification(EvidenceClassification.CALCULATED)
                .baselineQuality(BaselineQuality.VERIFIED_BASELINE)
                .calculationMethod("ACOUSTIC_MASS_BALANCE")
                .seasonContext("MAIN_IRRIGATION_SEASON")
                .approvalStatus("APPROVED")
                .approvedBy(null)
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceBaseline.builder()
                .id(UUID.randomUUID())
                .indicatorCode("DEO_EQUITY_CV")
                .name("Tuman suv taqsimoti tengsizligi bazasi")
                .description("WUA lar boʻyicha yetkazib berish intensivligi dispersiyasining oʻrtacha qiymati.")
                .scopeType(GeographicScopeType.DISTRICT)
                .scopeId(null)
                .scopeName("Yangiariq Tumani")
                .periodStart(start)
                .periodEnd(end)
                .baselineValue(new BigDecimal("0.24"))
                .measurementUnit(MeasurementUnit.RATIO)
                .dataCompletenessPct(new BigDecimal("96.50"))
                .evidenceClassification(EvidenceClassification.CALCULATED)
                .baselineQuality(BaselineQuality.VERIFIED_BASELINE)
                .calculationMethod("CLUSTER_VARIANCE_ANALYSIS")
                .seasonContext("MAIN_IRRIGATION_SEASON")
                .approvalStatus("APPROVED")
                .approvedBy(null)
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceBaseline.builder()
                .id(UUID.randomUUID())
                .indicatorCode("CAE_RECURRENCE_RATE")
                .name("Nomuvofiqliklar takrorlanishi bazasi")
                .description("Gidropost va tarmoqdagi texnik buzilishlarning oʻtgan davrdagi qayta ochilish ulushi.")
                .scopeType(GeographicScopeType.DISTRICT)
                .scopeId(null)
                .scopeName("Yangiariq Tumani")
                .periodStart(start)
                .periodEnd(end)
                .baselineValue(new BigDecimal("14.20"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .dataCompletenessPct(new BigDecimal("100.00"))
                .evidenceClassification(EvidenceClassification.OBSERVED)
                .baselineQuality(BaselineQuality.VERIFIED_BASELINE)
                .calculationMethod("INCIDENT_LIFECYCLE_AUDIT")
                .seasonContext("MAIN_IRRIGATION_SEASON")
                .approvalStatus("APPROVED")
                .approvedBy(null)
                .createdAt(now)
                .updatedAt(now)
                .build());

        baselineRepository.saveAll(list);
    }

    private void initTargets() {
        List<PerformanceTarget> list = new ArrayList<>();
        Instant start = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(60, ChronoUnit.DAYS);
        Instant now = Instant.now();

        list.add(PerformanceTarget.builder()
                .id(UUID.randomUUID())
                .indicatorCode("WDP_DELIVERY_FULFILLMENT")
                .title("2026-yil suv yetkazish boʻyicha operatsion meʼyor")
                .scopeType(GeographicScopeType.DISTRICT)
                .scopeId(null)
                .scopeName("Yangiariq Tumani")
                .targetType(TargetType.MINIMUM)
                .targetCategory(TargetCategory.OPERATIONAL_TARGET)
                .targetValue(new BigDecimal("92.00"))
                .rangeMin(new BigDecimal("90.00"))
                .rangeMax(new BigDecimal("100.00"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .effectiveStart(start)
                .effectiveEnd(end)
                .targetOwner("Tuman Suv Xoʻjaligi Boshqarmasi")
                .status(TargetStatus.APPROVED)
                .rationale("Dispecherlik jadvalini optimallashtirish hisobiga yetkazish ishonchliligini 92% dan yuqori saqlash.")
                .targetVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceTarget.builder()
                .id(UUID.randomUUID())
                .indicatorCode("CCP_LOSS_PERCENTAGE")
                .title("Magistral kanallarda yoʻqotishning chegaraviy meʼyori")
                .scopeType(GeographicScopeType.CANAL_REACH)
                .scopeId(null)
                .scopeName("Boʻzyop Bosh Kanali")
                .targetType(TargetType.MAXIMUM)
                .targetCategory(TargetCategory.ENGINEERING_CONSTRAINT)
                .targetValue(new BigDecimal("8.50"))
                .rangeMin(BigDecimal.ZERO)
                .rangeMax(new BigDecimal("8.50"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .effectiveStart(start)
                .effectiveEnd(end)
                .targetOwner("Bosh Muhandislik Xizmati")
                .status(TargetStatus.APPROVED)
                .rationale("Tuproq oʻzanli kanallarda gidrotexnik xavfsizlik va loyqalanish meʼyorlariga koʻra yoʻqotish 8.5% dan oshmasligi lozim.")
                .targetVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceTarget.builder()
                .id(UUID.randomUUID())
                .indicatorCode("DEO_EQUITY_CV")
                .title("Tuman taqsimot adolati (CV <= 0.18)")
                .scopeType(GeographicScopeType.DISTRICT)
                .scopeId(null)
                .scopeName("Yangiariq Tumani")
                .targetType(TargetType.MAXIMUM)
                .targetCategory(TargetCategory.OPERATIONAL_TARGET)
                .targetValue(new BigDecimal("0.18"))
                .rangeMin(BigDecimal.ZERO)
                .rangeMax(new BigDecimal("0.18"))
                .measurementUnit(MeasurementUnit.RATIO)
                .effectiveStart(start)
                .effectiveEnd(end)
                .targetOwner("Suv Taqsimoti Dispecherlik Markazi")
                .status(TargetStatus.APPROVED)
                .rationale("Barcha WUA lar va fermer xoʻjaliklariga suvni ekin tanqisligi hisobga olingan holda teng yetkazish.")
                .targetVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        list.add(PerformanceTarget.builder()
                .id(UUID.randomUUID())
                .indicatorCode("CAE_RESOLUTION_RATE")
                .title("Tuzatish choralarining mustaqil tekshiruvdan oʻtish darajasi (>= 90%)")
                .scopeType(GeographicScopeType.DISTRICT)
                .scopeId(null)
                .scopeName("Yangiariq Tumani")
                .targetType(TargetType.MINIMUM)
                .targetCategory(TargetCategory.POLICY_THRESHOLD)
                .targetValue(new BigDecimal("90.00"))
                .rangeMin(new BigDecimal("90.00"))
                .rangeMax(new BigDecimal("100.00"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .effectiveStart(start)
                .effectiveEnd(end)
                .targetOwner("Auditorlik va Ichki Nazorat Departamenti")
                .status(TargetStatus.APPROVED)
                .rationale("Toʻrt koʻz prinsipi asosida mustaqil tekshiruv talabini qatʼiy taʼminlash.")
                .targetVersion("v1.0.0")
                .createdAt(now)
                .updatedAt(now)
                .build());

        targetRepository.saveAll(list);
    }

    private void initEvaluations() {
        List<PerformanceEvaluation> list = new ArrayList<>();
        Instant pStart = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant pEnd = Instant.now();

        list.add(PerformanceEvaluation.builder()
                .id(UUID.randomUUID())
                .evaluationNumber("EVAL-WDP-001")
                .indicatorCode("WDP_DELIVERY_FULFILLMENT")
                .scopeType(GeographicScopeType.DISTRICT)
                .scopeId(null)
                .scopeName("Yangiariq Tumani")
                .periodStart(pStart)
                .periodEnd(pEnd)
                .calculatedValue(new BigDecimal("92.80"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .baselineId(null)
                .baselineValue(new BigDecimal("84.50"))
                .targetId(null)
                .targetValue(new BigDecimal("92.00"))
                .targetCompliance(TargetComplianceStatus.MET)
                .varianceFromBaselinePct(new BigDecimal("9.82"))
                .varianceFromTargetPct(new BigDecimal("0.87"))
                .trend(PerformanceTrend.IMPROVING)
                .evidenceClassification(EvidenceClassification.OBSERVED)
                .observedSampleRatio(new BigDecimal("94.50"))
                .missingDataIntervals(2)
                .comparisonValid(true)
                .validityNotes("Duration and seasonal context fully aligned with baseline.")
                .explanatorySummary("Delivery fulfillment achieved 92.80%, exceeding target (92.00%) and demonstrating +9.82% improvement over historical baseline.")
                .calculationVersion("v1.0.0")
                .evaluatedAt(pEnd)
                .build());

        list.add(PerformanceEvaluation.builder()
                .id(UUID.randomUUID())
                .evaluationNumber("EVAL-CCP-001")
                .indicatorCode("CCP_LOSS_PERCENTAGE")
                .scopeType(GeographicScopeType.CANAL_REACH)
                .scopeId(null)
                .scopeName("Boʻzyop Bosh Kanali (Reach PK0-PK12)")
                .periodStart(pStart)
                .periodEnd(pEnd)
                .calculatedValue(new BigDecimal("8.00"))
                .measurementUnit(MeasurementUnit.PERCENT)
                .baselineId(null)
                .baselineValue(new BigDecimal("12.40"))
                .targetId(null)
                .targetValue(new BigDecimal("8.50"))
                .targetCompliance(TargetComplianceStatus.MET)
                .varianceFromBaselinePct(new BigDecimal("-35.48"))
                .varianceFromTargetPct(new BigDecimal("-5.88"))
                .trend(PerformanceTrend.IMPROVING)
                .evidenceClassification(EvidenceClassification.CALCULATED)
                .observedSampleRatio(new BigDecimal("92.00"))
                .missingDataIntervals(1)
                .comparisonValid(true)
                .validityNotes("Calibrated acoustic flow meters at upstream PK0 and downstream PK12.")
                .explanatorySummary("Canal reach loss dropped to 8.00%, fully meeting engineering constraint (<= 8.50%) due to recent concrete bed compaction.")
                .calculationVersion("v1.0.0")
                .evaluatedAt(pEnd)
                .build());

        list.add(PerformanceEvaluation.builder()
                .id(UUID.randomUUID())
                .evaluationNumber("EVAL-DEO-001")
                .indicatorCode("DEO_EQUITY_CV")
                .scopeType(GeographicScopeType.DISTRICT)
                .scopeId(null)
                .scopeName("Yangiariq Tumani")
                .periodStart(pStart)
                .periodEnd(pEnd)
                .calculatedValue(new BigDecimal("0.16"))
                .measurementUnit(MeasurementUnit.RATIO)
                .baselineId(null)
                .baselineValue(new BigDecimal("0.24"))
                .targetId(null)
                .targetValue(new BigDecimal("0.18"))
                .targetCompliance(TargetComplianceStatus.MET)
                .varianceFromBaselinePct(new BigDecimal("-33.33"))
                .varianceFromTargetPct(new BigDecimal("-11.11"))
                .trend(PerformanceTrend.IMPROVING)
                .evidenceClassification(EvidenceClassification.CALCULATED)
                .observedSampleRatio(new BigDecimal("96.00"))
                .missingDataIntervals(0)
                .comparisonValid(true)
                .validityNotes("Cluster deliveries evaluated across all 6 WUA zones.")
                .explanatorySummary("Distribution equity CV improved from 0.24 to 0.16, indicating LOW_DISPARITY and equitable tail-end delivery adherence.")
                .calculationVersion("v1.0.0")
                .evaluatedAt(pEnd)
                .build());

        evaluationRepository.saveAll(list);
    }

    private void initImpactAssessments() {
        List<ImpactAssessment> list = new ArrayList<>();
        Instant baseStart = Instant.now().minus(60, ChronoUnit.DAYS);
        Instant baseEnd = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant evalStart = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant evalEnd = Instant.now();
        Instant intDate = Instant.now().minus(31, ChronoUnit.DAYS);

        // 1. DIRECTLY_VERIFIED: Concrete bed lining on Bo'zyop Canal Reach
        list.add(ImpactAssessment.builder()
                .id(UUID.randomUUID())
                .assessmentNumber("IMP-2026-001")
                .title("Boʻzyop bosh kanali PK4-PK8 oʻzanini betonlashtirish samaradorligi")
                .interventionType("CANAL_MAINTENANCE")
                .interventionId(UUID.randomUUID())
                .interventionReference("CA-2026-0014: Concrete Flume Bed Compaction")
                .interventionDate(intDate)
                .indicatorCode("CCP_LOSS_PERCENTAGE")
                .scopeType(GeographicScopeType.CANAL_REACH)
                .scopeId(null)
                .scopeName("Boʻzyop Bosh Kanali (Reach PK4-PK8)")
                .baselineId(null)
                .baselinePeriodStart(baseStart)
                .baselinePeriodEnd(baseEnd)
                .baselineValue(new BigDecimal("12.40"))
                .evaluationPeriodStart(evalStart)
                .evaluationPeriodEnd(evalEnd)
                .postInterventionValue(new BigDecimal("6.80"))
                .absoluteChange(new BigDecimal("-5.60"))
                .percentageChange(new BigDecimal("-45.16"))
                .changeDirection(ChangeDirection.FAVORABLE)
                .measurementUnit(MeasurementUnit.PERCENT)
                .evidenceClassification(EvidenceClassification.CALCULATED)
                .baselineQuality(BaselineQuality.VERIFIED_BASELINE)
                .attributionLevel(AttributionLevel.DIRECTLY_VERIFIED)
                .confoundingFactors("Hech qanday gidravlik buzilish yoki kuchli yogʻingarchilik qayd etilmadi.")
                .dataLimitations("Yuqori va quyi akustik gidropostlar toʻliq kalibrlangan, maʼlumot toʻliqligi 98.4%.")
                .comparisonValid(true)
                .methodologyNotes("Gidravlik mass-balans hisobi va mustaqil texnik nazorat dalolatnomasi asosida tasdiqlangan.")
                .reviewStatus(AssessmentReviewStatus.VERIFIED_OUTCOME)
                .reviewerId(null)
                .reviewerName("Mustaqil Auditor: Sobirov D.")
                .reviewNotes("Joyiga chiqib koʻrikdan oʻtkazildi va kalibrlangan sarf oʻlchagichlar bilan yoʻqotish kamayganligi toʻliq tasdiqlandi.")
                .reviewedAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .createdAt(evalStart)
                .updatedAt(Instant.now())
                .build());

        // 2. STRONGLY_ASSOCIATED: WUA Bog'olon rotational schedule optimization
        list.add(ImpactAssessment.builder()
                .id(UUID.randomUUID())
                .assessmentNumber("IMP-2026-002")
                .title("Bogʻolon WUA hududida navbatli sugʻorish jadvalini joriy etish")
                .interventionType("DISPATCH_SCHEDULE")
                .interventionId(UUID.randomUUID())
                .interventionReference("DISPATCH-OPT-2026-08: Staggered Rotational Gate Delivery")
                .interventionDate(intDate)
                .indicatorCode("WDP_DELIVERY_FULFILLMENT")
                .scopeType(GeographicScopeType.WUA)
                .scopeId(null)
                .scopeName("Bogʻolon WUA")
                .baselineId(null)
                .baselinePeriodStart(baseStart)
                .baselinePeriodEnd(baseEnd)
                .baselineValue(new BigDecimal("78.40"))
                .evaluationPeriodStart(evalStart)
                .evaluationPeriodEnd(evalEnd)
                .postInterventionValue(new BigDecimal("91.20"))
                .absoluteChange(new BigDecimal("12.80"))
                .percentageChange(new BigDecimal("16.33"))
                .changeDirection(ChangeDirection.FAVORABLE)
                .measurementUnit(MeasurementUnit.PERCENT)
                .evidenceClassification(EvidenceClassification.OBSERVED)
                .baselineQuality(BaselineQuality.VERIFIED_BASELINE)
                .attributionLevel(AttributionLevel.STRONGLY_ASSOCIATED)
                .confoundingFactors("Vegetatsiya davri boshida umumiy daryo suvi sathi 4% ga oshgan boʻlishi mumkin.")
                .dataLimitations("Barcha 14 ta fermer xoʻjaligi suv olgichlarida loglar mavjud.")
                .comparisonValid(true)
                .methodologyNotes("Fermer darajasidagi haqiqiy suv yetkazish dalolatnomalari orqali hisoblangan.")
                .reviewStatus(AssessmentReviewStatus.VERIFIED_OUTCOME)
                .reviewerId(null)
                .reviewerName("Viloyat Dispecheri: Xoʻjayev B.")
                .reviewNotes("Jadval intizomiga rioya qilinganligi hisobiga etak qismdagi dalalarga suv toʻliq yetib borgan.")
                .reviewedAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .createdAt(evalStart)
                .updatedAt(Instant.now())
                .build());

        // 3. ASSOCIATED: Field F-102 laser leveling and moisture feedback
        list.add(ImpactAssessment.builder()
                .id(UUID.randomUUID())
                .assessmentNumber("IMP-2026-003")
                .title("F-102 dalasida lazerli tekislash va datchiklar asosida sugʻorish")
                .interventionType("FIELD_IRRIGATION_PLAN")
                .interventionId(UUID.randomUUID())
                .interventionReference("PLAN-2026-44: Precision Irrigation Adherence")
                .interventionDate(intDate)
                .indicatorCode("IOP_SCHEDULE_ADHERENCE")
                .scopeType(GeographicScopeType.FIELD)
                .scopeId(null)
                .scopeName("Field F-102 (Paxta, 24.5 ga)")
                .baselineId(null)
                .baselinePeriodStart(baseStart)
                .baselinePeriodEnd(baseEnd)
                .baselineValue(new BigDecimal("68.00"))
                .evaluationPeriodStart(evalStart)
                .evaluationPeriodEnd(evalEnd)
                .postInterventionValue(new BigDecimal("88.50"))
                .absoluteChange(new BigDecimal("20.50"))
                .percentageChange(new BigDecimal("30.15"))
                .changeDirection(ChangeDirection.FAVORABLE)
                .measurementUnit(MeasurementUnit.PERCENT)
                .evidenceClassification(EvidenceClassification.OBSERVED)
                .baselineQuality(BaselineQuality.PARTIALLY_VERIFIED_BASELINE)
                .attributionLevel(AttributionLevel.ASSOCIATED)
                .confoundingFactors("Havo harorati oʻtgan oyga nisbatan 2 daraja salqinroq boʻlgan.")
                .dataLimitations("Tuproq namligi sensori oraliq 2 kunda texnik qayta oʻrnatilgan.")
                .comparisonValid(true)
                .methodologyNotes("Sugʻorish jurnali va telemetriya namlik profili solishtirildi.")
                .reviewStatus(AssessmentReviewStatus.SUBMITTED)
                .reviewerId(null)
                .reviewerName(null)
                .reviewNotes(null)
                .reviewedAt(null)
                .createdAt(evalStart)
                .updatedAt(Instant.now())
                .build());

        impactAssessmentRepository.saveAll(list);
    }
}
