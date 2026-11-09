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
import { MessageSquarePlus } from 'lucide-react'
import { translations, SupportedLang } from '@/shared/i18n'
import { OperatorFeedbackCategory } from '@/entities/pilot'

interface SubmitFeedbackDialogProps {
  isOpen: boolean
  onClose: () => void
  pilotId: string
  onSubmit: (payload: {
    farmId?: string
    fieldId?: string
    sensorId?: string
    dataSourceId?: string
    incidentId?: string
    category: OperatorFeedbackCategory
    title: string
    description: string
  }) => Promise<void>
  lang: SupportedLang
}

export const SubmitFeedbackDialog: React.FC<SubmitFeedbackDialogProps> = ({
  isOpen,
  onClose,
  onSubmit,
  lang,
}) => {
  const t = translations[lang].pilot

  const [category, setCategory] = useState<OperatorFeedbackCategory>('DATA_QUALITY')
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [farmId, setFarmId] = useState('')
  const [fieldId, setFieldId] = useState('')
  const [sensorId, setSensorId] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!title.trim() || !description.trim()) {
      setError(t.fillRequiredFields)
      return
    }

    setError(null)
    setIsSubmitting(true)
    try {
      await onSubmit({
        category,
        title: title.trim(),
        description: description.trim(),
        farmId: farmId.trim() || undefined,
        fieldId: fieldId.trim() || undefined,
        sensorId: sensorId.trim() || undefined,
      })
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Submission failed')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <MessageSquarePlus className="h-5 w-5 text-primary" />
            <DialogTitle className="text-lg font-bold">
              {t.submitFeedback}
            </DialogTitle>
          </div>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 py-2">
          {error && (
            <div className="p-3 text-xs bg-destructive/10 text-destructive border border-destructive/20 rounded-md">
              {error}
            </div>
          )}

          <div>
            <Label className="text-xs font-semibold">{t.feedbackCategory} *</Label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value as OperatorFeedbackCategory)}
              className="w-full h-8 text-xs rounded-md border border-input bg-background px-3 mt-1"
            >
              <option value="DATA_QUALITY">DATA_QUALITY (Data discrepancy / telemetry error)</option>
              <option value="SENSOR_ISSUE">SENSOR_ISSUE (Hardware / probe calibration)</option>
              <option value="WEATHER_DISCREPANCY">WEATHER_DISCREPANCY (Forecast vs observed microclimate)</option>
              <option value="IRRIGATION_OPERATION">IRRIGATION_OPERATION (Water scheduling execution)</option>
              <option value="WATER_DELIVERY">WATER_DELIVERY (Canal dispatch / intake deficit)</option>
              <option value="FIELD_CONDITION">FIELD_CONDITION (Soil salinity / crop stress)</option>
              <option value="SYSTEM_USABILITY">SYSTEM_USABILITY (UI / operator workflow)</option>
              <option value="OTHER">OTHER (General operational observation)</option>
            </select>
          </div>

          <div>
            <Label className="text-xs font-semibold">{t.feedbackTitle} *</Label>
            <Input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Concise summary of the discrepancy or field observation"
              className="h-8 text-xs mt-1"
              required
            />
          </div>

          <div>
            <Label className="text-xs font-semibold">{t.description} *</Label>
            <textarea
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Detailed description of the issue, observed facts, physical evidence..."
              className="w-full text-xs rounded-md border border-input bg-background p-2.5 mt-1 focus:outline-none focus:ring-1 focus:ring-ring"
              required
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <Label className="text-xs text-muted-foreground">{t.farmIdOptional}</Label>
              <Input
                value={farmId}
                onChange={(e) => setFarmId(e.target.value)}
                placeholder="Farm UUID"
                className="h-8 text-xs mt-1"
              />
            </div>
            <div>
              <Label className="text-xs text-muted-foreground">{t.fieldIdOptional}</Label>
              <Input
                value={fieldId}
                onChange={(e) => setFieldId(e.target.value)}
                placeholder="Field UUID"
                className="h-8 text-xs mt-1"
              />
            </div>
            <div>
              <Label className="text-xs text-muted-foreground">{t.sensorIdOptional}</Label>
              <Input
                value={sensorId}
                onChange={(e) => setSensorId(e.target.value)}
                placeholder="Sensor UUID"
                className="h-8 text-xs mt-1"
              />
            </div>
          </div>

          <DialogFooter className="pt-2">
            <Button type="button" variant="outline" size="sm" onClick={onClose}>
              {t.cancel}
            </Button>
            <Button type="submit" size="sm" disabled={isSubmitting}>
              {isSubmitting ? t.saving : t.save}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
