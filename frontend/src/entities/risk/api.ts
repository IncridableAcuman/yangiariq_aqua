import { apiClient, PageResponse } from '@/shared/api/client'
import {
  FarmRiskOverview,
  FieldRiskListParams,
  FieldRiskProfile,
  RiskRecommendation,
  RiskRecommendationListParams,
} from './types'

export const riskApi = {
  getOverview: async (signal?: AbortSignal): Promise<FarmRiskOverview> => {
    const response = await apiClient<FarmRiskOverview>('/risk-intelligence/overview', {
      method: 'GET',
      signal,
    })
    if (!response.data) throw new Error('Risk overview data not returned by server')
    return response.data
  },

  listFields: async (
    params?: FieldRiskListParams,
    signal?: AbortSignal
  ): Promise<PageResponse<FieldRiskProfile>> => {
    const query = new URLSearchParams()
    if (params?.severity) query.set('severity', params.severity)
    if (params?.category) query.set('category', params.category)
    if (params?.confidence) query.set('confidence', params.confidence)
    if (params?.status) query.set('status', params.status)
    if (params?.search) query.set('search', params.search)
    if (params?.sortBy) query.set('sortBy', params.sortBy)
    if (params?.sortDirection) query.set('sortDirection', params.sortDirection)
    if (params?.page !== undefined) query.set('page', params.page.toString())
    if (params?.size !== undefined) query.set('size', params.size.toString())

    const qs = query.toString()
    const path = `/risk-intelligence/fields${qs ? `?${qs}` : ''}`
    const response = await apiClient<PageResponse<FieldRiskProfile>>(path, {
      method: 'GET',
      signal,
    })
    if (!response.data) throw new Error('Field risk profiles not returned by server')
    return response.data
  },

  getFieldProfile: async (fieldId: string, signal?: AbortSignal): Promise<FieldRiskProfile> => {
    const response = await apiClient<FieldRiskProfile>(
      `/risk-intelligence/fields/${encodeURIComponent(fieldId)}`,
      {
        method: 'GET',
        signal,
      }
    )
    if (!response.data) throw new Error('Field risk profile not returned by server')
    return response.data
  },

  getFieldHistory: async (
    fieldId: string,
    params?: { from?: string; to?: string; page?: number; size?: number },
    signal?: AbortSignal
  ): Promise<PageResponse<FieldRiskProfile>> => {
    const query = new URLSearchParams()
    if (params?.from) query.set('from', params.from)
    if (params?.to) query.set('to', params.to)
    if (params?.page !== undefined) query.set('page', params.page.toString())
    if (params?.size !== undefined) query.set('size', params.size.toString())

    const qs = query.toString()
    const path = `/risk-intelligence/fields/${encodeURIComponent(fieldId)}/history${qs ? `?${qs}` : ''}`
    const response = await apiClient<PageResponse<FieldRiskProfile>>(path, {
      method: 'GET',
      signal,
    })
    if (!response.data) throw new Error('Field risk history not returned by server')
    return response.data
  },

  listRecommendations: async (
    params?: RiskRecommendationListParams,
    signal?: AbortSignal
  ): Promise<PageResponse<RiskRecommendation>> => {
    const query = new URLSearchParams()
    if (params?.severity) query.set('severity', params.severity)
    if (params?.category) query.set('category', params.category)
    if (params?.fieldId) query.set('fieldId', params.fieldId)
    if (params?.page !== undefined) query.set('page', params.page.toString())
    if (params?.size !== undefined) query.set('size', params.size.toString())

    const qs = query.toString()
    const path = `/risk-intelligence/recommendations${qs ? `?${qs}` : ''}`
    const response = await apiClient<PageResponse<RiskRecommendation>>(path, {
      method: 'GET',
      signal,
    })
    if (!response.data) throw new Error('Risk recommendations not returned by server')
    return response.data
  },

  getRecommendationDetail: async (
    id: string,
    signal?: AbortSignal
  ): Promise<RiskRecommendation> => {
    const response = await apiClient<RiskRecommendation>(
      `/risk-intelligence/recommendations/${encodeURIComponent(id)}`,
      {
        method: 'GET',
        signal,
      }
    )
    if (!response.data) throw new Error('Risk recommendation detail not returned by server')
    return response.data
  },

  acknowledgeAssessment: async (
    id: string,
    notes?: string
  ): Promise<FieldRiskProfile> => {
    const response = await apiClient<FieldRiskProfile>(
      `/risk-intelligence/assessments/${encodeURIComponent(id)}/acknowledge`,
      {
        method: 'POST',
        body: JSON.stringify({ notes }),
      }
    )
    if (!response.data) throw new Error('Failed to acknowledge risk assessment')
    return response.data
  },

  resolveAssessment: async (
    id: string,
    resolutionNotes: string
  ): Promise<FieldRiskProfile> => {
    const response = await apiClient<FieldRiskProfile>(
      `/risk-intelligence/assessments/${encodeURIComponent(id)}/resolve`,
      {
        method: 'POST',
        body: JSON.stringify({ resolutionNotes }),
      }
    )
    if (!response.data) throw new Error('Failed to resolve risk assessment')
    return response.data
  },
}
