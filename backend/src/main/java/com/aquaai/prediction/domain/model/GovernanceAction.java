package com.aquaai.prediction.domain.model;

/**
 * Traceable governance decisions logged for accountability and auditability.
 */
public enum GovernanceAction {
    PROPOSAL_CREATED,
    BACKTEST_EXECUTED,
    PROPOSAL_APPROVED,
    PROPOSAL_REJECTED,
    VERSION_SHADOW_STARTED,
    VERSION_CANARY_STARTED,
    VERSION_ACTIVATED,
    VERSION_ROLLBACK
}
