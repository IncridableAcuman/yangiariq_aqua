# Database Backup, Disaster Recovery & Migration Safety Runbook
## YANGIARIQ AQUA AI Platform — Milestone 08.18

This document defines the operational procedures for backing up, restoring, and maintaining schema integrity for PostgreSQL 16 and Liquibase changelogs in production.

---

## 1. Automated Backup Architecture

Automated backups are managed via `scripts/production/backup-postgres.sh`.

```text
PostgreSQL Container (Internal) 
       |
       v (pg_dump stream)
gzip -9 Compression Engine
       |
       v
Timestamped File: backups/postgres/aquaai_<DB>_YYYYMMDD_HHMMSSZ.sql.gz
       |
       +---> gzip -t Integrity Test (Exit 1 if corrupted)
       +---> SHA256 Checksum Generation (.sql.gz.sha256)
       +---> Retention Enforcement (Deletes files > RETENTION_DAYS)
```

### Key Technical Characteristics:
- **No Disk Plaintext:** Backups are piped directly from `pg_dump` into `gzip -9`, preventing unencrypted plaintext SQL dumps from resting on host disks.
- **Strict Integrity Verification:** Every archive is immediately tested via `gzip -t`. Corrupted or 0-byte dumps are deleted immediately and trigger non-zero exit codes.
- **Non-Interactive & Secure:** Executes without prompting for passwords or printing credentials to terminal or system logs.
- **Configurable Retention:** Automatically purges backups older than `RETENTION_DAYS` (default: 7 days).

### Running a Manual Backup:
```bash
./scripts/production/backup-postgres.sh
```

---

## 2. Disaster Recovery & Restore Procedure

Restores are executed via `scripts/production/restore-postgres.sh`.

### 2.1 Safety Interlocks
To prevent catastrophic accidental overwrite of live production databases, the restore script enforces a safety gate:
- If the target database is named `yangiariq_aqua` or `aquaai`, the script will refuse execution unless `--force-production-overwrite` is passed explicitly.
- For disaster recovery testing, restores should always be performed into an isolated test database (e.g., `aqua_restore_test`).

### 2.2 Restore Execution Syntax
```bash
# Test restore into isolated database:
./scripts/production/restore-postgres.sh \
  ./backups/postgres/aquaai_yangiariq_aqua_20261109_031843Z.sql.gz \
  aqua_restore_test

# Production emergency disaster recovery:
./scripts/production/restore-postgres.sh \
  ./backups/postgres/aquaai_yangiariq_aqua_20261109_031843Z.sql.gz \
  aquaai \
  aqua-prod-postgres \
  aqua_admin \
  --force-production-overwrite
```

### 2.3 Automated Post-Restore Verification
The restore script automatically validates:
1. **Public Relation Count:** Confirms at least 50+ relational tables restored.
2. **Liquibase State:** Verifies `databasechangelog` contains all historic changesets.
3. **Critical Domain Tables:** Confirms presence and schema integrity of `users`, `fields`, `water_allocations`, `performance_indicators`, and `impact_assessments`.

---

## 3. Verified Disaster Recovery Test Results

On **2026-11-09**, an automated end-to-end backup and restore test was conducted on host infrastructure:

| Verification Metric | Value Observed | Status |
| :--- | :--- | :--- |
| **Source Database** | `yangiariq_aqua` | Verified |
| **Backup Archive** | `aquaai_yangiariq_aqua_20261109_031843Z.sql.gz` (108 KB) | **PASSED** |
| **Gzip Integrity Check (`gzip -t`)** | Zero defects | **PASSED** |
| **SHA256 Checksum** | Generated successfully | **PASSED** |
| **Isolated Target DB** | `aqua_restore_test` | Created |
| **Restored Tables Count** | **51 tables** | **PASSED** |
| **Restored Liquibase Changesets** | **42 changesets** | **PASSED** |
| **Critical Table Invariants** | All 5 critical tables verified | **PASSED** |
| **Cleanup** | `aqua_restore_test` dropped safely | **PASSED** |

---

## 4. Liquibase Migration Safety & Forward-Fix Policy

The YANGIARIQ AQUA AI platform relies on Liquibase for deterministic, versioned database schema evolution (currently migration changesets 001 through 025).

### Operational Rules for Production Migrations:
1. **Automatic Execution on Boot:** Spring Boot automatically executes pending changesets during container startup via `spring.liquibase.enabled: true`.
2. **Forward-Fix Only Policy:**
   - In production relational databases holding active water allocation and accounting ledgers, automated rollback of destructive migrations (e.g. dropping columns, altering constraints) can cause irreversible data loss.
   - If a migration contains a defect or performance regression, the team must apply a **forward-fix changeset** (e.g. changeset `026-fix-...`) rather than attempting automated database rollbacks.
3. **Lock Release Protocol:**
   - If a deployment aborts unexpectedly while Liquibase holds the migration lock, check `DATABASECHANGELOGLOCK`.
   - Release only after confirming no other container is executing migrations:
     ```sql
     UPDATE databasechangeloglock SET locked = false, lockedby = NULL, lockgranted = NULL WHERE id = 1;
     ```

---

## 5. Off-Server Backup Storage Recommendations

Local disk backups protect against software errors and accidental table drops, but do NOT protect against physical host failure or data center outages.

**Recommended Off-Server Sync Architecture:**
- Use `rclone` or AWS CLI to sync `./backups/postgres` to an encrypted, off-site S3-compatible bucket (e.g., Cloudflare R2, Backblaze B2, or private S3):
  ```bash
  # Daily sync cron task:
  rclone sync /opt/yangiariq-aqua/backups/postgres remote:aquaai-backups-offsite/postgres/
  ```
- Retain daily backups for 30 days, weekly backups for 12 weeks, and monthly backups for 1 year off-site.
