import { apiClient } from '@/shared/api/client';
import {
  ActualDispatchDelivery,
  AffectedFieldsResponse,
  ApproveDispatchRequest,
  DispatchOptimizationRequest,
  DispatchSchedule,
  NetworkTopology,
  RecordActualDeliveryRequest,
  RegionalDemandResponse,
  RejectDispatchRequest,
  ResilienceScenario,
  ResilienceScenarioRequest,
  SegmentCapacity,
  WaterNetworkOverview,
} from './types';

export const networkApi = {
  getOverview: async (signal?: AbortSignal): Promise<WaterNetworkOverview> => {
    const res = await apiClient<WaterNetworkOverview>('/water-network/overview', {
      method: 'GET',
      signal,
    });
    if (!res.data) throw new Error('Water network overview not returned by server');
    return res.data;
  },

  getTopology: async (signal?: AbortSignal): Promise<NetworkTopology> => {
    const res = await apiClient<NetworkTopology>('/water-network/topology', {
      method: 'GET',
      signal,
    });
    if (!res.data) throw new Error('Network topology not returned by server');
    return res.data;
  },

  getSegmentCapacity: async (
    segmentId: string,
    signal?: AbortSignal
  ): Promise<SegmentCapacity> => {
    const res = await apiClient<SegmentCapacity>(
      `/water-network/segments/${encodeURIComponent(segmentId)}/capacity`,
      { method: 'GET', signal }
    );
    if (!res.data) throw new Error('Segment capacity not returned by server');
    return res.data;
  },

  getAffectedFields: async (
    segmentId: string,
    signal?: AbortSignal
  ): Promise<AffectedFieldsResponse> => {
    const res = await apiClient<AffectedFieldsResponse>(
      `/water-network/affected-fields/${encodeURIComponent(segmentId)}`,
      { method: 'GET', signal }
    );
    if (!res.data) throw new Error('Affected fields not returned by server');
    return res.data;
  },

  getDemandOverview: async (signal?: AbortSignal): Promise<RegionalDemandResponse> => {
    const res = await apiClient<RegionalDemandResponse>('/water-network/demand/overview', {
      method: 'GET',
      signal,
    });
    if (!res.data) throw new Error('Demand overview not returned by server');
    return res.data;
  },

  optimizeDispatch: async (
    request: DispatchOptimizationRequest,
    signal?: AbortSignal
  ): Promise<DispatchSchedule> => {
    const res = await apiClient<DispatchSchedule>('/water-network/dispatch/optimize', {
      method: 'POST',
      body: JSON.stringify(request),
      signal,
    });
    if (!res.data) throw new Error('Dispatch optimization failed');
    return res.data;
  },

  listSchedules: async (
    status?: string,
    targetWuaId?: string,
    signal?: AbortSignal
  ): Promise<DispatchSchedule[]> => {
    const query = new URLSearchParams();
    if (status) query.set('status', status);
    if (targetWuaId) query.set('targetWuaId', targetWuaId);
    const qs = query.toString();
    const res = await apiClient<DispatchSchedule[]>(
      `/water-network/dispatch/schedules${qs ? `?${qs}` : ''}`,
      { method: 'GET', signal }
    );
    return res.data ?? [];
  },

  getScheduleDetail: async (id: string, signal?: AbortSignal): Promise<DispatchSchedule> => {
    const res = await apiClient<DispatchSchedule>(
      `/water-network/dispatch/schedules/${encodeURIComponent(id)}`,
      { method: 'GET', signal }
    );
    if (!res.data) throw new Error('Schedule detail not returned by server');
    return res.data;
  },

  approveSchedule: async (
    id: string,
    request?: ApproveDispatchRequest,
    signal?: AbortSignal
  ): Promise<DispatchSchedule> => {
    const res = await apiClient<DispatchSchedule>(
      `/water-network/dispatch/schedules/${encodeURIComponent(id)}/approve`,
      {
        method: 'POST',
        body: request ? JSON.stringify(request) : undefined,
        signal,
      }
    );
    if (!res.data) throw new Error('Schedule approval failed');
    return res.data;
  },

  rejectSchedule: async (
    id: string,
    request: RejectDispatchRequest,
    signal?: AbortSignal
  ): Promise<DispatchSchedule> => {
    const res = await apiClient<DispatchSchedule>(
      `/water-network/dispatch/schedules/${encodeURIComponent(id)}/reject`,
      {
        method: 'POST',
        body: JSON.stringify(request),
        signal,
      }
    );
    if (!res.data) throw new Error('Schedule rejection failed');
    return res.data;
  },

  startExecution: async (id: string, signal?: AbortSignal): Promise<DispatchSchedule> => {
    const res = await apiClient<DispatchSchedule>(
      `/water-network/dispatch/schedules/${encodeURIComponent(id)}/start`,
      { method: 'POST', signal }
    );
    if (!res.data) throw new Error('Execution start failed');
    return res.data;
  },

  completeExecution: async (id: string, signal?: AbortSignal): Promise<DispatchSchedule> => {
    const res = await apiClient<DispatchSchedule>(
      `/water-network/dispatch/schedules/${encodeURIComponent(id)}/complete`,
      { method: 'POST', signal }
    );
    if (!res.data) throw new Error('Execution complete failed');
    return res.data;
  },

  recordActualDelivery: async (
    request: RecordActualDeliveryRequest,
    signal?: AbortSignal
  ): Promise<ActualDispatchDelivery> => {
    const res = await apiClient<ActualDispatchDelivery>(
      '/water-network/dispatch/actual-delivery',
      {
        method: 'POST',
        body: JSON.stringify(request),
        signal,
      }
    );
    if (!res.data) throw new Error('Record actual delivery failed');
    return res.data;
  },

  listActualDeliveries: async (
    dispatchScheduleId?: string,
    fieldId?: string,
    signal?: AbortSignal
  ): Promise<ActualDispatchDelivery[]> => {
    const query = new URLSearchParams();
    if (dispatchScheduleId) query.set('dispatchScheduleId', dispatchScheduleId);
    if (fieldId) query.set('fieldId', fieldId);
    const qs = query.toString();
    const res = await apiClient<ActualDispatchDelivery[]>(
      `/water-network/dispatch/actual-deliveries${qs ? `?${qs}` : ''}`,
      { method: 'GET', signal }
    );
    return res.data ?? [];
  },

  simulateScenario: async (
    request: ResilienceScenarioRequest,
    signal?: AbortSignal
  ): Promise<ResilienceScenario> => {
    const res = await apiClient<ResilienceScenario>(
      '/water-network/scenarios/simulate',
      {
        method: 'POST',
        body: JSON.stringify(request),
        signal,
      }
    );
    if (!res.data) throw new Error('Resilience simulation failed');
    return res.data;
  },

  listScenarios: async (signal?: AbortSignal): Promise<ResilienceScenario[]> => {
    const res = await apiClient<ResilienceScenario[]>('/water-network/scenarios', {
      method: 'GET',
      signal,
    });
    return res.data ?? [];
  },

  getScenarioDetail: async (id: string, signal?: AbortSignal): Promise<ResilienceScenario> => {
    const res = await apiClient<ResilienceScenario>(
      `/water-network/scenarios/${encodeURIComponent(id)}`,
      { method: 'GET', signal }
    );
    if (!res.data) throw new Error('Scenario detail not returned by server');
    return res.data;
  },
};
