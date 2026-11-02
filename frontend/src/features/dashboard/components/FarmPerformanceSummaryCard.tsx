import React from 'react'
import {
  TrendingUp,
  TrendingDown,
  Droplet,
  CalendarCheck,
  Activity,
  ShieldCheck,
  ArrowRight,
  AlertTriangle,
} from 'lucide-react'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { SupportedLanguage } from '@/shared/i18n'
import { useAnalyticsOverview } from '@/features/analytics'
import { NavSection } from '@/widgets/sidebar'

interface FarmPerformanceSummaryCardProps {
  lang: SupportedLanguage
  onNavigate: (section: NavSection, fieldId?: string) => void
}

export const FarmPerformanceSummaryCard: React.FC<FarmPerformanceSummaryCardProps> = ({
  lang,
  onNavigate,
}) => {
  const { data: analytics, isLoading } = useAnalyticsOverview({ period: 'LAST_30_DAYS' })

  const t = {
    uz: {
      title: 'Fermer xoʻjaligi samaradorligi',
      period: 'Oxirgi 30 kunlik koʻrsatkichlar',
      waterUtil: 'Suv kvotasi sarfi',
      planAdherence: 'Reja intizomi',
      telemetry: 'Telemetriya qamrovi',
      verification: 'Natija tekshiruvi',
      viewAnalytics: 'Batafsil tahlilni koʻrish',
      vsPrevious: 'oldingi davrga nisbatan',
      noData: 'Mavjud emas',
      dataWarning: 'Telemetriya toʻliq emas',
    },
    ru: {
      title: 'Эффективность хозяйства',
      period: 'Показатели за последние 30 дней',
      waterUtil: 'Использование квоты',
      planAdherence: 'Дисциплина планов',
      telemetry: 'Охват телеметрии',
      verification: 'Верификация поливов',
      viewAnalytics: 'Подробная аналитика',
      vsPrevious: 'к прошлому периоду',
      noData: 'Нет данных',
      dataWarning: 'Телеметрия неполная',
    },
    en: {
      title: 'Farm Performance',
      period: 'Last 30 days performance indicators',
      waterUtil: 'Water Utilization',
      planAdherence: 'Plan Adherence',
      telemetry: 'Telemetry Coverage',
      verification: 'Verification Coverage',
      viewAnalytics: 'View detailed analytics',
      vsPrevious: 'vs previous period',
      noData: 'N/A',
      dataWarning: 'Incomplete telemetry',
    },
  }[lang]

  if (isLoading) {
    return (
      <div className="rounded-xl border border-border bg-card p-5 shadow-xs animate-pulse">
        <div className="h-6 w-48 bg-muted rounded mb-4" />
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-20 bg-muted/60 rounded-lg" />
          ))}
        </div>
      </div>
    )
  }

  const waterUtil = analytics?.waterPerformance?.utilizationPercent
  const planAdherence = analytics?.irrigationPerformance?.planAdherencePercent
  const telemetryCov =
    analytics?.telemetryQuality?.validReadingPercent ??
    analytics?.telemetryQuality?.telemetryCoveragePercent
  const verificationCov = analytics?.verificationPerformance?.verificationCoveragePercent

  const waterDelta = analytics?.periodComparison?.hasComparisonData
    ? analytics?.periodComparison?.waterConsumedDeltaPercent
    : null

  const isDataIncomplete = analytics?.dataConfidence?.hasIncompleteData

  return (
    <div
      className="rounded-xl border border-blue-200/70 dark:border-blue-900/50 bg-gradient-to-r from-blue-50/40 via-card to-card p-5 shadow-xs space-y-4"
      data-testid="section-farm-performance-summary"
    >
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <h3 className="text-base font-bold tracking-tight text-foreground">{t.title}</h3>
            {analytics?.period?.label && (
              <Badge variant="outline" className="text-[11px] font-normal text-muted-foreground">
                {analytics.period.label}
              </Badge>
            )}
            {isDataIncomplete && (
              <Badge
                variant="outline"
                className="bg-amber-100 dark:bg-amber-950/40 text-amber-800 dark:text-amber-300 border-amber-300 text-[10px] gap-1"
              >
                <AlertTriangle className="w-3 h-3 text-amber-600" />
                <span>{t.dataWarning}</span>
              </Badge>
            )}
          </div>
          <p className="text-xs text-muted-foreground mt-0.5">{t.period}</p>
        </div>

        <Button
          variant="outline"
          size="sm"
          onClick={() => onNavigate('analytics')}
          className="text-xs font-semibold text-blue-600 dark:text-blue-400 border-blue-200 dark:border-blue-800 hover:bg-blue-50 dark:hover:bg-blue-950/40 gap-1.5 self-start sm:self-auto"
        >
          <span>{t.viewAnalytics}</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </Button>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {/* 1. Water Utilization */}
        <div className="bg-background/80 dark:bg-slate-900/60 p-3.5 rounded-lg border border-border/80 flex flex-col justify-between">
          <div className="flex items-center justify-between text-muted-foreground mb-1">
            <span className="text-xs font-medium">{t.waterUtil}</span>
            <Droplet className="w-4 h-4 text-blue-500" />
          </div>
          <div>
            <div className="text-xl font-bold text-foreground">
              {waterUtil !== null && waterUtil !== undefined ? `${waterUtil}%` : t.noData}
            </div>
            {waterDelta !== null && waterDelta !== undefined && (
              <div className="flex items-center gap-1 text-[11px] mt-0.5 text-muted-foreground">
                {waterDelta > 0 ? (
                  <span className="text-rose-500 font-medium inline-flex items-center">
                    <TrendingUp className="w-3 h-3 mr-0.5" />
                    +{waterDelta}%
                  </span>
                ) : (
                  <span className="text-emerald-500 font-medium inline-flex items-center">
                    <TrendingDown className="w-3 h-3 mr-0.5" />
                    {waterDelta}%
                  </span>
                )}
                <span className="truncate">{t.vsPrevious}</span>
              </div>
            )}
          </div>
        </div>

        {/* 2. Plan Adherence */}
        <div className="bg-background/80 dark:bg-slate-900/60 p-3.5 rounded-lg border border-border/80 flex flex-col justify-between">
          <div className="flex items-center justify-between text-muted-foreground mb-1">
            <span className="text-xs font-medium">{t.planAdherence}</span>
            <CalendarCheck className="w-4 h-4 text-emerald-500" />
          </div>
          <div>
            <div className="text-xl font-bold text-foreground">
              {planAdherence !== null && planAdherence !== undefined ? `${planAdherence}%` : t.noData}
            </div>
            <p className="text-[11px] text-muted-foreground mt-0.5">
              {analytics?.irrigationPerformance?.totalOperationsCompleted ??
                analytics?.irrigationPerformance?.completedCount ??
                0}{' '}
              bajarildi
            </p>
          </div>
        </div>

        {/* 3. Telemetry Coverage */}
        <div className="bg-background/80 dark:bg-slate-900/60 p-3.5 rounded-lg border border-border/80 flex flex-col justify-between">
          <div className="flex items-center justify-between text-muted-foreground mb-1">
            <span className="text-xs font-medium">{t.telemetry}</span>
            <Activity className="w-4 h-4 text-cyan-500" />
          </div>
          <div>
            <div className="text-xl font-bold text-foreground">
              {telemetryCov !== null && telemetryCov !== undefined ? `${telemetryCov}%` : t.noData}
            </div>
            <p className="text-[11px] text-muted-foreground mt-0.5">
              {analytics?.telemetryQuality?.onlineSensors ??
                analytics?.telemetryQuality?.activeSensors ??
                0}
              /
              {analytics?.telemetryQuality?.totalSensors ?? 0} faol
            </p>
          </div>
        </div>

        {/* 4. Verification Coverage */}
        <div className="bg-background/80 dark:bg-slate-900/60 p-3.5 rounded-lg border border-border/80 flex flex-col justify-between">
          <div className="flex items-center justify-between text-muted-foreground mb-1">
            <span className="text-xs font-medium">{t.verification}</span>
            <ShieldCheck className="w-4 h-4 text-indigo-500" />
          </div>
          <div>
            <div className="text-xl font-bold text-foreground">
              {verificationCov !== null && verificationCov !== undefined
                ? `${verificationCov}%`
                : t.noData}
            </div>
            <p className="text-[11px] text-muted-foreground mt-0.5">
              {analytics?.verificationPerformance?.verifiedOperationsCount ?? 0} tasdiqlandi
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
