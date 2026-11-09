#!/usr/bin/env bash
# ==============================================================================
# YANGIARIQ AQUA AI - PostgreSQL Automated Backup Script
# Milestone 08.18: Production Reliability & Disaster Recovery
# ==============================================================================

set -euo pipefail

# Configuration with environment defaults
DB_CONTAINER="${DB_CONTAINER:-aqua-prod-postgres}"
DB_NAME="${DB_NAME:-yangiariq_aqua}"
DB_USER="${DB_USER:-${DB_USERNAME:-aqua_user}}"
BACKUP_DIR="${BACKUP_DIR:-./backups/postgres}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"

TIMESTAMP=$(date -u +"%Y%m%d_%H%M%SZ")
BACKUP_FILE="${BACKUP_DIR}/aquaai_${DB_NAME}_${TIMESTAMP}.sql.gz"
CHECKSUM_FILE="${BACKUP_FILE}.sha256"

log() {
    echo "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [INFO] $*"
}

log_error() {
    echo "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [ERROR] $*" >&2
}

# 1. Validation & Preflight
if ! command -v docker >/dev/null 2>&1; then
    log_error "Docker is required but not installed."
    exit 1
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

mkdir -p "${BACKUP_DIR}"

log "Starting automated backup for database '${DB_NAME}' from container '${DB_CONTAINER}'..."

# 2. Execute pg_dump directly through container stream to avoid storing unencrypted plaintext
if ! docker exec "${DB_CONTAINER}" pg_dump -U "${DB_USER}" -d "${DB_NAME}" --clean --if-exists --no-owner --no-privileges | gzip -9 > "${BACKUP_FILE}"; then
    log_error "pg_dump failed."
    rm -f "${BACKUP_FILE}"
    exit 1
fi

# 3. Verify Backup File Integrity
if [ ! -s "${BACKUP_FILE}" ]; then
    log_error "Backup file was created but is empty: ${BACKUP_FILE}"
    rm -f "${BACKUP_FILE}"
    exit 1
fi

if ! gzip -t "${BACKUP_FILE}"; then
    log_error "Backup integrity check failed (corrupted gzip): ${BACKUP_FILE}"
    rm -f "${BACKUP_FILE}"
    exit 1
fi

# 4. Generate SHA256 Checksum
if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "${BACKUP_FILE}" > "${CHECKSUM_FILE}"
fi

BACKUP_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
log "Backup successfully created and verified: ${BACKUP_FILE} (Size: ${BACKUP_SIZE})"

# 5. Retention Management
log "Applying backup retention policy (${RETENTION_DAYS} days)..."
find "${BACKUP_DIR}" -name "aquaai_${DB_NAME}_*.sql.gz" -type f -mtime +"${RETENTION_DAYS}" -delete || true
find "${BACKUP_DIR}" -name "aquaai_${DB_NAME}_*.sql.gz.sha256" -type f -mtime +"${RETENTION_DAYS}" -delete || true

log "Backup completed successfully."
exit 0
