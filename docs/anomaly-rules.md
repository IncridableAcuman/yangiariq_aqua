# Configurable Anomaly Rules & Threshold Management (`docs/anomaly-rules.md`)

## 1. Scientific & Agronomic Grounding

> [!WARNING]
> **Scientific Reliability & Agronomic Disclaimer (Refinement #1)**:
> Provisional agricultural thresholds established in YANGIARIQ AQUA AI are operational engineering baselines tuned for Yangiariq district's alluvial meadow soils and arid oasis conditions. They must **not** be treated as scientifically validated universal biological constants. All thresholds are configurable per installation and must be verified against field ground-truth and local agronomist counsel.

### Environmental Context for Yangiariq District, Khorezm
1. **Soil Hydrology**: Loam and clay alluvial soils dominate the lower Amu Darya basin. Field capacity typically ranges between 24% and 30% volumetric water content, with permanent wilting point near 12% to 15%.
2. **Atmospheric Stress (Garmsel)**: Summer "chilla" periods frequently experience ambient temperatures exceeding $40^\circ\text{C}$ coupled with relative humidity dropping below 15%, causing acute crop transpiration stress.
3. **Soil & Irrigation Water Salinity**: Soil salinity and irrigation runoff water electrical conductivity (EC) are critical variables. Levels exceeding $4.0\text{ dS/m}$ require seasonal leaching (shoʻr yuvish).

---

## 2. Dynamic Rule Configuration Architecture

Rules are stored in table `anomaly_rule_configs` and managed via [`AnomalyRuleConfigRepository`](file:///home/izzatbek/Desktop/agronexsus/backend/src/main/java/com/aquaai/anomaly/domain/repository/AnomalyRuleConfigRepository.java).

```sql
CREATE TABLE anomaly_rule_configs (
    id UUID PRIMARY KEY,
    rule_code VARCHAR(64) NOT NULL UNIQUE,
    sensor_type VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    severity VARCHAR(32) NOT NULL,
    threshold NUMERIC(14, 4) NOT NULL,
    secondary_threshold NUMERIC(14, 4),
    time_window_minutes INT NOT NULL DEFAULT 60,
    min_consecutive_readings INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

---

## 3. Seeded Default Rules Catalog

| Rule Code | Sensor Modality | Severity | Primary Threshold | Secondary Threshold | Time Window | Min Consecutive | Description & Agronomic Rationale |
| :--- | :--- | :--- | :---: | :---: | :---: | :---: | :--- |
| `RULE_SOIL_MOISTURE_DEFICIT` | `SOIL_MOISTURE` | `CRITICAL` | `15.0000 %` | `25.0000 %` | 60 min | 1 | Volumetric moisture $\le 15\%$ indicates root zone approach to wilting point. |
| `RULE_WATER_FLOW_SPIKE` | `WATER_FLOW` | `HIGH` | `45.0000 m³/h` | `30.0000 m³/h` | 30 min | 1 | Delivery exceeding $45\text{ m}^3/\text{h}$ flags potential main pipe rupture or open gate valve. |
| `RULE_PRESSURE_DROP` | `PRESSURE` | `HIGH` | `1.2000 bar` | `5.5000 bar` | 15 min | 1 | Sub-nominal pressure in pressurized drip line ($< 1.2\text{ bar}$) or overpressure ($> 5.5\text{ bar}$). |
| `RULE_SALINITY_HIGH` | `SALINITY` | `HIGH` | `4.0000 dS/m` | `6.5000 dS/m` | 120 min | 1 | Electrical conductivity $> 4.0\text{ dS/m}$ signals severe salt accumulation requiring flushing. |
| `RULE_TEMPERATURE_EXTREME` | `TEMPERATURE` | `HIGH` | `42.0000 °C` | `2.0000 °C` | 60 min | 1 | Detects acute heatwave ($> 42^\circ\text{C}$) or early/late spring frost risk ($< 2^\circ\text{C}$). |
| `RULE_HUMIDITY_LOW` | `HUMIDITY` | `MEDIUM` | `15.0000 %` | `null` | 60 min | 1 | Dry garmsel wind indicator requiring immediate localized canopy misting. |
| `RULE_RAINFALL_HEAVY` | `RAINFALL` | `HIGH` | `50.0000 mm/h` | `null` | 60 min | 1 | Intense cloudburst precipitation presenting field drainage and mud inundation hazard. |

---

## 4. Rule Editing & RBAC Security

### Access Control Rules:
- **`ADMIN` Role**: Exclusive authority to modify primary thresholds, secondary thresholds, evaluation windows, and enable/disable states.
- **`OPERATOR` & `FARMER` Roles**: Read-only visibility into operational detection thresholds. Requests by non-admins to `PUT /api/v1/anomaly-rules/{id}` or `PATCH /api/v1/anomaly-rules/{id}/toggle` receive HTTP `403 Forbidden`.

### Validation Guardrails:
1. `threshold`: Must be a valid numeric quantity; cannot be null.
2. `timeWindowMinutes`: Must be $\ge 1$ minute (defaults to 60).
3. `minConsecutiveReadings`: Must be $\ge 1$ observation to avoid division-by-zero or infinite loops.
4. `severity`: Must match a valid enumeration: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `INFO`.
