import React from 'react'
import { Card, CardContent, CardHeader } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  SmartIrrigationPlan,
  IrrigationPriority,
  IrrigationPlanStatus,
} from '@/entities/irrigation'
import {
  Droplets,
  Clock,
  Gauge,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  ShieldAlert,
  Sparkles,
  Calendar,
  Layers,
} from 'lucide-react'
import { formatVolume, formatDateTime } from '@/features/water/lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { cn } from '@/shared/lib/utils'

interface SmartPlanCardProps {
  plan: SmartIrrigationPlan
  fieldName?: string
  cropType?: string
  areaHectares?: number
  onApprove?: (plan: SmartIrrigationPlan) => void
  onCancel?: (plan: SmartIrrigationPlan) => void
  onComplete?: (plan: SmartIrrigationPlan) => void
  lang: SupportedLanguage
  className?: string
}

export const SmartPlanCard: React.FC<SmartPlanCardProps> = ({
  plan,
  fieldName,
  cropType,
  areaHectares,
  onApprove,
  onCancel,
  onComplete,
  lang,
  className,
}) => {
  const t = translations[lang]

  const getPriorityBadge = (priority: IrrigationPriority) => {
    switch (priority) {
      case 'CRITICAL':
        return <Badge variant="destructive" className="bg-rose-600 text-white font-bold">{t.irrigationPlanning?.priority?.critical || 'Kritik'}</Badge>
      case 'HIGH':
        return <Badge className="bg-amber-500 text-white font-semibold">{t.irrigationPlanning?.priority?.high || 'Yuqori'}</Badge>
      case 'MEDIUM':
        return <Badge variant="secondary" className="bg-sky-500/10 text-sky-700 dark:text-sky-300">{t.irrigationPlanning?.priority?.medium || 'Oʻrtacha'}</Badge>
      case 'LOW':
        return <Badge variant="outline" className="text-muted-foreground">{t.irrigationPlanning?.priority?.low || 'Past'}</Badge>
    }
  }

  const getStatusBadge = (status: IrrigationPlanStatus, isBlocked: boolean) => {
    if (isBlocked) {
      return (
        <Badge variant="destructive" className="flex items-center gap-1 font-bold animate-pulse">
          <ShieldAlert className="w-3 h-3" />
          {t.irrigationPlanning?.status?.blocked || 'BLOKLANGAN'}
        </Badge>
      )
    }
    switch (status) {
      case 'DRAFT':
        return <Badge variant="outline">{t.irrigationPlanning?.status?.draft || 'Qoralama'}</Badge>
      case 'PROPOSED':
        return <Badge className="bg-purple-600 text-white font-medium">{t.irrigationPlanning?.status?.proposed || 'Tavsiya etilgan'}</Badge>
      case 'APPROVED':
        return <Badge className="bg-blue-600 text-white font-medium">{t.irrigationPlanning?.status?.approved || 'Tasdiqlangan'}</Badge>
      case 'SCHEDULED':
        return <Badge className="bg-indigo-600 text-white font-medium">{t.irrigationPlanning?.status?.scheduled || 'Jadvalga kiritilgan'}</Badge>
      case 'IN_PROGRESS':
        return <Badge className="bg-emerald-600 text-white font-medium animate-pulse">{t.irrigationPlanning?.status?.inProgress || 'Sugʻorilmoqda'}</Badge>
      case 'COMPLETED':
        return <Badge variant="secondary" className="bg-emerald-500/10 text-emerald-700 dark:text-emerald-300 font-medium">{t.irrigationPlanning?.status?.completed || 'Bajarildi'}</Badge>
      case 'CANCELLED':
        return <Badge variant="secondary" className="text-muted-foreground line-through">{t.irrigationPlanning?.status?.cancelled || 'Bekor qilingan'}</Badge>
      case 'EXPIRED':
        return <Badge variant="outline" className="text-muted-foreground">{t.irrigationPlanning?.status?.expired || 'Muddati oʻtgan'}</Badge>
    }
  }

  const durationHours = (plan.estimatedDurationMinutes / 60).toFixed(1)

  return (
    <Card
      className={cn(
        'border transition-all duration-200 overflow-hidden shadow-sm hover:shadow-md',
        plan.isBlocked
          ? 'border-rose-400 dark:border-rose-900 bg-rose-50/10'
          : plan.status === 'PROPOSED'
          ? 'border-purple-300 dark:border-purple-900 bg-card'
          : 'border-border/80 bg-card',
        className
      )}
    >
      <CardHeader className="p-4 pb-3 border-b border-border/60 bg-muted/20">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="font-bold text-base text-foreground">
                {fieldName || `Dala: ${plan.fieldId.slice(0, 8)}...`}
              </span>
              {cropType && (
                <Badge variant="outline" className="text-[11px] font-medium">
                  {cropType} {areaHectares ? `• ${areaHectares} ga` : ''}
                </Badge>
              )}
            </div>
            <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
              <Calendar className="w-3.5 h-3.5 text-muted-foreground shrink-0" />
              <span>{plan.plannedDate}</span>
              <span>•</span>
              <Clock className="w-3.5 h-3.5 text-muted-foreground shrink-0" />
              <span>
                {formatDateTime(plan.plannedStartAt)} — {formatDateTime(plan.plannedEndAt)}
              </span>
            </div>
          </div>
          <div className="flex items-center gap-2 shrink-0">
            {getPriorityBadge(plan.priority)}
            {getStatusBadge(plan.status, plan.isBlocked)}
          </div>
        </div>
      </CardHeader>

      <CardContent className="p-4 space-y-4 text-xs">
        {/* Blocked Safety Interlock Banner */}
        {plan.isBlocked && (
          <div
            role="alert"
            className="p-3 rounded-lg border border-rose-300 dark:border-rose-900 bg-rose-50/70 dark:bg-rose-950/40 text-rose-800 dark:text-rose-200 space-y-1"
          >
            <div className="flex items-center gap-1.5 font-bold text-xs text-rose-600 dark:text-rose-400">
              <ShieldAlert className="w-4 h-4 shrink-0" />
              <span>{t.irrigationPlanning?.interlockBlocked || 'Gidravlik xavfsizlik: Sugʻorish bloklangan!'}</span>
            </div>
            <p className="text-[11px] leading-relaxed">
              {plan.blockedReason || 'Quvurda anomaliya yoki bosim keskin tushishi sababli ushbu reja bajarilishi taqiqlanadi.'}
            </p>
          </div>
        )}

        {/* Volumetric Metrics Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
          <div className="p-2.5 rounded-lg border border-border/70 bg-background/60 space-y-0.5">
            <span className="text-[10px] text-muted-foreground flex items-center gap-1">
              <Droplets className="w-3 h-3 text-sky-500" />
              {t.irrigationPlanning?.volume || 'Tavsiya hajmi'}
            </span>
            <span className="font-bold text-sm text-foreground block">
              {formatVolume(plan.recommendedVolumeM3)}
            </span>
          </div>

          <div className="p-2.5 rounded-lg border border-border/70 bg-background/60 space-y-0.5">
            <span className="text-[10px] text-muted-foreground flex items-center gap-1">
              <Clock className="w-3 h-3 text-amber-500" />
              {t.irrigationPlanning?.duration || 'Davomiyligi'}
            </span>
            <span className="font-bold text-sm text-foreground block">
              {durationHours} {t.irrigationPlanning?.hours || 'soat'} ({plan.estimatedDurationMinutes} daqiqa)
            </span>
          </div>

          <div className="p-2.5 rounded-lg border border-border/70 bg-background/60 space-y-0.5">
            <span className="text-[10px] text-muted-foreground flex items-center gap-1">
              <Gauge className="w-3 h-3 text-emerald-500" />
              {t.irrigationPlanning?.flowRate || 'Oqim tezligi'}
            </span>
            <span className="font-bold text-sm text-foreground block">
              {plan.recommendedFlowRateM3H ? `${plan.recommendedFlowRateM3H} m³/h` : '—'}
            </span>
          </div>

          <div className="p-2.5 rounded-lg border border-border/70 bg-background/60 space-y-0.5">
            <span className="text-[10px] text-muted-foreground flex items-center gap-1">
              <Layers className="w-3 h-3 text-purple-500" />
              {t.irrigationPlanning?.moistureTarget || 'Namlik (joriy / maqsad)'}
            </span>
            <span className="font-bold text-sm text-foreground block">
              {plan.currentMoisturePercent ? `${plan.currentMoisturePercent}%` : '—'} /{' '}
              {plan.targetMoisturePercent ? `${plan.targetMoisturePercent}%` : '—'}
            </span>
          </div>
        </div>

        {/* Explainability & Agronomic Reasoning */}
        <div className="space-y-1.5 p-3 rounded-lg border border-border/70 bg-muted/20">
          <div className="flex items-center gap-1.5 font-semibold text-foreground text-xs">
            <Sparkles className="w-3.5 h-3.5 text-purple-600 dark:text-purple-400" />
            <span>{t.irrigationPlanning?.reasoningTitle || 'Agronomik asos va izoh'}</span>
          </div>
          <p className="text-xs text-foreground/90 font-medium leading-relaxed">
            {plan.reason}
          </p>
          {plan.reasoningSummary && (
            <p className="text-[11px] text-muted-foreground leading-relaxed pt-1 border-t border-border/40">
              {plan.reasoningSummary}
            </p>
          )}
        </div>

        {/* Evaluated Plan Constraints */}
        {plan.constraints && plan.constraints.length > 0 && (
          <div className="space-y-1.5">
            <span className="font-semibold text-xs text-muted-foreground uppercase tracking-wider block">
              {t.irrigationPlanning?.constraintsTitle || 'Tekshirilgan cheklovlar (Gidravlik, Kvota, Ob-havo)'}:
            </span>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-1.5">
              {plan.constraints.map((c) => (
                <div
                  key={c.id}
                  className={cn(
                    'p-2 rounded-md border text-[11px] flex items-start gap-1.5',
                    c.status === 'BLOCKED' || c.status === 'VIOLATED'
                      ? 'border-rose-400 bg-rose-50/30 text-rose-800 dark:text-rose-200'
                      : c.status === 'WARNING'
                      ? 'border-amber-400 bg-amber-50/30 text-amber-800 dark:text-amber-200'
                      : 'border-border/60 bg-background/50 text-muted-foreground'
                  )}
                >
                  {c.status === 'BLOCKED' || c.status === 'VIOLATED' ? (
                    <XCircle className="w-3.5 h-3.5 text-rose-600 shrink-0 mt-0.5" />
                  ) : c.status === 'WARNING' ? (
                    <AlertTriangle className="w-3.5 h-3.5 text-amber-600 shrink-0 mt-0.5" />
                  ) : (
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 shrink-0 mt-0.5" />
                  )}
                  <span className="leading-snug">{c.message}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Cancellation Reason if Cancelled */}
        {plan.status === 'CANCELLED' && plan.cancellationReason && (
          <div className="p-2.5 rounded-lg border border-border bg-muted/40 text-muted-foreground">
            <span className="font-semibold text-xs block text-foreground">
              {t.irrigationPlanning?.cancellationReason || 'Bekor qilish sababi'}:
            </span>
            <p className="text-[11px] italic mt-0.5">{plan.cancellationReason}</p>
          </div>
        )}

        {/* Action Buttons */}
        <div className="pt-2 border-t border-border/60 flex flex-wrap items-center justify-end gap-2">
          {/* Approve button: enabled for DRAFT or PROPOSED, disabled if isBlocked */}
          {(plan.status === 'DRAFT' || plan.status === 'PROPOSED') && onApprove && (
            <Button
              size="sm"
              variant={plan.isBlocked ? 'outline' : 'default'}
              disabled={plan.isBlocked}
              onClick={() => onApprove(plan)}
              className={cn(
                'text-xs font-semibold',
                !plan.isBlocked && 'bg-emerald-600 hover:bg-emerald-700 text-white'
              )}
            >
              <CheckCircle2 className="w-3.5 h-3.5 mr-1.5" />
              {t.irrigationPlanning?.approveButton || 'Tasdiqlash'}
            </Button>
          )}

          {/* Complete button: for SCHEDULED or IN_PROGRESS */}
          {(plan.status === 'SCHEDULED' || plan.status === 'IN_PROGRESS') && onComplete && (
            <Button
              size="sm"
              variant="default"
              onClick={() => onComplete(plan)}
              className="text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white"
            >
              <CheckCircle2 className="w-3.5 h-3.5 mr-1.5" />
              {t.irrigationPlanning?.completeButton || 'Tugatish'}
            </Button>
          )}

          {/* Cancel button: for any active non-terminal state */}
          {plan.status !== 'COMPLETED' && plan.status !== 'CANCELLED' && plan.status !== 'EXPIRED' && onCancel && (
            <Button
              size="sm"
              variant="outline"
              onClick={() => onCancel(plan)}
              className="text-xs text-rose-600 hover:text-rose-700 hover:bg-rose-50 dark:hover:bg-rose-950/30"
            >
              <XCircle className="w-3.5 h-3.5 mr-1.5" />
              {t.irrigationPlanning?.cancelButton || 'Bekor qilish'}
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
