import { AlertTriangle, Clock } from 'lucide-react'
import { ScheduleConflict } from '@/entities/operations'
import { SupportedLanguage } from '@/shared/i18n'

interface ScheduleConflictAlertProps {
  conflict: ScheduleConflict | null
  lang: SupportedLanguage
  onAdjustTime?: () => void
}

export function ScheduleConflictAlert({ conflict, lang, onAdjustTime }: ScheduleConflictAlertProps) {
  if (!conflict || !conflict.hasConflict) {
    return null
  }

  return (
    <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 flex flex-col sm:flex-row sm:items-center justify-between gap-3 animate-in fade-in duration-200">
      <div className="flex items-start space-x-3">
        <div className="h-7 w-7 rounded-lg bg-amber-500/20 text-amber-700 dark:text-amber-300 flex items-center justify-center shrink-0 mt-0.5">
          <AlertTriangle className="h-4 w-4" />
        </div>
        <div className="space-y-0.5">
          <div className="flex items-center space-x-2">
            <span className="text-xs font-bold uppercase tracking-wider text-amber-700 dark:text-amber-300">
              {conflict.conflictType === 'TIME_OVERLAP'
                ? lang === 'uz' ? 'Jadval Toʻqnashuvi (Schedule Conflict)' : 'Конфликт расписания'
                : lang === 'uz' ? 'Operatsion Cheklov (Operational Block)' : 'Операционное ограничение'}
            </span>
          </div>
          <p className="text-xs text-foreground font-medium">{conflict.conflictMessage}</p>
          {conflict.suggestedAdjustment && (
            <p className="text-xs text-muted-foreground mt-0.5">
              <strong>{lang === 'uz' ? 'Tavsiya: ' : 'Рекомендация: '}</strong>
              {conflict.suggestedAdjustment}
            </p>
          )}
        </div>
      </div>

      {onAdjustTime && (
        <button
          type="button"
          onClick={onAdjustTime}
          className="inline-flex items-center justify-center px-3 py-1.5 rounded-lg text-xs font-semibold bg-amber-600 text-white hover:bg-amber-700 transition-colors shrink-0 shadow-sm"
        >
          <Clock className="h-3.5 w-3.5 mr-1.5" />
          {lang === 'uz' ? 'Vaqtni Moslashtirish' : 'Изменить время'}
        </button>
      )}
    </div>
  )
}
