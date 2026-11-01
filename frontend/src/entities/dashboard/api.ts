import { apiClient } from '@/shared/api/client'
import { DashboardOverviewParams, DashboardOverviewResponse } from './types'

export async function fetchDashboardOverview(
  params: DashboardOverviewParams = {}
): Promise<DashboardOverviewResponse> {
  const searchParams = new URLSearchParams()
  if (params.fieldId) searchParams.set('fieldId', params.fieldId)
  if (params.cropType) searchParams.set('cropType', params.cropType)
  if (params.priority) searchParams.set('priority', params.priority)
  if (params.from) searchParams.set('from', params.from)
  if (params.to) searchParams.set('to', params.to)

  const queryString = searchParams.toString()
  const endpoint = queryString ? `/dashboard/overview?${queryString}` : '/dashboard/overview'

  const response = await apiClient<DashboardOverviewResponse>(endpoint, { method: 'GET' })
  if (!response.data) {
    throw new Error('Dashboard overview data was not returned by server')
  }
  return response.data
}
