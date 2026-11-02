import { useState } from 'react'
import {
  PeriodFilterBar,
  DataConfidenceBanner,
  FarmPerformanceKpis,
  WaterPerformanceCard,
  IrrigationPerformanceCard,
  TelemetryQualityCard,
  PeriodComparisonCard,
  PerformanceTrendCharts,
  FieldPerformanceTable,
  FarmInsightsCard,
  useAnalyticsOverview,
  usePerformanceTrends,
  useFieldPerformanceRankings,
  useFarmInsights,
} from '@/features/analytics'
import { PerformancePeriodType, TrendGranularity } from '@/entities/analytics'
import { useFieldsQuery } from '@/features/field/queries/useFieldQueries'
import { SupportedLanguage } from '@/shared/i18n'
import { BarChart3, AlertCircle } from 'lucide-react'

interface AnalyticsPageProps {
  lang: SupportedLanguage
  initialFieldId?: string | null
  onNavigate?: (section: any, payload?: any) => void
}

export function AnalyticsPage({ lang, initialFieldId, onNavigate }: AnalyticsPageProps) {
  const [selectedPeriod, setSelectedPeriod] = useState<PerformancePeriodType>('LAST_30_DAYS')
  const [selectedFieldId, setSelectedFieldId] = useState<string | null>(initialFieldId || null)
  const [selectedCrop, setSelectedCrop] = useState<string | null>(null)
  const [customFrom, setCustomFrom] = useState<string>('')
  const [customTo, setCustomTo] = useState<string>('')
  const [granularity, setGranularity] = useState<TrendGranularity>('DAILY')

  // Field rankings table state
  const [rankingPage, setRankingPage] = useState<number>(0)
  const [rankingSortBy, setRankingSortBy] = useState<string>('score')
  const [rankingSortDir, setRankingSortDir] = useState<'asc' | 'desc'>('desc')

  // Fetch available parcel list for dropdown filter
  const { data: fieldsData } = useFieldsQuery({ size: 100 })
  const fieldList = (fieldsData?.content || []).map((f) => ({ id: f.id, name: f.name }))

  // Parameters for queries
  const commonParams = {
    period: selectedPeriod,
    from: selectedPeriod === 'CUSTOM' && customFrom ? new Date(customFrom).toISOString() : undefined,
    to: selectedPeriod === 'CUSTOM' && customTo ? new Date(customTo).toISOString() : undefined,
    fieldId: selectedFieldId || undefined,
    cropType: selectedCrop || undefined,
  }

  // 1. Overview Query
  const {
    data: overviewData,
    isFetching: isOverviewFetching,
    error: overviewError,
    refetch: refetchOverview,
  } = useAnalyticsOverview(commonParams)

  // 2. Trends Query
  const {
    data: trendsData,
    refetch: refetchTrends,
  } = usePerformanceTrends({
    ...commonParams,
    granularity,
  })

  // 3. Field Rankings Query
  const {
    data: rankingsData,
    refetch: refetchRankings,
  } = useFieldPerformanceRankings({
    ...commonParams,
    page: rankingPage,
    size: 10,
    sortBy: rankingSortBy,
    sortDirection: rankingSortDir,
  })

  // 4. Insights Query
  const {
    data: insightsData,
    refetch: refetchInsights,
  } = useFarmInsights(commonParams)

  const handleRefreshAll = () => {
    refetchOverview()
    refetchTrends()
    refetchRankings()
    refetchInsights()
  }

  const handleCustomDatesChange = (from: string, to: string) => {
    setCustomFrom(from)
    setCustomTo(to)
  }

  const handleNavigateToAction = (targetSection: string, payload?: Record<string, string>) => {
    if (onNavigate) {
      if (targetSection === 'operations' && payload?.fieldId) {
        onNavigate('operations', payload.fieldId)
      } else {
        onNavigate(targetSection as any, payload)
      }
    }
  }

  const t = {
    uz: {
      pageTitle: 'Fermer xoʻjaligi samaradorligi va tarixiy tahlil',
      pageSubtitle:
        'Suv sarfi, reja intizomi, telemetriya sifati va gidravlik xavfsizlik boʻyicha yaxlit tahlil markazi',
      errorTitle: 'Tahliliy maʼlumotlarni yuklashda xatolik yuz berdi',
      errorSubtitle: 'Iltimos, server ulanishini tekshiring yoki qayta urinib koʻring.',
    },
    ru: {
      pageTitle: 'Эффективность хозяйства и историческая аналитика',
      pageSubtitle:
        'Единый центр анализа расхода воды, соблюдения планов, качества телеметрии и гидравлической безопасности',
      errorTitle: 'Ошибка загрузки аналитических данных',
      errorSubtitle: 'Пожалуйста, проверьте соединение с сервером или повторите попытку.',
    },
    en: {
      pageTitle: 'Farm Performance & Historical Intelligence',
      pageSubtitle:
        'Unified intelligence workspace for water compliance, plan adherence, telemetry reliability, and safety records',
      errorTitle: 'Error loading performance analytics',
      errorSubtitle: 'Please verify server connectivity or retry the request.',
    },
  }[lang]

  return (
    <div className="space-y-6 pb-12 animate-fade-in">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-blue-600 text-white shadow-sm">
            <BarChart3 className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white">
              {t.pageTitle}
            </h1>
            <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-0.5">
              {t.pageSubtitle}
            </p>
          </div>
        </div>
      </div>

      {/* Filter Bar */}
      <PeriodFilterBar
        lang={lang}
        selectedPeriod={selectedPeriod}
        onSelectPeriod={setSelectedPeriod}
        dateRangeLabel={overviewData?.period?.label}
        fields={fieldList}
        selectedFieldId={selectedFieldId}
        onSelectField={setSelectedFieldId}
        selectedCrop={selectedCrop}
        onSelectCrop={setSelectedCrop}
        customFrom={customFrom}
        customTo={customTo}
        onCustomDatesChange={handleCustomDatesChange}
        onRefresh={handleRefreshAll}
        isFetching={isOverviewFetching}
      />

      {/* Error state */}
      {overviewError && (
        <div className="p-4 rounded-xl bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900 flex items-start gap-3">
          <AlertCircle className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
          <div>
            <h4 className="text-sm font-semibold text-rose-900 dark:text-rose-200">{t.errorTitle}</h4>
            <p className="text-xs text-rose-700 dark:text-rose-300 mt-0.5">{t.errorSubtitle}</p>
          </div>
        </div>
      )}

      {/* Data Confidence Banner */}
      <DataConfidenceBanner confidence={overviewData?.dataConfidence} lang={lang} />

      {/* KPI Cards Row */}
      <FarmPerformanceKpis
        lang={lang}
        farmOverview={overviewData?.farmOverview}
        water={overviewData?.waterPerformance}
        irrigation={overviewData?.irrigationPerformance}
        telemetry={overviewData?.telemetryQuality}
        verification={overviewData?.verificationPerformance}
        safety={overviewData?.safetyMetrics}
      />

      {/* Water Performance & Period-over-Period Comparison */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <WaterPerformanceCard water={overviewData?.waterPerformance} lang={lang} />
        <PeriodComparisonCard comparison={overviewData?.periodComparison} lang={lang} />
      </div>

      {/* Operations & Telemetry Quality Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <IrrigationPerformanceCard irrigation={overviewData?.irrigationPerformance} lang={lang} />
        <TelemetryQualityCard telemetry={overviewData?.telemetryQuality} lang={lang} />
      </div>

      {/* Trend Dynamics Charts */}
      <PerformanceTrendCharts
        trendPoints={trendsData?.trendPoints || []}
        granularity={granularity}
        onGranularityChange={setGranularity}
        lang={lang}
      />

      {/* Actionable Insights */}
      <FarmInsightsCard
        insights={insightsData || overviewData?.topInsights || []}
        lang={lang}
        onNavigateToAction={handleNavigateToAction}
      />

      {/* Field Performance & Ranking Table */}
      <FieldPerformanceTable
        fields={rankingsData?.fields || []}
        totalElements={rankingsData?.totalElements || 0}
        page={rankingPage}
        size={10}
        totalPages={rankingsData?.totalPages || 0}
        onPageChange={setRankingPage}
        onSortChange={(sortBy, dir) => {
          setRankingSortBy(sortBy)
          setRankingSortDir(dir)
        }}
        currentSortBy={rankingSortBy}
        currentSortDir={rankingSortDir}
        lang={lang}
        onNavigateToOperations={(fId) => handleNavigateToAction('operations', { fieldId: fId })}
      />
    </div>
  )
}
