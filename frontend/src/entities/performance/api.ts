import { apiClient } from '@/shared/api/client'
import {
  ImpactAssessment,
  PerformanceBaseline,
  PerformanceEvaluation,
  PerformanceIndicator,
  PerformanceOverview,
  PerformanceReport,
  PerformanceTarget,
  PerformanceTrendResponse,
} from './types'

export interface CreateBaselinePayload {
  indicatorCode: string
  name: string
  description?: string
  scopeType: string
  scopeId?: string
  scopeName: string
  periodStart: string
  periodEnd: string
  baselineValue: number
  measurementUnit: string
  dataCompletenessPct?: number
  evidenceClassification?: string
  baselineQuality?: string
  calculationMethod?: string
  seasonContext?: string
}

export interface CreateTargetPayload {
  indicatorCode: string
  title: string
  scopeType: string
  scopeId?: string
  scopeName: string
  targetType: string
  targetCategory?: string
  targetValue: number
  rangeMin?: number
  rangeMax?: number
  measurementUnit: string
  effectiveStart: string
  effectiveEnd?: string
  targetOwner: string
  rationale?: string
}

export interface RunEvaluationPayload {
  indicatorCode: string
  scopeType: string
  scopeId?: string
  periodStart?: string
  periodEnd?: string
  customValue?: number
  calculationVersion?: string
}

export interface CreateImpactAssessmentPayload {
  title: string
  interventionType: string
  interventionId: string
  interventionReference: string
  interventionDate: string
  indicatorCode: string
  scopeType: string
  scopeId?: string
  scopeName: string
  baselineId?: string
  baselinePeriodStart: string
  baselinePeriodEnd: string
  baselineValue: number
  evaluationPeriodStart: string
  evaluationPeriodEnd: string
  postInterventionValue: number
  measurementUnit: string
  evidenceClassification?: string
  baselineQuality?: string
  attributionLevel?: string
  confoundingFactors?: string
  dataLimitations?: string
  methodologyNotes: string
}

export interface ReviewImpactAssessmentPayload {
  reviewStatus: string
  reviewNotes?: string
}

