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
