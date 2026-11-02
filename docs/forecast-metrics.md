# Forecast Metrics & Agronomic Formulas (Milestone 08.7)

## 1. Soil Moisture Trajectory & Depletion Modeling

### 1.1 Empirical Depletion Velocity
Soil moisture depletion is estimated using robust least-squares linear regression over the recent observation window $[t_0, t_n]$ (minimum 3 readings):

$$\text{slope } m = \frac{n \sum (t_i y_i) - \sum t_i \sum y_i}{n \sum t_i^2 - (\sum t_i)^2} \quad \left(\frac{\%}{\text{hour}}\right)$$

To ensure physical realism, the hourly depletion rate is constrained by the FAO-56 daily crop evapotranspiration ($ET_c$):
$$v_{\text{eff}} = \min\left(0, \max\left(m \times 24, -ET_c \times \frac{100}{\text{RAW}}\right)\right) \quad \left(\frac{\%}{\text{day}}\right)$$

Where:
- $\text{RAW}$ is Readily Available Water in millimeters:
  $$\text{RAW} = p \times (\text{FC} - \text{WP}) \times Z_r$$
  - $p$: Evapotranspiration depletion fraction (crop-specific, default $0.50$ for cotton/wheat).
  - $\text{FC}$: Field Capacity (volumetric %).
  - $\text{WP}$: Wilting Point (volumetric %).
  - $Z_r$: Effective rooting depth ($m$).

### 1.2 Moisture Projection & Uncertainty Bounds
For a given horizon duration $\Delta t$ (in days):
$$\theta_{\text{projected}} = \theta_{\text{latest}} + (v_{\text{eff}} \times \Delta t)$$

Uncertainty spreads expand deterministically with the time horizon:
$$\text{spread} = \text{baseSpread} \times (1 + 0.5 \times \Delta t)$$

Lower and upper confidence bounds:
$$\text{lowerBound} = \max(\text{WP}, \theta_{\text{projected}} - \text{spread})$$
$$\text{upperBound} = \min(\text{FC}, \theta_{\text{projected}} + \text{spread})$$

---

## 2. Irrigation Need & Management Allowed Depletion (MAD)

### 2.1 Threshold Evaluation
The critical moisture threshold $\theta_{\text{MAD}}$ defines the onset of crop water stress:
$$\theta_{\text{MAD}} = \text{FC} - p \times (\text{FC} - \text{WP})$$

| Condition | Likelihood Classification | Operational Urgency |
| :--- | :--- | :--- |
| $\theta_{\text{projected}} < \theta_{\text{MAD}} - 2\%$ | `HIGHLY_LIKELY` | Immediate scheduling required (< 24h) |
| $\theta_{\text{MAD}} - 2\% \le \theta_{\text{projected}} \le \theta_{\text{MAD}}$ | `LIKELY` | Schedule within next 24–48 hours |
| $\theta_{\text{MAD}} < \theta_{\text{projected}} \le \theta_{\text{MAD}} + 3\%$ | `POSSIBLE` | Monitor telemetry closely |
| $\theta_{\text{projected}} > \theta_{\text{MAD}} + 6\%$ | `NOT_EXPECTED` | Soil moisture adequate |

### 2.2 FAO-56 Volumetric Deficit Calculation
When irrigation is required, the volumetric water deficit ($V_{\text{req}}$ in $m^3$) is calculated to restore the root zone to field capacity:
$$V_{\text{req}} = \frac{10 \times \text{Area}_{\text{ha}} \times Z_r \times (\text{FC} - \theta_{\text{projected}})}{\eta_{\text{irr}}}$$

Where $\eta_{\text{irr}}$ is application efficiency:
- Drip irrigation: $\eta = 0.90$
- Sprinkler irrigation: $\eta = 0.80$
- Furrow / Surface irrigation: $\eta = 0.65$

---

## 3. Water Quota Headroom Risk

The water availability risk evaluator compares expected demand against remaining allocated quota:
$$\text{Headroom Ratio } R = \frac{\text{Remaining Seasonal Quota } (m^3)}{\text{Expected Horizon Demand } (m^3)}$$

| Ratio $R$ | Risk Level | Description & Action |
| :--- | :--- | :--- |
| $R < 1.0$ | `CRITICAL` | Imminent deficit. Expected demand exceeds total remaining water. |
| $1.0 \le R < 1.3$ | `HIGH` | Tight headroom (< 30% margin). Plan quota reallocation. |
| $1.3 \le R < 2.0$ | `MEDIUM` | Moderate headroom. Review irrigation plan volumes. |
| $R \ge 2.0$ | `LOW` | Quota sufficient for planned demand. |

---

## 4. Sensor Telemetry Reliability Score

Telemetry reliability is evaluated over a rolling 14-day observation window:
$$\text{Reliability } \% = 100 \times \left(1.0 - \left(0.4 \times \frac{\text{staleIntervals}}{\text{expectedIntervals}} + 0.4 \times \frac{\text{offlineHours}}{\text{totalHours}} + 0.2 \times \text{jitterPenalty}\right)\right)$$

- If $\text{Reliability} \ge 90\%$: Risk is `LOW` (`SUFFICIENT`).
- If $70\% \le \text{Reliability} < 90\%$: Risk is `MEDIUM` (`LIMITED`).
- If $\text{Reliability} < 70\%$: Risk is `HIGH` (`INSUFFICIENT`).

---

## 5. Forecast Calibration & Verification Statuses

When a forecast's target timestamp elapses and fresh ground truth telemetry arrives, the forecast is calibrated:
$$\text{Error } \epsilon = |\theta_{\text{actual}} - \theta_{\text{predicted}}|$$

- If $\epsilon \le \text{tolerance}$ (e.g. $3.5\%$ moisture or $10\%$ volume): Status is `VERIFIED_ACCURATE`.
- If $\epsilon > \text{tolerance}$: Status is `VERIFIED_INACCURATE`.
- If telemetry is missing at target timestamp: Status is `UNVERIFIABLE`.
- If unverified after 7 days: Status is `EXPIRED`.
