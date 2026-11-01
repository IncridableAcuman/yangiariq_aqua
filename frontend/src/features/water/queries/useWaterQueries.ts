import {
  useQuery,
  useMutation,
  useQueryClient,
  UseQueryResult,
  UseMutationResult,
} from '@tanstack/react-query'
import {
  WaterSource,
  WaterAllocation,
  IrrigationPlan,
  IrrigationEvent,
  WaterAllocationConsumption,
  PlannedVsActualMetrics,
  FieldWaterSummary,
  WaterSourceQueryParams,
  WaterAllocationQueryParams,
  IrrigationPlanQueryParams,
  IrrigationEventQueryParams,
  FieldWaterSummaryQueryParams,
  CreateWaterSourcePayload,
  UpdateWaterSourcePayload,
  CreateWaterAllocationPayload,
  UpdateWaterAllocationPayload,
  CreateIrrigationPlanPayload,
  UpdateIrrigationPlanPayload,
  CreateIrrigationEventPayload,
  RecordCompletedIrrigationEventPayload,
  CompleteIrrigationEventPayload,
  CancelOperationPayload,
  getWaterSources,
  getWaterSourceById,
  createWaterSource,
  updateWaterSource,
  activateWaterSource,
  deactivateWaterSource,
  getWaterAllocations,
  getWaterAllocationById,
  createWaterAllocation,
  updateWaterAllocation,
  closeWaterAllocation,
  cancelWaterAllocation,
  getIrrigationPlans,
  getIrrigationPlanById,
  createIrrigationPlan,
  updateIrrigationPlan,
  startIrrigationPlan,
  completeIrrigationPlan,
  cancelIrrigationPlan,
  getIrrigationEvents,
  getIrrigationEventById,
  createIrrigationEvent,
  recordCompletedIrrigationEvent,
  completeIrrigationEvent,
  cancelIrrigationEvent,
  getFieldWaterSummary,
  getAllocationConsumption,
  getPlanMetrics,
} from '@/entities/water'
import { PageResponse } from '@/shared/api/client'

// -------------------------------------------------------------
// Query Key Factory
// -------------------------------------------------------------
export const waterKeys = {
  all: ['water'] as const,
  sources: () => [...waterKeys.all, 'sources'] as const,
  sourceList: (params: WaterSourceQueryParams = {}) =>
    [...waterKeys.sources(), 'list', params] as const,
  sourceDetail: (id: string) => [...waterKeys.sources(), 'detail', id] as const,

  allocations: () => [...waterKeys.all, 'allocations'] as const,
  allocationList: (params: WaterAllocationQueryParams = {}) =>
    [...waterKeys.allocations(), 'list', params] as const,
  allocationDetail: (id: string) => [...waterKeys.allocations(), 'detail', id] as const,
  allocationConsumption: (id: string) =>
    [...waterKeys.allocations(), 'consumption', id] as const,

  plans: () => [...waterKeys.all, 'plans'] as const,
  planList: (params: IrrigationPlanQueryParams = {}) =>
    [...waterKeys.plans(), 'list', params] as const,
  planDetail: (id: string) => [...waterKeys.plans(), 'detail', id] as const,
  planMetrics: (id: string) => [...waterKeys.plans(), 'metrics', id] as const,

  events: () => [...waterKeys.all, 'events'] as const,
  eventList: (params: IrrigationEventQueryParams = {}) =>
    [...waterKeys.events(), 'list', params] as const,
  eventDetail: (id: string) => [...waterKeys.events(), 'detail', id] as const,

  fieldSummaries: () => [...waterKeys.all, 'field-summaries'] as const,
  fieldSummary: (fieldId: string, params?: FieldWaterSummaryQueryParams) =>
    [...waterKeys.fieldSummaries(), fieldId, params || {}] as const,
}

// -------------------------------------------------------------
// Water Source Hooks
// -------------------------------------------------------------
export function useWaterSourcesQuery(
  params: WaterSourceQueryParams = {}
): UseQueryResult<PageResponse<WaterSource>, Error> {
  return useQuery({
    queryKey: waterKeys.sourceList(params),
    queryFn: () => getWaterSources(params),
  })
}

export function useWaterSourceQuery(
  id: string | null | undefined
): UseQueryResult<WaterSource, Error> {
  return useQuery({
    queryKey: waterKeys.sourceDetail(id || ''),
    queryFn: () => getWaterSourceById(id!),
    enabled: Boolean(id),
  })
}

