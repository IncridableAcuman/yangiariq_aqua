import React, { useState } from 'react'
import {
  Button,
  Input,
  Badge,
} from '@/shared/ui'
import {
  Search,
  UserPlus,
  Shield,
  CheckCircle,
  Ban,
  PauseCircle,
  ChevronLeft,
  ChevronRight,
  AlertTriangle,
} from 'lucide-react'
import { UserProfile, UserRole, UserStatus } from '@/entities/user/types'
import { UserAvatar } from '@/entities/user/ui/UserAvatar'
import { UserRoleBadge } from '@/entities/user/ui/UserBadge'
import { PageResponse } from '@/shared/api/client'
import { AdminCreateUserPayload } from '@/entities/admin'
import { CreateUserDialog } from './CreateUserDialog'
import { ChangeRoleModal } from './ChangeRoleModal'
import { translations, SupportedLang } from '@/shared/i18n'

interface UserManagementTableProps {
  usersPage: PageResponse<UserProfile> | undefined
  currentUser: UserProfile | null
  lang: SupportedLang
  isLoading?: boolean
  search: string
  setSearch: (s: string) => void
  selectedRole?: UserRole
  setSelectedRole: (r?: UserRole) => void
  selectedStatus?: UserStatus
  setSelectedStatus: (s?: UserStatus) => void
  currentPage: number
  setCurrentPage: (p: number) => void
  onCreateUser: (payload: AdminCreateUserPayload) => Promise<unknown>
  onChangeRole: (userId: string, role: UserRole) => Promise<unknown>
  onChangeStatus: (userId: string, status: UserStatus, reason?: string) => Promise<unknown>
}

