import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { apiClient, PageResponse } from '@/shared/api/client'
import { UserProfile, UserRole, UserStatus } from '@/entities/user/types'
import {
  UserSummaryStats,
  AdminCreateUserPayload,
  AdminUpdateUserPayload,
  RoleDto,
  PermissionCategoryGroup,
  SecurityAuditLogDto,
  SystemSettingsDto,
} from '@/entities/admin'

export function useUserSummaryStatsQuery() {
  return useQuery({
    queryKey: ['admin', 'users', 'summary'],
    queryFn: async () => {
      const res = await apiClient<UserSummaryStats>('/users/summary')
      return res.data || null
    },
  })
}

export function useUsersListQuery(
  search: string,
  role?: UserRole,
  status?: UserStatus,
  page: number = 0,
  size: number = 10
) {
  return useQuery({
    queryKey: ['admin', 'users', 'list', search, role, status, page, size],
    queryFn: async () => {
      const params = new URLSearchParams()
      if (search) params.append('search', search)
      if (role) params.append('role', role)
      if (status) params.append('status', status)
      params.append('page', String(page))
      params.append('size', String(size))

      const res = await apiClient<PageResponse<UserProfile>>(`/users?${params.toString()}`)
      return res.data || { content: [], page: 0, size, totalElements: 0, totalPages: 0, last: true }
    },
  })
}

export function useUserDetailQuery(id: string | null) {
  return useQuery({
    queryKey: ['admin', 'users', 'detail', id],
    queryFn: async () => {
      if (!id) return null
      const res = await apiClient<UserProfile>(`/users/${id}`)
      return res.data || null
    },
    enabled: Boolean(id),
  })
}

export function useAdminCreateUserMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (payload: AdminCreateUserPayload) => {
      const res = await apiClient<UserProfile>('/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'audit-logs'] })
    },
  })
}

export function useAdminUpdateUserMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, payload }: { id: string; payload: AdminUpdateUserPayload }) => {
      const res = await apiClient<UserProfile>(`/users/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'audit-logs'] })
    },
  })
}

export function useAdminChangeStatusMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, status, reason }: { id: string; status: UserStatus; reason?: string }) => {
      const res = await apiClient<UserProfile>(`/users/${id}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status, reason }),
      })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'audit-logs'] })
    },
  })
}

export function useAdminChangeRoleMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, role }: { id: string; role: UserRole }) => {
      const res = await apiClient<UserProfile>(`/users/${id}/role`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ role }),
      })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'roles'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'audit-logs'] })
    },
  })
}

export function useAdminDeleteUserMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await apiClient<void>(`/users/${id}`, {
        method: 'DELETE',
      })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'audit-logs'] })
    },
  })
}

export function useRolesListQuery() {
  return useQuery({
    queryKey: ['admin', 'roles', 'list'],
    queryFn: async () => {
      const res = await apiClient<RoleDto[]>('/admin/roles')
      return res.data || []
    },
  })
}

export function usePermissionsGroupedQuery() {
  return useQuery({
    queryKey: ['admin', 'roles', 'permissions', 'grouped'],
    queryFn: async () => {
      const res = await apiClient<PermissionCategoryGroup[]>('/admin/roles/permissions/grouped')
      return res.data || []
    },
  })
}

export function useUpdateRolePermissionsMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({ code, permissions }: { code: string; permissions: string[] }) => {
      const res = await apiClient<RoleDto>(`/admin/roles/${code}/permissions`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ permissions }),
      })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'roles'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'audit-logs'] })
    },
  })
}

export function useCreateRoleMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (payload: { code: string; name: string; description?: string; permissions: string[] }) => {
      const res = await apiClient<RoleDto>('/admin/roles', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      return res.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'roles'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'audit-logs'] })
    },
  })
}

export function useDeleteRoleMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (code: string) => {
      await apiClient<void>(`/admin/roles/${code}`, {
        method: 'DELETE',
      })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'roles'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'audit-logs'] })
    },
  })
}

export function useSecurityAuditLogsQuery(
  action?: string,
  targetType?: string,
  result?: string,
  page: number = 0,
  size: number = 20
) {
  return useQuery({
    queryKey: ['admin', 'audit-logs', action, targetType, result, page, size],
    queryFn: async () => {
      const params = new URLSearchParams()
      if (action) params.append('action', action)
      if (targetType) params.append('targetType', targetType)
      if (result) params.append('result', result)
      params.append('page', String(page))
      params.append('size', String(size))

      const res = await apiClient<PageResponse<SecurityAuditLogDto>>(`/admin/audit-logs?${params.toString()}`)
      return res.data || { content: [], page: 0, size, totalElements: 0, totalPages: 0, last: true }
    },
  })
}

export function useSystemSettingsQuery() {
  return useQuery({
    queryKey: ['admin', 'settings'],
    queryFn: async () => {
      const res = await apiClient<SystemSettingsDto>('/admin/settings')
      return res.data || null
    },
  })
}
