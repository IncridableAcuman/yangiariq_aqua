import React, { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Textarea } from '@/shared/ui/textarea'
import { Anomaly } from '@/entities/anomaly'
import { SupportedLanguage, translations } from '@/shared/i18n'
import {
  useAcknowledgeAnomalyMutation,
  useDismissAnomalyMutation,
  useInvestigateAnomalyMutation,
  useResolveAnomalyMutation,
} from '../queries/useAnomalyQueries'

export type AnomalyActionType = 'acknowledge' | 'investigate' | 'resolve' | 'dismiss'

interface AnomalyActionDialogProps {
  isOpen: boolean
  onClose: () => void
  anomaly: Anomaly | null
  actionType: AnomalyActionType | null
  lang: SupportedLanguage
}

export const AnomalyActionDialog: React.FC<AnomalyActionDialogProps> = ({
  isOpen,
  onClose,
  anomaly,
  actionType,
  lang,
}) => {
  const t = translations[lang].anomalies
  const [notes, setNotes] = useState('')
  const [error, setError] = useState<string | null>(null)

  const ackMutation = useAcknowledgeAnomalyMutation()
  const invMutation = useInvestigateAnomalyMutation()
  const resMutation = useResolveAnomalyMutation()
  const disMutation = useDismissAnomalyMutation()

  const isPending =
    ackMutation.isPending ||
    invMutation.isPending ||
    resMutation.isPending ||
    disMutation.isPending

  if (!anomaly || !actionType) return null

  const getActionTitle = () => {
    switch (actionType) {
      case 'acknowledge':
        return t.actions.acknowledge
      case 'investigate':
        return t.actions.investigate
      case 'resolve':
        return t.actions.resolve
      case 'dismiss':
        return t.actions.dismiss
    }
  }

  const handleConfirm = async () => {
    setError(null)
    const trimmedNotes = notes.trim()

    if ((actionType === 'resolve' || actionType === 'dismiss') && !trimmedNotes) {
      setError(t.actions.notesRequired)
      return
    }

    try {
      if (actionType === 'acknowledge') {
        await ackMutation.mutateAsync({ id: anomaly.id, notes: trimmedNotes || undefined })
      } else if (actionType === 'investigate') {
        await invMutation.mutateAsync({ id: anomaly.id, notes: trimmedNotes || undefined })
      } else if (actionType === 'resolve') {
        await resMutation.mutateAsync({ id: anomaly.id, notes: trimmedNotes })
      } else if (actionType === 'dismiss') {
        await disMutation.mutateAsync({ id: anomaly.id, notes: trimmedNotes })
      }
      setNotes('')
      onClose()
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Operation failed'
      setError(message)
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <span>{getActionTitle()}</span>
          </DialogTitle>
          <DialogDescription className="text-xs text-muted-foreground">
            {t.types[anomaly.anomalyType]} • {anomaly.description}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-3 py-2">
          <label className="text-xs font-semibold text-foreground">
            {t.actions.notes}
            {(actionType === 'resolve' || actionType === 'dismiss') && (
              <span className="text-destructive ml-1">*</span>
            )}
          </label>
          <Textarea
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder={t.actions.notesPlaceholder}
            rows={3}
            className="text-xs resize-none"
          />
          {error && <p className="text-xs text-destructive font-medium">{error}</p>}
        </div>

        <DialogFooter className="gap-2 sm:gap-0">
          <Button variant="outline" size="sm" onClick={onClose} disabled={isPending}>
            {t.actions.cancel}
          </Button>
          <Button
            size="sm"
            onClick={handleConfirm}
            disabled={isPending}
            className={
              actionType === 'dismiss'
                ? 'bg-destructive hover:bg-destructive/90 text-white'
                : 'bg-primary hover:bg-primary/90 text-primary-foreground'
            }
          >
            {isPending ? '...' : t.actions.confirm}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
