import React, { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { Input } from '@/shared/ui/Input'
import { Button } from '@/shared/ui/Button'
import { User, Phone, Mail, Lock, MapPin, Eye, EyeOff, AlertCircle } from 'lucide-react'
import { ApiError } from '@/shared/api/client'

interface RegisterFormProps {
  lang?: SupportedLanguage
  onSuccess?: () => void
  onSwitchToLogin?: () => void
}

export const RegisterForm: React.FC<RegisterFormProps> = ({
  lang = 'uz',
  onSuccess,
  onSwitchToLogin,
}) => {
  const t = translations[lang]
  const { register } = useAuth()

  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('+998')
  const [email, setEmail] = useState('')
  const [district, setDistrict] = useState('Yangiariq')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage(null)

    if (!firstName.trim() || !lastName.trim() || !phoneNumber.trim() || !password) {
      setErrorMessage(t.auth.messages.requiredFields)
      return
    }

    if (password.length < 8) {
      setErrorMessage(t.auth.messages.passwordTooShort)
      return
    }

    if (password !== confirmPassword) {
      setErrorMessage(t.auth.messages.passwordMismatch)
      return
    }

    const cleanPhone = phoneNumber.replace(/[\s-]/g, '')
    const phoneRegex = /^\+?[0-9]{9,15}$/
    if (!phoneRegex.test(cleanPhone)) {
      setErrorMessage(t.auth.messages.phoneInvalid)
      return
    }

    setIsLoading(true)
    try {
      await register({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        phoneNumber: cleanPhone,
        email: email.trim() ? email.trim() : undefined,
        district: district.trim() ? district.trim() : undefined,
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
    <form onSubmit={handleSubmit} className="space-y-3.5">
      {errorMessage && (
        <div className="flex items-center gap-2 p-3 text-xs font-medium text-destructive bg-destructive/10 border border-destructive/20 rounded-lg animate-in fade-in">
          <AlertCircle className="h-4 w-4 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      <div className="grid grid-cols-2 gap-3">
        <Input
          label={t.auth.firstNameLabel}
          placeholder={t.auth.firstNamePlaceholder}
          value={firstName}
          onChange={(e) => setFirstName(e.target.value)}
          leftIcon={<User className="h-4 w-4" />}
          required
        />
        <Input
          label={t.auth.lastNameLabel}
          placeholder={t.auth.lastNamePlaceholder}
          value={lastName}
          onChange={(e) => setLastName(e.target.value)}
          leftIcon={<User className="h-4 w-4" />}
          required
        />
      </div>

      <Input
        label={t.auth.phoneLabel}
        placeholder={t.auth.phonePlaceholder}
        value={phoneNumber}
        onChange={(e) => setPhoneNumber(e.target.value)}
        leftIcon={<Phone className="h-4 w-4" />}
        autoComplete="tel"
        required
      />

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <Input
          label={t.auth.emailLabel}
          type="email"
          placeholder={t.auth.emailPlaceholder}
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          leftIcon={<Mail className="h-4 w-4" />}
          autoComplete="email"
        />

        <Input
          label={t.auth.districtLabel}
          value={district}
          onChange={(e) => setDistrict(e.target.value)}
          leftIcon={<MapPin className="h-4 w-4" />}
        />
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
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
          autoComplete="new-password"
          required
        />

        <Input
          label={t.auth.confirmPasswordLabel}
          type={showPassword ? 'text' : 'password'}
          placeholder={t.auth.passwordPlaceholder}
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          leftIcon={<Lock className="h-4 w-4" />}
          autoComplete="new-password"
          required
        />
      </div>

      <Button
        type="submit"
        variant="primary"
        size="md"
        className="w-full mt-3 font-semibold shadow-md"
        isLoading={isLoading}
      >
        {t.auth.registerAction}
      </Button>

      {onSwitchToLogin && (
        <div className="text-center pt-2 border-t border-border/50">
          <p className="text-xs text-muted-foreground">
            {t.auth.haveAccount}{' '}
            <button
              type="button"
              onClick={onSwitchToLogin}
              className="font-semibold text-primary hover:underline ml-1"
            >
              {t.auth.signIn}
            </button>
          </p>
        </div>
      )}
    </form>
  )
}
