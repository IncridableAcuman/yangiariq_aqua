import { SmartIrrigationPlan } from '@/entities/irrigation/types'
export type { SmartIrrigationPlan }

export type OperationalWorkflowState =
  | 'DETECTED'
  | 'REVIEW_REQUIRED'
  | 'RECOMMENDATION_READY'
  | 'PLAN_REQUIRED'
  | 'PLAN_PROPOSED'
  | 'APPROVAL_REQUIRED'
  | 'APPROVED'
  | 'SCHEDULED'
  | 'READY'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'VERIFIED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'EXPIRED'
  | 'BLOCKED'

export type WorkflowStepStatus =
  | 'COMPLETED'
  | 'CURRENT'
  | 'UPCOMING'
  | 'BLOCKED'
  | 'SKIPPED'
  | 'REJECTED'

export interface WorkflowStep {
  stepKey: string
  title: string
  status: WorkflowStepStatus
  timestamp: string | null
  actor: string | null
  reason: string | null
  availableAction: string | null
}

export interface ScheduleConflict {
  hasConflict: boolean
  conflictType: string | null
  conflictMessage: string | null
  conflictingPlanId: string | null
  conflictingStartTime: string | null
  conflictingEndTime: string | null
  suggestedAdjustment: string | null
}

export interface FieldSummary {
  id: string
  name: string
  cropType: string | null
  areaHectares: number
  latitude: number | null
  longitude: number | null
  status: string
}

export interface SituationSummary {
  latestSoilMoisture: number | null
  soilMoistureTrend12h: number | null
  temperatureCelsius: number | null
  telemetryFreshness: 'FRESH' | 'STALE' | 'MISSING' | 'EXPIRED'
  lastTelemetryAt: string | null
  waterRemainingM3: number
  waterAllocatedM3: number
  waterConsumedM3: number
  hydraulicSafetyStatus: 'SAFE' | 'WARNING' | 'BLOCKED'
  hydraulicBlockedReason: string | null
}

export interface AnomalySummary {
  id: string
  type: string
  severity: string
  description: string
  detectedAt: string
  isBlocking: boolean
}

export interface RecommendationSummary {
  id: string
  type: string
  priority: string
  reason: string
  confidence: string
  suggestedVolumeM3: number | null
  suggestedDurationMinutes: number | null
  aiExplanation: string | null
  evidence: string[]
}

export interface IrrigationEventSummary {
  id: string
  status: string
  startedAt: string
  endedAt: string | null
  actualVolumeM3: number | null
  notes: string | null
}

export interface WorkflowSummary {
  currentState: OperationalWorkflowState
  isBlocked: boolean
  blockedReason: string | null
  timeline: WorkflowStep[]
  availableActions: string[]
  scheduleConflict: ScheduleConflict | null
}

export interface OperationalContextResponse {
  field: FieldSummary
  situation: SituationSummary
  activeAnomaly: AnomalySummary | null
  activeRecommendation: RecommendationSummary | null
  activePlan: SmartIrrigationPlan | null
  activeIrrigationEvent: IrrigationEventSummary | null
  workflow: WorkflowSummary
}

export interface PreparePlanPayload {
  targetDate?: string
  customVolumeM3?: number
  customNotes?: string
}

export interface ApprovePlanPayload {
  acknowledgeConstraints: boolean
  approvalNotes?: string
}

export interface RejectPlanPayload {
  reason: string
}

export interface SchedulePlanPayload {
  scheduledStartAt?: string
  scheduledEndAt?: string
  dispatchNotes?: string
}

export interface StartOperationPayload {
  startedAt?: string
  operatorNotes?: string
}

export interface CompleteOperationPayload {
  actualVolumeDeliveredM3?: number
  endedAt?: string
  operatorNotes?: string
}

export interface OperationalVerificationResponse {
  planId: string
  fieldId: string
  fieldName: string
  cropType: string | null
  executionDate: string
  plannedVolumeM3: number
  actualVolumeM3: number
  volumeVarianceM3: number
  volumeVariancePercent: number
  plannedDurationMinutes: number
  actualDurationMinutes: number
  durationVarianceMinutes: number
  soilMoistureBeforePercent: number
  soilMoistureAfterPercent: number
  soilMoistureChangePercent: number
  waterBudgetRemainingBeforeM3: number
  waterBudgetRemainingAfterM3: number
  isRechargeSuccessful: boolean
  verificationSummary: string
  learningEvidence: string
}
