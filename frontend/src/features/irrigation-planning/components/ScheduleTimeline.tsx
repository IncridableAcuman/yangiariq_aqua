import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { SmartIrrigationPlan } from '@/entities/irrigation'
import {
  CalendarClock,
  Clock,
  Droplets,
  Moon,
  Sun,
  ShieldAlert,
  ArrowRight,
} from 'lucide-react'
import { formatVolume } from '@/features/water/lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { cn } from '@/shared/lib/utils'

interface ScheduleTimelineProps {
  plans: SmartIrrigationPlan[]
  fieldNamesById?: Record<string, string>
  cropsById?: Record<string, string>
  onSelectPlan?: (plan: SmartIrrigationPlan) => void
  lang: SupportedLanguage
  className?: string
}

export const ScheduleTimeline: React.FC<ScheduleTimelineProps> = ({
  plans,
  fieldNamesById = {},
  cropsById = {},
  onSelectPlan,
  lang,
  className,
}) => {
  const t = translations[lang]

  // Filter plans that are active or scheduled
  const activePlans = [...plans]
    .filter((p) => p.status !== 'CANCELLED' && p.status !== 'EXPIRED')
    .sort((a, b) => new Date(a.plannedStartAt).getTime() - new Date(b.plannedStartAt).getTime())

  const formatTimeOnly = (iso: string) => {
    try {
      const d = new Date(iso)
      if (isNaN(d.getTime())) return iso
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false })
    } catch {
      return iso
    }
  }

  const isNocturnal = (iso: string) => {
    try {
      const hours = new Date(iso).getHours()
      return hours >= 21 || hours < 6
    } catch {
      return false
    }
  }

  return (
    <Card className={cn('border border-border/70 shadow-sm bg-card overflow-hidden', className)}>
      <CardHeader className="p-4 pb-3 border-b border-border/60 bg-muted/20 flex flex-row items-center justify-between">
        <div className="flex items-center gap-2">
          <CalendarClock className="w-4 h-4 text-primary" />
          <CardTitle className="text-sm font-bold text-foreground">
            {t.irrigationPlanning?.timeline?.title || 'Sugʻorish Navbati & Operatsion Jadval'}
          </CardTitle>
        </div>
        <Badge variant="outline" className="text-xs font-semibold">
          {activePlans.length} {t.irrigationPlanning?.timeline?.turnsScheduled || 'navbat belgilangan'}
        </Badge>
      </CardHeader>

      <CardContent className="p-4">
        {activePlans.length === 0 ? (
          <div className="py-8 text-center text-muted-foreground text-xs space-y-1">
            <Clock className="w-8 h-8 mx-auto text-muted-foreground/50 mb-2" />
            <p className="font-semibold text-foreground">
              {t.irrigationPlanning?.timeline?.noPlansTitle || 'Jadvalda faol sugʻorish navbatlari yoʻq'}
            </p>
            <p>
              {t.irrigationPlanning?.timeline?.noPlansDesc ||
                'Dalalar uchun yangi AI-reja generatsiya qiling yoki mavjud rejalarni tasdiqlang.'}
            </p>
          </div>
        ) : (
          <div className="relative border-l border-border/80 ml-3 pl-4 space-y-4 my-1">
            {activePlans.map((plan) => {
              const nocturnal = isNocturnal(plan.plannedStartAt)
              const startTime = formatTimeOnly(plan.plannedStartAt)
              const endTime = formatTimeOnly(plan.plannedEndAt)
              const fieldName = fieldNamesById[plan.fieldId] || `Dala: ${plan.fieldId.slice(0, 6)}...`
              const crop = cropsById[plan.fieldId]

              return (
                <div
                  key={plan.id}
                  onClick={() => onSelectPlan?.(plan)}
                  className={cn(
                    'relative p-3 rounded-xl border transition-all cursor-pointer group flex flex-col sm:flex-row sm:items-center justify-between gap-3',
                    plan.isBlocked
                      ? 'border-rose-400 bg-rose-50/20 hover:bg-rose-50/40 dark:bg-rose-950/20'
                      : plan.status === 'IN_PROGRESS'
                      ? 'border-emerald-500 bg-emerald-50/30 dark:bg-emerald-950/30 shadow-xs'
                      : plan.status === 'APPROVED' || plan.status === 'SCHEDULED'
                      ? 'border-primary/50 bg-background/80 hover:bg-muted/40'
                      : 'border-border/60 bg-muted/10 hover:bg-muted/30'
                  )}
                >
                  {/* Timeline bullet dot */}
                  <div
                    className={cn(
                      'absolute -left-[23px] top-4.5 w-3.5 h-3.5 rounded-full border-2 bg-card',
                      plan.isBlocked
                        ? 'border-rose-500 bg-rose-500'
                        : plan.status === 'IN_PROGRESS'
                        ? 'border-emerald-500 bg-emerald-500 animate-ping'
                        : plan.status === 'APPROVED'
                        ? 'border-primary bg-primary'
                        : 'border-muted-foreground bg-muted'
                    )}
                  />

                  {/* Left: Time and Parcel Info */}
                  <div className="space-y-1">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-bold text-sm text-foreground flex items-center gap-1.5">
                        <Clock className="w-3.5 h-3.5 text-primary" />
                        {startTime} — {endTime}
                      </span>

                      {nocturnal && (
                        <Badge
                          variant="secondary"
                          className="text-[10px] bg-indigo-500/10 text-indigo-700 dark:text-indigo-300 flex items-center gap-1 py-0 px-1.5"
                        >
                          <Moon className="w-3 h-3" />
                          <span>{t.irrigationPlanning?.timeline?.nocturnal || 'Tungi salqin'}</span>
                        </Badge>
                      )}

                      {!nocturnal && (
                        <Badge
                          variant="outline"
                          className="text-[10px] text-amber-600 dark:text-amber-400 flex items-center gap-1 py-0 px-1.5"
                        >
                          <Sun className="w-3 h-3" />
                          <span>{t.irrigationPlanning?.timeline?.daytime || 'Kunduzgi'}</span>
                        </Badge>
                      )}

                      <span className="text-xs text-muted-foreground font-semibold">
                        • {plan.plannedDate}
                      </span>
                    </div>

                    <div className="flex items-center gap-2 pt-0.5">
                      <span className="font-bold text-xs text-foreground group-hover:text-primary transition-colors">
                        {fieldName}
                      </span>
                      {crop && (
                        <Badge variant="outline" className="text-[10px] py-0 px-1.5">
                          {crop}
                        </Badge>
                      )}
                      {plan.isBlocked && (
                        <Badge variant="destructive" className="text-[10px] py-0 px-1.5 flex items-center gap-1">
                          <ShieldAlert className="w-3 h-3" />
                          Bloklangan
                        </Badge>
                      )}
                    </div>
                  </div>

                  {/* Right: Water Volume & Priority */}
                  <div className="flex items-center gap-4 shrink-0 sm:text-right">
                    <div className="space-y-0.5">
                      <div className="flex items-center sm:justify-end gap-1 text-xs font-bold text-foreground">
                        <Droplets className="w-3.5 h-3.5 text-sky-500" />
                        <span>{formatVolume(plan.recommendedVolumeM3)}</span>
                      </div>
                      <span className="text-[11px] text-muted-foreground block">
                        {(plan.estimatedDurationMinutes / 60).toFixed(1)} soat ({plan.estimatedDurationMinutes} daq)
                      </span>
                    </div>

                    <div className="space-y-1">
                      <Badge
                        variant="outline"
                        className={cn(
                          'text-[10px] uppercase font-bold py-0.5 px-2 block text-center',
                          plan.priority === 'CRITICAL' && 'border-rose-500 text-rose-600 bg-rose-50 dark:bg-rose-950/30',
                          plan.priority === 'HIGH' && 'border-amber-500 text-amber-600 bg-amber-50 dark:bg-amber-950/30',
                          plan.priority === 'MEDIUM' && 'border-sky-500 text-sky-600',
                          plan.priority === 'LOW' && 'text-muted-foreground'
                        )}
                      >
                        {plan.priority}
                      </Badge>

                      <span className="text-[10px] text-muted-foreground block text-center">
                        {plan.status}
                      </span>
                    </div>

                    <ArrowRight className="w-4 h-4 text-muted-foreground group-hover:text-primary group-hover:translate-x-0.5 transition-all hidden sm:block" />
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