export function useCreateWaterSourceMutation(): UseMutationResult<
  WaterSource,
  Error,
  CreateWaterSourcePayload
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateWaterSourcePayload) => createWaterSource(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: waterKeys.sources() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

export function useUpdateWaterSourceMutation(): UseMutationResult<
  WaterSource,
  Error,
  { id: string; payload: UpdateWaterSourcePayload }
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateWaterSourcePayload }) =>
      updateWaterSource(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: waterKeys.sourceDetail(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.sources() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

export function useActivateWaterSourceMutation(): UseMutationResult<
  WaterSource,
  Error,
  string
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => activateWaterSource(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: waterKeys.sourceDetail(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.sources() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

export function useDeactivateWaterSourceMutation(): UseMutationResult<
  WaterSource,
  Error,
  string
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deactivateWaterSource(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: waterKeys.sourceDetail(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.sources() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

// -------------------------------------------------------------
// Water Allocation Hooks
// -------------------------------------------------------------
export function useWaterAllocationsQuery(
  params: WaterAllocationQueryParams = {}
): UseQueryResult<PageResponse<WaterAllocation>, Error> {
  return useQuery({
    queryKey: waterKeys.allocationList(params),
    queryFn: () => getWaterAllocations(params),
  })
}

export function useWaterAllocationQuery(
  id: string | null | undefined
): UseQueryResult<WaterAllocation, Error> {
  return useQuery({
    queryKey: waterKeys.allocationDetail(id || ''),
    queryFn: () => getWaterAllocationById(id!),
    enabled: Boolean(id),
  })
}

export function useAllocationConsumptionQuery(
  allocationId: string | null | undefined
): UseQueryResult<WaterAllocationConsumption, Error> {
  return useQuery({
    queryKey: waterKeys.allocationConsumption(allocationId || ''),
    queryFn: () => getAllocationConsumption(allocationId!),
    enabled: Boolean(allocationId),
  })
}

export function useCreateWaterAllocationMutation(): UseMutationResult<
  WaterAllocation,
  Error,
  CreateWaterAllocationPayload
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateWaterAllocationPayload) => createWaterAllocation(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: waterKeys.allocations() })
      queryClient.invalidateQueries({ queryKey: waterKeys.fieldSummaries() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

export function useUpdateWaterAllocationMutation(): UseMutationResult<
  WaterAllocation,
  Error,
  { id: string; payload: UpdateWaterAllocationPayload }
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateWaterAllocationPayload }) =>
      updateWaterAllocation(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: waterKeys.allocationDetail(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.allocationConsumption(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.allocations() })
      queryClient.invalidateQueries({ queryKey: waterKeys.fieldSummaries() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

export function useCloseWaterAllocationMutation(): UseMutationResult<
  WaterAllocation,
  Error,
  { id: string; payload?: CancelOperationPayload }
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload?: CancelOperationPayload }) =>
      closeWaterAllocation(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: waterKeys.allocationDetail(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.allocationConsumption(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.allocations() })
      queryClient.invalidateQueries({ queryKey: waterKeys.fieldSummaries() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

export function useCancelWaterAllocationMutation(): UseMutationResult<
  WaterAllocation,
  Error,
  { id: string; payload?: CancelOperationPayload }
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload?: CancelOperationPayload }) =>
      cancelWaterAllocation(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: waterKeys.allocationDetail(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.allocationConsumption(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.allocations() })
      queryClient.invalidateQueries({ queryKey: waterKeys.fieldSummaries() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

// -------------------------------------------------------------
// Irrigation Plan Hooks
// -------------------------------------------------------------
export function useIrrigationPlansQuery(
  params: IrrigationPlanQueryParams = {}
): UseQueryResult<PageResponse<IrrigationPlan>, Error> {
  return useQuery({
    queryKey: waterKeys.planList(params),
    queryFn: () => getIrrigationPlans(params),
  })
}

export function useIrrigationPlanQuery(
  id: string | null | undefined
): UseQueryResult<IrrigationPlan, Error> {
  return useQuery({
    queryKey: waterKeys.planDetail(id || ''),
    queryFn: () => getIrrigationPlanById(id!),
    enabled: Boolean(id),
  })
}

export function usePlanMetricsQuery(
  planId: string | null | undefined
): UseQueryResult<PlannedVsActualMetrics, Error> {
  return useQuery({
    queryKey: waterKeys.planMetrics(planId || ''),
    queryFn: () => getPlanMetrics(planId!),
    enabled: Boolean(planId),
  })
}

export function useCreateIrrigationPlanMutation(): UseMutationResult<
  IrrigationPlan,
  Error,
  CreateIrrigationPlanPayload
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateIrrigationPlanPayload) => createIrrigationPlan(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: waterKeys.plans() })
      queryClient.invalidateQueries({ queryKey: waterKeys.fieldSummaries() })
    },
  })
}

export function useUpdateIrrigationPlanMutation(): UseMutationResult<
  IrrigationPlan,
  Error,
  { id: string; payload: UpdateIrrigationPlanPayload }
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateIrrigationPlanPayload }) =>
      updateIrrigationPlan(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: waterKeys.planDetail(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.planMetrics(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.plans() })
      queryClient.invalidateQueries({ queryKey: waterKeys.fieldSummaries() })
    },
  })
}

