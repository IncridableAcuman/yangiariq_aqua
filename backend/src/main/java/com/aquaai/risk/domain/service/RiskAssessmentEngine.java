package com.aquaai.risk.domain.service;

import com.aquaai.intelligence.domain.model.TelemetryTrend;
import com.aquaai.intelligence.domain.model.TrendDirection;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskAssessmentContext;
import com.aquaai.risk.domain.model.RiskAssessmentLifecycleStatus;
import com.aquaai.risk.domain.model.RiskCategory;
import com.aquaai.risk.domain.model.RiskConfidence;
import com.aquaai.risk.domain.model.RiskEvidence;
import com.aquaai.risk.domain.model.RiskEvidenceSource;
import com.aquaai.risk.domain.model.RiskEvidenceType;
import com.aquaai.risk.domain.model.RiskFactor;
import com.aquaai.risk.domain.model.RiskRecommendation;
import com.aquaai.risk.domain.model.RiskSeverity;
import com.aquaai.risk.domain.model.RiskTrend;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Deterministic Risk Assessment Engine.
 * <p>
 * Core Architectural Mandates:
 * 1. Hydraulic safety interlocks always have the highest priority.
 * 2. Never present predictions as observations.
 * 3. Missing data is never interpreted as a healthy condition.
 * 4. Model degradation lowers prediction confidence.
 * 5. Explanations and triggered evidence are strictly linked.
 */
public class RiskAssessmentEngine {

    private static final BigDecimal HUNDRED = new BigDecimal("100.00");
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal CRITICAL_MOISTURE_THRESHOLD = new BigDecimal("15.0000");
    private static final BigDecimal DEFICIT_MOISTURE_THRESHOLD = new BigDecimal("20.0000");
    private static final BigDecimal SATURATION_MOISTURE_THRESHOLD = new BigDecimal("35.0000");
    private static final BigDecimal MIN_HYDRAULIC_PRESSURE_BAR = new BigDecimal("1.2000");
    private static final BigDecimal CRITICAL_PRESSURE_COLLAPSE_BAR = new BigDecimal("0.5000");

