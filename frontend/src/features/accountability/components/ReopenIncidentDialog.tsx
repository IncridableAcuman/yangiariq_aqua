import React, { useState } from 'react'
import { WaterIncident } from '@/entities/accountability'
import { Button } from '@/shared/ui/button'
import { Textarea } from '@/shared/ui/textarea'
import { RotateCcw, X } from 'lucide-react'

interface Props {
  isOpen: boolean
  onClose: () => void
  incident?: WaterIncident
  onSubmit: (reason: string) => Promise<void>
  isSubmitting?: boolean
}

export const ReopenIncidentDialog: React.FC<Props> = ({
  isOpen,
  onClose,
  incident,
  onSubmit,
  isSubmitting = false,
}) => {
  const [reason, setReason] = useState('')
  const [error, setError] = useState<string | null>(null)

  if (!isOpen || !incident) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!reason.trim()) {
      setError('Qayta ochish sababi va qaytalanganlik faktlari koʻrsatilishi shart')
      return
    }
    setError(null)
    await onSubmit(reason.trim())
    setReason('')
    onClose()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm animate-in fade-in">
      <div className="bg-card border border-border rounded-xl shadow-xl w-full max-w-lg overflow-hidden">
        <div className="p-4 sm:p-5 border-b border-border flex items-center justify-between">
          <div className="flex items-center gap-2">
            <RotateCcw className="w-4 h-4 text-rose-500" />
            <h3 className="text-sm font-bold text-foreground">Hodisani qayta ochish (Reopen)</h3>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose} className="h-7 w-7 p-0">
            <X className="w-4 h-4" />
          </Button>
        </div>

        <form onSubmit={handleSubmit} className="p-4 sm:p-5 space-y-4">
          <p className="text-xs text-muted-foreground">
            Ushbu hodisa (<strong>{incident.incidentNumber}</strong>) avval hal etilgan yoki rad etilgan edi. Agar muammo yana qaytalangan boʻlsa, qaytalanish sababini koʻrsatib qayta ochishingiz mumkin. Qaytalanishlar soni avtomatik oshiriladi.
          </p>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">Qayta ochish sababi va faktlar *:</label>
            <Textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Masalan: Taʼmirlangan flumeda yana 10% tafovut aniqlandi, datchik qayta tekshirilsin..."
              rows={4}
              className="text-xs"
            />
            {error && <p className="text-[11px] text-destructive">{error}</p>}
          </div>

          <div className="flex items-center justify-end gap-2 pt-2 border-t border-border">
            <Button type="button" variant="outline" size="sm" onClick={onClose} disabled={isSubmitting}>
              Bekor qilish
            </Button>
            <Button type="submit" size="sm" disabled={isSubmitting} className="bg-rose-600 hover:bg-rose-700 text-white">
              {isSubmitting ? 'Ochilmoqda...' : 'Hodisani qayta ochish'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
