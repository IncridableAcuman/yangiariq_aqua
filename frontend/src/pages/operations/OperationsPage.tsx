import { useState, useEffect } from 'react'
import {
  useOperationalContext,
  useOperationalVerification,
  usePreparePlanMutation,
  useApprovePlanMutation,
  useRejectPlanMutation,
  useSchedulePlanMutation,
  useStartOperationMutation,
  useCompleteOperationMutation,
  OperationalContextHeader,
  OperationalWorkflowTimeline,
  CurrentSituationPanel,
  RecommendationSummaryCard,
  ConstraintChecklistCard,
  PlanReviewCard,
  PlanApprovalDialog,
  PlanRejectDialog,
  ScheduleConflictAlert,
  OperationExecutionPanel,
  PostIrrigationVerificationCard,
  WorkflowActionBar,
} from '@/features/operations'
import { useFieldsQuery } from '@/features/field/queries/useFieldQueries'
import { SupportedLanguage } from '@/shared/i18n'
import { AlertCircle, RefreshCw, Layers } from 'lucide-react'

interface OperationsPageProps {
  lang: SupportedLanguage
  initialFieldId?: string | null
  onNavigate?: (section: any, fieldId?: string) => void
}

export function OperationsPage({ lang, initialFieldId, onNavigate }: OperationsPageProps) {
  const { data: fieldsData, isLoading: isFieldsLoading } = useFieldsQuery({ size: 50 })
  const fields = fieldsData?.content || []

  const [selectedFieldId, setSelectedFieldId] = useState<string | null>(initialFieldId || null)
  const [approvalModalOpen, setApprovalModalOpen] = useState(false)
  const [rejectModalOpen, setRejectModalOpen] = useState(false)

  // Default to initialFieldId or first field in list
  useEffect(() => {
    if (initialFieldId) {
      setSelectedFieldId(initialFieldId)
    } else if (!selectedFieldId && fields.length > 0) {
      setSelectedFieldId(fields[0].id)
    }
  }, [initialFieldId, fields, selectedFieldId])

  const {
    data: context,
    isLoading: isContextLoading,
    isError,
    error,
    refetch,
  } = useOperationalContext(selectedFieldId)

  const activePlanId = context?.activePlan?.id || null
  const isPlanCompleted = context?.workflow?.currentState === 'COMPLETED' || context?.workflow?.currentState === 'VERIFIED'

  const { data: verification } = useOperationalVerification(isPlanCompleted ? activePlanId : null)

  const preparePlanMutation = usePreparePlanMutation()
  const approvePlanMutation = useApprovePlanMutation()
  const rejectPlanMutation = useRejectPlanMutation()
  const schedulePlanMutation = useSchedulePlanMutation()
  const startOperationMutation = useStartOperationMutation()
  const completeOperationMutation = useCompleteOperationMutation()

  const isMutating =
    preparePlanMutation.isPending ||
    approvePlanMutation.isPending ||
    rejectPlanMutation.isPending ||
    schedulePlanMutation.isPending ||
    startOperationMutation.isPending ||
    completeOperationMutation.isPending

  const handleActionClick = async (action: string) => {
    if (!context || !selectedFieldId) return

    switch (action) {
      case 'PREPARE_PLAN':
        await preparePlanMutation.mutateAsync({
          fieldId: selectedFieldId,
          payload: { targetDate: new Date().toISOString().split('T')[0] },
        })
        break
      case 'APPROVE_PLAN':
        setApprovalModalOpen(true)
        break
      case 'REJECT_PLAN':
        setRejectModalOpen(true)
        break
      case 'SCHEDULE_PLAN':
        if (context.activePlan) {
          await schedulePlanMutation.mutateAsync({
            planId: context.activePlan.id,
            payload: {},
            fieldId: selectedFieldId,
          })
        }
        break
      case 'START_OPERATION':
        if (context.activePlan) {
          await startOperationMutation.mutateAsync({
            planId: context.activePlan.id,
            payload: { startedAt: new Date().toISOString() },
            fieldId: selectedFieldId,
          })
        }
        break
      case 'COMPLETE_OPERATION':
        if (context.activePlan) {
          await completeOperationMutation.mutateAsync({
            planId: context.activePlan.id,
            payload: {
              actualVolumeDeliveredM3: context.activePlan.recommendedVolumeM3,
              endedAt: new Date().toISOString(),
            },
            fieldId: selectedFieldId,
          })
        }
        break
      case 'INVESTIGATE_SAFETY':
        if (onNavigate) {
          onNavigate('anomalies')
        }
        break
      default:
        break
    }
  }

  const handleConfirmApproval = async (notes?: string) => {
    if (!context?.activePlan || !selectedFieldId) return
    await approvePlanMutation.mutateAsync({
      planId: context.activePlan.id,
      payload: {
        acknowledgeConstraints: true,
        approvalNotes: notes,
      },
      fieldId: selectedFieldId,
    })
  }

  const handleConfirmReject = async (reason: string) => {
    if (!context?.activePlan || !selectedFieldId) return
    await rejectPlanMutation.mutateAsync({
      planId: context.activePlan.id,
      payload: { reason },
      fieldId: selectedFieldId,
    })
  }

  // Loading Skeleton
  if (isFieldsLoading || (isContextLoading && !context)) {
    return (
      <div data-testid="operations-loading" className="space-y-6 animate-pulse">
        <div className="h-24 bg-card border border-border rounded-xl" />
        <div className="h-16 bg-card border border-border rounded-xl" />
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            <div className="h-44 bg-card border border-border rounded-xl" />
            <div className="h-56 bg-card border border-border rounded-xl" />
          </div>
          <div className="h-96 bg-card border border-border rounded-xl" />
        </div>
      </div>
    )
  }

  // Error State
  if (isError || !context) {
    return (
      <div data-testid="operations-error" className="bg-card border border-destructive/30 rounded-xl p-8 text-center space-y-4">
        <AlertCircle className="h-10 w-10 text-destructive mx-auto" />
        <h2 className="text-lg font-bold text-foreground">
          {lang === 'uz' ? 'Operatsion maydon yuklanmadi' : 'Не удалось загрузить операционный контекст'}
        </h2>
        <p className="text-xs text-muted-foreground max-w-md mx-auto">
          {error?.message || (lang === 'uz' ? 'Iltimos, qayta urinib koʻring.' : 'Пожалуйста, повторите попытку.')}
        </p>
        <button
          type="button"
          onClick={() => refetch()}
          className="inline-flex items-center px-4 py-2 rounded-lg text-xs font-semibold bg-primary text-primary-foreground hover:bg-primary/90 transition-colors shadow-sm"
        >
          <RefreshCw className="h-3.5 w-3.5 mr-1.5" />
          {lang === 'uz' ? 'Qayta urinish' : 'Повторить'}
        </button>
      </div>
    )
  }

  return (
    <div data-testid="operations-workspace" className="space-y-6">
      {/* Field Selector & Actions Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-card border border-border p-3.5 rounded-xl shadow-sm">
        <div className="flex items-center space-x-2">
          <Layers className="h-4 w-4 text-muted-foreground" />
          <span className="text-xs font-semibold text-muted-foreground">
            {lang === 'uz' ? 'Operatsion Dala:' : 'Операционное поле:'}
          </span>
          <select
            value={selectedFieldId || ''}
            onChange={(e) => setSelectedFieldId(e.target.value)}
            className="text-xs font-semibold bg-background border border-border rounded-lg px-3 py-1.5 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/20"
          >
            {fields.map((f) => (
              <option key={f.id} value={f.id}>
                {f.name} ({f.cropType || 'Crop'})
              </option>
            ))}
          </select>
        </div>

        <div className="flex items-center space-x-2">
          <button
            type="button"
            onClick={() => refetch()}
            className="p-1.5 rounded-lg border border-border text-muted-foreground hover:text-foreground hover:bg-muted transition-colors text-xs inline-flex items-center"
            title="Yangilash"
          >
            <RefreshCw className="h-3.5 w-3.5" />
          </button>
        </div>
      </div>

      {/* Operational Context Header */}
      <OperationalContextHeader field={context.field} situation={context.situation} lang={lang} />

      {/* Contextual Workflow Action Bar */}
      <WorkflowActionBar
        currentState={context.workflow.currentState}
        isBlocked={context.workflow.isBlocked}
        availableActions={context.workflow.availableActions}
        lang={lang}
        onActionClick={handleActionClick}
        isLoading={isMutating}
      />

      {/* Schedule Conflict Warning */}
      {context.workflow.scheduleConflict && (
        <ScheduleConflictAlert
          conflict={context.workflow.scheduleConflict}
          lang={lang}
          onAdjustTime={() => handleActionClick('SCHEDULE_PLAN')}
        />
      )}

      {/* Main Dual-Column Operational Workspace */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Situation, Advice, Constraints, Execution */}
        <div className="lg:col-span-2 space-y-6">
          <CurrentSituationPanel situation={context.situation} lang={lang} />

          <RecommendationSummaryCard
            recommendation={context.activeRecommendation}
            lang={lang}
            onPreparePlan={
              context.workflow.availableActions.includes('PREPARE_PLAN')
                ? () => handleActionClick('PREPARE_PLAN')
                : undefined
            }
          />

          <ConstraintChecklistCard situation={context.situation} lang={lang} />

          {context.activePlan && (
            <PlanReviewCard
              plan={context.activePlan}
              lang={lang}
              onOpenApprove={() => setApprovalModalOpen(true)}
              onOpenReject={() => setRejectModalOpen(true)}
              onOpenSchedule={() => handleActionClick('SCHEDULE_PLAN')}
            />
          )}

          {/* Operation Recording Controls (No Fake Actuators) */}
          {context.activePlan && (
            <OperationExecutionPanel
              plan={context.activePlan}
              currentState={context.workflow.currentState}
              lang={lang}
              onStartOperation={async (notes) => {
                await startOperationMutation.mutateAsync({
                  planId: context.activePlan!.id,
                  payload: { startedAt: new Date().toISOString(), operatorNotes: notes },
                  fieldId: selectedFieldId!,
                })
              }}
              onCompleteOperation={async (vol, notes) => {
                await completeOperationMutation.mutateAsync({
                  planId: context.activePlan!.id,
                  payload: {
                    actualVolumeDeliveredM3: vol,
                    endedAt: new Date().toISOString(),
                    operatorNotes: notes,
                  },
                  fieldId: selectedFieldId!,
                })
              }}
              isLoading={isMutating}
            />
          )}

          {/* Post-Irrigation Verification & Telemetry Delta */}
          {verification && <PostIrrigationVerificationCard verification={verification} lang={lang} />}
        </div>

        {/* Right Column: 7-Step Operational Timeline */}
        <div className="lg:col-span-1">
          <OperationalWorkflowTimeline
            timeline={context.workflow.timeline}
            lang={lang}
            onActionClick={handleActionClick}
          />
        </div>
      </div>

      {/* Modals */}
      {context.activePlan && (
        <PlanApprovalDialog
          isOpen={approvalModalOpen}
          onClose={() => setApprovalModalOpen(false)}
          onConfirm={handleConfirmApproval}
          plan={context.activePlan}
          fieldName={context.field.name}
          cropType={context.field.cropType}
          waterRemainingM3={context.situation.waterRemainingM3}
          isLoading={approvePlanMutation.isPending}
          lang={lang}
        />
      )}

      <PlanRejectDialog
        isOpen={rejectModalOpen}
        onClose={() => setRejectModalOpen(false)}
        onConfirm={handleConfirmReject}
        fieldName={context.field.name}
        isLoading={rejectPlanMutation.isPending}
        lang={lang}
      />
    </div>
  )
}
