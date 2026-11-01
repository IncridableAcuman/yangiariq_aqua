# Anomaly Detection & Intelligent Alerting REST API (`docs/anomaly-api.md`)

## 1. Overview & Conventions

The Anomaly REST API exposes complete control over telemetry anomalies, lifecycle state transitions, diagnostic evidence, and configurable detection rules.

### Standard Response Structure:
All endpoints return standard platform envelopes:
- Single Object: `ApiResponse<T>` with fields `success`, `data`, and `timestamp`.
- Paginated: `ApiResponse<PageResponse<T>>` with fields `content`, `page`, `size`, `totalElements`, `totalPages`.
- Error: RFC 7807 `application/problem+json` format with `type`, `title`, `status`, `detail`, `instance`, `code`, and `timestamp`.

---

## 2. Security & RBAC Access Matrix

| Endpoint | Method | FARMER | OPERATOR | ADMIN | Description |
| :--- | :---: | :---: | :---: | :---: | :--- |
| `GET /api/v1/anomalies` | `GET` | Own fields only | ✅ District-wide | ✅ Full | List anomalies with flexible filtering and pagination |
| `GET /api/v1/anomalies/{id}` | `GET` | Parcel owner only | ✅ District-wide | ✅ Full | Get detailed anomaly record including evidence |
| `GET /api/v1/fields/{fieldId}/anomalies` | `GET` | Parcel owner only | ✅ District-wide | ✅ Full | List historical anomalies for a specific field parcel |
| `GET /api/v1/sensors/{sensorId}/anomalies` | `GET` | Parcel owner only | ✅ District-wide | ✅ Full | List historical anomalies for a specific sensor |
| `PATCH /api/v1/anomalies/{id}/acknowledge` | `PATCH` | Parcel owner only | ✅ Yes | ✅ Full | Acknowledge active anomaly |
| `PATCH /api/v1/anomalies/{id}/investigate` | `PATCH` | Parcel owner only | ✅ Yes | ✅ Full | Mark anomaly as under field investigation |
| `PATCH /api/v1/anomalies/{id}/resolve` | `PATCH` | Parcel owner only | ✅ Yes | ✅ Full | Mark anomaly as resolved (notes mandatory) |
| `PATCH /api/v1/anomalies/{id}/dismiss` | `PATCH` | Parcel owner only | ✅ Yes | ✅ Full | Dismiss anomaly as false positive (notes mandatory) |
| `GET /api/v1/anomalies/summary` | `GET` | Own fields only | ✅ District-wide | ✅ Full | Aggregate count summary across severities and statuses |
| `GET /api/v1/fields/{fieldId}/anomalies/summary` | `GET` | Parcel owner only | ✅ District-wide | ✅ Full | Active anomaly summary for GIS parcel integration |
| `GET /api/v1/anomaly-rules` | `GET` | ✅ Read-only | ✅ Read-only | ✅ Full | List configurable detection rules and thresholds |
| `GET /api/v1/anomaly-rules/{id}` | `GET` | ✅ Read-only | ✅ Read-only | ✅ Full | Get specific detection rule metadata |
| `PUT /api/v1/anomaly-rules/{id}` | `PUT` | ❌ 403 Forbidden | ❌ 403 Forbidden | ✅ Full | Update detection rule thresholds and parameters |
| `PATCH /api/v1/anomaly-rules/{id}/toggle` | `PATCH` | ❌ 403 Forbidden | ❌ 403 Forbidden | ✅ Full | Enable or disable detection rule |

### Anti-IDOR Security Implementation (Refinement #7)
- **Anti-Enumeration 404**: When a farmer requests an anomaly belonging to another farmer's parcel, the system responds with **`404 Not Found`** instead of `403 Forbidden`, preventing attacker enumeration of active alerts across the district.

---

## 3. Endpoints Specification

