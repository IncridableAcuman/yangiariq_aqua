# Training Dataset Snapshots & Feature Schema

## 1. Dataset Reproducibility & Auditing
Every model training execution produces an immutable `PredictionTrainingDataset` record:
- **`datasetId`**: Unique UUID assigned upon snapshot creation.
- **`datasetHash`**: Deterministic SHA-256 fingerprint computed across sorted, canonicalized feature vectors.
- **`featureSchemaVersion`**: Explicit schema identifier (e.g. `FEAT_SOIL_MOISTURE_V1`).
- **`recordCount`**: Total verified observation records included in the snapshot.
- **`qualityStatus`**: Audited quality status (`HIGH_QUALITY`, `ACCEPTABLE`, `DEGRADED`, `INSUFFICIENT`).

## 2. Feature Schema: `FEAT_SOIL_MOISTURE_V1`
The initial regression target (`SOIL_MOISTURE_FORECAST`) uses 14 strictly governed agronomic and telemetry features:

| Index | Feature Name | Description | Source | Unit |
|---|---|---|---|---|
| 0 | `current_soil_moisture` | Latest sensor reading | Telemetry | % |
| 1 | `soil_temperature` | Subsurface soil temperature | Telemetry | °C |
| 2 | `past_moisture_trend_24h` | 24-hour rate of moisture change | Telemetry | %/hr |
| 3 | `telemetry_confidence_score` | Sensor health & reliability score | Hardware | [0, 1] |
| 4 | `irrigation_volume_recent_m3` | Water volume applied in past 48h | Irrigation | m³ |
| 5 | `irrigation_frequency_recent` | Irrigation event count in past 7d | Irrigation | count |
| 6 | `water_headroom_m3` | Remaining seasonal allocation | Water Domain | m³ |
| 7 | `field_area_ha` | Parcel size | GIS | ha |
| 8 | `crop_type_code` | Categorical crop identifier | Field Domain | enum int |
| 9 | `soil_type_code` | Soil texture classification | Soil Survey | enum int |
| 10 | `irrigation_method_code` | Drip, furrow, or pivot | Hardware | enum int |
| 11 | `season_day_of_year` | Day of year (1-366) | Temporal | integer |
| 12 | `hydraulic_interlock_active` | Binary hydraulic safety state | Interlocks | {0, 1} |
| 13 | `active_anomaly_flag` | Recent telemetry or flow anomaly | Anomaly Engine | {0, 1} |

## 3. Strict Temporal Splitting
To eliminate temporal data leakage:
- **Train Split (60%)**: Oldest historical time partition.
- **Validation Split (20%)**: Intermediate partition for parameter validation.
- **Test Split (20%)**: Most recent unseen partition reserved exclusively for baseline comparison.

Random k-fold or random shuffling is strictly prohibited for agricultural time-series training.
