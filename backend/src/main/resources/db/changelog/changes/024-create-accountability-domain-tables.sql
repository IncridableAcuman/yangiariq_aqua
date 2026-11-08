-- liquibase formatted sql

-- changeset aqua-ai:041-create-accountability-domain-tables
-- validCheckSum: ANY
-- comment: Milestone 08.16: Tables for regional water accountability, incident investigation, corrective actions, and independent verification

CREATE TABLE IF NOT EXISTS water_incidents (
    id UUID PRIMARY KEY,
    incident_number VARCHAR(32) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    category VARCHAR(64) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    confidence VARCHAR(32) NOT NULL DEFAULT 'UNVERIFIED',
    status VARCHAR(32) NOT NULL DEFAULT 'DETECTED',
    source_module VARCHAR(64) NOT NULL,
    source_entity_type VARCHAR(64) NOT NULL,
    source_entity_id UUID,
    finding_reference VARCHAR(255),
    wua_id UUID,
    canal_id UUID,
    field_id UUID,
    location_description VARCHAR(500),
    detection_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    sla_deadline TIMESTAMP WITH TIME ZONE,
    assigned_investigator_id UUID,
    assigned_investigator_name VARCHAR(255),
    responsible_organization VARCHAR(255),
    root_cause_category VARCHAR(128),
    root_cause_details VARCHAR(2000),
    review_notes VARCHAR(2000),
    reviewed_by UUID,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    resolution_notes VARCHAR(2000),
    recurrence_count INT NOT NULL DEFAULT 0,
    parent_incident_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_incident_wua FOREIGN KEY (wua_id) REFERENCES water_user_associations(id) ON DELETE SET NULL,
    CONSTRAINT fk_incident_canal FOREIGN KEY (canal_id) REFERENCES water_network_edges(id) ON DELETE SET NULL,
    CONSTRAINT fk_incident_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE SET NULL,
    CONSTRAINT fk_incident_investigator FOREIGN KEY (assigned_investigator_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_incident_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_incident_parent FOREIGN KEY (parent_incident_id) REFERENCES water_incidents(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS incident_evidence (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL,
    evidence_type VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    evidence_uri VARCHAR(1000),
    confidence VARCHAR(32) NOT NULL DEFAULT 'UNVERIFIED',
    is_simulated BOOLEAN NOT NULL DEFAULT FALSE,
    source_reference VARCHAR(255),
    recorded_by UUID,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evidence_incident FOREIGN KEY (incident_id) REFERENCES water_incidents(id) ON DELETE CASCADE,
    CONSTRAINT fk_evidence_recorder FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS incident_corrective_actions (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL,
    action_number VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    objective VARCHAR(1000) NOT NULL,
    action_owner_id UUID,
    action_owner_name VARCHAR(255),
    priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    due_date TIMESTAMP WITH TIME ZONE,
    expected_outcome VARCHAR(1000) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PROPOSED',
    approved_by UUID,
    approved_at TIMESTAMP WITH TIME ZONE,
    approval_notes VARCHAR(1000),
    completed_by UUID,
    completed_at TIMESTAMP WITH TIME ZONE,
    completion_notes VARCHAR(2000),
    verification_requirement VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_action_incident FOREIGN KEY (incident_id) REFERENCES water_incidents(id) ON DELETE CASCADE,
    CONSTRAINT fk_action_owner FOREIGN KEY (action_owner_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_action_approver FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_action_completer FOREIGN KEY (completed_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS incident_verifications (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL,
    action_id UUID,
    verifier_id UUID NOT NULL,
    verifier_name VARCHAR(255) NOT NULL,
    verified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verification_method VARCHAR(128) NOT NULL,
    result VARCHAR(64) NOT NULL,
    explanatory_notes VARCHAR(2000) NOT NULL,
    supporting_evidence_uri VARCHAR(1000),
    CONSTRAINT fk_verification_incident FOREIGN KEY (incident_id) REFERENCES water_incidents(id) ON DELETE CASCADE,
    CONSTRAINT fk_verification_action FOREIGN KEY (action_id) REFERENCES incident_corrective_actions(id) ON DELETE SET NULL,
    CONSTRAINT fk_verification_verifier FOREIGN KEY (verifier_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS incident_timeline (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    actor_id UUID,
    actor_name VARCHAR(255),
    actor_role VARCHAR(64),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    previous_state VARCHAR(32),
    new_state VARCHAR(32),
    reason VARCHAR(2000) NOT NULL,
    correlation_id VARCHAR(128),
    metadata_json VARCHAR(4000),
    CONSTRAINT fk_timeline_incident FOREIGN KEY (incident_id) REFERENCES water_incidents(id) ON DELETE CASCADE
);

-- Indexes for performance and lookup
CREATE INDEX IF NOT EXISTS idx_incidents_status ON water_incidents(status);
CREATE INDEX IF NOT EXISTS idx_incidents_category ON water_incidents(category);
CREATE INDEX IF NOT EXISTS idx_incidents_severity ON water_incidents(severity);
CREATE INDEX IF NOT EXISTS idx_incidents_field ON water_incidents(field_id);
CREATE INDEX IF NOT EXISTS idx_incidents_wua ON water_incidents(wua_id);
CREATE INDEX IF NOT EXISTS idx_incidents_canal ON water_incidents(canal_id);
CREATE INDEX IF NOT EXISTS idx_incidents_investigator ON water_incidents(assigned_investigator_id);
CREATE INDEX IF NOT EXISTS idx_incidents_detection ON water_incidents(detection_timestamp);
CREATE INDEX IF NOT EXISTS idx_incidents_sla_deadline ON water_incidents(sla_deadline);
CREATE INDEX IF NOT EXISTS idx_incidents_source ON water_incidents(source_module, source_entity_type, source_entity_id);

CREATE INDEX IF NOT EXISTS idx_evidence_incident ON incident_evidence(incident_id);
CREATE INDEX IF NOT EXISTS idx_actions_incident ON incident_corrective_actions(incident_id);
CREATE INDEX IF NOT EXISTS idx_actions_owner ON incident_corrective_actions(action_owner_id);
CREATE INDEX IF NOT EXISTS idx_actions_status ON incident_corrective_actions(status);
CREATE INDEX IF NOT EXISTS idx_verifications_incident ON incident_verifications(incident_id);
CREATE INDEX IF NOT EXISTS idx_verifications_verifier ON incident_verifications(verifier_id);
CREATE INDEX IF NOT EXISTS idx_timeline_incident ON incident_timeline(incident_id);
CREATE INDEX IF NOT EXISTS idx_timeline_timestamp ON incident_timeline(timestamp);
