import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  evaluateFieldIntelligence,
  getFieldAgronomicAssessment,
  getFieldTelemetrySignals,
} from '@/entities/intelligence'

export const intelligenceKeys = {
  all: ['intelligence'] as const,
  assessment: (fieldId: string) => [...intelligenceKeys.all, 'assessment', fieldId] as const,
  signals: (fieldId: string) => [...intelligenceKeys.all, 'signals', fieldId] as const,
}

export function useFieldAgronomicAssessment(fieldId?: string) {
  return useQuery({
    queryKey: intelligenceKeys.assessment(fieldId || ''),
    queryFn: () => getFieldAgronomicAssessment(fieldId!),
    enabled: !!fieldId,
    staleTime: 60 * 1000,
  })
}

export function useFieldTelemetrySignals(fieldId?: string) {
  return useQuery({
    queryKey: intelligenceKeys.signals(fieldId || ''),
    queryFn: () => getFieldTelemetrySignals(fieldId!),
    enabled: !!fieldId,
    staleTime: 60 * 1000,
  })
}

export function useEvaluateFieldIntelligence() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (fieldId: string) => evaluateFieldIntelligence(fieldId),
    onSuccess: (data, fieldId) => {
      queryClient.setQueryData(intelligenceKeys.assessment(fieldId), data.assessment)
      queryClient.invalidateQueries({ queryKey: intelligenceKeys.signals(fieldId) })
      queryClient.invalidateQueries({ queryKey: ['recommendations'] })
    },
  })
}
