import React from 'react'
import { Droplets, AlertTriangle, CheckCircle2, TrendingUp, Info } from 'lucide-react'
import { WaterPerformanceDto } from '@/entities/analytics'
import { SupportedLanguage } from '@/shared/i18n'

interface WaterPerformanceCardProps {
  water?: WaterPerformanceDto
  lang: SupportedLanguage
}

export const WaterPerformanceCard: React.FC<WaterPerformanceCardProps> = ({ water, lang }) => {
  const t = {
    uz: {
      title: 'Suv sarfi va kvota tahlili',
      allocated: 'Ajratilgan limit (Quota)',
      consumed: 'Haqiqiy sarf (Consumed)',
      remaining: 'Qolgan limit (Remaining)',
      utilization: 'Limit sarfi (Utilization)',
      variance: 'Rejadan farq (Variance)',
      overConsumedFields: 'Limitdan oshgan dalalar:',
      excessVolume: 'Ortiqcha sarflangan hajm:',
      explanation: 'Xulosa va tavsiya:',
      normalTitle: 'Suv sarfi meʼyorda',
      warningTitle: 'Kvota tugash xavfi mavjud',
      overTitle: 'Mavsumiy limit oshirilgan',
      distinctionNotice:
        'Izoh: "Limit sarfi" (utilization) — ajratilgan mavsumiy kvotaga nisbatan sarf; "Rejadan chetlanish" (variance) — smart sugʻorish rejasiga nisbatan farq.',
    },
    ru: {
      title: 'Анализ расхода воды и квот',
      allocated: 'Утвержденный лимит (Quota)',
      consumed: 'Фактический расход (Consumed)',
      remaining: 'Остаток лимита (Remaining)',
      utilization: 'Использование лимита (Utilization)',
      variance: 'Отклонение от плана (Variance)',
      overConsumedFields: 'Полей с превышением лимита:',
      excessVolume: 'Объем перерасхода:',
      explanation: 'Заключение и рекомендация:',
      normalTitle: 'Расход воды в пределах нормы',
      warningTitle: 'Риск исчерпания квоты',
      overTitle: 'Сезонный лимит превышен',
      distinctionNotice:
        'Примечание: "Использование лимита" (utilization) — расход относительно сезонной квоты; "Отклонение" (variance) — разница относительно плана полива.',
    },
    en: {
      title: 'Water Consumption & Quota Performance',
      allocated: 'Allocated Quota',
      consumed: 'Actual Consumed',
      remaining: 'Remaining Quota',
      utilization: 'Quota Utilization',
      variance: 'Plan Variance',
      overConsumedFields: 'Fields with over-consumption:',
      excessVolume: 'Excess volume:',
      explanation: 'Assessment & guidance:',
      normalTitle: 'Water consumption within normal quota',
      warningTitle: 'Quota exhaustion risk',
      overTitle: 'Seasonal quota exceeded',
      distinctionNotice:
        'Governance note: "Quota Utilization" is consumed vs allocated seasonal limit; "Plan Variance" is actual delivery vs scheduled smart irrigation volume.',
    },
  }[lang]

  if (!water) return null

  const utilPercent = water.utilizationPercent ?? 0
  const isOver = utilPercent > 100
  const isWarning = utilPercent > 80 && !isOver

  const getStatusColor = () => {
    if (isOver) return 'text-rose-600 dark:text-rose-400'
    if (isWarning) return 'text-amber-600 dark:text-amber-400'
    return 'text-emerald-600 dark:text-emerald-400'
  }

  const getProgressColor = () => {
    if (isOver) return 'bg-rose-500'
    if (isWarning) return 'bg-amber-500'
    return 'bg-emerald-500'
  }

  const overFieldsCount = water.overConsumedFieldsCount ?? water.overConsumedFieldCount ?? 0
  const overVolume = water.overConsumedTotalM3 ?? water.overConsumedVolumeM3 ?? 0
  const statusNote = water.statusExplanation || water.contextualInterpretation

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm space-y-5">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-lg bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400">
            <Droplets className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-base font-semibold text-slate-900 dark:text-white">{t.title}</h3>
            <span className={`text-xs font-medium ${getStatusColor()}`}>
              {isOver ? t.overTitle : isWarning ? t.warningTitle : t.normalTitle}
            </span>
          </div>
        </div>

        {/* Status Badge */}
        <div className="flex items-center gap-1.5">
          {isOver ? (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-300">
              <AlertTriangle className="w-3.5 h-3.5" />
              OVER CONSUMED ({utilPercent}%)
            </span>
          ) : isWarning ? (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300">
              <AlertTriangle className="w-3.5 h-3.5" />
              WARNING ({utilPercent}%)
            </span>
          ) : (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300">
              <CheckCircle2 className="w-3.5 h-3.5" />
              NORMAL ({utilPercent}%)
            </span>
          )}
        </div>
      </div>

      {/* Quota Progress Bar */}
      <div className="space-y-1.5">
        <div className="flex justify-between text-xs text-slate-500 dark:text-slate-400">
          <span>{t.utilization}: <strong className="text-slate-800 dark:text-slate-200">{utilPercent}%</strong></span>
          <span>{t.allocated}: <strong className="text-slate-800 dark:text-slate-200">{(water.totalAllocatedM3 || 0).toLocaleString()} m³</strong></span>
        </div>
        <div className="w-full bg-slate-100 dark:bg-slate-800 rounded-full h-2.5 overflow-hidden">
          <div
            className={`h-2.5 rounded-full transition-all duration-500 ${getProgressColor()}`}
            style={{ width: `${Math.min(100, utilPercent)}%` }}
          />
        </div>
      </div>

      {/* Metric Breakdown Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2">
        <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-lg border border-slate-100 dark:border-slate-800">
          <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.consumed}</span>
          <div className="text-base font-bold text-slate-900 dark:text-white mt-0.5">
            {(water.totalConsumedM3 || 0).toLocaleString()} <span className="text-xs font-normal text-slate-400">m³</span>
          </div>
        </div>

        <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-lg border border-slate-100 dark:border-slate-800">
          <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.remaining}</span>
          <div className={`text-base font-bold mt-0.5 ${(water.totalRemainingM3 || 0) < 0 ? 'text-rose-600' : 'text-slate-900 dark:text-white'}`}>
            {(water.totalRemainingM3 || 0).toLocaleString()} <span className="text-xs font-normal text-slate-400">m³</span>
          </div>
        </div>

        <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-lg border border-slate-100 dark:border-slate-800">
          <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.variance}</span>
          <div className="text-base font-bold text-slate-900 dark:text-white mt-0.5 flex items-center gap-1">
            {water.variancePercent !== null && water.variancePercent !== undefined ? (
              <>
                <TrendingUp className="w-3.5 h-3.5 text-blue-500" />
                <span>{water.variancePercent > 0 ? `+${water.variancePercent}` : water.variancePercent}%</span>
              </>
            ) : (
              <span className="text-xs font-normal text-slate-400">—</span>
            )}
          </div>
        </div>

        <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-lg border border-slate-100 dark:border-slate-800">
          <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.overConsumedFields}</span>
          <div className="text-base font-bold text-slate-900 dark:text-white mt-0.5">
            <span className={overFieldsCount > 0 ? 'text-rose-600' : 'text-slate-900 dark:text-white'}>
              {overFieldsCount}
            </span>
            {overVolume > 0 && (
              <span className="text-xs font-normal text-rose-500 ml-1">
                (+{overVolume.toLocaleString()} m³)
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Explanation text */}
      {statusNote && (
        <div className="p-3 bg-blue-50/50 dark:bg-blue-950/20 border border-blue-100 dark:border-blue-900/40 rounded-lg text-xs text-blue-900 dark:text-blue-300 flex items-start gap-2">
          <Info className="w-4 h-4 text-blue-600 dark:text-blue-400 shrink-0 mt-0.5" />
          <p>{statusNote}</p>
        </div>
      )}

      {/* Governance footer note */}
      <div className="text-[11px] text-slate-400 dark:text-slate-500 italic">
        {t.distinctionNotice}
      </div>
    </div>
  )
}
