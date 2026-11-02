package com.aquaai.prediction.domain.model;

/**
 * Status lifecycle of a predictive calibration proposal.
 */
public enum ProposalStatus {
    DRAFT,
    BACKTESTING,
    REVIEW_REQUIRED,
    APPROVED,
    REJECTED,
    EXPIRED,
    ACTIVE,
    ROLLED_BACK
}
