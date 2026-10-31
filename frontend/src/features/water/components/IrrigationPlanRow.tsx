import React from 'react'
import { TableRow, TableCell } from '@/shared/ui/table'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/ui/dropdown-menu'
import { MoreHorizontal, Play, CheckCircle, XCircle, Edit, Activity } from 'lucide-react'
import { IrrigationPlan } from '@/entities/water'
import { Field } from '@/entities/field'
import { usePlanMetricsQuery } from '../queries/useWaterQueries'
import { WaterStatusBadge } from './WaterStatusBadge'
import { formatVolume, formatDateTime, formatPercentage } from '../lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface IrrigationPlanRowProps {
  plan: IrrigationPlan
  field?: Field
  canManage: boolean
  lang: SupportedLanguage
  onStart: (plan: IrrigationPlan) => void
  onComplete: (plan: IrrigationPlan) => void
  onCancel: (plan: IrrigationPlan) => void
  onEdit: (plan: IrrigationPlan) => void
}

export const IrrigationPlanRow: React.FC<IrrigationPlanRowProps> = ({
  plan,
  field,
  canManage,
  lang,
  onStart,
  onComplete,
  onCancel,
  onEdit,
}) => {
  const t = translations[lang]
  const { data: metrics, isLoading } = usePlanMetricsQuery(plan.id)

  const actualVolume = metrics ? metrics.actualVolumeM3 : 0
  const variance = metrics ? metrics.varianceVolumeM3 : 0
  const ratio = metrics ? metrics.consumptionRatioPercentage : 0

  return (
    <TableRow className="hover:bg-muted/50 transition-colors">
      {/* Field */}
      <TableCell className="font-medium text-foreground py-3">
        <div>{field?.name || 'Field #' + plan.fieldId.slice(0, 8)}</div>
        <div className="text-xs text-muted-foreground">{plan.method}</div>
      </TableCell>

      {/* Planned Schedule */}
      <TableCell className="text-xs text-muted-foreground">
        <div>{formatDateTime(plan.plannedStartAt)}</div>
        <div className="text-[11px] opacity-75">to {formatDateTime(plan.plannedEndAt)}</div>
      </TableCell>

      {/* Planned Volume */}
      <TableCell className="text-sm font-semibold text-foreground whitespace-nowrap">
        {formatVolume(plan.plannedVolumeM3)}
      </TableCell>

      {/* Actual Volume */}
      <TableCell className="text-sm whitespace-nowrap">
        {isLoading ? (
          <span className="text-xs text-muted-foreground">{t.common.loading}</span>
        ) : plan.status === 'COMPLETED' || actualVolume > 0 ? (
          <span className="font-medium text-foreground">{formatVolume(actualVolume)}</span>
        ) : (
          <span className="text-muted-foreground text-xs">—</span>
        )}
      </TableCell>

      {/* Planned vs Actual Variance Pill */}
      <TableCell className="min-w-[150px]">
        {isLoading ? (
          <div className="h-5 bg-muted rounded animate-pulse" />
        ) : plan.status === 'COMPLETED' && metrics ? (
          <div className="flex flex-col gap-0.5">
            <div className="flex items-center gap-1.5 text-xs font-semibold">
              <span
                className={
                  variance > 0
                    ? 'text-rose-600 dark:text-rose-400'
                    : variance < 0
                      ? 'text-emerald-600 dark:text-emerald-400'
                      : 'text-foreground'
                }
              >
                {variance > 0 ? `+${variance.toFixed(1)}` : variance.toFixed(1)} m³
              </span>
              <Badge variant="secondary" className="text-[10px] px-1 py-0 font-normal">
                {formatPercentage(ratio)}
              </Badge>
            </div>
            <span className="text-[10px] text-muted-foreground">
              {metrics.completedEventsCount} delivery event(s)
            </span>
          </div>
        ) : plan.status === 'IN_PROGRESS' ? (
          <span className="text-xs text-amber-600 dark:text-amber-400 font-medium flex items-center gap-1">
            <Activity className="w-3 h-3 animate-spin" /> In Delivery
          </span>
        ) : (
          <span className="text-xs text-muted-foreground">—</span>
        )}
      </TableCell>

      {/* Status */}
      <TableCell className="whitespace-nowrap">
        <WaterStatusBadge status={plan.status} lang={lang} />
      </TableCell>

      {/* Actions */}
      <TableCell className="text-right">
        {canManage && (plan.status === 'PLANNED' || plan.status === 'IN_PROGRESS') ? (
          <div className="flex items-center justify-end gap-1">
            {plan.status === 'PLANNED' && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => onStart(plan)}
                className="h-7 px-2 text-xs text-amber-700 dark:text-amber-300 border-amber-300 dark:border-amber-700 bg-amber-50/50 hover:bg-amber-100 dark:bg-amber-950/40"
              >
                <Play className="w-3 h-3 mr-1 fill-amber-500" />
                {t.water.plans.start}
              </Button>
            )}

            {plan.status === 'IN_PROGRESS' && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => onComplete(plan)}
                className="h-7 px-2 text-xs text-teal-700 dark:text-teal-300 border-teal-300 dark:border-teal-700 bg-teal-50/50 hover:bg-teal-100 dark:bg-teal-950/40"
              >
                <CheckCircle className="w-3 h-3 mr-1" />
                {t.water.plans.complete}
              </Button>
            )}

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="sm" className="h-7 w-7 p-0">
                  <MoreHorizontal className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-40">
                {plan.status === 'PLANNED' && (
                  <DropdownMenuItem onClick={() => onEdit(plan)} className="gap-2 cursor-pointer">
                    <Edit className="h-3.5 w-3.5 text-muted-foreground" />
                    <span>{t.water.plans.edit}</span>
                  </DropdownMenuItem>
                )}
                <DropdownMenuItem
                  onClick={() => onCancel(plan)}
                  className="gap-2 cursor-pointer text-rose-600 dark:text-rose-400"
                >
                  <XCircle className="h-3.5 w-3.5" />
                  <span>{t.water.plans.cancel}</span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        ) : (
          <span className="text-muted-foreground text-xs">—</span>
        )}
      </TableCell>
    </TableRow>
  )
}
