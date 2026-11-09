-- liquibase formatted sql

-- changeset aqua-ai:027-1-create-pilot-tables
-- comment: Create pilot programs and participant tables for Milestone 08.20

CREATE TABLE IF NOT EXISTS pilot_programs (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    geographic_scope VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    organization VARCHAR(255) NOT NULL,
    owner_id UUID NOT NULL,
    operational_objectives TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    approved_by UUID,
    approved_at TIMESTAMP WITH TIME ZONE,
    approval_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pilot_owner FOREIGN KEY (owner_id) REFERENCES users(id),
    CONSTRAINT fk_pilot_approver FOREIGN KEY (approved_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS pilot_participants (
    id UUID PRIMARY KEY,
    pilot_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role_in_pilot VARCHAR(64) NOT NULL,
    assigned_field_ids TEXT,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_participant_pilot FOREIGN KEY (pilot_id) REFERENCES pilot_programs(id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_participant_pilot_user UNIQUE (pilot_id, user_id)
);

CREATE TABLE IF NOT EXISTS pilot_fields (
    id UUID PRIMARY KEY,
    pilot_id UUID NOT NULL,
    field_id UUID NOT NULL,
    monitoring_target VARCHAR(255) NOT NULL,
    measurement_plan TEXT NOT NULL,
    validation_criteria TEXT NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pilot_field_pilot FOREIGN KEY (pilot_id) REFERENCES pilot_programs(id) ON DELETE CASCADE,
    CONSTRAINT fk_pilot_field_field FOREIGN KEY (field_id) REFERENCES fields(id),
    CONSTRAINT uk_pilot_field UNIQUE (pilot_id, field_id)
);

CREATE TABLE IF NOT EXISTS pilot_readiness_assessments (
    id UUID PRIMARY KEY,
    pilot_id UUID NOT NULL,
    overall_status VARCHAR(32) NOT NULL,
    infrastructure_status VARCHAR(32) NOT NULL,
    data_status VARCHAR(32) NOT NULL,
    operational_status VARCHAR(32) NOT NULL,
    governance_status VARCHAR(32) NOT NULL,
    checks_json TEXT NOT NULL,
    assessed_by UUID NOT NULL,
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_readiness_pilot FOREIGN KEY (pilot_id) REFERENCES pilot_programs(id) ON DELETE CASCADE,
    CONSTRAINT fk_readiness_assessor FOREIGN KEY (assessed_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS ground_truth_validations (
    id UUID PRIMARY KEY,
    pilot_id UUID NOT NULL,
    field_id UUID NOT NULL,
    measurement_type VARCHAR(64) NOT NULL,
    measured_value DOUBLE PRECISION NOT NULL,
    canonical_unit VARCHAR(32) NOT NULL,
    measured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    collection_method VARCHAR(128) NOT NULL,
    measurement_source VARCHAR(128) NOT NULL,
    submitted_by UUID NOT NULL,
    verification_status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    verifier_id UUID,
    evidence_reference VARCHAR(255),
    notes TEXT,
    verified_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    CONSTRAINT fk_gt_pilot FOREIGN KEY (pilot_id) REFERENCES pilot_programs(id) ON DELETE CASCADE,
    CONSTRAINT fk_gt_field FOREIGN KEY (field_id) REFERENCES fields(id),
    CONSTRAINT fk_gt_submitter FOREIGN KEY (submitted_by) REFERENCES users(id),
    CONSTRAINT fk_gt_verifier FOREIGN KEY (verifier_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS pilot_prediction_validations (
    id UUID PRIMARY KEY,
    pilot_id UUID NOT NULL,
    field_id UUID NOT NULL,
    prediction_id VARCHAR(128),
    model_name VARCHAR(128) NOT NULL,
    model_version VARCHAR(64) NOT NULL,
    predicted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    prediction_horizon_hours INTEGER NOT NULL,
    predicted_value DOUBLE PRECISION NOT NULL,
    actual_value DOUBLE PRECISION NOT NULL,
    ground_truth_id UUID,
    absolute_error DOUBLE PRECISION NOT NULL,
    relative_error_percentage DOUBLE PRECISION,
    evaluation_status VARCHAR(32) NOT NULL,
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pred_val_pilot FOREIGN KEY (pilot_id) REFERENCES pilot_programs(id) ON DELETE CASCADE,
    CONSTRAINT fk_pred_val_field FOREIGN KEY (field_id) REFERENCES fields(id),
    CONSTRAINT fk_pred_val_gt FOREIGN KEY (ground_truth_id) REFERENCES ground_truth_validations(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS pilot_operator_feedback (
    id UUID PRIMARY KEY,
    pilot_id UUID NOT NULL,
    farm_id UUID,
    field_id UUID,
    sensor_id UUID,
    data_source_id UUID,
    incident_id UUID,
    category VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    submitted_by UUID NOT NULL,
    assigned_to UUID,
    resolution_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_pilot FOREIGN KEY (pilot_id) REFERENCES pilot_programs(id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE SET NULL,
    CONSTRAINT fk_feedback_submitter FOREIGN KEY (submitted_by) REFERENCES users(id),
    CONSTRAINT fk_feedback_assignee FOREIGN KEY (assigned_to) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS pilot_evaluation_snapshots (
    id UUID PRIMARY KEY,
    pilot_id UUID NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    data_performance_json TEXT NOT NULL,
    prediction_performance_json TEXT NOT NULL,
    operational_performance_json TEXT NOT NULL,
    water_outcomes_json TEXT NOT NULL,
    limitations_text TEXT NOT NULL,
    recommendations_text TEXT NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eval_pilot FOREIGN KEY (pilot_id) REFERENCES pilot_programs(id) ON DELETE CASCADE,
    CONSTRAINT fk_eval_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Indices for high-frequency queries
CREATE INDEX IF NOT EXISTS idx_pilot_programs_status ON pilot_programs(status);
CREATE INDEX IF NOT EXISTS idx_pilot_participants_user ON pilot_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_pilot_fields_field ON pilot_fields(field_id);
CREATE INDEX IF NOT EXISTS idx_gt_validations_pilot_status ON ground_truth_validations(pilot_id, verification_status);
CREATE INDEX IF NOT EXISTS idx_gt_validations_field_type ON ground_truth_validations(field_id, measurement_type);
CREATE INDEX IF NOT EXISTS idx_pred_validations_pilot ON pilot_prediction_validations(pilot_id);
CREATE INDEX IF NOT EXISTS idx_operator_feedback_pilot_status ON pilot_operator_feedback(pilot_id, status);
CREATE INDEX IF NOT EXISTS idx_eval_snapshots_pilot ON pilot_evaluation_snapshots(pilot_id);
