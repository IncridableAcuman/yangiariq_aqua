import {
  useQuery,
  useMutation,
  useQueryClient,
  UseQueryResult,
  UseMutationResult,
} from '@tanstack/react-query'
import {
  Sensor,
  SensorReading,
  SensorReadingPage,
  TelemetryAggregate,
  FieldAnalyticsSummary,
  TelemetryAnalyticsQueryParams,
  SensorReadingQueryParams,
  CreateSensorPayload,
  createSensor,
  getSensor,
  getFieldSensors,
  getSensorReadings,
  simulateSensorReading,
  getSensorAnalytics,
  getFieldAnalytics,
  getFieldAnalyticsSummary,
} from '@/entities/sensor'

export const sensorQueryKeys = {
  all: ['sensors'] as const,
  byField: (fieldId: string) => [...sensorQueryKeys.all, 'field', fieldId] as const,
  detail: (id: string) => [...sensorQueryKeys.all, 'detail', id] as const,
  analytics: (sensorId: string, params: TelemetryAnalyticsQueryParams) =>
    [...sensorQueryKeys.all, 'analytics', sensorId, params] as const,
  fieldAnalytics: (fieldId: string, params: TelemetryAnalyticsQueryParams) =>
    [...sensorQueryKeys.all, 'fieldAnalytics', fieldId, params] as const,
  fieldSummary: (fieldId: string) =>
    [...sensorQueryKeys.all, 'fieldSummary', fieldId] as const,
  sensorHistory: (sensorId: string, params: SensorReadingQueryParams) =>
    [...sensorQueryKeys.all, 'history', sensorId, params] as const,
}

export function useFieldSensorsQuery(
  fieldId: string | null | undefined
): UseQueryResult<Sensor[], Error> {
  return useQuery({
    queryKey: sensorQueryKeys.byField(fieldId || ''),
    queryFn: () => getFieldSensors(fieldId!),
    enabled: Boolean(fieldId),
    staleTime: 30_000,
  })
}

export function useSensorDetailQuery(
  sensorId: string | null | undefined
): UseQueryResult<Sensor, Error> {
  return useQuery({
    queryKey: sensorQueryKeys.detail(sensorId || ''),
    queryFn: () => getSensor(sensorId!),
    enabled: Boolean(sensorId),
    staleTime: 30_000,
  })
}

export function useSensorAnalyticsQuery(
  sensorId: string | null | undefined,
  params: TelemetryAnalyticsQueryParams = {}
): UseQueryResult<TelemetryAggregate[], Error> {
  return useQuery({
    queryKey: sensorQueryKeys.analytics(sensorId || '', params),
    queryFn: () => getSensorAnalytics(sensorId!, params),
    enabled: Boolean(sensorId),
    staleTime: 60_000,
  })
}

export function useFieldAnalyticsQuery(
  fieldId: string | null | undefined,
  params: TelemetryAnalyticsQueryParams = {}
): UseQueryResult<TelemetryAggregate[], Error> {
  return useQuery({
    queryKey: sensorQueryKeys.fieldAnalytics(fieldId || '', params),
    queryFn: () => getFieldAnalytics(fieldId!, params),
    enabled: Boolean(fieldId),
    staleTime: 60_000,
  })
}

export function useFieldAnalyticsSummaryQuery(
  fieldId: string | null | undefined
): UseQueryResult<FieldAnalyticsSummary, Error> {
  return useQuery({
    queryKey: sensorQueryKeys.fieldSummary(fieldId || ''),
    queryFn: () => getFieldAnalyticsSummary(fieldId!),
    enabled: Boolean(fieldId),
    staleTime: 30_000,
  })
}

export function useSensorHistoryQuery(
  sensorId: string | null | undefined,
  params: SensorReadingQueryParams = {}
): UseQueryResult<SensorReadingPage, Error> {
  return useQuery({
    queryKey: sensorQueryKeys.sensorHistory(sensorId || '', params),
    queryFn: () => getSensorReadings(sensorId!, params),
    enabled: Boolean(sensorId),
    staleTime: 30_000,
  })
}

export function useCreateSensorMutation(): UseMutationResult<
  Sensor,
  Error,
  CreateSensorPayload
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createSensor,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: sensorQueryKeys.byField(data.fieldId) })
      queryClient.invalidateQueries({ queryKey: sensorQueryKeys.fieldSummary(data.fieldId) })
    },
  })
}

export function useSimulateReadingMutation(): UseMutationResult<
  SensorReading,
  Error,
  { sensorId: string; fieldId?: string }
> {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ sensorId }) => simulateSensorReading(sensorId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: sensorQueryKeys.detail(variables.sensorId) })
      if (variables.fieldId) {
        queryClient.invalidateQueries({ queryKey: sensorQueryKeys.fieldSummary(variables.fieldId) })
        queryClient.invalidateQueries({ queryKey: sensorQueryKeys.byField(variables.fieldId) })
      }
    },
  })
}
