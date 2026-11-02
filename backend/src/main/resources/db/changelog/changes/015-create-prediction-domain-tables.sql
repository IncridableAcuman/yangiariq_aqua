-- liquibase formatted sql

-- changeset aqua-ai:032-create-predictions-table
-- comment: Milestone 08.7: Table storing deterministic predictive intelligence forecasts and evaluation outcomes
CREATE TABLE IF NOT EXISTS predictions (
    id UUID PRIMARY KEY,
    field_id UUID NOT NULL,
    prediction_type VARCHAR(64) NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    target_time TIMESTAMP WITH TIME ZONE NOT NULL,
    predicted_value NUMERIC(12, 2),
    lower_bound NUMERIC(12, 2),
    upper_bound NUMERIC(12, 2),
    unit VARCHAR(32),
    confidence_percent INT NOT NULL,
    risk_level VARCHAR(32) NOT NULL,
    data_sufficiency VARCHAR(32) NOT NULL,
    likelihood VARCHAR(32),
    evidence TEXT,
    assumptions TEXT,
    recommended_action TEXT,
    action_target_module VARCHAR(64),
    actual_outcome_value NUMERIC(12, 2),
    outcome_recorded_at TIMESTAMP WITH TIME ZONE,
    accuracy_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prediction_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_predictions_field_type_target ON predictions(field_id, prediction_type, target_time);
CREATE INDEX IF NOT EXISTS idx_predictions_field_generated ON predictions(field_id, generated_at DESC);
CREATE INDEX IF NOT EXISTS idx_predictions_type_risk ON predictions(prediction_type, risk_level);
CREATE INDEX IF NOT EXISTS idx_predictions_accuracy ON predictions(accuracy_status);
