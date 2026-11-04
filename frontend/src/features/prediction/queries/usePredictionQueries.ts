import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  getPredictionOverview,
  getFieldPredictions,
  getWaterDemandForecast,
  getIrrigationNeedRanking,
  getEmergingRisks,
  getForecastHistory,
  getPredictionEvaluationOverview,
  getPredictionEvaluationByType,
  getPredictionEvaluationByField,
  getPredictionEvaluationHistory,
  getPredictionDetailEvaluation,
  triggerEvaluatePendingPredictions,
  getGovernanceOverview,
  getPredictionMethodVersions,
  getCalibrationProposals,
  getCalibrationProposalDetail,
  runBacktest,
  approveCalibrationProposal,
  rejectCalibrationProposal,
  activateVersion,
  rollbackVersion,
  getGovernanceAuditTrail,
  generateCalibrationProposal,
  createCandidateProposal,
  getAdaptiveOverview,
  getFieldPredictionContext,
  getContextPerformanceProfiles,
  getAdaptiveSelectionHistory,
  selectAdaptiveMethod,
  getPredictionAdaptiveDecision,
  SelectAdaptiveMethodRequest,
  ApproveProposalRequest,
  RejectProposalRequest,
  ActivateVersionRequest,
  RollbackVersionRequest,
  CreateCandidateProposalRequest,
  PredictionHorizon,
  PredictionType,
} from '@/entities/prediction'

export const predictionKeys = {
  all: ['predictions'] as const,
  overview: (horizon: PredictionHorizon) => [...predictionKeys.all, 'overview', horizon] as const,
  field: (fieldId: string, horizon: PredictionHorizon) => [...predictionKeys.all, 'field', fieldId, horizon] as const,
  waterDemand: (horizon: PredictionHorizon) => [...predictionKeys.all, 'waterDemand', horizon] as const,
  irrigationNeed: (horizon: PredictionHorizon) => [...predictionKeys.all, 'irrigationNeed', horizon] as const,
  risks: (horizon: PredictionHorizon) => [...predictionKeys.all, 'risks', horizon] as const,
  history: (params?: { from?: string; to?: string; limit?: number }) => [...predictionKeys.all, 'history', params] as const,
  evaluationOverview: (params?: { from?: string; to?: string }) => [...predictionKeys.all, 'evaluation', 'overview', params] as const,
  evaluationByType: (params?: { type?: string; from?: string; to?: string }) => [...predictionKeys.all, 'evaluation', 'byType', params] as const,
  evaluationByField: (params?: { from?: string; to?: string; page?: number; size?: number }) => [...predictionKeys.all, 'evaluation', 'byField', params] as const,
  evaluationHistory: (params?: { fieldId?: string; type?: string; status?: string; from?: string; to?: string; page?: number; size?: number }) => [...predictionKeys.all, 'evaluation', 'history', params] as const,
  detailEvaluation: (predictionId?: string | null) => [...predictionKeys.all, 'evaluation', 'detail', predictionId] as const,
  governanceOverview: () => [...predictionKeys.all, 'governance', 'overview'] as const,
  governanceVersions: (type?: string) => [...predictionKeys.all, 'governance', 'versions', type] as const,
  governanceProposals: (params?: { type?: string; openOnly?: boolean }) => [...predictionKeys.all, 'governance', 'proposals', params] as const,
  governanceProposalDetail: (id?: string | null) => [...predictionKeys.all, 'governance', 'proposalDetail', id] as const,
  governanceAuditTrail: (limit?: number) => [...predictionKeys.all, 'governance', 'auditTrail', limit] as const,
  adaptiveOverview: () => [...predictionKeys.all, 'adaptive', 'overview'] as const,
  adaptiveContext: (fieldId?: string | null) => [...predictionKeys.all, 'adaptive', 'context', fieldId] as const,
  adaptivePerformance: (params?: { predictionType?: string; versionTag?: string }) => [...predictionKeys.all, 'adaptive', 'performance', params] as const,
  adaptiveSelections: (params?: { fieldId?: string; limit?: number }) => [...predictionKeys.all, 'adaptive', 'selections', params] as const,
  predictionAdaptiveDecision: (predictionId?: string | null) => [...predictionKeys.all, 'adaptive', 'decision', predictionId] as const,
  modelCandidates: (type?: string) => [...predictionKeys.all, 'models', 'candidates', type] as const,
  modelCandidateDetail: (id?: string | null) => [...predictionKeys.all, 'models', 'candidateDetail', id] as const,
  trainingDatasets: (type?: string) => [...predictionKeys.all, 'models', 'datasets', type] as const,
  trainingDatasetDetail: (id?: string | null) => [...predictionKeys.all, 'models', 'datasetDetail', id] as const,
  activeModelComparison: (type?: string) => [...predictionKeys.all, 'models', 'comparison', type] as const,
}

