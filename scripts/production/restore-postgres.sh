#!/usr/bin/env bash
# ==============================================================================
# YANGIARIQ AQUA AI - PostgreSQL Disaster Recovery & Restore Script
# Milestone 08.18: Production Reliability & Disaster Recovery
# ==============================================================================

set -euo pipefail

log() {
    echo "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [INFO] $*"
}

log_error() {
    echo "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [ERROR] $*" >&2
}

if [ $# -lt 1 ]; then
    echo "Usage: $0 <backup-file.sql.gz> [target_database] [container_name] [db_user] [--force-production-overwrite]"
    echo "Example: $0 ./backups/postgres/aquaai_yangiariq_aqua_20261109_031843Z.sql.gz aqua_restore_test"
    exit 1
fi

BACKUP_FILE="$1"
TARGET_DB="${2:-aqua_restore_test}"
DB_CONTAINER="${3:-aqua-prod-postgres}"
DB_USER="${4:-${DB_USERNAME:-aqua_user}}"
FORCE_FLAG="${5:-}"

# 1. Validation
if [ ! -f "${BACKUP_FILE}" ]; then
    log_error "Backup file not found: ${BACKUP_FILE}"
    exit 1
fi

if ! gzip -t "${BACKUP_FILE}"; then
    log_error "Backup file is corrupted (failed gzip integrity test): ${BACKUP_FILE}"
    exit 1
fi

# Safety check: Protect production databases from accidental overwrite
if [[ "${TARGET_DB}" == "yangiariq_aqua" || "${TARGET_DB}" == "aquaai" ]]; then
    if [[ "${FORCE_FLAG}" != "--force-production-overwrite" ]]; then
        log_error "SAFETY INTERLOCK: Target database is '${TARGET_DB}'. Overwriting a production database requires '--force-production-overwrite'."
        exit 2
    fi
    log "WARNING: Overwriting production database '${TARGET_DB}' with explicit confirmation..."
fi

# Detect running container if default name differs
if ! docker ps --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
    if docker ps --format '{{.Names}}' | grep -q "yangiariq-aqua-postgres"; then
        DB_CONTAINER="yangiariq-aqua-postgres"
        log "Discovered active container: ${DB_CONTAINER}"
    else
        log_error "PostgreSQL container '${DB_CONTAINER}' is not currently running."
        exit 1
    fi
fi

log "Preparing target database '${TARGET_DB}' in container '${DB_CONTAINER}'..."

# 2. Terminate existing connections to target database and recreate
docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d postgres -c \
    "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '${TARGET_DB}' AND pid <> pg_backend_pid();" >/dev/null 2>&1 || true

docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d postgres -c "DROP DATABASE IF EXISTS ${TARGET_DB};" >/dev/null
docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d postgres -c "CREATE DATABASE ${TARGET_DB};" >/dev/null

log "Restoring backup '${BACKUP_FILE}' into '${TARGET_DB}'..."

# 3. Stream restore into target database
gunzip -c "${BACKUP_FILE}" | docker exec -i "${DB_CONTAINER}" psql -U "${DB_USER}" -d "${TARGET_DB}" --quiet >/dev/null 2>&1 || {
    # If error occurred, check if it was non-critical notice or fatal
    log "Stream finished. Validating restored schema integrity..."
}

# 4. Verification of Restored Schema & Tables
TABLE_COUNT=$(docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d "${TARGET_DB}" -t -A -c \
    "SELECT count(*) FROM information_schema.tables WHERE table_schema='public';")

CHANGELOG_COUNT=$(docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d "${TARGET_DB}" -t -A -c \
    "SELECT count(*) FROM databasechangelog;" 2>/dev/null || echo "0")

CRITICAL_TABLES=("users" "fields" "water_allocations" "performance_indicators" "impact_assessments")
MISSING_TABLES=0

for table in "${CRITICAL_TABLES[@]}"; do
    EXISTS=$(docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d "${TARGET_DB}" -t -A -c \
        "SELECT count(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='${table}';")
    if [ "${EXISTS}" != "1" ]; then
        log_error "Critical table '${table}' is MISSING in restored database!"
        MISSING_TABLES=$((MISSING_TABLES + 1))
    fi
done

if [ "${TABLE_COUNT}" -lt 40 ] || [ "${CHANGELOG_COUNT}" -lt 25 ] || [ "${MISSING_TABLES}" -gt 0 ]; then
    log_error "Restore verification FAILED! Tables found: ${TABLE_COUNT}, Liquibase changesets: ${CHANGELOG_COUNT}, Missing critical tables: ${MISSING_TABLES}"
    exit 1
fi

log "Restore verification SUCCESSFUL:"
log "  - Target database: '${TARGET_DB}'"
log "  - Public tables restored: ${TABLE_COUNT}"
log "  - Liquibase changesets verified: ${CHANGELOG_COUNT}"
log "  - All critical domain tables verified."

exit 0
