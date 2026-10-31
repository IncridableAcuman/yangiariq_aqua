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
  irrigationPlanSchema,
  IrrigationPlanFormData,
  IRRIGATION_METHODS,
} from '../schemas/waterSchemas'
import { Field } from '@/entities/field'
import { WaterAllocation, IrrigationPlan } from '@/entities/water'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface PlanFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  fields: Field[]
  allocations: WaterAllocation[]
  planToEdit?: IrrigationPlan | null
  isLoading?: boolean
  lang: SupportedLanguage
  onSubmitPlan: (data: IrrigationPlanFormData, id?: string) => Promise<void>
}

export const PlanFormDialog: React.FC<PlanFormDialogProps> = ({
  open,
  onOpenChange,
  fields,
  allocations,
  planToEdit,
  isLoading = false,
  lang,
  onSubmitPlan,
}) => {
  const t = translations[lang]
  const isEditing = Boolean(planToEdit)

  const tomorrow = new Date(Date.now() + 24 * 3600 * 1000)
  const tomorrowPlus8h = new Date(Date.now() + 32 * 3600 * 1000)

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors },
  } = useForm<IrrigationPlanFormData>({
    resolver: zodResolver(irrigationPlanSchema),
    defaultValues: {
      fieldId: '',
      waterAllocationId: '',
      plannedStartAt: tomorrow.toISOString().slice(0, 16),
      plannedEndAt: tomorrowPlus8h.toISOString().slice(0, 16),
      plannedVolumeM3: 400,
      method: 'FURROW',
      notes: '',
    },
  })

  useEffect(() => {
    if (open) {
      if (planToEdit) {
        reset({
          fieldId: planToEdit.fieldId,
          waterAllocationId: planToEdit.waterAllocationId || '',
          plannedStartAt: planToEdit.plannedStartAt.slice(0, 16),
          plannedEndAt: planToEdit.plannedEndAt.slice(0, 16),
          plannedVolumeM3: planToEdit.plannedVolumeM3,
          method: planToEdit.method,
          notes: planToEdit.notes || '',
        })
      } else {
        const tStart = new Date(Date.now() + 24 * 3600 * 1000)
        const tEnd = new Date(Date.now() + 32 * 3600 * 1000)
        reset({
          fieldId: fields[0]?.id || '',
          waterAllocationId: '',
          plannedStartAt: tStart.toISOString().slice(0, 16),
          plannedEndAt: tEnd.toISOString().slice(0, 16),
          plannedVolumeM3: 400,
          method: 'FURROW',
          notes: '',
        })
      }
    }
  }, [open, planToEdit, fields, reset])

  const selectedFieldId = watch('fieldId')
  const availableAllocations = allocations.filter(
    (a) => (!selectedFieldId || a.fieldId === selectedFieldId) && a.status === 'ACTIVE'
  )

  const onSubmit = async (data: IrrigationPlanFormData) => {
    try {
      await onSubmitPlan(
        {
          ...data,
          plannedStartAt: new Date(data.plannedStartAt).toISOString(),
          plannedEndAt: new Date(data.plannedEndAt).toISOString(),
          waterAllocationId: data.waterAllocationId || undefined,
        },
        planToEdit?.id
      )
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
          <DialogTitle>
            {isEditing ? t.water.plans.edit : t.water.plans.create}
          </DialogTitle>
          <DialogDescription className="text-sm text-muted-foreground mt-1">
            Schedule an upcoming parcel irrigation event.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-2">
          {/* Field Selection */}
          <div className="space-y-1.5">
            <Label htmlFor="plan-fieldId" className="text-xs font-semibold">
              {t.water.plans.field} *
            </Label>
            <select
              id="plan-fieldId"
              disabled={isEditing}
              className="w-full px-3 py-2 text-sm rounded-md border border-input bg-background focus:outline-none focus:ring-2 focus:ring-ring disabled:opacity-50"
              {...register('fieldId', {
                onChange: () => setValue('waterAllocationId', ''),
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

          {/* Water Allocation (optional) */}
          <div className="space-y-1.5">
            <Label htmlFor="plan-allocId" className="text-xs font-semibold">
              {t.water.allocations.title} (Optional)
            </Label>
            <select
              id="plan-allocId"
              disabled={isEditing}
              className="w-full px-3 py-2 text-sm rounded-md border border-input bg-background focus:outline-none focus:ring-2 focus:ring-ring disabled:opacity-50"
              {...register('waterAllocationId')}
            >
              <option value="">None / Unlinked</option>
              {availableAllocations.map((a) => (
                <option key={a.id} value={a.id}>
                  Quota: {a.allocatedVolumeM3} m³ ({new Date(a.periodStart).toLocaleDateString()} - {new Date(a.periodEnd).toLocaleDateString()})
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {/* Planned Start At */}
            <div className="space-y-1.5">
              <Label htmlFor="plan-start" className="text-xs font-semibold">
                {t.water.plans.plannedStart} *
              </Label>
              <Input
                id="plan-start"
                type="datetime-local"
                {...register('plannedStartAt')}
              />
              {errors.plannedStartAt && (
                <p className="text-xs text-rose-500">{errors.plannedStartAt.message}</p>
              )}
            </div>

            {/* Planned End At */}
            <div className="space-y-1.5">
              <Label htmlFor="plan-end" className="text-xs font-semibold">
                {t.water.plans.plannedEnd} *
              </Label>
              <Input
                id="plan-end"
                type="datetime-local"
                {...register('plannedEndAt')}
              />
              {errors.plannedEndAt && (
                <p className="text-xs text-rose-500">{errors.plannedEndAt.message}</p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {/* Planned Volume */}
            <div className="space-y-1.5">
              <Label htmlFor="plan-volume" className="text-xs font-semibold">
                {t.water.plans.plannedVolume} *
              </Label>
              <Input
                id="plan-volume"
                type="number"
                step="1"
                min="1"
                placeholder="e.g. 450"
                {...register('plannedVolumeM3')}
              />
              {errors.plannedVolumeM3 && (
                <p className="text-xs text-rose-500">{errors.plannedVolumeM3.message}</p>
              )}
            </div>

            {/* Irrigation Method */}
            <div className="space-y-1.5">
              <Label htmlFor="plan-method" className="text-xs font-semibold">
                {t.water.plans.method} *
              </Label>
              <select
                id="plan-method"
                className="w-full px-3 py-2 text-sm rounded-md border border-input bg-background focus:outline-none focus:ring-2 focus:ring-ring"
                {...register('method')}
              >
                {IRRIGATION_METHODS.map((m) => (
                  <option key={m} value={m}>
                    {t.water.methods[m] || m}
                  </option>
                ))}
              </select>
              {errors.method && (
                <p className="text-xs text-rose-500">{errors.method.message}</p>
              )}
            </div>
          </div>

          {/* Notes */}
          <div className="space-y-1.5">
            <Label htmlFor="plan-notes" className="text-xs font-semibold">
              {t.water.events.notes}
            </Label>
            <Textarea
              id="plan-notes"
              placeholder="e.g. Vegetative furrow flush before afternoon heat"
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
            <Button type="submit" disabled={isLoading}>
              {isLoading ? t.common.processing : t.common.save}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
