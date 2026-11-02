export type PerformancePeriodType =
  | 'TODAY'
  | 'LAST_7_DAYS'
  | 'LAST_30_DAYS'
  | 'LAST_90_DAYS'
  | 'SEASON'
  | 'CUSTOM'

export type DataConfidenceLevel = 'COMPLETE' | 'PARTIAL' | 'INSUFFICIENT'

export type FieldPerformanceStatus = 'NORMAL' | 'WARNING' | 'ATTENTION'

export type InsightCategory =
  | 'WATER'
  | 'IRRIGATION'
  | 'TELEMETRY'
  | 'SAFETY'
  | 'VERIFICATION'

export type InsightSeverity = 'CRITICAL' | 'HIGH' | 'WARNING' | 'INFO'

export type TrendGranularity = 'DAILY' | 'WEEKLY' | 'MONTHLY'

export interface PerformancePeriodDto {
  type: PerformancePeriodType
  from: string
  to: string
  label: string
  durationDays: number
}

export interface FarmOverviewMetricsDto {
  totalFields: number
  activeFields: number
  totalAreaHectares: number
  totalAllocatedM3?: number
  totalConsumedM3?: number
  totalRemainingM3?: number
  totalCompletedOperations?: number
  totalCancelledOperations?: number
  totalBlockedOperations?: number
  totalSensors?: number
  healthySensors?: number
  staleSensors?: number
  offlineSensors?: number
  criticalSafetyEvents?: number
  warningSafetyEvents?: number
  aggregatePerformanceScore?: number
  overallStatus?: FieldPerformanceStatus | string
  totalSafetyEvents?: number
  criticalAnomaliesCount?: number
}

export interface WaterPerformanceDto {
  totalAllocatedM3: number
  totalConsumedM3: number
  totalRemainingM3: number
  utilizationPercent: number | null
  variancePercent: number | null
  overConsumedFieldsCount?: number
  overConsumedTotalM3?: number
  overConsumedVolumeM3?: number
  overConsumedFieldCount?: number
  waterStatus?: 'NORMAL' | 'WARNING' | 'OVER_CONSUMED' | string
  utilizationStatus?: 'NORMAL' | 'WARNING' | 'OVER_CONSUMED' | string
  statusExplanation?: string
  contextualInterpretation?: string
  plannedVolumeM3?: number
  actualVolumeM3?: number
  varianceM3?: number
  averageIrrigationVolumeM3?: number
  completedIrrigationsCount?: number
}

export interface IrrigationPerformanceDto {
  totalPlans?: number
  totalPlansCreated?: number
  totalPlansApproved?: number
  proposedCount?: number
  approvedCount?: number
  scheduledCount?: number
  inProgressCount?: number
  completedCount?: number
  cancelledCount?: number
  rejectedCount?: number
  blockedCount?: number
  totalOperationsScheduled?: number
  totalOperationsCompleted?: number
  operationCompletionRate?: number | null
  executionRatePercent?: number | null
  planAdherencePercent: number | null
  averagePlannedDurationMinutes?: number
  averageDelayMinutes?: number
  averageActualDurationMinutes?: number
  adherenceExplanation?: string
}

export interface TelemetryQualityDto {
  totalSensors: number
  onlineSensors?: number
  activeSensors?: number
  offlineSensors: number
  degradedSensors?: number
  staleSensors?: number
  totalReadings?: number
  validReadings?: number
  suspectReadings?: number
  invalidReadings?: number
  totalReadingsCount?: number
  validReadingsCount?: number
  telemetryCoveragePercent?: number | null
  validReadingPercent?: number | null
  sensorAvailabilityPercent?: number | null
  dataConfidence: DataConfidenceLevel
  coverageDefinition?: string
  qualityNotice?: string
}

export interface VerificationPerformanceDto {
  totalOperationsToVerify?: number
  completedOperationsCount?: number
  verifiedOperationsCount: number
  unverifiedOperationsCount?: number
  unverifiedCompletedOperationsCount?: number
  verificationCoveragePercent: number | null
  automatedVerificationPercent?: number | null
  successfulRechargeCount?: number
  deficitRechargeCount?: number
  dataConfidence?: DataConfidenceLevel
  coverageNotice?: string
}

export interface SafetyEventMetricsDto {
  totalEvents?: number
  totalAnomaliesDetected?: number
  criticalCount?: number
  criticalAnomaliesCount?: number
  highCount?: number
  warningAnomaliesCount?: number
  mediumCount?: number
  lowCount?: number
  repeatedSafetyFieldCount?: number
  activeBlockedParcelsCount?: number
  activeHydraulicBlocksCount?: number
  safetyIncidentRatePer100Ha?: number
}

export interface PeriodComparisonDto {
  hasComparisonData: boolean
  previousPeriodLabel: string
  waterConsumedDeltaPercent: number
  waterConsumedDeltaM3: number
  operationsCompletedDelta: number
  planAdherenceDeltaPercent: number
  telemetryCoverageDeltaPercent: number
  safetyEventsDelta: number
  comparisonSummary: string
}

export interface DataConfidenceDto {
  overall: DataConfidenceLevel
  waterConfidence: DataConfidenceLevel
  telemetryConfidence: DataConfidenceLevel
  verificationConfidence: DataConfidenceLevel
  hasIncompleteData: boolean
  warningMessage?: string
}

export interface FarmInsightDto {
  id: string
  category: InsightCategory
  severity: InsightSeverity
  fieldId?: string
  fieldName?: string
  title: string
  explanation: string
  evidence: string[]
  recommendedAction: string
  targetSection: string
  targetPayload?: Record<string, string>
  createdAt: string
  what?: string
  why?: string
  nextStep?: string
}

export interface FieldPerformanceDto {
  fieldId: string
  fieldName: string
  cropType: string
  soilType: string
  areaHectares: number
  allocatedM3: number
  consumedM3: number
  remainingM3: number
  utilizationPercent: number | null
  waterStatus: string
  plansCount: number
  completedOperationsCount: number
  planAdherencePercent: number | null
  telemetryCoveragePercent: number | null
  sensorHealth: string
  safetyEventCount: number
  criticalAnomalyCount: number
  verificationCoveragePercent: number | null
  performanceScore: number
  performanceStatus: FieldPerformanceStatus
  scoreBreakdown: Record<string, number>
}

export interface FieldPerformanceRankingResponse {
  fields: FieldPerformanceDto[]
  totalElements: number
  page: number
  size: number
  totalPages: number
}

export interface PerformanceTrendPointDto {
  timestamp: string
  dateLabel: string
  waterConsumedM3: number
  waterPlannedM3: number
  utilizationPercent: number | null
  completedOperationsCount: number
  telemetryCoveragePercent: number | null
  safetyEventsCount: number
}

export interface PerformanceTrendsResponse {
  granularity: TrendGranularity
  trendPoints: PerformanceTrendPointDto[]
}

export interface AnalyticsOverviewResponse {
  period: PerformancePeriodDto
  farmOverview: FarmOverviewMetricsDto
  waterPerformance: WaterPerformanceDto
  irrigationPerformance: IrrigationPerformanceDto
  telemetryQuality: TelemetryQualityDto
  verificationPerformance: VerificationPerformanceDto
  safetyMetrics: SafetyEventMetricsDto
  periodComparison: PeriodComparisonDto
  dataConfidence: DataConfidenceDto
  topInsights: FarmInsightDto[]
  generatedAt: string
  tenantScope: string
}
