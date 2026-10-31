import React from 'react'
import { TableRow, TableCell } from '@/shared/ui/table'
import { Button } from '@/shared/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/ui/dropdown-menu'
import { MoreHorizontal, Edit, CheckCircle, XCircle } from 'lucide-react'
import { WaterAllocation, WaterSource } from '@/entities/water'
import { Field } from '@/entities/field'
import { useAllocationConsumptionQuery } from '../queries/useWaterQueries'
import { WaterProgressBar } from './WaterProgressBar'
import { WaterStatusBadge, OverConsumptionBadge } from './WaterStatusBadge'
import { formatVolume, formatDate } from '../lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface WaterAllocationRowProps {
  allocation: WaterAllocation
  field?: Field
  source?: WaterSource
  canManage: boolean
  lang: SupportedLanguage
  onEdit: (allocation: WaterAllocation) => void
  onClose: (allocation: WaterAllocation) => void
  onCancel: (allocation: WaterAllocation) => void
}

export const WaterAllocationRow: React.FC<WaterAllocationRowProps> = ({
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
    <TableRow className="hover:bg-muted/50 transition-colors">
      {/* Field */}
      <TableCell className="font-medium text-foreground py-3">
        <div>{field?.name || 'Field #' + allocation.fieldId.slice(0, 8)}</div>
        <div className="text-xs text-muted-foreground">
          {field?.cropType ? `${field.cropType} • ${field.areaHectares} ha` : 'Parcel'}
        </div>
      </TableCell>

      {/* Water Source */}
      <TableCell className="text-sm">
        <div className="font-medium">{source?.name || 'Source #' + allocation.waterSourceId.slice(0, 8)}</div>
        <div className="text-xs text-muted-foreground">{source?.type || 'CANAL'}</div>
      </TableCell>

      {/* Quota Period */}
      <TableCell className="text-xs text-muted-foreground whitespace-nowrap">
        {formatDate(allocation.periodStart)} — {formatDate(allocation.periodEnd)}
      </TableCell>

      {/* Quota Volume */}
      <TableCell className="text-sm font-semibold text-foreground whitespace-nowrap">
        {formatVolume(allocation.allocatedVolumeM3)}
      </TableCell>

      {/* Consumed Volume */}
      <TableCell className="text-sm whitespace-nowrap">
        {isLoading ? (
          <span className="text-xs text-muted-foreground">{t.common.loading}</span>
        ) : (
          <span className={isOver ? 'text-rose-600 dark:text-rose-400 font-semibold' : 'text-foreground font-medium'}>
            {formatVolume(consumed)}
          </span>
        )}
      </TableCell>

      {/* Remaining Volume */}
      <TableCell className="text-sm whitespace-nowrap">
        {isLoading ? (
          <span className="text-xs text-muted-foreground">{t.common.loading}</span>
        ) : (
          <span className="text-emerald-600 dark:text-emerald-400 font-medium">
            {formatVolume(remaining)}
          </span>
        )}
      </TableCell>

      {/* Utilization & Progress */}
      <TableCell className="min-w-[140px] max-w-[200px]">
        {isLoading ? (
          <div className="h-2 bg-muted rounded animate-pulse" />
        ) : (
          <div className="flex flex-col gap-1">
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
                className="mt-0.5 text-[10px] py-0 px-1.5 self-start"
              />
            )}
          </div>
        )}
      </TableCell>

      {/* Status */}
      <TableCell className="whitespace-nowrap">
        <WaterStatusBadge status={allocation.status} lang={lang} />
      </TableCell>

      {/* Actions */}
      <TableCell className="text-right">
        {canManage && allocation.status === 'ACTIVE' ? (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                <MoreHorizontal className="h-4 w-4" />
                <span className="sr-only">{t.common.actions}</span>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-44">
              <DropdownMenuItem onClick={() => onEdit(allocation)} className="gap-2 cursor-pointer">
                <Edit className="h-3.5 w-3.5 text-muted-foreground" />
                <span>{t.water.allocations.edit}</span>
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => onClose(allocation)} className="gap-2 cursor-pointer text-slate-600 dark:text-slate-300">
                <CheckCircle className="h-3.5 w-3.5" />
                <span>{t.water.allocations.close}</span>
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => onCancel(allocation)} className="gap-2 cursor-pointer text-rose-600 dark:text-rose-400">
                <XCircle className="h-3.5 w-3.5" />
                <span>{t.water.allocations.cancel}</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : (
          <span className="text-muted-foreground text-xs">—</span>
        )}
      </TableCell>
    </TableRow>
  )
}
