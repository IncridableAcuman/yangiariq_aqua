import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '@/shared/api/client'
import {
  DataSource,
  DataQualitySummary,
  IngestedProvenance,
  ImportBatchSummary,
  CreateDataSourceRequest,
  UpdateDataSourceRequest,
  RecordManualMeasurementRequest,
  IngestionResult,
} from '@/entities/integration'

export const INTEGRATION_QUERY_KEYS = {
  allSources: ['data-sources'] as const,
  sourceDetail: (id: string) => ['data-sources', id] as const,
  qualitySummary: (days: number) => ['data-quality', 'summary', days] as const,
  ingestionHistory: (params: { dataSourceId?: string; fieldId?: string; limit?: number }) =>
    ['data-ingestion', 'history', params] as const,
  importBatches: ['data-imports', 'batches'] as const,
}

export function useDataSourcesQuery() {
  return useQuery({
    queryKey: INTEGRATION_QUERY_KEYS.allSources,
    queryFn: async () => {
      const res = await apiClient<DataSource[]>('/data-sources')
      return res.data || []
    },
  })
}

export function useDataSourceQuery(id: string) {
  return useQuery({
    queryKey: INTEGRATION_QUERY_KEYS.sourceDetail(id),
    queryFn: async () => {
      const res = await apiClient<DataSource>(`/data-sources/${id}`)
      return res.data
    },
    enabled: Boolean(id),
  })
}

export function useDataQualitySummaryQuery(days: number = 30) {
  return useQuery({
    queryKey: INTEGRATION_QUERY_KEYS.qualitySummary(days),
    queryFn: async () => {
      const res = await apiClient<DataQualitySummary>(`/data-quality/summary?days=${days}`)
      return res.data
    },
  })
}

export function useIngestionHistoryQuery(params: { dataSourceId?: string; fieldId?: string; limit?: number } = {}) {
  return useQuery({
    queryKey: INTEGRATION_QUERY_KEYS.ingestionHistory(params),
    queryFn: async () => {
      const query = new URLSearchParams()
      if (params.dataSourceId) query.set('dataSourceId', params.dataSourceId)
      if (params.fieldId) query.set('fieldId', params.fieldId)
      if (params.limit) query.set('limit', String(params.limit))
      const res = await apiClient<IngestedProvenance[]>(`/data-ingestion/history?${query.toString()}`)
      return res.data || []
    },
  })
}

export function useImportBatchesQuery() {
  return useQuery({
    queryKey: INTEGRATION_QUERY_KEYS.importBatches,
    queryFn: async () => {
      const res = await apiClient<ImportBatchSummary[]>('/data-imports')
      return res.data || []
    },
  })
}

export function useRegisterDataSourceMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (payload: CreateDataSourceRequest) => {
      const res = await apiClient<DataSource>('/data-sources', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: INTEGRATION_QUERY_KEYS.allSources })
    },
  })
}

export function useUpdateDataSourceMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, data }: { id: string; data: UpdateDataSourceRequest }) => {
      const res = await apiClient<DataSource>(`/data-sources/${id}`, {
        method: 'PATCH',
        body: JSON.stringify(data),
      })
      return res.data
    },
    onSuccess: (_, vars) => {
      queryClient.invalidateQueries({ queryKey: INTEGRATION_QUERY_KEYS.allSources })
      queryClient.invalidateQueries({ queryKey: INTEGRATION_QUERY_KEYS.sourceDetail(vars.id) })
    },
  })
}

export function useToggleDataSourceMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      const res = await apiClient<DataSource>(`/data-sources/${id}/toggle`, {
        method: 'POST',
      })
      return res.data
    },
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: INTEGRATION_QUERY_KEYS.allSources })
      queryClient.invalidateQueries({ queryKey: INTEGRATION_QUERY_KEYS.sourceDetail(id) })
    },
  })
}

export function useSyncDataSourceMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      const res = await apiClient<DataSource>(`/data-sources/${id}/sync`, {
        method: 'POST',
      })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: INTEGRATION_QUERY_KEYS.allSources })
    },
  })
}

export function useRecordManualMeasurementMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (payload: RecordManualMeasurementRequest) => {
      const res = await apiClient<IngestionResult>('/data-ingestion/manual', {
        method: 'POST',
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sensors'] })
      queryClient.invalidateQueries({ queryKey: ['data-ingestion', 'history'] })
      queryClient.invalidateQueries({ queryKey: ['data-quality'] })
    },
  })
}

export function useUploadCsvMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ file, dryRun }: { file: File; dryRun: boolean }) => {
      const formData = new FormData()
      formData.append('file', file)
      const res = await apiClient<ImportBatchSummary>(`/data-imports/csv?dryRun=${dryRun}`, {
        method: 'POST',
        body: formData,
      })
      return res.data
    },
    onSuccess: (_, vars) => {
      if (!vars.dryRun) {
        queryClient.invalidateQueries({ queryKey: INTEGRATION_QUERY_KEYS.importBatches })
        queryClient.invalidateQueries({ queryKey: INTEGRATION_QUERY_KEYS.allSources })
        queryClient.invalidateQueries({ queryKey: ['data-ingestion', 'history'] })
        queryClient.invalidateQueries({ queryKey: ['data-quality'] })
      }
    },
  })
}
