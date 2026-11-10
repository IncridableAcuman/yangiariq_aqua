import { authStorage } from '@/features/auth/storage'

export interface ApiErrorPayload {
  code: string
  message: string
  details?: string[]
}

export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: ApiErrorPayload
  timestamp: string
  correlationId?: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export class ApiError extends Error {
  code?: string
  details?: string[]
  status: number

  constructor(status: number, message: string, code?: string, details?: string[]) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.details = details
  }
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://yangiariq-aqua-6hfh.onrender.com' || '/api/v1'

let refreshPromise: Promise<string | null> | null = null

async function requestRefreshToken(): Promise<string | null> {
  const refreshToken = authStorage.getRefreshToken()
  if (!refreshToken) {
    authStorage.clearTokens()
    return null
  }

  try {
    const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
    })

    if (!response.ok) {
      authStorage.clearTokens()
      window.dispatchEvent(new Event('aqua:unauthorized'))
      return null
    }

    const payload: ApiResponse<{ accessToken: string; refreshToken: string }> = await response.json()
    if (payload.success && payload.data) {
      authStorage.setTokens(payload.data.accessToken, payload.data.refreshToken)
      return payload.data.accessToken
    }

    authStorage.clearTokens()
    window.dispatchEvent(new Event('aqua:unauthorized'))
    return null
  } catch {
    authStorage.clearTokens()
    window.dispatchEvent(new Event('aqua:unauthorized'))
    return null
  } finally {
    refreshPromise = null
  }
}

export async function apiClient<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const token = authStorage.getAccessToken()
  const headers = new Headers(options.headers || {})

  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }

  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    })
  } catch (err: unknown) {
    throw new ApiError(0, err instanceof Error ? err.message : 'Network error')
  }

  // Handle 401 Unauthorized for endpoints that allow refresh (i.e. not auth endpoints themselves)
  const isAuthEndpoint = endpoint.startsWith('/auth/login') ||
    endpoint.startsWith('/auth/register') ||
    endpoint.startsWith('/auth/refresh') ||
    endpoint.startsWith('/auth/logout') ||
    endpoint.startsWith('/auth/password-reset')

  if (response.status === 401 && !isAuthEndpoint && authStorage.getRefreshToken()) {
    if (!refreshPromise) {
      refreshPromise = requestRefreshToken()
    }
    const newAccessToken = await refreshPromise

    if (newAccessToken) {
      headers.set('Authorization', `Bearer ${newAccessToken}`)
      response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers,
      })
    }
  }

  if (!response.ok) {
    let errorData: ApiResponse<T> | null = null
    try {
      errorData = await response.json()
    } catch {
      // not JSON
    }

    const message = errorData?.error?.message || `HTTP ${response.status}: Request failed`
    const code = errorData?.error?.code
    const details = errorData?.error?.details

    if (response.status === 401 && !isAuthEndpoint) {
      authStorage.clearTokens()
      window.dispatchEvent(new Event('aqua:unauthorized'))
    }

    throw new ApiError(response.status, message, code, details)
  }

  return response.json()
}
