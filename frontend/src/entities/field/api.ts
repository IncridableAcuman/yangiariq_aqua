import { apiClient, PageResponse } from '@/shared/api/client'
import {
  Field,
  FieldQueryParams,
  CreateFieldPayload,
  UpdateFieldPayload,
} from './types'

export async function getFields(
  params: FieldQueryParams = {}
): Promise<PageResponse<Field>> {
  const searchParams = new URLSearchParams()

  if (params.status) {
    searchParams.set('status', params.status)
  }
  if (params.cropType) {
    searchParams.set('cropType', params.cropType)
  }
  if (params.soilType) {
    searchParams.set('soilType', params.soilType)
  }
  if (params.irrigationMethod) {
    searchParams.set('irrigationMethod', params.irrigationMethod)
  }
  if (params.ownerId) {
    searchParams.set('ownerId', params.ownerId)
  }
  if (typeof params.page === 'number') {
    searchParams.set('page', params.page.toString())
  }
  if (typeof params.size === 'number') {
    searchParams.set('size', params.size.toString())
  }
  if (params.sort) {
    searchParams.set('sort', params.sort)
  } else if (params.sortBy) {
    const direction = params.direction || 'desc'
    searchParams.set('sort', `${params.sortBy},${direction}`)
  }

  const queryString = searchParams.toString()
  const endpoint = queryString ? `/fields?${queryString}` : '/fields'

  const response = await apiClient<PageResponse<Field>>(endpoint, {
    method: 'GET',
  })

  if (!response.data) {
    throw new Error('Fields data not returned by server')
  }

  return response.data
}

export async function getFieldById(fieldId: string): Promise<Field> {
  const response = await apiClient<Field>(`/fields/${fieldId}`, {
    method: 'GET',
  })

  if (!response.data) {
    throw new Error(`Field ${fieldId} not returned by server`)
  }

  return response.data
}

export async function createField(payload: CreateFieldPayload): Promise<Field> {
  const response = await apiClient<Field>('/fields', {
    method: 'POST',
    body: JSON.stringify(payload),
  })

  if (!response.data) {
    throw new Error('Created field data not returned by server')
  }

  return response.data
}

export async function updateField(
  fieldId: string,
  payload: UpdateFieldPayload
): Promise<Field> {
  const response = await apiClient<Field>(`/fields/${fieldId}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })

  if (!response.data) {
    throw new Error(`Updated field ${fieldId} not returned by server`)
  }

  return response.data
}

export async function deactivateField(fieldId: string): Promise<void> {
  await apiClient<void>(`/fields/${fieldId}`, {
    method: 'DELETE',
  })
}

export async function reactivateField(fieldId: string): Promise<Field> {
  const response = await apiClient<Field>(`/fields/${fieldId}/activate`, {
    method: 'PATCH',
  })

  if (!response.data) {
    throw new Error(`Reactivated field ${fieldId} not returned by server`)
  }

  return response.data
}
