import { CheckCircle2, AlertTriangle, ShieldCheck, ShieldAlert } from 'lucide-react'
import { SituationSummary } from '@/entities/operations'
import { SupportedLanguage } from '@/shared/i18n'

interface ConstraintChecklistCardProps {
  situation: SituationSummary
  lang: SupportedLanguage
}

export function ConstraintChecklistCard({ situation, lang }: ConstraintChecklistCardProps) {
  const isHydraulicSafe = situation.hydraulicSafetyStatus === 'SAFE'
  const isWaterAvailable = situation.waterRemainingM3 > 0

  return (
    <div className="bg-card border border-border rounded-xl p-5 shadow-sm space-y-4">
      <div className="flex items-center justify-between border-b border-border pb-3">
        <h2 className="text-base font-semibold text-foreground">
          {lang === 'uz' ? 'Operatsion Cheklovlar (Constraints)' : lang === 'ru' ? 'Операционные ограничения' : 'Operational Constraints'}
        </h2>
        <span className="text-xs font-mono text-muted-foreground">Precedence Rules</span>
      </div>

      <div className="space-y-2.5">
        {/* Hydraulic Safety */}
        <div className="flex items-center justify-between p-2.5 rounded-lg bg-muted/20 border border-border/60">
          <div className="flex items-center space-x-2">
            {isHydraulicSafe ? (
              <ShieldCheck className="h-4 w-4 text-emerald-500" />
            ) : (
              <ShieldAlert className="h-4 w-4 text-red-500" />
            )}
            <span className="text-xs font-medium text-foreground">
              {lang === 'uz' ? 'Gidravlik xavfsizlik interlogi' : 'Гидравлическая безопасность'}
            </span>
          </div>
          <span
            className={`text-xs font-semibold px-2 py-0.5 rounded ${
              isHydraulicSafe
                ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300'
                : 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-300'
            }`}
          >
            {isHydraulicSafe ? (lang === 'uz' ? 'Xavfsiz' : 'Безопасно') : (lang === 'uz' ? 'Bloklangan' : 'Блокировка')}
          </span>
        </div>

        {/* Water Allocation Quota */}
        <div className="flex items-center justify-between p-2.5 rounded-lg bg-muted/20 border border-border/60">
          <div className="flex items-center space-x-2">
            {isWaterAvailable ? (
              <CheckCircle2 className="h-4 w-4 text-emerald-500" />
            ) : (
              <AlertTriangle className="h-4 w-4 text-amber-500" />
            )}
            <span className="text-xs font-medium text-foreground">
              {lang === 'uz' ? 'Mavsumiy suv kvotasi' : 'Сезонный лимит воды'}
            </span>
          </div>
          <span
            className={`text-xs font-semibold px-2 py-0.5 rounded ${
              isWaterAvailable
                ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300'
                : 'bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300'
            }`}
          >
            {isWaterAvailable ? `${Math.round(situation.waterRemainingM3).toLocaleString()} m³` : 'Tugagan'}
          </span>
        </div>

        {/* Dispatch Window */}
        <div className="flex items-center justify-between p-2.5 rounded-lg bg-muted/20 border border-border/60">
          <div className="flex items-center space-x-2">
            <CheckCircle2 className="h-4 w-4 text-emerald-500" />
            <span className="text-xs font-medium text-foreground">
              {lang === 'uz' ? 'Tungi dispetcherizatsiya optimalligi' : 'Ночное окно полива'}
            </span>
          </div>
          <span className="text-xs font-semibold px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300">
            22:00 - 06:00
          </span>
        </div>
      </div>
    </div>
  )
}
