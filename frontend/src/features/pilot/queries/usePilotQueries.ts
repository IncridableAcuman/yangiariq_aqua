import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '@/shared/api/client'
import {
  PilotProgram,
  PilotStatus,
  PilotParticipant,
  PilotField,
  PilotReadinessAssessment,
  GroundTruthMeasurement,
  GroundTruthStatus,
  PredictionValidationRecord,
  PredictionEvaluationStatus,
  OperatorFeedback,
  OperatorFeedbackCategory,
  OperatorFeedbackStatus,
  PilotEvaluationReport,
  PilotOperationalMonitoring,
} from '@/entities/pilot'

export function usePilotProgramsQuery(status?: PilotStatus) {
  return useQuery({
    queryKey: ['pilot-programs', status],
    queryFn: async () => {
      const endpoint = status ? `/pilot-programs?status=${status}` : '/pilot-programs'
      const res = await apiClient<PilotProgram[]>(endpoint)
      return res.data || []
    },
  })
}

export function usePilotProgramDetailQuery(id: string | null) {
  return useQuery({
    queryKey: ['pilot-program', id],
    queryFn: async () => {
      if (!id) return null
      const res = await apiClient<PilotProgram>(`/pilot-programs/${id}`)
      return res.data || null
    },
    enabled: Boolean(id),
  })
}

export function usePilotParticipantsQuery(pilotId: string | null) {
  return useQuery({
    queryKey: ['pilot-participants', pilotId],
    queryFn: async () => {
      if (!pilotId) return []
      const res = await apiClient<PilotParticipant[]>(`/pilot-programs/${pilotId}/participants`)
      return res.data || []
    },
    enabled: Boolean(pilotId),
  })
}

export function usePilotFieldsQuery(pilotId: string | null) {
  return useQuery({
    queryKey: ['pilot-fields', pilotId],
    queryFn: async () => {
      if (!pilotId) return []
      const res = await apiClient<PilotField[]>(`/pilot-programs/${pilotId}/fields`)
      return res.data || []
    },
    enabled: Boolean(pilotId),
  })
}

export function usePilotReadinessQuery(pilotId: string | null) {
  return useQuery({
    queryKey: ['pilot-readiness', pilotId],
    queryFn: async () => {
      if (!pilotId) return null
      const res = await apiClient<PilotReadinessAssessment>(`/pilot-readiness/${pilotId}`)
      return res.data || null
    },
    enabled: Boolean(pilotId),
  })
}

export function useGroundTruthMeasurementsQuery(pilotId: string | null, status?: GroundTruthStatus) {
  return useQuery({
    queryKey: ['pilot-ground-truth', pilotId, status],
    queryFn: async () => {
      if (!pilotId) return []
      const endpoint = status
        ? `/pilot-ground-truth/pilot/${pilotId}?status=${status}`
        : `/pilot-ground-truth/pilot/${pilotId}`
      const res = await apiClient<GroundTruthMeasurement[]>(endpoint)
      return res.data || []
    },
    enabled: Boolean(pilotId),
  })
}

export function usePredictionValidationsQuery(pilotId: string | null, status?: PredictionEvaluationStatus) {
  return useQuery({
    queryKey: ['pilot-validations', pilotId, status],
    queryFn: async () => {
      if (!pilotId) return []
      const endpoint = status
        ? `/pilot-validation/pilot/${pilotId}?status=${status}`
        : `/pilot-validation/pilot/${pilotId}`
      const res = await apiClient<PredictionValidationRecord[]>(endpoint)
      return res.data || []
    },
    enabled: Boolean(pilotId),
  })
}

export function useOperatorFeedbackQuery(
  pilotId: string | null,
  status?: OperatorFeedbackStatus,
  category?: OperatorFeedbackCategory
) {
  return useQuery({
    queryKey: ['pilot-feedback', pilotId, status, category],
    queryFn: async () => {
      if (!pilotId) return []
      const params = new URLSearchParams()
      if (status) params.append('status', status)
      if (category) params.append('category', category)
      const qs = params.toString() ? `?${params.toString()}` : ''
      const res = await apiClient<OperatorFeedback[]>(`/pilot-feedback/pilot/${pilotId}${qs}`)
      return res.data || []
    },
    enabled: Boolean(pilotId),
  })
}

export function usePilotMonitoringQuery(pilotId: string | null) {
  return useQuery({
    queryKey: ['pilot-monitoring', pilotId],
    queryFn: async () => {
      if (!pilotId) return null
      const res = await apiClient<PilotOperationalMonitoring>(`/pilot-evaluations/pilot/${pilotId}/monitoring`)
      return res.data || null
    },
    enabled: Boolean(pilotId),
  })
}

export function usePilotEvaluationReportQuery(pilotId: string | null) {
  return useQuery({
    queryKey: ['pilot-evaluation-report', pilotId],
    queryFn: async () => {
      if (!pilotId) return null
      const res = await apiClient<PilotEvaluationReport>(`/pilot-evaluations/pilot/${pilotId}/report`)
      return res.data || null
    },
    enabled: Boolean(pilotId),
  })
}

// Mutations
export function useCreatePilotMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (payload: {
      code: string
      name: string
      description?: string
      geographicScope: string
      startDate: string
      endDate: string
      organization: string
      operationalObjectives: string
    }) => {
      const res = await apiClient<PilotProgram>('/pilot-programs', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pilot-programs'] })
    },
  })
}

