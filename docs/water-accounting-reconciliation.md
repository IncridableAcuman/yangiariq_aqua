# Water Accounting Reconciliation & Discrepancy Governance

## 1. Multi-Stage Boundary Reconciliation

Reconciliation compares water quantities across successive physical boundary tiers to detect unaccounted water losses or measurement discrepancies:

```
TIER 1: Source Abstraction vs. Canal Headworks Intake
TIER 2: Upstream Reach Conveyance vs. Downstream Reach Inflow
TIER 3: Canal Branch Turnout Delivery vs. Farmer Parcel Inlets
TIER 4: Parcel Turnout Delivery vs. Measured Field Application
```

For each tier, the discrepancy is calculated as:

$$\Delta V = V_{expected} - V_{recorded}$$

$$\text{Discrepancy \%} = \left(\frac{|\Delta V|}{V_{expected}}\right) \times 100$$

---

## 2. Reconciliation Statuses

Every boundary reconciliation item is assigned a state in `WaterReconciliationItem`:
- **`RECONCILED`**: Discrepancy is zero or negligible ($\le 1.0\%$).
- **`WITHIN_TOLERANCE`**: Discrepancy is within the operational threshold (typically $\le 5.0\%$).
- **`DISCREPANCY`**: Discrepancy exceeds tolerance ($> 5.0\%$).
- **`UNVERIFIED`**: Pending administrative review.
- **`INSUFFICIENT_DATA`**: Incomplete metering at one or both boundary ends.

---

## 3. Human Review & Auditable Governance

In accordance with strict operational governance principles:
- The system automatically detects and flags discrepancies.
- The system **does NOT** autonomously alter physical balances or overwrite historical transaction records.
- Discrepancies require formal review by an authorized **`OPERATOR`** or **`ADMIN`**.
- Upon review (`POST /api/v1/water-accounting/reconciliation/{id}/review`), the operator supplies mandatory explanatory audit notes (e.g., "Verification confirmed against SCADA intake", "Gauge maintenance required").
- The review timestamp, operator ID, and notes are immutably recorded in PostgreSQL and reflected across the UI.
