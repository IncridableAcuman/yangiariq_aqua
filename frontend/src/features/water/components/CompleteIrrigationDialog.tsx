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
  completeIrrigationEventSchema,
  CompleteIrrigationEventFormData,
} from '../schemas/waterSchemas'
import { IrrigationEvent } from '@/entities/water'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface CompleteIrrigationDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  event: IrrigationEvent | null
  isLoading?: boolean
  lang: SupportedLanguage
  onComplete: (eventId: string, data: CompleteIrrigationEventFormData) => Promise<void>
}

export const CompleteIrrigationDialog: React.FC<CompleteIrrigationDialogProps> = ({
  open,
  onOpenChange,
  event,
  isLoading = false,
  lang,
  onComplete,
}) => {
  const t = translations[lang]

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CompleteIrrigationEventFormData>({
    resolver: zodResolver(completeIrrigationEventSchema),
    defaultValues: {
      endAt: new Date().toISOString().slice(0, 16),
      actualVolumeM3: 0,
      notes: '',
    },
  })

  useEffect(() => {
    if (open) {
      reset({
        endAt: new Date().toISOString().slice(0, 16),
        actualVolumeM3: 0,
        notes: '',
      })
    }
  }, [open, reset])

  const onSubmit = async (data: CompleteIrrigationEventFormData) => {
    if (!event) return
    try {
      await onComplete(event.id, {
        ...data,
        endAt: new Date(data.endAt).toISOString(),
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
          <DialogTitle className="text-foreground">
            {t.water.events.completeEvent}
          </DialogTitle>
          <DialogDescription className="text-sm text-muted-foreground mt-1">
            Record actual water volume measured by flow meter or weir gauge.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-2">
          <div className="space-y-1.5">
            <Label htmlFor="complete-endAt" className="text-xs font-semibold">
              {t.water.events.endedAt} *
            </Label>
            <Input
              id="complete-endAt"
              type="datetime-local"
              {...register('endAt')}
            />
            {errors.endAt && (
              <p className="text-xs text-rose-500">{errors.endAt.message}</p>
            )}
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="complete-volume" className="text-xs font-semibold">
              {t.water.events.measuredVolume} *
            </Label>
            <Input
              id="complete-volume"
              type="number"
              step="0.1"
              min="0"
              placeholder="e.g. 450.5"
              {...register('actualVolumeM3')}
            />
            {errors.actualVolumeM3 && (
              <p className="text-xs text-rose-500">{errors.actualVolumeM3.message}</p>
            )}
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="complete-notes" className="text-xs font-semibold">
              {t.water.events.notes}
            </Label>
            <Textarea
              id="complete-notes"
              placeholder="e.g. Sluice gate closed, watering completed uniformly"
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
            <Button type="submit" disabled={isLoading} className="bg-teal-600 hover:bg-teal-700">
              {isLoading ? t.common.processing : t.water.events.completeEvent}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
