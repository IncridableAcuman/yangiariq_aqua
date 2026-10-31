import React from 'react'
import { Badge } from '@/shared/ui/badge'
import { AlertTriangle, CheckCircle2, Clock, XCircle, Play } from 'lucide-react'
import {
  WaterSourceStatus,
  WaterAllocationStatus,
  IrrigationPlanStatus,
  IrrigationEventStatus,
} from '@/entities/water'
import { SupportedLanguage, translations } from '@/shared/i18n'

type AnyWaterStatus =
  | WaterSourceStatus
  | WaterAllocationStatus
  | IrrigationPlanStatus
  | IrrigationEventStatus

interface WaterStatusBadgeProps {
  status: AnyWaterStatus
  lang: SupportedLanguage
  className?: string
}

export const WaterStatusBadge: React.FC<WaterStatusBadgeProps> = ({
  status,
  lang,
  className,
}) => {
  const t = translations[lang].water.statuses

  switch (status) {
    case 'ACTIVE':
      return (
        <Badge
          variant="outline"
          className={`bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800 gap-1 ${className || ''}`}
        >
          <CheckCircle2 className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
          <span>{t.ACTIVE}</span>
        </Badge>
      )

    case 'INACTIVE':
      return (
        <Badge
          variant="outline"
          className={`bg-slate-100 text-slate-600 border-slate-300 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700 gap-1 ${className || ''}`}
        >
          <Clock className="w-3 h-3 text-slate-500" />
          <span>{t.INACTIVE}</span>
        </Badge>
      )

    case 'CLOSED':
      return (
        <Badge
          variant="outline"
          className={`bg-zinc-100 text-zinc-700 border-zinc-300 dark:bg-zinc-800 dark:text-zinc-300 dark:border-zinc-700 gap-1 ${className || ''}`}
        >
          <CheckCircle2 className="w-3 h-3 text-zinc-500" />
          <span>{t.CLOSED}</span>
        </Badge>
      )

    case 'CANCELLED':
      return (
        <Badge
          variant="outline"
          className={`bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950/40 dark:text-rose-300 dark:border-rose-800 gap-1 ${className || ''}`}
        >
          <XCircle className="w-3 h-3 text-rose-600 dark:text-rose-400" />
          <span>{t.CANCELLED}</span>
        </Badge>
      )

    case 'PLANNED':
      return (
        <Badge
          variant="outline"
          className={`bg-sky-50 text-sky-700 border-sky-200 dark:bg-sky-950/40 dark:text-sky-300 dark:border-sky-800 gap-1 ${className || ''}`}
        >
          <Clock className="w-3 h-3 text-sky-600 dark:text-sky-400" />
          <span>{t.PLANNED}</span>
        </Badge>
      )

    case 'IN_PROGRESS':
    case 'STARTED':
      return (
        <Badge
          variant="outline"
          className={`bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/40 dark:text-amber-300 dark:border-amber-800 gap-1 animate-pulse ${className || ''}`}
        >
          <Play className="w-3 h-3 text-amber-600 dark:text-amber-400 fill-amber-500" />
          <span>{status === 'STARTED' ? t.STARTED : t.IN_PROGRESS}</span>
        </Badge>
      )

    case 'COMPLETED':
      return (
        <Badge
          variant="outline"
          className={`bg-teal-50 text-teal-700 border-teal-200 dark:bg-teal-950/40 dark:text-teal-300 dark:border-teal-800 gap-1 ${className || ''}`}
        >
          <CheckCircle2 className="w-3 h-3 text-teal-600 dark:text-teal-400" />
          <span>{t.COMPLETED}</span>
        </Badge>
      )

    default:
      return <Badge variant="secondary">{status}</Badge>
  }
}

interface OverConsumptionBadgeProps {
  isOverConsumed: boolean
  overConsumedVolumeM3?: number
  lang: SupportedLanguage
  className?: string
}

export const OverConsumptionBadge: React.FC<OverConsumptionBadgeProps> = ({
  isOverConsumed,
  overConsumedVolumeM3,
  lang,
  className,
}) => {
  const t = translations[lang].water

  if (!isOverConsumed) {
    return (
      <Badge
        variant="outline"
        className={`bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/30 dark:text-emerald-300 dark:border-emerald-800 gap-1 ${className || ''}`}
      >
        <CheckCircle2 className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
        <span>{t.stats.withinQuota}</span>
      </Badge>
    )
  }

  return (
    <Badge
      variant="destructive"
      className={`bg-rose-600 hover:bg-rose-700 text-white gap-1 font-semibold ${className || ''}`}
    >
      <AlertTriangle className="w-3 h-3" />
      <span>{t.allocations.overQuotaBadge}</span>
      {typeof overConsumedVolumeM3 === 'number' && overConsumedVolumeM3 > 0 && (
        <span className="ml-1 opacity-90">
          (+{overConsumedVolumeM3.toFixed(1)} m³)
        </span>
      )}
    </Badge>
  )
}
