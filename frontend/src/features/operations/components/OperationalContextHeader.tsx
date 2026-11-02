import { ShieldAlert, ShieldCheck, Shield, MapPin } from 'lucide-react'
import { FieldSummary, SituationSummary } from '@/entities/operations'
import { SupportedLanguage } from '@/shared/i18n'

interface OperationalContextHeaderProps {
  field: FieldSummary
  situation: SituationSummary
  lang: SupportedLanguage
}

export function OperationalContextHeader({ field, situation, lang }: OperationalContextHeaderProps) {
  const isBlocked = situation.hydraulicSafetyStatus === 'BLOCKED'
  const isWarning = situation.hydraulicSafetyStatus === 'WARNING'

  return (
    <div className="bg-card border border-border rounded-xl p-5 shadow-sm space-y-4">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-3">
            <h1 className="text-2xl font-bold tracking-tight text-foreground">{field.name}</h1>
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-primary/10 text-primary border border-primary/20">
              {field.cropType || 'Crop'}
            </span>
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-secondary text-secondary-foreground">
              {field.areaHectares} ha
            </span>
          </div>
          <div className="flex items-center text-xs text-muted-foreground mt-1 space-x-2">
            <MapPin className="h-3.5 w-3.5" />
            <span>
              {field.latitude && field.longitude
                ? `${field.latitude.toFixed(4)}°N, ${field.longitude.toFixed(4)}°E • Yangiariq, Xorazm`
                : 'Yangiariq tumani, Xorazm'}
            </span>
          </div>
        </div>

        {/* Hydraulic Safety Indicator */}
        <div
          className={`inline-flex items-center px-3.5 py-2 rounded-lg text-xs font-semibold border ${
            isBlocked
              ? 'bg-red-50 text-red-700 border-red-200 dark:bg-red-950/40 dark:text-red-300 dark:border-red-900'
              : isWarning
              ? 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/40 dark:text-amber-300 dark:border-amber-900'
              : 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-900'
          }`}
        >
          {isBlocked ? (
            <>
              <ShieldAlert className="h-4 w-4 mr-2 text-red-600" />
              <span>
                {lang === 'uz'
                  ? 'Gidravlik xavfsizlik: BLOKLANGAN'
                  : lang === 'ru'
                  ? 'Гидравлическая блокировка: АКТИВНА'
                  : 'Hydraulic Safety: BLOCKED'}
              </span>
            </>
          ) : isWarning ? (
            <>
              <Shield className="h-4 w-4 mr-2 text-amber-600" />
              <span>
                {lang === 'uz'
                  ? 'Gidravlik xavfsizlik: OGOHLANTIRISH'
                  : lang === 'ru'
                  ? 'Гидравлика: ПРЕДУПРЕЖДЕНИЕ'
                  : 'Hydraulic Safety: WARNING'}
              </span>
            </>
          ) : (
            <>
              <ShieldCheck className="h-4 w-4 mr-2 text-emerald-600" />
              <span>
                {lang === 'uz'
                  ? 'Gidravlik holat: XAVFSIZ'
                  : lang === 'ru'
                  ? 'Гидравлика: БЕЗОПАСНО'
                  : 'Hydraulic Safety: SAFE'}
              </span>
            </>
          )}
        </div>
      </div>

      {isBlocked && situation.hydraulicBlockedReason && (
        <div className="p-3 bg-red-100/70 border border-red-300 dark:bg-red-950/60 dark:border-red-800 rounded-lg text-xs text-red-800 dark:text-red-200">
          <strong>{lang === 'uz' ? 'Xavfsizlik cheklovi: ' : 'Блокировка: '}</strong>
          {situation.hydraulicBlockedReason}
        </div>
      )}
    </div>
  )
}
