import React from 'react'
import { Card } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Edit, CheckCircle, XCircle } from 'lucide-react'
import { WaterAllocation, WaterSource } from '@/entities/water'
import { Field } from '@/entities/field'
import { useAllocationConsumptionQuery } from '../queries/useWaterQueries'
import { WaterProgressBar } from './WaterProgressBar'
import { WaterStatusBadge, OverConsumptionBadge } from './WaterStatusBadge'
import { formatVolume, formatDate } from '../lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface WaterAllocationCardProps {
  allocation: WaterAllocation
  field?: Field
  source?: WaterSource
  canManage: boolean
  lang: SupportedLanguage
  onEdit: (allocation: WaterAllocation) => void
  onClose: (allocation: WaterAllocation) => void
  onCancel: (allocation: WaterAllocation) => void
}

export const WaterAllocationCard: React.FC<WaterAllocationCardProps> = ({
  allocation,
  field,
  source,
  canManage,
  lang,
  onEdit,
  onClose,
  onCancel,
}) => {
  const t = translations[lang]
  const { data: consumption, isLoading } = useAllocationConsumptionQuery(allocation.id)

  const consumed = consumption ? consumption.consumedVolumeM3 : 0
  const remaining = consumption
    ? consumption.remainingVolumeM3
    : Math.max(allocation.allocatedVolumeM3 - consumed, 0)
  const isOver = consumption ? consumption.isOverConsumed : false
  const overVolume = consumption ? consumption.overConsumedVolumeM3 : 0

  return (
    <Card className="border-border/60 shadow-sm p-4 space-y-3">
      <div className="flex items-start justify-between gap-2">
        <div>
          <h4 className="font-semibold text-base text-foreground">
            {field?.name || 'Field #' + allocation.fieldId.slice(0, 8)}
          </h4>
          <p className="text-xs text-muted-foreground">
            {source?.name || 'Water Source'} ({source?.type || 'CANAL'})
          </p>
        </div>
        <WaterStatusBadge status={allocation.status} lang={lang} />
      </div>

      <div className="grid grid-cols-3 gap-2 py-2 border-y border-border/50 text-xs">
        <div>
          <span className="text-muted-foreground block">{t.water.allocations.volume}</span>
          <span className="font-semibold text-foreground text-sm">
            {formatVolume(allocation.allocatedVolumeM3)}
          </span>
        </div>
        <div>
          <span className="text-muted-foreground block">{t.water.allocations.consumed}</span>
          <span className={isOver ? 'text-rose-600 font-semibold text-sm' : 'text-foreground font-semibold text-sm'}>
            {isLoading ? '...' : formatVolume(consumed)}
          </span>
        </div>
        <div>
          <span className="text-muted-foreground block">{t.water.allocations.remaining}</span>
          <span className="font-semibold text-emerald-600 text-sm">
            {isLoading ? '...' : formatVolume(remaining)}
          </span>
        </div>
      </div>

      <div className="space-y-1.5">
        <WaterProgressBar
          consumed={consumed}
          allocated={allocation.allocatedVolumeM3}
          isOverConsumed={isOver}
        />
        {isOver && (
          <OverConsumptionBadge
            isOverConsumed={true}
            overConsumedVolumeM3={overVolume}
            lang={lang}
            className="text-[10px] py-0 px-1.5"
          />
        )}
      </div>

      <div className="flex items-center justify-between text-xs text-muted-foreground pt-1">
        <span>
          {formatDate(allocation.periodStart)} — {formatDate(allocation.periodEnd)}
        </span>
        {canManage && allocation.status === 'ACTIVE' && (
          <div className="flex gap-1.5">
            <Button
              variant="outline"
              size="sm"
              className="h-7 px-2 text-xs"
              onClick={() => onEdit(allocation)}
            >
              <Edit className="w-3 h-3 mr-1" />
              {t.water.allocations.edit}
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="h-7 px-2 text-xs text-slate-700 dark:text-slate-300"
              onClick={() => onClose(allocation)}
            >
              <CheckCircle className="w-3 h-3 mr-1" />
              {t.water.allocations.close}
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="h-7 px-2 text-xs text-rose-600"
              onClick={() => onCancel(allocation)}
            >
              <XCircle className="w-3 h-3 mr-1" />
              {t.water.allocations.cancel}
            </Button>
          </div>
        )}
      </div>
    </Card>
  )
}
