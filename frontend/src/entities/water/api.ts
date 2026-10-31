import { apiClient, PageResponse } from '@/shared/api/client'
import {
  WaterSource,
  WaterAllocation,
  IrrigationPlan,
  IrrigationEvent,
  WaterAllocationConsumption,
  PlannedVsActualMetrics,
  FieldWaterSummary,
  WaterSourceQueryParams,
  WaterAllocationQueryParams,
  IrrigationPlanQueryParams,
  IrrigationEventQueryParams,
  FieldWaterSummaryQueryParams,
  CreateWaterSourcePayload,
  UpdateWaterSourcePayload,
  CreateWaterAllocationPayload,
  UpdateWaterAllocationPayload,
  CreateIrrigationPlanPayload,
  UpdateIrrigationPlanPayload,
  CreateIrrigationEventPayload,
  RecordCompletedIrrigationEventPayload,
  CompleteIrrigationEventPayload,
  CancelOperationPayload,
} from './types'

// -------------------------------------------------------------
// Water Sources API
// -------------------------------------------------------------

export async function getWaterSources(
  params: WaterSourceQueryParams = {}
): Promise<PageResponse<WaterSource>> {
  const searchParams = new URLSearchParams()
  if (params.query) searchParams.set('query', params.query)
  if (params.type) searchParams.set('type', params.type)
  if (params.status) searchParams.set('status', params.status)
  if (typeof params.page === 'number') searchParams.set('page', params.page.toString())
  if (typeof params.size === 'number') searchParams.set('size', params.size.toString())
  if (params.sortBy) {
    const direction = params.direction || 'desc'
    searchParams.set('sort', `${params.sortBy},${direction}`)
  }

  const queryString = searchParams.toString()
  const endpoint = queryString ? `/water-sources?${queryString}` : '/water-sources'

  const response = await apiClient<PageResponse<WaterSource>>(endpoint, { method: 'GET' })
  if (!response.data) throw new Error('Water sources data not returned by server')
  return response.data
}

export async function getWaterSourceById(id: string): Promise<WaterSource> {
  const response = await apiClient<WaterSource>(`/water-sources/${id}`, { method: 'GET' })
  if (!response.data) throw new Error(`Water source ${id} not returned by server`)
  return response.data
}

