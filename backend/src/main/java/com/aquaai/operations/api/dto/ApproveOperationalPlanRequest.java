package com.aquaai.operations.api.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Human approval request payload enforcing mandatory constraint acknowledgement.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveOperationalPlanRequest {

    @AssertTrue(message = "Mandatory acknowledgement is required: You must review and acknowledge operational constraints.")
    private boolean acknowledgeConstraints;

    private String approvalNotes;
}
