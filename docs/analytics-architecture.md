# Analytics & Historical Intelligence Architecture

## 1. Architectural Philosophy

Analytics in **YANGIARIQ AQUA AI** is designed strictly as a **Read-Model / Query Layer**.
It does **NOT** duplicate authoritative business domains (Field, Water, Sensor, Anomaly, Irrigation, Recommendation). Instead, it acts as an immutable projection orchestrator that aggregates authoritative operational data across temporal windows.

```
┌────────────────────────────────────────────────────────┐
│                   Analytics API                        │
│               (AnalyticsController)                    │
└──────────────────────────┬─────────────────────────────┘
                           │ DTOs / Queries
┌──────────────────────────▼─────────────────────────────┐
│                 Application Services                   │
│   • GetFarmPerformanceAnalyticsUseCase                 │
│   • GetPerformanceTrendsUseCase                        │
│   • GetFieldPerformanceRankingUseCase                  │
│   • GetFarmInsightsUseCase                             │
│   • FarmInsightEngine (Deterministic Rules)            │
│   • PerformanceScoringService (Explainable Index)      │
│   • PeriodResolver (Temporal Boundaries)               │
└──────────────────────────┬─────────────────────────────┘
                           │ Domain Port
┌──────────────────────────▼─────────────────────────────┐
│                   AnalyticsQueryPort                   │
│         (Batch Multi-Parcel Read Projections)          │
└──────────────────────────┬─────────────────────────────┘
                           │ JPA / SQL Projections
┌──────────────────────────▼─────────────────────────────┐
│             AnalyticsQueryRepositoryImpl               │
│ • Zero N+1 Batch In-Memory SQL Aggregation             │
│ • Reads WaterConsumptionQueryRepository, Plans, Events,│
│   Readings, and Anomalies Operational Tables           │
└────────────────────────────────────────────────────────┘
```

---

## 2. Package Organization

```
com.aquaai.analytics
├── api
│   ├── AnalyticsController.java
│   └── dto
│       ├── AnalyticsOverviewResponse.java
│       ├── FarmOverviewMetricsDto.java
│       ├── WaterPerformanceDto.java
│       ├── IrrigationPerformanceDto.java
│       ├── TelemetryQualityDto.java
│       ├── VerificationPerformanceDto.java
│       ├── SafetyEventMetricsDto.java
│       ├── PeriodComparisonDto.java
│       ├── DataConfidenceDto.java
│       ├── FarmInsightDto.java
│       ├── FieldPerformanceDto.java
│       ├── FieldPerformanceRankingResponse.java
│       ├── PerformanceTrendPointDto.java
│       ├── PerformanceTrendsResponse.java
│       └── PerformancePeriodDto.java
├── application
│   ├── service
│   │   ├── FarmInsightEngine.java
│   │   ├── PerformanceScoringService.java
│   │   └── PeriodResolver.java
│   └── usecase
│       ├── GetFarmInsightsUseCase.java
│       ├── GetFarmPerformanceAnalyticsUseCase.java
│       ├── GetFieldPerformanceRankingUseCase.java
│       └── GetPerformanceTrendsUseCase.java
├── domain
│   ├── model
│   │   ├── DataConfidenceLevel.java
│   │   ├── FieldPerformanceStatus.java
│   │   ├── InsightCategory.java
│   │   ├── InsightSeverity.java
│   │   ├── PerformancePeriodType.java
│   │   └── TrendGranularity.java
│   └── port
│       └── AnalyticsQueryPort.java
└── infrastructure
    └── persistence
        └── AnalyticsQueryRepositoryImpl.java
```

---

## 3. Database Strategy & Zero N+1 Prevention

