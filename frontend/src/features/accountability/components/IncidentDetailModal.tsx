import React, { useState } from 'react'
import {
  IncidentDetails,
  CorrectiveAction,
} from '@/entities/accountability'
import { IncidentSeverityBadge } from './IncidentSeverityBadge'
import { IncidentStatusBadge } from './IncidentStatusBadge'
import { AccountabilityEvidenceBadge } from './AccountabilityEvidenceBadge'
import { Button } from '@/shared/ui/button'
import {
  AlertTriangle,
  CheckCircle2,
  Clock,
  ExternalLink,
  FileCheck,
  FileSearch,
  Flame,
  History,
  Layers,
  MapPin,
  Plus,
  RotateCcw,
  ShieldCheck,
  UserCheck,
  Users,
  X,
} from 'lucide-react'

interface Props {
  isOpen: boolean
  onClose: () => void
  details?: IncidentDetails
  isLoading?: boolean
  onReview?: () => void
  onAssign?: () => void
  onRecordInvestigation?: () => void
  onAttachEvidence?: () => void
  onProposeAction?: () => void
  onApproveAction?: (action: CorrectiveAction) => void
  onCompleteAction?: (action: CorrectiveAction) => void
  onVerify?: () => void
  onReopen?: () => void
  isOperatorOrAdmin?: boolean
}

type TabType = 'overview' | 'evidence' | 'investigation' | 'actions' | 'verification' | 'timeline'

