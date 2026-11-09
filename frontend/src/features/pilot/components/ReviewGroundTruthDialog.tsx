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
import { ShieldCheck, CheckCircle2, XCircle, RefreshCw } from 'lucide-react'
import { GroundTruthMeasurement } from '@/entities/pilot'
import { translations, SupportedLang } from '@/shared/i18n'

interface ReviewGroundTruthDialogProps {
  measurement: GroundTruthMeasurement | null
  isOpen: boolean
  onClose: () => void
  onReview: (payload: {
    action: 'VERIFY' | 'REJECT' | 'REQUEST_REMEASUREMENT'
    evidenceReference?: string
    notes?: string
    rejectionReason?: string
  }) => Promise<void>
  lang: SupportedLang
}

export const ReviewGroundTruthDialog: React.FC<ReviewGroundTruthDialogProps> = ({
  measurement,
  isOpen,
  onClose,
  onReview,
  lang,
}) => {
  const t = translations[lang].pilot

  const [action, setAction] = useState<'VERIFY' | 'REJECT' | 'REQUEST_REMEASUREMENT'>('VERIFY')
  const [evidenceReference, setEvidenceReference] = useState('')
  const [notes, setNotes] = useState('')
  const [rejectionReason, setRejectionReason] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!measurement) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (action === 'REJECT' && !rejectionReason.trim()) {
      setError(t.rejectionReasonRequired)
      return
    }
    setError(null)
    setIsSubmitting(true)
    try {
      await onReview({
        action,
        evidenceReference: evidenceReference.trim() || undefined,
        notes: notes.trim() || undefined,
        rejectionReason: rejectionReason.trim() || undefined,
      })
      onClose()
      setEvidenceReference('')
      setNotes('')
      setRejectionReason('')
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : t.reviewFailed)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <ShieldCheck className="h-5 w-5 text-primary" />
            {t.independentVerificationTitle}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-3.5 py-2">
          {error && (
            <div className="p-2.5 rounded bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-300 text-xs">
              {error}
            </div>
          )}

          {/* Measurement Context Card */}
          <div className="p-3 rounded-lg border border-border/60 bg-muted/30 text-xs space-y-1">
            <div className="flex justify-between">
              <span className="text-muted-foreground">{t.measurementType}:</span>
              <span className="font-semibold text-foreground">{measurement.measurementType}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">{t.recordedValue}:</span>
              <span className="font-bold text-foreground">
                {measurement.measuredValue} {measurement.canonicalUnit}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">{t.collectionMethod}:</span>
              <span className="font-mono text-muted-foreground">{measurement.collectionMethod}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">{t.submittedBy}:</span>
              <span className="font-mono text-muted-foreground">{measurement.submittedBy}</span>
            </div>
          </div>

          <div className="space-y-1">
            <Label className="text-xs font-semibold">{t.action} *</Label>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => setAction('VERIFY')}
                className={`p-2 rounded-lg border text-xs font-medium flex flex-col items-center gap-1 transition-colors ${
                  action === 'VERIFY'
                    ? 'border-emerald-500 bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300 font-bold'
                    : 'border-input hover:bg-muted/50'
                }`}
              >
                <CheckCircle2 className="h-4 w-4 text-emerald-500" />
                {t.verify}
              </button>
              <button
                type="button"
                onClick={() => setAction('REJECT')}
                className={`p-2 rounded-lg border text-xs font-medium flex flex-col items-center gap-1 transition-colors ${
                  action === 'REJECT'
                    ? 'border-rose-500 bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-300 font-bold'
                    : 'border-input hover:bg-muted/50'
                }`}
              >
                <XCircle className="h-4 w-4 text-rose-500" />
                {t.reject}
              </button>
              <button
                type="button"
                onClick={() => setAction('REQUEST_REMEASUREMENT')}
                className={`p-2 rounded-lg border text-xs font-medium flex flex-col items-center gap-1 transition-colors ${
                  action === 'REQUEST_REMEASUREMENT'
                    ? 'border-amber-500 bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300 font-bold'
                    : 'border-input hover:bg-muted/50'
                }`}
              >
                <RefreshCw className="h-4 w-4 text-amber-500" />
                {t.remeasure}
              </button>
            </div>
          </div>

          {action === 'VERIFY' && (
            <div className="space-y-1">
              <Label className="text-xs font-semibold">{t.evidenceAttestationRef}</Label>
              <Input
                placeholder="CERT-PROBE-CALIB-2026-ATTESTED"
                value={evidenceReference}
                onChange={(e) => setEvidenceReference(e.target.value)}
                className="h-8 text-xs font-mono"
              />
            </div>
          )}

          {action === 'REJECT' && (
            <div className="space-y-1">
              <Label className="text-xs font-semibold">{t.rejectionReason} *</Label>
              <textarea
                className="w-full rounded-md border border-input bg-background px-3 py-2 text-xs min-h-[50px]"
                placeholder={t.rejectionReasonPlaceholder}
                value={rejectionReason}
                onChange={(e) => setRejectionReason(e.target.value)}
                required
              />
            </div>
          )}

          <div className="space-y-1">
            <Label className="text-xs font-semibold">{t.verificationNotes}</Label>
            <textarea
              className="w-full rounded-md border border-input bg-background px-3 py-2 text-xs min-h-[50px]"
              placeholder={t.verificationNotesPlaceholder}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
            />
          </div>

          <DialogFooter className="pt-2">
            <Button type="button" variant="outline" size="sm" onClick={onClose} disabled={isSubmitting}>
              {t.cancel}
            </Button>
            <Button type="submit" size="sm" disabled={isSubmitting}>
              {isSubmitting ? t.submitting : t.submitReviewDecision}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
