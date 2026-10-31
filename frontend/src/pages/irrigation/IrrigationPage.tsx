import React, { useState } from 'react'
import {
  Tabs,
  TabsList,
  TabsTrigger,
  TabsContent,
} from '@/shared/ui/tabs'
import { Button } from '@/shared/ui/button'
import { useToast } from '@/shared/ui/toast'
import { Plus, Play, Droplets } from 'lucide-react'
import { useAuth } from '@/features/auth'
import { useFieldsQuery } from '@/features/field'
import {
  WaterAllocation,
  IrrigationPlan,
  IrrigationEvent,
  WaterSource,
} from '@/entities/water'
import {
  useWaterSourcesQuery,
  useWaterAllocationsQuery,
  useIrrigationPlansQuery,
  useIrrigationEventsQuery,
  useCreateWaterSourceMutation,
  useUpdateWaterSourceMutation,
  useActivateWaterSourceMutation,
  useDeactivateWaterSourceMutation,
  useCreateWaterAllocationMutation,
  useUpdateWaterAllocationMutation,
  useCloseWaterAllocationMutation,
  useCancelWaterAllocationMutation,
  useCreateIrrigationPlanMutation,
  useUpdateIrrigationPlanMutation,
  useStartIrrigationPlanMutation,
  useCompleteIrrigationPlanMutation,
  useCancelIrrigationPlanMutation,
  useCreateIrrigationEventMutation,
  useRecordCompletedIrrigationEventMutation,
  useCompleteIrrigationEventMutation,
  useCancelIrrigationEventMutation,
  WaterSummaryCards,
  WaterAllocationSection,
  IrrigationPlanSection,
  IrrigationEventSection,
  FieldWaterSummarySection,
  WaterSourceCatalog,
  RecordCompletedDialog,
  StartIrrigationDialog,
  CompleteIrrigationDialog,
  AllocationFormDialog,
  PlanFormDialog,
  WaterSourceFormDialog,
  CancelOperationDialog,
  WaterSourceFormData,
  WaterAllocationFormData,
  IrrigationPlanFormData,
  StartIrrigationEventFormData,
  RecordCompletedEventFormData,
  CompleteIrrigationEventFormData,
} from '@/features/water'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface IrrigationPageProps {
  lang: SupportedLanguage
}

type CancelTarget =
  | { type: 'ALLOCATION_CLOSE'; id: string }
  | { type: 'ALLOCATION_CANCEL'; id: string }
  | { type: 'PLAN_CANCEL'; id: string }
  | { type: 'EVENT_CANCEL'; id: string }