    public FarmRiskAssessment assess(RiskAssessmentContext ctx) {
        if (ctx == null || ctx.fieldId() == null) {
            throw new IllegalArgumentException("RiskAssessmentContext and fieldId must not be null");
        }

        UUID assessmentId = UUID.randomUUID();
        UUID fieldId = ctx.fieldId();
        String fieldName = ctx.fieldName() != null ? ctx.fieldName() : "Field-" + fieldId.toString().substring(0, 8);
        Instant evalTime = ctx.evaluationTime() != null ? ctx.evaluationTime() : Instant.now();

        List<RiskFactor> factors = new ArrayList<>();
        List<RiskRecommendation> recommendations = new ArrayList<>();
        List<String> missingWarnings = new ArrayList<>();

        // Group latest sensor readings by sensor type
        Map<SensorType, SensorReading> latestReadingsMap = new HashMap<>();
        if (ctx.latestReadings() != null) {
            for (SensorReading r : ctx.latestReadings()) {
                if (r != null && r.getType() != null) {
                    latestReadingsMap.put(r.getType(), r);
                }
            }
        }

        // =========================================================================
        // 1. HYDRAULIC SAFETY RISK
        // =========================================================================
        boolean hydraulicBlocked = false;
        String hydraulicBlockReason = null;

        SensorReading pressureReading = latestReadingsMap.get(SensorType.PRESSURE);
        SensorReading flowReading = latestReadingsMap.get(SensorType.WATER_FLOW);
        BigDecimal currentPressure = ctx.currentPressureBar() != null ? ctx.currentPressureBar()
                : (pressureReading != null ? pressureReading.getValue() : null);
        BigDecimal currentFlow = ctx.currentFlowM3H() != null ? ctx.currentFlowM3H()
                : (flowReading != null ? flowReading.getValue() : null);

        if (ctx.hasSuspectedPipeBurst()) {
            hydraulicBlocked = true;
            hydraulicBlockReason = "Hydraulic Safety Interlock: SUSPECTED_PIPE_BURST detected. Pressurization strictly blocked to prevent canal rupture.";
            List<RiskEvidence> ev = List.of(
                    RiskEvidence.of(
                            RiskCategory.HYDRAULIC,
                            "PIPE_BURST_ANOMALY",
                            BigDecimal.ONE,
                            BigDecimal.ZERO,
                            "flag",
                            RiskEvidenceType.OBSERVED,
                            RiskEvidenceSource.HYDRAULIC_SAFETY,
                            "Anomalous flow surge coupled with pressure collapse detected on parcel mainline.",
                            true
                    )
            );
            RiskFactor factor = new RiskFactor(
                    UUID.randomUUID(),
                    RiskCategory.HYDRAULIC,
                    RiskSeverity.CRITICAL,
                    RiskConfidence.HIGH,
                    "Catastrophic Pipeline Rupture Suspected",
                    hydraulicBlockReason,
                    ev,
                    new BigDecimal("100.00"),
                    "Immediate flooding, catastrophic soil erosion, and severe pressure collapse across downstream distribution sectors.",
                    "Isolate distribution valve immediately and inspect physical pipeline segment.",
                    Collections.emptyList()
            );
            factors.add(factor);
            recommendations.add(new RiskRecommendation(
                    UUID.randomUUID(),
                    assessmentId,
                    fieldId,
                    fieldName,
                    "Inspect Mainline Hydraulic Infrastructure Immediately",
                    "Hydraulic safety interlock has blocked pressurization. Dispatch field technician to locate rupture before any irrigation dispatch.",
                    RiskCategory.HYDRAULIC,
                    RiskSeverity.CRITICAL,
                    RiskConfidence.HIGH,
                    ev,
                    "Pipe burst anomaly active on field supply line.",
                    "Verify physical line condition and replace damaged coupling or poly-pipe segment.",
                    1,
                    "ANOMALY:SUSPECTED_PIPE_BURST",
                    "/anomalies?fieldId=" + fieldId,
                    evalTime
            ));
        } else if (ctx.hasCriticalPressureDrop()) {
            hydraulicBlocked = true;
            hydraulicBlockReason = "Hydraulic Safety Interlock: Critical pressure drop detected. Safe delivery pressure unavailable.";
            List<RiskEvidence> ev = List.of(
                    RiskEvidence.of(
                            RiskCategory.HYDRAULIC,
                            "PRESSURE_DROP_ANOMALY",
                            currentPressure != null ? currentPressure : BigDecimal.ZERO,
                            CRITICAL_PRESSURE_COLLAPSE_BAR,
                            "bar",
                            RiskEvidenceType.OBSERVED,
                            RiskEvidenceSource.HYDRAULIC_SAFETY,
                            "Mainline pressure collapsed below minimum operational delivery threshold.",
                            true
                    )
            );
            RiskFactor factor = new RiskFactor(
                    UUID.randomUUID(),
                    RiskCategory.HYDRAULIC,
                    RiskSeverity.CRITICAL,
                    RiskConfidence.HIGH,
                    "Critical Distribution Pressure Loss",
                    hydraulicBlockReason,
                    ev,
                    new BigDecimal("90.00"),
                    "Drip emitter clogging, erratic delivery uniformity, and inadequate root-zone water penetration.",
                    "Verify pump station discharge valve and check for secondary branch leaks.",
                    Collections.emptyList()
            );
            factors.add(factor);
            recommendations.add(new RiskRecommendation(
                    UUID.randomUUID(),
                    assessmentId,
                    fieldId,
                    fieldName,
                    "Verify Pump Station and Line Integrity",
                    "Distribution line pressure collapsed. Check intake filters and pump station status.",
                    RiskCategory.HYDRAULIC,
                    RiskSeverity.CRITICAL,
                    RiskConfidence.HIGH,
                    ev,
                    "Critical pressure collapse detected on distribution line.",
                    "Inspect pump manifold and check for stuck pressure relief valves.",
                    1,
                    "ANOMALY:PRESSURE_DROP",
                    "/anomalies?fieldId=" + fieldId,
                    evalTime
            ));
        } else if (currentPressure != null && currentPressure.compareTo(MIN_HYDRAULIC_PRESSURE_BAR) < 0
                && currentPressure.compareTo(BigDecimal.ZERO) > 0) {
            List<RiskEvidence> ev = List.of(
                    RiskEvidence.of(
                            RiskCategory.HYDRAULIC,
                            "LINE_PRESSURE",
                            currentPressure,
                            MIN_HYDRAULIC_PRESSURE_BAR,
                            "bar",
                            RiskEvidenceType.OBSERVED,
                            RiskEvidenceSource.TELEMETRY,
                            "Distribution line pressure is below nominal design point (" + currentPressure + " bar < " + MIN_HYDRAULIC_PRESSURE_BAR + " bar).",
                            true
                    )
            );
            factors.add(new RiskFactor(
                    UUID.randomUUID(),
                    RiskCategory.HYDRAULIC,
                    RiskSeverity.MEDIUM,
                    RiskConfidence.HIGH,
                    "Sub-optimal Mainline Pressure",
                    "Operating pressure is marginally low; distribution uniformity may be impaired.",
                    ev,
                    new BigDecimal("40.00"),
                    "Inconsistent application depth along drip runs; outer rows may experience deficit.",
                    "Adjust secondary pressure regulating valve before initiating full scheduled run.",
                    Collections.emptyList()
            ));
        }

        // =========================================================================
        // 2. WATER AVAILABILITY RISK
        // =========================================================================
        WaterBudget budget = ctx.waterBudget();
        if (!ctx.hasActiveWaterAllocation()) {
            List<RiskEvidence> ev = List.of(
                    RiskEvidence.of(
                            RiskCategory.WATER_AVAILABILITY,
                            "WATER_ALLOCATION",
                            BigDecimal.ZERO,
                            BigDecimal.ONE,
                            "allocation",
                            RiskEvidenceType.MISSING,
                            RiskEvidenceSource.WATER_BUDGET,
                            "No active seasonal water allocation registered for field parcel.",
                            true
                    )
            );
            missingWarnings.add("Missing seasonal water allocation quota for parcel.");
            factors.add(new RiskFactor(
                    UUID.randomUUID(),
                    RiskCategory.WATER_AVAILABILITY,
                    RiskSeverity.HIGH,
                    RiskConfidence.HIGH,
                    "Missing Water Allocation Quota",
                    "Field parcel has no active seasonal water quota recorded in the water registry.",
                    ev,
                    new BigDecimal("65.00"),
                    "Unregistered irrigation draws risk administrative penalties and water canal shutoff.",
                    "Register seasonal water quota with Yangiariq district water management authority.",
                    List.of("Seasonal water allocation quota")
            ));
            recommendations.add(new RiskRecommendation(
                    UUID.randomUUID(),
                    assessmentId,
                    fieldId,
                    fieldName,
                    "Register Seasonal Water Allocation",
                    "No active allocation found. File water quota request with district distribution center.",
                    RiskCategory.WATER_AVAILABILITY,
                    RiskSeverity.HIGH,
                    RiskConfidence.HIGH,
                    ev,
                    "Absence of legal water quota blocks scheduled water delivery.",
                    "Submit water allocation filing in Water Management module.",
                    4,
                    "WATER_ALLOCATION",
                    "/irrigation?tab=allocations&fieldId=" + fieldId,
                    evalTime
            ));
        } else if (budget != null) {
            BigDecimal remaining = budget.getRemainingAvailableM3();
            BigDecimal allocated = budget.getTotalAllocatedM3();
            BigDecimal consumed = budget.getConsumedM3();
            BigDecimal upcoming = ctx.totalUpcomingPlannedVolumeM3() != null ? ctx.totalUpcomingPlannedVolumeM3() : BigDecimal.ZERO;

            List<RiskEvidence> budgetEv = new ArrayList<>();
            budgetEv.add(RiskEvidence.of(
                    RiskCategory.WATER_AVAILABILITY,
                    "REMAINING_BUDGET",
                    remaining,
                    BigDecimal.ZERO,
                    "m³",
                    RiskEvidenceType.OBSERVED,
                    RiskEvidenceSource.WATER_BUDGET,
                    "Remaining available water quota: " + remaining + " m³ (Allocated: " + allocated + " m³, Consumed: " + consumed + " m³).",
                    true
            ));

            if (remaining.compareTo(BigDecimal.ZERO) <= 0 && (budget.getCommittedM3().compareTo(BigDecimal.ZERO) > 0 || upcoming.compareTo(BigDecimal.ZERO) > 0)) {
                factors.add(new RiskFactor(
                        UUID.randomUUID(),
                        RiskCategory.WATER_AVAILABILITY,
                        RiskSeverity.CRITICAL,
                        RiskConfidence.HIGH,
                        "Water Allocation Quota Exhausted",
                        "Seasonal water budget is completely depleted while irrigation demands remain.",
                        budgetEv,
                        new BigDecimal("85.00"),
                        "Upcoming planned irrigation operations cannot be fulfilled without overdraft penalties.",
                        "Request emergency quota transfer or apply strict deficit irrigation protocols.",
                        Collections.emptyList()
                ));
                recommendations.add(new RiskRecommendation(
                        UUID.randomUUID(),
                        assessmentId,
                        fieldId,
                        fieldName,
                        "Resolve Seasonal Water Quota Shortage",
                        "Water budget exhausted. Submit quota reallocation or postpone non-critical irrigation.",
                        RiskCategory.WATER_AVAILABILITY,
                        RiskSeverity.CRITICAL,
                        RiskConfidence.HIGH,
                        budgetEv,
                        "Remaining quota is zero while upcoming water demand exists.",
                        "Reallocate surplus water from adjacent parcels or submit emergency quota appeal.",
                        4,
                        "WATER_BUDGET:EXHAUSTED",
                        "/irrigation?tab=budget&fieldId=" + fieldId,
                        evalTime
                ));
            } else if (upcoming.compareTo(BigDecimal.ZERO) > 0 && remaining.compareTo(upcoming) < 0) {
                BigDecimal deficit = upcoming.subtract(remaining);
                budgetEv.add(RiskEvidence.of(
                        RiskCategory.WATER_AVAILABILITY,
                        "UPCOMING_DEFICIT",
                        deficit,
                        BigDecimal.ZERO,
                        "m³",
                        RiskEvidenceType.PREDICTED,
                        RiskEvidenceSource.WATER_BUDGET,
                        "Upcoming planned irrigation volume (" + upcoming + " m³) exceeds remaining quota by " + deficit + " m³.",
                        true
                ));
                factors.add(new RiskFactor(
                        UUID.randomUUID(),
                        RiskCategory.WATER_AVAILABILITY,
                        RiskSeverity.HIGH,
                        RiskConfidence.HIGH,
                        "Upcoming Irrigation Exceeds Available Water",
                        "Scheduled irrigation plans for the next week require more water than the remaining budget.",
                        budgetEv,
                        new BigDecimal("70.00"),
                        "Planned irrigation events will be blocked before completion due to quota exhaustion.",
                        "Rebalance scheduled application depth or request volume adjustment.",
                        Collections.emptyList()
                ));
                recommendations.add(new RiskRecommendation(
                        UUID.randomUUID(),
                        assessmentId,
                        fieldId,
                        fieldName,
                        "Adjust Planned Irrigation Volumes to Match Quota",
                        "Upcoming delivery requirements exceed remaining allocation balance by " + deficit + " m³.",
                        RiskCategory.WATER_AVAILABILITY,
                        RiskSeverity.HIGH,
                        RiskConfidence.HIGH,
                        budgetEv,
                        "Upcoming irrigation volume exceeds remaining balance.",
                        "Reduce irrigation runtime in Operations Workflow or request quota increase.",
                        4,
                        "WATER_BUDGET:DEFICIT",
                        "/operations?fieldId=" + fieldId,
                        evalTime
                ));
            } else if (allocated.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal utilPercent = consumed.multiply(HUNDRED).divide(allocated, 2, RoundingMode.HALF_UP);
                if (utilPercent.compareTo(new BigDecimal("90.00")) >= 0) {
                    factors.add(new RiskFactor(
                            UUID.randomUUID(),
                            RiskCategory.WATER_AVAILABILITY,
                            RiskSeverity.MEDIUM,
                            RiskConfidence.HIGH,
                            "High Water Quota Utilization (>90%)",
                            "Parcel has consumed " + utilPercent + "% of its seasonal allocation.",
                            budgetEv,
                            new BigDecimal("40.00"),
                            "Limited headroom remaining for late-season crop maturation.",
                            "Implement water-saving nighttime drip cycles to conserve remaining quota.",
                            Collections.emptyList()
                    ));
                }
            }
        }

        // =========================================================================
        // 3. SOIL MOISTURE RISK
        // =========================================================================
        SensorReading moistureReading = latestReadingsMap.get(SensorType.SOIL_MOISTURE);
        TelemetryTrend moistureTrend = ctx.soilMoistureTrend();
        Double predictedMoisture24h = ctx.predictedMoisture24h();

        if (moistureReading == null) {
            missingWarnings.add("No soil moisture sensor reading available for parcel.");
            factors.add(new RiskFactor(
                    UUID.randomUUID(),
                    RiskCategory.SOIL_MOISTURE,
                    RiskSeverity.HIGH,
                    RiskConfidence.INSUFFICIENT_DATA,
                    "Soil Moisture Data Unavailable",
                    "No active soil moisture telemetry detected. Parcel root-zone status cannot be confirmed.",
                    List.of(RiskEvidence.of(
                            RiskCategory.SOIL_MOISTURE,
                            "SOIL_MOISTURE",
                            BigDecimal.ZERO,
                            CRITICAL_MOISTURE_THRESHOLD,
                            "%",
                            RiskEvidenceType.MISSING,
                            RiskEvidenceSource.TELEMETRY,
                            "Soil moisture sensor reading is completely missing.",
                            false
                    )),
                    new BigDecimal("60.00"),
                    "Undetected crop water stress or undetected waterlogging leading to crop damage.",
                    "Conduct manual soil probe test and verify soil moisture sensor telemetry.",
                    List.of("Soil moisture telemetry reading")
            ));
            recommendations.add(new RiskRecommendation(
                    UUID.randomUUID(),
                    assessmentId,
                    fieldId,
                    fieldName,
                    "Verify Soil Moisture Manually & Restore Telemetry",
                    "No automated moisture readings available. Inspect parcel probe and take manual core sample.",
                    RiskCategory.SOIL_MOISTURE,
                    RiskSeverity.HIGH,
                    RiskConfidence.INSUFFICIENT_DATA,
                    Collections.emptyList(),
                    "Missing root-zone moisture telemetry prevents automated stress detection.",
                    "Inspect probe connection in Sensors module and record manual measurement.",
                    5,
                    "SENSOR:MISSING",
                    "/sensors?fieldId=" + fieldId,
                    evalTime
            ));
        } else {
            BigDecimal moistureVal = moistureReading.getValue();
            boolean isReadingStale = isReadingStale(moistureReading.getMeasuredAt(), evalTime);
            boolean isReadingSuspect = moistureReading.getQuality() == ReadingQuality.SUSPECT;
            boolean isReadingInvalid = moistureReading.getQuality() == ReadingQuality.INVALID;

            RiskConfidence moistureConf = isReadingInvalid ? RiskConfidence.LOW
                    : (isReadingStale || isReadingSuspect ? RiskConfidence.MEDIUM : RiskConfidence.HIGH);

            List<RiskEvidence> moistureEv = new ArrayList<>();
            moistureEv.add(RiskEvidence.of(
                    RiskCategory.SOIL_MOISTURE,
                    "SOIL_MOISTURE",
                    moistureVal,
                    CRITICAL_MOISTURE_THRESHOLD,
                    "%",
                    isReadingInvalid ? RiskEvidenceType.UNRELIABLE : RiskEvidenceType.OBSERVED,
                    RiskEvidenceSource.TELEMETRY,
                    "Current ground-truth soil moisture: " + moistureVal + "% (Quality: " + moistureReading.getQuality()
                            + ", Recorded: " + moistureReading.getMeasuredAt() + ").",
                    moistureConf == RiskConfidence.HIGH
            ));

            if (moistureTrend != null) {
                moistureEv.add(RiskEvidence.of(
                        RiskCategory.SOIL_MOISTURE,
                        "MOISTURE_TREND",
                        moistureTrend.getRateOfChangePerHour(),
                        BigDecimal.ZERO,
                        "%/hr",
                        RiskEvidenceType.OBSERVED,
                        RiskEvidenceSource.TELEMETRY,
                        "24h Moisture rate of change: " + moistureTrend.getRateOfChangePerHour() + "%/hr (" + moistureTrend.getDirection() + ").",
                        true
                ));
            }

            if (moistureVal.compareTo(CRITICAL_MOISTURE_THRESHOLD) < 0) {
                factors.add(new RiskFactor(
                        UUID.randomUUID(),
                        RiskCategory.SOIL_MOISTURE,
                        RiskSeverity.CRITICAL,
                        moistureConf,
                        "Critical Soil Moisture Deficit (<15%)",
                        "Soil moisture has fallen below permanent wilting point baseline (" + moistureVal + "% < 15.0%).",
                        moistureEv,
                        new BigDecimal("80.00"),
                        "Imminent plant tissue desiccation, flower shedding, and irreversible yield collapse.",
                        "Initiate emergency irrigation run in Operations Workflow immediately if hydraulic safety permits.",
                        Collections.emptyList()
                ));
                recommendations.add(new RiskRecommendation(
                        UUID.randomUUID(),
                        assessmentId,
                        fieldId,
                        fieldName,
                        "Schedule Emergency Irrigation Run",
                        "Soil moisture is critically low at " + moistureVal + "%. Root zone is approaching wilting point.",
                        RiskCategory.SOIL_MOISTURE,
                        RiskSeverity.CRITICAL,
                        moistureConf,
                        moistureEv,
                        "Ground-truth moisture is critically below agronomic safe threshold (15%).",
                        "Approve and dispatch irrigation plan in Operations Workflow.",
                        3,
                        "SOIL_MOISTURE:CRITICAL",
                        "/operations?fieldId=" + fieldId,
                        evalTime
                ));
            } else if (moistureVal.compareTo(DEFICIT_MOISTURE_THRESHOLD) < 0) {
                boolean fastDrop = moistureTrend != null
                        && moistureTrend.getDirection() == TrendDirection.DECREASING
                        && moistureTrend.getRateOfChangePerHour().compareTo(new BigDecimal("-0.1500")) < 0;

                factors.add(new RiskFactor(
                        UUID.randomUUID(),
                        RiskCategory.SOIL_MOISTURE,
                        fastDrop ? RiskSeverity.HIGH : RiskSeverity.MEDIUM,
                        moistureConf,
                        fastDrop ? "Rapid Soil Drying Towards Wilting Point" : "Moderate Soil Moisture Deficit",
                        "Soil moisture is in stress range (" + moistureVal + "%). " + (fastDrop ? "Rapid depletion detected." : "Approaching irrigation threshold."),
                        moistureEv,
                        fastDrop ? new BigDecimal("65.00") : new BigDecimal("45.00"),
                        "Continued depletion will cross critical wilting threshold within 12-24 hours.",
                        "Queue irrigation operation in Smart Irrigation Planning.",
                        Collections.emptyList()
                ));
                recommendations.add(new RiskRecommendation(
                        UUID.randomUUID(),
                        assessmentId,
                        fieldId,
                        fieldName,
                        "Prepare Next Irrigation Run",
                        "Soil moisture is at " + moistureVal + "% and declining. Prepare irrigation plan for delivery.",
                        RiskCategory.SOIL_MOISTURE,
                        fastDrop ? RiskSeverity.HIGH : RiskSeverity.MEDIUM,
                        moistureConf,
                        moistureEv,
                        "Soil moisture is entering agronomic stress zone.",
                        "Review proposed smart irrigation plan in Irrigation Planning.",
                        5,
                        "SOIL_MOISTURE:DEFICIT",
                        "/operations?fieldId=" + fieldId,
                        evalTime
                ));
            } else if (moistureVal.compareTo(SATURATION_MOISTURE_THRESHOLD) > 0) {
                factors.add(new RiskFactor(
                        UUID.randomUUID(),
                        RiskCategory.SOIL_MOISTURE,
                        RiskSeverity.MEDIUM,
                        moistureConf,
                        "Excessive Soil Moisture (Waterlogging Risk)",
                        "Soil moisture is high (" + moistureVal + "% > 35.0%), indicating saturation or standing water.",
                        moistureEv,
                        new BigDecimal("35.00"),
                        "Root hypoxia, fungal pathogen propagation, and nutrient leaching in active root zone.",
                        "Withhold irrigation until soil drains to field capacity (~25-30%). Inspect drainage canals.",
                        Collections.emptyList()
                ));
            }

            // Forecasted future soil moisture risk (distinct from observed!)
            if (predictedMoisture24h != null && predictedMoisture24h < 15.0 && moistureVal.compareTo(DEFICIT_MOISTURE_THRESHOLD) >= 0) {
                List<RiskEvidence> predEv = List.of(
                        RiskEvidence.of(
                                RiskCategory.SOIL_MOISTURE,
                                "PREDICTED_MOISTURE_24H",
                                BigDecimal.valueOf(predictedMoisture24h).setScale(2, RoundingMode.HALF_UP),
                                CRITICAL_MOISTURE_THRESHOLD,
                                "%",
                                RiskEvidenceType.PREDICTED,
                                RiskEvidenceSource.PREDICTION_ENGINE,
                                "Predictive model forecasts soil moisture dropping to " + String.format("%.1f", predictedMoisture24h) + "% within 24 hours.",
                                false
                        )
                );
                factors.add(new RiskFactor(
                        UUID.randomUUID(),
                        RiskCategory.SOIL_MOISTURE,
                        RiskSeverity.HIGH,
                        RiskConfidence.MEDIUM,
                        "Forecasted Moisture Depletion (Next 24h)",
                        "Predictive engine projects root-zone moisture breaching critical wilting threshold due to weather and crop ET.",
                        predEv,
                        new BigDecimal("50.00"),
                        "Anticipated moisture deficit will develop unless proactive preventive hydration is scheduled.",
                        "Review 24-hour predictive forecast and prepare proactive irrigation schedule.",
                        Collections.emptyList()
                ));
            }
        }

        // =========================================================================
        // 4. IRRIGATION OPERATIONAL RISK
        // =========================================================================
        List<SmartIrrigationPlan> plans = ctx.activePlans();
        boolean hasScheduleConflict = ctx.hasScheduleConflict();
        LocalDate today = LocalDate.now();

        if (hasScheduleConflict) {
            List<RiskEvidence> conflictEv = List.of(
                    RiskEvidence.of(
                            RiskCategory.IRRIGATION_OPERATIONAL,
                            "SCHEDULE_CONFLICT",
                            BigDecimal.ONE,
                            BigDecimal.ZERO,
                            "conflict",
                            RiskEvidenceType.OBSERVED,
                            RiskEvidenceSource.IRRIGATION_OPERATION,
                            "Canal delivery contention detected: Multiple parcels scheduled simultaneously on shared canal branch.",
                            true
                    )
            );
            factors.add(new RiskFactor(
                    UUID.randomUUID(),
                    RiskCategory.IRRIGATION_OPERATIONAL,
                    RiskSeverity.HIGH,
                    RiskConfidence.HIGH,
                    "Secondary Canal Schedule Contention",
                    "Simultaneous irrigation scheduled on shared canal exceeds delivery capacity.",
                    conflictEv,
                    new BigDecimal("55.00"),
                    "Inadequate water flow velocity, incomplete delivery, and pump pressure drops.",
                    "Stagger execution windows in Decision-to-Action operations center.",
                    Collections.emptyList()
            ));
            recommendations.add(new RiskRecommendation(
                    UUID.randomUUID(),
                    assessmentId,
                    fieldId,
                    fieldName,
                    "Resolve Canal Delivery Schedule Conflict",
                    "Overlapping delivery windows detected on shared branch canal. Stagger delivery start times.",
                    RiskCategory.IRRIGATION_OPERATIONAL,
                    RiskSeverity.HIGH,
                    RiskConfidence.HIGH,
                    conflictEv,
                    "Simultaneous delivery requests exceed secondary canal discharge limit.",
                    "Adjust start time in Operations Workflow to decouple overlapping schedules.",
                    6,
                    "OPERATION:SCHEDULE_CONFLICT",
                    "/operations?fieldId=" + fieldId,
                    evalTime
            ));
        }

        if (plans != null) {
            for (SmartIrrigationPlan p : plans) {
                if ((p.getStatus() == IrrigationPlanStatus.SCHEDULED || p.getStatus() == IrrigationPlanStatus.APPROVED)
                        && p.getPlannedDate() != null && p.getPlannedDate().isBefore(today)) {
                    List<RiskEvidence> delayEv = List.of(
                            RiskEvidence.of(
                                    RiskCategory.IRRIGATION_OPERATIONAL,
                                    "PLAN_OVERDUE",
                                    BigDecimal.ONE,
                                    BigDecimal.ZERO,
                                    "plan",
                                    RiskEvidenceType.OBSERVED,
                                    RiskEvidenceSource.IRRIGATION_OPERATION,
                                    "Plan #" + p.getId().toString().substring(0, 8) + " was scheduled for " + p.getPlannedDate() + " but remains unexecuted.",
                                    true
                            )
                    );
                    factors.add(new RiskFactor(
                            UUID.randomUUID(),
                            RiskCategory.IRRIGATION_OPERATIONAL,
                            RiskSeverity.HIGH,
                            RiskConfidence.HIGH,
                            "Overdue Irrigation Operation",
                            "Irrigation plan missed its scheduled delivery window without execution confirmation.",
                            delayEv,
                            new BigDecimal("50.00"),
                            "Crop drought exposure intensifies as scheduled water application is delayed.",
                            "Execute plan now or reschedule delivery window.",
                            Collections.emptyList()
                    ));
                    recommendations.add(new RiskRecommendation(
                            UUID.randomUUID(),
                            assessmentId,
                            fieldId,
                            fieldName,
                            "Execute or Reschedule Overdue Irrigation",
                            "Scheduled delivery date (" + p.getPlannedDate() + ") has passed. Start operation or update plan.",
                            RiskCategory.IRRIGATION_OPERATIONAL,
                            RiskSeverity.HIGH,
                            RiskConfidence.HIGH,
                            delayEv,
                            "Delayed water application prolongs moisture deficit.",
                            "Open Operations Center to start execution or reschedule.",
                            6,
                            "OPERATION:OVERDUE_PLAN",
                            "/operations?fieldId=" + fieldId,
                            evalTime
                    ));
                    break;
                }
            }
        }

        // =========================================================================
        // 5. SENSOR AND DATA RELIABILITY RISK
        // =========================================================================
        List<Sensor> fieldSensors = ctx.fieldSensors();
        if (fieldSensors == null || fieldSensors.isEmpty()) {
            missingWarnings.add("No telemetry sensors configured or registered for field parcel.");
            factors.add(new RiskFactor(
                    UUID.randomUUID(),
                    RiskCategory.SENSOR_DATA_RELIABILITY,
                    RiskSeverity.HIGH,
                    RiskConfidence.INSUFFICIENT_DATA,
                    "No Monitoring Hardware Configured",
                    "Parcel has no IoT telemetry station or sensor devices registered.",
                    Collections.emptyList(),
                    new BigDecimal("50.00"),
                    "Total monitoring blindspot; platform cannot provide automated agronomic guarantees.",
                    "Provision IoT station and soil moisture probes for parcel in Sensors module.",
                    List.of("Field sensor devices")
            ));
        } else {
            int offlineCount = 0;
            int staleCount = 0;
            for (Sensor s : fieldSensors) {
                if (s.getStatus() == SensorStatus.OFFLINE || s.getStatus() == SensorStatus.MAINTENANCE) {
                    offlineCount++;
                }
            }

            if (moistureReading != null && isReadingStale(moistureReading.getMeasuredAt(), evalTime)) {
                staleCount++;
            }

            if (offlineCount > 0) {
                List<RiskEvidence> sensorEv = List.of(
                        RiskEvidence.of(
                                RiskCategory.SENSOR_DATA_RELIABILITY,
                                "OFFLINE_SENSORS",
                                BigDecimal.valueOf(offlineCount),
                                BigDecimal.ZERO,
                                "count",
                                RiskEvidenceType.OBSERVED,
                                RiskEvidenceSource.TELEMETRY,
                                offlineCount + " sensor device(s) are currently OFFLINE or under MAINTENANCE.",
                                true
                        )
                );
                factors.add(new RiskFactor(
                        UUID.randomUUID(),
                        RiskCategory.SENSOR_DATA_RELIABILITY,
                        offlineCount >= 2 ? RiskSeverity.HIGH : RiskSeverity.MEDIUM,
                        RiskConfidence.HIGH,
                        "Hardware Sensors Offline",
                        offlineCount + " sensor device(s) on parcel have lost telemetry connectivity.",
                        sensorEv,
                        offlineCount >= 2 ? new BigDecimal("50.00") : new BigDecimal("30.00"),
                        "Degraded telemetry coverage leaves crop health indicators unmonitored.",
                        "Inspect solar charge, SIM/LoRaWAN connection, and sensor probe cables.",
                        Collections.emptyList()
                ));
                recommendations.add(new RiskRecommendation(
                        UUID.randomUUID(),
                        assessmentId,
                        fieldId,
                        fieldName,
                        "Inspect Offline Sensors on Parcel",
                        offlineCount + " sensor(s) have dropped offline. Verify power supply and transmission.",
                        RiskCategory.SENSOR_DATA_RELIABILITY,
                        offlineCount >= 2 ? RiskSeverity.HIGH : RiskSeverity.MEDIUM,
                        RiskConfidence.HIGH,
                        sensorEv,
                        "Sensor disconnection impairs automated risk and irrigation monitoring.",
                        "Open Sensors module to review station health and diagnose device errors.",
                        7,
                        "SENSOR:OFFLINE",
                        "/sensors?fieldId=" + fieldId,
                        evalTime
                ));
            } else if (staleCount > 0) {
                List<RiskEvidence> staleEv = List.of(
                        RiskEvidence.of(
                                RiskCategory.SENSOR_DATA_RELIABILITY,
                                "STALE_TELEMETRY",
                                BigDecimal.valueOf(staleCount),
                                BigDecimal.ZERO,
                                "count",
                                RiskEvidenceType.UNRELIABLE,
                                RiskEvidenceSource.TELEMETRY,
                                "Latest telemetry transmission is older than 6 hours.",
                                true
                        )
                );
                factors.add(new RiskFactor(
                        UUID.randomUUID(),
                        RiskCategory.SENSOR_DATA_RELIABILITY,
                        RiskSeverity.MEDIUM,
                        RiskConfidence.LOW,
                        "Stale Telemetry Feed (>6h)",
                        "Sensor readings have not refreshed in over 6 hours; assessment uses aged data.",
                        staleEv,
                        new BigDecimal("25.00"),
                        "Risk assessment may lag actual drying trends or unexpected system events.",
                        "Check field gateway transmission intervals and node battery status.",
                        Collections.emptyList()
                ));
            }
        }

        // =========================================================================
        // 6. PREDICTIVE MODEL RELIABILITY RISK
        // =========================================================================
        ModelHealthStatus modelHealth = ctx.predictionModelHealth();
        boolean modelDrift = ctx.predictionModelDrift();
        int sampleCount = ctx.predictionSampleCount();

        if (modelHealth == ModelHealthStatus.DEGRADED) {
            List<RiskEvidence> modelEv = List.of(
                    RiskEvidence.of(
                            RiskCategory.PREDICTIVE_MODEL_RELIABILITY,
                            "MODEL_HEALTH",
                            BigDecimal.valueOf(modelHealth.ordinal()),
                            BigDecimal.ZERO,
                            "status",
                            RiskEvidenceType.UNRELIABLE,
                            RiskEvidenceSource.MODEL_GOVERNANCE,
                            "Predictive forecasting model health status is " + modelHealth + " with elevated error rates.",
                            true
                    )
            );
            factors.add(new RiskFactor(
                    UUID.randomUUID(),
                    RiskCategory.PREDICTIVE_MODEL_RELIABILITY,
                    RiskSeverity.HIGH,
                    RiskConfidence.MEDIUM,
                    "Predictive Model Degraded",
                    "Forecasting model error variance exceeds governed quality threshold. Treat predictions as advisory.",
                    modelEv,
                    new BigDecimal("35.00"),
                    "Predictive water demand and moisture projections may diverge from actual parcel response.",
                    "Rely strictly on ground-truth sensor telemetry; review pending calibration proposal.",
                    Collections.emptyList()
            ));
            recommendations.add(new RiskRecommendation(
                    UUID.randomUUID(),
                    assessmentId,
                    fieldId,
                    fieldName,
                    "Review Model Governance & Calibration",
                    "Production forecasting model is degraded. Inspect model evaluation metrics.",
                    RiskCategory.PREDICTIVE_MODEL_RELIABILITY,
                    RiskSeverity.MEDIUM,
                    RiskConfidence.MEDIUM,
                    modelEv,
                    "High forecasting error variance impairs long-horizon planning.",
                    "Review model evaluation and promote calibrated candidate in Predictions module.",
                    8,
                    "MODEL_GOVERNANCE:DEGRADED",
                    "/predictions?tab=governance",
                    evalTime
            ));
        } else if (modelDrift) {
            List<RiskEvidence> driftEv = List.of(
                    RiskEvidence.of(
                            RiskCategory.PREDICTIVE_MODEL_RELIABILITY,
                            "MODEL_DRIFT",
                            BigDecimal.ONE,
                            BigDecimal.ZERO,
                            "flag",
                            RiskEvidenceType.UNRELIABLE,
                            RiskEvidenceSource.MODEL_GOVERNANCE,
                            "Contextual drift detected between predicted values and verified ground truth.",
                            true
                    )
            );
            factors.add(new RiskFactor(
                    UUID.randomUUID(),
                    RiskCategory.PREDICTIVE_MODEL_RELIABILITY,
                    RiskSeverity.MEDIUM,
                    RiskConfidence.MEDIUM,
                    "Predictive Model Drift Active",
                    "Seasonal or contextual shift observed in prediction accuracy.",
                    driftEv,
                    new BigDecimal("25.00"),
                    "Predictive accuracy is declining compared to governed baseline.",
                    "Inspect champion-challenger performance in Predictions Center.",
                    Collections.emptyList()
            ));
        } else if (sampleCount > 0 && sampleCount < 30) {
            factors.add(new RiskFactor(
                    UUID.randomUUID(),
                    RiskCategory.PREDICTIVE_MODEL_RELIABILITY,
                    RiskSeverity.LOW,
                    RiskConfidence.LOW,
                    "Limited Historical Ground-Truth Samples",
                    "Evaluation relies on only " + sampleCount + " verified outcomes (< 30 required for full statistical significance).",
                    Collections.emptyList(),
                    new BigDecimal("15.00"),
                    "Statistical evaluation confidence is low; model predictions remain experimental.",
                    "Accumulate post-irrigation ground truth before relying on automated scheduling.",
                    List.of("Historical outcome ground truth (> 30 samples)")
            ));
        }

        // =========================================================================
        // COMPOSITE RISK SCORE CALCULATION
        // =========================================================================
        BigDecimal riskScore = calculateCompositeScore(factors, hydraulicBlocked);

        // =========================================================================
        // OVERALL SEVERITY, CONFIDENCE & TREND EVALUATION
        // =========================================================================
        RiskSeverity overallSeverity = determineOverallSeverity(factors, hydraulicBlocked);
        RiskConfidence overallConfidence = determineOverallConfidence(factors, missingWarnings);
        RiskTrend riskTrend = determineRiskTrend(riskScore, ctx.previousRiskScore());

        // Primary Risk Category
        RiskCategory primaryCategory = determinePrimaryCategory(factors);

        // =========================================================================
        // DETERMINISTIC PRIORITIZATION HIERARCHY (1..9)
        // =========================================================================
        int priorityRank = determinePriorityRank(factors, hydraulicBlocked, overallSeverity, overallConfidence);
        String priorityReason = composePriorityReason(priorityRank, hydraulicBlocked, factors);

        // Sort recommendations by priorityRank ascending
        recommendations.sort(Comparator.comparingInt(RiskRecommendation::priorityRank));

        return FarmRiskAssessment.builder()
                .id(assessmentId)
                .fieldId(fieldId)
                .fieldName(fieldName)
                .overallSeverity(overallSeverity)
                .overallConfidence(overallConfidence)
                .riskTrend(riskTrend)
                .riskScore(riskScore)
                .isHydraulicBlocked(hydraulicBlocked)
                .hydraulicBlockReason(hydraulicBlockReason)
                .primaryRiskCategory(primaryCategory)
                .lifecycleStatus(RiskAssessmentLifecycleStatus.OPEN)
                .priorityRank(priorityRank)
                .priorityReason(priorityReason)
                .riskFactors(factors)
                .recommendations(recommendations)
                .missingDataWarnings(missingWarnings)
                .evaluatedAt(evalTime)
                .createdAt(evalTime)
                .updatedAt(evalTime)
                .build();
    }

