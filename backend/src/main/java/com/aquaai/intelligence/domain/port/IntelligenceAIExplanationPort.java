package com.aquaai.intelligence.domain.port;

import com.aquaai.intelligence.domain.model.AgronomicAssessment;

/**
 * Domain port for optional non-authoritative advisory commentary.
 * Implementations remain in the infrastructure layer.
 */
public interface IntelligenceAIExplanationPort {

    String generateAdvisoryExplanation(AgronomicAssessment assessment);
}
