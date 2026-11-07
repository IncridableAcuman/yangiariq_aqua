-- liquibase formatted sql

-- changeset aqua-ai:040-create-water-accounting-tables
-- validCheckSum: ANY
-- comment: Milestone 08.15: Tables for regional water accounting ledger and reconciliation

CREATE TABLE IF NOT EXISTS water_ledger_entries (
    id UUID PRIMARY KEY,
    entry_type VARCHAR(64) NOT NULL,
    accounting_boundary VARCHAR(64) NOT NULL,
    volume_m3 NUMERIC(14, 4) NOT NULL,
    flow_rate_m3_s NUMERIC(8, 4),
    source_node_or_canal VARCHAR(255),
    destination_node_or_field VARCHAR(255),
    field_id UUID,
    wua_id UUID,
    edge_id UUID,
    dispatch_schedule_id UUID,
    allocation_id UUID,
    event_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    evidence_classification VARCHAR(32) NOT NULL DEFAULT 'CALCULATED',
    data_source VARCHAR(128) NOT NULL,
    notes VARCHAR(1000),
    recorded_by UUID,
    is_correction BOOLEAN NOT NULL DEFAULT FALSE,
    replaces_entry_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ledger_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE SET NULL,
    CONSTRAINT fk_ledger_wua FOREIGN KEY (wua_id) REFERENCES water_user_associations(id) ON DELETE SET NULL,
    CONSTRAINT fk_ledger_edge FOREIGN KEY (edge_id) REFERENCES water_network_edges(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS water_reconciliations (
    id UUID PRIMARY KEY,
    boundary_type VARCHAR(64) NOT NULL,
    boundary_name VARCHAR(255) NOT NULL,
    target_id UUID,
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    expected_volume_m3 NUMERIC(14, 4) NOT NULL DEFAULT 0.0000,
    recorded_volume_m3 NUMERIC(14, 4) NOT NULL DEFAULT 0.0000,
    discrepancy_volume_m3 NUMERIC(14, 4) NOT NULL DEFAULT 0.0000,
    discrepancy_percent NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    tolerance_percent NUMERIC(5, 2) NOT NULL DEFAULT 5.00,
    status VARCHAR(32) NOT NULL DEFAULT 'RECONCILED',
    evidence_classification VARCHAR(32) NOT NULL DEFAULT 'CALCULATED',
    explanation VARCHAR(1000),
    reviewed_by UUID,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    review_notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ledger_field ON water_ledger_entries(field_id);
CREATE INDEX IF NOT EXISTS idx_ledger_wua ON water_ledger_entries(wua_id);
CREATE INDEX IF NOT EXISTS idx_ledger_edge ON water_ledger_entries(edge_id);
CREATE INDEX IF NOT EXISTS idx_ledger_timestamp ON water_ledger_entries(event_timestamp);
CREATE INDEX IF NOT EXISTS idx_ledger_entry_type ON water_ledger_entries(entry_type);
CREATE INDEX IF NOT EXISTS idx_ledger_boundary ON water_ledger_entries(accounting_boundary);

CREATE INDEX IF NOT EXISTS idx_reconciliation_boundary ON water_reconciliations(boundary_type);
CREATE INDEX IF NOT EXISTS idx_reconciliation_status ON water_reconciliations(status);
CREATE INDEX IF NOT EXISTS idx_reconciliation_period ON water_reconciliations(period_start, period_end);
