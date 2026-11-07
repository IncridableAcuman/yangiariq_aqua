# Conveyance Loss Accounting & Canal Reach Differential Analysis

## 1. Principles of Conveyance Loss

In the Yangiariq district and the lower Amu Darya basin, open earthen canals suffer substantial volumetric losses during transit. Conveyance loss consists of three primary physical components:
1. **Seepage / Infiltration**: Water permeating through unlined canal beds and embankments into surrounding groundwater tables.
2. **Surface Evaporation**: Evaporative losses driven by high ambient arid temperatures and wind speed across open water surfaces.
3. **Operational Discrepancy & Spillage**: Transits overtopping banks, unmetered withdrawals, or gate leakage.

---

## 2. Inflow-Outflow Differential Method

Conveyance losses are quantified per canal segment (`edgeId`) using continuous hydrometric station records:

$$V_{loss} = V_{upstream} - V_{downstream}$$

$$P_{loss} = \left(\frac{V_{loss}}{V_{upstream}}\right) \times 100$$

Where:
- $V_{upstream}$: Total volume entering the canal reach at the upstream headworks or telemetry node ($m^3$).
- $V_{downstream}$: Sum of measured volumes exiting the reach (downstream terminus plus any monitored turnout diversions along the reach) ($m^3$).

---

## 3. Loss Classifications

Canal reaches are categorized according to physical thresholds modeled in `ConveyanceLossClassification`:

| Classification | Loss Percentage Range ($P_{loss}$) | Agronomic & Engineering Interpretation | System Action |
|---|---|---|---|
| **`ACCEPTABLE_CONVEYANCE`** | $\le 5.0\%$ | Modern lined or well-compacted section operating within design standards | Normal operational monitoring |
| **`VERIFIED_LOSS`** | $5.0\% < P_{loss} \le 15.0\%$ | Expected physical transit loss for standard earthen canals in Khorezm | Accounted for in seasonal water budget |
| **`SUSPECTED_LOSS`** | $15.0\% < P_{loss} \le 30.0\%$ | Elevated seepage, sediment accumulation, or compromised bank structure | Scheduled inspection recommended |
| **`UNACCOUNTED_WITHDRAWAL`** | $> 30.0\%$ | Critical anomaly; probable unauthorized off-take or severe structural breach | High-priority operator alert generated |
| **`MEASUREMENT_DISCREPANCY`** | $V_{upstream} < V_{downstream}$ ($P_{loss} < 0$) | Sensor calibration drift or backflow condition | Hydrometric sensor recalibration requested |
| **`INSUFFICIENT_DATA`** | Missing boundary telemetry | Missing upstream or downstream sensors | Highlighted as data gap |

---

## 4. Confidence & Evidence Evaluation

In `ConveyanceLossEngine.java`:
- If both upstream and downstream telemetry are sourced from authoritative physical sensors (`SCADA_TELEMETRY` / `FLOW_METER`), evidence is classified as **`CALCULATED`** with **`HIGH`** confidence.
- If one station relies on hydraulic estimation models, evidence is classified as **`ESTIMATED`** with **`MEDIUM`** confidence.
- Baseless accusations of water theft are prohibited; losses exceeding 30% are objectively labeled as `UNACCOUNTED_WITHDRAWAL` based strictly on verified flow differences.
