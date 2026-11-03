# Predictive Model Evaluation, Accuracy & Continuous Intelligence (Milestone 08.8)

## 1. Executive Summary & Objective

**Milestone 08.8** establishes the **Continuous Intelligence & Verification Loop** for the **YANGIARIQ AQUA AI** platform.

While Milestone 08.7 answered:
> *"What is likely to happen next?"*

Milestone 08.8 answers the vital empirical and operational questions:
> *"How accurate was that prediction?"*  
> *"Where did the prediction succeed, and where did it fail?"*  
> *"Why did it fail (agronomic model, sensor omission, or hydraulic safety block)?"*  
> *"Can we trust this prediction type under current field conditions?"*  
> *"Is the predictive system improving or degrading over time?"*

```
DATA & TELEMETRY
       ↓
PREDICTION (Target Horizon)
       ↓
REAL OPERATIONAL OUTCOME
       ↓
OUTCOME MATCHING (Authoritative Window)
       ↓
NUMERICAL ERROR & CLASSIFICATION
       ↓
MODEL HEALTH & CONTINUOUS INTELLIGENCE
       ↓
ACTIONABLE AGRONOMIC & CALIBRATION INSIGHTS
```

---

## 2. Mandatory Core Principle: Absolute Honesty in Evaluation

1. **Zero Fabrication**: Model performance, accuracy scores, and error rates are **never** fabricated, hard-coded, or simulated.
2. **Sample-Size Gating ($n \ge 10$)**:
   - If historical verified predictions for a model type or field are below $n = 10$, the system displays:
     > *"Insufficient data for evaluation ($n = X$, minimum required: 10). Performance metrics are honestly gated to prevent false confidence."*
   - In field-level breakdowns, records with $n < 5$ display `limited sample (n < 5)`.
3. **Trust Over Impressive Numbers**: Agronomic decisions in Aral Sea basin agriculture (Khorezm region) carry real yield and water security consequences. A transparent system stating "Insufficient data" is infinitely more trustworthy than an inflated "94% Accuracy".

---

## 3. Hexagonal Architecture & Bounded Context

The evaluation subsystem resides strictly inside the bounded context:
```
com.aquaai.prediction
```

It is governed by strict ArchUnit rules (Rules 66–72) ensuring zero unauthorized external imports, zero framework coupling in domain models, and complete multi-tenant isolation.

```
                          ┌─────────────────────────────────────────────────────────┐
                          │                      REST API LAYER                     │
                          │   PredictionController (/api/v1/predictions/evaluation) │
                          └────────────────────────────┬────────────────────────────┘
                                                       │
                                                       ▼
                          ┌─────────────────────────────────────────────────────────┐
                          │                 APPLICATION USE CASES                   │
                          │   • EvaluatePendingPredictionsUseCase (Batch runner)    │
                          │   • GetPredictionEvaluationOverviewUseCase              │
                          │   • GetPredictionEvaluationByTypeUseCase                │
                          │   • GetPredictionEvaluationByFieldUseCase               │
                          │   • GetPredictionEvaluationHistoryUseCase               │
                          │   • GetPredictionDetailEvaluationUseCase                │
                          └────────────────────────────┬────────────────────────────┘
                                                       │
                                                       ▼
                          ┌─────────────────────────────────────────────────────────┐
                          │                      DOMAIN CORE                        │
                          │   Entities & Aggregates:                                │
                          │     • PredictionEvaluation (Immutable Audit Aggregate)  │
                          │   Value Objects:                                        │
                          │     • NumericalEvaluationMetrics                        │
                          │     • ClassificationEvaluationMetrics                   │
                          │     • EvaluationConfidenceTier                          │
                          │     • EvaluationDataQualityImpact                       │
                          │     • EvaluationInsight                                 │
                          │     • HumanDecisionImpact                               │
                          │   Domain Services:                                      │
                          │     • PredictionOutcomeMatcher                          │
                          │     • PredictionEvaluationAggregator                    │
                          │     • EvaluationInsightGenerator                        │
                          └────────────────────────────┬────────────────────────────┘
                                                       │
                                                       ▼
                          ┌─────────────────────────────────────────────────────────┐
                          │               PORTS & PERSISTENCE ADAPTERS              │
                          │   Ports:                                                │
                          │     • PredictionEvaluationRepository                    │
                          │     • PredictionDataProvider                            │
                          │   Adapters:                                             │
                          │     • PredictionEvaluationRepositoryImpl                │
                          │     • SpringDataPredictionEvaluationRepository          │
                          │     • PredictionEvaluationJpaEntity                     │
                          │   Database:                                             │
                          │     • Liquibase Migration 016 (PostgreSQL 16)           │
                          │     • Table: prediction_evaluations                     │
                          └─────────────────────────────────────────────────────────┘
```

