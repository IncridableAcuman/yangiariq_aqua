import { apiClient } from '@/shared/api/client'
import {
  Recommendation,
  RecommendationFilterParams,
  RecommendationPage,
  RecommendationSummary,
} from './types'

export async function listRecommendations(params?: RecommendationFilterParams): Promise<RecommendationPage> {
  const query = new URLSearchParams()
  if (params?.fieldId) query.set('fieldId', params.fieldId)
  if (params?.status) query.set('status', params.status)
  if (params?.priority) query.set('priority', params.priority)
  if (params?.type) query.set('type', params.type)
  if (params?.from) query.set('from', params.from)
  if (params?.to) query.set('to', params.to)
  if (params?.page !== undefined) query.set('page', String(params.page))
  if (params?.size !== undefined) query.set('size', String(params.size))

  const qs = query.toString()
  const endpoint = qs ? `/recommendations?${qs}` : '/recommendations'
  const response = await apiClient<RecommendationPage>(endpoint, { method: 'GET' })
  if (!response.data) throw new Error('Recommendations data not returned by server')
  return response.data
}

export async function getRecommendation(id: string): Promise<Recommendation> {
  const response = await apiClient<Recommendation>(`/recommendations/${id}`, { method: 'GET' })
  if (!response.data) throw new Error('Recommendation not found')
  return response.data
}

export async function listFieldRecommendations(fieldId: string, page = 0, size = 20): Promise<RecommendationPage> {
  const response = await apiClient<RecommendationPage>(`/fields/${fieldId}/recommendations?page=${page}&size=${size}`, {
    method: 'GET',
  })
  if (!response.data) throw new Error('Field recommendations not returned by server')
  return response.data
}

export async function generateRecommendation(fieldId: string): Promise<Recommendation> {
  const response = await apiClient<Recommendation>(`/recommendations/generate?fieldId=${fieldId}`, {
    method: 'POST',
  })
  if (!response.data) throw new Error('Failed to generate recommendation')
  return response.data
}

export async function markRecommendationViewed(id: string): Promise<Recommendation> {
  const response = await apiClient<Recommendation>(`/recommendations/${id}/view`, {
    method: 'PATCH',
  })
  if (!response.data) throw new Error('Failed to mark recommendation as viewed')
  return response.data
}

export async function acceptRecommendation(id: string): Promise<Recommendation> {
  const response = await apiClient<Recommendation>(`/recommendations/${id}/accept`, {
    method: 'PATCH',
  })
  if (!response.data) throw new Error('Failed to accept recommendation')
  return response.data
}

export async function dismissRecommendation(id: string, reason: string): Promise<Recommendation> {
  const response = await apiClient<Recommendation>(`/recommendations/${id}/dismiss`, {
    method: 'PATCH',
    body: JSON.stringify({ reason }),
  })
  if (!response.data) throw new Error('Failed to dismiss recommendation')
  return response.data
}

export async function completeRecommendation(id: string): Promise<Recommendation> {
  const response = await apiClient<Recommendation>(`/recommendations/${id}/complete`, {
    method: 'PATCH',
  })
  if (!response.data) throw new Error('Failed to complete recommendation')
  return response.data
}

export async function getRecommendationSummary(fieldId?: string): Promise<RecommendationSummary> {
  const endpoint = fieldId ? `/recommendations/summary?fieldId=${fieldId}` : '/recommendations/summary'
  const response = await apiClient<RecommendationSummary>(endpoint, { method: 'GET' })
  if (!response.data) throw new Error('Recommendation summary not returned by server')
  return response.data
}

export async function getFieldRecommendationSummary(fieldId: string): Promise<RecommendationSummary> {
  const response = await apiClient<RecommendationSummary>(`/fields/${fieldId}/recommendations/summary`, {
    method: 'GET',
  })
  if (!response.data) throw new Error('Field recommendation summary not returned by server')
  return response.data
}
