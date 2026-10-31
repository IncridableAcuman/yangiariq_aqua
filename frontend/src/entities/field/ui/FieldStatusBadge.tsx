import React from 'react'
import { Badge } from '@/shared/ui/badge'
import { FieldStatus } from '../types'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { cn } from '@/shared/lib/utils'

interface FieldStatusBadgeProps {
  status: FieldStatus
  lang?: SupportedLanguage
  className?: string
}

export const FieldStatusBadge: React.FC<FieldStatusBadgeProps> = ({
  status,
  lang = 'uz',
  className,
}) => {
  const isActive = status === 'ACTIVE'
  const label = translations[lang]?.fields?.status?.[status] || status

  return (
    <Badge
      variant="outline"
      className={cn(
        'font-medium tracking-wide text-xs px-2.5 py-0.5 inline-flex items-center gap-1.5 transition-colors',
        isActive
          ? 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border-emerald-300 dark:border-emerald-800'
          : 'bg-muted/80 text-muted-foreground border-border',
        className
      )}
    >
      <span
        className={cn(
          'h-1.5 w-1.5 rounded-full shrink-0',
          isActive
            ? 'bg-emerald-500 animate-pulse'
            : 'bg-muted-foreground/60'
        )}
      />
      {label}
    </Badge>
  )
}
