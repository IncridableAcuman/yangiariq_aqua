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
import { PlusCircle } from 'lucide-react'
import { translations, SupportedLang } from '@/shared/i18n'

interface SubmitGroundTruthDialogProps {
  isOpen: boolean
  onClose: () => void
  onSubmit: (payload: {
    fieldId: string
    measurementType: string
    measuredValue: number
    canonicalUnit: string
    measuredAt?: string
    collectionMethod: string
    measurementSource: string
    evidenceReference?: string
    notes?: string
  }) => Promise<void>
  lang: SupportedLang
}

export const SubmitGroundTruthDialog: React.FC<SubmitGroundTruthDialogProps> = ({
  isOpen,
  onClose,
  onSubmit,
  lang,
}) => {
  const t = translations[lang].pilot

  const [fieldId, setFieldId] = useState('')
  const [measurementType, setMeasurementType] = useState('SOIL_MOISTURE')
  const [measuredValue, setMeasuredValue] = useState('')
  const [canonicalUnit, setCanonicalUnit] = useState('%')
  const [collectionMethod, setCollectionMethod] = useState('PORTABLE_TDR_PROBE')
  const [measurementSource, setMeasurementSource] = useState('Field Inspection Team Alpha')
  const [evidenceReference, setEvidenceReference] = useState('')
  const [notes, setNotes] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleTypeChange = (type: string) => {
    setMeasurementType(type)
    switch (type) {
      case 'SOIL_MOISTURE':
        setCanonicalUnit('%')
        setCollectionMethod('PORTABLE_TDR_PROBE')
        break
      case 'WATER_FLOW':
        setCanonicalUnit('m³/h')
        setCollectionMethod('ULTRASONIC_FLOW_METER')
        break
      case 'TEMPERATURE':
        setCanonicalUnit('°C')
        setCollectionMethod('DIGITAL_THERMOMETER')
        break
      case 'PRESSURE':
        setCanonicalUnit('bar')
        setCollectionMethod('CALIBRATED_MANOMETER')
        break
      case 'WATER_LEVEL':
        setCanonicalUnit('m')
        setCollectionMethod('ULTRASONIC_LEVEL_GAUGE')
        break
      default:
        setCanonicalUnit('%')
        break
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!fieldId.trim() || !measuredValue.trim()) {
      setError(t.fillRequiredFields)
      return
    }
    const val = parseFloat(measuredValue)
    if (isNaN(val)) {
      setError(t.numericValueError)
      return
    }

    setError(null)
    setIsSubmitting(true)
    try {
      await onSubmit({
        fieldId: fieldId.trim(),
        measurementType,
        measuredValue: val,
        canonicalUnit,
        collectionMethod: collectionMethod.trim(),
        measurementSource: measurementSource.trim(),
        evidenceReference: evidenceReference.trim() || undefined,
        notes: notes.trim() || undefined,
      })
      onClose()
      setFieldId('')
      setMeasuredValue('')
      setEvidenceReference('')
      setNotes('')
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : t.submitFailed)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <PlusCircle className="h-5 w-5 text-primary" />
            {t.submitGroundTruthTitle}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-3.5 py-2">
          {error && (
            <div className="p-2.5 rounded bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-300 text-xs">
              {error}
            </div>
          )}

          <div className="space-y-1">
            <Label className="text-xs font-semibold">{t.fieldId} *</Label>
            <Input
              placeholder="00000000-0000-0000-0000-000000000000"
              value={fieldId}
              onChange={(e) => setFieldId(e.target.value)}
              className="h-8 text-xs font-mono"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label className="text-xs font-semibold">{t.measurementType}</Label>
              <select
                className="w-full h-8 rounded-md border border-input bg-background px-2 text-xs"
                value={measurementType}
                onChange={(e) => handleTypeChange(e.target.value)}
              >
                <option value="SOIL_MOISTURE">{t.soilMoisture}</option>
                <option value="WATER_FLOW">{t.waterFlowRate}</option>
                <option value="TEMPERATURE">{t.temperature}</option>
                <option value="PRESSURE">{t.waterPressure}</option>
                <option value="WATER_LEVEL">{t.waterLevel}</option>
              </select>
            </div>
            <div className="space-y-1">
              <Label className="text-xs font-semibold">{t.measuredValue} *</Label>
              <div className="flex gap-1.5">
                <Input
                  type="number"
                  step="any"
                  placeholder="48.5"
                  value={measuredValue}
                  onChange={(e) => setMeasuredValue(e.target.value)}
                  className="h-8 text-xs font-mono"
                  required
                />
                <span className="inline-flex items-center px-2 text-xs text-muted-foreground bg-muted rounded border border-border">
                  {canonicalUnit}
                </span>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label className="text-xs font-semibold">{t.collectionMethod}</Label>
              <Input
                value={collectionMethod}
                onChange={(e) => setCollectionMethod(e.target.value)}
                className="h-8 text-xs"
                required
              />
            </div>
            <div className="space-y-1">
              <Label className="text-xs font-semibold">{t.measurementSource}</Label>
              <Input
                value={measurementSource}
                onChange={(e) => setMeasurementSource(e.target.value)}
                className="h-8 text-xs"
                required
              />
            </div>
          </div>

          <div className="space-y-1">
            <Label className="text-xs font-semibold">{t.evidenceReference}</Label>
            <Input
              placeholder="CERT-TDR-CALIB-2026-99"
              value={evidenceReference}
              onChange={(e) => setEvidenceReference(e.target.value)}
              className="h-8 text-xs font-mono"
            />
          </div>

          <div className="space-y-1">
            <Label className="text-xs font-semibold">{t.notes}</Label>
            <textarea
              className="w-full rounded-md border border-input bg-background px-3 py-2 text-xs min-h-[50px]"
              placeholder={t.groundTruthNotesPlaceholder}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
            />
          </div>

          <DialogFooter className="pt-2">
            <Button type="button" variant="outline" size="sm" onClick={onClose} disabled={isSubmitting}>
              {t.cancel}
            </Button>
            <Button type="submit" size="sm" disabled={isSubmitting}>
              {isSubmitting ? t.submitting : t.submitForVerification}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
