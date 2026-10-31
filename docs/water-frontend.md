# Water Management UI Architecture (`docs/water-frontend.md`)

## 1. Overview & Objectives

Milestone 06.4 delivers the production-ready **Water Management & Irrigation Operations UI** for the YANGIARIQ AQUA AI platform (`frontend/src/entities/water`, `frontend/src/features/water`, and `frontend/src/pages/irrigation/IrrigationPage.tsx`).

The interface directly interfaces with the backend REST APIs established in Milestones 06.2 (CRUD & Lifecycle) and 06.3 (Rules, Volumetric Accounting, Over-Consumption, and Planned vs Actual Variance), providing:
- Real-time volumetric water accounting ($V_{\text{allocated}}$, $V_{\text{consumed}}$, $V_{\text{remaining}}$, $V_{\text{overConsumed}}$).
- Clamped visual progress indicators (0–100%) with non-blocking over-consumption alert badges.
- Agricultural irrigation event logging (live tracking with start/complete, plus direct historical logging).
- Planned vs actual variance analysis ($\Delta V = V_{\text{actual}} - V_{\text{planned}}$ and execution ratio $R_{\text{consumption}}$).
- Periodic field water summaries with ISO date range filters.
- Regional water source catalog with role-based administration (Operator/Admin).
- 100% real server-state data management via TanStack Query (zero mock data in production).
- Complete multilingual support (`uz`, `ru`, `en`) and seamless dark/light mode responsiveness.

---

## 2. Layered Architecture (Feature-Sliced Design)

```text
frontend/src/
├── entities/water/
│   ├── types.ts                     # Strong domain contracts, enums, query params, and mutation payloads
│   ├── api.ts                       # Typed REST API client functions calling /api/v1 endpoints
│   └── index.ts                     # Public entity exports
│
├── features/water/
│   ├── schemas/waterSchemas.ts      # Zod validation schemas matching backend domain invariants
│   ├── queries/useWaterQueries.ts   # TanStack Query hooks with targeted cache invalidation
│   ├── lib/waterFormatters.ts       # Volumetric formatting (e.g. "12 500 m³"), percentages, clamped fill
│   ├── components/
│   │   ├── WaterProgressBar.tsx     # Clamped visual progress bar with semantic alert thresholds
│   │   ├── WaterStatusBadge.tsx     # Accessible status badges and OverConsumptionBadge
│   │   ├── WaterSummaryCards.tsx    # Top operational KPI cards (Allocated, Consumed, Balance, Over-Consumption)
│   │   ├── WaterAllocationRow.tsx   # Quota row with live consumption, progress bar, and action dropdown
│   │   ├── WaterAllocationCard.tsx  # Mobile responsive quota card
│   │   ├── WaterAllocationSection.tsx # Quota listing with field/status filters and create modal trigger
│   │   ├── IrrigationPlanRow.tsx    # Plan row with Planned vs Actual metric pill and lifecycle triggers
│   │   ├── IrrigationPlanCard.tsx   # Mobile responsive plan card
│   │   ├── IrrigationPlanSection.tsx# Plan listing with field/status filters and create modal trigger
│   │   ├── IrrigationEventRow.tsx   # Event row with delivery duration, measured volume, and action triggers
│   │   ├── IrrigationEventCard.tsx  # Mobile responsive event card
│   │   ├── IrrigationEventSection.tsx# Event listing with "Start Live" and "Record Completed" actions
│   │   ├── FieldWaterSummarySection.tsx # Periodic parcel water balance with ISO date pickers
│   │   ├── WaterSourceCatalog.tsx   # Regional water source cards with search, type/status filters, and RBAC actions
│   │   ├── RecordCompletedDialog.tsx# Modal for direct logging of completed watering delivery
│   │   ├── StartIrrigationDialog.tsx # Modal for initiating real-time watering operation
│   │   ├── CompleteIrrigationDialog.tsx # Modal for finalizing in-progress event with meter volume
│   │   ├── AllocationFormDialog.tsx # Create / edit water quota modal
│   │   ├── PlanFormDialog.tsx       # Create / edit irrigation schedule modal
│   │   ├── WaterSourceFormDialog.tsx# Create / edit regional water source modal (Operator/Admin)
│   │   ├── CancelOperationDialog.tsx# Confirmation modal for closing or cancelling quotas/plans/events
│   │   └── index.ts
│   └── index.ts
│
├── pages/irrigation/
│   ├── IrrigationPage.tsx           # Primary dashboard orchestrator integrating tabs, KPIs, and dialogs
│   └── index.ts
│
└── shared/i18n/index.ts             # Comprehensive i18n dictionaries (uz, ru, en) under 'water' namespace
```