    private BigDecimal calculateCompositeScore(List<RiskFactor> factors, boolean hydraulicBlocked) {
        if (hydraulicBlocked) {
            return HUNDRED;
        }
        if (factors.isEmpty()) {
            return ZERO;
        }

        BigDecimal maxScore = ZERO;
        BigDecimal sumOther = ZERO;

        for (RiskFactor f : factors) {
            BigDecimal score = f.contributionScore();
            if (score.compareTo(maxScore) > 0) {
                sumOther = sumOther.add(maxScore);
                maxScore = score;
            } else {
                sumOther = sumOther.add(score);
            }
        }

        // Formula: maxScore * 0.70 + min(30, sumOther * 0.15)
        BigDecimal weighted = maxScore.multiply(new BigDecimal("0.70"))
                .add(sumOther.multiply(new BigDecimal("0.15")));

        if (weighted.compareTo(HUNDRED) > 0) {
            return HUNDRED;
        }
        if (weighted.compareTo(ZERO) < 0) {
            return ZERO;
        }
        return weighted.setScale(2, RoundingMode.HALF_UP);
    }

    private RiskSeverity determineOverallSeverity(List<RiskFactor> factors, boolean hydraulicBlocked) {
        if (hydraulicBlocked) {
            return RiskSeverity.CRITICAL;
        }
        if (factors.isEmpty()) {
            return RiskSeverity.LOW;
        }

        RiskSeverity highest = RiskSeverity.LOW;
        for (RiskFactor f : factors) {
            if (f.severity() == RiskSeverity.CRITICAL) {
                return RiskSeverity.CRITICAL;
            }
            if (f.severity() == RiskSeverity.HIGH && highest != RiskSeverity.CRITICAL) {
                highest = RiskSeverity.HIGH;
            } else if (f.severity() == RiskSeverity.MEDIUM && highest == RiskSeverity.LOW) {
                highest = RiskSeverity.MEDIUM;
            }
        }
        return highest;
    }

