# Hydraulic Safety Limitations & Zero Autonomous Actuation Guardrails

## 1. Core Safety Principle: Decision Support Only

**YANGIARIQ AQUA AI** is strictly a **decision-support and operational planning intelligence system**.

Under **NO CIRCUMSTANCES** does this software directly or autonomously actuate physical pumps, headgates, turnout valves, sluices, or canal infrastructure. 

```
                                [SYSTEM BOUNDARY]
                                        │
┌─────────────────────────────────┐     │     ┌─────────────────────────────────┐
│     YANGIARIQ AQUA AI           │     │     │     PHYSICAL CANAL SYSTEM       │
│                                 │     │     │                                 │
│ - Topology & Flow Analysis      │     │     │ - Primary Intake Gates          │
│ - Fairness Policy Optimization  │     │     │ - Secondary Sluice Gates        │
│ - Multi-Parcel Scheduling       │     │     │ - Field Turnout Valves          │
│ - Hydraulic Interlock Checks    │     │     │ - Pumping Station Starters      │
│                                 │     │     │                                 │
│  [Dispatch Proposal Generated]  │     │     │                                 │
└────────────────┬────────────────┘     │     └────────────────▲────────────────┘
                 │                      │                      │
                 ▼                      │                      │
       [Authorized Operator]            │                      │
       [Human Review & Check] ──────────┼──────────────────────┘
                                        │   Manual Gate / Turnout Operation
                                        │   (Recorded via Confirmation Flow)
```

---

## 2. Hard Hydraulic Safety Interlocks

The network intelligence layer directly queries the existing Anomaly Detection engine (`NetworkAnomalyPort`).

### 2.1 Pipe Burst Interlock
- If an active pipe burst anomaly is flagged for any agricultural parcel intake or distribution conduit, that parcel is **strictly blocked** from dispatch proposals.
- Even if an operator attempts to approve a schedule, `ApproveDispatchScheduleUseCase` re-validates the interlock. If a burst is detected, approval is rejected and the schedule status is escalated to `BLOCKED`.

### 2.2 Critical Pressure Drop Interlock
- Pressure anomalies indicating line collapse or major leakages automatically inhibit pressurized dispatch to the downstream zone.
- Pressure thresholds must recover and anomalies must be resolved by maintenance teams before the dispatch can be reconsidered.

### 2.3 Upstream Surge Risk Prevention
- When water is closed or curtailed on downstream segments, the system checks for sudden hydraulic head spikes in upstream primary canals.
- The dispatch proposal alerts operators to open diversion relief gates prior to main canal gate reduction.

---

## 3. Architecture-Level Enforcement (ArchUnit Guardrail)

To ensure this boundary can never be bypassed by future code changes or third-party libraries, the repository enforces **ArchUnit Test 88**:

```java
@ArchTest
static final ArchRule networkModuleMustNotContainActuationOrHardwareControlPackages =
    noClasses()
        .that().resideInAPackage("com.aquaai.network..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "com.pi4j..",
            "org.eclipse.paho..",
            "com.fazecast.jSerialComm..",
            "org.firmata..",
            "org.openhab.."
        )
        .because("Milestone 08.14 is strictly decision-support; autonomous hardware actuation is prohibited.");
```

---

## 4. Operational Human Lifecycle

1. **System Proposes**: Canal dispatch schedules are generated using fairness policies and capacity checks. Status = `PROPOSED` or `REVIEW_REQUIRED`.
2. **Operator Reviews**: Human operators examine delivery timelines, canal capacity usage, and conflict alerts.
3. **Safety Interlock Revalidation**: System ensures no fresh pipe bursts or pressure drops have occurred.
4. **Authorized Approval**: Operator inputs approval notes and signs off. Status = `SCHEDULED`.
5. **Physical Execution**: WUA water masters physically adjust canal gates and turnouts in the field. Operator marks status = `IN_PROGRESS`.
6. **Turnout Closure**: After water delivery completes, gates are closed. Operator marks status = `COMPLETED`.
7. **Delivery Recording**: Actual water volumes delivered are measured (or read from canal sensors) and logged via `/actual-delivery`. Discrepancies and conveyance losses are calculated and tracked.
