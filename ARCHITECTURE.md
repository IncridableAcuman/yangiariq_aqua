# YANGIARIQ AQUA AI - ARCHITECTURE BLUEPRINT

## 1. Executive Summary

**Yangiariq Aqua AI** is an agricultural decision-support platform designed for Yangiariq district (Khorezm Region, Uzbekistan). The primary mission of the platform is to optimize water allocation, prevent drought stress across agricultural fields, detect and mitigate distribution canal incidents, and empower farmers and district water management operators with timely, AI-driven advisory and telemetry visualization.

This document outlines the architectural foundations, architectural decisions (ADRs), module boundaries, domain rules, and clean engineering standards governing the platform.

---

## 2. Architectural Paradigm: Modular Monolith

To maximize development velocity during the MVP and early-growth phases while eliminating premature distributed system complexity, Yangiariq Aqua AI is built as a **Modular Monolith**.

```
+----------------------------------------------------------------------------------+
|                             SPRING BOOT APPLICATION                              |
|                                                                                  |
|  +--------------+  +--------------+  +--------------+  +-----------------------+ |
|  |     auth     |  |     user     |  |    field     |  |         water         | |
|  +--------------+  +--------------+  +--------------+  +-----------------------+ |
|                                                                                  |
|  +--------------+  +--------------+  +--------------+  +-----------------------+ |
|  |   problem    |  |  dashboard   |  | notification |  |        shared         | |
|  +--------------+  +--------------+  +--------------+  +-----------------------+ |
|                                                                                  |
|  +--------------------------------+  +-----------------------------------------+ |
|  |         ai (Abstraction)       |  |          sensor (Abstraction)           | |
|  |   [AiProvider -> Gemini/Mock]  |  | [SensorProvider -> Simulated/Future IoT]| |
|  +--------------------------------+  +-----------------------------------------+ |
+----------------------------------------------------------------------------------+
                                        |
                                        v
                       +---------------------------------+
                       |     POSTGRESQL (via Liquibase)  |
                       +---------------------------------+
```

### Key Principles
1. **Single Deployable Unit, Strict Internal Decoupling**: The system runs as a single process in development and production, but package boundaries are treated as strict module contracts.
2. **Path to Microservices**: If an individual capability (such as AI analytics or high-throughput IoT ingestion) requires independent scaling in the future, its module can be extracted into an independent microservice with zero changes to other domain models.
3. **No Monolithic Service Bloat**: Business logic is encapsulated in cohesive application services dedicated to specific aggregate roots.

---

## 3. Backend Architecture: Clean Package-by-Feature

Every business module follows the Clean / Hexagonal Architecture structure:

```
com.aquaai.<module>
├── api
│   ├── <Module>Controller.java          # REST entrypoints (HTTP status codes, routing)
│   └── dto                              # Strictly segregated Request & Response DTOs
│       ├── Create<Entity>Request.java
│       └── <Entity>Response.java
├── application
│   └── <Module>Service.java             # Use case orchestration, transactions, domain events
├── domain
│   ├── <Entity>.java                    # JPA Domain entities with business invariants
│   ├── <Entity>Repository.java          # Spring Data JPA interface (Ports)
│   └── valueobjects / enums             # Domain types, status enums, value objects
└── infrastructure
    ├── <Module>RepositoryImpl.java      # Custom repository implementations (Adapters)
    └── external                         # Third-party integrations & SDK adapters
```

### Boundary Rules
- **No Controller Business Logic**: Controllers only validate inputs, call the application service, and wrap results into `ApiResponse<T>`.
- **No Direct Entity Exposure**: REST controllers never return JPA entities. They only accept and return DTOs.
- **No Direct Repository Exposure to Controllers**: Controllers never inject or call Repositories directly. This rule is enforced automatically via ArchUnit tests (`ArchitectureTests.java`).
- **Encapsulated Dependencies**: Cross-module communication occurs strictly via public Application Service interfaces or Domain Events, never through direct database joins across modules.

---

## 4. Artificial Intelligence (AI) Abstraction

The platform delivers smart water optimization using the Gemini API, but the domain layer **never depends directly on the Google Gemini SDK**.

```
              +---------------------------+
              |   AiService (Application) |
              +---------------------------+
                            |
                            v
              +---------------------------+
              |   <<interface>>           |
              |   AiProvider (Domain)     |
              +---------------------------+
                     /                 \
                    /                   \
+-----------------------+     +-----------------------+
|  GeminiAiProvider     |     |   MockAiProvider      |
|  (Infrastructure)     |     |   (Infrastructure)    |
+-----------------------+     +-----------------------+
```

