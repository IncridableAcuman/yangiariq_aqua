import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import {
  ShieldAlert,
  ArrowRight,
  Droplets,
  Cpu,
  Info,
} from 'lucide-react'
import { PredictionItem } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface PredictionRiskOverviewProps {
  waterRisk?: PredictionItem | null
  planRisk?: PredictionItem | null
  sensorRisk?: PredictionItem | null
  lang: SupportedLanguage
  onExplain: (prediction: PredictionItem) => void
  onNavigateToModule: (module: string, fieldId?: string) => void
}

export const PredictionRiskOverview: React.FC<PredictionRiskOverviewProps> = ({
  waterRisk,
  planRisk,
  sensorRisk,
  lang,
  onExplain,
  onNavigateToModule,
}) => {
  const t = {
    uz: {
      title: 'Emerging Risks & Infrastructure Constraints',
      subtitle: 'Kutilayotgan limit tanqisligi, gidravlik blokirovkalar va datchik ishonchliligi',
      waterTitle: 'Suv Limiti Yetarliligi Xavfi',
      planTitle: 'Gidravlik Xavfsizlik va Reja Riski',
      sensorTitle: 'Datchiklar Ishonchliligi Prognozi',
      explainBtn: 'Asoslash',
      openModule: 'Ko‘rib chiqish',
      sufficient: 'Yetarli / Xavf yo‘q',
      healthy: 'Barqaror (95%+)',
      blocked: 'GIDRAVLIK BLOKIROVKA',
    },
    ru: {
      title: 'Возникающие Риски и Инфраструктурные Ограничения',
      subtitle: 'Дефицит квоты, гидравлические блокировки и надежность телеметрии',
      waterTitle: 'Риск Доступности Воды',
      planTitle: 'Гидравлическая Безопасность и План',
      sensorTitle: 'Прогноз Надежности Датчиков',
      explainBtn: 'Обоснование',
      openModule: 'Перейти',
      sufficient: 'Достаточно / Без риска',
      healthy: 'Стабильно (95%+)',
      blocked: 'ГИДРАВЛИЧЕСКАЯ БЛОКИРОВКА',
    },
    en: {
      title: 'Emerging Risks & Infrastructure Constraints',
      subtitle: 'Water quota headroom, hydraulic safety locks, and sensor telemetry reliability',
      waterTitle: 'Water Availability Risk',
      planTitle: 'Hydraulic Safety & Plan Risk',
      sensorTitle: 'Sensor Reliability Forecast',
      explainBtn: 'Evidence',
      openModule: 'Review',
      sufficient: 'Sufficient / Low Risk',
      healthy: 'Healthy (95%+)',
      blocked: 'HYDRAULIC SAFETY BLOCKED',
    },
  }[lang]

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      {/* 1. Water Availability Risk */}
      <Card className="border-border shadow-xs bg-card hover:border-primary/20 transition-all flex flex-col justify-between">
        <CardHeader className="pb-2">
          <div className="flex items-center justify-between gap-1">
            <div className="flex items-center gap-1.5">
              <Droplets className="h-4 w-4 text-blue-500" />
              <CardTitle className="text-xs font-bold text-foreground">{t.waterTitle}</CardTitle>
            </div>
            {waterRisk && (
              <Badge
                variant="outline"
                className={`text-[10px] font-semibold ${
                  waterRisk.riskLevel === 'CRITICAL' || waterRisk.riskLevel === 'HIGH'
                    ? 'bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/20'
                    : waterRisk.riskLevel === 'MEDIUM'
                    ? 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20'
                    : 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20'
                }`}
              >
                {waterRisk.riskLevel}
              </Badge>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-3 pt-1 text-xs">
          <p className="text-[11px] text-muted-foreground leading-relaxed">
            {waterRisk?.evidence || t.sufficient}
          </p>
          <div className="flex items-center justify-between pt-2 border-t border-border/60">
            {waterRisk ? (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => onExplain(waterRisk)}
                className="text-[11px] h-7 px-2 text-muted-foreground hover:text-foreground gap-1"
              >
                <Info className="h-3 w-3" />
                <span>{t.explainBtn}</span>
              </Button>
            ) : <span />}
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigateToModule('water', waterRisk?.fieldId)}
              className="text-[11px] h-7 px-2.5 gap-1 text-blue-600 dark:text-blue-400"
            >
              <span>{t.openModule}</span>
              <ArrowRight className="h-3 w-3" />
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* 2. Hydraulic Safety & Plan Risk */}
      <Card className="border-border shadow-xs bg-card hover:border-primary/20 transition-all flex flex-col justify-between">
        <CardHeader className="pb-2">
          <div className="flex items-center justify-between gap-1">
            <div className="flex items-center gap-1.5">
              <ShieldAlert className="h-4 w-4 text-emerald-500" />
              <CardTitle className="text-xs font-bold text-foreground">{t.planTitle}</CardTitle>
            </div>
            {planRisk && (
              <Badge
                variant="outline"
                className={`text-[10px] font-semibold ${
                  planRisk.riskLevel === 'BLOCKED'
                    ? 'bg-rose-500 text-white border-rose-600 animate-pulse'
                    : planRisk.riskLevel === 'HIGH'
                    ? 'bg-orange-500/10 text-orange-600 border-orange-500/20'
                    : 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20'
                }`}
              >
                {planRisk.riskLevel === 'BLOCKED' ? t.blocked : planRisk.riskLevel}
              </Badge>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-3 pt-1 text-xs">
          <p className="text-[11px] text-muted-foreground leading-relaxed">
            {planRisk?.evidence || t.sufficient}
          </p>
          <div className="flex items-center justify-between pt-2 border-t border-border/60">
            {planRisk ? (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => onExplain(planRisk)}
                className="text-[11px] h-7 px-2 text-muted-foreground hover:text-foreground gap-1"
              >
                <Info className="h-3 w-3" />
                <span>{t.explainBtn}</span>
              </Button>
            ) : <span />}
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigateToModule('operations', planRisk?.fieldId)}
              className="text-[11px] h-7 px-2.5 gap-1 text-emerald-600 dark:text-emerald-400"
            >
              <span>{t.openModule}</span>
              <ArrowRight className="h-3 w-3" />
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* 3. Sensor Reliability Forecast */}
      <Card className="border-border shadow-xs bg-card hover:border-primary/20 transition-all flex flex-col justify-between">
        <CardHeader className="pb-2">
          <div className="flex items-center justify-between gap-1">
            <div className="flex items-center gap-1.5">
              <Cpu className="h-4 w-4 text-purple-500" />
              <CardTitle className="text-xs font-bold text-foreground">{t.sensorTitle}</CardTitle>
            </div>
            {sensorRisk && (
              <Badge
                variant="outline"
                className={`text-[10px] font-semibold ${
                  sensorRisk.riskLevel === 'HIGH'
                    ? 'bg-rose-500/10 text-rose-600 border-rose-500/20'
                    : sensorRisk.riskLevel === 'MEDIUM'
                    ? 'bg-amber-500/10 text-amber-600 border-amber-500/20'
                    : 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20'
                }`}
              >
                {sensorRisk.predictedValue ? `${sensorRisk.predictedValue}%` : t.healthy}
              </Badge>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-3 pt-1 text-xs">
          <p className="text-[11px] text-muted-foreground leading-relaxed">
            {sensorRisk?.evidence || t.healthy}
          </p>
          <div className="flex items-center justify-between pt-2 border-t border-border/60">
            {sensorRisk ? (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => onExplain(sensorRisk)}
                className="text-[11px] h-7 px-2 text-muted-foreground hover:text-foreground gap-1"
              >
                <Info className="h-3 w-3" />
                <span>{t.explainBtn}</span>
              </Button>
            ) : <span />}
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigateToModule('sensors', sensorRisk?.fieldId)}
              className="text-[11px] h-7 px-2.5 gap-1 text-purple-600 dark:text-purple-400"
            >
              <span>{t.openModule}</span>
              <ArrowRight className="h-3 w-3" />
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
