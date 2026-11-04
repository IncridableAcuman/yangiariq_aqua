# Machine Learning Prediction Engine & Abstraction

## 1. Domain SPI & Provider Abstraction
To preserve architectural purity and prevent coupling the domain/application layer to external C/Python runtimes or heavyweight ML libraries, the domain layer defines a clean, technology-agnostic SPI:

```java
package com.aquaai.prediction.domain.port;

public interface PredictionModelProvider {
    String getProviderName();
    boolean supportsPredictionType(PredictionType type);
    double predict(PredictionType type, Map<String, Object> features, String modelCandidateId);
    boolean isHealthy();
}
```

Implementations:
- `DeterministicPredictionProvider`: Primary agronomic baseline provider implementing deterministic physical models.
- `MLRegressionPredictionModelProvider`: JVM-native regularized regression provider utilizing Ridge L2 shrinkage with standardized features and deterministic matrix solvers.

## 2. In-Process JVM-Native Ridge Regression
The candidate trainer `RidgeRegressionTrainer` executes in-process within the JVM with zero native JNI or Python subprocess overhead:
1. **Feature Standardization**: Features are centered and scaled by their training mean and standard deviation:
   $$\tilde{x}_j = \frac{x_j - \mu_j}{\sigma_j}$$
2. **L2 Regularization (Shrinkage)**:
   $$\mathbf{w} = (\mathbf{X}^T \mathbf{X} + \lambda \mathbf{I})^{-1} \mathbf{X}^T \mathbf{y}$$
   where $\lambda$ default is $1.0$.
3. **Deterministic Artifact Hash**: Every trained model produces a canonical configuration snapshot and weights vector, which is hashed via SHA-256 for bitwise verification and auditability.

## 3. Evidence-Based Training Preflight Gate
Training cannot proceed if:
- Sample size $N < 30$ verified ground-truth records across the selected historical period.
- Data quality status is `INSUFFICIENT` or contains unverified synthetic values.
- Temporal consistency checks fail.

When data is insufficient, the system explicitly returns `INSUFFICIENT_TRAINING_DATA`, honestly displaying the sample count found vs required, without fabricating mock data.