export function usePredictionOverviewQuery(horizon: PredictionHorizon = 'NEXT_48_HOURS') {
  return useQuery({
    queryKey: predictionKeys.overview(horizon),
    queryFn: ({ signal }) => getPredictionOverview(horizon, signal),
    staleTime: 60_000,
  })
}

export function useFieldPredictionsQuery(fieldId?: string | null, horizon: PredictionHorizon = 'NEXT_48_HOURS') {
  return useQuery({
    queryKey: predictionKeys.field(fieldId ?? '', horizon),
    queryFn: ({ signal }) => (fieldId ? getFieldPredictions(fieldId, horizon, signal) : Promise.reject('No fieldId')),
    enabled: !!fieldId,
    staleTime: 60_000,
  })
}

export function useWaterDemandForecastQuery(horizon: PredictionHorizon = 'NEXT_48_HOURS') {
  return useQuery({
    queryKey: predictionKeys.waterDemand(horizon),
    queryFn: ({ signal }) => getWaterDemandForecast(horizon, signal),
    staleTime: 60_000,
  })
}

export function useIrrigationNeedRankingQuery(horizon: PredictionHorizon = 'NEXT_48_HOURS') {
  return useQuery({
    queryKey: predictionKeys.irrigationNeed(horizon),
    queryFn: ({ signal }) => getIrrigationNeedRanking(horizon, signal),
    staleTime: 60_000,
  })
}

export function useEmergingRisksQuery(horizon: PredictionHorizon = 'NEXT_48_HOURS') {
  return useQuery({
    queryKey: predictionKeys.risks(horizon),
    queryFn: ({ signal }) => getEmergingRisks(horizon, signal),
    staleTime: 60_000,
  })
}

export function useForecastHistoryQuery(params?: { from?: string; to?: string; limit?: number }) {
  return useQuery({
    queryKey: predictionKeys.history(params),
    queryFn: ({ signal }) => getForecastHistory(params, signal),
    staleTime: 120_000,
  })
}

// Milestone 08.8: Evaluation & Model Health Hooks

export function usePredictionEvaluationOverviewQuery(params?: { from?: string; to?: string }) {
  return useQuery({
    queryKey: predictionKeys.evaluationOverview(params),
    queryFn: ({ signal }) => getPredictionEvaluationOverview(params, signal),
    staleTime: 60_000,
  })
}

export function usePredictionEvaluationByTypeQuery(params?: { type?: PredictionType | string; from?: string; to?: string }) {
  return useQuery({
    queryKey: predictionKeys.evaluationByType(params),
    queryFn: ({ signal }) => getPredictionEvaluationByType(params, signal),
    staleTime: 60_000,
  })
}

export function usePredictionEvaluationByFieldQuery(params?: { from?: string; to?: string; page?: number; size?: number }) {
  return useQuery({
    queryKey: predictionKeys.evaluationByField(params),
    queryFn: ({ signal }) => getPredictionEvaluationByField(params, signal),
    staleTime: 60_000,
  })
}

export function usePredictionEvaluationHistoryQuery(params?: {
  fieldId?: string
  type?: string
  status?: string
  from?: string
  to?: string
  page?: number
  size?: number
}) {
  return useQuery({
    queryKey: predictionKeys.evaluationHistory(params),
    queryFn: ({ signal }) => getPredictionEvaluationHistory(params, signal),
    staleTime: 30_000,
  })
}

