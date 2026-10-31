# Water Rules, Consumption & Efficiency Blueprint (`docs/water-consumption.md`)

## 1. Domain Purpose & Overview

Milestone 06.3 establishes the core business rules, volumetric consumption accounting, and efficiency metrics for the **Water Management & Irrigation (`com.aquaai.water`)** domain.

While Milestones 06.1 and 06.2 provided the aggregate storage and CRUD operations for `WaterAllocation`, `IrrigationPlan`, and `IrrigationEvent`, Milestone 06.3 answers the foundational operational questions for Yangiariq district:

```text
1. How much water quota is allocated to this parcel?
2. How much water has actually been delivered and consumed?
3. What is the remaining available balance?
4. What percentage of the allocated quota has been consumed?
5. Has the parcel exceeded its water quota (over-consumption)? By how much?
6. What is the variance between scheduled intention (planned) and execution (actual)?
7. How does executed watering roll up into field-level seasonal consumption?
```

---

## 2. Water Consumption Accounting Model

### Aggregate Derivation Chain
An `IrrigationEvent` captures physical water delivery and consumption (`actualVolumeM3`). It links to an `IrrigationPlan` (`irrigationPlanId`), which in turn links to a `WaterAllocation` (`waterAllocationId`):

```text
IrrigationEvent (COMPLETED, actualVolumeM3)
       │
       ▼ (fk_irrigation_events_plan)
IrrigationPlan
       │
       ▼ (fk_irrigation_plans_allocation)
WaterAllocation (allocatedVolumeM3)
```

### Event Status Inclusion Rules
Only physical deliveries that have concluded successfully count toward water consumption:

| Event Status | Counted in Consumption? | Business Rationale |
| :--- | :---: | :--- |
| `COMPLETED` | **YES** | Delivery has ended, flow meter or mechanical gauge reading is confirmed (`actualVolumeM3 >= 0`). |
| `STARTED` | **NO** | Irrigation operation is currently in-progress; water is still flowing. Final consumption is unknown. |
| `CANCELLED` | **NO** | Operation was aborted (e.g. pump failure, pipeline rupture). Any water lost or not delivered is excluded. |

---

## 3. Allocation Consumption & Over-Consumption Invariants

For each `WaterAllocation`:

### Formulations
1. **Allocated Volume**:
   $$V_{\text{allocated}} = \text{allocation.allocatedVolumeM3}$$
2. **Consumed Volume**:
   $$V_{\text{consumed}} = \sum_{e \in \text{CompletedEvents}} e.\text{actualVolumeM3}$$
3. **Remaining Volume (Clamped to Zero)**:
   $$V_{\text{remaining}} = \max(V_{\text{allocated}} - V_{\text{consumed}}, 0)$$
   > [!IMPORTANT]
   > **Invariant**: $V_{\text{remaining}}$ MUST NEVER be negative under any over-consumption magnitude.
4. **Over-Consumption Detection**:
   $$\text{isOverConsumed} = (V_{\text{consumed}} > V_{\text{allocated}})$$
5. **Over-Consumed Volume**:
   $$V_{\text{overConsumed}} = \max(V_{\text{consumed}} - V_{\text{allocated}}, 0)$$
6. **Quota Utilization Percentage**:
   $$P_{\text{consumption}} = \begin{cases} \frac{V_{\text{consumed}}}{V_{\text{allocated}}} \times 100 & \text{if } V_{\text{allocated}} > 0 \\ 0.00 & \text{otherwise} \end{cases}$$

### Over-Consumption Non-Blocking Policy
In real-world agricultural operations (especially in Yangiariq district with high-evapotranspiration crops like cotton and rice), water master operators must be able to log real ground truth without the software arbitrarily rejecting completed waterings.
- **Default Policy**: The system **does not block** irrigation events when a quota is exceeded.
- Instead, the system authoritatively tracks:
  - $V_{\text{remaining}} = 0.0000\,\text{m}^3$
  - $\text{isOverConsumed} = \text{true}$
  - $V_{\text{overConsumed}} = V_{\text{consumed}} - V_{\text{allocated}}$
  - $P_{\text{consumption}} > 100.00\%$
  - Operators and farmers receive immediate transparent indicators without losing telemetry integrity.

