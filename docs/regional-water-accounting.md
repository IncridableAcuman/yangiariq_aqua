# Regional Water Accounting & Conservation Intelligence

## 1. Architectural Scope & Objective

**Milestone 08.15** establishes an authoritative, evidence-based regional water accounting, distribution equity, and conservation intelligence layer for **YANGIARIQ AQUA AI**.

Prior milestones modeled field-level water budgeting (06.x), GIS spatial boundaries (08.2), cooperative irrigation plans (08.5), hydraulic safety interlocks (08.7), predictive intelligence and machine learning (08.8–08.12), farm risk intelligence (08.13), and regional water distribution network scheduling (08.14). 

Milestone 08.15 integrates these domains into a closed-loop accounting system answering the foundational water governance questions:
1. **How much water entered the district irrigation network from upstream sources?**
2. **How much water was transferred through primary and secondary canals?**
3. **What volumetric conveyance losses occurred along each canal reach?**
4. **How much water was physically delivered to parcel turnouts versus applied to crops?**
5. **Are water user associations (WUA / SIU) and downstream tail-end farmers receiving equitable water distribution?**
6. **Which parcels or canal reaches violate allocated quotas or exhibit excessive seepage?**
7. **What verifiable, evidence-backed conservation measures can be enacted?**

---

## 2. Core Accounting Principles

### 2.1 Traceable Accounting Chain
Water movements follow a traceable mass-conservation path:
```
WATER SOURCE (Tuyamuyun Intake)
      │
      ▼
NETWORK INTAKE (Primary Canal Headworks)
      │
      ▼
CANAL DELIVERY & CONVEYANCE (Main & Secondary Reaches)
      │
      ▼
DISTRIBUTION POINT (WUA Turnout / Sluice Gate)
      │
      ▼
FIELD DELIVERY (Parcel Gate Turnout)
      │
      ▼
FIELD CONSUMPTION (Crop Root Zone Application)
      │
      ▼
RETURN FLOW / RECORDED DRAINAGE (Collector System)
```

### 2.2 Strict Evidence Classification
Every quantity recorded in the ledger or reported in water balance statements is tagged with an immutable evidence classification:
- **`OBSERVED`**: Directly measured by SCADA sensors, ultrasonic flow meters, or verified field delivery logs.
- **`CALCULATED`**: Derived from verified source values using explicit mass-balance conservation formulas.
- **`ESTIMATED`**: Derived using documented agronomic crop evapotranspiration ($ET_c$) models or soil-water depletion curves.
- **`SIMULATED`**: Synthetic or model-generated values from hydraulic simulation tests.
- **`MISSING`**: Required telemetry or field reports are absent.

> [!IMPORTANT]
> **Non-Fabrication Guarantee**: Estimated or simulated values are never presented as actual physical measurements. Water savings potential is only calculated when verifiable baseline telemetry exists; otherwise, the system reports "Measurement required for quantification."

---

## 3. Package Structure & Hexagonal Boundaries

The accounting capability is encapsulated in `com.aquaai.wateraccounting`:

