# Sensor & Telemetry REST API Specification (`docs/sensor-api.md`)

## 1. Overview & Architecture

Milestone 07.1 delivers the foundational REST API layer for the **Sensor & Telemetry (`com.aquaai.sensor.api`)** domain. It exposes endpoints for sensor device lifecycle management, telemetry recording, latest reading inspection, paginated historical time-series retrieval, and realistic telemetry drift simulation.

The API strictly adheres to project conventions:
- Standard REST semantics under `/api/v1/sensors` and `/api/v1/fields/{fieldId}/sensors`.
- Uniform response wrapper: `ApiResponse<T>` with `success: boolean`, `data: T`, and `timestamp: Instant`.
- RFC 7807 problem details for errors.
- Strict Role-Based Access Control (RBAC) and Anti-IDOR security via `FieldOwnershipChecker`.

---

## 2. Security & RBAC Matrix

| Endpoint | Method | FARMER | OPERATOR | ADMIN | Description |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `/api/v1/fields/{fieldId}/sensors` | `POST` | Owner only | ❌ (403) | ✅ Full | Register new sensor device on field |
| `/api/v1/sensors/{id}` | `GET` | Parcel owner | ✅ Read-only | ✅ Full | Get sensor device details |
| `/api/v1/fields/{fieldId}/sensors` | `GET` | Parcel owner | ✅ Read-only | ✅ Full | List sensors on a field |
| `/api/v1/sensors/{id}` | `PUT` | Parcel owner | ❌ (403) | ✅ Full | Update sensor details |
| `/api/v1/sensors/{id}/activate` | `PATCH` | Parcel owner | ❌ (403) | ✅ Full | Transition sensor to ACTIVE |
| `/api/v1/sensors/{id}/deactivate` | `PATCH` | Parcel owner | ❌ (403) | ✅ Full | Transition sensor to INACTIVE |
| `/api/v1/sensors/{id}/maintenance` | `PATCH` | Parcel owner | ❌ (403) | ✅ Full | Transition sensor to MAINTENANCE |
| `/api/v1/sensors/{id}/readings` | `POST` | Parcel owner | ❌ (403) | ✅ Full | Record telemetry reading |
| `/api/v1/sensors/{id}/latest-reading` | `GET` | Parcel owner | ✅ Read-only | ✅ Full | Get latest reading for sensor |
| `/api/v1/sensors/{id}/readings` | `GET` | Parcel owner | ✅ Read-only | ✅ Full | Paginated sensor time-series history |
| `/api/v1/fields/{fieldId}/readings` | `GET` | Parcel owner | ✅ Read-only | ✅ Full | Paginated field parcel time-series |
| `/api/v1/fields/{fieldId}/readings/latest-summary`| `GET` | Parcel owner | ✅ Read-only | ✅ Full | Latest reading grouped per sensor type |
| `/api/v1/sensors/{id}/simulate` | `POST` | Parcel owner | ❌ (403) | ✅ Full | Trigger drift simulation reading |

*Note: For farmers accessing sensors or fields they do NOT own, the API responds with `404 Not Found` rather than `403 Forbidden` to prevent object identifier enumeration.*

---

## 3. Endpoints Detail

### 3.1 Register Sensor Device
- **URL**: `POST /api/v1/fields/{fieldId}/sensors`
- **Status**: `201 Created`
- **Request Body**:
```json
{
  "name": "Soil Moisture Probe - Zone 1",
  "deviceIdentifier": "SN-SM-2026-0042",
  "type": "SOIL_MOISTURE",
  "provider": "SIMULATED",
  "description": "Installed at 30cm root depth in furrow head"
}
```
- **Response**:
```json
{
  "success": true,
  "data": {
    "id": "e7c4f4b2-29e2-4ef8-a1c9-76b6b772c510",
    "fieldId": "9fa8b123-5e3a-4299-a68b-70c8cf85d9b2",
    "name": "Soil Moisture Probe - Zone 1",
    "deviceIdentifier": "SN-SM-2026-0042",
    "type": "SOIL_MOISTURE",
    "status": "ACTIVE",
    "provider": "SIMULATED",
    "description": "Installed at 30cm root depth in furrow head",
    "installedAt": "2026-11-01T04:20:00Z",
    "lastSeenAt": null,
    "createdAt": "2026-11-01T04:20:00Z",
    "updatedAt": "2026-11-01T04:20:00Z"
  },
  "timestamp": "2026-11-01T04:20:00Z"
}
```

---

### 3.2 Get Sensor Device Details
- **URL**: `GET /api/v1/sensors/{id}`
- **Status**: `200 OK`
- **Response**: Returns `SensorResponse` object wrapped in `ApiResponse`.

---

