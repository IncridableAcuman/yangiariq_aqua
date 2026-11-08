import React, { useState } from 'react'
import {
  useAccountabilityDashboard,
  useIncidentsList,
  useIncidentDetail,
  useReviewIncidentMutation,
  useAssignInvestigatorMutation,
  useProposeActionMutation,
  useApproveActionMutation,
  useCompleteActionMutation,
  useVerifyResolutionMutation,
  useReopenIncidentMutation,
  useIngestFindingsMutation,
  AccountabilityMetricsCards,
  IncidentListTable,
  IncidentDetailModal,
  IncidentReviewDialog,
  AssignInvestigatorDialog,
  ProposeActionDialog,
  VerifyResolutionDialog,
  ReopenIncidentDialog,
} from '@/features/accountability'
import { WaterIncident, CorrectiveAction } from '@/entities/accountability'
import { Button } from '@/shared/ui/button'
import { useToast } from '@/shared/ui/toast'
import {
  DownloadCloud,
  RefreshCw,
  Scale,
  ShieldCheck,
} from 'lucide-react'
import { NavSection } from '@/widgets/sidebar'

interface Props {
  onNavigate?: (section: NavSection, fieldId?: string) => void
}

export const RegionalAccountabilityPage: React.FC<Props> = ({ onNavigate }) => {
  const { toast } = useToast()
  const [selectedIncidentId, setSelectedIncidentId] = useState<string | null>(null)
  const [activeStatusFilter, setActiveStatusFilter] = useState<string | undefined>()

  // Modals state
  const [isDetailOpen, setIsDetailOpen] = useState(false)
  const [reviewingIncident, setReviewingIncident] = useState<WaterIncident | null>(null)
  const [assigningIncident, setAssigningIncident] = useState<WaterIncident | null>(null)
  const [isProposingAction, setIsProposingAction] = useState(false)
  const [isVerifying, setIsVerifying] = useState(false)
  const [isReopening, setIsReopening] = useState(false)

  // Queries
  const { data: dashboardSummary, isLoading: isDashboardLoading, refetch: refetchDashboard } = useAccountabilityDashboard()
  const { data: incidents = [], isLoading: isIncidentsLoading, refetch: refetchIncidents } = useIncidentsList()
  const { data: incidentDetail, isLoading: isDetailLoading, refetch: refetchDetail } = useIncidentDetail(selectedIncidentId ?? undefined)

  // Mutations
  const reviewMutation = useReviewIncidentMutation()
  const assignMutation = useAssignInvestigatorMutation()
  const proposeActionMutation = useProposeActionMutation()
  const approveActionMutation = useApproveActionMutation()
  const completeActionMutation = useCompleteActionMutation()
  const verifyMutation = useVerifyResolutionMutation()
  const reopenMutation = useReopenIncidentMutation()
  const ingestMutation = useIngestFindingsMutation()

  const handleSelectIncident = (incident: WaterIncident) => {
    setSelectedIncidentId(incident.id)
    setIsDetailOpen(true)
  }

  const handleRefreshAll = () => {
    refetchDashboard()
    refetchIncidents()
    if (selectedIncidentId) refetchDetail()
    toast({ title: 'Yangilandi', description: 'Hodisalar maʼlumotlari yangilandi', variant: 'default' })
  }

  const handleIngestFindings = async () => {
    try {
      const ingested = await ingestMutation.mutateAsync()
      toast({
        title: 'Topilmalar integratsiya qilindi',
        description: `${ingested.length} ta operatsion topilma (tafovut va yoʻqotishlar) koʻrib chiqish uchun tekshiruvga olindi.`,
        variant: 'default',
      })
      refetchDashboard()
      refetchIncidents()
    } catch {
      toast({ title: 'Xatolik', description: 'Topilmalarni import qilishda xatolik yuz berdi', variant: 'destructive' })
    }
  }

  const handleReviewSubmit = async (accept: boolean, notes: string) => {
    if (!reviewingIncident) return
    try {
      await reviewMutation.mutateAsync({ id: reviewingIncident.id, payload: { accept, notes } })
      toast({
        title: accept ? 'Ish ochildi (OPEN)' : 'Hodisa rad etildi (DISMISSED)',
        description: 'Koʻrib chiqish xulosasi oʻzgarmas audit jurnaliga saqlandi.',
      })
      setReviewingIncident(null)
      refetchDashboard()
      refetchIncidents()
      if (selectedIncidentId === reviewingIncident.id) refetchDetail()
    } catch (err: any) {
      toast({ title: 'Xatolik', description: err.message || 'Xatolik yuz berdi', variant: 'destructive' })
    }
  }

  const handleAssignSubmit = async (investigatorId: string, investigatorName: string, organization: string, reason: string) => {
    const incId = assigningIncident?.id || selectedIncidentId
    if (!incId) return
    try {
      await assignMutation.mutateAsync({
        id: incId,
        payload: { investigatorId, investigatorName, organization, reason },
      })
      toast({ title: 'Tergovchi biriktirildi', description: `${investigatorName} masʼul tergovchi etib tayinlandi.` })
      setAssigningIncident(null)
      refetchDashboard()
      refetchIncidents()
      if (selectedIncidentId) refetchDetail()
    } catch (err: any) {
      toast({ title: 'Xatolik', description: err.message || 'Biriktirishda xatolik yuz berdi', variant: 'destructive' })
    }
  }

  const handleProposeActionSubmit = async (payload: any) => {
    if (!selectedIncidentId) return
    try {
      await proposeActionMutation.mutateAsync({ id: selectedIncidentId, payload })
      toast({ title: 'Chora taklif etildi', description: 'Tuzatish chorasi tasdiqlash uchun roʻyxatga olindi.' })
      setIsProposingAction(false)
      refetchDashboard()
      refetchIncidents()
      refetchDetail()
    } catch (err: any) {
      toast({ title: 'Xatolik', description: err.message || 'Chorani saqlashda xatolik', variant: 'destructive' })
    }
  }

  const handleApproveAction = async (action: CorrectiveAction) => {
    if (!selectedIncidentId) return
    try {
      await approveActionMutation.mutateAsync({
        id: selectedIncidentId,
        payload: { actionId: action.id, notes: 'Tasdiqlandi' },
      })
      toast({ title: 'Chora tasdiqlandi', description: `${action.actionNumber} ijroga qabul qilindi.` })
      refetchDetail()
      refetchDashboard()
    } catch (err: any) {
      toast({ title: 'Xatolik', description: err.message || 'Tasdiqlashda xatolik', variant: 'destructive' })
    }
  }

  const handleCompleteAction = async (action: CorrectiveAction) => {
    if (!selectedIncidentId) return
    try {
      await completeActionMutation.mutateAsync({
        id: selectedIncidentId,
        payload: { actionId: action.id, completionNotes: 'Fizik koʻrik va taʼmirlash tugatildi' },
      })
      toast({
        title: 'Chora bajarildi',
        description: 'Hodisa endi Mustaqil Tekshiruv (PENDING_VERIFICATION) holatiga oʻtkazildi.',
      })
      refetchDetail()
      refetchDashboard()
      refetchIncidents()
    } catch (err: any) {
      toast({ title: 'Xatolik', description: err.message || 'Xatolik yuz berdi', variant: 'destructive' })
    }
  }

  const handleVerifySubmit = async (payload: any) => {
    if (!selectedIncidentId) return
    try {
      await verifyMutation.mutateAsync({ id: selectedIncidentId, payload })
      toast({
        title: 'Mustaqil tekshiruv yakunlandi',
        description: 'Xulosa rasmiylashtirildi va oʻzgarmas audit jurnaliga kiritildi.',
      })
      setIsVerifying(false)
      refetchDetail()
      refetchDashboard()
      refetchIncidents()
    } catch (err: any) {
      toast({
        title: 'Tekshiruv xatosi',
        description: err.message || 'Vazifalar taqsimoti yoki tekshiruv qoidasi buzildi',
        variant: 'destructive',
      })
    }
  }

  const handleReopenSubmit = async (reason: string) => {
    if (!selectedIncidentId) return
    try {
      await reopenMutation.mutateAsync({ id: selectedIncidentId, payload: { reason } })
      toast({
        title: 'Hodisa qayta ochildi',
        description: 'Qaytalanganlik hisobi yuritilib, tekshiruv qayta faollashtirildi.',
      })
      setIsReopening(false)
      refetchDetail()
      refetchDashboard()
      refetchIncidents()
    } catch (err: any) {
      toast({ title: 'Xatolik', description: err.message || 'Qayta ochishda xatolik', variant: 'destructive' })
    }
  }

  return (
    <div className="space-y-6">
      {/* Top Banner Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-card border border-border p-4 sm:p-6 rounded-2xl shadow-sm">
        <div className="space-y-1">
          <div className="flex flex-wrap items-center gap-2">
            <span className="font-mono text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-primary/10 text-primary border border-primary/20">
              MILESTONE 08.16
            </span>
            <span className="inline-flex items-center gap-1 text-[11px] font-semibold px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
              <span>Mustaqil Tekshiruv & Vazifalar Taqsimoti</span>
            </span>
          </div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <Scale className="w-6 h-6 text-primary" />
            <span>Mintaqaviy Suv Javobgarligi va Hodisalar Tekshiruvi Markazi</span>
          </h1>
          <p className="text-xs sm:text-sm text-muted-foreground max-w-3xl leading-relaxed">
            Dalillarga asoslangan operatsion tergov, masʼuliyat biriktiruvi, tuzatish choralari boshqaruvi va mustaqil yakuniy verifikatsiya tizimi.
          </p>
        </div>

        {/* Global Header Actions */}
        <div className="flex items-center gap-2 shrink-0">
          {onNavigate && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigate('water-accounting')}
              className="text-xs gap-1.5 hidden sm:inline-flex"
            >
              <Scale className="w-3.5 h-3.5" />
              <span>Suv hisobi</span>
            </Button>
          )}

          <Button
            variant="outline"
            size="sm"
            onClick={handleRefreshAll}
            disabled={isDashboardLoading || isIncidentsLoading}
            className="text-xs gap-1.5"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isDashboardLoading || isIncidentsLoading ? 'animate-spin' : ''}`} />
            <span>Yangilash</span>
          </Button>

          <Button
            size="sm"
            onClick={handleIngestFindings}
            disabled={ingestMutation.isPending}
            className="text-xs gap-1.5 bg-primary"
          >
            <DownloadCloud className={`w-3.5 h-3.5 ${ingestMutation.isPending ? 'animate-pulse' : ''}`} />
            <span>Topilmalarni import qilish</span>
          </Button>
        </div>
      </div>

      {/* Metrics Cards */}
      <AccountabilityMetricsCards
        summary={dashboardSummary}
        isLoading={isDashboardLoading}
        onFilterClick={(status) => setActiveStatusFilter(status === activeStatusFilter ? undefined : status)}
      />

      {/* Incidents List Table */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-foreground">Operatsion hodisalar va tekshiruv ishlari</h2>
          <span className="text-xs text-muted-foreground">Jami: <strong>{incidents.length}</strong> ta hodisa</span>
        </div>

        <IncidentListTable
          incidents={incidents}
          isLoading={isIncidentsLoading}
          onSelectIncident={handleSelectIncident}
          onReviewIncident={(inc) => setReviewingIncident(inc)}
          onAssignIncident={(inc) => setAssigningIncident(inc)}
          activeStatusFilter={activeStatusFilter}
          onClearFilters={() => setActiveStatusFilter(undefined)}
        />
      </div>

      {/* Detail Workspace Modal */}
      <IncidentDetailModal
        isOpen={isDetailOpen}
        onClose={() => setIsDetailOpen(false)}
        details={incidentDetail}
        isLoading={isDetailLoading}
        onReview={() => {
          if (incidentDetail?.incident) setReviewingIncident(incidentDetail.incident)
        }}
        onAssign={() => {
          if (incidentDetail?.incident) setAssigningIncident(incidentDetail.incident)
        }}
        onProposeAction={() => setIsProposingAction(true)}
        onApproveAction={handleApproveAction}
        onCompleteAction={handleCompleteAction}
        onVerify={() => setIsVerifying(true)}
        onReopen={() => setIsReopening(true)}
        isOperatorOrAdmin={true}
      />

      {/* Review Dialog */}
      <IncidentReviewDialog
        isOpen={Boolean(reviewingIncident)}
        onClose={() => setReviewingIncident(null)}
        incident={reviewingIncident ?? undefined}
        onSubmit={handleReviewSubmit}
        isSubmitting={reviewMutation.isPending}
      />

      {/* Assign Investigator Dialog */}
      <AssignInvestigatorDialog
        isOpen={Boolean(assigningIncident)}
        onClose={() => setAssigningIncident(null)}
        incident={assigningIncident ?? undefined}
        onSubmit={handleAssignSubmit}
        isSubmitting={assignMutation.isPending}
      />

      {/* Propose Action Dialog */}
      <ProposeActionDialog
        isOpen={isProposingAction}
        onClose={() => setIsProposingAction(false)}
        incident={incidentDetail?.incident}
        onSubmit={handleProposeActionSubmit}
        isSubmitting={proposeActionMutation.isPending}
      />

      {/* Verify Resolution Dialog */}
      <VerifyResolutionDialog
        isOpen={isVerifying}
        onClose={() => setIsVerifying(false)}
        incident={incidentDetail?.incident}
        actions={incidentDetail?.actions}
        onSubmit={handleVerifySubmit}
        isSubmitting={verifyMutation.isPending}
      />

      {/* Reopen Incident Dialog */}
      <ReopenIncidentDialog
        isOpen={isReopening}
        onClose={() => setIsReopening(false)}
        incident={incidentDetail?.incident}
        onSubmit={handleReopenSubmit}
        isSubmitting={reopenMutation.isPending}
      />
    </div>
  )
}
