# Model Evidence, Verification & Governance

## 1. Dual-Track Comparison (Baseline vs ML)
A candidate model can never be evaluated in isolation. Every candidate must be compared side-by-side with the active deterministic baseline on the unseen **Test Split**:

- **MAE (Mean Absolute Error)**: Primary evaluation metric.
- **RMSE (Root Mean Squared Error)**: Penalizes large outliers.
- **MBE (Mean Bias Error)**: Identifies persistent over- or under-prediction.
- **Relative MAE Improvement (%)**:
  $$\Delta_{\text{MAE}} = \frac{\text{MAE}_{\text{baseline}} - \text{MAE}_{\text{ML}}}{\text{MAE}_{\text{baseline}}} \times 100\%$$

## 2. Regression Guard Gate
If $\text{MAE}_{\text{ML}} > \text{MAE}_{\text{baseline}}$ on the test split:
- `regressionDetected` is flagged as `true`.
- The candidate model is tagged `BLOCKED` for production promotion.
- Human governance operators are warned of negative predictive gain.

## 3. Human Governance & Rollout Progression
No model candidate ever activates autonomously. The transition lifecycle is strictly governed via RBAC:

```text
[CANDIDATE] (Produced by training run)
     ↓ (Admin / Operator manual review of evidence)
[APPROVED] (Approved by Agronomist / ML Architect)
     ↓ (Admin initiates shadow rollout)
[SHADOW] (Runs parallel inferences, zero actuator effect)
     ↓ (Admin canary deployment)
[CANARY] (Evaluated on small controlled field cohort)
     ↓ (Admin explicit activation)
[ACTIVE] (Production hybrid residual serving)
```

At any point, rollback to the deterministic baseline is instant and one-click.
