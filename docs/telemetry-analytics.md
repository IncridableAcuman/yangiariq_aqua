# Telemetry Analytics & Historical Aggregation REST API (`docs/telemetry-analytics.md`)

## 1. Architectural Overview

Milestone 07.3 delivers the **Time-Series Storage & Telemetry Analytics** platform for YANGIARIQ AQUA AI. Building on the foundational Sensor Domain (Milestone 07.1), this module transforms discrete point-in-time sensor readings into aggregated, queryable time-series datasets.

These analytics provide the physical foundation for:
1. **Interactive Real-Time Dashboards**: Multi-modality field telemetry dashboards with Recharts visualization and interval toggles.
2. **GIS Water Intelligence Integration**: Live sensor counts, sensor availability, and current environmental observations surfaced in parcel side panels.
3. **Agronomic & Irrigation Decision Support**: Baseline for calculating actual vs planned water delivery, evapotranspiration indicators, and automated soil moisture stress alerts.
4. **Hardware Fleet Monitoring**: Continuous tracking of sensor heartbeat communication states (`communicating` vs `unresponsive`) and reading reliability quality (`VALID`, `SUSPECT`, `INVALID`).

All endpoints strictly adhere to platform conventions:
- Standard REST semantics under `/api/v1/sensors` and `/api/v1/fields/{fieldId}`.
- Uniform response wrapper: `ApiResponse<T>` with `success: true`, `data: T`, and `timestamp: Instant`.
- RFC 7807 Problem Details for all validation and security errors.
- Strict Anti-IDOR parcel tenancy checks via `FieldOwnershipChecker`. Non-owned resources respond with `404 Not Found` rather than `403 Forbidden` to prevent identifier enumeration.

---

## 2. Security & RBAC Matrix

| Endpoint | Method | FARMER | OPERATOR | ADMIN | Description |
| :--- | :---: | :---: | :---: | :---: | :--- |
| `GET /api/v1/sensors/{sensorId}/analytics` | `GET` | Parcel owner | ✅ District-wide | ✅ Full | Get aggregated time-series buckets for a specific sensor |
| `GET /api/v1/sensors/{sensorId}/history` | `GET` | Parcel owner | ✅ District-wide | ✅ Full | Alias for sensor historical analytics |
| `GET /api/v1/fields/{fieldId}/analytics` | `GET` | Parcel owner | ✅ District-wide | ✅ Full | Get aggregated time-series buckets for a parcel modality |
| `GET /api/v1/fields/{fieldId}/history` | `GET` | Parcel owner | ✅ District-wide | ✅ Full | Alias for field historical analytics |
| `GET /api/v1/fields/{fieldId}/analytics/summary` | `GET` | Parcel owner | ✅ District-wide | ✅ Full | Comprehensive 24h KPI summary, fleet availability & quality |

### Anti-IDOR Enforcement Rules
- **Farmer Role**: Evaluated against parcel ownership (`Field.ownerId == currentUserId`). If a farmer requests analytics for a `fieldId` or a `sensorId` located on a parcel they do not own, the system throws `ResourceNotFoundException`, rendering an HTTP 404.
- **Operator Role**: Read-only district-wide visibility across all fields and sensors in Yangiariq district.
- **Admin Role**: Full system-wide visibility and administrative authority.

---

## 3. Query Parameters & Interval Constraints

Analytics endpoints support the following request parameters:

| Parameter | Type | Required | Default | Description & Validation |
| :--- | :--- | :---: | :---: | :--- |
| `interval` | `String` | No | `1h` | Bucketing interval: `5m`, `15m`, `1h`, `1d`, `1w`. Must not be blank. |
| `from` | `Instant` (ISO-8601) | No | `now - 7d` | Time-series query range start (UTC). |
| `to` | `Instant` (ISO-8601) | No | `now` | Time-series query range end (UTC). Must be $\ge$ `from`. |
| `sensorType` | `SensorType` | Field queries only | `SOIL_MOISTURE` | Telemetry modality: `SOIL_MOISTURE`, `WATER_FLOW`, `TEMPERATURE`, etc. |
| `quality` | `ReadingQuality` | No | All | Optional quality filter: `VALID`, `SUSPECT`, `INVALID`. |

### Maximum Query Range Guardrails

To protect against unbounded database scans and memory exhaustion, maximum time windows are strictly enforced according to interval granularity:

| Interval Code | Granularity | Max Window | Max Expected Data Points |
| :--- | :--- | :--- | :--- |
| `5m` | 5 Minutes | 2 Days (48 Hours) | 576 points |
| `15m` | 15 Minutes | 7 Days | 672 points |
| `1h` | 1 Hour | 31 Days | 744 points |
| `1d` | 1 Day | 365 Days (1 Year) | 365 points |
| `1w` | 1 Week | 1825 Days (5 Years)| 260 points |

Exceeding `maxAllowedQueryDays` results in an RFC 7807 `400 Bad Request` with message:
`"Query range of X days exceeds maximum allowed 2 days for interval 5m"`.

---

## 4. REST Endpoints Specification

