import React, { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  Button,
  Input,
  Label,
} from '@/shared/ui'
import { FileBarChart } from 'lucide-react'
import { translations, SupportedLang } from '@/shared/i18n'

interface GenerateReportDialogProps {
  isOpen: boolean
  onClose: () => void
  pilotId: string
  onSubmit: (payload: {
    periodStart?: string
    periodEnd?: string
    limitationsText?: string
    recommendationsText?: string
  }) => Promise<void>
  lang: SupportedLang
}

export const GenerateReportDialog: React.FC<GenerateReportDialogProps> = ({
  isOpen,
  onClose,
  onSubmit,
  lang,
}) => {
  const t = translations[lang].pilot

  const [periodStart, setPeriodStart] = useState(
    new Date(Date.now() - 30 * 24 * 3600 * 1000).toISOString().slice(0, 10)
  )
  const [periodEnd, setPeriodEnd] = useState(new Date().toISOString().slice(0, 10))
  const [limitationsText, setLimitationsText] = useState(
    'Hardware gateway currently in emulation mode awaiting physical LoRaWAN probes; weather inputs cross-verified using municipal open-data sensors.'
  )
  const [recommendationsText, setRecommendationsText] = useState(
    'Proceed with field sensor deployment; expand Four-Eyes peer verifier pool across Water Users Association (WUA) agronomists.'
  )
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setIsSubmitting(true)
    try {
      await onSubmit({
        periodStart: periodStart ? new Date(periodStart).toISOString() : undefined,
        periodEnd: periodEnd ? new Date(periodEnd).toISOString() : undefined,
        limitationsText: limitationsText.trim() || undefined,
        recommendationsText: recommendationsText.trim() || undefined,
      })
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Generation failed')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <FileBarChart className="h-5 w-5 text-primary" />
            <DialogTitle className="text-lg font-bold">
              {t.generateReportTitle}
            </DialogTitle>
          </div>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 py-2">
          {error && (
            <div className="p-3 text-xs bg-destructive/10 text-destructive border border-destructive/20 rounded-md">
              {error}
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <Label className="text-xs font-semibold">{t.periodStart}</Label>
              <Input
                type="date"
                value={periodStart}
                onChange={(e) => setPeriodStart(e.target.value)}
                className="h-8 text-xs mt-1"
              />
            </div>
            <div>
              <Label className="text-xs font-semibold">{t.periodEnd}</Label>
              <Input
                type="date"
                value={periodEnd}
                onChange={(e) => setPeriodEnd(e.target.value)}
                className="h-8 text-xs mt-1"
              />
            </div>
          </div>

          <div>
            <Label className="text-xs font-semibold">{t.limitationsNotes}</Label>
            <textarea
              rows={3}
              value={limitationsText}
              onChange={(e) => setLimitationsText(e.target.value)}
              placeholder="State any hardware, data availability, or field limitations..."
              className="w-full text-xs rounded-md border border-input bg-background p-2.5 mt-1 focus:outline-none focus:ring-1 focus:ring-ring"
            />
          </div>

          <div>
            <Label className="text-xs font-semibold">{t.recommendationsNotes}</Label>
            <textarea
              rows={3}
              value={recommendationsText}
              onChange={(e) => setRecommendationsText(e.target.value)}
              placeholder="Operational steps, hardware readiness, protocol enhancements..."
              className="w-full text-xs rounded-md border border-input bg-background p-2.5 mt-1 focus:outline-none focus:ring-1 focus:ring-ring"
            />
          </div>

          <DialogFooter className="pt-2">
            <Button type="button" variant="outline" size="sm" onClick={onClose}>
              {t.cancel}
            </Button>
            <Button type="submit" size="sm" disabled={isSubmitting}>
              {isSubmitting ? t.generating : t.generate}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
