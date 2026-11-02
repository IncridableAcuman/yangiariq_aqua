import { Droplets, Thermometer, Radio, Clock, ArrowDown, ArrowUp, Minus } from 'lucide-react'
import { SituationSummary } from '@/entities/operations'
import { SupportedLanguage } from '@/shared/i18n'

interface CurrentSituationPanelProps {
  situation: SituationSummary
  lang: SupportedLanguage
}

export function CurrentSituationPanel({ situation, lang }: CurrentSituationPanelProps) {
  const getMoistureStatus = (val: number | null) => {
    if (val === null) return { label: 'Maʼlumot yoʻq', color: 'text-muted-foreground' }
    if (val < 15) return { label: lang === 'uz' ? 'Kritik qurgʻoqchilik (<15%)' : 'Критическая засуха', color: 'text-red-600 dark:text-red-400 font-bold' }
    if (val < 25) return { label: lang === 'uz' ? 'Suv taqchilligi (<25%)' : 'Дефицит влаги', color: 'text-amber-600 dark:text-amber-400 font-semibold' }
    if (val <= 65) return { label: lang === 'uz' ? 'Optimal namlik' : 'Оптимально', color: 'text-emerald-600 dark:text-emerald-400 font-medium' }
    return { label: lang === 'uz' ? 'Ortiqcha namlik' : 'Переувлажнение', color: 'text-blue-600 dark:text-blue-400' }
  }

  const moistureInfo = getMoistureStatus(situation.latestSoilMoisture)

  return (
    <div className="bg-card border border-border rounded-xl p-5 shadow-sm space-y-4">
      <div className="flex items-center justify-between border-b border-border pb-3">
        <h2 className="text-base font-semibold text-foreground">
          {lang === 'uz' ? 'Joriy Maydon Holati (Current Situation)' : lang === 'ru' ? 'Текущее состояние поля' : 'Current Field Situation'}
        </h2>
        <span className="inline-flex items-center text-xs text-muted-foreground">
          <Clock className="h-3.5 w-3.5 mr-1" />
          {situation.lastTelemetryAt
            ? new Date(situation.lastTelemetryAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
            : lang === 'uz' ? 'Datchik signali yoʻq' : 'Нет сигнала'}
        </span>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {/* Soil Moisture */}
        <div className="p-3.5 rounded-lg bg-muted/30 border border-border space-y-1">
          <div className="flex items-center justify-between text-muted-foreground text-xs">
            <span className="flex items-center">
              <Droplets className="h-3.5 w-3.5 mr-1 text-blue-500" />
              {lang === 'uz' ? 'Tuproq Namligi' : 'Влажность'}
            </span>
            {situation.soilMoistureTrend12h !== null && (
              <span className="inline-flex items-center text-[10px] font-mono text-muted-foreground">
                {situation.soilMoistureTrend12h < 0 ? (
                  <ArrowDown className="h-3 w-3 text-amber-500 mr-0.5" />
                ) : situation.soilMoistureTrend12h > 0 ? (
                  <ArrowUp className="h-3 w-3 text-emerald-500 mr-0.5" />
                ) : (
                  <Minus className="h-3 w-3 text-slate-400 mr-0.5" />
                )}
                {Math.abs(situation.soilMoistureTrend12h)}%/12h
              </span>
            )}
          </div>
          <div className="text-2xl font-bold tracking-tight text-foreground font-mono">
            {situation.latestSoilMoisture !== null ? `${situation.latestSoilMoisture.toFixed(1)}%` : '—'}
          </div>
          <p className={`text-[11px] truncate ${moistureInfo.color}`}>{moistureInfo.label}</p>
        </div>

        {/* Temperature */}
        <div className="p-3.5 rounded-lg bg-muted/30 border border-border space-y-1">
          <div className="flex items-center text-muted-foreground text-xs">
            <Thermometer className="h-3.5 w-3.5 mr-1 text-orange-500" />
            {lang === 'uz' ? 'Harorat' : 'Температура'}
          </div>
          <div className="text-2xl font-bold tracking-tight text-foreground font-mono">
            {situation.temperatureCelsius !== null ? `${situation.temperatureCelsius.toFixed(1)}°C` : '—'}
          </div>
          <p className="text-[11px] text-muted-foreground">
            {situation.temperatureCelsius && situation.temperatureCelsius >= 32
              ? lang === 'uz' ? 'Tungi sugʻorish tavsiya' : 'Ночной полив'
              : lang === 'uz' ? 'Moʻtadil' : 'Норма'}
          </p>
        </div>

        {/* Telemetry Freshness */}
        <div className="p-3.5 rounded-lg bg-muted/30 border border-border space-y-1">
          <div className="flex items-center text-muted-foreground text-xs">
            <Radio className="h-3.5 w-3.5 mr-1 text-emerald-500" />
            {lang === 'uz' ? 'Telemetriya' : 'Телеметрия'}
          </div>
          <div className="text-2xl font-bold tracking-tight text-foreground font-mono">
            {situation.telemetryFreshness}
          </div>
          <p className="text-[11px] text-muted-foreground">
            {situation.telemetryFreshness === 'FRESH'
              ? lang === 'uz' ? 'Yangi (<30 daqiqa)' : 'Свежие данные'
              : lang === 'uz' ? 'Eskirgan signal' : 'Устаревшие данные'}
          </p>
        </div>

        {/* Water Remaining Quota */}
        <div className="p-3.5 rounded-lg bg-muted/30 border border-border space-y-1">
          <div className="flex items-center text-muted-foreground text-xs">
            <Droplets className="h-3.5 w-3.5 mr-1 text-teal-500" />
            {lang === 'uz' ? 'Mavjud Limit' : 'Остаток лимита'}
          </div>
          <div className="text-2xl font-bold tracking-tight text-teal-600 dark:text-teal-400 font-mono">
            {Math.round(situation.waterRemainingM3).toLocaleString()} <span className="text-xs font-normal text-muted-foreground">m³</span>
          </div>
          <p className="text-[11px] text-muted-foreground">
            {Math.round(situation.waterConsumedM3).toLocaleString()} m³ {lang === 'uz' ? 'sarflangan' : 'израсходовано'}
          </p>
        </div>
      </div>
    </div>
  )
}
