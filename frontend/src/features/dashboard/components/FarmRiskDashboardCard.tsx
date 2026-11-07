import React from 'react'
import {
  AlertOctagon,
  ArrowRight,
  ExternalLink,
  ShieldAlert,
} from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { SupportedLanguage } from '@/shared/i18n'
import { NavSection } from '@/widgets/sidebar'
import { useRiskOverviewQuery } from '@/features/risk/queries/useRiskQueries'
import { cn } from '@/shared/lib/utils'

interface FarmRiskDashboardCardProps {
  lang: SupportedLanguage
  onNavigate: (section: NavSection, fieldId?: string) => void
}

export const FarmRiskDashboardCard: React.FC<FarmRiskDashboardCardProps> = ({
  lang,
  onNavigate,
}) => {
  const { data: overview, isLoading } = useRiskOverviewQuery()

  const t = {
    uz: {
      title: 'Prediktiv Xavflar va Barqarorlik',
      subtitle: 'Gidravlik xavfsizlik, suv taqchilligi va telemetriya ishonchliligi monitoringi',
      hydraulicAlert: 'DIQQAT: Gidravlik xavfsizlik blokirovkasi faol!',
      critical: 'Kritik xavflar',
      waterShortage: 'Suv taqchilligi',
      telemetryGaps: 'Telemetriya uzilishi',
      nominal: 'Barcha xavf indikatorlari ruxsat etilgan xavfsiz meʼyorda',
      viewAll: 'Xavf Markazini ochish',
      topActions: 'Eng muhim tavsiyalar',
      execute: 'Bajarish',
    },
    ru: {
      title: 'Предиктивные Риски и Устойчивость',
      subtitle: 'Мониторинг гидравлической безопасности, дефицита воды и датчиков',
      hydraulicAlert: 'ВНИМАНИЕ: Активна гидравлическая блокировка безопасности!',
      critical: 'Критические риски',
      waterShortage: 'Дефицит воды',
      telemetryGaps: 'Проблемы датчиков',
      nominal: 'Все показатели рисков находятся в пределах нормы',
      viewAll: 'Открыть Центр Рисков',
      topActions: 'Приоритетные рекомендации',
      execute: 'Выполнить',
    },
    en: {
      title: 'Predictive Risk & Resilience',
      subtitle: 'Evidence-based monitoring across hydraulic safety, water balance, and telemetry',
      hydraulicAlert: 'CRITICAL: Hydraulic safety interlock active!',
      critical: 'Critical Risks',
      waterShortage: 'Water Shortage',
      telemetryGaps: 'Sensor Gaps',
      nominal: 'All monitored risk parameters within nominal safety thresholds',
      viewAll: 'Open Risk Intelligence Center',
      topActions: 'Top Prioritized Actions',
      execute: 'Execute',
    },
  }[lang]

  if (isLoading) {
    return (
      <Card className="animate-pulse">
        <CardHeader className="pb-3">
          <div className="h-5 w-48 bg-muted rounded" />
          <div className="h-4 w-72 bg-muted/60 rounded" />
        </CardHeader>
        <CardContent>
          <div className="h-20 bg-muted/40 rounded-xl" />
        </CardContent>
      </Card>
    )
  }

  const hasHydraulicEmergency = (overview?.hydraulicEmergencyCount ?? 0) > 0
  const hasCritical = (overview?.criticalRiskCount ?? 0) > 0
  const topRecommendations = overview?.topPrioritizedRecommendations?.slice(0, 2) || []

  return (
    <Card
      className={cn(
        'transition-all border',
        hasHydraulicEmergency
          ? 'bg-rose-500/5 border-rose-300 dark:border-rose-900/60'
          : 'bg-card'
      )}
      data-testid="farm-risk-dashboard-card"
    >
      <CardHeader className="pb-3 flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <CardTitle className="text-base font-bold text-foreground flex items-center gap-2">
              <ShieldAlert className={cn('w-4 h-4', hasHydraulicEmergency ? 'text-rose-600' : 'text-primary')} />
              <span>{t.title}</span>
            </CardTitle>
            {hasHydraulicEmergency ? (
              <Badge variant="destructive" className="text-[10px] uppercase font-bold animate-pulse">
                Interlock
              </Badge>
            ) : hasCritical ? (
              <Badge variant="outline" className="text-[10px] uppercase font-bold bg-rose-500/10 text-rose-700 border-rose-300">
                Critical Alert
              </Badge>
            ) : (
              <Badge variant="outline" className="text-[10px] uppercase font-semibold bg-emerald-500/10 text-emerald-700 border-emerald-300">
                Nominal
              </Badge>
            )}
          </div>
          <CardDescription className="text-xs">{t.subtitle}</CardDescription>
        </div>

        <Button
          variant="ghost"
          size="sm"
          className="h-8 text-xs gap-1.5 text-primary hover:text-primary self-start sm:self-center font-semibold"
          onClick={() => onNavigate('risks')}
          data-testid="btn-open-risk-center"
        >
          <span>{t.viewAll}</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </Button>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Hydraulic emergency callout */}
        {hasHydraulicEmergency && (
          <div className="p-3 rounded-lg bg-rose-500/10 border border-rose-300 dark:border-rose-900 text-xs text-rose-800 dark:text-rose-300 flex items-center justify-between gap-3">
            <div className="flex items-center gap-2">
              <AlertOctagon className="w-4 h-4 text-rose-600 shrink-0" />
              <span className="font-bold">{t.hydraulicAlert}</span>
            </div>
            <Button
              size="sm"
              variant="destructive"
              className="h-7 text-xs px-2.5 font-semibold"
              onClick={() => onNavigate('risks')}
            >
              Inspect
            </Button>
          </div>
        )}

        {/* 4-Stat Metric Row */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
          <div className="p-2.5 rounded-lg border bg-muted/30 space-y-1">
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider block">
              {t.critical}
            </span>
            <div className="flex items-baseline gap-1">
              <span className={cn('text-xl font-bold font-mono', (overview?.criticalRiskCount ?? 0) > 0 ? 'text-rose-600' : 'text-foreground')}>
                {overview?.criticalRiskCount ?? 0}
              </span>
              <span className="text-[11px] text-muted-foreground">parcels</span>
            </div>
          </div>

          <div className="p-2.5 rounded-lg border bg-muted/30 space-y-1">
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider block">
              {t.waterShortage}
            </span>
            <div className="flex items-baseline gap-1">
              <span className={cn('text-xl font-bold font-mono', (overview?.waterShortageCount ?? 0) > 0 ? 'text-blue-600' : 'text-foreground')}>
                {overview?.waterShortageCount ?? 0}
              </span>
              <span className="text-[11px] text-muted-foreground">parcels</span>
            </div>
          </div>

          <div className="p-2.5 rounded-lg border bg-muted/30 space-y-1">
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider block">
              {t.telemetryGaps}
            </span>
            <div className="flex items-baseline gap-1">
              <span className={cn('text-xl font-bold font-mono', (overview?.telemetryIssuesCount ?? 0) > 0 ? 'text-purple-600' : 'text-foreground')}>
                {overview?.telemetryIssuesCount ?? 0}
              </span>
              <span className="text-[11px] text-muted-foreground">probes</span>
            </div>
          </div>

          <div className="p-2.5 rounded-lg border bg-muted/30 space-y-1">
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider block">
              Actions Pending
            </span>
            <div className="flex items-baseline gap-1">
              <span className="text-xl font-bold font-mono text-primary">
                {overview?.activeRecommendationsCount ?? 0}
              </span>
              <span className="text-[11px] text-muted-foreground">recs</span>
            </div>
          </div>
        </div>

        {/* Top 2 Recommendations Snapshot */}
        {topRecommendations.length > 0 && (
          <div className="space-y-2 pt-1 border-t border-border/50">
            <span className="text-[11px] font-bold uppercase tracking-wider text-muted-foreground block">
              {t.topActions}
            </span>
            <div className="space-y-2">
              {topRecommendations.map((rec) => (
                <div
                  key={rec.id}
                  className="p-2.5 rounded-lg border bg-card/60 flex items-center justify-between gap-3 text-xs"
                >
                  <div className="space-y-0.5">
                    <div className="flex items-center gap-1.5">
                      <span className="font-bold text-foreground line-clamp-1">{rec.title}</span>
                      <span className="text-muted-foreground font-semibold">({rec.fieldName})</span>
                    </div>
                    <p className="text-muted-foreground text-[11px] line-clamp-1">{rec.description}</p>
                  </div>

                  <Button
                    size="sm"
                    variant="outline"
                    className="h-7 text-[11px] px-2 font-semibold shrink-0 gap-1"
                    onClick={() => {
                      if (rec.destinationRoute?.startsWith('/anomalies')) {
                        onNavigate('anomalies', rec.fieldId)
                      } else if (rec.destinationRoute?.startsWith('/operations')) {
                        onNavigate('operations', rec.fieldId)
                      } else {
                        onNavigate('risks', rec.fieldId)
                      }
                    }}
                  >
                    <span>{t.execute}</span>
                    <ExternalLink className="w-3 h-3" />
                  </Button>
                </div>
              ))}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
