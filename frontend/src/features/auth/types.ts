import { UserProfile } from '@/entities/user/types'

export interface AuthTokens {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresInSeconds: number
}

export interface AuthResponse extends AuthTokens {
  user: UserProfile
}

export interface LoginCredentials {
  identifier: string
  password: string
}

export interface RegisterCredentials {
  firstName: string
  lastName: string
  phoneNumber: string
  email?: string
  password: string
  district?: string
}

export interface ChangePasswordPayload {
  currentPassword: string
  newPassword: string
}

export interface PasswordResetRequestPayload {
  identifier: string
}

export interface PasswordResetConfirmPayload {
  token: string
  newPassword: string
}