export function usePredictionDetailEvaluationQuery(predictionId?: string | null) {
  return useQuery({
    queryKey: predictionKeys.detailEvaluation(predictionId),
    queryFn: ({ signal }) => (predictionId ? getPredictionDetailEvaluation(predictionId, signal) : Promise.reject('No predictionId')),
    enabled: !!predictionId,
    staleTime: 60_000,
  })
}

export function useTriggerEvaluationMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => triggerEvaluatePendingPredictions(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: predictionKeys.all })
    },
  })
}

// ============================================================================
// MILESTONE 08.9: GOVERNANCE & CONTROLLED CALIBRATION HOOKS
// ============================================================================

export function useGovernanceOverviewQuery() {
  return useQuery({
    queryKey: predictionKeys.governanceOverview(),
    queryFn: ({ signal }) => getGovernanceOverview(signal),
    staleTime: 30_000,
  })
}

export function usePredictionMethodVersionsQuery(type?: string) {
  return useQuery({
    queryKey: predictionKeys.governanceVersions(type),
    queryFn: ({ signal }) => getPredictionMethodVersions(type, signal),
    staleTime: 60_000,
  })
}

export function useCalibrationProposalsQuery(params?: { type?: string; openOnly?: boolean }) {
  return useQuery({
    queryKey: predictionKeys.governanceProposals(params),
    queryFn: ({ signal }) => getCalibrationProposals(params, signal),
    staleTime: 30_000,
  })
}

export function useCalibrationProposalDetailQuery(proposalId?: string | null) {
  return useQuery({
    queryKey: predictionKeys.governanceProposalDetail(proposalId),
    queryFn: ({ signal }) => (proposalId ? getCalibrationProposalDetail(proposalId, signal) : Promise.reject('No proposalId')),
    enabled: !!proposalId,
    staleTime: 30_000,
  })
}

export function useGovernanceAuditTrailQuery(limit = 50) {
  return useQuery({
    queryKey: predictionKeys.governanceAuditTrail(limit),
    queryFn: ({ signal }) => getGovernanceAuditTrail(limit, signal),
    staleTime: 30_000,
  })
}

export function useRunBacktestMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (proposalId: string) => runBacktest(proposalId),
    onSuccess: (_, proposalId) => {
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceProposalDetail(proposalId) })
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceProposals() })
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceOverview() })
    },
  })
}

export function useApproveProposalMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ proposalId, request }: { proposalId: string; request?: ApproveProposalRequest }) =>
      approveCalibrationProposal(proposalId, request),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceProposalDetail(vars.proposalId) })
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceProposals() })
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceOverview() })
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceAuditTrail() })
    },
  })
}

export function useRejectProposalMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ proposalId, request }: { proposalId: string; request: RejectProposalRequest }) =>
      rejectCalibrationProposal(proposalId, request),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceProposalDetail(vars.proposalId) })
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceProposals() })
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceOverview() })
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceAuditTrail() })
    },
  })
}

export function useActivateVersionMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ versionId, request }: { versionId: string; request: ActivateVersionRequest }) =>
      activateVersion(versionId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: predictionKeys.all })
    },
  })
}

export function useRollbackVersionMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ type, request }: { type: string; request: RollbackVersionRequest }) =>
      rollbackVersion(type, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: predictionKeys.all })
    },
  })
}

export function useGenerateProposalMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (type: string) => generateCalibrationProposal(type),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceProposals() })
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceOverview() })
    },
  })
}

export function useCreateCandidateProposalMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CreateCandidateProposalRequest) => createCandidateProposal(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceProposals() })
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceOverview() })
      queryClient.invalidateQueries({ queryKey: predictionKeys.governanceVersions() })
    },
  })
}

// ============================================================================
// MILESTONE 08.10: CONTEXT-AWARE ADAPTIVE INTELLIGENCE HOOKS
// ============================================================================

export function useAdaptiveOverviewQuery() {
  return useQuery({
    queryKey: predictionKeys.adaptiveOverview(),
    queryFn: ({ signal }) => getAdaptiveOverview(signal),
    staleTime: 30_000,
  })
}