### Design Rationale
- **Pluggability**: The provider can be swapped between Gemini, OpenAI, Claude, Local LLMs, or an offline Mock AI without altering a single line in `AiService` or the domain.
- **Testability & Determinism**: Unit and integration tests run against `MockAiProvider`, eliminating external network dependencies, rate limits, and API cost overhead.
- **Provider Switching**: Controlled by the `app.ai.provider` configuration property (`gemini` or `mock`).

---

## 5. IoT Sensor Abstraction & Simulation Strategy

Currently, physical IoT sensors do not exist in the fields of Yangiariq. However, the system architecture treats sensor telemetry as first-class citizens.

```
              +--------------------------------+
              |  SensorService (Application)   |
              +--------------------------------+
                               |
                               v
              +--------------------------------+
              |         <<interface>>          |
              |     SensorProvider (Domain)    |
              +--------------------------------+
                               |
              +--------------------------------+
              |     SimulatedSensorProvider    |
              |        (Infrastructure)        |
              +--------------------------------+
            (Future: MqttSensorProvider / HttpSensorProvider)
```

### Guarantees
- The domain models (`SensorReading`, `SensorType`) model real-world telemetry (soil moisture %, water flow rate, canal level, battery levels).
- `SimulatedSensorProvider` emits realistic telemetry bounded to Yangiariq's regional climate and crop water intake profiles.
- When physical hardware (LoRaWAN / MQTT gateways) is installed in later phases, an `MqttSensorProvider` adapter can be added with zero changes to the core business logic.

---

## 6. Database & Migration Strategy

- **DBMS**: PostgreSQL 16
- **Migration Engine**: Liquibase (`db.changelog-master.xml`)
- **Primary Keys**: UUIDs (`gen_random_uuid()`) for distributed scalability and secure identifiers.
- **Audit Columns**: Every table inherits `created_at`, `updated_at`, and `version` (optimistic locking) through `BaseAuditableEntity`.
- **Integrity**: Referential integrity enforced with explicit foreign key cascades (`ON DELETE CASCADE` or `ON DELETE SET NULL`) and compound indexes.
- **Rule**: Manual changes to the production schema are strictly forbidden. All schema modifications must occur via versioned Liquibase changelogs.

---

## 7. Security Architecture

- **Mechanism**: Hybrid JWT & Stateful Session Security:
  - **Access Tokens**: Stateless signed JWT (HS256) for zero-database verification per request.
  - **Refresh Tokens**: Cryptographically secure UUIDs rotated on every refresh. Only SHA-256 digests (`token_hash`) are stored in `refresh_token_sessions`. Supports multi-device tracking.
  - **Reuse Detection**: Presenting an already-revoked refresh token triggers an immediate automated compromise mitigation that invalidates all active sessions for that user across all devices.
  - **Password Reset**: Single-use tokens stored as SHA-256 digests with expiration and non-enumerating responses. Dispatched via decoupled `PasswordResetNotificationProvider`.
- **Roles & Permissions**:
  - `FARMER`: Manages their own fields, logs irrigation, submits problem reports, views field recommendations.
  - `OPERATOR`: Manages district-wide water scheduling, monitors incidents, views aggregated analytics.
  - `ADMIN`: User management, global configuration, system auditing.
- **Ownership-Aware Authorization**: `SecurityContextHelper` provides programmatic evaluation of resource ownership against the authenticated JWT principal, allowing role-based overrides (e.g. `OPERATOR` or `ADMIN`) where needed.
- **Data Protection**: Passwords hashed using BCrypt. Secrets injected exclusively via environment variables (`.env`). Secrets and tokens are filtered and never logged.

---

## 8. Frontend Architecture

The frontend is structured according to **Feature-Oriented Architecture**:

```
frontend/src/
├── app/          # Global application setup, TanStack Query provider, global styles
├── pages/        # Route components (Dashboard, Fields, Irrigation, Incidents, Settings)
├── widgets/      # Compositional blocks (Sidebar, NavigationHeader, MetricCards)
├── features/     # Feature interactions (field-map, water-logging, problem-reporting)
├── entities/     # Domain models & UI cards (user, field, sensor, irrigation)
└── shared/       # Reusable primitives (shadcn/ui, API client, i18n, formatting)
```

### State Management
- **Server State**: Managed exclusively by **TanStack Query** (caching, background revalidation, optimistic updates).
- **Client State**: Minimal local state (React hooks / Zustand where needed for non-server UI state).
- **Forms**: React Hook Form validated with **Zod** schemas.
- **Localization**: Native dictionary structure supporting Uzbek (`uz`), Russian (`ru`), and English (`en`).