---

## 3. UI Modules & Operational Capabilities

### 3.1 Operational KPI Summary Cards (`WaterSummaryCards.tsx`)
Four high-density metric cards provide immediate visibility into district and parcel water posture:
1. **Total Allocated Quota ($V_{\text{allocated}}$)**: Aggregate seasonal water entitlement in $m^3$.
2. **Actual Consumed Water ($V_{\text{consumed}}$)**: Total physical water delivered across completed irrigation events.
3. **Remaining Balance ($V_{\text{remaining}}$)**: Available water balance, mathematically clamped to non-negative ($\max(V_{\text{allocated}} - V_{\text{consumed}}, 0)$).
4. **Over-Consumption ($V_{\text{overConsumed}}$)**: Prominent metric card displaying excess water delivered beyond quota. When $V_{\text{consumed}} > V_{\text{allocated}}$, a high-priority alert banner is automatically mounted at the top of the dashboard.

### 3.2 Water Quota Management (`WaterAllocationSection.tsx`)
- Displays all active, closed, and cancelled parcel quotas.
- Displays parcel name, water source name/type, allocation period, and volumetric breakdown.
- **Volumetric Utilization Bar**: Clamps visual fill to $100\%$ while displaying true percentage label (e.g. $125\%$).
- **Over-Quota Badge**: When over-consumed, renders `Over Quota (+X m³)` in destructive red.
- **Actions**:
  - `Edit`: Update allocated volume or period while quota is `ACTIVE`.
  - `Close`: Conclude seasonal quota upon crop harvest (`ACTIVE -> CLOSED`).
  - `Cancel`: Administrative revocation (`ACTIVE -> CANCELLED`).

### 3.3 Irrigation Scheduling & Variance (`IrrigationPlanSection.tsx`)
- Displays upcoming and historical irrigation plans with parcel, method, schedule, and planned volume ($V_{\text{planned}}$).
- **Planned vs. Actual Visualizer**: Displays volumetric deviation $\Delta V = V_{\text{actual}} - V_{\text{planned}}$ and operational execution ratio $R_{\text{consumption}} = \frac{V_{\text{actual}}}{V_{\text{planned}}} \times 100$.
- **Lifecycle Transition Triggers**:
  - `Start`: Advances plan state from `PLANNED -> IN_PROGRESS`.
  - `Complete`: Advances plan state from `IN_PROGRESS -> COMPLETED`.
  - `Cancel`: Aborts plan (`PLANNED/IN_PROGRESS -> CANCELLED`).

### 3.4 Operational Delivery Logs (`IrrigationEventSection.tsx`)
- Real-time and historical record of water delivery events on parcels.
- **Direct Record ("Record Completed Irrigation")**: Enables historical, offline, or batch logging of finished watering with measured $m^3$.
- **Real-Time Start ("Start Live Irrigation")**: Logs opening of sluice gates or pump activation (`status = STARTED`).
- **Complete In-Progress ("Complete Irrigation")**: Finalizes delivery with end timestamp and physical flow meter reading.

### 3.5 Field Periodic Water Summary (`FieldWaterSummarySection.tsx`)
- Dedicated analytical widget allowing farmers and operators to select any parcel and specify custom `from` and `to` dates.
- Integrates with `GET /api/v1/water/fields/{fieldId}/summary?from=...&to=...`.
- Displays parcel-level quotas count, completed events count, balance breakdown, and warning banner if the parcel has exceeded its seasonal quota.

### 3.6 Regional Water Sources Catalog (`WaterSourceCatalog.tsx`)
- Filterable catalog of regional canals, ditches, wells, and reservoirs.
- Search by name, filter by hydraulic type and operational status (`ACTIVE`, `INACTIVE`).
- **RBAC Enforcement**:
  - `FARMER`: Read-only access to browse available water sources for quota requests.
  - `OPERATOR` & `ADMIN`: Access to "Register Water Source", "Edit", "Activate", and "Deactivate" actions.

---

## 4. State Management & Cache Synchronization

The frontend utilizes **TanStack Query (v5)** with a centralized query key factory:

```ts
export const waterKeys = {
  all: ['water'] as const,
  sources: () => [...waterKeys.all, 'sources'] as const,
  sourceList: (params) => [...waterKeys.sources(), 'list', params] as const,
  sourceDetail: (id) => [...waterKeys.sources(), 'detail', id] as const,

  allocations: () => [...waterKeys.all, 'allocations'] as const,
  allocationList: (params) => [...waterKeys.allocations(), 'list', params] as const,
  allocationConsumption: (id) => [...waterKeys.allocations(), 'consumption', id] as const,

  plans: () => [...waterKeys.all, 'plans'] as const,
  planList: (params) => [...waterKeys.plans(), 'list', params] as const,
  planMetrics: (id) => [...waterKeys.plans(), 'metrics', id] as const,

  events: () => [...waterKeys.all, 'events'] as const,
  eventList: (params) => [...waterKeys.events(), 'list', params] as const,

  fieldSummaries: () => [...waterKeys.all, 'field-summaries'] as const,
  fieldSummary: (fieldId, params) => [...waterKeys.fieldSummaries(), fieldId, params] as const,
}
```

