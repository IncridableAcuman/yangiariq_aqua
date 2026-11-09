package com.aquaai.pilot.domain.model;

import java.util.Objects;

public class PilotReadinessCheck {
    private final ReadinessCategory category;
    private final String name;
    private final ReadinessResult result;
    private final String reason;
    private final String supportingEvidence;
    private final boolean isMandatory;

    public PilotReadinessCheck(
            ReadinessCategory category,
            String name,
            ReadinessResult result,
            String reason,
            String supportingEvidence,
            boolean isMandatory) {
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.result = Objects.requireNonNull(result, "result must not be null");
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
        this.supportingEvidence = supportingEvidence;
        this.isMandatory = isMandatory;
    }

    public ReadinessCategory getCategory() { return category; }
    public String getName() { return name; }
    public ReadinessResult getResult() { return result; }
    public String getReason() { return reason; }
    public String getSupportingEvidence() { return supportingEvidence; }
    public boolean isMandatory() { return isMandatory; }
}
