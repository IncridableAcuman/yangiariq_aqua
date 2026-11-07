# Canal Dispatch Optimization & Fairness Policies

## 1. Dispatch Optimization Engine

The canal dispatch optimizer (`CanalDispatchOptimizer`) solves multi-parcel contention through deterministic scheduling rules.

### Input Parameters:
- Planned field irrigation demands (volume $m^3$, required flow rate $m^3/s$).
- Soil moisture deficit and crop wilting risk.
- Remaining seasonal water budget allocations ($m^3$).
- Canal segment maximum carrying capacities ($m^3/s$).
- Downstream active hydraulic anomalies (pipe bursts, pressure drops).

---

## 2. Fairness Policies

Water distribution equity is protected through transparent policies:

### 2.1 `CROP_CRITICALITY_FIRST`
- Prioritizes parcels with moisture-sensitive crops (cotton flowering, wheat grain-fill) near critical depletion thresholds.
- Once critical needs are satisfied, remaining canal volume is partitioned proportionally.

### 2.2 `MOISTURE_DEFICIT_PRIORITY`
- Orders parcels strictly by telemetry-observed soil moisture depletion ($\theta_{current} - \theta_{threshold}$).
- Prevents favoritism towards larger landowners or upstream farms.

### 2.3 `PROPORTIONAL_DEFICIT`
- During extreme district-wide water shortages, deficit is shared uniformly: every parcel receives an equal fraction of its requested volume.

---

## 3. Human Approval Workflow

```
DRAFT / PROPOSED
       │
       ▼ [Human Review & Safety Verification]
APPROVED
       │
       ▼
SCHEDULED
       │
       ▼ [Gate Operation Confirmation]
IN_PROGRESS
       │
       ▼ [Turnout Closure Confirmation]
COMPLETED
```