### 4.1 Sensor Analytics & History
- **Path**: `GET /api/v1/sensors/{sensorId}/analytics` (or `/api/v1/sensors/{sensorId}/history`)
- **Success Status**: `200 OK`
- **Response Format**:
```json
{
  "success": true,
  "data": [
    {
      "sensorId": "1b09b0b4-1e07-4e67-961f-d38eeb5b6b80",
      "fieldId": "c4794db1-7b02-4fc4-8dcf-3367b6eb74a8",
      "sensorType": "SOIL_MOISTURE",
      "interval": "1h",
      "bucketStartTime": "2026-11-01T08:00:00Z",
      "bucketEndTime": "2026-11-01T09:00:00Z",
      "average": 38.4500,
      "minimum": 37.9000,
      "maximum": 39.1000,
      "sum": null,
      "firstReadingValue": 37.9000,
      "firstReadingAt": "2026-11-01T08:05:12Z",
      "lastReadingValue": 38.6000,
      "lastReadingAt": "2026-11-01T08:55:00Z",
      "readingCount": 12,
      "unit": "PERCENT"
    }
  ],
  "timestamp": "2026-11-01T09:30:00.123456Z"
}
```

### 4.2 Field Parcel Analytics & History
- **Path**: `GET /api/v1/fields/{fieldId}/analytics` (or `/api/v1/fields/{fieldId}/history`)
- **Query Params**: `?sensorType=SOIL_MOISTURE&interval=1h&from=2026-10-25T00:00:00Z&to=2026-11-01T00:00:00Z`
- **Success Status**: `200 OK`
- **Description**: Returns chronological buckets aggregated across all active sensors of the specified `sensorType` deployed on the field parcel.

### 4.3 Field Analytics 24h Summary
- **Path**: `GET /api/v1/fields/{fieldId}/analytics/summary`
- **Success Status**: `200 OK`
- **Description**: Provides an executive telemetry overview combining:
  1. Latest point-in-time reading for every modality deployed on the parcel.
  2. Rolling 24-hour aggregated statistics (`average`, `min`, `max`, `sum`) for each modality.
  3. Rolling 24-hour telemetry data quality metrics.
  4. Real-time hardware fleet availability breakdown.
- **Response Format**:
```json
{
  "success": true,
  "data": {
    "fieldId": "c4794db1-7b02-4fc4-8dcf-3367b6eb74a8",
    "latestReadings": {
      "SOIL_MOISTURE": {
        "id": "e229a473-b3c1-45f8-8bb0-cbe0d3ce39f9",
        "sensorId": "1b09b0b4-1e07-4e67-961f-d38eeb5b6b80",
        "fieldId": "c4794db1-7b02-4fc4-8dcf-3367b6eb74a8",
        "type": "SOIL_MOISTURE",
        "value": 38.4000,
        "unit": "PERCENT",
        "unitSymbol": "%",
        "quality": "VALID",
        "source": "SIMULATED",
        "measuredAt": "2026-11-01T09:00:00Z",
        "createdAt": "2026-11-01T09:00:01Z"
      }
    },
    "past24hAggregates": {
      "SOIL_MOISTURE": {
        "fieldId": "c4794db1-7b02-4fc4-8dcf-3367b6eb74a8",
        "sensorId": null,
        "sensorType": "SOIL_MOISTURE",
        "interval": "1d",
        "bucketStartTime": "2026-10-31T09:30:00Z",
        "bucketEndTime": "2026-11-01T09:30:00Z",
        "average": 37.8200,
        "minimum": 34.2000,
        "maximum": 41.5000,
        "sum": null,
        "firstReadingValue": 34.2000,
        "firstReadingAt": "2026-10-31T09:35:00Z",
        "lastReadingValue": 38.4000,
        "lastReadingAt": "2026-11-01T09:00:00Z",
        "readingCount": 288,
        "unit": "PERCENT"
      }
    },
    "availability": {
      "totalSensors": 4,
      "activeCount": 4,
      "inactiveCount": 0,
      "maintenanceCount": 0,
      "offlineCount": 0,
      "communicatingCount": 3,
      "unresponsiveCount": 1
    },
    "past24hDataQuality": {
      "totalReadings": 288,
      "validReadings": 280,
      "suspectReadings": 6,
      "invalidReadings": 2,
      "validPercentage": 97.2,
      "missingIntervals": 0,
      "delayedReadings": 1
    },
    "generatedAt": "2026-11-01T09:30:00Z"
  },
  "timestamp": "2026-11-01T09:30:00.123456Z"
}
```

---

## 5. Hardware Availability & Data Quality Model

### 5.1 Hardware Availability Classification
- **Operational Statuses (`SensorStatus`)**:
  - `ACTIVE`: Deployed and expected to transmit telemetry.
  - `INACTIVE`: Temporarily powered off or suspended.
  - `MAINTENANCE`: Device currently undergoing field calibration or physical repair.
  - `OFFLINE`: Decommissioned or permanently taken out of service.
- **Heartbeat Communication State**:
  - `communicating`: An `ACTIVE` device whose `lastSeenAt` is within the past 24 hours ($\le 24\text{h}$).
  - `unresponsive`: An `ACTIVE` device whose `lastSeenAt` is `null` or older than 24 hours ($> 24\text{h}$), indicating radio silence, battery exhaustion, or gateway disconnection.

### 5.2 Data Quality Classification
- **Reading Quality Flags (`ReadingQuality`)**:
  - `VALID`: Within legitimate physical sensor limits (e.g., soil moisture $0\% - 100\%$, ambient temperature $-30^\circ\text{C} - +60^\circ\text{C}$).
  - `SUSPECT`: Physically borderline or exhibiting anomalous rate-of-change.
  - `INVALID`: Physically impossible values, hardware sensor fault codes, or electrical shorts.
- **Data Quality Aggregation**:
  $$\text{validPercentage} = \text{round}\left( \frac{\text{validReadings}}{\text{totalReadings}} \times 100, 1 \right)$$
  If $\text{totalReadings} == 0$, $\text{validPercentage} = 100.0\%$.
