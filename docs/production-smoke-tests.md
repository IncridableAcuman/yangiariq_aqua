# Production Smoke Test Protocol & Verification Suite
## YANGIARIQ AQUA AI (Agronexsus) — Milestone 08.22.1

**Platform**: YANGIARIQ AQUA AI (Agronexsus)  
**Milestone**: 08.22.1 — Backend Cloud Deployment, End-to-End Integration & Production Go-Live Acceptance  
**Target Environment**: Render Cloud (`yangiariq-aqua-api` + `yangiariq-aqua`) + Neon Serverless PostgreSQL  
**Execution Timestamp**: November 12, 2026, 02:44:19 UTC  
**Protocol Version**: 1.1.0 (Live Cloud Verified)  
**Author**: Lead Infrastructure & Security Engineer  

---

## 1. Live Cloud Automated Smoke Test Matrix

Every test in this matrix was executed directly against the live production cloud URLs:
- **Frontend CDN**: `https://yangiariq-aqua.onrender.com`
- **Backend API**: `https://yangiariq-aqua-api.onrender.com`
- **Database**: `ep-fragrant-mouse-begewk9z-pooler.c-9.us-east-2.aws.neon.tech`

| ID | Test Target | Command / Request | Expected Result | Actual Live Result | Status |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **ST-01** | Frontend CDN Availability | `curl -sI https://yangiariq-aqua.onrender.com` | `HTTP/2 200`, Cloudflare edge | `HTTP/2 200 OK`, `rndr-id: ec146318-5b81-4d23` | **PASS** 🟢 |
| **ST-02** | Frontend SPA HTML Content | `curl -s https://yangiariq-aqua.onrender.com \| grep "<title>"` | `<title>Yangiariq Aqua AI - Smart Water Management</title>` | Exact match rendered | **PASS** 🟢 |
| **ST-03** | Backend Actuator Health | `GET /actuator/health` | `HTTP/2 200`, `{"status":"UP",...}` | `HTTP/2 200 OK`, `{"status":"UP","groups":["liveness","readiness"]}` | **PASS** 🟢 |
| **ST-04** | Backend Liveness Probe | `GET /actuator/health/liveness` | `HTTP/2 200`, `{"status":"UP"}` | `HTTP/2 200 OK`, `{"status":"UP"}` | **PASS** 🟢 |
| **ST-05** | Backend Readiness Probe | `GET /actuator/health/readiness` | `HTTP/2 200`, `{"status":"UP"}` | `HTTP/2 200 OK`, `{"status":"UP"}` | **PASS** 🟢 |
| **ST-06** | Sensitive Actuator Probe 1 | `GET /actuator/env` | `HTTP/2 401 Unauthorized` | `HTTP/2 401 Unauthorized` | **PASS** 🟢 |
| **ST-07** | Sensitive Actuator Probe 2 | `GET /actuator/beans` | `HTTP/2 401 Unauthorized` | `HTTP/2 401 Unauthorized` | **PASS** 🟢 |
| **ST-08** | Sensitive Actuator Probe 3 | `GET /actuator/configprops` | `HTTP/2 401 Unauthorized` | `HTTP/2 401 Unauthorized` | **PASS** 🟢 |
| **ST-09** | CORS Preflight Origin Check | `OPTIONS /api/v1/auth/login` (`Origin: https://yangiariq-aqua.onrender.com`) | `HTTP/2 200 OK`, `access-control-allow-origin: https://yangiariq-aqua.onrender.com`, `credentials: true` | `HTTP/2 200 OK`, exact origin reflected, credentials true | **PASS** 🟢 |
| **ST-10** | Clickjacking Defense Header | `OPTIONS /api/v1/auth/login` | `x-frame-options: DENY` | `x-frame-options: DENY` present | **PASS** 🟢 |
| **ST-11** | MIME Sniffing Defense Header | `OPTIONS /api/v1/auth/login` | `x-content-type-options: nosniff` | `x-content-type-options: nosniff` present | **PASS** 🟢 |
| **ST-12** | Unauthenticated Access Control | `GET /api/v1/users` (no token) | `HTTP/2 401 Unauthorized` | `HTTP/2 401`, `{"code":"AUTHENTICATION_REQUIRED",...}` | **PASS** 🟢 |
| **ST-13** | Invalid Token Rejection | `GET /api/v1/users` (`Bearer invalid.jwt.token`) | `HTTP/2 401 Unauthorized` | `HTTP/2 401`, `{"code":"AUTHENTICATION_REQUIRED",...}` | **PASS** 🟢 |
| **ST-14** | Authentication Failure | `POST /api/v1/auth/login` (bad credentials) | `HTTP/2 401 Unauthorized` | `HTTP/2 401`, `{"code":"AUTHENTICATION_FAILED",...}` | **PASS** 🟢 |
| **ST-15** | Neon Database Connectivity | `psql [DATABASE_URL] -c "SELECT 1;"` | Query returns `1` row | Latency < 100ms via pooler | **PASS** 🟢 |
| **ST-16** | Database Table Schema Check | `SELECT count(*) FROM information_schema.tables WHERE table_schema='public';` | Exactly 67 tables | Exactly 67 tables verified | **PASS** 🟢 |
| **ST-17** | Liquibase Migration Count | `SELECT count(*) FROM databasechangelog;` | Exactly 51 changesets | Exactly 51 changesets verified | **PASS** 🟢 |
| **ST-18** | Non-Actuation Invariant | ArchUnit Rule 124 execution | Zero actuator/hardware libraries | 124 of 124 rules passed (0 failures) | **PASS** 🟢 |

---

## 2. In-Memory Rate Limiting Operational Guidance

The backend rate limiting engine (`RateLimitingFilter`) operates using an in-process sliding-window counter.
- **Production Setting**: 10 requests/minute on `/api/v1/auth/login`, 30 requests/minute on `/api/v1/auth/refresh`.
- **Single-Instance Deployment (Current Render Free/Starter)**: 100% effective per client IP.
- **Multi-Instance Deployment Limitation**: If the backend is horizontally scaled across multiple Render containers without sticky routing, rate-limit buckets are partitioned per container instance. For distributed rate limiting in multi-instance topologies, an external Redis cache adapter can be introduced when multi-replica scaling is required.
