import React, { useState } from 'react'
import { Modal } from '@/shared/ui/Modal'
import { Input } from '@/shared/ui/Input'
import { Button } from '@/shared/ui/Button'
import { useAuth } from '../context/AuthContext'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { Phone, KeyRound, Lock, Eye, EyeOff, AlertCircle, CheckCircle2, ArrowLeft } from 'lucide-react'
import { ApiError } from '@/shared/api/client'

interface PasswordResetModalProps {
  isOpen: boolean
  onClose: () => void
  onSuccessReturnToLogin: () => void
  lang?: SupportedLanguage
}

export const PasswordResetModal: React.FC<PasswordResetModalProps> = ({
  isOpen,
  onClose,
  onSuccessReturnToLogin,
  lang = 'uz',
}) => {
  const t = translations[lang]
  const { requestReset, confirmReset } = useAuth()

  const [step, setStep] = useState<'request' | 'confirm'>('request')
  const [identifier, setIdentifier] = useState('')
  const [token, setToken] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successNotice, setSuccessNotice] = useState<string | null>(null)

  const handleRequest = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage(null)
    setSuccessNotice(null)

    if (!identifier.trim()) {
      setErrorMessage(t.auth.messages.requiredFields)
      return
    }

    setIsLoading(true)
    try {
      await requestReset(identifier.trim())
      setSuccessNotice(t.auth.messages.resetRequested)
      setStep('confirm')
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

  const handleConfirm = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage(null)

    if (!token.trim() || !newPassword) {
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
      await confirmReset(token.trim(), newPassword)
      setSuccessNotice(t.auth.messages.resetSuccess)
      setTimeout(() => {
        onSuccessReturnToLogin()
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
    setStep('request')
    setErrorMessage(null)
    setSuccessNotice(null)
    setToken('')
    setNewPassword('')
    setConfirmPassword('')
    onClose()
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title={t.auth.resetPasswordTitle}
      description={t.auth.resetPasswordSubtitle}
      maxWidth="md"
    >
      <div className="space-y-4">
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

        {step === 'request' ? (
          <form onSubmit={handleRequest} className="space-y-4">
            <Input
              label={t.auth.identifierLabel}
              placeholder={t.auth.identifierPlaceholder}
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
              leftIcon={<Phone className="h-4 w-4" />}
              required
            />

            <Button
              type="submit"
              variant="primary"
              size="md"
              className="w-full"
              isLoading={isLoading}
            >
              {t.auth.sendResetCode}
            </Button>

            <div className="flex items-center justify-between text-xs pt-2 border-t">
              <button
                type="button"
                onClick={onSuccessReturnToLogin}
                className="inline-flex items-center gap-1 text-muted-foreground hover:text-foreground"
              >
                <ArrowLeft className="h-3.5 w-3.5" />
                <span>{t.auth.backToLogin}</span>
              </button>

              <button
                type="button"
                onClick={() => setStep('confirm')}
                className="text-primary hover:underline font-medium"
              >
                {t.auth.confirmReset} →
              </button>
            </div>
          </form>
        ) : (
          <form onSubmit={handleConfirm} className="space-y-3.5">
            <Input
              label={t.auth.resetTokenLabel}
              placeholder={t.auth.resetTokenPlaceholder}
              value={token}
              onChange={(e) => setToken(e.target.value)}
              leftIcon={<KeyRound className="h-4 w-4" />}
              required
            />

            <Input
              label={t.auth.newPasswordLabel}
              type={showPassword ? 'text' : 'password'}
              placeholder={t.auth.passwordPlaceholder}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              leftIcon={<Lock className="h-4 w-4" />}
              rightIcon={
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="hover:text-foreground"
                  tabIndex={-1}
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              }
              required
            />

            <Input
              label={t.auth.confirmPasswordLabel}
              type={showPassword ? 'text' : 'password'}
              placeholder={t.auth.passwordPlaceholder}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              leftIcon={<Lock className="h-4 w-4" />}
              required
            />

            <Button
              type="submit"
              variant="primary"
              size="md"
              className="w-full"
              isLoading={isLoading}
            >
              {t.auth.confirmReset}
            </Button>

            <div className="flex items-center justify-between text-xs pt-2 border-t">
              <button
                type="button"
                onClick={() => setStep('request')}
                className="inline-flex items-center gap-1 text-muted-foreground hover:text-foreground"
              >
                <ArrowLeft className="h-3.5 w-3.5" />
                <span>{t.auth.sendResetCode}</span>
              </button>

              <button
                type="button"
                onClick={onSuccessReturnToLogin}
                className="text-primary hover:underline font-medium"
              >
                {t.auth.backToLogin}
              </button>
            </div>
          </form>
        )}
      </div>
    </Modal>
  )
}
