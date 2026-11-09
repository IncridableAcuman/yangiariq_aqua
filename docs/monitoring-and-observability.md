# Monitoring, Observability & Structured Logging
## YANGIARIQ AQUA AI Platform — Milestone 08.18

This document details the health probe architecture, Prometheus metrics exposition, and structured production logging implemented for the YANGIARIQ AQUA AI platform.

---

## 1. Health Probe Architecture

Spring Boot Actuator is configured with dedicated liveness and readiness probe groups in `application-prod.yml`.

| Probe | Endpoint | Probed Components | Intended Consumer | Action on Failure |
| :--- | :--- | :--- | :--- | :--- |
| **Liveness** | `/actuator/health/liveness` | JVM runtime state, deadlock detection | Docker container healthcheck / K8s | Restarts the container |
| **Readiness** | `/actuator/health/readiness` | Database connection (`db`), connection pool | Orchestrator load balancer / NGINX | Pauses routing incoming traffic |

### Security Invariant:
In `application-prod.yml`, `management.endpoint.health.show-details: never` is strictly enforced. Unauthenticated public requests will never see database hostnames, schema version strings, disk capacities, or internal IP addresses.

---

## 2. Metrics & Observability (Prometheus / Micrometer)

With the inclusion of `io.micrometer:micrometer-registry-prometheus`, the backend exposes standard OpenMetrics on `/actuator/prometheus`.

### 2.1 Critical Operational Metrics Directory

| Metric Category | Metric Name | Description | Alerting Threshold |
| :--- | :--- | :--- | :--- |
| **JVM Memory** | `jvm_memory_used_bytes{area="heap"}` | Current JVM heap memory consumption | > 85% of max heap for 5 min |
| **Garbage Collection**| `jvm_gc_pause_seconds_max` | Longest GC pause in scrape window | > 2.5 seconds |
| **Database Pool** | `hikaricp_connections_active` | Active JDBC database connections | > 80% of `DB_POOL_MAX_SIZE` (16/20) |
| **Database Pending** | `hikaricp_connections_pending` | Threads blocked waiting for a connection | > 2 threads for > 10 seconds |
| **Database Acquire** | `hikaricp_connections_acquire_seconds`| Connection checkout latency from pool | 95th percentile > 500ms |
| **HTTP Throughput** | `http_server_requests_seconds_count` | Total request count per endpoint/method | Anomaly detection vs baseline |
| **HTTP Latency** | `http_server_requests_seconds_max` | Maximum HTTP request response time | 95th percentile > 1500ms |
| **HTTP Error Rate** | `http_server_requests_seconds_count{status=~"5.."}`| Internal server error count | > 1% of total requests |
| **Rate Limit Events**| `http_server_requests_seconds_count{status="429"}` | Rate limit throttle activations | Spike indicates brute force attempt |

### Prometheus Scrape Configuration (`docker/monitoring/prometheus.yml`):
```yaml
scrape_configs:
  - job_name: 'yangiariq-aqua-backend'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['backend:8080']
        labels:
          application: 'yangiariq-aqua-ai'
          environment: 'production'
```

---

## 3. Structured Production Logging

### 3.1 Format & Pattern
The backend uses SLF4J / Logback with correlation ID propagation:
```text
%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{correlationId}] %-5level %logger{36} - %msg%n
```

Example Log Entry:
```text
2026-11-09 08:06:23.973 [a7b2c9e4-18d2-4321-9876-abcdef012345] INFO  c.a.s.s.ProductionSecurityValidator - Production security invariants successfully verified.
```

### 3.2 Correlation ID Propagation
The `CorrelationIdFilter` (`X-Correlation-Id`) runs at highest precedence for every incoming HTTP request:
1. If the client or NGINX provides an `X-Correlation-Id` header, it is adopted.
2. If omitted, a cryptographically random UUID is generated.
3. The correlation ID is injected into SLF4J's `MDC` (Mapped Diagnostic Context) and returned in the HTTP response header `X-Correlation-Id`.
4. All logs emitted during the request automatically include the correlation ID.

### 3.3 Sensitive Data Protection Rules
- **Passwords & Hashes:** Passwords are never logged in debug, info, or error messages.
- **Tokens & Secrets:** Bearer access tokens, refresh tokens, and JWT secrets are stripped or excluded from log outputs.
- **Personal Data:** Personal identifying numbers, biometric data, and raw passwords are never passed to logging parameters.
- **Database Logs:** `show-sql: false` and `format_sql: false` are enforced in `application-prod.yml` to prevent query parameters from spilling into stdout.
