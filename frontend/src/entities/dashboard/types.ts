export type PriorityLevel = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'
export type ActionSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'
export type WaterStatus = 'OPTIMAL' | 'ATTENTION_REQUIRED' | 'CRITICAL_OVER_CONSUMED' | 'NORMAL'
export type SensorHealth = 'ONLINE' | 'OFFLINE' | 'DEGRADED' | 'NO_SENSORS'
export type TelemetryFreshness = 'FRESH' | 'STALE' | 'MISSING'
export type MoistureTrend = 'INCREASING' | 'DECREASING' | 'STABLE' | 'UNKNOWN'

export interface FieldMetrics {
  totalFields: number
  activeFields: number
  inactiveFields: number
  fieldsRequiringAttention: number
  totalAreaHectares: number
}

export interface WaterMetrics {
  totalAllocatedM3: number
  totalConsumedM3: number
  totalRemainingM3: number
  totalCommittedM3: number
  totalReservedM3: number
  totalOverConsumedM3: number
  overConsumedFieldCount: number
  overallConsumptionPercentage: number
}

export interface SensorMetrics {
  totalSensors: number
  activeSensors: number
  offlineSensors: number
  maintenanceSensors: number
  staleTelemetrySensors: number
  validTelemetryPercentage: number
}

export interface AnomalyMetrics {
  totalOpen: number
  critical: number
  high: number
  medium: number
  acknowledged: number
}

export interface RecommendationMetrics {
  totalActive: number
  critical: number
  high: number
  accepted: number
}

export interface IrrigationMetrics {
  draftCount: number
  proposedCount: number
  approvedCount: number
  scheduledTodayCount: number
  inProgressCount: number
  completedCount: number
  blockedCount: number
}

export interface WaterBudgetSummary {
  totalAllocatedM3: number
  consumedM3: number
  committedM3: number
  reservedM3: number
  remainingAvailableM3: number
  isExhausted: boolean
  utilizationPercentage: number
}

export interface PriorityField {
  fieldId: string
  fieldName: string
  cropType: string
  soilType: string
  areaHectares: number
  currentMoisturePercent: number | null
  targetMoisturePercent: number | null
  moistureTrend: MoistureTrend
  telemetryFreshness: TelemetryFreshness
  waterStatus: WaterStatus
  allocatedM3: number
  consumedM3: number
  remainingM3: number
  sensorHealth: SensorHealth
  activeAnomalyCount: number
  criticalAnomaly: string | null
  activeRecommendationCount: number
  topRecommendation: string | null
  activePlanStatus: string
  hydraulicSafetyBlocked: boolean
  hydraulicSafetyReason: string | null
  priority: PriorityLevel
  priorityScore: number
  priorityReasons: string[]
  recommendedAction: string
  actionTargetSection: string
}

export interface UrgentAction {
  id: string
  actionType: string
  severity: ActionSeverity
  title: string
  description: string
  fieldId?: string | null
  fieldName?: string | null
  targetSection: string
  actionLabel: string
  actionPayload?: Record<string, string>
  createdAt: string
}

export interface RecentActivity {
  id: string
  timestamp: string
  activityType: string
  title: string
  description: string
  severity: 'CRITICAL' | 'WARNING' | 'INFO' | 'SUCCESS'
  fieldId?: string | null
  fieldName?: string | null
}

export interface DashboardOverviewResponse {
  fieldMetrics: FieldMetrics
  waterMetrics: WaterMetrics
  sensorMetrics: SensorMetrics
  anomalyMetrics: AnomalyMetrics
  recommendationMetrics: RecommendationMetrics
  irrigationMetrics: IrrigationMetrics
  waterBudget: WaterBudgetSummary
  priorityFields: PriorityField[]
  urgentActions: UrgentAction[]
  recentActivity: RecentActivity[]
  generatedAt: string
  tenantScope: string
}

export interface DashboardOverviewParams {
  fieldId?: string
  cropType?: string
  priority?: PriorityLevel
  from?: string
  to?: string
}
