/**
 * Strongly typed frontend models for telemetry anomaly detection & intelligent alerting.
 * Aligned with backend bounded context com.aquaai.anomaly.
 */

import { SensorType } from '@/entities/sensor'

export type AnomalyType =
  | 'SOIL_MOISTURE_DROP'
  | 'SOIL_MOISTURE_CRITICAL'
  | 'WATER_FLOW_SPIKE'
  | 'WATER_FLOW_ZERO'
  | 'WATER_PRESSURE_DROP'
  | 'WATER_LEVEL_DROP'
  | 'SALINITY_INCREASE'
  | 'TEMPERATURE_ANOMALY'
  | 'HUMIDITY_ANOMALY'
  | 'RAINFALL_ANOMALY'
  | 'SENSOR_OFFLINE'
  | 'POSSIBLE_SENSOR_DRIFT'
  | 'SENSOR_STUCK'
  | 'SENSOR_OUTLIER'
  | 'IRRIGATION_FAILURE'
  | 'SUSPECTED_PIPE_BURST'

export type AnomalySeverity =
  | 'INFO'
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH'
  | 'CRITICAL'

export type AnomalyStatus =
  | 'DETECTED'
  | 'ACKNOWLEDGED'
  | 'INVESTIGATING'
  | 'RESOLVED'
  | 'DISMISSED'

export interface AnomalyEvidence {
  id: string
  evidenceType: string
  metric: string
  observedValue?: number | null
  expectedValue?: number | null
  measuredAt: string
  metadata?: string | null
}

export interface Anomaly {
  id: string
  fieldId: string
  sensorId?: string | null
  sensorType: SensorType
  anomalyType: AnomalyType
  severity: AnomalySeverity
  status: AnomalyStatus
  detectedValue?: number | null
  expectedValue?: number | null
  deviation?: number | null
  confidence: number
  isSimulated: boolean
  description: string
  explanation: string
  source: string
  observationCount: number
  firstObservedAt: string
  lastObservedAt: string
  detectedAt: string
  acknowledgedAt?: string | null
  acknowledgedBy?: string | null
  resolvedAt?: string | null
  resolvedBy?: string | null
  resolutionNotes?: string | null
  evidence: AnomalyEvidence[]
  createdAt: string
  updatedAt: string
}

export interface AnomalyPage {
  content: Anomaly[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface AnomalySummary {
  totalActive: number
  criticalCount: number
  highCount: number
  mediumCount: number
  lowCount: number
  infoCount: number
  detectedCount: number
  acknowledgedCount: number
  investigatingCount: number
  resolvedCount: number
  dismissedCount: number
}

export interface FieldAnomalySummary {
  fieldId: string
  activeCount: number
  hasCritical: boolean
  hasHigh: boolean
  activeAnomalies: Anomaly[]
}

export interface AnomalyRuleConfig {
  id: string
  ruleCode: string
  sensorType: SensorType
  name: string
  description?: string | null
  enabled: boolean
  severity: AnomalySeverity
  threshold: number
  secondaryThreshold?: number | null
  timeWindowMinutes: number
  minConsecutiveReadings: number
  createdAt: string
  updatedAt: string
}

export interface UpdateAnomalyRulePayload {
  name: string
  description?: string
  severity: AnomalySeverity
  threshold: number
  secondaryThreshold?: number | null
  timeWindowMinutes?: number
  minConsecutiveReadings?: number
}

export interface AnomalyFilterParams {
  fieldId?: string
  sensorId?: string
  status?: AnomalyStatus
  severity?: AnomalySeverity
  anomalyType?: AnomalyType
  sensorType?: SensorType
  from?: string
  to?: string
  page?: number
  size?: number
}
