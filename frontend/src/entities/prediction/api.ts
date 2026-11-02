import { apiClient } from '@/shared/api/client'
import {
  ActivateVersionRequest,
  ApproveProposalRequest,
  BacktestRun,
  CalibrationProposal,
  CalibrationProposalDetailResponse,
  EvaluateBatchResponse,
  EvaluationHistoryResponse,
  FieldEvaluationResponse,
  FieldPredictions,
  ForecastHistoryResponse,
  GovernanceAuditLog,
  GovernanceOverview,
  IrrigationNeedRanking,
  PredictionEvaluationDetail,
  PredictionEvaluationOverview,
  PredictionItem,
  PredictionMethodVersion,
  PredictionOverview,
  PredictionType,
  PredictionTypePerformance,
  RejectProposalRequest,
  RollbackVersionRequest,
  WaterDemandForecast,
} from './types'

export const getPredictionOverview = async (
  horizon = 'NEXT_48_HOURS',
  signal?: AbortSignal
): Promise<PredictionOverview> => {
  const response = await apiClient<PredictionOverview>(
    `/predictions/overview?horizon=${encodeURIComponent(horizon)}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Prediction overview not returned by server')
  return response.data
}

export const getFieldPredictions = async (
  fieldId: string,
  horizon = 'NEXT_48_HOURS',
  signal?: AbortSignal
): Promise<FieldPredictions> => {
  const response = await apiClient<FieldPredictions>(
    `/predictions/fields/${encodeURIComponent(fieldId)}?horizon=${encodeURIComponent(horizon)}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Field predictions not returned by server')
  return response.data
}

export const getWaterDemandForecast = async (
  horizon = 'NEXT_48_HOURS',
  signal?: AbortSignal
): Promise<WaterDemandForecast> => {
  const response = await apiClient<WaterDemandForecast>(
    `/predictions/water-demand?horizon=${encodeURIComponent(horizon)}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Water demand forecast not returned by server')
  return response.data
}

export const getIrrigationNeedRanking = async (
  horizon = 'NEXT_48_HOURS',
  signal?: AbortSignal
): Promise<IrrigationNeedRanking> => {
  const response = await apiClient<IrrigationNeedRanking>(
    `/predictions/irrigation-need?horizon=${encodeURIComponent(horizon)}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Irrigation need ranking not returned by server')
  return response.data
}

export const getEmergingRisks = async (
  horizon = 'NEXT_48_HOURS',
  signal?: AbortSignal
): Promise<PredictionItem[]> => {
  const response = await apiClient<PredictionItem[]>(
    `/predictions/risks?horizon=${encodeURIComponent(horizon)}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Emerging risks not returned by server')
  return response.data
}

export const getForecastHistory = async (
  params?: { from?: string; to?: string; limit?: number },
  signal?: AbortSignal
): Promise<ForecastHistoryResponse> => {
  const searchParams = new URLSearchParams()
  if (params?.from) searchParams.set('from', params.from)
  if (params?.to) searchParams.set('to', params.to)
  if (params?.limit) searchParams.set('limit', String(params.limit))

  const query = searchParams.toString()
  const response = await apiClient<ForecastHistoryResponse>(
    `/predictions/history${query ? `?${query}` : ''}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Forecast history not returned by server')
  return response.data
}

export const getPredictionEvaluationOverview = async (
  params?: { from?: string; to?: string },
  signal?: AbortSignal
): Promise<PredictionEvaluationOverview> => {
  const searchParams = new URLSearchParams()
  if (params?.from) searchParams.set('from', params.from)
  if (params?.to) searchParams.set('to', params.to)
  const query = searchParams.toString()

  const response = await apiClient<PredictionEvaluationOverview>(
    `/predictions/evaluation/overview${query ? `?${query}` : ''}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Prediction evaluation overview not returned by server')
  return response.data
}

export const getPredictionEvaluationByType = async (
  params?: { type?: PredictionType | string; from?: string; to?: string },
  signal?: AbortSignal
): Promise<PredictionTypePerformance[]> => {
  const searchParams = new URLSearchParams()
  if (params?.type) searchParams.set('type', params.type)
  if (params?.from) searchParams.set('from', params.from)
  if (params?.to) searchParams.set('to', params.to)
  const query = searchParams.toString()

  const response = await apiClient<PredictionTypePerformance[]>(
    `/predictions/evaluation/by-type${query ? `?${query}` : ''}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Prediction type performance not returned by server')
  return response.data
}

export const getPredictionEvaluationByField = async (
  params?: { from?: string; to?: string; page?: number; size?: number },
  signal?: AbortSignal
): Promise<FieldEvaluationResponse> => {
  const searchParams = new URLSearchParams()
  if (params?.from) searchParams.set('from', params.from)
  if (params?.to) searchParams.set('to', params.to)
  if (params?.page !== undefined) searchParams.set('page', String(params.page))
  if (params?.size !== undefined) searchParams.set('size', String(params.size))
  const query = searchParams.toString()

  const response = await apiClient<FieldEvaluationResponse>(
    `/predictions/evaluation/by-field${query ? `?${query}` : ''}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Field evaluations not returned by server')
  return response.data
}

export const getPredictionEvaluationHistory = async (
  params?: {
    fieldId?: string
    type?: string
    status?: string
    from?: string
    to?: string
    page?: number
    size?: number
  },
  signal?: AbortSignal
): Promise<EvaluationHistoryResponse> => {
  const searchParams = new URLSearchParams()
  if (params?.fieldId) searchParams.set('fieldId', params.fieldId)
  if (params?.type) searchParams.set('type', params.type)
  if (params?.status) searchParams.set('status', params.status)
  if (params?.from) searchParams.set('from', params.from)
  if (params?.to) searchParams.set('to', params.to)
  if (params?.page !== undefined) searchParams.set('page', String(params.page))
  if (params?.size !== undefined) searchParams.set('size', String(params.size))
  const query = searchParams.toString()

  const response = await apiClient<EvaluationHistoryResponse>(
    `/predictions/evaluation/history${query ? `?${query}` : ''}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Evaluation history not returned by server')
  return response.data
}

export const getPredictionDetailEvaluation = async (
  predictionId: string,
  signal?: AbortSignal
): Promise<PredictionEvaluationDetail> => {
  const response = await apiClient<PredictionEvaluationDetail>(
    `/predictions/${encodeURIComponent(predictionId)}/evaluation`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Prediction detail evaluation not returned by server')
  return response.data
}

export const triggerEvaluatePendingPredictions = async (
  signal?: AbortSignal
): Promise<EvaluateBatchResponse> => {
  const response = await apiClient<EvaluateBatchResponse>(
    '/predictions/evaluation/evaluate',
    {
      method: 'POST',
      signal,
    }
  )
  if (!response.data) throw new Error('Trigger evaluation failed')
  return response.data
}

// ============================================================================
// MILESTONE 08.9: PREDICTION GOVERNANCE & CONTROLLED CALIBRATION API CLIENT
// ============================================================================

export const getGovernanceOverview = async (signal?: AbortSignal): Promise<GovernanceOverview> => {
  const response = await apiClient<GovernanceOverview>('/predictions/governance/overview', {
    method: 'GET',
    signal,
  })
  if (!response.data) throw new Error('Governance overview not returned by server')
  return response.data
}

export const getPredictionMethodVersions = async (
  type?: string,
  signal?: AbortSignal
): Promise<PredictionMethodVersion[]> => {
  const query = type ? `?type=${encodeURIComponent(type)}` : ''
  const response = await apiClient<PredictionMethodVersion[]>(
    `/predictions/governance/versions${query}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Prediction method versions not returned by server')
  return response.data
}

export const getCalibrationProposals = async (
  params?: { type?: string; openOnly?: boolean },
  signal?: AbortSignal
): Promise<CalibrationProposal[]> => {
  const searchParams = new URLSearchParams()
  if (params?.type) searchParams.set('type', params.type)
  if (params?.openOnly !== undefined) searchParams.set('openOnly', String(params.openOnly))
  const query = searchParams.toString()

  const response = await apiClient<CalibrationProposal[]>(
    `/predictions/governance/proposals${query ? `?${query}` : ''}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Calibration proposals not returned by server')
  return response.data
}

export const getCalibrationProposalDetail = async (
  proposalId: string,
  signal?: AbortSignal
): Promise<CalibrationProposalDetailResponse> => {
  const response = await apiClient<CalibrationProposalDetailResponse>(
    `/predictions/governance/proposals/${encodeURIComponent(proposalId)}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Calibration proposal detail not returned by server')
  return response.data
}

export const runBacktest = async (
  proposalId: string,
  signal?: AbortSignal
): Promise<BacktestRun> => {
  const response = await apiClient<BacktestRun>(
    `/predictions/governance/proposals/${encodeURIComponent(proposalId)}/backtest`,
    {
      method: 'POST',
      signal,
    }
  )
  if (!response.data) throw new Error('Backtest run failed to execute')
  return response.data
}

export const approveCalibrationProposal = async (
  proposalId: string,
  request?: ApproveProposalRequest,
  signal?: AbortSignal
): Promise<CalibrationProposal> => {
  const response = await apiClient<CalibrationProposal>(
    `/predictions/governance/proposals/${encodeURIComponent(proposalId)}/approve`,
    {
      method: 'POST',
      body: JSON.stringify(request || {}),
      signal,
    }
  )
  if (!response.data) throw new Error('Failed to approve calibration proposal')
  return response.data
}

export const rejectCalibrationProposal = async (
  proposalId: string,
  request: RejectProposalRequest,
  signal?: AbortSignal
): Promise<CalibrationProposal> => {
  const response = await apiClient<CalibrationProposal>(
    `/predictions/governance/proposals/${encodeURIComponent(proposalId)}/reject`,
    {
      method: 'POST',
      body: JSON.stringify(request),
      signal,
    }
  )
  if (!response.data) throw new Error('Failed to reject calibration proposal')
  return response.data
}

export const activateVersion = async (
  versionId: string,
  request: ActivateVersionRequest,
  signal?: AbortSignal
): Promise<PredictionMethodVersion> => {
  const response = await apiClient<PredictionMethodVersion>(
    `/predictions/governance/versions/${encodeURIComponent(versionId)}/activate`,
    {
      method: 'POST',
      body: JSON.stringify(request),
      signal,
    }
  )
  if (!response.data) throw new Error('Failed to activate version')
  return response.data
}

export const rollbackVersion = async (
  type: string,
  request: RollbackVersionRequest,
  signal?: AbortSignal
): Promise<PredictionMethodVersion> => {
  const response = await apiClient<PredictionMethodVersion>(
    `/predictions/governance/versions/rollback?type=${encodeURIComponent(type)}`,
    {
      method: 'POST',
      body: JSON.stringify(request),
      signal,
    }
  )
  if (!response.data) throw new Error('Failed to rollback version')
  return response.data
}

export const getGovernanceAuditTrail = async (
  limit = 50,
  signal?: AbortSignal
): Promise<GovernanceAuditLog[]> => {
  const response = await apiClient<GovernanceAuditLog[]>(
    `/predictions/governance/audit-trail?limit=${limit}`,
    {
      method: 'GET',
      signal,
    }
  )
  if (!response.data) throw new Error('Audit trail not returned by server')
  return response.data
}

export const generateCalibrationProposal = async (
  type: string,
  signal?: AbortSignal
): Promise<CalibrationProposal | null> => {
  const response = await apiClient<CalibrationProposal | null>(
    `/predictions/governance/proposals/generate?type=${encodeURIComponent(type)}`,
    {
      method: 'POST',
      signal,
    }
  )
  return response.data || null
}

