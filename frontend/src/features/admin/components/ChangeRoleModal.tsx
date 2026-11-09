import React, { useState, useEffect } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  Button,
  Label,
} from '@/shared/ui'
import { Shield, AlertCircle } from 'lucide-react'
import { UserProfile, UserRole } from '@/entities/user/types'
import { translations, SupportedLang } from '@/shared/i18n'

interface ChangeRoleModalProps {
  user: UserProfile | null
  isOpen: boolean
  onClose: () => void
  onConfirm: (userId: string, newRole: UserRole) => Promise<unknown>
  currentUserRole?: UserRole
  lang: SupportedLang
}

export const ChangeRoleModal: React.FC<ChangeRoleModalProps> = ({
  user,
  isOpen,
  onClose,
  onConfirm,
  currentUserRole,
  lang,
}) => {
  const t = translations[lang].admin
  const [selectedRole, setSelectedRole] = useState<UserRole>('FARMER')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (user?.role) {
      setSelectedRole(user.role)
    }
  }, [user])

  if (!user) return null

  const isSuperAdmin = currentUserRole === 'SUPER_ADMIN'
  const availableRoles: UserRole[] = isSuperAdmin
    ? ['FARMER', 'OPERATOR', 'ADMIN', 'SUPER_ADMIN']
    : ['FARMER', 'OPERATOR', 'ADMIN']

  const handleSave = async () => {
    if (selectedRole === user.role) {
      onClose()
      return
    }

    setIsSubmitting(true)
    setError(null)

    try {
      await onConfirm(user.id, selectedRole)
      onClose()
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to update role'
      setError(msg)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <div className="p-2 bg-primary/10 rounded-lg text-primary">
              <Shield className="w-5 h-5" />
            </div>
            <div>
              <DialogTitle>{t.changeRoleModal.title}</DialogTitle>
              <p className="text-xs text-muted-foreground mt-0.5">
                {user.fullName || user.phoneNumber} ({user.phoneNumber})
              </p>
            </div>
          </div>
        </DialogHeader>

        <div className="space-y-4 pt-2">
          {error && (
            <div className="flex items-center gap-2 p-3 text-xs rounded-lg bg-destructive/10 text-destructive border border-destructive/20">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="space-y-1">
            <Label className="text-xs font-semibold">{t.changeRoleModal.currentRole}</Label>
            <div className="p-2 bg-muted rounded-md text-sm font-medium">
              {user.role}
            </div>
          </div>

          <div className="space-y-1">
            <Label htmlFor="newRole" className="text-xs font-semibold">
              {t.changeRoleModal.newRole}
            </Label>
            <select
              id="newRole"
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value as UserRole)}
              className="w-full h-9 rounded-md border border-input bg-background px-3 py-1 text-sm shadow-xs focus-visible:outline-hidden focus-visible:ring-1 focus-visible:ring-ring"
            >
              {availableRoles.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </div>

          <DialogFooter className="pt-2 gap-2">
            <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
              {t.changeRoleModal.cancel}
            </Button>
            <Button type="button" onClick={handleSave} disabled={isSubmitting}>
              {isSubmitting ? t.changeRoleModal.updating : t.changeRoleModal.submit}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  )
}