export function useUpdatePilotMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({
      id,
      payload,
    }: {
      id: string
      payload: {
        name: string
        description?: string
        geographicScope: string
        startDate: string
        endDate: string
        organization: string
        operationalObjectives: string
      }
    }) => {
      const res = await apiClient<PilotProgram>(`/pilot-programs/${id}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['pilot-programs'] })
      queryClient.invalidateQueries({ queryKey: ['pilot-program', variables.id] })
    },
  })
}

export function useApprovePilotMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, notes }: { id: string; notes: string }) => {
      const res = await apiClient<PilotProgram>(`/pilot-programs/${id}/approve`, {
        method: 'POST',
        body: JSON.stringify({ notes }),
      })
      return res.data
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['pilot-programs'] })
      queryClient.invalidateQueries({ queryKey: ['pilot-program', variables.id] })
      queryClient.invalidateQueries({ queryKey: ['pilot-readiness', variables.id] })
    },
  })
}

export function useActivatePilotMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      const res = await apiClient<PilotProgram>(`/pilot-programs/${id}/activate`, {
        method: 'POST',
      })
      return res.data
    },
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['pilot-programs'] })
      queryClient.invalidateQueries({ queryKey: ['pilot-program', id] })
    },
  })
}

export function useAssessReadinessMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (pilotId: string) => {
      const res = await apiClient<PilotReadinessAssessment>(`/pilot-readiness/${pilotId}/assess`, {
        method: 'POST',
      })
      return res.data
    },
    onSuccess: (_, pilotId) => {
      queryClient.invalidateQueries({ queryKey: ['pilot-readiness', pilotId] })
      queryClient.invalidateQueries({ queryKey: ['pilot-program', pilotId] })
      queryClient.invalidateQueries({ queryKey: ['pilot-monitoring', pilotId] })
    },
  })
}

export function useAddParticipantMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({
      pilotId,
      payload,
    }: {
      pilotId: string
      payload: { userId: string; roleInPilot: string; assignedFieldIds?: string }
    }) => {
      const res = await apiClient<PilotParticipant>(`/pilot-programs/${pilotId}/participants`, {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['pilot-participants', variables.pilotId] })
      queryClient.invalidateQueries({ queryKey: ['pilot-monitoring', variables.pilotId] })
    },
  })
}

export function useAddFieldMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({
      pilotId,
      payload,
    }: {
      pilotId: string
      payload: { fieldId: string; monitoringTarget: string; measurementPlan: string; validationCriteria: string }
    }) => {
      const res = await apiClient<PilotField>(`/pilot-programs/${pilotId}/fields`, {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['pilot-fields', variables.pilotId] })
      queryClient.invalidateQueries({ queryKey: ['pilot-monitoring', variables.pilotId] })
    },
  })
}

export function useSubmitGroundTruthMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({
      pilotId,
      payload,
    }: {
      pilotId: string
      payload: {
        fieldId: string
        measurementType: string
        measuredValue: number
        canonicalUnit: string
        measuredAt?: string
        collectionMethod: string
        measurementSource: string
        evidenceReference?: string
        notes?: string
      }
    }) => {
      const res = await apiClient<GroundTruthMeasurement>(`/pilot-ground-truth/pilot/${pilotId}`, {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['pilot-ground-truth', variables.pilotId] })
      queryClient.invalidateQueries({ queryKey: ['pilot-monitoring', variables.pilotId] })
    },
  })
}

export function useReviewGroundTruthMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({
      measurementId,
      payload,
    }: {
      pilotId: string
      measurementId: string
      payload: {
        action: 'VERIFY' | 'REJECT' | 'REQUEST_REMEASUREMENT'
        evidenceReference?: string
        notes?: string
        rejectionReason?: string
      }
    }) => {
      const res = await apiClient<GroundTruthMeasurement>(`/pilot-ground-truth/${measurementId}/review`, {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['pilot-ground-truth', variables.pilotId] })
      queryClient.invalidateQueries({ queryKey: ['pilot-monitoring', variables.pilotId] })
    },
  })
}

export function useEvaluatePredictionMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({
      pilotId,
      payload,
    }: {
      pilotId: string
      payload: {
        fieldId: string
        predictionId?: string
        modelName: string
        modelVersion: string
        predictedAt: string
        predictionHorizonHours: number
        predictedValue: number
        actualValue: number
        groundTruthId?: string
      }
    }) => {
      const res = await apiClient<PredictionValidationRecord>(`/pilot-validation/pilot/${pilotId}`, {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['pilot-validations', variables.pilotId] })
      queryClient.invalidateQueries({ queryKey: ['pilot-monitoring', variables.pilotId] })
    },
  })
}

export function useSubmitFeedbackMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({
      pilotId,
      payload,
    }: {
      pilotId: string
      payload: {
        farmId?: string
        fieldId?: string
        sensorId?: string
        dataSourceId?: string
        incidentId?: string
        category: string
        title: string
        description: string
      }
    }) => {
      const res = await apiClient<OperatorFeedback>(`/pilot-feedback/pilot/${pilotId}`, {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['pilot-feedback', variables.pilotId] })
      queryClient.invalidateQueries({ queryKey: ['pilot-monitoring', variables.pilotId] })
    },
  })
}

export function useGenerateEvaluationReportMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({
      pilotId,
      payload,
    }: {
      pilotId: string
      payload: {
        periodStart?: string
        periodEnd?: string
        limitationsText?: string
        recommendationsText?: string
      }
    }) => {
      const res = await apiClient<PilotEvaluationReport>(`/pilot-evaluations/pilot/${pilotId}/report`, {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['pilot-evaluation-report', variables.pilotId] })
    },
  })
}
