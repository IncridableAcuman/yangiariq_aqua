import React, { useState } from 'react'
import {
  ShieldAlert,
  Shield,
  Users,
  KeyRound,
  FileText,
  Sliders,
} from 'lucide-react'
import { SupportedLang, translations } from '@/shared/i18n'
import { useAuth } from '@/features/auth/context/AuthContext'
import { UserRole, UserStatus } from '@/entities/user/types'
import {
  useUserSummaryStatsQuery,
  useUsersListQuery,
  useAdminCreateUserMutation,
  useAdminChangeRoleMutation,
  useAdminChangeStatusMutation,
  useRolesListQuery,
  usePermissionsGroupedQuery,
  useUpdateRolePermissionsMutation,
  useSecurityAuditLogsQuery,
  useSystemSettingsQuery,
} from '@/features/admin/queries/useAdminQueries'
import {
  AdminOverviewCards,
  UserManagementTable,
  RolePermissionMatrix,
  AuditLogsTable,
  SystemSettingsView,
} from '@/features/admin/components'
import { AdminCreateUserPayload } from '@/entities/admin'

interface AdminPageProps {
  lang: SupportedLang
}

type AdminTab = 'overview' | 'users' | 'roles' | 'audit' | 'settings'

export const AdminPage: React.FC<AdminPageProps> = ({ lang }) => {
  const t = translations[lang].admin
  const { user } = useAuth()

  const [activeTab, setActiveTab] = useState<AdminTab>('overview')

  // Users Filter State
  const [userSearch, setUserSearch] = useState('')
  const [selectedRole, setSelectedRole] = useState<UserRole | undefined>(undefined)
  const [selectedStatus, setSelectedStatus] = useState<UserStatus | undefined>(undefined)
  const [userPage, setUserPage] = useState(0)

  // Audit Logs Filter State
  const [auditAction, setAuditAction] = useState<string | undefined>(undefined)
  const [auditPage, setAuditPage] = useState(0)

  // Authorization Check: Must be ADMIN or SUPER_ADMIN
  const isAuthorized = user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN'

  // Queries (only enabled if authorized)
  const { data: stats, isLoading: statsLoading } = useUserSummaryStatsQuery()
  const { data: usersPage, isLoading: usersLoading } = useUsersListQuery(
    userSearch,
    selectedRole,
    selectedStatus,
    userPage,
    10
  )
  const { data: roles = [], isLoading: rolesLoading } = useRolesListQuery()
  const { data: permissionGroups = [], isLoading: permsLoading } = usePermissionsGroupedQuery()
  const { data: auditLogsPage, isLoading: auditLoading } = useSecurityAuditLogsQuery(
    auditAction,
    undefined,
    undefined,
    auditPage,
    15
  )
  const { data: systemSettings, isLoading: settingsLoading } = useSystemSettingsQuery()

  // Mutations
  const createUserMutation = useAdminCreateUserMutation()
  const changeRoleMutation = useAdminChangeRoleMutation()
  const changeStatusMutation = useAdminChangeStatusMutation()
  const updatePermissionsMutation = useUpdateRolePermissionsMutation()

  if (!isAuthorized) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] p-4 text-center">
        <div className="p-4 bg-destructive/10 text-destructive rounded-full mb-4">
          <ShieldAlert className="w-12 h-12" />
        </div>
        <h2 className="text-xl font-bold text-foreground mb-2">{t.accessDeniedTitle}</h2>
        <p className="text-sm text-muted-foreground max-w-md">{t.accessDeniedDesc}</p>
      </div>
    )
  }

  const handleCreateUser = async (payload: AdminCreateUserPayload) => {
    await createUserMutation.mutateAsync(payload)
  }

  const handleChangeRole = async (userId: string, role: UserRole) => {
    await changeRoleMutation.mutateAsync({ id: userId, role })
  }

  const handleChangeStatus = async (userId: string, status: UserStatus, reason?: string) => {
    await changeStatusMutation.mutateAsync({ id: userId, status, reason })
  }

  const handleSaveRolePermissions = async (roleCode: string, permissions: string[]) => {
    await updatePermissionsMutation.mutateAsync({ code: roleCode, permissions })
  }

  return (
    <div className="space-y-6 p-4 md:p-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <div className="p-2 bg-primary/10 rounded-lg text-primary">
              <Shield className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-2xl font-bold tracking-tight text-foreground">{t.title}</h1>
              <p className="text-xs text-muted-foreground mt-0.5">{t.subtitle}</p>
            </div>
          </div>
        </div>

        {/* User Identity Chip */}
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg border bg-card text-xs">
          <span className="text-muted-foreground">Admin Session:</span>
          <span className="font-semibold text-foreground">{user?.fullName || user?.phoneNumber}</span>
          <span className="font-mono text-primary font-bold">[{user?.role}]</span>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="flex border-b border-border overflow-x-auto gap-1">
        <button
          onClick={() => setActiveTab('overview')}
          className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold border-b-2 transition-colors whitespace-nowrap ${
            activeTab === 'overview'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <Shield className="w-4 h-4" />
          <span>{t.tabs.overview}</span>
        </button>

        <button
          onClick={() => setActiveTab('users')}
          className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold border-b-2 transition-colors whitespace-nowrap ${
            activeTab === 'users'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <Users className="w-4 h-4" />
          <span>{t.tabs.users}</span>
        </button>

        <button
          onClick={() => setActiveTab('roles')}
          className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold border-b-2 transition-colors whitespace-nowrap ${
            activeTab === 'roles'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <KeyRound className="w-4 h-4" />
          <span>{t.tabs.roles}</span>
        </button>

        <button
          onClick={() => setActiveTab('audit')}
          className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold border-b-2 transition-colors whitespace-nowrap ${
            activeTab === 'audit'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <FileText className="w-4 h-4" />
          <span>{t.tabs.audit}</span>
        </button>

        <button
          onClick={() => setActiveTab('settings')}
          className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold border-b-2 transition-colors whitespace-nowrap ${
            activeTab === 'settings'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <Sliders className="w-4 h-4" />
          <span>{t.tabs.settings}</span>
        </button>
      </div>

      {/* Tab Panels */}
      {activeTab === 'overview' && (
        <div className="space-y-6">
          <AdminOverviewCards
            stats={stats}
            settings={systemSettings}
            lang={lang}
            isLoading={statsLoading || settingsLoading}
          />
          <UserManagementTable
            usersPage={usersPage}
            currentUser={user}
            lang={lang}
            isLoading={usersLoading}
            search={userSearch}
            setSearch={setUserSearch}
            selectedRole={selectedRole}
            setSelectedRole={setSelectedRole}
            selectedStatus={selectedStatus}
            setSelectedStatus={setSelectedStatus}
            currentPage={userPage}
            setCurrentPage={setUserPage}
            onCreateUser={handleCreateUser}
            onChangeRole={handleChangeRole}
            onChangeStatus={handleChangeStatus}
          />
        </div>
      )}

      {activeTab === 'users' && (
        <UserManagementTable
          usersPage={usersPage}
          currentUser={user}
          lang={lang}
          isLoading={usersLoading}
          search={userSearch}
          setSearch={setUserSearch}
          selectedRole={selectedRole}
          setSelectedRole={setSelectedRole}
          selectedStatus={selectedStatus}
          setSelectedStatus={setSelectedStatus}
          currentPage={userPage}
          setCurrentPage={setUserPage}
          onCreateUser={handleCreateUser}
          onChangeRole={handleChangeRole}
          onChangeStatus={handleChangeStatus}
        />
      )}

      {activeTab === 'roles' && (
        <RolePermissionMatrix
          roles={roles}
          permissionGroups={permissionGroups}
          currentUser={user}
          lang={lang}
          isLoading={rolesLoading || permsLoading}
          onSaveRolePermissions={handleSaveRolePermissions}
        />
      )}

      {activeTab === 'audit' && (
        <AuditLogsTable
          logsPage={auditLogsPage}
          lang={lang}
          isLoading={auditLoading}
          selectedAction={auditAction}
          setSelectedAction={setAuditAction}
          currentPage={auditPage}
          setCurrentPage={setAuditPage}
        />
      )}

      {activeTab === 'settings' && (
        <SystemSettingsView
          settings={systemSettings}
          lang={lang}
          isLoading={settingsLoading}
        />
      )}
    </div>
  )
}
export default AdminPage
