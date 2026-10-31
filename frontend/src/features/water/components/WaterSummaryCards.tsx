import React from 'react'
import { Card, CardContent } from '@/shared/ui/card'
import { Droplet, Gauge, AlertTriangle, ShieldCheck, Waves } from 'lucide-react'
import { formatVolume, formatPercentage, calculateFillPercent } from '../lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { cn } from '@/shared/lib/utils'

interface WaterSummaryCardsProps {
  totalAllocatedVolumeM3: number
  totalConsumedVolumeM3: number
  totalRemainingVolumeM3: number
  totalOverConsumedVolumeM3: number
  isOverConsumed: boolean
  activeQuotaCount?: number
  completedEventsCount?: number
  lang: SupportedLanguage
  className?: string
}

export const WaterSummaryCards: React.FC<WaterSummaryCardsProps> = ({
  totalAllocatedVolumeM3,
  totalConsumedVolumeM3,
  totalRemainingVolumeM3,
  totalOverConsumedVolumeM3,
  isOverConsumed,
  activeQuotaCount = 0,
  completedEventsCount = 0,
  lang,
  className,
}) => {
  const t = translations[lang].water
  const utilization =
    totalAllocatedVolumeM3 > 0
      ? (totalConsumedVolumeM3 / totalAllocatedVolumeM3) * 100
      : 0
  const fillPercent = calculateFillPercent(totalConsumedVolumeM3, totalAllocatedVolumeM3)

  return (
    <div className={cn('space-y-4', className)}>
      {isOverConsumed && (
        <div
          role="alert"
          className="flex items-center gap-3 p-4 rounded-lg bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 text-rose-800 dark:text-rose-200"
        >
          <AlertTriangle className="w-5 h-5 text-rose-600 dark:text-rose-400 shrink-0" />
          <div className="flex-1 text-sm font-medium">
            <span>{t.stats.overQuotaAlert}</span>
            <span className="ml-2 font-semibold underline">
              +{formatVolume(totalOverConsumedVolumeM3)}
            </span>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Allocated */}
        <Card className="border-border/60 shadow-sm relative overflow-hidden bg-card">
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {t.stats.totalAllocated}
              </span>
              <div className="p-2 rounded-lg bg-sky-100 dark:bg-sky-950/60 text-sky-600 dark:text-sky-400">
                <Droplet className="w-4 h-4" />
              </div>
            </div>
            <div className="mt-3">
              <div className="text-2xl font-bold tracking-tight text-foreground">
                {formatVolume(totalAllocatedVolumeM3)}
              </div>
              <p className="mt-1 text-xs text-muted-foreground flex items-center gap-1.5">
                <span className="font-semibold text-foreground">{activeQuotaCount}</span>
                <span>{t.stats.activeQuotas}</span>
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Total Consumed */}
        <Card className="border-border/60 shadow-sm relative overflow-hidden bg-card">
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {t.stats.totalConsumed}
              </span>
              <div className="p-2 rounded-lg bg-indigo-100 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400">
                <Waves className="w-4 h-4" />
              </div>
            </div>
            <div className="mt-3">
              <div className="text-2xl font-bold tracking-tight text-foreground">
                {formatVolume(totalConsumedVolumeM3)}
              </div>
              <p className="mt-1 text-xs text-muted-foreground flex items-center gap-1.5">
                <span className="font-semibold text-foreground">{completedEventsCount}</span>
                <span>{t.stats.completedEvents}</span>
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Remaining Balance */}
        <Card className="border-border/60 shadow-sm relative overflow-hidden bg-card">
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {t.stats.remainingBalance}
              </span>
              <div className="p-2 rounded-lg bg-emerald-100 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400">
                <ShieldCheck className="w-4 h-4" />
              </div>
            </div>
            <div className="mt-3">
              <div className="text-2xl font-bold tracking-tight text-emerald-600 dark:text-emerald-400">
                {formatVolume(totalRemainingVolumeM3)}
              </div>
              <div className="mt-2 w-full">
                <div className="flex justify-between text-xs text-muted-foreground mb-1">
                  <span>{t.stats.utilization}</span>
                  <span className="font-semibold text-foreground">
                    {formatPercentage(utilization)}
                  </span>
                </div>
                <div className="w-full h-1.5 bg-muted rounded-full overflow-hidden">
                  <div
                    className={cn(
                      'h-full rounded-full transition-all',
                      isOverConsumed
                        ? 'bg-rose-500'
                        : fillPercent >= 80
                          ? 'bg-amber-500'
                          : 'bg-emerald-500'
                    )}
                    style={{ width: `${fillPercent}%` }}
                  />
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Over-Consumption Status */}
        <Card
          className={cn(
            'border-border/60 shadow-sm relative overflow-hidden bg-card',
            isOverConsumed && 'border-rose-300 dark:border-rose-900/60 bg-rose-50/20 dark:bg-rose-950/10'
          )}
        >
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {t.stats.overConsumption}
              </span>
              <div
                className={cn(
                  'p-2 rounded-lg',
                  isOverConsumed
                    ? 'bg-rose-100 dark:bg-rose-950/60 text-rose-600 dark:text-rose-400'
                    : 'bg-teal-100 dark:bg-teal-950/60 text-teal-600 dark:text-teal-400'
                )}
              >
                {isOverConsumed ? (
                  <AlertTriangle className="w-4 h-4" />
                ) : (
                  <Gauge className="w-4 h-4" />
                )}
              </div>
            </div>
            <div className="mt-3">
              <div
                className={cn(
                  'text-2xl font-bold tracking-tight',
                  isOverConsumed
                    ? 'text-rose-600 dark:text-rose-400'
                    : 'text-foreground'
                )}
              >
                {formatVolume(totalOverConsumedVolumeM3)}
              </div>
              <p className="mt-1 text-xs">
                {isOverConsumed ? (
                  <span className="font-semibold text-rose-600 dark:text-rose-400">
                    {t.allocations.overQuotaBadge}
                  </span>
                ) : (
                  <span className="text-emerald-600 dark:text-emerald-400 font-medium">
                    {t.stats.withinQuota}
                  </span>
                )}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
