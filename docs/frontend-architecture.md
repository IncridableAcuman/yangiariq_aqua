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

