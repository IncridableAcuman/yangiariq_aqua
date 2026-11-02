import { useState } from 'react'
import { XCircle, X } from 'lucide-react'
import { SupportedLanguage } from '@/shared/i18n'

interface PlanRejectDialogProps {
  isOpen: boolean
  onClose: () => void
  onConfirm: (reason: string) => Promise<void>
  fieldName: string
  isLoading?: boolean
  lang: SupportedLanguage
}

export function PlanRejectDialog({
  isOpen,
  onClose,
  onConfirm,
  fieldName,
  isLoading,
  lang,
}: PlanRejectDialogProps) {
  const [reason, setReason] = useState('')
  const [error, setError] = useState<string | null>(null)

  if (!isOpen) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!reason.trim() || reason.trim().length < 3) {
      setError(
        lang === 'uz'
          ? 'Rad etish sababi kamida 3 belgidan iborat boʻlishi shart'
          : 'Причина отклонения должна содержать минимум 3 символа'
      )
      return
    }

    setError(null)
    await onConfirm(reason.trim())
    onClose()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-card border border-border w-full max-w-md rounded-2xl shadow-2xl overflow-hidden flex flex-col">
        {/* Header */}
        <div className="px-6 py-4 border-b border-border flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <div className="h-8 w-8 rounded-lg bg-rose-500/10 text-rose-600 dark:text-rose-400 flex items-center justify-center">
              <XCircle className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-foreground">
                {lang === 'uz' ? 'Rejani Rad Etish' : 'Отклонение плана'}
              </h3>
              <p className="text-xs text-muted-foreground">{fieldName}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-muted-foreground hover:text-foreground transition-colors p-1"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Content Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <p className="text-xs text-muted-foreground">
            {lang === 'uz'
              ? 'Rejani rad etish sababini kiriting. Ushbu maʼlumot operatsion audit jurnaliga yoziladi.'
              : 'Укажите причину отклонения плана. Данные сохраняются в журнале аудита.'}
          </p>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">
              {lang === 'uz' ? 'Rad etish sababi (majburiy)' : 'Причина отклонения (обязательно)'}
            </label>
            <textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder={
                lang === 'uz'
                  ? 'Masalan: Yomgʻir kutilmoqda, tuproq yetarli namlikka ega...'
                  : 'Ожидаются осадки, полив не требуется...'
              }
              rows={3}
              required
              className="w-full text-xs p-2.5 rounded-lg border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-rose-500/20"
            />
            {error && <p className="text-[11px] text-rose-600 dark:text-rose-400">{error}</p>}
          </div>

          {/* Footer Actions */}
          <div className="pt-2 flex items-center justify-end space-x-2 border-t border-border">
            <button
              type="button"
              onClick={onClose}
              disabled={isLoading}
              className="px-4 py-2 rounded-lg text-xs font-medium text-muted-foreground hover:bg-muted transition-colors"
            >
              {lang === 'uz' ? 'Bekor qilish' : 'Отмена'}
            </button>
            <button
              type="submit"
              disabled={isLoading || !reason.trim()}
              className="px-4 py-2 rounded-lg text-xs font-semibold bg-rose-600 text-white hover:bg-rose-700 disabled:opacity-50 transition-all shadow-sm"
            >
              {isLoading
                ? lang === 'uz'
                  ? 'Rad etilmoqda...'
                  : 'Отклонение...'
                : lang === 'uz'
                ? 'Rad etishni tasdiqlash'
                : 'Подтвердить отклонение'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