---

## 4. Planned vs. Actual Metrics & Efficiency Ratios

For each `IrrigationPlan`:

### Formulations
1. **Planned Volume**:
   $$V_{\text{planned}} = \text{plan.plannedVolumeM3}$$
2. **Actual Volume**:
   $$V_{\text{actual}} = \sum_{e \in \text{CompletedPlanEvents}} e.\text{actualVolumeM3}$$
3. **Volumetric Variance (Signed)**:
   $$\Delta V = V_{\text{actual}} - V_{\text{planned}}$$
   - $\Delta V < 0$: Water saved / under-utilized relative to plan.
   - $\Delta V = 0$: Exact target match.
   - $\Delta V > 0$: Excess water delivered beyond plan.
4. **Absolute Variance**:
   $$|\Delta V| = |V_{\text{actual}} - V_{\text{planned}}|$$
5. **Variance Percentage**:
   $$P_{\text{variance}} = \frac{V_{\text{actual}} - V_{\text{planned}}}{V_{\text{planned}}} \times 100$$
6. **Operational Consumption Ratio**:
   $$R_{\text{consumption}} = \frac{V_{\text{actual}}}{V_{\text{planned}}} \times 100$$

> [!NOTE]
> $R_{\text{consumption}}$ represents the **operational execution ratio** against the plan. It reflects execution adherence, not full agronomical crop water efficiency (which requires evapotranspiration modeling in later AI milestones).

---

## 5. Field Water Summary & Temporal Filtering

Field-level summary provides parcel periodic water balances across all associated quotas and executions:

### Parameters
- `fieldId` (`UUID`, required)
- `from` (`Instant`, optional, UTC)
- `to` (`Instant`, optional, UTC)

### Invariants & Date Filtering
- **Temporal Validation**: If both `from` and `to` are present, `from <= to` is enforced. Supplying `from > to` is rejected with `422 Unprocessable Entity` (`INVALID_DATE_RANGE`).
- **Quota Inclusion**:
  $$\text{status} \neq \text{'CANCELLED'} \quad \text{AND} \quad (\text{from is null} \lor \text{periodEnd} \ge \text{from}) \quad \text{AND} \quad (\text{to is null} \lor \text{periodStart} \le \text{to})$$
- **Event Inclusion**:
  $$\text{status} = \text{'COMPLETED'} \quad \text{AND} \quad (\text{from is null} \lor \text{startedAt} \ge \text{from}) \quad \text{AND} \quad (\text{to is null} \lor \text{startedAt} \le \text{to})$$

---

## 6. REST API Reference

### 6.1 Field Water Summary
`GET /api/v1/water/fields/{fieldId}/summary`

**Query Parameters:**
- `from` (ISO-8601 UTC timestamp, optional)
- `to` (ISO-8601 UTC timestamp, optional)

**Response (HTTP 200 OK):**
```json
{
  "success": true,
  "data": {
    "fieldId": "3e6077f2-a0a8-4f37-9ec8-8cb018488a51",
    "period": {
      "from": "2026-05-01T00:00:00Z",
      "to": "2026-05-31T23:59:59Z"
    },
    "totalAllocatedVolumeM3": 3500.0000,
    "totalConsumedVolumeM3": 1050.0000,
    "totalRemainingVolumeM3": 2450.0000,
    "totalOverConsumedVolumeM3": 0.0000,
    "isOverConsumed": false,
    "consumptionPercentage": 30.00,
    "allocationCount": 2,
    "irrigationEventCount": 3
  },
  "timestamp": "2026-05-31T12:00:00.000Z"
}
```

---

### 6.2 Allocation Consumption Summary
- `GET /api/v1/water/allocations/{id}/summary`
- `GET /api/v1/water-allocations/{id}/consumption`

