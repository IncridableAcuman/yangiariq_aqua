# YANGIARIQ AQUA AI — Production Operations Runbook & Maintenance Guide

**Milestone**: 08.22 — Production Cloud Deployment, Initial Administration & Go-Live Verification  
**Target Environments**: 
- Primary Cloud: Render Cloud (Static CDN + Docker Web Service) + Neon Serverless PostgreSQL (AWS Ohio)
- On-Premise / Self-Hosted: Ubuntu 24.04 LTS / Docker Compose / Nginx / PostgreSQL 16
**Version**: 0.8.22  
**Last Updated**: November 12, 2026  
**Security Classification**: CONFIDENTIAL — OPERATIONAL RUNBOOK  

---

## 1. System Architecture & Component Overview

The YANGIARIQ AQUA AI production architecture implements defense-in-depth edge isolation:

```text
                             INTERNET (Users / Agronomists / Auditors)
                                                |
                                                | HTTPS (Port 443) / HTTP (Port 80 -> 301 Redirect)
                                                v
                              +-----------------------------------+
                              |         EDGE GATEWAY: NGINX       |
                              | - TLS 1.2 / 1.3 Termination       |
                              | - HSTS, CSP, OWASP Headers        |
                              | - Client Request Rate Limiting    |
                              | - Gzip Compression               |
                              | - Static Asset Caching            |
                              | - ACME Certbot Renewal Proxy      |
                              +-----------------+-----------------+
                                                |
                            Internal Docker Bridge Network: yangiariq_aqua_prod_net
                                                |
                         +----------------------+----------------------+
                         |                                             |
                         v                                             v
         +-------------------------------+             +-------------------------------+
         |     FRONTEND SERVICE (SPA)    |             |      BACKEND SERVICE (API)    |
         | - React 18 / Vite Static Dist |             | - Java 21 / Spring Boot 3.3.5 |
         | - Hardened Nginx Container    |             | - Non-root (aqua:aqua)        |
         | - SPA Client-Side Routing     |             | - Spring Security Central RBAC|
         | - Port 80 (Internal Only)     |             | - Sliding Window Auth Limiter |
         |                               |             | - Actuator Health & Metrics   |
         +-------------------------------+             | - Port 8080 (Internal Only)   |
                                                       +---------------+---------------+
                                                                       |
                                                                    HikariCP
                                                                       |
                                                                       v
                                                       +-------------------------------+
                                                       |       PERSISTENCE LAYER       |
                                                       | - PostgreSQL 16 (Alpine)      |
                                                       | - Port 5432 (Internal Only!)  |
                                                       | - Liquibase (51 Changesets)   |
                                                       | - Named Persistent Volume:    |
                                                       |   yangiariq_aqua_postgres_prod|
                                                       +-------------------------------+
```

---

## 2. Production Readiness Matrix

Technical capabilities are explicitly segregated from physical real-world field readiness:

### 2.1 Technical & Platform Readiness

| Dimension | Component / Capability | Status | Notes & Evidence |
|---|---|---|---|
| **Security & RBAC** | 4 Platform Roles (`SUPER_ADMIN`, `ADMIN`, `OPERATOR`, `FARMER`) | **READY** | Enforced via Spring Security `@PreAuthorize` and database role-permission mapping. |
| **Security & RBAC** | Anti-Privilege Escalation | **READY** | Standard admins cannot mutate `SUPER_ADMIN`. Last `SUPER_ADMIN` cannot be deleted. |
| **Security & RBAC** | Immutable Security Audit Trail | **READY** | Persisted in `security_audit_logs`. Passwords and tokens sanitized and redacted. |
| **Security & RBAC** | Anti-IDOR Resource Isolation | **READY** | Farmers strictly restricted to owned farms/fields. Tested and verified. |
| **Security & RBAC** | Controlled First `SUPER_ADMIN` Bootstrap | **READY** | Idempotent startup service with complexity checks, auditing, and auto-disabling. |
| **Database** | PostgreSQL 16 Schema & Liquibase Migrations | **READY** | Complete chain of 51 changesets verified on fresh PostgreSQL 16 in 4.1s. |
| **Containers** | Multi-Stage Hardened Docker Images | **READY** | Non-root backend user (`aqua:aqua`), G1GC memory tuning, graceful termination. |
| **Edge Gateway** | Nginx Reverse Proxy & Security Headers | **READY** | CSP, HSTS, X-Frame-Options DENY, gzip compression, SPA client routing. |
| **Edge Gateway** | TLS Termination & Automated ACME | **WARNING** | Configuration template ready; **BLOCKED** from live certificate issuance pending DNS. |
| **Observability** | Spring Boot Actuator Probes | **READY** | Liveness, readiness, and OpenMetrics Prometheus endpoints operational. |
| **Disaster Recovery** | Automated Compressed Backup Tooling | **READY** | `backup-postgres.sh` tested with gzip -9 and SHA256 checksum generation. |
| **Disaster Recovery** | Automated Database Restoration Tooling | **READY** | `restore-postgres.sh` successfully restored and verified all 67 tables and 51 changesets. |
| **CI/CD** | Automated PR & Controlled Dispatch Workflows | **READY** | Workflows enforce build, lint, typecheck, unit tests, and manual deployment gate. |
| **Server Host** | Dedicated Cloud/Bare-Metal Production Server | **BLOCKED** | No public static IP, remote server SSH access, or DNS records currently allocated. |

