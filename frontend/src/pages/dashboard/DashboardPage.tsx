import React from 'react'
import {
  Droplets,
  Map,
  Sparkles,
  RotateCcw,
  AlertCircle,
  ShieldCheck,
  BarChart3,
  AlertOctagon,
  Waves,
  Scale,
} from 'lucide-react'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { useAuth } from '@/features/auth/context/AuthContext'
import { NavSection } from '@/widgets/sidebar'
import {
  useDashboardOverviewQuery,
  FarmOverviewMetrics,
  ActionCenter,
  PriorityFieldsTable,
  WaterBudgetOverviewCard,
  IrrigationLifecycleTracker,
  RecentActivityFeed,
  DashboardFieldMap,
  FarmPerformanceSummaryCard,
  PredictiveModelHealthCard,
  FarmRiskDashboardCard,
} from '@/features/dashboard'
import { PredictiveSummaryCard } from '@/features/prediction'

interface DashboardPageProps {
  lang: SupportedLanguage
  onNavigate: (section: NavSection, fieldId?: string) => void
}

export const DashboardPage: React.FC<DashboardPageProps> = ({ lang, onNavigate }) => {
  const { user } = useAuth()
  const t = translations[lang].dashboard

  const {
    data: overview,
    isLoading,
    isError,
    error,
    refetch,
    isFetching,
  } = useDashboardOverviewQuery()

  // Format relative last updated time
  const formatTime = (isoString?: string) => {
    if (!isoString) return ''
    try {
      return new Date(isoString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    } catch {
      return isoString
    }
  }

  // -------------------------------------------------------------
  // Loading State: Professional Skeleton Layout
  // -------------------------------------------------------------
  if (isLoading) {
    return (
      <div className="space-y-6 animate-pulse p-1" data-testid="dashboard-loading">
        {/* Welcome Skeleton */}
        <div className="h-28 rounded-2xl bg-muted/60" />

        {/* Action Center Skeleton */}
        <div className="h-36 rounded-xl bg-muted/50" />

        {/* 5-Metrics Grid Skeleton */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3.5">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="h-24 rounded-xl bg-muted/50" />
          ))}
        </div>

        {/* Priority Matrix Skeleton */}
        <div className="h-72 rounded-xl bg-muted/40" />

        {/* Bottom Split Skeleton */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 h-64 rounded-xl bg-muted/40" />
          <div className="h-64 rounded-xl bg-muted/40" />
        </div>
      </div>
    )
  }

  // -------------------------------------------------------------
  // Error State: Reassuring Error Banner with Retry
  // -------------------------------------------------------------
  if (isError || !overview) {
    return (
      <div className="p-8 flex flex-col items-center justify-center text-center space-y-4 max-w-lg mx-auto" data-testid="dashboard-error">
        <div className="h-12 w-12 rounded-full bg-rose-500/10 text-rose-600 flex items-center justify-center">
          <AlertCircle className="w-6 h-6" />
        </div>
        <div className="space-y-1">
          <h3 className="text-base font-bold text-foreground">{t.error}</h3>
          <p className="text-xs text-muted-foreground leading-relaxed">
            {error?.message || 'Server bilan aloqa uzildi. Iltimos qayta urinib koʻring.'}
          </p>
        </div>
        <Button variant="default" size="sm" onClick={() => refetch()} className="gap-2">
          <RotateCcw className="w-3.5 h-3.5" />
          <span>{t.retry}</span>
        </Button>
      </div>
    )
  }

  const roleLabel = user?.role === 'FARMER'
    ? t.roleFarmer
    : user?.role === 'OPERATOR'
    ? t.roleOperator
    : t.roleAdmin

  return (
    <div className="space-y-6 pb-12 animate-in fade-in duration-300" data-testid="dashboard-workspace">
      {/* 1. Header & Welcome Workspace Banner */}
      <div className="relative overflow-hidden rounded-2xl border border-primary/20 bg-gradient-to-br from-card via-card to-primary/5 p-5 sm:p-6 shadow-xs">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="space-y-1.5">
            <div className="flex items-center gap-2 flex-wrap">
              <Badge variant="outline" className="bg-primary/10 text-primary border-primary/20 gap-1 text-[11px] font-semibold">
                <Sparkles className="h-3 w-3" />
                <span>Yangiariq Aqua AI • Farm Intelligence</span>
              </Badge>
              <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20 text-[11px] gap-1 font-medium">
                <ShieldCheck className="h-3 w-3" />
                <span>{roleLabel}</span>
              </Badge>
              {overview.tenantScope && (
                <span className="text-[11px] text-muted-foreground font-mono hidden sm:inline">
                  [{overview.tenantScope}]
                </span>
              )}
            </div>

            <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-foreground">
              {t.welcome}, {user?.firstName || 'Fermer'}!
            </h1>
            <p className="text-xs sm:text-sm text-muted-foreground max-w-2xl leading-relaxed">
              {t.subtitle}
            </p>
          </div>

          {/* Quick Action Navigation & Refresh */}
          <div className="flex items-center gap-2 flex-wrap">
            <Button
              variant="default"
              size="sm"
              onClick={() => onNavigate('gis')}
              className="gap-1.5 shadow-xs text-xs"
            >
              <Map className="h-3.5 w-3.5" />
              <span>{t.quickNavGis}</span>
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigate('irrigation')}
              className="gap-1.5 text-xs"
            >
              <Droplets className="h-3.5 w-3.5 text-sky-500" />
              <span>{t.quickNavIrrigation}</span>
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigate('ai-advisor')}
              className="gap-1.5 text-xs"
            >
              <Sparkles className="h-3.5 w-3.5 text-primary" />
              <span>{t.quickNavAdvisor}</span>
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigate('analytics')}
              className="gap-1.5 text-xs border-blue-200 dark:border-blue-900 text-blue-700 dark:text-blue-300 bg-blue-50/50 dark:bg-blue-950/30 hover:bg-blue-100"
            >
              <BarChart3 className="h-3.5 w-3.5 text-blue-600 dark:text-blue-400" />
              <span>{lang === 'uz' ? 'Tahlil va Statistika' : lang === 'ru' ? 'Аналитика' : 'Analytics'}</span>
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigate('risks')}
              className="gap-1.5 text-xs border-amber-200 dark:border-amber-900 text-amber-700 dark:text-amber-300 bg-amber-50/50 dark:bg-amber-950/30 hover:bg-amber-100"
            >
              <AlertOctagon className="h-3.5 w-3.5 text-amber-600 dark:text-amber-400" />
              <span>{lang === 'uz' ? 'Xavf Intellekti' : lang === 'ru' ? 'Интеллект Рисков' : 'Risk Intelligence'}</span>
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigate('network')}
              className="gap-1.5 text-xs border-sky-200 dark:border-sky-900 text-sky-700 dark:text-sky-300 bg-sky-50/50 dark:bg-sky-950/30 hover:bg-sky-100"
            >
              <Waves className="h-3.5 w-3.5 text-sky-600 dark:text-sky-400" />
              <span>{lang === 'uz' ? 'Suv Tarmogʻi' : lang === 'ru' ? 'Водная Сеть' : 'Water Network'}</span>
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigate('water-accounting')}
              className="gap-1.5 text-xs border-indigo-200 dark:border-indigo-900 text-indigo-700 dark:text-indigo-300 bg-indigo-50/50 dark:bg-indigo-950/30 hover:bg-indigo-100"
            >
              <Scale className="h-3.5 w-3.5 text-indigo-600 dark:text-indigo-400" />
              <span>{lang === 'uz' ? 'Suv Hisobi' : lang === 'ru' ? 'Водный Учет' : 'Water Accounting'}</span>
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8 text-muted-foreground hover:text-foreground"
              title={`${t.refresh} (${t.lastUpdated}: ${formatTime(overview.generatedAt)})`}
              onClick={() => refetch()}
            >
              <RotateCcw className={`h-3.5 w-3.5 ${isFetching ? 'animate-spin' : ''}`} />
            </Button>
          </div>
        </div>
      </div>

      {/* 2. Priority-First: Action Center */}
      <section data-testid="section-action-center">
        <ActionCenter
          urgentActions={overview.urgentActions}
          lang={lang}
          onNavigate={onNavigate}
        />
      </section>

      {/* 2.1 Farm Risk & Resilience Intelligence (Milestone 08.13) */}
      <section data-testid="section-risk-intelligence">
        <FarmRiskDashboardCard
          lang={lang}
          onNavigate={onNavigate}
        />
      </section>

      {/* 2.5 Farm Performance Summary (Milestone 08.6) */}
      <section data-testid="section-farm-performance-summary">
        <FarmPerformanceSummaryCard
          lang={lang}
          onNavigate={onNavigate}
        />
      </section>

      {/* 2.6 Predictive Farm Intelligence Summary (Milestone 08.7) */}
      <section data-testid="section-predictive-summary">
        <PredictiveSummaryCard
          lang={lang}
          onNavigate={onNavigate}
        />
      </section>

      {/* 2.7 Compact Predictive Model Health (Milestone 08.9) */}
      <section data-testid="section-predictive-health">
        <PredictiveModelHealthCard
          predictiveHealth={overview.predictiveHealth}
          lang={lang}
          onOpenGovernance={() => onNavigate('predictions')}
        />
      </section>

      {/* 3. Farm Overview: 5-Metric Aggregation Cards */}
      <section data-testid="section-farm-metrics">
        <FarmOverviewMetrics
          fieldMetrics={overview.fieldMetrics}
          waterMetrics={overview.waterMetrics}
          sensorMetrics={overview.sensorMetrics}
          anomalyMetrics={overview.anomalyMetrics}
          irrigationMetrics={overview.irrigationMetrics}
          lang={lang}
          onNavigate={onNavigate}
        />
      </section>

      {/* 4. Priority Parcels Matrix: Explainable Prioritization */}
      <section data-testid="section-priority-fields">
        <PriorityFieldsTable
          fields={overview.priorityFields}
          lang={lang}
          onNavigate={onNavigate}
        />
      </section>

      {/* 5. Water Budget & Operations Lifecycles */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          {/* Water Budget Visualization */}
          <section data-testid="section-water-budget">
            <WaterBudgetOverviewCard
              waterBudget={overview.waterBudget}
              lang={lang}
              onNavigate={onNavigate}
            />
          </section>

          {/* Irrigation Lifecycle Tracker */}
          <section data-testid="section-irrigation-lifecycle">
            <IrrigationLifecycleTracker
              metrics={overview.irrigationMetrics}
              lang={lang}
              onNavigate={onNavigate}
            />
          </section>
        </div>

        {/* Right Column: Real-time Operational Activity Stream */}
        <div className="space-y-6">
          <section data-testid="section-recent-activity">
            <RecentActivityFeed
              activity={overview.recentActivity}
              lang={lang}
            />
          </section>
        </div>
      </div>

      {/* 6. Integrated Spatial Cartography (Reusing Leaflet GIS Map) */}
      <section data-testid="section-gis-map">
        <DashboardFieldMap
          lang={lang}
          onNavigate={onNavigate}
        />
      </section>
    </div>
  )
}
