import React from 'react'
import { Badge } from '@/shared/ui/badge'
import { AlertCircle, AlertOctagon, AlertTriangle, Info } from 'lucide-react'
import { AnomalySeverity } from '@/entities/anomaly'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface AnomalySeverityBadgeProps {
  severity: AnomalySeverity
  lang: SupportedLanguage
  className?: string
}

export const AnomalySeverityBadge: React.FC<AnomalySeverityBadgeProps> = ({
  severity,
  lang,
  className,
}) => {
  const t = translations[lang].anomalies.severities

  switch (severity) {
    case 'CRITICAL':
      return (
        <Badge
          variant="outline"
          className={`bg-rose-500/10 text-rose-700 border-rose-300 dark:bg-rose-950/40 dark:text-rose-300 dark:border-rose-800 gap-1 font-semibold ${className || ''}`}
        >
          <AlertOctagon className="w-3.5 h-3.5 text-rose-600 dark:text-rose-400 animate-pulse" />
          <span>{t.CRITICAL}</span>
        </Badge>
      )

    case 'HIGH':
      return (
        <Badge
          variant="outline"
          className={`bg-amber-500/10 text-amber-700 border-amber-300 dark:bg-amber-950/40 dark:text-amber-300 dark:border-amber-800 gap-1 font-medium ${className || ''}`}
        >
          <AlertTriangle className="w-3.5 h-3.5 text-amber-600 dark:text-amber-400" />
          <span>{t.HIGH}</span>
        </Badge>
      )

    case 'MEDIUM':
      return (
        <Badge
          variant="outline"
          className={`bg-yellow-500/10 text-yellow-700 border-yellow-300 dark:bg-yellow-950/40 dark:text-yellow-300 dark:border-yellow-800 gap-1 ${className || ''}`}
        >
          <AlertCircle className="w-3.5 h-3.5 text-yellow-600 dark:text-yellow-400" />
          <span>{t.MEDIUM}</span>
        </Badge>
      )

    case 'LOW':
      return (
        <Badge
          variant="outline"
          className={`bg-blue-500/10 text-blue-700 border-blue-300 dark:bg-blue-950/40 dark:text-blue-300 dark:border-blue-800 gap-1 ${className || ''}`}
        >
          <Info className="w-3.5 h-3.5 text-blue-600 dark:text-blue-400" />
          <span>{t.LOW}</span>
        </Badge>
      )

    case 'INFO':
    default:
      return (
        <Badge
          variant="outline"
          className={`bg-slate-500/10 text-slate-700 border-slate-300 dark:bg-slate-900/50 dark:text-slate-300 dark:border-slate-700 gap-1 ${className || ''}`}
        >
          <Info className="w-3.5 h-3.5 text-slate-500" />
          <span>{t.INFO}</span>
        </Badge>
      )
  }
}
