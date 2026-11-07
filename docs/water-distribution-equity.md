# Water Distribution Equity & Inter-Cluster Fairness Intelligence

## 1. Context & Motivation

In irrigated command areas, spatial inequity is a systemic risk:
- **Head-End Advantage**: Farms near primary canal intakes often receive excess water.
- **Tail-End Deprivation**: Farms at downstream branch canal terminuses suffer water shortfalls, delayed irrigation rotations, and increased salinity risk.
- **WUA Disparities**: Varied managerial capacity among Water User Associations (WUAs) creates inequity across adjacent administrative zones.

Milestone 08.15 provides an objective, statistical distribution equity analysis layer to detect disparities without bias.

---

## 2. Statistical Methodology

Distribution equity is evaluated across agricultural clusters (WUAs, sub-districts, canal branches) using the following metrics implemented in `DistributionEquityEngine.java`:

### 2.1 Water Delivery Intensity ($m^3/\text{ha}$)
For each parcel $i$ within cluster $C$:

$$d_i = \frac{V_{delivered, i}}{A_i}$$

Where $V_{delivered, i}$ is total turnout delivery volume ($m^3$) and $A_i$ is arable area (hectares).

### 2.2 Cluster Mean and Sample Variance

$$\mu_C = \frac{1}{N} \sum_{i=1}^{N} d_i$$

$$\sigma^2_C = \frac{1}{N-1} \sum_{i=1}^{N} (d_i - \mu_C)^2$$

### 2.3 Coefficient of Variation ($CV$)

$$CV = \frac{\sigma_C}{\mu_C}$$

$CV$ provides a scale-independent index of dispersion.

---

## 3. Disparity Levels & Fairness Thresholds

Equity evaluations are classified into standardized levels:

| Disparity Level | $CV$ Threshold | Interpretation & Guidance |
|---|---|---|
| **`BALANCED`** | $CV \le 0.15$ | Uniform water distribution across all members of the WUA/cluster. |
| **`POTENTIAL_DISPARITY`** | $0.15 < CV \le 0.30$ | Moderate delivery unevenness; review rotation timing and gate scheduling. |
| **`SIGNIFICANT_DISPARITY`** | $CV > 0.30$ | Severe skew; tail-end parcels receiving disproportionately low volumes or experiencing persistent rotational delays. |
| **`INSUFFICIENT_DATA`** | Sample size $< 3$ | Sample count is statistically inadequate to establish reliable dispersion metrics. |

---

## 4. Rotational Delivery Delay (Lag) Tracking

In addition to volumetric equity, delivery timeliness is tracked:

$$\bar{t}_{delay} = \frac{1}{N} \sum_{i=1}^{N} \max(0, t_{actual\_delivery} - t_{scheduled\_window})$$

Persistent delays ($> 24$ hours) at branch tail-ends trigger operational fairness flags even if eventual cumulative volume approaches quota.
