import React from 'react'
import {
  LayoutDashboard,
  MapPin,
  Map,
  Droplets,
  Cpu,
  Sparkles,
  AlertTriangle,
  AlertOctagon,
  ShieldAlert,
  Settings,
  Workflow,
  Globe,
  LogOut,
  LogIn,
  UserPlus,
  BarChart3,
  Compass,
  Network,
  Scale,
  FileSearch,
  Gauge,
  Database,
  Shield,
} from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { useAuth } from '@/features/auth/context/AuthContext'
import { UserAvatar, UserRoleBadge } from '@/entities/user'
import { Button } from '@/shared/ui/button'
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/shared/ui/sheet'
import { NavSection } from './Sidebar'
import { cn } from '@/shared/lib/utils'

interface MobileSidebarProps {
  isOpen: boolean
  onClose: () => void
  currentSection: NavSection
  onSelectSection: (section: NavSection) => void
  lang: SupportedLanguage
  onOpenAuthModal?: (view: 'login' | 'register') => void
  onOpenProfileModal?: () => void
  onGoToLanding?: () => void
}

export const MobileSidebar: React.FC<MobileSidebarProps> = ({
  isOpen,
  onClose,
  currentSection,
  onSelectSection,
  lang,
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
      id: 'data-integration' as NavSection,
      label: t.nav.dataIntegration,
      icon: Database,
      badge: '08.19',
      badgeVariant: 'secondary' as const,
    },
    {
      id: 'pilot' as NavSection,
      label: t.nav.pilot,
      icon: Compass,
      badge: '08.20',
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
    ...(user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN'
      ? [
          {
            id: 'administration' as NavSection,
            label: t.nav.administration,
            icon: Shield,
            badge: '08.20.1',
            badgeVariant: 'secondary' as const,
          },
        ]
      : []),
    {
      id: 'settings' as NavSection,
      label: t.nav.settings,
      icon: Settings,
      badge: null,
    },
  ]

  const handleSelect = (id: NavSection) => {
    onSelectSection(id)
    onClose()
  }

  return (
    <Sheet open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <SheetContent side="left" className="p-0 flex flex-col w-72">
        {/* Header */}
        <SheetHeader
          className="p-4 border-b border-border flex flex-row items-center gap-3 text-left cursor-pointer"
          onClick={() => {
            onClose()
            onGoToLanding?.()
          }}
        >
          <div className="h-10 w-10 shrink-0 rounded-xl bg-primary/10 text-primary flex items-center justify-center font-bold">
            <Droplets className="h-6 w-6 text-primary" />
          </div>
          <div>
            <SheetTitle className="text-base font-extrabold tracking-tight">
              Aqua AI
            </SheetTitle>
            <p className="text-xs text-muted-foreground">
              {lang === 'uz' ? 'Yangiariq Suv Boshqaruvi' : 'Yangiariq Water AI'}
            </p>
          </div>
        </SheetHeader>

        {/* Nav List */}
        <div className="flex-1 overflow-y-auto p-3 space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon
            const isActive = currentSection === item.id

            return (
              <button
                key={item.id}
                type="button"
                onClick={() => handleSelect(item.id)}
                className={cn(
                  'w-full flex items-center gap-3 px-3 py-2.5 rounded-xl font-medium text-sm transition-colors text-left',
                  isActive
                    ? 'bg-primary text-primary-foreground font-semibold'
                    : 'text-muted-foreground hover:bg-muted hover:text-foreground'
                )}
              >
                <Icon className="h-5 w-5 shrink-0" />
                <span className="flex-1 truncate">{item.label}</span>
                {item.badge && (
                  <span
                    className={cn(
                      'text-[10px] font-bold px-2 py-0.5 rounded-full',
                      isActive
                        ? 'bg-primary-foreground/20 text-primary-foreground'
                        : item.badgeVariant === 'success'
                        ? 'bg-emerald-500/15 text-emerald-600'
                        : item.badgeVariant === 'warning'
                        ? 'bg-amber-500/15 text-amber-600'
                        : 'bg-muted text-muted-foreground'
                    )}
                  >
                    {item.badge}
                  </span>
                )}
              </button>
            )
          })}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-border bg-card/50">
          {isAuthenticated && user ? (
            <div className="space-y-3">
              <div
                className="flex items-center gap-3 cursor-pointer p-2 rounded-xl hover:bg-muted"
                onClick={() => {
                  onClose()
                  onOpenProfileModal?.()
                }}
              >
                <UserAvatar user={user} size="sm" />
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-bold text-foreground truncate">
                    {user.fullName || `${user.firstName} ${user.lastName}`}
                  </p>
                  <UserRoleBadge role={user.role} lang={lang} showIcon={false} className="py-0 px-1 text-[9px]" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    onClose()
                    onGoToLanding?.()
                  }}
                  className="w-full text-xs"
                >
                  <Globe className="h-4 w-4 mr-1.5" />
                  {lang === 'uz' ? 'Landing' : 'Лендинг'}
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  className="w-full text-xs text-destructive hover:bg-destructive/10 hover:text-destructive"
                  onClick={async () => {
                    onClose()
                    await logout()
                    onGoToLanding?.()
                  }}
                >
                  <LogOut className="h-4 w-4 mr-1.5" />
                  {t.auth.signOut}
                </Button>
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  onClose()
                  onOpenAuthModal?.('login')
                }}
              >
                <LogIn className="h-3.5 w-3.5 mr-1" />
                {t.auth.signIn}
              </Button>
              <Button
                variant="default"
                size="sm"
                onClick={() => {
                  onClose()
                  onOpenAuthModal?.('register')
                }}
              >
                <UserPlus className="h-3.5 w-3.5 mr-1" />
                {t.auth.signUp}
              </Button>
            </div>
          )}
        </div>
      </SheetContent>
    </Sheet>
  )
}