### 2.2 Real-World Field Readiness

> [!CRITICAL]
> In strict accordance with platform governance rules, all real-world physical and human dimensions must remain classified as **NOT_READY** until physical deployment, on-site calibration, and field verification have occurred.

| Real-World Dimension | Operational Status | Current Real-World Reality |
|---|---|---|
| **Physical IoT Hardware** | **NOT_READY** | No automated physical ultrasonic or soil moisture sensors deployed in Yangiariq canals. Adapter status: `AWAITING_HARDWARE`. |
| **Field Pilot Participants** | **NOT_READY** | No physical farming cooperatives or field agronomists have been enrolled for on-site operations. |
| **Ground-Truth Measurements** | **NOT_READY** | No physical gravimetric soil samples or canal staff-gauge readings collected. |
| **Physical Actuation** | **PROHIBITED** | Platform remains strictly advisory/decision-support. Physical pumps, valves, and gates are NOT actuated. |

---

## 3. Production Deployment Runbook

### 3.1 Initial Server Provisioning (One-Time Setup)

#### Step 1: Operating System & Package Installation
Target OS: Ubuntu 24.04 LTS (64-bit x86_64).
```bash
# Update package repositories and install dependencies
sudo apt-get update && sudo apt-get upgrade -y
sudo apt-get install -y curl wget git ufw gzip tar jq ca-certificates gnupg

# Install Docker Engine and Docker Compose Plugin
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Enable and start Docker service
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
```

#### Step 2: Edge Firewall Hardening
```bash
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp comment "SSH Remote Management"
sudo ufw allow 80/tcp comment "HTTP (ACME Challenge & HTTPS Redirect)"
sudo ufw allow 443/tcp comment "HTTPS Production Traffic"
sudo ufw enable
```

#### Step 3: Deployment Directory Setup
```bash
sudo mkdir -p /opt/yangiariq-aqua /opt/yangiariq-aqua/backups/postgres
sudo chown -R $USER:$USER /opt/yangiariq-aqua
cd /opt/yangiariq-aqua
git clone https://github.com/IncridableAcuman/yangiariq_aqua.git .
```

### 3.2 Environment Configuration

1. Copy the reference template:
   ```bash
   cp .env.prod.example .env.prod
   chmod 600 .env.prod
   ```
2. Generate production secrets:
   ```bash
   # Generate high-entropy database password
   DB_PASS=$(openssl rand -base64 24)
   sed -i "s|REPLACE_WITH_STRONG_GENERATED_PRODUCTION_PASSWORD|${DB_PASS}|g" .env.prod

   # Generate 512-bit Base64 JWT signing secret
   JWT_KEY=$(openssl rand -base64 64)
   sed -i "s|REPLACE_WITH_CRYPTO_SECURE_64_BYTE_BASE64_SECRET|${JWT_KEY}|g" .env.prod
   ```
3. Set domain coordinates and bootstrap credentials:
   ```text
   DOMAIN_NAME=aqua.yangiariq.uz
   CORS_ALLOWED_ORIGINS=https://aqua.yangiariq.uz
   SUPER_ADMIN_BOOTSTRAP_ENABLED=true
   SUPER_ADMIN_BOOTSTRAP_EMAIL=admin@aqua.yangiariq.uz
   SUPER_ADMIN_BOOTSTRAP_PASSWORD=StrongSuperPassword2026!#$
   ```

### 3.3 Preflight Check & Stack Launch

