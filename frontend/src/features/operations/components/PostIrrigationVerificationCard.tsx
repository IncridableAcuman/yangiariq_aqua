import { CheckCircle2, TrendingUp, Droplets, Clock, Brain } from 'lucide-react'
import { OperationalVerificationResponse } from '@/entities/operations'
import { SupportedLanguage } from '@/shared/i18n'

interface PostIrrigationVerificationCardProps {
  verification: OperationalVerificationResponse | null
  lang: SupportedLanguage
}

export function PostIrrigationVerificationCard({ verification, lang }: PostIrrigationVerificationCardProps) {
  if (!verification) return null

  const isRechargeOk = verification.isRechargeSuccessful

  return (
    <div className="bg-card border border-border rounded-xl p-5 shadow-sm space-y-4 animate-in fade-in duration-300">
      <div className="flex items-center justify-between border-b border-border pb-3">
        <div className="flex items-center space-x-2">
          <CheckCircle2 className="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
          <h2 className="text-base font-semibold text-foreground">
            {lang === 'uz' ? 'Sugʻorish Natijalarini Tekshirish (Verification)' : 'Верификация результатов'}
          </h2>
        </div>
        <span
          className={`inline-flex items-center px-2.5 py-0.5 rounded text-xs font-semibold ${
            isRechargeOk
              ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300 border border-emerald-200'
              : 'bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300 border border-amber-200'
          }`}
        >
          {isRechargeOk
            ? lang === 'uz' ? 'Muvaffaqiyatli Namlanish' : 'Успешный полив'
            : lang === 'uz' ? 'Qisman Namlanish' : 'Частичный полив'}
        </span>
      </div>

      {/* Primary Comparison Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {/* Planned vs Actual Volume */}
        <div className="p-3.5 rounded-lg bg-muted/20 border border-border/60 space-y-1">
          <span className="text-xs text-muted-foreground flex items-center">
            <Droplets className="h-3.5 w-3.5 mr-1 text-primary" />
            {lang === 'uz' ? 'Hajm: Reja vs Amalda' : 'Объем: План / Факт'}
          </span>
          <div className="text-lg font-bold font-mono text-foreground">
            {verification.actualVolumeM3.toFixed(1)} <span className="text-xs font-normal text-muted-foreground">/ {verification.plannedVolumeM3.toFixed(1)} m³</span>
          </div>
          <p className="text-[11px] font-mono text-muted-foreground">
            Δ {verification.volumeVarianceM3 >= 0 ? '+' : ''}{verification.volumeVarianceM3.toFixed(1)} m³ ({verification.volumeVariancePercent}%)
          </p>
        </div>

        {/* Duration Comparison */}
        <div className="p-3.5 rounded-lg bg-muted/20 border border-border/60 space-y-1">
          <span className="text-xs text-muted-foreground flex items-center">
            <Clock className="h-3.5 w-3.5 mr-1 text-primary" />
            {lang === 'uz' ? 'Davomiylik: Reja vs Amalda' : 'Время: План / Факт'}
          </span>
          <div className="text-lg font-bold font-mono text-foreground">
            {verification.actualDurationMinutes} <span className="text-xs font-normal text-muted-foreground">/ {verification.plannedDurationMinutes} min</span>
          </div>
          <p className="text-[11px] font-mono text-muted-foreground">
            Δ {verification.durationVarianceMinutes >= 0 ? '+' : ''}{verification.durationVarianceMinutes} min
          </p>
        </div>

        {/* Moisture Delta */}
        <div className="p-3.5 rounded-lg bg-emerald-50/40 border border-emerald-200 dark:bg-emerald-950/20 dark:border-emerald-900 space-y-1">
          <span className="text-xs text-emerald-800 dark:text-emerald-300 flex items-center font-medium">
            <TrendingUp className="h-3.5 w-3.5 mr-1 text-emerald-600" />
            {lang === 'uz' ? 'Tuproq Namligi Oʻzgarishi' : 'Изменение влажности'}
          </span>
          <div className="text-lg font-bold font-mono text-emerald-700 dark:text-emerald-300">
            +{verification.soilMoistureChangePercent}%
          </div>
          <p className="text-[11px] text-muted-foreground font-mono">
            {verification.soilMoistureBeforePercent}% → {verification.soilMoistureAfterPercent}%
          </p>
        </div>

        {/* Water Budget After */}
        <div className="p-3.5 rounded-lg bg-muted/20 border border-border/60 space-y-1">
          <span className="text-xs text-muted-foreground flex items-center">
            <Droplets className="h-3.5 w-3.5 mr-1 text-teal-500" />
            {lang === 'uz' ? 'Yangilangan Qoldiq Limit' : 'Остаток лимита'}
          </span>
          <div className="text-lg font-bold font-mono text-teal-600 dark:text-teal-400">
            {Math.round(verification.waterBudgetRemainingAfterM3).toLocaleString()} <span className="text-xs font-normal text-muted-foreground">m³</span>
          </div>
          <p className="text-[11px] text-muted-foreground font-mono">
            {lang === 'uz' ? 'Oldin: ' : 'До: '} {Math.round(verification.waterBudgetRemainingBeforeM3).toLocaleString()} m³
          </p>
        </div>
      </div>

      {/* Summary Text */}
      <div className="p-3 bg-muted/30 rounded-lg text-xs text-foreground/90 border border-border/60 leading-relaxed">
        {verification.verificationSummary}
      </div>

      {/* Learning Loop Evidence */}
      <div className="flex items-start space-x-2 p-3 bg-primary/5 rounded-lg border border-primary/20 text-xs text-primary">
        <Brain className="h-4 w-4 shrink-0 mt-0.5" />
        <div>
          <span className="font-semibold">{lang === 'uz' ? 'Kelajakdagi Intellekt Asosi (Learning Loop Evidence): ' : 'Основа обучения системы: '}</span>
          <code className="text-[11px] font-mono break-all">{verification.learningEvidence}</code>
        </div>
      </div>
    </div>
  )
}
