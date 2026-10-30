# YANGIARIQ AQUA AI

> **Smart Water-Management Decision Support Platform for Yangiariq District**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6-blue.svg)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-5.4-purple.svg)](https://vitejs.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-Managed-red.svg)](https://www.liquibase.org/)

---

## Overview

**Yangiariq Aqua AI** is an agricultural technology platform engineered to deliver decision support for water distribution operators and local farmers in Yangiariq district (Khorezm, Uzbekistan).

The platform addresses regional challenges in canal water scheduling, soil salinization risk, and irrigation optimization through:
- **Farmer & Field Management**: Geospatial mapping of parcels, crop types, and soil parameters.
- **Irrigation Auditing**: Quantitative records of water volume delivered per field.
- **Incident Reporting**: Real-time reporting of canal blockages, leakages, and water disputes.
- **AI-Powered Decision Support**: Decoupled advisory model leveraging Google Gemini for drought prevention and irrigation scheduling.
- **Hardware-Decoupled Telemetry**: Sensor abstraction enabling simulated soil moisture and canal flow metrics today, ready for physical IoT gateways tomorrow.
- **Multilingual Support**: Uzbek (`uz`), Russian (`ru`), and English (`en`).

---

## Architectural Highlights

- **Modular Monolith**: Clean boundaries between business domains (`auth`, `user`, `field`, `water`, `problem`, `dashboard`, `ai`, `sensor`, `notification`, `shared`).
- **Clean Package-by-Feature**: Explicit separation between `api`, `application`, `domain`, and `infrastructure` layers.
- **Architectural Guardrails**: Enforced via **ArchUnit** tests ensuring that controllers never access repositories or bypass DTO mappings.
- **Pluggable AI Port**: `AiProvider` interface supporting Gemini API and mock fallbacks without leaking vendor SDK dependencies into domain logic.
- **Pluggable Sensor Port**: `SensorProvider` with `SimulatedSensorProvider` for realistic agricultural telemetry without requiring physical sensors.

For deep architectural details, see [ARCHITECTURE.md](ARCHITECTURE.md) and [docs/](docs/).

---

## Quick Start

### 1. Prerequisites
- Docker & Docker Compose
- Java 21 OpenJDK
- Node.js 20+

### 2. Configure Environment
```bash
cp .env.example .env
```

### 3. Start Database
```bash
docker compose up -d postgres
```

### 4. Run Backend
```bash
cd backend
./gradlew bootRun
```
Access Swagger UI at `http://localhost:8080/swagger-ui.html`.

### 5. Run Frontend
```bash
cd frontend
npm install
npm run dev
```
Access Frontend at `http://localhost:5173`.

### 6. Run Complete Stack in Docker
```bash
docker compose up --build -d
```
- Frontend UI: `http://localhost:3000`
- Backend API: `http://localhost:8080`

---

## Seed Accounts (Development)

| Role | Username / Phone | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `+998901234567` | `admin123` | Yangiariq District Super Admin |
| **Operator** | `+998907654321` | `admin123` | Canal Dispatch Operator |

---

## Documentation Index

- [Architecture Blueprint](ARCHITECTURE.md)
- [Architecture Overview](docs/architecture-overview.md)
- [Module Boundaries & Ownership](docs/module-boundaries.md)
- [AI Abstraction Strategy](docs/ai-abstraction.md)
- [Sensor Abstraction Strategy](docs/sensor-abstraction.md)
- [API Conventions](docs/api-conventions.md)
- [Database & Liquibase Strategy](docs/database-strategy.md)
- [Frontend Architecture](docs/frontend-architecture.md)
- [Development Workflow](docs/development-workflow.md)

---

## Engineering Standards

- **Code Quality**: SOLID, DRY, KISS, Separation of Concerns.
- **No Monolithic Bloat**: Granular domain services and DTOs.
- **Security**: No secrets or passwords in source control or application logs.