```
com.aquaai.wateraccounting
├── domain
│   ├── model
│   │   ├── AccountingDimension.java
│   │   ├── AccountingPeriodType.java
│   │   ├── AllocationComplianceItem.java
│   │   ├── AllocationComplianceStatus.java
│   │   ├── ConservationOpportunity.java
│   │   ├── ConveyanceLossAssessment.java
│   │   ├── ConveyanceLossClassification.java
│   │   ├── DistributionEquityAssessment.java
│   │   ├── EquityDisparityLevel.java
│   │   ├── EvidenceClassification.java
│   │   ├── LedgerEntryType.java
│   │   ├── ReconciliationStatus.java
│   │   ├── RegionalWaterBalance.java
│   │   ├── WaterAccountingEvidence.java
│   │   ├── WaterLedgerEntry.java
│   │   └── WaterReconciliationItem.java
│   ├── port
│   │   ├── AccountingFieldPort.java
│   │   ├── AccountingNetworkPort.java
│   │   ├── AccountingNotificationPort.java
│   │   ├── AccountingSensorPort.java
│   │   ├── AccountingWaterPort.java
│   │   ├── WaterLedgerRepository.java
│   │   └── WaterReconciliationRepository.java
│   └── service
│       ├── AllocationComplianceEngine.java
│       ├── ConservationIntelligenceEngine.java
│       ├── ConveyanceLossEngine.java
│       ├── DistributionEquityEngine.java
│       ├── WaterBalanceEngine.java
│       └── WaterReconciliationEngine.java
├── application
│   └── usecase
│       ├── GetAllocationComplianceUseCase.java
│       ├── GetConservationOpportunitiesUseCase.java
│       ├── GetConveyanceLossAnalysisUseCase.java
│       ├── GetDistributionEquityUseCase.java
│       ├── GetFieldWaterAccountingUseCase.java
│       ├── GetRegionalWaterBalanceUseCase.java
│       ├── GetRegionalWaterOverviewUseCase.java
│       ├── GetWaterReconciliationUseCase.java
│       ├── GetWuaWaterAccountingUseCase.java
│       ├── ListWaterLedgerEntriesUseCase.java
│       └── ReviewReconciliationUseCase.java
├── infrastructure
│   ├── adapter
│   │   ├── AccountingFieldAdapter.java
│   │   ├── AccountingNetworkAdapter.java
│   │   ├── AccountingSensorAdapter.java
│   │   ├── AccountingWaterAdapter.java
│   │   └── InAppAccountingNotificationAdapter.java
│   ├── config
│   │   └── WaterAccountingDomainConfig.java
│   └── persistence
│       ├── SpringDataWaterLedgerEntryRepository.java
│       ├── SpringDataWaterReconciliationRepository.java
│       ├── WaterAccountingDataInitializer.java
│       ├── WaterLedgerEntryJpaEntity.java
│       ├── WaterLedgerEntryJpaMapper.java
│       ├── WaterLedgerRepositoryImpl.java
│       ├── WaterReconciliationJpaEntity.java
│       ├── WaterReconciliationJpaMapper.java
│       └── WaterReconciliationRepositoryImpl.java
└── api
    ├── WaterAccountingController.java
    └── dto
        ├── AllocationComplianceResponse.java
        ├── ConservationOpportunityResponse.java
        ├── ConveyanceLossResponse.java
        ├── DistributionEquityResponse.java
        ├── FieldWaterAccountingResponse.java
        ├── ReconciliationReviewRequest.java
        ├── RegionalWaterBalanceResponse.java
        ├── RegionalWaterOverviewResponse.java
        ├── WaterLedgerEntryResponse.java
        ├── WaterReconciliationResponse.java
        └── WuaWaterAccountingResponse.java
```

---

## 4. API Endpoints

All endpoints are secured under `/api/v1/water-accounting` and enforce role-based access control (FARMER, OPERATOR, ADMIN) with anti-IDOR checks:

| Method | Endpoint | Allowed Roles | Description |
|---|---|---|---|
| `GET` | `/api/v1/water-accounting/overview` | ALL | District high-level accounting KPIs, reconciliation alerts, and evidence stats |
| `GET` | `/api/v1/water-accounting/balance` | ALL | Mass-balance calculation across District, WUA, Canal, or Field boundaries |
| `GET` | `/api/v1/water-accounting/ledger` | ALL | Immutable double-entry water movement audit journal |
| `GET` | `/api/v1/water-accounting/conveyance-losses` | ALL | Canal reach conveyance loss assessments with upstream/downstream differential |
| `GET` | `/api/v1/water-accounting/allocation-compliance` | ALL | Quota consumption vs delivery compliance per parcel (farmers scoped to own fields) |
| `GET` | `/api/v1/water-accounting/equity` | ALL | WUA and branch equity metrics ($m^3/ha$, Gini / CV, lag hours) |
| `GET` | `/api/v1/water-accounting/conservation-opportunities` | ALL | Verifiable water conservation actions with quantified savings potential |
| `GET` | `/api/v1/water-accounting/reconciliation` | ALL | Stage reconciliation items comparing intake, delivery, and consumption |
| `POST` | `/api/v1/water-accounting/reconciliation/{id}/review` | OPERATOR, ADMIN | Human review and confirmation of water balance discrepancies |
| `GET` | `/api/v1/water-accounting/fields/{fieldId}` | ALL (Anti-IDOR) | Field-level turnout delivery vs crop consumption accounting breakdown |
| `GET` | `/api/v1/water-accounting/wuas/{wuaId}` | ALL | WUA-level water balance and aggregate equity profile |

---

## 5. Security and Anti-IDOR Enforcement

Farmers may only query their own fields (`/api/v1/water-accounting/fields/{fieldId}` and `/allocation-compliance?fieldId=...`).
If a farmer attempts to inspect a field owned by another tenant, `AccountingFieldAdapter` validates ownership via `FieldRepository` and throws `UnauthorizedOperationException`, resulting in HTTP 403 Forbidden.
Reconciliation review (`POST .../reconciliation/{id}/review`) is strictly restricted to `OPERATOR` and `ADMIN` roles.
