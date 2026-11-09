# Production Security Checklist & Hardening Audit
## YANGIARIQ AQUA AI Platform — Milestone 08.18

This checklist documents the security controls, defense-in-depth measures, and vulnerability mitigations verified for YANGIARIQ AQUA AI production deployments.

---

## 1. Security Verification Matrix

| Category | Security Control | Verified Implementation | Status |
| :--- | :--- | :--- | :--- |
| **Authentication** | Strong JWT Signing Key | HMAC-SHA512 with >= 512-bit key. Dev secret rejected on boot by `ProductionSecurityValidator`. | **VERIFIED** |
| **Authentication** | Rate Limiting | `RateLimitingFilter` enforces 10 req/min on `/login`, 30 req/min on `/refresh`. Returns RFC 6585 `429`. | **VERIFIED** |
| **Authentication** | Token Expiration | Configurable short-lived access tokens (1h in prod) with secure refresh token rotation and revocation. | **VERIFIED** |
| **Authorization** | Strict RBAC Enforcement | Spring Security method security (`@EnableMethodSecurity`, `@PreAuthorize`) protects all domain endpoints. | **VERIFIED** |
| **Authorization** | IDOR Protection | Field ownership, canal dispatch permissions, and district boundaries validated against principal context. | **VERIFIED** |
| **Network** | Database Isolation | PostgreSQL port `5432` has NO published host ports in `docker-compose.prod.yml`. Internal network only. | **VERIFIED** |
| **Network** | Backend API Isolation | Backend port `8080` has NO published host ports. Internal access only via NGINX reverse proxy. | **VERIFIED** |
| **Network** | Actuator Protection | Actuator `/actuator/**` is never exposed through NGINX public routing blocks. Unauthenticated access blocked. | **VERIFIED** |
| **CORS** | Strict Origin Whitelisting | `ProductionSecurityValidator` rejects `localhost`, `127.0.0.1`, and wildcard `*` with credentials in production. | **VERIFIED** |
| **Injection** | SQL Injection Mitigation | 100% parameterized queries via JPA/Hibernate; zero raw string SQL concatenation in application code. | **VERIFIED** |
| **Web Security** | X-Frame-Options | Injected by NGINX: `X-Frame-Options: DENY` (prevents clickjacking). | **VERIFIED** |
| **Web Security** | MIME Sniffing | Injected by NGINX: `X-Content-Type-Options: nosniff`. | **VERIFIED** |
| **Web Security** | Referrer Policy | Injected by NGINX: `Referrer-Policy: strict-origin-when-cross-origin`. | **VERIFIED** |
| **Web Security** | Content Security Policy | Configured: `default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https://*.tile.openstreetmap.org`. | **VERIFIED** |
| **Container** | Unprivileged Execution | Backend container runs under non-root system user `aqua:aqua` (UID/GID created in alpine runtime). | **VERIFIED** |
| **Secrets** | Zero Secrets in VCS | All production credentials loaded via `.env.prod`. `.env` and `.env.*` listed in `.gitignore`. | **VERIFIED** |
| **Safety** | Zero Hardware Actuation | Zero actuator control libraries, zero physical gate manipulation APIs. Evaluative & advisory only. | **VERIFIED** |

---

## 2. Pre-Deployment Operator Checklist

Before declaring a production deployment operational, the server administrator must verify each item:

- [ ] **1. Secret Generation:** Dedicated 512-bit Base64 `JWT_SECRET` generated via `openssl rand -base64 64` and placed in `.env.prod`.
- [ ] **2. Database Credential:** Random 24-character alphanumeric `DB_PASSWORD` generated and configured in `.env.prod`.
- [ ] **3. Domain & DNS:** DNS A-record points to the Ubuntu host IP address.
- [ ] **4. Firewall Verification:** `ufw status verbose` confirms ONLY ports `22`, `80`, and `443` are allowed.
- [ ] **5. Preflight Pass:** `./scripts/production/preflight-check.sh .env.prod` reports `[PASS]` on all checks.
- [ ] **6. TLS Certificate:** Valid Let's Encrypt certificate generated and active in NGINX.
- [ ] **7. Health Probes:** `./scripts/production/health-check.sh` reports `ALL PRODUCTION HEALTH CHECKS PASSED`.
- [ ] **8. Automated Backup:** Nightly backup cron configured and verified via manual run.
