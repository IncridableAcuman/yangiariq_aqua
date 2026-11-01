import { FieldBoundary } from '@/entities/field'
import { WaterSourceType, WaterSourceStatus, WaterAllocationStatus } from '@/entities/water'

export type WaterStatusCategory =
  | 'NORMAL'
  | 'WARNING'
  | 'OVER_CONSUMED'
  | 'NO_ALLOCATION'

export interface GisField {
  id: string
  ownerId: string
  name: string
  description?: string | null
  areaHectares: number
  cropType: string
  soilType: string
  irrigationMethod: string
  latitude?: number | null
  longitude?: number | null
  boundary?: FieldBoundary | null
  status: 'ACTIVE' | 'INACTIVE'
}

export interface GisWaterSource {
  id: string
  name: string
  type: WaterSourceType
  description?: string | null
  status: WaterSourceStatus
  latitude?: number | null
  longitude?: number | null
}

export interface GisFieldWaterStatus {
  fieldId: string
  totalAllocatedVolumeM3: number
  totalConsumedVolumeM3: number
  totalRemainingVolumeM3: number
  totalOverConsumedVolumeM3: number
  isOverConsumed: boolean
  consumptionPercentage: number
  category: WaterStatusCategory
  allocationCount: number
  irrigationEventCount: number
}

export interface GisAllocationLink {
  id: string
  fieldId: string
  waterSourceId: string
  allocatedVolumeM3: number
  status: WaterAllocationStatus
}

export interface GisOverviewData {
  fields: GisField[]
  waterSources: GisWaterSource[]
  waterStatuses: GisFieldWaterStatus[]
  allocations: GisAllocationLink[]
  period?: {
    from?: string | null
    to?: string | null
  } | null
}

export interface GisOverviewParams {
  from?: string
  to?: string
}
