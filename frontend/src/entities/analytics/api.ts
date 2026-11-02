import { apiClient } from '@/shared/api/client'
import {
  AnalyticsOverviewResponse,
  FarmInsightDto,
  FieldPerformanceRankingResponse,
  FieldPerformanceStatus,
  InsightCategory,
  InsightSeverity,
  PerformancePeriodType,
  PerformanceTrendsResponse,
  TrendGranularity,
} from './types'

export interface AnalyticsOverviewParams {
  period?: PerformancePeriodType
  from?: string
  to?: string
  fieldId?: string
  cropType?: string
}

export interface AnalyticsTrendsParams {
  period?: PerformancePeriodType
  from?: string
  to?: string
  fieldId?: string
  granularity?: TrendGranularity
}

export interface AnalyticsFieldsParams {
  period?: PerformancePeriodType
  from?: string
  to?: string
  cropType?: string
  status?: FieldPerformanceStatus
  page?: number
  size?: number
  sortBy?: string
  sortDirection?: 'asc' | 'desc'
}

export interface AnalyticsInsightsParams {
  period?: PerformancePeriodType
  from?: string
  to?: string
  fieldId?: string
  category?: InsightCategory
  severity?: InsightSeverity
}

export const analyticsApi = {
  fetchOverview: async (
    params: AnalyticsOverviewParams = {},
    signal?: AbortSignal
  ): Promise<AnalyticsOverviewResponse> => {
    const searchParams = new URLSearchParams()
    if (params.period) searchParams.set('period', params.period)
    if (params.from) searchParams.set('from', params.from)
    if (params.to) searchParams.set('to', params.to)
    if (params.fieldId) searchParams.set('fieldId', params.fieldId)
    if (params.cropType) searchParams.set('cropType', params.cropType)

    const query = searchParams.toString()
    const path = `/analytics/overview${query ? `?${query}` : ''}`
    const response = await apiClient<AnalyticsOverviewResponse>(path, { method: 'GET', signal })
    if (!response.data) throw new Error('Analytics overview not returned by server')
    return response.data
  },

  fetchTrends: async (
    params: AnalyticsTrendsParams = {},
    signal?: AbortSignal
  ): Promise<PerformanceTrendsResponse> => {
    const searchParams = new URLSearchParams()
    if (params.period) searchParams.set('period', params.period)
    if (params.from) searchParams.set('from', params.from)
    if (params.to) searchParams.set('to', params.to)
    if (params.fieldId) searchParams.set('fieldId', params.fieldId)
    if (params.granularity) searchParams.set('granularity', params.granularity)

    const query = searchParams.toString()
    const path = `/analytics/trends${query ? `?${query}` : ''}`
    const response = await apiClient<PerformanceTrendsResponse>(path, { method: 'GET', signal })
    if (!response.data) throw new Error('Performance trends not returned by server')
    return response.data
  },

  fetchFieldRankings: async (
    params: AnalyticsFieldsParams = {},
    signal?: AbortSignal
  ): Promise<FieldPerformanceRankingResponse> => {
    const searchParams = new URLSearchParams()
    if (params.period) searchParams.set('period', params.period)
    if (params.from) searchParams.set('from', params.from)
    if (params.to) searchParams.set('to', params.to)
    if (params.cropType) searchParams.set('cropType', params.cropType)
    if (params.status) searchParams.set('status', params.status)
    if (params.page !== undefined) searchParams.set('page', String(params.page))
    if (params.size !== undefined) searchParams.set('size', String(params.size))
    if (params.sortBy) searchParams.set('sortBy', params.sortBy)
    if (params.sortDirection) searchParams.set('sortDirection', params.sortDirection)

    const query = searchParams.toString()
    const path = `/analytics/fields${query ? `?${query}` : ''}`
    const response = await apiClient<FieldPerformanceRankingResponse>(path, { method: 'GET', signal })
    if (!response.data) throw new Error('Field rankings not returned by server')
    return response.data
  },

  fetchInsights: async (
    params: AnalyticsInsightsParams = {},
    signal?: AbortSignal
  ): Promise<FarmInsightDto[]> => {
    const searchParams = new URLSearchParams()
    if (params.period) searchParams.set('period', params.period)
    if (params.from) searchParams.set('from', params.from)
    if (params.to) searchParams.set('to', params.to)
    if (params.fieldId) searchParams.set('fieldId', params.fieldId)
    if (params.category) searchParams.set('category', params.category)
    if (params.severity) searchParams.set('severity', params.severity)

    const query = searchParams.toString()
    const path = `/analytics/insights${query ? `?${query}` : ''}`
    const response = await apiClient<FarmInsightDto[]>(path, { method: 'GET', signal })
    if (!response.data) throw new Error('Farm insights not returned by server')
    return response.data
  },
}
