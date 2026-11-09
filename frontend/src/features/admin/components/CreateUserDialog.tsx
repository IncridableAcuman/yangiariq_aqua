import React, { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  Button,
  Input,
  Label,
} from '@/shared/ui'
import { UserPlus, AlertCircle } from 'lucide-react'
import { UserRole } from '@/entities/user/types'
import { AdminCreateUserPayload } from '@/entities/admin'
import { translations, SupportedLang } from '@/shared/i18n'

interface CreateUserDialogProps {
  isOpen: boolean
  onClose: () => void
  onCreate: (payload: AdminCreateUserPayload) => Promise<unknown>
  currentUserRole?: UserRole
  lang: SupportedLang
}

export const CreateUserDialog: React.FC<CreateUserDialogProps> = ({
  isOpen,
  onClose,
  onCreate,
  currentUserRole,
  lang,
}) => {
  const t = translations[lang].admin

  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<UserRole>('FARMER')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const isSuperAdmin = currentUserRole === 'SUPER_ADMIN'

  const availableRoles: UserRole[] = isSuperAdmin
    ? ['FARMER', 'OPERATOR', 'ADMIN', 'SUPER_ADMIN']
    : ['FARMER', 'OPERATOR', 'ADMIN']

  const resetForm = () => {
    setFirstName('')
    setLastName('')
    setPhoneNumber('')
    setEmail('')
    setPassword('')
    setRole('FARMER')
    setError(null)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!firstName.trim() || !lastName.trim() || !phoneNumber.trim() || !password.trim()) {
      setError('Please fill in first name, last name, phone number, and password.')
      return
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters long.')
      return
    }

    setError(null)
    setIsSubmitting(true)

    try {
      await onCreate({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        phoneNumber: phoneNumber.trim(),
        email: email.trim() || undefined,
        password,
        role,
      })
      resetForm()
      onClose()
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to create user'
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
              <UserPlus className="w-5 h-5" />
            </div>
            <div>
              <DialogTitle>{t.createUserModal.title}</DialogTitle>
              <p className="text-xs text-muted-foreground mt-0.5">{t.createUserModal.description}</p>
            </div>
          </div>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 pt-2">
          {error && (
            <div className="flex items-center gap-2 p-3 text-xs rounded-lg bg-destructive/10 text-destructive border border-destructive/20">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label htmlFor="firstName" className="text-xs font-semibold">
                First Name *
              </Label>
              <Input
                id="firstName"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                placeholder="e.g. Jamshid"
                required
              />
            </div>

            <div className="space-y-1">
              <Label htmlFor="lastName" className="text-xs font-semibold">
                Last Name *
              </Label>
              <Input
                id="lastName"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                placeholder="e.g. Karimov"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label htmlFor="phoneNumber" className="text-xs font-semibold">
                {t.createUserModal.phone} *
              </Label>
              <Input
                id="phoneNumber"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                placeholder="+998901234567"
                required
              />
            </div>

            <div className="space-y-1">
              <Label htmlFor="role" className="text-xs font-semibold">
                {t.createUserModal.role} *
              </Label>
              <select
                id="role"
                value={role}
                onChange={(e) => setRole(e.target.value as UserRole)}
                className="w-full h-9 rounded-md border border-input bg-background px-3 py-1 text-sm shadow-xs focus-visible:outline-hidden focus-visible:ring-1 focus-visible:ring-ring"
              >
                {availableRoles.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="space-y-1">
            <Label htmlFor="email" className="text-xs font-semibold">
              {t.createUserModal.email}
            </Label>
            <Input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="user@aqua-ai.uz"
            />
          </div>

          <div className="space-y-1">
            <Label htmlFor="password" className="text-xs font-semibold">
              {t.createUserModal.password} *
            </Label>
            <Input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Minimum 6 characters"
              required
            />
          </div>

          <DialogFooter className="pt-2 gap-2">
            <Button type="button" variant="outline" onClick={onClose} disabled={isSubmitting}>
              {t.createUserModal.cancel}
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? t.createUserModal.creating : t.createUserModal.submit}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
