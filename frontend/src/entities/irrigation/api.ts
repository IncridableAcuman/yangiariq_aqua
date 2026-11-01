import { apiClient, PageResponse } from '@/shared/api/client'
import {
  SmartIrrigationPlan,
  WaterBudget,
  SmartIrrigationPlanFilterParams,
  GeneratePlanRequest,
  ApprovePlanRequest,
  CancelPlanRequest,
  CompletePlanRequest,
} from './types'

export async function listSmartIrrigationPlans(
  params?: SmartIrrigationPlanFilterParams
): Promise<PageResponse<SmartIrrigationPlan>> {
  const query = new URLSearchParams()
  if (params?.fieldId) query.set('fieldId', params.fieldId)
  if (params?.status) query.set('status', params.status)
  if (params?.priority) query.set('priority', params.priority)
  if (params?.dateFrom) query.set('dateFrom', params.dateFrom)
  if (params?.dateTo) query.set('dateTo', params.dateTo)
  if (params?.page !== undefined) query.set('page', String(params.page))
  if (params?.size !== undefined) query.set('size', String(params.size))

  const qs = query.toString()
  const endpoint = qs ? `/irrigation/plans?${qs}` : '/irrigation/plans'
  const response = await apiClient<PageResponse<SmartIrrigationPlan>>(endpoint, { method: 'GET' })
  if (!response.data) throw new Error('Smart irrigation plans not returned by server')
  return response.data
}

export async function getSmartIrrigationPlan(id: string): Promise<SmartIrrigationPlan> {
  const response = await apiClient<SmartIrrigationPlan>(`/irrigation/plans/${id}`, { method: 'GET' })
  if (!response.data) throw new Error('Smart irrigation plan not found')
  return response.data
}

export async function listFieldSmartIrrigationPlans(
  fieldId: string,
  params?: SmartIrrigationPlanFilterParams
): Promise<PageResponse<SmartIrrigationPlan>> {
  const query = new URLSearchParams()
  if (params?.status) query.set('status', params.status)
  if (params?.priority) query.set('priority', params.priority)
  if (params?.dateFrom) query.set('dateFrom', params.dateFrom)
  if (params?.dateTo) query.set('dateTo', params.dateTo)
  if (params?.page !== undefined) query.set('page', String(params.page))
  if (params?.size !== undefined) query.set('size', String(params.size))

  const qs = query.toString()
  const endpoint = qs ? `/fields/${fieldId}/irrigation/plans?${qs}` : `/fields/${fieldId}/irrigation/plans`
  const response = await apiClient<PageResponse<SmartIrrigationPlan>>(endpoint, { method: 'GET' })
  if (!response.data) throw new Error('Field smart irrigation plans not returned by server')
  return response.data
}

export async function getActiveFieldSmartIrrigationPlan(
  fieldId: string
): Promise<SmartIrrigationPlan | null> {
  try {
    const response = await apiClient<SmartIrrigationPlan>(`/fields/${fieldId}/irrigation/plans/active`, {
      method: 'GET',
    })
    return response.data || null
  } catch (error) {
    // 404 indicates no active plan exists currently
    return null
  }
}

export async function generateSmartIrrigationPlan(
  fieldId: string,
  request?: GeneratePlanRequest
): Promise<SmartIrrigationPlan> {
  const response = await apiClient<SmartIrrigationPlan>(`/fields/${fieldId}/irrigation/plans/generate`, {
    method: 'POST',
    body: JSON.stringify(request || {}),
  })
  if (!response.data) throw new Error('Failed to generate smart irrigation plan')
  return response.data
}

export async function approveSmartIrrigationPlan(
  id: string,
  request?: ApprovePlanRequest
): Promise<SmartIrrigationPlan> {
  const response = await apiClient<SmartIrrigationPlan>(`/irrigation/plans/${id}/approve`, {
    method: 'POST',
    body: JSON.stringify(request || {}),
  })
  if (!response.data) throw new Error('Failed to approve smart irrigation plan')
  return response.data
}

export async function cancelSmartIrrigationPlan(
  id: string,
  request: CancelPlanRequest
): Promise<SmartIrrigationPlan> {
  const response = await apiClient<SmartIrrigationPlan>(`/irrigation/plans/${id}/cancel`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
  if (!response.data) throw new Error('Failed to cancel smart irrigation plan')
  return response.data
}

export async function completeSmartIrrigationPlan(
  id: string,
  request?: CompletePlanRequest
): Promise<SmartIrrigationPlan> {
  const response = await apiClient<SmartIrrigationPlan>(`/irrigation/plans/${id}/complete`, {
    method: 'POST',
    body: JSON.stringify(request || {}),
  })
  if (!response.data) throw new Error('Failed to complete smart irrigation plan')
  return response.data
}

export async function getFieldWaterBudget(fieldId: string): Promise<WaterBudget | null> {
  try {
    const response = await apiClient<WaterBudget>(`/fields/${fieldId}/water-budget`, { method: 'GET' })
    return response.data || null
  } catch (error) {
    return null
  }
}
