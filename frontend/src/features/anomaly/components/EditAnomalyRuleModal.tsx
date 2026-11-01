import React, { useState, useEffect } from 'react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Textarea } from '@/shared/ui/textarea'
import { AnomalyRuleConfig, AnomalySeverity, UpdateAnomalyRulePayload } from '@/entities/anomaly'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { useUpdateAnomalyRuleMutation } from '../queries/useAnomalyQueries'

interface EditAnomalyRuleModalProps {
  isOpen: boolean
  onClose: () => void
  rule: AnomalyRuleConfig | null
  lang: SupportedLanguage
}

export const EditAnomalyRuleModal: React.FC<EditAnomalyRuleModalProps> = ({
  isOpen,
  onClose,
  rule,
  lang,
}) => {
  const t = translations[lang].anomalies.rules
  const severitiesT = translations[lang].anomalies.severities

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [severity, setSeverity] = useState<AnomalySeverity>('HIGH')
  const [threshold, setThreshold] = useState<string>('')
  const [secondaryThreshold, setSecondaryThreshold] = useState<string>('')
  const [windowMinutes, setWindowMinutes] = useState<string>('60')
  const [minConsecutive, setMinConsecutive] = useState<string>('1')
  const [error, setError] = useState<string | null>(null)

  const updateMutation = useUpdateAnomalyRuleMutation()

  useEffect(() => {
    if (rule) {
      setName(rule.name)
      setDescription(rule.description || '')
      setSeverity(rule.severity)
      setThreshold(String(rule.threshold))
      setSecondaryThreshold(
        rule.secondaryThreshold !== null && rule.secondaryThreshold !== undefined
          ? String(rule.secondaryThreshold)
          : ''
      )
      setWindowMinutes(String(rule.timeWindowMinutes))
      setMinConsecutive(String(rule.minConsecutiveReadings))
      setError(null)
    }
  }, [rule])

  if (!rule) return null

  const handleSave = async () => {
    setError(null)
    const numThreshold = parseFloat(threshold)
    if (isNaN(numThreshold)) {
      setError('Chegara son boʻlishi kerak')
      return
    }

    const payload: UpdateAnomalyRulePayload = {
      name: name.trim() || rule.name,
      description: description.trim() || undefined,
      severity,
      threshold: numThreshold,
      secondaryThreshold: secondaryThreshold.trim() ? parseFloat(secondaryThreshold) : null,
      timeWindowMinutes: parseInt(windowMinutes, 10) || 60,
      minConsecutiveReadings: parseInt(minConsecutive, 10) || 1,
    }

    try {
      await updateMutation.mutateAsync({ id: rule.id, payload })
      onClose()
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Saqlashda xatolik yuz berdi'
      setError(msg)
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="text-base font-bold">
            {t.editTitle}
          </DialogTitle>
          <DialogDescription className="text-xs text-muted-foreground">
            {rule.ruleCode} • {rule.sensorType}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-3 py-2 text-xs">
          <div>
            <label className="font-semibold block mb-1">{t.name}</label>
            <Input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="text-xs"
            />
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="font-semibold block mb-1">{t.threshold} *</label>
              <Input
                type="number"
                step="any"
                value={threshold}
                onChange={(e) => setThreshold(e.target.value)}
                className="text-xs"
              />
            </div>
            <div>
              <label className="font-semibold block mb-1">{t.secondaryThreshold}</label>
              <Input
                type="number"
                step="any"
                value={secondaryThreshold}
                onChange={(e) => setSecondaryThreshold(e.target.value)}
                placeholder="Ixtiyoriy"
                className="text-xs"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="font-semibold block mb-1">Xavflilik darajasi</label>
              <select
                value={severity}
                onChange={(e) => setSeverity(e.target.value as AnomalySeverity)}
                className="w-full h-9 px-2.5 rounded-md border border-input bg-background text-xs focus:outline-none focus:ring-1 focus:ring-ring"
              >
                <option value="CRITICAL">{severitiesT.CRITICAL}</option>
                <option value="HIGH">{severitiesT.HIGH}</option>
                <option value="MEDIUM">{severitiesT.MEDIUM}</option>
                <option value="LOW">{severitiesT.LOW}</option>
                <option value="INFO">{severitiesT.INFO}</option>
              </select>
            </div>

            <div>
              <label className="font-semibold block mb-1">{t.window}</label>
              <Input
                type="number"
                value={windowMinutes}
                onChange={(e) => setWindowMinutes(e.target.value)}
                className="text-xs"
              />
            </div>
          </div>

          <div>
            <label className="font-semibold block mb-1">{t.readings}</label>
            <Input
              type="number"
              min="1"
              value={minConsecutive}
              onChange={(e) => setMinConsecutive(e.target.value)}
              className="text-xs"
            />
          </div>

          <div>
            <label className="font-semibold block mb-1">Tavsif</label>
            <Textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={2}
              className="text-xs resize-none"
            />
          </div>

          {error && <p className="text-destructive font-medium">{error}</p>}
        </div>

        <DialogFooter>
          <Button variant="outline" size="sm" onClick={onClose} disabled={updateMutation.isPending}>
            {translations[lang].common.cancel}
          </Button>
          <Button size="sm" onClick={handleSave} disabled={updateMutation.isPending}>
            {updateMutation.isPending ? '...' : translations[lang].common.save}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
