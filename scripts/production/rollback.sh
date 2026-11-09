#!/usr/bin/env bash
# ==============================================================================
# YANGIARIQ AQUA AI - Automated Production Rollback Script
# Milestone 08.18: Production Deployment Engineering
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
cd "${ROOT_DIR}"

ENV_FILE="${ENV_FILE:-.env.prod}"
HISTORY_FILE="${ROOT_DIR}/.deployments/history.log"

log() {
    echo "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [INFO] $*"
}

log_error() {
    echo "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [ERROR] $*" >&2
}

log "=================================================================="
log "Initiating Production Rollback Procedure"
log "=================================================================="

TARGET_TAG=""

if [ $# -ge 1 ]; then
    TARGET_TAG="$1"
elif [ -f "${HISTORY_FILE}" ]; then
    # Pick the penultimate successfully deployed tag
    TARGET_TAG=$(tail -n 2 "${HISTORY_FILE}" | head -n 1 | cut -d',' -f2 || echo "")
fi

if [ -z "${TARGET_TAG}" ] || [ "${TARGET_TAG}" == "none" ]; then
    log_error "No previous deployment version found in history. Specify a tag explicitly: $0 <image-tag>"
    exit 1
fi

log "Rolling back application stack to version: ${TARGET_TAG}..."

# Execute Rollback via Docker Compose
export IMAGE_TAG="${TARGET_TAG}"
docker compose --env-file "${ENV_FILE}" -f docker-compose.prod.yml up -d --remove-orphans

log "Waiting for services to recover under rolled-back version..."
sleep 15

if "${SCRIPT_DIR}/health-check.sh"; then
    ROLLBACK_TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    echo "${ROLLBACK_TIMESTAMP},${TARGET_TAG},ROLLBACK,SUCCESS" >> "${HISTORY_FILE}"
    
    log "=================================================================="
    log "ROLLBACK COMPLETED SUCCESSFULLY."
    log "Active Version: ${TARGET_TAG}"
    log "IMPORTANT NOTICE: Database migrations applied by Liquibase are forward-fix"
    log "by design. If database state requires restoration to a prior point in time,"
    log "use '${SCRIPT_DIR}/restore-postgres.sh' with a verified backup archive."
    log "=================================================================="
    exit 0
else
    log_error "Rollback health check failed! Inspect logs: docker compose -f docker-compose.prod.yml logs"
    exit 1
fi
