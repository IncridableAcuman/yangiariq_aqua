package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.BaselineComparisonMetrics;
import com.aquaai.prediction.domain.model.DatasetSplit;
import com.aquaai.prediction.domain.model.FeatureRecord;
import com.aquaai.prediction.domain.model.FeatureSchemaVersion;
import com.aquaai.prediction.domain.model.ModelMetrics;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Pure JVM-native L2-Regularized Multiple Linear Regression (Ridge Regression) solver.
 * Completely deterministic, reproducible, and mathematically rigorous.
 * Zero external library dependencies, zero C-bindings, zero native runtime requirements.
 */
public class RidgeRegressionTrainer {

    private final double l2Penalty;

    public RidgeRegressionTrainer() {
        this(1.0);
    }

    public RidgeRegressionTrainer(double l2Penalty) {
        this.l2Penalty = Math.max(1e-4, l2Penalty);
    }

    public record TrainedModelResult(
            double[] weights,
            double intercept,
            double[] featureMeans,
            double[] featureStdDevs,
            ModelMetrics trainMetrics,
            ModelMetrics validationMetrics,
            ModelMetrics testMetrics,
            ModelMetrics baselineTestMetrics,
            BaselineComparisonMetrics baselineComparison,
            String artifactHash,
            List<String> featureNames,
            String configurationJson
    ) {}

    public TrainedModelResult train(DatasetSplit split) {
        List<FeatureRecord> trainRecords = split.trainRecords();
        int nTrain = trainRecords.size();
        if (nTrain < 2) {
            throw new IllegalArgumentException("Insufficient training samples (n < 2) for regression fit.");
        }

        int m = FeatureExtractor.FEATURE_COUNT;

        // 1. Compute training set feature statistics (means and standard deviations)
        double[] means = new double[m];
        double[] stdDevs = new double[m];
        double ySum = 0.0;

        for (FeatureRecord r : trainRecords) {
            ySum += r.targetValue();
            for (int j = 0; j < m; j++) {
                means[j] += r.features()[j];
            }
        }
        double yMean = ySum / nTrain;
        for (int j = 0; j < m; j++) {
            means[j] /= nTrain;
        }

        for (FeatureRecord r : trainRecords) {
            for (int j = 0; j < m; j++) {
                double diff = r.features()[j] - means[j];
                stdDevs[j] += diff * diff;
            }
        }
        for (int j = 0; j < m; j++) {
            stdDevs[j] = Math.sqrt(stdDevs[j] / nTrain);
            if (stdDevs[j] < 1e-6) {
                stdDevs[j] = 1.0; // avoid divide-by-zero for constant features
            }
        }

        // 2. Standardize training features Z and center targets
        double[][] Z = new double[nTrain][m];
        double[] yCentered = new double[nTrain];
        for (int i = 0; i < nTrain; i++) {
            FeatureRecord r = trainRecords.get(i);
            yCentered[i] = r.targetValue() - yMean;
            for (int j = 0; j < m; j++) {
                Z[i][j] = (r.features()[j] - means[j]) / stdDevs[j];
            }
        }

        // 3. Compute A = Z^T * Z + lambda * I (m x m) and b = Z^T * yCentered (m x 1)
        double[][] A = new double[m][m];
        double[] b = new double[m];

        for (int j1 = 0; j1 < m; j1++) {
            for (int j2 = 0; j2 < m; j2++) {
                double dot = 0.0;
                for (int i = 0; i < nTrain; i++) {
                    dot += Z[i][j1] * Z[i][j2];
                }
                A[j1][j2] = dot;
            }
            A[j1][j1] += l2Penalty; // Ridge L2 regularization

            double bDot = 0.0;
            for (int i = 0; i < nTrain; i++) {
                bDot += Z[i][j1] * yCentered[i];
            }
            b[j1] = bDot;
        }

        // 4. Solve linear system A * w = b using Gaussian Elimination with partial pivoting
        double[] weights = solveLinearSystem(A, b, m);
        double intercept = yMean;

        // 5. Evaluate Metrics on Train, Validation, and Test
        ModelMetrics trainMetrics = evaluatePerformance(trainRecords, weights, intercept, means, stdDevs);
        ModelMetrics valMetrics = evaluatePerformance(split.validationRecords(), weights, intercept, means, stdDevs);
        ModelMetrics testMetrics = evaluatePerformance(split.testRecords(), weights, intercept, means, stdDevs);

        // 6. Evaluate Deterministic Baseline on the EXACT same test split
        ModelMetrics baselineTestMetrics = evaluateBaselinePerformance(split.testRecords());

        // 7. Side-by-side Baseline Comparison & Regression Guard Check
        BaselineComparisonMetrics comparison = BaselineComparisonMetrics.evaluate(baselineTestMetrics, testMetrics, 5);

        // 8. Build Deterministic Artifact Hash
        String artifactHash = computeArtifactHash(weights, intercept, means, stdDevs, l2Penalty);

        List<String> featureNames = FeatureSchemaVersion.getSoilMoistureSchemaV1().features().stream()
                .map(FeatureSchemaVersion.FeatureDefinition::name)
                .toList();

        // 9. Build JSON Configuration Snapshot
        String configJson = serializeConfiguration(weights, intercept, means, stdDevs, l2Penalty, featureNames);

        return new TrainedModelResult(
                weights,
                intercept,
                means,
                stdDevs,
                trainMetrics,
                valMetrics,
                testMetrics,
                baselineTestMetrics,
                comparison,
                artifactHash,
                featureNames,
                configJson
        );
    }

