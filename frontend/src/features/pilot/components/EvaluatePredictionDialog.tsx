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
import { Calculator } from 'lucide-react'
import { translations, SupportedLang } from '@/shared/i18n'

interface EvaluatePredictionDialogProps {
  isOpen: boolean
  onClose: () => void
  pilotId: string
  onSubmit: (payload: {
    fieldId: string
    predictionId?: string
    modelName: string
    modelVersion: string
    predictedAt: string
    predictionHorizonHours: number
    predictedValue: number
    actualValue: number
    groundTruthId?: string
  }) => Promise<void>
  lang: SupportedLang
}

export const EvaluatePredictionDialog: React.FC<EvaluatePredictionDialogProps> = ({
  isOpen,
  onClose,
  onSubmit,
  lang,
}) => {
  const t = translations[lang].pilot

  const [fieldId, setFieldId] = useState('')
  const [modelName, setModelName] = useState('SOIL_MOISTURE_HYDRAULIC_SURROGATE')
  const [modelVersion, setModelVersion] = useState('v2.4.1')
  const [predictedAt, setPredictedAt] = useState(new Date().toISOString())
  const [horizonHours, setHorizonHours] = useState('24')
  const [predictedValue, setPredictedValue] = useState('')
  const [actualValue, setActualValue] = useState('')
  const [groundTruthId, setGroundTruthId] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!fieldId.trim() || !predictedValue || !actualValue) {
      setError(t.fillRequiredFields)
      return
    }

    const pVal = parseFloat(predictedValue)
    const aVal = parseFloat(actualValue)
    const horizon = parseInt(horizonHours, 10)

    if (isNaN(pVal) || isNaN(aVal) || isNaN(horizon)) {
      setError(t.numericValueError)
      return
    }

    setError(null)
    setIsSubmitting(true)
    try {
      await onSubmit({
        fieldId: fieldId.trim(),
        modelName: modelName.trim(),
        modelVersion: modelVersion.trim(),
        predictedAt: predictedAt.trim(),
        predictionHorizonHours: horizon,
        predictedValue: pVal,
        actualValue: aVal,
        groundTruthId: groundTruthId.trim() || undefined,
      })
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Evaluation failed')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-xl">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <Calculator className="h-5 w-5 text-primary" />
            <DialogTitle className="text-lg font-bold">
              {t.evaluatePredictionTitle}
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
              <Label className="text-xs font-semibold">{t.fieldId} *</Label>
              <Input
                value={fieldId}
                onChange={(e) => setFieldId(e.target.value)}
                placeholder="Field UUID"
                className="h-8 text-xs mt-1"
                required
              />
            </div>

            <div>
              <Label className="text-xs font-semibold">{t.predictionHorizon} *</Label>
              <Input
                type="number"
                value={horizonHours}
                onChange={(e) => setHorizonHours(e.target.value)}
                className="h-8 text-xs mt-1"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <Label className="text-xs font-semibold">{t.modelName} *</Label>
              <Input
                value={modelName}
                onChange={(e) => setModelName(e.target.value)}
                className="h-8 text-xs mt-1"
                required
              />
            </div>

            <div>
              <Label className="text-xs font-semibold">{t.modelVersion} *</Label>
              <Input
                value={modelVersion}
                onChange={(e) => setModelVersion(e.target.value)}
                className="h-8 text-xs mt-1"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <Label className="text-xs font-semibold">{t.predictedValue} *</Label>
              <Input
                type="number"
                step="any"
                value={predictedValue}
                onChange={(e) => setPredictedValue(e.target.value)}
                placeholder="e.g. 24.5"
                className="h-8 text-xs mt-1"
                required
              />
            </div>

            <div>
              <Label className="text-xs font-semibold">{t.actualValue} *</Label>
              <Input
                type="number"
                step="any"
                value={actualValue}
                onChange={(e) => setActualValue(e.target.value)}
                placeholder="e.g. 23.1"
                className="h-8 text-xs mt-1"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <Label className="text-xs font-semibold">{t.predictedAt}</Label>
              <Input
                type="datetime-local"
                value={predictedAt.slice(0, 16)}
                onChange={(e) => setPredictedAt(new Date(e.target.value).toISOString())}
                className="h-8 text-xs mt-1"
              />
            </div>

            <div>
              <Label className="text-xs font-semibold">{t.groundTruthId}</Label>
              <Input
                value={groundTruthId}
                onChange={(e) => setGroundTruthId(e.target.value)}
                placeholder="Ground Truth Measurement UUID"
                className="h-8 text-xs mt-1"
              />
            </div>
          </div>

          <DialogFooter className="pt-2">
            <Button type="button" variant="outline" size="sm" onClick={onClose}>
              {t.cancel}
            </Button>
            <Button type="submit" size="sm" disabled={isSubmitting}>
              {isSubmitting ? t.saving : t.evaluatePrediction}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
