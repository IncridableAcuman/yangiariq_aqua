import React from 'react'
import { TelemetryFreshness } from '@/entities/intelligence'
import { Badge } from '@/shared/ui/badge'
import { Clock, AlertTriangle, XCircle } from 'lucide-react'

interface TelemetryFreshnessBadgeProps {
  freshness: TelemetryFreshness
  className?: string
}

export const TelemetryFreshnessBadge: React.FC<TelemetryFreshnessBadgeProps> = ({
  freshness,
  className = '',
}) => {
  switch (freshness) {
    case 'FRESH':
      return (
        <Badge
          variant="outline"
          className={`bg-emerald-50 text-emerald-700 border-emerald-300 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800 flex items-center gap-1 ${className}`}
        >
          <Clock className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
          <span>Fresh</span>
        </Badge>
      )
    case 'STALE':
      return (
        <Badge
          variant="outline"
          className={`bg-amber-50 text-amber-700 border-amber-300 dark:bg-amber-950/40 dark:text-amber-300 dark:border-amber-800 flex items-center gap-1 ${className}`}
        >
          <AlertTriangle className="w-3 h-3 text-amber-600 dark:text-amber-400" />
          <span>Stale</span>
        </Badge>
      )
    case 'MISSING':
    default:
      return (
        <Badge
          variant="outline"
          className={`bg-rose-50 text-rose-700 border-rose-300 dark:bg-rose-950/40 dark:text-rose-300 dark:border-rose-800 flex items-center gap-1 ${className}`}
        >
          <XCircle className="w-3 h-3 text-rose-600 dark:text-rose-400" />
          <span>Missing</span>
        </Badge>
      )
  }
}
