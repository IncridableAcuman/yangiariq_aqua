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
  waterSourceSchema,
  WaterSourceFormData,
  WATER_SOURCE_TYPES,
} from '../schemas/waterSchemas'
import { WaterSource } from '@/entities/water'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface WaterSourceFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  sourceToEdit?: WaterSource | null
  isLoading?: boolean
  lang: SupportedLanguage
  onSubmitSource: (data: WaterSourceFormData, id?: string) => Promise<void>
}

export const WaterSourceFormDialog: React.FC<WaterSourceFormDialogProps> = ({
  open,
  onOpenChange,
  sourceToEdit,
  isLoading = false,
  lang,
  onSubmitSource,
}) => {
  const t = translations[lang]
  const isEditing = Boolean(sourceToEdit)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<WaterSourceFormData>({
    resolver: zodResolver(waterSourceSchema),
    defaultValues: {
      name: '',
      type: 'CANAL',
      description: '',
    },
  })

  useEffect(() => {
    if (open) {
      if (sourceToEdit) {
        reset({
          name: sourceToEdit.name,
          type: sourceToEdit.type,
          description: sourceToEdit.description || '',
        })
      } else {
        reset({
          name: '',
          type: 'CANAL',
          description: '',
        })
      }
    }
  }, [open, sourceToEdit, reset])

  const onSubmit = async (data: WaterSourceFormData) => {
    try {
      await onSubmitSource(data, sourceToEdit?.id)
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
          <DialogTitle>
            {isEditing ? t.water.sources.edit : t.water.sources.create}
          </DialogTitle>
          <DialogDescription className="text-sm text-muted-foreground mt-1">
            Regional hydraulic intake or distribution infrastructure in Yangiariq.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-2">
          {/* Name */}
          <div className="space-y-1.5">
            <Label htmlFor="source-name" className="text-xs font-semibold">
              {t.water.sources.name} *
            </Label>
            <Input
              id="source-name"
              placeholder="e.g. Palvon-Yobi Primary Canal"
              {...register('name')}
            />
            {errors.name && (
              <p className="text-xs text-rose-500">{errors.name.message}</p>
            )}
          </div>

          {/* Type */}
          <div className="space-y-1.5">
            <Label htmlFor="source-type" className="text-xs font-semibold">
              {t.water.sources.type} *
            </Label>
            <select
              id="source-type"
              className="w-full px-3 py-2 text-sm rounded-md border border-input bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              {...register('type')}
            >
              {WATER_SOURCE_TYPES.map((type) => (
                <option key={type} value={type}>
                  {t.water.sourceTypes[type] || type}
                </option>
              ))}
            </select>
            {errors.type && (
              <p className="text-xs text-rose-500">{errors.type.message}</p>
            )}
          </div>

          {/* Description */}
          <div className="space-y-1.5">
            <Label htmlFor="source-description" className="text-xs font-semibold">
              {t.water.sources.description}
            </Label>
            <Textarea
              id="source-description"
              placeholder="e.g. Main gravity canal supplying northern cluster fields"
              rows={3}
              {...register('description')}
            />
            {errors.description && (
              <p className="text-xs text-rose-500">{errors.description.message}</p>
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
