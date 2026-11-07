export type RiskCategory =
  | 'WATER_AVAILABILITY'
  | 'SOIL_MOISTURE'
  | 'HYDRAULIC'
  | 'IRRIGATION_OPERATIONAL'
  | 'SENSOR_DATA_RELIABILITY'
  | 'PREDICTIVE_MODEL_RELIABILITY'

export type RiskSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'UNKNOWN'

export type RiskConfidence = 'HIGH' | 'MEDIUM' | 'LOW' | 'INSUFFICIENT_DATA'

export type RiskTrend = 'IMPROVING' | 'STABLE' | 'DEGRADING' | 'UNKNOWN'

export type RiskEvidenceType = 'OBSERVED' | 'PREDICTED' | 'MISSING' | 'UNRELIABLE'

export type RiskEvidenceSource =
  | 'TELEMETRY'
  | 'WATER_BUDGET'
  | 'HYDRAULIC_SAFETY'
  | 'IRRIGATION_OPERATION'
  | 'PREDICTION_ENGINE'
  | 'MODEL_GOVERNANCE'

export type RiskAssessmentLifecycleStatus =
  | 'OPEN'
  | 'ACKNOWLEDGED'
  | 'ACTION_REQUIRED'
  | 'MITIGATED'
  | 'RESOLVED'
  | 'EXPIRED'

export interface RiskEvidence {
  id: string
  category: RiskCategory
  metricName: string
  observedValue: number | null
  thresholdValue: number | null
  unit: string | null
  evidenceType: RiskEvidenceType
  source: RiskEvidenceSource
  message: string
  recordedAt: string
  isAuthoritative: boolean
}

export interface RiskFactor {
  id: string
  category: RiskCategory
  severity: RiskSeverity
  confidence: RiskConfidence
  title: string
  description: string
  evidenceList: RiskEvidence[]
  contributionScore: number
  potentialImpact: string
  suggestedAction: string
  missingInputs: string[]
}

export interface RiskRecommendation {
  id: string
  assessmentId: string
  fieldId: string
  fieldName: string
  title: string
  description: string
  category: RiskCategory
  severity: RiskSeverity
  confidence: RiskConfidence
  supportingEvidence: RiskEvidence[]
  reason: string
  suggestedNextAction: string
  priorityRank: number
  relatedOperationalEntity?: string | null
  destinationRoute: string
  createdAt: string
}

export interface FieldRiskProfile {
  assessmentId: string
  fieldId: string
  fieldName: string
  areaHectares: number | null
  cropType: string | null
  soilType: string | null
  irrigationMethod: string | null
  overallSeverity: RiskSeverity
  overallConfidence: RiskConfidence
  riskTrend: RiskTrend
  riskScore: number
  isHydraulicBlocked: boolean
  hydraulicBlockReason: string | null
  primaryRiskCategory: RiskCategory
  lifecycleStatus: RiskAssessmentLifecycleStatus
  priorityRank: number
  priorityReason: string
  activeFactorCount: number
  recommendationCount: number
  riskFactors?: RiskFactor[]
  recommendations?: RiskRecommendation[]
  missingDataWarnings?: string[]
  evaluatedAt: string
  acknowledgedAt?: string | null
  acknowledgedBy?: string | null
  resolvedAt?: string | null
  resolvedBy?: string | null
  resolutionNotes?: string | null
}

export interface FarmRiskOverview {
  totalAssessedFields: number
  criticalRiskCount: number
  highRiskCount: number
  mediumRiskCount: number
  lowRiskCount: number
  insufficientDataCount: number
  hydraulicEmergencyCount: number
  waterShortageCount: number
  severeMoistureRiskCount: number
  telemetryIssuesCount: number
  modelReliabilityWarningsCount: number
  activeRecommendationsCount: number
  categoryDistribution: Record<RiskCategory, number>
  criticalEmergencyFields: FieldRiskProfile[]
  topPrioritizedRecommendations: RiskRecommendation[]
  evaluatedAt: string
}

export interface FieldRiskListParams {
  severity?: string
  category?: string
  confidence?: string
  status?: string
  search?: string
  sortBy?: string
  sortDirection?: 'asc' | 'desc'
  page?: number
  size?: number
}

export interface RiskRecommendationListParams {
  severity?: string
  category?: string
  fieldId?: string
  page?: number
  size?: number
}