    public static double predict(double[] features, double[] weights, double intercept, double[] means, double[] stdDevs) {
        double y = intercept;
        int m = Math.min(features.length, weights.length);
        for (int j = 0; j < m; j++) {
            double sigma = (stdDevs != null && j < stdDevs.length && stdDevs[j] > 1e-6) ? stdDevs[j] : 1.0;
            double mu = (means != null && j < means.length) ? means[j] : 0.0;
            double z = (features[j] - mu) / sigma;
            y += weights[j] * z;
        }
        // Physical soil moisture clamping
        if (y < 5.0) y = 5.0;
        if (y > 55.0) y = 55.0;
        return y;
    }

    private ModelMetrics evaluatePerformance(List<FeatureRecord> records, double[] weights, double intercept, double[] means, double[] stdDevs) {
        if (records == null || records.isEmpty()) {
            return ModelMetrics.empty(0);
        }

        double sumAbsErr = 0.0;
        double sumSqErr = 0.0;
        double sumBiasErr = 0.0;
        double sumRelErr = 0.0;
        int validRelCount = 0;
        int withinBoundsCount = 0;

        double ySum = 0.0;
        for (FeatureRecord r : records) {
            ySum += r.targetValue();
        }
        double yMean = ySum / records.size();
        double ssTot = 0.0;

        for (FeatureRecord r : records) {
            double yPred = predict(r.features(), weights, intercept, means, stdDevs);
            double yTrue = r.targetValue();
            double err = yPred - yTrue;
            double absErr = Math.abs(err);

            sumAbsErr += absErr;
            sumSqErr += err * err;
            sumBiasErr += err;
            ssTot += (yTrue - yMean) * (yTrue - yMean);

            if (absErr <= 3.0) {
                withinBoundsCount++;
            }

            if (yTrue > 1e-6) {
                sumRelErr += (absErr / yTrue) * 100.0;
                validRelCount++;
            }
        }

        int n = records.size();
        double mae = sumAbsErr / n;
        double rmse = Math.sqrt(sumSqErr / n);
        double mbe = sumBiasErr / n;
        Double mape = validRelCount > 0 ? (sumRelErr / validRelCount) : null;
        double r2 = ssTot > 1e-6 ? (1.0 - (sumSqErr / ssTot)) : 0.0;
        double withinBoundsRate = (double) withinBoundsCount / n;

        // Confidence: calculated based on sample size and error bounds
        int confidence = Math.min(95, Math.max(30, (int) Math.round(100.0 - (mae * 5.0))));

        return new ModelMetrics(mae, rmse, mbe, mape, r2, n, confidence, withinBoundsRate);
    }