    private RiskConfidence determineOverallConfidence(List<RiskFactor> factors, List<String> missingWarnings) {
        if (missingWarnings != null && !missingWarnings.isEmpty()) {
            for (String w : missingWarnings) {
                if (w.contains("soil moisture") || w.contains("sensor reading")) {
                    return RiskConfidence.INSUFFICIENT_DATA;
                }
            }
        }

        boolean hasLow = false;
        boolean hasMedium = false;
        boolean hasInsufficient = false;

        for (RiskFactor f : factors) {
            if (f.confidence() == RiskConfidence.INSUFFICIENT_DATA) hasInsufficient = true;
            if (f.confidence() == RiskConfidence.LOW) hasLow = true;
            if (f.confidence() == RiskConfidence.MEDIUM) hasMedium = true;
        }

        if (hasInsufficient) return RiskConfidence.INSUFFICIENT_DATA;
        if (hasLow) return RiskConfidence.LOW;
        if (hasMedium) return RiskConfidence.MEDIUM;
        return RiskConfidence.HIGH;
    }

    private RiskTrend determineRiskTrend(BigDecimal currentScore, BigDecimal previousScore) {
        if (previousScore == null) {
            return RiskTrend.UNKNOWN;
        }
        BigDecimal delta = currentScore.subtract(previousScore);
        if (delta.compareTo(new BigDecimal("-5.00")) <= 0) {
            return RiskTrend.IMPROVING;
        }
        if (delta.compareTo(new BigDecimal("5.00")) >= 0) {
            return RiskTrend.DEGRADING;
        }
        return RiskTrend.STABLE;
    }

