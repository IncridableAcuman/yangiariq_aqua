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
  Badge,
} from '@/shared/ui'
import {
  Users,
  MapPin,
  Calendar,
  Building2,
  PlusCircle,
  Layers,
  Target,
} from 'lucide-react'
import { translations, SupportedLang } from '@/shared/i18n'
import { PilotProgram } from '@/entities/pilot'
import {
  usePilotParticipantsQuery,
  usePilotFieldsQuery,
  useAddParticipantMutation,
  useAddFieldMutation,
} from '../queries/usePilotQueries'

interface PilotDetailsModalProps {
  isOpen: boolean
  onClose: () => void
  pilot: PilotProgram | null
  lang: SupportedLang
}

export const PilotDetailsModal: React.FC<PilotDetailsModalProps> = ({
  isOpen,
  onClose,
  pilot,
  lang,
}) => {
  const t = translations[lang].pilot

  const [activeTab, setActiveTab] = useState<'info' | 'participants' | 'fields'>('info')

  // Participant form state
  const [showAddParticipant, setShowAddParticipant] = useState(false)
  const [participantUserId, setParticipantUserId] = useState('')
  const [participantRole, setParticipantRole] = useState('OPERATOR')
  const [participantAssignedFields, setParticipantAssignedFields] = useState('')

  // Field form state
  const [showAddField, setShowAddField] = useState(false)
  const [fieldId, setFieldId] = useState('')
  const [monitoringTarget, setMonitoringTarget] = useState('SOIL_MOISTURE_REGIME')
  const [measurementPlan, setMeasurementPlan] = useState('Daily in-situ tensiometer cross-checked with gravimetric core')
  const [validationCriteria, setValidationCriteria] = useState('Absolute error <= 3% volumetric water content')

  const { data: participants = [], isLoading: isLoadingParticipants } = usePilotParticipantsQuery(
    pilot?.id ?? null
  )
  const { data: fields = [], isLoading: isLoadingFields } = usePilotFieldsQuery(pilot?.id ?? null)

  const addParticipantMutation = useAddParticipantMutation()
  const addFieldMutation = useAddFieldMutation()

  if (!pilot) return null

  const handleAddParticipant = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!participantUserId.trim()) return
    await addParticipantMutation.mutateAsync({
      pilotId: pilot.id,
      payload: {
        userId: participantUserId.trim(),
        roleInPilot: participantRole.trim(),
        assignedFieldIds: participantAssignedFields.trim() || undefined,
      },
    })
    setParticipantUserId('')
    setShowAddParticipant(false)
  }

  const handleAddField = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!fieldId.trim()) return
    await addFieldMutation.mutateAsync({
      pilotId: pilot.id,
      payload: {
        fieldId: fieldId.trim(),
        monitoringTarget: monitoringTarget.trim(),
        measurementPlan: measurementPlan.trim(),
        validationCriteria: validationCriteria.trim(),
      },
    })
    setFieldId('')
    setShowAddField(false)
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-4xl max-h-[85vh] flex flex-col p-6 overflow-hidden">
        <DialogHeader className="border-b pb-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="font-mono text-sm font-bold bg-muted px-2.5 py-1 rounded">
                {pilot.code}
              </span>
              <DialogTitle className="text-xl font-bold">{pilot.name}</DialogTitle>
              <Badge variant="outline">{pilot.status}</Badge>
            </div>
          </div>
          {pilot.description && (
            <p className="text-sm text-muted-foreground mt-2">{pilot.description}</p>
          )}

          {/* Sub Navigation */}
          <div className="flex gap-2 mt-4 pt-2 border-t">
            <Button
              variant={activeTab === 'info' ? 'default' : 'ghost'}
              size="sm"
              onClick={() => setActiveTab('info')}
              className="text-xs"
            >
              <Layers className="h-3.5 w-3.5 mr-1.5" />
              {t.viewDetails}
            </Button>
            <Button
              variant={activeTab === 'participants' ? 'default' : 'ghost'}
              size="sm"
              onClick={() => setActiveTab('participants')}
              className="text-xs"
            >
              <Users className="h-3.5 w-3.5 mr-1.5" />
              {t.participants} ({participants.length})
            </Button>
            <Button
              variant={activeTab === 'fields' ? 'default' : 'ghost'}
              size="sm"
              onClick={() => setActiveTab('fields')}
              className="text-xs"
            >
              <Target className="h-3.5 w-3.5 mr-1.5" />
              {t.enrolledFields} ({fields.length})
            </Button>
          </div>
        </DialogHeader>

        <div className="flex-1 overflow-y-auto py-4 space-y-4">
          {activeTab === 'info' && (
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="border rounded-lg p-4 bg-muted/20 space-y-2 text-xs">
                  <div className="flex items-center gap-2 text-muted-foreground">
                    <MapPin className="h-4 w-4" />
                    <span>{t.geographicScope}</span>
                  </div>
                  <p className="font-semibold text-sm">{pilot.geographicScope}</p>

                  <div className="flex items-center gap-2 text-muted-foreground pt-2">
                    <Building2 className="h-4 w-4" />
                    <span>{t.organization}</span>
                  </div>
                  <p className="font-semibold text-sm">{pilot.organization}</p>
                </div>

                <div className="border rounded-lg p-4 bg-muted/20 space-y-2 text-xs">
                  <div className="flex items-center gap-2 text-muted-foreground">
                    <Calendar className="h-4 w-4" />
                    <span>{t.duration}</span>
                  </div>
                  <p className="font-semibold text-sm">
                    {pilot.startDate} → {pilot.endDate}
                  </p>

                  <div className="flex items-center gap-2 text-muted-foreground pt-2">
                    <Layers className="h-4 w-4" />
                    <span>ID / Owner</span>
                  </div>
                  <p className="font-mono text-xs truncate">
                    {pilot.id} (Owner: {pilot.ownerId})
                  </p>
                </div>
              </div>

              <div className="border rounded-lg p-4 space-y-2">
                <div className="text-xs font-semibold text-muted-foreground uppercase tracking-wide">
                  {t.operationalObjectives}
                </div>
                <p className="text-sm leading-relaxed whitespace-pre-wrap">
                  {pilot.operationalObjectives}
                </p>
              </div>

              {pilot.approvedBy && (
                <div className="border rounded-lg p-4 bg-emerald-50 dark:bg-emerald-950/20 border-emerald-200 dark:border-emerald-900/50 space-y-1 text-xs">
                  <div className="font-semibold text-emerald-800 dark:text-emerald-300">
                    {t.statusApproved} by {pilot.approvedBy} at {pilot.approvedAt}
                  </div>
                  {pilot.approvalNotes && (
                    <div className="text-emerald-700 dark:text-emerald-400">
                      {pilot.approvalNotes}
                    </div>
                  )}
                </div>
              )}
            </div>
          )}

          {activeTab === 'participants' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h4 className="text-sm font-semibold">{t.participants}</h4>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => setShowAddParticipant(!showAddParticipant)}
                >
                  <PlusCircle className="h-4 w-4 mr-1.5" />
                  {t.addParticipant}
                </Button>
              </div>

              {showAddParticipant && (
                <form
                  onSubmit={handleAddParticipant}
                  className="border rounded-lg p-4 bg-muted/30 space-y-3"
                >
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                    <div>
                      <Label className="text-xs">{t.userId} *</Label>
                      <Input
                        value={participantUserId}
                        onChange={(e) => setParticipantUserId(e.target.value)}
                        placeholder="UUID or username"
                        className="h-8 text-xs"
                        required
                      />
                    </div>
                    <div>
                      <Label className="text-xs">{t.roleInPilot} *</Label>
                      <select
                        value={participantRole}
                        onChange={(e) => setParticipantRole(e.target.value)}
                        className="w-full h-8 text-xs rounded-md border border-input bg-background px-3"
                      >
                        <option value="OPERATOR">OPERATOR</option>
                        <option value="AGRONOMIST">AGRONOMIST</option>
                        <option value="FARMER">FARMER</option>
                        <option value="DATA_STEWARD">DATA_STEWARD</option>
                        <option value="INDEPENDENT_VERIFIER">INDEPENDENT_VERIFIER</option>
                      </select>
                    </div>
                    <div>
                      <Label className="text-xs">{t.assignedFields}</Label>
                      <Input
                        value={participantAssignedFields}
                        onChange={(e) => setParticipantAssignedFields(e.target.value)}
                        placeholder="Comma-separated IDs"
                        className="h-8 text-xs"
                      />
                    </div>
                  </div>
                  <div className="flex justify-end gap-2 pt-1">
                    <Button
                      type="button"
                      size="sm"
                      variant="ghost"
                      onClick={() => setShowAddParticipant(false)}
                    >
                      {t.cancel}
                    </Button>
                    <Button
                      type="submit"
                      size="sm"
                      disabled={addParticipantMutation.isPending}
                    >
                      {addParticipantMutation.isPending ? t.saving : t.save}
                    </Button>
                  </div>
                </form>
              )}

              {isLoadingParticipants ? (
                <div className="text-xs text-muted-foreground text-center py-4">Loading...</div>
              ) : participants.length === 0 ? (
                <div className="border border-dashed rounded-lg p-6 text-center text-xs text-muted-foreground">
                  No participants enrolled in this pilot yet.
                </div>
              ) : (
                <div className="border rounded-md overflow-x-auto">
                  <table className="w-full text-xs">
                    <thead className="bg-muted text-muted-foreground border-b text-left">
                      <tr>
                        <th className="py-2 px-3 font-semibold">{t.userId}</th>
                        <th className="py-2 px-3 font-semibold">{t.roleInPilot}</th>
                        <th className="py-2 px-3 font-semibold">{t.assignedFields}</th>
                        <th className="py-2 px-3 font-semibold">Enrolled At</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {participants.map((p) => (
                        <tr key={p.id} className="hover:bg-muted/50">
                          <td className="py-2 px-3 font-mono">{p.userId}</td>
                          <td className="py-2 px-3">
                            <Badge variant="outline">{p.roleInPilot}</Badge>
                          </td>
                          <td className="py-2 px-3 font-mono text-muted-foreground">
                            {p.assignedFieldIds || '—'}
                          </td>
                          <td className="py-2 px-3 text-muted-foreground">
                            {p.joinedAt ? new Date(p.joinedAt).toLocaleDateString() : '—'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {activeTab === 'fields' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h4 className="text-sm font-semibold">{t.enrolledFields}</h4>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => setShowAddField(!showAddField)}
                >
                  <PlusCircle className="h-4 w-4 mr-1.5" />
                  {t.addField}
                </Button>
              </div>

              {showAddField && (
                <form
                  onSubmit={handleAddField}
                  className="border rounded-lg p-4 bg-muted/30 space-y-3"
                >
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div>
                      <Label className="text-xs">{t.fieldId} *</Label>
                      <Input
                        value={fieldId}
                        onChange={(e) => setFieldId(e.target.value)}
                        placeholder="UUID of farm field"
                        className="h-8 text-xs"
                        required
                      />
                    </div>
                    <div>
                      <Label className="text-xs">{t.monitoringTarget} *</Label>
                      <Input
                        value={monitoringTarget}
                        onChange={(e) => setMonitoringTarget(e.target.value)}
                        placeholder="e.g. SOIL_MOISTURE_REGIME"
                        className="h-8 text-xs"
                        required
                      />
                    </div>
                    <div>
                      <Label className="text-xs">{t.measurementPlan} *</Label>
                      <Input
                        value={measurementPlan}
                        onChange={(e) => setMeasurementPlan(e.target.value)}
                        placeholder="Daily tensiometer vs gravimetric"
                        className="h-8 text-xs"
                        required
                      />
                    </div>
                    <div>
                      <Label className="text-xs">{t.validationCriteria} *</Label>
                      <Input
                        value={validationCriteria}
                        onChange={(e) => setValidationCriteria(e.target.value)}
                        placeholder="Error <= 3% VWC"
                        className="h-8 text-xs"
                        required
                      />
                    </div>
                  </div>
                  <div className="flex justify-end gap-2 pt-1">
                    <Button
                      type="button"
                      size="sm"
                      variant="ghost"
                      onClick={() => setShowAddField(false)}
                    >
                      {t.cancel}
                    </Button>
                    <Button
                      type="submit"
                      size="sm"
                      disabled={addFieldMutation.isPending}
                    >
                      {addFieldMutation.isPending ? t.saving : t.save}
                    </Button>
                  </div>
                </form>
              )}

              {isLoadingFields ? (
                <div className="text-xs text-muted-foreground text-center py-4">Loading...</div>
              ) : fields.length === 0 ? (
                <div className="border border-dashed rounded-lg p-6 text-center text-xs text-muted-foreground">
                  No fields enrolled in this pilot yet.
                </div>
              ) : (
                <div className="border rounded-md overflow-x-auto">
                  <table className="w-full text-xs">
                    <thead className="bg-muted text-muted-foreground border-b text-left">
                      <tr>
                        <th className="py-2 px-3 font-semibold">{t.fieldId}</th>
                        <th className="py-2 px-3 font-semibold">{t.monitoringTarget}</th>
                        <th className="py-2 px-3 font-semibold">{t.measurementPlan}</th>
                        <th className="py-2 px-3 font-semibold">{t.validationCriteria}</th>
                        <th className="py-2 px-3 font-semibold">Added At</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {fields.map((f) => (
                        <tr key={f.id} className="hover:bg-muted/50">
                          <td className="py-2 px-3 font-mono">{f.fieldId}</td>
                          <td className="py-2 px-3 font-medium">{f.monitoringTarget}</td>
                          <td className="py-2 px-3 text-muted-foreground">{f.measurementPlan}</td>
                          <td className="py-2 px-3 text-muted-foreground">{f.validationCriteria}</td>
                          <td className="py-2 px-3 text-muted-foreground">
                            {f.addedAt ? new Date(f.addedAt).toLocaleDateString() : '—'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
        </div>

        <DialogFooter className="border-t pt-3">
          <Button variant="outline" onClick={onClose} size="sm">
            {t.close}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
