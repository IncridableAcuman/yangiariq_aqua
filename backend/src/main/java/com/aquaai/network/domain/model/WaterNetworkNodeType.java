package com.aquaai.network.domain.model;

/**
 * Types of nodes within the district water distribution network graph.
 */
public enum WaterNetworkNodeType {
    WATER_SOURCE,
    RESERVOIR,
    PUMP_STATION,
    CANAL_JUNCTION,
    CONTROL_GATE,
    DISTRIBUTION_POINT,
    FIELD_INLET,
    TERMINAL_NODE
}
