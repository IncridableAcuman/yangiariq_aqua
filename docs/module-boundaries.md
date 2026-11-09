# Module Boundaries & Ownership

In Yangiariq Aqua AI, every backend module has a strictly bounded context. Modules must never bypass public contracts or directly manipulate database tables belonging to other modules.

| Module | Core Responsibilities | Owned Entities | External Ports / Dependencies |
| :--- | :--- | :--- | :--- |
| **`auth`** | Token issuing, login, registration, password hashing, session lifecycle, password reset | `RefreshTokenSession`, `PasswordResetToken` | Interacts with `user` via `UserRepository` or `UserService`; uses `PasswordResetNotificationProvider` |
| **`user`** | Farmer profiles, operator credentials, roles, user self-service | `User` | Shared security context (`SecurityContextHelper`) |
| **`field`** | Parcels, crop types, soil characteristics, boundaries | `Field` | References `User` (farmerId) |
| **`water`** | Irrigation logs, water delivery, volumetric intake | `IrrigationRecord` | References `Field` (fieldId), `User` (recordedBy) |
| **`problem`**| Canal blockage, water shortage, leak reports, status | `ProblemReport` | References `User` (reportedBy), `Field` (optional fieldId) |
| **`ai`** | Recommendation orchestration, prompt generation | - | Defines `AiProvider`, depends on AI adapter |
| **`sensor`** | Sensor lifecycle, time-series telemetry observations, historical analytics & bucketing (`5m`–`1w`), hardware availability & data quality metrics, telemetry retention policy, telemetry drift simulation, provider SPI | `Sensor`, `SensorReading` | Defines `SensorProvider` SPI; implementations: `SimulatedSensorProvider`; references `Field` (fieldId) via `FieldOwnershipChecker`; exposes `SensorAnalyticsRepository`; publishes `TelemetryReadingRecordedEvent` |
| **`anomaly`** | Real-time telemetry anomaly detection, statistical Z-score algorithms, agricultural threshold evaluation, multi-metric pipe burst corroboration, alert lifecycle state machine, active deduplication, rule configuration | `Anomaly`, `AnomalyEvidence`, `AnomalyRuleConfig` | Listens to `TelemetryReadingRecordedEvent`; queries `FieldRepository` (ownership isolation) and `SensorReadingRepository` (moving baseline); defines `AnomalyDetector` SPI and `AnomalyNotificationPort` |
| **`recommendation`** | AI agronomic recommendations & decision support, hybrid deterministic safety interlocks, Gemini 1.5 Flash LLM advisory adapter, Hargreaves-Samani ET0 estimation, multi-factor confidence calibration, recommendation lifecycle & cooldown deduplication | `AgronomicRecommendation`, `RecommendationEvidence`, `RecommendedAction` | Consumes `FieldRepository`, `SensorReadingRepository`, `AnomalyRepository`, `WaterConsumptionQueryRepository`; defines `WeatherDataProvider`, `EvapotranspirationCalculator`, `AgronomicAIProvider`, `RecommendationNotificationPort` |
| **`irrigation`** | Smart irrigation planning & water allocation optimization, deterministic scheduling engine, hydraulic safety interlock, root-zone deficit model with crop coefficients & ET0, multi-tier water budget accounting, priority scoring & conflict resolution, plan lifecycle state machine, human approval workflow | `SmartIrrigationPlan`, `PlanConstraint`, `PlanTradeoff`, `WaterBudget`, `IrrigationRequirement` | Defines `FieldDataPort`, `TelemetryDataPort`, `AnomalyDataPort`, `AgronomicContextPort`, `WaterBudgetDataPort`, `IrrigationAIExplanationPort`, `SmartIrrigationPlanRepository` |
| **`intelligence`** | Unified telemetry intelligence, temporal trend evaluation (Δmoisture/Δtime), telemetry freshness SLA tracking, deterministic agronomic assessment engine, safety-aware decision-support recommendation generation | `AgronomicAssessment`, `TelemetrySignal`, `TelemetryTrend`, `AgronomicSignal`, `IntelligenceRecommendation` | Defines `IntelligenceFieldPort`, `IntelligenceTelemetryPort`, `IntelligenceAnomalyPort`, `IntelligenceWaterBudgetPort`, `IntelligenceHydraulicSafetyPort`, `IntelligenceAIExplanationPort`, `AgronomicAssessmentRepository` |
| **`dashboard`**| Farm intelligence workspace & action center, read-model aggregation, deterministic priority scoring, urgent action orchestration, multi-module KPI synthesis (parcels, water balance, sensors, anomalies, recommendations, irrigation plans) | - | Read-only batch queries across `field`, `sensor`, `anomaly`, `recommendation`, `irrigation`, `water`, `problem`; Anti-IDOR tenant isolation |
| **`operations`** | Decision-to-action workflow orchestration, 7-step operational timeline derivation, safety interlock revalidation, conflict detection, human approval guardrails, manual operation execution logging, post-irrigation verification & learning loop | - | Orchestrates domain aggregates (`SmartIrrigationPlan`, `WaterBudget`, `HydraulicSafetyInterlock`, `AgronomicRecommendation`, `IrrigationEvent`); zero N+1 operational aggregator; tenant anti-IDOR isolation |
| **`analytics`** | Farm performance, historical intelligence & analytics read-model, metric governance (utilization vs variance), chronological trends, explainable field rankings with safety dominance, period-over-period comparison, deterministic actionable insight engine | - | Read-only batch queries via `AnalyticsQueryPort` across `water`, `irrigation`, `sensor`, `anomaly`, `field`; Zero N+1 projections; tenant anti-IDOR isolation |
| **`prediction`** | Predictive farm intelligence & forecasting, statistical soil moisture trajectories via FAO-56 regression, irrigation likelihood via MAD thresholds, multi-horizon water demand, water quota headroom risk, irrigation plan hydraulic safety enforcement, 14-day telemetry reliability forecast, explainable AI modal (WHAT, WHY, EVIDENCE, ASSUMPTIONS, NEXT STEP); Continuous Intelligence & Model Evaluation (Milestone 08.8): prediction-outcome matching, numerical error (MAE, RMSE, safe MAPE, MBE), classification confusion matrix, confidence calibration, data quality impact, performance drift detection, and human-in-the-loop decision audit; Adaptive Intelligence & Model Governance (Milestone 08.9): immutable method versioning (`PredictionMethodVersion`), candidate calibration proposals (`CalibrationProposal`), deterministic anti-leakage historical backtesting (`BacktestingEngine`), multi-factor regression guarding (`RegressionGuard`), controlled rollout staging (`SHADOW`, `CANARY`, `ACTIVE`), instant 1-click emergency rollback, and immutable governance audit trail (`GovernanceAuditLog`) | `Prediction`, `PredictionEvaluation`, `PredictionMethodVersion`, `CalibrationProposal`, `BacktestRun`, `GovernanceAuditLog` | Defines `PredictionProvider` SPI, `PredictionDataProvider` port, `PredictionEvaluationRepository` port, `PredictionMethodVersionRepository` port, `CalibrationProposalRepository` port, `BacktestRunRepository` port, `GovernanceAuditLogRepository` port; implements `DeterministicPredictionProvider`; queries `water`, `irrigation`, `sensor`, `anomaly`, `field`; tenant anti-IDOR isolation |
| **`risk`** | Farm-level predictive risk & resilience intelligence, evidence-based multi-factor risk assessment, hydraulic safety dominance, water availability risk, soil moisture deficit/excess, operational delay & schedule contention, telemetry reliability & missing-data gating, predictive model reliability & drift tracking, deterministic 9-tier priority ranking, explainable recommendation synthesis, and human-in-the-loop lifecycle (`OPEN`, `ACKNOWLEDGED`, `ACTION_REQUIRED`, `MITIGATED`, `RESOLVED`, `EXPIRED`) | `FarmRiskAssessment`, `RiskFactor`, `RiskEvidence`, `RiskRecommendation` | Defines `RiskFieldPort`, `RiskWaterPort`, `RiskSensorPort`, `RiskAnomalyPort`, `RiskIrrigationPort`, `RiskPredictionPort`, `RiskNotificationPort`, `RiskAssessmentRepository`; Anti-IDOR tenant isolation |
| **`network`** | Regional water distribution network topology, capacity analysis, coordinated multi-parcel canal dispatch optimization, hydraulic safety interlock precedence, isolated what-if resilience simulations, delivery accounting, zero autonomous actuation | `WaterDistributionNetwork`, `WaterNetworkNode`, `WaterNetworkEdge`, `DispatchSchedule`, `ActualDispatchDelivery`, `ResilienceScenario` | Defines `NetworkFieldPort`, `NetworkWaterPort`, `NetworkIrrigationPort`, `NetworkAnomalyPort`, `NetworkNotificationPort`, `WaterNetworkRepository`; Anti-IDOR tenant isolation |
| **`wateraccounting`** | Regional water accounting, mass balance conservation ($Closing = Opening + Inflow - Consumed - Losses - ReturnFlow$), double-counting prevention, conveyance loss differential analysis, parcel allocation quota compliance, distribution equity ($m^3/\text{ha}$, CV dispersion index, rotational lag), multi-stage boundary reconciliation, verifiable conservation intelligence, zero fabricated savings | `WaterLedgerEntry`, `RegionalWaterBalance`, `ConveyanceLossAssessment`, `AllocationComplianceItem`, `DistributionEquityAssessment`, `ConservationOpportunity`, `WaterReconciliationItem` | Defines `AccountingFieldPort`, `AccountingWaterPort`, `AccountingNetworkPort`, `AccountingSensorPort`, `AccountingNotificationPort`, `WaterLedgerRepository`, `WaterReconciliationRepository`; Anti-IDOR tenant isolation |
| **`accountability`** | Regional water accountability, incident investigation & corrective action governance, 10 deterministic categories, deterministic severity & SLA horizons (4h, 24h, 72h, 168h), evidence confidence taxonomy, presumption of innocence, 12-state incident lifecycle state machine, strict separation of duties (Four-Eyes Principle), corrective action planning, independent verification, idempotent deduplication, zero autonomous actuation | `WaterIncident`, `IncidentEvidence`, `CorrectiveAction`, `IncidentVerification`, `IncidentTimelineEntry` | Defines `AccountabilityFieldPort`, `AccountabilityNetworkPort`, `AccountabilityWaterAccountingPort`, `AccountabilityNotificationPort`, `IncidentRepository`, `IncidentEvidenceRepository`, `CorrectiveActionRepository`, `IncidentVerificationRepository`, `IncidentTimelineRepository`; Anti-IDOR tenant isolation |
| **`performance`** | Regional water operations performance, outcome measurement & evidence-based impact assessment, tripartite separation (OUTPUT, OUTCOME, IMPACT), deterministic indicator catalog, 5-point baseline comparability engine, conservative attribution hierarchy, operational target compliance, zero physical actuator control | `PerformanceIndicator`, `PerformanceBaseline`, `PerformanceTarget`, `PerformanceEvaluation`, `ImpactAssessment` | Defines `PerformanceWaterAccountingPort`, `PerformanceNetworkPort`, `PerformanceAccountabilityPort`, `PerformanceSensorPort`, `PerformanceIrrigationPort`, `PerformancePredictionPort`, `PerformanceFieldPort`, `PerformanceIndicatorRepository`, `PerformanceBaselineRepository`, `PerformanceTargetRepository`, `PerformanceEvaluationRepository`, `ImpactAssessmentRepository`; Anti-IDOR tenant isolation |
| **`notification`**| User alerts, push notifications, read states | `Notification` | References `User` (recipientId) |
| **`shared`** | Exception handlers, JWT filter, base entity, pagination | `BaseAuditableEntity` | Injected across all modules |

