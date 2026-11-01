/**
 * Strongly typed models for Telemetry Intelligence & Deterministic Agronomic Recommendations.
 * Aligned with backend bounded context com.aquaai.intelligence.
 */

import { SensorType } from '@/entities/sensor'

export type TelemetryFreshness = 'FRESH' | 'STALE' | 'MISSING'

export type TrendDirection = 'INCREASING' | 'DECREASING' | 'STABLE'

export type AgronomicSignalType =
  | 'SOIL_TOO_DRY'
  | 'SOIL_TOO_WET'
  | 'HIGH_TEMPERATURE'
  | 'LOW_TEMPERATURE'
  | 'HIGH_HUMIDITY'
  | 'LOW_HUMIDITY'
  | 'LOW_WATER_LEVEL'
  | 'ABNORMAL_FLOW'
  | 'PRESSURE_DROP'
  | 'NO_RECENT_TELEMETRY'

export type IntelligenceSeverity =
  | 'INFO'
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH'
  | 'CRITICAL'

export type IntelligenceConfidence = 'HIGH' | 'MEDIUM' | 'LOW'

export interface AgronomicSignal {
  id: string
  signalType: AgronomicSignalType
  severity: IntelligenceSeverity
  metric: string
  observedValue?: number | null
  thresholdValue?: number | null
  message: string
}

export interface TelemetryTrend {
  direction: TrendDirection
  rateOfChangePerHour: number
  dataPointsEvaluated: number
  windowStart?: string | null
  windowEnd?: string | null
}

export interface TelemetrySignal {
  fieldId: string
  sensorId?: string | null
  type: SensorType
  value: number
  unit: string
  quality: 'VALID' | 'SUSPECT' | 'INVALID'
  measuredAt: string
  freshness: TelemetryFreshness
  isAuthoritative: boolean
}

export interface AgronomicAssessment {
  id: string
  fieldId: string
  primarySignal: AgronomicSignalType
  overallSeverity: IntelligenceSeverity
  soilMoistureTrend: TelemetryTrend
  telemetryFreshness: TelemetryFreshness
  confidence: IntelligenceConfidence
  confidenceScore: number
  agronomicSummary: string
  recommendationsOverview?: string | null
  hydraulicSafetyBlocked: boolean
  hydraulicSafetyReason?: string | null
  waterBudgetConstrained: boolean
  waterBudgetReason?: string | null
  assessedAt: string
  signals: AgronomicSignal[]
}

export interface IntelligenceRecommendation {
  id: string
  fieldId: string
  recommendationType: string
  priority: IntelligenceSeverity
  reason: string
  recommendedVolumeM3: number
  recommendedDurationMinutes: number
  recommendedWindowStart?: string | null
  recommendedWindowEnd?: string | null
  confidence: IntelligenceConfidence
  constraints: string[]
  supportingSignals: AgronomicSignalType[]
  status: string
  generatedAt: string
}

export interface IntelligenceEvaluationResult {
  assessment: AgronomicAssessment
  recommendation: IntelligenceRecommendation
}
