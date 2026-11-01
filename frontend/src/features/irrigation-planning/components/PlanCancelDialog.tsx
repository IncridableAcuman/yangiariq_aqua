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
import { XCircle, AlertCircle } from 'lucide-react'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface PlanCancelDialogProps {
  planId: string | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onConfirm: (planId: string, reason: string) => Promise<void> | void
  isSubmitting?: boolean
  lang: SupportedLanguage
}

export const PlanCancelDialog: React.FC<PlanCancelDialogProps> = ({
  planId,
  open,
  onOpenChange,
  onConfirm,
  isSubmitting = false,
  lang,
}) => {
  const t = translations[lang]
  const [reason, setReason] = useState<string>('')
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!planId) return

    const trimmed = reason.trim()
    if (!trimmed || trimmed.length < 3) {
      setError(
        t.irrigationPlanning?.cancellation?.reasonRequired ||
          'Bekor qilish sababi kiritilishi shart (kamida 3 belgi).'
      )
      return
    }

    setError(null)
    await onConfirm(planId, trimmed)
    setReason('')
    onOpenChange(false)
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        if (!next) {
          setReason('')
          setError(null)
        }
        onOpenChange(next)
      }}
    >
      <DialogContent className="max-w-md">
        <form onSubmit={handleSubmit} className="space-y-4">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-base font-bold text-rose-600 dark:text-rose-400">
              <XCircle className="w-5 h-5" />
              <span>{t.irrigationPlanning?.cancellation?.title || 'Sugʻorish rejasini bekor qilish'}</span>
            </DialogTitle>
            <DialogDescription className="text-xs text-muted-foreground">
              {t.irrigationPlanning?.cancellation?.subtitle ||
                'Reja bekor qilingach, unga ajratilgan kvota qaytariladi va audit uchun sabab saqlanadi.'}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-2">
            <label className="text-xs font-semibold text-foreground block">
              {t.irrigationPlanning?.cancellation?.reasonLabel || 'Bekor qilish sababi *'}
            </label>
            <textarea
              value={reason}
              onChange={(e) => {
                setReason(e.target.value)
                if (error) setError(null)
              }}
              placeholder={
                t.irrigationPlanning?.cancellation?.placeholder ||
                'Masalan: Kanalda tozalash ishlari yoki nasos agregati taʼmiri...'
              }
              rows={3}
              className="w-full text-xs rounded-md border border-input bg-background px-3 py-2 text-foreground focus:outline-none focus:ring-2 focus:ring-ring"
            />
            {error && (
              <div className="flex items-center gap-1.5 text-xs text-rose-600 font-medium">
                <AlertCircle className="w-3.5 h-3.5" />
                <span>{error}</span>
              </div>
            )}
          </div>

          <DialogFooter className="gap-2 sm:gap-0 pt-2 border-t border-border/60">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              {t.common?.cancel || 'Orqaga'}
            </Button>
            <Button
              type="submit"
              size="sm"
              variant="destructive"
              disabled={isSubmitting || !reason.trim()}
              className="text-xs font-semibold"
            >
              {isSubmitting ? (
                <span>{t.common?.loading || 'Yuklanmoqda...'}</span>
              ) : (
                <span className="flex items-center gap-1.5">
                  <XCircle className="w-3.5 h-3.5" />
                  {t.irrigationPlanning?.cancellation?.confirmButton || 'Rejani bekor qilish'}
                </span>
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
