# Role & Permission Matrix (Milestone 08.20.1)

This document specifies the default mapping between platform security roles and the 30 platform permissions in **YANGIARIQ AQUA AI**.

---

## 1. Permission Matrix Summary

| Category | Permission Code | Description | `SUPER_ADMIN` | `ADMIN` | `OPERATOR` | `FARMER` |
|---|---|---|:---:|:---:|:---:|:---:|
| **User Management** | `USER_CREATE` | Provision new platform user accounts | ✅ | ✅ | ❌ | ❌ |
| | `USER_READ` | Inspect user accounts and profiles | ✅ | ✅ | ❌ | ❌ |
| | `USER_UPDATE` | Update user profile information | ✅ | ✅ | ❌ | ❌ |
| | `USER_STATUS_CHANGE` | Activate, deactivate, or block users | ✅ | ✅ | ❌ | ❌ |
| | `USER_ROLE_CHANGE` | Change platform security roles | ✅ | ❌* | ❌ | ❌ |
| | `USER_DELETE` | Delete or archive user accounts | ✅ | ❌ | ❌ | ❌ |
| **Farm & Field** | `FARM_CREATE` | Register agricultural farms | ✅ | ✅ | ❌ | ❌ |
| | `FARM_READ` | View farms, boundaries, and crops | ✅ | ✅ | ✅ | ✅ (Own) |
| | `FARM_UPDATE` | Update farm details and boundaries | ✅ | ✅ | ❌ | ❌ |
| | `FIELD_CREATE` | Create land parcel GIS boundaries | ✅ | ✅ | ❌ | ❌ |
| | `FIELD_READ` | View field details, soil data, telemetry | ✅ | ✅ | ✅ | ✅ (Own) |
| | `FIELD_UPDATE` | Update field properties & soil curves | ✅ | ✅ | ❌ | ❌ |
| **Sensors & Telemetry** | `SENSOR_READ` | View moisture, ultrasonic, weather data | ✅ | ✅ | ✅ | ✅ (Own) |
| | `SENSOR_MANAGE` | Register & calibrate physical/virtual sensors | ✅ | ✅ | ✅ | ❌ |
| | `TELEMETRY_INGEST` | Push sensor telemetry payloads | ✅ | ✅ | ✅ | ❌ |
| **Water Accounting** | `WATER_DISTRIBUTION_SCHEDULE` | Compute and publish rotational turns | ✅ | ✅ | ✅ | ❌ |
| | `WATER_ACCOUNTING_READ` | View volumetric water ledgers | ✅ | ✅ | ✅ | ✅ (Own) |
| | `WATER_LIMIT_OVERRIDE` | Override volumetric water allowances | ✅ | ✅ | ❌ | ❌ |
| **Reporting & Analytics** | `REPORT_READ` | Access performance & risk reports | ✅ | ✅ | ✅ | ✅ (Own) |
| | `REPORT_EXPORT` | Export synthetic reports to PDF/CSV | ✅ | ✅ | ✅ | ❌ |
| **Governance & Audit** | `AUDIT_LOG_READ` | Inspect security audit trail | ✅ | ✅ | ❌ | ❌ |
| | `ROLE_MANAGE` | Create custom roles & edit permissions | ✅ | ❌ | ❌ | ❌ |
| | `SYSTEM_SETTINGS_READ` | Inspect runtime environment parameters | ✅ | ✅ | ❌ | ❌ |
| **Field Pilot** | `PILOT_CREATE` | Define controlled pilot programs | ✅ | ✅ | ❌ | ❌ |
| | `PILOT_READ` | View pilot status, gates, and participants | ✅ | ✅ | ✅ | ✅ (Enrolled) |
| | `PILOT_PARTICIPANT_MANAGE` | Enroll/withdraw pilot participants | ✅ | ✅ | ❌ | ❌ |
| | `GROUND_TRUTH_SUBMIT` | Submit physical soil/flow ground truth | ✅ | ✅ | ✅ | ✅ (Enrolled) |
| | `GROUND_TRUTH_REVIEW` | Approve/reject ground truth (Four-Eyes) | ✅ | ✅ | ✅ | ❌ |
| **Incidents** | `INCIDENT_READ` | View canal and field water incidents | ✅ | ✅ | ✅ | ❌ |
| | `INCIDENT_INVESTIGATE` | Conduct root-cause analysis & corrective actions | ✅ | ✅ | ✅ | ❌ |

*\*Note on `USER_ROLE_CHANGE`: In standard operation, only `SUPER_ADMIN` can assign roles or promote users. District `ADMIN` can update farmer profile details but cannot alter user security roles without `ROLE_SUPER_ADMIN` authority.*

---

## 2. Protection of System Roles

System roles (`SUPER_ADMIN`, `ADMIN`, `OPERATOR`, `FARMER`) have the flag `is_system = true` in the `roles` table.
- System roles cannot be deleted via the API (`DELETE /api/v1/admin/roles/{code}` returns `400 BAD_REQUEST`).
- Custom roles created via `POST /api/v1/admin/roles` have `is_system = false` and can be customized and deleted by a `SUPER_ADMIN`.
- System role permission modifications are restricted to `SUPER_ADMIN` and recorded in `security_audit_logs`.