export function useStartIrrigationPlanMutation(): UseMutationResult<
  IrrigationPlan,
  Error,
  string
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => startIrrigationPlan(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: waterKeys.planDetail(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.planMetrics(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.plans() })
    },
  })
}

export function useCompleteIrrigationPlanMutation(): UseMutationResult<
  IrrigationPlan,
  Error,
  string
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => completeIrrigationPlan(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: waterKeys.planDetail(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.planMetrics(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.plans() })
    },
  })
}

export function useCancelIrrigationPlanMutation(): UseMutationResult<
  IrrigationPlan,
  Error,
  { id: string; payload?: CancelOperationPayload }
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload?: CancelOperationPayload }) =>
      cancelIrrigationPlan(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: waterKeys.planDetail(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.planMetrics(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.plans() })
    },
  })
}

// -------------------------------------------------------------
// Irrigation Event Hooks
// -------------------------------------------------------------
export function useIrrigationEventsQuery(
  params: IrrigationEventQueryParams = {}
): UseQueryResult<PageResponse<IrrigationEvent>, Error> {
  return useQuery({
    queryKey: waterKeys.eventList(params),
    queryFn: () => getIrrigationEvents(params),
  })
}

export function useIrrigationEventQuery(
  id: string | null | undefined
): UseQueryResult<IrrigationEvent, Error> {
  return useQuery({
    queryKey: waterKeys.eventDetail(id || ''),
    queryFn: () => getIrrigationEventById(id!),
    enabled: Boolean(id),
  })
}

export function useCreateIrrigationEventMutation(): UseMutationResult<
  IrrigationEvent,
  Error,
  CreateIrrigationEventPayload
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateIrrigationEventPayload) => createIrrigationEvent(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: waterKeys.events() })
      queryClient.invalidateQueries({ queryKey: waterKeys.plans() })
    },
  })
}

export function useRecordCompletedIrrigationEventMutation(): UseMutationResult<
  IrrigationEvent,
  Error,
  RecordCompletedIrrigationEventPayload
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: RecordCompletedIrrigationEventPayload) =>
      recordCompletedIrrigationEvent(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: waterKeys.events() })
      queryClient.invalidateQueries({ queryKey: waterKeys.plans() })
      queryClient.invalidateQueries({ queryKey: waterKeys.allocations() })
      queryClient.invalidateQueries({ queryKey: waterKeys.fieldSummaries() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

export function useCompleteIrrigationEventMutation(): UseMutationResult<
  IrrigationEvent,
  Error,
  { id: string; payload: CompleteIrrigationEventPayload }
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: CompleteIrrigationEventPayload }) =>
      completeIrrigationEvent(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: waterKeys.eventDetail(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.events() })
      queryClient.invalidateQueries({ queryKey: waterKeys.plans() })
      queryClient.invalidateQueries({ queryKey: waterKeys.allocations() })
      queryClient.invalidateQueries({ queryKey: waterKeys.fieldSummaries() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

export function useCancelIrrigationEventMutation(): UseMutationResult<
  IrrigationEvent,
  Error,
  { id: string; payload?: CancelOperationPayload }
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload?: CancelOperationPayload }) =>
      cancelIrrigationEvent(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: waterKeys.eventDetail(data.id) })
      queryClient.invalidateQueries({ queryKey: waterKeys.events() })
      queryClient.invalidateQueries({ queryKey: waterKeys.plans() })
    },
  })
}

// -------------------------------------------------------------
// Field Water Summary Hook
// -------------------------------------------------------------
export function useFieldWaterSummaryQuery(
  fieldId: string | null | undefined,
  params: FieldWaterSummaryQueryParams = {}
): UseQueryResult<FieldWaterSummary, Error> {
  return useQuery({
    queryKey: waterKeys.fieldSummary(fieldId || '', params),
    queryFn: () => getFieldWaterSummary(fieldId!, params),
    enabled: Boolean(fieldId),
  })
}
