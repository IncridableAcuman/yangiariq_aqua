#!/usr/bin/env bash
# ==============================================================================
# YANGIARIQ AQUA AI - Automated Production Deployment Script
# Milestone 08.18: Production Deployment Engineering
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
cd "${ROOT_DIR}"

ENV_FILE="${ENV_FILE:-.env.prod}"
IMAGE_TAG="${IMAGE_TAG:-0.8.18}"
SKIP_BACKUP=false

for arg in "$@"; do
    case "$arg" in
        --skip-backup)
            SKIP_BACKUP=true
            shift
            ;;
        --tag=*)
            IMAGE_TAG="${arg#*=}"
            shift
            ;;
    esac
done

log() {
    echo "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [INFO] $*"
}

log_error() {
    echo "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [ERROR] $*" >&2
}

log "=================================================================="
log "Initiating Production Deployment for YANGIARIQ AQUA AI"
log "Target Tag: ${IMAGE_TAG}"
log "=================================================================="

# 1. Run Preflight Checks
log "Running preflight validation..."
if ! "${SCRIPT_DIR}/preflight-check.sh" "${ENV_FILE}"; then
    log_error "Deployment halted: Preflight validation failed."
    exit 1
fi

# 2. Automated Safety Backup
if [ "${SKIP_BACKUP}" = true ]; then
    log "Skipping pre-deployment database backup (--skip-backup specified)."
else
    log "Creating pre-deployment database backup..."
    if docker compose -f docker-compose.prod.yml ps postgres --format '{{.State}}' 2>/dev/null | grep -q "running"; then
        "${SCRIPT_DIR}/backup-postgres.sh"
    else
        log "PostgreSQL container is not yet running (initial deployment). Skipping backup."
    fi
fi

# 3. Record Version in Deployment History for Rollback
mkdir -p "${ROOT_DIR}/.deployments"
HISTORY_FILE="${ROOT_DIR}/.deployments/history.log"
CURRENT_DEPLOYED_TAG=$(tail -n 1 "${HISTORY_FILE}" 2>/dev/null | cut -d',' -f2 || echo "none")

# 4. Deploy Services via Docker Compose
log "Deploying production stack with IMAGE_TAG=${IMAGE_TAG}..."
export IMAGE_TAG
docker compose --env-file "${ENV_FILE}" -f docker-compose.prod.yml up -d --remove-orphans

# 5. Wait for Container Readiness
log "Waiting for services to become healthy..."
MAX_WAIT=120
ELAPSED=0
HEALTHY=false

while [ ${ELAPSED} -lt ${MAX_WAIT} ]; do
    if "${SCRIPT_DIR}/health-check.sh" >/dev/null 2>&1; then
        HEALTHY=true
        break
    fi
    sleep 5
    ELAPSED=$((ELAPSED + 5))
    echo -n "."
done
echo ""

if [ "${HEALTHY}" != true ]; then
    log_error "Deployment failed: Services did not achieve healthy state within ${MAX_WAIT} seconds!"
    log_error "To roll back to previous version '${CURRENT_DEPLOYED_TAG}', run: ${SCRIPT_DIR}/rollback.sh"
    exit 1
fi

# 6. Final Health Verification
log "Running final post-deployment health verification..."
"${SCRIPT_DIR}/health-check.sh"

# 7. Record Successful Deployment
DEPLOY_TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
COMMIT_SHA=$(git rev-parse HEAD 2>/dev/null || echo "unknown")
echo "${DEPLOY_TIMESTAMP},${IMAGE_TAG},${COMMIT_SHA},SUCCESS" >> "${HISTORY_FILE}"

log "=================================================================="
log "DEPLOYMENT SUCCESSFUL!"
log "Active Version: ${IMAGE_TAG} (Commit: ${COMMIT_SHA})"
log "Timestamp: ${DEPLOY_TIMESTAMP}"
log "=================================================================="

exit 0
