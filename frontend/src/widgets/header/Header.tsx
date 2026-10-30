import React, { useState } from 'react'
import { Droplets, LogIn, UserPlus, LogOut, ChevronDown } from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { useAuth } from '@/features/auth/context/AuthContext'
import { AuthModal, AuthModalView } from '@/features/auth/ui/AuthModal'
import { UserProfileModal } from '@/features/auth/ui/UserProfileModal'
import { ChangePasswordModal } from '@/features/auth/ui/ChangePasswordModal'
import { UserAvatar, UserRoleBadge } from '@/entities/user'
import { Button } from '@/shared/ui/Button'

interface HeaderProps {
  lang: SupportedLanguage
  onLangChange: (lang: SupportedLanguage) => void
}

export const Header: React.FC<HeaderProps> = ({ lang, onLangChange }) => {
  const t = translations[lang]
  const { user, isAuthenticated, logout, isLoading } = useAuth()

  const [authModalOpen, setAuthModalOpen] = useState(false)
  const [authModalView, setAuthModalView] = useState<AuthModalView>('login')
  const [profileModalOpen, setProfileModalOpen] = useState(false)
  const [changePasswordModalOpen, setChangePasswordModalOpen] = useState(false)
  const [userDropdownOpen, setUserDropdownOpen] = useState(false)

  const openLogin = () => {
    setAuthModalView('login')
    setAuthModalOpen(true)
  }

  const openRegister = () => {
    setAuthModalView('register')
    setAuthModalOpen(true)
  }

  return (
    <>
      <header className="sticky top-0 z-40 border-b border-border bg-card/80 backdrop-blur-md px-4 sm:px-6 py-3.5 flex items-center justify-between shadow-xs">
        {/* Brand */}
        <div className="flex items-center space-x-3">
          <div className="h-10 w-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center font-bold shadow-sm">
            <Droplets className="h-6 w-6 text-primary" />
          </div>
          <div>
            <h1 className="text-lg sm:text-xl font-bold tracking-tight text-foreground leading-tight">
              {t.app.title}
            </h1>
            <p className="text-xs text-muted-foreground hidden sm:block">
              {t.app.subtitle}
            </p>
          </div>
        </div>

        {/* Right Actions */}
        <div className="flex items-center space-x-3 sm:space-x-4">
          {/* District Status Badge */}
          <div className="hidden lg:flex items-center space-x-2 text-xs text-muted-foreground bg-muted/60 px-2.5 py-1.5 rounded-full border border-border/60">
            <span className="inline-block h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
            <span className="font-medium">{t.common.district}</span>
          </div>

          {/* Language Selector */}
          <div className="flex items-center space-x-1 border rounded-lg p-1 bg-background text-xs shadow-xs">
            {(['uz', 'ru', 'en'] as SupportedLanguage[]).map((locale) => (
              <button
                key={locale}
                onClick={() => onLangChange(locale)}
                className={`px-2 py-1 rounded-md font-medium transition-colors ${
                  lang === locale
                    ? 'bg-primary text-primary-foreground font-semibold shadow-xs'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                {locale.toUpperCase()}
              </button>
            ))}
          </div>

          {/* Authentication Section */}
          <div className="flex items-center space-x-2 pl-2 border-l border-border">
            {isLoading ? (
              <div className="h-9 w-24 bg-muted animate-pulse rounded-lg" />
            ) : isAuthenticated && user ? (
              <div className="relative">
                <button
                  type="button"
                  onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                  className="flex items-center gap-2 p-1.5 rounded-xl hover:bg-muted/80 transition-colors border border-transparent hover:border-border"
                >
                  <UserAvatar user={user} size="sm" />
                  <div className="text-left hidden md:block">
                    <div className="text-xs font-bold text-foreground leading-none truncate max-w-[120px]">
                      {user.firstName} {user.lastName}
                    </div>
                    <div className="mt-1">
                      <UserRoleBadge role={user.role} lang={lang} showIcon={false} className="py-0 px-1.5 text-[10px]" />
                    </div>
                  </div>
                  <ChevronDown className="h-3.5 w-3.5 text-muted-foreground hidden sm:block" />
                </button>

                {/* Dropdown Menu */}
                {userDropdownOpen && (
                  <>
                    <div
                      className="fixed inset-0 z-30"
                      onClick={() => setUserDropdownOpen(false)}
                    />
                    <div className="absolute right-0 mt-2 w-56 rounded-xl border border-border bg-card p-2 shadow-xl z-40 text-xs animate-in fade-in zoom-in-95">
                      <div className="px-3 py-2 border-b border-border/60 mb-1">
                        <p className="font-semibold text-foreground truncate">{user.fullName}</p>
                        <p className="text-[11px] text-muted-foreground truncate">{user.phoneNumber}</p>
                        <div className="mt-1.5">
                          <UserRoleBadge role={user.role} lang={lang} />
                        </div>
                      </div>

                      <button
                        type="button"
                        onClick={() => {
                          setUserDropdownOpen(false)
                          setProfileModalOpen(true)
                        }}
                        className="w-full text-left px-3 py-2 rounded-lg hover:bg-muted text-foreground transition-colors flex items-center justify-between"
                      >
                        <span>{t.auth.profileDetails}</span>
                      </button>

                      <button
                        type="button"
                        onClick={() => {
                          setUserDropdownOpen(false)
                          setChangePasswordModalOpen(true)
                        }}
                        className="w-full text-left px-3 py-2 rounded-lg hover:bg-muted text-foreground transition-colors"
                      >
                        <span>{t.auth.changePassword}</span>
                      </button>

                      <div className="border-t border-border/60 my-1" />

                      <button
                        type="button"
                        onClick={async () => {
                          setUserDropdownOpen(false)
                          await logout()
                        }}
                        className="w-full text-left px-3 py-2 rounded-lg hover:bg-destructive/10 text-destructive transition-colors flex items-center gap-2 font-medium"
                      >
                        <LogOut className="h-3.5 w-3.5" />
                        <span>{t.auth.signOut}</span>
                      </button>
                    </div>
                  </>
                )}
              </div>
            ) : (
              <div className="flex items-center space-x-2">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={openLogin}
                  leftIcon={<LogIn className="h-4 w-4" />}
                >
                  {t.auth.signIn}
                </Button>

                <Button
                  variant="primary"
                  size="sm"
                  onClick={openRegister}
                  leftIcon={<UserPlus className="h-4 w-4" />}
                  className="hidden sm:inline-flex shadow-xs"
                >
                  {t.auth.signUp}
                </Button>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* Auth Modal */}
      <AuthModal
        isOpen={authModalOpen}
        onClose={() => setAuthModalOpen(false)}
        initialView={authModalView}
        lang={lang}
      />

      {/* User Profile Modal */}
      <UserProfileModal
        isOpen={profileModalOpen}
        onClose={() => setProfileModalOpen(false)}
        onOpenChangePassword={() => setChangePasswordModalOpen(true)}
        lang={lang}
      />

      {/* Change Password Modal */}
      <ChangePasswordModal
        isOpen={changePasswordModalOpen}
        onClose={() => setChangePasswordModalOpen(false)}
        lang={lang}
      />
    </>
  )
}