export const UserManagementTable: React.FC<UserManagementTableProps> = ({
  usersPage,
  currentUser,
  lang,
  isLoading,
  search,
  setSearch,
  selectedRole,
  setSelectedRole,
  selectedStatus,
  setSelectedStatus,
  currentPage,
  setCurrentPage,
  onCreateUser,
  onChangeRole,
  onChangeStatus,
}) => {
  const t = translations[lang].admin
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [roleModalUser, setRoleModalUser] = useState<UserProfile | null>(null)
  const [actionInProgressId, setActionInProgressId] = useState<string | null>(null)
  const [statusConfirm, setStatusConfirm] = useState<{
    user: UserProfile
    newStatus: UserStatus
  } | null>(null)

  const isSuperAdmin = currentUser?.role === 'SUPER_ADMIN'

  const canModifyUser = (targetUser: UserProfile) => {
    if (isSuperAdmin) return true
    // Standard ADMIN cannot modify a SUPER_ADMIN
    if (targetUser.role === 'SUPER_ADMIN') return false
    return true
  }

  const handleStatusChange = async (targetUser: UserProfile, newStatus: UserStatus) => {
    setActionInProgressId(targetUser.id)
    try {
      await onChangeStatus(targetUser.id, newStatus)
    } finally {
      setActionInProgressId(null)
      setStatusConfirm(null)
    }
  }

  const users = usersPage?.content || []
  const totalPages = usersPage?.totalPages || 0
  const totalElements = usersPage?.totalElements || 0

  return (
    <div className="space-y-4">
      {/* Top Filter and Action Bar */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        <div className="flex flex-1 items-center gap-2 max-w-lg">
          <div className="relative flex-1">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={search}
              onChange={(e) => {
                setSearch(e.target.value)
                setCurrentPage(0)
              }}
              placeholder={t.usersTable.searchPlaceholder}
              className="pl-9 h-9"
            />
          </div>

          <select
            value={selectedRole || ''}
            onChange={(e) => {
              setSelectedRole(e.target.value ? (e.target.value as UserRole) : undefined)
              setCurrentPage(0)
            }}
            className="h-9 rounded-md border border-input bg-background px-3 py-1 text-xs shadow-xs focus-visible:outline-hidden focus-visible:ring-1 focus-visible:ring-ring"
          >
            <option value="">{t.usersTable.allRoles}</option>
            <option value="SUPER_ADMIN">SUPER_ADMIN</option>
            <option value="ADMIN">ADMIN</option>
            <option value="OPERATOR">OPERATOR</option>
            <option value="FARMER">FARMER</option>
          </select>

          <select
            value={selectedStatus || ''}
            onChange={(e) => {
              setSelectedStatus(e.target.value ? (e.target.value as UserStatus) : undefined)
              setCurrentPage(0)
            }}
            className="h-9 rounded-md border border-input bg-background px-3 py-1 text-xs shadow-xs focus-visible:outline-hidden focus-visible:ring-1 focus-visible:ring-ring"
          >
            <option value="">{t.usersTable.allStatuses}</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
            <option value="BLOCKED">BLOCKED</option>
          </select>
        </div>

        <Button onClick={() => setIsCreateOpen(true)} className="gap-2 shrink-0">
          <UserPlus className="w-4 h-4" />
          <span>{t.usersTable.createUser}</span>
        </Button>
      </div>

      {/* Users Table */}
      <div className="border rounded-xl bg-card shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-muted/50 border-b text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              <tr>
                <th className="px-4 py-3">{t.usersTable.fullName} / Phone</th>
                <th className="px-4 py-3">{t.usersTable.role}</th>
                <th className="px-4 py-3">{t.usersTable.status}</th>
                <th className="px-4 py-3">{t.usersTable.created}</th>
                <th className="px-4 py-3 text-right">{t.usersTable.actions}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {isLoading ? (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-muted-foreground">
                    <div className="flex items-center justify-center gap-2">
                      <div className="w-4 h-4 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                      <span>Loading users...</span>
                    </div>
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-muted-foreground">
                    {t.usersTable.noUsersFound}
                  </td>
                </tr>
              ) : (
                users.map((u) => {
                  const allowed = canModifyUser(u)
                  const isProcessing = actionInProgressId === u.id

                  return (
                    <tr key={u.id} className="hover:bg-muted/30 transition-colors">
                      {/* User Info */}
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-3">
                          <UserAvatar user={u} size="md" />
                          <div>
                            <div className="font-semibold text-foreground flex items-center gap-2">
                              <span>{u.fullName || `${u.firstName} ${u.lastName}`}</span>
                              {u.id === currentUser?.id && (
                                <Badge variant="secondary" className="text-[10px] py-0 px-1.5">
                                  You
                                </Badge>
                              )}
                            </div>
                            <div className="text-xs text-muted-foreground flex items-center gap-2">
                              <span>{u.phoneNumber}</span>
                              {u.email && <span>• {u.email}</span>}
                              {u.district && <span>• {u.district}</span>}
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* Role */}
                      <td className="px-4 py-3">
                        <UserRoleBadge role={u.role} />
                      </td>

                      {/* Status */}
                      <td className="px-4 py-3">
                        <Badge
                          variant="outline"
                          className={
                            u.status === 'ACTIVE'
                              ? 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-300'
                              : u.status === 'INACTIVE'
                              ? 'bg-amber-500/10 text-amber-700 dark:text-amber-400 border-amber-300'
                              : 'bg-rose-500/10 text-rose-700 dark:text-rose-400 border-rose-300'
                          }
                        >
                          {u.status}
                        </Badge>
                      </td>

                      {/* Created */}
                      <td className="px-4 py-3 text-xs text-muted-foreground whitespace-nowrap">
                        {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}
                      </td>

                      {/* Actions */}
                      <td className="px-4 py-3 text-right">
                        {!allowed ? (
                          <span
                            className="text-xs text-muted-foreground italic px-2"
                            title={t.usersTable.cannotModifySuperAdmin}
                          >
                            Protected
                          </span>
                        ) : (
                          <div className="flex items-center justify-end gap-1.5">
                            {/* Change Role Button */}
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => setRoleModalUser(u)}
                              disabled={isProcessing}
                              className="h-8 px-2.5 text-xs gap-1"
                              title={t.usersTable.changeRole}
                            >
                              <Shield className="w-3.5 h-3.5 text-primary" />
                              <span className="hidden sm:inline">{t.usersTable.changeRole}</span>
                            </Button>

                            {/* Status Actions */}
                            {u.status === 'ACTIVE' ? (
                              <>
                                <Button
                                  size="sm"
                                  variant="ghost"
                                  onClick={() => setStatusConfirm({ user: u, newStatus: 'INACTIVE' })}
                                  disabled={isProcessing}
                                  className="h-8 px-2 text-amber-600 hover:text-amber-700 hover:bg-amber-50 dark:hover:bg-amber-950/30"
                                  title={t.usersTable.deactivate}
                                >
                                  <PauseCircle className="w-4 h-4" />
                                </Button>
                                <Button
                                  size="sm"
                                  variant="ghost"
                                  onClick={() => setStatusConfirm({ user: u, newStatus: 'BLOCKED' })}
                                  disabled={isProcessing}
                                  className="h-8 px-2 text-rose-600 hover:text-rose-700 hover:bg-rose-50 dark:hover:bg-rose-950/30"
                                  title={t.usersTable.block}
                                >
                                  <Ban className="w-4 h-4" />
                                </Button>
                              </>
                            ) : (
                              <Button
                                size="sm"
                                variant="ghost"
                                onClick={() => setStatusConfirm({ user: u, newStatus: 'ACTIVE' })}
                                disabled={isProcessing}
                                className="h-8 px-2 text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50 dark:hover:bg-emerald-950/30"
                                title={t.usersTable.activate}
                              >
                                <CheckCircle className="w-4 h-4" />
                              </Button>
                            )}
                          </div>
                        )}
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        <div className="flex items-center justify-between px-4 py-3 border-t bg-muted/20 text-xs text-muted-foreground">
          <div>
            Showing <span className="font-semibold text-foreground">{users.length}</span> of{' '}
            <span className="font-semibold text-foreground">{totalElements}</span> users
          </div>
          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="outline"
              onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0 || isLoading}
              className="h-8 px-2 gap-1"
            >
              <ChevronLeft className="w-4 h-4" />
              <span>Previous</span>
            </Button>
            <span className="text-xs">
              Page {currentPage + 1} of {Math.max(1, totalPages)}
            </span>
            <Button
              size="sm"
              variant="outline"
              onClick={() => setCurrentPage(currentPage + 1)}
              disabled={currentPage + 1 >= totalPages || isLoading}
              className="h-8 px-2 gap-1"
            >
              <span>Next</span>
              <ChevronRight className="w-4 h-4" />
            </Button>
          </div>
        </div>
      </div>

      {/* Confirmation Dialog for Status Change */}
      {statusConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="bg-background rounded-xl p-5 max-w-sm w-full border shadow-lg space-y-4 animate-in fade-in zoom-in-95">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-full bg-amber-500/10 text-amber-600">
                <AlertTriangle className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-semibold text-sm">
                  {statusConfirm.newStatus === 'ACTIVE'
                    ? t.usersTable.confirmActivate
                    : statusConfirm.newStatus === 'INACTIVE'
                    ? t.usersTable.confirmDeactivate
                    : t.usersTable.confirmBlock}
                </h3>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {statusConfirm.user.fullName || statusConfirm.user.phoneNumber}
                </p>
              </div>
            </div>

            <div className="flex items-center justify-end gap-2 pt-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setStatusConfirm(null)}
                disabled={Boolean(actionInProgressId)}
              >
                Cancel
              </Button>
              <Button
                variant={statusConfirm.newStatus === 'BLOCKED' ? 'destructive' : 'default'}
                size="sm"
                onClick={() => handleStatusChange(statusConfirm.user, statusConfirm.newStatus)}
                disabled={Boolean(actionInProgressId)}
              >
                {actionInProgressId ? 'Processing...' : 'Confirm'}
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Dialogs */}
      <CreateUserDialog
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onCreate={onCreateUser}
        currentUserRole={currentUser?.role}
        lang={lang}
      />

      <ChangeRoleModal
        user={roleModalUser}
        isOpen={Boolean(roleModalUser)}
        onClose={() => setRoleModalUser(null)}
        onConfirm={onChangeRole}
        currentUserRole={currentUser?.role}
        lang={lang}
      />
    </div>
  )
}
