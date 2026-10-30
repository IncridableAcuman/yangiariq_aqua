export type UserRole = 'FARMER' | 'OPERATOR' | 'ADMIN'

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BLOCKED'

export interface UserProfile {
  id: string
  firstName: string
  lastName: string
  fullName: string
  phoneNumber: string
  email?: string | null
  role: UserRole
  status: UserStatus
  district?: string | null
  createdAt: string
}

export interface UpdateUserProfilePayload {
  firstName: string
  lastName: string
  email?: string | null
  district?: string | null
}
