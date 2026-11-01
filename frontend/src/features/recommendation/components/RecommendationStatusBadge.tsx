import React from 'react'
import { Badge } from '@/shared/ui/badge'
import { CheckCircle2, Clock, Eye, XCircle, CheckSquare, AlertCircle } from 'lucide-react'
import { RecommendationStatus } from '@/entities/recommendation'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface RecommendationStatusBadgeProps {
  status: RecommendationStatus
  lang: SupportedLanguage
  className?: string
}

export const RecommendationStatusBadge: React.FC<RecommendationStatusBadgeProps> = ({
  status,
  lang,
  className,
}) => {
  const t = translations[lang].recommendations?.statuses || {
    GENERATED: 'Generated',
    VIEWED: 'Viewed',
    ACCEPTED: 'Accepted',
    DISMISSED: 'Dismissed',
    COMPLETED: 'Completed',
    EXPIRED: 'Expired',
  }

  switch (status) {
    case 'GENERATED':
      return (
        <Badge variant="outline" className={`bg-purple-500/10 text-purple-700 border-purple-300 dark:bg-purple-950/40 dark:text-purple-300 dark:border-purple-800 gap-1 ${className || ''}`}>
          <Clock className="w-3.5 h-3.5 text-purple-600 dark:text-purple-400" />
          <span>{t.GENERATED}</span>
        </Badge>
      )

    case 'VIEWED':
      return (
        <Badge variant="outline" className={`bg-sky-500/10 text-sky-700 border-sky-300 dark:bg-sky-950/40 dark:text-sky-300 dark:border-sky-800 gap-1 ${className || ''}`}>
          <Eye className="w-3.5 h-3.5 text-sky-600 dark:text-sky-400" />
          <span>{t.VIEWED}</span>
        </Badge>
      )

    case 'ACCEPTED':
      return (
        <Badge variant="outline" className={`bg-emerald-500/10 text-emerald-700 border-emerald-300 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800 gap-1 font-medium ${className || ''}`}>
          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
          <span>{t.ACCEPTED}</span>
        </Badge>
      )

    case 'COMPLETED':
      return (
        <Badge variant="outline" className={`bg-teal-500/10 text-teal-700 border-teal-300 dark:bg-teal-950/40 dark:text-teal-300 dark:border-teal-800 gap-1 font-semibold ${className || ''}`}>
          <CheckSquare className="w-3.5 h-3.5 text-teal-600 dark:text-teal-400" />
          <span>{t.COMPLETED}</span>
        </Badge>
      )

    case 'DISMISSED':
      return (
        <Badge variant="outline" className={`bg-gray-500/10 text-gray-700 border-gray-300 dark:bg-gray-800 dark:text-gray-400 dark:border-gray-700 gap-1 ${className || ''}`}>
          <XCircle className="w-3.5 h-3.5 text-gray-500" />
          <span>{t.DISMISSED}</span>
        </Badge>
      )

    case 'EXPIRED':
    default:
      return (
        <Badge variant="outline" className={`bg-rose-500/10 text-rose-700 border-rose-300 dark:bg-rose-950/40 dark:text-rose-400 dark:border-rose-800 gap-1 ${className || ''}`}>
          <AlertCircle className="w-3.5 h-3.5 text-rose-500" />
          <span>{t.EXPIRED}</span>
        </Badge>
      )
  }
}
