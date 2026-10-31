# Water Application Services & REST API Specification (`docs/water-api.md`)

## 1. Overview & Clean Architecture

Milestone 06.2 delivers the production-ready Application Services and REST API Layer for the **Water Management & Irrigation (`com.aquaai.water`)** domain. Built on top of the Milestone 06.1 domain and PostgreSQL database foundation, it orchestrates agricultural water quotas, scheduling, and physical consumption tracking for Yangiariq district.

The implementation strictly adheres to Clean / Hexagonal Architecture:

```text
HTTP Client (Web / Mobile / curl)
            │
            ▼
┌────────────────────────────────────────────────────────┐
│  API Layer (com.aquaai.water.api)                      │
│  - WaterSourceController (/api/v1/water-sources)       │
│  - WaterAllocationController (/api/v1/water-allocations)│
│  - IrrigationPlanController (/api/v1/irrigation-plans) │
│  - IrrigationEventController (/api/v1/irrigation-events)│
│  - Strongly-typed Request & Response DTOs              │
└────────────────────────────────────────────────────────┘
            │ Request DTOs
            ▼
┌────────────────────────────────────────────────────────┐
│  Application Layer (com.aquaai.water.application)      │
│  - Single-responsibility Use Cases                     │
│  - Field ownership verification & anti-enumeration     │
│  - Cross-aggregate invariant orchestration             │
│  - Declarative @Transactional boundaries               │
└────────────────────────────────────────────────────────┘
            │ Domain Invariants & Entity Factory Methods
            ▼
┌────────────────────────────────────────────────────────┐
│  Domain Layer (com.aquaai.water.domain)                │
│  - Aggregate Roots: WaterSource, WaterAllocation,      │
│    IrrigationPlan, IrrigationEvent                     │
│  - Repository Ports (Interfaces)                       │
│  - Rich domain methods & lifecycle state transitions   │
└────────────────────────────────────────────────────────┘
            │ Persistence Ports
            ▼
┌────────────────────────────────────────────────────────┐
│  Infrastructure Layer (com.aquaai.water.infrastructure)│
│  - Spring Data JPA Repositories                        │
│  - Repository Implementations & JPQL Filtering         │
│  - JPA Entities & PostgreSQL 16 Persistence            │
└────────────────────────────────────────────────────────┘
```

### Strict Architectural Boundaries
1. **Controllers are Pure HTTP Gateways**:
   - Zero business logic in controllers.
   - Never inject Spring Data or Domain repositories directly.
   - Never access domain entities or JPA entities directly.
   - Never interact with `SecurityContextHolder` directly (delegated to `CurrentUserProvider` in the application layer).
   - Only convert incoming HTTP requests to typed records, invoke the dedicated application use case, and wrap the resulting DTO in `ApiResponse<T>`.
2. **DTO Isolation**:
   - JPA entities and domain entities never escape to the API boundary.
   - Request DTOs use Jakarta Bean Validation (`@NotNull`, `@NotBlank`, `@Positive`, `@DecimalMin`, `@Size`).
   - Response DTOs are immutable Java records.
   - Client requests never accept `ownerId` or user identity fields; identity is authoritatively derived from the validated JWT token via `CurrentUserProvider`.

---

## 2. Role-Based Access Control (RBAC) & Security Matrix

Security enforcement operates at two distinct layers:
1. **API / Method Security**: Annotated with Spring Security `@PreAuthorize` where role restrictions apply.
2. **Application Defense-in-Depth**: Application use cases inspect `currentUserProvider.getRequiredUserRole()` and `currentUserProvider.getRequiredUserId()` to enforce regional vs parcel-level governance.

