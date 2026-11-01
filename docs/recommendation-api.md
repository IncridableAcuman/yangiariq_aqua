# YANGIARIQ AQUA AI — RECOMMENDATION REST API SPECIFICATION

## Overview
The Agronomic Recommendations REST API provides endpoints to generate, list, filter, inspect, and manage the lifecycle of agricultural proposals and decision-support alerts.

Base URL: `/api/v1`

---

## 1. Endpoints

### 1.1 List Recommendations
`GET /api/v1/recommendations`

Query Parameters:
- `fieldId` (UUID, optional): Filter by field parcel.
- `status` (string, optional): `GENERATED`, `VIEWED`, `ACCEPTED`, `DISMISSED`, `COMPLETED`, `EXPIRED`.
- `priority` (string, optional): `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `INFO`.
- `type` (string, optional): `IRRIGATE_NOW`, `DELAY_IRRIGATION`, etc.
- `from` (ISO-8601, optional): Temporal filter start.
- `to` (ISO-8601, optional): Temporal filter end.
- `page` (int, default: 0): Page offset.
- `size` (int, default: 20): Page size limit.

Response `200 OK`:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "fieldId": "7b134812-70b5-4f32-849c-f2305a4ecb32",
        "recommendationType": "IRRIGATE_NOW",
        "priority": "HIGH",
        "status": "GENERATED",
        "title": "Soil Hydration Deficit Detected",
        "summary": "Volumetric soil moisture dropped to 14.8%. Prompt irrigation required.",
        "explanation": "Cotton crop in sandy loam soil reached allowable depletion threshold.",
        "confidence": 0.88,
        "confidenceLevel": "HIGH",
        "validFrom": "2026-11-01T08:00:00Z",
        "validUntil": "2026-11-02T08:00:00Z",
        "generatedAt": "2026-11-01T08:00:00Z",
        "source": "HYBRID_AI",
        "limitations": "Provisional regional baseline calibration.",
        "isSimulated": false,
        "actions": [
          {
            "id": "e445a6c3-9828-4ad0-b8c3-5c8e310ab2e7",
            "actionOrder": 1,
            "actionText": "Initiate planned furrow irrigation with 45-55 mm depth",
            "urgency": "IMMEDIATE",
            "isNegativeAction": false
          },
          {
            "id": "3bb4a6c3-9828-4ad0-b8c3-5c8e310ab2e8",
            "actionOrder": 2,
            "actionText": "Do not apply fertilizer while water stressed",
            "urgency": "IMMEDIATE",
            "isNegativeAction": true
          }
        ],
        "evidence": [
          {
            "id": "a112a6c3-9828-4ad0-b8c3-5c8e310ab2e9",
            "evidenceType": "SENSOR_TELEMETRY",
            "metric": "SOIL_MOISTURE",
            "observedValue": 14.8,
            "baselineValue": 22.0,
            "unit": "%",
            "summary": "Moisture critically below MAD"
          }
        ],
        "createdAt": "2026-11-01T08:00:00Z",
        "updatedAt": "2026-11-01T08:00:00Z",
        "version": 0
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

---

### 1.2 Get Recommendation by ID
`GET /api/v1/recommendations/{id}`

- Returns `200 OK` with recommendation payload.
- Returns `404 Not Found` if not found or unauthorized farmer (Anti-IDOR).

---

### 1.3 On-Demand Recommendation Generation
`POST /api/v1/recommendations/generate?fieldId={fieldId}`
or
`POST /api/v1/fields/{fieldId}/recommendations/generate`

- Triggers immediate evaluation and deduplication check.
- Returns `201 Created` with the generated or existing active recommendation.

---

### 1.4 Mark Recommendation Viewed
`PATCH /api/v1/recommendations/{id}/view`

Transitions recommendation from `GENERATED` to `VIEWED`.

---

### 1.5 Accept Recommendation
`PATCH /api/v1/recommendations/{id}/accept`

Transitions recommendation to `ACCEPTED`.

---

### 1.6 Dismiss Recommendation
`PATCH /api/v1/recommendations/{id}/dismiss`

Request Body:
```json
{
  "reason": "Field was watered manually via secondary pump."
}
```
- Reason is mandatory (1–500 chars).
- Returns `400 Bad Request` if reason is blank.
- Transitions recommendation to terminal `DISMISSED` state.

---

### 1.7 Complete Recommendation
`PATCH /api/v1/recommendations/{id}/complete`

Transitions recommendation from `ACCEPTED` to terminal `COMPLETED` state.

---

### 1.8 Recommendation KPI Summary
`GET /api/v1/recommendations/summary` (optional `?fieldId={fieldId}`)
or
`GET /api/v1/fields/{fieldId}/recommendations/summary`

Response `200 OK`:
```json
{
  "success": true,
  "data": {
    "totalActive": 4,
    "criticalCount": 1,
    "highCount": 2,
    "mediumCount": 1,
    "lowCount": 0,
    "infoCount": 0,
    "generatedCount": 2,
    "viewedCount": 1,
    "acceptedCount": 1,
    "dismissedCount": 3,
    "completedCount": 5,
    "expiredCount": 2
  }
}
```
