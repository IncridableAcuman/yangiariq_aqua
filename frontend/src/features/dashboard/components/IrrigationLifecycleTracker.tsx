import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import {
  Calendar,
  CheckCircle2,
  Clock,
  Play,
  FileText,
  AlertOctagon,
  ArrowRight,
  ShieldCheck,
} from 'lucide-react'
import { IrrigationMetrics } from '@/entities/dashboard'
import { SupportedLanguage } from '@/shared/i18n'
import { NavSection } from '@/widgets/sidebar'

interface IrrigationLifecycleTrackerProps {
  metrics: IrrigationMetrics
  lang: SupportedLanguage
  onNavigate: (section: NavSection) => void
}

export const IrrigationLifecycleTracker: React.FC<IrrigationLifecycleTrackerProps> = ({
  metrics,
  lang,
  onNavigate,
}) => {
  const t = {
    uz: {
      title: 'Sugʻorish Bosqichlari (Lifecycle)',
      subtitle: 'Bugungi operatsion taqsimot va rejalarning joriy holati',
      draft: 'Qoralama',
      proposed: 'Tasdiq Kutilmoqda',
      approved: 'Tasdiqlangan',
      scheduledToday: 'Bugungi Navbat',
      inProgress: 'Ochilgan / Faol',
      completed: 'Muvaffaqiyatli Tugagan',
      blocked: 'Gidravlik Blokda',
      openPlanner: 'Sugʻorish Jurnali',
    },
    ru: {
      title: 'Жизненный Цикл Орошения (Lifecycle)',
      subtitle: 'Текущий статус планов полива и суточный график подачи воды',
      draft: 'Черновик',
      proposed: 'Ожидает Одобрения',
      approved: 'Одобрено',
      scheduledToday: 'Сегодня в Графике',
      inProgress: 'Полив Выполняется',
      completed: 'Завершено',
      blocked: 'Блокировка Защитой',
      openPlanner: 'Журнал Орошения',
    },
    en: {
      title: 'Irrigation Lifecycle Pipeline',
      subtitle: 'Operational execution states across scheduled parcel deliveries',
      draft: 'Draft',
      proposed: 'Awaiting Approval',
      approved: 'Approved',
      scheduledToday: 'Scheduled Today',
      inProgress: 'In Progress',
      completed: 'Completed',
      blocked: 'Safety Blocked',
      openPlanner: 'Open Irrigation',
    },
  }[lang]

  const stages = [
    {
      label: t.draft,
      count: metrics.draftCount,
      icon: FileText,
      color: 'text-muted-foreground',
      bg: 'bg-muted/50',
    },
    {
      label: t.proposed,
      count: metrics.proposedCount,
      icon: Clock,
      color: 'text-amber-600 dark:text-amber-400',
      bg: metrics.proposedCount > 0 ? 'bg-amber-500/15 border-amber-500/30' : 'bg-muted/40',
      highlight: metrics.proposedCount > 0,
    },
    {
      label: t.approved,
      count: metrics.approvedCount,
      icon: ShieldCheck,
      color: 'text-sky-600 dark:text-sky-400',
      bg: 'bg-sky-500/10',
    },
    {
      label: t.scheduledToday,
      count: metrics.scheduledTodayCount,
      icon: Calendar,
      color: 'text-primary',
      bg: metrics.scheduledTodayCount > 0 ? 'bg-primary/15 border-primary/30' : 'bg-muted/40',
      highlight: metrics.scheduledTodayCount > 0,
    },
    {
      label: t.inProgress,
      count: metrics.inProgressCount,
      icon: Play,
      color: 'text-emerald-600 dark:text-emerald-400',
      bg: metrics.inProgressCount > 0 ? 'bg-emerald-500/15 border-emerald-500/30' : 'bg-muted/40',
      highlight: metrics.inProgressCount > 0,
    },
    {
      label: t.completed,
      count: metrics.completedCount,
      icon: CheckCircle2,
      color: 'text-muted-foreground',
      bg: 'bg-muted/30',
    },
  ]

  return (
    <Card className="shadow-xs border-border/80">
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div>
            <CardTitle className="text-base font-bold text-foreground flex items-center gap-2">
              <Calendar className="w-5 h-5 text-primary" />
              <span>{t.title}</span>
            </CardTitle>
            <CardDescription className="text-xs">
              {t.subtitle}
            </CardDescription>
          </div>

          <Button
            variant="outline"
            size="sm"
            className="text-xs self-start sm:self-auto gap-1"
            onClick={() => onNavigate('irrigation')}
          >
            <span>{t.openPlanner}</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Button>
        </div>
      </CardHeader>

      <CardContent className="pt-4 space-y-4">
        {/* Safety Block Alert Banner if any blocked */}
        {metrics.blockedCount > 0 && (
          <div className="p-3 rounded-lg border border-rose-500/30 bg-rose-500/10 flex items-center justify-between gap-3 text-xs">
            <div className="flex items-center gap-2 text-rose-700 dark:text-rose-300 font-semibold">
              <AlertOctagon className="w-4 h-4 shrink-0 animate-pulse" />
              <span>{metrics.blockedCount} ta dalada gidravlik xavfsizlik bloki faol (quvur yorilishi / bosim pasayishi)</span>
            </div>
            <Button
              variant="destructive"
              size="sm"
              className="h-7 text-xs px-2.5 shrink-0"
              onClick={() => onNavigate('anomalies')}
            >
              Tekshirish
            </Button>
          </div>
        )}

        {/* 6-Stage Horizontal Pipeline Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-2.5">
          {stages.map((stage, idx) => {
            const Icon = stage.icon
            return (
              <div
                key={idx}
                className={`p-3 rounded-lg border transition-colors flex flex-col items-center text-center space-y-1.5 ${
                  stage.bg
                } ${stage.highlight ? 'border-primary/40 shadow-xs' : 'border-border/60'}`}
              >
                <div className={`h-7 w-7 rounded-md flex items-center justify-center ${stage.color}`}>
                  <Icon className="w-4 h-4" />
                </div>
                <div className="text-xl font-bold font-mono text-foreground">
                  {stage.count}
                </div>
                <span className="text-[11px] font-medium text-muted-foreground leading-tight">
                  {stage.label}
                </span>
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}
