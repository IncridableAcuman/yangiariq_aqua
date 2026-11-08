import React from 'react'
import {
  LayoutDashboard,
  MapPin,
  Map,
  Droplets,
  Cpu,
  Sparkles,
  AlertTriangle,
  Settings,
  ChevronLeft,
  ChevronRight,
  LogOut,
  LogIn,
  UserPlus,
  Globe,
  Radio,
  ShieldAlert,
  AlertOctagon,
  Workflow,
  BarChart3,
  Compass,
  Network,
  Scale,
  FileSearch,
  Gauge,
} from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { useAuth } from '@/features/auth/context/AuthContext'
import { UserAvatar, UserRoleBadge } from '@/entities/user'
import { Button } from '@/shared/ui/button'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/shared/ui/tooltip'
import { cn } from '@/shared/lib/utils'

export type NavSection =
  | 'dashboard'
  | 'fields'
  | 'gis'
  | 'irrigation'
  | 'operations'
  | 'analytics'
  | 'predictions'
  | 'sensors'
  | 'anomalies'
  | 'risks'
  | 'network'
  | 'water-accounting'
  | 'accountability'
  | 'performance'
  | 'ai-advisor'
  | 'problems'
  | 'settings'

interface SidebarProps {
  currentSection: NavSection
  onSelectSection: (section: NavSection) => void
  lang: SupportedLanguage
  isCollapsed: boolean
  onToggleCollapse: () => void
  onOpenAuthModal?: (view: 'login' | 'register') => void
  onOpenProfileModal?: () => void
  onGoToLanding?: () => void
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentSection,
  onSelectSection,
  lang,
  isCollapsed,
  onToggleCollapse,
  onOpenAuthModal,
  onOpenProfileModal,
  onGoToLanding,
}) => {
  const t = translations[lang]
  const { user, isAuthenticated, logout } = useAuth()

  const navItems = [
    {
      id: 'dashboard' as NavSection,
      label: t.nav.dashboard,
      icon: LayoutDashboard,
      badge: null,
    },
    {
      id: 'fields' as NavSection,
      label: t.nav.fields,
      icon: MapPin,
      badge: null,
    },
    {
      id: 'gis' as NavSection,
      label: t.nav.gis,
      icon: Map,
      badge: 'GIS',
      badgeVariant: 'secondary' as const,
    },
    {
      id: 'irrigation' as NavSection,
      label: t.nav.irrigation,
      icon: Droplets,
      badge: null,
    },
    {
      id: 'operations' as NavSection,
      label: t.nav.operations,
      icon: Workflow,
      badge: 'Action',
      badgeVariant: 'secondary' as const,
    },
    {
      id: 'analytics' as NavSection,
      label: t.nav.analytics,
      icon: BarChart3,
      badge: 'Stats',
      badgeVariant: 'secondary' as const,
    },
    {
      id: 'predictions' as NavSection,
      label: t.nav.predictions,
      icon: Compass,
      badge: 'Forecast',
      badgeVariant: 'secondary' as const,
    },
    {
      id: 'sensors' as NavSection,
      label: t.nav.sensors,
      icon: Cpu,
      badge: 'Live',
      badgeVariant: 'success' as const,
    },
    {
      id: 'anomalies' as NavSection,
      label: t.nav.anomalies,
      icon: ShieldAlert,
      badge: null,
    },
    {
      id: 'risks' as NavSection,
      label: t.nav.risks,
      icon: AlertOctagon,
      badge: 'Risk',
      badgeVariant: 'warning' as const,
    },
    {
      id: 'network' as NavSection,
      label: t.nav.network,
      icon: Network,
      badge: '08.14',
      badgeVariant: 'secondary' as const,
    },
    {
      id: 'water-accounting' as NavSection,
      label: t.nav.waterAccounting,
      icon: Scale,
      badge: '08.15',
      badgeVariant: 'secondary' as const,
    },
    {
      id: 'accountability' as NavSection,
      label: t.nav.accountability,
      icon: FileSearch,
      badge: '08.16',
      badgeVariant: 'secondary' as const,
    },
    {
      id: 'performance' as NavSection,
      label: t.nav.performance,
      icon: Gauge,
      badge: '08.17',
      badgeVariant: 'secondary' as const,
    },
    {
      id: 'ai-advisor' as NavSection,
      label: t.nav.aiAdvisor,
      icon: Sparkles,
      badge: 'AI',
      badgeVariant: 'secondary' as const,
    },
    {
      id: 'problems' as NavSection,
      label: t.nav.problems,
      icon: AlertTriangle,
      badge: '2',
      badgeVariant: 'warning' as const,
    },
    {
      id: 'settings' as NavSection,
      label: t.nav.settings,
      icon: Settings,
      badge: null,
    },
  ]

  return (
    <TooltipProvider delayDuration={200}>
      <aside
        className={cn(
          'relative flex flex-col h-screen bg-card border-r border-border transition-all duration-300 ease-in-out z-30 select-none shadow-sm',
          isCollapsed ? 'w-20' : 'w-64'
        )}
      >
        {/* Brand Header */}
        <div className="h-16 flex items-center justify-between px-4 border-b border-border">
          <div
            className={cn(
              'flex items-center gap-3 overflow-hidden cursor-pointer group',
              isCollapsed && 'justify-center w-full'
            )}
            onClick={() => (onGoToLanding ? onGoToLanding() : onSelectSection('dashboard'))}
            title={lang === 'uz' ? 'Bosh sahifaga (Landing) oʻtish' : 'На главную'}
          >
            <div className="h-10 w-10 shrink-0 rounded-xl bg-primary/10 text-primary flex items-center justify-center font-bold shadow-xs group-hover:scale-105 transition-transform">
              <Droplets className="h-6 w-6 text-primary animate-pulse" />
            </div>
            {!isCollapsed && (
              <div className="flex flex-col truncate">
                <span className="font-extrabold text-base tracking-tight text-foreground leading-tight truncate">
                  Aqua AI
                </span>
                <span className="text-[11px] text-muted-foreground truncate font-medium">
                  {lang === 'uz' ? 'Yangiariq Suv Boshqaruvi' : lang === 'ru' ? 'Янгиарык Агро-Вода' : 'Yangiariq Water AI'}
                </span>
              </div>
            )}
          </div>
        </div>

        {/* Collapsed Toggle Button */}
        <button
          type="button"
          onClick={onToggleCollapse}
          className="absolute -right-3 top-20 h-6 w-6 rounded-full bg-background border border-border shadow-md flex items-center justify-center text-muted-foreground hover:text-foreground hover:bg-muted transition-colors z-40 hidden lg:flex"
          aria-label={isCollapsed ? 'Expand Sidebar' : 'Collapse Sidebar'}
        >
          {isCollapsed ? (
            <ChevronRight className="h-3.5 w-3.5" />
          ) : (
            <ChevronLeft className="h-3.5 w-3.5" />
          )}
        </button>

        {/* Navigation Items List */}
        <div className="flex-1 overflow-y-auto px-3 py-4 space-y-1.5 scrollbar-thin scrollbar-thumb-muted">
          <div className="px-2 mb-2">
            {!isCollapsed ? (
              <span className="text-[11px] font-bold uppercase tracking-wider text-muted-foreground/70">
                {lang === 'uz' ? 'Asosiy Menyu' : lang === 'ru' ? 'Главное Меню' : 'Main Menu'}
              </span>
            ) : (
              <div className="h-px bg-border my-1" />
            )}
          </div>

          {navItems.map((item) => {
            const Icon = item.icon
            const isActive = currentSection === item.id

            const navButton = (
              <button
                key={item.id}
                type="button"
                onClick={() => onSelectSection(item.id)}
                className={cn(
                  'w-full flex items-center gap-3 px-3 py-2.5 rounded-xl font-medium text-sm transition-all duration-150 group relative',
                  isActive
                    ? 'bg-primary text-primary-foreground shadow-xs font-semibold'
                    : 'text-muted-foreground hover:bg-muted hover:text-foreground',
                  isCollapsed && 'justify-center px-0'
                )}
              >
                <Icon
                  className={cn(
                    'h-5 w-5 shrink-0 transition-transform group-hover:scale-110',
                    isActive ? 'text-primary-foreground' : 'text-muted-foreground group-hover:text-foreground'
                  )}
                />

                {!isCollapsed && (
                  <span className="truncate flex-1 text-left">{item.label}</span>
                )}

                {!isCollapsed && item.badge && (
                  <span
                    className={cn(
                      'ml-auto text-[10px] font-bold px-2 py-0.5 rounded-full shrink-0',
                      isActive
                        ? 'bg-primary-foreground/20 text-primary-foreground'
                        : item.badgeVariant === 'success'
                        ? 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400'
                        : item.badgeVariant === 'warning'
                        ? 'bg-amber-500/15 text-amber-600 dark:text-amber-400'
                        : 'bg-muted text-muted-foreground'
                    )}
                  >
                    {item.badge}
                  </span>
                )}

                {/* Collapsed dot badge */}
                {isCollapsed && item.badge && (
                  <span
                    className={cn(
                      'absolute top-2 right-2 h-2 w-2 rounded-full ring-2 ring-card',
                      item.badgeVariant === 'warning'
                        ? 'bg-amber-500'
                        : 'bg-emerald-500'
                    )}
                  />
                )}
              </button>
            )

            if (isCollapsed) {
              return (
                <Tooltip key={item.id}>
                  <TooltipTrigger asChild>{navButton}</TooltipTrigger>
                  <TooltipContent side="right" className="font-medium">
                    {item.label}
                  </TooltipContent>
                </Tooltip>
              )
            }

            return navButton
          })}
        </div>

        {/* Live Hydro Sensor Telemetry Indicator */}
        {!isCollapsed && (
          <div className="mx-3 mb-3 p-3 rounded-xl bg-gradient-to-br from-hydro-500/10 to-primary/5 border border-primary/20 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-[11px] font-bold text-foreground flex items-center gap-1.5">
                <Radio className="h-3 w-3 text-emerald-500 animate-pulse" />
                {lang === 'uz' ? 'Yangiariq Kanali' : lang === 'ru' ? 'Канал Янгиарык' : 'Canal Status'}
              </span>
              <span className="text-[10px] font-semibold text-emerald-600 bg-emerald-500/10 px-1.5 py-0.5 rounded-md">
                NORM
              </span>
            </div>
            <div className="flex items-center justify-between text-xs text-muted-foreground font-mono">
              <span>Q = 14.8 m³/s</span>
              <span>pH 7.2</span>
            </div>
          </div>
        )}

        {/* User Account / Footer */}
        <div className="p-3 border-t border-border bg-card/50">
          {isAuthenticated && user ? (
            <div
              className={cn(
                'flex items-center gap-3 p-2 rounded-xl transition-colors hover:bg-muted/80',
                isCollapsed && 'justify-center p-0 py-2'
              )}
            >
              <div
                className="cursor-pointer shrink-0"
                onClick={onOpenProfileModal}
                title={user.fullName || user.phoneNumber}
              >
                <UserAvatar user={user} size="sm" />
              </div>

              {!isCollapsed && (
                <div
                  className="flex-1 min-w-0 cursor-pointer"
                  onClick={onOpenProfileModal}
                >
                  <p className="text-xs font-bold text-foreground truncate leading-tight">
                    {user.firstName} {user.lastName}
                  </p>
                  <div className="mt-1">
                    <UserRoleBadge
                      role={user.role}
                      lang={lang}
                      showIcon={false}
                      className="py-0 px-1.5 text-[9px]"
                    />
                  </div>
                </div>
              )}

              {!isCollapsed && (
                <div className="flex items-center gap-1">
                  {onGoToLanding && (
                    <button
                      type="button"
                      onClick={onGoToLanding}
                      className="p-1.5 rounded-lg text-muted-foreground hover:text-primary hover:bg-primary/10 transition-colors"
                      title={lang === 'uz' ? 'Landing sahifasiga oʻtish' : 'На лендинг'}
                    >
                      <Globe className="h-4 w-4" />
                    </button>
                  )}
                  <button
                    type="button"
                    onClick={async () => {
                      await logout()
                      onGoToLanding?.()
                    }}
                    className="p-1.5 rounded-lg text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-colors"
                    title={t.auth.signOut}
                  >
                    <LogOut className="h-4 w-4" />
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="space-y-2">
              {!isCollapsed ? (
                <div className="grid grid-cols-2 gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    className="w-full text-xs"
                    onClick={() => onOpenAuthModal?.('login')}
                  >
                    <LogIn className="h-3.5 w-3.5 mr-1" />
                    {t.auth.signIn}
                  </Button>
                  <Button
                    variant="default"
                    size="sm"
                    className="w-full text-xs"
                    onClick={() => onOpenAuthModal?.('register')}
                  >
                    <UserPlus className="h-3.5 w-3.5 mr-1" />
                    {t.auth.signUp}
                  </Button>
                </div>
              ) : (
                <Tooltip>
                  <TooltipTrigger asChild>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="w-full"
                      onClick={() => onOpenAuthModal?.('login')}
                    >
                      <LogIn className="h-4 w-4" />
                    </Button>
                  </TooltipTrigger>
                  <TooltipContent side="right">
                    {t.auth.signIn}
                  </TooltipContent>
                </Tooltip>
              )}
            </div>
          )}
        </div>
      </aside>
    </TooltipProvider>
  )
}