    private RiskCategory determinePrimaryCategory(List<RiskFactor> factors) {
        if (factors.isEmpty()) {
            return RiskCategory.WATER_AVAILABILITY;
        }
        RiskFactor top = factors.get(0);
        for (RiskFactor f : factors) {
            if (f.contributionScore().compareTo(top.contributionScore()) > 0) {
                top = f;
            }
        }
        return top.category();
    }

    private int determinePriorityRank(
            List<RiskFactor> factors,
            boolean hydraulicBlocked,
            RiskSeverity overallSeverity,
            RiskConfidence overallConfidence
    ) {
        // Hierarchy:
        // 1. Hydraulic safety emergencies
        if (hydraulicBlocked) {
            return 1;
        }

        // 2. Confirmed critical operational problems (over-consumption with exhausted allocation, pump failure)
        for (RiskFactor f : factors) {
            if (f.category() == RiskCategory.WATER_AVAILABILITY && f.severity() == RiskSeverity.CRITICAL) {
                return 2;
            }
        }

        // 3. High-confidence critical agricultural risks (soil moisture < 15% with high confidence)
        for (RiskFactor f : factors) {
            if (f.category() == RiskCategory.SOIL_MOISTURE && f.severity() == RiskSeverity.CRITICAL && f.confidence() == RiskConfidence.HIGH) {
                return 3;
            }
        }

        // 4. Water shortage risks
        for (RiskFactor f : factors) {
            if (f.category() == RiskCategory.WATER_AVAILABILITY && f.severity() == RiskSeverity.HIGH) {
                return 4;
            }
        }

        // 5. Severe soil moisture risks
        for (RiskFactor f : factors) {
            if (f.category() == RiskCategory.SOIL_MOISTURE && (f.severity() == RiskSeverity.CRITICAL || f.severity() == RiskSeverity.HIGH)) {
                return 5;
            }
        }

        // 6. Irrigation delays
        for (RiskFactor f : factors) {
            if (f.category() == RiskCategory.IRRIGATION_OPERATIONAL && f.severity() == RiskSeverity.HIGH) {
                return 6;
            }
        }

        // 7. Sensor and telemetry reliability problems
        for (RiskFactor f : factors) {
            if (f.category() == RiskCategory.SENSOR_DATA_RELIABILITY && (f.severity() == RiskSeverity.HIGH || f.severity() == RiskSeverity.CRITICAL)) {
                return 7;
            }
        }

        // 8. Model reliability warnings
        for (RiskFactor f : factors) {
            if (f.category() == RiskCategory.PREDICTIVE_MODEL_RELIABILITY && f.severity() != RiskSeverity.LOW) {
                return 8;
            }
        }

        // 9. Preventive recommendations / nominal conditions
        return 9;
    }

