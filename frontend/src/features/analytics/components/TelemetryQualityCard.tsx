import React from 'react'
import { Activity, Radio, ShieldAlert } from 'lucide-react'
import { TelemetryQualityDto } from '@/entities/analytics'
import { SupportedLanguage } from '@/shared/i18n'

interface TelemetryQualityCardProps {
  telemetry?: TelemetryQualityDto
  lang: SupportedLanguage
}

export const TelemetryQualityCard: React.FC<TelemetryQualityCardProps> = ({ telemetry, lang }) => {
  const t = {
    uz: {
      title: 'Sensor telemetriyasi va maʼlumotlar sifati',
      totalSensors: 'Jami datchiklar',
      activeSensors: 'Faol (Online)',
      degradedSensors: 'Aloqa sust (Degraded)',
      offlineSensors: 'Oʻchiq (Offline)',
      validReadings: 'Yaroqli oʻlchovlar',
      sensorAvailability: 'Datchiklar mavjudligi',
      totalReadings: 'Jami oʻlchovlar',
      qualityRuleNote:
        'Qatʼiy qoida: Sensor maʼlumotlari yetishmasligi hech qachon muvaffaqiyatli sugʻorish sifatida qabul qilinmaydi va alohida tekshiruv talab qiladi.',
    },
    ru: {
      title: 'Телеметрия датчиков и качество данных',
      totalSensors: 'Всего датчиков',
      activeSensors: 'Активные (Online)',
      degradedSensors: 'Нестабильные (Degraded)',
      offlineSensors: 'Офлайн (Offline)',
      validReadings: 'Достоверные замеры',
      sensorAvailability: 'Доступность датчиков',
      totalReadings: 'Всего замеров',
      qualityRuleNote:
        'Строгое правило: Отсутствие телеметрии никогда не считается успешным поливом и требует обязательной проверки.',
    },
    en: {
      title: 'Sensor Telemetry & Data Quality',
      totalSensors: 'Total Sensors',
      activeSensors: 'Online',
      degradedSensors: 'Degraded',
      offlineSensors: 'Offline',
      validReadings: 'Valid Readings',
      sensorAvailability: 'Sensor Availability',
      totalReadings: 'Total Readings',
      qualityRuleNote:
        'Strict governance: Missing telemetry is NEVER treated as successful irrigation and triggers verification warnings.',
    },
  }[lang]

  if (!telemetry) return null

  const onlineSensors = telemetry.onlineSensors ?? telemetry.activeSensors ?? 0
  const degradedSensors = telemetry.degradedSensors ?? telemetry.staleSensors ?? 0
  const offlineSensors = telemetry.offlineSensors ?? 0
  const totalSensors = telemetry.totalSensors ?? 0
  const validReadings = telemetry.validReadingsCount ?? telemetry.validReadings ?? 0
  const totalReadings = telemetry.totalReadingsCount ?? telemetry.totalReadings ?? 0
  const coveragePercent = telemetry.validReadingPercent ?? telemetry.telemetryCoveragePercent
  const availabilityPercent =
    telemetry.sensorAvailabilityPercent ??
    (totalSensors > 0 ? Math.round((onlineSensors * 100) / totalSensors) : null)

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-lg bg-cyan-50 dark:bg-cyan-950/40 text-cyan-600 dark:text-cyan-400">
            <Radio className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-base font-semibold text-slate-900 dark:text-white">{t.title}</h3>
            <span className="text-xs text-slate-500 dark:text-slate-400">
              {onlineSensors} / {totalSensors} {t.activeSensors.toLowerCase()}
            </span>
          </div>
        </div>

        {coveragePercent !== null && coveragePercent !== undefined && (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-cyan-100 text-cyan-800 dark:bg-cyan-900/40 dark:text-cyan-300">
            <Activity className="w-3.5 h-3.5" />
            {coveragePercent}% {t.validReadings}
          </span>
        )}
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-lg border border-slate-100 dark:border-slate-800">
          <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.activeSensors}</span>
          <div className="text-base font-bold text-emerald-600 dark:text-emerald-400 mt-0.5">
            {onlineSensors}
          </div>
          <span className="text-[10px] text-slate-400">
            / {totalSensors} {t.totalSensors.toLowerCase()}
          </span>
        </div>

        <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-lg border border-slate-100 dark:border-slate-800">
          <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.offlineSensors}</span>
          <div className={`text-base font-bold mt-0.5 ${offlineSensors > 0 ? 'text-rose-600' : 'text-slate-900 dark:text-white'}`}>
            {offlineSensors}
          </div>
          {degradedSensors > 0 && (
            <span className="text-[10px] text-amber-500">
              {degradedSensors} {t.degradedSensors.toLowerCase()}
            </span>
          )}
        </div>

        <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-lg border border-slate-100 dark:border-slate-800">
          <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.validReadings}</span>
          <div className="text-base font-bold text-slate-900 dark:text-white mt-0.5">
            {validReadings.toLocaleString()}
          </div>
          <span className="text-[10px] text-slate-400">
            / {totalReadings.toLocaleString()} {t.totalReadings.toLowerCase()}
          </span>
        </div>

        <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-lg border border-slate-100 dark:border-slate-800">
          <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.sensorAvailability}</span>
          <div className="text-base font-bold text-slate-900 dark:text-white mt-0.5">
            {availabilityPercent !== null && availabilityPercent !== undefined
              ? `${availabilityPercent}%`
              : '—'}
          </div>
        </div>
      </div>

      <div className="p-3 bg-slate-50 dark:bg-slate-800/40 border border-slate-200/60 dark:border-slate-700/60 rounded-lg text-xs text-slate-600 dark:text-slate-400 flex items-start gap-2">
        <ShieldAlert className="w-4 h-4 text-cyan-600 dark:text-cyan-400 shrink-0 mt-0.5" />
        <p>{t.qualityRuleNote}</p>
      </div>
    </div>
  )
}
