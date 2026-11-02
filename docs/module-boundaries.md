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
| **`prediction`** | Predictive farm intelligence & forecasting, statistical soil moisture trajectories via FAO-56 regression, irrigation likelihood via MAD thresholds, multi-horizon water demand, water quota headroom risk, irrigation plan hydraulic safety enforcement, 14-day telemetry reliability forecast, explainable AI modal (WHAT, WHY, EVIDENCE, ASSUMPTIONS, NEXT STEP); Continuous Intelligence & Model Evaluation (Milestone 08.8): prediction-outcome matching, numerical error (MAE, RMSE, safe MAPE, MBE), classification confusion matrix, confidence calibration, data quality impact, performance drift detection, and human-in-the-loop decision audit | `Prediction`, `PredictionEvaluation` | Defines `PredictionProvider` SPI, `PredictionDataProvider` port, `PredictionEvaluationRepository` port; implements `DeterministicPredictionProvider`; queries `water`, `irrigation`, `sensor`, `anomaly`, `field`; tenant anti-IDOR isolation |
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
