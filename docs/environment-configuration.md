# Environment Configuration Reference
## YANGIARIQ AQUA AI Platform — Milestone 08.18

This document details all supported environment variables, profiles, sensitive secret requirements, and fail-fast validation rules across all deployment stages.

---

## 1. Environment Profiles

YANGIARIQ AQUA AI supports three distinct operational profiles:
- `dev` (Local Development): Developer defaults enabled, verbose SQL logging permitted, mock providers enabled.
- `test` (Automated Testing): In-memory/testcontainers databases, disabled rate limiters, isolated contexts.
- `prod` (Production): Strict security validations, no hardcoded secrets, connection pool optimization, disabled Swagger UI, minimal structured logging.

---

## 2. Configuration Parameters Directory

### 2.1 Backend Core & Server

| Variable Name | Default (Dev) | Production Requirement | Description |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `dev` | **MUST be `prod`** | Activates production profile configurations |
| `SERVER_PORT` | `8080` | `8080` | Internal HTTP listening port for Spring Boot |
| `SERVER_FORWARD_HEADERS_STRATEGY` | `framework` | `framework` | Enables trusted parsing of `X-Forwarded-*` headers from NGINX |

### 2.2 Database & Connection Pool (HikariCP)

| Variable Name | Default (Dev) | Production Requirement | Description |
| :--- | :--- | :--- | :--- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5433/yangiariq_aqua` | `jdbc:postgresql://postgres:5432/${DB_NAME}` | JDBC connection string |
| `DATABASE_USERNAME` / `DB_USERNAME` | `aqua_user` | Non-default production admin user | Database role |
| `DATABASE_PASSWORD` / `DB_PASSWORD` | `aqua_secure_password_dev` | **MANDATORY**: Min 16 char random password | Database credential (**Dev password REJECTED**) |
| `DB_POOL_MAX_SIZE` | `10` | `20` (Standard) / `10` (Small) | Maximum active connections in Hikari pool |
| `DB_POOL_MIN_IDLE` | `2` | `5` | Minimum idle connections kept warm |

### 2.3 Security & Authentication (JWT & Rate Limiting)

| Variable Name | Default (Dev) | Production Requirement | Description |
| :--- | :--- | :--- | :--- |
| `JWT_SECRET` | Known 512-bit dev string | **MANDATORY**: 64-byte Base64 key | HMAC-SHA512 signing secret (**Dev secret REJECTED**) |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `86400000` (24h) | `3600000` (1h) | Access token lifespan in milliseconds |
| `JWT_REFRESH_TOKEN_EXPIRATION` | `604800000` (7d) | `604800000` (7d) | Refresh token session lifespan |
| `CORS_ALLOWED_ORIGINS` | `localhost:3000, localhost:5173` | Explicit domain (e.g. `https://aqua.yangiariq.uz`) | Allowed CORS origins (**Localhost REJECTED**) |
| `RATE_LIMIT_ENABLED` | `true` | `true` | Enables IP sliding-window authentication rate limiter |
| `RATE_LIMIT_LOGIN_PER_MINUTE`| `10` | `10` | Maximum login attempts per IP per minute |
| `RATE_LIMIT_REFRESH_PER_MINUTE`| `30`| `30` | Maximum token refresh calls per IP per minute |

### 2.4 Monitoring & API Documentation

| Variable Name | Default (Dev) | Production Requirement | Description |
| :--- | :--- | :--- | :--- |
| `ENABLE_SWAGGER_PROD` | `false` | `false` | Disables Swagger UI & OpenAPI in production |
| `AI_PROVIDER` | `mock` | `mock` or `gemini` | AI reasoning provider |
| `GEMINI_API_KEY` | None | Secure production API key if enabled | Google Gemini API key |
| `SENSOR_PROVIDER` | `simulated` | `simulated`, `mqtt`, or `http` | Sensor telemetry ingestion provider |

### 2.5 Frontend Environment Variables (Vite)

| Variable Name | Default (Dev) | Production Value | Description |
| :--- | :--- | :--- | :--- |
| `VITE_API_BASE_URL` | `/api/v1` | `/api/v1` | Relative path proxied via NGINX |
| `VITE_DEFAULT_LANGUAGE` | `uz` | `uz` | Default locale (`uz`, `ru`, `en`) |
| `VITE_APP_NAME` | `Yangiariq Aqua AI` | `Yangiariq Aqua AI` | Application branding |

---

## 3. Production Security Startup Validations

During application initialization (`ProductionSecurityValidator`), the backend performs mandatory sanity checks whenever `prod` is active:

1. **JWT Secret Invariance:**
   - Secret must be present.
   - Secret must NOT match the development default `404E6352...`.
   - Secret length must be >= 32 characters (256 bits minimum).
2. **Database Credential Invariance:**
   - Password must be present.
   - Password must NOT match `aqua_secure_password_dev`.
3. **CORS Invariance:**
   - Allowed origins must NOT contain `localhost` or `127.0.0.1`.
   - Wildcard `*` is forbidden when `allowCredentials=true`.

If any of these constraints fail, the backend aborts startup immediately with a clear error explaining the violation.
