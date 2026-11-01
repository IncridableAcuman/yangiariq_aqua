import React, { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { SmartIrrigationPlan } from '@/entities/irrigation'
import {
  Droplets,
  Clock,
  Calendar,
  AlertTriangle,
  ShieldAlert,
  CheckCircle2,
} from 'lucide-react'
import { formatVolume, formatDateTime } from '@/features/water/lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { cn } from '@/shared/lib/utils'

interface PlanApprovalDialogProps {
  plan: SmartIrrigationPlan | null
  fieldName?: string
  cropType?: string
  open: boolean
  onOpenChange: (open: boolean) => void
  onConfirm: (planId: string, acknowledgeWarnings: boolean) => Promise<void> | void
  isSubmitting?: boolean
  lang: SupportedLanguage
}

export const PlanApprovalDialog: React.FC<PlanApprovalDialogProps> = ({
  plan,
  fieldName,
  cropType,
  open,
  onOpenChange,
  onConfirm,
  isSubmitting = false,
  lang,
}) => {
  const t = translations[lang]
  const [acknowledged, setAcknowledged] = useState<boolean>(false)

  if (!plan) return null

  const warnings = plan.constraints?.filter((c) => c.status === 'WARNING') || []
  const hasWarnings = warnings.length > 0
  const isBlocked = plan.isBlocked

  // Can approve only if not blocked, and if warnings exist, user must acknowledge
  const canApprove = !isBlocked && (!hasWarnings || acknowledged)

  const handleConfirm = async () => {
    if (!canApprove) return
    await onConfirm(plan.id, acknowledged)
    setAcknowledged(false)
    onOpenChange(false)
  }

  const durationHours = (plan.estimatedDurationMinutes / 60).toFixed(1)

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        if (!next) setAcknowledged(false)
        onOpenChange(next)
      }}
    >
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-base font-bold text-foreground">
            <CheckCircle2 className="w-5 h-5 text-emerald-600" />
            <span>{t.irrigationPlanning?.approval?.dialogTitle || 'Sugʻorish rejasini tasdiqlash'}</span>
          </DialogTitle>
          <DialogDescription className="text-xs text-muted-foreground">
            {t.irrigationPlanning?.approval?.dialogSubtitle ||
              'Ushbu operatsion rejani tasdiqlash suv kvotasi va tarmoq jadvallariga kiritiladi.'}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-3.5 text-xs">
          {/* Safety Interlock Alert */}
          {isBlocked && (
            <div
              role="alert"
              className="p-3 rounded-lg border border-rose-400 bg-rose-50 dark:bg-rose-950/40 text-rose-800 dark:text-rose-200 space-y-1.5"
            >
              <div className="flex items-center gap-1.5 font-bold text-xs text-rose-600 dark:text-rose-400">
                <ShieldAlert className="w-4 h-4 shrink-0" />
                <span>{t.irrigationPlanning?.approval?.blockedTitle || 'Gidravlik xavfsizlik: Reja tasdiqlanishi mumkin emas!'}</span>
              </div>
              <p className="text-[11px] leading-relaxed">
                {plan.blockedReason ||
                  'Gidravlik interlock quvurda anomaliya yoki bosim keskin tushishini aniqlagan. Infratuzilma xavfsizligi tiklanmaguncha tasdiqlash taqiqlanadi.'}
              </p>
            </div>
          )}

          {/* Operational Warnings Banner */}
          {!isBlocked && hasWarnings && (
            <div
              role="alert"
              className="p-3 rounded-lg border border-amber-300 dark:border-amber-900 bg-amber-50/80 dark:bg-amber-950/30 text-amber-900 dark:text-amber-200 space-y-2"
            >
              <div className="flex items-center gap-1.5 font-bold text-xs text-amber-700 dark:text-amber-400">
                <AlertTriangle className="w-4 h-4 shrink-0" />
                <span>{t.irrigationPlanning?.approval?.warningsTitle || 'Diqqat: Operatsion ogohlantirishlar mavjud'}</span>
              </div>
              <ul className="space-y-1 pl-4 list-disc text-[11px]">
                {warnings.map((w) => (
                  <li key={w.id}>{w.message}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Plan Attributes Summary Table */}
          <div className="rounded-lg border border-border/80 bg-muted/20 divide-y divide-border/60">
            <div className="p-2.5 flex items-center justify-between">
              <span className="text-muted-foreground">{t.irrigationPlanning?.approval?.field || 'Dala'}:</span>
              <span className="font-semibold text-foreground">
                {fieldName || plan.fieldId} {cropType ? `(${cropType})` : ''}
              </span>
            </div>

            <div className="p-2.5 flex items-center justify-between">
              <span className="text-muted-foreground flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5" />
                {t.irrigationPlanning?.approval?.date || 'Belgilangan sana'}:
              </span>
              <span className="font-semibold text-foreground">{plan.plannedDate}</span>
            </div>

            <div className="p-2.5 flex items-center justify-between">
              <span className="text-muted-foreground flex items-center gap-1">
                <Clock className="w-3.5 h-3.5" />
                {t.irrigationPlanning?.approval?.timeWindow || 'Sugʻorish oraligʻi'}:
              </span>
              <span className="font-semibold text-foreground">
                {formatDateTime(plan.plannedStartAt)} — {formatDateTime(plan.plannedEndAt)}
              </span>
            </div>

            <div className="p-2.5 flex items-center justify-between">
              <span className="text-muted-foreground flex items-center gap-1">
                <Droplets className="w-3.5 h-3.5 text-sky-500" />
                {t.irrigationPlanning?.approval?.recommendedVolume || 'Tavsiya etilgan suv hajmi'}:
              </span>
              <span className="font-bold text-primary text-sm">
                {formatVolume(plan.recommendedVolumeM3)}
              </span>
            </div>

            <div className="p-2.5 flex items-center justify-between">
              <span className="text-muted-foreground">{t.irrigationPlanning?.approval?.duration || 'Davomiyligi'}:</span>
              <span className="font-semibold text-foreground">
                {durationHours} {t.irrigationPlanning?.hours || 'soat'} ({plan.estimatedDurationMinutes} daqiqa)
              </span>
            </div>

            <div className="p-2.5 flex items-center justify-between">
              <span className="text-muted-foreground">{t.irrigationPlanning?.approval?.priority || 'Ustuvorlik darajasi'}:</span>
              <Badge variant="outline" className="font-bold">
                {plan.priority}
              </Badge>
            </div>

            <div className="p-2.5 flex items-center justify-between">
              <span className="text-muted-foreground">{t.irrigationPlanning?.approval?.confidence || 'Tavsiya ishonchliligi'}:</span>
              <span className="font-medium text-foreground">
                {plan.confidenceLevel} {plan.confidenceScore ? `(${Math.round(plan.confidenceScore * 100)}%)` : ''}
              </span>
            </div>
          </div>

          {/* Reasoning */}
          <div className="p-2.5 rounded-lg border border-border/70 bg-background/50 space-y-1">
            <span className="text-[11px] font-semibold text-muted-foreground block">
              {t.irrigationPlanning?.approval?.reasonTitle || 'Asoslash va algoritmik hisob'}:
            </span>
            <p className="text-[11px] text-foreground leading-relaxed">
              {plan.reason}
            </p>
          </div>

          {/* Warning Acknowledgment Checkbox */}
          {!isBlocked && hasWarnings && (
            <label className="flex items-start gap-2 pt-1 cursor-pointer select-none">
              <input
                type="checkbox"
                checked={acknowledged}
                onChange={(e) => setAcknowledged(e.target.checked)}
                className="mt-0.5 rounded border-border text-primary focus:ring-primary h-4 w-4"
              />
              <span className="text-xs font-semibold text-foreground leading-snug">
                {t.irrigationPlanning?.approval?.ackCheckbox ||
                  'Men yuqoridagi operatsion ogohlantirishlarni koʻrib chiqdim va sugʻorishga rozilik bildiraman.'}
              </span>
            </label>
          )}
        </div>

        <DialogFooter className="gap-2 sm:gap-0 pt-2 border-t border-border/60">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={() => onOpenChange(false)}
            disabled={isSubmitting}
          >
            {t.common?.cancel || 'Bekor qilish'}
          </Button>

          <Button
            type="button"
            size="sm"
            onClick={handleConfirm}
            disabled={!canApprove || isSubmitting}
            className={cn(
              'font-semibold text-xs',
              canApprove && 'bg-emerald-600 hover:bg-emerald-700 text-white'
            )}
          >
            {isSubmitting ? (
              <span>{t.common?.loading || 'Yuklanmoqda...'}</span>
            ) : (
              <span className="flex items-center gap-1.5">
                <CheckCircle2 className="w-3.5 h-3.5" />
                {t.irrigationPlanning?.approval?.confirmButton || 'Rejani tasdiqlash'}
              </span>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
