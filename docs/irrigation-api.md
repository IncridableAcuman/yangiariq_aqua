# YANGIARIQ AQUA AI — SMART IRRIGATION REST API SPECIFICATION

**API Version:** `v1`  
**Base Path:** `/api/v1`  
**Target Milestone:** 08.2  

---

## 1. Overview & Authentication

All endpoints require JWT Bearer authentication:
```http
Authorization: Bearer <accessToken>
```

### Role-Based Access Control (RBAC) & Anti-IDOR
- **`FARMER`**: Can only view, generate, approve, and cancel plans for parcels they own. Any attempt to access, generate, or modify a plan for a parcel owned by another farmer immediately returns **`HTTP 404 Not Found`** (anti-enumeration security).
- **`OPERATOR` & `ADMIN`**: District-wide read and operational dispatch authority across all parcels in Yangiariq.

---

## 2. API Endpoints

### 2.1 List Smart Irrigation Plans
- **Method:** `GET`
- **Path:** `/api/v1/irrigation/plans`
- **Query Parameters:**
  - `fieldId` (optional, UUID): Filter by field parcel
  - `status` (optional, Enum): `DRAFT`, `PROPOSED`, `APPROVED`, `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `EXPIRED`
  - `priority` (optional, Enum): `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`
  - `dateFrom` (optional, ISO Date): Start date filter (`YYYY-MM-DD`)
  - `dateTo` (optional, ISO Date): End date filter (`YYYY-MM-DD`)
  - `page` (optional, int, default 0): Page index
  - `size` (optional, int, default 20): Page size
- **Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "e6a21330-47b2-4d32-9cb5-bbdfc833211e",
        "fieldId": "73e93bfa-9ab2-4b2a-8c76-599d10e0147c",
        "waterAllocationId": "0982bbfe-d3f3-4a11-82d2-28df324bf891",
        "status": "PROPOSED",
        "priority": "HIGH",
        "plannedDate": "2026-11-02",
        "plannedStartAt": "2026-11-02T06:00:00Z",
        "plannedEndAt": "2026-11-02T10:00:00Z",
        "recommendedVolumeM3": 150.0,
        "estimatedDurationMinutes": 240,
        "recommendedFlowRateM3H": 37.5,
        "targetMoisturePercent": 70.0,
        "currentMoisturePercent": 45.0,
        "confidenceLevel": "HIGH",
        "confidenceScore": 0.92,
        "reason": "Root-zone moisture deficit (45% vs 70% target) on Cotton parcel requires 150 m³.",
        "reasoningSummary": "Hargreaves-Samani ET0 estimated at 5.2 mm/day with zero rain forecast.",
        "isBlocked": false,
        "blockedReason": null,
        "source": "DETERMINISTIC_SCHEDULER",
        "constraints": [
          {
            "id": "c3e98212-32a1-4322-89cd-4982aef1234a",
            "type": "HYDRAULIC_SAFETY",
            "status": "SATISFIED",
            "message": "Line integrity verified normal. No pipe burst anomalies."
          }
        ],
        "tradeoffs": []
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2026-11-01T10:00:00Z"
}
```

---

### 2.2 Get Smart Irrigation Plan by ID
- **Method:** `GET`
- **Path:** `/api/v1/irrigation/plans/{id}`
- **Response:** `200 OK` (or `404 Not Found` if non-existent or owned by another farmer)

---

### 2.3 Get Active Smart Plan for Parcel
- **Method:** `GET`
- **Path:** `/api/v1/fields/{fieldId}/irrigation/plans/active`
- **Response:** `200 OK` with plan object, or `404 Not Found` if no active plan exists.

---

### 2.4 Generate Smart Irrigation Plan
- **Method:** `POST`
- **Path:** `/api/v1/fields/{fieldId}/irrigation/plans/generate`
- **Request Body (optional):**
```json
{
  "targetDate": "2026-11-02"
}
```
- **Response:** `201 Created` with generated `SmartIrrigationPlanResponse`.
- **Idempotency:** If an active plan already exists for that field and date, the existing plan is returned without duplicate creation.

---

### 2.5 Approve Smart Irrigation Plan
- **Method:** `POST`
- **Path:** `/api/v1/irrigation/plans/{id}/approve`
- **Request Body:**
```json
{
  "acknowledgeWarnings": true
}
```
- **Response:** `200 OK` with updated status `APPROVED`.
- **Validation:**
  - If plan `isBlocked == true`, returns `HTTP 400 Bad Request` (*"Cannot approve plan blocked by safety interlock"*).
  - If plan contains warning constraints and `acknowledgeWarnings != true`, returns `HTTP 400 Bad Request`.

---

### 2.6 Cancel Smart Irrigation Plan
- **Method:** `POST`
- **Path:** `/api/v1/irrigation/plans/{id}/cancel`
- **Request Body (Mandatory):**
```json
{
  "reason": "Main canal cleaning in progress by water authority."
}
```
- **Response:** `200 OK` with status `CANCELLED`.
- **Validation:** `reason` must not be blank (min 3 chars).

---

### 2.7 Complete Smart Irrigation Plan
- **Method:** `POST`
- **Path:** `/api/v1/irrigation/plans/{id}/complete`
- **Request Body (optional):**
```json
{
  "actualVolumeDeliveredM3": 148.5
}
```
- **Response:** `200 OK` with status `COMPLETED`.

---

### 2.8 Get Field Water Budget
- **Method:** `GET`
- **Path:** `/api/v1/fields/{fieldId}/water-budget`
- **Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "fieldId": "73e93bfa-9ab2-4b2a-8c76-599d10e0147c",
    "totalAllocatedM3": 10000.0,
    "consumedM3": 6000.0,
    "committedM3": 1500.0,
    "reservedM3": 500.0,
    "remainingAvailableM3": 2000.0
  },
  "timestamp": "2026-11-01T10:00:00Z"
}
```
