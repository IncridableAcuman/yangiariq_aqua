# Prediction-Outcome Matching Semantics (Milestone 08.8)

## 1. Overview & Objective

Associating forward-looking predictive models with real-world operational facts cannot rely on simplistic timestamp equality. In agricultural water systems, soil moisture dynamics fluctuate continuously, irrigation operations take hours to execute, and sensor transmissions experience jitter.

The `PredictionOutcomeMatcher` domain service implements strict, authoritative matching semantics tailored to each prediction target.

---

## 2. Matching Rules by Prediction Type

### 2.1 Soil Moisture Forecast (`SOIL_MOISTURE_FORECAST`)
- **Prediction Target**: Soil moisture percentage at target timestamp $T_{\text{target}}$.
- **Observation Window**:
  $$[T_{\text{target}} - 3\,\text{hours},\, T_{\text{target}} + 3\,\text{hours}]$$
- **Matching Rule**:
  1. Searches for valid sensor observations within the $\pm 3\,\text{hour}$ window.
  2. Selects the observation nearest in time to $T_{\text{target}}$.
  3. If no observation exists within $\pm 3\,\text{hours}$, searches an expanded fallback window of $\pm 6\,\text{hours}$.
  4. If still no valid telemetry is found:
     $$\text{Status} = \mathbf{INSUFFICIENT\_DATA}, \quad \text{FailureReason} = \mathbf{STALE\_TELEMETRY}$$
- **Numerical Target**: Compares $\text{predictedValue}$ against $\text{actualMoisturePercent}$.
- **Tolerance Bounds**: Evaluates whether $\text{actualValue} \in [\text{lowerBound}, \text{upperBound}]$.

---

### 2.2 Irrigation Need Forecast (`IRRIGATION_NEED_FORECAST`)
- **Prediction Target**: Binary determination whether field requires irrigation within horizon window $[T_{\text{gen}}, T_{\text{target}}]$.
- **Matching Rule**:
  1. Ingests all recorded irrigation operations (`irrigation_events`) for the field within the horizon.
  2. A qualifying operation is defined as any executed event with volume $> 0\,\text{m}^3$ and status $\in \{\text{COMPLETED}, \text{IN\_PROGRESS}\}$.
  3. If predicted positive and a qualifying event occurred $\rightarrow \mathbf{TRUE\_POSITIVE}$.
  4. If predicted negative and no qualifying event occurred $\rightarrow \mathbf{TRUE\_NEGATIVE}$.
  5. If predicted negative but an emergency or manual irrigation occurred $\rightarrow \mathbf{FALSE\_NEGATIVE}$.
  6. If predicted positive but no qualifying event occurred:
     - **OPERATIONAL SAFETY CHECK**: Checks if active hydraulic anomalies (`HIGH_PRESSURE_SURGE`, `PIPE_BURST`, `CANAL_OVERFLOW`) or smart irrigation safety blocks prevented the operation.
     - If blocked by safety interlock:
       $$\text{Classification} = \mathbf{INTERRUPTED}, \quad \text{Status} = \mathbf{OUTCOME\_INTERRUPTED}, \quad \text{FailureReason} = \mathbf{SAFETY\_EVENT}$$
     - If no safety interlock intervened $\rightarrow \mathbf{FALSE\_POSITIVE}$.

---

### 2.3 Water Demand Forecast (`WATER_DEMAND_FORECAST`)
- **Prediction Target**: Total expected water volume ($\text{m}^3$) consumed by the parcel over the forecast horizon.
- **Matching Rule**:
  1. Aggregates all authoritative consumption records (`water_consumption_records`) for the field where:
     $$\text{recordDate} \in [\text{generatedAt}, \text{targetTime}]$$
  2. Sums total gross consumed volume ($V_{\text{actual}}$).
  3. Matches against forecasted expected demand ($V_{\text{pred}}$).
  4. Cross-references the **Human-in-the-Loop** audit trail:
     - $V_{\text{recommended}}$: System AI recommendation
     - $V_{\text{approved}}$: Agronomist / Operator approved plan volume
     - $V_{\text{actual}}$: Realized water consumption from flow meter
- **Metrics Evaluated**:
  $$\text{AbsoluteError} = |V_{\text{pred}} - V_{\text{actual}}|$$
  $$\text{DirectionalError} = V_{\text{pred}} - V_{\text{actual}}$$

---

### 2.4 Sensor Reliability Forecast (`SENSOR_RELIABILITY_FORECAST`)
- **Prediction Target**: Anticipated telemetry degradation or hardware failure risk.
- **Matching Rule**:
  1. Ingests telemetry packet records and calculates observed packet drop rate and gap duration.
  2. If packet loss $> 30\%$ or sensor gap $> 6\,\text{hours}$, sensor is classified as degraded (`DEGRADED`).
  3. Evaluates binary agreement between predicted degradation and observed telemetry loss.

---

### 2.5 Water Availability & Allocation Risk (`WATER_AVAILABILITY_RISK`)
- **Prediction Target**: Risk of seasonal water quota exhaustion or canal delivery shortage.
- **Matching Rule**:
  1. Ingests cumulative seasonal water consumption vs allocated quota.
  2. Compares whether predicted shortage occurred during the evaluation horizon.

---

### 2.6 Irrigation Plan Risk (`IRRIGATION_PLAN_RISK`)
- **Prediction Target**: Anticipated execution failure due to canal capacity limits, pump scheduling conflicts, or soil saturation.
- **Matching Rule**:
  1. Examines smart irrigation plans scheduled within the horizon window.
  2. Evaluates whether planned start was delayed, cancelled, or rejected.

---

## 3. Operational Governance: Preserving Safety Interlock Integrity

> [!IMPORTANT]
> A critical failure of naive evaluation systems is penalizing predictive models when operators halt planned irrigations for safety reasons.

In the YANGIARIQ AQUA AI architecture:
- When a model predicts that irrigation is needed, an agronomist approves a 200\,$\text{m}^3$ irrigation.
- If a downstream pressure surge or canal breach triggers the **Hydraulic Safety Interlock**, the smart plan is automatically halted to prevent catastrophic pipe bursts.
- The outcome matcher identifies the safety block from `SmartPlanOutcome::isBlocked` and `HydraulicAnomalyOutcome`.
- **Governance Result**:
  - `classificationResult`: `INTERRUPTED`
  - `evaluationStatus`: `OUTCOME_INTERRUPTED`
  - `safetyInterlockTriggered`: `true`
  - `failureReason`: `SAFETY_EVENT`
  - `operationalNotes`: *"Predicted irrigation was not executed due to safety interlock: Hydraulic safety block active"*
- The event is **never** counted in False Positives ($FP$), ensuring model evaluation honors physical safety interlocks.
