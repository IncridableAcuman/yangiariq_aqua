import { apiClient } from '@/shared/api/client'
import {
  AuthResponse,
  LoginCredentials,
  RegisterCredentials,
  ChangePasswordPayload,
  PasswordResetRequestPayload,
  PasswordResetConfirmPayload,
} from './types'

export async function loginUser(credentials: LoginCredentials): Promise<AuthResponse> {
  const response = await apiClient<AuthResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  })
  if (!response.data) {
    throw new Error('Authentication failed - no data returned')
  }
  return response.data
}

export async function registerUser(credentials: RegisterCredentials): Promise<AuthResponse> {
  const response = await apiClient<AuthResponse>('/auth/register', {
    method: 'POST',
    body: JSON.stringify(credentials),
  })
  if (!response.data) {
    throw new Error('Registration failed - no data returned')
  }
  return response.data
}

export async function refreshAccessToken(refreshToken: string): Promise<AuthResponse> {
  const response = await apiClient<AuthResponse>('/auth/refresh', {
    method: 'POST',
    body: JSON.stringify({ refreshToken }),
  })
  if (!response.data) {
    throw new Error('Token refresh failed')
  }
  return response.data
}

export async function logoutUser(refreshToken?: string | null): Promise<void> {
  try {
    await apiClient<void>('/auth/logout', {
      method: 'POST',
      body: JSON.stringify({ refreshToken: refreshToken || null }),
    })
  } catch {
    // Session may already be expired or invalid on the backend; ignore network or 401 error
  }
}

export async function changeUserPassword(payload: ChangePasswordPayload): Promise<void> {
  await apiClient<void>('/auth/change-password', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function requestPasswordReset(payload: PasswordResetRequestPayload): Promise<void> {
  await apiClient<void>('/auth/password-reset/request', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function confirmPasswordReset(payload: PasswordResetConfirmPayload): Promise<void> {
  await apiClient<void>('/auth/password-reset/confirm', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}
