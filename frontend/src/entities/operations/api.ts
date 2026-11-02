import { apiClient } from '@/shared/api/client'
import {
  OperationalContextResponse,
  PreparePlanPayload,
  ApprovePlanPayload,
  RejectPlanPayload,
  SchedulePlanPayload,
  StartOperationPayload,
  CompleteOperationPayload,
  OperationalVerificationResponse,
} from './types'
import { SmartIrrigationPlan } from '@/entities/irrigation/types'

export const operationsApi = {
  fetchContext: async (fieldId: string, signal?: AbortSignal): Promise<OperationalContextResponse> => {
    const response = await apiClient<OperationalContextResponse>(
      `/operations/${fieldId}/context`,
      { method: 'GET', signal }
    )
    if (!response.data) throw new Error('Operational context not returned by server')
    return response.data
  },

  preparePlan: async (fieldId: string, payload: PreparePlanPayload): Promise<SmartIrrigationPlan> => {
    const response = await apiClient<SmartIrrigationPlan>(
      `/operations/${fieldId}/prepare-plan`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      }
    )
    if (!response.data) throw new Error('Smart plan could not be prepared')
    return response.data
  },

  approvePlan: async (planId: string, payload: ApprovePlanPayload): Promise<SmartIrrigationPlan> => {
    const response = await apiClient<SmartIrrigationPlan>(
      `/operations/plans/${planId}/approve`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      }
    )
    if (!response.data) throw new Error('Smart plan could not be approved')
    return response.data
  },

  rejectPlan: async (planId: string, payload: RejectPlanPayload): Promise<SmartIrrigationPlan> => {
    const response = await apiClient<SmartIrrigationPlan>(
      `/operations/plans/${planId}/reject`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      }
    )
    if (!response.data) throw new Error('Smart plan could not be rejected')
    return response.data
  },

  schedulePlan: async (planId: string, payload: SchedulePlanPayload): Promise<SmartIrrigationPlan> => {
    const response = await apiClient<SmartIrrigationPlan>(
      `/operations/plans/${planId}/schedule`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      }
    )
    if (!response.data) throw new Error('Smart plan could not be scheduled')
    return response.data
  },

  startOperation: async (planId: string, payload: StartOperationPayload): Promise<SmartIrrigationPlan> => {
    const response = await apiClient<SmartIrrigationPlan>(
      `/operations/plans/${planId}/start`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      }
    )
    if (!response.data) throw new Error('Operation could not be started')
    return response.data
  },

  completeOperation: async (planId: string, payload: CompleteOperationPayload): Promise<SmartIrrigationPlan> => {
    const response = await apiClient<SmartIrrigationPlan>(
      `/operations/plans/${planId}/complete`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      }
    )
    if (!response.data) throw new Error('Operation could not be completed')
    return response.data
  },

  fetchVerification: async (planId: string, signal?: AbortSignal): Promise<OperationalVerificationResponse> => {
    const response = await apiClient<OperationalVerificationResponse>(
      `/operations/plans/${planId}/verification`,
      { method: 'GET', signal }
    )
    if (!response.data) throw new Error('Verification data not returned by server')
    return response.data
  },
}
