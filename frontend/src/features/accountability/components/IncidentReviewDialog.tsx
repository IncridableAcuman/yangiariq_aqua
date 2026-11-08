import React, { useState } from 'react'
import { WaterIncident } from '@/entities/accountability'
import { Button } from '@/shared/ui/button'
import { Textarea } from '@/shared/ui/textarea'
import { CheckCircle2, X, XCircle } from 'lucide-react'

interface Props {
  isOpen: boolean
  onClose: () => void
  incident?: WaterIncident
  onSubmit: (accept: boolean, notes: string) => Promise<void>
  isSubmitting?: boolean
}

export const IncidentReviewDialog: React.FC<Props> = ({
  isOpen,
  onClose,
  incident,
  onSubmit,
  isSubmitting = false,
}) => {
  const [decision, setDecision] = useState<'accept' | 'dismiss'>('accept')
  const [notes, setNotes] = useState('')
  const [error, setError] = useState<string | null>(null)

  if (!isOpen || !incident) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!notes.trim()) {
      setError('Koʻrib chiqish xulosasi va asosi kiritilishi shart')
      return
    }
    setError(null)
    await onSubmit(decision === 'accept', notes.trim())
    setNotes('')
    onClose()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm animate-in fade-in">
      <div className="bg-card border border-border rounded-xl shadow-xl w-full max-w-lg overflow-hidden">
        <div className="p-4 sm:p-5 border-b border-border flex items-center justify-between">
          <div>
            <h3 className="text-sm font-bold text-foreground">Operatsion hodisani koʻrib chiqish</h3>
            <p className="text-xs text-muted-foreground">{incident.incidentNumber} — {incident.title}</p>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose} className="h-7 w-7 p-0">
            <X className="w-4 h-4" />
          </Button>
        </div>

        <form onSubmit={handleSubmit} className="p-4 sm:p-5 space-y-4">
          <div className="space-y-2">
            <label className="text-xs font-semibold text-foreground">Qaror (Review Decision):</label>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => setDecision('accept')}
                className={`flex items-center justify-center gap-2 p-3 rounded-lg border text-xs font-semibold transition-all ${
                  decision === 'accept'
                    ? 'border-emerald-500 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 ring-1 ring-emerald-500'
                    : 'border-border bg-muted/30 text-muted-foreground hover:bg-muted/50'
                }`}
              >
                <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                <span>Qabul qilish (OPEN)</span>
              </button>
              <button
                type="button"
                onClick={() => setDecision('dismiss')}
                className={`flex items-center justify-center gap-2 p-3 rounded-lg border text-xs font-semibold transition-all ${
                  decision === 'dismiss'
                    ? 'border-rose-500 bg-rose-500/10 text-rose-600 dark:text-rose-400 ring-1 ring-rose-500'
                    : 'border-border bg-muted/30 text-muted-foreground hover:bg-muted/50'
                }`}
              >
                <XCircle className="w-4 h-4 text-rose-500" />
                <span>Rad etish (DISMISSED)</span>
              </button>
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">
              Koʻrib chiqish xulosasi va asosi (Review Notes):
            </label>
            <Textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder={
                decision === 'accept'
                  ? 'Faktlar tasdiqlandi, rasmiy tekshiruv ishi ochilsin...'
                  : 'Nega bu hodisa rad etilmoqda (masalan: rejalashtirilgan tozalash oʻtkazilayotgan edi)...'
              }
              rows={4}
              className="text-xs"
            />
            {error && <p className="text-[11px] text-destructive">{error}</p>}
          </div>

          <div className="flex items-center justify-end gap-2 pt-2 border-t border-border">
            <Button type="button" variant="outline" size="sm" onClick={onClose} disabled={isSubmitting}>
              Bekor qilish
            </Button>
            <Button
              type="submit"
              size="sm"
              disabled={isSubmitting}
              className={decision === 'accept' ? 'bg-primary' : 'bg-destructive text-destructive-foreground'}
            >
              {isSubmitting ? 'Saqlanmoqda...' : decision === 'accept' ? 'Ishni ochish (OPEN)' : 'Rad etish (DISMISS)'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
