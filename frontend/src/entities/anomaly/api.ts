import { apiClient } from '@/shared/api/client'
import {
  Anomaly,
  AnomalyFilterParams,
  AnomalyPage,
  AnomalyRuleConfig,
  AnomalySummary,
  FieldAnomalySummary,
  UpdateAnomalyRulePayload,
} from './types'

export async function listAnomalies(params?: AnomalyFilterParams): Promise<AnomalyPage> {
  const query = new URLSearchParams()
  if (params?.fieldId) query.set('fieldId', params.fieldId)
  if (params?.sensorId) query.set('sensorId', params.sensorId)
  if (params?.status) query.set('status', params.status)
  if (params?.severity) query.set('severity', params.severity)
  if (params?.anomalyType) query.set('anomalyType', params.anomalyType)
  if (params?.sensorType) query.set('sensorType', params.sensorType)
  if (params?.from) query.set('from', params.from)
  if (params?.to) query.set('to', params.to)
  if (params?.page !== undefined) query.set('page', String(params.page))
  if (params?.size !== undefined) query.set('size', String(params.size))

  const qs = query.toString()
  const endpoint = qs ? `/anomalies?${qs}` : '/anomalies'
  const response = await apiClient<AnomalyPage>(endpoint, { method: 'GET' })
  if (!response.data) throw new Error('Anomalies data not returned by server')
  return response.data
}

export async function getAnomaly(id: string): Promise<Anomaly> {
  const response = await apiClient<Anomaly>(`/anomalies/${id}`, { method: 'GET' })
  if (!response.data) throw new Error('Anomaly not found')
  return response.data
}

export async function listFieldAnomalies(fieldId: string, page = 0, size = 20): Promise<AnomalyPage> {
  const response = await apiClient<AnomalyPage>(`/fields/${fieldId}/anomalies?page=${page}&size=${size}`, {
    method: 'GET',
  })
  if (!response.data) throw new Error('Field anomalies not returned by server')
  return response.data
}

export async function listSensorAnomalies(sensorId: string, page = 0, size = 20): Promise<AnomalyPage> {
  const response = await apiClient<AnomalyPage>(`/sensors/${sensorId}/anomalies?page=${page}&size=${size}`, {
    method: 'GET',
  })
  if (!response.data) throw new Error('Sensor anomalies not returned by server')
  return response.data
}

export async function acknowledgeAnomaly(id: string, notes?: string): Promise<Anomaly> {
  const response = await apiClient<Anomaly>(`/anomalies/${id}/acknowledge`, {
    method: 'PATCH',
    body: JSON.stringify({ notes }),
  })
  if (!response.data) throw new Error('Failed to acknowledge anomaly')
  return response.data
}

export async function investigateAnomaly(id: string, notes?: string): Promise<Anomaly> {
  const response = await apiClient<Anomaly>(`/anomalies/${id}/investigate`, {
    method: 'PATCH',
    body: JSON.stringify({ notes }),
  })
  if (!response.data) throw new Error('Failed to investigate anomaly')
  return response.data
}

export async function resolveAnomaly(id: string, notes: string): Promise<Anomaly> {
  const response = await apiClient<Anomaly>(`/anomalies/${id}/resolve`, {
    method: 'PATCH',
    body: JSON.stringify({ notes }),
  })
  if (!response.data) throw new Error('Failed to resolve anomaly')
  return response.data
}

export async function dismissAnomaly(id: string, notes: string): Promise<Anomaly> {
  const response = await apiClient<Anomaly>(`/anomalies/${id}/dismiss`, {
    method: 'PATCH',
    body: JSON.stringify({ notes }),
  })
  if (!response.data) throw new Error('Failed to dismiss anomaly')
  return response.data
}

export async function getAnomalySummary(): Promise<AnomalySummary> {
  const response = await apiClient<AnomalySummary>('/anomalies/summary', { method: 'GET' })
  if (!response.data) throw new Error('Anomaly summary not returned by server')
  return response.data
}

export async function getFieldAnomalySummary(fieldId: string): Promise<FieldAnomalySummary> {
  const response = await apiClient<FieldAnomalySummary>(`/fields/${fieldId}/anomalies/summary`, {
    method: 'GET',
  })
  if (!response.data) throw new Error('Field anomaly summary not returned by server')
  return response.data
}

export async function listAnomalyRules(): Promise<AnomalyRuleConfig[]> {
  const response = await apiClient<AnomalyRuleConfig[]>('/anomaly-rules', { method: 'GET' })
  if (!response.data) throw new Error('Anomaly rules not returned by server')
  return response.data
}

export async function getAnomalyRule(id: string): Promise<AnomalyRuleConfig> {
  const response = await apiClient<AnomalyRuleConfig>(`/anomaly-rules/${id}`, { method: 'GET' })
  if (!response.data) throw new Error('Anomaly rule not found')
  return response.data
}

export async function updateAnomalyRule(id: string, payload: UpdateAnomalyRulePayload): Promise<AnomalyRuleConfig> {
  const response = await apiClient<AnomalyRuleConfig>(`/anomaly-rules/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error('Failed to update anomaly rule')
  return response.data
}

export async function toggleAnomalyRule(id: string, enabled: boolean): Promise<AnomalyRuleConfig> {
  const response = await apiClient<AnomalyRuleConfig>(`/anomaly-rules/${id}/toggle?enabled=${enabled}`, {
    method: 'PATCH',
  })
  if (!response.data) throw new Error('Failed to toggle anomaly rule')
  return response.data
}
