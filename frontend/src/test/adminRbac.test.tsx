import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AdminPage } from '@/pages/admin'
import {
  AdminOverviewCards,
  UserManagementTable,
  RolePermissionMatrix,
  AuditLogsTable,
  SystemSettingsView,
} from '@/features/admin/components'
import { UserProfile } from '@/entities/user/types'
import {
  UserSummaryStats,
  RoleDto,
  PermissionCategoryGroup,
  SecurityAuditLogDto,
  SystemSettingsDto,
} from '@/entities/admin'
import * as clientModule from '@/shared/api/client'

// Mutable active auth role for testing
let mockCurrentUser: UserProfile | null = {
  id: 'usr-admin-1',
  firstName: 'Admin',
  lastName: 'Yangiariq',
  fullName: 'Admin Yangiariq',
  phoneNumber: '+998901234500',
  email: 'admin@aqua-ai.uz',
  role: 'ADMIN',
  status: 'ACTIVE',
  createdAt: '2026-11-01T00:00:00Z',
}

vi.mock('@/features/auth/context/AuthContext', () => ({
  useAuth: () => ({
    user: mockCurrentUser,
    isAuthenticated: Boolean(mockCurrentUser),
  }),
}))

const mockStats: UserSummaryStats = {
  totalUsers: 142,
  activeUsers: 135,
  inactiveUsers: 5,
  blockedUsers: 2,
  farmersCount: 110,
  operatorsCount: 20,
  adminsCount: 10,
  superAdminsCount: 2,
}

const mockUsers: UserProfile[] = [
  {
    id: 'usr-super-1',
    firstName: 'Super',
    lastName: 'Admin',
    fullName: 'Super Administrator',
    phoneNumber: '+998901111111',
    email: 'superadmin@aqua-ai.uz',
    role: 'SUPER_ADMIN',
    status: 'ACTIVE',
    createdAt: '2026-10-01T00:00:00Z',
  },
  {
    id: 'usr-admin-1',
    firstName: 'Admin',
    lastName: 'Yangiariq',
    fullName: 'Admin Yangiariq',
    phoneNumber: '+998901234500',
    email: 'admin@aqua-ai.uz',
    role: 'ADMIN',
    status: 'ACTIVE',
    createdAt: '2026-11-01T00:00:00Z',
  },
  {
    id: 'usr-farmer-1',
    firstName: 'Aziz',
    lastName: 'Fayzullayev',
    fullName: 'Aziz Fayzullayev',
    phoneNumber: '+998907777777',
    email: 'farmer@yangiariq.uz',
    role: 'FARMER',
    status: 'ACTIVE',
    createdAt: '2026-11-05T00:00:00Z',
  },
]

const mockRoles: RoleDto[] = [
  {
    id: 'role-1',
    code: 'SUPER_ADMIN',
    name: 'Super Administrator',
    description: 'Full unconstrained platform control',
    isSystem: true,
    permissions: ['USER_CREATE', 'USER_READ', 'USER_ROLE_CHANGE', 'AUDIT_LOG_READ'],
    userCount: 2,
    createdAt: '2026-10-01T00:00:00Z',
    updatedAt: '2026-10-01T00:00:00Z',
  },
  {
    id: 'role-2',
    code: 'ADMIN',
    name: 'District Administrator',
    description: 'Administrative access over district scope',
    isSystem: true,
    permissions: ['USER_CREATE', 'USER_READ', 'AUDIT_LOG_READ'],
    userCount: 10,
    createdAt: '2026-10-01T00:00:00Z',
    updatedAt: '2026-10-01T00:00:00Z',
  },
  {
    id: 'role-3',
    code: 'OPERATOR',
    name: 'Water Operator',
    description: 'Regional water management & canal telemetry',
    isSystem: true,
    permissions: ['FARM_READ', 'SENSOR_READ', 'WATER_DISTRIBUTION_SCHEDULE'],
    userCount: 20,
    createdAt: '2026-10-01T00:00:00Z',
    updatedAt: '2026-10-01T00:00:00Z',
  },
]

