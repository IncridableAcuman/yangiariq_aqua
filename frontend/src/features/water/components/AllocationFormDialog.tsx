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
  waterAllocationSchema,
  WaterAllocationFormData,
} from '../schemas/waterSchemas'
import { Field } from '@/entities/field'
import { WaterSource, WaterAllocation } from '@/entities/water'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface AllocationFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  fields: Field[]
  sources: WaterSource[]
  allocationToEdit?: WaterAllocation | null
  isLoading?: boolean
  lang: SupportedLanguage
  onSubmitAllocation: (data: WaterAllocationFormData, id?: string) => Promise<void>
}

export const AllocationFormDialog: React.FC<AllocationFormDialogProps> = ({
  open,
  onOpenChange,
  fields,
  sources,
  allocationToEdit,
  isLoading = false,
  lang,
  onSubmitAllocation,
}) => {
  const t = translations[lang]
  const isEditing = Boolean(allocationToEdit)

  const activeSources = sources.filter(
    (s) => s.status === 'ACTIVE' || (isEditing && s.id === allocationToEdit?.waterSourceId)
  )

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<WaterAllocationFormData>({
    resolver: zodResolver(waterAllocationSchema),
    defaultValues: {
      fieldId: '',
      waterSourceId: '',
      allocatedVolumeM3: 1000,
      periodStart: new Date().toISOString().slice(0, 10),
      periodEnd: new Date(Date.now() + 180 * 24 * 3600 * 1000).toISOString().slice(0, 10),
      notes: '',
    },
  })

  useEffect(() => {
    if (open) {
      if (allocationToEdit) {
        reset({
          fieldId: allocationToEdit.fieldId,
          waterSourceId: allocationToEdit.waterSourceId,
          allocatedVolumeM3: allocationToEdit.allocatedVolumeM3,
          periodStart: allocationToEdit.periodStart.slice(0, 10),
          periodEnd: allocationToEdit.periodEnd.slice(0, 10),
          notes: allocationToEdit.notes || '',
        })
      } else {
        reset({
          fieldId: fields[0]?.id || '',
          waterSourceId: activeSources[0]?.id || '',
          allocatedVolumeM3: 1000,
          periodStart: new Date().toISOString().slice(0, 10),
          periodEnd: new Date(Date.now() + 180 * 24 * 3600 * 1000).toISOString().slice(0, 10),
          notes: '',
        })
      }
    }
  }, [open, allocationToEdit, fields, activeSources, reset])

  const onSubmit = async (data: WaterAllocationFormData) => {
    try {
      await onSubmitAllocation(
        {
          ...data,
          periodStart: new Date(data.periodStart).toISOString(),
          periodEnd: new Date(`${data.periodEnd}T23:59:59Z`).toISOString(),
        },
        allocationToEdit?.id
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
            {isEditing ? t.water.allocations.edit : t.water.allocations.create}
          </DialogTitle>
          <DialogDescription className="text-sm text-muted-foreground mt-1">
            Assign seasonal water volume quota to parcel from an active water source.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-2">
          {/* Field Selection */}
          <div className="space-y-1.5">
            <Label htmlFor="alloc-fieldId" className="text-xs font-semibold">
              {t.water.allocations.field} *
            </Label>
            <select
              id="alloc-fieldId"
              disabled={isEditing}
              className="w-full px-3 py-2 text-sm rounded-md border border-input bg-background focus:outline-none focus:ring-2 focus:ring-ring disabled:opacity-50"
              {...register('fieldId')}
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

          {/* Water Source Selection */}
          <div className="space-y-1.5">
            <Label htmlFor="alloc-sourceId" className="text-xs font-semibold">
              {t.water.allocations.source} *
            </Label>
            <select
              id="alloc-sourceId"
              disabled={isEditing}
              className="w-full px-3 py-2 text-sm rounded-md border border-input bg-background focus:outline-none focus:ring-2 focus:ring-ring disabled:opacity-50"
              {...register('waterSourceId')}
            >
              <option value="">Select Water Source</option>
              {activeSources.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} ({s.type}) [{s.status}]
                </option>
              ))}
            </select>
            {errors.waterSourceId && (
              <p className="text-xs text-rose-500">{errors.waterSourceId.message}</p>
            )}
          </div>

          {/* Allocated Volume */}
          <div className="space-y-1.5">
            <Label htmlFor="alloc-volume" className="text-xs font-semibold">
              {t.water.allocations.volume} *
            </Label>
            <Input
              id="alloc-volume"
              type="number"
              step="1"
              min="1"
              placeholder="e.g. 2500"
              {...register('allocatedVolumeM3')}
            />
            {errors.allocatedVolumeM3 && (
              <p className="text-xs text-rose-500">{errors.allocatedVolumeM3.message}</p>
            )}
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {/* Period Start */}
            <div className="space-y-1.5">
              <Label htmlFor="alloc-start" className="text-xs font-semibold">
                {t.water.summary.dateFrom} *
              </Label>
              <Input
                id="alloc-start"
                type="date"
                {...register('periodStart')}
              />
              {errors.periodStart && (
                <p className="text-xs text-rose-500">{errors.periodStart.message}</p>
              )}
            </div>

            {/* Period End */}
            <div className="space-y-1.5">
              <Label htmlFor="alloc-end" className="text-xs font-semibold">
                {t.water.summary.dateTo} *
              </Label>
              <Input
                id="alloc-end"
                type="date"
                {...register('periodEnd')}
              />
              {errors.periodEnd && (
                <p className="text-xs text-rose-500">{errors.periodEnd.message}</p>
              )}
            </div>
          </div>

          {/* Notes */}
          <div className="space-y-1.5">
            <Label htmlFor="alloc-notes" className="text-xs font-semibold">
              {t.water.events.notes}
            </Label>
            <Textarea
              id="alloc-notes"
              placeholder="e.g. Vegetative season cotton quota"
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