| Endpoint | Method | Path | FARMER | OPERATOR | ADMIN | Security Policy |
| :--- | :--- | :--- | :---: | :---: | :---: | :--- |
| **Water Source** | `POST` | `/api/v1/water-sources` | ❌ 403 | ✅ 201 | ✅ 201 | Regional infrastructure registration |
| | `GET` | `/api/v1/water-sources` | ✅ 200 | ✅ 200 | ✅ 200 | Public catalog for quota requests |
| | `GET` | `/api/v1/water-sources/{id}` | ✅ 200 | ✅ 200 | ✅ 200 | Source inspection |
| | `PATCH` | `/api/v1/water-sources/{id}` | ❌ 403 | ✅ 200 | ✅ 200 | Regional metadata update |
| | `PATCH` | `/api/v1/water-sources/{id}/activate` | ❌ 403 | ✅ 200 | ✅ 200 | Hydraulic availability lifecycle |
| | `PATCH` | `/api/v1/water-sources/{id}/deactivate` | ❌ 403 | ✅ 200 | ✅ 200 | Off-season or maintenance shutdown |
| **Water Allocation** | `POST` | `/api/v1/water-allocations` | ✅ (Own) | ✅ | ✅ | Parcel quota request; source must be ACTIVE |
| | `GET` | `/api/v1/water-allocations` | ✅ (Own) | ✅ (All) | ✅ (All) | Filtered by parcel fieldId |
| | `GET` | `/api/v1/water-allocations/{id}` | ✅ (Own) | ✅ | ✅ | Anti-enumeration 404 for foreign parcels |
| | `PATCH` | `/api/v1/water-allocations/{id}` | ✅ (Own) | ✅ | ✅ | Volume/period update while ACTIVE |
| | `PATCH` | `/api/v1/water-allocations/{id}/close` | ✅ (Own) | ✅ | ✅ | Natural quota close (`ACTIVE -> CLOSED`) |
| | `PATCH` | `/api/v1/water-allocations/{id}/cancel` | ✅ (Own) | ✅ | ✅ | Administrative revocation (`ACTIVE -> CANCELLED`) |
| **Irrigation Plan** | `POST` | `/api/v1/irrigation-plans` | ✅ (Own) | ✅ | ✅ | Schedule watering; cross-field allocation rejected |
| | `GET` | `/api/v1/irrigation-plans` | ✅ (Own) | ✅ (All) | ✅ (All) | Filtered by parcel and status |
| | `GET` | `/api/v1/irrigation-plans/{id}` | ✅ (Own) | ✅ | ✅ | Anti-enumeration 404 for foreign parcels |
| | `PATCH` | `/api/v1/irrigation-plans/{id}` | ✅ (Own) | ✅ | ✅ | Schedule update while PLANNED |
| | `PATCH` | `/api/v1/irrigation-plans/{id}/start` | ✅ (Own) | ✅ | ✅ | State transition (`PLANNED -> IN_PROGRESS`) |
| | `PATCH` | `/api/v1/irrigation-plans/{id}/complete` | ✅ (Own) | ✅ | ✅ | State transition (`IN_PROGRESS -> COMPLETED`) |
| | `PATCH` | `/api/v1/irrigation-plans/{id}/cancel` | ✅ (Own) | ✅ | ✅ | State transition (`PLANNED/IN_PROGRESS -> CANCELLED`) |
| **Irrigation Event**| `POST` | `/api/v1/irrigation-events` | ✅ (Own) | ✅ | ✅ | Real-time start (`STARTED`); cross-field plan rejected |
| | `POST` | `/api/v1/irrigation-events/completed` | ✅ (Own) | ✅ | ✅ | Direct record with actual volume (`COMPLETED`) |
| | `GET` | `/api/v1/irrigation-events` | ✅ (Own) | ✅ (All) | ✅ (All) | Filtered by parcel and status |
| | `GET` | `/api/v1/irrigation-events/{id}` | ✅ (Own) | ✅ | ✅ | Anti-enumeration 404 for foreign parcels |
| | `PATCH` | `/api/v1/irrigation-events/{id}/complete`| ✅ (Own) | ✅ | ✅ | Finalize volume & duration (`STARTED -> COMPLETED`) |
| | `PATCH` | `/api/v1/irrigation-events/{id}/cancel`| ✅ (Own) | ✅ | ✅ | Abort watering (`STARTED -> CANCELLED`) |

---

## 3. IDOR Prevention & Anti-Enumeration Security

Agricultural parcels and their water records represent sensitive commercial assets. To prevent malicious enumeration, Yangiariq Aqua AI implements an **Anti-Enumeration 404 Policy**:

