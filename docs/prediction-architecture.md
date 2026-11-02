# Predictive Intelligence Architecture (Milestone 08.7)

## 1. Architectural Philosophy & Hexagonal Boundary

The prediction module is organized as a dedicated bounded context:
```
com.aquaai.prediction
```

It follows strict Hexagonal Architecture (Ports and Adapters) principles:
- **No external ML framework lock-in**: Does not depend on heavy Python ML servers, Spring AI SDK, or cloud vendor APIs.
- **Strict ArchUnit Governance**: Bounded context access rules (Rules 66–72) enforce zero illegal inbound/outbound references.
- **Reusability of Authoritative Services**: Integrates authoritative data ports (`WaterConsumptionQueryRepository`, `SmartIrrigationPlanRepository`, `AnomalyRepository`, `FieldRepository`) without duplicating calculation engines.

```
                  ┌─────────────────────────────────────────────────────────┐
                  │                 API LAYER                               │
                  │   PredictionController (/api/v1/predictions/*)          │
                  └─────────────────────────┬───────────────────────────────┘
                                            │
                                            ▼
                  ┌─────────────────────────────────────────────────────────┐
                  │             APPLICATION USE CASES                       │
                  │   GetFieldPredictionsUseCase                            │
                  │   GetFarmPredictionOverviewUseCase                      │
                  │   GetWaterDemandForecastUseCase                         │
                  │   GetIrrigationNeedRankingUseCase                       │
                  │   GetEmergingRisksUseCase                               │
                  │   GetForecastHistoryUseCase                             │
                  └─────────────────────────┬───────────────────────────────┘
                                            │
                                            ▼
                  ┌─────────────────────────────────────────────────────────┐
                  │                 DOMAIN CORE                             │
                  │   Prediction (Aggregate Root)                           │
                  │   PredictionType, Horizon, RiskLevel, DataSufficiency    │
                  │   Domain Services:                                      │
                  │     • SoilMoistureForecaster                            │
                  │     • IrrigationNeedForecaster                          │
                  │     • WaterDemandForecaster                             │
                  │     • WaterAvailabilityForecaster                       │
                  │     • IrrigationPlanRiskForecaster                      │
                  │     • SensorReliabilityForecaster                       │
                  │     • DataSufficiencyEvaluator                          │
                  │   Ports:                                                │
                  │     • PredictionProvider (SPI)                          │
                  │     • PredictionRepository (SPI)                        │
                  │     • PredictionDataProvider (SPI)                      │
                  └───────────────▲───────────────────────▲─────────────────┘
                                  │                       │
                                  │ Implements            │ Implements
         ┌────────────────────────┴──────────┐ ┌──────────┴─────────────────────────┐
         │     INFRASTRUCTURE PROVIDER       │ │       INFRASTRUCTURE PERSISTENCE   │
         │  DeterministicPredictionProvider  │ │  PredictionRepositoryImpl          │
         │  (Deterministic FAO-56 & Trends)  │ │  SpringDataPredictionRepository    │
         │                                   │ │  PredictionDataProviderImpl        │
         │                                   │ │  (High-Perf JDBC Read Model)       │
         └───────────────────────────────────┘ └────────────────────────────────────┘
```

---

## 2. Model Provider Abstraction (`PredictionProvider`)

Prediction generation is decoupled behind the `PredictionProvider` domain port:
```java
public interface PredictionProvider {
    List<Prediction> generateFieldPredictions(
        FieldMetadata field,
        List<TelemetryObservation> telemetry,
        WaterBalance waterBalance,
        List<UpcomingPlanInfo> upcomingPlans,
        List<HydraulicAnomalyInfo> anomalies,
        List<SensorHealthSummary> sensorHealthList,
        PredictionHorizon horizon
    );
}
```

### Implementations:
1. **`DeterministicPredictionProvider` (Production Default)**:
   - Resides in `com.aquaai.prediction.infrastructure.provider` (satisfying ArchUnit Rule 3).
   - Completely deterministic, reproducible, and explainable.
   - Evaluates physics-informed agronomical formulas (soil moisture depletion velocity, MAD breach times, FAO-56 root zone replenishment).
2. **Future Extensibility**:
   - `MLPredictionProvider` (lightweight ONNX runtime or statistical ARIMA / Prophet time-series models).
   - `AIAugmentedExplanationProvider` (natural-language synthesis of deterministic evidence chains).

---

## 3. High-Performance Read Adapter (`PredictionDataProviderImpl`)

