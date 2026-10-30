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

- **Mechanism**: Stateless JWT authentication with HMAC-SHA512.
- **Roles**:
  - `FARMER`: Manages their own fields, logs irrigation, submits problem reports, views field recommendations.
  - `OPERATOR`: Manages district-wide water scheduling, monitors incidents, views aggregated analytics.
  - `ADMIN`: User management, global configuration, system auditing.
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