```text
Farmer B (Attacker)
       │
       ▼
GET /api/v1/water-allocations/{Farmer_A_Allocation_ID}
       │
       ▼
GetWaterAllocationUseCase
       │
       ├── FieldOwnershipChecker.verifyFieldAccess(fieldId, callerId, callerRole)
       │         │
       │         ▼
       │   Caller does not own field -> throws ResourceNotFoundException("Field", fieldId)
       │
       ▼
Catches Field ResourceNotFoundException
       │
       ▼
Throws ResourceNotFoundException("WaterAllocation", allocationId)
       │
       ▼
GlobalExceptionHandler returns HTTP 404 NOT FOUND (RESOURCE_NOT_FOUND)
```

- An attacker receiving a `404 Not Found` cannot distinguish between a non-existent UUID and a valid UUID belonging to another farmer.
- `403 Forbidden` is reserved strictly for authenticated users attempting operations forbidden by their overarching role (e.g. `FARMER` attempting `POST /api/v1/water-sources`).

---

## 4. Cross-Aggregate Invariant Rules (422 UNPROCESSABLE_ENTITY)

The application layer validates relational consistency across decoupled aggregates:

1. **Active Water Source Requirement**:
   - `CreateWaterAllocationUseCase` verifies `waterSource.isActive()`.
   - Creating an allocation against an `INACTIVE` water source is rejected with `422 Unprocessable Entity` (`BUSINESS_RULE_VIOLATION`: *"Cannot allocate water from an INACTIVE water source"*).

2. **Cross-Field Allocation Tampering**:
   - `CreateIrrigationPlanUseCase` verifies that if an `allocationId` is supplied, `allocation.getFieldId().equals(request.fieldId())`.
   - Referencing an allocation belonging to Field 2 while creating a plan for Field 1 is rejected with `422 Unprocessable Entity` (`BUSINESS_RULE_VIOLATION`: *"Water allocation does not belong to field"*).

3. **Active Allocation Requirement**:
   - Linking a plan to an allocation in `CLOSED` or `CANCELLED` status is rejected with `422 Unprocessable Entity` (`BUSINESS_RULE_VIOLATION`: *"Cannot link irrigation plan to water allocation in status CLOSED"*).

4. **Cross-Field Plan Tampering**:
   - `StartIrrigationEventUseCase` and `RecordCompletedIrrigationEventUseCase` verify that if an `irrigationPlanId` is supplied, `plan.getFieldId().equals(request.fieldId())`.
   - Logging an irrigation event for Field 2 while linking a plan scheduled for Field 1 is rejected with `422 Unprocessable Entity` (`BUSINESS_RULE_VIOLATION`: *"Irrigation plan does not belong to field"*).

---

## 5. Endpoints Reference & Schemas

### 5.1 Water Source (`/api/v1/water-sources`)

#### `POST /api/v1/water-sources`
Creates a regional water intake or distribution infrastructure entity. Requires `OPERATOR` or `ADMIN` role.

**Request Body (`CreateWaterSourceRequest`):**
```json
{
  "name": "Palvon-Yobi Primary Canal",
  "type": "CANAL",
  "description": "Main gravity intake canal supplying Northern Yangiariq parcels"
}
```

