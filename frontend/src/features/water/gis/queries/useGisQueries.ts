import { useQuery, useQueryClient, UseQueryResult } from '@tanstack/react-query'
import { getGisOverview, GisOverviewData, GisOverviewParams } from '@/entities/gis'

export const gisKeys = {
  all: ['gis'] as const,
  overview: (params: GisOverviewParams = {}) =>
    [...gisKeys.all, 'overview', params] as const,
}

export function useGisOverviewQuery(
  params: GisOverviewParams = {}
): UseQueryResult<GisOverviewData, Error> {
  return useQuery({
    queryKey: gisKeys.overview(params),
    queryFn: () => getGisOverview(params),
    staleTime: 1000 * 60 * 2,
    placeholderData: (previousData) => previousData,
  })
}

export function useInvalidateGis() {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: gisKeys.all })
  }
}
