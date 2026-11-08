import React, { useState } from 'react'
import { WaterIncident, CorrectiveAction, VerificationOutcome, VerifyResolutionPayload } from '@/entities/accountability'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Textarea } from '@/shared/ui/textarea'
import { AlertTriangle, ShieldCheck, X } from 'lucide-react'

interface Props {
  isOpen: boolean
  onClose: () => void
  incident?: WaterIncident
  actions?: CorrectiveAction[]
  onSubmit: (payload: VerifyResolutionPayload) => Promise<void>
  isSubmitting?: boolean
}

export const VerifyResolutionDialog: React.FC<Props> = ({
  isOpen,
  onClose,
  incident,
  actions = [],
  onSubmit,
  isSubmitting = false,
}) => {
  const [selectedActionId, setSelectedActionId] = useState<string>(actions[0]?.id || '')
  const [method, setMethod] = useState<string>('ONSITE_PHYSICAL_INSPECTION')
  const [result, setResult] = useState<VerificationOutcome>('VERIFIED_RESOLVED')
  const [explanatoryNotes, setExplanatoryNotes] = useState('')
  const [evidenceUri, setEvidenceUri] = useState('')
  const [administrativeOverride, setAdministrativeOverride] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!isOpen || !incident) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!explanatoryNotes.trim()) {
      setError('Mustaqil tekshiruv xulosasi va daliliy asoslar kiritilishi shart')
      return
    }
    setError(null)
    await onSubmit({
      actionId: selectedActionId || undefined,
      verificationMethod: method,
      result,
      explanatoryNotes: explanatoryNotes.trim(),
      supportingEvidenceUri: evidenceUri.trim() || undefined,
      administrativeOverride,
    })
    onClose()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm animate-in fade-in">
      <div className="bg-card border border-border rounded-xl shadow-xl w-full max-w-lg overflow-hidden">
        <div className="p-4 sm:p-5 border-b border-border flex items-center justify-between">
          <div className="flex items-center gap-2">
            <ShieldCheck className="w-5 h-5 text-emerald-500" />
            <div>
              <h3 className="text-sm font-bold text-foreground">Mustaqil tekshiruv xulosasi (Verification)</h3>
              <p className="text-xs text-muted-foreground">{incident.incidentNumber} — {incident.title}</p>
            </div>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose} className="h-7 w-7 p-0">
            <X className="w-4 h-4" />
          </Button>
        </div>

        <form onSubmit={handleSubmit} className="p-4 sm:p-5 space-y-4">
          {/* Separation of duties alert */}
          <div className="bg-amber-500/10 border border-amber-500/20 p-3 rounded-lg flex items-start gap-2 text-xs text-amber-800 dark:text-amber-300">
            <AlertTriangle className="w-4 h-4 text-amber-500 shrink-0 mt-0.5" />
            <p>
              <strong>Vazifalar taqsimoti talabi:</strong> Siz ushbu tekshiruvni mustaqil inspektor sifatida amalga oshirmoqdasiz. Chora ijrochisi oʻz ishini tasdiqlashi mumkin emas.
            </p>
          </div>

          {actions.length > 0 && (
            <div className="space-y-1">
              <label className="text-xs font-semibold text-foreground">Baholanayotgan tuzatish chorasi:</label>
              <select
                value={selectedActionId}
                onChange={(e) => setSelectedActionId(e.target.value)}
                className="w-full h-9 px-2.5 rounded-md border border-input bg-background text-xs"
              >
                {actions.map((act) => (
                  <option key={act.id} value={act.id}>
                    {act.actionNumber}: {act.title} ({act.status})
                  </option>
                ))}
              </select>
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <label className="text-xs font-semibold text-foreground">Tekshiruv usuli:</label>
              <select
                value={method}
                onChange={(e) => setMethod(e.target.value)}
                className="w-full h-9 px-2.5 rounded-md border border-input bg-background text-xs"
              >
                <option value="ONSITE_PHYSICAL_INSPECTION">Joyida fizik koʻrik</option>
                <option value="RE_MEASUREMENT_FLOW">Qayta oqim oʻlchovi (ADCP/flume)</option>
                <option value="AUDIT_LOG_COMPARISON">Audit jurnali solishtiruvi</option>
                <option value="SENSOR_RECALIBRATION_TEST">Datchik qayta kalibrlash testi</option>
                <option value="SATELLITE_VERIFICATION">Kosmik/Dron monitoringi</option>
              </select>
            </div>

            <div className="space-y-1">
              <label className="text-xs font-semibold text-foreground">Tekshiruv natijasi:</label>
              <select
                value={result}
                onChange={(e) => setResult(e.target.value as VerificationOutcome)}
                className="w-full h-9 px-2.5 rounded-md border border-input bg-background text-xs font-bold"
              >
                <option value="VERIFIED_RESOLVED">Toʻliq hal etilgan (RESOLVED)</option>
                <option value="PARTIALLY_RESOLVED">Qisman hal etilgan</option>
                <option value="NOT_RESOLVED">Hal etilmagan (NOT RESOLVED)</option>
                <option value="INSUFFICIENT_EVIDENCE">Dalil yetarli emas</option>
              </select>
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-foreground">
              Inspektor xulosasi va asoslantirish *:
            </label>
            <Textarea
              value={explanatoryNotes}
              onChange={(e) => setExplanatoryNotes(e.target.value)}
              placeholder="Joyida tekshiruv oʻtkazildi, oqish toʻxtatilgani va hisoblagich aniq ishlayotgani tasdiqlandi..."
              rows={3}
              className="text-xs"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-foreground">Tekshiruv hujjati havolasi (Evidence URI):</label>
            <Input
              value={evidenceUri}
              onChange={(e) => setEvidenceUri(e.target.value)}
              placeholder="/evidence/inspection-report-2026.pdf"
              className="text-xs"
            />
          </div>

          <div className="flex items-center gap-2 pt-1">
            <input
              type="checkbox"
              id="adminOverride"
              checked={administrativeOverride}
              onChange={(e) => setAdministrativeOverride(e.target.checked)}
              className="rounded border-input text-primary focus:ring-primary"
            />
            <label htmlFor="adminOverride" className="text-[11px] text-muted-foreground cursor-pointer">
              Maʼmuriy istisno (Documented administrative exception)
            </label>
          </div>

          {error && <p className="text-[11px] text-destructive">{error}</p>}

          <div className="flex items-center justify-end gap-2 pt-2 border-t border-border">
            <Button type="button" variant="outline" size="sm" onClick={onClose} disabled={isSubmitting}>
              Bekor qilish
            </Button>
            <Button
              type="submit"
              size="sm"
              disabled={isSubmitting}
              className="bg-emerald-600 hover:bg-emerald-700 text-white"
            >
              {isSubmitting ? 'Tasdiqlanmoqda...' : 'Xulosani rasmiylashtirish'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
