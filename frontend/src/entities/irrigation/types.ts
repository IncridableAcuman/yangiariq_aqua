/**
 * Strongly typed models for Smart Irrigation Planning & Water Allocation Optimization.
 * Bounded context: com.aquaai.irrigation
 */

export type IrrigationPlanStatus =
  | 'DRAFT'
  | 'PROPOSED'
  | 'APPROVED'
  | 'SCHEDULED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'EXPIRED'

export type IrrigationPriority =
  | 'CRITICAL'
  | 'HIGH'
  | 'MEDIUM'
  | 'LOW'

export type ConstraintStatus =
  | 'SATISFIED'
  | 'WARNING'
  | 'VIOLATED'
  | 'BLOCKED'

export type ConstraintType =
  | 'HYDRAULIC_SAFETY'
  | 'WATER_QUOTA'
  | 'INFRASTRUCTURE_LIMIT'
  | 'AGRONOMIC_DEFICIT'
  | 'WEATHER_PRECIPITATION'
  | 'HEAT_WIND_WINDOW'

export type DataQualityGate =
  | 'SUFFICIENT'
  | 'PARTIAL'
  | 'INSUFFICIENT'
  | 'UNSAFE'

export interface PlanConstraint {
  id: string
  type: ConstraintType
  status: ConstraintStatus
  message: string
  details?: string | null
  createdAt?: string
}

export interface PlanTradeoff {
  id: string
  category: string
  description: string
  createdAt?: string
}

export interface SmartIrrigationPlan {
  id: string
  fieldId: string
  waterAllocationId?: string | null
  status: IrrigationPlanStatus
  priority: IrrigationPriority
  plannedDate: string
  plannedStartAt: string
  plannedEndAt: string
  recommendedVolumeM3: number
  estimatedDurationMinutes: number
  recommendedFlowRateM3H?: number | null
  targetMoisturePercent?: number | null
  currentMoisturePercent?: number | null
  confidenceLevel: string
  confidenceScore?: number | null
  reason: string
  reasoningSummary?: string | null
  cancellationReason?: string | null
  isBlocked: boolean
  blockedReason?: string | null
  approvedBy?: string | null
  approvedAt?: string | null
  source: string
  createdAt: string
  updatedAt: string
  version?: number
  constraints: PlanConstraint[]
  tradeoffs: PlanTradeoff[]
}

export interface WaterBudget {
  fieldId: string
  totalAllocatedM3: number
  consumedM3: number
  committedM3: number
  reservedM3: number
  remainingAvailableM3: number
}

export interface SmartIrrigationPlanFilterParams {
  fieldId?: string
  status?: IrrigationPlanStatus
  priority?: IrrigationPriority
  dateFrom?: string
  dateTo?: string
  page?: number
  size?: number
}

export interface GeneratePlanRequest {
  targetDate?: string
}

export interface ApprovePlanRequest {
  acknowledgeWarnings?: boolean
}

export interface CancelPlanRequest {
  reason: string
}

export interface CompletePlanRequest {
  actualVolumeDeliveredM3?: number
}