**Response (HTTP 201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "name": "Palvon-Yobi Primary Canal",
    "type": "CANAL",
    "description": "Main gravity intake canal supplying Northern Yangiariq parcels",
    "status": "ACTIVE",
    "createdAt": "2026-04-01T08:00:00Z",
    "updatedAt": "2026-04-01T08:00:00Z"
  },
  "timestamp": "2026-04-01T08:00:00.123Z"
}
```

#### `GET /api/v1/water-sources`
Lists water sources with optional filtering and pagination. Open to all authenticated users.

**Query Parameters:**
- `query` (string, optional): Case-insensitive name search
- `type` (enum `WaterSourceType`, optional): `CANAL`, `DITCH`, `RESERVOIR`, `WELL`, `RIVER`, `OTHER`
- `status` (enum `WaterSourceStatus`, optional): `ACTIVE`, `INACTIVE`
- `page` (int, default `0`): Page index
- `size` (int, default `20`): Page size
- `sortBy` (string, default `createdAt`): Whitelisted sort property (`id`, `name`, `type`, `status`, `createdAt`, `updatedAt`)
- `direction` (string, default `desc`): `asc` or `desc`

---

### 5.2 Water Allocation (`/api/v1/water-allocations`)

#### `POST /api/v1/water-allocations`
Allocates seasonal volumetric water quota for a field from an active water source.

**Request Body (`CreateWaterAllocationRequest`):**
```json
{
  "fieldId": "3e6077f2-a0a8-4f37-9ec8-8cb018488a51",
  "waterSourceId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "allocatedVolumeM3": 2500.0000,
  "periodStart": "2026-04-01T00:00:00Z",
  "periodEnd": "2026-09-30T23:59:59Z",
  "notes": "Spring-Summer cotton vegetative seasonal quota"
}
```

**Response (HTTP 201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "e4b2d3c1-5f6a-4c7b-8910-111213141516",
    "fieldId": "3e6077f2-a0a8-4f37-9ec8-8cb018488a51",
    "waterSourceId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "allocatedVolumeM3": 2500.0000,
    "periodStart": "2026-04-01T00:00:00Z",
    "periodEnd": "2026-09-30T23:59:59Z",
    "status": "ACTIVE",
    "notes": "Spring-Summer cotton vegetative seasonal quota",
    "createdAt": "2026-04-01T08:15:00Z",
    "updatedAt": "2026-04-01T08:15:00Z"
  },
  "timestamp": "2026-04-01T08:15:00.456Z"
}
```

#### `PATCH /api/v1/water-allocations/{id}/close`
Closes an active allocation quota upon seasonal expiration or crop completion.

**Request Body (`CancelOperationRequest`, optional):**
```json
{
  "reason": "Harvest completed, season concluded"
}
```

---

### 5.3 Irrigation Plan (`/api/v1/irrigation-plans`)

#### `POST /api/v1/irrigation-plans`
Schedules an upcoming parcel watering event.

**Request Body (`CreateIrrigationPlanRequest`):**
```json
{
  "fieldId": "3e6077f2-a0a8-4f37-9ec8-8cb018488a51",
  "waterAllocationId": "e4b2d3c1-5f6a-4c7b-8910-111213141516",
  "plannedStartAt": "2026-05-10T04:00:00Z",
  "plannedEndAt": "2026-05-10T12:00:00Z",
  "plannedVolumeM3": 450.0000,
  "method": "FURROW",
  "notes": "First vegetative furrow flush before high temperatures"
}
```

**Response (HTTP 201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "fieldId": "3e6077f2-a0a8-4f37-9ec8-8cb018488a51",
    "waterAllocationId": "e4b2d3c1-5f6a-4c7b-8910-111213141516",
    "plannedStartAt": "2026-05-10T04:00:00Z",
    "plannedEndAt": "2026-05-10T12:00:00Z",
    "plannedVolumeM3": 450.0000,
    "method": "FURROW",
    "status": "PLANNED",
    "notes": "First vegetative furrow flush before high temperatures",
    "createdAt": "2026-05-01T10:00:00Z",
    "updatedAt": "2026-05-01T10:00:00Z"
  },
  "timestamp": "2026-05-01T10:00:00.789Z"
}
```

#### State Transition Endpoints:
- `PATCH /api/v1/irrigation-plans/{id}/start` (`PLANNED -> IN_PROGRESS`)
- `PATCH /api/v1/irrigation-plans/{id}/complete` (`IN_PROGRESS -> COMPLETED`)
- `PATCH /api/v1/irrigation-plans/{id}/cancel` (`PLANNED/IN_PROGRESS -> CANCELLED`)

---

### 5.4 Irrigation Event (`/api/v1/irrigation-events`)

#### `POST /api/v1/irrigation-events`
Initiates a real-time watering operation on a parcel.

**Request Body (`CreateIrrigationEventRequest`):**
```json
{
  "fieldId": "3e6077f2-a0a8-4f37-9ec8-8cb018488a51",
  "irrigationPlanId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "startAt": "2026-05-10T04:05:00Z",
  "notes": "Intake sluice gate opened manually"
}
```

**Response (HTTP 201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "f9e8d7c6-b5a4-3210-fedc-ba9876543210",
    "fieldId": "3e6077f2-a0a8-4f37-9ec8-8cb018488a51",
    "irrigationPlanId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "startedAt": "2026-05-10T04:05:00Z",
    "endedAt": null,
    "actualVolumeM3": null,
    "status": "STARTED",
    "notes": "Intake sluice gate opened manually",
    "createdAt": "2026-05-10T04:05:01Z",
    "updatedAt": "2026-05-10T04:05:01Z"
  },
  "timestamp": "2026-05-10T04:05:01.123Z"
}
```

