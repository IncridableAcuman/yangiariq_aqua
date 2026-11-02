# Prediction Data Quality & Sufficiency (Milestone 08.7)

## 1. The Principle of Explicit Uncertainty

The YANGIARIQ AQUA AI platform adheres to a zero-fabrication principle:
> **The system shall never invent telemetry, hallucinate optimistic projections, or guess predictions when physical evidence is lacking.**

When telemetry is absent or degraded, the system:
1. Explains why data is insufficient.
2. Emits explicit `UNKNOWN` or `INSUFFICIENT` states.
3. Suggests operational remediation (e.g. inspecting physical telemetry nodes or manual soil probing).

---

## 2. Data Sufficiency Evaluation Matrix

Before evaluating any forecast model, the domain executes `DataSufficiencyEvaluator`:

| Level | Criteria | System Behavior |
| :--- | :--- | :--- |
| `SUFFICIENT` | $\ge 6$ readings in last 48 hours; latest reading $\le 12$ hours old; sensor status `ONLINE`. | Full statistical forecast enabled with narrow uncertainty bounds ($\pm 2.5\%$). |
| `LIMITED` | $3$ to $5$ readings in last 48 hours; latest reading $12$ to $24$ hours old; or battery low. | Forecast calculated with expanded uncertainty bounds ($\pm 5.0\%$) and reduced confidence penalty. |
| `INSUFFICIENT` | $< 3$ readings in 48 hours; latest reading $> 24$ hours old; or sensor `OFFLINE`. | **No numerical prediction generated**. Status is set to `predictedValue = null`, `riskLevel = UNKNOWN`, confidence $< 30\%$. |

---

## 3. Dynamic Confidence Scoring Formula

Confidence score $C \in [0, 100]\%$ is calculated deterministically:
$$C = \text{round}\left(C_{\text{base}} \times f_{\text{age}} \times f_{\text{count}} \times f_{\text{horizon}} \times f_{\text{anomaly}}\right)$$

Where:
- $C_{\text{base}} = 95\%$ for calibrated digital soil probes.
- **Freshness Factor $f_{\text{age}}$**:
  - Age $\le 3$ hours: $1.0$
  - Age $3$ to $12$ hours: $0.90$
  - Age $12$ to $24$ hours: $0.70$
  - Age $> 24$ hours: $0.30$
- **Sample Density Factor $f_{\text{count}}$**:
  - $N \ge 12$ readings: $1.0$
  - $6 \le N < 12$: $0.90$
  - $3 \le N < 6$: $0.75$
  - $N < 3$: $0.30$
- **Horizon Decay Factor $f_{\text{horizon}}$**:
  - `NEXT_24_HOURS`: $1.0$
  - `NEXT_48_HOURS`: $0.90$
  - `NEXT_7_DAYS`: $0.75$
- **Anomaly Factor $f_{\text{anomaly}}$**:
  - No active anomalies: $1.0$
  - Active telemetry anomaly: $0.60$

---

## 4. UI Representation of Uncertainty

1. **Badges**:
   - `DataSufficiencyBadge` displays green (`To'liq ma'lumot` / `Достаточно`), yellow (`Cheklangan` / `Ограничено`), or red (`Yetarli emas` / `Недостаточно`).
2. **Fallback Cards**:
   - If a forecast cannot be made due to data starvation, the card displays an informative prompt with an amber alert explaining which sensor has gone stale.
3. **No Synthetic Model Accuracy**:
   - Calibration history metrics (`overallAccuracyPercent`) only render if at least 5 forecasts have been verified against ground truth. Otherwise, the UI displays a clear explanation:
   *"Statistical calibration requires at least 5 completed and verified forecast cycles."*
