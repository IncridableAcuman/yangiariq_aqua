import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Droplets, ArrowRight, HelpCircle, AlertTriangle, CheckCircle2 } from 'lucide-react'
import { WaterBudgetSummary } from '@/entities/dashboard'
import { SupportedLanguage } from '@/shared/i18n'
import { NavSection } from '@/widgets/sidebar'

interface WaterBudgetOverviewCardProps {
  waterBudget: WaterBudgetSummary
  lang: SupportedLanguage
  onNavigate: (section: NavSection) => void
}

export const WaterBudgetOverviewCard: React.FC<WaterBudgetOverviewCardProps> = ({
  waterBudget,
  lang,
  onNavigate,
}) => {
  const t = {
    uz: {
      title: 'Mavsumiy Suv Balansi (Water Budget)',
      subtitle: 'Ajratilgan kvota, haqiqiy sarf, rejadagi taqsimot va mavjud suv hajmi',
      formulaTitle: 'Hisoblash formulasi:',
      formula: 'Qolgan = Ajratilgan - Sarflangan - Tasdiqlangan - Band qilingan',
      allocated: 'Ajratilgan Kvota',
      consumed: 'Haqiqiy Sarflangan',
      committed: 'Jarayondagi / Rejalangan',
      reserved: 'Tasdiqlangan / Kutilayotgan',
      remaining: 'Mavjud Qoldiq',
      exhausted: 'KVOTA TUGAGAN',
      available: 'Mavjud zaxira yetarli',
      manageQuota: 'Suv hisobini boshqarish',
    },
    ru: {
      title: 'Сезонный Водный Баланс (Water Budget)',
      subtitle: 'Выделенный лимит, фактический расход, запланированный объем и доступный остаток',
      formulaTitle: 'Формула расчета:',
      formula: 'Остаток = Выделено - Израсходовано - Запланировано - Зарезервировано',
      allocated: 'Выделенный Лимит',
      consumed: 'Фактический Расход',
      committed: 'В Процессе / В Графике',
      reserved: 'Одобрено / В Резерве',
      remaining: 'Доступный Остаток',
      exhausted: 'ЛИМИТ ИСЧЕРПАН',
      available: 'Запас воды достаточен',
      manageQuota: 'Управление квотами',
    },
    en: {
      title: 'Seasonal Water Budget',
      subtitle: 'Allocated volume, metered intake, scheduled delivery, and available headroom',
      formulaTitle: 'Calculation rule:',
      formula: 'Remaining = Allocated - Consumed - Committed - Reserved',
      allocated: 'Total Allocated',
      consumed: 'Actual Consumed',
      committed: 'Committed / Scheduled',
      reserved: 'Reserved / Approved',
      remaining: 'Remaining Available',
      exhausted: 'BUDGET DEPLETED',
      available: 'Water budget sufficient',
      manageQuota: 'Manage Allocations',
    },
  }[lang]

  const total = waterBudget.totalAllocatedM3 > 0 ? waterBudget.totalAllocatedM3 : 1
  const pctConsumed = Math.min(100, (waterBudget.consumedM3 / total) * 100)
  const pctCommitted = Math.min(100, (waterBudget.committedM3 / total) * 100)
  const pctReserved = Math.min(100, (waterBudget.reservedM3 / total) * 100)
  const pctRemaining = Math.max(0, 100 - (pctConsumed + pctCommitted + pctReserved))

  return (
    <Card className="shadow-xs border-border/80">
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="space-y-0.5">
            <CardTitle className="text-base font-bold text-foreground flex items-center gap-2">
              <Droplets className="w-5 h-5 text-sky-500" />
              <span>{t.title}</span>
            </CardTitle>
            <CardDescription className="text-xs">
              {t.subtitle}
            </CardDescription>
          </div>

          <div className="flex items-center gap-2">
            {waterBudget.isExhausted ? (
              <Badge variant="outline" className="bg-rose-500/15 text-rose-700 border-rose-400 font-bold text-[10px] py-0.5 px-2">
                <AlertTriangle className="w-3.5 h-3.5 mr-1" />
                {t.exhausted}
              </Badge>
            ) : (
              <Badge variant="outline" className="bg-emerald-500/15 text-emerald-700 border-emerald-400 font-semibold text-[10px] py-0.5 px-2">
                <CheckCircle2 className="w-3.5 h-3.5 mr-1" />
                {t.available}
              </Badge>
            )}
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-5 pt-4">
        {/* Multi-segment Progress Bar */}
        <div className="space-y-2">
          <div className="h-4 w-full bg-muted/60 rounded-full overflow-hidden flex shadow-inner">
            <div
              className="bg-primary h-full transition-all"
              style={{ width: `${pctConsumed}%` }}
              title={`Consumed: ${waterBudget.consumedM3} m³ (${pctConsumed.toFixed(1)}%)`}
            />
            <div
              className="bg-amber-500 h-full transition-all"
              style={{ width: `${pctCommitted}%` }}
              title={`Committed: ${waterBudget.committedM3} m³ (${pctCommitted.toFixed(1)}%)`}
            />
            <div
              className="bg-sky-400 h-full transition-all"
              style={{ width: `${pctReserved}%` }}
              title={`Reserved: ${waterBudget.reservedM3} m³ (${pctReserved.toFixed(1)}%)`}
            />
            <div
              className="bg-emerald-500/40 h-full transition-all"
              style={{ width: `${pctRemaining}%` }}
              title={`Remaining: ${waterBudget.remainingAvailableM3} m³ (${pctRemaining.toFixed(1)}%)`}
            />
          </div>

          {/* Legend */}
          <div className="flex items-center justify-between text-[11px] text-muted-foreground flex-wrap gap-2 pt-1 font-mono">
            <div className="flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded-xs bg-primary inline-block" />
              <span>{t.consumed} ({pctConsumed.toFixed(0)}%)</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded-xs bg-amber-500 inline-block" />
              <span>{t.committed} ({pctCommitted.toFixed(0)}%)</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded-xs bg-sky-400 inline-block" />
              <span>{t.reserved} ({pctReserved.toFixed(0)}%)</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded-xs bg-emerald-500 inline-block" />
              <span className="text-foreground font-bold">{t.remaining} ({pctRemaining.toFixed(0)}%)</span>
            </div>
          </div>
        </div>

        {/* 5-Column Volumetric Stats Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-3 p-3 rounded-xl bg-muted/40 border border-border/70 text-center">
          <div className="space-y-0.5">
            <span className="text-[11px] text-muted-foreground uppercase font-semibold">{t.allocated}</span>
            <div className="text-base sm:text-lg font-bold font-mono text-foreground">
              {Math.round(waterBudget.totalAllocatedM3).toLocaleString()} <span className="text-xs font-normal text-muted-foreground">m³</span>
            </div>
          </div>

          <div className="space-y-0.5">
            <span className="text-[11px] text-muted-foreground uppercase font-semibold">{t.consumed}</span>
            <div className="text-base sm:text-lg font-bold font-mono text-primary">
              {Math.round(waterBudget.consumedM3).toLocaleString()} <span className="text-xs font-normal text-muted-foreground">m³</span>
            </div>
          </div>

          <div className="space-y-0.5">
            <span className="text-[11px] text-muted-foreground uppercase font-semibold">{t.committed}</span>
            <div className="text-base sm:text-lg font-bold font-mono text-amber-600 dark:text-amber-400">
              {Math.round(waterBudget.committedM3).toLocaleString()} <span className="text-xs font-normal text-muted-foreground">m³</span>
            </div>
          </div>

          <div className="space-y-0.5">
            <span className="text-[11px] text-muted-foreground uppercase font-semibold">{t.reserved}</span>
            <div className="text-base sm:text-lg font-bold font-mono text-sky-600 dark:text-sky-400">
              {Math.round(waterBudget.reservedM3).toLocaleString()} <span className="text-xs font-normal text-muted-foreground">m³</span>
            </div>
          </div>

          <div className="col-span-2 sm:col-span-1 space-y-0.5 border-t sm:border-t-0 sm:border-l border-border/80 pt-2 sm:pt-0 sm:pl-2">
            <span className="text-[11px] text-muted-foreground uppercase font-bold text-emerald-600 dark:text-emerald-400">{t.remaining}</span>
            <div className={`text-base sm:text-lg font-bold font-mono ${
              waterBudget.isExhausted ? 'text-rose-600 dark:text-rose-400' : 'text-emerald-600 dark:text-emerald-400'
            }`}>
              {Math.round(waterBudget.remainingAvailableM3).toLocaleString()} <span className="text-xs font-normal text-muted-foreground">m³</span>
            </div>
          </div>
        </div>

        {/* Explain Formula & Action Link */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs text-muted-foreground pt-1">
          <div className="flex items-center gap-1.5 font-mono">
            <HelpCircle className="w-4 h-4 text-primary shrink-0" />
            <span>{t.formulaTitle} <strong className="text-foreground">{t.formula}</strong></span>
          </div>

          <Button
            variant="ghost"
            size="sm"
            className="text-xs text-primary hover:text-primary/90 self-start sm:self-auto gap-1"
            onClick={() => onNavigate('irrigation')}
          >
            <span>{t.manageQuota}</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}
