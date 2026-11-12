# Production Environment Variables Specification
## YANGIARIQ AQUA AI (Agronexsus) — Milestone 08.22

This specification provides the comprehensive inventory of all runtime and build-time environment variables across the backend, frontend, database, and third-party integrations.

> [!CAUTION]
> **Zero Secrets in VCS Policy**: Never commit actual passwords, private API keys, or JWT secrets to Git. Always supply secrets via secure environment secret stores (e.g., Render Dashboard Environment Variables, GitHub Actions Secrets, or host-level protected `.env.prod` files).

---

## 1. Backend Runtime Environment Variables (`yangiariq-aqua-api`)

These variables govern the Java 21 Spring Boot 3 modular monolith application.

| Variable Name | Category | Required / Optional | Default (if unset) | Production Validator Rule | Description & Safe Example |
| :--- | :---: | :---: | :---: | :---: | :--- |
| `SPRING_PROFILES_ACTIVE` | Core | **Required** | `dev` | Must contain `prod` | Activates production configuration profiles. Example: `prod` |
| `PORT` | Networking | **Required** (on Cloud) | `8080` | None | Container HTTP listening port automatically assigned by Render/Cloud platforms. Example: `10000` |
| `DATABASE_URL` | Database | **Required** | None | Must not use dev password | Standard PostgreSQL or JDBC URI. Example: `postgresql://user:secret@host.aws.neon.tech/neondb?sslmode=require` |
| `DATABASE_USERNAME` | Database | Optional (derived) | `aqua_user` | Extracted from `DATABASE_URL` | Explicit DB user if not embedded in URI. Example: `neondb_owner` |
| `DATABASE_PASSWORD` | Database | Optional (derived) | None | Must not equal `aqua_secure_password_dev` | Explicit DB password if not embedded in URI. |
| `DB_POOL_MAX_SIZE` | Database | Optional | `20` | > 0 | HikariCP maximum connection pool size. Recommended `10`–`20`. |
| `DB_POOL_MIN_IDLE` | Database | Optional | `5` | >= 0 | HikariCP minimum idle connection count. Recommended `2`–`5`. |
| `JWT_SECRET` | Security | **Required** | None | Must be >= 32 chars (256-bit); dev key rejected | HMAC-SHA signing secret for authentication tokens. Example: `GeneratedBase64RandomStringOf64CharactersOrMore...` |
| `JWT_ACCESS_TOKEN_EXPIRATION` | Security | Optional | `3600000` (1h) | Must be <= 86,400,000ms (24h) | Access token validity duration in milliseconds. Example: `3600000` |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Security | Optional | `604800000` (7d) | Must be <= 2,592,000,000ms (30d) | Refresh token validity duration in milliseconds. Example: `604800000` |
| `CORS_ALLOWED_ORIGINS` | Security | **Required** | None | Must NOT contain `localhost` or `*` with credentials | Comma-separated list of permitted frontend web origins. Example: `https://yangiariq-aqua.onrender.com` |
| `RATE_LIMIT_ENABLED` | Security | **Required** | `true` | Cannot be `false` in prod | Enables token bucket and sliding-window rate limiting on `/login` and `/refresh`. Example: `true` |
| `SUPER_ADMIN_BOOTSTRAP_ENABLED` | Admin | Optional | `false` | If true, password complexity enforced | One-time switch to provision initial SuperAdmin. Disable after initial run. Example: `false` |
| `SUPER_ADMIN_BOOTSTRAP_EMAIL` | Admin | Conditional | None | Valid email required if bootstrap enabled | Initial root administrator email address. Example: `admin@aqua.yangiariq.uz` |
| `SUPER_ADMIN_BOOTSTRAP_PASSWORD` | Admin | Conditional | None | Min 12 chars, upper, lower, digit, symbol | Initial root administrator temporary password. Must be rotated immediately. |
| `AI_PROVIDER` | Integration | Optional | `mock` | None | AI reasoning engine. Supported: `mock` (fallback), `gemini`. Example: `mock` |
| `GEMINI_API_KEY` | Integration | Optional | None | Masked in logs | Google Gemini API key for automated agronomic decision support. |
| `GEMINI_MODEL` | Integration | Optional | `gemini-1.5-flash` | None | Target LLM model name. Example: `gemini-1.5-flash` |
| `SENSOR_PROVIDER` | Integration | Optional | `simulated` | Cannot claim hardware if simulated | Telemetry ingest mode. Supported: `simulated`, `gateway`. Example: `simulated` |
| `ENABLE_SWAGGER_PROD` | Documentation | Optional | `false` | None | Flag to expose OpenAPI / Swagger UI in production. Example: `true` |
| `JAVA_OPTS` | JVM Runtime | Optional | G1GC flags | Memory bounds | JVM optimization flags. Example: `-XX:+UseG1GC -XX:MaxRAMPercentage=60.0 -Xss256k -XX:MaxMetaspaceSize=128m` |

---

## 2. Frontend Build-Time Environment Variables (`yangiariq-aqua`)

> [!IMPORTANT]
> **Vite Bundling Principle**: Variables prefixed with `VITE_` are compiled statically into the distributed JavaScript bundle (`dist/assets/*.js`) during `npm run build`. Never place secrets, database strings, or private API keys in `VITE_` variables.

| Variable Name | Required / Optional | Default (if unset) | Production Setting | Description |
| :--- | :---: | :---: | :---: | :--- |
| `VITE_API_BASE_URL` | **Required** | `/api/v1` | `https://yangiariq-aqua-api.onrender.com/api/v1` | Public REST API base URL queried by frontend Axios/Fetch client. |
| `VITE_DEFAULT_LANGUAGE` | Optional | `uz` | `uz` | Default i18n localization locale (`uz` = Uzbek, `ru` = Russian, `en` = English). |
| `VITE_APP_NAME` | Optional | `Yangiariq Aqua AI` | `Yangiariq Aqua AI` | Application branding title rendered in navigation headers and tab title. |

---

## 3. Database Connection Configuration (`Neon PostgreSQL`)

The application supports standard cloud connection strings via `DatabaseUrlEnvironmentPostProcessor`:

### Neon Connection URI Format:
```text
postgresql://[DB_USER]:[DB_PASSWORD]@[EP_HOST].c-9.us-east-2.aws.neon.tech/[DB_NAME]?sslmode=require&channel_binding=require
```

### Automatic Post-Processing:
- Protocol prefix `postgresql://` is converted to `jdbc:postgresql://`.
- User and password credentials are securely decoupled from the URL and mapped to `spring.datasource.username` and `spring.datasource.password`.
- Query parameter `sslmode=require` is preserved to guarantee encrypted in-transit communication with Neon AWS Ohio infrastructure.
