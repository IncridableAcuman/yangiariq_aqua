import React, { useState } from 'react'
import { Modal } from '@/shared/ui/Modal'
import { Input } from '@/shared/ui/Input'
import { Button } from '@/shared/ui/Button'
import { useAuth } from '../context/AuthContext'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { Lock, Eye, EyeOff, AlertCircle, CheckCircle2 } from 'lucide-react'
import { ApiError } from '@/shared/api/client'

interface ChangePasswordModalProps {
  isOpen: boolean
  onClose: () => void
  lang?: SupportedLanguage
}

export const ChangePasswordModal: React.FC<ChangePasswordModalProps> = ({
  isOpen,
  onClose,
  lang = 'uz',
}) => {
  const t = translations[lang]
  const { changePassword } = useAuth()

  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showCurrent, setShowCurrent] = useState(false)
  const [showNew, setShowNew] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successNotice, setSuccessNotice] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage(null)
    setSuccessNotice(null)

    if (!currentPassword || !newPassword) {
      setErrorMessage(t.auth.messages.requiredFields)
      return
    }

    if (newPassword.length < 8) {
      setErrorMessage(t.auth.messages.passwordTooShort)
      return
    }

    if (newPassword !== confirmPassword) {
      setErrorMessage(t.auth.messages.passwordMismatch)
      return
    }

    setIsLoading(true)
    try {
      await changePassword({ currentPassword, newPassword })
      setSuccessNotice(t.auth.messages.passwordChanged)
      setTimeout(() => {
        handleClose()
      }, 1500)
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

  const handleClose = () => {
    setCurrentPassword('')
    setNewPassword('')
    setConfirmPassword('')
    setErrorMessage(null)
    setSuccessNotice(null)
    onClose()
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title={t.auth.changePassword}
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
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

        <Input
          label={t.auth.currentPasswordLabel}
          type={showCurrent ? 'text' : 'password'}
          value={currentPassword}
          onChange={(e) => setCurrentPassword(e.target.value)}
          leftIcon={<Lock className="h-4 w-4" />}
          rightIcon={
            <button
              type="button"
              onClick={() => setShowCurrent(!showCurrent)}
              className="hover:text-foreground"
              tabIndex={-1}
            >
              {showCurrent ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          }
          required
        />

        <Input
          label={t.auth.newPasswordLabel}
          type={showNew ? 'text' : 'password'}
          placeholder={t.auth.passwordPlaceholder}
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          leftIcon={<Lock className="h-4 w-4" />}
          rightIcon={
            <button
              type="button"
              onClick={() => setShowNew(!showNew)}
              className="hover:text-foreground"
              tabIndex={-1}
            >
              {showNew ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          }
          required
        />

        <Input
          label={t.auth.confirmPasswordLabel}
          type={showNew ? 'text' : 'password'}
          placeholder={t.auth.passwordPlaceholder}
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          leftIcon={<Lock className="h-4 w-4" />}
          required
        />

        <div className="flex items-center justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" size="md" onClick={handleClose}>
            {t.common.cancel}
          </Button>
          <Button type="submit" variant="primary" size="md" isLoading={isLoading}>
            {t.common.save}
          </Button>
        </div>
      </form>
    </Modal>
  )
}
