import { UserRole, UserStatus } from '@/entities/user/types'

export interface UserSummaryStats {
  totalUsers: number
  activeUsers: number
  inactiveUsers: number
  blockedUsers: number
  farmersCount: number
  operatorsCount: number
  adminsCount: number
  superAdminsCount: number
}

export interface AdminCreateUserPayload {
  firstName: string
  lastName: string
  phoneNumber: string
  email?: string
  password: string
  role: UserRole
  status?: UserStatus
  district?: string
  organization?: string
  telegramUsername?: string
  preferredLanguage?: string
}

export interface AdminUpdateUserPayload {
  firstName?: string
  lastName?: string
  phoneNumber?: string
  email?: string
  role?: UserRole
  status?: UserStatus
  district?: string
  organization?: string
  telegramUsername?: string
  preferredLanguage?: string
}

export interface RoleDto {
  id: string
  code: string
  name: string
  description?: string | null
  isSystem: boolean
  permissions: string[]
  userCount: number
  createdAt: string
  updatedAt: string
}

export interface PermissionDto {
  id: string
  code: string
  name: string
  description?: string | null
  category: string
}

export interface PermissionCategoryGroup {
  category: string
  categoryLabel: string
  permissions: PermissionDto[]
}

export interface SecurityAuditLogDto {
  id: string
  actorId?: string | null
  actorName?: string | null
  actorRole?: string | null
  action: string
  targetType: string
  targetId?: string | null
  detailsJson?: string | null
  result: string
  ipAddress?: string | null
  createdAt: string
}

export interface SystemSettingsDto {
  applicationName: string
  environment: string
  version: string
  accessTokenExpirationMinutes: number
  refreshTokenExpirationDays: number
  activeSensorProvider: string
  weatherProvider: string
  rateLimitingEnabled: boolean
  rateLimitPerMinute: number
  fourEyesPrincipleStrict: boolean
  physicalActuationProhibited: boolean
  serverTime: string
}
