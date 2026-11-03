# Controlled Rollout & Emergency Rollback Architecture

## 1. Controlled Rollout Workflow

To prevent disruption to real-world irrigation scheduling and water allocation, new predictive versions transition through a multi-stage gated rollout:

```
+-----------+       +-------------------+       +---------------+
|  CANDIDATE| ----> |     APPROVED      | ----> |    SHADOW     |
| (Backtest)|       |  (Human Sign-off) |       | (Live Replay) |
+-----------+       +-------------------+       +---------------+
                                                        |
                                                        v
+-----------+       +-------------------+       +---------------+
|  ACTIVE   | <---- |      CANARY       | <---- |    CANARY     |
| (100% Ops)|       |  (Target Parcels) |       | (Evaluation)  |
+-----------+       +-------------------+       +---------------+
```

### Stage Definitions
1. **`CANDIDATE`**: Method version created with proposed soft agronomic configurations; evaluated exclusively in backtesting sandbox.
2. **`APPROVED`**: Administrator reviewed candidate vs. baseline metrics and authorized staging.
3. **`SHADOW`**: The candidate version runs concurrently alongside the active baseline version on incoming telemetry. It produces predictions and evaluations for comparison, but **does NOT** feed into irrigation schedules or AI recommendations.
4. **`CANARY`**: The candidate version is enabled for a controlled subset of non-critical fields (e.g., test parcels or specific crop varieties).
5. **`ACTIVE`**: Full deployment across the Yangiariq district. The previous version transitions to `DEPRECATED`.

---

## 2. Emergency 1-Click Rollback

### 2.1 Operational Context
If an active model exhibits unexpected drift, sensor incompatibilities, or operator dissatisfaction under field conditions, immediate recovery is guaranteed.

### 2.2 Rollback Endpoints
The platform supports two equivalent administrative endpoints:
- `POST /api/v1/predictions/governance/versions/{versionId}/rollback`: Direct version-targeted rollback.
- `POST /api/v1/predictions/governance/versions/rollback?type={predictionType}`: Type-scoped rollback by specifying the target version tag.

### 2.3 Rollback Guarantees
- **Mandatory Justification**: Every rollback requires a non-blank reason (e.g., `"Excessive negative bias observed in loam parcels during high-temperature spell"`).
- **Zero Downtime**: Activation state flips atomically within a database transaction.
- **Audit Logging**: A `VERSION_ROLLBACK` event is immediately appended to `governance_audit_logs`.
- **Field Safety**: Hydraulic safety interlocks remain active and independent throughout the entire rollback event.
