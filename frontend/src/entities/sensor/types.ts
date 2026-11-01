/**
 * Strongly typed frontend models for agricultural sensors and telemetry observations.
 * Aligned with backend contract com.aquaai.sensor.
 */

export type SensorType =
  | 'SOIL_MOISTURE'
  | 'WATER_FLOW'
  | 'TEMPERATURE'
  | 'HUMIDITY'
  | 'WATER_LEVEL'
  | 'PRESSURE'
  | 'RAINFALL'
  | 'SALINITY'

export type SensorStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'MAINTENANCE'
  | 'OFFLINE'

export type MeasurementUnit =
  | 'PERCENT'
  | 'CUBIC_METER_PER_HOUR'
  | 'LITERS_PER_SECOND'
  | 'CELSIUS'
  | 'METERS'
  | 'MILLIMETERS'
  | 'BAR'
  | 'MILLIBAR'
  | 'DECISIEMENS_PER_METER'

export type ReadingQuality =
  | 'VALID'
  | 'SUSPECT'
  | 'INVALID'

export interface Sensor {
  id: string
  fieldId: string
  name: string
  deviceIdentifier: string
  type: SensorType
  status: SensorStatus
  provider: string
  description?: string | null
  installedAt: string
  lastSeenAt?: string | null
  createdAt: string
  updatedAt: string
  version?: number
}

export interface SensorReading {
  id: string
  sensorId: string
  fieldId: string
  type: SensorType
  value: number
  unit: MeasurementUnit
  unitSymbol: string
  quality: ReadingQuality
  source: string
  measuredAt: string
  createdAt: string
}

export interface SensorReadingPage {
  content: SensorReading[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface FieldTelemetrySummary {
  fieldId: string
  readings: SensorReading[]
  generatedAt: string
}

export interface CreateSensorPayload {
  fieldId: string
  name: string
  deviceIdentifier: string
  type: SensorType
  provider?: string
  description?: string
}

export interface UpdateSensorPayload {
  name?: string
  description?: string
}

export interface RecordReadingPayload {
  value: number
  unit?: MeasurementUnit
  quality?: ReadingQuality
  source?: string
  measuredAt?: string
}

export interface TelemetryAggregate {
  fieldId: string
  sensorId?: string | null
  sensorType: SensorType
  interval: string
  bucketStartTime: string
  bucketEndTime: string
  average?: number | null
  minimum: number
  maximum: number
  sum?: number | null
  firstReadingValue?: number | null
  firstReadingAt?: string | null
  lastReadingValue?: number | null
  lastReadingAt?: string | null
  readingCount: number
  unit: MeasurementUnit
}

export interface DataQualityMetrics {
  totalReadings: number
  validReadings: number
  suspectReadings: number
  invalidReadings: number
  validPercentage: number
  missingIntervals: number
  delayedReadings: number
}

export interface SensorAvailabilitySummary {
  totalSensors: number
  activeCount: number
  inactiveCount: number
  maintenanceCount: number
  offlineCount: number
  communicatingCount: number
  unresponsiveCount: number
}

export interface FieldAnalyticsSummary {
  fieldId: string
  latestReadings: Record<string, SensorReading>
  past24hAggregates: Record<string, TelemetryAggregate>
  availability: SensorAvailabilitySummary
  past24hDataQuality: DataQualityMetrics
  generatedAt: string
}

export interface TelemetryAnalyticsQueryParams {
  sensorType?: SensorType
  interval?: '5m' | '15m' | '1h' | '1d' | '1w' | string
  from?: string
  to?: string
  quality?: ReadingQuality
}
