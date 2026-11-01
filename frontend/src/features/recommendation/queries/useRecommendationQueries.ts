import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  RecommendationFilterParams,
  acceptRecommendation,
  completeRecommendation,
  dismissRecommendation,
  generateRecommendation,
  getRecommendation,
  getRecommendationSummary,
  listFieldRecommendations,
  listRecommendations,
  markRecommendationViewed,
} from '@/entities/recommendation'

export const recommendationQueryKeys = {
  all: ['recommendations'] as const,
  list: (params?: RecommendationFilterParams) => [...recommendationQueryKeys.all, 'list', params] as const,
  detail: (id: string) => [...recommendationQueryKeys.all, 'detail', id] as const,
  summary: (fieldId?: string) => [...recommendationQueryKeys.all, 'summary', fieldId] as const,
  fieldList: (fieldId: string, page: number, size: number) =>
    [...recommendationQueryKeys.all, 'field', fieldId, { page, size }] as const,
}

export function useRecommendations(params?: RecommendationFilterParams) {
  return useQuery({
    queryKey: recommendationQueryKeys.list(params),
    queryFn: () => listRecommendations(params),
    refetchInterval: 30000,
  })
}

export function useRecommendation(id: string) {
  return useQuery({
    queryKey: recommendationQueryKeys.detail(id),
    queryFn: () => getRecommendation(id),
    enabled: Boolean(id),
  })
}

export function useFieldRecommendations(fieldId: string, page = 0, size = 20) {
  return useQuery({
    queryKey: recommendationQueryKeys.fieldList(fieldId, page, size),
    queryFn: () => listFieldRecommendations(fieldId, page, size),
    enabled: Boolean(fieldId),
  })
}

export function useRecommendationSummary(fieldId?: string) {
  return useQuery({
    queryKey: recommendationQueryKeys.summary(fieldId),
    queryFn: () => getRecommendationSummary(fieldId),
    refetchInterval: 30000,
  })
}

export function useGenerateRecommendation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (fieldId: string) => generateRecommendation(fieldId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: recommendationQueryKeys.all })
    },
  })
}

export function useMarkRecommendationViewed() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => markRecommendationViewed(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: recommendationQueryKeys.all })
      queryClient.setQueryData(recommendationQueryKeys.detail(data.id), data)
    },
  })
}

export function useAcceptRecommendation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => acceptRecommendation(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: recommendationQueryKeys.all })
      queryClient.setQueryData(recommendationQueryKeys.detail(data.id), data)
    },
  })
}

export function useDismissRecommendation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => dismissRecommendation(id, reason),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: recommendationQueryKeys.all })
      queryClient.setQueryData(recommendationQueryKeys.detail(data.id), data)
    },
  })
}

export function useCompleteRecommendation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => completeRecommendation(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: recommendationQueryKeys.all })
      queryClient.setQueryData(recommendationQueryKeys.detail(data.id), data)
    },
  })
}
