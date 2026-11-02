import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Info, ArrowRight, Gauge, AlertCircle } from 'lucide-react'
import { PredictionItem } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'
import { DataSufficiencyBadge } from './DataSufficiencyBadge'

interface WaterDemandForecastCardProps {
  prediction?: PredictionItem | null
  lang: SupportedLanguage
  onExplain: (prediction: PredictionItem) => void
  onNavigateToWater?: (fieldId?: string) => void
}

export const WaterDemandForecastCard: React.FC<WaterDemandForecastCardProps> = ({
  prediction,
  lang,
  onExplain,
  onNavigateToWater,
}) => {
  const t = {
    uz: {
      title: 'Suv Talabi Prognozi',
      subtitle: 'Rejalashtirilgan sug‘orishlar va agronomik defitsit asosida',
      expectedDemand: 'Kutilayotgan umumiy talab',
      range: 'Oraliq chegaralari',
      confidence: 'Ishonchlilik',
      action: 'Tavsiya',
      viewWater: 'Suv taqsimotini ko‘rish',
      explainBtn: 'Batafsil',
      zeroDemand: 'Tanlangan vaqt ufqida suv talabi kutilmaydi (0 m³).',
    },
    ru: {
      title: 'Прогноз Потребности в Воде',
      subtitle: 'На основе утвержденных планов и прогнозируемого дефицита влаги',
      expectedDemand: 'Ожидаемая общая потребность',
      range: 'Интервал спроса',
      confidence: 'Достоверность',
      action: 'Рекомендация',
      viewWater: 'Управление водным лимитом',
      explainBtn: 'Подробнее',
      zeroDemand: 'В выбранном горизонте потребность в воде не ожидается (0 м³).',
    },
    en: {
      title: 'Water Demand Forecast',
      subtitle: 'Based on upcoming schedules and root-zone soil deficits',
      expectedDemand: 'Expected Total Demand',
      range: 'Demand Range',
      confidence: 'Confidence',
      action: 'Recommendation',
      viewWater: 'Water Allocation Center',
      explainBtn: 'Explain',
      zeroDemand: 'No water demand projected within selected horizon (0 m³).',
    },
  }[lang]

  if (!prediction || prediction.predictedValue === null) {
    return (
      <Card className="border-border/60 shadow-xs bg-card/60">
        <CardHeader className="pb-3">
          <CardTitle className="text-sm font-bold flex items-center gap-2">
            <Gauge className="h-4 w-4 text-blue-500" />
            <span>{t.title}</span>
          </CardTitle>
          <CardDescription className="text-xs">{t.subtitle}</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="p-3 rounded-xl bg-muted/40 border border-border/60 text-xs text-muted-foreground flex items-center gap-2">
            <AlertCircle className="h-4 w-4 text-muted-foreground" />
            <span>{prediction?.evidence || t.zeroDemand}</span>
          </div>
        </CardContent>
      </Card>
    )
  }

  const demandVal = prediction.predictedValue ?? 0
  const lowerVal = prediction.lowerBound ?? 0
  const upperVal = prediction.upperBound ?? 0

  return (
    <Card className="border-border shadow-xs hover:border-primary/30 transition-all bg-gradient-to-br from-card via-card to-blue-500/5">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <div className="p-2 rounded-xl bg-blue-500/10 text-blue-600 dark:text-blue-400">
              <Gauge className="h-4 w-4" />
            </div>
            <div>
              <CardTitle className="text-sm font-bold text-foreground">{t.title}</CardTitle>
              <CardDescription className="text-[11px] text-muted-foreground">
                {prediction.horizon.replace(/_/g, ' ')}
              </CardDescription>
            </div>
          </div>
          <DataSufficiencyBadge sufficiency={prediction.dataSufficiency} lang={lang} />
        </div>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Volumetric Number & Range */}
        <div className="flex items-baseline justify-between p-3.5 rounded-xl bg-muted/30 border border-border/60">
          <div>
            <span className="text-[11px] uppercase tracking-wider text-muted-foreground font-semibold block">
              {t.expectedDemand}
            </span>
            <div className="flex items-baseline gap-1.5 mt-0.5">
              <span className="text-2xl sm:text-3xl font-extrabold tracking-tight text-foreground font-mono">
                {demandVal.toLocaleString()} m³
              </span>
              <span className="text-xs text-muted-foreground font-medium">
                ({t.range}: {lowerVal.toLocaleString()} – {upperVal.toLocaleString()} m³)
              </span>
            </div>
          </div>

          <div className="text-right">
            <span className="text-[11px] uppercase tracking-wider text-muted-foreground font-semibold block">
              {t.confidence}
            </span>
            <Badge variant="outline" className="font-mono text-xs font-bold bg-primary/10 text-primary border-primary/20 mt-1">
              {prediction.confidencePercent}%
            </Badge>
          </div>
        </div>

        {/* Evidence & Recommendation */}
        <div className="space-y-2 text-xs">
          <div className="p-2.5 rounded-lg bg-background/80 border border-border/60 text-[11px] text-muted-foreground leading-relaxed">
            <strong className="text-foreground font-medium">{t.action}: </strong>
            {prediction.recommendedAction}
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-between gap-2 pt-1 border-t border-border/60">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onExplain(prediction)}
            className="text-xs text-muted-foreground hover:text-foreground gap-1.5 h-8 px-2"
          >
            <Info className="h-3.5 w-3.5 text-primary" />
            <span>{t.explainBtn}</span>
          </Button>

          {onNavigateToWater && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigateToWater(prediction.fieldId)}
              className="text-xs gap-1.5 h-8 border-blue-200 dark:border-blue-900 text-blue-700 dark:text-blue-300 hover:bg-blue-100"
            >
              <span>{t.viewWater}</span>
              <ArrowRight className="h-3 w-3" />
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
