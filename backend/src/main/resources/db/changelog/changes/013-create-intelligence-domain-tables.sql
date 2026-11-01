-- liquibase formatted sql

-- changeset aqua-ai:029-create-agronomic-assessments-table
-- comment: Milestone 08.3: Table storing deterministic agronomic assessments derived from telemetry intelligence
CREATE TABLE IF NOT EXISTS agronomic_assessments (
    id UUID PRIMARY KEY,
    field_id UUID NOT NULL,
    primary_signal VARCHAR(64) NOT NULL,
    overall_severity VARCHAR(32) NOT NULL DEFAULT 'INFO',
    soil_moisture_trend_direction VARCHAR(32) NOT NULL DEFAULT 'STABLE',
    soil_moisture_rate_per_hour NUMERIC(8, 4),
    telemetry_freshness VARCHAR(32) NOT NULL DEFAULT 'FRESH',
    confidence VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    confidence_score NUMERIC(5, 4),
    agronomic_summary TEXT NOT NULL,
    recommendations_overview TEXT,
    hydraulic_safety_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    hydraulic_safety_reason VARCHAR(500),
    water_budget_constrained BOOLEAN NOT NULL DEFAULT FALSE,
    water_budget_reason VARCHAR(500),
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assessment_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_agronomic_assessments_field_assessed ON agronomic_assessments(field_id, assessed_at DESC);
CREATE INDEX IF NOT EXISTS idx_agronomic_assessments_field_signal ON agronomic_assessments(field_id, primary_signal);

-- changeset aqua-ai:030-create-agronomic-assessment-signals-table
-- comment: Milestone 08.3: Table storing supporting agronomic signals for each assessment
CREATE TABLE IF NOT EXISTS agronomic_assessment_signals (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL,
    signal_type VARCHAR(64) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    metric VARCHAR(64) NOT NULL,
    observed_value NUMERIC(14, 4),
    threshold_value NUMERIC(14, 4),
    message VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assessment_signals_assessment FOREIGN KEY (assessment_id) REFERENCES agronomic_assessments(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_assessment_signals_assessment_id ON agronomic_assessment_signals(assessment_id);
