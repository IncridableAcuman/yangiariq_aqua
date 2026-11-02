import { Calendar, Clock, Droplets, CheckCircle2, XCircle } from 'lucide-react'
import { SmartIrrigationPlan } from '@/entities/irrigation/types'
import { SupportedLanguage } from '@/shared/i18n'

interface PlanReviewCardProps {
  plan: SmartIrrigationPlan | null
  lang: SupportedLanguage
  onOpenApprove?: () => void
  onOpenReject?: () => void
  onOpenSchedule?: () => void
}

export function PlanReviewCard({
  plan,
  lang,
  onOpenApprove,
  onOpenReject,
  onOpenSchedule,
}: PlanReviewCardProps) {
  if (!plan) {
    return null
  }

  const isProposed = plan.status === 'PROPOSED' || plan.status === 'DRAFT'
  const isApproved = plan.status === 'APPROVED'
  const isScheduled = plan.status === 'SCHEDULED'

  return (
    <div className="bg-card border border-border rounded-xl p-5 shadow-sm space-y-4">
      <div className="flex items-center justify-between border-b border-border pb-3">
        <h2 className="text-base font-semibold text-foreground">
          {lang === 'uz' ? 'Sugʻorish Rejasi (Plan Review)' : lang === 'ru' ? 'План полива' : 'Irrigation Plan Review'}
        </h2>
        <span
          className={`inline-flex items-center px-2.5 py-0.5 rounded text-xs font-semibold ${
            isApproved
              ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300 border border-emerald-200'
              : isScheduled
              ? 'bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-300 border border-blue-200'
              : 'bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300 border border-amber-200'
          }`}
        >
          {plan.status}
        </span>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {/* Planned Volume */}
        <div className="p-3.5 rounded-lg bg-muted/20 border border-border/60">
          <span className="text-xs text-muted-foreground flex items-center">
            <Droplets className="h-3.5 w-3.5 mr-1 text-primary" />
            {lang === 'uz' ? 'Rejalashtirilgan Hajm' : 'Объем'}
          </span>
          <div className="text-xl font-bold font-mono text-foreground mt-1">
            {plan.recommendedVolumeM3.toFixed(1)} <span className="text-xs font-normal text-muted-foreground">m³</span>
          </div>
        </div>

        {/* Estimated Duration */}
        <div className="p-3.5 rounded-lg bg-muted/20 border border-border/60">
          <span className="text-xs text-muted-foreground flex items-center">
            <Clock className="h-3.5 w-3.5 mr-1 text-primary" />
            {lang === 'uz' ? 'Davomiyligi' : 'Длительность'}
          </span>
          <div className="text-xl font-bold font-mono text-foreground mt-1">
            {plan.estimatedDurationMinutes} <span className="text-xs font-normal text-muted-foreground">daqiqa</span>
          </div>
        </div>

        {/* Planned Date */}
        <div className="p-3.5 rounded-lg bg-muted/20 border border-border/60">
          <span className="text-xs text-muted-foreground flex items-center">
            <Calendar className="h-3.5 w-3.5 mr-1 text-primary" />
            {lang === 'uz' ? 'Sana' : 'Дата'}
          </span>
          <div className="text-sm font-semibold font-mono text-foreground mt-1">
            {plan.plannedDate}
          </div>
        </div>

        {/* Delivery Window */}
        <div className="p-3.5 rounded-lg bg-muted/20 border border-border/60">
          <span className="text-xs text-muted-foreground flex items-center">
            <Clock className="h-3.5 w-3.5 mr-1 text-primary" />
            {lang === 'uz' ? 'Vaqt oraligʻi' : 'Окно'}
          </span>
          <div className="text-xs font-semibold font-mono text-foreground mt-1">
            {plan.plannedStartAt && plan.plannedEndAt
              ? `${new Date(plan.plannedStartAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - ${new Date(plan.plannedEndAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
              : '22:00 - 01:00'}
          </div>
        </div>
      </div>

      {plan.reason && (
        <p className="text-xs text-muted-foreground bg-muted/20 p-3 rounded-lg border border-border/40">
          <strong>{lang === 'uz' ? 'Reja asosi: ' : 'Обоснование: '}</strong>
          {plan.reason}
        </p>
      )}

      {/* Action Buttons for Plan Review */}
      <div className="pt-2 flex flex-wrap items-center justify-end gap-2.5">
        {isProposed && (
          <>
            {onOpenReject && (
              <button
                type="button"
                onClick={onOpenReject}
                className="inline-flex items-center px-3 py-1.5 rounded-lg text-xs font-semibold bg-rose-50 text-rose-700 hover:bg-rose-100 dark:bg-rose-950/40 dark:text-rose-300 dark:hover:bg-rose-900/60 transition-colors border border-rose-200"
              >
                <XCircle className="h-3.5 w-3.5 mr-1" />
                {lang === 'uz' ? 'Rad etish' : 'Отклонить'}
              </button>
            )}
            {onOpenApprove && (
              <button
                type="button"
                onClick={onOpenApprove}
                className="inline-flex items-center px-4 py-2 rounded-lg text-xs font-semibold bg-emerald-600 text-white hover:bg-emerald-700 transition-colors shadow-sm"
              >
                <CheckCircle2 className="h-3.5 w-3.5 mr-1.5" />
                {lang === 'uz' ? 'Insoniy Tasdiqlash' : 'Утвердить'}
              </button>
            )}
          </>
        )}

        {isApproved && onOpenSchedule && (
          <button
            type="button"
            onClick={onOpenSchedule}
            className="inline-flex items-center px-4 py-2 rounded-lg text-xs font-semibold bg-primary text-primary-foreground hover:bg-primary/90 transition-colors shadow-sm"
          >
            <Clock className="h-3.5 w-3.5 mr-1.5" />
            {lang === 'uz' ? 'Dispetcher Jadvaliga Kiritish' : 'Внести в расписание'}
          </button>
        )}
      </div>
    </div>
  )
}
