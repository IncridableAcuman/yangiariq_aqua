# Docker Production Deployment Guide
## YANGIARIQ AQUA AI Platform — Milestone 08.18

This document outlines the container engineering, multi-stage build strategy, and orchestration configuration for running YANGIARIQ AQUA AI in a production environment using Docker and Docker Compose.

---

## 1. Multi-Stage Dockerfile Architecture

### 1.1 Backend (`docker/backend/Dockerfile`)
The backend build utilizes an isolated multi-stage build pattern:
- **Build Stage (`eclipse-temurin:21-jdk-alpine`):**
  - Copies Gradle wrapper and build configuration files (`gradlew`, `build.gradle`, `settings.gradle`).
  - Executes `./gradlew dependencies` for layer caching.
  - Compiles the source and builds the production executable JAR (`./gradlew bootJar -x test`).
- **Runtime Stage (`eclipse-temurin:21-jre-alpine`):**
  - Lightweight Alpine-based JRE image (~137MB compressed).
  - Creates dedicated unprivileged system user/group `aqua:aqua` (non-root execution).
  - Installs `curl` and `tzdata` for container health monitoring and UTC clock sync.
  - Injects production container-aware JVM flags:
    ```bash
    -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=40.0 -XX:+ExitOnOutOfMemoryError -Duser.timezone=UTC
    ```
  - Configures container `HEALTHCHECK` against Spring Boot Actuator liveness probe.
  - Sets `STOPSIGNAL SIGTERM` to ensure graceful application shutdown.

### 1.2 Frontend (`docker/frontend/Dockerfile`)
- **Build Stage (`node:22-alpine`):**
  - Executes clean dependency install with `npm ci`.
  - Builds production static assets via `npm run build` with Vite manual chunk optimization.
- **Runtime Stage (`nginx:alpine`):**
  - Ultra-lightweight Nginx container (~27MB compressed).
  - Copies pre-compiled static bundles from build stage into `/usr/share/nginx/html`.
  - Injects hardened Nginx configuration with gzip compression, security headers, and immutable asset caching.
  - Native healthcheck via `wget -qO- http://localhost:80/index.html`.

---

## 2. Docker Compose Production Topology (`docker-compose.prod.yml`)

The production Compose configuration enforces:
- **Internal Service Isolation:** `postgres`, `backend`, and `frontend` do NOT publish ports to the host interface. They communicate exclusively over the private bridge network `yangiariq_aqua_prod_net`.
- **Public Edge Gateway:** Only `nginx` publishes ports `80` (HTTP) and `443` (HTTPS) to the host.
- **Resource Limits:**
  - `postgres`: 1.5 CPUs, 1024MB RAM.
  - `backend`: 2.0 CPUs, 1536MB RAM.
  - `frontend`: 0.5 CPUs, 256MB RAM.
  - `nginx`: 0.5 CPUs, 256MB RAM.
- **Persistent Storage:** Named volume `yangiariq_aqua_postgres_prod_data` guarantees database persistence across restarts.

---

## 3. Step-by-Step Deployment Instructions

### Step 1: Prepare Production Environment File
Copy the production environment template:
```bash
cp .env.prod.example .env.prod
chmod 600 .env.prod
```
Edit `.env.prod` with strong cryptographic keys and valid domain settings:
```bash
# Generate secure 64-byte JWT secret:
openssl rand -base64 64

# Generate secure PostgreSQL password:
openssl rand -hex 24
```

### Step 2: Validate Configuration via Preflight Check
Before launching containers, run the automated preflight verification:
```bash
./scripts/production/preflight-check.sh .env.prod
```

### Step 3: Build Production Images
Build the production Docker images locally:
```bash
# Build Frontend
docker build -t yangiariq-aqua-frontend:0.8.18 -f docker/frontend/Dockerfile ./frontend

# Build Backend
docker build -t yangiariq-aqua-backend:0.8.18 -f docker/backend/Dockerfile ./backend
```

### Step 4: Validate Compose Configuration
Verify Compose merges environment variables without errors:
```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml config
```

### Step 5: Start the Production Stack
```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d
```

### Step 6: Verify Service Health
Monitor startup until all containers report `healthy`:
```bash
docker compose -f docker-compose.prod.yml ps
```
Or execute the automated health probe suite:
```bash
./scripts/production/health-check.sh
```

---

## 4. Operational Management & Logs

### View Consolidated Production Logs
```bash
docker compose -f docker-compose.prod.yml logs -f
```

### View Individual Service Logs
```bash
# Backend application logs
docker compose -f docker-compose.prod.yml logs -f backend

# Nginx access & error logs
docker compose -f docker-compose.prod.yml logs -f nginx

# PostgreSQL logs
docker compose -f docker-compose.prod.yml logs -f postgres
```

### Stopping the Stack
To gracefully stop all services without removing persistent data:
```bash
docker compose -f docker-compose.prod.yml stop
```

To take down the stack:
```bash
docker compose -f docker-compose.prod.yml down
```
*(Note: Named volumes are preserved by default).*
