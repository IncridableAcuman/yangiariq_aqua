# Architecture Overview

## Project: YANGIARIQ AQUA AI

Yangiariq Aqua AI is designed as a **Modular Monolith** targeting the water management challenges of Yangiariq district. The architecture balances agility, clean separation of concerns, and future scalability.

### Architectural Drivers
- **Domain Focus**: Optimizing agricultural irrigation efficiency in the arid and semi-arid terrain of Khorezm.
- **Maintainability**: Low coupling and high cohesion across modules. No monolithic god-services.
- **Evolutionary Path**: Prepared for module extraction into microservices when organizational or operational scaling requires it.
- **Provider Independence**: Pluggable AI engine (Gemini / Mock) and Sensor ingestion (Simulated / IoT).

### High-Level Monorepo Topology
```
agronexsus/
├── backend/                  # Java 21 & Spring Boot 3 Modular Monolith
│   ├── src/main/java/com/aquaai/
│   │   ├── auth/             # Authentication & token management
│   │   ├── user/             # User profiles, farmer & operator accounts
│   │   ├── field/            # Agricultural fields, crops, soil metrics
│   │   ├── water/            # Irrigation logs, water consumption
│   │   ├── problem/          # Water incidents and priority reporting
│   │   ├── ai/               # AI recommendation engine & provider ports
│   │   ├── sensor/           # Sensor telemetry & simulation adapter
│   │   ├── dashboard/        # Read-model aggregated statistics
│   │   ├── notification/     # User notifications & alerts
│   │   └── shared/           # Cross-cutting primitives (security, pagination, exceptions)
│   └── src/main/resources/   # Application config & Liquibase changesets
├── frontend/                 # React 18 + Vite + TypeScript SPA
│   └── src/
│       ├── app/              # QueryClient, Providers, Router, Global styles
│       ├── pages/            # Page-level routes
│       ├── widgets/          # Compound UI widgets
│       ├── features/         # User interaction features
│       ├── entities/         # Domain entities & cards
│       └── shared/           # shadcn/ui components, i18n, API client
├── docker/                   # Dockerfiles & DB initializers
├── docs/                     # Engineering reference documentation
├── ARCHITECTURE.md           # Primary architectural blueprint
├── README.md                 # Project orientation & startup guide
└── docker-compose.yml        # Multi-container local orchestration
```
