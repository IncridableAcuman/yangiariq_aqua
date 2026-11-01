import React, { useState } from 'react'
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
import { SupportedLanguage, translations } from '@/shared/i18n'

interface DismissRecommendationDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onConfirm: (reason: string) => Promise<void>
  isLoading?: boolean
  lang: SupportedLanguage
}

export const DismissRecommendationDialog: React.FC<DismissRecommendationDialogProps> = ({
  open,
  onOpenChange,
  onConfirm,
  isLoading = false,
  lang,
}) => {
  const [reason, setReason] = useState('')
  const [error, setError] = useState<string | null>(null)

  const t = translations[lang].recommendations?.actions || {
    dismissReasonLabel: 'Dismissal reason (mandatory)',
    dismissReasonPlaceholder: 'State why this recommendation is not applicable...',
    confirmDismiss: 'Confirm Dismissal',
  }
  const common = translations[lang].common

  const handleConfirm = async () => {
    if (!reason.trim()) {
      setError(
        lang === 'uz'
          ? 'Rad etish sababini koʻrsatish majburiy.'
          : lang === 'ru'
          ? 'Укажите обязательную причину отклонения.'
          : 'Dismissal reason is required.'
      )
      return
    }
    setError(null)
    await onConfirm(reason.trim())
    setReason('')
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>
            {lang === 'uz'
              ? 'Tavsiyani rad etish'
              : lang === 'ru'
              ? 'Отклонить рекомендацию'
              : 'Dismiss Recommendation'}
          </DialogTitle>
          <DialogDescription>
            {lang === 'uz'
              ? 'Agronomik xavfsizlik va audit uchun rad etish sababini kiritishingiz shart.'
              : lang === 'ru'
              ? 'Для агрономического аудита и безопасности необходимо указать причину отклонения.'
              : 'For agronomic safety and auditing, you must provide a reason for dismissing this recommendation.'}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-3 py-2">
          <Label htmlFor="dismiss-reason" className="text-sm font-medium">
            {t.dismissReasonLabel}
          </Label>
          <Textarea
            id="dismiss-reason"
            rows={4}
            value={reason}
            onChange={(e) => {
              setReason(e.target.value)
              if (error) setError(null)
            }}
            placeholder={t.dismissReasonPlaceholder}
            className={error ? 'border-red-500' : ''}
          />
          {error && <p className="text-xs text-red-500">{error}</p>}
        </div>

        <DialogFooter className="gap-2 sm:gap-0">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={isLoading}
          >
            {common.cancel}
          </Button>
          <Button
            type="button"
            variant="destructive"
            onClick={handleConfirm}
            disabled={isLoading || !reason.trim()}
          >
            {isLoading ? common.processing : t.confirmDismiss}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