### 3.3 List Sensors on a Field
- **URL**: `GET /api/v1/fields/{fieldId}/sensors`
- **Status**: `200 OK`
- **Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": "e7c4f4b2-29e2-4ef8-a1c9-76b6b772c510",
      "fieldId": "9fa8b123-5e3a-4299-a68b-70c8cf85d9b2",
      "name": "Soil Moisture Probe - Zone 1",
      "deviceIdentifier": "SN-SM-2026-0042",
      "type": "SOIL_MOISTURE",
      "status": "ACTIVE",
      "provider": "SIMULATED",
      "description": "Installed at 30cm depth",
      "installedAt": "2026-11-01T04:20:00Z",
      "lastSeenAt": "2026-11-01T04:25:00Z",
      "createdAt": "2026-11-01T04:20:00Z",
      "updatedAt": "2026-11-01T04:25:00Z"
    }
  ],
  "timestamp": "2026-11-01T04:25:00Z"
}
```

---

### 3.4 Update Sensor Details
- **URL**: `PUT /api/v1/sensors/{id}`
- **Status**: `200 OK`
- **Request Body**:
```json
{
  "name": "Updated Sensor Name",
  "description": "New calibration notes"
}
```

---

### 3.5 Lifecycle Transitions
- **`PATCH /api/v1/sensors/{id}/activate`**: Transitions to `ACTIVE`.
- **`PATCH /api/v1/sensors/{id}/deactivate`**: Transitions to `INACTIVE`.
- **`PATCH /api/v1/sensors/{id}/maintenance`**: Transitions to `MAINTENANCE`.
- **Status**: `200 OK`
- **Error**: `400 Bad Request` or `422 Unprocessable Entity` if illegal transition attempted.

---

### 3.6 Record Telemetry Reading
- **URL**: `POST /api/v1/sensors/{id}/readings`
- **Status**: `201 Created`
- **Request Body**:
```json
{
  "value": 48.75,
  "unit": "PERCENT",
  "quality": "VALID",
  "source": "FIELD_GATEWAY_01",
  "measuredAt": "2026-11-01T04:30:00Z"
}
```
- **Response**:
```json
{
  "success": true,
  "data": {
    "id": "18c8e19e-e374-4b55-a50d-85fa76451000",
    "sensorId": "e7c4f4b2-29e2-4ef8-a1c9-76b6b772c510",
    "fieldId": "9fa8b123-5e3a-4299-a68b-70c8cf85d9b2",
    "type": "SOIL_MOISTURE",
    "value": 48.7500,
    "unit": "PERCENT",
    "quality": "VALID",
    "source": "FIELD_GATEWAY_01",
    "measuredAt": "2026-11-01T04:30:00Z",
    "createdAt": "2026-11-01T04:30:01Z"
  },
  "timestamp": "2026-11-01T04:30:01Z"
}
```

---

### 3.7 Get Latest Reading for Sensor
- **URL**: `GET /api/v1/sensors/{id}/latest-reading`
- **Status**: `200 OK`
- **Response**: Returns the most recent `SensorReadingResponse` or `null` if no readings recorded yet.

---

### 3.8 Paginated Historical Telemetry for Sensor
- **URL**: `GET /api/v1/sensors/{id}/readings`
- **Query Parameters**:
  - `from`: ISO-8601 instant (default: 30 days ago)
  - `to`: ISO-8601 instant (default: now)
  - `page`: 0-indexed page number (default: 0)
  - `size`: page size, max 100 (default: 20)
  - `sort`: `desc` or `asc` (default: `desc`)
- **Status**: `200 OK`
- **Validation**: If `from` is after `to`, returns `422 Unprocessable Entity`.
- **Response**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "18c8e19e-e374-4b55-a50d-85fa76451000",
        "sensorId": "e7c4f4b2-29e2-4ef8-a1c9-76b6b772c510",
        "fieldId": "9fa8b123-5e3a-4299-a68b-70c8cf85d9b2",
        "type": "SOIL_MOISTURE",
        "value": 48.7500,
        "unit": "PERCENT",
        "quality": "VALID",
        "source": "FIELD_GATEWAY_01",
        "measuredAt": "2026-11-01T04:30:00Z",
        "createdAt": "2026-11-01T04:30:01Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2026-11-01T04:30:02Z"
}
```

---

### 3.9 Field Telemetry Summary
- **URL**: `GET /api/v1/fields/{fieldId}/readings/latest-summary`
- **Status**: `200 OK`
- **Response**: Aggregates the single latest reading for each active sensor type on the field parcel:
```json
{
  "success": true,
  "data": {
    "fieldId": "9fa8b123-5e3a-4299-a68b-70c8cf85d9b2",
    "readingsByType": {
      "SOIL_MOISTURE": {
        "id": "18c8e19e-e374-4b55-a50d-85fa76451000",
        "sensorId": "e7c4f4b2-29e2-4ef8-a1c9-76b6b772c510",
        "fieldId": "9fa8b123-5e3a-4299-a68b-70c8cf85d9b2",
        "type": "SOIL_MOISTURE",
        "value": 48.7500,
        "unit": "PERCENT",
        "quality": "VALID",
        "source": "FIELD_GATEWAY_01",
        "measuredAt": "2026-11-01T04:30:00Z",
        "createdAt": "2026-11-01T04:30:01Z"
      },
      "TEMPERATURE": {
        "id": "22a9b34f-f111-44aa-9900-112233445566",
        "sensorId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
        "fieldId": "9fa8b123-5e3a-4299-a68b-70c8cf85d9b2",
        "type": "TEMPERATURE",
        "value": 27.8000,
        "unit": "CELSIUS",
        "quality": "VALID",
        "source": "FIELD_GATEWAY_01",
        "measuredAt": "2026-11-01T04:30:00Z",
        "createdAt": "2026-11-01T04:30:01Z"
      }
    },
    "generatedAt": "2026-11-01T04:30:05Z"
  },
  "timestamp": "2026-11-01T04:30:05Z"
}
```

---

### 3.10 Simulate Drift Telemetry Reading
- **URL**: `POST /api/v1/sensors/{id}/simulate`
- **Status**: `201 Created`
- **Description**: Evaluates the bounded random-walk drift provider for the sensor's physical modality, stores the new reading, and advances the sensor's `lastSeenAt` heartbeat.
- **Response**: `SensorReadingResponse`