---

## 4. Evaluation Lifecycle & Idempotency

### 4.1 Evaluation States (`EvaluationStatus`)
- **`PENDING`**: Target time has not yet passed, or evaluation horizon is still open.
- **`EVALUATED`**: Target outcome was successfully observed and evaluated against prediction.
- **`INSUFFICIENT_DATA`**: Horizon has passed, but missing or stale sensor telemetry prevents definitive numerical verification.
- **`OUTCOME_INTERRUPTED`**: An operational safety event (e.g. hydraulic safety interlock block) prevented planned execution.
- **`EXPIRED`**: Target time passed beyond maximum evaluation horizon without qualifying observations.
- **`INVALID`**: Model input or sensor data was corrupted during prediction horizon.

### 4.2 Idempotency & Database Integrity
The database schema enforces strict idempotency:
```sql
ALTER TABLE prediction_evaluations ADD CONSTRAINT uk_prediction_eval_pred_id UNIQUE (prediction_id);
```
- Re-running batch evaluations will **never** generate duplicate evaluation rows for the same prediction.
- If an evaluation already exists, `save()` updates the existing record or skips without error.

---

## 5. Continuous Intelligence & Diagnostic Insights

The domain service `EvaluationInsightGenerator` evaluates aggregated error vectors and generates deterministic, audit-backed recommendations:

1. **Systematic Bias Insight (`BIAS`)**:
   - Detects persistent over-prediction ($MBE > +8.0$) or under-prediction ($MBE < -8.0$).
   - Recommends agronomic tuning of crop coefficient ($K_c$) or reference evapotranspiration ($ET_0$).
2. **Operational Safety Blocks Recognized (`SAFETY`)**:
   - Explicitly accounts for planned irrigations halted by hydraulic interlocks.
   - Prevents artificial penalty to predictive model accuracy.
3. **Telemetry Bottlenecks (`TELEMETRY`)**:
   - Flags fields where $\ge 3$ evaluations resulted in `INSUFFICIENT_DATA` due to probe battery or LoRaWAN gateway dropouts.
4. **Calibration Inversion Alert (`CALIBRATION`)**:
   - Flags anomalies where High-Confidence predictions exhibited larger error variance than Low-Confidence predictions.

---

## 6. Transition to Milestone 08.9: From Insight to Controlled Calibration

In Milestone 08.9, the diagnostic insights generated above become the actionable input for **Controlled Model Governance**:

$$\text{Diagnostic Insight (MBE Drift)} \longrightarrow \text{Calibration Proposal (V2)} \longrightarrow \text{Anti-Leakage Backtest} \longrightarrow \text{Admin Review} \longrightarrow \text{Controlled Rollout}$$

- Evaluations link directly to `method_version_tag` and `configuration_snapshot`.
- When an insight indicates persistent bias, the system or operator generates a `CalibrationProposal` specifying the candidate configuration.
- The backtesting engine deterministic validates candidate parameters against historical ground-truth evaluations before any proposal can be approved.
- Production activation requires explicit human sign-off (`ROLE_ADMIN`).

