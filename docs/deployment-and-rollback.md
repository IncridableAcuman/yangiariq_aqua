# Deployment & Rollback Operational Playbook
## YANGIARIQ AQUA AI Platform — Milestone 08.18

This playbook outlines the exact operational procedures for executing production deployments, verifying service health, and executing rollbacks in the event of an incident.

---

## 1. Automated Production Deployment Workflow

All production deployments are executed via `scripts/production/deploy.sh`.

```text
[ deploy.sh ]
      |
      +---> 1. Run Preflight Check (scripts/production/preflight-check.sh)
      |        - Validates Docker, Compose, Memory, Disk, Ports, Env file
      |
      +---> 2. Create Pre-Deployment Backup (scripts/production/backup-postgres.sh)
      |        - Dumps current database to backups/postgres/
      |
      +---> 3. Apply Compose Update
      |        - docker compose -f docker-compose.prod.yml up -d --remove-orphans
      |
      +---> 4. Wait for Service Health
      |        - Polls healthcheck endpoints up to 120s
      |
      +---> 5. Execute Post-Deployment Verification (scripts/production/health-check.sh)
      |        - Nginx /healthz
      |        - Backend /actuator/health/liveness & /readiness
      |        - Frontend /index.html
      |
      +---> 6. Record Version to Deployment History (.deployments/history.log)
```

### Standard Deployment Command:
```bash
# Deploy with current code / default tag:
./scripts/production/deploy.sh

# Deploy with an explicit semantic release tag:
./scripts/production/deploy.sh --tag=0.8.18
```

---

## 2. Automated Application Rollback Workflow

If a critical error, crash loop, or unexpected latency spike occurs after a deployment, execute `scripts/production/rollback.sh`.

### How Rollback Works:
1. `rollback.sh` inspects `.deployments/history.log` to determine the previous known-good version.
2. It sets `IMAGE_TAG` to the previous version and executes:
   ```bash
   docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --remove-orphans
   ```
3. It waits for service recovery and executes `scripts/production/health-check.sh`.
4. It logs the rollback event in `.deployments/history.log`.

### Rollback Commands:
```bash
# Roll back automatically to previous known-good deployment:
./scripts/production/rollback.sh

# Roll back to a specific target image tag:
./scripts/production/rollback.sh 0.8.17
```

---

## 3. Database Migration Considerations During Rollback

> [!WARNING]
> Container rollback restores application and frontend container images to an earlier version, but does **NOT** automatically alter or drop database tables created by Liquibase.

### Database Strategy During Incidents:
1. **Backward-Compatible Schema Design:** All Liquibase changesets (001 through 025) are designed to be non-destructive (adding columns with defaults or new tables). An earlier application version can almost always run against a newer schema if backward-incompatible column deletions were avoided.
2. **Forward-Fix Strategy (Preferred):** If a bug is traced to a migration, deploy a new forward-fix changeset rather than rolling back the database.
3. **Emergency Point-in-Time Database Restore:** If database corruption occurred, use the pre-deployment backup created in Step 2:
   ```bash
   # Restore pre-deployment backup into production:
   ./scripts/production/restore-postgres.sh \
     ./backups/postgres/aquaai_yangiariq_aqua_<TIMESTAMP>.sql.gz \
     aquaai \
     aqua-prod-postgres \
     aqua_admin \
     --force-production-overwrite
   ```
