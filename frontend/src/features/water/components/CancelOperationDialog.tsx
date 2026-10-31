import React from 'react'
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
import { Textarea } from '@/shared/ui/textarea'
import { Label } from '@/shared/ui/label'
import { cancelOperationSchema, CancelOperationFormData } from '../schemas/waterSchemas'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface CancelOperationDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  description: string
  confirmText?: string
  isDestructive?: boolean
  isLoading?: boolean
  lang: SupportedLanguage
  onConfirm: (reason?: string) => Promise<void>
}

export const CancelOperationDialog: React.FC<CancelOperationDialogProps> = ({
  open,
  onOpenChange,
  title,
  description,
  confirmText,
  isDestructive = true,
  isLoading = false,
  lang,
  onConfirm,
}) => {
  const t = translations[lang]

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CancelOperationFormData>({
    resolver: zodResolver(cancelOperationSchema),
    defaultValues: { reason: '' },
  })

  const onSubmit = async (data: CancelOperationFormData) => {
    try {
      await onConfirm(data.reason || undefined)
      reset()
      onOpenChange(false)
    } catch {
      // Handled by mutation
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[460px]">
        <DialogHeader>
          <DialogTitle className={isDestructive ? 'text-rose-600 dark:text-rose-400' : ''}>
            {title}
          </DialogTitle>
          <DialogDescription className="text-sm text-muted-foreground mt-1">
            {description}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-2">
          <div className="space-y-1.5">
            <Label htmlFor="cancel-reason" className="text-xs font-semibold">
              {t.water.events.notes} (Optional)
            </Label>
            <Textarea
              id="cancel-reason"
              placeholder="e.g. Operation cancelled due to rainfall or season completion"
              rows={3}
              {...register('reason')}
            />
            {errors.reason && (
              <p className="text-xs text-rose-500">{errors.reason.message}</p>
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
            <Button
              type="submit"
              variant={isDestructive ? 'destructive' : 'default'}
              disabled={isLoading}
            >
              {isLoading
                ? t.common.processing
                : confirmText || (isDestructive ? t.common.cancel : t.common.save)}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
