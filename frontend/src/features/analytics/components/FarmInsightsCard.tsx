import React from 'react'
import {
  Lightbulb,
  ShieldAlert,
  Droplet,
  Radio,
  ArrowRight,
  CheckCircle,
} from 'lucide-react'
import { FarmInsightDto, InsightCategory, InsightSeverity } from '@/entities/analytics'
import { SupportedLanguage } from '@/shared/i18n'

interface FarmInsightsCardProps {
  insights: FarmInsightDto[]
  lang: SupportedLanguage
  onNavigateToAction?: (section: string, payload?: Record<string, string>) => void
}

export const FarmInsightsCard: React.FC<FarmInsightsCardProps> = ({
  insights,
  lang,
  onNavigateToAction,
}) => {
  const t = {
    uz: {
      title: 'Harakatga chaqiruvchi tavsiyalar va xulosalar (Actionable Insights)',
      subtitle: 'Tizim maʼlumotlari asosida shakllangan aniq agronomiya va suv xoʻjaligi tavsiyalari',
      noInsights: 'Hozircha tizimda faol xavf yoki ogohlantirish aniqlanmadi. Barcha koʻrsatkichlar meʼyorda.',
      evidence: 'Asoslovchi dalillar:',
      action: 'Amalni bajarish',
      critical: 'Kritik xavf',
      high: 'Yuqori muhimlik',
      warning: 'Ogohlantirish',
      info: 'Maʼlumot',
      catWater: 'Suv xoʻjaligi',
      catIrrigation: 'Sugʻorish',
      catTelemetry: 'Sensor telemetriyasi',
      catSafety: 'Gidro-xavfsizlik',
      catVerification: 'Tekshiruv',
    },
    ru: {
      title: 'Аналитические выводы и рекомендации (Actionable Insights)',
      subtitle: 'Четкие агрономические и водные рекомендации на основе операционных данных',
      noInsights: 'В настоящий момент критических отклонений не зафиксировано. Показатели в норме.',
      evidence: 'Обоснование и факты:',
      action: 'Выполнить действие',
      critical: 'Критично',
      high: 'Высокий приоритет',
      warning: 'Предупреждение',
      info: 'Информация',
      catWater: 'Водное хозяйство',
      catIrrigation: 'Ирригация',
      catTelemetry: 'Телеметрия',
      catSafety: 'Безопасность',
      catVerification: 'Верификация',
    },
    en: {
      title: 'Actionable Performance Insights',
      subtitle: 'Deterministic agricultural and water management conclusions derived from operational data',
      noInsights: 'No active risks or anomalies detected. All farm performance parameters are compliant.',
      evidence: 'Supporting evidence:',
      action: 'Take Action',
      critical: 'Critical',
      high: 'High',
      warning: 'Warning',
      info: 'Info',
      catWater: 'Water Quota',
      catIrrigation: 'Irrigation',
      catTelemetry: 'Telemetry',
      catSafety: 'Safety',
      catVerification: 'Verification',
    },
  }[lang]

  const getCategoryIcon = (category: InsightCategory) => {
    switch (category) {
      case 'WATER':
        return <Droplet className="w-4 h-4 text-blue-500" />
      case 'SAFETY':
        return <ShieldAlert className="w-4 h-4 text-rose-500" />
      case 'TELEMETRY':
        return <Radio className="w-4 h-4 text-cyan-500" />
      case 'IRRIGATION':
        return <Lightbulb className="w-4 h-4 text-amber-500" />
      default:
        return <CheckCircle className="w-4 h-4 text-indigo-500" />
    }
  }

  const getSeverityBadge = (severity: InsightSeverity) => {
    switch (severity) {
      case 'CRITICAL':
        return (
          <span className="px-2 py-0.5 rounded text-[11px] font-semibold bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-300">
            {t.critical}
          </span>
        )
      case 'HIGH':
        return (
          <span className="px-2 py-0.5 rounded text-[11px] font-semibold bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300">
            {t.high}
          </span>
        )
      case 'WARNING':
        return (
          <span className="px-2 py-0.5 rounded text-[11px] font-semibold bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400">
            {t.warning}
          </span>
        )
      case 'INFO':
        return (
          <span className="px-2 py-0.5 rounded text-[11px] font-semibold bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-400">
            {t.info}
          </span>
        )
    }
  }

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
      <div className="flex items-center gap-2">
        <div className="p-2 rounded-lg bg-amber-50 dark:bg-amber-950/40 text-amber-600 dark:text-amber-400">
          <Lightbulb className="w-5 h-5" />
        </div>
        <div>
          <h3 className="text-base font-semibold text-slate-900 dark:text-white">{t.title}</h3>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{t.subtitle}</p>
        </div>
      </div>

      {insights.length === 0 ? (
        <div className="p-6 text-center text-xs text-slate-400 bg-slate-50 dark:bg-slate-800/40 rounded-lg">
          {t.noInsights}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {insights.map((ins) => (
            <div
              key={ins.id}
              className={`p-4 rounded-xl border flex flex-col justify-between space-y-3 transition-shadow ${
                ins.severity === 'CRITICAL'
                  ? 'bg-rose-50/40 dark:bg-rose-950/20 border-rose-200 dark:border-rose-900/50'
                  : ins.severity === 'HIGH'
                  ? 'bg-amber-50/40 dark:bg-amber-950/20 border-amber-200 dark:border-amber-900/50'
                  : 'bg-slate-50/70 dark:bg-slate-800/40 border-slate-200 dark:border-slate-800'
              }`}
            >
              <div className="space-y-2">
                <div className="flex items-center justify-between gap-2">
                  <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-800 dark:text-slate-200">
                    {getCategoryIcon(ins.category)}
                    <span>{ins.fieldName || ins.category}</span>
                  </div>
                  <div>{getSeverityBadge(ins.severity)}</div>
                </div>

                <h4 className="text-sm font-bold text-slate-900 dark:text-white">
                  {ins.title}
                </h4>

                <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">
                  {ins.explanation}
                </p>

                {ins.evidence && ins.evidence.length > 0 && (
                  <div className="pt-2 border-t border-slate-200/60 dark:border-slate-700/60">
                    <span className="text-[11px] font-semibold text-slate-500 dark:text-slate-400 block mb-1">
                      {t.evidence}
                    </span>
                    <ul className="list-disc list-inside space-y-0.5 text-[11px] text-slate-600 dark:text-slate-300">
                      {ins.evidence.map((ev, idx) => (
                        <li key={idx}>{ev}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>

              <div className="pt-3 flex items-center justify-between border-t border-slate-200/60 dark:border-slate-700/60">
                <span className="text-xs font-medium text-slate-700 dark:text-slate-300">
                  {ins.recommendedAction}
                </span>

                {onNavigateToAction && (
                  <button
                    onClick={() => onNavigateToAction(ins.targetSection, ins.targetPayload)}
                    className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-semibold rounded-lg bg-blue-600 hover:bg-blue-700 text-white transition-colors shadow-sm"
                  >
                    <span>{t.action}</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
