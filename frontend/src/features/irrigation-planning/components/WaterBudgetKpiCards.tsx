import React from 'react'
import { Card, CardContent } from '@/shared/ui/card'
import { Droplets, AlertTriangle, ShieldAlert, CheckCircle2, CalendarClock, Activity } from 'lucide-react'
import { formatVolume } from '@/features/water/lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { cn } from '@/shared/lib/utils'

interface WaterBudgetKpiCardsProps {
  totalAllocatedM3: number
  consumedM3: number
  committedM3: number
  reservedM3: number
  remainingAvailableM3: number
  requiringIrrigationCount?: number
  blockedCount?: number
  criticalCount?: number
  lang: SupportedLanguage
  className?: string
}

export const WaterBudgetKpiCards: React.FC<WaterBudgetKpiCardsProps> = ({
  totalAllocatedM3,
  consumedM3,
  committedM3,
  reservedM3,
  remainingAvailableM3,
  requiringIrrigationCount = 0,
  blockedCount = 0,
  criticalCount = 0,
  lang,
  className,
}) => {
  const t = translations[lang]
  const plannedWaterM3 = committedM3 + reservedM3
  const isBudgetLow = totalAllocatedM3 > 0 && remainingAvailableM3 / totalAllocatedM3 < 0.15

  return (
    <div className={cn('grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3', className)}>
      {/* 1. Total Allocated */}
      <Card className="border border-border/70 shadow-sm bg-card hover:border-primary/40 transition-colors">
        <CardContent className="p-4 flex flex-col justify-between h-full space-y-2">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium uppercase tracking-wider">
              {t.irrigationPlanning?.kpi?.totalAllocation || 'Mavsumiy limit'}
            </span>
            <div className="p-1.5 rounded-md bg-blue-500/10 text-blue-600 dark:text-blue-400">
              <Droplets className="w-4 h-4" />
            </div>
          </div>
          <div>
            <div className="text-xl font-bold text-foreground tracking-tight">
              {formatVolume(totalAllocatedM3)}
            </div>
            <p className="text-[11px] text-muted-foreground mt-0.5">
              {t.irrigationPlanning?.kpi?.totalAllocationDesc || 'Tasdiqlangan suv kvotasi'}
            </p>
          </div>
        </CardContent>
      </Card>

      {/* 2. Used / Consumed */}
      <Card className="border border-border/70 shadow-sm bg-card hover:border-primary/40 transition-colors">
        <CardContent className="p-4 flex flex-col justify-between h-full space-y-2">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium uppercase tracking-wider">
              {t.irrigationPlanning?.kpi?.consumedWater || 'Sarflangan suv'}
            </span>
            <div className="p-1.5 rounded-md bg-cyan-500/10 text-cyan-600 dark:text-cyan-400">
              <Activity className="w-4 h-4" />
            </div>
          </div>
          <div>
            <div className="text-xl font-bold text-foreground tracking-tight">
              {formatVolume(consumedM3)}
            </div>
            <p className="text-[11px] text-muted-foreground mt-0.5">
              {totalAllocatedM3 > 0
                ? `${((consumedM3 / totalAllocatedM3) * 100).toFixed(1)}% ${t.irrigationPlanning?.kpi?.ofTotal || 'sarflandi'}`
                : 'Haqiqiy isteʼmol'}
            </p>
          </div>
        </CardContent>
      </Card>

      {/* 3. Planned / Committed */}
      <Card className="border border-border/70 shadow-sm bg-card hover:border-primary/40 transition-colors">
        <CardContent className="p-4 flex flex-col justify-between h-full space-y-2">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium uppercase tracking-wider">
              {t.irrigationPlanning?.kpi?.plannedWater || 'Rejalashtirilgan'}
            </span>
            <div className="p-1.5 rounded-md bg-amber-500/10 text-amber-600 dark:text-amber-400">
              <CalendarClock className="w-4 h-4" />
            </div>
          </div>
          <div>
            <div className="text-xl font-bold text-foreground tracking-tight">
              {formatVolume(plannedWaterM3)}
            </div>
            <p className="text-[11px] text-muted-foreground mt-0.5">
              {t.irrigationPlanning?.kpi?.committedDesc || 'Kutilayotgan navbatlar'}
            </p>
          </div>
        </CardContent>
      </Card>

      {/* 4. Remaining Water */}
      <Card
        className={cn(
          'border shadow-sm transition-colors',
          isBudgetLow
            ? 'border-rose-400 dark:border-rose-900 bg-rose-50/20 dark:bg-rose-950/20'
            : 'border-border/70 bg-card hover:border-emerald-500/40'
        )}
      >
        <CardContent className="p-4 flex flex-col justify-between h-full space-y-2">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium uppercase tracking-wider">
              {t.irrigationPlanning?.kpi?.remainingWater || 'Qolgan limit'}
            </span>
            <div
              className={cn(
                'p-1.5 rounded-md',
                isBudgetLow
                  ? 'bg-rose-500/10 text-rose-600 dark:text-rose-400'
                  : 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400'
              )}
            >
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div>
            <div
              className={cn(
                'text-xl font-bold tracking-tight',
                isBudgetLow ? 'text-rose-600 dark:text-rose-400' : 'text-emerald-600 dark:text-emerald-400'
              )}
            >
              {formatVolume(remainingAvailableM3)}
            </div>
            <p className="text-[11px] text-muted-foreground mt-0.5">
              {isBudgetLow
                ? t.irrigationPlanning?.kpi?.lowQuotaWarning || 'Kritik qoldiq!'
                : t.irrigationPlanning?.kpi?.availableForPlanning || 'Rejalar uchun mavjud'}
            </p>
          </div>
        </CardContent>
      </Card>

      {/* 5. Field Operational Health / Safety KPI */}
      <Card
        className={cn(
          'border shadow-sm transition-colors',
          blockedCount > 0
            ? 'border-rose-400 dark:border-rose-900 bg-rose-50/30 dark:bg-rose-950/20'
            : criticalCount > 0
            ? 'border-amber-400 dark:border-amber-900 bg-amber-50/20 dark:bg-amber-950/20'
            : 'border-border/70 bg-card'
        )}
      >
        <CardContent className="p-4 flex flex-col justify-between h-full space-y-2">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium uppercase tracking-wider">
              {t.irrigationPlanning?.kpi?.operationalStatus || 'Operatsion holat'}
            </span>
            <div
              className={cn(
                'p-1.5 rounded-md',
                blockedCount > 0
                  ? 'bg-rose-500/10 text-rose-600 dark:text-rose-400 animate-pulse'
                  : 'bg-purple-500/10 text-purple-600 dark:text-purple-400'
              )}
            >
              {blockedCount > 0 ? (
                <ShieldAlert className="w-4 h-4" />
              ) : (
                <AlertTriangle className="w-4 h-4" />
              )}
            </div>
          </div>
          <div>
            <div className="text-xl font-bold text-foreground tracking-tight flex items-baseline gap-2">
              <span>{requiringIrrigationCount}</span>
              <span className="text-xs font-normal text-muted-foreground">
                {t.irrigationPlanning?.kpi?.fieldsNeedingWater || 'dala navbatda'}
              </span>
            </div>
            <div className="flex items-center gap-2 mt-0.5 text-[11px]">
              {blockedCount > 0 && (
                <span className="font-semibold text-rose-600 dark:text-rose-400">
                  {blockedCount} {t.irrigationPlanning?.kpi?.blocked || 'bloklangan'}
                </span>
              )}
              {criticalCount > 0 && (
                <span className="font-semibold text-amber-600 dark:text-amber-400">
                  {criticalCount} {t.irrigationPlanning?.kpi?.critical || 'shoshilinch'}
                </span>
              )}
              {blockedCount === 0 && criticalCount === 0 && (
                <span className="text-emerald-600 dark:text-emerald-400 font-medium">
                  {t.irrigationPlanning?.kpi?.systemNormal || 'Tizim barqaror'}
                </span>
              )}
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
