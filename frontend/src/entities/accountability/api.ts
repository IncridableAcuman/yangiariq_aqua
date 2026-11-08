import { apiClient } from '@/shared/api/client'
import {
  AccountabilityDashboardSummary,
  ApproveActionPayload,
  AssignInvestigatorPayload,
  AttachEvidencePayload,
  CompleteActionPayload,
  CorrectiveAction,
  CreateIncidentPayload,
  IncidentDetails,
  IncidentEvidence,
  IncidentVerification,
  ProposeActionPayload,
  RecordInvestigationPayload,
  ReopenIncidentPayload,
  ReviewIncidentPayload,
  VerifyResolutionPayload,
  WaterIncident,
} from './types'

export const accountabilityApi = {
  getDashboard: async (signal?: AbortSignal): Promise<AccountabilityDashboardSummary> => {
    const res = await apiClient<AccountabilityDashboardSummary>('/accountability/dashboard', {
      method: 'GET',
      signal,
    })
    if (!res.data) throw new Error('Failed to load accountability dashboard')
    return res.data
  },

  listIncidents: async (
    params?: {
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
    },
    signal?: AbortSignal
  ): Promise<WaterIncident[]> => {
    const searchParams = new URLSearchParams()
    if (params?.category) searchParams.set('category', params.category)
    if (params?.severity) searchParams.set('severity', params.severity)
    if (params?.status) searchParams.set('status', params.status)
    if (params?.wuaId) searchParams.set('wuaId', params.wuaId)
    if (params?.canalId) searchParams.set('canalId', params.canalId)
    if (params?.fieldId) searchParams.set('fieldId', params.fieldId)
    if (params?.overdueOnly !== undefined) searchParams.set('overdueOnly', String(params.overdueOnly))
    if (params?.startDate) searchParams.set('startDate', params.startDate)
    if (params?.endDate) searchParams.set('endDate', params.endDate)
    if (params?.page !== undefined) searchParams.set('page', String(params.page))
    if (params?.size !== undefined) searchParams.set('size', String(params.size))

    const query = searchParams.toString() ? `?${searchParams.toString()}` : ''
    const res = await apiClient<WaterIncident[]>(`/accountability/incidents${query}`, {
      method: 'GET',
      signal,
    })
    return res.data || []
  },

  getIncident: async (id: string, signal?: AbortSignal): Promise<IncidentDetails> => {
    const res = await apiClient<IncidentDetails>(`/accountability/incidents/${id}`, {
      method: 'GET',
      signal,
    })
    if (!res.data) throw new Error(`Failed to load incident details for ${id}`)
    return res.data
  },

  createIncident: async (payload: CreateIncidentPayload): Promise<WaterIncident> => {
    const res = await apiClient<WaterIncident>('/accountability/incidents', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to create incident')
    return res.data
  },

  reviewIncident: async (id: string, payload: ReviewIncidentPayload): Promise<WaterIncident> => {
    const res = await apiClient<WaterIncident>(`/accountability/incidents/${id}/review`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to review incident')
    return res.data
  },

  assignInvestigator: async (id: string, payload: AssignInvestigatorPayload): Promise<WaterIncident> => {
    const res = await apiClient<WaterIncident>(`/accountability/incidents/${id}/assign`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to assign investigator')
    return res.data
  },

  recordInvestigation: async (id: string, payload: RecordInvestigationPayload): Promise<WaterIncident> => {
    const res = await apiClient<WaterIncident>(`/accountability/incidents/${id}/investigations`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to record investigation')
    return res.data
  },

  attachEvidence: async (id: string, payload: AttachEvidencePayload): Promise<IncidentEvidence> => {
    const res = await apiClient<IncidentEvidence>(`/accountability/incidents/${id}/evidence`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to attach evidence')
    return res.data
  },

  proposeAction: async (id: string, payload: ProposeActionPayload): Promise<CorrectiveAction> => {
    const res = await apiClient<CorrectiveAction>(`/accountability/incidents/${id}/actions`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to propose corrective action')
    return res.data
  },

  approveAction: async (id: string, payload: ApproveActionPayload): Promise<CorrectiveAction> => {
    const res = await apiClient<CorrectiveAction>(`/accountability/incidents/${id}/approve-action`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to approve corrective action')
    return res.data
  },

  completeAction: async (id: string, payload: CompleteActionPayload): Promise<CorrectiveAction> => {
    const res = await apiClient<CorrectiveAction>(`/accountability/incidents/${id}/complete-action`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to complete corrective action')
    return res.data
  },

  verifyResolution: async (id: string, payload: VerifyResolutionPayload): Promise<IncidentVerification> => {
    const res = await apiClient<IncidentVerification>(`/accountability/incidents/${id}/verify`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to verify resolution')
    return res.data
  },

  reopenIncident: async (id: string, payload: ReopenIncidentPayload): Promise<WaterIncident> => {
    const res = await apiClient<WaterIncident>(`/accountability/incidents/${id}/reopen`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
    if (!res.data) throw new Error('Failed to reopen incident')
    return res.data
  },

  getOverdue: async (signal?: AbortSignal): Promise<WaterIncident[]> => {
    const res = await apiClient<WaterIncident[]>('/accountability/sla/overdue', {
      method: 'GET',
      signal,
    })
    return res.data || []
  },

  ingestFindings: async (): Promise<WaterIncident[]> => {
    const res = await apiClient<WaterIncident[]>('/accountability/ingest-findings', {
      method: 'POST',
    })
    return res.data || []
  },
}
