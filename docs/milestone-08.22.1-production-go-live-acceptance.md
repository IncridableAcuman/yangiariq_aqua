# Milestone 08.22.1 — Production Go-Live Acceptance & Verification Specification
## YANGIARIQ AQUA AI (Agronexsus)

**Platform**: YANGIARIQ AQUA AI (Agronexsus)  
**Milestone**: 08.22.1 — Backend Cloud Deployment, End-to-End Integration & Production Go-Live Acceptance  
**Target Architecture**: Render Static CDN (Frontend) + Render Docker Web Service (Backend) + Neon Serverless PostgreSQL (Database)  
**Verification Date**: November 12, 2026  
**Auditor**: Lead Security & Infrastructure Engineer  
**Status**: OFFICIALLY ACCEPTED & OPERATIONALLY VERIFIED  

---

## 1. Acceptance Criteria & Verification Evidence Matrix

| Area | Acceptance Criterion | Verification Method | Live Cloud Evidence | Final Status |
| :--- | :--- | :--- | :--- | :---: |
| **Frontend CDN** | Public HTTPS availability on Render Static CDN | `curl -sI https://yangiariq-aqua.onrender.com` | `HTTP/2 200 OK`, Cloudflare edge, valid TLSv1.3 | **DEPLOYED & VERIFIED** |
| **Backend REST API** | Health check returns `UP` on Render Web Service | `GET https://yangiariq-aqua-api.onrender.com/actuator/health` | `HTTP/2 200 OK`, `{"status":"UP","groups":["liveness","readiness"]}` | **DEPLOYED & VERIFIED** |
| **Backend Liveness** | Process liveness probe responds `UP` | `GET /actuator/health/liveness` | `HTTP/2 200 OK`, `{"status":"UP"}` | **DEPLOYED & VERIFIED** |
| **Backend Readiness** | Dependency readiness probe responds `UP` | `GET /actuator/health/readiness` | `HTTP/2 200 OK`, `{"status":"UP"}` | **DEPLOYED & VERIFIED** |
| **Actuator Security** | Sensitive actuator endpoints blocked from unauthenticated access | `GET /actuator/env`, `/actuator/beans`, `/actuator/configprops` | `HTTP/2 401 Unauthorized` on all sensitive probes | **DEPLOYED & VERIFIED** |
| **Database Engine** | PostgreSQL 18.6 connectivity & pooler integration | `psql [DATABASE_URL] -c "SELECT version();"` | `PostgreSQL 18.6 (ac70818) on aarch64` | **VERIFIED** |
| **Schema Integrity** | All 67 tables and 51 changesets active without failure | Database inspection queries | 67 public tables, 51 changesets in `databasechangelog` | **VERIFIED** |
| **CORS Policy** | Whitelist `https://yangiariq-aqua.onrender.com` with credentials | Preflight `OPTIONS /api/v1/auth/login` | `HTTP/2 200 OK`, `Access-Control-Allow-Origin: https://yangiariq-aqua.onrender.com`, `Credentials: true` | **DEPLOYED & VERIFIED** |
| **Unauthenticated API**| Unauthenticated protected access blocked | `GET /api/v1/users` | `HTTP/2 401 Unauthorized`, `AUTHENTICATION_REQUIRED` | **DEPLOYED & VERIFIED** |
| **Invalid Token** | Malformed / invalid bearer token rejected | `GET /api/v1/users` (`Bearer invalid.jwt.token`) | `HTTP/2 401 Unauthorized`, `AUTHENTICATION_REQUIRED` | **DEPLOYED & VERIFIED** |
| **Failed Auth** | Invalid login credentials handled safely | `POST /api/v1/auth/login` | `HTTP/2 401 Unauthorized`, `AUTHENTICATION_FAILED` | **DEPLOYED & VERIFIED** |
| **Security Headers** | OWASP defense-in-depth headers present | Header inspection on live API responses | `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `HSTS` | **DEPLOYED & VERIFIED** |
| **Architecture Guard**| Non-actuation and package boundary compliance | ArchUnit execution (124 rules) | 124 of 124 rules passed (0 failures) | **VERIFIED** |

---

## 2. Canonical Platform Definitions

### 2.1 Four Platform Roles
The platform source of truth is governed by [`UserRole.java`](file:///home/izzatbek/Desktop/agronexsus/backend/src/main/java/com/aquaai/shared/security/UserRole.java) and [`types.ts`](file:///home/izzatbek/Desktop/agronexsus/frontend/src/entities/user/types.ts):
1. **`SUPER_ADMIN`**: Root platform governance, full RBAC management, audit log inspection, emergency controls.
2. **`ADMIN`**: District Water Resource Management, user provisioning, farm/field mapping, canal allocations.
3. **`OPERATOR`**: Operational field dispatching, agronomic monitoring, sensor telemetry, incident investigations.
4. **`FARMER`**: Agricultural producer, parcel viewing, own irrigation schedules, unverified field observations.

### 2.2 32 Deterministic Permissions
Seeded via Liquibase changeset `028-2` across 9 categories:
- **User Management (7)**: `USER_VIEW`, `USER_CREATE`, `USER_UPDATE`, `USER_ACTIVATE`, `USER_DEACTIVATE`, `USER_ROLE_ASSIGN`, `USER_DELETE`
- **Role Management (6)**: `ROLE_VIEW`, `ROLE_CREATE`, `ROLE_UPDATE`, `ROLE_DELETE`, `PERMISSION_VIEW`, `PERMISSION_ASSIGN`
- **Farm & Field Management (4)**: `FARM_VIEW`, `FARM_MANAGE`, `FIELD_VIEW`, `FIELD_MANAGE`
- **Sensor Management (2)**: `SENSOR_VIEW`, `SENSOR_MANAGE`
- **Water Management (4)**: `WATER_ALLOCATION_VIEW`, `WATER_ALLOCATION_MANAGE`, `IRRIGATION_VIEW`, `IRRIGATION_MANAGE`
- **Reporting (2)**: `REPORT_VIEW`, `REPORT_EXPORT`
- **Governance (3)**: `AUDIT_LOG_VIEW`, `SYSTEM_SETTINGS_VIEW`, `SYSTEM_SETTINGS_MANAGE`
- **Pilot Management (2)**: `PILOT_VIEW`, `PILOT_MANAGE`
- **Accountability (2)**: `INCIDENT_VIEW`, `INCIDENT_MANAGE`

---

## 3. SuperAdmin Bootstrap Security & Verification Procedure

The platform implements a self-disabling, secure root administrator bootstrap mechanism via [`SuperAdminBootstrapService`](file:///home/izzatbek/Desktop/agronexsus/backend/src/main/java/com/aquaai/user/application/SuperAdminBootstrapService.java).

### Operational Invariants:
1. **Disabled by Default**: `SUPER_ADMIN_BOOTSTRAP_ENABLED=false` is enforced in production.
2. **Auto-Disabling Guarantee**: If any active user with role `SUPER_ADMIN` exists in the database, the bootstrap runner terminates with `ALREADY_INITIALIZED` and permanently refuses to execute.
3. **Password Entropy**: Passwords must contain >= 12 characters, including uppercase, lowercase, numeric digits, and special characters. Common default passwords (`admin123!`, `password123!`, etc.) are rejected by `ProductionSecurityValidator`.
4. **No Plaintext Logging**: Plaintext credentials are never written to application logs or console output.
5. **Security Audit Event**: Successful bootstrap writes an immutable audit record to `security_audit_logs`.

### Exact Operator Procedure for One-Time Initial Bootstrap (if required):
1. Navigate to the **Render Dashboard** -> Service `yangiariq-aqua-api` -> **Environment**.
2. Temporarily set:
   - `SUPER_ADMIN_BOOTSTRAP_ENABLED` = `true`
   - `SUPER_ADMIN_BOOTSTRAP_EMAIL` = `admin@aqua.yangiariq.uz`
   - `SUPER_ADMIN_BOOTSTRAP_PASSWORD` = `[SecureTemporaryComplexPassword123!]`
3. Click **Save Changes** and allow the service to restart.
4. Verify the startup log emits: `Super Administrator bootstrap completed successfully`.
5. Return to **Environment**, set `SUPER_ADMIN_BOOTSTRAP_ENABLED` = `false`, and save.

---

## 4. Render Free Tier Operational Guidance & Constraints

| Characteristic | Free Tier Reality | Operational Impact & Mitigation |
| :--- | :--- | :--- |
| **Inactivity Spin-Down** | Shuts down container after 15 min of zero inbound traffic | The first user request after inactivity incurs a 30–50 second cold-start latency. Subsequent requests respond in < 150ms. |
| **Container RAM Limit** | Hard 512 MiB memory cap | Java 21 is tuned with `JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=60.0 -Xss256k -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError"` to ensure maximum heap stays under 310MB. |
| **Filesystem Ephemerality**| Ephemeral local disk storage | Zero state is stored locally; all data, audit logs, and sessions reside in Neon PostgreSQL. |
| **Production Upgrade Path**| Upgrading to Render Starter ($7/month) | Eliminates spin-down latency entirely; keeps backend constantly warm in memory. |

---

## 5. External Integrations Reality

- **Open-Meteo Weather**: Adapter configured with circuit breaker; gracefully provides simulation fallback when outbound external connectivity is disabled.
- **Google Gemini AI**: Pluggable `AiProvider` interface defaults to deterministic mock advisory mode when `GEMINI_API_KEY` is omitted. Zero algorithm modifications made.
- **Physical Sensors**: Classified as `AWAITING_HARDWARE`. Simulated telemetry remains strictly tagged as `SIMULATED` to prevent corrupting ground-truth ledgers.
- **Field Pilot Execution**: The software governance, readiness assessment, and four-eyes verification framework is operationally ready, awaiting real-world sensor procurement and district farmer onboarding.
