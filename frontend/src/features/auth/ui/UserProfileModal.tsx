import React, { useState } from 'react'
import { Modal } from '@/shared/ui/Modal'
import { Input } from '@/shared/ui/Input'
import { Button } from '@/shared/ui/Button'
import { useAuth } from '../context/AuthContext'
import { UserRoleBadge, UserStatusBadge, UserAvatar } from '@/entities/user'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { User, Phone, Mail, MapPin, KeyRound, LogOut, CheckCircle2, AlertCircle, Edit3 } from 'lucide-react'
import { ApiError } from '@/shared/api/client'

interface UserProfileModalProps {
  isOpen: boolean
  onClose: () => void
  onOpenChangePassword: () => void
  lang?: SupportedLanguage
}

export const UserProfileModal: React.FC<UserProfileModalProps> = ({
  isOpen,
  onClose,
  onOpenChangePassword,
  lang = 'uz',
}) => {
  const t = translations[lang]
  const { user, updateProfile, logout } = useAuth()

  const [isEditing, setIsEditing] = useState(false)
  const [firstName, setFirstName] = useState(user?.firstName || '')
  const [lastName, setLastName] = useState(user?.lastName || '')
  const [email, setEmail] = useState(user?.email || '')
  const [district, setDistrict] = useState(user?.district || '')
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successNotice, setSuccessNotice] = useState<string | null>(null)

  // Sync state when user changes
  React.useEffect(() => {
    if (user) {
      setFirstName(user.firstName || '')
      setLastName(user.lastName || '')
      setEmail(user.email || '')
      setDistrict(user.district || '')
    }
  }, [user])

  if (!user) return null

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage(null)
    setSuccessNotice(null)

    if (!firstName.trim() || !lastName.trim()) {
      setErrorMessage(t.auth.messages.requiredFields)
      return
    }

    setIsLoading(true)
    try {
      await updateProfile({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim() ? email.trim() : null,
        district: district.trim() ? district.trim() : null,
      })
      setSuccessNotice(t.auth.messages.profileUpdated)
      setIsEditing(false)
      setTimeout(() => setSuccessNotice(null), 3000)
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setErrorMessage(err.message)
      } else if (err instanceof Error) {
        setErrorMessage(err.message)
      } else {
        setErrorMessage(t.common.error)
      }
    } finally {
      setIsLoading(false)
    }
  }

  const handleLogout = async () => {
    onClose()
    await logout()
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={t.auth.userProfileTitle}
      maxWidth="md"
    >
      <div className="space-y-5">
        {errorMessage && (
          <div className="flex items-center gap-2 p-3 text-xs font-medium text-destructive bg-destructive/10 border border-destructive/20 rounded-lg">
            <AlertCircle className="h-4 w-4 shrink-0" />
            <span>{errorMessage}</span>
          </div>
        )}

        {successNotice && (
          <div className="flex items-center gap-2 p-3 text-xs font-medium text-emerald-800 bg-emerald-100 dark:bg-emerald-950/60 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800 rounded-lg">
            <CheckCircle2 className="h-4 w-4 shrink-0 text-emerald-600" />
            <span>{successNotice}</span>
          </div>
        )}

        {/* User Card Header */}
        <div className="flex items-center gap-4 p-4 rounded-xl bg-muted/40 border border-border">
          <UserAvatar user={user} size="lg" />
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <h3 className="font-bold text-base text-foreground truncate">{user.fullName}</h3>
              <UserRoleBadge role={user.role} lang={lang} />
            </div>
            <p className="text-xs text-muted-foreground mt-0.5">{user.phoneNumber}</p>
            <div className="flex items-center gap-2 mt-2">
              <span className="text-[11px] text-muted-foreground">{t.auth.accountStatus}:</span>
              <UserStatusBadge status={user.status} lang={lang} />
            </div>
          </div>
        </div>

        {/* Content Tabs / View */}
        {isEditing ? (
          <form onSubmit={handleUpdate} className="space-y-3.5">
            <div className="grid grid-cols-2 gap-3">
              <Input
                label={t.auth.firstNameLabel}
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                required
              />
              <Input
                label={t.auth.lastNameLabel}
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                required
              />
            </div>

            <Input
              label={t.auth.emailLabel}
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />

            <Input
              label={t.auth.districtLabel}
              value={district}
              onChange={(e) => setDistrict(e.target.value)}
            />

            <div className="flex items-center justify-end gap-2 pt-2">
              <Button
                type="button"
                variant="ghost"
                size="md"
                onClick={() => setIsEditing(false)}
              >
                {t.common.cancel}
              </Button>
              <Button type="submit" variant="primary" size="md" isLoading={isLoading}>
                {t.auth.saveProfile}
              </Button>
            </div>
          </form>
        ) : (
          <div className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
              <div className="p-3 rounded-lg border bg-card/60 space-y-1">
                <span className="text-xs text-muted-foreground flex items-center gap-1.5">
                  <Phone className="h-3.5 w-3.5 text-primary" /> {t.auth.phoneLabel}
                </span>
                <p className="font-medium">{user.phoneNumber}</p>
              </div>

              <div className="p-3 rounded-lg border bg-card/60 space-y-1">
                <span className="text-xs text-muted-foreground flex items-center gap-1.5">
                  <Mail className="h-3.5 w-3.5 text-primary" /> {t.auth.emailLabel}
                </span>
                <p className="font-medium truncate">{user.email || '—'}</p>
              </div>

              <div className="p-3 rounded-lg border bg-card/60 space-y-1">
                <span className="text-xs text-muted-foreground flex items-center gap-1.5">
                  <MapPin className="h-3.5 w-3.5 text-primary" /> {t.auth.districtLabel}
                </span>
                <p className="font-medium">{user.district || 'Yangiariq tumani'}</p>
              </div>

              <div className="p-3 rounded-lg border bg-card/60 space-y-1">
                <span className="text-xs text-muted-foreground flex items-center gap-1.5">
                  <User className="h-3.5 w-3.5 text-primary" /> ID
                </span>
                <p className="font-mono text-xs truncate" title={user.id}>
                  {user.id}
                </p>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="pt-2 border-t border-border/80 flex flex-col sm:flex-row items-center justify-between gap-2">
              <Button
                type="button"
                variant="outline"
                size="sm"
                className="w-full sm:w-auto"
                leftIcon={<Edit3 className="h-4 w-4" />}
                onClick={() => setIsEditing(true)}
              >
                {t.auth.editProfile}
              </Button>

              <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  leftIcon={<KeyRound className="h-4 w-4" />}
                  onClick={() => {
                    onClose()
                    onOpenChangePassword()
                  }}
                >
                  {t.auth.changePassword}
                </Button>

                <Button
                  type="button"
                  variant="destructive"
                  size="sm"
                  leftIcon={<LogOut className="h-4 w-4" />}
                  onClick={handleLogout}
                >
                  {t.auth.signOut}
                </Button>
              </div>
            </div>
          </div>
        )}
      </div>
    </Modal>
  )
}
