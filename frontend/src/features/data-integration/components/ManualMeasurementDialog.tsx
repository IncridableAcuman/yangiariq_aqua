import React, { useState } from 'react'
import { RecordManualMeasurementRequest } from '@/entities/integration'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Label } from '@/shared/ui/label'
import { Textarea } from '@/shared/ui/textarea'
import { UserCheck, CheckCircle2 } from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'

interface ManualMeasurementDialogProps {
  isOpen: boolean
  onClose: () => void
  onSubmit: (data: RecordManualMeasurementRequest) => Promise<void>
  isSubmitting?: boolean
  canVerify: boolean
  lang: SupportedLanguage
}

export const ManualMeasurementDialog: React.FC<ManualMeasurementDialogProps> = ({
  isOpen,
  onClose,
  onSubmit,
  isSubmitting,
  canVerify,
  lang,
}) => {
  const t = translations[lang].dataIntegration

  const [fieldId, setFieldId] = useState('')
  const [sensorType, setSensorType] = useState('SOIL_MOISTURE')
  const [value, setValue] = useState('')
  const [unit, setUnit] = useState('%')
  const [verified, setVerified] = useState(canVerify)
  const [notes, setNotes] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  const handleTypeChange = (type: string) => {
    setSensorType(type)
    switch (type) {
      case 'SOIL_MOISTURE':
      case 'HUMIDITY':
        setUnit('%')
        break
      case 'WATER_FLOW':
        setUnit('m³/h')
        break
      case 'TEMPERATURE':
        setUnit('°C')
        break
      case 'PRESSURE':
        setUnit('bar')
        break
      case 'WATER_LEVEL':
        setUnit('m')
        break
      case 'RAINFALL':
        setUnit('mm')
        break
      case 'SALINITY':
        setUnit('dS/m')
        break
      default:
        setUnit('')
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!fieldId.trim()) {
      setError(t.errorFieldIdRequired)
      return
    }
    const num = parseFloat(value)
    if (isNaN(num)) {
      setError(t.errorValidNumberRequired)
      return
    }

    try {
      await onSubmit({
        fieldId: fieldId.trim(),
        sensorType,
        value: num,
        unit: unit.trim() || undefined,
        measuredAt: new Date().toISOString(),
        verified: canVerify && verified,
        notes: notes.trim() || undefined,
      })

      setSuccess(true)
      setTimeout(() => {
        setSuccess(false)
        setFieldId('')
        setValue('')
        setNotes('')
        onClose()
      }, 1200)
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Submission failed')
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="text-lg font-bold flex items-center gap-2">
            <UserCheck className="h-5 w-5 text-primary" />
            {t.manualEntryTitle}
          </DialogTitle>
        </DialogHeader>

        {success ? (
          <div className="py-8 flex flex-col items-center justify-center space-y-2 text-emerald-600">
            <CheckCircle2 className="h-10 w-10 animate-bounce" />
            <span className="font-semibold text-sm">{t.measurementRecorded}</span>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-3 py-1">
            {error && (
              <div className="p-2.5 rounded bg-rose-50 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300 text-xs border border-rose-200">
                {error}
              </div>
            )}

            <div>
              <Label htmlFor="man-field" className="text-xs">{t.fieldParcelId} *</Label>
              <Input
                id="man-field"
                placeholder="00000000-0000-0000-0000-000000000000"
                value={fieldId}
                onChange={(e) => setFieldId(e.target.value)}
                className="mt-1 font-mono text-xs"
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="man-type" className="text-xs">{t.measurementType} *</Label>
                <select
                  id="man-type"
                  value={sensorType}
                  onChange={(e) => handleTypeChange(e.target.value)}
                  className="mt-1 flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-xs shadow-xs transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                >
                  <option value="SOIL_MOISTURE">SOIL_MOISTURE</option>
                  <option value="WATER_FLOW">WATER_FLOW</option>
                  <option value="TEMPERATURE">TEMPERATURE</option>
                  <option value="HUMIDITY">HUMIDITY</option>
                  <option value="PRESSURE">PRESSURE</option>
                  <option value="WATER_LEVEL">WATER_LEVEL</option>
                  <option value="RAINFALL">RAINFALL</option>
                  <option value="SALINITY">SALINITY</option>
                </select>
              </div>

              <div>
                <Label htmlFor="man-val" className="text-xs">{t.value} ({unit}) *</Label>
                <Input
                  id="man-val"
                  type="number"
                  step="any"
                  placeholder="45.5"
                  value={value}
                  onChange={(e) => setValue(e.target.value)}
                  className="mt-1 text-sm"
                  required
                />
              </div>
            </div>

            {canVerify && (
              <div className="flex items-center gap-2 p-2 rounded bg-muted/40 border text-xs">
                <input
                  type="checkbox"
                  id="man-verify"
                  checked={verified}
                  onChange={(e) => setVerified(e.target.checked)}
                  className="rounded border-border"
                />
                <Label htmlFor="man-verify" className="cursor-pointer text-xs font-medium">
                  {t.markAsOperatorVerified}
                </Label>
              </div>
            )}

            <div>
              <Label htmlFor="man-notes" className="text-xs">{t.notesRemarks}</Label>
              <Textarea
                id="man-notes"
                placeholder="Field observations, meter serial, or physical verification details"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                rows={2}
                className="mt-1 text-xs"
              />
            </div>

            <DialogFooter className="pt-2">
              <Button type="button" variant="outline" size="sm" onClick={onClose}>
                {t.cancel}
              </Button>
              <Button type="submit" size="sm" disabled={isSubmitting}>
                {isSubmitting ? t.recording : t.submitMeasurement}
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  )
}
