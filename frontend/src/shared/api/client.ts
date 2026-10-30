export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: {
    code: string
    message: string
    details?: string[]
  }
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

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1'

export async function apiClient<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const token = localStorage.getItem('aqua_auth_token')
  const headers = new Headers(options.headers || {})

  headers.set('Content-Type', 'application/json')
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  })

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({
      message: 'An unexpected network error occurred',
    }))
    throw new Error(errorData.error?.message || `HTTP ${response.status}`)
  }

  return response.json()
}