**Response (HTTP 200 OK):**
```json
{
  "success": true,
  "data": {
    "allocationId": "e4b2d3c1-5f6a-4c7b-8910-111213141516",
    "fieldId": "3e6077f2-a0a8-4f37-9ec8-8cb018488a51",
    "waterSourceId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "allocatedVolumeM3": 1000.0000,
    "consumedVolumeM3": 350.0000,
    "remainingVolumeM3": 650.0000,
    "consumptionPercentage": 35.00,
    "isOverConsumed": false,
    "overConsumedVolumeM3": 0.0000,
    "completedEventsCount": 1,
    "status": "ACTIVE",
    "periodStart": "2026-04-01T00:00:00Z",
    "periodEnd": "2026-09-30T23:59:59Z"
  },
  "timestamp": "2026-05-15T10:00:00.000Z"
}
```

---

### 6.3 Planned vs Actual Metrics
- `GET /api/v1/water/plans/{id}/metrics`
- `GET /api/v1/irrigation-plans/{id}/metrics`

**Response (HTTP 200 OK):**
```json
{
  "success": true,
  "data": {
    "planId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "fieldId": "3e6077f2-a0a8-4f37-9ec8-8cb018488a51",
    "waterAllocationId": "e4b2d3c1-5f6a-4c7b-8910-111213141516",
    "plannedVolumeM3": 400.0000,
    "actualVolumeM3": 380.0000,
    "varianceVolumeM3": -20.0000,
    "absoluteVarianceVolumeM3": 20.0000,
    "variancePercentage": -5.00,
    "consumptionRatioPercentage": 95.00,
    "completedEventsCount": 1,
    "status": "COMPLETED"
  },
  "timestamp": "2026-05-10T12:00:00.000Z"
}
```

---

## 7. Security & IDOR Anti-Enumeration

All consumption and metrics endpoints enforce parcel ownership via [`FieldOwnershipChecker`](file:///home/izzatbek/Desktop/agronexsus/backend/src/main/java/com/aquaai/field/application/FieldOwnershipChecker.java):

```text
Farmer B (Attacker)
       │
       ▼
GET /api/v1/water/fields/{Field_A_ID}/summary
       │
       ▼
FieldOwnershipChecker.getFieldForRead(fieldAId, farmerBId, FARMER)
       │
       ▼
Throws ResourceNotFoundException("Field", fieldAId)
       │
       ▼
GlobalExceptionHandler returns 404 NOT FOUND (RESOURCE_NOT_FOUND)
```

- **Anti-Enumeration Guarantee**: An attacker receives `404 Not Found`, giving zero indication whether the foreign ID exists.
- **Operator & Admin Scope**: District operators (`OPERATOR`) and system administrators (`ADMIN`) can query metrics for any parcel across Yangiariq district.

---

## 8. Query Optimization & Zero N+1 Architecture

To satisfy strict enterprise performance standards:
1. **Derive on Read**: Consumption values are computed via SQL aggregations on demand; no redundant counter columns that risk database desynchronization.
2. **Zero N+1 Query Aggregation**:
   - Single allocation: Evaluated via single indexed JPQL `SUM` and `COUNT`.
   - Batch allocations / plans: Evaluated via `GROUP BY p.waterAllocationId` or `GROUP BY e.irrigationPlanId` in a single database query.
3. **Targeted Composite Indexes (Changeset 014)**:
   - `idx_irrigation_events_plan_status`: `(irrigation_plan_id, status)`
   - `idx_irrigation_events_field_status`: `(field_id, status)`
   - `idx_irrigation_events_field_started`: `(field_id, started_at)`
   - `idx_water_allocations_field_status`: `(field_id, status)`

---

## 9. Future Telemetry & Sensor Integration

While Milestone 06.3 implements deterministic business calculation rules without sensor hardware, the architecture seamlessly supports future IoT flow meters:

```text
Physical Ultrasonic / Flow Sensor
       │
       ▼ (LoRaWAN / MQTT Gateway)
Sensor Telemetry Ingestion (Future)
       │
       ▼
IrrigationEvent.actualVolumeM3
       │
       ▼
WaterConsumptionQueryRepository & WaterConsumptionCalculator
       │
       ▼
Real-time Consumption, Remaining Balance & Over-consumption Alerting
```

No sensor module dependency exists in the water domain, keeping the modular monolith clean and decoupled.
