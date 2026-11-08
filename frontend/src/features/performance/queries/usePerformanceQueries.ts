import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  CreateBaselinePayload,
  CreateImpactAssessmentPayload,
  CreateTargetPayload,
  performanceApi,
  ReviewImpactAssessmentPayload,
  RunEvaluationPayload,
} from '@/entities/performance'

export const PERFORMANCE_QUERY_KEYS = {
  overview: ['performance', 'overview'] as const,
  indicators: (category?: string) => ['performance', 'indicators', category] as const,
  indicator: (code: string) => ['performance', 'indicator', code] as const,
  trends: (code?: string, interval?: string) => ['performance', 'trends', code, interval] as const,
  baselines: (code?: string, scope?: string) => ['performance', 'baselines', code, scope] as const,
  targets: (code?: string, scope?: string) => ['performance', 'targets', code, scope] as const,
  evaluations: ['performance', 'evaluations'] as const,
  evaluation: (id: string) => ['performance', 'evaluation', id] as const,
  impactAssessments: (params?: Record<string, string | undefined>) => ['performance', 'impactAssessments', params] as const,
  report: (params?: Record<string, string | undefined>) => ['performance', 'report', params] as const,
}

export function usePerformanceOverview() {
  return useQuery({
    queryKey: PERFORMANCE_QUERY_KEYS.overview,
    queryFn: ({ signal }) => performanceApi.getOverview(signal),
    staleTime: 30_000,
  })
}

export function usePerformanceIndicators(category?: string) {
  return useQuery({
    queryKey: PERFORMANCE_QUERY_KEYS.indicators(category),
    queryFn: ({ signal }) => performanceApi.listIndicators(category, signal),
    staleTime: 60_000,
  })
}

export function usePerformanceIndicator(code: string) {
  return useQuery({
    queryKey: PERFORMANCE_QUERY_KEYS.indicator(code),
    queryFn: ({ signal }) => performanceApi.getIndicator(code, signal),
    enabled: !!code,
  })
}

export function usePerformanceTrends(indicatorCode?: string, interval?: string) {
  return useQuery({
    queryKey: PERFORMANCE_QUERY_KEYS.trends(indicatorCode, interval),
    queryFn: ({ signal }) => performanceApi.getTrends(indicatorCode, interval, signal),
    staleTime: 30_000,
  })
}

export function usePerformanceBaselines(indicatorCode?: string, scopeType?: string) {
  return useQuery({
    queryKey: PERFORMANCE_QUERY_KEYS.baselines(indicatorCode, scopeType),
    queryFn: ({ signal }) => performanceApi.listBaselines(indicatorCode, scopeType, signal),
    staleTime: 30_000,
  })
}

export function useCreateBaseline() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateBaselinePayload) => performanceApi.createBaseline(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['performance', 'baselines'] })
      queryClient.invalidateQueries({ queryKey: PERFORMANCE_QUERY_KEYS.overview })
    },
  })
}

export function usePerformanceTargets(indicatorCode?: string, scopeType?: string) {
  return useQuery({
    queryKey: PERFORMANCE_QUERY_KEYS.targets(indicatorCode, scopeType),
    queryFn: ({ signal }) => performanceApi.listTargets(indicatorCode, scopeType, signal),
    staleTime: 30_000,
  })
}

export function useCreateTarget() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateTargetPayload) => performanceApi.createTarget(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['performance', 'targets'] })
      queryClient.invalidateQueries({ queryKey: PERFORMANCE_QUERY_KEYS.overview })
    },
  })
}

export function useRunEvaluation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: RunEvaluationPayload) => performanceApi.runEvaluation(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: PERFORMANCE_QUERY_KEYS.overview })
      queryClient.invalidateQueries({ queryKey: ['performance', 'trends'] })
      queryClient.invalidateQueries({ queryKey: ['performance', 'evaluations'] })
    },
  })
}

export function useImpactAssessments(params?: {
  interventionType?: string
  attributionLevel?: string
  reviewStatus?: string
  scopeId?: string
}) {
  return useQuery({
    queryKey: PERFORMANCE_QUERY_KEYS.impactAssessments(params),
    queryFn: ({ signal }) => performanceApi.listImpactAssessments(params, signal),
    staleTime: 15_000,
  })
}

export function useCreateImpactAssessment() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateImpactAssessmentPayload) => performanceApi.createImpactAssessment(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['performance', 'impactAssessments'] })
      queryClient.invalidateQueries({ queryKey: PERFORMANCE_QUERY_KEYS.overview })
    },
  })
}

export function useReviewImpactAssessment() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: ReviewImpactAssessmentPayload }) =>
      performanceApi.reviewImpactAssessment(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['performance', 'impactAssessments'] })
      queryClient.invalidateQueries({ queryKey: PERFORMANCE_QUERY_KEYS.overview })
    },
  })
}