To avoid N+1 query cascades across multiple aggregate boundaries, `PredictionDataProviderImpl` utilizes optimized JDBC batch queries:
- **`findActiveFieldsByOwner(...)`**: Direct single-query resolution of parcel geometry, soil types, and crop phenology stages.
- **`getTelemetryWindow(...)`**: Index-assisted extraction of recent calibrated soil moisture and environmental readings (`sensor_readings.recorded_at >= now - INTERVAL`).
- **`WaterConsumptionQueryRepository`**: Reuses the authoritative water consumption query repository for instantaneous seasonal quota consumption and remaining volume.
- **`smart_irrigation_plans` & `anomalies`**: Efficient queries to identify upcoming planned water deliveries and unresolved hydraulic safety interlocks.

---

## 4. Immutable Persistence Model

Predictions are persisted as immutable historical snapshots in the PostgreSQL table `predictions`:
```sql
CREATE TABLE predictions (
    id VARCHAR(36) PRIMARY KEY,
    field_id VARCHAR(36) NOT NULL,
    field_name VARCHAR(255),
    prediction_type VARCHAR(64) NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    target_time TIMESTAMP WITH TIME ZONE NOT NULL,
    predicted_value NUMERIC(10, 2),
    lower_bound NUMERIC(10, 2),
    upper_bound NUMERIC(10, 2),
    unit VARCHAR(32),
    confidence_percent INT NOT NULL,
    risk_level VARCHAR(32) NOT NULL,
    data_sufficiency VARCHAR(32) NOT NULL,
    likelihood VARCHAR(32),
    evidence TEXT,
    assumptions TEXT,
    recommended_action TEXT,
    action_target_module VARCHAR(64),
    actual_outcome_value NUMERIC(10, 2),
    accuracy_status VARCHAR(32) NOT NULL,
    evaluated_at TIMESTAMP WITH TIME ZONE
);
```

### Indices:
- `idx_predictions_field_horizon` on `(field_id, horizon)`
- `idx_predictions_target_time` on `(target_time)`
- `idx_predictions_type_acc` on `(prediction_type, accuracy_status)`

---

## 5. Security & Anti-IDOR Protections

1. **Owner-Scoped Anti-Enumeration (404 Not Found)**:
   - When a farmer requests predictions for a parcel they do not own, the system responds with `404 NOT_FOUND` rather than `403 FORBIDDEN`, preventing parcel ID enumeration.
2. **Role Hierarchy**:
   - `FARMER`: Bound strictly to parcels where `owner_id = principal.id`.
   - `DISTRICT_OPERATOR`: Can access parcels across their assigned irrigation canal network.
   - `SYSTEM_ADMIN`: Global read and audit privileges.

---

## 6. Continuous Intelligence & Evaluation Subsystem (Milestone 08.8)

### 6.1 Database Schema (`prediction_evaluations`)
The evaluation lifecycle is persisted in an immutable audit table configured via Liquibase changeset 033:
- Table: `prediction_evaluations`
- Unique constraint: `uk_prediction_eval_pred_id` on `(prediction_id)` ensuring strict evaluation idempotency.
- Foreign keys: `field_id` referencing `fields(id)`, `prediction_id` referencing `predictions(id)`.
- Indexes: `idx_pred_eval_field_time`, `idx_pred_eval_type_status`, `idx_pred_eval_status`.

### 6.2 Key Domain Services
- `PredictionOutcomeMatcher`: Matches forecast targets against actual outcomes across temporal windows ($\pm 3\,\text{h}$ for soil moisture, qualifying events for irrigation need, consumption records for water demand). Intercepts hydraulic safety blocks to classify outcomes as `OUTCOME_INTERRUPTED` rather than false positives.
- `PredictionEvaluationAggregator`: Computes exact arbitrary-precision MAE, RMSE, safe MAPE (handling zero denominators), MBE bias, confusion matrices, and confidence calibration.
- `EvaluationInsightGenerator`: Formulates actionable deterministic insights for systematic biases, telemetry bottlenecks, and calibration inversions.

### 6.3 REST Endpoints
- `GET /api/v1/predictions/evaluation/overview`: Tenant-isolated evaluation KPI summary, model health, and drift alerts.
- `GET /api/v1/predictions/evaluation/by-type`: Detailed numerical and classification metrics per prediction type.
- `GET /api/v1/predictions/evaluation/by-field`: Parcel-level breakdown with $n < 5$ limited sample gating.
- `GET /api/v1/predictions/evaluation/history`: Filtered, paginated audit ledger of prediction vs reality comparisons.
- `GET /api/v1/predictions/{predictionId}/evaluation`: In-depth audit modal details for a single forecast.
- `POST /api/v1/predictions/evaluation/evaluate`: Triggers batch evaluation of all pending predictions whose horizons have matured.
