import React from 'react'
import { Card } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Play, CheckCircle, XCircle, Edit } from 'lucide-react'
import { IrrigationPlan } from '@/entities/water'
import { Field } from '@/entities/field'
import { usePlanMetricsQuery } from '../queries/useWaterQueries'
import { WaterStatusBadge } from './WaterStatusBadge'
import { formatVolume, formatDateTime, formatPercentage } from '../lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface IrrigationPlanCardProps {
  plan: IrrigationPlan
  field?: Field
  canManage: boolean
  lang: SupportedLanguage
  onStart: (plan: IrrigationPlan) => void
  onComplete: (plan: IrrigationPlan) => void
  onCancel: (plan: IrrigationPlan) => void
  onEdit: (plan: IrrigationPlan) => void
}

export const IrrigationPlanCard: React.FC<IrrigationPlanCardProps> = ({
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
    <Card className="border-border/60 shadow-sm p-4 space-y-3">
      <div className="flex items-start justify-between gap-2">
        <div>
          <h4 className="font-semibold text-base text-foreground">
            {field?.name || 'Field #' + plan.fieldId.slice(0, 8)}
          </h4>
          <p className="text-xs text-muted-foreground">{plan.method}</p>
        </div>
        <WaterStatusBadge status={plan.status} lang={lang} />
      </div>

      <div className="text-xs text-muted-foreground">
        <div>
          <span className="font-medium text-foreground">Scheduled:</span>{' '}
          {formatDateTime(plan.plannedStartAt)} — {formatDateTime(plan.plannedEndAt)}
        </div>
      </div>

      <div className="grid grid-cols-2 gap-2 py-2 border-y border-border/50 text-xs">
        <div>
          <span className="text-muted-foreground block">{t.water.plans.plannedVolume}</span>
          <span className="font-semibold text-foreground text-sm">
            {formatVolume(plan.plannedVolumeM3)}
          </span>
        </div>
        <div>
          <span className="text-muted-foreground block">{t.water.plans.actualVolume}</span>
          <span className="font-semibold text-foreground text-sm">
            {isLoading ? '...' : actualVolume > 0 ? formatVolume(actualVolume) : '—'}
          </span>
        </div>
      </div>

      {plan.status === 'COMPLETED' && metrics && (
        <div className="flex items-center justify-between text-xs bg-muted/40 p-2 rounded">
          <span className="text-muted-foreground">{t.water.plans.variance}:</span>
          <div className="flex items-center gap-1.5 font-semibold">
            <span
              className={
                variance > 0
                  ? 'text-rose-600'
                  : variance < 0
                    ? 'text-emerald-600'
                    : 'text-foreground'
              }
            >
              {variance > 0 ? `+${variance.toFixed(1)}` : variance.toFixed(1)} m³
            </span>
            <Badge variant="secondary" className="text-[10px] px-1 py-0 font-normal">
              {formatPercentage(ratio)}
            </Badge>
          </div>
        </div>
      )}

      {canManage && (plan.status === 'PLANNED' || plan.status === 'IN_PROGRESS') && (
        <div className="flex gap-2 pt-1">
          {plan.status === 'PLANNED' && (
            <>
              <Button
                variant="outline"
                size="sm"
                className="h-8 flex-1 text-xs text-amber-700 bg-amber-50"
                onClick={() => onStart(plan)}
              >
                <Play className="w-3 h-3 mr-1 fill-amber-500" />
                {t.water.plans.start}
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="h-8 text-xs"
                onClick={() => onEdit(plan)}
              >
                <Edit className="w-3 h-3 mr-1" />
                {t.water.plans.edit}
              </Button>
            </>
          )}

          {plan.status === 'IN_PROGRESS' && (
            <Button
              variant="outline"
              size="sm"
              className="h-8 flex-1 text-xs text-teal-700 bg-teal-50"
              onClick={() => onComplete(plan)}
            >
              <CheckCircle className="w-3 h-3 mr-1" />
              {t.water.plans.complete}
            </Button>
          )}

          <Button
            variant="outline"
            size="sm"
            className="h-8 text-xs text-rose-600"
            onClick={() => onCancel(plan)}
          >
            <XCircle className="w-3 h-3 mr-1" />
            {t.water.plans.cancel}
          </Button>
        </div>
      )}
    </Card>
  )
}
