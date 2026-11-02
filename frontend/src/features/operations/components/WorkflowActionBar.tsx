import { PlayCircle, CheckCircle2, XCircle, Calendar, FileText, ShieldAlert } from 'lucide-react'
import { OperationalWorkflowState } from '@/entities/operations'
import { SupportedLanguage } from '@/shared/i18n'

interface WorkflowActionBarProps {
  currentState: OperationalWorkflowState
  isBlocked: boolean
  availableActions: string[]
  lang: SupportedLanguage
  onActionClick: (action: string) => void
  isLoading?: boolean
}

export function WorkflowActionBar({
  currentState,
  isBlocked,
  availableActions,
  lang,
  onActionClick,
  isLoading,
}: WorkflowActionBarProps) {
  return (
    <div className="bg-card border border-border rounded-xl p-4 shadow-sm flex flex-wrap items-center justify-between gap-3">
      <div className="flex items-center space-x-2">
        <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
          {lang === 'uz' ? 'Joriy Bosqich: ' : 'Текущий этап: '}
        </span>
        <span
          className={`px-2.5 py-0.5 rounded text-xs font-bold ${
            isBlocked
              ? 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-300 border border-red-200'
              : currentState === 'COMPLETED' || currentState === 'VERIFIED'
              ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300 border border-emerald-200'
              : currentState === 'IN_PROGRESS'
              ? 'bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-300 border border-blue-200'
              : 'bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300 border border-amber-200'
          }`}
        >
          {currentState}
        </span>
      </div>

      <div className="flex flex-wrap items-center gap-2">
        {isBlocked && (
          <button
            type="button"
            onClick={() => onActionClick('INVESTIGATE_SAFETY')}
            className="inline-flex items-center px-3.5 py-2 rounded-lg text-xs font-semibold bg-red-600 text-white hover:bg-red-700 transition-colors shadow-sm"
          >
            <ShieldAlert className="h-4 w-4 mr-1.5" />
            {lang === 'uz' ? 'Xavfsizlik Muammosini Tekshirish' : 'Проверить блокировку'}
          </button>
        )}

        {availableActions.includes('PREPARE_PLAN') && (
          <button
            type="button"
            disabled={isLoading}
            onClick={() => onActionClick('PREPARE_PLAN')}
            className="inline-flex items-center px-3.5 py-2 rounded-lg text-xs font-semibold bg-primary text-primary-foreground hover:bg-primary/90 transition-colors shadow-sm disabled:opacity-50"
          >
            <FileText className="h-4 w-4 mr-1.5" />
            {lang === 'uz' ? 'Reja Tayyorlash' : 'Подготовить план'}
          </button>
        )}

        {availableActions.includes('APPROVE_PLAN') && (
          <button
            type="button"
            disabled={isLoading}
            onClick={() => onActionClick('APPROVE_PLAN')}
            className="inline-flex items-center px-3.5 py-2 rounded-lg text-xs font-semibold bg-emerald-600 text-white hover:bg-emerald-700 transition-colors shadow-sm disabled:opacity-50"
          >
            <CheckCircle2 className="h-4 w-4 mr-1.5" />
            {lang === 'uz' ? 'Insoniy Tasdiqlash' : 'Утвердить план'}
          </button>
        )}

        {availableActions.includes('REJECT_PLAN') && (
          <button
            type="button"
            disabled={isLoading}
            onClick={() => onActionClick('REJECT_PLAN')}
            className="inline-flex items-center px-3.5 py-2 rounded-lg text-xs font-semibold bg-rose-50 text-rose-700 hover:bg-rose-100 dark:bg-rose-950/40 dark:text-rose-300 transition-colors border border-rose-200 disabled:opacity-50"
          >
            <XCircle className="h-4 w-4 mr-1.5" />
            {lang === 'uz' ? 'Rad Etish' : 'Отклонить'}
          </button>
        )}

        {availableActions.includes('SCHEDULE_PLAN') && (
          <button
            type="button"
            disabled={isLoading}
            onClick={() => onActionClick('SCHEDULE_PLAN')}
            className="inline-flex items-center px-3.5 py-2 rounded-lg text-xs font-semibold bg-primary text-primary-foreground hover:bg-primary/90 transition-colors shadow-sm disabled:opacity-50"
          >
            <Calendar className="h-4 w-4 mr-1.5" />
            {lang === 'uz' ? 'Jadvalga Kiritish' : 'Внести в расписание'}
          </button>
        )}

        {availableActions.includes('START_OPERATION') && (
          <button
            type="button"
            disabled={isLoading}
            onClick={() => onActionClick('START_OPERATION')}
            className="inline-flex items-center px-3.5 py-2 rounded-lg text-xs font-semibold bg-blue-600 text-white hover:bg-blue-700 transition-colors shadow-sm disabled:opacity-50"
          >
            <PlayCircle className="h-4 w-4 mr-1.5" />
            {lang === 'uz' ? 'Boshlashni Qayd Etish' : 'Начать полив'}
          </button>
        )}

        {availableActions.includes('COMPLETE_OPERATION') && (
          <button
            type="button"
            disabled={isLoading}
            onClick={() => onActionClick('COMPLETE_OPERATION')}
            className="inline-flex items-center px-3.5 py-2 rounded-lg text-xs font-semibold bg-emerald-600 text-white hover:bg-emerald-700 transition-colors shadow-sm disabled:opacity-50"
          >
            <CheckCircle2 className="h-4 w-4 mr-1.5" />
            {lang === 'uz' ? 'Tugallashni Qayd Etish' : 'Завершить полив'}
          </button>
        )}

        {availableActions.includes('VERIFY_RESULT') && (
          <button
            type="button"
            disabled={isLoading}
            onClick={() => onActionClick('VERIFY_RESULT')}
            className="inline-flex items-center px-3.5 py-2 rounded-lg text-xs font-semibold bg-emerald-600 text-white hover:bg-emerald-700 transition-colors shadow-sm disabled:opacity-50"
          >
            <CheckCircle2 className="h-4 w-4 mr-1.5" />
            {lang === 'uz' ? 'Natijani Tekshirish' : 'Верификация'}
          </button>
        )}
      </div>
    </div>
  )
}
