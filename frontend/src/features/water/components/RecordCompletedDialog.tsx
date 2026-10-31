import React, { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Textarea } from '@/shared/ui/textarea'
import { Label } from '@/shared/ui/label'
import {
  recordCompletedEventSchema,
  RecordCompletedEventFormData,
} from '../schemas/waterSchemas'
import { Field } from '@/entities/field'
import { IrrigationPlan } from '@/entities/water'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface RecordCompletedDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  fields: Field[]
  plans: IrrigationPlan[]
  initialFieldId?: string
  initialPlanId?: string
  isLoading?: boolean
  lang: SupportedLanguage
  onRecord: (data: RecordCompletedEventFormData) => Promise<void>
}

export const RecordCompletedDialog: React.FC<RecordCompletedDialogProps> = ({
  open,
  onOpenChange,
  fields,
  plans,
  initialFieldId,
  initialPlanId,
  isLoading = false,
  lang,
  onRecord,
}) => {
  const t = translations[lang]

  const now = new Date()
  const threeHoursAgo = new Date(now.getTime() - 3 * 3600 * 1000)

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors },
  } = useForm<RecordCompletedEventFormData>({
    resolver: zodResolver(recordCompletedEventSchema),
    defaultValues: {
      fieldId: initialFieldId || fields[0]?.id || '',
      irrigationPlanId: initialPlanId || '',
      startAt: threeHoursAgo.toISOString().slice(0, 16),
      endAt: now.toISOString().slice(0, 16),
      actualVolumeM3: 0,
      notes: '',
    },
  })

  useEffect(() => {
    if (open) {
      const curNow = new Date()
      const curPrev = new Date(curNow.getTime() - 3 * 3600 * 1000)
      reset({
        fieldId: initialFieldId || fields[0]?.id || '',
        irrigationPlanId: initialPlanId || '',
        startAt: curPrev.toISOString().slice(0, 16),
        endAt: curNow.toISOString().slice(0, 16),
        actualVolumeM3: 0,
        notes: '',
      })
    }
  }, [open, initialFieldId, initialPlanId, fields, reset])

  const selectedFieldId = watch('fieldId')
  const availablePlans = plans.filter(
    (p) => (!selectedFieldId || p.fieldId === selectedFieldId) && p.status !== 'CANCELLED'
  )

  const onSubmit = async (data: RecordCompletedEventFormData) => {
    try {
      await onRecord({
        ...data,
        startAt: new Date(data.startAt).toISOString(),
        endAt: new Date(data.endAt).toISOString(),
        irrigationPlanId: data.irrigationPlanId || undefined,
      })
      reset()
      onOpenChange(false)
    } catch {
      // Handled by caller
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{t.water.events.recordCompleted}</DialogTitle>
          <DialogDescription className="text-sm text-muted-foreground mt-1">
            Log historical or completed irrigation delivery with measured volume.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-2">
          {/* Field Selection */}
          <div className="space-y-1.5">
            <Label htmlFor="record-fieldId" className="text-xs font-semibold">
              {t.water.allocations.field} *
            </Label>
            <select
              id="record-fieldId"
              className="w-full px-3 py-2 text-sm rounded-md border border-input bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              {...register('fieldId', {
                onChange: () => setValue('irrigationPlanId', ''),
              })}
            >
              <option value="">{t.water.summary.selectField}</option>
              {fields.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name} ({f.areaHectares} ha)
                </option>
              ))}
            </select>
            {errors.fieldId && (
              <p className="text-xs text-rose-500">{errors.fieldId.message}</p>
            )}
          </div>

          {/* Irrigation Plan (optional) */}
          <div className="space-y-1.5">
            <Label htmlFor="record-planId" className="text-xs font-semibold">
              {t.water.events.plan} (Optional)
            </Label>
            <select
              id="record-planId"
              className="w-full px-3 py-2 text-sm rounded-md border border-input bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              {...register('irrigationPlanId')}
            >
              <option value="">None / Direct Watering</option>
              {availablePlans.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.method} — {p.plannedVolumeM3} m³ ({new Date(p.plannedStartAt).toLocaleDateString()}) [{p.status}]
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {/* Start At */}
            <div className="space-y-1.5">
              <Label htmlFor="record-startAt" className="text-xs font-semibold">
                {t.water.events.startedAt} *
              </Label>
              <Input
                id="record-startAt"
                type="datetime-local"
                {...register('startAt')}
              />
              {errors.startAt && (
                <p className="text-xs text-rose-500">{errors.startAt.message}</p>
              )}
            </div>

            {/* End At */}
            <div className="space-y-1.5">
              <Label htmlFor="record-endAt" className="text-xs font-semibold">
                {t.water.events.endedAt} *
              </Label>
              <Input
                id="record-endAt"
                type="datetime-local"
                {...register('endAt')}
              />
              {errors.endAt && (
                <p className="text-xs text-rose-500">{errors.endAt.message}</p>
              )}
            </div>
          </div>

          {/* Actual Volume */}
          <div className="space-y-1.5">
            <Label htmlFor="record-volume" className="text-xs font-semibold">
              {t.water.events.measuredVolume} *
            </Label>
            <Input
              id="record-volume"
              type="number"
              step="0.1"
              min="0"
              placeholder="e.g. 520.0"
              {...register('actualVolumeM3')}
            />
            {errors.actualVolumeM3 && (
              <p className="text-xs text-rose-500">{errors.actualVolumeM3.message}</p>
            )}
          </div>

          {/* Notes */}
          <div className="space-y-1.5">
            <Label htmlFor="record-notes" className="text-xs font-semibold">
              {t.water.events.notes}
            </Label>
            <Textarea
              id="record-notes"
              placeholder="e.g. Furrow watering executed according to schedule"
              rows={2}
              {...register('notes')}
            />
            {errors.notes && (
              <p className="text-xs text-rose-500">{errors.notes.message}</p>
            )}
          </div>

          <DialogFooter className="gap-2 sm:gap-0 pt-2">
            <Button
              type="button"
              variant="outline"
              disabled={isLoading}
              onClick={() => onOpenChange(false)}
            >
              {t.common.cancel}
            </Button>
            <Button type="submit" disabled={isLoading} className="bg-primary hover:bg-primary/90">
              {isLoading ? t.common.processing : t.water.events.recordCompleted}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
