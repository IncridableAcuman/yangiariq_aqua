# Water Balance Methodology & Calculation Engine

## 1. Mathematical Formulation

The regional water balance is governed by the universal mass-conservation equation applied over defined spatial accounting boundaries (District, Canal Reach, WUA, or Parcel):

$$V_{Closing} = V_{Opening} + V_{Inflow} + V_{TransferredIn} - V_{Delivered} - V_{Consumed} - V_{Losses} - V_{TransferredOut} - V_{ReturnFlow}$$

Where:
- $V_{Opening}$: Initial storage volume held in reservoirs, basins, and active canal sections at the start of the period ($m^3$).
- $V_{Inflow}$: Measured water abstracted from district water sources (e.g., Tuyamuyun, Amu Darya river intakes) ($m^3$).
- $V_{TransferredIn}$: Inter-boundary water received from adjacent administrative or hydraulic networks ($m^3$).
- $V_{Delivered}$: Water delivered to field turnouts and secondary distribution nodes ($m^3$).
- $V_{Consumed}$: Water consumed on agricultural parcels through evapotranspiration and beneficial soil moisture retention ($m^3$).
- $V_{Losses}$: Verified conveyance losses comprising filtration/seepage, open-water surface evaporation, and operational gate-overflow losses ($m^3$).
- $V_{TransferredOut}$: Water conveyed beyond the accounting boundary to downstream sub-districts ($m^3$).
- $V_{ReturnFlow}$: Measured or authoritatively calculated drainage flow collected in collector-drainage networks ($m^3$).
- $V_{Closing}$: Residual storage volume at the end of the accounting period ($m^3$).

---

## 2. Mass Balance Discrepancy & Tolerance

Due to measurement sensor inaccuracies, uncalibrated flow gauges, or unrecorded withdrawals, an accounting discrepancy ($\Delta V$) may exist:

$$\Delta V = V_{InflowTotal} - (V_{OutflowTotal} + \Delta V_{Storage})$$

The percentage discrepancy is evaluated against the total inflow volume:

$$\text{Discrepancy \%} = \left(\frac{|\Delta V|}{V_{InflowTotal}}\right) \times 100$$

A boundary balance is classified as **`RECONCILED`** if:

$$\text{Discrepancy \%} \le \text{Tolerance Threshold \%} \quad (\text{default: } 5.0\%)$$

If the discrepancy exceeds the tolerance threshold, the balance is marked as **`DISCREPANCY`**, prompting mandatory operational investigation.

---

## 3. Double-Counting Prevention Rules

To preserve accounting integrity across nested boundaries:
1. **Intake Separation**: Water abstracted from natural sources (`SOURCE_INFLOW`) is distinctly classified from internal conveyance re-allocations (`CANAL_TRANSFER`).
2. **Turnout vs. Field Consumption**: 
   - `TURNOUT_DELIVERY` represents water released through the sluice gate into the farmer's on-farm canal.
   - `FIELD_CONSUMPTION` represents water applied to crop root zones.
   - The differential represents on-farm ditch loss and is never conflated with district main-canal conveyance loss.
3. **Net Internal Transfers**: In intra-district balances, transfers between internal canal branches net out to zero, preventing false inflation of regional supply.

---

## 4. Implementation Details

The core calculation logic resides in `WaterBalanceEngine.java`:

- Implemented as a pure domain service with zero framework dependencies.
- Handles multiple spatial dimensions: `DISTRICT`, `CANAL_REACH`, `WUA`, `FIELD`.
- Aggregates recorded ledger movements within the specified timestamp window $[t_{start}, t_{end}]$.
- Computes data completeness percentages and assigns evidence classifications dynamically based on underlying telemetry provenance.
