export type WaterSourceType =
  | 'CANAL'
  | 'DITCH'
  | 'RESERVOIR'
  | 'WELL'
  | 'RIVER'
  | 'OTHER'

export type WaterSourceStatus = 'ACTIVE' | 'INACTIVE'

export type WaterAllocationStatus = 'ACTIVE' | 'CLOSED' | 'CANCELLED'

export type IrrigationPlanStatus =
  | 'PLANNED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'

export type IrrigationEventStatus = 'STARTED' | 'COMPLETED' | 'CANCELLED'

export type IrrigationMethod =
  | 'CANAL'
  | 'DITCH'
  | 'FURROW'
  | 'DRIP'
  | 'SPRINKLER'
  | 'OTHER'

export interface WaterSource {
  id: string
  name: string
  type: WaterSourceType
  description?: string | null
  status: WaterSourceStatus
  createdAt: string
  updatedAt: string
}

export interface WaterAllocation {
  id: string
  fieldId: string
  waterSourceId: string
  allocatedVolumeM3: number
  periodStart: string
  periodEnd: string
  status: WaterAllocationStatus
  notes?: string | null
  createdAt: string
  updatedAt: string
}

export interface IrrigationPlan {
  id: string
  fieldId: string
  waterAllocationId?: string | null
  plannedStartAt: string
  plannedEndAt: string
  plannedVolumeM3: number
  method: IrrigationMethod
  status: IrrigationPlanStatus
  notes?: string | null
  createdAt: string
  updatedAt: string
}

export interface IrrigationEvent {
  id: string
  fieldId: string
  irrigationPlanId?: string | null
  startedAt: string
  endedAt?: string | null
  actualVolumeM3?: number | null
  status: IrrigationEventStatus
  notes?: string | null
  createdAt: string
  updatedAt: string
}

export interface WaterAllocationConsumption {
  allocationId: string
  fieldId: string
  waterSourceId: string
  allocatedVolumeM3: number
  consumedVolumeM3: number
  remainingVolumeM3: number
  consumptionPercentage: number
  isOverConsumed: boolean
  overConsumedVolumeM3: number
  completedEventsCount: number
  status: WaterAllocationStatus
  periodStart: string
  periodEnd: string
}

export interface PlannedVsActualMetrics {
  planId: string
  fieldId: string
  waterAllocationId?: string | null
  plannedVolumeM3: number
  actualVolumeM3: number
  varianceVolumeM3: number
  absoluteVarianceVolumeM3: number
  variancePercentage: number
  consumptionRatioPercentage: number
  completedEventsCount: number
  status: IrrigationPlanStatus
}

export interface FieldWaterPeriod {
  from?: string | null
  to?: string | null
}

export interface FieldWaterSummary {
  fieldId: string
  period: FieldWaterPeriod
  totalAllocatedVolumeM3: number
  totalConsumedVolumeM3: number
  totalRemainingVolumeM3: number
  totalOverConsumedVolumeM3: number
  isOverConsumed: boolean
  consumptionPercentage: number
  allocationCount: number
  irrigationEventCount: number
}

// Query Parameters
export interface WaterSourceQueryParams {
  query?: string
  type?: WaterSourceType
  status?: WaterSourceStatus
  page?: number
  size?: number
  sortBy?: 'id' | 'name' | 'type' | 'status' | 'createdAt' | 'updatedAt'
  direction?: 'asc' | 'desc'
}

export interface WaterAllocationQueryParams {
  fieldId?: string
  waterSourceId?: string
  status?: WaterAllocationStatus
  page?: number
  size?: number
  sortBy?: 'id' | 'allocatedVolumeM3' | 'periodStart' | 'periodEnd' | 'status' | 'createdAt' | 'updatedAt'
  direction?: 'asc' | 'desc'
}

export interface IrrigationPlanQueryParams {
  fieldId?: string
  waterAllocationId?: string
  status?: IrrigationPlanStatus
  page?: number
  size?: number
  sortBy?: 'id' | 'plannedStartAt' | 'plannedEndAt' | 'plannedVolumeM3' | 'method' | 'status' | 'createdAt' | 'updatedAt'
  direction?: 'asc' | 'desc'
}

export interface IrrigationEventQueryParams {
  fieldId?: string
  irrigationPlanId?: string
  status?: IrrigationEventStatus
  page?: number
  size?: number
  sortBy?: 'id' | 'startedAt' | 'endedAt' | 'actualVolumeM3' | 'status' | 'createdAt' | 'updatedAt'
  direction?: 'asc' | 'desc'
}

export interface FieldWaterSummaryQueryParams {
  from?: string
  to?: string
}

// Request Payloads
export interface CreateWaterSourcePayload {
  name: string
  type: WaterSourceType
  description?: string | null
}

export interface UpdateWaterSourcePayload {
  name?: string
  type?: WaterSourceType
  description?: string | null
}

export interface CreateWaterAllocationPayload {
  fieldId: string
  waterSourceId: string
  allocatedVolumeM3: number
  periodStart: string
  periodEnd: string
  notes?: string | null
}

export interface UpdateWaterAllocationPayload {
  allocatedVolumeM3?: number
  periodStart?: string
  periodEnd?: string
  notes?: string | null
}

export interface CreateIrrigationPlanPayload {
  fieldId: string
  waterAllocationId?: string | null
  plannedStartAt: string
  plannedEndAt: string
  plannedVolumeM3: number
  method: IrrigationMethod
  notes?: string | null
}

export interface UpdateIrrigationPlanPayload {
  plannedStartAt?: string
  plannedEndAt?: string
  plannedVolumeM3?: number
  method?: IrrigationMethod
  notes?: string | null
}

export interface CreateIrrigationEventPayload {
  fieldId: string
  irrigationPlanId?: string | null
  startAt: string
  notes?: string | null
}

export interface RecordCompletedIrrigationEventPayload {
  fieldId: string
  irrigationPlanId?: string | null
  startAt: string
  endAt: string
  actualVolumeM3: number
  notes?: string | null
}

export interface CompleteIrrigationEventPayload {
  endAt: string
  actualVolumeM3: number
  notes?: string | null
}

export interface CancelOperationPayload {
  reason?: string | null
}