export const IncidentDetailModal: React.FC<Props> = ({
  isOpen,
  onClose,
  details,
  isLoading = false,
  onReview,
  onAssign,
  onRecordInvestigation,
  onAttachEvidence,
  onProposeAction,
  onApproveAction,
  onCompleteAction,
  onVerify,
  onReopen,
  isOperatorOrAdmin = true,
}) => {
  const [activeTab, setActiveTab] = useState<TabType>('overview')

  if (!isOpen) return null

  const incident = details?.incident

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-background/80 backdrop-blur-sm animate-in fade-in">
      <div className="bg-card border border-border rounded-2xl shadow-2xl w-full max-w-5xl h-[90vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="p-4 sm:p-6 border-b border-border bg-muted/20 shrink-0">
          <div className="flex items-start justify-between gap-4">
            <div className="space-y-1.5 flex-1 min-w-0">
              <div className="flex flex-wrap items-center gap-2">
                <span className="font-mono text-xs px-2 py-0.5 rounded bg-muted font-bold text-foreground">
                  {incident?.incidentNumber}
                </span>
                {incident && <IncidentSeverityBadge severity={incident.severity} />}
                {incident && <IncidentStatusBadge status={incident.status} />}
                {incident && <AccountabilityEvidenceBadge confidence={incident.confidence} />}
                {incident?.isOverdue && (
                  <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded bg-rose-500/10 text-rose-600 border border-rose-500/20">
                    <AlertTriangle className="w-3 h-3 text-rose-500" />
                    SLA MUDDATI OʻTGAN
                  </span>
                )}
              </div>
              <h2 className="text-lg sm:text-xl font-bold text-foreground tracking-tight line-clamp-2">
                {incident?.title}
              </h2>
            </div>
            <Button variant="ghost" size="sm" onClick={onClose} className="h-8 w-8 p-0 shrink-0">
              <X className="w-4 h-4" />
            </Button>
          </div>

          {/* Nav Tabs */}
          <div className="flex items-center gap-1 mt-4 overflow-x-auto no-scrollbar border-b border-border/40 pb-px">
            <button
              onClick={() => setActiveTab('overview')}
              className={`flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-t-lg transition-colors border-b-2 shrink-0 ${
                activeTab === 'overview'
                  ? 'border-primary text-primary bg-primary/5'
                  : 'border-transparent text-muted-foreground hover:text-foreground'
              }`}
            >
              <Layers className="w-3.5 h-3.5" />
              <span>Dastlabki Topilma & Umumiy</span>
            </button>
            <button
              onClick={() => setActiveTab('evidence')}
              className={`flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-t-lg transition-colors border-b-2 shrink-0 ${
                activeTab === 'evidence'
                  ? 'border-primary text-primary bg-primary/5'
                  : 'border-transparent text-muted-foreground hover:text-foreground'
              }`}
            >
              <FileSearch className="w-3.5 h-3.5" />
              <span>Dalillar toʻplami ({details?.evidence.length ?? 0})</span>
            </button>
            <button
              onClick={() => setActiveTab('investigation')}
              className={`flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-t-lg transition-colors border-b-2 shrink-0 ${
                activeTab === 'investigation'
                  ? 'border-primary text-primary bg-primary/5'
                  : 'border-transparent text-muted-foreground hover:text-foreground'
              }`}
            >
              <Flame className="w-3.5 h-3.5" />
              <span>Tekshiruv & Sabablar</span>
            </button>
            <button
              onClick={() => setActiveTab('actions')}
              className={`flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-t-lg transition-colors border-b-2 shrink-0 ${
                activeTab === 'actions'
                  ? 'border-primary text-primary bg-primary/5'
                  : 'border-transparent text-muted-foreground hover:text-foreground'
              }`}
            >
              <FileCheck className="w-3.5 h-3.5" />
              <span>Tuzatish choralari ({details?.actions.length ?? 0})</span>
            </button>
            <button
              onClick={() => setActiveTab('verification')}
              className={`flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-t-lg transition-colors border-b-2 shrink-0 ${
                activeTab === 'verification'
                  ? 'border-primary text-primary bg-primary/5'
                  : 'border-transparent text-muted-foreground hover:text-foreground'
              }`}
            >
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>Mustaqil Tekshiruv ({details?.verifications.length ?? 0})</span>
            </button>
            <button
              onClick={() => setActiveTab('timeline')}
              className={`flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-t-lg transition-colors border-b-2 shrink-0 ${
                activeTab === 'timeline'
                  ? 'border-primary text-primary bg-primary/5'
                  : 'border-transparent text-muted-foreground hover:text-foreground'
              }`}
            >
              <History className="w-3.5 h-3.5" />
              <span>Audit Tarixi ({details?.timeline.length ?? 0})</span>
            </button>
          </div>
        </div>

        {/* Tab Content Body */}
        <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-6">
          {isLoading ? (
            <div className="flex flex-col items-center justify-center h-48 space-y-3">
              <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
              <p className="text-xs text-muted-foreground">Hodisa maʼlumotlari yuklanmoqda...</p>
            </div>
          ) : !incident ? (
            <p className="text-xs text-muted-foreground text-center py-12">Maʼlumot topilmadi</p>
          ) : (
            <>
              {/* TAB 1: OVERVIEW */}
              {activeTab === 'overview' && (
                <div className="space-y-6">
                  {/* Summary & Description */}
                  <div className="bg-card border border-border p-4 rounded-xl space-y-2">
                    <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                      Hodisa tavsifi
                    </h3>
                    <p className="text-sm text-foreground leading-relaxed whitespace-pre-wrap">
                      {incident.description}
                    </p>
                  </div>

                  {/* Context Grid */}
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    <div className="bg-muted/30 border border-border/60 p-3.5 rounded-xl space-y-1">
                      <span className="text-[11px] text-muted-foreground">Kategoriya</span>
                      <p className="text-xs font-semibold text-foreground">{incident.category.replace(/_/g, ' ')}</p>
                    </div>
                    <div className="bg-muted/30 border border-border/60 p-3.5 rounded-xl space-y-1">
                      <span className="text-[11px] text-muted-foreground">Joylashuvi / Obyekt</span>
                      <p className="text-xs font-semibold text-foreground flex items-center gap-1.5">
                        <MapPin className="w-3.5 h-3.5 text-primary shrink-0" />
                        <span>{details?.canalName || details?.wuaName || details?.fieldName || incident.locationDescription || 'Hududiy obyekt'}</span>
                      </p>
                    </div>
                    <div className="bg-muted/30 border border-border/60 p-3.5 rounded-xl space-y-1">
                      <span className="text-[11px] text-muted-foreground">Manba moduli</span>
                      <p className="text-xs font-mono font-semibold text-foreground">{incident.sourceModule} ({incident.sourceEntityType})</p>
                    </div>
                    <div className="bg-muted/30 border border-border/60 p-3.5 rounded-xl space-y-1">
                      <span className="text-[11px] text-muted-foreground">Aniqlangan vaqti</span>
                      <p className="text-xs font-semibold text-foreground">
                        {new Date(incident.detectionTimestamp).toLocaleString()}
                      </p>
                    </div>
                    <div className="bg-muted/30 border border-border/60 p-3.5 rounded-xl space-y-1">
                      <span className="text-[11px] text-muted-foreground">SLA Bajarilish Muddati</span>
                      <p className="text-xs font-semibold text-foreground flex items-center gap-1">
                        <Clock className="w-3.5 h-3.5 text-muted-foreground" />
                        <span>{incident.slaDeadline ? new Date(incident.slaDeadline).toLocaleString() : 'Belgilanmagan'}</span>
                      </p>
                    </div>
                    <div className="bg-muted/30 border border-border/60 p-3.5 rounded-xl space-y-1">
                      <span className="text-[11px] text-muted-foreground">Masʼul tashkilot</span>
                      <p className="text-xs font-semibold text-foreground flex items-center gap-1.5">
                        <Users className="w-3.5 h-3.5 text-muted-foreground" />
                        <span>{incident.responsibleOrganization || 'Hali biriktirilmagan'}</span>
                      </p>
                    </div>
                  </div>

                  {/* Operational Principle Notice */}
                  <div className="bg-primary/5 border border-primary/20 p-4 rounded-xl flex items-start gap-3">
                    <ShieldCheck className="w-5 h-5 text-primary shrink-0 mt-0.5" />
                    <div className="space-y-1 text-xs">
                      <p className="font-semibold text-foreground">
                        YANGIARIQ AQUA AI: Aybsizlik prezumpsiyasi va xolis operatsion boshqaruv
                      </p>
                      <p className="text-muted-foreground leading-relaxed">
                        Suv hisobidagi tafovut yoki uzatish yoʻqotishi avtomatik ravishda ayblov hisoblanmaydi. Tizim xolis operatsion faktlarni toʻplaydi, fizik koʻrik dalillarini biriktiradi va mustaqil tekshiruv orqali yakuniy xulosa chiqaradi.
                      </p>
                    </div>
                  </div>

                  {/* Review / Dismiss notes if present */}
                  {incident.reviewNotes && (
                    <div className="bg-muted/40 border border-border p-4 rounded-xl space-y-1">
                      <span className="text-[11px] font-semibold text-muted-foreground uppercase">
                        Koʻrib chiqish xulosasi (Review Notes):
                      </span>
                      <p className="text-xs text-foreground italic">{incident.reviewNotes}</p>
                    </div>
                  )}

                  {/* Resolution notes if resolved */}
                  {incident.resolutionNotes && (
                    <div className="bg-emerald-500/10 border border-emerald-500/20 p-4 rounded-xl space-y-1">
                      <span className="text-[11px] font-semibold text-emerald-600 dark:text-emerald-400 uppercase">
                        Hal etish xulosasi (Resolution Notes):
                      </span>
                      <p className="text-xs text-emerald-700 dark:text-emerald-300 whitespace-pre-wrap">{incident.resolutionNotes}</p>
                    </div>
                  )}
                </div>
              )}

              {/* TAB 2: EVIDENCE */}
              {activeTab === 'evidence' && (
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-sm font-bold text-foreground">Biriktirilgan Fizik va Telemetrik Dalillar</h3>
                      <p className="text-xs text-muted-foreground">Akustik oʻlchovlar, datchik loglari, koʻrik aktlari va hisob-kitoblar</p>
                    </div>
                    {isOperatorOrAdmin && incident.status !== 'RESOLVED' && incident.status !== 'CLOSED' && (
                      <Button size="sm" onClick={onAttachEvidence} className="text-xs gap-1.5">
                        <Plus className="w-3.5 h-3.5" />
                        <span>Dalil qoʻshish</span>
                      </Button>
                    )}
                  </div>

                  {details.evidence.length === 0 ? (
                    <div className="p-8 text-center bg-muted/20 border border-dashed border-border rounded-xl">
                      <FileSearch className="w-8 h-8 text-muted-foreground/40 mx-auto mb-2" />
                      <p className="text-xs font-semibold text-foreground">Hali hech qanday dalil biriktirilmagan</p>
                      <p className="text-[11px] text-muted-foreground mt-1">Dala koʻrigi yoki gidrometrik oʻlchov dalillarini yuklang.</p>
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                      {details.evidence.map((ev) => (
                        <div key={ev.id} className="bg-card border border-border p-4 rounded-xl space-y-2.5 shadow-sm">
                          <div className="flex items-start justify-between gap-2">
                            <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-muted text-muted-foreground font-semibold">
                              {ev.evidenceType}
                            </span>
                            <AccountabilityEvidenceBadge confidence={ev.confidence} />
                          </div>
                          <div>
                            <h4 className="text-xs font-bold text-foreground">{ev.title}</h4>
                            {ev.description && <p className="text-[11px] text-muted-foreground mt-1">{ev.description}</p>}
                          </div>
                          <div className="flex items-center justify-between text-[10px] text-muted-foreground pt-2 border-t border-border/50">
                            <span>{new Date(ev.recordedAt).toLocaleString()}</span>
                            {ev.evidenceUri && (
                              <a
                                href={ev.evidenceUri}
                                target="_blank"
                                rel="noreferrer"
                                className="inline-flex items-center gap-1 text-primary hover:underline font-medium"
                              >
                                <span>Faylni ochish</span>
                                <ExternalLink className="w-3 h-3" />
                              </a>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* TAB 3: INVESTIGATION */}
              {activeTab === 'investigation' && (
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-sm font-bold text-foreground">Tekshiruv xulosalari va Ildiz sabablar</h3>
                      <p className="text-xs text-muted-foreground">Texnik, gidravlik yoki tashkiliy omillar tahlili</p>
                    </div>
                    {isOperatorOrAdmin && incident.status !== 'RESOLVED' && incident.status !== 'CLOSED' && (
                      <Button size="sm" onClick={onRecordInvestigation} className="text-xs gap-1.5">
                        <Flame className="w-3.5 h-3.5" />
                        <span>Natijalarni yangilash</span>
                      </Button>
                    )}
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="bg-card border border-border p-4 rounded-xl space-y-2">
                      <span className="text-[11px] font-semibold text-muted-foreground uppercase">Masʼul tergovchi:</span>
                      <p className="text-xs font-bold text-foreground">
                        {incident.assignedInvestigatorName || 'Biriktirilmagan'}
                      </p>
                      <span className="text-[11px] text-muted-foreground block">
                        Tashkilot: {incident.responsibleOrganization || '—'}
                      </span>
                    </div>

                    <div className="bg-card border border-border p-4 rounded-xl space-y-2">
                      <span className="text-[11px] font-semibold text-muted-foreground uppercase">Ildiz sababi kategoriyasi:</span>
                      <p className="text-xs font-bold text-foreground">
                        {incident.rootCauseCategory || 'Hali aniqlanmagan'}
                      </p>
                      <span className="text-[11px] text-muted-foreground block">
                        Dalil ishonchliligi: {incident.confidence}
                      </span>
                    </div>
                  </div>

                  <div className="bg-card border border-border p-4 rounded-xl space-y-2">
                    <span className="text-[11px] font-semibold text-muted-foreground uppercase">Batafsil tekshiruv xulosasi:</span>
                    <p className="text-xs text-foreground leading-relaxed whitespace-pre-wrap">
                      {incident.rootCauseDetails || 'Tekshiruv jarayoni davom etmoqda. Texnik xulosa hali kiritilmagan.'}
                    </p>
                  </div>
                </div>
              )}

              {/* TAB 4: ACTIONS */}
              {activeTab === 'actions' && (
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-sm font-bold text-foreground">Tuzatish choralari rejasi (Corrective Actions)</h3>
                      <p className="text-xs text-muted-foreground">Fizik taʼmirlash, kalibrlash yoki gidropost sozlash topshiriqlari</p>
                    </div>
                    {isOperatorOrAdmin && incident.status !== 'RESOLVED' && incident.status !== 'CLOSED' && (
                      <Button size="sm" onClick={onProposeAction} className="text-xs gap-1.5">
                        <Plus className="w-3.5 h-3.5" />
                        <span>Yangi chora taklif etish</span>
                      </Button>
                    )}
                  </div>

                  {details.actions.length === 0 ? (
                    <div className="p-8 text-center bg-muted/20 border border-dashed border-border rounded-xl">
                      <FileCheck className="w-8 h-8 text-muted-foreground/40 mx-auto mb-2" />
                      <p className="text-xs font-semibold text-foreground">Hali tuzatish chorasi taklif etilmagan</p>
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {details.actions.map((act) => (
                        <div key={act.id} className="bg-card border border-border p-4 rounded-xl space-y-3 shadow-sm">
                          <div className="flex flex-wrap items-center justify-between gap-2">
                            <div className="flex items-center gap-2">
                              <span className="font-mono text-xs font-bold px-2 py-0.5 rounded bg-muted text-foreground">
                                {act.actionNumber}
                              </span>
                              <span className="text-xs font-bold text-foreground">{act.title}</span>
                            </div>
                            <span className="text-[11px] font-semibold px-2 py-0.5 rounded bg-primary/10 text-primary border border-primary/20">
                              {act.status}
                            </span>
                          </div>

                          <p className="text-xs text-muted-foreground leading-relaxed">{act.description}</p>

                          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-[11px] bg-muted/20 p-2.5 rounded-lg border border-border/40">
                            <div>
                              <span className="text-muted-foreground block">Masʼul ijrochi:</span>
                              <span className="font-semibold text-foreground">{act.actionOwnerName || 'Belgilanmagan'}</span>
                            </div>
                            <div>
                              <span className="text-muted-foreground block">Kutilayotgan natija:</span>
                              <span className="font-semibold text-foreground">{act.expectedOutcome}</span>
                            </div>
                            <div>
                              <span className="text-muted-foreground block">Muddat:</span>
                              <span className="font-semibold text-foreground">
                                {act.dueDate ? new Date(act.dueDate).toLocaleDateString() : '—'}
                              </span>
                            </div>
                            <div>
                              <span className="text-muted-foreground block">Tasdiqlagan:</span>
                              <span className="font-semibold text-foreground">{act.approvedBy ? 'Tasdiqlangan' : 'Kutilmoqda'}</span>
                            </div>
                          </div>

                          {act.completionNotes && (
                            <div className="bg-emerald-500/10 border border-emerald-500/20 p-2.5 rounded-lg text-xs text-emerald-700 dark:text-emerald-300">
                              <span className="font-semibold block">Bajarilish hisoboti:</span>
                              {act.completionNotes}
                            </div>
                          )}

                          {/* Action Buttons for operators */}
                          {isOperatorOrAdmin && (
                            <div className="flex items-center justify-end gap-2 pt-2 border-t border-border/50">
                              {act.status === 'PROPOSED' && onApproveAction && (
                                <Button
                                  size="sm"
                                  variant="outline"
                                  className="h-7 text-xs border-teal-500/40 text-teal-600 hover:bg-teal-500/10"
                                  onClick={() => onApproveAction(act)}
                                >
                                  Chorani tasdiqlash
                                </Button>
                              )}
                              {(act.status === 'APPROVED' || act.status === 'IN_PROGRESS') && onCompleteAction && (
                                <Button
                                  size="sm"
                                  className="h-7 text-xs bg-emerald-600 hover:bg-emerald-700 text-white"
                                  onClick={() => onCompleteAction(act)}
                                >
                                  Bajarildi deb belgilash
                                </Button>
                              )}
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* TAB 5: VERIFICATION */}
              {activeTab === 'verification' && (
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-sm font-bold text-foreground">Mustaqil Tekshiruv va Natijalarni Qabul Qilish</h3>
                      <p className="text-xs text-muted-foreground">Vazifalar taqsimoti (Separation of Duties) boʻyicha tekshiruv</p>
                    </div>
                    {isOperatorOrAdmin && incident.status === 'PENDING_VERIFICATION' && onVerify && (
                      <Button size="sm" onClick={onVerify} className="text-xs gap-1.5 bg-emerald-600 hover:bg-emerald-700 text-white">
                        <ShieldCheck className="w-3.5 h-3.5" />
                        <span>Mustaqil tekshiruv oʻtkazish</span>
                      </Button>
                    )}
                  </div>

                  {/* Separation of duties rule reminder */}
                  <div className="bg-amber-500/10 border border-amber-500/20 p-3.5 rounded-xl flex items-start gap-2.5 text-xs">
                    <AlertTriangle className="w-4 h-4 text-amber-500 shrink-0 mt-0.5" />
                    <p className="text-amber-800 dark:text-amber-300 leading-relaxed">
                      <strong>Muhim qoida:</strong> Tuzatish chorasini bajargan shaxs oʻz ishini oʻzi mustaqil tasdiqlashi qatʼiyan man etiladi. Tekshiruv boshqa mustaqil inspektor yoki maʼmur tomonidan fizik koʻrik akti bilan oʻtkazilishi shart.
                    </p>
                  </div>

                  {details.verifications.length === 0 ? (
                    <div className="p-8 text-center bg-muted/20 border border-dashed border-border rounded-xl">
                      <ShieldCheck className="w-8 h-8 text-muted-foreground/40 mx-auto mb-2" />
                      <p className="text-xs font-semibold text-foreground">Mustaqil tekshiruv akti hali kiritilmagan</p>
                      <p className="text-[11px] text-muted-foreground mt-1">Chora bajarilgach, mustaqil inspektor xulosasi talab etiladi.</p>
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {details.verifications.map((v) => (
                        <div key={v.id} className="bg-card border border-border p-4 rounded-xl space-y-2.5 shadow-sm">
                          <div className="flex items-center justify-between gap-2">
                            <span className="text-xs font-bold text-foreground flex items-center gap-1.5">
                              <UserCheck className="w-4 h-4 text-primary" />
                              <span>Inspektor: {v.verifierName}</span>
                            </span>
                            <span className={`text-[11px] font-bold px-2 py-0.5 rounded ${
                              v.result === 'VERIFIED_RESOLVED'
                                ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                                : 'bg-rose-500/10 text-rose-600 border border-rose-500/20'
                            }`}>
                              {v.result}
                            </span>
                          </div>

                          <div className="text-xs bg-muted/30 p-2.5 rounded-lg border border-border/40 space-y-1">
                            <span className="text-[10px] uppercase text-muted-foreground font-semibold">Usul:</span>
                            <p className="font-semibold text-foreground">{v.verificationMethod}</p>
                            <span className="text-[10px] uppercase text-muted-foreground font-semibold block mt-2">Xulosa:</span>
                            <p className="text-foreground leading-relaxed">{v.explanatoryNotes}</p>
                          </div>

                          <div className="flex items-center justify-between text-[10px] text-muted-foreground">
                            <span>Tekshirilgan vaqti: {new Date(v.verifiedAt).toLocaleString()}</span>
                            {v.supportingEvidenceUri && (
                              <a
                                href={v.supportingEvidenceUri}
                                target="_blank"
                                rel="noreferrer"
                                className="inline-flex items-center gap-1 text-primary hover:underline"
                              >
                                <span>Dalil hujjati</span>
                                <ExternalLink className="w-3 h-3" />
                              </a>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* TAB 6: TIMELINE */}
              {activeTab === 'timeline' && (
                <div className="space-y-4">
                  <div>
                    <h3 className="text-sm font-bold text-foreground">Oʻzgarmas Audit Tarixi (Immutable Timeline)</h3>
                    <p className="text-xs text-muted-foreground">Barcha holat oʻzgarishlari, masʼullar va asoslar xronologiyasi</p>
                  </div>

                  <div className="relative border-l-2 border-border ml-3 space-y-6 py-2">
                    {details.timeline.map((entry) => (
                      <div key={entry.id} className="relative pl-6">
                        <div className="absolute -left-1.5 top-1 w-3 h-3 rounded-full bg-primary ring-4 ring-background" />
                        <div className="bg-card border border-border p-3.5 rounded-xl space-y-1.5 shadow-sm">
                          <div className="flex flex-wrap items-center justify-between gap-2">
                            <div className="flex items-center gap-2">
                              <span className="text-xs font-bold text-foreground font-mono">{entry.eventType}</span>
                              {entry.previousState && entry.newState && (
                                <span className="text-[10px] px-1.5 py-0.5 rounded bg-muted text-muted-foreground">
                                  {entry.previousState} → {entry.newState}
                                </span>
                              )}
                            </div>
                            <span className="text-[10px] text-muted-foreground">
                              {new Date(entry.timestamp).toLocaleString()}
                            </span>
                          </div>
                          <p className="text-xs text-foreground">{entry.reason}</p>
                          <div className="text-[10px] text-muted-foreground flex items-center gap-2 pt-1 border-t border-border/40">
                            <span>Masʼul: <strong>{entry.actorName}</strong> ({entry.actorRole || 'SYSTEM'})</span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </>
          )}
        </div>

        {/* Footer Actions */}
        <div className="p-4 border-t border-border bg-muted/20 flex flex-wrap items-center justify-between gap-3 shrink-0">
          <div className="flex items-center gap-2">
            {incident?.status === 'DETECTED' && onReview && isOperatorOrAdmin && (
              <Button size="sm" onClick={onReview} className="text-xs gap-1.5">
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Koʻrib chiqish (Qabul/Rad)</span>
              </Button>
            )}
            {incident?.status === 'OPEN' && onAssign && isOperatorOrAdmin && (
              <Button size="sm" onClick={onAssign} className="text-xs gap-1.5">
                <UserCheck className="w-3.5 h-3.5" />
                <span>Tergovchi biriktirish</span>
              </Button>
            )}
            {(incident?.status === 'RESOLVED' || incident?.status === 'DISMISSED') && onReopen && isOperatorOrAdmin && (
              <Button size="sm" variant="outline" onClick={onReopen} className="text-xs gap-1.5 text-rose-600 border-rose-500/40 hover:bg-rose-500/10">
                <RotateCcw className="w-3.5 h-3.5" />
                <span>Qayta ochish (Muammo qaytalanganda)</span>
              </Button>
            )}
          </div>
          <Button variant="outline" size="sm" onClick={onClose} className="text-xs">
            Yopish
          </Button>
        </div>
      </div>
    </div>
  )
}
