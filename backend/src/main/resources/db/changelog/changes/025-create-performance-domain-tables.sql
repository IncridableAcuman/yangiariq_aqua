-- liquibase formatted sql

-- changeset aqua-ai:042-create-performance-domain-tables
-- validCheckSum: ANY
-- comment: Milestone 08.17: Tables for regional water operations performance, outcome measurement, baselines, targets, evaluations, and impact assessment

CREATE TABLE IF NOT EXISTS performance_indicators (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    category VARCHAR(64) NOT NULL,
    measurement_unit VARCHAR(32) NOT NULL,
    calculation_method VARCHAR(1000) NOT NULL,
    aggregation_method VARCHAR(32) NOT NULL,
    supported_geographic_scope VARCHAR(64) NOT NULL,
    evidence_requirement VARCHAR(64) NOT NULL,
    minimum_data_quality_pct DECIMAL(5, 2) NOT NULL DEFAULT 80.00,
    applicable_time_period VARCHAR(32) NOT NULL DEFAULT 'MONTHLY',
    source_module VARCHAR(64) NOT NULL,
    calculation_version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_perf_ind_category ON performance_indicators(category);
CREATE INDEX IF NOT EXISTS idx_perf_ind_source_module ON performance_indicators(source_module);

CREATE TABLE IF NOT EXISTS performance_baselines (
    id UUID PRIMARY KEY,
    indicator_code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    scope_type VARCHAR(64) NOT NULL,
    scope_id UUID,
    scope_name VARCHAR(255) NOT NULL,
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    baseline_value DECIMAL(18, 4) NOT NULL,
    measurement_unit VARCHAR(32) NOT NULL,
    data_completeness_pct DECIMAL(5, 2) NOT NULL DEFAULT 100.00,
    evidence_classification VARCHAR(32) NOT NULL DEFAULT 'CALCULATED',
    baseline_quality VARCHAR(32) NOT NULL DEFAULT 'VERIFIED_BASELINE',
    calculation_method VARCHAR(500) NOT NULL,
    season_context VARCHAR(64),
    approval_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED',
    approved_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_perf_baseline_indicator FOREIGN KEY (indicator_code) REFERENCES performance_indicators(code) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_perf_baseline_ind_scope ON performance_baselines(indicator_code, scope_type, scope_id);
CREATE INDEX IF NOT EXISTS idx_perf_baseline_period ON performance_baselines(period_start, period_end);

CREATE TABLE IF NOT EXISTS performance_targets (
    id UUID PRIMARY KEY,
    indicator_code VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    scope_type VARCHAR(64) NOT NULL,
    scope_id UUID,
    scope_name VARCHAR(255) NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_category VARCHAR(32) NOT NULL DEFAULT 'OPERATIONAL_TARGET',
    target_value DECIMAL(18, 4) NOT NULL,
    range_min DECIMAL(18, 4),
    range_max DECIMAL(18, 4),
    measurement_unit VARCHAR(32) NOT NULL,
    effective_start TIMESTAMP WITH TIME ZONE NOT NULL,
    effective_end TIMESTAMP WITH TIME ZONE,
    target_owner VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'APPROVED',
    rationale VARCHAR(2000),
    target_version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_perf_target_indicator FOREIGN KEY (indicator_code) REFERENCES performance_indicators(code) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_perf_target_ind_scope ON performance_targets(indicator_code, scope_type, scope_id);
CREATE INDEX IF NOT EXISTS idx_perf_target_status ON performance_targets(status);

CREATE TABLE IF NOT EXISTS performance_evaluations (
    id UUID PRIMARY KEY,
    evaluation_number VARCHAR(32) NOT NULL UNIQUE,
    indicator_code VARCHAR(64) NOT NULL,
    scope_type VARCHAR(64) NOT NULL,
    scope_id UUID,
    scope_name VARCHAR(255) NOT NULL,
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    calculated_value DECIMAL(18, 4) NOT NULL,
    measurement_unit VARCHAR(32) NOT NULL,
    baseline_id UUID,
    baseline_value DECIMAL(18, 4),
    target_id UUID,
    target_value DECIMAL(18, 4),
    target_compliance VARCHAR(32) NOT NULL DEFAULT 'MET',
    variance_from_baseline_pct DECIMAL(8, 2),
    variance_from_target_pct DECIMAL(8, 2),
    trend VARCHAR(32) NOT NULL DEFAULT 'STABLE',
    evidence_classification VARCHAR(32) NOT NULL DEFAULT 'CALCULATED',
    observed_sample_ratio DECIMAL(5, 2) NOT NULL DEFAULT 100.00,
    missing_data_intervals INT NOT NULL DEFAULT 0,
    comparison_valid BOOLEAN NOT NULL DEFAULT TRUE,
    validity_notes VARCHAR(1000),
    explanatory_summary VARCHAR(2000) NOT NULL,
    calculation_version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_perf_eval_indicator FOREIGN KEY (indicator_code) REFERENCES performance_indicators(code) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_perf_eval_ind_period ON performance_evaluations(indicator_code, period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_perf_eval_scope ON performance_evaluations(scope_type, scope_id);

CREATE TABLE IF NOT EXISTS impact_assessments (
    id UUID PRIMARY KEY,
    assessment_number VARCHAR(32) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    intervention_type VARCHAR(64) NOT NULL,
    intervention_id UUID NOT NULL,
    intervention_reference VARCHAR(255) NOT NULL,
    intervention_date TIMESTAMP WITH TIME ZONE NOT NULL,
    indicator_code VARCHAR(64) NOT NULL,
    scope_type VARCHAR(64) NOT NULL,
    scope_id UUID,
    scope_name VARCHAR(255) NOT NULL,
    baseline_id UUID,
    baseline_period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    baseline_period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    baseline_value DECIMAL(18, 4) NOT NULL,
    evaluation_period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    evaluation_period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    post_intervention_value DECIMAL(18, 4) NOT NULL,
    absolute_change DECIMAL(18, 4) NOT NULL,
    percentage_change DECIMAL(8, 2) NOT NULL,
    change_direction VARCHAR(32) NOT NULL DEFAULT 'FAVORABLE',
    measurement_unit VARCHAR(32) NOT NULL,
    evidence_classification VARCHAR(32) NOT NULL DEFAULT 'CALCULATED',
    baseline_quality VARCHAR(32) NOT NULL DEFAULT 'VERIFIED_BASELINE',
    attribution_level VARCHAR(32) NOT NULL DEFAULT 'STRONGLY_ASSOCIATED',
    confounding_factors VARCHAR(2000),
    data_limitations VARCHAR(2000),
    comparison_valid BOOLEAN NOT NULL DEFAULT TRUE,
    methodology_notes VARCHAR(2000) NOT NULL,
    review_status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    reviewer_id UUID,
    reviewer_name VARCHAR(255),
    review_notes VARCHAR(2000),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_perf_impact_indicator FOREIGN KEY (indicator_code) REFERENCES performance_indicators(code) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_perf_impact_intervention ON impact_assessments(intervention_type, intervention_id);
CREATE INDEX IF NOT EXISTS idx_perf_impact_attribution ON impact_assessments(attribution_level);
CREATE INDEX IF NOT EXISTS idx_perf_impact_review_status ON impact_assessments(review_status);
CREATE INDEX IF NOT EXISTS idx_perf_impact_scope ON impact_assessments(scope_type, scope_id);