### Inter-Module Communication Rules
1. **Synchronous Internal Calls**: Modules invoke Application Service interfaces of another module if needed.
2. **Data Aggregations**: The `dashboard` module is explicitly designated as a read-model aggregator; it is allowed to query module repositories in read-only mode to prevent write-service pollution.
3. **Future Microservice Transition**: Modules communicate strictly using UUIDs rather than direct object references (`@ManyToOne` across different module entities is intentionally avoided). This allows any module to be moved to an external microservice without breaking database integrity.

### Domain Documentation References
- Field Management: [`docs/field-domain.md`](field-domain.md), [`docs/field-api.md`](field-api.md)
- Water Management: [`docs/water-domain.md`](water-domain.md), [`docs/water-api.md`](water-api.md), [`docs/water-consumption.md`](water-consumption.md)
- Sensor & Telemetry Foundation: [`docs/sensor-domain.md`](sensor-domain.md), [`docs/sensor-api.md`](sensor-api.md)
- Telemetry Analytics & Historical REST API: [`docs/telemetry-analytics.md`](telemetry-analytics.md)
- Time-Series Storage & TimescaleDB Strategy: [`docs/time-series-storage.md`](time-series-storage.md)
- Telemetry Aggregation Engine & Modality Policies: [`docs/telemetry-aggregation.md`](telemetry-aggregation.md)
- Telemetry Anomaly Detection Architecture: [`docs/anomaly-detection.md`](anomaly-detection.md)
- Configurable Anomaly Rules & Thresholds: [`docs/anomaly-rules.md`](anomaly-rules.md)
- Alert Lifecycle, Deduplication & Management: [`docs/alert-management.md`](alert-management.md)
- Anomaly REST API Contracts & Security: [`docs/anomaly-api.md`](anomaly-api.md)
- AI Agronomic Recommendations & Decision Support: [`docs/ai-agronomic-recommendations.md`](ai-agronomic-recommendations.md)
- Recommendation REST API Contracts & Lifecycle: [`docs/recommendation-api.md`](recommendation-api.md)
- Advisory AI Provider & Reasoning Architecture: [`docs/ai-provider.md`](ai-provider.md)
- Smart Irrigation Planning & Optimization: [`docs/irrigation-planning.md`](irrigation-planning.md)
- Water Allocation & Budgeting Optimization: [`docs/water-allocation.md`](water-allocation.md)
- Smart Irrigation REST API Specification: [`docs/irrigation-api.md`](irrigation-api.md)
- Telemetry Intelligence & Agronomic Recommendations: [`docs/telemetry-intelligence.md`](telemetry-intelligence.md)
- Agronomic Recommendations Architecture: [`docs/agronomic-recommendations.md`](agronomic-recommendations.md)
- Farm Intelligence Dashboard & Action Center Architecture: [`docs/dashboard-architecture.md`](dashboard-architecture.md)
- Agricultural UX & Action Center Guidelines: [`docs/ux-guidelines.md`](ux-guidelines.md)
- Decision-to-Action Operational Workflow Architecture: [`docs/operational-workflow.md`](operational-workflow.md)
- Operations Center Workspace UX Guidelines: [`docs/operations-ux.md`](operations-ux.md)
- Farm Performance, Historical Intelligence & Analytics: [`docs/farm-performance.md`](farm-performance.md)
- Adaptive Intelligence & Model Governance: [`docs/adaptive-intelligence.md`](adaptive-intelligence.md)
- Model Governance Architecture: [`docs/model-governance.md`](model-governance.md)
- Prediction Method Versioning: [`docs/prediction-versioning.md`](prediction-versioning.md)
- Calibration & Historical Backtesting: [`docs/calibration-and-backtesting.md`](calibration-and-backtesting.md)
- Controlled Rollout & Rollback: [`docs/controlled-rollout.md`](controlled-rollout.md)
- Predictive Farm Intelligence & Forecasting: [`docs/predictive-intelligence.md`](predictive-intelligence.md)
- Predictive Intelligence Architecture: [`docs/prediction-architecture.md`](prediction-architecture.md)
- Forecast Metrics & Agronomic Formulas: [`docs/forecast-metrics.md`](forecast-metrics.md)
- Prediction Data Quality & Sufficiency: [`docs/prediction-data-quality.md`](prediction-data-quality.md)
- Prediction Model Evaluation & Continuous Intelligence: [`docs/prediction-evaluation.md`](prediction-evaluation.md)
- Prediction Outcome Matching Semantics: [`docs/prediction-outcome-matching.md`](prediction-outcome-matching.md)
- Prediction Evaluation Metrics & Formulation: [`docs/prediction-metrics.md`](prediction-metrics.md)
- Prediction Data Quality, Gating & Drift: [`docs/prediction-quality.md`](prediction-quality.md)
- Analytics & Historical Intelligence Architecture: [`docs/analytics-architecture.md`](analytics-architecture.md)
- Analytics Metric Governance & Definitions: [`docs/analytics-metrics.md`](analytics-metrics.md)
- Farm-Level Predictive Risk & Resilience Intelligence: [`docs/farm-risk-intelligence.md`](farm-risk-intelligence.md)
- Risk Assessment Rules & Prioritization Formulas: [`docs/risk-assessment-rules.md`](risk-assessment-rules.md)
- Risk Confidence & Evidence Model: [`docs/risk-confidence-and-evidence.md`](risk-confidence-and-evidence.md)
- Milestone 08.13 Completion Report: [`docs/milestone-08.13-completion-report.md`](milestone-08.13-completion-report.md)
- Regional Water Distribution Network Architecture: [`docs/water-network-architecture.md`](water-network-architecture.md)
- Milestone 08.14 Completion Report: [`docs/milestone-08.14-completion-report.md`](milestone-08.14-completion-report.md)
- Regional Water Accounting & Conservation Intelligence: [`docs/regional-water-accounting.md`](regional-water-accounting.md)
- Water Balance Methodology & Calculation Engine: [`docs/water-balance-methodology.md`](water-balance-methodology.md)
- Conveyance Loss Accounting & Differential Analysis: [`docs/conveyance-loss-accounting.md`](conveyance-loss-accounting.md)
- Water Distribution Equity & Inter-Cluster Fairness: [`docs/water-distribution-equity.md`](water-distribution-equity.md)
- Water Accounting Reconciliation & Discrepancy Governance: [`docs/water-accounting-reconciliation.md`](water-accounting-reconciliation.md)
- Evidence-Based Water Conservation Intelligence: [`docs/water-conservation-intelligence.md`](water-conservation-intelligence.md)
- Milestone 08.15 Completion Report: [`docs/milestone-08.15-completion-report.md`](milestone-08.15-completion-report.md)
- Regional Water Accountability Architecture: [`docs/regional-accountability-architecture.md`](regional-accountability-architecture.md)
- Incident Lifecycle and Governance: [`docs/incident-lifecycle-and-governance.md`](incident-lifecycle-and-governance.md)
- Investigation and Evidence Management: [`docs/investigation-and-evidence-management.md`](investigation-and-evidence-management.md)
- Corrective Action Governance: [`docs/corrective-action-governance.md`](corrective-action-governance.md)
- Independent Verification and Separation of Duties: [`docs/independent-verification.md`](independent-verification.md)
- Accountability SLA and Escalation Governance: [`docs/accountability-sla-and-escalation.md`](accountability-sla-and-escalation.md)
- Milestone 08.16 Completion Report: [`docs/milestone-08.16-completion-report.md`](milestone-08.16-completion-report.md)
- Regional Water Operations Performance Architecture: [`docs/regional-performance-architecture.md`](regional-performance-architecture.md)
- Performance Indicator Catalog: [`docs/performance-indicator-catalog.md`](performance-indicator-catalog.md)
- Baseline and Comparability Methodology: [`docs/baseline-and-comparability-methodology.md`](baseline-and-comparability-methodology.md)
- Outcome and Evidence-Based Impact Assessment: [`docs/outcome-and-impact-assessment.md`](outcome-and-impact-assessment.md)
- Water Conservation Impact Verification: [`docs/conservation-impact-verification.md`](conservation-impact-verification.md)
- Performance Data Quality and Reliability: [`docs/performance-data-quality.md`](performance-data-quality.md)
- Regional Performance Reporting & Export: [`docs/regional-performance-reporting.md`](regional-performance-reporting.md)
- Milestone 08.17 Completion Report: [`docs/milestone-08.17-completion-report.md`](milestone-08.17-completion-report.md)
- Production Readiness Audit: [`docs/production-readiness-audit.md`](production-readiness-audit.md)
- Production Architecture & Deployment Topology: [`docs/production-architecture.md`](production-architecture.md)
- Docker Production Deployment Guide: [`docs/docker-production-deployment.md`](docker-production-deployment.md)
- Ubuntu Server Deployment Runbook: [`docs/ubuntu-server-deployment.md`](ubuntu-server-deployment.md)
- Environment Configuration Reference: [`docs/environment-configuration.md`](environment-configuration.md)
- Database Backup, Recovery & Migration Safety: [`docs/database-backup-and-recovery.md`](database-backup-and-recovery.md)
- Continuous Integration & Deployment (CI/CD): [`docs/ci-cd-pipeline.md`](ci-cd-pipeline.md)
- Monitoring, Observability & Structured Logging: [`docs/monitoring-and-observability.md`](monitoring-and-observability.md)
- Production Security Checklist: [`docs/production-security-checklist.md`](production-security-checklist.md)
- Deployment & Rollback Playbook: [`docs/deployment-and-rollback.md`](deployment-and-rollback.md)
- Milestone 08.18 Completion Report: [`docs/milestone-08.18-completion-report.md`](milestone-08.18-completion-report.md)
