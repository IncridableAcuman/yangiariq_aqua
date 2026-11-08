# Regional Water Operations Performance Indicator Catalog

## 1. Overview & Standard Units

The YANGIARIQ AQUA AI platform maintains a deterministic catalog of regional performance indicators. Every indicator is uniquely identified by code, categorized, calibrated with explicit SI units, and calculated using deterministic formulas with zero-division guards.

### Supported SI & Operational Units
- Volumetric: $m^3$
- Irrigation Intensity: $m^3/ha$
- Percentage / Fulfillment: $\%$
- Time Intervals: $hours$, $days$
- Normalized Disparity: $CV$ (Coefficient of Variation, dimensionless)
- Count: Integer count of occurrences

---

## 2. Core Indicator Catalog

### 2.1 Water Delivery Reliability

#### Indicator: `WDP_DELIVERY_FULFILLMENT`
- **Name**: Water Delivery Fulfillment Percentage
- **Category**: `DELIVERY_RELIABILITY`
- **Unit**: `%`
- **Scope**: `CANAL_REACH`, `WUA`, `DISTRICT`, `FIELD`
- **Minimum Data Quality**: `OBSERVED`
- **Formula**:
  $$\text{Fulfillment (\%)} = \begin{cases} 
  100.0, & \text{if } V_{\text{planned}} = 0 \text{ and } V_{\text{delivered}} = 0 \\
  \min\left(100.0, \frac{V_{\text{delivered}}}{V_{\text{planned}}} \times 100.0\right), & \text{otherwise}
  \end{cases}$$
- **Data Guard**: Handles zero planned volume gracefully; missing delivery records are treated as unmeasured, not zero delivery.

---

### 2.2 Canal Conveyance Efficiency

#### Indicator: `CCP_LOSS_PERCENTAGE`
- **Name**: Canal Conveyance Loss Percentage
- **Category**: `CONVEYANCE_EFFICIENCY`
- **Unit**: `%`
- **Scope**: `CANAL_REACH`, `DISTRICT`
- **Minimum Data Quality**: `CALCULATED`
- **Formula**:
  $$\text{Conveyance Loss (\%)} = \begin{cases}
  0.0, & \text{if } V_{\text{inflow}} \le 0 \\
  \max\left(0.0, \frac{V_{\text{inflow}} - V_{\text{outflow}}}{V_{\text{inflow}}} \times 100.0\right), & \text{otherwise}
  \end{cases}$$
- **Verification Rule**: Upstream and downstream ultrasonic hydro-post measurements must be verified. Differences due to measurement drift are isolated from physical percolation.

---

### 2.3 Water Allocation Compliance

#### Indicator: `WAC_QUOTA_UTILIZATION`
- **Name**: Approved Quota Utilization Rate
- **Category**: `ALLOCATION_COMPLIANCE`
- **Unit**: `%`
- **Scope**: `WUA`, `DISTRICT`, `FIELD`
- **Minimum Data Quality**: `CALCULATED`
- **Formula**:
  $$\text{Utilization (\%)} = \begin{cases}
  0.0, & \text{if } Q_{\text{approved}} \le 0 \\
  \frac{V_{\text{delivered}}}{Q_{\text{approved}}} \times 100.0, & \text{otherwise}
  \end{cases}$$
- **Compliance Status**:
  - `MET`: $90.0\% \le \text{Utilization} \le 100.0\%$
  - `WITHIN_TOLERANCE`: $85.0\% \le \text{Utilization} < 90.0\%$
  - `EXCEEDED`: $\text{Utilization} > 100.0\%$ (Over-extraction)
  - `UNMET`: $\text{Utilization} < 85.0\%$ (Under-delivery)

---

### 2.4 Distribution Equity

#### Indicator: `DEO_EQUITY_CV`
- **Name**: Distribution Equity Coefficient of Variation
- **Category**: `DISTRIBUTION_EQUITY`
- **Unit**: Dimensionless ($CV$)
- **Scope**: `DISTRICT`, `CANAL_REACH`
- **Minimum Data Quality**: `CALCULATED`
- **Formula**:
  $$CV = \frac{\sigma}{\mu} = \frac{\sqrt{\frac{1}{N} \sum_{i=1}^{N} (I_i - \mu)^2}}{\mu}$$
  where $I_i$ is the water intensity ($m^3/ha$) for entity $i$, and $\mu$ is the mean regional intensity.
