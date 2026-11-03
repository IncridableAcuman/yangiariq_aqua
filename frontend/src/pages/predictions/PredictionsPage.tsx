import React, { useState, useEffect } from 'react'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import {
  Compass,
  Droplets,
  Sprout,
  AlertTriangle,
  Cpu,
  ShieldAlert,
  RotateCcw,
  History,
  MapPin,
  Activity,
  Sliders,
} from 'lucide-react'
import { SupportedLanguage } from '@/shared/i18n'
import { NavSection } from '@/widgets/sidebar'
import { Field } from '@/entities/field'
import {
  PredictionHorizon,
  PredictionItem,
  AdaptiveSelectionDecision,
} from '@/entities/prediction'
import {
  usePredictionOverviewQuery,
  useFieldPredictionsQuery,
  useIrrigationNeedRankingQuery,
  useForecastHistoryQuery,
  usePredictionEvaluationOverviewQuery,
  usePredictionEvaluationByTypeQuery,
  usePredictionEvaluationByFieldQuery,
  usePredictionEvaluationHistoryQuery,
  useTriggerEvaluationMutation,
  useGovernanceOverviewQuery,
  usePredictionMethodVersionsQuery,
  useCalibrationProposalsQuery,
  useCalibrationProposalDetailQuery,
  useGovernanceAuditTrailQuery,
  useRunBacktestMutation,
  useApproveProposalMutation,
  useRejectProposalMutation,
  useActivateVersionMutation,
  useRollbackVersionMutation,
  useGenerateProposalMutation,
  useAdaptiveOverviewQuery,
  useContextPerformanceProfilesQuery,
  useAdaptiveSelectionHistoryQuery,
  useSelectAdaptiveMethodMutation,
  PredictionHorizonSelector,
  SoilMoistureForecastCard,
  WaterDemandForecastCard,
  PredictionRiskOverview,
  IrrigationNeedMatrix,
  ForecastHistoryTable,
  PredictionExplanationModal,
  PredictionEvaluationOverviewCard,
  PredictionTypeEvaluationCard,
  ConfidenceCalibrationCard,
  DataQualityImpactCard,
  EvaluationInsightsList,
  HumanDecisionImpactCard,
  FieldEvaluationTable,
  EvaluationHistoryTable,
  GovernanceOverviewCard,
  MethodVersionTable,
  CalibrationProposalList,
  BacktestComparisonModal,
  RollbackModal,
  GovernanceAuditTrailTable,
  AdaptiveHealthStrip,
  ContextPerformanceTable,
  AdaptiveSelectionsLedger,
  ExplainableSelectionModal,
} from '@/features/prediction'
import { useFieldsQuery } from '@/features/field/queries/useFieldQueries'

interface PredictionsPageProps {
  lang: SupportedLanguage
  initialFieldId?: string | null
  onNavigate: (section: NavSection, fieldId?: string) => void
}

