import React, { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  BarChart3,
  HelpCircle,
  Target,
  ArrowUpRight,
  ArrowDownRight,
  Layers,
} from 'lucide-react'
import {
  ModelHealthStatus,
  PredictionTypePerformance,
} from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface PredictionTypeEvaluationCardProps {
  performances?: PredictionTypePerformance[]
  lang: SupportedLanguage
}

export const PredictionTypeEvaluationCard: React.FC<PredictionTypeEvaluationCardProps> = ({
  performances = [],
  lang,
}) => {
  const [selectedTypeIndex, setSelectedTypeIndex] = useState<number>(0)

  const t = {
    uz: {
      title: 'Prognoz Turlari Bo‘yicha Model Aniqligi',
      subtitle: 'Har bir bashorat turi bo‘yicha metrikalar, xatolik taqsimoti va adashish matritsasi',
      numericalTitle: 'Raqamli Xatolik Metrikalari (Regression Metrics)',
      categoricalTitle: 'Tasniflash va Adashish Matritsasi (Confusion Matrix)',
      mae: 'O‘rtacha Mutlaq Xatolik (MAE)',
      rmse: 'O‘rtacha Kvadratik Xatolik (RMSE)',
      mape: 'Nisbiy Foiz Xatolik (MAPE)',
      mbe: 'O‘rtacha Og‘ish / Bias (MBE)',
      tolerance: 'Oraliqqa Moslik Foizi',
      accuracy: 'Aniqlik (Accuracy)',
      precision: 'Aniqlik (Precision)',
      recall: 'Qamrov (Recall)',
      f1: 'F1 Ko‘rsatkich',
      tp: 'Haqiqiy Ijobiy (TP)',
      tn: 'Haqiqiy Salbiy (TN)',
      fp: 'Soxta Ijobiy (FP)',
      fn: 'Soxta Salbiy (FN)',
      interrupted: 'Xavfsizlik Tufayli To‘xtatilgan',
      insufficientSample: 'Namuna yetarli emas: statistik aniqlik hisoblash uchun kamida 10 ta o‘lchov kerak.',
      overPredicting: 'Muntazam oshirib ko‘rsatish (Over-prediction)',
      underPredicting: 'Muntazam kamaytirib ko‘rsatish (Under-prediction)',
      neutralBias: 'Balanslashgan xatolik',
      naMape: 'N/A (haqiqiy qiymatlar 0 ga yaqin)',
      modelVersion: 'Model Versiyasi',
      sampleSize: 'Namunalar soni (n)',
    },
    ru: {
      title: 'Точность Моделей по Типам Прогнозов',
      subtitle: 'Метрики регрессии, матрицы ошибок и отклонения по каждому типу прогноза',
      numericalTitle: 'Метрики Численной Ошибки (Regression Metrics)',
      categoricalTitle: 'Матрица Ошибок и Классификация (Confusion Matrix)',
      mae: 'Средняя Абсолютная Ошибка (MAE)',
      rmse: 'Корень из Ср. Квадр. Ошибки (RMSE)',
      mape: 'Средняя Относительная Ошибка (MAPE)',
      mbe: 'Систематическое Смещение / Bias (MBE)',
      tolerance: 'Попадание в Доверительный Интервал',
      accuracy: 'Точность (Accuracy)',
      precision: 'Точность Класса (Precision)',
      recall: 'Полнота (Recall)',
      f1: 'F1 Мера',
      tp: 'Истинно Положительные (TP)',
      tn: 'Истинно Отрицательные (TN)',
      fp: 'Ложно Положительные (FP)',
      fn: 'Ложно Отрицательные (FN)',
      interrupted: 'Прервано Защитой (Interrupted)',
      insufficientSample: 'Недостаточно данных: для расчета надежных метрик требуется минимум 10 измерений.',
      overPredicting: 'Систематическое завышение (Over-prediction)',
      underPredicting: 'Систематическое занижение (Under-prediction)',
      neutralBias: 'Сбалансированная ошибка',
      naMape: 'Н/Д (значения близки к нулю)',
      modelVersion: 'Версия Модели',
      sampleSize: 'Объем выборки (n)',
    },
    en: {
      title: 'Model Performance by Prediction Type',
      subtitle: 'Rigorous numerical regression errors and categorical confusion matrices per forecast type',
      numericalTitle: 'Numerical Regression Metrics',
      categoricalTitle: 'Classification & Confusion Matrix',
      mae: 'Mean Absolute Error (MAE)',
      rmse: 'Root Mean Squared Error (RMSE)',
      mape: 'Mean Absolute Percentage Error (MAPE)',
      mbe: 'Mean Bias Error (MBE)',
      tolerance: 'Within Tolerance Bounds',
      accuracy: 'Accuracy',
      precision: 'Precision',
      recall: 'Recall',
      f1: 'F1 Score',
      tp: 'True Positives (TP)',
      tn: 'True Negatives (TN)',
      fp: 'False Positives (FP)',
      fn: 'False Negatives (FN)',
      interrupted: 'Interrupted by Safety',
      insufficientSample: 'Insufficient sample size: at least 10 verified outcomes are required for trustworthy metrics.',
      overPredicting: 'Systematic Over-prediction',
      underPredicting: 'Systematic Under-prediction',
      neutralBias: 'Balanced Error',
      naMape: 'N/A (actual values near zero)',
      modelVersion: 'Model Version',
      sampleSize: 'Sample size (n)',
    },
  }[lang]

  if (!performances || performances.length === 0) {
    return null
  }

  const selectedPerformance = performances[selectedTypeIndex] || performances[0]
  const num = selectedPerformance.numericalMetrics
  const cls = selectedPerformance.classificationMetrics
  const hasSufficient = selectedPerformance.isSufficientSample

  const getHealthBadge = (health: ModelHealthStatus) => {
    switch (health) {
      case 'HEALTHY':
        return <Badge className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20 text-xs">HEALTHY</Badge>
      case 'WATCH':
        return <Badge className="bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20 text-xs">WATCH</Badge>
      case 'DEGRADED':
        return <Badge className="bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/20 text-xs">DEGRADED</Badge>
      default:
        return <Badge variant="outline" className="text-muted-foreground text-xs">INSUFFICIENT DATA</Badge>
    }
  }

  return (
    <Card className="border-border shadow-xs" data-testid="prediction-type-evaluation-card">
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <BarChart3 className="h-4 w-4 text-primary" />
              <CardTitle className="text-base font-bold">{t.title}</CardTitle>
            </div>
            <CardDescription className="text-xs text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>

          <div className="flex items-center gap-2">
            <Badge variant="outline" className="font-mono text-[11px]">
              {t.modelVersion}: {selectedPerformance.modelVersion}
            </Badge>
            {getHealthBadge(selectedPerformance.modelHealth)}
          </div>
        </div>

        {/* Type Selection Tabs */}
        <div className="flex items-center gap-1.5 overflow-x-auto pt-3 scrollbar-none">
          {performances.map((perf, idx) => {
            const isSelected = idx === selectedTypeIndex
            return (
              <Button
                key={perf.predictionType}
                variant={isSelected ? 'default' : 'outline'}
                size="sm"
                onClick={() => setSelectedTypeIndex(idx)}
                className={`text-xs h-7 px-3 rounded-md shrink-0 font-medium ${
                  isSelected ? 'shadow-xs' : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                <span>{perf.displayName}</span>
                <span className="ml-1.5 opacity-70 font-mono text-[10px]">
                  (n={perf.sampleSize})
                </span>
              </Button>
            )
          })}
        </div>
      </CardHeader>

      <CardContent className="pt-4 space-y-4">
        {/* Sample Size Gating Check */}
        {!hasSufficient && (
          <div className="rounded-lg border border-amber-500/30 bg-amber-500/10 p-3.5 flex items-start gap-3 text-amber-900 dark:text-amber-200">
            <HelpCircle className="h-5 w-5 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
            <div className="text-xs space-y-1">
              <p className="font-semibold">{t.insufficientSample}</p>
              <p className="opacity-90">
                Hozirda baholangan namunalar: <span className="font-mono font-bold">{selectedPerformance.sampleSize}</span> / {selectedPerformance.minSampleSizeRequired} ta talab qilinadi.
              </p>
            </div>
          </div>
        )}

        {/* 1. Numerical Evaluation Metrics (if applicable) */}
        {num && (
          <div className="space-y-2">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              <Layers className="h-3.5 w-3.5 text-primary" />
              <span>{t.numericalTitle}</span>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3">
              <div className="p-3 rounded-lg border border-border/80 bg-card">
                <span className="text-[11px] font-medium text-muted-foreground">{t.mae}</span>
                <p className="text-lg font-bold font-mono text-foreground mt-0.5">
                  {typeof num.meanAbsoluteError === 'number' ? num.meanAbsoluteError.toFixed(2) : '—'}
                </p>
                <span className="text-[10px] text-muted-foreground">Absolyut og‘ish</span>
              </div>

              <div className="p-3 rounded-lg border border-border/80 bg-card">
                <span className="text-[11px] font-medium text-muted-foreground">{t.rmse}</span>
                <p className="text-lg font-bold font-mono text-foreground mt-0.5">
                  {typeof num.rootMeanSquaredError === 'number' ? num.rootMeanSquaredError.toFixed(2) : '—'}
                </p>
                <span className="text-[10px] text-muted-foreground">Katta xatolar penalizatsiyasi</span>
              </div>

              <div className="p-3 rounded-lg border border-border/80 bg-card">
                <span className="text-[11px] font-medium text-muted-foreground">{t.mape}</span>
                <p className="text-lg font-bold font-mono text-foreground mt-0.5">
                  {num.mapeReliable && typeof num.meanAbsolutePercentageError === 'number'
                    ? `${num.meanAbsolutePercentageError.toFixed(1)}%`
                    : 'N/A'}
                </p>
                <span className="text-[10px] text-muted-foreground">
                  {num.mapeReliable ? 'Nisbiy xato' : t.naMape}
                </span>
              </div>

              <div className="p-3 rounded-lg border border-border/80 bg-card">
                <span className="text-[11px] font-medium text-muted-foreground">{t.mbe}</span>
                <div className="flex items-center gap-1 mt-0.5">
                  <p className="text-lg font-bold font-mono text-foreground">
                    {typeof num.meanBiasError === 'number'
                      ? `${num.meanBiasError > 0 ? '+' : ''}${num.meanBiasError.toFixed(2)}`
                      : '—'}
                  </p>
                  {typeof num.meanBiasError === 'number' && num.meanBiasError > 2 && (
                    <ArrowUpRight className="h-4 w-4 text-amber-500" />
                  )}
                  {typeof num.meanBiasError === 'number' && num.meanBiasError < -2 && (
                    <ArrowDownRight className="h-4 w-4 text-sky-500" />
                  )}
                </div>
                <span className="text-[10px] text-muted-foreground">
                  {typeof num.meanBiasError === 'number'
                    ? num.meanBiasError > 2 ? t.overPredicting : num.meanBiasError < -2 ? t.underPredicting : t.neutralBias
                    : '—'}
                </span>
              </div>

              <div className="p-3 rounded-lg border border-border/80 bg-card">
                <span className="text-[11px] font-medium text-muted-foreground">{t.tolerance}</span>
                <p className="text-lg font-bold font-mono text-emerald-600 dark:text-emerald-400 mt-0.5">
                  {typeof num.withinTolerancePercentage === 'number' ? `${num.withinTolerancePercentage.toFixed(1)}%` : '—'}
                </p>
                <span className="text-[10px] text-muted-foreground">
                  {num.withinToleranceCount ?? 0} / {num.validComparisonsCount ?? 0} ta oraliqda
                </span>
              </div>
            </div>
          </div>
        )}

        {/* 2. Categorical Classification & Confusion Matrix (if applicable) */}
        {cls && (
          <div className="space-y-2 pt-2">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              <Target className="h-3.5 w-3.5 text-primary" />
              <span>{t.categoricalTitle}</span>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <div className="p-3 rounded-lg border border-border/80 bg-card">
                <span className="text-[11px] font-medium text-muted-foreground">{t.accuracy}</span>
                <p className="text-lg font-bold font-mono text-foreground mt-0.5">
                  {typeof cls.accuracyPercentage === 'number' ? `${cls.accuracyPercentage.toFixed(1)}%` : '—'}
                </p>
              </div>

              <div className="p-3 rounded-lg border border-border/80 bg-card">
                <span className="text-[11px] font-medium text-muted-foreground">{t.precision}</span>
                <p className="text-lg font-bold font-mono text-foreground mt-0.5">
                  {typeof cls.precisionPercentage === 'number' ? `${cls.precisionPercentage.toFixed(1)}%` : '—'}
                </p>
              </div>

              <div className="p-3 rounded-lg border border-border/80 bg-card">
                <span className="text-[11px] font-medium text-muted-foreground">{t.recall}</span>
                <p className="text-lg font-bold font-mono text-foreground mt-0.5">
                  {typeof cls.recallPercentage === 'number' ? `${cls.recallPercentage.toFixed(1)}%` : '—'}
                </p>
              </div>

              <div className="p-3 rounded-lg border border-border/80 bg-card">
                <span className="text-[11px] font-medium text-muted-foreground">{t.f1}</span>
                <p className="text-lg font-bold font-mono text-foreground mt-0.5">
                  {typeof cls.f1Score === 'number' ? cls.f1Score.toFixed(2) : '—'}
                </p>
              </div>
            </div>

            {/* Confusion Matrix Table */}
            <div className="rounded-lg border border-border overflow-hidden mt-3">
              <div className="p-2.5 bg-muted/40 border-b border-border text-xs font-semibold flex items-center justify-between">
                <span>Adashish Matritsasi (Confusion Matrix)</span>
                <span className="font-mono text-[11px] text-muted-foreground">Jami: {cls.evaluatedCount}</span>
              </div>
              <div className="grid grid-cols-2 sm:grid-cols-5 divide-y sm:divide-y-0 sm:divide-x divide-border bg-card text-center text-xs">
                <div className="p-3 space-y-1">
                  <p className="text-[11px] font-semibold text-emerald-600 dark:text-emerald-400">{t.tp}</p>
                  <p className="text-xl font-bold font-mono text-foreground" data-testid="tp-count">{cls.truePositives}</p>
                  <p className="text-[10px] text-muted-foreground">Prognoz: HA, Fakt: HA</p>
                </div>
                <div className="p-3 space-y-1">
                  <p className="text-[11px] font-semibold text-emerald-600 dark:text-emerald-400">{t.tn}</p>
                  <p className="text-xl font-bold font-mono text-foreground" data-testid="tn-count">{cls.trueNegatives}</p>
                  <p className="text-[10px] text-muted-foreground">Prognoz: YO‘Q, Fakt: YO‘Q</p>
                </div>
                <div className="p-3 space-y-1">
                  <p className="text-[11px] font-semibold text-amber-600 dark:text-amber-400">{t.fp}</p>
                  <p className="text-xl font-bold font-mono text-foreground" data-testid="fp-count">{cls.falsePositives}</p>
                  <p className="text-[10px] text-muted-foreground">Prognoz: HA, Fakt: YO‘Q</p>
                </div>
                <div className="p-3 space-y-1">
                  <p className="text-[11px] font-semibold text-rose-600 dark:text-rose-400">{t.fn}</p>
                  <p className="text-xl font-bold font-mono text-foreground" data-testid="fn-count">{cls.falseNegatives}</p>
                  <p className="text-[10px] text-muted-foreground">Prognoz: YO‘Q, Fakt: HA</p>
                </div>
                <div className="p-3 space-y-1 bg-muted/20">
                  <p className="text-[11px] font-semibold text-rose-500">{t.interrupted}</p>
                  <p className="text-xl font-bold font-mono text-foreground" data-testid="interrupted-count">{cls.interruptedBySafetyCount}</p>
                  <p className="text-[10px] text-muted-foreground">Gidravlik blok (soxta xato emas)</p>
                </div>
              </div>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
