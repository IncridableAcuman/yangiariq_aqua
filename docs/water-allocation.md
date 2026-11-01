# YANGIARIQ AQUA AI — WATER BUDGETING & ALLOCATION OPTIMIZATION

**Bounded Contexts:** `com.aquaai.water`, `com.aquaai.irrigation`  
**Milestone:** 08.2  
**Status:** Implemented & Verified  

---

## 1. Executive Summary

Water allocation and quota enforcement in the Yangiariq district operate under the strict principle of **Hard Water Constraints**. The optimizer treats water allocations as non-negotiable boundaries: under no circumstances may AI recommendations or automated schedules authorize water extraction exceeding remaining seasonal or periodic quotas.

---

## 2. Water Budget Accounting Model

For every agricultural parcel $F$ with registered water allocations, the system dynamically calculates a multi-tier volumetric budget:

$$\text{Total Allocated} = \sum_{a \in \text{Allocations}_{\text{ACTIVE}}} \text{allocatedVolumeM3}(a)$$

$$\text{Consumed} = \sum_{e \in \text{Events}_{\text{COMPLETED}}} \text{actualVolumeM3}(e)$$

$$\text{Committed} = \sum_{p \in \text{Plans}_{\text{SCHEDULED} \cup \text{IN\_PROGRESS}}} \text{recommendedVolumeM3}(p)$$

$$\text{Reserved} = \sum_{p \in \text{Plans}_{\text{APPROVED} \cup \text{PROPOSED}}} \text{recommendedVolumeM3}(p)$$

$$\text{Remaining Available} = \max\left(0, \text{Total Allocated} - \text{Consumed} - \text{Committed} - \text{Reserved}\right)$$

### Volumetric Definitions
- **Total Allocated ($m^3$):** Legal volumetric entitlement sanctioned by the Yangiariq Water Authority for the field and season.
- **Consumed ($m^3$):** Physical water already delivered, measured by flow meters or confirmed in completed irrigation events.
- **Committed ($m^3$):** Water assigned to scheduled turns currently locked in dispatch or in physical delivery.
- **Reserved ($m^3$):** Water provisionally earmarked for approved or pending smart plans awaiting execution.
- **Remaining Available ($m^3$):** Usable headroom available for new smart plan generation.

---

## 3. Quota Enforcement & Shortage Handling

When a parcel's agronomic deficit requires $V_{\text{gross}}$:
1. **Full Fulfillability:** If $V_{\text{gross}} \le \text{Remaining Available}$, the full requested volume is scheduled.
2. **Quota Capping:** If $V_{\text{gross}} > \text{Remaining Available}$ and $\text{Remaining Available} > 0$:
   - The planned volume is scaled down to $\text{Remaining Available}$.
   - A `WATER_QUOTA` constraint with status `WARNING` is added to the plan.
   - An agronomic trade-off explanation is recorded: *"Volume scaled down to prevent seasonal quota overdraft."*
3. **Exhausted Quota:** If $\text{Remaining Available} = 0$:
   - The planned volume is set to $0$.
   - A `WATER_QUOTA` constraint with status `VIOLATED` is attached.
   - The plan cannot transition to `APPROVED` or `SCHEDULED` until an operator extends the quota.

---

## 4. Anti-IDOR Security & RBAC

- **FARMER:**
  - Can only query water budget for fields owned by their user ID.
  - Queries for unauthorized parcels return **HTTP 404 Not Found** (anti-enumeration).
- **OPERATOR & ADMIN:**
  - District-level visibility across all parcels in Yangiariq.
  - Can view aggregated water budgets and cross-parcel dispatch queues.
