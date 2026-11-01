import React from 'react'
import { Badge } from '@/shared/ui/badge'
import { AlertCircle, CheckCircle2, Clock, Eye, XCircle } from 'lucide-react'
import { AnomalyStatus } from '@/entities/anomaly'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface AnomalyStatusBadgeProps {
  status: AnomalyStatus
  lang: SupportedLanguage
  className?: string
}

export const AnomalyStatusBadge: React.FC<AnomalyStatusBadgeProps> = ({
  status,
  lang,
  className,
}) => {
  const t = translations[lang].anomalies.statuses

  switch (status) {
    case 'DETECTED':
      return (
        <Badge
          variant="outline"
          className={`bg-rose-50 text-rose-700 border-rose-300 dark:bg-rose-950/40 dark:text-rose-300 dark:border-rose-800 gap-1 ${className || ''}`}
        >
          <AlertCircle className="w-3 h-3 text-rose-500" />
          <span>{t.DETECTED}</span>
        </Badge>
      )

    case 'ACKNOWLEDGED':
      return (
        <Badge
          variant="outline"
          className={`bg-blue-50 text-blue-700 border-blue-300 dark:bg-blue-950/40 dark:text-blue-300 dark:border-blue-800 gap-1 ${className || ''}`}
        >
          <Clock className="w-3 h-3 text-blue-500" />
          <span>{t.ACKNOWLEDGED}</span>
        </Badge>
      )

    case 'INVESTIGATING':
      return (
        <Badge
          variant="outline"
          className={`bg-purple-50 text-purple-700 border-purple-300 dark:bg-purple-950/40 dark:text-purple-300 dark:border-purple-800 gap-1 ${className || ''}`}
        >
          <Eye className="w-3 h-3 text-purple-500" />
          <span>{t.INVESTIGATING}</span>
        </Badge>
      )

    case 'RESOLVED':
      return (
        <Badge
          variant="outline"
          className={`bg-emerald-50 text-emerald-700 border-emerald-300 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800 gap-1 ${className || ''}`}
        >
          <CheckCircle2 className="w-3 h-3 text-emerald-500" />
          <span>{t.RESOLVED}</span>
        </Badge>
      )

    case 'DISMISSED':
      return (
        <Badge
          variant="outline"
          className={`bg-slate-100 text-slate-600 border-slate-300 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700 gap-1 ${className || ''}`}
        >
          <XCircle className="w-3 h-3 text-slate-400" />
          <span>{t.DISMISSED}</span>
        </Badge>
      )
  }
}