export async function createWaterSource(payload: CreateWaterSourcePayload): Promise<WaterSource> {
  const response = await apiClient<WaterSource>('/water-sources', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error('Created water source data not returned by server')
  return response.data
}

export async function updateWaterSource(
  id: string,
  payload: UpdateWaterSourcePayload
): Promise<WaterSource> {
  const response = await apiClient<WaterSource>(`/water-sources/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error(`Updated water source ${id} not returned by server`)
  return response.data
}

export async function activateWaterSource(id: string): Promise<WaterSource> {
  const response = await apiClient<WaterSource>(`/water-sources/${id}/activate`, {
    method: 'PATCH',
  })
  if (!response.data) throw new Error(`Activated water source ${id} not returned by server`)
  return response.data
}

export async function deactivateWaterSource(id: string): Promise<WaterSource> {
  const response = await apiClient<WaterSource>(`/water-sources/${id}/deactivate`, {
    method: 'PATCH',
  })
  if (!response.data) throw new Error(`Deactivated water source ${id} not returned by server`)
  return response.data
}

// -------------------------------------------------------------
// Water Allocations API
// -------------------------------------------------------------

export async function getWaterAllocations(
  params: WaterAllocationQueryParams = {}
): Promise<PageResponse<WaterAllocation>> {
  const searchParams = new URLSearchParams()
  if (params.fieldId) searchParams.set('fieldId', params.fieldId)
  if (params.waterSourceId) searchParams.set('waterSourceId', params.waterSourceId)
  if (params.status) searchParams.set('status', params.status)
  if (typeof params.page === 'number') searchParams.set('page', params.page.toString())
  if (typeof params.size === 'number') searchParams.set('size', params.size.toString())
  if (params.sortBy) {
    const direction = params.direction || 'desc'
    searchParams.set('sort', `${params.sortBy},${direction}`)
  }

  const queryString = searchParams.toString()
  const endpoint = queryString ? `/water-allocations?${queryString}` : '/water-allocations'

  const response = await apiClient<PageResponse<WaterAllocation>>(endpoint, { method: 'GET' })
  if (!response.data) throw new Error('Water allocations data not returned by server')
  return response.data
}

export async function getWaterAllocationById(id: string): Promise<WaterAllocation> {
  const response = await apiClient<WaterAllocation>(`/water-allocations/${id}`, { method: 'GET' })
  if (!response.data) throw new Error(`Water allocation ${id} not returned by server`)
  return response.data
}

export async function createWaterAllocation(
  payload: CreateWaterAllocationPayload
): Promise<WaterAllocation> {
  const response = await apiClient<WaterAllocation>('/water-allocations', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error('Created water allocation data not returned by server')
  return response.data
}

export async function updateWaterAllocation(
  id: string,
  payload: UpdateWaterAllocationPayload
): Promise<WaterAllocation> {
  const response = await apiClient<WaterAllocation>(`/water-allocations/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error(`Updated water allocation ${id} not returned by server`)
  return response.data
}

export async function closeWaterAllocation(
  id: string,
  payload?: CancelOperationPayload
): Promise<WaterAllocation> {
  const response = await apiClient<WaterAllocation>(`/water-allocations/${id}/close`, {
    method: 'PATCH',
    body: payload ? JSON.stringify(payload) : undefined,
  })
  if (!response.data) throw new Error(`Closed water allocation ${id} not returned by server`)
  return response.data
}

export async function cancelWaterAllocation(
  id: string,
  payload?: CancelOperationPayload
): Promise<WaterAllocation> {
  const response = await apiClient<WaterAllocation>(`/water-allocations/${id}/cancel`, {
    method: 'PATCH',
    body: payload ? JSON.stringify(payload) : undefined,
  })
  if (!response.data) throw new Error(`Cancelled water allocation ${id} not returned by server`)
  return response.data
}

// -------------------------------------------------------------
// Irrigation Plans API
// -------------------------------------------------------------

export async function getIrrigationPlans(
  params: IrrigationPlanQueryParams = {}
): Promise<PageResponse<IrrigationPlan>> {
  const searchParams = new URLSearchParams()
  if (params.fieldId) searchParams.set('fieldId', params.fieldId)
  if (params.waterAllocationId) searchParams.set('waterAllocationId', params.waterAllocationId)
  if (params.status) searchParams.set('status', params.status)
  if (typeof params.page === 'number') searchParams.set('page', params.page.toString())
  if (typeof params.size === 'number') searchParams.set('size', params.size.toString())
  if (params.sortBy) {
    const direction = params.direction || 'desc'
    searchParams.set('sort', `${params.sortBy},${direction}`)
  }

  const queryString = searchParams.toString()
  const endpoint = queryString ? `/irrigation-plans?${queryString}` : '/irrigation-plans'

  const response = await apiClient<PageResponse<IrrigationPlan>>(endpoint, { method: 'GET' })
  if (!response.data) throw new Error('Irrigation plans data not returned by server')
  return response.data
}

export async function getIrrigationPlanById(id: string): Promise<IrrigationPlan> {
  const response = await apiClient<IrrigationPlan>(`/irrigation-plans/${id}`, { method: 'GET' })
  if (!response.data) throw new Error(`Irrigation plan ${id} not returned by server`)
  return response.data
}

export async function createIrrigationPlan(
  payload: CreateIrrigationPlanPayload
): Promise<IrrigationPlan> {
  const response = await apiClient<IrrigationPlan>('/irrigation-plans', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error('Created irrigation plan data not returned by server')
  return response.data
}

export async function updateIrrigationPlan(
  id: string,
  payload: UpdateIrrigationPlanPayload
): Promise<IrrigationPlan> {
  const response = await apiClient<IrrigationPlan>(`/irrigation-plans/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error(`Updated irrigation plan ${id} not returned by server`)
  return response.data
}

export async function startIrrigationPlan(id: string): Promise<IrrigationPlan> {
  const response = await apiClient<IrrigationPlan>(`/irrigation-plans/${id}/start`, {
    method: 'PATCH',
  })
  if (!response.data) throw new Error(`Started irrigation plan ${id} not returned by server`)
  return response.data
}

export async function completeIrrigationPlan(id: string): Promise<IrrigationPlan> {
  const response = await apiClient<IrrigationPlan>(`/irrigation-plans/${id}/complete`, {
    method: 'PATCH',
  })
  if (!response.data) throw new Error(`Completed irrigation plan ${id} not returned by server`)
  return response.data
}

export async function cancelIrrigationPlan(
  id: string,
  payload?: CancelOperationPayload
): Promise<IrrigationPlan> {
  const response = await apiClient<IrrigationPlan>(`/irrigation-plans/${id}/cancel`, {
    method: 'PATCH',
    body: payload ? JSON.stringify(payload) : undefined,
  })
  if (!response.data) throw new Error(`Cancelled irrigation plan ${id} not returned by server`)
  return response.data
}

// -------------------------------------------------------------
// Irrigation Events API
// -------------------------------------------------------------

export async function getIrrigationEvents(
  params: IrrigationEventQueryParams = {}
): Promise<PageResponse<IrrigationEvent>> {
  const searchParams = new URLSearchParams()
  if (params.fieldId) searchParams.set('fieldId', params.fieldId)
  if (params.irrigationPlanId) searchParams.set('irrigationPlanId', params.irrigationPlanId)
  if (params.status) searchParams.set('status', params.status)
  if (typeof params.page === 'number') searchParams.set('page', params.page.toString())
  if (typeof params.size === 'number') searchParams.set('size', params.size.toString())
  if (params.sortBy) {
    const direction = params.direction || 'desc'
    searchParams.set('sort', `${params.sortBy},${direction}`)
  }

  const queryString = searchParams.toString()
  const endpoint = queryString ? `/irrigation-events?${queryString}` : '/irrigation-events'

  const response = await apiClient<PageResponse<IrrigationEvent>>(endpoint, { method: 'GET' })
  if (!response.data) throw new Error('Irrigation events data not returned by server')
  return response.data
}

export async function getIrrigationEventById(id: string): Promise<IrrigationEvent> {
  const response = await apiClient<IrrigationEvent>(`/irrigation-events/${id}`, { method: 'GET' })
  if (!response.data) throw new Error(`Irrigation event ${id} not returned by server`)
  return response.data
}

export async function createIrrigationEvent(
  payload: CreateIrrigationEventPayload
): Promise<IrrigationEvent> {
  const response = await apiClient<IrrigationEvent>('/irrigation-events', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error('Created irrigation event data not returned by server')
  return response.data
}

export async function recordCompletedIrrigationEvent(
  payload: RecordCompletedIrrigationEventPayload
): Promise<IrrigationEvent> {
  const response = await apiClient<IrrigationEvent>('/irrigation-events/completed', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error('Recorded completed irrigation event data not returned by server')
  return response.data
}

export async function completeIrrigationEvent(
  id: string,
  payload: CompleteIrrigationEventPayload
): Promise<IrrigationEvent> {
  const response = await apiClient<IrrigationEvent>(`/irrigation-events/${id}/complete`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error(`Completed irrigation event ${id} not returned by server`)
  return response.data
}

export async function cancelIrrigationEvent(
  id: string,
  payload?: CancelOperationPayload
): Promise<IrrigationEvent> {
  const response = await apiClient<IrrigationEvent>(`/irrigation-events/${id}/cancel`, {
    method: 'PATCH',
    body: payload ? JSON.stringify(payload) : undefined,
  })
  if (!response.data) throw new Error(`Cancelled irrigation event ${id} not returned by server`)
  return response.data
}

// -------------------------------------------------------------
// Water Consumption & Efficiency Metrics API
// -------------------------------------------------------------

export async function getFieldWaterSummary(
  fieldId: string,
  params: FieldWaterSummaryQueryParams = {}
): Promise<FieldWaterSummary> {
  const searchParams = new URLSearchParams()
  if (params.from) searchParams.set('from', params.from)
  if (params.to) searchParams.set('to', params.to)

  const queryString = searchParams.toString()
  const endpoint = queryString
    ? `/water/fields/${fieldId}/summary?${queryString}`
    : `/water/fields/${fieldId}/summary`

  const response = await apiClient<FieldWaterSummary>(endpoint, { method: 'GET' })
  if (!response.data) throw new Error(`Field water summary for ${fieldId} not returned by server`)
  return response.data
}

export async function getAllocationConsumption(
  allocationId: string
): Promise<WaterAllocationConsumption> {
  const response = await apiClient<WaterAllocationConsumption>(
    `/water/allocations/${allocationId}/summary`,
    { method: 'GET' }
  )
  if (!response.data) {
    throw new Error(`Allocation consumption for ${allocationId} not returned by server`)
  }
  return response.data
}

export async function getPlanMetrics(planId: string): Promise<PlannedVsActualMetrics> {
  const response = await apiClient<PlannedVsActualMetrics>(
    `/water/plans/${planId}/metrics`,
    { method: 'GET' }
  )
  if (!response.data) {
    throw new Error(`Plan metrics for ${planId} not returned by server`)
  }
  return response.data
}
