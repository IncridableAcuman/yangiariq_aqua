-- liquibase formatted sql

-- changeset aquaai:020-create-recommendation-domain-tables
-- validCheckSum: ANY
-- comment: Milestone 08.1: Create tables for AI agronomic recommendations, evidence, and actions

CREATE TABLE IF NOT EXISTS recommendations (
    id UUID PRIMARY KEY,
    field_id UUID NOT NULL REFERENCES fields(id) ON DELETE CASCADE,
    recommendation_type VARCHAR(64) NOT NULL,
    priority VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'GENERATED',
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    explanation TEXT NOT NULL,
    confidence NUMERIC(5, 4) NOT NULL,
    confidence_level VARCHAR(32) NOT NULL,
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_until TIMESTAMP WITH TIME ZONE NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    source VARCHAR(64) NOT NULL DEFAULT 'HYBRID_AI',
    limitations TEXT,
    is_simulated BOOLEAN NOT NULL DEFAULT FALSE,
    viewed_at TIMESTAMP WITH TIME ZONE,
    viewed_by UUID,
    accepted_at TIMESTAMP WITH TIME ZONE,
    accepted_by UUID,
    dismissed_at TIMESTAMP WITH TIME ZONE,
    dismissed_by UUID,
    dismissal_reason VARCHAR(500),
    completed_at TIMESTAMP WITH TIME ZONE,
    completed_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_recommendations_field_status ON recommendations(field_id, status);
CREATE INDEX IF NOT EXISTS idx_recommendations_priority ON recommendations(priority);
CREATE INDEX IF NOT EXISTS idx_recommendations_valid_until ON recommendations(valid_until);
CREATE INDEX IF NOT EXISTS idx_recommendations_active_dedup ON recommendations(field_id, recommendation_type, status);

-- changeset aquaai:020b-create-recommendation-postgresql-unique-index dbms:postgresql
-- validCheckSum: ANY
-- comment: PostgreSQL-specific unique partial index for active recommendation deduplication
CREATE UNIQUE INDEX IF NOT EXISTS uq_active_recommendation_dedup 
ON recommendations(field_id, recommendation_type) 
WHERE status IN ('GENERATED', 'VIEWED', 'ACCEPTED');

-- changeset aquaai:020c-create-recommendation-evidence-and-actions
-- validCheckSum: ANY
-- comment: Tables for recommendation corroborating evidence and action steps

CREATE TABLE IF NOT EXISTS recommendation_evidence (
    id UUID PRIMARY KEY,
    recommendation_id UUID NOT NULL REFERENCES recommendations(id) ON DELETE CASCADE,
    evidence_type VARCHAR(64) NOT NULL,
    metric VARCHAR(64) NOT NULL,
    observed_value NUMERIC(14, 4),
    baseline_value NUMERIC(14, 4),
    unit VARCHAR(32),
    summary VARCHAR(255) NOT NULL,
    source_reference VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_recommendation_evidence_rec_id ON recommendation_evidence(recommendation_id);

CREATE TABLE IF NOT EXISTS recommendation_actions (
    id UUID PRIMARY KEY,
    recommendation_id UUID NOT NULL REFERENCES recommendations(id) ON DELETE CASCADE,
    action_order INT NOT NULL,
    action_text VARCHAR(500) NOT NULL,
    urgency VARCHAR(32) NOT NULL DEFAULT 'ROUTINE',
    is_negative_action BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_recommendation_actions_rec_id ON recommendation_actions(recommendation_id);
