import { apiClient } from '@/shared/api/client'
import {
  Sensor,
  SensorReading,
  SensorReadingPage,
  FieldTelemetrySummary,
  CreateSensorPayload,
  UpdateSensorPayload,
  RecordReadingPayload,
  TelemetryAggregate,
  FieldAnalyticsSummary,
  TelemetryAnalyticsQueryParams,
} from './types'

export interface SensorReadingQueryParams {
  from?: string
  to?: string
  page?: number
  size?: number
  sort?: string
}

/**
 * Register a new sensor device assigned to a parcel.
 */
export async function createSensor(payload: CreateSensorPayload): Promise<Sensor> {
  const response = await apiClient<Sensor>('/sensors', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error('Sensor data not returned by server')
  return response.data
}

/**
 * Retrieve sensor metadata by ID.
 */
export async function getSensor(id: string): Promise<Sensor> {
  const response = await apiClient<Sensor>(`/sensors/${id}`, { method: 'GET' })
  if (!response.data) throw new Error('Sensor not found')
  return response.data
}

/**
 * Update mutable metadata for a sensor.
 */
export async function updateSensor(id: string, payload: UpdateSensorPayload): Promise<Sensor> {
  const response = await apiClient<Sensor>(`/sensors/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error('Sensor data not returned by server')
  return response.data
}

/**
 * Activate a sensor.
 */
export async function activateSensor(id: string): Promise<Sensor> {
  const response = await apiClient<Sensor>(`/sensors/${id}/activate`, { method: 'PATCH' })
  if (!response.data) throw new Error('Sensor data not returned by server')
  return response.data
}

/**
 * Deactivate a sensor.
 */
export async function deactivateSensor(id: string): Promise<Sensor> {
  const response = await apiClient<Sensor>(`/sensors/${id}/deactivate`, { method: 'PATCH' })
  if (!response.data) throw new Error('Sensor data not returned by server')
  return response.data
}

/**
 * Put a sensor under maintenance.
 */
export async function setSensorMaintenance(id: string): Promise<Sensor> {
  const response = await apiClient<Sensor>(`/sensors/${id}/maintenance`, { method: 'PATCH' })
  if (!response.data) throw new Error('Sensor data not returned by server')
  return response.data
}

/**
 * List all sensors deployed on a field.
 */
export async function getFieldSensors(fieldId: string): Promise<Sensor[]> {
  const response = await apiClient<Sensor[]>(`/fields/${fieldId}/sensors`, { method: 'GET' })
  return response.data || []
}

/**
 * Record an instantaneous telemetry reading for a sensor.
 */
export async function recordSensorReading(
  sensorId: string,
  payload: RecordReadingPayload
): Promise<SensorReading> {
  const response = await apiClient<SensorReading>(`/sensors/${sensorId}/readings`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  if (!response.data) throw new Error('Reading data not returned by server')
  return response.data
}

/**
 * Retrieve the latest telemetry observation for a sensor.
 */
export async function getLatestSensorReading(sensorId: string): Promise<SensorReading> {
  const response = await apiClient<SensorReading>(`/sensors/${sensorId}/latest-reading`, {
    method: 'GET',
  })
  if (!response.data) throw new Error('Latest reading not found')
  return response.data
}

/**
 * Query historical telemetry observations for a sensor.
 */
export async function getSensorReadings(
  sensorId: string,
  params: SensorReadingQueryParams = {}
): Promise<SensorReadingPage> {
  const searchParams = new URLSearchParams()
  if (params.from) searchParams.set('from', params.from)
  if (params.to) searchParams.set('to', params.to)
  if (typeof params.page === 'number') searchParams.set('page', params.page.toString())
  if (typeof params.size === 'number') searchParams.set('size', params.size.toString())
  if (params.sort) searchParams.set('sort', params.sort)

  const qs = searchParams.toString()
  const endpoint = qs ? `/sensors/${sensorId}/readings?${qs}` : `/sensors/${sensorId}/readings`

  const response = await apiClient<SensorReadingPage>(endpoint, { method: 'GET' })
  if (!response.data) throw new Error('Readings data not returned by server')
  return response.data
}

/**
 * Query historical telemetry observations across all sensors on a field parcel.
 */
export async function getFieldReadings(
  fieldId: string,
  params: SensorReadingQueryParams = {}
): Promise<SensorReadingPage> {
  const searchParams = new URLSearchParams()
  if (params.from) searchParams.set('from', params.from)
  if (params.to) searchParams.set('to', params.to)
  if (typeof params.page === 'number') searchParams.set('page', params.page.toString())
  if (typeof params.size === 'number') searchParams.set('size', params.size.toString())
  if (params.sort) searchParams.set('sort', params.sort)

  const qs = searchParams.toString()
  const endpoint = qs ? `/fields/${fieldId}/readings?${qs}` : `/fields/${fieldId}/readings`

  const response = await apiClient<SensorReadingPage>(endpoint, { method: 'GET' })
  if (!response.data) throw new Error('Field readings data not returned by server')
  return response.data
}

/**
 * Get latest telemetry summary per sensor type on a field.
 */
export async function getFieldTelemetrySummary(fieldId: string): Promise<FieldTelemetrySummary> {
  const response = await apiClient<FieldTelemetrySummary>(
    `/fields/${fieldId}/readings/latest-summary`,
    { method: 'GET' }
  )
  if (!response.data) throw new Error('Telemetry summary not returned by server')
  return response.data
}

/**
 * Trigger an instantaneous on-demand simulation reading for demo or testing.
 */
export async function simulateSensorReading(sensorId: string): Promise<SensorReading> {
  const response = await apiClient<SensorReading>(`/sensors/${sensorId}/simulate`, {
    method: 'POST',
  })
  if (!response.data) throw new Error('Simulated reading not returned by server')
  return response.data
}

/**
 * Get aggregated time-series telemetry for a specific sensor.
 */
export async function getSensorAnalytics(
  sensorId: string,
  params: TelemetryAnalyticsQueryParams = {}
): Promise<TelemetryAggregate[]> {
  const searchParams = new URLSearchParams()
  if (params.from) searchParams.set('from', params.from)
  if (params.to) searchParams.set('to', params.to)
  if (params.interval) searchParams.set('interval', params.interval)
  if (params.quality) searchParams.set('quality', params.quality)

  const qs = searchParams.toString()
  const endpoint = qs ? `/sensors/${sensorId}/analytics?${qs}` : `/sensors/${sensorId}/analytics`

  const response = await apiClient<TelemetryAggregate[]>(endpoint, { method: 'GET' })
  return response.data || []
}

/**
 * Get aggregated time-series telemetry across sensors on a field.
 */
export async function getFieldAnalytics(
  fieldId: string,
  params: TelemetryAnalyticsQueryParams = {}
): Promise<TelemetryAggregate[]> {
  const searchParams = new URLSearchParams()
  if (params.sensorType) searchParams.set('sensorType', params.sensorType)
  if (params.from) searchParams.set('from', params.from)
  if (params.to) searchParams.set('to', params.to)
  if (params.interval) searchParams.set('interval', params.interval)
  if (params.quality) searchParams.set('quality', params.quality)

  const qs = searchParams.toString()
  const endpoint = qs ? `/fields/${fieldId}/analytics?${qs}` : `/fields/${fieldId}/analytics`

  const response = await apiClient<TelemetryAggregate[]>(endpoint, { method: 'GET' })
  return response.data || []
}

/**
 * Get executive operational analytics summary for a field parcel.
 */
export async function getFieldAnalyticsSummary(fieldId: string): Promise<FieldAnalyticsSummary> {
  const response = await apiClient<FieldAnalyticsSummary>(
    `/fields/${fieldId}/analytics/summary`,
    { method: 'GET' }
  )
  if (!response.data) throw new Error('Analytics summary not returned by server')
  return response.data
}

