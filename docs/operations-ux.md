# Yangiariq Aqua AI — Decision-to-Action Operations Center UX Specification

**Document Version:** 1.0.0  
**Milestone:** 08.5  
**Scope:** Operations Center Workspace, Operational Workflow Timeline, Contextual Action Execution, Trilingual UI.

---

## 1. User Problem & Design Philosophy

### The Multi-Screen Navigation Problem
Prior to Milestone 08.5, an operational user navigating the platform experienced high cognitive overhead:
```text
Dashboard (identify problem)
   ↓
Fields (locate specific parcel)
   ↓
AI Recommendations (read root-zone advice)
   ↓
Water Quota (verify remaining m³)
   ↓
Smart Irrigation Plan (prepare or review)
   ↓
Approvals (confirm)
   ↓
Schedule (check canal dispatch window)
   ↓
Irrigation Events (log physical watering)
   ↓
Sensors (inspect post-watering soil moisture)
```
This 9-step journey forced the user to manually correlate data across multiple browser tabs and pages, creating delays and risking miscommunication during critical drought or hydraulic events.

### The Unified Operations Center Solution
Milestone 08.5 introduces the **Continuous Decision-to-Action Workspace** (`/operations`):
```text
Action Center / Priority Fields (1-click trigger)
                      ↓
   DECISION-TO-ACTION OPERATIONS CENTER
  ┌───────────────────────────────────────────────────────────┐
  │ Field Header & Hydraulic Safety Interlock Banner          │
  ├───────────────────────────────────────────────────────────┤
  │ 7-Step Operational Workflow Timeline                      │
  ├───────────────────────────────────────────────────────────┤
  │ Current Telemetry & Freshness | Water Quota Remaining     │
  ├───────────────────────────────────────────────────────────┤
  │ AI Agronomic Recommendation & Root-Zone Evidence          │
  ├───────────────────────────────────────────────────────────┤
  │ Precedence Constraints Checklist (Safety, Quota, Window)  │
  ├───────────────────────────────────────────────────────────┤
  │ Smart Plan Review & Volume Calibration                    │
  ├───────────────────────────────────────────────────────────┤
  │ Human Approval Modal (Mandatory Acknowledgement Check)    │
  ├───────────────────────────────────────────────────────────┤
  │ Manual Field Watering Execution (Start & Complete Record) │
  ├───────────────────────────────────────────────────────────┤
  │ Post-Irrigation Verification & Closed-Loop Learning       │
  └───────────────────────────────────────────────────────────┘
```
**Goal:** The user understands the field status, reviews constraints, and performs the correct operational decision within **10 seconds** of opening the workspace.

---

## 2. Workspace Layout & Component Hierarchy

### 2.1 Parcel Context Header (`OperationalContextHeader`)
- **Field Identification:** Parcel name, crop type, acreage in hectares, centroid coordinates.
- **Safety Interlock Status:**
  - `SAFE`: Emerald badge indicating clear hydraulic channels.
  - `BLOCKED`: Pulsing rose alert banner indicating active pipe burst or canal maintenance with the exact blocking root-cause.
- **Parcel Switcher:** Dropdown allowing swift switching between all accessible tenant parcels without leaving the workspace.

### 2.2 Operational Workflow Timeline (`OperationalWorkflowTimeline`)
Chronological 7-step operational timeline across the top of the workspace:
1. `Anomaliya Aniqlanishi` (Detection)
2. `Agronomik Tavsiya` (AI Recommendation)
3. `Aqlli Sugʻorish Rejasi` (Plan Proposed)
4. `Inson Tasdigʻi` (Human Approval)
5. `Gidravlik Rejalashtirish` (Scheduling)
6. `Dala Bajarilishi` (Execution Recording)
7. `Natija Tekshiruvi` (Verification & Learning)

**States & Styling:**
- `COMPLETED`: Solid emerald circle with checkmark, timestamp, and actor details.
- `CURRENT`: Primary glowing pulse ring indicating the actionable current stage.
- `BLOCKED`: Rose octagon alert with blocking constraint explanation.
- `UPCOMING`: Muted circle representing future stages.

### 2.3 Current Situation Panel (`CurrentSituationPanel`)
High-contrast metric cards highlighting operational telemetry:
- **Root-Zone Soil Moisture:** e.g., `14.2%` with dynamic color-coding (`<15%` red, `15-25%` amber, `25-65%` green).
- **12-Hour Trend Indicator:** Directional icon and delta (`-2.4%/12h`).
- **Ambient Temperature:** e.g., `28.5°C` with daytime heat warning.
- **Telemetry Freshness:** `FRESH` (<30 min), `STALE` (>3 hr), or `MISSING` badge.
- **Volumetric Quota Remaining:** e.g., `1,580 m³` remaining of `3,000 m³` seasonal allocation.

