# Milestone 08.19 — Real-World Data Integration Audit & Baseline

## 1. Executive Summary

Milestone 08.19 introduces a production-oriented real-world data integration foundation for **YANGIARIQ AQUA AI**. This baseline audit inspects existing modules across the backend and frontend, establishes baseline test execution counts, identifies reusable domain entities and ports, and defines the roadmap for real-world telemetry ingestion, external weather connectivity, CSV batch imports, manual operator measurements, and synchronization orchestration.

---

## 2. Baseline Test Execution Results

Prior to making any code modifications, baseline verification suites were executed.

### 2.1 Backend Baseline (`./gradlew clean test`)
- **Execution Date**: 2026-11-09T09:52:42+05:00
- **Total Test Classes Executed**: 152 XML test suite reports
- **Total Tests Executed**: 846
- **Passed**: 830
- **Failures**: 0
- **Errors**: 0
- **Skipped**: 16 (Pre-existing ignored/placeholder integration tests)
- **ArchUnit Tests**: 106 architectural rules verified (all passed)
- **Status**: **PASS (Clean Baseline)**

### 2.2 Frontend Baseline (`npm run typecheck && npm run lint && npm run test -- --run && npm run build`)
- **TypeScript Typecheck**: Clean, 0 errors (`tsc -b`)
- **ESLint**: Clean, 0 errors (`eslint .`)
- **Vitest Suite**: 29 test files, 275 tests passed (0 failed)
- **Vite Build**: Successful production build (`dist/` generated, 11.67s)
- **Status**: **PASS (Clean Baseline)**

---

## 3. Comprehensive Repository Audit

### 3.1 Sensor Domain & Telemetry Architecture (`com.aquaai.sensor`)
- **Existing Entities & Aggregates**:
  - `Sensor`: Domain aggregate representing field IoT / telemetry sensors with `SensorType`, `SensorStatus` (`ACTIVE`, `INACTIVE`, `MAINTENANCE`, `OFFLINE`), `provider` string, and device identifier.
  - `SensorReading`: Immutable domain value record with `id`, `sensorId`, `fieldId`, `type`, `value`, `unit`, `quality` (`VALID`, `SUSPECT`, `INVALID`), `source`, `measuredAt`, `createdAt`.
  - `MeasurementUnit`: Agronomic units (`PERCENT`, `CUBIC_METER_PER_HOUR`, `LITERS_PER_SECOND`, `CELSIUS`, `METERS`, `MILLIMETERS`, `BAR`, `MILLIBAR`, `DECISIEMENS_PER_METER`).
  - `SensorType`: Enum validating physical bounds and compatible units (`SOIL_MOISTURE`, `WATER_FLOW`, `TEMPERATURE`, `HUMIDITY`, `WATER_LEVEL`, `PRESSURE`, `RAINFALL`, `SALINITY`).
  - `SensorProvider`: Domain port SPI: `read(Sensor sensor)`, `getProviderType()`, `isAvailable()`.
  - `SimulatedSensorProvider`: Infrastructure implementation generating bounded random-walk drift (`SIMULATED_SENSOR_PROVIDER`).
  - `SensorDeviceJpaEntity` & `SensorReadingJpaEntity`: Mapped to `sensor_devices` and `sensor_readings` tables.
- **What Can Be Reused**:
  - Existing `SensorReading` domain entity and table (`sensor_readings`) for storing actual telemetry readings.
  - Existing `SensorProvider` SPI can be extended or supported with specific real-world gateway adapters.
  - Existing `Sensor` aggregate can be linked to external data sources.
- **What Is Missing**:
  - Centralized `DataSource` registry tracking source types (`SENSOR_DEVICE`, `WEATHER_PROVIDER`, `OPERATOR_INPUT`, `FARMER_INPUT`, `CSV_IMPORT`, `EXTERNAL_API`, `MANUAL_MEASUREMENT`, `SIMULATED_PROVIDER`).
  - Normalized ingestion pipeline with multi-step validation (raw check, canonical normalization, unit conversion, duplicate detection, timestamp validation, data quality classification).
  - Provenance tracking linking ingested records to external batch/message IDs and source records.
  - Deduping indexes and idempotency mechanisms for external payloads.

### 3.2 Weather Integrations (`com.aquaai.recommendation`, `com.aquaai.prediction`, `com.aquaai.irrigation`)
- **Existing Contracts**:
  - `WeatherDataProvider`: Interface in `com.aquaai.recommendation.domain.port` with `WeatherSnapshot getWeatherForField(UUID fieldId)`.
  - `WeatherSnapshot`: Domain model with `temperature`, `relativeHumidity`, `precipitationProbability`, `precipitationAmountMm`, `windSpeedMps`, `atmosphericPressureHpa`, `timestamp`, `source`.
  - `SimulatedYangiariqWeatherProvider`: Deterministic seasonal weather model for Yangiariq district.
- **What Can Be Reused**:
  - Existing `WeatherDataProvider` and `WeatherSnapshot` models consumed across recommendation, prediction, and irrigation engines.
- **What Is Missing**:
  - External weather service provider adapter (e.g. Open-Meteo, weather API gateway) with resilience (timeouts, bounded retries, rate limits, circuit breaker fallback).
  - Multi-source weather provider manager that can switch between real external weather data (when configured) and fallback simulated data, while strictly labeling simulated data as `SIMULATED` and external observations as `OBSERVED`.

