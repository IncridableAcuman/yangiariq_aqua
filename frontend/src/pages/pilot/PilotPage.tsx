import React, { useState } from 'react'
import {
  usePilotProgramsQuery,
  usePilotMonitoringQuery,
  usePilotReadinessQuery,
  useCreatePilotMutation,
  useActivatePilotMutation,
  useAssessReadinessMutation,
} from '@/features/pilot'
import {
  PilotProgramsSummaryCards,
  PilotProgramsTable,
  CreatePilotDialog,
  PilotDetailsModal,
  PilotReadinessPanel,
  GroundTruthValidationView,
  PredictionValidationView,
  OperatorFeedbackView,
  PilotEvaluationReportView,
} from '@/features/pilot'
import { PilotProgram } from '@/entities/pilot'
import { useAuth } from '@/features/auth/context/AuthContext'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import {
  Compass,
  PlusCircle,
  Layers,
  ShieldCheck,
  CheckCheck,
  TrendingUp,
  MessageSquare,
  FileBarChart,
  RefreshCw,
} from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'

interface PilotPageProps {
  lang: SupportedLanguage
}

export const PilotPage: React.FC<PilotPageProps> = ({ lang }) => {
  const t = translations[lang].pilot
  const { user } = useAuth()
  const canManage = user?.role === 'OPERATOR' || user?.role === 'ADMIN'

  const [activeTab, setActiveTab] = useState<
    'programs' | 'readiness' | 'groundTruth' | 'prediction' | 'feedback' | 'report'
  >('programs')

  const [selectedPilotId, setSelectedPilotId] = useState<string | null>(null)
  const [detailPilot, setDetailPilot] = useState<PilotProgram | null>(null)
  const [isCreateOpen, setIsCreateOpen] = useState(false)

  const { data: programs = [], refetch } = usePilotProgramsQuery()

  // Default selected pilot to the first active or first pilot if none selected
  const activePilotId =
    selectedPilotId ||
    programs.find((p) => p.status === 'ACTIVE')?.id ||
    programs[0]?.id ||
    ''

  const activePilot = programs.find((p) => p.id === activePilotId) || null

  const { data: monitoring } = usePilotMonitoringQuery(activePilotId || null)
  const { data: readinessAssessment } = usePilotReadinessQuery(activePilotId || null)

  const createMutation = useCreatePilotMutation()
  const activateMutation = useActivatePilotMutation()
  const assessReadinessMutation = useAssessReadinessMutation()

  return (
    <div className="space-y-6 p-4 md:p-8 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b pb-5">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-primary/10 rounded-lg text-primary">
              <Compass className="h-6 w-6" />
            </div>
            <div>
              <h1 className="text-2xl font-bold tracking-tight">{t.title}</h1>
              <p className="text-xs text-muted-foreground mt-0.5">{t.subtitle}</p>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            className="h-8 text-xs"
          >
            <RefreshCw className="h-3.5 w-3.5 mr-1.5" />
            Yangilash
          </Button>

          {canManage && (
            <Button
              size="sm"
              onClick={() => setIsCreateOpen(true)}
              className="h-8 text-xs font-semibold"
            >
              <PlusCircle className="h-3.5 w-3.5 mr-1.5" />
              {t.createPilot}
            </Button>
          )}
        </div>
      </div>

      {/* Summary KPI Cards */}
      <PilotProgramsSummaryCards
        programs={programs}
        monitoring={monitoring}
        lang={lang}
      />

      {/* Program Selector Bar (visible when navigating sub-views) */}
      {programs.length > 0 && activeTab !== 'programs' && (
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-3 bg-muted/40 border rounded-lg">
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold text-muted-foreground">Active Pilot Program:</span>
            <select
              value={activePilotId}
              onChange={(e) => setSelectedPilotId(e.target.value)}
              className="h-8 text-xs font-medium rounded-md border border-input bg-background px-3"
            >
              {programs.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.code} — {p.name} ({p.status})
                </option>
              ))}
            </select>
          </div>

          {activePilot && (
            <div className="flex items-center gap-2">
              <Badge variant="outline" className="font-mono text-xs">
                {activePilot.geographicScope}
              </Badge>
              <Badge>{activePilot.status}</Badge>
            </div>
          )}
        </div>
      )}

      {/* Navigation Tabs */}
      <div className="flex items-center gap-1 border-b overflow-x-auto pb-1 text-xs">
        <Button
          variant={activeTab === 'programs' ? 'default' : 'ghost'}
          size="sm"
          onClick={() => setActiveTab('programs')}
          className="h-8 text-xs"
        >
          <Layers className="h-3.5 w-3.5 mr-1.5" />
          {t.tabs.programs}
        </Button>
        <Button
          variant={activeTab === 'readiness' ? 'default' : 'ghost'}
          size="sm"
          onClick={() => setActiveTab('readiness')}
          className="h-8 text-xs"
        >
          <ShieldCheck className="h-3.5 w-3.5 mr-1.5" />
          {t.tabs.readiness}
        </Button>
        <Button
          variant={activeTab === 'groundTruth' ? 'default' : 'ghost'}
          size="sm"
          onClick={() => setActiveTab('groundTruth')}
          className="h-8 text-xs"
        >
          <CheckCheck className="h-3.5 w-3.5 mr-1.5" />
          {t.tabs.groundTruth}
        </Button>
        <Button
          variant={activeTab === 'prediction' ? 'default' : 'ghost'}
          size="sm"
          onClick={() => setActiveTab('prediction')}
          className="h-8 text-xs"
        >
          <TrendingUp className="h-3.5 w-3.5 mr-1.5" />
          {t.tabs.predictionValidation}
        </Button>
        <Button
          variant={activeTab === 'feedback' ? 'default' : 'ghost'}
          size="sm"
          onClick={() => setActiveTab('feedback')}
          className="h-8 text-xs"
        >
          <MessageSquare className="h-3.5 w-3.5 mr-1.5" />
          {t.tabs.feedback}
        </Button>
        <Button
          variant={activeTab === 'report' ? 'default' : 'ghost'}
          size="sm"
          onClick={() => setActiveTab('report')}
          className="h-8 text-xs"
        >
          <FileBarChart className="h-3.5 w-3.5 mr-1.5" />
          {t.tabs.evaluationReport}
        </Button>
      </div>

      {/* Tab Panels */}
      {activeTab === 'programs' && (
        <PilotProgramsTable
          programs={programs}
          selectedPilotId={activePilotId}
          onSelectPilot={(p: PilotProgram) => {
            setSelectedPilotId(p.id)
          }}
          onViewDetails={(p: PilotProgram) => {
            setSelectedPilotId(p.id)
            setDetailPilot(p)
          }}
          onAssessReadiness={(p: PilotProgram) => {
            setSelectedPilotId(p.id)
            setActiveTab('readiness')
          }}
          onActivatePilot={async (p: PilotProgram) => {
            await activateMutation.mutateAsync(p.id)
            refetch()
          }}
          canManage={canManage}
          lang={lang}
        />
      )}

      {activeTab === 'readiness' && (
        <PilotReadinessPanel
          assessment={readinessAssessment}
          onTriggerAssessment={async () => {
            if (activePilot) {
              await assessReadinessMutation.mutateAsync(activePilot.id)
              refetch()
            }
          }}
          isAssessing={assessReadinessMutation.isPending}
          canManage={canManage}
          lang={lang}
        />
      )}

      {activeTab === 'groundTruth' && activePilot && (
        <GroundTruthValidationView pilotId={activePilot.id} lang={lang} />
      )}

      {activeTab === 'prediction' && activePilot && (
        <PredictionValidationView pilotId={activePilot.id} lang={lang} />
      )}

      {activeTab === 'feedback' && activePilot && (
        <OperatorFeedbackView pilotId={activePilot.id} lang={lang} />
      )}

      {activeTab === 'report' && activePilot && (
        <PilotEvaluationReportView pilotId={activePilot.id} lang={lang} />
      )}

      {/* Fallback if no pilot programs created yet */}
      {programs.length === 0 && activeTab !== 'programs' && (
        <div className="py-16 text-center border border-dashed rounded-lg text-xs text-muted-foreground">
          Please register a pilot program first.
        </div>
      )}

      {/* Create Pilot Dialog */}
      <CreatePilotDialog
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onCreate={async (payload) => {
          await createMutation.mutateAsync(payload)
          refetch()
        }}
        lang={lang}
      />

      {/* Pilot Details Modal */}
      <PilotDetailsModal
        isOpen={Boolean(detailPilot)}
        onClose={() => setDetailPilot(null)}
        pilot={detailPilot}
        lang={lang}
      />
    </div>
  )
}