- **Classification**:
  - $CV < 0.15$: `EQUITABLE` (Low disparity)
  - $0.15 \le CV \le 0.25$: `MODERATE_DISPARITY`
  - $CV > 0.25$: `HIGH_DISPARITY` (Severe head-tail inequality)

---

### 2.5 Irrigation Operational Efficiency

#### Indicator: `IOE_SCHEDULE_ADHERENCE`
- **Name**: Field Irrigation Schedule Adherence
- **Category**: `IRRIGATION_EFFICIENCY`
- **Unit**: `%`
- **Scope**: `FIELD`, `WUA`
- **Minimum Data Quality**: `OBSERVED`
- **Formula**:
  $$\text{Adherence (\%)} = \frac{N_{\text{completed on schedule}}}{N_{\text{total planned}}} \times 100.0$$
- **Rule**: Claims of crop-yield benefit require direct verified yield logs; water delivery alone does not prove root-zone assimilation.

---

### 2.6 Accountability & Resolution Effectiveness

#### Indicator: `CAE_RESOLUTION_RATE`
- **Name**: Verified Corrective Action Resolution Rate
- **Category**: `ACCOUNTABILITY_EFFECTIVENESS`
- **Unit**: `%`
- **Scope**: `DISTRICT`
- **Minimum Data Quality**: `OBSERVED`
- **Formula**:
  $$\text{Resolution Rate (\%)} = \frac{N_{\text{independently verified resolutions}}}{N_{\text{total investigated incidents}}} \times 100.0$$
- **Governance Invariant**: Actions marked as completed do NOT count towards this rate until independently verified by a third-party auditor or chief hydrologist.

#### Indicator: `CAE_RECURRENCE_RATE`
- **Name**: Operational Incident Recurrence Rate
- **Category**: `ACCOUNTABILITY_EFFECTIVENESS`
- **Unit**: `%`
- **Scope**: `CANAL_REACH`, `DISTRICT`
- **Formula**:
  $$\text{Recurrence Rate (\%)} = \frac{N_{\text{recurrent incidents in same canal reach within 90 days}}}{N_{\text{total resolved incidents}}} \times 100.0$$

---

### 2.7 Sensor Reliability & Data Quality

#### Indicator: `SDR_OBSERVED_COVERAGE`
- **Name**: Telemetry Observed Coverage Ratio
- **Category**: `DATA_QUALITY`
- **Unit**: `%`
- **Scope**: `DISTRICT`, `CANAL_REACH`
- **Formula**:
  $$\text{Observed Coverage (\%)} = \frac{M_{\text{observed readings}}}{M_{\text{total scheduled readings}}} \times 100.0$$
- **Integrity Rule**: Simulated telemetry or neural net interpolations must be counted in $M_{\text{simulated}}$, never as $M_{\text{observed}}$.

---

### 2.8 Predictive Intelligence Usefulness

#### Indicator: `PIU_FORECAST_ACCURACY`
- **Name**: Hybrid ML Demand Forecast Accuracy
- **Category**: `PREDICTIVE_INTELLIGENCE`
- **Unit**: `%`
- **Scope**: `DISTRICT`
- **Formula**:
  $$\text{Accuracy (\%)} = \max\left(0.0, 100.0 - \text{MAPE}\right) = 100.0 - \left(\frac{1}{N} \sum_{t=1}^{N} \left|\frac{V_{\text{actual}} - V_{\text{predicted}}}{V_{\text{actual}}}\right| \times 100.0\right)$$

---

### 2.9 Conservation Outcome Verification

#### Indicator: `COV_VERIFIED_SAVINGS_VOLUME`
- **Name**: Verified Water Conservation Savings
- **Category**: `CONSERVATION_OUTCOME`
- **Unit**: $m^3$
- **Scope**: `DISTRICT`, `CANAL_REACH`, `WUA`
- **Formula**:
  $$V_{\text{saved}} = V_{\text{counterfactual baseline}} - V_{\text{actual consumption}}$$
  *subject to climatic, weather, and crop acreage comparability normalization.*
