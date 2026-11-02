# Frontend Architecture & Design Guidelines

## 1. Architectural Style: Feature-Sliced / Feature-Oriented

To avoid component bloat and tight coupling, `frontend/src` is divided into layered slices:

```
src/
├── app/          # App initialization, Providers, CSS variable themes
├── pages/        # Route views (Dashboard, Fields, Irrigation, Incidents, Settings)
├── widgets/      # Self-contained composite components (AppHeader, SidebarNav, MetricGrid)
├── features/     # User workflows (auth-form, irrigation-log, incident-report, ai-advisor)
├── entities/     # Domain data structures & presentational cards (FieldCard, SensorBadge)
└── shared/       # Cross-cutting primitives (shadcn UI, API client, i18n, formatting utilities)
```

## 2. Server State Management

- **TanStack Query (React Query v5)** handles all server data synchronization, caching, and background invalidation.
- No mirroring of server responses into global client stores (like Redux or Zustand).
- `QueryClient` defaults to 5-minute stale-time and disabled automatic window-refocus fetching to optimize mobile network usage.

## 3. UI & Design System

- **Design Language**: Dedicated water-management and agricultural palette (`hydro-*` deep blues, `agri-*` irrigation greens, crisp card borders).
- **Prohibited Aesthetics**: No generic AI rainbow gradients, excessive glassmorphism, or non-functional animations.
- **Responsiveness**:
  - **Operator Interface**: Desktop-first data density (multi-column tables, telemetry chart grids, geospatial canal overlays).
  - **Farmer Interface**: Mobile-first ergonomics (large tap targets, quick logging buttons, offline-resilient alerts).
- **Dark Mode**: Fully supported via CSS variables and `.dark` class toggles.

## 4. Internationalization (i18n)

The application supports three languages:
- **`uz`** (Uzbek) - Primary regional language
- **`ru`** (Russian) - Administrative & technical language
- **`en`** (English) - Engineering & international language

User-facing strings are strictly referenced from the typed dictionary in `src/shared/i18n`. Hardcoded strings in TSX elements are prohibited.

---

## 5. Authentication & User Management Integration

The frontend authentication subsystem integrates seamlessly with the backend `com.aquaai.auth` and `com.aquaai.user` modules:

- **State & Context (`src/features/auth/context/AuthContext.tsx`)**:
  - Provides reactive user authentication state (`user`, `isAuthenticated`, `isLoading`).
  - Exposes actions: `login`, `register`, `logout`, `updateProfile`, `changePassword`, `requestReset`, `confirmReset`, `refreshUser`.
- **Token Management & Refresh Token Rotation (`src/shared/api/client.ts` & `src/features/auth/storage.ts`)**:
  - Automatically attaches `Authorization: Bearer <accessToken>` headers.
  - Automatically intercepts HTTP 401 responses, initiates a single queued call to `POST /api/v1/auth/refresh`, rotates stored tokens, and retries the original failed request.
  - Gracefully invalidates sessions and broadcasts `aqua:unauthorized` when sessions expire or token reuse is detected.
- **Role & Profile Presentation (`src/entities/user`)**:
  - Role-specific theming for `FARMER` (agri green), `OPERATOR` (hydro blue), and `ADMIN` (purple).
  - Status badges for `ACTIVE`, `INACTIVE`, and `BLOCKED` accounts.
  - Personal profile drawer allowing real-time profile editing (`PATCH /api/v1/user/profile`) and credential rotation.

---

## 6. Field Management Subsystem (`src/features/field` & `src/entities/field`)

