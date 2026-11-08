import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  accountabilityApi,
  ApproveActionPayload,
  AssignInvestigatorPayload,
  AttachEvidencePayload,
  CompleteActionPayload,
  CreateIncidentPayload,
  ProposeActionPayload,
  RecordInvestigationPayload,
  ReopenIncidentPayload,
  ReviewIncidentPayload,
  VerifyResolutionPayload,
} from '@/entities/accountability'

export const accountabilityKeys = {
  all: ['accountability'] as const,
  dashboard: () => [...accountabilityKeys.all, 'dashboard'] as const,
  incidents: (params?: Record<string, unknown>) => [...accountabilityKeys.all, 'incidents', params] as const,
  incidentDetail: (id?: string) => [...accountabilityKeys.all, 'incident', id] as const,
  overdue: () => [...accountabilityKeys.all, 'overdue'] as const,
}

export function useAccountabilityDashboard() {
  return useQuery({
    queryKey: accountabilityKeys.dashboard(),
    queryFn: ({ signal }) => accountabilityApi.getDashboard(signal),
  })
}

export function useIncidentsList(params?: {
  category?: string
  severity?: string
  status?: string
  wuaId?: string
  canalId?: string
  fieldId?: string
  overdueOnly?: boolean
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}) {
  return useQuery({
    queryKey: accountabilityKeys.incidents(params),
    queryFn: ({ signal }) => accountabilityApi.listIncidents(params, signal),
  })
}

export function useIncidentDetail(id?: string) {
  return useQuery({
    queryKey: accountabilityKeys.incidentDetail(id),
    queryFn: ({ signal }) => {
      if (!id) throw new Error('Incident ID required')
      return accountabilityApi.getIncident(id, signal)
    },
    enabled: Boolean(id),
  })
}

export function useOverdueIncidents() {
  return useQuery({
    queryKey: accountabilityKeys.overdue(),
    queryFn: ({ signal }) => accountabilityApi.getOverdue(signal),
  })
}

export function useCreateIncidentMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateIncidentPayload) => accountabilityApi.createIncident(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.all })
    },
  })
}

export function useReviewIncidentMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: ReviewIncidentPayload }) =>
      accountabilityApi.reviewIncident(id, payload),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.incidentDetail(vars.id) })
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.all })
    },
  })
}

export function useAssignInvestigatorMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: AssignInvestigatorPayload }) =>
      accountabilityApi.assignInvestigator(id, payload),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.incidentDetail(vars.id) })
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.all })
    },
  })
}

export function useRecordInvestigationMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: RecordInvestigationPayload }) =>
      accountabilityApi.recordInvestigation(id, payload),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.incidentDetail(vars.id) })
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.all })
    },
  })
}

export function useAttachEvidenceMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: AttachEvidencePayload }) =>
      accountabilityApi.attachEvidence(id, payload),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.incidentDetail(vars.id) })
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.all })
    },
  })
}

export function useProposeActionMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: ProposeActionPayload }) =>
      accountabilityApi.proposeAction(id, payload),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.incidentDetail(vars.id) })
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.all })
    },
  })
}

export function useApproveActionMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: ApproveActionPayload }) =>
      accountabilityApi.approveAction(id, payload),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.incidentDetail(vars.id) })
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.all })
    },
  })
}

export function useCompleteActionMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: CompleteActionPayload }) =>
      accountabilityApi.completeAction(id, payload),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.incidentDetail(vars.id) })
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.all })
    },
  })
}

export function useVerifyResolutionMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: VerifyResolutionPayload }) =>
      accountabilityApi.verifyResolution(id, payload),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.incidentDetail(vars.id) })
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.all })
    },
  })
}

export function useReopenIncidentMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: ReopenIncidentPayload }) =>
      accountabilityApi.reopenIncident(id, payload),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.incidentDetail(vars.id) })
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.all })
    },
  })
}

export function useIngestFindingsMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => accountabilityApi.ingestFindings(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: accountabilityKeys.all })
    },
  })
}