export const performanceApi = {
  getOverview: async (signal?: AbortSignal): Promise<PerformanceOverview> => {
    const res = await apiClient<PerformanceOverview>('/performance/overview', {
      method: 'GET',
      signal,
    })
    if (!res.data) throw new Error('Failed to load performance overview')
    return res.data
  },

  listIndicators: async (category?: string, signal?: AbortSignal): Promise<PerformanceIndicator[]> => {
    const searchParams = new URLSearchParams()
    if (category) searchParams.set('category', category)
    const query = searchParams.toString() ? `?${searchParams.toString()}` : ''

    const res = await apiClient<PerformanceIndicator[]>(`/performance/indicators${query}`, {
      method: 'GET',
      signal,
    })
    if (!res.data) throw new Error('Failed to load performance indicators')
    return res.data
  },

  getIndicator: async (code: string, signal?: AbortSignal): Promise<PerformanceIndicator> => {
    const res = await apiClient<PerformanceIndicator>(`/performance/indicators/${code}`, {
      method: 'GET',
      signal,
    })
    if (!res.data) throw new Error(`Failed to load indicator ${code}`)
    return res.data
  },

  getTrends: async (indicatorCode?: string, interval?: string, signal?: AbortSignal): Promise<PerformanceTrendResponse> => {
    const searchParams = new URLSearchParams()
    if (indicatorCode) searchParams.set('indicatorCode', indicatorCode)
    if (interval) searchParams.set('interval', interval)
    const query = searchParams.toString() ? `?${searchParams.toString()}` : ''

    const res = await apiClient<PerformanceTrendResponse>(`/performance/trends${query}`, {
      method: 'GET',
      signal,
    })
    if (!res.data) throw new Error('Failed to load performance trends')
    return res.data
  },

  listBaselines: async (indicatorCode?: string, scopeType?: string, signal?: AbortSignal): Promise<PerformanceBaseline[]> => {
    const searchParams = new URLSearchParams()
    if (indicatorCode) searchParams.set('indicatorCode', indicatorCode)
    if (scopeType) searchParams.set('scopeType', scopeType)
    const query = searchParams.toString() ? `?${searchParams.toString()}` : ''

    const res = await apiClient<PerformanceBaseline[]>(`/performance/baselines${query}`, {
      method: 'GET',
      signal,
    })
    if (!res.data) throw new Error('Failed to load baselines')
    return res.data
  },

  createBaseline: async (payload: CreateBaselinePayload): Promise<PerformanceBaseline> => {
    const res = await apiClient<PerformanceBaseline>('/performance/baselines', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to create baseline')
    return res.data
  },

  listTargets: async (indicatorCode?: string, scopeType?: string, signal?: AbortSignal): Promise<PerformanceTarget[]> => {
    const searchParams = new URLSearchParams()
    if (indicatorCode) searchParams.set('indicatorCode', indicatorCode)
    if (scopeType) searchParams.set('scopeType', scopeType)
    const query = searchParams.toString() ? `?${searchParams.toString()}` : ''

    const res = await apiClient<PerformanceTarget[]>(`/performance/targets${query}`, {
      method: 'GET',
      signal,
    })
    if (!res.data) throw new Error('Failed to load targets')
    return res.data
  },

  createTarget: async (payload: CreateTargetPayload): Promise<PerformanceTarget> => {
    const res = await apiClient<PerformanceTarget>('/performance/targets', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to create target')
    return res.data
  },

  runEvaluation: async (payload: RunEvaluationPayload): Promise<PerformanceEvaluation> => {
    const res = await apiClient<PerformanceEvaluation>('/performance/evaluations', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to run performance evaluation')
    return res.data
  },

  getEvaluation: async (id: string, signal?: AbortSignal): Promise<PerformanceEvaluation> => {
    const res = await apiClient<PerformanceEvaluation>(`/performance/evaluations/${id}`, {
      method: 'GET',
      signal,
    })
    if (!res.data) throw new Error(`Failed to load evaluation ${id}`)
    return res.data
  },

  listImpactAssessments: async (
    params?: {
      interventionType?: string
      attributionLevel?: string
      reviewStatus?: string
      scopeId?: string
    },
    signal?: AbortSignal
  ): Promise<ImpactAssessment[]> => {
    const searchParams = new URLSearchParams()
    if (params?.interventionType) searchParams.set('interventionType', params.interventionType)
    if (params?.attributionLevel) searchParams.set('attributionLevel', params.attributionLevel)
    if (params?.reviewStatus) searchParams.set('reviewStatus', params.reviewStatus)
    if (params?.scopeId) searchParams.set('scopeId', params.scopeId)
    const query = searchParams.toString() ? `?${searchParams.toString()}` : ''

    const res = await apiClient<ImpactAssessment[]>(`/performance/impact-assessments${query}`, {
      method: 'GET',
      signal,
    })
    if (!res.data) throw new Error('Failed to load impact assessments')
    return res.data
  },

  createImpactAssessment: async (payload: CreateImpactAssessmentPayload): Promise<ImpactAssessment> => {
    const res = await apiClient<ImpactAssessment>('/performance/impact-assessments', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to create impact assessment')
    return res.data
  },

  reviewImpactAssessment: async (id: string, payload: ReviewImpactAssessmentPayload): Promise<ImpactAssessment> => {
    const res = await apiClient<ImpactAssessment>(`/performance/impact-assessments/${id}/review`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to review impact assessment')
    return res.data
  },

  getReport: async (params?: { startDate?: string; endDate?: string; scope?: string }, signal?: AbortSignal): Promise<PerformanceReport> => {
    const searchParams = new URLSearchParams()
    if (params?.startDate) searchParams.set('startDate', params.startDate)
    if (params?.endDate) searchParams.set('endDate', params.endDate)
    if (params?.scope) searchParams.set('scope', params.scope)
    const query = searchParams.toString() ? `?${searchParams.toString()}` : ''

    const res = await apiClient<PerformanceReport>(`/performance/reports${query}`, {
      method: 'GET',
      signal,
    })
    if (!res.data) throw new Error('Failed to generate performance report')
    return res.data
  },

  generateReport: async (
    scopeType?: string,
    scopeId?: string,
    interval?: string,
    signal?: AbortSignal
  ): Promise<PerformanceReport> => {
    const searchParams = new URLSearchParams()
    if (scopeType) searchParams.set('scope', scopeType)
    if (scopeId) searchParams.set('scopeId', scopeId)
    if (interval) searchParams.set('interval', interval)
    const query = searchParams.toString() ? `?${searchParams.toString()}` : ''

    const res = await apiClient<PerformanceReport>(`/performance/reports${query}`, {
      method: 'GET',
      signal,
    })
    if (!res.data) throw new Error('Failed to generate performance report')
    return res.data
  },
}
