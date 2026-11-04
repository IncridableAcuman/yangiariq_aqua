# Model Governance & Controlled Calibration Architecture

## 1. Overview & Objective

In the YANGIARIQ AQUA AI platform, **Milestone 08.9** advances the system from passive observation to disciplined, auditable governance:

$$\text{Insight} \longrightarrow \text{Calibration Proposal} \longrightarrow \text{Historical Backtest} \longrightarrow \text{Human Review} \longrightarrow \text{Version} \longrightarrow \text{Controlled Rollout} \longrightarrow \text{Monitoring} \longrightarrow \text{Rollback}$$

The platform transitions from merely observing that *"prediction performance is degrading"* to:
> *"We detected degradation (+11.2% bias over 42 predictions), prepared an agronomic calibration proposal, validated it deterministically against historical ground truth without future data leakage, verified regression guards, and safely presented it to an authorized human administrator for approval."*

---

## 2. Critical Safety Principles & Boundaries

### 2.1 Zero Autonomous Self-Modification
Under no circumstances does the system alter production prediction formulas, crop coefficients ($K_c$), evapotranspiration parameters ($ET_0$), or thresholds autonomously. 
- The system **may**: detect degradation, generate proposals, run backtests, compute candidate improvements, and flag regressions.
- The system **must NOT**: execute automatic database updates of configuration parameters or activate candidates without explicit human administrator approval (`ROLE_ADMIN`).

### 2.2 Hard Safety Constraints vs. Soft Agronomic Parameters
The governance domain enforces a strict partition between what can be proposed for calibration and what is permanently immutable:

| Parameter Category | Examples | Governance Status | Calibratable? |
| :--- | :--- | :--- | :--- |
| **Hard Hydraulic Constraints** | `pipeBurstPressureBar`, `emergencyPressureThreshold`, `hydraulicSafetyInterlock`, `emergencyStop`, `safetyBlock`, `actuatorCommandForce`, `valveOverride`, `pumpOverride` | **HARD CONSTRAINT** | **STRICTLY FORBIDDEN**. Rejection enforced at domain model, use case, and API boundaries. |
| **Soft Agronomic Parameters** | `trendWindowHours`, `smoothingAlpha`, `kcReference`, `applicationEfficiency`, `leachingFraction`, `packetLossWarningThreshold` | **SOFT PARAMETER** | **PERMITTED** via Proposal $\to$ Backtest $\to$ Human Review $\to$ Controlled Rollout. |

Any configuration attempting to include hard safety keywords is immediately rejected with an `IllegalArgumentException` (HTTP 400 Bad Request).

---

## 3. Governance Domain Model

The governance domain lives within `com.aquaai.prediction.domain.model` and `com.aquaai.prediction.domain.port`, fully isolated from UI, persistence, or external ML SDKs:

```
+-----------------------------------------------------------------------------------+
| com.aquaai.prediction.domain.model                                                |
|                                                                                   |
|  +-------------------------+            +-------------------------------------+   |
|  | PredictionMethodVersion |            |         CalibrationProposal         |   |
|  +-------------------------+            +-------------------------------------+   |
|  | versionTag: String      | 1        * | proposalCode: String                |   |
|  | status: VersionStatus   |<-----------| currentVersionTag: String           |   |
|  | configurationJson: JSON |            | candidateVersionTag: String         |   |
|  | isImmutable: boolean    |            | status: ProposalStatus              |   |
|  | activatedBy: String     |            | riskLevel: GovernanceRiskLevel      |   |
|  +-------------------------+            | affectedScope: String               |   |
|                                         +-------------------------------------+   |
|                                                            | 1                    |
|                                                            |                      |
|                                                            | *                    |
|  +-------------------------+            +-------------------------------------+   |
|  |   GovernanceAuditLog    |            |             BacktestRun             |   |
|  +-------------------------+            +-------------------------------------+   |
|  | action: GovernanceAction|            | sampleSize: int                     |   |
|  | actorId: String         |            | maeImprovementPercent: BigDecimal   |   |
|  | actorRole: String       |            | regressionDetected: boolean         |   |
|  | previousState / newState|            | isComparable: boolean               |   |
|  +-------------------------+            +-------------------------------------+   |
+-----------------------------------------------------------------------------------+
```

---

## 4. Lifecycle & Governance State Transitions

### 4.1 Calibration Proposal Lifecycle
1. **`DRAFT`**: Created by drift detection or operator; candidate version created in `CANDIDATE` status.
2. **`BACKTESTING`**: Historical anti-leakage backtest is running.
3. **`REVIEW_REQUIRED`**: Backtest complete. Metrics and regression guards available for inspection.
4. **`APPROVED`**: Administrator explicitly approved proposal with justification notes.
5. **`REJECTED`**: Administrator rejected proposal with mandatory rejection reason.

### 4.2 Prediction Method Version Lifecycle
$$\text{DRAFT} \longrightarrow \text{CANDIDATE} \longrightarrow \text{APPROVED} \longrightarrow \text{SHADOW} \longrightarrow \text{CANARY} \longrightarrow \text{ACTIVE} \longrightarrow \text{DEPRECATED} \longrightarrow \text{RETIRED}$$

- Once a version transitions to `ACTIVE`, its configuration is stamped **immutable** (`isImmutable = true`).
- Any attempt to mutate configuration on an immutable version throws `IllegalStateException`.
- Reproducibility guarantee: Historical predictions always reference their originating version tag.

---

## 5. Security & Role-Based Access Control

- **Farmers & Field Operators**: Read-only access to active model health overview.
- **Water Management Operators**: Can inspect model versions and proposals; cannot approve or activate.
- **System Administrators (`ROLE_ADMIN`)**: Exclusive authority to:
  - Create manual candidate calibration proposals (`POST /proposals`).
  - Run backtests (`POST /proposals/{id}/backtest`).
  - Approve proposals (`POST /proposals/{id}/approve`).
  - Reject proposals (`POST /proposals/{id}/reject`).
  - Activate versions into `SHADOW`, `CANARY`, or `ACTIVE` (`POST /versions/{id}/activate`).
  - Trigger 1-click emergency rollback (`POST /versions/{id}/rollback` or `POST /versions/rollback`).

All actions write immutable records into the `governance_audit_logs` table.

---

## 6. Milestone 08.11: Hybrid ML Model Governance & Reproducibility

Milestone 08.11 extends the model governance framework to encompass machine learning candidates alongside deterministic versions:
1. **Governed Candidate Lifecycle**:
   $$\text{TRAINING\_RUN} \longrightarrow \text{DATASET\_SNAPSHOT} \longrightarrow \text{CANDIDATE} \longrightarrow \text{APPROVED} \longrightarrow \text{SHADOW} \longrightarrow \text{CANARY} \longrightarrow \text{ACTIVE} \longrightarrow \text{RETIRED}$$
2. **Deterministic Baseline Primacy**:
   Every ML candidate is trained and evaluated in parallel with the deterministic baseline on an unseen test split.
3. **Automated Regression Guards**:
   If test $\text{MAE}_{\text{ML}} > \text{MAE}_{\text{baseline}}$, the model candidate is automatically flagged as `regressionDetected = true`, and promotion to production is blocked.
4. **Reproducible Dataset Hashes**:
   Every candidate references an immutable `PredictionTrainingDataset` with a canonical SHA-256 hash.

