import { apiClient } from '@/shared/api/client'
import { UserProfile, UpdateUserProfilePayload } from './types'

export async function getCurrentUserProfile(): Promise<UserProfile> {
  const response = await apiClient<UserProfile>('/user/profile', {
    method: 'GET',
  })
  if (!response.data) {
    throw new Error('User profile data not returned by server')
  }
  return response.data
}

export async function updateCurrentUserProfile(
  payload: UpdateUserProfilePayload
): Promise<UserProfile> {
  const response = await apiClient<UserProfile>('/user/profile', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
  if (!response.data) {
    throw new Error('Updated profile data not returned by server')
  }
  return response.data
}
