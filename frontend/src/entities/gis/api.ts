import { apiClient } from '@/shared/api/client'
import { GisOverviewData, GisOverviewParams } from './types'

export async function getGisOverview(
  params: GisOverviewParams = {}
): Promise<GisOverviewData> {
  const searchParams = new URLSearchParams()
  if (params.from) searchParams.set('from', params.from)
  if (params.to) searchParams.set('to', params.to)

  const queryString = searchParams.toString()
  const endpoint = queryString ? `/gis/overview?${queryString}` : '/gis/overview'

  const response = await apiClient<GisOverviewData>(endpoint, { method: 'GET' })
  if (!response.data) {
    throw new Error('GIS overview payload not returned by server')
  }
  return response.data
}