### 3.1 List Anomalies
- **Path**: `GET /api/v1/anomalies`
- **Query Parameters**:
  - `fieldId` (`UUID`, optional): Filter by field parcel.
  - `sensorId` (`UUID`, optional): Filter by telemetry sensor.
  - `status` (`AnomalyStatus`, optional): `DETECTED`, `ACKNOWLEDGED`, `INVESTIGATING`, `RESOLVED`, `DISMISSED`.
  - `severity` (`AnomalySeverity`, optional): `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `INFO`.
  - `anomalyType` (`AnomalyType`, optional): Filter by specific anomaly classification.
  - `sensorType` (`SensorType`, optional): Filter by telemetry sensor modality.
  - `from` (`Instant`, optional): ISO-8601 start date.
  - `to` (`Instant`, optional): ISO-8601 end date.
  - `page` (`int`, optional, default `0`): Page index.
  - `size` (`int`, optional, default `20`): Page size (max `100`).

#### Sample Response (`200 OK`):
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "e5c70b8a-1311-4ca5-985f-8468307d0f01",
        "fieldId": "1b34d781-6453-488b-a279-b1d55e8c1872",
        "sensorId": "c83b8ec8-854e-4bca-86be-8f38c3c1db64",
        "sensorType": "SOIL_MOISTURE",
        "anomalyType": "SOIL_MOISTURE_CRITICAL",
        "severity": "CRITICAL",
        "status": "DETECTED",
        "detectedValue": 11.5000,
        "expectedValue": 25.0000,
        "deviation": -13.5000,
        "confidence": 0.9500,
        "isSimulated": false,
        "description": "Tuproq namligi kritik darajada kamaydi: 11.50% (chegara: 15.00%)",
        "explanation": "Tuproq namligi kritik darajadan (15.00%) pastga tushib ketgan.",
        "source": "RULE_ENGINE",
        "observationCount": 2,
        "firstObservedAt": "2026-11-01T08:00:00Z",
        "lastObservedAt": "2026-11-01T08:05:00Z",
        "detectedAt": "2026-11-01T08:00:00Z",
        "evidence": [
          {
            "id": "f1011111-2222-3333-4444-555555555555",
            "evidenceType": "TRIGGERING_READING",
            "metric": "SOIL_MOISTURE",
            "observedValue": 11.5000,
            "expectedValue": 25.0000,
            "measuredAt": "2026-11-01T08:00:00Z",
            "metadata": "Reading ID: ..."
          }
        ],
        "createdAt": "2026-11-01T08:00:00Z",
        "updatedAt": "2026-11-01T08:05:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-11-01T08:05:02.124Z"
}
```

---

### 3.2 Transition Anomaly State (Resolve / Dismiss)
- **Path**: `PATCH /api/v1/anomalies/{id}/resolve`
- **Request Body**:
```json
{
  "notes": "Sug'orish tarmog'i nazorat qilindi, nasos filtri tozalanib bosim normallashtirildi."
}
```

#### Validation Error Example (`400 Bad Request`):
```json
{
  "type": "https://api.yangiariq-aqua.uz/errors/validation",
  "title": "Validation Error",
  "status": 400,
  "detail": "Action notes are required when resolving or dismissing an anomaly",
  "code": "VALIDATION_FAILED",
  "timestamp": "2026-11-01T08:10:00Z"
}
```

---

### 3.3 Anomaly Summary KPI
- **Path**: `GET /api/v1/anomalies/summary`

#### Sample Response (`200 OK`):
```json
{
  "success": true,
  "data": {
    "totalActive": 3,
    "criticalCount": 1,
    "highCount": 1,
    "mediumCount": 1,
    "lowCount": 0,
    "infoCount": 0,
    "detectedCount": 2,
    "acknowledgedCount": 1,
    "investigatingCount": 0,
    "resolvedCount": 14,
    "dismissedCount": 2
  },
  "timestamp": "2026-11-01T08:15:00Z"
}
```

---

### 3.4 Update Detection Rule Thresholds (Admin Only)
- **Path**: `PUT /api/v1/anomaly-rules/{id}`
- **Request Body**:
```json
{
  "name": "Tuproq namligi kritik kamayishi (Kuzgi me'yor)",
  "description": "Kuzgi chopiq davri uchun moslashtirilgan chegara",
  "severity": "CRITICAL",
  "threshold": 16.5000,
  "secondaryThreshold": 26.0000,
  "timeWindowMinutes": 45,
  "minConsecutiveReadings": 2
}
```

#### Sample Response (`200 OK`):
```json
{
  "success": true,
  "data": {
    "id": "a1111111-1111-1111-1111-111111111101",
    "ruleCode": "RULE_SOIL_MOISTURE_DEFICIT",
    "sensorType": "SOIL_MOISTURE",
    "name": "Tuproq namligi kritik kamayishi (Kuzgi me'yor)",
    "description": "Kuzgi chopiq davri uchun moslashtirilgan chegara",
    "enabled": true,
    "severity": "CRITICAL",
    "threshold": 16.5000,
    "secondaryThreshold": 26.0000,
    "timeWindowMinutes": 45,
    "minConsecutiveReadings": 2,
    "createdAt": "2026-01-01T00:00:00Z",
    "updatedAt": "2026-11-01T08:20:00Z"
  },
  "timestamp": "2026-11-01T08:20:00Z"
}
```
