import React from 'react'
import { UserRole, UserStatus } from '../types'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { Badge } from '@/shared/ui/badge'
import { Shield, Tractor, Compass, CheckCircle2, AlertOctagon, XCircle } from 'lucide-react'
import { cn } from '@/shared/lib/utils'

interface UserRoleBadgeProps {
  role: UserRole
  lang?: SupportedLanguage
  className?: string
  showIcon?: boolean
}

export const UserRoleBadge: React.FC<UserRoleBadgeProps> = ({
  role,
  lang = 'uz',
  className = '',
  showIcon = true,
}) => {
  const t = translations[lang]

  const configs: Record<UserRole, { label: string; bg: string; text: string; border: string; icon: React.ReactNode }> = {
    SUPER_ADMIN: {
      label: lang === 'uz' ? 'Bosh administrator' : lang === 'ru' ? 'Главный администратор' : 'Super Administrator',
      bg: 'bg-amber-50 dark:bg-amber-950/40',
      text: 'text-amber-700 dark:text-amber-300',
      border: 'border-amber-300 dark:border-amber-700',
      icon: <Shield className="h-3.5 w-3.5 text-amber-600 dark:text-amber-400" />,
    },
    ADMIN: {
      label: t.auth.roles.ADMIN,
      bg: 'bg-purple-50 dark:bg-purple-950/40',
      text: 'text-purple-700 dark:text-purple-300',
      border: 'border-purple-200 dark:border-purple-800',
      icon: <Shield className="h-3.5 w-3.5" />,
    },
    OPERATOR: {
      label: t.auth.roles.OPERATOR,
      bg: 'bg-sky-50 dark:bg-sky-950/40',
      text: 'text-sky-700 dark:text-sky-300',
      border: 'border-sky-200 dark:border-sky-800',
      icon: <Compass className="h-3.5 w-3.5" />,
    },
    FARMER: {
      label: t.auth.roles.FARMER,
      bg: 'bg-emerald-50 dark:bg-emerald-950/40',
      text: 'text-emerald-700 dark:text-emerald-300',
      border: 'border-emerald-200 dark:border-emerald-800',
      icon: <Tractor className="h-3.5 w-3.5" />,
    },
  }

  const config = configs[role] || configs.FARMER

  return (
    <Badge
      variant="outline"
      className={cn(
        'gap-1.5 font-semibold text-xs',
        config.bg,
        config.text,
        config.border,
        className
      )}
    >
      {showIcon && config.icon}
      <span>{config.label}</span>
    </Badge>
  )
}

interface UserStatusBadgeProps {
  status: UserStatus
  lang?: SupportedLanguage
  className?: string
}

export const UserStatusBadge: React.FC<UserStatusBadgeProps> = ({
  status,
  lang = 'uz',
  className = '',
}) => {
  const t = translations[lang]

  const configs: Record<UserStatus, { label: string; className: string; icon: React.ReactNode }> = {
    ACTIVE: {
      label: t.auth.statuses.ACTIVE,
      className: 'bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 border-emerald-500/30',
      icon: <CheckCircle2 className="h-3 w-3" />,
    },
    INACTIVE: {
      label: t.auth.statuses.INACTIVE,
      className: 'bg-muted text-muted-foreground border-border',
      icon: <AlertOctagon className="h-3 w-3" />,
    },
    BLOCKED: {
      label: t.auth.statuses.BLOCKED,
      className: 'bg-destructive/15 text-destructive border-destructive/30',
      icon: <XCircle className="h-3 w-3" />,
    },
  }

  const config = configs[status] || configs.ACTIVE

  return (
    <Badge
      variant="outline"
      className={cn('gap-1 font-medium text-xs', config.className, className)}
    >
      {config.icon}
      <span>{config.label}</span>
    </Badge>
  )
}
