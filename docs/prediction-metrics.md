# Prediction Evaluation Metrics & Statistical Formulation (Milestone 08.8)

## 1. Overview & Objective

To prevent false confidence and deliver actionable continuous intelligence, the **YANGIARIQ AQUA AI** platform strictly separates:
1. **Numerical Regression Error**: Applied to continuous agronomical variables (soil moisture %, water demand $\text{m}^3$).
2. **Categorical Classification**: Applied to discrete and event-based decisions (irrigation need, sensor failure, safety blocks).

All calculations are executed in `PredictionEvaluationAggregator` using exact arbitrary-precision arithmetic (`BigDecimal`) to prevent IEEE-754 floating-point inaccuracies.

---

## 2. Numerical Regression Error Formulations

Given $n$ evaluated predictions where $\hat{y}_i$ is the predicted value and $y_i$ is the authoritative observed value:

### 2.1 Mean Absolute Error (MAE)
Measures the average magnitude of prediction errors in the original physical units:
$$\text{MAE} = \frac{1}{n} \sum_{i=1}^{n} |\hat{y}_i - y_i|$$
- **Soil Moisture Target**: $\le 3.5\%$ moisture deviation.
- **Water Demand Target**: $\le 10\%$ of parcel baseline quota.

### 2.2 Root Mean Squared Error (RMSE)
Penalizes large outlier forecast errors more heavily than small consistent discrepancies:
$$\text{RMSE} = \sqrt{\frac{1}{n} \sum_{i=1}^{n} (\hat{y}_i - y_i)^2}$$

### 2.3 Safe Mean Absolute Percentage Error (MAPE)
$$\text{MAPE} = \frac{100\%}{k} \sum_{i=1}^{k} \frac{|\hat{y}_i - y_i|}{|y_i|}$$

> [!WARNING]
> **Zero-Division Handling**: If any actual outcome $y_i \approx 0$ (e.g. zero water consumed during rain), standard MAPE divides by zero and explodes towards infinity.
> The system tracks `mapeReliable`:
> - If zero or near-zero observations exist, MAPE is marked `mapeReliable = false`.
> - The UI honestly renders `N/A (actuals near zero)` instead of misleading percentages.

### 2.4 Mean Bias Error (MBE)
Tracks persistent directional model deviation:
$$\text{MBE} = \frac{1}{n} \sum_{i=1}^{n} (\hat{y}_i - y_i)$$
- $\mathbf{MBE > +8.0}$: **Systematic Over-Prediction**. The model consistently overestimates moisture or demand, risking over-irrigation and unnecessary canal allocation requests.
- $\mathbf{MBE < -8.0}$: **Systematic Under-Prediction**. The model consistently underestimates water demand, risking crop water stress in Khorezm summer heat.
- $\mathbf{-2.0 \le MBE \le +2.0}$: **Balanced Error Distribution**.

### 2.5 Tolerance Interval Hit Rate
Evaluates empirical coverage of the prediction uncertainty bounds $[L_i, U_i]$:
$$\text{WithinBoundsRate} = \frac{1}{n} \sum_{i=1}^{n} \mathbb{I}(L_i \le y_i \le U_i) \times 100\%$$
- **Target**: $\ge 85\%$ of observations should reside within the calibrated uncertainty bounds.

---

## 3. Categorical Classification Metrics & Confusion Matrix

For binary forecasts (e.g., Irrigation Need Forecast: "Is irrigation required within 48 hours?"):

| | Actual Irrigation Executed | No Irrigation Executed | Interrupted by Hydraulic Safety |
|---|---|---|---|
| **Predicted Irrigation** | **True Positive ($TP$)** | **False Positive ($FP$)** | **Outcome Interrupted ($INT$)** |
| **Predicted No Irrigation** | **False Negative ($FN$)** | **True Negative ($TN$)** | **True Negative ($TN$)** |

### 3.1 Metric Formulations
- **Accuracy**:
  $$\text{Accuracy} = \frac{TP + TN}{TP + TN + FP + FN} \times 100\%$$
- **Precision**:
  $$\text{Precision} = \frac{TP}{TP + FP} \times 100\%$$
- **Recall (Sensitivity)**:
  $$\text{Recall} = \frac{TP}{TP + FN} \times 100\%$$
- **$F_1$ Score**:
  $$F_1 = 2 \times \frac{\text{Precision} \times \text{Recall}}{\text{Precision} + \text{Recall}}$$

---

## 4. Confidence Tier Calibration Verification

Predictions carry confidence ratings grouped into 3 operational tiers:
- **`HIGH`**: Confidence $\ge 80\%$
- **`MEDIUM`**: $60\% \le \text{Confidence} < 80\%$
- **`LOW`**: $\text{Confidence} < 60\%$

### 4.1 Calibration Integrity Rule
A well-calibrated machine learning or heuristic model must exhibit monotonic error reduction with increasing confidence:
$$\text{MAE}_{\text{HIGH}} < \text{MAE}_{\text{MEDIUM}} < \text{MAE}_{\text{LOW}}$$
$$\text{Accuracy}_{\text{HIGH}} > \text{Accuracy}_{\text{MEDIUM}} > \text{Accuracy}_{\text{LOW}}$$

If empirical evaluation discovers that $\text{MAE}_{\text{HIGH}} > \text{MAE}_{\text{LOW}}$, the `EvaluationInsightGenerator` fires a `CALIBRATION` inversion alert, notifying the agronomy engineering team that the confidence scoring function is overconfident and requires recalibration.
