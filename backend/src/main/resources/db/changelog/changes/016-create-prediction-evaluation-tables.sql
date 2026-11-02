-- liquibase formatted sql

-- changeset aqua-ai:033-create-prediction-evaluations-table
-- comment: Milestone 08.8: Table storing immutable predictive model evaluations, accuracy metrics, and operational audit context
CREATE TABLE IF NOT EXISTS prediction_evaluations (
    id UUID PRIMARY KEY,
    prediction_id UUID NOT NULL UNIQUE,
    field_id UUID NOT NULL,
    prediction_type VARCHAR(64) NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    model_version VARCHAR(64) NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    target_time TIMESTAMP WITH TIME ZONE NOT NULL,
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actual_observed_at TIMESTAMP WITH TIME ZONE,
    predicted_value NUMERIC(12, 2),
    lower_bound NUMERIC(12, 2),
    upper_bound NUMERIC(12, 2),
    actual_value NUMERIC(12, 2),
    absolute_error NUMERIC(12, 4),
    relative_error_percent NUMERIC(8, 4),
    directional_error NUMERIC(12, 4),
    is_within_bounds BOOLEAN,
    predicted_class VARCHAR(64),
    actual_class VARCHAR(64),
    classification_result VARCHAR(32) NOT NULL DEFAULT 'NOT_APPLICABLE',
    recommended_value NUMERIC(12, 2),
    approved_value NUMERIC(12, 2),
    operational_event_id VARCHAR(64),
    operational_notes TEXT,
    safety_interlock_triggered BOOLEAN NOT NULL DEFAULT FALSE,
    confidence_percent INT NOT NULL,
    evaluation_status VARCHAR(32) NOT NULL,
    failure_reason VARCHAR(64) NOT NULL DEFAULT 'NONE',
    data_quality VARCHAR(32) NOT NULL DEFAULT 'DATA_COMPLETE',
    evaluation_method VARCHAR(64) NOT NULL,
    notes TEXT,
    CONSTRAINT fk_eval_prediction FOREIGN KEY (prediction_id) REFERENCES predictions(id) ON DELETE CASCADE,
    CONSTRAINT fk_eval_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_eval_field_type ON prediction_evaluations(field_id, prediction_type);
CREATE INDEX IF NOT EXISTS idx_eval_evaluated_at ON prediction_evaluations(evaluated_at DESC);
CREATE INDEX IF NOT EXISTS idx_eval_type_status ON prediction_evaluations(prediction_type, evaluation_status);
CREATE INDEX IF NOT EXISTS idx_eval_classification ON prediction_evaluations(classification_result);
CREATE INDEX IF NOT EXISTS idx_eval_data_quality ON prediction_evaluations(data_quality);
