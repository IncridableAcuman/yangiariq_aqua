import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  AnomalyFilterParams,
  UpdateAnomalyRulePayload,
  acknowledgeAnomaly,
  dismissAnomaly,
  getAnomaly,
  getAnomalyRule,
  getAnomalySummary,
  getFieldAnomalySummary,
  investigateAnomaly,
  listAnomalies,
  listAnomalyRules,
  listFieldAnomalies,
  listSensorAnomalies,
  resolveAnomaly,
  toggleAnomalyRule,
  updateAnomalyRule,
} from '@/entities/anomaly'

export const anomalyQueryKeys = {
  all: ['anomalies'] as const,
  list: (params?: AnomalyFilterParams) => [...anomalyQueryKeys.all, 'list', params] as const,
  detail: (id: string) => [...anomalyQueryKeys.all, 'detail', id] as const,
  summary: () => [...anomalyQueryKeys.all, 'summary'] as const,
  fieldList: (fieldId: string, page: number, size: number) =>
    [...anomalyQueryKeys.all, 'field', fieldId, { page, size }] as const,
  fieldSummary: (fieldId: string) => [...anomalyQueryKeys.all, 'field-summary', fieldId] as const,
  sensorList: (sensorId: string, page: number, size: number) =>
    [...anomalyQueryKeys.all, 'sensor', sensorId, { page, size }] as const,
  rules: () => ['anomaly-rules'] as const,
  ruleDetail: (id: string) => ['anomaly-rules', id] as const,
}

export function useAnomalies(params?: AnomalyFilterParams) {
  return useQuery({
    queryKey: anomalyQueryKeys.list(params),
    queryFn: () => listAnomalies(params),
    refetchInterval: 30000,
  })
}

export function useAnomaly(id: string) {
  return useQuery({
    queryKey: anomalyQueryKeys.detail(id),
    queryFn: () => getAnomaly(id),
    enabled: Boolean(id),
  })
}

export function useAnomalySummary() {
  return useQuery({
    queryKey: anomalyQueryKeys.summary(),
    queryFn: getAnomalySummary,
    refetchInterval: 30000,
  })
}

export function useFieldAnomalies(fieldId: string, page = 0, size = 20) {
  return useQuery({
    queryKey: anomalyQueryKeys.fieldList(fieldId, page, size),
    queryFn: () => listFieldAnomalies(fieldId, page, size),
    enabled: Boolean(fieldId),
  })
}

export function useFieldAnomalySummary(fieldId: string) {
  return useQuery({
    queryKey: anomalyQueryKeys.fieldSummary(fieldId),
    queryFn: () => getFieldAnomalySummary(fieldId),
    enabled: Boolean(fieldId),
    refetchInterval: 30000,
  })
}

export function useSensorAnomalies(sensorId: string, page = 0, size = 20) {
  return useQuery({
    queryKey: anomalyQueryKeys.sensorList(sensorId, page, size),
    queryFn: () => listSensorAnomalies(sensorId, page, size),
    enabled: Boolean(sensorId),
  })
}

export function useAnomalyRules() {
  return useQuery({
    queryKey: anomalyQueryKeys.rules(),
    queryFn: listAnomalyRules,
  })
}

export function useAnomalyRule(id: string) {
  return useQuery({
    queryKey: anomalyQueryKeys.ruleDetail(id),
    queryFn: () => getAnomalyRule(id),
    enabled: Boolean(id),
  })
}

export function useAcknowledgeAnomalyMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, notes }: { id: string; notes?: string }) => acknowledgeAnomaly(id, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: anomalyQueryKeys.all })
    },
  })
}

export function useInvestigateAnomalyMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, notes }: { id: string; notes?: string }) => investigateAnomaly(id, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: anomalyQueryKeys.all })
    },
  })
}

export function useResolveAnomalyMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, notes }: { id: string; notes: string }) => resolveAnomaly(id, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: anomalyQueryKeys.all })
    },
  })
}

export function useDismissAnomalyMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, notes }: { id: string; notes: string }) => dismissAnomaly(id, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: anomalyQueryKeys.all })
    },
  })
}

export function useUpdateAnomalyRuleMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateAnomalyRulePayload }) =>
      updateAnomalyRule(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: anomalyQueryKeys.rules() })
    },
  })
}

export function useToggleAnomalyRuleMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) =>
      toggleAnomalyRule(id, enabled),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: anomalyQueryKeys.rules() })
    },
  })
}
