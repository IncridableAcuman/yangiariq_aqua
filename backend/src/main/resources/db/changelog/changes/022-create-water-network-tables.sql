-- liquibase formatted sql

-- changeset aqua-ai:039-create-water-network-tables
-- validCheckSum: ANY
-- comment: Milestone 08.14: Tables for regional water distribution network, canal dispatch optimization, and resilience scenarios

CREATE TABLE IF NOT EXISTS water_user_associations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    district VARCHAR(100) NOT NULL DEFAULT 'Yangiariq',
    contact_person VARCHAR(150),
    phone_number VARCHAR(32),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS water_network_nodes (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    node_type VARCHAR(64) NOT NULL,
    latitude NUMERIC(10, 6),
    longitude NUMERIC(10, 6),
    elevation_meters NUMERIC(8, 2) DEFAULT 0.00,
    water_source_id UUID,
    field_id UUID,
    wua_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    properties_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_node_wua FOREIGN KEY (wua_id) REFERENCES water_user_associations(id) ON DELETE SET NULL,
    CONSTRAINT fk_node_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS water_network_edges (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    edge_type VARCHAR(64) NOT NULL,
    start_node_id UUID NOT NULL,
    end_node_id UUID NOT NULL,
    wua_id UUID,
    length_meters NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    max_capacity_m3_s NUMERIC(8, 4) NOT NULL,
    min_flow_m3_s NUMERIC(8, 4) NOT NULL DEFAULT 0.0000,
    current_flow_m3_s NUMERIC(8, 4) NOT NULL DEFAULT 0.0000,
    operational_status VARCHAR(32) NOT NULL DEFAULT 'OPERATIONAL',
    geometry_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_edge_start_node FOREIGN KEY (start_node_id) REFERENCES water_network_nodes(id) ON DELETE CASCADE,
    CONSTRAINT fk_edge_end_node FOREIGN KEY (end_node_id) REFERENCES water_network_nodes(id) ON DELETE CASCADE,
    CONSTRAINT fk_edge_wua FOREIGN KEY (wua_id) REFERENCES water_user_associations(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS canal_dispatch_schedules (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    target_wua_id UUID,
    target_wua_name VARCHAR(255),
    total_requested_m3 NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total_allocated_m3 NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total_unmet_m3 NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    assignments_json TEXT NOT NULL,
    conflicts_json TEXT,
    metrics_json TEXT,
    fairness_policy_json TEXT,
    notes VARCHAR(1000),
    approved_by UUID,
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason VARCHAR(1000),
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS actual_dispatch_deliveries (
    id UUID PRIMARY KEY,
    dispatch_schedule_id UUID,
    field_id UUID NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    edge_id UUID,
    canal_name VARCHAR(255),
    actual_start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    actual_end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    planned_volume_m3 NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    actual_volume_m3 NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    variance_volume_m3 NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    variance_percent NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    measured_flow_m3_s NUMERIC(8, 4) DEFAULT 0.0000,
    operator_id UUID,
    operator_notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_delivery_field FOREIGN KEY (field_id) REFERENCES fields(id) ON DELETE CASCADE,
    CONSTRAINT fk_delivery_schedule FOREIGN KEY (dispatch_schedule_id) REFERENCES canal_dispatch_schedules(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS resilience_scenarios (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    scenario_type VARCHAR(64) NOT NULL,
    disabled_node_ids_json TEXT,
    disabled_edge_ids_json TEXT,
    capacity_reduction_percent NUMERIC(5, 2) DEFAULT 0.00,
    demand_surge_percent NUMERIC(5, 2) DEFAULT 0.00,
    affected_field_count INT NOT NULL DEFAULT 0,
    lost_capacity_m3_s NUMERIC(8, 4) DEFAULT 0.0000,
    unmet_demand_m3 NUMERIC(12, 2) DEFAULT 0.00,
    affected_wua_names_json TEXT,
    alternative_feasible_routes_count INT DEFAULT 0,
    summary VARCHAR(1000),
    simulation_results_json TEXT,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_network_nodes_type ON water_network_nodes(node_type);
CREATE INDEX IF NOT EXISTS idx_network_nodes_wua ON water_network_nodes(wua_id);
CREATE INDEX IF NOT EXISTS idx_network_nodes_field ON water_network_nodes(field_id);
CREATE INDEX IF NOT EXISTS idx_network_edges_nodes ON water_network_edges(start_node_id, end_node_id);
CREATE INDEX IF NOT EXISTS idx_network_edges_wua ON water_network_edges(wua_id);
CREATE INDEX IF NOT EXISTS idx_dispatch_schedules_status ON canal_dispatch_schedules(status);
CREATE INDEX IF NOT EXISTS idx_dispatch_schedules_period ON canal_dispatch_schedules(period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_dispatch_deliveries_field ON actual_dispatch_deliveries(field_id);
CREATE INDEX IF NOT EXISTS idx_dispatch_deliveries_schedule ON actual_dispatch_deliveries(dispatch_schedule_id);