### 3.3 Evidence Classification & Provenance Models
- **Existing Classifications**:
  - `com.aquaai.wateraccounting.domain.model.EvidenceClassification`: `OBSERVED`, `CALCULATED`, `ESTIMATED`, `SIMULATED`, `MISSING`.
  - `com.aquaai.performance.domain.model.EvidenceClassification`: `OBSERVED`, `CALCULATED`, `ESTIMATED`, `SIMULATED`, `MISSING`.
  - `com.aquaai.accountability.domain.model.EvidenceConfidence`: `VERIFIED`, `PARTIALLY_VERIFIED`, `UNVERIFIED`, `SIMULATED`, `INSUFFICIENT_DATA`.
- **Reuse Strategy**:
  - Unify or bridge evidence classifications in data integration: ensure ingested real sensor telemetry is labeled `OBSERVED` or `VERIFIED` (if operator-confirmed) while simulated data remains strictly `SIMULATED`.

### 3.4 CSV Import & Manual Operator Measurements
- **What Already Exists**:
  - `RecordSensorReadingRequest` and `recordReading` endpoint on `SensorController`.
  - No batch CSV import pipeline or review workflow currently exists.
- **What Is Missing**:
  - Multipart CSV upload handling with security guards (MIME type check, file size limit <= 10MB, CSV bomb prevention).
  - Column mapping and row-by-row parsing with validation error accumulation.
  - Staging/execution results with rejected row reporting and summary statistics.
  - Dedicated operator manual measurement entry endpoint with explicit verification metadata (`VERIFIED`, `UNVERIFIED`, author ID, notes).

### 3.5 Synchronization Orchestration & Monitoring
- **Existing Scheduling**:
  - Spring `@EnableScheduling` used in `anomaly` module (`AnomalySchedulerService`).
- **What Is Missing**:
  - Source synchronization coordinator with scheduled and manual triggers.
  - Non-overlapping execution guards (`AtomicBoolean` or concurrency locks).
  - Synchronization run history tracking (status, record counts, durations, sanitized errors).
  - Data quality metrics aggregation and Micrometer counters.

### 3.6 Frontend Integration
- **Existing Structure**:
  - React 18 + Vite + Tailwind + shadcn/ui + TanStack Query.
  - Existing pages: `SensorsPage`, `OperationsPage`, `RegionalWaterAccountingPage`, `RegionalAccountabilityPage`, `RegionalPerformancePage`.
  - Navigation sections in `NavSection` and `Sidebar.tsx`.
  - i18n support in `frontend/src/shared/i18n/index.ts` (uz, ru, en).
- **What Is Missing**:
  - Data Integration / Sources management dashboard (`/data-sources` or dedicated view).
  - Data Quality & Provenance monitoring tab.
  - CSV file import modal / wizard with mapping preview, dry-run validation, and import report.
  - Manual measurement recording modal for operators/farmers with parcel validation.
  - Complete translations for Uzbek, Russian, and English.

---

## 4. External Dependencies & Integration Classification

| Integration | Requires External Credentials? | Requires Physical Hardware? | Safe Integration Strategy | Target Status |
|-------------|--------------------------------|-----------------------------|---------------------------|---------------|
| **Data Source Registry** | No | No | Internal PostgreSQL database via Liquibase | `IMPLEMENTED` |
| **Ingestion & Normalization Pipeline** | No | No | In-memory validation, idempotency hash, canonical unit conversion | `IMPLEMENTED` |
| **External Weather Adapter (Open-Meteo / WMO)** | Optional (Public API / API key optional) | No | Non-commercial open endpoint with configurable base URL, timeout, circuit breaker; falls back to `NOT_CONFIGURED` or `SIMULATED` if disabled | `CONFIGURED` / `IMPLEMENTED` |
| **HTTP Sensor Gateway Adapter** | No (Mock / In-memory HTTP endpoint) | Future Edge Gateway | Standardized webhook / REST telemetry receiver with API token validation | `IMPLEMENTED` / `AWAITING_HARDWARE` |
| **CSV Telemetry Import** | No | No | File upload parser with row-level error reporting and preview | `IMPLEMENTED` |
| **Operator Manual Measurement** | No | No | Authenticated REST endpoints with provenance and evidence tracking | `IMPLEMENTED` |
| **Simulated Sensor Provider** | No | No | Retained for local testing, explicitly labeled as `SIMULATED` | `IMPLEMENTED` |

---

## 5. Architectural Boundaries & Module Plan

To respect Clean Architecture and ArchUnit rules:
1. Create a dedicated `com.aquaai.integration` module (or integrate within `sensor` / shared boundaries) following the established 4-layer architecture:
   - `domain`: `DataSource`, `DataSourceType`, `DataSourceStatus`, `IngestionRecord`, `DataQualityStatus`, `DataSourceRepository`, `IngestionPipeline` ports.
   - `application`: Use cases for data source registration, synchronization, ingestion, CSV processing, manual measurement submission.
   - `infrastructure`: REST adapters, external weather HTTP client, JPA repositories, Liquibase migrations, Spring scheduled synchronization.
   - `api`: Controllers in `com.aquaai.integration.api` exposing `/api/v1/data-sources`, `/api/v1/data-ingestion`, `/api/v1/data-imports`.
2. Ensure ArchUnit rules test this new module to enforce no leaking of HTTP or JPA classes into the domain, no actuator SDKs, and proper package placement.
3. Keep Liquibase changesets clean and additive (`026-create-data-integration-tables.sql`).
