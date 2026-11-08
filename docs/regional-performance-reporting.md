# Regional Water Operations Performance Reporting

## 1. Overview & Export Capabilities

The reporting engine provides formal operational audit reports for ministerial review, basin water management boards, and donor verification missions.

Key features:
1. Multi-level reporting scope (`DISTRICT`, `CANAL_REACH`, `WUA`).
2. Comprehensive aggregation across delivery fulfillment, conveyance loss, equity variance, and incident resolution rates.
3. Printable formal summary view formatted for direct PDF generation and printing.
4. Mandatory legal disclaimer guarding against unauthorized automated liability.

---

## 2. Mandatory Legal Disclaimer

Every generated performance report and exported data table includes the statutory disclaimer:

> **Yuridik Ogohlantirish / Qonuniy Izoh:**
> Ushbu hisobot YANGIARIQ AQUA AI avtomatlashtirilgan intellektual monitoring tizimi tomonidan amaldagi telemetrik datchiklar, gidropost oʻlchovlari va suv balansi algoritmlari asosida shakllantirilgan. Mazkur hujjat faqat axborot va operatsion tahlil xarakteriga ega boʻlib, yuridik javobgarlik yoki moliyaviy jazolarni qoʻllash uchun mustaqil davlat inspeksiyasi dalolatnomasi va tasdiqlangan laboratoriya xulosalari talab etiladi. Tizim jismoniy gidrotexnik inshootlarni toʻgʻridan-toʻgʻri avtomatlashtirilgan boshqarish (actuation) vakolatiga ega emas.

---

## 3. Report Structure & Metadata

A standard report (`PerformanceReport`) contains:
- `reportId`: Unique cryptographically indexed UUID.
- `generatedAt`: ISO 8601 UTC timestamp.
- `generatedBy`: User principal name and organization.
- `reportingPeriod`: Start and end timestamps of the evaluated window.
- `geographicScope`: Geographic scope type and boundary reference.
- `overview`: Core regional metrics (`deliveryFulfillmentPct`, `averageConveyanceLossPct`, `distributionEquityCv`, `quotaCompliancePct`, `verifiedConservationSavingsM3`).
- `evaluations`: List of individual indicator evaluation records with compliance statuses (`MET`, `EXCEEDED`, `UNMET`, `WITHIN_TOLERANCE`).
- `impactAssessments`: List of evaluated interventions with conservative attribution ratings (`DIRECTLY_VERIFIED`, `STRONGLY_ASSOCIATED`, `ASSOCIATED`, `INCONCLUSIVE`).
