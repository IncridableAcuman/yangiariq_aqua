import React from 'react'
import {
  ResponsiveContainer,
  ComposedChart,
  Bar,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts'
import { TrendingUp, BarChart2 } from 'lucide-react'
import { PerformanceTrendPointDto, TrendGranularity } from '@/entities/analytics'
import { SupportedLanguage } from '@/shared/i18n'

interface PerformanceTrendChartsProps {
  trendPoints: PerformanceTrendPointDto[]
  granularity: TrendGranularity
  onGranularityChange: (g: TrendGranularity) => void
  lang: SupportedLanguage
}

export const PerformanceTrendCharts: React.FC<PerformanceTrendChartsProps> = ({
  trendPoints,
  granularity,
  onGranularityChange,
  lang,
}) => {
  const t = {
    uz: {
      waterTitle: 'Suv sarfi dinamikasi (Amalda va Rejalashtirilgan)',
      opsTitle: 'Sugʻorish amallari va telemetriya sifati dinamikasi',
      daily: 'Kunlik',
      weekly: 'Haftalik',
      monthly: 'Oylik',
      waterConsumed: 'Haqiqiy sarf (m³)',
      waterPlanned: 'Rejalashtirilgan (m³)',
      completedOps: 'Bajarilgan amallar',
      telemetryCoverage: 'Telemetriya qamrovi (%)',
      safetyEvents: 'Xavfsizlik hodisalari',
      noData: 'Tanlangan davr uchun dinamika maʼlumotlari mavjud emas.',
    },
    ru: {
      waterTitle: 'Динамика расхода воды (Факт и План)',
      opsTitle: 'Динамика поливов и качества телеметрии',
      daily: 'По дням',
      weekly: 'По неделям',
      monthly: 'По месяцам',
      waterConsumed: 'Факт расход (м³)',
      waterPlanned: 'План расход (м³)',
      completedOps: 'Выполнено поливов',
      telemetryCoverage: 'Полнота телеметрии (%)',
      safetyEvents: 'Инциденты безопасности',
      noData: 'Нет данных динамики за выбранный период.',
    },
    en: {
      waterTitle: 'Water Delivery Dynamics (Actual vs Planned)',
      opsTitle: 'Operations & Telemetry Reliability Dynamics',
      daily: 'Daily',
      weekly: 'Weekly',
      monthly: 'Monthly',
      waterConsumed: 'Actual Consumed (m³)',
      waterPlanned: 'Planned Volume (m³)',
      completedOps: 'Completed Operations',
      telemetryCoverage: 'Telemetry Coverage (%)',
      safetyEvents: 'Safety Incidents',
      noData: 'No trend data available for the selected period.',
    },
  }[lang]

  const chartData = trendPoints.map((p) => ({
    date: p.dateLabel,
    consumed: p.waterConsumedM3,
    planned: p.waterPlannedM3,
    ops: p.completedOperationsCount,
    telemetry: p.telemetryCoveragePercent ?? 0,
    safety: p.safetyEventsCount,
  }))

  return (
    <div className="space-y-6">
      {/* Chart 1: Water Delivery Dynamics */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <div className="p-2 rounded-lg bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400">
              <TrendingUp className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-semibold text-slate-900 dark:text-white">{t.waterTitle}</h3>
              <span className="text-xs text-slate-500 dark:text-slate-400">
                {trendPoints.length} nuqta
              </span>
            </div>
          </div>

          {/* Granularity Selector */}
          <div className="inline-flex rounded-lg border border-slate-200 dark:border-slate-800 p-0.5 bg-slate-50 dark:bg-slate-800/60">
            {(['DAILY', 'WEEKLY', 'MONTHLY'] as TrendGranularity[]).map((g) => (
              <button
                key={g}
                onClick={() => onGranularityChange(g)}
                className={`px-2.5 py-1 text-xs font-medium rounded-md transition-colors ${
                  granularity === g
                    ? 'bg-white dark:bg-slate-700 text-blue-600 dark:text-blue-400 shadow-sm font-semibold'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
                }`}
              >
                {g === 'DAILY' ? t.daily : g === 'WEEKLY' ? t.weekly : t.monthly}
              </button>
            ))}
          </div>
        </div>

        {chartData.length === 0 ? (
          <div className="h-64 flex items-center justify-center text-xs text-slate-400">
            {t.noData}
          </div>
        ) : (
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={chartData} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" className="dark:stroke-slate-800" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} stroke="#94a3b8" />
                <YAxis tick={{ fontSize: 11 }} stroke="#94a3b8" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#1e293b',
                    borderColor: '#334155',
                    color: '#f8fafc',
                    borderRadius: '8px',
                    fontSize: '12px',
                  }}
                />
                <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '8px' }} />
                <Bar dataKey="consumed" name={t.waterConsumed} fill="#3b82f6" radius={[4, 4, 0, 0]} />
                <Line
                  type="monotone"
                  dataKey="planned"
                  name={t.waterPlanned}
                  stroke="#10b981"
                  strokeWidth={2}
                  dot={{ r: 3 }}
                />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      {/* Chart 2: Operations & Telemetry Quality Dynamics */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-lg bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400">
            <BarChart2 className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-base font-semibold text-slate-900 dark:text-white">{t.opsTitle}</h3>
            <span className="text-xs text-slate-500 dark:text-slate-400">
              {t.completedOps} & {t.telemetryCoverage}
            </span>
          </div>
        </div>

        {chartData.length === 0 ? (
          <div className="h-64 flex items-center justify-center text-xs text-slate-400">
            {t.noData}
          </div>
        ) : (
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={chartData} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" className="dark:stroke-slate-800" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} stroke="#94a3b8" />
                <YAxis yAxisId="left" tick={{ fontSize: 11 }} stroke="#94a3b8" />
                <YAxis yAxisId="right" orientation="right" domain={[0, 100]} tick={{ fontSize: 11 }} stroke="#94a3b8" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#1e293b',
                    borderColor: '#334155',
                    color: '#f8fafc',
                    borderRadius: '8px',
                    fontSize: '12px',
                  }}
                />
                <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '8px' }} />
                <Bar yAxisId="left" dataKey="ops" name={t.completedOps} fill="#6366f1" radius={[4, 4, 0, 0]} />
                <Bar yAxisId="left" dataKey="safety" name={t.safetyEvents} fill="#f43f5e" radius={[4, 4, 0, 0]} />
                <Line
                  yAxisId="right"
                  type="monotone"
                  dataKey="telemetry"
                  name={t.telemetryCoverage}
                  stroke="#06b6d4"
                  strokeWidth={2}
                  dot={{ r: 3 }}
                />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>
    </div>
  )
}
