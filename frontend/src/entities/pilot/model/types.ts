export type PilotStatus =
  | 'DRAFT'
  | 'PLANNED'
  | 'APPROVED'
  | 'READY'
  | 'ACTIVE'
  | 'PAUSED'
  | 'COMPLETED'
  | 'CANCELLED'

export type ReadinessCategory = 'INFRASTRUCTURE' | 'DATA' | 'OPERATIONAL' | 'GOVERNANCE'
export type ReadinessResult = 'PASS' | 'WARNING' | 'BLOCKED' | 'NOT_ASSESSED'

export interface PilotReadinessCheck {
  category: ReadinessCategory
  name: String
  result: ReadinessResult
  reason: String
  supportingEvidence?: String
  isMandatory: boolean
}

export interface PilotProgram {
  id: string
  code: string
  name: string
  description?: string
  geographicScope: string
  startDate: string
  endDate: string
  organization: string
  ownerId: string
  operationalObjectives: string
  status: PilotStatus
  approvedBy?: string
  approvedAt?: string
  approvalNotes?: string
  createdAt: string
  updatedAt: string
}

export interface PilotParticipant {
  id: string
  pilotId: string
  userId: string
  roleInPilot: string
  assignedFieldIds?: string
  joinedAt: string
}

export interface PilotField {
  id: string
  pilotId: string
  fieldId: string
  monitoringTarget: string
  measurementPlan: string
  validationCriteria: string
  addedAt: string
}

export interface PilotReadinessAssessment {
  id: string
  pilotId: string
  overallStatus: ReadinessResult
  infrastructureStatus: ReadinessResult
  dataStatus: ReadinessResult
  operationalStatus: ReadinessResult
  governanceStatus: ReadinessResult
  checks: PilotReadinessCheck[]
  assessedBy: string
  assessedAt: string
}

export type GroundTruthStatus =
  | 'SUBMITTED'
  | 'PENDING_REVIEW'
  | 'VERIFIED'
  | 'REJECTED'
  | 'NEEDS_REMEASUREMENT'

export interface GroundTruthMeasurement {
  id: string
  pilotId: string
  fieldId: string
  measurementType: string
  measuredValue: number
  canonicalUnit: string
  measuredAt: string
  submittedAt: string
  collectionMethod: string
  measurementSource: string
  submittedBy: string
  verificationStatus: GroundTruthStatus
  verifierId?: string
  evidenceReference?: string
  notes?: string
  verifiedAt?: string
  rejectionReason?: string
}

export type PredictionEvaluationStatus =
  | 'VALIDATION_PENDING'
  | 'VALIDATED'
  | 'INSUFFICIENT_DATA'
  | 'INVALID_COMPARISON'

export interface PredictionValidationRecord {
  id: string
  pilotId: string
  fieldId: string
  predictionId?: string
  modelName: string
  modelVersion: string
  predictedAt: string
  predictionHorizonHours: number
  predictedValue: number
  actualValue: number
  groundTruthId?: string
  absoluteError: number
  relativeErrorPercentage?: number
  evaluationStatus: PredictionEvaluationStatus
  evaluatedAt: string
}

export type OperatorFeedbackCategory =
  | 'DATA_QUALITY'
  | 'SENSOR_ISSUE'
  | 'WEATHER_DISCREPANCY'
  | 'IRRIGATION_OPERATION'
  | 'WATER_DELIVERY'
  | 'FIELD_CONDITION'
  | 'SYSTEM_USABILITY'
  | 'OTHER'

export type OperatorFeedbackStatus =
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'ACTION_REQUIRED'
  | 'RESOLVED'
  | 'REJECTED'

export interface OperatorFeedback {
  id: string
  pilotId: string
  farmId?: string
  fieldId?: string
  sensorId?: string
  dataSourceId?: string
  incidentId?: string
  category: OperatorFeedbackCategory
  title: string
  description: string
  status: OperatorFeedbackStatus
  submittedBy: string
  assignedTo?: string
  resolutionNotes?: string
  createdAt: string
  updatedAt: string
}

export interface PilotEvaluationReport {
  id: string
  pilotId: string
  periodStart: string
  periodEnd: string
  dataPerformanceJson: string
  predictionPerformanceJson: string
  operationalPerformanceJson: string
  waterOutcomesJson: string
  limitationsText: string
  recommendationsText: string
  createdBy: string
  createdAt: string
}

export interface PilotOperationalMonitoring {
  pilotId: string
  pilotName: string
  pilotStatus: string
  totalParticipants: number
  totalFields: number
  totalGroundTruthMeasurements: number
  pendingVerifications: number
  verifiedGroundTruths: number
  totalPredictionValidations: number
  averageAbsoluteError?: number
  openFeedbackCount: number
  latestReadinessStatus: ReadinessResult
}
