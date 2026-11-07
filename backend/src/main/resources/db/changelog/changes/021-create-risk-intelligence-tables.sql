-- liquibase formatted sql

-- changeset aqua-ai:038-create-risk-intelligence-tables
-- validCheckSum: ANY
-- comment: Milestone 08.13: Table storing explainable farm-level predictive risk & resilience assessments
CREATE TABLE IF NOT EXISTS farm_risk_assessments (
    id UUID PRIMARY KEY,
    field_id UUID NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    overall_severity VARCHAR(32) NOT NULL DEFAULT 'LOW',
    overall_confidence VARCHAR(32) NOT NULL DEFAULT 'INSUFFICIENT_DATA',
    risk_trend VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    risk_score NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    is_hydraulic_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    hydraulic_block_reason VARCHAR(500),
    primary_risk_category VARCHAR(64) NOT NULL DEFAULT 'WATER_AVAILABILITY',
    lifecycle_status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    priority_rank INT NOT NULL DEFAULT 9,
    priority_reason VARCHAR(500) NOT NULL,
    risk_factors_json TEXT NOT NULL,
    recommendations_json TEXT NOT NULL,
    missing_data_warnings_json TEXT,
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    acknowledged_by UUID,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by UUID,
    resolution_notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_risk_assessment_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_risk_assessments_field_evaluated ON farm_risk_assessments(field_id, evaluated_at DESC);
CREATE INDEX IF NOT EXISTS idx_risk_assessments_status ON farm_risk_assessments(lifecycle_status);
CREATE INDEX IF NOT EXISTS idx_risk_assessments_severity ON farm_risk_assessments(overall_severity);
CREATE INDEX IF NOT EXISTS idx_risk_assessments_evaluated ON farm_risk_assessments(evaluated_at DESC);
