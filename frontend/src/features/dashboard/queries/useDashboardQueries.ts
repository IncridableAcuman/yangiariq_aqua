import { useQuery } from '@tanstack/react-query'
import {
  DashboardOverviewParams,
  DashboardOverviewResponse,
  fetchDashboardOverview,
} from '@/entities/dashboard'

export const DASHBOARD_QUERY_KEY = 'dashboard'

export function useDashboardOverviewQuery(params: DashboardOverviewParams = {}) {
  return useQuery<DashboardOverviewResponse, Error>({
    queryKey: [DASHBOARD_QUERY_KEY, 'overview', params],
    queryFn: () => fetchDashboardOverview(params),
    staleTime: 1000 * 60, // 1 minute fresh
    refetchInterval: 1000 * 60 * 2, // Background polling every 2 minutes
    refetchOnWindowFocus: true,
  })
}