const mockPermissionGroups: PermissionCategoryGroup[] = [
  {
    category: 'USER_MANAGEMENT',
    categoryLabel: 'User Management',
    permissions: [
      {
        id: 'p-1',
        code: 'USER_CREATE',
        name: 'Create User',
        description: 'Ability to provision users',
        category: 'USER_MANAGEMENT',
      },
      {
        id: 'p-2',
        code: 'USER_ROLE_CHANGE',
        name: 'Change User Role',
        description: 'Ability to modify roles',
        category: 'USER_MANAGEMENT',
      },
    ],
  },
  {
    category: 'GOVERNANCE_AND_AUDIT',
    categoryLabel: 'Governance & Auditing',
    permissions: [
      {
        id: 'p-3',
        code: 'AUDIT_LOG_READ',
        name: 'Read Audit Logs',
        description: 'Ability to inspect audit trail',
        category: 'GOVERNANCE_AND_AUDIT',
      },
    ],
  },
]

const mockLogs: SecurityAuditLogDto[] = [
  {
    id: 'log-1',
    actorId: 'usr-admin-1',
    actorName: 'admin',
    actorRole: 'ADMIN',
    action: 'USER_CREATED',
    targetType: 'USER',
    targetId: 'usr-farmer-1',
    detailsJson: '{"role":"FARMER"}',
    result: 'SUCCESS',
    ipAddress: '127.0.0.1',
    createdAt: '2026-11-09T08:00:00Z',
  },
  {
    id: 'log-2',
    actorId: 'usr-super-1',
    actorName: 'superadmin',
    actorRole: 'SUPER_ADMIN',
    action: 'USER_ROLE_CHANGED',
    targetType: 'USER',
    targetId: 'usr-admin-1',
    detailsJson: '{"newRole":"ADMIN"}',
    result: 'SUCCESS',
    ipAddress: '192.168.1.10',
    createdAt: '2026-11-09T09:00:00Z',
  },
]

const mockSettings: SystemSettingsDto = {
  applicationName: 'Yangiariq Aqua AI',
  environment: 'production',
  version: '08.20.1',
  accessTokenExpirationMinutes: 1440,
  refreshTokenExpirationDays: 7,
  activeSensorProvider: 'SIMULATED_PROVIDER',
  weatherProvider: 'OPEN_METEO_WEATHER',
  rateLimitingEnabled: true,
  rateLimitPerMinute: 60,
  fourEyesPrincipleStrict: true,
  physicalActuationProhibited: true,
  serverTime: '2026-11-09T10:00:00Z',
}