```bash
# Execute automated preflight verification
./scripts/production/preflight-check.sh .env.prod

# Execute deployment script
./scripts/production/deploy.sh --tag=0.8.21

# Verify container health
./scripts/production/health-check.sh
```

---

## 4. First Administrator Bootstrap & Recovery Procedure

### 4.1 Standard Controlled Bootstrap (Initial Setup)

1. Ensure no active `SUPER_ADMIN` accounts exist in the database.
2. In `.env.prod`, configure:
   ```env
   SUPER_ADMIN_BOOTSTRAP_ENABLED=true
   SUPER_ADMIN_BOOTSTRAP_EMAIL=superadmin@aqua.uz
   SUPER_ADMIN_BOOTSTRAP_PASSWORD=YourStrongComplexPassword2026!
   SUPER_ADMIN_BOOTSTRAP_PHONE=+998901234567
   SUPER_ADMIN_BOOTSTRAP_FIRST_NAME=District
   SUPER_ADMIN_BOOTSTRAP_LAST_NAME=Administrator
   ```
3. Start the application stack:
   ```bash
   docker compose --env-file .env.prod -f docker-compose.prod.yml up -d
   ```
4. During startup, `SuperAdminBootstrapRunner` runs at `Order(5)`:
   - Validates password complexity (minimum 12 characters, uppercase, lowercase, digit, special character).
   - Generates salted BCrypt hash.
   - Persists account with role `SUPER_ADMIN` and status `ACTIVE`.
   - Emits an append-only record into `security_audit_logs`.
   - Safely logs: `Super Administrator bootstrap SUCCESS: Root SUPER_ADMIN account (s***n@aqua.uz) created. Bootstrap is now permanently disabled.`
5. Set `SUPER_ADMIN_BOOTSTRAP_ENABLED=false` and clear password in `.env.prod`. Any subsequent restart safely skips bootstrap.

### 4.2 Emergency Super Administrator Account Recovery

If all Super Administrator credentials are lost or administrators are locked out:

> [!WARNING]
> Emergency recovery requires direct server host access. Ordinary `ADMIN` users cannot escalate themselves or reset Super Administrator passwords via the API.

#### Recovery Procedure (Offline Database Access)
```bash
# 1. Connect directly to the internal PostgreSQL container
docker exec -it aqua-prod-postgres psql -U aqua_admin -d aquaai

# 2. Check existing Super Administrator accounts
SELECT id, email, phone_number, role, status FROM users WHERE role = 'SUPER_ADMIN';

# 3. Generate a new BCrypt hash for a replacement password using htpasswd or node/python
# Example using python3 on the host:
NEW_HASH=$(python3 -c "import bcrypt; print(bcrypt.hashpw(b'EmergencyResetPass2026!#$', bcrypt.gensalt(12)).decode('utf-8'))")

# 4. Update the Super Administrator password hash and ensure ACTIVE status
UPDATE users
SET password_hash = '${NEW_HASH}',
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'superadmin@aqua.uz' AND role = 'SUPER_ADMIN';

# 5. Log the manual emergency override in the security audit trail
INSERT INTO security_audit_logs (id, actor_id, actor_name, actor_role, action, target_type, target_id, details_json, result, ip_address, created_at)
VALUES (
    gen_random_uuid(),
    NULL,
    'HOST_CONSOLE_EMERGENCY',
    'ROOT_OPERATOR',
    'EMERGENCY_SUPER_ADMIN_PASSWORD_RESET',
    'USER',
    (SELECT id::text FROM users WHERE email = 'superadmin@aqua.uz'),
    '{"reason":"Emergency account credential recovery via direct host psql session"}',
    'SUCCESS',
    '127.0.0.1',
    CURRENT_TIMESTAMP
);
```

---

## 5. Database Backup & Disaster Recovery Guide

### 5.1 Automated Scheduled Backups

Backups are executed via cron using `scripts/production/backup-postgres.sh`.

#### Cron Configuration (`/etc/cron.d/aqua-backup`)
```text
# Run daily compressed backup at 02:30 UTC
30 2 * * * root cd /opt/yangiariq-aqua && ./scripts/production/backup-postgres.sh >> /var/log/aqua-backup.log 2>&1
```

#### Backup Behavior
- Streams directly from `pg_dump` through `gzip -9` (no plaintext SQL stored on disk).
- Computes SHA256 cryptographic checksums (`.sql.gz.sha256`).
- Validates gzip archive integrity (`gzip -t`).
- Automatically deletes archives older than `RETENTION_DAYS` (default 7 days).

