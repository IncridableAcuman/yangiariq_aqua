-- liquibase formatted sql

-- changeset aqua-ai:036-create-adaptive-intelligence-tables
-- validCheckSum: ANY
-- comment: Milestone 08.10: Tables for context-aware adaptive intelligence method selection decisions

CREATE TABLE IF NOT EXISTS adaptive_selection_decisions (
    id UUID PRIMARY KEY,
    prediction_id UUID,
    field_id UUID NOT NULL,
    prediction_type VARCHAR(64) NOT NULL,
    selected_version_tag VARCHAR(64) NOT NULL,
    selection_mode VARCHAR(32) NOT NULL,
    selection_reason TEXT NOT NULL,
    evidence_summary TEXT,
    context_snapshot_json TEXT NOT NULL,
    alternatives_json TEXT,
    confidence_percent INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_adapt_dec_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_adapt_dec_field_type ON adaptive_selection_decisions(field_id, prediction_type, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_adapt_dec_created ON adaptive_selection_decisions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_adapt_dec_ver_tag ON adaptive_selection_decisions(selected_version_tag);
CREATE INDEX IF NOT EXISTS idx_adapt_dec_pred_id ON adaptive_selection_decisions(prediction_id);
