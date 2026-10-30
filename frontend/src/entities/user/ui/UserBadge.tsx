import React from 'react'
import { UserRole, UserStatus } from '../types'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { Shield, Tractor, Compass, CheckCircle2, AlertOctagon, XCircle } from 'lucide-react'

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
    FARMER: {
      label: t.auth.roles.FARMER,
      bg: 'bg-emerald-50 dark:bg-emerald-950/40',
      text: 'text-emerald-700 dark:text-emerald-300',
      border: 'border-emerald-200 dark:border-emerald-800',
      icon: <Tractor className="h-3.5 w-3.5" />,
    },
    OPERATOR: {
      label: t.auth.roles.OPERATOR,
      bg: 'bg-sky-50 dark:bg-sky-950/40',
      text: 'text-sky-700 dark:text-sky-300',
      border: 'border-sky-200 dark:border-sky-800',
      icon: <Compass className="h-3.5 w-3.5" />,
    },
    ADMIN: {
      label: t.auth.roles.ADMIN,
      bg: 'bg-purple-50 dark:bg-purple-950/40',
      text: 'text-purple-700 dark:text-purple-300',
      border: 'border-purple-200 dark:border-purple-800',
      icon: <Shield className="h-3.5 w-3.5" />,
    },
  }

  const config = configs[role] || configs.FARMER

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border ${config.bg} ${config.text} ${config.border} ${className}`}
    >
      {showIcon && config.icon}
      <span>{config.label}</span>
    </span>
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

  const configs: Record<UserStatus, { label: string; bg: string; text: string; icon: React.ReactNode }> = {
    ACTIVE: {
      label: t.auth.statuses.ACTIVE,
      bg: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300',
      text: 'text-emerald-500',
      icon: <CheckCircle2 className="h-3 w-3" />,
    },
    INACTIVE: {
      label: t.auth.statuses.INACTIVE,
      bg: 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300',
      text: 'text-gray-400',
      icon: <AlertOctagon className="h-3 w-3" />,
    },
    BLOCKED: {
      label: t.auth.statuses.BLOCKED,
      bg: 'bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-300',
      text: 'text-rose-500',
      icon: <XCircle className="h-3 w-3" />,
    },
  }

  const config = configs[status] || configs.ACTIVE

  return (
    <span
      className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${config.bg} ${className}`}
    >
      {config.icon}
      <span>{config.label}</span>
    </span>
  )
}