### 5.2 Manual Backup Execution
```bash
cd /opt/yangiariq-aqua
./scripts/production/backup-postgres.sh
```

### 5.3 Disaster Recovery & Database Restoration

> [!CAUTION]
> Restoring a database drops the target database and recreates it from the backup stream. Overwriting a live production database (`aquaai` or `yangiariq_aqua`) requires the explicit `--force-production-overwrite` safety interlock flag.

#### Step 1: Select Verified Backup Archive
```bash
ls -lh /opt/yangiariq-aqua/backups/postgres/
# Verify SHA256 checksum
sha256sum -c backups/postgres/aquaai_aquaai_20261110_023000Z.sql.gz.sha256
```

#### Step 2: Practice Restore in Isolated Staging Database First
```bash
./scripts/production/restore-postgres.sh \
  ./backups/postgres/aquaai_aquaai_20261110_023000Z.sql.gz \
  aqua_restore_verification
```

#### Step 3: Production Database Restoration (If Disaster Recovery is Declared)
```bash
# 1. Stop backend application to terminate active connection pool
docker compose -f docker-compose.prod.yml stop backend

# 2. Execute restore with safety interlock
./scripts/production/restore-postgres.sh \
  ./backups/postgres/aquaai_aquaai_20261110_023000Z.sql.gz \
  aquaai \
  aqua-prod-postgres \
  aqua_admin \
  --force-production-overwrite

# 3. Restart backend application
docker compose -f docker-compose.prod.yml start backend

# 4. Verify system health
./scripts/production/health-check.sh
```

---

## 6. Server Maintenance & Security Operations

### 6.1 Routine Health & Resource Monitoring

Check container statuses:
```bash
docker compose -f docker-compose.prod.yml ps
```

Probe health endpoints:
```bash
# Nginx Edge probe
curl -i http://localhost/healthz

# Backend Liveness probe
curl -i http://localhost:8080/actuator/health/liveness

# Backend Readiness probe (probes DB connectivity)
curl -i http://localhost:8080/actuator/health/readiness
```

Inspect resource consumption:
```bash
docker stats --no-stream
free -h
df -h
```

### 6.2 Security Audit Inspection

Query the security audit trail directly via API (as `ADMIN` or `SUPER_ADMIN`):
```bash
curl -H "Authorization: Bearer <TOKEN>" \
  "https://aqua.yangiariq.uz/api/v1/admin/audit-logs?page=0&size=20"
```

Or query directly in PostgreSQL:
```bash
docker exec -it aqua-prod-postgres psql -U aqua_admin -d aquaai -c \
  "SELECT created_at, actor_name, actor_role, action, target_type, result, ip_address FROM security_audit_logs ORDER BY created_at DESC LIMIT 20;"
```

### 6.3 Certificate Renewal (Let's Encrypt / Certbot)

ACME webroot challenges are handled transparently by Nginx at `/.well-known/acme-challenge/`.

```bash
# Test dry run renewal
docker run --rm \
  -v yangiariq_aqua_certbot_www:/var/www/certbot \
  -v yangiariq_aqua_certbot_conf:/etc/letsencrypt \
  certbot/certbot renew --dry-run

# Reload Nginx to load renewed certificate
docker exec aqua-prod-nginx nginx -s reload
```

---

## 7. Application Upgrade & Rollback Procedures

### 7.1 Zero-Downtime / Rolling Upgrade

1. Pull or build the new image tag:
   ```bash
   export NEW_TAG="0.8.22"
   docker pull ghcr.io/incridableacuman/yangiariq_aqua-backend:${NEW_TAG}
   docker pull ghcr.io/incridableacuman/yangiariq_aqua-frontend:${NEW_TAG}
   ```
2. Create automated pre-upgrade safety backup:
   ```bash
   ./scripts/production/backup-postgres.sh
   ```
3. Deploy new tag:
   ```bash
   ./scripts/production/deploy.sh --tag=${NEW_TAG}
   ```
4. Verify health checks pass.

### 7.2 Automated Rollback Procedure

If post-deployment health verification fails or an issue is detected:
```bash
# Automatically rolls back to the previous recorded version in .deployments/history.log
./scripts/production/rollback.sh

# Or specify a target image tag explicitly:
./scripts/production/rollback.sh 0.8.21
```

> [!NOTE]
> Liquibase migrations are designed to be forward-compatible. If a database schema mutation must be reversed, restore from the pre-upgrade database backup using `./scripts/production/restore-postgres.sh`.
