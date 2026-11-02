import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import {
  Compass,
  Droplets,
  AlertTriangle,
  Cpu,
  ArrowRight,
  Sprout,
  ShieldAlert,
} from 'lucide-react'
import { SupportedLanguage } from '@/shared/i18n'
import { NavSection } from '@/widgets/sidebar'
import {
  usePredictionOverviewQuery,
  usePredictionEvaluationOverviewQuery,
  useGovernanceOverviewQuery,
} from '../queries/usePredictionQueries'

interface PredictiveSummaryCardProps {
  lang: SupportedLanguage
  onNavigate: (section: NavSection, fieldId?: string) => void
}

export const PredictiveSummaryCard: React.FC<PredictiveSummaryCardProps> = ({
  lang,
  onNavigate,
}) => {
  const { data: overview, isLoading, isError } = usePredictionOverviewQuery('NEXT_48_HOURS')
  const { data: evalOverview } = usePredictionEvaluationOverviewQuery()
  const { data: govOverview } = useGovernanceOverviewQuery()

  const t = {
    uz: {
      title: 'Prediktiv Ferma Intellekti (Predictive Intelligence)',
      subtitle: 'Keyingi 48 soatlik kutilayotgan agronomik, gidravlik va datchik prognozlari',
      irrigationNeed: 'Sug‘orish ehtiyoji',
      fieldsLikely: 'ta dalada 48h ichida kutiladi',
      waterDemand: 'Kutilayotgan suv talabi',
      waterDemandSub: 'm³ umumiy talab',
      waterRisk: 'Suv limiti xavfi',
      waterRiskSub: 'ta dalada limit tanqisligi xavfi',
      sensorHealth: 'Datchiklar ishonchliligi',
      sensorHealthSub: 'telemetriya barqaror',
      viewForecasts: 'Barcha prognozlarni ko‘rish',
      loading: 'Prediktiv tahlillar hisoblanmoqda...',
      error: 'Prognozlarni yuklashda xatolik yuz berdi.',
      hydraulicBlocked: 'ta dalada gidravlik blokirovkasi faol',
    },
    ru: {
      title: 'Предиктивный Интеллект Фермы (Predictive Intelligence)',
      subtitle: 'Ожидаемые агрономические и гидравлические прогнозы на 48 часов',
      irrigationNeed: 'Потребность в поливе',
      fieldsLikely: 'полей ожидают полив в теч. 48ч',
      waterDemand: 'Ожидаемый спрос на воду',
      waterDemandSub: 'м³ общий прогнозируемый объем',
      waterRisk: 'Риск лимита воды',
      waterRiskSub: 'полей под угрозой дефицита',
      sensorHealth: 'Надежность датчиков',
      sensorHealthSub: 'исправной телеметрии',
      viewForecasts: 'Смотреть все прогнозы',
      loading: 'Расчет прогнозных моделей...',
      error: 'Ошибка загрузки предиктивной аналитики.',
      hydraulicBlocked: 'полей заблокированы защитой',
    },
    en: {
      title: 'Predictive Farm Intelligence',
      subtitle: 'Forecast water demand, irrigation likelihood, and risks for next 48 hours',
      irrigationNeed: 'Irrigation Need',
      fieldsLikely: 'fields likely within 48h',
      waterDemand: 'Expected Water Demand',
      waterDemandSub: 'm³ total projected need',
      waterRisk: 'Water Quota Risk',
      waterRiskSub: 'fields at risk of deficit',
      sensorHealth: 'Sensor Reliability',
      sensorHealthSub: 'healthy telemetry',
      viewForecasts: 'View Full Forecasts',
      loading: 'Calculating predictive models...',
      error: 'Failed to load predictive analytics.',
      hydraulicBlocked: 'parcels hydraulically blocked',
    },
  }[lang]

  if (isLoading) {
    return (
      <Card className="border-border/60 shadow-xs bg-card/60 animate-pulse">
        <CardHeader className="pb-3">
          <div className="h-5 w-48 bg-muted rounded" />
          <div className="h-3 w-72 bg-muted/70 rounded mt-1" />
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-16 rounded-xl bg-muted/40" />
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (isError || !overview) {
    return null
  }

  return (
    <Card className="border-primary/20 shadow-xs bg-gradient-to-br from-card via-card to-primary/5 hover:border-primary/40 transition-all">
      <CardHeader className="pb-3">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-primary/10 text-primary">
              <Compass className="h-4 w-4" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <CardTitle className="text-sm sm:text-base font-bold text-foreground">
                  {t.title}
                </CardTitle>
                <Badge variant="outline" className="text-[10px] bg-primary/10 text-primary border-primary/20 font-mono">
                  48H FORECAST
                </Badge>
                {evalOverview?.overallModelHealth && (
                  <Badge
                    variant="outline"
                    className={`text-[10px] font-mono ${
                      evalOverview.overallModelHealth === 'HEALTHY'
                        ? 'text-emerald-600 dark:text-emerald-400 border-emerald-500/30 bg-emerald-500/10'
                        : evalOverview.overallModelHealth === 'WATCH'
                        ? 'text-amber-600 dark:text-amber-400 border-amber-500/30 bg-amber-500/10'
                        : evalOverview.overallModelHealth === 'DEGRADED'
                        ? 'text-rose-600 dark:text-rose-400 border-rose-500/30 bg-rose-500/10'
                        : 'text-muted-foreground'
                    }`}
                  >
                    HEALTH: {evalOverview.overallModelHealth}
                  </Badge>
                )}
                {govOverview && govOverview.openProposalsCount > 0 && (
                  <Badge
                    variant="outline"
                    className="text-[10px] font-mono text-amber-600 dark:text-amber-400 border-amber-500/30 bg-amber-500/10 cursor-pointer"
                    onClick={() => onNavigate('predictions')}
                  >
                    GOVERNANCE: {govOverview.openProposalsCount} PROPOSAL(S)
                  </Badge>
                )}
              </div>
              <CardDescription className="text-xs text-muted-foreground">
                {t.subtitle}
              </CardDescription>
            </div>
          </div>

          <Button
            variant="default"
            size="sm"
            onClick={() => onNavigate('predictions')}
            className="text-xs gap-1.5 shadow-xs"
          >
            <span>{t.viewForecasts}</span>
            <ArrowRight className="h-3.5 w-3.5" />
          </Button>
        </div>
      </CardHeader>

      <CardContent>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {/* Signal 1: Irrigation Need */}
          <div className="p-3 rounded-xl bg-muted/40 border border-border/60 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
              <Sprout className="h-4 w-4" />
            </div>
            <div>
              <span className="text-[11px] font-medium text-muted-foreground block">{t.irrigationNeed}</span>
              <div className="flex items-baseline gap-1 mt-0.5">
                <strong className="text-base sm:text-lg font-extrabold font-mono text-foreground">
                  {overview.fieldsNeedingIrrigationCount}
                </strong>
                <span className="text-xs text-muted-foreground">{t.fieldsLikely}</span>
              </div>
            </div>
          </div>

          {/* Signal 2: Water Demand */}
          <div className="p-3 rounded-xl bg-muted/40 border border-border/60 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-sky-500/10 text-sky-500">
              <Droplets className="h-4 w-4" />
            </div>
            <div>
              <span className="text-[11px] font-medium text-muted-foreground block">{t.waterDemand}</span>
              <div className="flex items-baseline gap-1 mt-0.5">
                <strong className="text-base sm:text-lg font-extrabold font-mono text-foreground">
                  {overview.totalWaterDemandM3.toLocaleString()}
                </strong>
                <span className="text-xs text-muted-foreground">{t.waterDemandSub}</span>
              </div>
            </div>
          </div>

          {/* Signal 3: Water Availability Risk */}
          <div className="p-3 rounded-xl bg-muted/40 border border-border/60 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-amber-500/10 text-amber-500">
              <AlertTriangle className="h-4 w-4" />
            </div>
            <div>
              <span className="text-[11px] font-medium text-muted-foreground block">{t.waterRisk}</span>
              <div className="flex items-baseline gap-1 mt-0.5">
                <strong className={`text-base sm:text-lg font-extrabold font-mono ${overview.fieldsAtWaterRiskCount > 0 ? 'text-amber-600 dark:text-amber-400' : 'text-foreground'}`}>
                  {overview.fieldsAtWaterRiskCount}
                </strong>
                <span className="text-xs text-muted-foreground">{t.waterRiskSub}</span>
              </div>
            </div>
          </div>

          {/* Signal 4: Sensor Reliability */}
          <div className="p-3 rounded-xl bg-muted/40 border border-border/60 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-purple-500/10 text-purple-500">
              <Cpu className="h-4 w-4" />
            </div>
            <div>
              <span className="text-[11px] font-medium text-muted-foreground block">{t.sensorHealth}</span>
              <div className="flex items-baseline gap-1 mt-0.5">
                <strong className="text-base sm:text-lg font-extrabold font-mono text-foreground">
                  {overview.sensorHealthPercent}%
                </strong>
                <span className="text-xs text-muted-foreground">{t.sensorHealthSub}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Hydraulic Blocks Warning Alert */}
        {overview.activeHydraulicBlocksCount > 0 && (
          <div className="mt-3 p-2.5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-700 dark:text-rose-400 text-xs flex items-center justify-between">
            <div className="flex items-center gap-2">
              <ShieldAlert className="h-4 w-4 text-rose-600 dark:text-rose-400 shrink-0" />
              <span>
                <strong>Diqqat:</strong> {overview.activeHydraulicBlocksCount} {t.hydraulicBlocked}
              </span>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onNavigate('operations')}
              className="text-[11px] text-rose-700 dark:text-rose-400 hover:bg-rose-100 dark:hover:bg-rose-950/40 h-6 px-2"
            >
              Operatsiyalar
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