export const IrrigationPage: React.FC<IrrigationPageProps> = ({ lang }) => {
  const t = translations[lang]
  const { user } = useAuth()
  const { success: toastSuccess, error: toastError } = useToast()

  const [activeTab, setActiveTab] = useState<string>('quotas')

  // Dialog states
  const [recordCompletedOpen, setRecordCompletedOpen] = useState<boolean>(false)
  const [startIrrigationOpen, setStartIrrigationOpen] = useState<boolean>(false)
  const [completeEventOpen, setCompleteEventOpen] = useState<boolean>(false)
  const [eventToComplete, setEventToComplete] = useState<IrrigationEvent | null>(null)

  const [allocationModalOpen, setAllocationModalOpen] = useState<boolean>(false)
  const [allocationToEdit, setAllocationToEdit] = useState<WaterAllocation | null>(null)

  const [planModalOpen, setPlanModalOpen] = useState<boolean>(false)
  const [planToEdit, setPlanToEdit] = useState<IrrigationPlan | null>(null)

  const [waterSourceModalOpen, setWaterSourceModalOpen] = useState<boolean>(false)
  const [waterSourceToEdit, setWaterSourceToEdit] = useState<WaterSource | null>(null)

  const [cancelTarget, setCancelTarget] = useState<CancelTarget | null>(null)

  // Roles & Permissions
  const isOperatorOrAdmin = user?.role === 'OPERATOR' || user?.role === 'ADMIN'
  const canManageSources = isOperatorOrAdmin
  const canManageWater = Boolean(user)

  // Queries
  const { data: fieldsPage } = useFieldsQuery({ size: 100 })
  const fields = fieldsPage?.content || []

  const { data: sourcesPage, isLoading: sourcesLoading } = useWaterSourcesQuery({ size: 100 })
  const sources = sourcesPage?.content || []

  const { data: allocationsPage, isLoading: allocationsLoading } = useWaterAllocationsQuery({
    size: 100,
  })
  const allocations = allocationsPage?.content || []

  const { data: plansPage, isLoading: plansLoading } = useIrrigationPlansQuery({ size: 100 })
  const plans = plansPage?.content || []

  const { data: eventsPage, isLoading: eventsLoading } = useIrrigationEventsQuery({ size: 100 })
  const events = eventsPage?.content || []

  // Top Aggregated Metrics
  const totalAllocatedVolumeM3 = allocations.reduce(
    (acc, a) => (a.status !== 'CANCELLED' ? acc + (a.allocatedVolumeM3 || 0) : acc),
    0
  )
  const totalConsumedVolumeM3 = events.reduce(
    (acc, e) => (e.status === 'COMPLETED' ? acc + (e.actualVolumeM3 || 0) : acc),
    0
  )
  const totalRemainingVolumeM3 = Math.max(totalAllocatedVolumeM3 - totalConsumedVolumeM3, 0)
  const totalOverConsumedVolumeM3 = Math.max(totalConsumedVolumeM3 - totalAllocatedVolumeM3, 0)
  const isOverConsumed = totalConsumedVolumeM3 > totalAllocatedVolumeM3
  const activeQuotaCount = allocations.filter((a) => a.status === 'ACTIVE').length
  const completedEventsCount = events.filter((e) => e.status === 'COMPLETED').length

  // Mutations
  const createWaterSourceMut = useCreateWaterSourceMutation()
  const updateWaterSourceMut = useUpdateWaterSourceMutation()
  const activateWaterSourceMut = useActivateWaterSourceMutation()
  const deactivateWaterSourceMut = useDeactivateWaterSourceMutation()

  const createAllocationMut = useCreateWaterAllocationMutation()
  const updateAllocationMut = useUpdateWaterAllocationMutation()
  const closeAllocationMut = useCloseWaterAllocationMutation()
  const cancelAllocationMut = useCancelWaterAllocationMutation()

  const createPlanMut = useCreateIrrigationPlanMutation()
  const updatePlanMut = useUpdateIrrigationPlanMutation()
  const startPlanMut = useStartIrrigationPlanMutation()
  const completePlanMut = useCompleteIrrigationPlanMutation()
  const cancelPlanMut = useCancelIrrigationPlanMutation()

  const createEventMut = useCreateIrrigationEventMutation()
  const recordCompletedEventMut = useRecordCompletedIrrigationEventMutation()
  const completeEventMut = useCompleteIrrigationEventMutation()
  const cancelEventMut = useCancelIrrigationEventMutation()

  // Handlers for Water Sources
  const handleSaveWaterSource = async (data: WaterSourceFormData, id?: string) => {
    try {
      if (id) {
        await updateWaterSourceMut.mutateAsync({ id, payload: data })
        toastSuccess(t.water.messages.sourceUpdated)
      } else {
        await createWaterSourceMut.mutateAsync(data)
        toastSuccess(t.water.messages.sourceCreated)
      }
    } catch (err: unknown) {
      toastError(err instanceof Error ? err.message : t.water.messages.operationFailed)
      throw err
    }
  }

  const handleActivateSource = async (source: WaterSource) => {
    try {
      await activateWaterSourceMut.mutateAsync(source.id)
      toastSuccess(t.water.messages.sourceActivated)
    } catch (err: unknown) {
      toastError(err instanceof Error ? err.message : t.water.messages.operationFailed)
    }
  }

  const handleDeactivateSource = async (source: WaterSource) => {
    try {
      await deactivateWaterSourceMut.mutateAsync(source.id)
      toastSuccess(t.water.messages.sourceDeactivated)
    } catch (err: unknown) {
      toastError(err instanceof Error ? err.message : t.water.messages.operationFailed)
    }
  }

  // Handlers for Allocations
  const handleSaveAllocation = async (data: WaterAllocationFormData, id?: string) => {
    try {
      if (id) {
        await updateAllocationMut.mutateAsync({
          id,
          payload: {
            allocatedVolumeM3: data.allocatedVolumeM3,
            periodStart: data.periodStart,
            periodEnd: data.periodEnd,
            notes: data.notes,
          },
        })
        toastSuccess(t.water.messages.allocationUpdated)
      } else {
        await createAllocationMut.mutateAsync({
          fieldId: data.fieldId,
          waterSourceId: data.waterSourceId,
          allocatedVolumeM3: data.allocatedVolumeM3,
          periodStart: data.periodStart,
          periodEnd: data.periodEnd,
          notes: data.notes,
        })
        toastSuccess(t.water.messages.allocationCreated)
      }
    } catch (err: unknown) {
      toastError(err instanceof Error ? err.message : t.water.messages.operationFailed)
      throw err
    }
  }

  // Handlers for Plans
  const handleSavePlan = async (data: IrrigationPlanFormData, id?: string) => {
    try {
      if (id) {
        await updatePlanMut.mutateAsync({
          id,
          payload: {
            plannedStartAt: data.plannedStartAt,
            plannedEndAt: data.plannedEndAt,
            plannedVolumeM3: data.plannedVolumeM3,
            method: data.method,
            notes: data.notes,
          },
        })
        toastSuccess(t.water.messages.planUpdated)
      } else {
        await createPlanMut.mutateAsync({
          fieldId: data.fieldId,
          waterAllocationId: data.waterAllocationId,
          plannedStartAt: data.plannedStartAt,
          plannedEndAt: data.plannedEndAt,
          plannedVolumeM3: data.plannedVolumeM3,
          method: data.method,
          notes: data.notes,
        })
        toastSuccess(t.water.messages.planCreated)
      }
    } catch (err: unknown) {
      toastError(err instanceof Error ? err.message : t.water.messages.operationFailed)
      throw err
    }
  }

  const handleStartPlan = async (plan: IrrigationPlan) => {
    try {
      await startPlanMut.mutateAsync(plan.id)
      toastSuccess(t.water.messages.planStarted)
    } catch (err: unknown) {
      toastError(err instanceof Error ? err.message : t.water.messages.operationFailed)
    }
  }

  const handleCompletePlan = async (plan: IrrigationPlan) => {
    try {
      await completePlanMut.mutateAsync(plan.id)
      toastSuccess(t.water.messages.planCompleted)
    } catch (err: unknown) {
      toastError(err instanceof Error ? err.message : t.water.messages.operationFailed)
    }
  }

  // Handlers for Events
  const handleStartEvent = async (data: StartIrrigationEventFormData) => {
    try {
      await createEventMut.mutateAsync({
        fieldId: data.fieldId,
        irrigationPlanId: data.irrigationPlanId,
        startAt: data.startAt,
        notes: data.notes,
      })
      toastSuccess(t.water.messages.eventStarted)
    } catch (err: unknown) {
      toastError(err instanceof Error ? err.message : t.water.messages.operationFailed)
      throw err
    }
  }

  const handleRecordCompletedEvent = async (data: RecordCompletedEventFormData) => {
    try {
      await recordCompletedEventMut.mutateAsync({
        fieldId: data.fieldId,
        irrigationPlanId: data.irrigationPlanId,
        startAt: data.startAt,
        endAt: data.endAt,
        actualVolumeM3: data.actualVolumeM3,
        notes: data.notes,
      })
      toastSuccess(t.water.messages.eventRecorded)
    } catch (err: unknown) {
      toastError(err instanceof Error ? err.message : t.water.messages.operationFailed)
      throw err
    }
  }

  const handleCompleteEvent = async (
    eventId: string,
    data: CompleteIrrigationEventFormData
  ) => {
    try {
      await completeEventMut.mutateAsync({
        id: eventId,
        payload: {
          endAt: data.endAt,
          actualVolumeM3: data.actualVolumeM3,
          notes: data.notes,
        },
      })
      toastSuccess(t.water.messages.eventCompleted)
    } catch (err: unknown) {
      toastError(err instanceof Error ? err.message : t.water.messages.operationFailed)
      throw err
    }
  }

  // Confirm generic cancel / close dialog
  const handleConfirmCancelTarget = async (reason?: string) => {
    if (!cancelTarget) return
    try {
      switch (cancelTarget.type) {
        case 'ALLOCATION_CLOSE':
          await closeAllocationMut.mutateAsync({
            id: cancelTarget.id,
            payload: { reason },
          })
          toastSuccess(t.water.messages.allocationClosed)
          break
        case 'ALLOCATION_CANCEL':
          await cancelAllocationMut.mutateAsync({
            id: cancelTarget.id,
            payload: { reason },
          })
          toastSuccess(t.water.messages.allocationCancelled)
          break
        case 'PLAN_CANCEL':
          await cancelPlanMut.mutateAsync({
            id: cancelTarget.id,
            payload: { reason },
          })
          toastSuccess(t.water.messages.planCancelled)
          break
        case 'EVENT_CANCEL':
          await cancelEventMut.mutateAsync({
            id: cancelTarget.id,
            payload: { reason },
          })
          toastSuccess(t.water.messages.eventCancelled)
          break
      }
      setCancelTarget(null)
    } catch (err: unknown) {
      toastError(err instanceof Error ? err.message : t.water.messages.operationFailed)
    }
  }

  return (
    <div className="space-y-6 pb-12">
      {/* Top Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold tracking-tight text-foreground">
              {t.water.title}
            </h1>
            <span className="p-1 rounded-md bg-sky-100 text-sky-700 dark:bg-sky-950/60 dark:text-sky-300">
              <Droplets className="w-5 h-5" />
            </span>
          </div>
          <p className="text-sm text-muted-foreground mt-1">{t.water.subtitle}</p>
        </div>

        {/* Global Quick Action Buttons */}
        {canManageWater && (
          <div className="flex flex-wrap items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setStartIrrigationOpen(true)}
              className="gap-1.5 text-xs h-9 text-amber-700 dark:text-amber-300 border-amber-300 dark:border-amber-700 bg-amber-50/60"
            >
              <Play className="w-3.5 h-3.5 fill-amber-500" />
              <span>{t.water.events.startEvent}</span>
            </Button>

            <Button
              size="sm"
              onClick={() => setRecordCompletedOpen(true)}
              className="gap-1.5 text-xs h-9 bg-primary"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>{t.water.events.recordCompleted}</span>
            </Button>
          </div>
        )}
      </div>

      {/* Operational Summary Cards */}
      <WaterSummaryCards
        totalAllocatedVolumeM3={totalAllocatedVolumeM3}
        totalConsumedVolumeM3={totalConsumedVolumeM3}
        totalRemainingVolumeM3={totalRemainingVolumeM3}
        totalOverConsumedVolumeM3={totalOverConsumedVolumeM3}
        isOverConsumed={isOverConsumed}
        activeQuotaCount={activeQuotaCount}
        completedEventsCount={completedEventsCount}
        lang={lang}
      />

      {/* Navigation Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
        <TabsList className="bg-muted/60 p-1 rounded-lg flex flex-wrap gap-1 max-w-full overflow-x-auto justify-start">
          <TabsTrigger value="quotas" className="text-xs px-3 py-1.5 font-medium">
            {t.water.tabs.quotas}
          </TabsTrigger>
          <TabsTrigger value="plans" className="text-xs px-3 py-1.5 font-medium">
            {t.water.tabs.plans}
          </TabsTrigger>
          <TabsTrigger value="events" className="text-xs px-3 py-1.5 font-medium">
            {t.water.tabs.events}
          </TabsTrigger>
          <TabsTrigger value="summary" className="text-xs px-3 py-1.5 font-medium">
            {t.water.tabs.summary}
          </TabsTrigger>
          <TabsTrigger value="sources" className="text-xs px-3 py-1.5 font-medium">
            {t.water.tabs.sources}
          </TabsTrigger>
        </TabsList>

        {/* Tab 1: Water Allocations */}
        <TabsContent value="quotas">
          <WaterAllocationSection
            allocations={allocations}
            fields={fields}
            sources={sources}
            canManage={canManageWater}
            isLoading={allocationsLoading}
            lang={lang}
            onCreateClick={() => {
              setAllocationToEdit(null)
              setAllocationModalOpen(true)
            }}
            onEditClick={(alloc) => {
              setAllocationToEdit(alloc)
              setAllocationModalOpen(true)
            }}
            onCloseClick={(alloc) => {
              setCancelTarget({
                type: 'ALLOCATION_CLOSE',
                id: alloc.id,
              })
            }}
            onCancelClick={(alloc) => {
              setCancelTarget({
                type: 'ALLOCATION_CANCEL',
                id: alloc.id,
              })
            }}
          />
        </TabsContent>

        {/* Tab 2: Irrigation Plans */}
        <TabsContent value="plans">
          <IrrigationPlanSection
            plans={plans}
            fields={fields}
            canManage={canManageWater}
            isLoading={plansLoading}
            lang={lang}
            onCreateClick={() => {
              setPlanToEdit(null)
              setPlanModalOpen(true)
            }}
            onStartClick={handleStartPlan}
            onCompleteClick={handleCompletePlan}
            onCancelClick={(plan) => {
              setCancelTarget({
                type: 'PLAN_CANCEL',
                id: plan.id,
              })
            }}
            onEditClick={(plan) => {
              setPlanToEdit(plan)
              setPlanModalOpen(true)
            }}
          />
        </TabsContent>

        {/* Tab 3: Irrigation Events */}
        <TabsContent value="events">
          <IrrigationEventSection
            events={events}
            fields={fields}
            canManage={canManageWater}
            isLoading={eventsLoading}
            lang={lang}
            onStartClick={() => setStartIrrigationOpen(true)}
            onRecordClick={() => setRecordCompletedOpen(true)}
            onCompleteClick={(event) => {
              setEventToComplete(event)
              setCompleteEventOpen(true)
            }}
            onCancelClick={(event) => {
              setCancelTarget({
                type: 'EVENT_CANCEL',
                id: event.id,
              })
            }}
          />
        </TabsContent>

        {/* Tab 4: Field Periodic Water Summary */}
        <TabsContent value="summary">
          <FieldWaterSummarySection fields={fields} lang={lang} />
        </TabsContent>

        {/* Tab 5: Regional Water Sources */}
        <TabsContent value="sources">
          <WaterSourceCatalog
            sources={sources}
            canManage={canManageSources}
            isLoading={sourcesLoading}
            lang={lang}
            onCreateClick={() => {
              setWaterSourceToEdit(null)
              setWaterSourceModalOpen(true)
            }}
            onEditClick={(source) => {
              setWaterSourceToEdit(source)
              setWaterSourceModalOpen(true)
            }}
            onActivateClick={handleActivateSource}
            onDeactivateClick={handleDeactivateSource}
          />
        </TabsContent>
      </Tabs>

      {/* MODALS */}
      {/* 1. Record Completed Irrigation Dialog */}
      <RecordCompletedDialog
        open={recordCompletedOpen}
        onOpenChange={setRecordCompletedOpen}
        fields={fields}
        plans={plans}
        isLoading={recordCompletedEventMut.isPending}
        lang={lang}
        onRecord={handleRecordCompletedEvent}
      />

      {/* 2. Start Irrigation Dialog */}
      <StartIrrigationDialog
        open={startIrrigationOpen}
        onOpenChange={setStartIrrigationOpen}
        fields={fields}
        plans={plans}
        isLoading={createEventMut.isPending}
        lang={lang}
        onStart={handleStartEvent}
      />

      {/* 3. Complete Irrigation Dialog */}
      <CompleteIrrigationDialog
        open={completeEventOpen}
        onOpenChange={setCompleteEventOpen}
        event={eventToComplete}
        isLoading={completeEventMut.isPending}
        lang={lang}
        onComplete={handleCompleteEvent}
      />

      {/* 4. Allocation Form Dialog */}
      <AllocationFormDialog
        open={allocationModalOpen}
        onOpenChange={setAllocationModalOpen}
        fields={fields}
        sources={sources}
        allocationToEdit={allocationToEdit}
        isLoading={createAllocationMut.isPending || updateAllocationMut.isPending}
        lang={lang}
        onSubmitAllocation={handleSaveAllocation}
      />

      {/* 5. Plan Form Dialog */}
      <PlanFormDialog
        open={planModalOpen}
        onOpenChange={setPlanModalOpen}
        fields={fields}
        allocations={allocations}
        planToEdit={planToEdit}
        isLoading={createPlanMut.isPending || updatePlanMut.isPending}
        lang={lang}
        onSubmitPlan={handleSavePlan}
      />

      {/* 6. Water Source Form Dialog */}
      <WaterSourceFormDialog
        open={waterSourceModalOpen}
        onOpenChange={setWaterSourceModalOpen}
        sourceToEdit={waterSourceToEdit}
        isLoading={createWaterSourceMut.isPending || updateWaterSourceMut.isPending}
        lang={lang}
        onSubmitSource={handleSaveWaterSource}
      />

      {/* 7. Generic Cancel / Close Dialog */}
      <CancelOperationDialog
        open={Boolean(cancelTarget)}
        onOpenChange={(open) => !open && setCancelTarget(null)}
        title={
          cancelTarget?.type === 'ALLOCATION_CLOSE'
            ? t.water.allocations.close
            : cancelTarget?.type === 'ALLOCATION_CANCEL'
              ? t.water.allocations.cancel
              : cancelTarget?.type === 'PLAN_CANCEL'
                ? t.water.plans.cancel
                : t.water.events.cancelEvent
        }
        description={
          cancelTarget?.type === 'ALLOCATION_CLOSE'
            ? 'Are you sure you want to close this seasonal allocation quota? It will mark the quota as completed.'
            : 'Are you sure you want to cancel this operation? This action will mark it as cancelled.'
        }
        isDestructive={cancelTarget?.type !== 'ALLOCATION_CLOSE'}
        confirmText={
          cancelTarget?.type === 'ALLOCATION_CLOSE'
            ? t.water.allocations.close
            : t.common.cancel
        }
        isLoading={
          closeAllocationMut.isPending ||
          cancelAllocationMut.isPending ||
          cancelPlanMut.isPending ||
          cancelEventMut.isPending
        }
        lang={lang}
        onConfirm={handleConfirmCancelTarget}
      />
    </div>
  )
}