#### `PATCH /api/v1/irrigation-events/{id}/complete`
Completes an in-progress watering event and records the measured volumetric consumption.

**Request Body (`CompleteIrrigationEventRequest`):**
```json
{
  "endAt": "2026-05-10T11:45:00Z",
  "actualVolumeM3": 442.8000,
  "notes": "Watering completed with uniform furrow distribution. Meter read recorded."
}
```

#### `POST /api/v1/irrigation-events/completed`
Directly records an already-completed watering operation (e.g. historical log or offline telemetry upload).

**Request Body (`RecordCompletedIrrigationEventRequest`):**
```json
{
  "fieldId": "3e6077f2-a0a8-4f37-9ec8-8cb018488a51",
  "irrigationPlanId": null,
  "startAt": "2026-05-09T22:00:00Z",
  "endAt": "2026-05-10T03:30:00Z",
  "actualVolumeM3": 380.0000,
  "notes": "Overnight supplemental irrigation"
}
```

---

## 6. Sorting Whitelists & Pagination Structure

All listing endpoints return `ApiResponse<PageResponse<T>>`.

### Pagination Parameters
| Parameter | Default | Constraint | Description |
| :--- | :---: | :--- | :--- |
| `page` | `0` | `page >= 0` | Zero-based page index |
| `size` | `20` | `1 <= size <= 100` | Number of items per page |
| `direction`| `desc` | `asc` or `desc` | Sorting direction |
| `sortBy` | `createdAt`| Entity-specific whitelist | Property to sort by |

### Whitelisted Sort Fields
- **WaterSource**: `id`, `name`, `type`, `status`, `createdAt`, `updatedAt`
- **WaterAllocation**: `id`, `allocatedVolumeM3`, `periodStart`, `periodEnd`, `status`, `createdAt`, `updatedAt`
- **IrrigationPlan**: `id`, `plannedStartAt`, `plannedEndAt`, `plannedVolumeM3`, `method`, `status`, `createdAt`, `updatedAt`
- **IrrigationEvent**: `id`, `startedAt`, `endedAt`, `actualVolumeM3`, `status`, `createdAt`, `updatedAt`

Any attempt to supply an unauthorized sort field falls back safely to `createdAt` to avoid SQL injection or query failure.

---

## 7. Error Handling & Status Codes

All errors conform to the platform standard:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable explanation"
  },
  "timestamp": "2026-10-31T15:49:32.134Z",
  "correlationId": "52b195c4-f615-4d97-9760-21b95880624c"
}
```

| HTTP Status | Error Code | Trigger Condition |
| :--- | :--- | :--- |
| **400 Bad Request** | `VALIDATION_ERROR` | Request body validation failure (blank name, negative volume, inverted period window) |
| **401 Unauthorized**| `UNAUTHORIZED` | Missing, expired, or malformed JWT Bearer token |
| **403 Forbidden** | `ACCESS_DENIED` | Caller role lacks permission (e.g. `FARMER` attempting to mutate `WaterSource`) |
| **404 Not Found** | `RESOURCE_NOT_FOUND`| Entity does not exist, or unowned parcel under anti-enumeration protection |
| **409 Conflict** | `DUPLICATE_RESOURCE`| Active water source with duplicate case-insensitive name already exists |
| **422 Unprocessable**| `BUSINESS_RULE_VIOLATION`| Domain invariant violation (inactive water source quota, cross-field allocation/plan mismatch, closed quota link) |
