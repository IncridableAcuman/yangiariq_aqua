import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Database, CheckCircle2, AlertTriangle, WifiOff } from 'lucide-react'
import { EvaluationDataQualityImpact } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface DataQualityImpactCardProps {
  qualityImpacts?: EvaluationDataQualityImpact[]
  lang: SupportedLanguage
}

export const DataQualityImpactCard: React.FC<DataQualityImpactCardProps> = ({
  qualityImpacts = [],
  lang,
}) => {
  const t = {
    uz: {
      title: 'Datchiklar Sifatining Prognozga Ta’siri (Telemetry Impact)',
      subtitle: 'Telemetriya to‘liqligi va uzatish barqarorligi bashorat aniqligiga qanday ta’sir qiladi?',
      category: 'Holat',
      samples: 'Namunalar',
      mae: 'O‘rtacha Xatolik',
      accuracy: 'Haqiqiy Aniqlik',
      unverifiable: 'Tekshirib bo‘lmaslik Foizi',
      completeDesc: 'Barcha tuproq namligi va meteorologiya datchiklari uzluksiz ishlaganda',
      partialDesc: 'Baʼzi qatlam datchiklari yoki oraliq signallar yo‘qolganda',
      insufficientDesc: 'Telemetriya uzilgan yoki 24 soatdan ortiq kechikkan holatda',
    },
    ru: {
      title: 'Влияние Качества Телеметрии на Точность Прогнозов',
      subtitle: 'Как полнота и свежесть сенсорных данных влияют на надежность моделей?',
      category: 'Качество Данных',
      samples: 'Выборка',
      mae: 'Средняя Ошибка',
      accuracy: 'Фактическая Точность',
      unverifiable: 'Доля Неверифицируемых',
      completeDesc: 'Все почвенные и метеорологические датчики функционируют штатно',
      partialDesc: 'Отсутствуют промежуточные слои телеметрии или часть интервалов',
      insufficientDesc: 'Датчики отключены или задержка телеметрии превышает 24 часа',
    },
    en: {
      title: 'Telemetry Quality Impact on Forecast Accuracy',
      subtitle: 'Quantifying the degradation of predictive accuracy caused by sensor omissions and stale telemetry',
      category: 'Data Quality',
      samples: 'Sample Size',
      mae: 'Mean Absolute Error',
      accuracy: 'Realized Accuracy',
      unverifiable: 'Unverifiable Rate',
      completeDesc: 'All sensor nodes transmitting within authoritative sampling intervals',
      partialDesc: 'Intermittent telemetry gaps or partial depth layer omissions',
      insufficientDesc: 'Severely degraded or missing telemetry (>24h stale)',
    },
  }[lang]

  if (!qualityImpacts || qualityImpacts.length === 0) {
    return null
  }

  return (
    <Card className="border-border shadow-xs" data-testid="data-quality-impact-card">
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <Database className="h-4 w-4 text-primary" />
              <CardTitle className="text-base font-bold">{t.title}</CardTitle>
            </div>
            <CardDescription className="text-xs text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>
        </div>
      </CardHeader>

      <CardContent className="pt-4 space-y-3">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {qualityImpacts.map((item) => {
            const isComplete = item.qualityCategory === 'DATA_COMPLETE'
            const isPartial = item.qualityCategory === 'DATA_PARTIAL'

            return (
              <div
                key={item.qualityCategory}
                className="p-3.5 rounded-lg border border-border bg-card space-y-2.5"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1.5">
                    {isComplete ? (
                      <CheckCircle2 className="h-4 w-4 text-emerald-500" />
                    ) : isPartial ? (
                      <AlertTriangle className="h-4 w-4 text-amber-500" />
                    ) : (
                      <WifiOff className="h-4 w-4 text-rose-500" />
                    )}
                    <span className="font-semibold text-xs text-foreground">
                      {item.qualityCategory}
                    </span>
                  </div>
                  <span className="text-[11px] font-mono text-muted-foreground">n={item.sampleSize}</span>
                </div>

                <p className="text-[11px] text-muted-foreground leading-snug">
                  {isComplete ? t.completeDesc : isPartial ? t.partialDesc : t.insufficientDesc}
                </p>

                <div className="pt-2 border-t border-border/60 space-y-1 text-xs">
                  <div className="flex justify-between items-center text-muted-foreground">
                    <span>{t.mae}:</span>
                    <span className="font-mono font-semibold text-foreground">
                      {typeof item.meanAbsoluteError === 'number' ? item.meanAbsoluteError.toFixed(2) : '—'}
                    </span>
                  </div>

                  <div className="flex justify-between items-center text-muted-foreground">
                    <span>{t.accuracy}:</span>
                    <span className="font-mono font-semibold text-foreground">
                      {typeof item.accuracyPercentage === 'number' ? `${item.accuracyPercentage.toFixed(1)}%` : '—'}
                    </span>
                  </div>

                  <div className="flex justify-between items-center text-muted-foreground">
                    <span>{t.unverifiable}:</span>
                    <span className="font-mono font-semibold text-rose-600 dark:text-rose-400">
                      {typeof item.unverifiablePercentage === 'number' ? `${item.unverifiablePercentage.toFixed(1)}%` : '0%'}
                    </span>
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}
