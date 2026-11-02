import { useQuery } from '@tanstack/react-query'
import {
  analyticsApi,
  AnalyticsFieldsParams,
  AnalyticsInsightsParams,
  AnalyticsOverviewParams,
  AnalyticsTrendsParams,
} from '@/entities/analytics'

export const ANALYTICS_QUERY_KEYS = {
  overview: (params: AnalyticsOverviewParams) => ['analytics', 'overview', params] as const,
  trends: (params: AnalyticsTrendsParams) => ['analytics', 'trends', params] as const,
  fields: (params: AnalyticsFieldsParams) => ['analytics', 'fields', params] as const,
  insights: (params: AnalyticsInsightsParams) => ['analytics', 'insights', params] as const,
}

export function useAnalyticsOverview(params: AnalyticsOverviewParams = {}, enabled: boolean = true) {
  return useQuery({
    queryKey: ANALYTICS_QUERY_KEYS.overview(params),
    queryFn: ({ signal }) => analyticsApi.fetchOverview(params, signal),
    enabled,
    staleTime: 60_000,
  })
}

export function usePerformanceTrends(params: AnalyticsTrendsParams = {}, enabled: boolean = true) {
  return useQuery({
    queryKey: ANALYTICS_QUERY_KEYS.trends(params),
    queryFn: ({ signal }) => analyticsApi.fetchTrends(params, signal),
    enabled,
    staleTime: 60_000,
  })
}

export function useFieldPerformanceRankings(params: AnalyticsFieldsParams = {}, enabled: boolean = true) {
  return useQuery({
    queryKey: ANALYTICS_QUERY_KEYS.fields(params),
    queryFn: ({ signal }) => analyticsApi.fetchFieldRankings(params, signal),
    enabled,
    staleTime: 60_000,
  })
}

export function useFarmInsights(params: AnalyticsInsightsParams = {}, enabled: boolean = true) {
  return useQuery({
    queryKey: ANALYTICS_QUERY_KEYS.insights(params),
    queryFn: ({ signal }) => analyticsApi.fetchInsights(params, signal),
    enabled,
    staleTime: 60_000,
  })
}
