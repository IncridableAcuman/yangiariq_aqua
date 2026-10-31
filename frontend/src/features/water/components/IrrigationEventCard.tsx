import React from 'react'
import { Card } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { CheckCircle, XCircle } from 'lucide-react'
import { IrrigationEvent } from '@/entities/water'
import { Field } from '@/entities/field'
import { WaterStatusBadge } from './WaterStatusBadge'
import { formatVolume, formatDateTime } from '../lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface IrrigationEventCardProps {
  event: IrrigationEvent
  field?: Field
  canManage: boolean
  lang: SupportedLanguage
  onComplete: (event: IrrigationEvent) => void
  onCancel: (event: IrrigationEvent) => void
}

export const IrrigationEventCard: React.FC<IrrigationEventCardProps> = ({
  event,
  field,
  canManage,
  lang,
  onComplete,
  onCancel,
}) => {
  const t = translations[lang]

  return (
    <Card className="border-border/60 shadow-sm p-4 space-y-3">
      <div className="flex items-start justify-between gap-2">
        <div>
          <h4 className="font-semibold text-base text-foreground">
            {field?.name || 'Field #' + event.fieldId.slice(0, 8)}
          </h4>
          {event.irrigationPlanId && (
            <p className="text-xs text-muted-foreground">
              Plan #{event.irrigationPlanId.slice(0, 8)}
            </p>
          )}
        </div>
        <WaterStatusBadge status={event.status} lang={lang} />
      </div>

      <div className="grid grid-cols-2 gap-2 py-2 border-y border-border/50 text-xs">
        <div>
          <span className="text-muted-foreground block">{t.water.events.startedAt}</span>
          <span className="font-medium text-foreground">{formatDateTime(event.startedAt)}</span>
        </div>
        <div>
          <span className="text-muted-foreground block">{t.water.events.endedAt}</span>
          <span className="font-medium text-foreground">
            {event.endedAt ? formatDateTime(event.endedAt) : 'In Progress'}
          </span>
        </div>
      </div>

      <div className="flex items-center justify-between text-xs">
        <span className="text-muted-foreground">{t.water.events.measuredVolume}:</span>
        <span className="text-base font-bold text-foreground">
          {event.actualVolumeM3 !== null && event.actualVolumeM3 !== undefined
            ? formatVolume(event.actualVolumeM3)
            : '—'}
        </span>
      </div>

      {event.notes && (
        <p className="text-xs text-muted-foreground bg-muted/30 p-2 rounded">
          {event.notes}
        </p>
      )}

      {canManage && event.status === 'STARTED' && (
        <div className="flex gap-2 pt-1">
          <Button
            variant="outline"
            size="sm"
            className="h-8 flex-1 text-xs text-teal-700 bg-teal-50"
            onClick={() => onComplete(event)}
          >
            <CheckCircle className="w-3 h-3 mr-1" />
            {t.water.events.completeEvent}
          </Button>
          <Button
            variant="outline"
            size="sm"
            className="h-8 text-xs text-rose-600"
            onClick={() => onCancel(event)}
          >
            <XCircle className="w-3 h-3 mr-1" />
            {t.water.events.cancelEvent}
          </Button>
        </div>
      )}
    </Card>
  )
}
