import React, { useState } from 'react'
import { Modal } from '@/shared/ui'
import { LoginForm } from './LoginForm'
import { RegisterForm } from './RegisterForm'
import { PasswordResetModal } from './PasswordResetModal'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { Droplets } from 'lucide-react'

export type AuthModalView = 'login' | 'register' | 'forgot-password'

interface AuthModalProps {
  isOpen: boolean
  onClose: () => void
  initialView?: AuthModalView
  lang?: SupportedLanguage
}

export const AuthModal: React.FC<AuthModalProps> = ({
  isOpen,
  onClose,
  initialView = 'login',
  lang = 'uz',
}) => {
  const t = translations[lang]
  const [view, setView] = useState<AuthModalView>(initialView)

  // Sync initial view when modal opens
  React.useEffect(() => {
    if (isOpen) {
      setView(initialView)
    }
  }, [isOpen, initialView])

  if (!isOpen) return null

  if (view === 'forgot-password') {
    return (
      <PasswordResetModal
        isOpen={isOpen}
        onClose={onClose}
        onSuccessReturnToLogin={() => setView('login')}
        lang={lang}
      />
    )
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      maxWidth={view === 'register' ? 'lg' : 'md'}
    >
      <div className="space-y-4">
        {/* Brand Header */}
        <div className="text-center space-y-1.5 pb-2">
          <div className="inline-flex items-center justify-center h-12 w-12 rounded-xl bg-primary/10 text-primary mb-1 shadow-sm">
            <Droplets className="h-6 w-6 text-primary" />
          </div>
          <h2 className="text-xl font-bold tracking-tight text-foreground">
            {view === 'login' ? t.auth.loginTitle : t.auth.registerTitle}
          </h2>
          <p className="text-xs text-muted-foreground max-w-sm mx-auto">
            {view === 'login' ? t.auth.loginSubtitle : t.auth.registerSubtitle}
          </p>

          {/* Toggle Pills */}
          <div className="inline-flex p-1 bg-muted rounded-lg text-xs font-medium mt-3 border border-border">
            <button
              type="button"
              onClick={() => setView('login')}
              className={`px-4 py-1.5 rounded-md transition-all ${
                view === 'login'
                  ? 'bg-background text-foreground shadow-sm font-semibold'
                  : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              {t.auth.signIn}
            </button>
            <button
              type="button"
              onClick={() => setView('register')}
              className={`px-4 py-1.5 rounded-md transition-all ${
                view === 'register'
                  ? 'bg-background text-foreground shadow-sm font-semibold'
                  : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              {t.auth.signUp}
            </button>
          </div>
        </div>

        {/* Content Form */}
        {view === 'login' ? (
          <LoginForm
            lang={lang}
            onSuccess={onClose}
            onSwitchToRegister={() => setView('register')}
            onSwitchToForgotPassword={() => setView('forgot-password')}
          />
        ) : (
          <RegisterForm
            lang={lang}
            onSuccess={onClose}
            onSwitchToLogin={() => setView('login')}
          />
        )}
      </div>
    </Modal>
  )
}