    private ModelMetrics evaluateBaselinePerformance(List<FeatureRecord> records) {
        if (records == null || records.isEmpty()) {
            return ModelMetrics.empty(0);
        }

        double sumAbsErr = 0.0;
        double sumSqErr = 0.0;
        double sumBiasErr = 0.0;
        double sumRelErr = 0.0;
        int validRelCount = 0;
        int withinBoundsCount = 0;

        for (FeatureRecord r : records) {
            double yPred = r.baselinePredictedValue();
            double yTrue = r.targetValue();
            double err = yPred - yTrue;
            double absErr = Math.abs(err);

            sumAbsErr += absErr;
            sumSqErr += err * err;
            sumBiasErr += err;

            if (absErr <= 3.0) {
                withinBoundsCount++;
            }
            if (yTrue > 1e-6) {
                sumRelErr += (absErr / yTrue) * 100.0;
                validRelCount++;
            }
        }

        int n = records.size();
        double mae = sumAbsErr / n;
        double rmse = Math.sqrt(sumSqErr / n);
        double mbe = sumBiasErr / n;
        Double mape = validRelCount > 0 ? (sumRelErr / validRelCount) : null;
        double withinBoundsRate = (double) withinBoundsCount / n;
        int confidence = Math.min(95, Math.max(30, (int) Math.round(100.0 - (mae * 5.0))));

        return new ModelMetrics(mae, rmse, mbe, mape, 0.0, n, confidence, withinBoundsRate);
    }

    private double[] solveLinearSystem(double[][] A, double[] b, int n) {
        double[][] M = new double[n][n];
        double[] x = new double[n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, M[i], 0, n);
            x[i] = b[i];
        }

        // Forward elimination with partial pivoting
        for (int p = 0; p < n; p++) {
            int max = p;
            for (int i = p + 1; i < n; i++) {
                if (Math.abs(M[i][p]) > Math.abs(M[max][p])) {
                    max = i;
                }
            }
            double[] tempRow = M[p];
            M[p] = M[max];
            M[max] = tempRow;

            double tempB = x[p];
            x[p] = x[max];
            x[max] = tempB;

            if (Math.abs(M[p][p]) <= 1e-9) {
                continue;
            }

            for (int i = p + 1; i < n; i++) {
                double alpha = M[i][p] / M[p][p];
                x[i] -= alpha * x[p];
                for (int j = p; j < n; j++) {
                    M[i][j] -= alpha * M[p][j];
                }
            }
        }

        // Back substitution
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < n; j++) {
                sum += M[i][j] * x[j];
            }
            if (Math.abs(M[i][i]) > 1e-9) {
                x[i] = (x[i] - sum) / M[i][i];
            } else {
                x[i] = 0.0;
            }
        }

        return x;
    }

    private String computeArtifactHash(double[] weights, double intercept, double[] means, double[] stdDevs, double lambda) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            sb.append("L2:").append(lambda).append(";INT:").append(intercept).append(";W:");
            for (double w : weights) sb.append(String.format("%.6f,", w));
            sb.append(";M:");
            for (double m : means) sb.append(String.format("%.6f,", m));
            sb.append(";S:");
            for (double s : stdDevs) sb.append(String.format("%.6f,", s));
            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return "HASH_" + Long.toHexString(Double.doubleToLongBits(intercept));
        }
    }

    private String serializeConfiguration(double[] weights, double intercept, double[] means, double[] stdDevs, double lambda, List<String> featureNames) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"modelAlgorithm\":\"RIDGE_REGRESSION\",");
        sb.append("\"l2Penalty\":").append(lambda).append(",");
        sb.append("\"intercept\":").append(String.format(java.util.Locale.US, "%.4f", intercept)).append(",");
        sb.append("\"maxResidualCorrection\":5.0,");
        sb.append("\"weights\":[");
        for (int i = 0; i < weights.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format(java.util.Locale.US, "%.6f", weights[i]));
        }
        sb.append("],\"featureMeans\":[");
        for (int i = 0; i < means.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format(java.util.Locale.US, "%.6f", means[i]));
        }
        sb.append("],\"featureStdDevs\":[");
        for (int i = 0; i < stdDevs.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format(java.util.Locale.US, "%.6f", stdDevs[i]));
        }
        sb.append("],\"featureImportance\":{");
        for (int i = 0; i < Math.min(weights.length, featureNames.size()); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(featureNames.get(i)).append("\":")
                    .append(String.format(java.util.Locale.US, "%.4f", Math.abs(weights[i])));
        }
        sb.append("}}");
        return sb.toString();
    }
}
