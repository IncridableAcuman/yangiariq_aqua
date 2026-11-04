export type PredictionHorizon = 'NEXT_24_HOURS' | 'NEXT_48_HOURS' | 'NEXT_7_DAYS'

export type PredictionType =
  | 'SOIL_MOISTURE_FORECAST'
  | 'IRRIGATION_NEED_FORECAST'
  | 'WATER_DEMAND_FORECAST'
  | 'WATER_AVAILABILITY_RISK'
  | 'IRRIGATION_PLAN_RISK'
  | 'SENSOR_RELIABILITY_FORECAST'

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | 'BLOCKED' | 'UNKNOWN'

export type DataSufficiency = 'SUFFICIENT' | 'LIMITED' | 'INSUFFICIENT'

export type IrrigationLikelihood =
  | 'NOT_EXPECTED'
  | 'LOW_LIKELIHOOD'
  | 'POSSIBLE'
  | 'LIKELY'
  | 'HIGHLY_LIKELY'
  | 'UNKNOWN'

export type PredictionAccuracyStatus =
  | 'PENDING'
  | 'VERIFIED_ACCURATE'
  | 'VERIFIED_INACCURATE'
  | 'EXPIRED'
  | 'UNVERIFIABLE'

export interface PredictionItem {
  id: string
  fieldId: string
  fieldName?: string
  predictionType: PredictionType
  horizon: PredictionHorizon
  targetTime: string
  predictedValue: number | null
  lowerBound: number | null
  upperBound: number | null
  unit?: string
  confidencePercent: number
  riskLevel: RiskLevel
  dataSufficiency: DataSufficiency
  likelihood?: IrrigationLikelihood | null
  evidence?: string
  assumptions?: string
  recommendedAction?: string
  actionTargetModule?: string
  actualOutcomeValue?: number | null
  accuracyStatus?: PredictionAccuracyStatus
  generatedAt: string
  methodVersionTag?: string
  configurationSnapshot?: string
}

export interface PredictionOverview {
  horizon: PredictionHorizon
  totalParcelsCount: number
  fieldsNeedingIrrigationCount: number
  totalWaterDemandM3: number
  waterDemandLowerBoundM3: number
  waterDemandUpperBoundM3: number
  fieldsAtWaterRiskCount: number
  sensorHealthPercent: number
  activeHydraulicBlocksCount: number
  urgentForecasts: PredictionItem[]
  generatedAt: string
}

export interface FieldPredictions {
  fieldId: string
  fieldName: string
  cropType: string
  soilType: string
  irrigationMethod: string
  areaHectares: number
  horizon: PredictionHorizon
  soilMoisture?: PredictionItem | null
  irrigationNeed?: PredictionItem | null
  waterDemand?: PredictionItem | null
  waterAvailability?: PredictionItem | null
  irrigationPlanRisk?: PredictionItem | null
  sensorReliability?: PredictionItem | null
  allPredictions: PredictionItem[]
  generatedAt: string
}

export interface FieldDemandItem {
  fieldId: string
  fieldName: string
  cropType: string
  areaHectares: number
  expectedDemandM3: number
  lowerBoundM3: number
  upperBoundM3: number
  confidencePercent: number
  riskLevel: RiskLevel
  assumptions?: string
}

export interface WaterDemandForecast {
  horizon: PredictionHorizon
  totalExpectedDemandM3: number
  totalLowerBoundM3: number
  totalUpperBoundM3: number
  fieldDemands: FieldDemandItem[]
  generatedAt: string
}

export interface RankedFieldNeedItem {
  fieldId: string
  fieldName: string
  cropType: string
  areaHectares: number
  likelihood: IrrigationLikelihood
  riskLevel: RiskLevel
  currentMoisturePercent: number | null
  forecastMoisturePercent: number | null
  expectedVolumeM3: number
  confidencePercent: number
  dataSufficiency: DataSufficiency
  recommendedAction?: string
  evidence?: string
}

export interface IrrigationNeedRanking {
  horizon: PredictionHorizon
  totalFieldsCount: number
  urgentCount: number
  rankedFields: RankedFieldNeedItem[]
  generatedAt: string
}

export interface HistoryItem {
  id: string
  fieldId: string
  fieldName: string
  predictionType: PredictionType
  horizon: PredictionHorizon
  generatedAt: string
  targetTime: string
  predictedValue: number | null
  actualOutcomeValue: number | null
  unit?: string
  confidencePercent: number
  accuracyStatus: PredictionAccuracyStatus
  evidence?: string
}

