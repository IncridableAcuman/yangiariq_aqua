import React, { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { Input } from '@/shared/ui/Input'
import { Button } from '@/shared/ui/Button'
import { Lock, Phone, Eye, EyeOff, AlertCircle } from 'lucide-react'
import { ApiError } from '@/shared/api/client'

interface LoginFormProps {
  lang?: SupportedLanguage
  onSuccess?: () => void
  onSwitchToRegister?: () => void
  onSwitchToForgotPassword?: () => void
}

export const LoginForm: React.FC<LoginFormProps> = ({
  lang = 'uz',
  onSuccess,
  onSwitchToRegister,
  onSwitchToForgotPassword,
}) => {
  const t = translations[lang]
  const { login } = useAuth()

  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(true)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage(null)

    if (!identifier.trim() || !password) {
      setErrorMessage(t.auth.messages.requiredFields)
      return
    }

    setIsLoading(true)
    try {
      await login({
        identifier: identifier.trim(),
        password,
      })
      onSuccess?.()
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

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {errorMessage && (
        <div className="flex items-center gap-2 p-3 text-xs font-medium text-destructive bg-destructive/10 border border-destructive/20 rounded-lg animate-in fade-in">
          <AlertCircle className="h-4 w-4 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      <Input
        label={t.auth.identifierLabel}
        placeholder={t.auth.identifierPlaceholder}
        value={identifier}
        onChange={(e) => setIdentifier(e.target.value)}
        leftIcon={<Phone className="h-4 w-4" />}
        autoComplete="username"
        required
      />

      <Input
        label={t.auth.passwordLabel}
        type={showPassword ? 'text' : 'password'}
        placeholder={t.auth.passwordPlaceholder}
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        leftIcon={<Lock className="h-4 w-4" />}
        rightIcon={
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="hover:text-foreground focus:outline-none"
            tabIndex={-1}
          >
            {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        }
        autoComplete="current-password"
        required
      />

      <div className="flex items-center justify-between text-xs pt-1">
        <label className="flex items-center gap-2 cursor-pointer select-none text-muted-foreground hover:text-foreground">
          <input
            type="checkbox"
            checked={rememberMe}
            onChange={(e) => setRememberMe(e.target.checked)}
            className="rounded border-input text-primary focus:ring-primary h-4 w-4"
          />
          <span>{t.auth.rememberMe}</span>
        </label>

        {onSwitchToForgotPassword && (
          <button
            type="button"
            onClick={onSwitchToForgotPassword}
            className="text-primary hover:underline font-medium"
          >
            {t.auth.forgotPassword}
          </button>
        )}
      </div>

      <Button
        type="submit"
        variant="primary"
        size="md"
        className="w-full mt-2 font-semibold shadow-md"
        isLoading={isLoading}
      >
        {t.auth.loginAction}
      </Button>

      {onSwitchToRegister && (
        <div className="text-center pt-2 border-t border-border/50">
          <p className="text-xs text-muted-foreground">
            {t.auth.noAccount}{' '}
            <button
              type="button"
              onClick={onSwitchToRegister}
              className="font-semibold text-primary hover:underline ml-1"
            >
              {t.auth.signUp}
            </button>
          </p>
        </div>
      )}
    </form>
  )
}
