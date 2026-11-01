/**
 * Strongly typed models for AI Agronomic Recommendations & Decision Support.
 * Aligned with backend bounded context com.aquaai.recommendation.
 */

export type RecommendationType =
  | 'IRRIGATE_NOW'
  | 'DELAY_IRRIGATION'
  | 'REDUCE_WATER_VOLUME'
  | 'AVOID_IRRIGATION'
  | 'INSPECT_PIPE_BURST'
  | 'INSPECT_SENSOR'
  | 'MONITOR_SOIL_MOISTURE'
  | 'MANAGE_SALINITY'
  | 'PREPARE_HEAT_STRESS'
  | 'PREPARE_FROST'
  | 'INVESTIGATE_OVERCONSUMPTION'

export type RecommendationPriority =
  | 'CRITICAL'
  | 'HIGH'
  | 'MEDIUM'
  | 'LOW'
  | 'INFO'

export type RecommendationStatus =
  | 'GENERATED'
  | 'VIEWED'
  | 'ACCEPTED'
  | 'DISMISSED'
  | 'COMPLETED'
  | 'EXPIRED'

export type ConfidenceLevel =
  | 'HIGH'
  | 'MODERATE'
  | 'LOW'

export type RecommendationSource =
  | 'DETERMINISTIC_ENGINE'
  | 'HYBRID_AI'
  | 'MANUAL_OVERRIDE'

export interface RecommendationEvidence {
  id: string
  evidenceType: string
  metric: string
  observedValue?: number | null
  baselineValue?: number | null
  unit?: string | null
  summary: string
  sourceReference?: string | null
}

export interface RecommendedAction {
  id: string
  actionOrder: number
  actionText: string
  urgency: string
  isNegativeAction: boolean
}

export interface Recommendation {
  id: string
  fieldId: string
  recommendationType: RecommendationType
  priority: RecommendationPriority
  status: RecommendationStatus
  title: string
  summary: string
  explanation: string
  confidence: number
  confidenceLevel: ConfidenceLevel
  validFrom: string
  validUntil: string
  generatedAt: string
  source: RecommendationSource
  limitations?: string | null
  isSimulated: boolean
  viewedAt?: string | null
  viewedBy?: string | null
  acceptedAt?: string | null
  acceptedBy?: string | null
  dismissedAt?: string | null
  dismissedBy?: string | null
  dismissalReason?: string | null
  completedAt?: string | null
  completedBy?: string | null
  evidence: RecommendationEvidence[]
  actions: RecommendedAction[]
  createdAt: string
  updatedAt: string
  version: number
}

export interface RecommendationSummary {
  totalActive: number
  criticalCount: number
  highCount: number
  mediumCount: number
  lowCount: number
  infoCount: number
  generatedCount: number
  viewedCount: number
  acceptedCount: number
  dismissedCount: number
  completedCount: number
  expiredCount: number
}

export interface RecommendationFilterParams {
  fieldId?: string
  status?: RecommendationStatus
  priority?: RecommendationPriority
  type?: RecommendationType
  from?: string
  to?: string
  page?: number
  size?: number
}

export interface RecommendationPage {
  content: Recommendation[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}