export interface ForecastHistoryResponse {
  totalEvaluated: number
  verifiedAccurateCount: number
  verifiedInaccurateCount: number
  pendingCount: number
  accuracyPercent: number | null
  history: HistoryItem[]
}

// ============================================================================
// Milestone 08.8: Prediction Evaluation & Continuous Intelligence Types
// ============================================================================

export type EvaluationStatus =
  | 'PENDING'
  | 'EVALUATED'
  | 'INSUFFICIENT_DATA'
  | 'OUTCOME_INTERRUPTED'
  | 'EXPIRED'
  | 'INVALID'

export type PredictionClassificationResult =
  | 'TRUE_POSITIVE'
  | 'TRUE_NEGATIVE'
  | 'FALSE_POSITIVE'
  | 'FALSE_NEGATIVE'
  | 'INTERRUPTED'
  | 'NOT_APPLICABLE'

export type EvaluationFailureReason =
  | 'NONE'
  | 'INSUFFICIENT_DATA'
  | 'MISSING_SENSOR'
  | 'STALE_TELEMETRY'
  | 'SAFETY_EVENT'
  | 'OPERATIONAL_OVERRIDE'
  | 'EXTREME_WEATHER'
  | 'EXPIRED_WITHOUT_DATA'
  | 'CALIBRATION_DRIFT'

export type EvaluationDataQuality = 'DATA_COMPLETE' | 'DATA_PARTIAL' | 'DATA_INSUFFICIENT'

export type ModelHealthStatus = 'HEALTHY' | 'WATCH' | 'DEGRADED' | 'INSUFFICIENT_DATA' | 'CANDIDATE_AVAILABLE'

export interface NumericalEvaluationMetrics {
  sampleSize: number
  validComparisonsCount: number
  sufficientSample: boolean
  meanAbsoluteError: number | null
  rootMeanSquaredError: number | null
  meanAbsolutePercentageError: number | null
  mapeReliable: boolean
  meanBiasError: number | null
  withinToleranceCount: number
  withinTolerancePercentage: number | null
}

export interface ClassificationEvaluationMetrics {
  sampleSize: number
  evaluatedCount: number
  sufficientSample: boolean
  truePositives: number
  trueNegatives: number
  falsePositives: number
  falseNegatives: number
  interruptedBySafetyCount: number
  accuracyPercentage: number | null
  precisionPercentage: number | null
  recallPercentage: number | null
  f1Score: number | null
}

export interface EvaluationConfidenceTier {
  tierName: string
  sampleSize: number
  sufficientSample: boolean
  meanAbsoluteError: number | null
  accuracyPercentage: number | null
  failureRatePercentage: number | null
}

export interface EvaluationDataQualityImpact {
  qualityCategory: EvaluationDataQuality
  sampleSize: number
  meanAbsoluteError: number | null
  accuracyPercentage: number | null
  unverifiablePercentage: number | null
}

export interface EvaluationInsight {
  id: string
  category: string
  severity: 'INFO' | 'WARNING' | 'CRITICAL'
  title: string
  description: string
  evidence: string
  recommendation: string
  sourceModule: string
  actionTarget: string
}

export interface HumanDecisionImpact {
  predictionType: PredictionType
  totalPredictionsCount: number
  approvedDirectlyCount: number
  modifiedByHumanCount: number
  rejectedByHumanCount: number
  directApprovalAccuracy: number | null
  humanOverrideImprovement: string | null
}

export interface PredictionTypePerformance {
  predictionType: PredictionType
  displayName: string
  evaluationMethod: string
  modelVersion: string
  sampleSize: number
  isSufficientSample: boolean
  minSampleSizeRequired: number
  modelHealth: ModelHealthStatus
  modelHealthReason: string
  numericalMetrics: NumericalEvaluationMetrics | null
  classificationMetrics: ClassificationEvaluationMetrics | null
  confidenceTiers: EvaluationConfidenceTier[]
  qualityImpacts: EvaluationDataQualityImpact[]
  insights: EvaluationInsight[]
}

export interface PredictionEvaluationOverview {
  totalPredictionsGenerated: number
  totalEvaluated: number
  pendingCount: number
  insufficientDataCount: number
  interruptedBySafetyCount: number
  evaluationCoveragePercentage: number | null
  overallModelHealth: ModelHealthStatus
  overallHealthReason: string
  performanceDriftDetected: boolean
  driftSummary: string | null
  typePerformances: PredictionTypePerformance[]
  systemInsights: EvaluationInsight[]
  humanDecisionImpacts: HumanDecisionImpact[]
  evaluatedAt: string
}

