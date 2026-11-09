#!/usr/bin/env bash
# ==============================================================================
# YANGIARIQ AQUA AI - Production Preflight Validation Script
# Milestone 08.18: Production Readiness & Reliability
# ==============================================================================

set -euo pipefail

ENV_FILE="${1:-.env.prod}"
FAILURES=0
WARNINGS=0

pass() {
    echo -e "  [PASS] $*"
}

warn() {
    echo -e "  [WARN] $*"
    WARNINGS=$((WARNINGS + 1))
}

fail() {
    echo -e "  [FAIL] $*" >&2
    FAILURES=$((FAILURES + 1))
}

echo "=================================================================="
echo "YANGIARIQ AQUA AI — Production Preflight Validation"
echo "=================================================================="

# 1. Host Runtime & Tooling
echo -e "\n1. Checking Tooling & Prerequisites..."
if command -v docker >/dev/null 2>&1; then
    DOCKER_VER=$(docker --version | cut -d' ' -f3 | tr -d ',')
    pass "Docker is installed (version ${DOCKER_VER})"
else
    fail "Docker is NOT installed."
fi

if docker compose version >/dev/null 2>&1; then
    COMPOSE_VER=$(docker compose version --short)
    pass "Docker Compose is installed (version ${COMPOSE_VER})"
else
    fail "Docker Compose (v2+) is NOT installed."
fi

# Check Docker daemon availability
if docker ps >/dev/null 2>&1; then
    pass "Docker daemon is reachable and responding"
else
    warn "Docker daemon is not reachable without elevated privileges or not running."
fi

# 2. Environment Configuration File
echo -e "\n2. Validating Environment Configuration (${ENV_FILE})..."
if [ ! -f "${ENV_FILE}" ]; then
    fail "Environment configuration file '${ENV_FILE}' not found. Copy .env.prod.example to '${ENV_FILE}'."
else
    pass "Environment file '${ENV_FILE}' exists"
    
    # Load environment variables without exporting to shell sub-processes
    set -a
    # shellcheck disable=SC1090
    source "${ENV_FILE}"
    set +a

    # Check Database Variables
    if [ -z "${DB_NAME:-}" ]; then fail "DB_NAME is not set."; else pass "DB_NAME: ${DB_NAME}"; fi
    if [ -z "${DB_USERNAME:-}" ]; then fail "DB_USERNAME is not set."; else pass "DB_USERNAME is configured"; fi
    if [ -z "${DB_PASSWORD:-}" ]; then
        fail "DB_PASSWORD is missing or empty."
    elif [ "${DB_PASSWORD}" == "aqua_secure_password_dev" ] || [ "${DB_PASSWORD}" == "REPLACE_WITH_STRONG_GENERATED_PRODUCTION_PASSWORD" ]; then
        fail "Insecure default/placeholder DB_PASSWORD detected."
    else
        pass "DB_PASSWORD is set and non-default"
    fi

    # Check JWT Variables
    DEV_JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D635166546A576E5A7234753778214125442A"
    if [ -z "${JWT_SECRET:-}" ]; then
        fail "JWT_SECRET is missing or empty."
    elif [ "${JWT_SECRET}" == "${DEV_JWT_SECRET}" ] || [ "${JWT_SECRET}" == "REPLACE_WITH_CRYPTO_SECURE_64_BYTE_BASE64_SECRET" ]; then
        fail "Insecure default/placeholder JWT_SECRET detected."
    elif [ ${#JWT_SECRET} -lt 32 ]; then
        fail "JWT_SECRET is too short (< 32 characters)."
    else
        pass "JWT_SECRET is cryptographically valid (length ${#JWT_SECRET})"
    fi

    # Check CORS
    if [ -z "${CORS_ALLOWED_ORIGINS:-}" ]; then
        fail "CORS_ALLOWED_ORIGINS is not set."
    elif [[ "${CORS_ALLOWED_ORIGINS}" =~ localhost|127\.0\.0\.1 ]]; then
        fail "CORS_ALLOWED_ORIGINS contains localhost/127.0.0.1 in production."
    else
        pass "CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS}"
    fi

    # Check Domain Name
    if [ -z "${DOMAIN_NAME:-}" ]; then
        warn "DOMAIN_NAME is not set. TLS configuration will require explicit domain."
    else
        pass "DOMAIN_NAME: ${DOMAIN_NAME}"
    fi
fi

# 3. System Resources
echo -e "\n3. Checking System Resources..."
AVAILABLE_MEM_MB=$(free -m | awk '/^Mem:/{print $7}')
if [ "${AVAILABLE_MEM_MB}" -lt 1024 ]; then
    warn "Available system memory is low: ${AVAILABLE_MEM_MB}MB (recommended >= 2048MB)"
else
    pass "Available memory: ${AVAILABLE_MEM_MB}MB"
fi

AVAILABLE_DISK_GB=$(df -BG . | awk 'NR==2 {print $4}' | tr -d 'G')
if [ "${AVAILABLE_DISK_GB}" -lt 2 ]; then
    fail "Free disk space is critically low: ${AVAILABLE_DISK_GB}GB (minimum 2GB required)"
else
    pass "Free disk space: ${AVAILABLE_DISK_GB}GB"
fi

# 4. Port Availability
echo -e "\n4. Checking Port Availability..."
for port in 80 443; do
    if command -v ss >/dev/null 2>&1; then
        if ss -tuln | grep -q ":${port} "; then
            warn "Port ${port} is currently bound by another service on host."
        else
            pass "Port ${port} is available"
        fi
    elif command -v netstat >/dev/null 2>&1; then
        if netstat -tuln | grep -q ":${port} "; then
            warn "Port ${port} is currently bound by another service on host."
        else
            pass "Port ${port} is available"
        fi
    fi
done

# 5. Docker Compose Config Validation
echo -e "\n5. Validating Docker Compose Production Configuration..."
if docker compose -f docker-compose.prod.yml config >/dev/null 2>&1; then
    pass "docker-compose.prod.yml configuration syntax is valid"
else
    fail "docker-compose.prod.yml validation failed. Run 'docker compose -f docker-compose.prod.yml config' for details."
fi

echo -e "\n=================================================================="
if [ ${FAILURES} -gt 0 ]; then
    echo -e "Preflight Check FAILED: ${FAILURES} critical issue(s) detected, ${WARNINGS} warning(s)."
    echo "Resolve all failures before deploying."
    exit 1
else
    echo -e "Preflight Check PASSED (${WARNINGS} warning(s)). Platform is ready for deployment."
    exit 0
fi
