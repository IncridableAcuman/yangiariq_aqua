import { CheckCircle2, CircleDot, Circle, AlertOctagon, XCircle } from 'lucide-react'
import { WorkflowStep, WorkflowStepStatus } from '@/entities/operations'
import { SupportedLanguage } from '@/shared/i18n'

interface OperationalWorkflowTimelineProps {
  timeline: WorkflowStep[]
  lang: SupportedLanguage
  onActionClick?: (actionKey: string) => void
}

export function OperationalWorkflowTimeline({
  timeline = [],
  lang,
  onActionClick,
}: OperationalWorkflowTimelineProps) {
  const getStatusIcon = (status: WorkflowStepStatus) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle2 className="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
      case 'CURRENT':
        return <CircleDot className="h-5 w-5 text-primary animate-pulse" />
      case 'BLOCKED':
        return <AlertOctagon className="h-5 w-5 text-red-600 dark:text-red-400" />
      case 'REJECTED':
        return <XCircle className="h-5 w-5 text-rose-600 dark:text-rose-400" />
      default:
        return <Circle className="h-5 w-5 text-muted-foreground/40" />
    }
  }

  const getStepActionLabel = (action: string) => {
    switch (action) {
      case 'PREPARE_PLAN':
        return lang === 'uz' ? 'Reja tuzish' : lang === 'ru' ? 'Создать план' : 'Create Plan'
      case 'APPROVE_PLAN':
        return lang === 'uz' ? 'Tasdiqlash' : lang === 'ru' ? 'Утвердить' : 'Approve'
      case 'SCHEDULE_PLAN':
        return lang === 'uz' ? 'Rejalashtirish' : lang === 'ru' ? 'В расписание' : 'Schedule'
      case 'START_OPERATION':
        return lang === 'uz' ? 'Boshlashni qayd etish' : lang === 'ru' ? 'Начать полив' : 'Start Operation'
      case 'COMPLETE_OPERATION':
        return lang === 'uz' ? 'Tugallashni qayd etish' : lang === 'ru' ? 'Завершить полив' : 'Complete Operation'
      case 'VERIFY_RESULT':
        return lang === 'uz' ? 'Natijani tekshirish' : lang === 'ru' ? 'Верификация' : 'Verify Result'
      default:
        return action
    }
  }

  return (
    <div className="bg-card border border-border rounded-xl p-5 shadow-sm space-y-4">
      <div className="flex items-center justify-between border-b border-border pb-3">
        <h2 className="text-base font-semibold text-foreground">
          {lang === 'uz'
            ? 'Operatsion Ish Jarayoni (Workflow Timeline)'
            : lang === 'ru'
            ? 'Операционный рабочий процесс (Workflow Timeline)'
            : 'Operational Workflow Timeline'}
        </h2>
        <span className="text-xs text-muted-foreground font-mono">7 Bosqich</span>
      </div>

      <div className="relative pl-6 space-y-6 before:absolute before:left-2.5 before:top-2 before:bottom-2 before:w-0.5 before:bg-border">
        {timeline.map((step, idx) => {
          const isCurrent = step.status === 'CURRENT'
          const isBlocked = step.status === 'BLOCKED'

          return (
            <div key={step.stepKey || idx} className="relative group">
              {/* Step indicator circle */}
              <div className="absolute -left-6 top-0.5 flex items-center justify-center bg-card rounded-full ring-4 ring-card">
                {getStatusIcon(step.status)}
              </div>

              {/* Step Content */}
              <div
                className={`p-3.5 rounded-lg border transition-all ${
                  isCurrent
                    ? 'bg-primary/5 border-primary/30 shadow-sm'
                    : isBlocked
                    ? 'bg-red-50/70 border-red-200 dark:bg-red-950/30 dark:border-red-900'
                    : 'bg-muted/20 border-border/60 hover:bg-muted/40'
                }`}
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div className="flex items-center space-x-2">
                    <span className="text-xs font-bold font-mono text-muted-foreground">
                      0{idx + 1}.
                    </span>
                    <h3
                      className={`text-sm font-semibold ${
                        isCurrent
                          ? 'text-primary'
                          : isBlocked
                          ? 'text-red-700 dark:text-red-300'
                          : 'text-foreground'
                      }`}
                    >
                      {step.title}
                    </h3>
                  </div>

                  <div className="flex items-center space-x-2 text-xs text-muted-foreground">
                    {step.timestamp ? (
                      <span className="font-mono">
                        {new Date(step.timestamp).toLocaleTimeString([], {
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </span>
                    ) : (
                      <span className="italic">
                        {step.status === 'UPCOMING'
                          ? lang === 'uz'
                            ? 'Kutilmoqda'
                            : 'Ожидается'
                          : ''}
                      </span>
                    )}

                    {step.actor && (
                      <span className="inline-block px-1.5 py-0.5 rounded bg-muted text-[10px] font-mono font-medium">
                        {step.actor}
                      </span>
                    )}
                  </div>
                </div>

                {step.reason && (
                  <p className="text-xs text-muted-foreground mt-2 leading-relaxed">
                    {step.reason}
                  </p>
                )}

                {step.availableAction && onActionClick && (
                  <div className="mt-3 pt-2 border-t border-border/40 flex justify-end">
                    <button
                      type="button"
                      onClick={() => onActionClick(step.availableAction!)}
                      className="inline-flex items-center px-3 py-1.5 rounded-md text-xs font-semibold bg-primary text-primary-foreground hover:bg-primary/90 transition-colors shadow-sm"
                    >
                      {getStepActionLabel(step.availableAction)}
                    </button>
                  </div>
                )}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
