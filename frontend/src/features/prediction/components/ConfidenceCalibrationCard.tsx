import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { AlertTriangle, ShieldCheck, Gauge } from 'lucide-react'
import { EvaluationConfidenceTier } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface ConfidenceCalibrationCardProps {
  confidenceTiers?: EvaluationConfidenceTier[]
  lang: SupportedLanguage
}

export const ConfidenceCalibrationCard: React.FC<ConfidenceCalibrationCardProps> = ({
  confidenceTiers = [],
  lang,
}) => {
  const t = {
    uz: {
      title: 'Ishonchlilik Darajasi Kalibratsiyasi (Confidence Calibration)',
      subtitle: 'Model o‘ziga yuqori baho bergan bashoratlar amalda haqiqatan ham aniqroqmi?',
      tier: 'Daraja',
      samples: 'Namunalar',
      mae: 'O‘rtacha Xato (MAE)',
      accuracy: 'Aniqlik',
      failureRate: 'Uzum / Xatolik Foizi',
      calibrated: 'Yaxshi kalibratsiyalangan: yuqori ishonchlilik pastroq xatolik bilan bevosita bog‘langan.',
      inversion: 'Kalibratsiya og‘ishi: model o‘ziga ishonmagan hollarda xatolik kamroq kuzatilgan.',
      insufficientNotice: 'Namunalar soni kamligi sababli kalibratsiya ishonchliligini baholash cheklangan.',
    },
    ru: {
      title: 'Калибровка Степени Достоверности (Confidence Calibration)',
      subtitle: 'Действительно ли прогнозы с высокой уверенностью более точны на практике?',
      tier: 'Уровень',
      samples: 'Выборка',
      mae: 'Средняя Ошибка (MAE)',
      accuracy: 'Точность',
      failureRate: 'Доля Ошибок',
      calibrated: 'Корректно откалибровано: высокая уверенность соответствует меньшей погрешности.',
      inversion: 'Инверсия калибровки: прогнозы с низкой уверенностью оказались точнее высокоуверенных.',
      insufficientNotice: 'Недостаточно данных для подтверждения достоверности калибровки.',
    },
    en: {
      title: 'Confidence Calibration & Trust Verification',
      subtitle: 'Empirically evaluating if high confidence scores reliably correlate with lower error',
      tier: 'Tier',
      samples: 'Samples',
      mae: 'Mean Error (MAE)',
      accuracy: 'Accuracy',
      failureRate: 'Failure Rate',
      calibrated: 'Well calibrated: higher confidence strictly correlates with lower error and variance.',
      inversion: 'Calibration anomaly: high-confidence forecasts showed higher error than lower tiers.',
      insufficientNotice: 'Sample size insufficient to draw definitive calibration boundaries.',
    },
  }[lang]

  if (!confidenceTiers || confidenceTiers.length === 0) {
    return null
  }

  // Check for inversion
  const highTier = confidenceTiers.find((t) => t.tierName === 'HIGH')
  const lowTier = confidenceTiers.find((t) => t.tierName === 'LOW' || t.tierName === 'MEDIUM')
  const hasInversion =
    highTier &&
    lowTier &&
    typeof highTier.meanAbsoluteError === 'number' &&
    typeof lowTier.meanAbsoluteError === 'number' &&
    highTier.meanAbsoluteError > lowTier.meanAbsoluteError

  return (
    <Card className="border-border shadow-xs" data-testid="confidence-calibration-card">
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <Gauge className="h-4 w-4 text-primary" />
              <CardTitle className="text-base font-bold">{t.title}</CardTitle>
            </div>
            <CardDescription className="text-xs text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>
        </div>
      </CardHeader>

      <CardContent className="pt-4 space-y-4">
        {/* Calibration Banner */}
        {hasInversion ? (
          <div className="rounded-lg border border-amber-500/30 bg-amber-500/10 p-3 flex items-center gap-2 text-xs text-amber-800 dark:text-amber-200">
            <AlertTriangle className="h-4 w-4 text-amber-600 dark:text-amber-400 shrink-0" />
            <span>{t.inversion}</span>
          </div>
        ) : (
          <div className="rounded-lg border border-emerald-500/30 bg-emerald-500/10 p-3 flex items-center gap-2 text-xs text-emerald-800 dark:text-emerald-200">
            <ShieldCheck className="h-4 w-4 text-emerald-600 dark:text-emerald-400 shrink-0" />
            <span>{t.calibrated}</span>
          </div>
        )}

        {/* Tiers Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          {confidenceTiers.map((tier) => {
            const isHigh = tier.tierName === 'HIGH'
            const isMed = tier.tierName === 'MEDIUM'

            return (
              <div
                key={tier.tierName}
                className="p-3.5 rounded-lg border border-border bg-card space-y-2"
              >
                <div className="flex items-center justify-between">
                  <Badge
                    variant="outline"
                    data-testid={`confidence-tier-${tier.tierName.toLowerCase()}`}
                    className={`font-mono text-xs ${
                      isHigh
                        ? 'border-emerald-500/40 text-emerald-600 dark:text-emerald-400 bg-emerald-500/5'
                        : isMed
                        ? 'border-sky-500/40 text-sky-600 dark:text-sky-400 bg-sky-500/5'
                        : 'border-muted text-muted-foreground'
                    }`}
                  >
                    {tier.tierName} CONFIDENCE
                  </Badge>
                  <span className="text-xs font-mono text-muted-foreground">n={tier.sampleSize}</span>
                </div>

                <div className="space-y-1 text-xs">
                  <div className="flex justify-between items-center text-muted-foreground">
                    <span>{t.mae}:</span>
                    <span className="font-mono font-semibold text-foreground">
                      {typeof tier.meanAbsoluteError === 'number' ? tier.meanAbsoluteError.toFixed(2) : '—'}
                    </span>
                  </div>

                  <div className="flex justify-between items-center text-muted-foreground">
                    <span>{t.accuracy}:</span>
                    <span className="font-mono font-semibold text-foreground">
                      {typeof tier.accuracyPercentage === 'number' ? `${tier.accuracyPercentage.toFixed(1)}%` : '—'}
                    </span>
                  </div>

                  <div className="flex justify-between items-center text-muted-foreground">
                    <span>{t.failureRate}:</span>
                    <span className="font-mono font-semibold text-foreground">
                      {typeof tier.failureRatePercentage === 'number' ? `${tier.failureRatePercentage.toFixed(1)}%` : '0%'}
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