### 3.1 Proactive Aggregations
To prevent catastrophic database performance ($N+1$ entity queries and memory bloat), `AnalyticsQueryPort` executes batch grouping queries:
1. `getFieldWaterSummaries(fieldIds, from, to)`: Reuses authoritative water consumption query repository.
2. `getPlanStatusCounts(fieldIds, fromDate, toDate)`: Groups `SmartIrrigationPlanJpaEntity` by status and field in a single round-trip.
3. `getEventStatusCounts(fieldIds, from, to)`: Groups `IrrigationEventJpaEntity` by status and field.
4. `getReadingQualityCounts(fieldIds, from, to)`: Aggregates `SensorReadingJpaEntity` quality counts (`VALID`, `SUSPECT`, `INVALID`).
5. `getAnomalyCounts(fieldIds, from, to)`: Groups `AnomalyJpaEntity` by severity.
6. `getVerifiedOperationsCount(fieldIds, from, to)`: Correlates completed irrigation events with post-operation moisture readings.

### 3.2 Performance Indexes (Liquibase `014-add-analytics-performance-indexes.sql`)
- `idx_irr_events_field_started_status` on `irrigation_events(field_id, started_at, status)`
- `idx_anomalies_field_detected_severity` on `anomalies(field_id, detected_at, severity)`
- `idx_sensor_readings_field_time_quality` on `sensor_readings(field_id, measured_at, quality)`
- `idx_smart_plans_field_date_status` on `smart_irrigation_plans(field_id, planned_date, status)`

---

## 4. REST API Endpoints

| Method | Endpoint | Description | Role / Scope |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/analytics/overview` | Full farm performance overview & KPIs | FARMER (own), OPERATOR/ADMIN (district) |
| `GET` | `/api/v1/analytics/water` | Dedicated water quota & plan variance metrics | FARMER (own), OPERATOR/ADMIN (district) |
| `GET` | `/api/v1/analytics/irrigation` | Dedicated irrigation plan adherence metrics | FARMER (own), OPERATOR/ADMIN (district) |
| `GET` | `/api/v1/analytics/telemetry` | Dedicated sensor telemetry quality metrics | FARMER (own), OPERATOR/ADMIN (district) |
| `GET` | `/api/v1/analytics/verification` | Dedicated post-irrigation verification metrics | FARMER (own), OPERATOR/ADMIN (district) |
| `GET` | `/api/v1/analytics/trends` | Chronological trends (`DAILY`, `WEEKLY`, `MONTHLY`) | FARMER (own), OPERATOR/ADMIN (district) |
| `GET` | `/api/v1/analytics/fields` | Ranked parcel table (paginated, sortable) | FARMER (own), OPERATOR/ADMIN (district) |
| `GET` | `/api/v1/analytics/insights` | Prioritized deterministic agricultural insights | FARMER (own), OPERATOR/ADMIN (district) |

---

## 5. Frontend FSD Organization

```
frontend/src
├── entities/analytics
│   ├── api.ts          # REST client methods with signal abort
│   ├── types.ts        # TypeScript DTO models & enumerations
│   └── index.ts
├── features/analytics
│   ├── components
│   │   ├── DataConfidenceBanner.tsx
│   │   ├── FarmInsightsCard.tsx
│   │   ├── FarmPerformanceKpis.tsx
│   │   ├── FieldPerformanceTable.tsx
│   │   ├── IrrigationPerformanceCard.tsx
│   │   ├── PerformanceTrendCharts.tsx
│   │   ├── PeriodComparisonCard.tsx
│   │   ├── PeriodFilterBar.tsx
│   │   ├── TelemetryQualityCard.tsx
│   │   └── WaterPerformanceCard.tsx
│   ├── queries
│   │   └── useAnalyticsQueries.ts
│   └── index.ts
└── pages/analytics
    ├── AnalyticsPage.tsx
    └── index.ts
```

---

## 6. Architecture Compliance (ArchUnit)

The architecture is permanently guarded by automated ArchUnit verification in `ArchitectureTests.java`:
- Rule 60: Analytics controllers must reside in `..analytics.api..`
- Rule 61: Application services must reside in `..analytics.application..`
- Rule 62: Controllers must never access repositories directly
- Rule 63: Analytics must not depend on hardware/actuator SDKs (Paho MQTT, Pi4J, Gemini SDK)
- Rule 64: Analytics domain must not depend on JPA or persistence annotations
- Rule 65: Analytics domain must not depend on Spring MVC or web classes
