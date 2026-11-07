import React, { useState } from 'react'
import { CheckCircle, ExternalLink, ShieldAlert, UserCheck } from 'lucide-react'
import { FieldRiskProfile } from '@/entities/risk'
import { Button } from '@/shared/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/shared/ui/dialog'
import { Textarea } from '@/shared/ui/textarea'
import { Label } from '@/shared/ui/label'

interface RiskActionPanelProps {
  profile: FieldRiskProfile
  onAcknowledge: (notes?: string) => Promise<void>
  onResolve: (notes: string) => Promise<void>
  onNavigateRoute?: (route: string) => void
  isSubmitting?: boolean
}

export const RiskActionPanel: React.FC<RiskActionPanelProps> = ({
  profile,
  onAcknowledge,
  onResolve,
  onNavigateRoute,
  isSubmitting = false,
}) => {
  const [ackModalOpen, setAckModalOpen] = useState(false)
  const [resolveModalOpen, setResolveModalOpen] = useState(false)
  const [ackNotes, setAckNotes] = useState('')
  const [resolveNotes, setResolveNotes] = useState('')
  const [errorMsg, setErrorMsg] = useState('')

  const handleConfirmAcknowledge = async () => {
    try {
      await onAcknowledge(ackNotes)
      setAckModalOpen(false)
      setAckNotes('')
    } catch (e: any) {
      setErrorMsg(e.message || 'Failed to acknowledge risk assessment')
    }
  }

  const handleConfirmResolve = async () => {
    if (!resolveNotes.trim()) {
      setErrorMsg('Resolution notes are mandatory to ensure an explainable operational audit trail.')
      return
    }
    try {
      setErrorMsg('')
      await onResolve(resolveNotes.trim())
      setResolveModalOpen(false)
      setResolveNotes('')
    } catch (e: any) {
      setErrorMsg(e.message || 'Failed to resolve risk assessment')
    }
  }

  return (
    <div className="space-y-4" data-testid="risk-action-panel">
      {/* Human-in-the-loop Operational Guardrail Notice */}
      <div className="p-3 rounded-xl bg-muted/40 border border-border text-xs text-muted-foreground flex items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <ShieldAlert className="w-4 h-4 text-primary shrink-0" />
          <span>
            <strong>Human Control Guarantee:</strong> Risk assessments provide decision support. Operational changes and valve actuations require explicit human review and approval.
          </span>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex flex-wrap items-center justify-between gap-3 pt-1">
        <div className="flex flex-wrap items-center gap-2">
          {profile.lifecycleStatus === 'OPEN' && (
            <Button
              variant="outline"
              size="sm"
              disabled={isSubmitting}
              onClick={() => {
                setErrorMsg('')
                setAckModalOpen(true)
              }}
              className="gap-1.5 text-xs font-semibold"
              data-testid="btn-acknowledge-risk"
            >
              <UserCheck className="w-3.5 h-3.5 text-blue-600" />
              <span>Acknowledge Risk</span>
            </Button>
          )}

          {profile.lifecycleStatus !== 'RESOLVED' && (
            <Button
              variant="default"
              size="sm"
              disabled={isSubmitting}
              onClick={() => {
                setErrorMsg('')
                setResolveModalOpen(true)
              }}
              className="gap-1.5 text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white"
              data-testid="btn-resolve-risk"
            >
              <CheckCircle className="w-3.5 h-3.5" />
              <span>Mark as Resolved</span>
            </Button>
          )}
        </div>

        {/* Deep Link Actions to Other Modules */}
        {onNavigateRoute && (
          <div className="flex items-center gap-2 text-xs">
            <Button
              variant="ghost"
              size="sm"
              className="h-8 px-2 text-xs gap-1 text-muted-foreground hover:text-foreground"
              onClick={() => onNavigateRoute(`/gis?fieldId=${profile.fieldId}`)}
            >
              <span>GIS Map</span>
              <ExternalLink className="w-3 h-3" />
            </Button>

            <Button
              variant="ghost"
              size="sm"
              className="h-8 px-2 text-xs gap-1 text-muted-foreground hover:text-foreground"
              onClick={() => onNavigateRoute(`/operations?fieldId=${profile.fieldId}`)}
            >
              <span>Operations Workflow</span>
              <ExternalLink className="w-3 h-3" />
            </Button>

            <Button
              variant="ghost"
              size="sm"
              className="h-8 px-2 text-xs gap-1 text-muted-foreground hover:text-foreground"
              onClick={() => onNavigateRoute(`/sensors?fieldId=${profile.fieldId}`)}
            >
              <span>Sensor Telemetry</span>
              <ExternalLink className="w-3 h-3" />
            </Button>
          </div>
        )}
      </div>

      {/* Acknowledge Dialog */}
      <Dialog open={ackModalOpen} onOpenChange={setAckModalOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Acknowledge Risk Assessment</DialogTitle>
            <DialogDescription>
              Record that you have reviewed this risk for parcel <strong>{profile.fieldName}</strong>.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-3 py-2">
            <div className="space-y-1.5">
              <Label htmlFor="ackNotes" className="text-xs">
                Optional Acknowledgement Notes
              </Label>
              <Textarea
                id="ackNotes"
                placeholder="e.g. Field operator informed, manual inspection scheduled for this afternoon."
                value={ackNotes}
                onChange={(e) => setAckNotes(e.target.value)}
                className="text-xs h-20"
              />
            </div>
            {errorMsg && <p className="text-xs text-rose-500">{errorMsg}</p>}
          </div>

          <DialogFooter>
            <Button variant="outline" size="sm" onClick={() => setAckModalOpen(false)}>
              Cancel
            </Button>
            <Button size="sm" onClick={handleConfirmAcknowledge} disabled={isSubmitting}>
              Confirm Acknowledgement
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Resolve Dialog */}
      <Dialog open={resolveModalOpen} onOpenChange={setResolveModalOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Resolve Risk Assessment</DialogTitle>
            <DialogDescription>
              Provide audit notes explaining the remedial action taken on parcel{' '}
              <strong>{profile.fieldName}</strong>.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-3 py-2">
            <div className="space-y-1.5">
              <Label htmlFor="resolveNotes" className="text-xs">
                Remedial Action Notes <span className="text-rose-500">*</span>
              </Label>
              <Textarea
                id="resolveNotes"
                placeholder="e.g. Repaired broken poly-pipe coupling; pressure restored to 2.1 bar; drip cycle resumed."
                value={resolveNotes}
                onChange={(e) => {
                  setResolveNotes(e.target.value)
                  if (errorMsg) setErrorMsg('')
                }}
                className="text-xs h-24"
                required
              />
            </div>
            {errorMsg && <p className="text-xs text-rose-500">{errorMsg}</p>}
          </div>

          <DialogFooter>
            <Button variant="outline" size="sm" onClick={() => setResolveModalOpen(false)}>
              Cancel
            </Button>
            <Button
              size="sm"
              onClick={handleConfirmResolve}
              disabled={isSubmitting || !resolveNotes.trim()}
              className="bg-emerald-600 hover:bg-emerald-700 text-white"
            >
              Confirm Resolution
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
