import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import {
  AlertOctagon,
  AlertTriangle,
  ArrowRight,
  CheckCircle2,
  Droplets,
  Flame,
  HelpCircle,
  Radio,
  ShieldAlert,
  Sparkles,
  Sliders,
  TrendingDown,
} from 'lucide-react'
import { UrgentAction } from '@/entities/dashboard'
import { SupportedLanguage } from '@/shared/i18n'
import { NavSection } from '@/widgets/sidebar'

interface ActionCenterProps {
  urgentActions: UrgentAction[]
  lang: SupportedLanguage
  onNavigate: (section: NavSection, fieldId?: string) => void
}

export const ActionCenter: React.FC<ActionCenterProps> = ({
  urgentActions,
  lang,
  onNavigate,
}) => {
  const t = {
    uz: {
      title: 'Harakatlar Markazi (Action Center)',
      subtitle: 'Zudlik bilan inson qarorini talab etuvchi operatsion vazifalar',
      noActionsTitle: 'Barcha holatlar meʼyorda',
      noActionsSubtitle: 'Hozirda zudlik bilan chora koʻrish talab etiladigan gidravlik, agronomik yoki datchik muammolari aniqlanmadi.',
      criticalBanner: 'DIQQAT: ZUDLIK BILAN CHORA KOʻRISH TALAB ETILADI',
      viewAll: 'Barchasi',
      field: 'Dala',
    },
    ru: {
      title: 'Центр Действий (Action Center)',
      subtitle: 'Операционные задачи, требующие срочного решения оператора или фермера',
      noActionsTitle: 'Все показатели в норме',
      noActionsSubtitle: 'В данный момент не обнаружено критических гидравлических, агрономических или сенсорных проблем.',
      criticalBanner: 'ВНИМАНИЕ: ТРЕБУЕТСЯ НЕОТЛОЖНОЕ ВМЕШАТЕЛЬСТВО',
      viewAll: 'Все',
      field: 'Поле',
    },
    en: {
      title: 'Farm Action Center',
      subtitle: 'Prioritized operational decisions requiring human confirmation',
      noActionsTitle: 'All systems operational',
      noActionsSubtitle: 'No active hydraulic safety blocks, critical drought alerts, or offline sensors detected.',
      criticalBanner: 'CRITICAL: IMMEDIATE ATTENTION REQUIRED',
      viewAll: 'All',
      field: 'Field',
    },
  }[lang]

  const getActionIcon = (actionType: string) => {
    switch (actionType) {
      case 'HYDRAULIC_SAFETY_BLOCK':
        return <AlertOctagon className="w-5 h-5 text-rose-600 dark:text-rose-400 shrink-0 animate-pulse" />
      case 'CRITICAL_DROUGHT':
        return <Flame className="w-5 h-5 text-rose-600 dark:text-rose-400 shrink-0" />
      case 'PLAN_APPROVAL_PENDING':
        return <Droplets className="w-5 h-5 text-amber-600 dark:text-amber-400 shrink-0" />
      case 'ACTIVE_ANOMALY':
        return <ShieldAlert className="w-5 h-5 text-rose-500 shrink-0" />
      case 'WATER_BUDGET_EXHAUSTED':
        return <AlertTriangle className="w-5 h-5 text-amber-500 shrink-0" />
      case 'SENSOR_OFFLINE':
      case 'STALE_TELEMETRY':
        return <Radio className="w-5 h-5 text-sky-500 shrink-0" />
      case 'RECOMMENDATION_READY':
        return <Sparkles className="w-5 h-5 text-primary shrink-0" />
      case 'CALIBRATION_PROPOSAL_PENDING':
        return <Sliders className="w-5 h-5 text-purple-600 dark:text-purple-400 shrink-0" />
      case 'MODEL_DRIFT_ALERT':
        return <TrendingDown className="w-5 h-5 text-amber-500 shrink-0" />
      default:
        return <HelpCircle className="w-5 h-5 text-muted-foreground shrink-0" />
    }
  }

  const getSeverityBadge = (severity: string) => {
    switch (severity.toUpperCase()) {
      case 'CRITICAL':
        return (
          <Badge variant="outline" className="bg-rose-500/15 text-rose-700 border-rose-400 dark:bg-rose-950/50 dark:text-rose-300 dark:border-rose-700 font-bold text-[10px] py-0.5 px-2">
            CRITICAL
          </Badge>
        )
      case 'HIGH':
        return (
          <Badge variant="outline" className="bg-amber-500/15 text-amber-700 border-amber-400 dark:bg-amber-950/50 dark:text-amber-300 dark:border-amber-700 font-semibold text-[10px] py-0.5 px-2">
            HIGH
          </Badge>
        )
      case 'MEDIUM':
        return (
          <Badge variant="outline" className="bg-sky-500/15 text-sky-700 border-sky-400 dark:bg-sky-950/50 dark:text-sky-300 dark:border-sky-700 font-medium text-[10px] py-0.5 px-2">
            MEDIUM
          </Badge>
        )
      default:
        return (
          <Badge variant="outline" className="text-[10px] py-0.5 px-2">
            {severity}
          </Badge>
        )
    }
  }

  if (urgentActions.length === 0) {
    return (
      <Card className="border-emerald-500/30 bg-emerald-500/5 shadow-xs">
        <CardContent className="p-6 flex flex-col sm:flex-row items-center gap-4 text-center sm:text-left">
          <div className="h-12 w-12 rounded-xl bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <div className="space-y-1">
            <h3 className="font-semibold text-base text-foreground flex items-center justify-center sm:justify-start gap-2">
              <span>{t.noActionsTitle}</span>
            </h3>
            <p className="text-xs sm:text-sm text-muted-foreground max-w-2xl">
              {t.noActionsSubtitle}
            </p>
          </div>
        </CardContent>
      </Card>
    )
  }

  const hasCritical = urgentActions.some(a => a.severity.toUpperCase() === 'CRITICAL')

  return (
    <Card className={`border shadow-xs ${
      hasCritical
        ? 'border-rose-500/40 bg-gradient-to-br from-card via-card to-rose-500/5'
        : 'border-border/80'
    }`}>
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <div className="h-2 w-2 rounded-full bg-rose-500 animate-ping" />
              <CardTitle className="text-base font-bold text-foreground">
                {t.title}
              </CardTitle>
              <Badge variant="secondary" className="font-mono text-xs">
                {urgentActions.length}
              </Badge>
            </div>
            <CardDescription className="text-xs">
              {t.subtitle}
            </CardDescription>
          </div>

          {hasCritical && (
            <Badge variant="outline" className="bg-rose-500/10 text-rose-600 border-rose-500/30 text-[11px] font-semibold py-1 px-2.5 self-start sm:self-auto animate-pulse">
              {t.criticalBanner}
            </Badge>
          )}
        </div>
      </CardHeader>

      <CardContent className="p-0 divide-y divide-border/60">
        {urgentActions.map((action) => (
          <div
            key={action.id}
            className={`p-4 transition-colors flex flex-col md:flex-row md:items-center justify-between gap-4 ${
              action.severity.toUpperCase() === 'CRITICAL'
                ? 'bg-rose-500/5 hover:bg-rose-500/10'
                : action.severity.toUpperCase() === 'HIGH'
                ? 'bg-amber-500/5 hover:bg-amber-500/10'
                : 'hover:bg-muted/40'
            }`}
          >
            {/* Left Content */}
            <div className="flex items-start gap-3.5 min-w-0">
              <div className="mt-0.5">
                {getActionIcon(action.actionType)}
              </div>
              <div className="space-y-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  {getSeverityBadge(action.severity)}
                  {action.fieldName && (
                    <Badge variant="outline" className="text-xs font-medium">
                      {t.field}: {action.fieldName}
                    </Badge>
                  )}
                  <h4 className="font-semibold text-sm text-foreground truncate">
                    {action.title}
                  </h4>
                </div>
                <p className="text-xs text-muted-foreground leading-relaxed max-w-3xl">
                  {action.description}
                </p>
              </div>
            </div>

            {/* Right Action Button */}
            <div className="flex items-center gap-2 shrink-0 self-end md:self-center">
              <Button
                variant={action.severity.toUpperCase() === 'CRITICAL' ? 'destructive' : 'default'}
                size="sm"
                className="gap-1.5 text-xs shadow-xs"
                onClick={() => {
                  if (action.targetSection === 'operations' && action.fieldId) {
                    onNavigate('operations', action.fieldId)
                  } else {
                    onNavigate(action.targetSection as NavSection)
                  }
                }}
              >
                <span>{action.actionLabel}</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </Button>
            </div>
          </div>
        ))}
      </CardContent>
    </Card>
  )
}
