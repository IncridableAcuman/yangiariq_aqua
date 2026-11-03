-- liquibase formatted sql

-- changeset aqua-ai:035-add-prediction-version-traceability
-- validCheckSum: ANY
-- comment: Milestone 08.9: Add immutable version traceability and configuration snapshots to predictions table, plus baseline versions

ALTER TABLE predictions ADD COLUMN IF NOT EXISTS method_version_tag VARCHAR(64);
ALTER TABLE predictions ADD COLUMN IF NOT EXISTS configuration_snapshot TEXT;

CREATE INDEX IF NOT EXISTS idx_predictions_version_tag ON predictions(method_version_tag);

-- Add affected_scope to calibration_proposals
ALTER TABLE calibration_proposals ADD COLUMN IF NOT EXISTS affected_scope VARCHAR(64) DEFAULT 'ALL_PARCELS';

-- Populate existing prediction records with their authoritative V1 method versions
UPDATE predictions
SET method_version_tag = 'SOIL_MOISTURE_TREND_V1',
    configuration_snapshot = '{"trendWindowHours":24,"minDepletionObservations":3,"smoothingAlpha":0.3,"kcReference":1.00}'
WHERE prediction_type = 'SOIL_MOISTURE_FORECAST' AND method_version_tag IS NULL;

UPDATE predictions
SET method_version_tag = 'WATER_DEMAND_RULE_V1',
    configuration_snapshot = '{"forecastPeriodDays":2,"et0ReferenceMm":4.5,"applicationEfficiency":0.80,"quotaBufferPercent":10}'
WHERE prediction_type = 'WATER_DEMAND_FORECAST' AND method_version_tag IS NULL;

UPDATE predictions
SET method_version_tag = 'IRRIGATION_NEED_RULE_V1',
    configuration_snapshot = '{"madThresholdFraction":0.45,"urgencyHours":48,"enforceSafetyBlock":true}'
WHERE prediction_type = 'IRRIGATION_NEED_FORECAST' AND method_version_tag IS NULL;

UPDATE predictions
SET method_version_tag = 'SENSOR_RELIABILITY_V1',
    configuration_snapshot = '{"observationWindowDays":14,"packetLossWarningThreshold":0.15,"packetLossCriticalThreshold":0.30}'
WHERE prediction_type = 'SENSOR_RELIABILITY_FORECAST' AND method_version_tag IS NULL;

UPDATE predictions
SET method_version_tag = 'WATER_AVAILABILITY_RISK_V1',
    configuration_snapshot = '{"headroomCriticalThresholdPercent":15,"quotaDepletionWarningPercent":80}'
WHERE prediction_type = 'WATER_AVAILABILITY_RISK' AND method_version_tag IS NULL;

UPDATE predictions
SET method_version_tag = 'IRRIGATION_PLAN_RISK_V1',
    configuration_snapshot = '{"enforceSafetyInterlock":true,"stalenessThresholdHours":6,"maxDelayHours":24}'
WHERE prediction_type = 'IRRIGATION_PLAN_RISK' AND method_version_tag IS NULL;

-- Seed remaining baseline V1 versions for risk forecasting methods
INSERT INTO prediction_method_versions (id, prediction_type, version_tag, status, description, configuration_json, is_immutable, created_at, activated_at, created_by, activated_by, opt_lock_version)
SELECT 'a0000001-0000-0000-0000-000000000005', 'WATER_AVAILABILITY_RISK', 'WATER_AVAILABILITY_RISK_V1', 'ACTIVE', 'Water quota depletion and entitlement headroom risk model', '{"headroomCriticalThresholdPercent":15,"quotaDepletionWarningPercent":80}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM_INIT', 'SYSTEM_INIT', 0
WHERE NOT EXISTS (SELECT 1 FROM prediction_method_versions WHERE version_tag = 'WATER_AVAILABILITY_RISK_V1');

INSERT INTO prediction_method_versions (id, prediction_type, version_tag, status, description, configuration_json, is_immutable, created_at, activated_at, created_by, activated_by, opt_lock_version)
SELECT 'a0000001-0000-0000-0000-000000000006', 'IRRIGATION_PLAN_RISK', 'IRRIGATION_PLAN_RISK_V1', 'ACTIVE', 'Multi-factor operational risk model with hydraulic safety interlock dominance', '{"enforceSafetyInterlock":true,"stalenessThresholdHours":6,"maxDelayHours":24}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM_INIT', 'SYSTEM_INIT', 0
WHERE NOT EXISTS (SELECT 1 FROM prediction_method_versions WHERE version_tag = 'IRRIGATION_PLAN_RISK_V1');