### 2.4 AI Agronomic Recommendation Card (`RecommendationSummaryCard`)
- Recommendation type (`IRRIGATE`, `DRAIN`, `MAINTENANCE`) and priority badge (`CRITICAL`, `HIGH`, `MEDIUM`).
- Clear agronomic justification in plain farmer-friendly terminology.
- AI Explanation detailing root depth, transpiration rate, and hydration urgency.
- Supporting evidence list (soil moisture reading, reference ET0, vegetation index).
- Contextual action: **"Create Plan on this Recommendation"** (`Ushbu tavsiya asosida reja tuzish`).

### 2.5 Precedence Constraints Checklist (`ConstraintChecklistCard`)
Clear operational rules verified in order:
- `Gidravlik xavfsizlik interlogi`: Checks for active pipe burst or pressure drop anomalies.
- `Mavsumiy suv kvotasi`: Verifies planned volume $\le$ remaining allocation.
- `Tungi dispetcherizatsiya optimalligi`: Validates preferred nocturnal watering window (22:00–06:00) to minimize evaporation.

### 2.6 Active Plan Review Card (`PlanReviewCard`)
- Displays recommended volume in $m^3$, planned duration in minutes, and target delivery date.
- Shows planned delivery window (e.g. `06:00:00 - 10:00:00`).
- Provides review and modification actions: **Approve**, **Reject**, or **Schedule**.

### 2.7 Non-Actuator Execution Panel (`OperationExecutionPanel`)
- **Clear Non-Actuator Disclaimer:**
  > *"Ushbu tizim qaror qabul qilishni qoʻllab-quvvatlash va operatsiyalarni qayd etish uchun moʻljallangan. Klapan yoki nasoslarni avtomatik ochmaydi. Barcha amaliyotlar dalada operator tomonidan bajariladi va qayd etiladi."*
- **Poliv Boshlanishini Qayd Etish (Record Start):** Logs manual valve opening in `IrrigationEvent` aggregate.
- **Poliv Tugallanishini Qayd Etish (Record Complete):** Opens modal to input actual delivered volume ($m^3$) and operator field notes.

### 2.8 Post-Irrigation Verification Card (`PostIrrigationVerificationCard`)
- **Volume Variance:** Planned ($420.0\,m^3$) vs. Actual ($415.0\,m^3$) with percentage variance ($-1.2\%$).
- **Duration Variance:** Planned ($120\,\text{min}$) vs. Actual ($118\,\text{min}$).
- **Soil Moisture Delta:** Pre-irrigation ($14.2\%$) to Post-irrigation ($27.8\%$), showing net change ($+13.6\%$).
- **Recharge Status Badge:** Emerald `Muvaffaqiyatli Namlanish` badge.
- **Continuous Learning Loop Evidence:** Displays infiltration accuracy calibration score (e.g., `Model accuracy 98.8%. Infiltration calibrated for loam parcel`).

---

## 3. Modal Dialogs & Safety Guardrails

### 3.1 Human Plan Approval Dialog (`PlanApprovalDialog`)
- **Mandatory Constraint Acknowledgment Checkbox:**
  The approval button is disabled by default. The operator must explicitly check:
  > *"Men barcha operatsion cheklovlar, gidravlik xavfsizlik va mavsumiy suv limitini koʻrib chiqdim hamda ushbu sugʻorish amaliyotini tasdiqlayman."*
- Summarizes planned volume, duration, remaining water quota, and AI confidence.
- Provides optional field for operator approval notes.

### 3.2 Plan Rejection Dialog (`PlanRejectDialog`)
- Requires a mandatory rejection reason of at least 3 characters.
- Explains that the reason is written to the immutable operational audit log.
- Cancels the smart plan and dismisses active agronomic recommendations with the specified justification.

---

## 4. Internationalization & Accessibility

- **Trilingual Parity:** Full UI support in Uzbek (`uz`), Russian (`ru`), and English (`en`).
- **Dark Mode & High Contrast:** Uses semantic Tailwind tokens (`bg-card`, `border-border`, `text-foreground`, `bg-primary`, `text-muted-foreground`) compatible with light, dark, and outdoor high-glare environments.
- **Responsive Layout:**
  - Desktop: Multi-column side-by-side situational and planning workspace.
  - Tablet & Mobile: Stacked single-column card layout with sticky action triggers.
