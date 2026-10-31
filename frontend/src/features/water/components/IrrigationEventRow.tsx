import React from 'react'
import { TableRow, TableCell } from '@/shared/ui/table'
import { Button } from '@/shared/ui/button'
import { CheckCircle, XCircle } from 'lucide-react'
import { IrrigationEvent } from '@/entities/water'
import { Field } from '@/entities/field'
import { WaterStatusBadge } from './WaterStatusBadge'
import { formatVolume, formatDateTime } from '../lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface IrrigationEventRowProps {
  event: IrrigationEvent
  field?: Field
  canManage: boolean
  lang: SupportedLanguage
  onComplete: (event: IrrigationEvent) => void
  onCancel: (event: IrrigationEvent) => void
}

export const IrrigationEventRow: React.FC<IrrigationEventRowProps> = ({
  event,
  field,
  canManage,
  lang,
  onComplete,
  onCancel,
}) => {
  const t = translations[lang]

  return (
    <TableRow className="hover:bg-muted/50 transition-colors">
      {/* Field */}
      <TableCell className="font-medium text-foreground py-3">
        <div>{field?.name || 'Field #' + event.fieldId.slice(0, 8)}</div>
        {event.irrigationPlanId && (
          <div className="text-[11px] text-muted-foreground">
            Plan #{event.irrigationPlanId.slice(0, 8)}
          </div>
        )}
      </TableCell>

      {/* Started At */}
      <TableCell className="text-xs text-muted-foreground whitespace-nowrap">
        {formatDateTime(event.startedAt)}
      </TableCell>

      {/* Ended At */}
      <TableCell className="text-xs text-muted-foreground whitespace-nowrap">
        {event.endedAt ? formatDateTime(event.endedAt) : (
          <span className="text-amber-600 dark:text-amber-400 font-medium">In Progress...</span>
        )}
      </TableCell>

      {/* Measured Volume */}
      <TableCell className="text-sm font-semibold whitespace-nowrap">
        {event.actualVolumeM3 !== null && event.actualVolumeM3 !== undefined ? (
          <span className="text-foreground">{formatVolume(event.actualVolumeM3)}</span>
        ) : (
          <span className="text-muted-foreground text-xs">—</span>
        )}
      </TableCell>

      {/* Status */}
      <TableCell className="whitespace-nowrap">
        <WaterStatusBadge status={event.status} lang={lang} />
      </TableCell>

      {/* Notes */}
      <TableCell className="text-xs text-muted-foreground max-w-[200px] truncate">
        {event.notes || '—'}
      </TableCell>

      {/* Actions */}
      <TableCell className="text-right whitespace-nowrap">
        {canManage && event.status === 'STARTED' ? (
          <div className="flex items-center justify-end gap-1.5">
            <Button
              variant="outline"
              size="sm"
              onClick={() => onComplete(event)}
              className="h-7 px-2 text-xs text-teal-700 dark:text-teal-300 border-teal-300 dark:border-teal-700 bg-teal-50/50 hover:bg-teal-100"
            >
              <CheckCircle className="w-3 h-3 mr-1" />
              {t.water.events.completeEvent}
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onCancel(event)}
              className="h-7 px-2 text-xs text-rose-600 dark:text-rose-400 hover:bg-rose-50"
            >
              <XCircle className="w-3 h-3 mr-1" />
              {t.water.events.cancelEvent}
            </Button>
          </div>
        ) : (
          <span className="text-muted-foreground text-xs">—</span>
        )}
      </TableCell>
    </TableRow>
  )
}