export interface FieldEvaluation {
  fieldId: string
  fieldName: string
  cropType: string
  totalEvaluations: number
  soilMoistureMae: number | null
  waterDemandMaeM3: number | null
  irrigationNeedAccuracyPercent: number | null
  interruptedCount: number
  dataQualityStatus: EvaluationDataQuality
  healthStatus: ModelHealthStatus
  hasSufficientData: boolean
}

export interface FieldEvaluationResponse {
  fields: FieldEvaluation[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface PredictionEvaluationDetail {
  id: string
  predictionId: string
  fieldId: string
  fieldName: string | null
  predictionType: PredictionType
  horizon: PredictionHorizon
  modelVersion: string
  generatedAt: string
  targetTime: string
  evaluatedAt: string
  actualObservedAt: string | null
  predictedValue: number | null
  lowerBound: number | null
  upperBound: number | null
  actualValue: number | null
  absoluteError: number | null
  relativeErrorPercent: number | null
  directionalError: number | null
  withinBounds: boolean | null
  predictedClass: string | null
  actualClass: string | null
  classificationResult: PredictionClassificationResult
  recommendedValue: number | null
  approvedValue: number | null
  operationalEventId: string | null
  operationalNotes: string | null
  safetyInterlockTriggered: boolean
  confidencePercent: number
  evaluationStatus: EvaluationStatus
  failureReason: EvaluationFailureReason
  dataQuality: EvaluationDataQuality
  evaluationMethod: string
  notes: string | null
}

export interface EvaluationHistoryResponse {
  evaluations: PredictionEvaluationDetail[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface EvaluateBatchResponse {
  totalPendingEvaluated: number
  evaluatedCount: number
  insufficientDataCount: number
  interruptedCount: number
  executedAt: string
}

// ============================================================================
// MILESTONE 08.9: ADAPTIVE INTELLIGENCE, MODEL GOVERNANCE & CONTROLLED CALIBRATION
// ============================================================================

export type MethodVersionStatus =
  | 'DRAFT'
  | 'CANDIDATE'
  | 'APPROVED'
  | 'SHADOW'
  | 'CANARY'
  | 'ACTIVE'
  | 'DEPRECATED'
  | 'RETIRED'

export type ProposalStatus =
  | 'DRAFT'
  | 'BACKTESTING'
  | 'REVIEW_REQUIRED'
  | 'APPROVED'
  | 'REJECTED'
  | 'EXPIRED'
  | 'ACTIVE'
  | 'ROLLED_BACK'

export type GovernanceRiskLevel = 'LOW' | 'MEDIUM' | 'HIGH'

export type GovernanceAction =
  | 'PROPOSAL_CREATED'
  | 'BACKTEST_EXECUTED'
  | 'PROPOSAL_APPROVED'
  | 'PROPOSAL_REJECTED'
  | 'VERSION_SHADOW_STARTED'
  | 'VERSION_CANARY_STARTED'
  | 'VERSION_ACTIVATED'
  | 'VERSION_ROLLBACK'

export interface PredictionMethodVersion {
  id: string
  predictionType: PredictionType
  versionTag: string
  status: MethodVersionStatus
  description: string
  configurationJson: string
  isImmutable: boolean
  createdAt: string
  activatedAt: string | null
  retiredAt: string | null
  createdBy: string
  activatedBy: string | null
}

export interface CalibrationProposal {
  id: string
  proposalCode: string
  predictionType: PredictionType
  currentVersionTag: string
  candidateVersionTag: string
  status: ProposalStatus
  reason: string
  evidence: string
  candidateChanges: string
  proposedConfigurationJson: string
  expectedEffect: string
  riskLevel: GovernanceRiskLevel
  affectedScope?: string
  createdAt: string
  updatedAt: string
  createdBy: string
  reviewedBy: string | null
  reviewedAt: string | null
  reviewNotes: string | null
}

export interface CreateCandidateProposalRequest {
  predictionType: PredictionType
  reason: string
  candidateChanges: string
  proposedConfigurationJson: string
  expectedEffect?: string
  riskLevel?: GovernanceRiskLevel
}

export interface BacktestRun {
  id: string
  proposalId: string
  currentVersionTag: string
  candidateVersionTag: string
  dateRangeFrom: string
  dateRangeTo: string
  sampleSize: number
  currentMae: number | null
  candidateMae: number | null
  maeImprovementPercent: number | null
  currentRmse: number | null
  candidateRmse: number | null
  currentMbe: number | null
  candidateMbe: number | null
  currentAccuracyPercent: number | null
  candidateAccuracyPercent: number | null
  currentF1: number | null
  candidateF1: number | null
  regressionDetected: boolean
  regressionDetails: string | null
  dataQuality: EvaluationDataQuality
  isComparable: boolean
  hasSufficientSample: boolean
  executedAt: string
  executedBy: string
}

export interface GovernanceAuditLog {
  id: string
  action: GovernanceAction
  predictionType: PredictionType
  versionTag: string | null
  proposalId: string | null
  actorId: string
  actorRole: string
  timestamp: string
  reason: string
  previousState: string | null
  newState: string | null
  detailsJson: string | null
}

export interface CandidateComparison {
  predictionType: PredictionType
  currentVersionTag: string
  candidateVersionTag: string
  sampleSize: number
  hasSufficientSample: boolean
  currentMae: number | null
  candidateMae: number | null
  maeImprovementPercent: number | null
  currentRmse: number | null
  candidateRmse: number | null
  currentMbe: number | null
  candidateMbe: number | null
  currentAccuracyPercent: number | null
  candidateAccuracyPercent: number | null
  currentF1: number | null
  candidateF1: number | null
  regressionDetected: boolean
  regressionDetails: string | null
}

export interface GovernanceOverview {
  activeVersionsCount: number
  candidateVersionsCount: number
  openProposalsCount: number
  healthyModelsCount: number
  watchModelsCount: number
  degradedModelsCount: number
  performanceDriftDetected: boolean
  driftSummary: string | null
  activeVersions: PredictionMethodVersion[]
  openProposals: CalibrationProposal[]
  recentAuditLogs: GovernanceAuditLog[]
}

export interface CalibrationProposalDetailResponse {
  proposal: CalibrationProposal
  currentVersion: PredictionMethodVersion | null
  candidateVersion: PredictionMethodVersion | null
  latestBacktest: BacktestRun | null
  comparison: CandidateComparison | null
}

export interface ApproveProposalRequest {
  notes?: string
}

export interface RejectProposalRequest {
  reason: string
}

export interface ActivateVersionRequest {
  rolloutMode: 'SHADOW' | 'CANARY' | 'ACTIVE'
  reason?: string
}

export interface RollbackVersionRequest {
  priorVersionTag: string
  reason: string
}

// =========================================================================
// Milestone 08.10: Context-Aware Adaptive Intelligence Engine Types
// =========================================================================

export type AgriculturalSeason = 'SPRING' | 'SUMMER' | 'AUTUMN' | 'WINTER'

export type TelemetryQualityLevel = 'HIGH' | 'MEDIUM' | 'LOW' | 'INSUFFICIENT' | 'UNKNOWN'

export type SensorAvailabilityStatus = 'COMPLETE' | 'PARTIAL' | 'MISSING' | 'UNKNOWN'

export type WaterAvailabilityContext = 'ABUNDANT' | 'NORMAL' | 'CONSTRAINED' | 'CRITICAL' | 'UNKNOWN'

export type AnomalySeverityContext = 'NONE' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | 'UNKNOWN'

export type ContextMatchLevel =
  | 'EXACT_CONTEXT'
  | 'PARTIAL_CONTEXT'
  | 'SIMILAR_CONTEXT'
  | 'BASELINE_FALLBACK'
  | 'INSUFFICIENT_DATA'

export interface PredictionContext {
  fieldId: string
  cropType: string
  soilType: string
  irrigationMethod: string
  fieldAreaHectares: number
  season: AgriculturalSeason
  telemetryQuality: TelemetryQualityLevel
  sensorAvailability: SensorAvailabilityStatus
  waterAvailability: WaterAvailabilityContext
  recentAnomalySeverity: AnomalySeverityContext
  temperatureContext: string
  rainfallContext: string
  capturedAt: string
}

export interface ContextPerformanceProfile {
  predictionType: PredictionType
  versionTag: string
  cropType: string
  soilType: string
  irrigationMethod: string
  telemetryQuality: TelemetryQualityLevel
  season: AgriculturalSeason
  sampleSize: number
  hasSufficientSample: boolean
  meanAbsoluteError: number | null
  rootMeanSquaredError: number | null
  meanBiasError: number | null
  accuracyPercent: number | null
  globalModelHealth: ModelHealthStatus
  contextualModelHealth: ModelHealthStatus
  contextDriftDetected: boolean
  driftDiagnosis: string | null
}

export interface AdaptiveSelectionDecision {
  id: string
  predictionId: string | null
  fieldId: string
  fieldName: string
  predictionType: PredictionType
  selectedVersionTag: string
  selectionMode: ContextMatchLevel
  selectionReason: string
  evidenceSummary: string
  contextSnapshotJson: string
  alternativesJson: string
  confidencePercent: number
  createdAt: string
}

export interface AdaptiveOverviewResponse {
  totalAdaptiveMethodsCount: number
  activeVersionsCount: number
  contextAwareSelectionsCount: number
  baselineFallbackCount: number
  insufficientContextCount: number
  contextualDriftWarningActive: boolean
  recentSelections: AdaptiveSelectionDecision[]
  contextualProfiles: ContextPerformanceProfile[]
}

export interface SelectAdaptiveMethodRequest {
  fieldId: string
  predictionType: string
}



// =========================================================================
// Milestone 08.11: Hybrid ML Prediction Engine & Evidence-Based Learning
// =========================================================================

export type TrainingDatasetQualityStatus = 'HIGH_QUALITY' | 'ACCEPTABLE' | 'DEGRADED' | 'INSUFFICIENT'

export interface TrainingDatasetDto {
  id: string
  predictionType: PredictionType
  datasetHash: string
  featureSchemaVersion: string
  periodStart: string
  periodEnd: string
  fieldScope: string
  recordCount: number
  qualityStatus: TrainingDatasetQualityStatus
  createdAt: string
  createdBy: string
}

export interface TrainingDatasetDetailResponse extends TrainingDatasetDto {
  qualitySummaryJson: string
}

export interface ModelCandidateDto {
  id: string
  predictionType: PredictionType
  modelType: string
  versionTag: string
  datasetSnapshotId: string
  featureSchemaVersion: string
  trainingSampleCount: number
  validationSampleCount: number
  testSampleCount: number
  artifactHash: string
  status: MethodVersionStatus
  isImmutable: boolean
  createdAt: string
  createdBy: string
  testMae: number | null
  baselineMae: number | null
  maeImprovementPercent: number | null
  regressionDetected: boolean
  recommendationStatus: string
}

export interface ModelCandidateDetailResponse {
  id: string
  predictionType: PredictionType
  modelType: string
  versionTag: string
  datasetSnapshotId: string
  featureSchemaVersion: string
  trainingPeriodStart: string
  trainingPeriodEnd: string
  validationPeriodStart: string
  validationPeriodEnd: string
  testPeriodStart: string
  testPeriodEnd: string
  trainingSampleCount: number
  validationSampleCount: number
  testSampleCount: number
  trainingMetricsJson: string
  validationMetricsJson: string
  testMetricsJson: string
  baselineComparisonMetricsJson: string
  configurationSnapshotJson: string
  artifactHash: string
  status: MethodVersionStatus
  isImmutable: boolean
  createdAt: string
  createdBy: string
  featureImportance: Record<string, number>
  limitations: string
}

export interface TrainPredictionModelRequest {
  predictionType: PredictionType
  fieldId?: string | null
  periodStart?: string | null
  periodEnd?: string | null
  l2Penalty?: number | null
}

export interface TrainPredictionModelResponse {
  status: 'SUCCESS' | 'INSUFFICIENT_TRAINING_DATA' | 'FAILED'
  message: string
  predictionType: PredictionType
  datasetId: string
  datasetHash: string
  sampleCount: number
  minRequiredSamples: number
  candidateId?: string | null
  versionTag?: string | null
  baselineComparisonSummary?: string | null
  baselineMae?: number | null
  candidateMae?: number | null
  improvementPercent?: number | null
  regressionDetected: boolean
}

export interface ActiveModelComparisonResponse {
  predictionType: PredictionType
  activeModelVersion: string
  activeModelType: string
  governanceStatus: string
  baselineModelVersion: string
  activeMae: number
  baselineMae: number
  relativeImprovementPercent: number
  sampleSize: number
  contextHealth: string
  dataQualityStatus: string
  isHybridActive: boolean
  summaryRationale: string
}

export interface PromoteCandidateRequest {
  targetStatus: MethodVersionStatus
  reason?: string
}
