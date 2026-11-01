import React from 'react'
import { Badge } from '@/shared/ui/badge'
import { AlertOctagon, AlertTriangle, CheckCircle2, Info } from 'lucide-react'
import { PriorityLevel } from '@/entities/dashboard'
import { SupportedLanguage } from '@/shared/i18n'

interface PriorityBadgeProps {
  priority: PriorityLevel
  score?: number
  lang: SupportedLanguage
  className?: string
  showScore?: boolean
}

export const PriorityBadge: React.FC<PriorityBadgeProps> = ({
  priority,
  score,
  lang,
  className = '',
  showScore = true,
}) => {
  const labels: Record<SupportedLanguage, Record<PriorityLevel, string>> = {
    uz: {
      CRITICAL: 'Kritik',
      HIGH: 'Yuqori',
      MEDIUM: 'Oʻrta',
      LOW: 'Oddiy',
    },
    ru: {
      CRITICAL: 'Критический',
      HIGH: 'Высокий',
      MEDIUM: 'Средний',
      LOW: 'Низкий',
    },
    en: {
      CRITICAL: 'Critical',
      HIGH: 'High',
      MEDIUM: 'Medium',
      LOW: 'Low',
    },
  }

  const label = labels[lang]?.[priority] || priority
  const scoreSuffix = showScore && typeof score === 'number' ? ` (${score})` : ''

  switch (priority) {
    case 'CRITICAL':
      return (
        <Badge
          variant="outline"
          className={`bg-rose-500/15 text-rose-700 border-rose-400 dark:bg-rose-950/50 dark:text-rose-300 dark:border-rose-700 gap-1 font-semibold text-xs py-0.5 px-2 ${className}`}
        >
          <AlertOctagon className="w-3.5 h-3.5 text-rose-600 dark:text-rose-400 animate-pulse shrink-0" />
          <span>{label}{scoreSuffix}</span>
        </Badge>
      )

    case 'HIGH':
      return (
        <Badge
          variant="outline"
          className={`bg-amber-500/15 text-amber-700 border-amber-400 dark:bg-amber-950/50 dark:text-amber-300 dark:border-amber-700 gap-1 font-medium text-xs py-0.5 px-2 ${className}`}
        >
          <AlertTriangle className="w-3.5 h-3.5 text-amber-600 dark:text-amber-400 shrink-0" />
          <span>{label}{scoreSuffix}</span>
        </Badge>
      )

    case 'MEDIUM':
      return (
        <Badge
          variant="outline"
          className={`bg-sky-500/10 text-sky-700 border-sky-300 dark:bg-sky-950/40 dark:text-sky-300 dark:border-sky-800 gap-1 font-medium text-xs py-0.5 px-2 ${className}`}
        >
          <Info className="w-3.5 h-3.5 text-sky-600 dark:text-sky-400 shrink-0" />
          <span>{label}{scoreSuffix}</span>
        </Badge>
      )

    case 'LOW':
    default:
      return (
        <Badge
          variant="outline"
          className={`bg-emerald-500/10 text-emerald-700 border-emerald-300 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800 gap-1 font-medium text-xs py-0.5 px-2 ${className}`}
        >
          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400 shrink-0" />
          <span>{label}{scoreSuffix}</span>
        </Badge>
      )
  }
}