export function useFieldPredictionContextQuery(fieldId?: string | null) {
  return useQuery({
    queryKey: predictionKeys.adaptiveContext(fieldId),
    queryFn: ({ signal }) =>
      fieldId ? getFieldPredictionContext(fieldId, signal) : Promise.reject('No fieldId'),
    enabled: !!fieldId,
    staleTime: 60_000,
  })
}

export function useContextPerformanceProfilesQuery(params?: { predictionType?: string; versionTag?: string }) {
  return useQuery({
    queryKey: predictionKeys.adaptivePerformance(params),
    queryFn: ({ signal }) => getContextPerformanceProfiles(params, signal),
    staleTime: 60_000,
  })
}

export function useAdaptiveSelectionHistoryQuery(params?: { fieldId?: string; limit?: number }) {
  return useQuery({
    queryKey: predictionKeys.adaptiveSelections(params),
    queryFn: ({ signal }) => getAdaptiveSelectionHistory(params, signal),
    staleTime: 30_000,
  })
}

export function useSelectAdaptiveMethodMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: SelectAdaptiveMethodRequest) => selectAdaptiveMethod(request),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: predictionKeys.adaptiveOverview() })
      queryClient.invalidateQueries({ queryKey: predictionKeys.adaptiveSelections() })
      queryClient.invalidateQueries({ queryKey: predictionKeys.adaptiveContext(vars.fieldId) })
    },
  })
}

export function usePredictionAdaptiveDecisionQuery(predictionId?: string | null) {
  return useQuery({
    queryKey: predictionKeys.predictionAdaptiveDecision(predictionId),
    queryFn: ({ signal }) =>
      predictionId ? getPredictionAdaptiveDecision(predictionId, signal) : Promise.reject('No predictionId'),
    enabled: !!predictionId,
    staleTime: 120_000,
  })
}



// ============================================================================
// MILESTONE 08.11: HYBRID ML PREDICTION ENGINE & EVIDENCE-BASED LEARNING HOOKS
// ============================================================================

export function useModelCandidatesQuery(type?: string) {
  return useQuery({
    queryKey: predictionKeys.modelCandidates(type),
    queryFn: ({ signal }) => import('@/entities/prediction').then((m) => m.getModelCandidates(type, signal)),
    staleTime: 30_000,
  })
}

export function useModelCandidateDetailQuery(id?: string | null) {
  return useQuery({
    queryKey: predictionKeys.modelCandidateDetail(id),
    queryFn: ({ signal }) =>
      id ? import('@/entities/prediction').then((m) => m.getModelCandidateDetail(id, signal)) : Promise.reject('No candidate ID'),
    enabled: !!id,
    staleTime: 60_000,
  })
}

export function useTrainingDatasetsQuery(type?: string) {
  return useQuery({
    queryKey: predictionKeys.trainingDatasets(type),
    queryFn: ({ signal }) => import('@/entities/prediction').then((m) => m.getTrainingDatasets(type, signal)),
    staleTime: 30_000,
  })
}

export function useTrainingDatasetDetailQuery(id?: string | null) {
  return useQuery({
    queryKey: predictionKeys.trainingDatasetDetail(id),
    queryFn: ({ signal }) =>
      id ? import('@/entities/prediction').then((m) => m.getTrainingDatasetDetail(id, signal)) : Promise.reject('No dataset ID'),
    enabled: !!id,
    staleTime: 60_000,
  })
}

export function useActiveModelComparisonQuery(type?: string) {
  return useQuery({
    queryKey: predictionKeys.activeModelComparison(type),
    queryFn: ({ signal }) => import('@/entities/prediction').then((m) => m.getActiveModelComparison(type, signal)),
    staleTime: 30_000,
  })
}

export function useTrainPredictionModelMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: import('@/entities/prediction').TrainPredictionModelRequest) =>
      import('@/entities/prediction').then((m) => m.trainPredictionModel(request)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: predictionKeys.all })
    },
  })
}

export function usePromoteModelCandidateMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({
      candidateId,
      request,
    }: {
      candidateId: string
      request: import('@/entities/prediction').PromoteCandidateRequest
    }) => import('@/entities/prediction').then((m) => m.promoteModelCandidate(candidateId, request)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: predictionKeys.all })
    },
  })
}
