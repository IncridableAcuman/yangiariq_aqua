package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.PredictionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain Service enforcing regression checks across candidate calibrations.
 * <p>
 * Ensures that an improvement in a single primary metric (e.g. MAE) does not mask
 * dangerous regressions in secondary metrics (e.g. false negative spike, severe bias inflation).
 */
public class RegressionGuardService {

    public record RegressionCheckResult(
            boolean regressionDetected,
            String details
    ) {}

    public RegressionCheckResult evaluateRegression(
            PredictionType type,
            BigDecimal currentMae,
            BigDecimal candidateMae,
            BigDecimal currentMbe,
            BigDecimal candidateMbe,
            BigDecimal currentAccuracy,
            BigDecimal candidateAccuracy,
            BigDecimal currentF1,
            BigDecimal candidateF1
    ) {
        List<String> regressions = new ArrayList<>();

        // 1. Bias Inflation Check (MBE)
        if (currentMbe != null && candidateMbe != null) {
            BigDecimal currentAbsBias = currentMbe.abs();
            BigDecimal candidateAbsBias = candidateMbe.abs();
            if (candidateAbsBias.compareTo(currentAbsBias.add(BigDecimal.valueOf(3.0))) > 0) {
                regressions.add(String.format("Candidate systematically inflates bias from %s to %s units.", currentMbe, candidateMbe));
            }
        }

        // 2. Classification Accuracy / F1 Regression
        if (currentAccuracy != null && candidateAccuracy != null) {
            if (candidateAccuracy.compareTo(currentAccuracy.subtract(BigDecimal.valueOf(5.0))) < 0) {
                regressions.add(String.format("Candidate reduces classification accuracy by >5%% (%s%% -> %s%%).", currentAccuracy, candidateAccuracy));
            }
        }

        if (currentF1 != null && candidateF1 != null) {
            if (candidateF1.compareTo(currentF1.subtract(BigDecimal.valueOf(0.08))) < 0) {
                regressions.add(String.format("Candidate reduces F1 score significantly (%s -> %s).", currentF1, candidateF1));
            }
        }

        // 3. Regression Error Check (if candidate MAE is worse than current)
        if (currentMae != null && candidateMae != null) {
            if (candidateMae.compareTo(currentMae) > 0) {
                regressions.add(String.format("Candidate increases Mean Absolute Error (MAE) from %s to %s.", currentMae, candidateMae));
            }
        }

        boolean hasRegression = !regressions.isEmpty();
        String details = hasRegression ? String.join(" | ", regressions) : "No regressions detected across primary and secondary metrics.";
        return new RegressionCheckResult(hasRegression, details);
    }
}
