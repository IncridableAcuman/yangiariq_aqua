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

