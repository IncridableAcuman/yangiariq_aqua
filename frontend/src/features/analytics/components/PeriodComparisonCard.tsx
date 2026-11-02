import React from 'react'
import { GitCompare, TrendingUp, TrendingDown, Minus, Info } from 'lucide-react'
import { PeriodComparisonDto } from '@/entities/analytics'
import { SupportedLanguage } from '@/shared/i18n'

interface PeriodComparisonCardProps {
  comparison?: PeriodComparisonDto
  lang: SupportedLanguage
}

export const PeriodComparisonCard: React.FC<PeriodComparisonCardProps> = ({ comparison, lang }) => {
  const t = {
    uz: {
      title: 'Oldingi davr bilan qiyosiy tahlil',
      previousPeriod: 'Taqqoslangan oldingi davr:',
      waterDelta: 'Suv sarfi oʻzgarishi',
      opsDelta: 'Sugʻorish amallari oʻzgarishi',
      insufficientData: 'Taqqoslash uchun oldingi davr maʼlumotlari yetarli emas.',
      increased: 'oshgan',
      decreased: 'kamaygan',
      unchanged: 'oʻzgarmagan',
      noComparison: 'Tarixiy maʼlumot yetarli emas',
    },
    ru: {
      title: 'Сравнительный анализ с предыдущим периодом',
      previousPeriod: 'Сравниваемый период:',
      waterDelta: 'Динамика расхода воды',
      opsDelta: 'Динамика операций полива',
      insufficientData: 'Недостаточно исторических данных за предыдущий период для корректного сравнения.',
      increased: 'увеличился',
      decreased: 'снизился',
      unchanged: 'без изменений',
      noComparison: 'Недостаточно данных',
    },
    en: {
      title: 'Period-over-Period Comparison',
      previousPeriod: 'Compared Previous Period:',
      waterDelta: 'Water Consumption Delta',
      opsDelta: 'Operations Delta',
      insufficientData: 'Insufficient historical data in the prior equivalent period for comparison.',
      increased: 'increased',
      decreased: 'decreased',
      unchanged: 'unchanged',
      noComparison: 'Insufficient Data',
    },
  }[lang]

  if (!comparison) return null

  if (!comparison.hasComparisonData) {
    return (
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm">
        <div className="flex items-center gap-2 mb-3">
          <div className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400">
            <GitCompare className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-base font-semibold text-slate-900 dark:text-white">{t.title}</h3>
            <span className="text-xs text-slate-400">{t.noComparison}</span>
          </div>
        </div>
        <div className="p-4 bg-slate-50 dark:bg-slate-800/40 rounded-lg text-xs text-slate-500 dark:text-slate-400 flex items-start gap-2">
          <Info className="w-4 h-4 text-slate-400 shrink-0 mt-0.5" />
          <p>{comparison.comparisonSummary || t.insufficientData}</p>
        </div>
      </div>
    )
  }

  const waterDelta = comparison.waterConsumedDeltaM3 ?? 0
  const waterPercent = comparison.waterConsumedDeltaPercent ?? 0
  const opsDelta = comparison.operationsCompletedDelta ?? 0

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-lg bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400">
            <GitCompare className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-base font-semibold text-slate-900 dark:text-white">{t.title}</h3>
            <span className="text-xs text-slate-500 dark:text-slate-400">
              {t.previousPeriod} <strong className="text-slate-700 dark:text-slate-300">{comparison.previousPeriodLabel}</strong>
            </span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {/* Water Delta */}
        <div className="bg-slate-50 dark:bg-slate-800/50 p-4 rounded-lg border border-slate-100 dark:border-slate-800 flex items-center justify-between">
          <div>
            <span className="text-xs text-slate-500 dark:text-slate-400">{t.waterDelta}</span>
            <div className="text-lg font-bold text-slate-900 dark:text-white mt-1">
              {waterDelta > 0 ? `+${waterDelta.toLocaleString()}` : waterDelta.toLocaleString()} m³
            </div>
            <span className="text-[11px] text-slate-400">
              ({waterPercent > 0 ? `+${waterPercent}` : waterPercent}%)
            </span>
          </div>
          <div>
            {waterDelta > 0 ? (
              <div className="p-2 rounded-full bg-rose-100 text-rose-700 dark:bg-rose-900/40 dark:text-rose-300">
                <TrendingUp className="w-5 h-5" />
              </div>
            ) : waterDelta < 0 ? (
              <div className="p-2 rounded-full bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-300">
                <TrendingDown className="w-5 h-5" />
              </div>
            ) : (
              <div className="p-2 rounded-full bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300">
                <Minus className="w-5 h-5" />
              </div>
            )}
          </div>
        </div>

        {/* Operations Delta */}
        <div className="bg-slate-50 dark:bg-slate-800/50 p-4 rounded-lg border border-slate-100 dark:border-slate-800 flex items-center justify-between">
          <div>
            <span className="text-xs text-slate-500 dark:text-slate-400">{t.opsDelta}</span>
            <div className="text-lg font-bold text-slate-900 dark:text-white mt-1">
              {opsDelta > 0 ? `+${opsDelta}` : opsDelta} amal
            </div>
            <span className="text-[11px] text-slate-400">
              oldingi davrga nisbatan
            </span>
          </div>
          <div>
            {opsDelta > 0 ? (
              <div className="p-2 rounded-full bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300">
                <TrendingUp className="w-5 h-5" />
              </div>
            ) : opsDelta < 0 ? (
              <div className="p-2 rounded-full bg-amber-100 text-amber-700 dark:bg-amber-900/40 dark:text-amber-300">
                <TrendingDown className="w-5 h-5" />
              </div>
            ) : (
              <div className="p-2 rounded-full bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300">
                <Minus className="w-5 h-5" />
              </div>
            )}
          </div>
        </div>
      </div>

      {comparison.comparisonSummary && (
        <div className="p-3 bg-slate-50 dark:bg-slate-800/40 border border-slate-200/60 dark:border-slate-700/60 rounded-lg text-xs text-slate-600 dark:text-slate-300">
          {comparison.comparisonSummary}
        </div>
      )}
    </div>
  )
}
