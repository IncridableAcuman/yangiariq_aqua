import { apiClient } from '@/shared/api/client';
import {
  AllocationComplianceResponse,
  ConservationOpportunityResponse,
  ConveyanceLossResponse,
  DistributionEquityResponse,
  FieldWaterAccountingResponse,
  ReconciliationReviewRequest,
  RegionalWaterBalanceResponse,
  RegionalWaterOverviewResponse,
  WaterLedgerEntryResponse,
  WaterReconciliationResponse,
  WuaWaterAccountingResponse,
} from './types';

export const waterAccountingApi = {
  getOverview: async (
    params?: { periodStart?: string; periodEnd?: string },
    signal?: AbortSignal
  ): Promise<RegionalWaterOverviewResponse> => {
    const searchParams = new URLSearchParams();
    if (params?.periodStart) searchParams.set('periodStart', params.periodStart);
    if (params?.periodEnd) searchParams.set('periodEnd', params.periodEnd);

    const query = searchParams.toString() ? `?${searchParams.toString()}` : '';
    const res = await apiClient<RegionalWaterOverviewResponse>(`/water-accounting/overview${query}`, {
      method: 'GET',
      signal,
    });
    if (!res.data) throw new Error('Water accounting overview not returned');
    return res.data;
  },

  getBalance: async (
    params?: {
      dimension?: string;
      boundaryId?: string;
      periodType?: string;
      periodStart?: string;
      periodEnd?: string;
    },
    signal?: AbortSignal
  ): Promise<RegionalWaterBalanceResponse> => {
    const searchParams = new URLSearchParams();
    if (params?.dimension) searchParams.set('dimension', params.dimension);
    if (params?.boundaryId) searchParams.set('boundaryId', params.boundaryId);
    if (params?.periodType) searchParams.set('periodType', params.periodType);
    if (params?.periodStart) searchParams.set('periodStart', params.periodStart);
    if (params?.periodEnd) searchParams.set('periodEnd', params.periodEnd);

    const query = searchParams.toString() ? `?${searchParams.toString()}` : '';
    const res = await apiClient<RegionalWaterBalanceResponse>(`/water-accounting/balance${query}`, {
      method: 'GET',
      signal,
    });
    if (!res.data) throw new Error('Water balance data not returned');
    return res.data;
  },

  getLedger: async (
    params?: {
      fieldId?: string;
      wuaId?: string;
      edgeId?: string;
      periodStart?: string;
      periodEnd?: string;
    },
    signal?: AbortSignal
  ): Promise<WaterLedgerEntryResponse[]> => {
    const searchParams = new URLSearchParams();
    if (params?.fieldId) searchParams.set('fieldId', params.fieldId);
    if (params?.wuaId) searchParams.set('wuaId', params.wuaId);
    if (params?.edgeId) searchParams.set('edgeId', params.edgeId);
    if (params?.periodStart) searchParams.set('periodStart', params.periodStart);
    if (params?.periodEnd) searchParams.set('periodEnd', params.periodEnd);

    const query = searchParams.toString() ? `?${searchParams.toString()}` : '';
    const res = await apiClient<WaterLedgerEntryResponse[]>(`/water-accounting/ledger${query}`, {
      method: 'GET',
      signal,
    });
    return res.data ?? [];
  },

  getConveyanceLosses: async (signal?: AbortSignal): Promise<ConveyanceLossResponse[]> => {
    const res = await apiClient<ConveyanceLossResponse[]>('/water-accounting/conveyance-losses', {
      method: 'GET',
      signal,
    });
    return res.data ?? [];
  },

  getAllocationCompliance: async (
    fieldId?: string,
    signal?: AbortSignal
  ): Promise<AllocationComplianceResponse[]> => {
    const query = fieldId ? `?fieldId=${encodeURIComponent(fieldId)}` : '';
    const res = await apiClient<AllocationComplianceResponse[]>(
      `/water-accounting/allocation-compliance${query}`,
      { method: 'GET', signal }
    );
    return res.data ?? [];
  },

  getEquity: async (signal?: AbortSignal): Promise<DistributionEquityResponse[]> => {
    const res = await apiClient<DistributionEquityResponse[]>('/water-accounting/equity', {
      method: 'GET',
      signal,
    });
    return res.data ?? [];
  },

  getConservationOpportunities: async (
    signal?: AbortSignal
  ): Promise<ConservationOpportunityResponse[]> => {
    const res = await apiClient<ConservationOpportunityResponse[]>(
      '/water-accounting/conservation-opportunities',
      { method: 'GET', signal }
    );
    return res.data ?? [];
  },

  getReconciliation: async (
    boundaryType?: string,
    signal?: AbortSignal
  ): Promise<WaterReconciliationResponse[]> => {
    const query = boundaryType ? `?boundaryType=${encodeURIComponent(boundaryType)}` : '';
    const res = await apiClient<WaterReconciliationResponse[]>(
      `/water-accounting/reconciliation${query}`,
      { method: 'GET', signal }
    );
    return res.data ?? [];
  },

  reviewReconciliation: async (
    id: string,
    request: ReconciliationReviewRequest
  ): Promise<WaterReconciliationResponse> => {
    const res = await apiClient<WaterReconciliationResponse>(
      `/water-accounting/reconciliation/${encodeURIComponent(id)}/review`,
      {
        method: 'POST',
        body: JSON.stringify(request),
      }
    );
    if (!res.data) throw new Error('Reconciliation review failed');
    return res.data;
  },

  getFieldAccounting: async (
    fieldId: string,
    signal?: AbortSignal
  ): Promise<FieldWaterAccountingResponse> => {
    const res = await apiClient<FieldWaterAccountingResponse>(
      `/water-accounting/fields/${encodeURIComponent(fieldId)}`,
      { method: 'GET', signal }
    );
    if (!res.data) throw new Error('Field water accounting not returned');
    return res.data;
  },

  getWuaAccounting: async (
    wuaId: string,
    signal?: AbortSignal
  ): Promise<WuaWaterAccountingResponse> => {
    const res = await apiClient<WuaWaterAccountingResponse>(
      `/water-accounting/wuas/${encodeURIComponent(wuaId)}`,
      { method: 'GET', signal }
    );
    if (!res.data) throw new Error('WUA water accounting not returned');
    return res.data;
  },
};