---

## 9. Water Management & Irrigation Domain

Following the user/auth and field management milestones, the platform establishes the **Water Management & Irrigation (`com.aquaai.water`)** domain context:

```text
Field
  │
  ├── WaterAllocation (Linked to WaterSource: Canal, Reservoir, Well)
  │
  └── IrrigationPlan (Scheduled water volume & method)
           │
           └── IrrigationEvent (Real executed delivery & actual consumption m³)
```

### Planned vs Actual Accounting & Future Capabilities
- **Domain & Database Foundation (Milestone 06.1)**: Robust pure domain models, invariants, status lifecycles, and PostgreSQL persistence capturing allocated quotas (`WaterAllocation`), planned irrigation schedules (`IrrigationPlan`), and recorded operational deliveries (`IrrigationEvent`).
- **Application Services & REST API (Milestone 06.2)**: Production-ready clean architecture REST APIs (`/api/v1/water-sources`, `/api/v1/water-allocations`, `/api/v1/irrigation-plans`, `/api/v1/irrigation-events`), IDOR protection with anti-enumeration (404), cross-aggregate invariant enforcement (422), pagination, whitelist sorting, and OpenAPI 3 contracts.
- **Water Rules, Consumption & Efficiency (Milestone 06.3)**: Real-time volumetric consumption accounting, non-negative remaining quota clamping, over-consumption detection and excess volume tracking, planned vs actual variance ratios, parcel periodic water summaries, and zero N+1 database aggregations.
- **Water Frontend UI: Consumption, Quotas & Variance Dashboards (Milestone 06.4)**: Production-ready FSD Water operations dashboard (`frontend/src/entities/water`, `frontend/src/features/water`, `frontend/src/pages/irrigation/IrrigationPage.tsx`), integrating TanStack Query, Zod form validations, clamped progress bars, over-consumption alert banners, planned vs actual variance visualizations, field periodic summaries, regional water source catalog with RBAC, and full i18n (`uz`, `ru`, `en`).
- **Future Integration (Upcoming Milestones)**:
  - Telemetry flow meters / ultrasonic sensors feeding `IrrigationEvent.actualVolumeM3`.
  - Quota consumption monitoring and over-allocation alert mechanisms.
  - AI advisory proposing optimal `IrrigationPlan` candidates tailored to soil water capacity and weather conditions.

---

## 10. Domain Documentation References

- **Water Management UI & Dashboards**: [`docs/water-frontend.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-frontend.md)
- **Water Management & Irrigation Domain**: [`docs/water-domain.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-domain.md)
- **Water Application Services & REST API**: [`docs/water-api.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-api.md)
- **Water Consumption & Efficiency Rules**: [`docs/water-consumption.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-consumption.md)
- **Field Domain & Parcel Architecture**: [`docs/field-domain.md`](file:///home/izzatbek/Desktop/agronexsus/docs/field-domain.md)
- **Field Authorization & Security Blueprint**: [`docs/field-security.md`](file:///home/izzatbek/Desktop/agronexsus/docs/field-security.md)
- **Field Frontend & UI Architecture**: [`docs/field-frontend.md`](file:///home/izzatbek/Desktop/agronexsus/docs/field-frontend.md)
- **Field GIS & Map Visualization**: [`docs/field-gis.md`](file:///home/izzatbek/Desktop/agronexsus/docs/field-gis.md)
- **Frontend Architecture & Design Guidelines**: [`docs/frontend-architecture.md`](file:///home/izzatbek/Desktop/agronexsus/docs/frontend-architecture.md)
- **Module Boundaries & Ownership**: [`docs/module-boundaries.md`](file:///home/izzatbek/Desktop/agronexsus/docs/module-boundaries.md)
- **Database Strategy & Migrations**: [`docs/database-strategy.md`](file:///home/izzatbek/Desktop/agronexsus/docs/database-strategy.md)
- **Authentication & Security Architecture**: [`docs/authentication.md`](file:///home/izzatbek/Desktop/agronexsus/docs/authentication.md)
- **AI Abstraction Strategy**: [`docs/ai-abstraction.md`](file:///home/izzatbek/Desktop/agronexsus/docs/ai-abstraction.md)
- **Sensor Telemetry & Simulation**: [`docs/sensor-abstraction.md`](file:///home/izzatbek/Desktop/agronexsus/docs/sensor-abstraction.md)

