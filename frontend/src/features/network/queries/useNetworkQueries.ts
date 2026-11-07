import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  networkApi,
  WaterNetworkOverview,
  NetworkTopology,
  SegmentCapacity,
  AffectedFieldsResponse,
  RegionalDemandResponse,
  DispatchSchedule,
  ActualDispatchDelivery,
  ResilienceScenario,
  DispatchOptimizationRequest,
  ApproveDispatchRequest,
  RejectDispatchRequest,
  RecordActualDeliveryRequest,
  ResilienceScenarioRequest,
} from '@/entities/network';

export const NETWORK_QUERY_KEYS = {
  all: ['network'] as const,
  overview: () => [...NETWORK_QUERY_KEYS.all, 'overview'] as const,
  topology: () => [...NETWORK_QUERY_KEYS.all, 'topology'] as const,
  segmentCapacity: (id?: string | null) => [...NETWORK_QUERY_KEYS.all, 'segmentCapacity', id] as const,
  affectedFields: (segmentId?: string | null) => [...NETWORK_QUERY_KEYS.all, 'affectedFields', segmentId] as const,
  demand: () => [...NETWORK_QUERY_KEYS.all, 'demand'] as const,
  schedules: (status?: string, wuaId?: string) => [...NETWORK_QUERY_KEYS.all, 'schedules', status, wuaId] as const,
  schedule: (id?: string | null) => [...NETWORK_QUERY_KEYS.all, 'schedule', id] as const,
  actualDeliveries: (schedId?: string, fieldId?: string) =>
    [...NETWORK_QUERY_KEYS.all, 'actualDeliveries', schedId, fieldId] as const,
  scenarios: () => [...NETWORK_QUERY_KEYS.all, 'scenarios'] as const,
  scenario: (id?: string | null) => [...NETWORK_QUERY_KEYS.all, 'scenario', id] as const,
};

export function useNetworkOverviewQuery() {
  return useQuery<WaterNetworkOverview>({
    queryKey: NETWORK_QUERY_KEYS.overview(),
    queryFn: ({ signal }) => networkApi.getOverview(signal),
    staleTime: 1000 * 60 * 2,
    refetchInterval: 1000 * 60 * 2,
  });
}

export function useNetworkTopologyQuery() {
  return useQuery<NetworkTopology>({
    queryKey: NETWORK_QUERY_KEYS.topology(),
    queryFn: ({ signal }) => networkApi.getTopology(signal),
    staleTime: 1000 * 60 * 5,
  });
}

export function useSegmentCapacityQuery(segmentId?: string | null) {
  return useQuery<SegmentCapacity>({
    queryKey: NETWORK_QUERY_KEYS.segmentCapacity(segmentId),
    queryFn: ({ signal }) => {
      if (!segmentId) throw new Error('segmentId is required');
      return networkApi.getSegmentCapacity(segmentId, signal);
    },
    enabled: Boolean(segmentId),
    staleTime: 1000 * 60 * 2,
  });
}

export function useAffectedFieldsQuery(segmentId?: string | null) {
  return useQuery<AffectedFieldsResponse>({
    queryKey: NETWORK_QUERY_KEYS.affectedFields(segmentId),
    queryFn: ({ signal }) => {
      if (!segmentId) throw new Error('segmentId is required');
      return networkApi.getAffectedFields(segmentId, signal);
    },
    enabled: Boolean(segmentId),
    staleTime: 1000 * 60 * 5,
  });
}

export function useRegionalDemandQuery() {
  return useQuery<RegionalDemandResponse>({
    queryKey: NETWORK_QUERY_KEYS.demand(),
    queryFn: ({ signal }) => networkApi.getDemandOverview(signal),
    staleTime: 1000 * 60 * 2,
  });
}

export function useDispatchSchedulesQuery(status?: string, targetWuaId?: string) {
  return useQuery<DispatchSchedule[]>({
    queryKey: NETWORK_QUERY_KEYS.schedules(status, targetWuaId),
    queryFn: ({ signal }) => networkApi.listSchedules(status, targetWuaId, signal),
    staleTime: 1000 * 30,
  });
}

export function useDispatchScheduleDetailQuery(id?: string | null) {
  return useQuery<DispatchSchedule>({
    queryKey: NETWORK_QUERY_KEYS.schedule(id),
    queryFn: ({ signal }) => {
      if (!id) throw new Error('id is required');
      return networkApi.getScheduleDetail(id, signal);
    },
    enabled: Boolean(id),
  });
}

export function useActualDeliveriesQuery(scheduleId?: string, fieldId?: string) {
  return useQuery<ActualDispatchDelivery[]>({
    queryKey: NETWORK_QUERY_KEYS.actualDeliveries(scheduleId, fieldId),
    queryFn: ({ signal }) => networkApi.listActualDeliveries(scheduleId, fieldId, signal),
    staleTime: 1000 * 60,
  });
}

export function useResilienceScenariosQuery() {
  return useQuery<ResilienceScenario[]>({
    queryKey: NETWORK_QUERY_KEYS.scenarios(),
    queryFn: ({ signal }) => networkApi.listScenarios(signal),
    staleTime: 1000 * 60 * 2,
  });
}

export function useOptimizeDispatchMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: DispatchOptimizationRequest) => networkApi.optimizeDispatch(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.schedules() });
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.overview() });
    },
  });
}

export function useApproveScheduleMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, req }: { id: string; req?: ApproveDispatchRequest }) =>
      networkApi.approveSchedule(id, req),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.schedules() });
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.schedule(vars.id) });
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.overview() });
    },
  });
}

export function useRejectScheduleMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, req }: { id: string; req: RejectDispatchRequest }) =>
      networkApi.rejectSchedule(id, req),
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.schedules() });
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.schedule(vars.id) });
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.overview() });
    },
  });
}

export function useStartExecutionMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => networkApi.startExecution(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.schedules() });
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.schedule(id) });
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.overview() });
    },
  });
}

export function useCompleteExecutionMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => networkApi.completeExecution(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.schedules() });
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.schedule(id) });
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.overview() });
    },
  });
}

export function useRecordActualDeliveryMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: RecordActualDeliveryRequest) => networkApi.recordActualDelivery(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.actualDeliveries() });
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.overview() });
    },
  });
}

export function useSimulateScenarioMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: ResilienceScenarioRequest) => networkApi.simulateScenario(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: NETWORK_QUERY_KEYS.scenarios() });
    },
  });
}
