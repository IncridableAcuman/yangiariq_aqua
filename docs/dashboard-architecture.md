# Unified Farm Intelligence Dashboard & Action Center Architecture

## 1. Architectural Overview & Purpose

Milestone 08.4 transforms Yangiariq Aqua AI from a collection of isolated operational modules into a unified **Farm Intelligence Workspace & Action Center**. Prior to this milestone, agricultural operators and farmers had to manually correlate telemetry, water balances, anomalies, recommendations, and irrigation schedules across 7+ distinct screens. 

The Dashboard module (`com.aquaai.dashboard`) serves as an authoritative **Read Model / Aggregation & Decision-Support Orchestrator**. It answers the primary operational question for farmers and operators within 10 seconds of login:
> **"What needs attention, why does it need attention, is there enough water, is irrigation safe, and what discrete action should I take right now?"**

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Dashboard API Controller                        │
│                   GET /api/v1/dashboard/overview                       │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   GetDashboardOverviewUseCase                          │
│     • Tenant Context Validation (Farmer / Operator / Water Admin)      │
│     • Batch Domain Repository Queries (Zero N+1)                       │
└───────────────┬───────────────────┬───────────────────┬────────────────┘
                │                   │                   │
                ▼                   ▼                   ▼
┌───────────────────────┐ ┌───────────────────┐ ┌────────────────────────┐
│ PriorityScoringService│ │ActionOrchestrator │ │ WaterBudgetAggregator  │
│ • Moisture Deficits   │ │ • Urgent Anomalies│ │ • Authoritative Math   │
│ • Crop Criticality    │ │ • Actions & CTAs  │ │ • Allocated/Consumed/  │
│ • Hydraulic Safety    │ │ • 1-Click Nav     │ │   Committed/Remaining  │
└───────────────────────┘ └───────────────────┘ └────────────────────────┘
```

---

## 2. Core Architectural Principles & Boundaries

1. **Read-Model / Query Orchestration Only**:
   - The dashboard owns **no transactional domain state** and creates **no new primary business tables**.
   - It performs read-only projections over existing domain repositories (`Field`, `Sensor`, `Anomaly`, `Recommendation`, `SmartIrrigationPlan`, `WaterConsumption`, `Problem`).
2. **Authoritative Calculation Preservation**:
   - The dashboard **does not re-implement domain math**.
   - Water balances use `WaterBudget` calculations: `Remaining = Allocated - Consumed - Approved - Committed`.
   - Priority scoring directly respects `HydraulicSafetyInterlock` and `IrrigationPriorityCalculator`.
   - Sensor thresholds respect the domain-configured agricultural wilting and field capacity bounds.
3. **Strict Tenancy & Anti-IDOR Enforcement**:
   - **`FARMER`**: Strictly isolated to parcels owned by the authenticated `userId`. All child metrics (sensors, anomalies, recommendations, irrigation plans, water consumption) are partitioned by `fieldId IN (farmerFields)`.
   - **`OPERATOR`**: Bounded to assigned district/canal system parcels.
   - **`WATER_ADMIN`**: Global system-wide visibility with district aggregations.
4. **Boundary Isolation & ArchUnit Governance**:
   - Tested under ArchUnit Rules 52-55 in `ArchitectureTests.java`.
   - Dashboard classes must not depend on low-level telemetry hardware protocols, valve actuators, or internal write-service implementations.

---

## 3. High-Performance Batch Read Model (Zero N+1)

To avoid frontend waterfall latency (which previously required 7+ separate HTTP calls), the backend endpoint executes single-pass indexed batch queries:

| Query Target | Method Invocation | Optimization Strategy |
| :--- | :--- | :--- |
| **Parcels** | `fieldRepository.findByFarmerId(farmerId)` | Filtered by owner index |
| **Sensors** | `sensorRepository.findByFieldIdIn(fieldIds)` | Single SQL `IN` query on indexed `field_id` |
| **Latest Telemetry** | `sensorReadingRepository.findLatestSoilMoistureByFieldIdIn(fieldIds)` | Window function / correlated subquery |
| **Active Anomalies** | `anomalyRepository.findActiveByFieldIdIn(fieldIds)` | Filtered by status `DETECTED` / `ACKNOWLEDGED` |
| **Recommendations** | `recommendationRepository.findActiveByFieldIdIn(fieldIds)` | Filtered by status `PENDING` / `ADOPTED` |
| **Irrigation Plans** | `smartIrrigationPlanRepository.findByFieldIdIn(fieldIds)` | Filtered by seasonal plan active states |
| **Assessments** | `agronomicAssessmentRepository.findLatestByFieldIdIn(fieldIds)` | Batch fetch latest temporal signals |
| **Water Usage** | `waterConsumptionQueryRepository.findUsageSummaryBySeason(season, fieldIds)` | Direct aggregate projection |

---

## 4. Deterministic Priority Scoring Engine

The `PriorityScoringService` implements an explainable, deterministic scoring model ($0 \dots 100$) that dynamically classifies every agricultural parcel into four priority bands:

```
Score >= 80  ──► CRITICAL (Red)
Score 60-79  ──► HIGH (Orange)
Score 35-59  ──► MEDIUM (Yellow)
Score < 35   ──► LOW (Green / Normal)
```

### Scoring Evaluation Rules

| Condition | Points Added | Agricultural / Safety Explanation |
| :--- | :--- | :--- |
| **Severe Drought Stress** | `+65` pts | Soil moisture is below wilting point ($< 15\%$). Risk of irreversible crop yield loss. |
| **Moderate Moisture Deficit** | `+35` pts | Soil moisture is below depletion threshold ($< 25\%$). Crop transpiration is restricted. |
| **Mild Moisture Deficit** | `+15` pts | Soil moisture is below optimal capacity ($< 35\%$). |
| **Critical Crop Stage** | `+20` pts | Crop is in flowering/bolling or grain-fill stage (`COTTON`, `WHEAT`). High water sensitivity. |
| **Active Critical Anomaly** | `+40` pts | Active critical anomaly (e.g. pipe burst, pressure drop, sensor malfunction). |
| **Active High Anomaly** | `+25` pts | High-severity anomaly affecting delivery or canal intake. |
| **Pending Recommendation** | `+30` pts | Agronomic recommendation generated with confidence $\ge 80\%$. |
| **Scheduled Irrigation Today** | `+20` pts | Approved irrigation plan awaiting execution or currently active. |
| **Hydraulic Safety Hazard** | `Score Capped at 30` | If canal flow is restricted, intake locked, or safety gate closed, score cannot trigger urgent irrigation. Explanation highlights hydraulic interlock. |

Every scored field response contains an explainable list of human-readable rationale strings (e.g., `"Critical drought: Soil moisture 12.4% is below wilting point 15%"`, `"Active critical anomaly: PIPE_BURST_SUSPECTED detected"`).

---

## 5. Action Center Orchestration

The `ActionCenterOrchestrator` scans cross-domain anomalies, safety states, recommendations, and water budgets to synthesize prioritized, actionable tasks:

1. **Urgent Hydraulic / Telemetry Anomalies** (`CRITICAL`):
   - Direct CTA: `/anomalies?id={anomalyId}`
   - Action label: *"Avaryani tekshirish"* / *"Review Anomaly"*
2. **Pending Agronomic Recommendations** (`HIGH`):
   - Direct CTA: `/recommendations?id={recId}`
   - Action label: *"Tavsiyani koʻrish"* / *"Review Recommendation"*
3. **Approved Irrigation Plans Ready for Execution** (`MEDIUM`):
   - Direct CTA: `/irrigation/plans?id={planId}`
   - Action label: *"Rejani koʻrish"* / *"View Plan"*
4. **Water Budget Exhaustion Warning** (`HIGH`):
   - Triggered when water consumption exceeds $90\%$ of allocated quota.
   - Direct CTA: `/water`
   - Action label: *"Limitni tekshirish"* / *"Check Limit"*
5. **Sensor Hardware Offline Warning** (`MEDIUM`):
   - Triggered when $\ge 1$ field has offline telemetry sensors.
   - Direct CTA: `/sensors`
   - Action label: *"Datchiklarni koʻrish"* / *"Check Sensors"*

---

## 6. REST API Specification

### Endpoint
```http
GET /api/v1/dashboard/overview
Authorization: Bearer <jwt_token>
```

### Response Payload Structure
```json
{
  "timestamp": "2026-11-01T13:45:00Z",
  "scope": "FARMER",
  "userName": "Rustam Karimov",
  "userRole": "FARMER",
  "fieldMetrics": {
    "totalFields": 24,
    "totalAreaHectares": 128.5,
    "activeFields": 24,
    "fieldsWithSensors": 20,
    "fieldsRequiringIrrigation": 3,
    "fieldsWithActiveIssues": 2
  },
  "waterMetrics": {
    "totalAllocatedM3": 25000.0,
    "totalConsumedM3": 16500.0,
    "totalRemainingM3": 5500.0,
    "totalApprovedM3": 2000.0,
    "totalCommittedM3": 1000.0,
    "consumptionPercentage": 66.0,
    "activeSourceCount": 4
  },
  "sensorMetrics": {
    "totalSensors": 42,
    "onlineSensors": 39,
    "offlineSensors": 3,
    "averageSoilMoisture": 28.4,
    "fleetHealthPercentage": 92.8
  },
  "anomalyMetrics": {
    "activeAnomalies": 2,
    "criticalAnomalies": 1,
    "highAnomalies": 1,
    "mediumAnomalies": 0,
    "pipeBurstSuspected": 1
  },
  "recommendationMetrics": {
    "pendingRecommendations": 4,
    "adoptedRecommendations": 12,
    "highConfidenceCount": 3
  },
  "irrigationMetrics": {
    "pendingApprovalCount": 2,
    "approvedCount": 3,
    "inProgressCount": 1,
    "completedTodayCount": 4
  },
  "waterBudget": {
    "season": "2026-SUMMER",
    "allocatedVolumeM3": 25000.0,
    "consumedVolumeM3": 16500.0,
    "approvedVolumeM3": 2000.0,
    "committedVolumeM3": 1000.0,
    "remainingAvailableVolumeM3": 5500.0,
    "utilizationRatePercent": 66.0,
    "budgetStatus": "NORMAL",
    "formulaExplanation": "Qolgan = Ajratilgan - Sarflangan - Tasdiqlangan - Band qilingan"
  },
  "priorityFields": [
    {
      "fieldId": "f7a1d120-1a1a-4c4c-b1b1-111111111111",
      "fieldName": "Dala 12 Paxta",
      "cropType": "COTTON",
      "areaHectares": 8.5,
      "priorityScore": 85,
      "priorityLevel": "CRITICAL",
      "latestSoilMoisture": 12.4,
      "hasActiveAnomaly": true,
      "activeAnomalySeverity": "CRITICAL",
      "hasPendingRecommendation": true,
      "hasScheduledPlan": false,
      "scoringReasons": [
        "Critical drought: Soil moisture 12.4% is below wilting point 15%",
        "Active critical anomaly: PIPE_BURST_SUSPECTED detected",
        "Sensitive crop stage: COTTON requires stable moisture"
      ],
      "recommendedAction": "Gidravlik klapan va namlikni darhol tekshiring"
    }
  ],
  "urgentActions": [
    {
      "id": "act-anom-1",
      "actionType": "ANOMALY_REVIEW",
      "severity": "CRITICAL",
      "title": "Kritik quvur yorilishi aniqlandi",
      "description": "Dala 12 datchigida bosim keskin tushishi qayd etildi",
      "fieldId": "f7a1d120-1a1a-4c4c-b1b1-111111111111",
      "fieldName": "Dala 12 Paxta",
      "targetUrl": "/anomalies?id=a1",
      "buttonLabel": "Avaryani tekshirish"
    }
  ],
  "recentActivity": [
    {
      "id": "act-1",
      "eventType": "ANOMALY_DETECTED",
      "severity": "CRITICAL",
      "title": "Quvur yorilishi shubhasi",
      "description": "Suv oqimi meʼyordan 2 barobar yuqori",
      "timestamp": "2026-11-01T13:30:00Z",
      "fieldId": "f7a1d120-1a1a-4c4c-b1b1-111111111111",
      "fieldName": "Dala 12 Paxta"
    }
  ]
}
```

---

## 7. Performance & Latency Budgets

- **Target Response Time**: $< 250\text{ ms}$ for $P_{95}$ single farm requests (20–100 parcels).
- **Database Query Count**: $\le 10$ round-trips total per overview execution.
- **Client Cache Window**: TanStack Query cache (`staleTime: 60s`, `refetchInterval: 120s`). Background refetch retains visible state without screen flickering.
