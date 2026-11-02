-- liquibase formatted sql

-- changeset aqua-ai:034-create-prediction-governance-tables
-- validCheckSum: ANY
-- comment: Milestone 08.9: Tables for prediction method versions, calibration proposals, backtest runs, and governance audit trails

CREATE TABLE IF NOT EXISTS prediction_method_versions (
    id UUID PRIMARY KEY,
    prediction_type VARCHAR(64) NOT NULL,
    version_tag VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    description TEXT,
    configuration_json TEXT NOT NULL,
    is_immutable BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activated_at TIMESTAMP WITH TIME ZONE,
    retired_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(64),
    activated_by VARCHAR(64),
    opt_lock_version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_pred_ver_type_status ON prediction_method_versions(prediction_type, status);
CREATE INDEX IF NOT EXISTS idx_pred_ver_tag ON prediction_method_versions(version_tag);

CREATE TABLE IF NOT EXISTS calibration_proposals (
    id UUID PRIMARY KEY,
    proposal_code VARCHAR(64) NOT NULL UNIQUE,
    prediction_type VARCHAR(64) NOT NULL,
    current_version_tag VARCHAR(64) NOT NULL,
    candidate_version_tag VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason TEXT NOT NULL,
    evidence TEXT NOT NULL,
    candidate_changes TEXT NOT NULL,
    proposed_configuration_json TEXT NOT NULL,
    expected_effect TEXT NOT NULL,
    risk_level VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    reviewed_by VARCHAR(64),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    review_notes TEXT,
    opt_lock_version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_cal_prop_type_status ON calibration_proposals(prediction_type, status);
CREATE INDEX IF NOT EXISTS idx_cal_prop_created_at ON calibration_proposals(created_at DESC);

CREATE TABLE IF NOT EXISTS backtest_runs (
    id UUID PRIMARY KEY,
    proposal_id UUID NOT NULL,
    current_version_tag VARCHAR(64) NOT NULL,
    candidate_version_tag VARCHAR(64) NOT NULL,
    date_range_from TIMESTAMP WITH TIME ZONE NOT NULL,
    date_range_to TIMESTAMP WITH TIME ZONE NOT NULL,
    sample_size INT NOT NULL,
    current_mae NUMERIC(12, 4),
    candidate_mae NUMERIC(12, 4),
    mae_improvement_percent NUMERIC(8, 2),
    current_rmse NUMERIC(12, 4),
    candidate_rmse NUMERIC(12, 4),
    current_mbe NUMERIC(12, 4),
    candidate_mbe NUMERIC(12, 4),
    current_accuracy_percent NUMERIC(8, 2),
    candidate_accuracy_percent NUMERIC(8, 2),
    current_f1 NUMERIC(6, 4),
    candidate_f1 NUMERIC(6, 4),
    regression_detected BOOLEAN NOT NULL DEFAULT FALSE,
    regression_details TEXT,
    data_quality VARCHAR(32) NOT NULL DEFAULT 'DATA_COMPLETE',
    is_comparable BOOLEAN NOT NULL DEFAULT TRUE,
    has_sufficient_sample BOOLEAN NOT NULL DEFAULT TRUE,
    executed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    executed_by VARCHAR(64),
    CONSTRAINT fk_backtest_proposal FOREIGN KEY (proposal_id) REFERENCES calibration_proposals(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_backtest_proposal ON backtest_runs(proposal_id);
CREATE INDEX IF NOT EXISTS idx_backtest_executed ON backtest_runs(executed_at DESC);

CREATE TABLE IF NOT EXISTS governance_audit_logs (
    id UUID PRIMARY KEY,
    action VARCHAR(64) NOT NULL,
    prediction_type VARCHAR(64) NOT NULL,
    version_tag VARCHAR(64),
    proposal_id UUID,
    actor_id VARCHAR(64) NOT NULL,
    actor_role VARCHAR(32) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason TEXT NOT NULL,
    previous_state VARCHAR(64),
    new_state VARCHAR(64),
    details_json TEXT
);

CREATE INDEX IF NOT EXISTS idx_gov_audit_time ON governance_audit_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_gov_audit_action ON governance_audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_gov_audit_type ON governance_audit_logs(prediction_type);

-- Seed Baseline V1 Versions for Authoritative Prediction Types
INSERT INTO prediction_method_versions (id, prediction_type, version_tag, status, description, configuration_json, is_immutable, created_at, activated_at, created_by, activated_by, opt_lock_version)
VALUES
    ('a0000001-0000-0000-0000-000000000001', 'SOIL_MOISTURE_FORECAST', 'SOIL_MOISTURE_TREND_V1', 'ACTIVE', 'Authoritative FAO-56 depletion velocity regression model with 24h rolling sensor window', '{"trendWindowHours":24,"minDepletionObservations":3,"smoothingAlpha":0.3,"kcReference":1.00}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM_INIT', 'SYSTEM_INIT', 0),
    ('a0000001-0000-0000-0000-000000000002', 'WATER_DEMAND_FORECAST', 'WATER_DEMAND_RULE_V1', 'ACTIVE', 'Volumetric crop evapotranspiration water budget model with Hargreaves-Samani ET0', '{"forecastPeriodDays":2,"et0ReferenceMm":4.5,"applicationEfficiency":0.80,"quotaBufferPercent":10}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM_INIT', 'SYSTEM_INIT', 0),
    ('a0000001-0000-0000-0000-000000000003', 'IRRIGATION_NEED_FORECAST', 'IRRIGATION_NEED_RULE_V1', 'ACTIVE', 'MAD depletion threshold with hydraulic safety dominance', '{"madThresholdFraction":0.45,"urgencyHours":48,"enforceSafetyBlock":true}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM_INIT', 'SYSTEM_INIT', 0),
    ('a0000001-0000-0000-0000-000000000004', 'SENSOR_RELIABILITY_FORECAST', 'SENSOR_RELIABILITY_V1', 'ACTIVE', 'Statistical 14-day telemetry transmission jitter and packet drop analysis', '{"observationWindowDays":14,"packetLossWarningThreshold":0.15,"packetLossCriticalThreshold":0.30}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SYSTEM_INIT', 'SYSTEM_INIT', 0);
