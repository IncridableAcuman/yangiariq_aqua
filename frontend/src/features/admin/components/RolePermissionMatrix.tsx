import React, { useState, useEffect } from 'react'
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
  Button,
  Badge,
} from '@/shared/ui'
import {
  ShieldCheck,
  Shield,
  Save,
  CheckCircle2,
  Lock,
  Layers,
} from 'lucide-react'
import { RoleDto, PermissionCategoryGroup } from '@/entities/admin'
import { UserProfile } from '@/entities/user/types'
import { translations, SupportedLang } from '@/shared/i18n'

interface RolePermissionMatrixProps {
  roles: RoleDto[]
  permissionGroups: PermissionCategoryGroup[]
  currentUser: UserProfile | null
  lang: SupportedLang
  isLoading?: boolean
  onSaveRolePermissions: (roleCode: string, permissions: string[]) => Promise<unknown>
}

export const RolePermissionMatrix: React.FC<RolePermissionMatrixProps> = ({
  roles,
  permissionGroups,
  currentUser,
  lang,
  isLoading,
  onSaveRolePermissions,
}) => {
  const t = translations[lang].admin
  const [selectedRoleCode, setSelectedRoleCode] = useState<string>('SUPER_ADMIN')
  const [selectedPermissions, setSelectedPermissions] = useState<Set<string>>(new Set())
  const [isSaving, setIsSaving] = useState(false)
  const [saveSuccess, setSaveSuccess] = useState(false)

  const isSuperAdmin = currentUser?.role === 'SUPER_ADMIN'

  // When roles load or selection changes, sync permissions
  useEffect(() => {
    if (roles.length > 0) {
      const activeRole = roles.find((r) => r.code === selectedRoleCode) || roles[0]
      if (activeRole) {
        setSelectedRoleCode(activeRole.code)
        setSelectedPermissions(new Set(activeRole.permissions))
      }
    }
  }, [roles, selectedRoleCode])

  const currentRole = roles.find((r) => r.code === selectedRoleCode)

  const handleTogglePermission = (permCode: string) => {
    if (!isSuperAdmin) return
    const next = new Set(selectedPermissions)
    if (next.has(permCode)) {
      next.delete(permCode)
    } else {
      next.add(permCode)
    }
    setSelectedPermissions(next)
    setSaveSuccess(false)
  }

  const handleSave = async () => {
    if (!isSuperAdmin || !currentRole) return
    setIsSaving(true)
    setSaveSuccess(false)
    try {
      await onSaveRolePermissions(currentRole.code, Array.from(selectedPermissions))
      setSaveSuccess(true)
      setTimeout(() => setSaveSuccess(false), 3000)
    } finally {
      setIsSaving(false)
    }
  }

  const getCategoryTitle = (category: string) => {
    switch (category) {
      case 'USER_MANAGEMENT':
        return t.rolesSection.categoryUser
      case 'FARM_AND_FIELD':
        return t.rolesSection.categoryFarmField
      case 'SENSOR_AND_TELEMETRY':
        return t.rolesSection.categorySensor
      case 'WATER_AND_DISTRIBUTION':
        return t.rolesSection.categoryWater
      case 'REPORTING_AND_ANALYTICS':
        return t.rolesSection.categoryReporting
      case 'GOVERNANCE_AND_AUDIT':
        return t.rolesSection.categoryGovernance
      case 'PILOT_MANAGEMENT':
        return t.rolesSection.categoryPilot
      case 'INCIDENT_MANAGEMENT':
        return t.rolesSection.categoryIncident
      default:
        return category.replace(/_/g, ' ')
    }
  }

  if (isLoading) {
    return (
      <div className="space-y-4 animate-pulse">
        <div className="h-40 bg-muted rounded-xl" />
        <div className="h-80 bg-muted rounded-xl" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Roles Selector Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {roles.map((role) => {
          const isSelected = role.code === selectedRoleCode
          return (
            <button
              key={role.code}
              onClick={() => {
                setSelectedRoleCode(role.code)
                setSelectedPermissions(new Set(role.permissions))
                setSaveSuccess(false)
              }}
              className={`text-left p-4 rounded-xl border transition-all ${
                isSelected
                  ? 'border-primary ring-2 ring-primary/20 bg-primary/5 shadow-xs'
                  : 'border-border bg-card hover:bg-muted/30 shadow-xs'
              }`}
            >
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <Shield className={`w-4 h-4 ${isSelected ? 'text-primary' : 'text-muted-foreground'}`} />
                  <span className="font-semibold text-sm">{role.name}</span>
                </div>
                {role.isSystem ? (
                  <Badge variant="outline" className="text-[10px] py-0 px-1.5 border-primary/30 text-primary">
                    System
                  </Badge>
                ) : (
                  <Badge variant="secondary" className="text-[10px] py-0 px-1.5">
                    Custom
                  </Badge>
                )}
              </div>
              <div className="text-xs font-mono text-muted-foreground">{role.code}</div>
              <div className="mt-3 flex items-center justify-between text-xs text-muted-foreground border-t pt-2">
                <span>{role.permissions.length} permissions</span>
                <span>{role.userCount} users</span>
              </div>
            </button>
          )
        })}
      </div>

      {/* Permission Matrix for Selected Role */}
      {currentRole && (
        <Card className="border shadow-xs">
          <CardHeader className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b">
            <div>
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-5 h-5 text-primary" />
                <CardTitle className="text-base">
                  {t.rolesSection.matrixTitle}: <span className="text-primary">{currentRole.name}</span>
                </CardTitle>
                <Badge variant="outline" className="font-mono text-xs">
                  {currentRole.code}
                </Badge>
              </div>
              <CardDescription className="text-xs mt-1">
                {currentRole.description || t.rolesSection.matrixDesc}
              </CardDescription>
            </div>

            <div className="flex items-center gap-2">
              {!isSuperAdmin && (
                <div className="flex items-center gap-1.5 text-xs text-amber-600 bg-amber-50 dark:bg-amber-950/30 px-3 py-1.5 rounded-lg border border-amber-200 dark:border-amber-900">
                  <Lock className="w-3.5 h-3.5" />
                  <span>View Only (Super Admin required to edit)</span>
                </div>
              )}

              {saveSuccess && (
                <div className="flex items-center gap-1 text-xs text-emerald-600 font-medium">
                  <CheckCircle2 className="w-4 h-4" />
                  <span>{t.rolesSection.savedSuccess}</span>
                </div>
              )}

              {isSuperAdmin && (
                <Button
                  onClick={handleSave}
                  disabled={isSaving}
                  size="sm"
                  className="gap-2"
                >
                  <Save className="w-4 h-4" />
                  <span>{isSaving ? t.rolesSection.saving : t.rolesSection.savePermissions}</span>
                </Button>
              )}
            </div>
          </CardHeader>

          <CardContent className="p-6 space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {permissionGroups.map((group) => {
                const groupPerms = group.permissions || []
                const activeCount = groupPerms.filter((p) => selectedPermissions.has(p.code)).length

                return (
                  <div
                    key={group.category}
                    className="border rounded-xl p-4 bg-muted/10 hover:bg-muted/20 transition-colors flex flex-col justify-between space-y-3"
                  >
                    <div>
                      <div className="flex items-center justify-between pb-2 border-b mb-3">
                        <div className="flex items-center gap-2 font-semibold text-xs text-foreground">
                          <Layers className="w-4 h-4 text-primary" />
                          <span>{getCategoryTitle(group.category)}</span>
                        </div>
                        <span className="text-[11px] font-mono text-muted-foreground">
                          {activeCount} / {groupPerms.length}
                        </span>
                      </div>

                      <div className="space-y-2.5">
                        {groupPerms.map((perm) => {
                          const isChecked = selectedPermissions.has(perm.code)
                          return (
                            <label
                              key={perm.code}
                              className={`flex items-start gap-2.5 p-1.5 rounded-lg text-xs cursor-pointer select-none transition-colors ${
                                isChecked
                                  ? 'bg-primary/5 text-foreground'
                                  : 'text-muted-foreground hover:bg-muted/50'
                              } ${!isSuperAdmin ? 'cursor-not-allowed opacity-80' : ''}`}
                            >
                              <input
                                type="checkbox"
                                checked={isChecked}
                                onChange={() => handleTogglePermission(perm.code)}
                                disabled={!isSuperAdmin}
                                className="mt-0.5 rounded border-input text-primary focus:ring-primary h-3.5 w-3.5"
                              />
                              <div className="flex-1">
                                <div className="font-medium text-xs text-foreground leading-tight">
                                  {perm.name}
                                </div>
                                <div className="font-mono text-[10px] text-muted-foreground mt-0.5">
                                  {perm.code}
                                </div>
                                {perm.description && (
                                  <div className="text-[11px] text-muted-foreground/80 mt-0.5 line-clamp-2">
                                    {perm.description}
                                  </div>
                                )}
                              </div>
                            </label>
                          )
                        })}
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
