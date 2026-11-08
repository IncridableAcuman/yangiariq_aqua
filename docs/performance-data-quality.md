# Performance Data Quality and Telemetry Reliability

## 1. Principles of Data Trustworthiness

Performance evaluations are only as trustworthy as the underlying measurements. In agricultural regions with harsh desert climates, high salinity, and potential telemetry network dropouts, telemetry data quality is paramount.

The YANGIARIQ AQUA AI platform implements a multi-tier data quality framework in `PerformanceDataQuality`.

---

## 2. Telemetry Quality Metrics

| Metric | Code | Mathematical Definition | Minimum Quality Standard |
| :--- | :--- | :--- | :--- |
| **Observed Coverage Ratio** | `SDR_OBSERVED_COVERAGE` | $\frac{N_{\text{observed}}}{N_{\text{expected}}} \times 100.0$ | $\ge 85.0\%$ for district evaluations |
| **Simulated Data Proportion** | `SDR_SIMULATED_PROPORTION` | $\frac{N_{\text{simulated}}}{N_{\text{total}}} \times 100.0$ | $\le 15.0\%$ maximum tolerable |
| **Missing Telemetry Rate** | `SDR_MISSING_RATE` | $\frac{N_{\text{missing}}}{N_{\text{expected}}} \times 100.0$ | $\le 10.0\%$ |
| **Sensor Availability** | `SDR_SENSOR_AVAILABILITY` | $\frac{H_{\text{uptime}}}{H_{\text{total scheduled}}} \times 100.0$ | $\ge 90.0\%$ uptime across active posts |
| **Stale Measurement Rate** | `SDR_STALE_RATE` | $\frac{N_{\text{stale readings}}}{N_{\text{total readings}}} \times 100.0$ | $\le 5.0\%$ |

---

## 3. Visual & Analytical Distinction of Missing vs. Simulated Data

In `PerformanceTrendChart`:
- **Missing Data Intervals**:
  - The line chart breaks cleanly across missing intervals.
  - The UI presents a prominent warning: `"Uzilish: Ayrim oraliqlarda oʻlchov yetishmaydi (uzluksiz chiziq tortilmagan)"`.
  - Missing readings are NEVER replaced with zeros, as treating missing data as zero would corrupt rate and loss calculations.
- **Simulated Intervals**:
  - When hydraulic or machine learning estimates fill telemetry gaps, they are rendered in distinct color schemes (purple/amber) and clearly badged with `SIMULATED`.
  - Tooltips explicitly state: `"Simulyatsiya qilingan nuqta: ML/gidravlik model bahosi"`.

---

## 4. Minimum Data Quality Thresholds for Impact Attribution

The `PerformanceEvaluationEngine` automatically blocks impact attribution when quality thresholds are breached:
- If `observedCoverage < 80.0%`, the evaluation classification is downgraded from `OBSERVED` to `ESTIMATED`.
- If `observedCoverage < 60.0%` or `missingRate > 25.0%`, the evaluation is flagged as `INSUFFICIENT_DATA` and cannot be used for compliance enforcement or bonus distribution.
