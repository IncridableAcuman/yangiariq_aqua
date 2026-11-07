import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  riskApi,
  FarmRiskOverview,
  FieldRiskListParams,
  FieldRiskProfile,
  RiskRecommendation,
  RiskRecommendationListParams,
} from '@/entities/risk'
import { PageResponse } from '@/shared/api/client'

export const RISK_QUERY_KEYS = {
  all: ['risk'] as const,
  overview: () => [...RISK_QUERY_KEYS.all, 'overview'] as const,
  fields: (params?: FieldRiskListParams) => [...RISK_QUERY_KEYS.all, 'fields', params] as const,
  field: (fieldId?: string | null) => [...RISK_QUERY_KEYS.all, 'field', fieldId] as const,
  history: (fieldId?: string | null, params?: { from?: string; to?: string; page?: number; size?: number }) =>
    [...RISK_QUERY_KEYS.all, 'history', fieldId, params] as const,
  recommendations: (params?: RiskRecommendationListParams) =>
    [...RISK_QUERY_KEYS.all, 'recommendations', params] as const,
  recommendation: (id?: string | null) => [...RISK_QUERY_KEYS.all, 'recommendation', id] as const,
}

export function useRiskOverviewQuery() {
  return useQuery<FarmRiskOverview>({
    queryKey: RISK_QUERY_KEYS.overview(),
    queryFn: ({ signal }) => riskApi.getOverview(signal),
    staleTime: 1000 * 60 * 2, // 2 minutes
    refetchInterval: 1000 * 60 * 2,
  })
}

export function useFieldRiskListQuery(params?: FieldRiskListParams) {
  return useQuery<PageResponse<FieldRiskProfile>>({
    queryKey: RISK_QUERY_KEYS.fields(params),
    queryFn: ({ signal }) => riskApi.listFields(params, signal),
    staleTime: 1000 * 60 * 2,
  })
}

export function useFieldRiskProfileQuery(fieldId?: string | null) {
  return useQuery<FieldRiskProfile>({
    queryKey: RISK_QUERY_KEYS.field(fieldId),
    queryFn: ({ signal }) => {
      if (!fieldId) throw new Error('fieldId is required')
      return riskApi.getFieldProfile(fieldId, signal)
    },
    enabled: Boolean(fieldId),
    staleTime: 1000 * 60 * 2,
  })
}

export function useFieldRiskHistoryQuery(
  fieldId?: string | null,
  params?: { from?: string; to?: string; page?: number; size?: number }
) {
  return useQuery<PageResponse<FieldRiskProfile>>({
    queryKey: RISK_QUERY_KEYS.history(fieldId, params),
    queryFn: ({ signal }) => {
      if (!fieldId) throw new Error('fieldId is required')
      return riskApi.getFieldHistory(fieldId, params, signal)
    },
    enabled: Boolean(fieldId),
    staleTime: 1000 * 60 * 5,
  })
}

export function useRiskRecommendationsQuery(params?: RiskRecommendationListParams) {
  return useQuery<PageResponse<RiskRecommendation>>({
    queryKey: RISK_QUERY_KEYS.recommendations(params),
    queryFn: ({ signal }) => riskApi.listRecommendations(params, signal),
    staleTime: 1000 * 60 * 2,
  })
}

export function useRiskRecommendationDetailQuery(id?: string | null) {
  return useQuery<RiskRecommendation>({
    queryKey: RISK_QUERY_KEYS.recommendation(id),
    queryFn: ({ signal }) => {
      if (!id) throw new Error('id is required')
      return riskApi.getRecommendationDetail(id, signal)
    },
    enabled: Boolean(id),
    staleTime: 1000 * 60 * 5,
  })
}

export function useAcknowledgeRiskMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, notes }: { id: string; notes?: string }) =>
      riskApi.acknowledgeAssessment(id, notes),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: RISK_QUERY_KEYS.all })
      queryClient.setQueryData(RISK_QUERY_KEYS.field(updated.fieldId), updated)
    },
  })
}

export function useResolveRiskMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, resolutionNotes }: { id: string; resolutionNotes: string }) =>
      riskApi.resolveAssessment(id, resolutionNotes),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: RISK_QUERY_KEYS.all })
      queryClient.setQueryData(RISK_QUERY_KEYS.field(updated.fieldId), updated)
    },
  })
}
