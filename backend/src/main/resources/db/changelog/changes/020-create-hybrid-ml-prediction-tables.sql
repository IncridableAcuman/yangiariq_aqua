-- liquibase formatted sql

-- changeset aqua-ai:037-create-hybrid-ml-prediction-tables
-- validCheckSum: ANY
-- comment: Milestone 08.11: Tables for training datasets, dataset snapshots, feature schemas, and governed ML model candidates

CREATE TABLE IF NOT EXISTS prediction_training_datasets (
    id UUID PRIMARY KEY,
    prediction_type VARCHAR(64) NOT NULL,
    dataset_hash VARCHAR(128) NOT NULL,
    feature_schema_version VARCHAR(64) NOT NULL,
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    field_scope VARCHAR(64) NOT NULL DEFAULT 'ALL',
    record_count INT NOT NULL,
    quality_status VARCHAR(32) NOT NULL,
    quality_summary_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pred_train_ds_type ON prediction_training_datasets(prediction_type, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_pred_train_ds_hash ON prediction_training_datasets(dataset_hash);

CREATE TABLE IF NOT EXISTS prediction_model_candidates (
    id UUID PRIMARY KEY,
    prediction_type VARCHAR(64) NOT NULL,
    model_type VARCHAR(64) NOT NULL,
    version_tag VARCHAR(64) NOT NULL UNIQUE,
    dataset_snapshot_id UUID NOT NULL,
    feature_schema_version VARCHAR(64) NOT NULL,
    training_period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    training_period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    validation_period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    validation_period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    test_period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    test_period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    training_sample_count INT NOT NULL,
    validation_sample_count INT NOT NULL,
    test_sample_count INT NOT NULL,
    training_metrics_json TEXT NOT NULL,
    validation_metrics_json TEXT NOT NULL,
    test_metrics_json TEXT NOT NULL,
    baseline_comparison_metrics_json TEXT NOT NULL,
    configuration_snapshot_json TEXT NOT NULL,
    artifact_hash VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CANDIDATE',
    is_immutable BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    opt_lock_version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_model_cand_dataset FOREIGN KEY (dataset_snapshot_id) REFERENCES prediction_training_datasets(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_model_cand_type_status ON prediction_model_candidates(prediction_type, status);
CREATE INDEX IF NOT EXISTS idx_model_cand_ver_tag ON prediction_model_candidates(version_tag);
CREATE INDEX IF NOT EXISTS idx_model_cand_created ON prediction_model_candidates(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_model_cand_dataset ON prediction_model_candidates(dataset_snapshot_id);

-- Optional traceability links on predictions table
ALTER TABLE predictions ADD COLUMN IF NOT EXISTS model_candidate_id UUID;
ALTER TABLE predictions ADD COLUMN IF NOT EXISTS dataset_snapshot_id UUID;

CREATE INDEX IF NOT EXISTS idx_predictions_cand_id ON predictions(model_candidate_id);
CREATE INDEX IF NOT EXISTS idx_predictions_dataset_id ON predictions(dataset_snapshot_id);
