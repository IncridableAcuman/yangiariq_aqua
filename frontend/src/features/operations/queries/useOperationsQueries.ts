import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  operationsApi,
  PreparePlanPayload,
  ApprovePlanPayload,
  RejectPlanPayload,
  SchedulePlanPayload,
  StartOperationPayload,
  CompleteOperationPayload,
} from '@/entities/operations'

export function useOperationalContext(fieldId: string | null) {
  return useQuery({
    queryKey: ['operations', 'context', fieldId],
    queryFn: ({ signal }) => {
      if (!fieldId) throw new Error('Field ID is required')
      return operationsApi.fetchContext(fieldId, signal)
    },
    enabled: Boolean(fieldId),
    staleTime: 1000 * 30, // 30 seconds
    refetchInterval: 1000 * 30, // Background poll every 30s during active operations
  })
}

export function useOperationalVerification(planId: string | null) {
  return useQuery({
    queryKey: ['operations', 'verification', planId],
    queryFn: ({ signal }) => {
      if (!planId) throw new Error('Plan ID is required')
      return operationsApi.fetchVerification(planId, signal)
    },
    enabled: Boolean(planId),
    staleTime: 1000 * 60 * 5,
  })
}

export function usePreparePlanMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ fieldId, payload }: { fieldId: string; payload: PreparePlanPayload }) =>
      operationsApi.preparePlan(fieldId, payload),
    onSuccess: (_, { fieldId }) => {
      queryClient.invalidateQueries({ queryKey: ['operations', 'context', fieldId] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      queryClient.invalidateQueries({ queryKey: ['irrigation'] })
    },
  })
}

export function useApprovePlanMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ planId, payload }: { planId: string; payload: ApprovePlanPayload; fieldId?: string }) =>
      operationsApi.approvePlan(planId, payload),
    onSuccess: (_, variables) => {
      if (variables.fieldId) {
        queryClient.invalidateQueries({ queryKey: ['operations', 'context', variables.fieldId] })
      } else {
        queryClient.invalidateQueries({ queryKey: ['operations', 'context'] })
      }
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      queryClient.invalidateQueries({ queryKey: ['irrigation'] })
      queryClient.invalidateQueries({ queryKey: ['recommendations'] })
    },
  })
}

export function useRejectPlanMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ planId, payload }: { planId: string; payload: RejectPlanPayload; fieldId?: string }) =>
      operationsApi.rejectPlan(planId, payload),
    onSuccess: (_, variables) => {
      if (variables.fieldId) {
        queryClient.invalidateQueries({ queryKey: ['operations', 'context', variables.fieldId] })
      } else {
        queryClient.invalidateQueries({ queryKey: ['operations', 'context'] })
      }
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      queryClient.invalidateQueries({ queryKey: ['irrigation'] })
    },
  })
}

export function useSchedulePlanMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ planId, payload }: { planId: string; payload: SchedulePlanPayload; fieldId?: string }) =>
      operationsApi.schedulePlan(planId, payload),
    onSuccess: (_, variables) => {
      if (variables.fieldId) {
        queryClient.invalidateQueries({ queryKey: ['operations', 'context', variables.fieldId] })
      } else {
        queryClient.invalidateQueries({ queryKey: ['operations', 'context'] })
      }
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      queryClient.invalidateQueries({ queryKey: ['irrigation'] })
    },
  })
}

export function useStartOperationMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ planId, payload }: { planId: string; payload: StartOperationPayload; fieldId?: string }) =>
      operationsApi.startOperation(planId, payload),
    onSuccess: (_, variables) => {
      if (variables.fieldId) {
        queryClient.invalidateQueries({ queryKey: ['operations', 'context', variables.fieldId] })
      } else {
        queryClient.invalidateQueries({ queryKey: ['operations', 'context'] })
      }
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      queryClient.invalidateQueries({ queryKey: ['irrigation'] })
      queryClient.invalidateQueries({ queryKey: ['water'] })
    },
  })
}

export function useCompleteOperationMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ planId, payload }: { planId: string; payload: CompleteOperationPayload; fieldId?: string }) =>
      operationsApi.completeOperation(planId, payload),
    onSuccess: (_, variables) => {
      if (variables.fieldId) {
        queryClient.invalidateQueries({ queryKey: ['operations', 'context', variables.fieldId] })
      } else {
        queryClient.invalidateQueries({ queryKey: ['operations', 'context'] })
      }
      queryClient.invalidateQueries({ queryKey: ['operations', 'verification', variables.planId] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      queryClient.invalidateQueries({ queryKey: ['irrigation'] })
      queryClient.invalidateQueries({ queryKey: ['water'] })
    },
  })
}