export const PredictionsPage: React.FC<PredictionsPageProps> = ({
  lang,
  initialFieldId,
  onNavigate,
}) => {
  const [horizon, setHorizon] = useState<PredictionHorizon>('NEXT_48_HOURS')
  const [selectedFieldId, setSelectedFieldId] = useState<string | null>(initialFieldId || null)
  const [activeTab, setActiveTab] = useState<'forecasts' | 'evaluation' | 'governance' | 'adaptive' | 'history'>('forecasts')
  const [explainingPrediction, setExplainingPrediction] = useState<PredictionItem | null>(null)
  const [explainingAdaptiveDecision, setExplainingAdaptiveDecision] = useState<AdaptiveSelectionDecision | null>(null)

  // Evaluation Sub-State
  const [fieldEvalPage, setFieldEvalPage] = useState<number>(0)
  const [evalHistoryPage, setEvalHistoryPage] = useState<number>(0)
  const [evalStatusFilter, setEvalStatusFilter] = useState<string>('')
  const [evalTypeFilter, setEvalTypeFilter] = useState<string>('')

  // Milestone 08.9: Governance Sub-State
  const [selectedProposalId, setSelectedProposalId] = useState<string | null>(null)
  const [isRollbackOpen, setIsRollbackOpen] = useState(false)

  // Sync initial field ID if passed
  useEffect(() => {
    if (initialFieldId) {
      setSelectedFieldId(initialFieldId)
    }
  }, [initialFieldId])

  // Queries
  const { data: fieldsData } = useFieldsQuery()
  const fields = fieldsData?.content || []

  // Default selected field if not set
  useEffect(() => {
    if (!selectedFieldId && fields.length > 0) {
      setSelectedFieldId(fields[0].id)
    }
  }, [fields, selectedFieldId])

  const {
    data: overview,
    refetch: refetchOverview,
  } = usePredictionOverviewQuery(horizon)

  const {
    data: fieldPredictions,
    refetch: refetchField,
  } = useFieldPredictionsQuery(selectedFieldId, horizon)

  const {
    data: rankingData,
  } = useIrrigationNeedRankingQuery(horizon)

  const {
    data: historyData,
  } = useForecastHistoryQuery()

  // Milestone 08.8: Evaluation Queries
  const {
    data: evalOverview,
    isLoading: isEvalOverviewLoading,
    refetch: refetchEvalOverview,
  } = usePredictionEvaluationOverviewQuery()

  const {
    data: evalByType,
  } = usePredictionEvaluationByTypeQuery()

  const {
    data: evalByField,
    isLoading: isEvalByFieldLoading,
  } = usePredictionEvaluationByFieldQuery({ page: fieldEvalPage, size: 10 })

  const {
    data: evalHistory,
    isLoading: isEvalHistoryLoading,
  } = usePredictionEvaluationHistoryQuery({
    page: evalHistoryPage,
    size: 15,
    status: evalStatusFilter || undefined,
    type: evalTypeFilter || undefined,
  })

  const {
    mutate: triggerEvaluation,
    isPending: isEvaluatingPending,
  } = useTriggerEvaluationMutation()

  // Milestone 08.9: Governance Queries & Mutations
  const {
    data: governanceOverview,
    isLoading: isGovOverviewLoading,
    refetch: refetchGovOverview,
  } = useGovernanceOverviewQuery()

  const {
    data: methodVersions = [],
    isLoading: isVersionsLoading,
  } = usePredictionMethodVersionsQuery()

  const {
    data: calibrationProposals = [],
    isLoading: isProposalsLoading,
  } = useCalibrationProposalsQuery()

  const {
    data: proposalDetail = null,
    isLoading: isProposalDetailLoading,
  } = useCalibrationProposalDetailQuery(selectedProposalId)

  const {
    data: auditTrail = [],
    isLoading: isAuditLoading,
  } = useGovernanceAuditTrailQuery(30)

  const { mutate: runBacktest, isPending: isBacktesting } = useRunBacktestMutation()
  const { mutate: approveProposal, isPending: isApproving } = useApproveProposalMutation()
  const { mutate: rejectProposal, isPending: isRejecting } = useRejectProposalMutation()
  const { mutate: activateVersion, isPending: isActivating } = useActivateVersionMutation()
  const { mutate: rollbackVersion, isPending: isRollingBack } = useRollbackVersionMutation()
  const { mutate: generateProposal, isPending: isGeneratingProposal } = useGenerateProposalMutation()

  // Milestone 08.10: Context-Aware Adaptive Intelligence Queries & Mutation
  const {
    data: adaptiveOverview,
    isLoading: isAdaptiveOverviewLoading,
  } = useAdaptiveOverviewQuery()

  const {
    data: contextProfiles = [],
    isLoading: isProfilesLoading,
  } = useContextPerformanceProfilesQuery()

  const {
    data: selectionDecisions = [],
    isLoading: isSelectionsLoading,
  } = useAdaptiveSelectionHistoryQuery()

  const {
    mutate: selectAdaptiveMethod,
  } = useSelectAdaptiveMethodMutation()

  const t = {
    uz: {
      title: 'Prediktiv Ferma Intellekti va Prognozlash',
      subtitle: 'Tarixiy maʼlumotlar va agronomik modellar asosida kelajakdagi suv, ehtiyoj va xatarlarni baholash',
      parcelsHeader: 'Dala Tanlash',
      allParcels: 'Barcha dalalar bo‘yicha',
      selectedFieldTitle: 'Tanlangan Dala Prognozlari',
      rankingTab: 'Sug‘orish Ehtiyoji Reytingi',
      evaluationTab: 'Model Baholash va Salomatlik (Milestone 08.8)',
      governanceTab: 'Model Boshqaruvi va Kalibratsiya (Milestone 08.9)',
      adaptiveTab: 'Adaptiv Intellekt Dvigateli (Milestone 08.10)',
      historyTab: 'Prognozlar Tarixi va Aniqlik',
      refresh: 'Yangilash',
      needIrrigation: 'Sug‘orish ehtiyoji',
      totalDemand: 'Kutilayotgan suv talabi',
      waterRisk: 'Suv limiti xavfi',
      sensorReliability: 'Datchiklar ishonchliligi',
      hydraulicBlocks: 'Gidravlik blokirovkalar',
    },
    ru: {
      title: 'Предиктивный Интеллект Фермы и Прогнозирование',
      subtitle: 'Оценка будущей потребности в воде, дефицита и рисков на основе агрономических моделей',
      parcelsHeader: 'Выбор Поля',
      allParcels: 'По всем полям',
      selectedFieldTitle: 'Прогнозы Выбранного Поля',
      rankingTab: 'Рейтинг Потребности в Поливе',
      evaluationTab: 'Оценка Моделей и Точность (Milestone 08.8)',
      governanceTab: 'Управление Моделями и Калибровка (Milestone 08.9)',
      adaptiveTab: 'Адаптивный Интеллект (Milestone 08.10)',
      historyTab: 'История Прогнозов и Точность',
      refresh: 'Обновить',
      needIrrigation: 'Потребность в поливе',
      totalDemand: 'Ожидаемый спрос на воду',
      waterRisk: 'Риск лимита воды',
      sensorReliability: 'Надежность датчиков',
      hydraulicBlocks: 'Блокировки безопасности',
    },
    en: {
      title: 'Predictive Farm Intelligence & Forecasting',
      subtitle: 'Evaluating future water demand, soil depletion trajectories, and operational risks',
      parcelsHeader: 'Select Field',
      allParcels: 'All Farm Parcels',
      selectedFieldTitle: 'Selected Field Projections',
      rankingTab: 'Irrigation Need Ranking',
      evaluationTab: 'Model Evaluation & Health (Continuous Intelligence)',
      governanceTab: 'Model Governance & Calibration (Milestone 08.9)',
      adaptiveTab: 'Adaptive Intelligence Engine (Milestone 08.10)',
      historyTab: 'Calibration History & Accuracy',
      refresh: 'Refresh',
      needIrrigation: 'Irrigation Need',
      totalDemand: 'Projected Water Demand',
      waterRisk: 'Water Quota Risk',
      sensorReliability: 'Sensor Reliability',
      hydraulicBlocks: 'Hydraulic Locks',
    },
  }[lang]

  return (
    <div className="space-y-6 pb-12 animate-in fade-in duration-300" data-testid="predictions-workspace">
      {/* 1. Workspace Header Banner */}
      <div className="relative overflow-hidden rounded-2xl border border-primary/20 bg-gradient-to-br from-card via-card to-primary/5 p-5 sm:p-6 shadow-xs">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="space-y-1.5">
            <div className="flex items-center gap-2">
              <Badge variant="outline" className="bg-primary/10 text-primary border-primary/20 gap-1 text-[11px] font-semibold">
                <Compass className="h-3 w-3" />
                <span>YANGIARIQ AQUA AI • PREDICTIVE ENGINE</span>
              </Badge>
              <Badge variant="outline" className="bg-muted text-muted-foreground text-[10px] font-mono">
                DETERMINISTIC XAI
              </Badge>
            </div>
            <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-foreground">
              {t.title}
            </h1>
            <p className="text-xs sm:text-sm text-muted-foreground max-w-2xl leading-relaxed">
              {t.subtitle}
            </p>
          </div>

          {/* Controls: Horizon Selector & Refresh */}
          <div className="flex items-center gap-2 flex-wrap">
            <PredictionHorizonSelector
              horizon={horizon}
              onHorizonChange={setHorizon}
              lang={lang}
            />
            <Button
              variant="outline"
              size="icon"
              className="h-8 w-8 text-muted-foreground hover:text-foreground"
              onClick={() => {
                refetchOverview()
                refetchField()
                refetchEvalOverview()
                refetchGovOverview()
              }}
              title={t.refresh}
            >
              <RotateCcw className="h-3.5 w-3.5" />
            </Button>
          </div>
        </div>
      </div>

      {/* 2. Top Overview Assets Strip (5 KPIs) */}
      {overview && (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3">
          <div className="p-3.5 rounded-xl bg-card border border-border shadow-xs">
            <div className="flex items-center gap-2 text-muted-foreground text-xs font-medium">
              <Sprout className="h-4 w-4 text-emerald-500" />
              <span>{t.needIrrigation}</span>
            </div>
            <div className="mt-1 flex items-baseline gap-1">
              <span className="text-2xl font-extrabold font-mono text-foreground">
                {overview.fieldsNeedingIrrigationCount}
              </span>
              <span className="text-[11px] text-muted-foreground">/{overview.totalParcelsCount}</span>
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-card border border-border shadow-xs">
            <div className="flex items-center gap-2 text-muted-foreground text-xs font-medium">
              <Droplets className="h-4 w-4 text-sky-500" />
              <span>{t.totalDemand}</span>
            </div>
            <div className="mt-1">
              <span className="text-2xl font-extrabold font-mono text-foreground">
                {overview.totalWaterDemandM3.toLocaleString()}
              </span>
              <span className="text-[11px] text-muted-foreground ml-1">m³</span>
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-card border border-border shadow-xs">
            <div className="flex items-center gap-2 text-muted-foreground text-xs font-medium">
              <AlertTriangle className="h-4 w-4 text-amber-500" />
              <span>{t.waterRisk}</span>
            </div>
            <div className="mt-1">
              <span className={`text-2xl font-extrabold font-mono ${overview.fieldsAtWaterRiskCount > 0 ? 'text-amber-600 dark:text-amber-400' : 'text-foreground'}`}>
                {overview.fieldsAtWaterRiskCount}
              </span>
              <span className="text-[11px] text-muted-foreground ml-1">xavf ostida</span>
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-card border border-border shadow-xs">
            <div className="flex items-center gap-2 text-muted-foreground text-xs font-medium">
              <Cpu className="h-4 w-4 text-purple-500" />
              <span>{t.sensorReliability}</span>
            </div>
            <div className="mt-1">
              <span className="text-2xl font-extrabold font-mono text-foreground">
                {overview.sensorHealthPercent}%
              </span>
              <span className="text-[11px] text-muted-foreground ml-1">barqaror</span>
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-card border border-border shadow-xs">
            <div className="flex items-center gap-2 text-muted-foreground text-xs font-medium">
              <ShieldAlert className="h-4 w-4 text-rose-500" />
              <span>{t.hydraulicBlocks}</span>
            </div>
            <div className="mt-1">
              <span className={`text-2xl font-extrabold font-mono ${overview.activeHydraulicBlocksCount > 0 ? 'text-rose-600 dark:text-rose-400' : 'text-foreground'}`}>
                {overview.activeHydraulicBlocksCount}
              </span>
              <span className="text-[11px] text-muted-foreground ml-1">bloklangan</span>
            </div>
          </div>
        </div>
      )}

      {/* 3. Parcel Selector Bar */}
      {fields.length > 0 && (
        <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
          <span className="text-xs font-medium text-muted-foreground flex items-center gap-1.5 shrink-0 mr-1">
            <MapPin className="h-3.5 w-3.5 text-primary" />
            <span>{t.parcelsHeader}:</span>
          </span>
          {fields.map((field: Field) => {
            const isSelected = selectedFieldId === field.id
            return (
              <Button
                key={field.id}
                variant={isSelected ? 'default' : 'outline'}
                size="sm"
                onClick={() => setSelectedFieldId(field.id)}
                className={`text-xs h-7 px-3 rounded-full shrink-0 ${
                  isSelected ? 'shadow-xs' : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                <span>{field.name}</span>
                <span className="text-[10px] opacity-70 ml-1">({field.cropType})</span>
              </Button>
            )
          })}
        </div>
      )}

      {/* 4. Selected Field Predictive Cards */}
      {fieldPredictions && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold text-foreground flex items-center gap-2">
              <span>{fieldPredictions.fieldName} ({fieldPredictions.cropType} • {fieldPredictions.areaHectares} ga)</span>
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <SoilMoistureForecastCard
              prediction={fieldPredictions.soilMoisture}
              lang={lang}
              onExplain={setExplainingPrediction}
              onNavigateToIrrigation={(fieldId) => onNavigate('irrigation', fieldId)}
            />
            <WaterDemandForecastCard
              prediction={fieldPredictions.waterDemand}
              lang={lang}
              onExplain={setExplainingPrediction}
              onNavigateToWater={(fieldId) => onNavigate('operations', fieldId)}
            />
          </div>

          {/* Infrastructure Constraints Strip */}
          <PredictionRiskOverview
            waterRisk={fieldPredictions.waterAvailability}
            planRisk={fieldPredictions.irrigationPlanRisk}
            sensorRisk={fieldPredictions.sensorReliability}
            lang={lang}
            onExplain={setExplainingPrediction}
            onNavigateToModule={(module, fieldId) => onNavigate(module as NavSection, fieldId)}
          />
        </div>
      )}

      {/* 5. Tabs: Irrigation Need Ranking vs Model Evaluation & Health vs Calibration History */}
      <div className="space-y-4 pt-4 border-t border-border">
        <div className="flex items-center gap-2 border-b border-border pb-2 flex-wrap">
          <Button
            variant={activeTab === 'forecasts' ? 'default' : 'ghost'}
            size="sm"
            onClick={() => setActiveTab('forecasts')}
            className="text-xs h-8 gap-1.5 font-medium"
            data-testid="tab-forecasts"
          >
            <Compass className="h-3.5 w-3.5" />
            <span>{t.rankingTab}</span>
          </Button>
          <Button
            variant={activeTab === 'evaluation' ? 'default' : 'ghost'}
            size="sm"
            onClick={() => setActiveTab('evaluation')}
            className="text-xs h-8 gap-1.5 font-medium"
            data-testid="tab-evaluation"
          >
            <Activity className="h-3.5 w-3.5 text-primary" />
            <span>{t.evaluationTab}</span>
          </Button>
          <Button
            variant={activeTab === 'governance' ? 'default' : 'ghost'}
            size="sm"
            onClick={() => setActiveTab('governance')}
            className="text-xs h-8 gap-1.5 font-medium"
            data-testid="tab-governance"
          >
            <Sliders className="h-3.5 w-3.5 text-blue-500" />
            <span>{t.governanceTab}</span>
          </Button>
          <Button
            variant={activeTab === 'adaptive' ? 'default' : 'ghost'}
            size="sm"
            onClick={() => setActiveTab('adaptive')}
            className="text-xs h-8 gap-1.5 font-medium"
            data-testid="tab-adaptive"
          >
            <Cpu className="h-3.5 w-3.5 text-emerald-500" />
            <span>{t.adaptiveTab}</span>
          </Button>
          <Button
            variant={activeTab === 'history' ? 'default' : 'ghost'}
            size="sm"
            onClick={() => setActiveTab('history')}
            className="text-xs h-8 gap-1.5 font-medium"
            data-testid="tab-history"
          >
            <History className="h-3.5 w-3.5" />
            <span>{t.historyTab}</span>
          </Button>
        </div>

        {activeTab === 'forecasts' && (
          <IrrigationNeedMatrix
            fields={rankingData?.rankedFields || []}
            lang={lang}
            onSelectField={setSelectedFieldId}
            onNavigateToIrrigation={(fieldId) => onNavigate('irrigation', fieldId)}
          />
        )}

        {activeTab === 'evaluation' && (
          <div className="space-y-6 animate-in fade-in duration-200" data-testid="evaluation-dashboard-view">
            {/* Top Overview & Gating Banner */}
            <PredictionEvaluationOverviewCard
              overview={evalOverview}
              isLoading={isEvalOverviewLoading}
              isEvaluating={isEvaluatingPending}
              onTriggerEvaluation={() => triggerEvaluation()}
              lang={lang}
            />

            {/* Regression Metrics & Confusion Matrices per Prediction Type */}
            <PredictionTypeEvaluationCard
              performances={evalOverview?.typePerformances || evalByType || []}
              lang={lang}
            />

            {/* Calibration & Telemetry Quality Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <ConfidenceCalibrationCard
                confidenceTiers={evalOverview?.typePerformances?.[0]?.confidenceTiers || []}
                lang={lang}
              />
              <DataQualityImpactCard
                qualityImpacts={evalOverview?.typePerformances?.[0]?.qualityImpacts || []}
                lang={lang}
              />
            </div>

            {/* Deterministic Actionable Insights */}
            <EvaluationInsightsList
              insights={evalOverview?.systemInsights || []}
              lang={lang}
              onNavigateToAction={(module) => onNavigate(module as NavSection)}
            />

            {/* Human Decision vs AI Audit */}
            <HumanDecisionImpactCard
              humanImpacts={evalOverview?.humanDecisionImpacts || []}
              lang={lang}
            />

            {/* Field Breakdown Table */}
            <FieldEvaluationTable
              data={evalByField}
              isLoading={isEvalByFieldLoading}
              currentPage={fieldEvalPage}
              onPageChange={setFieldEvalPage}
              onSelectField={(fieldId) => setSelectedFieldId(fieldId)}
              lang={lang}
            />

            {/* Detailed Verification Ledger */}
            <EvaluationHistoryTable
              data={evalHistory}
              isLoading={isEvalHistoryLoading}
              currentPage={evalHistoryPage}
              onPageChange={setEvalHistoryPage}
              statusFilter={evalStatusFilter}
              onStatusFilterChange={setEvalStatusFilter}
              typeFilter={evalTypeFilter}
              onTypeFilterChange={setEvalTypeFilter}
              lang={lang}
            />
          </div>
        )}

        {activeTab === 'governance' && (
          <div className="space-y-6 animate-in fade-in duration-200" data-testid="governance-dashboard-view">
            {/* 1. Governance Overview & Health Card */}
            <GovernanceOverviewCard
              overview={governanceOverview}
              isLoading={isGovOverviewLoading}
              onOpenRollback={() => setIsRollbackOpen(true)}
              onTriggerAnalysis={() => generateProposal('SOIL_MOISTURE_FORECAST')}
              isAnalyzing={isGeneratingProposal}
              lang={lang}
            />

            {/* 2. Method Versions Registry Table */}
            <MethodVersionTable
              versions={methodVersions}
              isLoading={isVersionsLoading}
              onActivateVersion={(versionId, mode) => activateVersion({ versionId, request: { rolloutMode: mode } })}
              isActivating={isActivating}
              lang={lang}
            />

            {/* 3. Calibration Proposals List */}
            <CalibrationProposalList
              proposals={calibrationProposals}
              isLoading={isProposalsLoading}
              onSelectProposal={(id) => setSelectedProposalId(id)}
              lang={lang}
            />

            {/* 4. Immutable Governance Audit Trail */}
            <GovernanceAuditTrailTable
              logs={auditTrail}
              isLoading={isAuditLoading}
              lang={lang}
            />
          </div>
        )}

        {/* Milestone 08.10: Context-Aware Adaptive Intelligence Engine */}
        {activeTab === 'adaptive' && (
          <div className="space-y-6 animate-in fade-in duration-200" data-testid="adaptive-dashboard-view">
            {/* Top KPI & Principle Banner */}
            <AdaptiveHealthStrip
              overview={adaptiveOverview}
              isLoading={isAdaptiveOverviewLoading}
              lang={lang}
            />

            {/* Context Performance Matrix Table */}
            <ContextPerformanceTable
              profiles={contextProfiles}
              isLoading={isProfilesLoading}
              lang={lang}
            />

            {/* Chronological Selections Ledger */}
            <AdaptiveSelectionsLedger
              decisions={selectionDecisions}
              isLoading={isSelectionsLoading}
              onExplainDecision={(d) => setExplainingAdaptiveDecision(d)}
              onTestParcelSelection={() => {
                if (selectedFieldId) {
                  selectAdaptiveMethod({
                    fieldId: selectedFieldId,
                    predictionType: 'SOIL_MOISTURE_FORECAST',
                  })
                }
              }}
              lang={lang}
            />
          </div>
        )}

        {activeTab === 'history' && (
          <ForecastHistoryTable
            historyData={historyData}
            lang={lang}
          />
        )}
      </div>

      {/* 6. Explainable AI (XAI) Modal */}
      <PredictionExplanationModal
        prediction={explainingPrediction}
        isOpen={!!explainingPrediction}
        onClose={() => setExplainingPrediction(null)}
        lang={lang}
        onNavigateToModule={(module, fieldId) => onNavigate(module as NavSection, fieldId)}
      />

      {/* 6b. Milestone 08.10: Explainable Adaptive Selection Decision Modal */}
      <ExplainableSelectionModal
        decision={explainingAdaptiveDecision}
        isOpen={!!explainingAdaptiveDecision}
        onClose={() => setExplainingAdaptiveDecision(null)}
        lang={lang}
      />

      {/* 7. Backtest & Candidate Comparison Modal */}
      {selectedProposalId && (
        <BacktestComparisonModal
          detail={proposalDetail}
          isLoading={isProposalDetailLoading}
          onClose={() => setSelectedProposalId(null)}
          onRunBacktest={(id) => runBacktest(id)}
          isBacktesting={isBacktesting}
          onApprove={(id, notes) => approveProposal({ proposalId: id, request: { notes } })}
          isApproving={isApproving}
          onReject={(id, reason) => rejectProposal({ proposalId: id, request: { reason } })}
          isRejecting={isRejecting}
          onActivate={(versionId, mode) => {
            activateVersion({ versionId, request: { rolloutMode: mode } })
            setSelectedProposalId(null)
          }}
          isActivating={isActivating}
          lang={lang}
        />
      )}

      {/* 8. Emergency Rollback Modal */}
      <RollbackModal
        isOpen={isRollbackOpen}
        onClose={() => setIsRollbackOpen(false)}
        activeVersions={methodVersions.filter((v) => v.status === 'ACTIVE')}
        onExecuteRollback={(type, priorVersionTag, reason) => {
          rollbackVersion({ type, request: { priorVersionTag, reason } })
          setIsRollbackOpen(false)
        }}
        isRollingBack={isRollingBack}
        lang={lang}
      />
    </div>
  )
}