### Targeted Cache Invalidation Matrix
| Mutation Triggered | Invalidated Query Keys |
| :--- | :--- |
| `useCreateWaterSourceMutation` | `waterKeys.sources()` |
| `useUpdateWaterSourceMutation` | `waterKeys.sources()`, `waterKeys.sourceDetail(id)` |
| `useActivateWaterSourceMutation` | `waterKeys.sources()`, `waterKeys.sourceDetail(id)` |
| `useDeactivateWaterSourceMutation` | `waterKeys.sources()`, `waterKeys.sourceDetail(id)` |
| `useCreateWaterAllocationMutation` | `waterKeys.allocations()`, `waterKeys.fieldSummaries()` |
| `useUpdateWaterAllocationMutation` | `waterKeys.allocations()`, `waterKeys.allocationConsumption(id)`, `waterKeys.fieldSummaries()` |
| `useCloseWaterAllocationMutation` | `waterKeys.allocations()`, `waterKeys.allocationConsumption(id)`, `waterKeys.fieldSummaries()` |
| `useCancelWaterAllocationMutation` | `waterKeys.allocations()`, `waterKeys.allocationConsumption(id)`, `waterKeys.fieldSummaries()` |
| `useCreateIrrigationPlanMutation` | `waterKeys.plans()`, `waterKeys.fieldSummaries()` |
| `useUpdateIrrigationPlanMutation` | `waterKeys.plans()`, `waterKeys.planDetail(id)`, `waterKeys.planMetrics(id)` |
| `useStartIrrigationPlanMutation` | `waterKeys.plans()`, `waterKeys.planMetrics(id)` |
| `useCompleteIrrigationPlanMutation`| `waterKeys.plans()`, `waterKeys.planMetrics(id)` |
| `useCancelIrrigationPlanMutation` | `waterKeys.plans()`, `waterKeys.planMetrics(id)` |
| `useRecordCompletedIrrigationEventMutation` | `waterKeys.events()`, `waterKeys.plans()`, `waterKeys.allocations()`, `waterKeys.fieldSummaries()` |
| `useCompleteIrrigationEventMutation` | `waterKeys.events()`, `waterKeys.plans()`, `waterKeys.allocations()`, `waterKeys.fieldSummaries()` |
| `useCancelIrrigationEventMutation` | `waterKeys.events()`, `waterKeys.plans()` |

---

## 5. Form Validation & Invariants

Forms are powered by **React Hook Form** and validated via **Zod**:
1. **Volumetric Constraints**:
   - `allocatedVolumeM3`: Positive number, max $10\,000\,000\,\text{m}^3$.
   - `plannedVolumeM3`: Positive number, max $1\,000\,000\,\text{m}^3$.
   - `actualVolumeM3`: Non-negative number ($\ge 0$), max $1\,000\,000\,\text{m}^3$.
2. **Chronological Invariants**:
   - `waterAllocationSchema`: Enforces `periodStart <= periodEnd`.
   - `irrigationPlanSchema`: Enforces `plannedStartAt <= plannedEndAt`.
   - `recordCompletedEventSchema`: Enforces `startAt <= endAt`.
3. **Non-Blocking Execution**:
   - Logging an irrigation event that results in over-consumption is never rejected on the client or server. Over-consumption metrics and alerts update reactively.

---

## 6. Verification & Test Coverage

The frontend implementation is verified across 10 test suites containing 72 automated Vitest tests:
- `src/test/waterApi.test.ts`: 12 tests validating URL construction, query parameters, payload serialization, and response handling for sources, quotas, plans, events, and consumption endpoints.
- `src/test/waterSchemas.test.ts`: 14 tests validating Zod form schemas, boundary constraints, and chronological error maps.
- `src/test/waterComponents.test.tsx`: 8 tests validating progress bar clamping, alert triggers, status badges, and volumetric string formatting.
- `src/test/waterRoleUx.test.tsx`: 2 tests verifying RBAC separation between Farmer and Operator/Admin.
- Additional Field, GIS, and Auth suites: 36 tests ensuring zero regression across the existing platform.
- Full compilation check: `tsc -b` and `vite build` complete with zero errors.
