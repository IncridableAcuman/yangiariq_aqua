import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  SmartIrrigationPlanFilterParams,
  GeneratePlanRequest,
  ApprovePlanRequest,
  CancelPlanRequest,
  CompletePlanRequest,
  listSmartIrrigationPlans,
  getSmartIrrigationPlan,
  listFieldSmartIrrigationPlans,
  getActiveFieldSmartIrrigationPlan,
  generateSmartIrrigationPlan,
  approveSmartIrrigationPlan,
  cancelSmartIrrigationPlan,
  completeSmartIrrigationPlan,
  getFieldWaterBudget,
} from '@/entities/irrigation'

export const smartIrrigationQueryKeys = {
  all: ['smart-irrigation-plans'] as const,
  list: (params?: SmartIrrigationPlanFilterParams) => [...smartIrrigationQueryKeys.all, 'list', params] as const,
  detail: (id: string) => [...smartIrrigationQueryKeys.all, 'detail', id] as const,
  fieldList: (fieldId: string, params?: SmartIrrigationPlanFilterParams) =>
    [...smartIrrigationQueryKeys.all, 'field', fieldId, params] as const,
  activeFieldPlan: (fieldId: string) => [...smartIrrigationQueryKeys.all, 'active', fieldId] as const,
  waterBudget: (fieldId: string) => ['water-budget', fieldId] as const,
}

export function useSmartIrrigationPlans(params?: SmartIrrigationPlanFilterParams) {
  return useQuery({
    queryKey: smartIrrigationQueryKeys.list(params),
    queryFn: () => listSmartIrrigationPlans(params),
    refetchInterval: 30000,
  })
}

export function useSmartIrrigationPlan(id: string) {
  return useQuery({
    queryKey: smartIrrigationQueryKeys.detail(id),
    queryFn: () => getSmartIrrigationPlan(id),
    enabled: Boolean(id),
  })
}

export function useFieldSmartIrrigationPlans(fieldId: string, params?: SmartIrrigationPlanFilterParams) {
  return useQuery({
    queryKey: smartIrrigationQueryKeys.fieldList(fieldId, params),
    queryFn: () => listFieldSmartIrrigationPlans(fieldId, params),
    enabled: Boolean(fieldId),
  })
}

export function useActiveFieldSmartIrrigationPlan(fieldId?: string) {
  return useQuery({
    queryKey: smartIrrigationQueryKeys.activeFieldPlan(fieldId || ''),
    queryFn: () => (fieldId ? getActiveFieldSmartIrrigationPlan(fieldId) : null),
    enabled: Boolean(fieldId),
  })
}

export function useFieldWaterBudget(fieldId?: string) {
  return useQuery({
    queryKey: smartIrrigationQueryKeys.waterBudget(fieldId || ''),
    queryFn: () => (fieldId ? getFieldWaterBudget(fieldId) : null),
    enabled: Boolean(fieldId),
  })
}

export function useGenerateSmartPlanMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ fieldId, request }: { fieldId: string; request?: GeneratePlanRequest }) =>
      generateSmartIrrigationPlan(fieldId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: smartIrrigationQueryKeys.all })
      queryClient.invalidateQueries({ queryKey: ['water-budget'] })
    },
  })
}

export function useApproveSmartPlanMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, request }: { id: string; request?: ApprovePlanRequest }) =>
      approveSmartIrrigationPlan(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: smartIrrigationQueryKeys.all })
      queryClient.invalidateQueries({ queryKey: ['water-budget'] })
    },
  })
}

export function useCancelSmartPlanMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, request }: { id: string; request: CancelPlanRequest }) =>
      cancelSmartIrrigationPlan(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: smartIrrigationQueryKeys.all })
      queryClient.invalidateQueries({ queryKey: ['water-budget'] })
    },
  })
}

export function useCompleteSmartPlanMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, request }: { id: string; request?: CompletePlanRequest }) =>
      completeSmartIrrigationPlan(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: smartIrrigationQueryKeys.all })
      queryClient.invalidateQueries({ queryKey: ['water-budget'] })
      queryClient.invalidateQueries({ queryKey: ['irrigation-events'] })
    },
  })
}
