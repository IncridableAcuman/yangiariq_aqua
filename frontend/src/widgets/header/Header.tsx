import React, { useState } from 'react'
import {
  Droplets,
  LogIn,
  UserPlus,
  LogOut,
  ChevronDown,
  Menu,
  User,
  KeyRound,
  Compass,
} from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { useAuth } from '@/features/auth/context/AuthContext'
import { AuthModal, AuthModalView } from '@/features/auth/ui/AuthModal'
import { UserProfileModal } from '@/features/auth/ui/UserProfileModal'
import { ChangePasswordModal } from '@/features/auth/ui/ChangePasswordModal'
import { UserAvatar, UserRoleBadge } from '@/entities/user'
import { Button } from '@/shared/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/ui/dropdown-menu'
import { NavSection } from '@/widgets/sidebar'

interface HeaderProps {
  lang: SupportedLanguage
  onLangChange: (lang: SupportedLanguage) => void
  currentSection?: NavSection
  onOpenMobileMenu?: () => void
  onOpenAuthModal?: (view: AuthModalView) => void
  onOpenProfileModal?: () => void
  onGoToLanding?: () => void
}

export const Header: React.FC<HeaderProps> = ({
  lang,
  onLangChange,
  currentSection = 'dashboard',
  onOpenMobileMenu,
  onGoToLanding,
}) => {
  const t = translations[lang]
  const { user, isAuthenticated, logout, isLoading } = useAuth()

  const [authModalOpen, setAuthModalOpen] = useState(false)
  const [authModalView, setAuthModalView] = useState<AuthModalView>('login')
  const [profileModalOpen, setProfileModalOpen] = useState(false)
  const [changePasswordModalOpen, setChangePasswordModalOpen] = useState(false)

  const openLogin = () => {
    setAuthModalView('login')
    setAuthModalOpen(true)
  }

  const openRegister = () => {
    setAuthModalView('register')
    setAuthModalOpen(true)
  }

  const sectionTitles: Record<NavSection, string> = {
    dashboard: t.nav.dashboard,
    fields: t.nav.fields,
    gis: t.nav.gis,
    irrigation: t.nav.irrigation,
    operations: t.nav.operations,
    analytics: t.nav.analytics,
    predictions: t.nav.predictions,
    sensors: t.nav.sensors,
    anomalies: t.nav.anomalies,
    risks: t.nav.risks,
    network: t.nav.network,
    'water-accounting': t.nav.waterAccounting,
    'ai-advisor': t.nav.aiAdvisor,
    problems: t.nav.problems,
    settings: t.nav.settings,
  }

  return (
    <>
      <header className="sticky top-0 z-20 border-b border-border bg-card/85 backdrop-blur-md px-4 sm:px-6 py-3 flex items-center justify-between shadow-2xs">
        {/* Left Section: Mobile Menu Trigger + Breadcrumb / Active Section */}
        <div className="flex items-center space-x-3">
          <Button
            variant="ghost"
            size="icon"
            className="lg:hidden h-9 w-9 text-muted-foreground hover:text-foreground"
            onClick={onOpenMobileMenu}
            aria-label="Open Navigation Menu"
          >
            <Menu className="h-5 w-5" />
          </Button>

          <div className="flex items-center space-x-2.5">
            <div className="lg:hidden h-8 w-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center font-bold">
              <Droplets className="h-4 w-4" />
            </div>
            <div>
              <h1 className="text-base sm:text-lg font-bold tracking-tight text-foreground leading-none">
                {sectionTitles[currentSection] || t.app.title}
              </h1>
              <div className="flex items-center gap-1.5 text-[11px] text-muted-foreground mt-0.5">
                <Compass className="h-3 w-3 text-emerald-500" />
                <span>{t.common.district}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Right Actions */}
        <div className="flex items-center space-x-2 sm:space-x-3">
          {/* Hydro Status Badge */}
          <div className="hidden md:flex items-center space-x-2 text-xs text-muted-foreground bg-muted/60 px-3 py-1.5 rounded-full border border-border/60">
            <span className="inline-block h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
            <span className="font-medium font-mono text-[11px]">
              {lang === 'uz' ? 'Gidrouzel: Faol' : lang === 'ru' ? 'Гидроузел: Активен' : 'Hydro: Active'}
            </span>
          </div>

          {/* Language Selector */}
          <div className="flex items-center space-x-0.5 border border-border rounded-lg p-0.5 bg-background text-xs shadow-2xs">
            {(['uz', 'ru', 'en'] as SupportedLanguage[]).map((locale) => (
              <button
                key={locale}
                onClick={() => onLangChange(locale)}
                className={`px-2 py-1 rounded-md text-[11px] font-medium transition-colors ${
                  lang === locale
                    ? 'bg-primary text-primary-foreground font-semibold shadow-2xs'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                {locale.toUpperCase()}
              </button>
            ))}
          </div>

          {/* Authentication Section */}
          <div className="flex items-center pl-2 border-l border-border">
            {isLoading ? (
              <div className="h-9 w-24 bg-muted animate-pulse rounded-lg" />
            ) : isAuthenticated && user ? (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <button
                    type="button"
                    className="flex items-center gap-2 p-1.5 rounded-xl hover:bg-muted/80 transition-colors border border-transparent hover:border-border cursor-pointer outline-none"
                  >
                    <UserAvatar user={user} size="sm" />
                    <div className="text-left hidden sm:block">
                      <div className="text-xs font-bold text-foreground leading-none truncate max-w-[120px]">
                        {user.firstName} {user.lastName}
                      </div>
                      <div className="mt-1">
                        <UserRoleBadge
                          role={user.role}
                          lang={lang}
                          showIcon={false}
                          className="py-0 px-1 text-[9px]"
                        />
                      </div>
                    </div>
                    <ChevronDown className="h-3.5 w-3.5 text-muted-foreground hidden sm:block" />
                  </button>
                </DropdownMenuTrigger>

                <DropdownMenuContent align="end" className="w-56">
                  <DropdownMenuLabel className="font-normal p-2">
                    <p className="font-semibold text-foreground text-sm truncate">{user.fullName}</p>
                    <p className="text-[11px] text-muted-foreground truncate">{user.phoneNumber}</p>
                    <div className="mt-2">
                      <UserRoleBadge role={user.role} lang={lang} />
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => setProfileModalOpen(true)}>
                    <User className="h-4 w-4 mr-2" />
                    <span>{t.auth.profileDetails}</span>
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => setChangePasswordModalOpen(true)}>
                    <KeyRound className="h-4 w-4 mr-2" />
                    <span>{t.auth.changePassword}</span>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    onClick={async () => {
                      await logout()
                      onGoToLanding?.()
                    }}
                    className="text-destructive focus:bg-destructive/10 focus:text-destructive"
                  >
                    <LogOut className="h-4 w-4 mr-2" />
                    <span>{t.auth.signOut}</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <div className="flex items-center space-x-1.5">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={openLogin}
                  className="text-xs hidden sm:inline-flex"
                >
                  <LogIn className="h-3.5 w-3.5 mr-1" />
                  {t.auth.signIn}
                </Button>
                <Button
                  variant="default"
                  size="sm"
                  onClick={openRegister}
                  className="text-xs"
                >
                  <UserPlus className="h-3.5 w-3.5 mr-1" />
                  <span className="hidden xs:inline">{t.auth.signUp}</span>
                  <span className="xs:hidden">{t.auth.signIn}</span>
                </Button>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* Global Modals for Auth & Profile */}
      <AuthModal
        isOpen={authModalOpen}
        onClose={() => setAuthModalOpen(false)}
        initialView={authModalView}
        lang={lang}
      />
      <UserProfileModal
        isOpen={profileModalOpen}
        onClose={() => setProfileModalOpen(false)}
        onOpenChangePassword={() => {
          setProfileModalOpen(false)
          setChangePasswordModalOpen(true)
        }}
        lang={lang}
      />
      <ChangePasswordModal
        isOpen={changePasswordModalOpen}
        onClose={() => setChangePasswordModalOpen(false)}
        lang={lang}
      />
    </>
  )
}
