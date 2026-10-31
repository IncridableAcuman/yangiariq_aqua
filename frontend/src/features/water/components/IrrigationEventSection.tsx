import React, { useState } from 'react'
import { Plus, Play, Filter } from 'lucide-react'
import { Button } from '@/shared/ui/button'
import {
  Table,
  TableBody,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/table'
import { IrrigationEventRow } from './IrrigationEventRow'
import { IrrigationEventCard } from './IrrigationEventCard'
import { IrrigationEvent } from '@/entities/water'
import { Field } from '@/entities/field'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface IrrigationEventSectionProps {
  events: IrrigationEvent[]
  fields: Field[]
  canManage: boolean
  isLoading: boolean
  lang: SupportedLanguage
  onStartClick: () => void
  onRecordClick: () => void
  onCompleteClick: (event: IrrigationEvent) => void
  onCancelClick: (event: IrrigationEvent) => void
}

export const IrrigationEventSection: React.FC<IrrigationEventSectionProps> = ({
  events,
  fields,
  canManage,
  isLoading,
  lang,
  onStartClick,
  onRecordClick,
  onCompleteClick,
  onCancelClick,
}) => {
  const t = translations[lang]
  const [selectedFieldId, setSelectedFieldId] = useState<string>('ALL')
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL')

  const fieldMap = new Map<string, Field>(fields.map((f) => [f.id, f]))

  const filteredEvents = events.filter((e) => {
    if (selectedFieldId !== 'ALL' && e.fieldId !== selectedFieldId) return false
    if (selectedStatus !== 'ALL' && e.status !== selectedStatus) return false
    return true
  })

  return (
    <div className="space-y-4">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <div>
          <h3 className="text-lg font-semibold tracking-tight text-foreground">
            {t.water.events.title}
          </h3>
          <p className="text-xs text-muted-foreground">
            Total {filteredEvents.length} operational records
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2 w-full sm:w-auto">
          {/* Field Filter */}
          <div className="flex items-center gap-1.5 bg-background border border-input rounded-md px-2 py-1 text-xs">
            <Filter className="w-3.5 h-3.5 text-muted-foreground" />
            <select
              value={selectedFieldId}
              onChange={(e) => setSelectedFieldId(e.target.value)}
              className="bg-transparent focus:outline-none text-xs"
            >
              <option value="ALL">All Fields</option>
              {fields.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </select>
          </div>

          {/* Status Filter */}
          <div className="flex items-center gap-1.5 bg-background border border-input rounded-md px-2 py-1 text-xs">
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="bg-transparent focus:outline-none text-xs"
            >
              <option value="ALL">All Statuses</option>
              <option value="STARTED">{t.water.statuses.STARTED}</option>
              <option value="COMPLETED">{t.water.statuses.COMPLETED}</option>
              <option value="CANCELLED">{t.water.statuses.CANCELLED}</option>
            </select>
          </div>

          {/* Action Buttons */}
          {canManage && (
            <div className="flex items-center gap-1.5 ml-auto sm:ml-0">
              <Button
                variant="outline"
                size="sm"
                onClick={onStartClick}
                className="gap-1.5 text-xs h-8 text-amber-700 dark:text-amber-300 border-amber-300 dark:border-amber-700 bg-amber-50/60"
              >
                <Play className="w-3.5 h-3.5 fill-amber-500" />
                <span>{t.water.events.startEvent}</span>
              </Button>
              <Button
                size="sm"
                onClick={onRecordClick}
                className="gap-1.5 text-xs h-8"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>{t.water.events.recordCompleted}</span>
              </Button>
            </div>
          )}
        </div>
      </div>

      {/* Empty State */}
      {filteredEvents.length === 0 && !isLoading && (
        <div className="text-center py-12 px-4 rounded-xl border border-dashed border-border bg-card">
          <p className="text-sm font-medium text-muted-foreground">
            {t.water.events.noEvents}
          </p>
          {canManage && (
            <div className="flex justify-center gap-2 mt-3">
              <Button
                variant="outline"
                size="sm"
                onClick={onStartClick}
                className="gap-1.5"
              >
                <Play className="w-3.5 h-3.5" />
                <span>{t.water.events.startEvent}</span>
              </Button>
              <Button
                size="sm"
                onClick={onRecordClick}
                className="gap-1.5"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>{t.water.events.recordCompleted}</span>
              </Button>
            </div>
          )}
        </div>
      )}

      {/* Desktop Table View */}
      {filteredEvents.length > 0 && (
        <div className="hidden md:block rounded-lg border border-border bg-card overflow-hidden">
          <Table>
            <TableHeader className="bg-muted/40">
              <TableRow>
                <TableHead>{t.water.events.field}</TableHead>
                <TableHead>{t.water.events.startedAt}</TableHead>
                <TableHead>{t.water.events.endedAt}</TableHead>
                <TableHead>{t.water.events.measuredVolume}</TableHead>
                <TableHead>{t.water.events.status}</TableHead>
                <TableHead>{t.water.events.notes}</TableHead>
                <TableHead className="text-right">{t.common.actions}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredEvents.map((evt) => (
                <IrrigationEventRow
                  key={evt.id}
                  event={evt}
                  field={fieldMap.get(evt.fieldId)}
                  canManage={canManage}
                  lang={lang}
                  onComplete={onCompleteClick}
                  onCancel={onCancelClick}
                />
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {/* Mobile Stacked Card View */}
      {filteredEvents.length > 0 && (
        <div className="grid grid-cols-1 gap-3 md:hidden">
          {filteredEvents.map((evt) => (
            <IrrigationEventCard
              key={evt.id}
              event={evt}
              field={fieldMap.get(evt.fieldId)}
              canManage={canManage}
              lang={lang}
              onComplete={onCompleteClick}
              onCancel={onCancelClick}
            />
          ))}
        </div>
      )}
    </div>
  )
}
