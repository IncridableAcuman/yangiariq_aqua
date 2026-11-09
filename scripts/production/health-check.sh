#!/usr/bin/env bash
# ==============================================================================
# YANGIARIQ AQUA AI - Production Health Verification Script
# Milestone 08.18: Production Readiness & Reliability
# ==============================================================================

set -euo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"
TIMEOUT_SECONDS=60
START_TIME=$(date +%s)

log() {
    echo "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [INFO] $*"
}

log_error() {
    echo "[$(date -u +"%Y-%m-%dT%H:%M:%SZ")] [ERROR] $*" >&2
}

log "Running production health check suite..."

# 1. Verify Docker Compose Services
log "Verifying service container states..."
SERVICES=("postgres" "backend" "frontend" "nginx")

for service in "${SERVICES[@]}"; do
    STATUS=$(docker compose -f "${COMPOSE_FILE}" ps "${service}" --format '{{.State}}' 2>/dev/null || echo "not-found")
    HEALTH=$(docker compose -f "${COMPOSE_FILE}" ps "${service}" --format '{{.Health}}' 2>/dev/null || echo "")
    
    if [ "${STATUS}" != "running" ]; then
        log_error "Service '${service}' is NOT running (State: ${STATUS})"
        exit 1
    fi

    if [ -n "${HEALTH}" ] && [ "${HEALTH}" != "healthy" ]; then
        log_error "Service '${service}' healthcheck is '${HEALTH}'"
        exit 1
    fi

    log "  - Service '${service}': RUNNING ${HEALTH:+(Health: ${HEALTH})}"
done

# 2. Probe Nginx Edge Health
log "Probing Nginx Edge Gateway..."
if docker exec aqua-prod-nginx wget -qO- http://localhost:80/healthz >/dev/null 2>&1; then
    log "  - Nginx Gateway (/healthz): OK"
else
    log_error "Nginx Gateway probe failed!"
    exit 1
fi

# 3. Probe Backend API Liveness and Readiness
log "Probing Backend API Probes..."
BACKEND_LIVENESS=$(docker exec aqua-prod-backend curl -s -f http://localhost:8080/actuator/health/liveness || echo "FAILED")
if echo "${BACKEND_LIVENESS}" | grep -q "UP"; then
    log "  - Backend Liveness (/actuator/health/liveness): UP"
else
    log_error "Backend Liveness failed: ${BACKEND_LIVENESS}"
    exit 1
fi

BACKEND_READINESS=$(docker exec aqua-prod-backend curl -s -f http://localhost:8080/actuator/health/readiness || echo "FAILED")
if echo "${BACKEND_READINESS}" | grep -q "UP"; then
    log "  - Backend Readiness (/actuator/health/readiness): UP"
else
    log_error "Backend Readiness failed: ${BACKEND_READINESS}"
    exit 1
fi

# 4. Probe Frontend Static SPA
log "Probing Frontend Static SPA..."
if docker exec aqua-prod-frontend wget -qO- http://localhost:80/index.html | grep -q "html"; then
    log "  - Frontend SPA (/index.html): OK"
else
    log_error "Frontend SPA probe failed!"
    exit 1
fi

log "ALL PRODUCTION HEALTH CHECKS PASSED SUCCESSFULLY."
exit 0
