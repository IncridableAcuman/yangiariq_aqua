# Development & Operational Workflow

## 1. Prerequisites

- **Java Development Kit**: JDK 21 (Eclipse Temurin or OpenJDK 21)
- **Node.js**: Node 20+ / 22+ & npm
- **Docker & Docker Compose**: v2+
- **Git**

## 2. Local Environment Setup

### Step 1: Clone & Configure Environment Variables
```bash
cp .env.example .env
```
Inspect `.env` and verify ports and secrets.

### Step 2: Spin Up Infrastructure Containers
Start PostgreSQL 16:
```bash
docker compose up -d postgres
```

### Step 3: Run Backend
```bash
cd backend
./gradlew bootRun
```
The backend initializes Liquibase migrations on start and binds to `http://localhost:8080`.
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health Check: `http://localhost:8080/actuator/health`

### Step 4: Run Frontend
```bash
cd frontend
npm install
npm run dev
```
The Vite development server runs at `http://localhost:5173`.

---

## 3. Testing & Verification

### Backend Tests
Execute unit tests, integration tests, and ArchUnit architecture verification:
```bash
cd backend
./gradlew test
```

### Frontend Build & Typecheck
```bash
cd frontend
npm run build
```

---

## 4. Full-Stack Docker Deployment

To launch all services (PostgreSQL, Backend, Frontend) in Docker:
```bash
docker compose up --build -d
```
- Frontend application: `http://localhost:3000`
- Backend REST API: `http://localhost:8080`
- Database: `localhost:5432`
