import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import {
  Droplets,
  AlertTriangle,
  ArrowRight,
  Info,
} from 'lucide-react'
import { PredictionItem } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'
import { DataSufficiencyBadge } from './DataSufficiencyBadge'

interface SoilMoistureForecastCardProps {
  prediction?: PredictionItem | null
  lang: SupportedLanguage
  onExplain: (prediction: PredictionItem) => void
  onNavigateToIrrigation?: (fieldId: string) => void
}

export const SoilMoistureForecastCard: React.FC<SoilMoistureForecastCardProps> = ({
  prediction,
  lang,
  onExplain,
  onNavigateToIrrigation,
}) => {
  const t = {
    uz: {
      title: 'Tuproq Namligi Prognozi',
      subtitle: 'Kutilayotgan bug‘lanish va dinamik pasayish tezligi asosida',
      forecast: 'Kutilayotgan namlik',
      confidence: 'Ishonchlilik',
      bounds: 'Noaniqlik oraliqlari',
      risk: 'Xavf darajasi',
      action: 'Tavsiya etilgan amal',
      explainBtn: 'Batafsil asoslash',
      planIrrigation: 'Sug‘orish rejasini ko‘rish',
      unavailable: 'Prognoz mavjud emas: datchik telemetriyasi yetarli emas yoki eski (>24h).',
    },
    ru: {
      title: 'Прогноз Влажности Почвы',
      subtitle: 'На основе динамической скорости истощения и эвапотранспирации',
      forecast: 'Прогнозируемая влажность',
      confidence: 'Достоверность',
      bounds: 'Интервал неопределенности',
      risk: 'Уровень риска',
      action: 'Рекомендуемое действие',
      explainBtn: 'Обоснование прогноза',
      planIrrigation: 'Просмотр плана полива',
      unavailable: 'Прогноз недоступен: недостаточно телеметрии с датчиков или данные устарели (>24ч).',
    },
    en: {
      title: 'Soil Moisture Forecast',
      subtitle: 'Based on depletion velocity, crop Kc, and reference ET0',
      forecast: 'Forecast Moisture',
      confidence: 'Confidence',
      bounds: 'Uncertainty Range',
      risk: 'Risk Level',
      action: 'Recommended Action',
      explainBtn: 'Explain Forecast',
      planIrrigation: 'Review Irrigation Plan',
      unavailable: 'Forecast unavailable: insufficient or stale telemetry (>24h).',
    },
  }[lang]

  if (!prediction || prediction.predictedValue === null || prediction.dataSufficiency === 'INSUFFICIENT') {
    return (
      <Card className="border-border/60 shadow-xs bg-card/60">
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-sm font-bold flex items-center gap-2">
              <Droplets className="h-4 w-4 text-sky-500" />
              <span>{t.title}</span>
            </CardTitle>
            <DataSufficiencyBadge sufficiency="INSUFFICIENT" lang={lang} />
          </div>
          <CardDescription className="text-xs">{t.subtitle}</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="p-3 rounded-xl bg-muted/40 border border-border/60 text-xs text-muted-foreground flex items-start gap-2">
            <AlertTriangle className="h-4 w-4 text-amber-500 shrink-0 mt-0.5" />
            <p>{prediction?.evidence || t.unavailable}</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  const isLowMoisture = (prediction.predictedValue ?? 100) < 24.0
  const riskBadgeColor =
    prediction.riskLevel === 'CRITICAL'
      ? 'bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/20'
      : prediction.riskLevel === 'HIGH'
      ? 'bg-orange-500/10 text-orange-600 dark:text-orange-400 border-orange-500/20'
      : prediction.riskLevel === 'MEDIUM'
      ? 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20'
      : 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20'

  return (
    <Card className="border-border shadow-xs hover:border-primary/30 transition-all bg-gradient-to-br from-card via-card to-sky-500/5">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <div className="p-2 rounded-xl bg-sky-500/10 text-sky-500">
              <Droplets className="h-4 w-4" />
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
        {/* Big Forecast Number & Uncertainty Range */}
        <div className="flex items-baseline justify-between p-3.5 rounded-xl bg-muted/30 border border-border/60">
          <div>
            <span className="text-[11px] uppercase tracking-wider text-muted-foreground font-semibold block">
              {t.forecast}
            </span>
            <div className="flex items-baseline gap-1.5 mt-0.5">
              <span className="text-2xl sm:text-3xl font-extrabold tracking-tight text-foreground font-mono">
                {prediction.predictedValue}%
              </span>
              <span className="text-xs text-muted-foreground font-medium">
                (oralıq: {prediction.lowerBound}% – {prediction.upperBound}%)
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

        {/* Risk & Recommendation */}
        <div className="space-y-2 text-xs">
          <div className="flex items-center justify-between">
            <span className="text-muted-foreground font-medium">{t.risk}:</span>
            <Badge variant="outline" className={`font-semibold text-[11px] ${riskBadgeColor}`}>
              {prediction.riskLevel}
            </Badge>
          </div>

          <div className="p-2.5 rounded-lg bg-background/80 border border-border/60 text-[11px] text-muted-foreground leading-relaxed">
            <strong className="text-foreground font-medium">{t.action}: </strong>
            {prediction.recommendedAction}
          </div>
        </div>

        {/* Buttons: Explain & Action */}
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

          {isLowMoisture && onNavigateToIrrigation && (
            <Button
              variant="default"
              size="sm"
              onClick={() => onNavigateToIrrigation(prediction.fieldId)}
              className="text-xs gap-1.5 h-8 bg-sky-600 hover:bg-sky-700 text-white shadow-xs"
            >
              <span>{t.planIrrigation}</span>
              <ArrowRight className="h-3 w-3" />
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
