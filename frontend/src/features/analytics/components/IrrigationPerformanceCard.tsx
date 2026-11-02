import React from 'react'
import { CalendarCheck, Clock, CheckCircle2 } from 'lucide-react'
import { IrrigationPerformanceDto } from '@/entities/analytics'
import { SupportedLanguage } from '@/shared/i18n'

interface IrrigationPerformanceCardProps {
  irrigation?: IrrigationPerformanceDto
  lang: SupportedLanguage
}

export const IrrigationPerformanceCard: React.FC<IrrigationPerformanceCardProps> = ({
  irrigation,
  lang,
}) => {
  const t = {
    uz: {
      title: 'Sugʻorish operatsiyalari va reja intizomi',
      plansCreated: 'Tuzilgan rejalar',
      plansApproved: 'Tasdiqlangan',
      opsScheduled: 'Rejalashtirilgan amallar',
      opsCompleted: 'Bajarilgan amallar',
      adherenceRate: 'Reja intizomi (Adherence)',
      executionRate: 'Bajarilish koʻrsatkichi',
      avgDelay: 'Oʻrtacha kechikish',
      avgDuration: 'Oʻrtacha davomiylik',
      minutes: 'daqiqa',
    },
    ru: {
      title: 'Ирригационные операции и дисциплина планов',
      plansCreated: 'Создано планов',
      plansApproved: 'Утверждено',
      opsScheduled: 'Запланировано операций',
      opsCompleted: 'Выполнено операций',
      adherenceRate: 'Дисциплина планов (Adherence)',
      executionRate: 'Коэффициент выполнения',
      avgDelay: 'Средняя задержка',
      avgDuration: 'Средняя длительность',
      minutes: 'мин',
    },
    en: {
      title: 'Irrigation Operations & Plan Adherence',
      plansCreated: 'Plans Created',
      plansApproved: 'Approved',
      opsScheduled: 'Operations Scheduled',
      opsCompleted: 'Operations Completed',
      adherenceRate: 'Plan Adherence Rate',
      executionRate: 'Execution Rate',
      avgDelay: 'Average Delay',
      avgDuration: 'Average Duration',
      minutes: 'mins',
    },
  }[lang]

  if (!irrigation) return null

  const plansCreated = irrigation.totalPlansCreated ?? irrigation.totalPlans ?? 0
  const plansApproved = irrigation.totalPlansApproved ?? irrigation.approvedCount ?? 0
  const opsScheduled = irrigation.totalOperationsScheduled ?? irrigation.scheduledCount ?? 0
  const opsCompleted = irrigation.totalOperationsCompleted ?? irrigation.completedCount ?? 0
  const avgDelay = irrigation.averageDelayMinutes ?? 0
  const avgDuration = irrigation.averageActualDurationMinutes ?? 0

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-lg bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400">
            <CalendarCheck className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-base font-semibold text-slate-900 dark:text-white">{t.title}</h3>
            <span className="text-xs text-slate-500 dark:text-slate-400">
              {opsCompleted} / {plansCreated} {t.opsCompleted.toLowerCase()}
            </span>
          </div>
        </div>

        {irrigation.planAdherencePercent !== null && irrigation.planAdherencePercent !== undefined && (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300">
            <CheckCircle2 className="w-3.5 h-3.5" />
            {irrigation.planAdherencePercent}% {t.adherenceRate}
          </span>
        )}
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-lg border border-slate-100 dark:border-slate-800">
          <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.plansCreated}</span>
          <div className="text-base font-bold text-slate-900 dark:text-white mt-0.5">
            {plansCreated}
          </div>
          <span className="text-[10px] text-slate-400">
            {plansApproved} {t.plansApproved.toLowerCase()}
          </span>
        </div>

        <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-lg border border-slate-100 dark:border-slate-800">
          <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.opsCompleted}</span>
          <div className="text-base font-bold text-slate-900 dark:text-white mt-0.5">
            {opsCompleted}
          </div>
          <span className="text-[10px] text-slate-400">
            {opsScheduled} {t.opsScheduled.toLowerCase()}
          </span>
        </div>

        <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-lg border border-slate-100 dark:border-slate-800">
          <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.avgDelay}</span>
          <div className="text-base font-bold text-slate-900 dark:text-white mt-0.5 flex items-center gap-1">
            <Clock className="w-3.5 h-3.5 text-slate-400" />
            <span>{avgDelay}</span>
            <span className="text-xs font-normal text-slate-400">{t.minutes}</span>
          </div>
        </div>

        <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-lg border border-slate-100 dark:border-slate-800">
          <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.avgDuration}</span>
          <div className="text-base font-bold text-slate-900 dark:text-white mt-0.5 flex items-center gap-1">
            <Clock className="w-3.5 h-3.5 text-slate-400" />
            <span>{avgDuration}</span>
            <span className="text-xs font-normal text-slate-400">{t.minutes}</span>
          </div>
        </div>
      </div>
    </div>
  )
}