Developed in Milestone 05.4, the Field Management UI provides complete CRUD, lifecycle management, and monitoring for agricultural parcels:
- **Architecture & Technical Blueprint**: [`docs/field-frontend.md`](file:///home/izzatbek/Desktop/agronexsus/docs/field-frontend.md)
- **Entities (`src/entities/field`)**: Strict TypeScript contracts matching backend REST API, `FieldStatusBadge`, and typed API service using `apiClient`.
- **Features (`src/features/field`)**:
  - `fieldFormSchema`: Zod schema validating parcel invariants (Uzbekistan coordinates, positive hectares, enum types).
  - `useFieldQueries`: TanStack Query hooks with targeted cache invalidation.
  - Role-aware components: `FieldToolbar`, `FieldFilters`, `FieldTable` (desktop), `FieldCard` (mobile), `FieldPagination`, `FieldFormDialog`, `FieldDetailsModal`, `FieldDeactivateDialog`, `FieldReactivateDialog`, `FieldEmptyState`.
- **Page (`src/pages/fields/FieldsPage.tsx`)**: Responsive orchestrator handling filtering, local search, toasts, and modal workflows.
- **Security Invariants**: `ownerId` is never sent in request bodies (server-derived); mutation buttons are hidden for `OPERATOR` role; soft deactivation explains data retention.

---

## 7. GIS & Map Field Visualization Subsystem (`src/features/field/gis`)

Developed in Milestone 05.5, the GIS subsystem provides interactive mapping, centroid coordinate picking, and GeoJSON boundary polygon editing:
- **Technical Blueprint**: [`docs/field-gis.md`](file:///home/izzatbek/Desktop/agronexsus/docs/field-gis.md)
- **Library**: Native Leaflet with OpenStreetMap tiles. No external token or API key requirements.
- **Components**:
  - `FieldMap`: Interactive map viewer embedded inside `FieldDetailsModal`.
  - `FieldLocationPickerModal`: Map click & drag-to-set centroid coordinates.
  - `FieldBoundaryEditorModal`: Interactive polygon perimeter drawing and vertex handle dragging.
- **RFC 7946 GeoJSON Standard**: Strict `[longitude, latitude]` order enforcement and automatic ring closure.
- **Validation**: Strict boundary checks against Uzbekistan bounds (`37.0 - 45.6°N`, `56.0 - 73.2°E`).

---

## 8. Farm Intelligence Workspace & Action Center (`src/features/dashboard` & `src/entities/dashboard`)

Developed in Milestone 08.4, the Farm Intelligence Workspace unifies all agricultural subsystems into a high-density, action-first operational center:
- **Technical Blueprint**: [`docs/dashboard-architecture.md`](file:///home/izzatbek/Desktop/agronexsus/docs/dashboard-architecture.md) & [`docs/ux-guidelines.md`](file:///home/izzatbek/Desktop/agronexsus/docs/ux-guidelines.md)
- **Entities (`src/entities/dashboard`)**:
  - `types.ts`: Comprehensive TypeScript interfaces for `DashboardOverviewResponse`, `FieldMetrics`, `WaterMetrics`, `SensorMetrics`, `AnomalyMetrics`, `RecommendationMetrics`, `IrrigationMetrics`, `WaterBudgetSummary`, `PriorityFieldItem`, `UrgentActionItem`, and `RecentActivityItem`.
  - `api.ts`: Typed read-model API client `fetchDashboardOverview(signal)`.
- **Features (`src/features/dashboard`)**:
  - `useDashboardQueries.ts`: TanStack Query integration with `staleTime: 60s` and automatic 2-minute refetch interval.
  - `PriorityBadge.tsx`: Visual token mapping `CRITICAL`, `HIGH`, `MEDIUM`, and `LOW` priority levels to semantic color tokens.
  - `FarmOverviewMetrics.tsx`: Five high-contrast operational metric cards (Parcels, Water Quota, Sensor Fleet, Active Issues, Irrigation Pipeline).
  - `ActionCenter.tsx`: Urgent action banner highlighting critical anomalies, pending recommendations, and irrigation execution readiness with one-click navigation CTAs.
  - `PriorityFieldsTable.tsx`: Ranked agricultural parcel list with search, priority filter, crop filter, explainable priority rationale popup modal, and responsive mobile cards.
  - `WaterBudgetOverviewCard.tsx`: Multi-segment volumetric progress bar visualizing Allocated vs. Consumed vs. Approved vs. Committed water, accompanied by the authoritative arithmetic formula.
  - `IrrigationLifecycleTracker.tsx`: 6-stage operational pipeline tracker displaying real-time counts from draft to completed today.
  - `RecentActivityFeed.tsx`: Chronological stream of operational events with relative timestamps.
  - `DashboardFieldMap.tsx`: Spatial GIS integration visualizing prioritized parcels on Leaflet with interactive popups and one-click field inspections.
- **Page (`src/pages/dashboard/DashboardPage.tsx`)**:
  - Replaces all static mockups with the unified farm workspace.
  - Renders skeleton loaders matching card geometry for zero layout shift (CLS = 0).
  - Provides graceful error recovery with retry mechanisms and multi-language support (`uz`, `ru`, `en`).

---

## 9. Decision-to-Action Operations Center (`src/features/operations` & `src/entities/operations`)

Developed in Milestone 08.5, the Operations Center transforms separate platform modules into a continuous, safe, fast, and transparent decision-to-action workspace:
- **Technical Blueprint**: [`docs/operational-workflow.md`](file:///home/izzatbek/Desktop/agronexsus/docs/operational-workflow.md) & [`docs/operations-ux.md`](file:///home/izzatbek/Desktop/agronexsus/docs/operations-ux.md)
- **Entities (`src/entities/operations`)**:
  - `types.ts`: TypeScript contracts for `OperationalWorkflowState`, `WorkflowStep`, `SituationSummary`, `ScheduleConflict`, `OperationalVerificationResponse`, and request payloads.
  - `api.ts`: Typed API client for single-pass context loading, plan preparation, approval, rejection, scheduling, execution start/completion, and verification.
- **Features (`src/features/operations`)**:
  - `queries/useOperationsQueries.ts`: TanStack Query hooks with targeted cache invalidation across operations, irrigation, water, and dashboard caches.
  - `OperationalContextHeader.tsx`: Parcel header with dynamic hydraulic safety interlock banner and parcel switcher.
  - `OperationalWorkflowTimeline.tsx`: Chronological 7-step operational timeline with status badges, timestamps, actors, and step action triggers.
  - `CurrentSituationPanel.tsx`: High-contrast telemetry cards displaying root-zone soil moisture, 12h trend, ambient temperature, telemetry freshness, and volumetric quota.
  - `RecommendationSummaryCard.tsx`: Agronomic advisory with confidence badge, root-zone rationale, and supporting evidence.
  - `ConstraintChecklistCard.tsx`: Precedence rules checklist covering hydraulic safety, seasonal water quota, and nocturnal dispatch window.
  - `PlanReviewCard.tsx`: Recommended volume, duration, date, and review actions.
  - `PlanApprovalDialog.tsx`: Modal requiring mandatory operational constraints acknowledgement checkbox (`acknowledgeConstraints = true`).
  - `PlanRejectDialog.tsx`: Modal requiring mandatory rejection reason of $\ge 3$ characters.
  - `ScheduleConflictAlert.tsx`: Alert banner highlighting canal dispatch conflicts with adjustment suggestions.
  - `OperationExecutionPanel.tsx`: Non-actuator manual recording controls for physical watering start and completion.
  - `PostIrrigationVerificationCard.tsx`: Planned vs actual volume/duration comparison, soil moisture delta ($\Delta\%$), recharge validation, and closed-loop calibration evidence.
  - `WorkflowActionBar.tsx`: Contextual action bar dynamically adapting based on current operational state.
- **Page (`src/pages/operations/OperationsPage.tsx`)**:
  - Single contextual workspace connecting all decision steps.
  - Accessible via sidebar navigation and 1-click CTAs from Dashboard Action Center and Priority Fields Table.

---

## 10. Farm Performance, Historical Intelligence & Analytics (`src/features/analytics` & `src/entities/analytics`)

Developed in Milestone 08.6, the Analytics workspace establishes historical intelligence and agricultural performance auditing:
- **Technical Blueprint**: [`docs/farm-performance.md`](file:///home/izzatbek/Desktop/agronexsus/docs/farm-performance.md), [`docs/analytics-architecture.md`](file:///home/izzatbek/Desktop/agronexsus/docs/analytics-architecture.md), and [`docs/analytics-metrics.md`](file:///home/izzatbek/Desktop/agronexsus/docs/analytics-metrics.md).
- **Entities (`src/entities/analytics`)**:
  - `types.ts`: TypeScript contracts for `AnalyticsOverviewResponse`, `WaterPerformanceDto`, `IrrigationPerformanceDto`, `TelemetryQualityDto`, `VerificationPerformanceDto`, `SafetyEventMetricsDto`, `PeriodComparisonDto`, `DataConfidenceDto`, `FarmInsightDto`, `FieldPerformanceDto`, and `PerformanceTrendsResponse`.
  - `api.ts`: Typed API client methods for fetching overview, trends, field rankings, and insights with `AbortSignal` cancellation support.
- **Features (`src/features/analytics`)**:
  - `queries/useAnalyticsQueries.ts`: TanStack Query hooks (`useAnalyticsOverview`, `usePerformanceTrends`, `useFieldPerformanceRankings`, `useFarmInsights`) with optimized stale times and automated cache invalidation.
  - `PeriodFilterBar.tsx`: Temporal preset switcher (`TODAY`, `7d`, `30d`, `90d`, `SEASON`, `CUSTOM`) with custom date pickers, parcel dropdown, and crop filter.
  - `DataConfidenceBanner.tsx`: High-visibility audit banner displaying `COMPLETE`, `PARTIAL`, or `INSUFFICIENT` data quality flags.
  - `FarmPerformanceKpis.tsx`: Farm overview strip (fields, active parcels, area, water quota, operations, sensors, safety) and 5 core performance KPI cards (utilization, adherence, telemetry, verification, safety).
  - `WaterPerformanceCard.tsx`: Dedicated water quota consumption progress bar, excess volume counter, and plan variance ratio ($m^3$ and $\%$).
  - `IrrigationPerformanceCard.tsx`: Operational discipline card displaying plan adherence %, execution rate, average delay, and average duration.
  - `TelemetryQualityCard.tsx`: Sensor reliability card showing valid reading %, fleet availability, and telemetry quality governance rules.
  - `PeriodComparisonCard.tsx`: Period-over-period delta comparison with safe fallback when prior historical data is missing.
  - `PerformanceTrendCharts.tsx`: Recharts-powered chronological composed charts (Actual vs Planned water delivery, Operations count, Telemetry coverage %, and Safety incidents) with `DAILY`, `WEEKLY`, and `MONTHLY` bucketing.
  - `FarmInsightsCard.tsx`: Actionable agricultural insight cards with WHAT, WHY, EVIDENCE, and NEXT STEP deep-linking to existing workflows (`/water`, `/sensors`, `/operations`, `/anomalies`).
  - `FieldPerformanceTable.tsx`: Ranked parcel table with search, status filtering, multi-column sorting, pagination, and explainable 5-factor score breakdown modal.
- **Dashboard Integration (`src/features/dashboard/components/FarmPerformanceSummaryCard.tsx`)**:
  - Compact summary card embedded directly in the Farm Intelligence Dashboard displaying 30-day water utilization with prior period delta, plan adherence %, telemetry coverage %, verification coverage %, and a 1-click CTA to the detailed analytics workspace.
- **Page (`src/pages/analytics/AnalyticsPage.tsx`)**:
  - Unified workspace bringing together all performance intelligence components.
  - Responsive layout (desktop multi-column grid, tablet stacked sections, mobile horizontal scrolling for tables).
  - Full trilingual internationalization (`uz`, `ru`, `en`) and dark mode support.

---

## 11. Predictive Farm Intelligence & Forecasting (`src/features/prediction` & `src/entities/prediction`)

Developed in Milestone 08.7, the Predictions workspace transforms platform telemetry and agronomic models into predictive decision support:
- **Technical Blueprint**: [`docs/predictive-intelligence.md`](file:///home/izzatbek/Desktop/agronexsus/docs/predictive-intelligence.md), [`docs/prediction-architecture.md`](file:///home/izzatbek/Desktop/agronexsus/docs/prediction-architecture.md), [`docs/forecast-metrics.md`](file:///home/izzatbek/Desktop/agronexsus/docs/forecast-metrics.md), and [`docs/prediction-data-quality.md`](file:///home/izzatbek/Desktop/agronexsus/docs/prediction-data-quality.md).
- **Entities (`src/entities/prediction`)**:
  - `types.ts`: TypeScript contracts for `PredictionHorizon`, `PredictionType`, `RiskLevel`, `DataSufficiency`, `IrrigationLikelihood`, `PredictionItem`, `PredictionOverview`, `FieldPredictions`, `WaterDemandForecast`, `IrrigationNeedRanking`, `HistoryItem`, and `ForecastHistoryResponse`.
  - `api.ts`: Typed API client methods for overview, parcel predictions, water demand, irrigation ranking, emerging risks, and forecast history with `AbortSignal` cancellation support.
- **Features (`src/features/prediction`)**:
  - `queries/usePredictionQueries.ts`: TanStack Query hooks (`usePredictionOverviewQuery`, `useFieldPredictionsQuery`, `useWaterDemandForecastQuery`, `useIrrigationNeedRankingQuery`, `useEmergingRisksQuery`, `useForecastHistoryQuery`).
  - `PredictionHorizonSelector.tsx`: Quick switcher across 3 horizons (`NEXT_24_HOURS`, `NEXT_48_HOURS`, `NEXT_7_DAYS`).
  - `DataSufficiencyBadge.tsx`: Visual audit indicators for `SUFFICIENT`, `LIMITED`, and `INSUFFICIENT` data.
  - `SoilMoistureForecastCard.tsx`: Root-zone moisture trajectory display with uncertainty ranges, confidence score, and direct deep-link to irrigation planning if low moisture is projected.
  - `WaterDemandForecastCard.tsx`: Multi-horizon volumetric water demand card with uncertainty range bounds.
  - `PredictionRiskOverview.tsx`: Emerging infrastructure risks (water quota headroom, hydraulic safety locks with strict `BLOCKED` status enforcement, and sensor telemetry reliability).
  - `IrrigationNeedMatrix.tsx`: Searchable and filterable table ranking parcels by MAD threshold breach urgency.
  - `ForecastHistoryTable.tsx`: Empirical calibration and historical accuracy verification table comparing past predicted values against actual sensor measurements.
  - `PredictionExplanationModal.tsx`: Transparent explainable AI modal with 5 standardized sections: WHAT, WHY, EVIDENCE, ASSUMPTIONS & LIMITATIONS, and RECOMMENDED NEXT STEP.
  - `PredictiveSummaryCard.tsx`: Compact dashboard summary card embedded directly in the Farm Intelligence Dashboard (`/dashboard`) displaying 4 core forecast signals and 1-click CTA `[View Forecasts →]`.
- **Page (`src/pages/predictions/PredictionsPage.tsx`)**:
  - Dedicated predictive intelligence workspace at `/predictions`.
  - Parcel selector dropdown, farm overview KPI cards, parcel forecast cards, tabs for Irrigation Ranking, Model Evaluation & Continuous Intelligence (Milestone 08.8), and Calibration History.
- **Continuous Intelligence & Model Evaluation (Milestone 08.8)**:
  - `PredictionEvaluationOverviewCard.tsx`: Evaluated count, coverage %, overall model health (`HEALTHY`, `WATCH`, `DEGRADED`, `INSUFFICIENT_DATA`), drift alerts, trigger batch evaluation button, and honest $n < 10$ insufficient data gating banner.
  - `PredictionTypeEvaluationCard.tsx`: Numerical regression errors (MAE, RMSE, safe MAPE with zero check, MBE bias, bounds hit rate) and categorical confusion matrix ($TP, TN, FP, FN, INT$, accuracy, precision, recall, F1).
  - `ConfidenceCalibrationCard.tsx`: Verification of confidence tiers (`HIGH`, `MEDIUM`, `LOW`) with automated detection of calibration inversions.
  - `DataQualityImpactCard.tsx`: Empirical comparison of accuracy and error across `DATA_COMPLETE`, `DATA_PARTIAL`, and `DATA_INSUFFICIENT` telemetry states.
  - `EvaluationInsightsList.tsx`: Actionable deterministic recommendations for systematic bias correction, hydraulic safety blocks recognized, and sensor dropouts.
  - `HumanDecisionImpactCard.tsx`: Audit of AI Recommendation vs Human Decision (approved directly vs modified vs rejected) vs Real-world Outcome.
  - `FieldEvaluationTable.tsx`: Parcel-by-parcel breakdown with $n < 5$ limited sample badges and pagination.
  - `EvaluationHistoryTable.tsx`: Filterable, searchable evaluation ledger with status and type filters.
  - `EvaluationDetailModal.tsx`: Comprehensive audit popup showing forecast parameters, actual outcome, regression error, confusion classification, and hydraulic safety interlock context.