    private String composePriorityReason(int rank, boolean hydraulicBlocked, List<RiskFactor> factors) {
        if (hydraulicBlocked) {
            return "Rank 1: Hydraulic safety emergency. Pipeline rupture or line pressure collapse detected. Pressurization strictly blocked.";
        }
        return switch (rank) {
            case 2 -> "Rank 2: Confirmed critical operational problem. Seasonal water quota exhausted while pending delivery exists.";
            case 3 -> "Rank 3: High-confidence critical agricultural risk. Soil moisture below 15% confirmed by fresh telemetry.";
            case 4 -> "Rank 4: Water shortage risk. Upcoming irrigation requirements exceed remaining allocation balance.";
            case 5 -> "Rank 5: Severe soil moisture risk. Root-zone drying trend indicates imminent deficit.";
            case 6 -> "Rank 6: Irrigation delay or secondary canal contention detected in Operations Workflow.";
            case 7 -> "Rank 7: Telemetry reliability problem. Hardware sensor offline or reading missing (>24h).";
            case 8 -> "Rank 8: Predictive model reliability warning. Forecasting error variance elevated.";
            default -> "Rank 9: Nominal agricultural and operational baseline.";
        };
    }

    private boolean isReadingStale(Instant recordedAt, Instant evalTime) {
        if (recordedAt == null || evalTime == null) return true;
        return Duration.between(recordedAt, evalTime).toHours() >= 6;
    }
}
