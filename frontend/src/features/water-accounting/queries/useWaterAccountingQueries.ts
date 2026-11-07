import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ReconciliationReviewRequest, waterAccountingApi } from '@/entities/water-accounting';

export const waterAccountingKeys = {
  all: ['water-accounting'] as const,
  overview: (params?: { periodStart?: string; periodEnd?: string }) =>
    [...waterAccountingKeys.all, 'overview', params] as const,
  balance: (params?: Record<string, string | undefined>) =>
    [...waterAccountingKeys.all, 'balance', params] as const,
  ledger: (params?: Record<string, string | undefined>) =>
    [...waterAccountingKeys.all, 'ledger', params] as const,
  losses: () => [...waterAccountingKeys.all, 'losses'] as const,
  compliance: (fieldId?: string) => [...waterAccountingKeys.all, 'compliance', fieldId] as const,
  equity: () => [...waterAccountingKeys.all, 'equity'] as const,
  conservation: () => [...waterAccountingKeys.all, 'conservation'] as const,
  reconciliation: (boundaryType?: string) =>
    [...waterAccountingKeys.all, 'reconciliation', boundaryType] as const,
  field: (fieldId: string) => [...waterAccountingKeys.all, 'field', fieldId] as const,
  wua: (wuaId: string) => [...waterAccountingKeys.all, 'wua', wuaId] as const,
};

export const useWaterAccountingOverview = (params?: { periodStart?: string; periodEnd?: string }) =>
  useQuery({
    queryKey: waterAccountingKeys.overview(params),
    queryFn: ({ signal }) => waterAccountingApi.getOverview(params, signal),
  });

export const useWaterBalance = (params?: {
  dimension?: string;
  boundaryId?: string;
  periodType?: string;
  periodStart?: string;
  periodEnd?: string;
}) =>
  useQuery({
    queryKey: waterAccountingKeys.balance(params),
    queryFn: ({ signal }) => waterAccountingApi.getBalance(params, signal),
  });

export const useWaterLedger = (params?: {
  fieldId?: string;
  wuaId?: string;
  edgeId?: string;
  periodStart?: string;
  periodEnd?: string;
}) =>
  useQuery({
    queryKey: waterAccountingKeys.ledger(params),
    queryFn: ({ signal }) => waterAccountingApi.getLedger(params, signal),
  });

export const useConveyanceLosses = () =>
  useQuery({
    queryKey: waterAccountingKeys.losses(),
    queryFn: ({ signal }) => waterAccountingApi.getConveyanceLosses(signal),
  });

export const useAllocationCompliance = (fieldId?: string) =>
  useQuery({
    queryKey: waterAccountingKeys.compliance(fieldId),
    queryFn: ({ signal }) => waterAccountingApi.getAllocationCompliance(fieldId, signal),
  });

export const useDistributionEquity = () =>
  useQuery({
    queryKey: waterAccountingKeys.equity(),
    queryFn: ({ signal }) => waterAccountingApi.getEquity(signal),
  });

export const useConservationOpportunities = () =>
  useQuery({
    queryKey: waterAccountingKeys.conservation(),
    queryFn: ({ signal }) => waterAccountingApi.getConservationOpportunities(signal),
  });

export const useWaterReconciliation = (boundaryType?: string) =>
  useQuery({
    queryKey: waterAccountingKeys.reconciliation(boundaryType),
    queryFn: ({ signal }) => waterAccountingApi.getReconciliation(boundaryType, signal),
  });

export const useReviewReconciliationMutation = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, request }: { id: string; request: ReconciliationReviewRequest }) =>
      waterAccountingApi.reviewReconciliation(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: waterAccountingKeys.all });
    },
  });
};

export const useFieldWaterAccounting = (fieldId?: string) =>
  useQuery({
    queryKey: fieldId ? waterAccountingKeys.field(fieldId) : ['water-accounting', 'field', 'empty'],
    queryFn: ({ signal }) => (fieldId ? waterAccountingApi.getFieldAccounting(fieldId, signal) : null),
    enabled: !!fieldId,
  });

export const useWuaWaterAccounting = (wuaId?: string) =>
  useQuery({
    queryKey: wuaId ? waterAccountingKeys.wua(wuaId) : ['water-accounting', 'wua', 'empty'],
    queryFn: ({ signal }) => (wuaId ? waterAccountingApi.getWuaAccounting(wuaId, signal) : null),
    enabled: !!wuaId,
  });
