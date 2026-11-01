import { apiClient } from '@/shared/api/client'
import {
  AgronomicAssessment,
  IntelligenceEvaluationResult,
  TelemetrySignal,
} from './types'

export async function evaluateFieldIntelligence(fieldId: string): Promise<IntelligenceEvaluationResult> {
  const response = await apiClient<IntelligenceEvaluationResult>(
    `/fields/${fieldId}/intelligence/evaluate`,
    { method: 'POST' }
  )
  if (!response.data) throw new Error('Intelligence evaluation data not returned by server')
  return response.data
}

export async function getFieldAgronomicAssessment(fieldId: string): Promise<AgronomicAssessment> {
  const response = await apiClient<AgronomicAssessment>(
    `/fields/${fieldId}/intelligence/assessment`,
    { method: 'GET' }
  )
  if (!response.data) throw new Error('Agronomic assessment data not returned by server')
  return response.data
}

export async function getFieldTelemetrySignals(fieldId: string): Promise<TelemetrySignal[]> {
  const response = await apiClient<TelemetrySignal[]>(
    `/fields/${fieldId}/intelligence/signals`,
    { method: 'GET' }
  )
  if (!response.data) throw new Error('Telemetry signals not returned by server')
  return response.data
}
