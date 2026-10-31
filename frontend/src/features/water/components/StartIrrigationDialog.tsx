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
  startIrrigationEventSchema,
  StartIrrigationEventFormData,
} from '../schemas/waterSchemas'
import { Field } from '@/entities/field'
import { IrrigationPlan } from '@/entities/water'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface StartIrrigationDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  fields: Field[]
  plans: IrrigationPlan[]
  initialFieldId?: string
  initialPlanId?: string
  isLoading?: boolean
  lang: SupportedLanguage
  onStart: (data: StartIrrigationEventFormData) => Promise<void>
}

export const StartIrrigationDialog: React.FC<StartIrrigationDialogProps> = ({
  open,
  onOpenChange,
  fields,
  plans,
  initialFieldId,
  initialPlanId,
  isLoading = false,
  lang,
  onStart,
}) => {
  const t = translations[lang]

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors },
  } = useForm<StartIrrigationEventFormData>({
    resolver: zodResolver(startIrrigationEventSchema),
    defaultValues: {
      fieldId: initialFieldId || fields[0]?.id || '',
      irrigationPlanId: initialPlanId || '',
      startAt: new Date().toISOString().slice(0, 16),
      notes: '',
    },
  })

  useEffect(() => {
    if (open) {
      reset({
        fieldId: initialFieldId || fields[0]?.id || '',
        irrigationPlanId: initialPlanId || '',
        startAt: new Date().toISOString().slice(0, 16),
        notes: '',
      })
    }
  }, [open, initialFieldId, initialPlanId, fields, reset])

  const selectedFieldId = watch('fieldId')
  const availablePlans = plans.filter(
    (p) => (!selectedFieldId || p.fieldId === selectedFieldId) && p.status === 'PLANNED'
  )

  const onSubmit = async (data: StartIrrigationEventFormData) => {
    try {
      await onStart({
        ...data,
        startAt: new Date(data.startAt).toISOString(),
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
      <DialogContent className="sm:max-w-[480px]">
        <DialogHeader>
          <DialogTitle>{t.water.events.startEvent}</DialogTitle>
          <DialogDescription className="text-sm text-muted-foreground mt-1">
            Initiate real-time watering operation on the selected parcel.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-2">
          {/* Field Selection */}
          <div className="space-y-1.5">
            <Label htmlFor="start-fieldId" className="text-xs font-semibold">
              {t.water.allocations.field} *
            </Label>
            <select
              id="start-fieldId"
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
            <Label htmlFor="start-planId" className="text-xs font-semibold">
              {t.water.events.plan} (Optional)
            </Label>
            <select
              id="start-planId"
              className="w-full px-3 py-2 text-sm rounded-md border border-input bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              {...register('irrigationPlanId')}
            >
              <option value="">None / Unscheduled Watering</option>
              {availablePlans.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.method} — {p.plannedVolumeM3} m³ ({new Date(p.plannedStartAt).toLocaleDateString()})
                </option>
              ))}
            </select>
          </div>

          {/* Start At */}
          <div className="space-y-1.5">
            <Label htmlFor="start-time" className="text-xs font-semibold">
              {t.water.events.startedAt} *
            </Label>
            <Input
              id="start-time"
              type="datetime-local"
              {...register('startAt')}
            />
            {errors.startAt && (
              <p className="text-xs text-rose-500">{errors.startAt.message}</p>
            )}
          </div>

          {/* Notes */}
          <div className="space-y-1.5">
            <Label htmlFor="start-notes" className="text-xs font-semibold">
              {t.water.events.notes}
            </Label>
            <Textarea
              id="start-notes"
              placeholder="e.g. Sluice intake opened, canal level optimal"
              rows={3}
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
            <Button type="submit" disabled={isLoading} className="bg-amber-600 hover:bg-amber-700">
              {isLoading ? t.common.processing : t.water.events.startEvent}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