describe('Admin Management & Centralized RBAC Frontend Tests', () => {
  let queryClient: QueryClient

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    })
    vi.clearAllMocks()

    // Default mock API router
    vi.spyOn(clientModule, 'apiClient').mockImplementation(async (endpoint: string) => {
      if (endpoint === '/users/summary') {
        return { data: mockStats, status: 200 } as any
      }
      if (endpoint.startsWith('/users')) {
        return {
          data: {
            content: mockUsers,
            page: 0,
            size: 10,
            totalElements: mockUsers.length,
            totalPages: 1,
            last: true,
          },
          status: 200,
        } as any
      }
      if (endpoint === '/admin/roles') {
        return { data: mockRoles, status: 200 } as any
      }
      if (endpoint === '/admin/roles/permissions/grouped') {
        return { data: mockPermissionGroups, status: 200 } as any
      }
      if (endpoint.startsWith('/admin/audit-logs')) {
        return {
          data: {
            content: mockLogs,
            page: 0,
            size: 15,
            totalElements: mockLogs.length,
            totalPages: 1,
            last: true,
          },
          status: 200,
        } as any
      }
      if (endpoint === '/admin/settings') {
        return { data: mockSettings, status: 200 } as any
      }
      return { data: null, status: 200 } as any
    })
  })

  it('scenario 1: non-admin (FARMER) is denied access with 403 screen', () => {
    mockCurrentUser = {
      id: 'usr-farmer-1',
      firstName: 'Aziz',
      lastName: 'Fayzullayev',
      fullName: 'Aziz Fayzullayev',
      phoneNumber: '+998907777777',
      role: 'FARMER',
      status: 'ACTIVE',
      createdAt: '2026-11-05T00:00:00Z',
    }

    render(
      <QueryClientProvider client={queryClient}>
        <AdminPage lang="en" />
      </QueryClientProvider>
    )

    expect(screen.getByText(/Access Restricted \(403\)/i)).toBeInTheDocument()
    expect(screen.getByText(/restricted to platform administrators/i)).toBeInTheDocument()
  })

  it('scenario 2: non-admin (OPERATOR) is denied access with 403 screen', () => {
    mockCurrentUser = {
      id: 'usr-op-1',
      firstName: 'Operator',
      lastName: 'Yangiariq',
      fullName: 'Operator Yangiariq',
      phoneNumber: '+998908888888',
      role: 'OPERATOR',
      status: 'ACTIVE',
      createdAt: '2026-11-05T00:00:00Z',
    }

    render(
      <QueryClientProvider client={queryClient}>
        <AdminPage lang="en" />
      </QueryClientProvider>
    )

    expect(screen.getByText(/Access Restricted \(403\)/i)).toBeInTheDocument()
  })

  it('scenario 3: ADMIN can access AdminPage and view overview KPIs & tabs', async () => {
    mockCurrentUser = {
      id: 'usr-admin-1',
      firstName: 'Admin',
      lastName: 'Yangiariq',
      fullName: 'Admin Yangiariq',
      phoneNumber: '+998901234500',
      role: 'ADMIN',
      status: 'ACTIVE',
      createdAt: '2026-11-01T00:00:00Z',
    }

    render(
      <QueryClientProvider client={queryClient}>
        <AdminPage lang="en" />
      </QueryClientProvider>
    )

    expect(await screen.findByText('System Administration & Security Governance')).toBeInTheDocument()
    expect(screen.getByText('Overview')).toBeInTheDocument()
    expect(screen.getByText('User Management')).toBeInTheDocument()
    expect(screen.getByText('Roles & Permissions')).toBeInTheDocument()
    expect(screen.getByText('Security Audit Logs')).toBeInTheDocument()
    expect(screen.getByText('System Settings')).toBeInTheDocument()
  })

  it('scenario 4: AdminOverviewCards renders genuine summary stats and security indicators', () => {
    render(
      <AdminOverviewCards
        stats={mockStats}
        settings={mockSettings}
        lang="en"
      />
    )

    expect(screen.getByText('142')).toBeInTheDocument() // total users
    expect(screen.getByText('2')).toBeInTheDocument() // super admins
    expect(screen.getByText(/30 Centralized Permissions/i)).toBeInTheDocument()
    expect(screen.getByText(/1440 min/i)).toBeInTheDocument()
    expect(screen.getByText(/production \(v08.20.1\)/i)).toBeInTheDocument()
  })

  it('scenario 5: UserManagementTable enforces anti-privilege escalation (ADMIN cannot modify SUPER_ADMIN)', () => {
    const onAction = vi.fn().mockResolvedValue({})

    render(
      <UserManagementTable
        usersPage={{
          content: mockUsers,
          page: 0,
          size: 10,
          totalElements: mockUsers.length,
          totalPages: 1,
          last: true,
        }}
        currentUser={{
          id: 'usr-admin-1',
          firstName: 'Admin',
          lastName: 'Yangiariq',
          fullName: 'Admin Yangiariq',
          phoneNumber: '+998901234500',
          role: 'ADMIN',
          status: 'ACTIVE',
          createdAt: '2026-11-01T00:00:00Z',
        }}
        lang="en"
        search=""
        setSearch={vi.fn()}
        setSelectedRole={vi.fn()}
        setSelectedStatus={vi.fn()}
        currentPage={0}
        setCurrentPage={vi.fn()}
        onCreateUser={onAction}
        onChangeRole={onAction}
        onChangeStatus={onAction}
      />
    )

    // Super Administrator row must show 'Protected' text instead of action buttons for an ordinary ADMIN
    expect(screen.getByText('Protected')).toBeInTheDocument()
    // Other users (Aziz Fayzullayev) should have actionable buttons
    expect(screen.getAllByText('Change Role').length).toBeGreaterThan(0)
  })

  it('scenario 6: RolePermissionMatrix renders roles and permission catalog', async () => {
    const onSave = vi.fn().mockResolvedValue({})

    render(
      <RolePermissionMatrix
        roles={mockRoles}
        permissionGroups={mockPermissionGroups}
        currentUser={{
          id: 'usr-admin-1',
          firstName: 'Admin',
          lastName: 'Yangiariq',
          fullName: 'Admin Yangiariq',
          phoneNumber: '+998901234500',
          role: 'ADMIN',
          status: 'ACTIVE',
          createdAt: '2026-11-01T00:00:00Z',
        }}
        lang="en"
        onSaveRolePermissions={onSave}
      />
    )

    expect(screen.getAllByText('Super Administrator').length).toBeGreaterThan(0)
    expect(screen.getByText('District Administrator')).toBeInTheDocument()
    expect(screen.getByText('Water Operator')).toBeInTheDocument()
    // View-only notification for ordinary ADMIN
    expect(screen.getByText(/View Only \(Super Admin required to edit\)/i)).toBeInTheDocument()
  })

  it('scenario 7: RolePermissionMatrix allows SUPER_ADMIN to save permissions', async () => {
    const onSave = vi.fn().mockResolvedValue({})

    render(
      <RolePermissionMatrix
        roles={mockRoles}
        permissionGroups={mockPermissionGroups}
        currentUser={{
          id: 'usr-super-1',
          firstName: 'Super',
          lastName: 'Admin',
          fullName: 'Super Administrator',
          phoneNumber: '+998901111111',
          role: 'SUPER_ADMIN',
          status: 'ACTIVE',
          createdAt: '2026-10-01T00:00:00Z',
        }}
        lang="en"
        onSaveRolePermissions={onSave}
      />
    )

    // Save button must be visible for SUPER_ADMIN
    const saveBtn = screen.getByText('Save Permissions')
    expect(saveBtn).toBeInTheDocument()
    fireEvent.click(saveBtn)
    expect(onSave).toHaveBeenCalled()
  })

  it('scenario 8: AuditLogsTable renders audit trail and view details modal', async () => {
    render(
      <AuditLogsTable
        logsPage={{
          content: mockLogs,
          page: 0,
          size: 15,
          totalElements: mockLogs.length,
          totalPages: 1,
          last: true,
        }}
        lang="en"
        selectedAction=""
        setSelectedAction={vi.fn()}
        currentPage={0}
        setCurrentPage={vi.fn()}
      />
    )

    expect(screen.getByText('@admin')).toBeInTheDocument()
    expect(screen.getAllByText('USER_CREATED').length).toBeGreaterThan(0)
    expect(screen.getByText('@superadmin')).toBeInTheDocument()
    expect(screen.getAllByText('USER_ROLE_CHANGED').length).toBeGreaterThan(0)

    // Click "View Details" on first log
    const viewButtons = screen.getAllByText('View Details')
    fireEvent.click(viewButtons[0])

    expect(await screen.findByText('Security Audit Details')).toBeInTheDocument()
    expect(screen.getAllByText('usr-admin-1').length).toBeGreaterThan(0)
  })

  it('scenario 9: SystemSettingsView renders runtime posture and security policies', () => {
    render(
      <SystemSettingsView
        settings={mockSettings}
        lang="en"
      />
    )

    expect(screen.getByText('production')).toBeInTheDocument()
    expect(screen.getByText('08.20.1')).toBeInTheDocument()
    expect(screen.getByText('1440 min')).toBeInTheDocument()
    expect(screen.getByText('OPEN_METEO_WEATHER')).toBeInTheDocument()
    expect(screen.getByText(/Zero Trust Default-Deny/i)).toBeInTheDocument()
    expect(screen.getByText(/Anti-Privilege Escalation/i)).toBeInTheDocument()
    expect(screen.getByText(/Immutable Audit Logging/i)).toBeInTheDocument()
  })
})
