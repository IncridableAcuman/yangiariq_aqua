import React from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/shared/ui/dialog'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  CheckCircle2,
  XCircle,
  ShieldAlert,
  Compass,
} from 'lucide-react'
import {
  PredictionClassificationResult,
  PredictionEvaluationDetail,
} from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface EvaluationDetailModalProps {
  evaluation?: PredictionEvaluationDetail | null
  isOpen: boolean
  onClose: () => void
  lang: SupportedLanguage
}

export const EvaluationDetailModal: React.FC<EvaluationDetailModalProps> = ({
  evaluation,
  isOpen,
  onClose,
  lang,
}) => {
  const t = {
    uz: {
      title: 'Prognoz Baholash Auditi (Prediction Evaluation Audit)',
      subtitle: 'Bashorat qiymati, haqiqiy o‘lchov va operatsion kontekstning batafsil tekshiruvi',
      forecastSection: 'Prognoz Qiymatlari',
      outcomeSection: 'Haqiqiy Dala Natijasi',
      errorSection: 'Xatolik va Tolerans Tahlili',
      classificationSection: 'Tasniflash va Adashish Matritsasi',
      operationalSection: 'Operatsion va Xavfsizlik Konteksti',
      humanSection: 'Inson Qarori (Human-in-the-Loop)',
      predicted: 'Prognoz qilingan',
      actual: 'Haqiqiy o‘lchangan',
      confidence: 'Ishonchlilik',
      bounds: 'Oraliq chegaralari',
      observedAt: 'O‘lchov vaqti',
      absError: 'Mutlaq xatolik (Abs Error)',
      relError: 'Nisbiy xatolik (%)',
      dirError: 'Yo‘nalishli og‘ish (Directional)',
      withinBounds: 'Oraliqqa moslik',
      status: 'Holat',
      modelVersion: 'Model Versiyasi',
      method: 'Baholash Usuli',
      failureReason: 'Xatolik / Yetishmovchilik Sababi',
      close: 'Yopish',
    },
    ru: {
      title: 'Аудит Оценки Прогноза (Prediction Evaluation Audit)',
      subtitle: 'Детальная верификация прогноза, фактического измерения и операционного контекста',
      forecastSection: 'Параметры Прогноза',
      outcomeSection: 'Фактический Результат',
      errorSection: 'Анализ Ошибки и Допусков',
      classificationSection: 'Классификация и Матрица Ошибок',
      operationalSection: 'Операционный Контекст и Безопасность',
      humanSection: 'Человеческое Решение (Human-in-the-Loop)',
      predicted: 'Прогноз',
      actual: 'Факт',
      confidence: 'Уверенность',
      bounds: 'Доверительный интервал',
      observedAt: 'Время фиксации факта',
      absError: 'Абсолютная ошибка',
      relError: 'Относительная ошибка (%)',
      dirError: 'Направленное смещение',
      withinBounds: 'Попадание в интервал',
      status: 'Статус',
      modelVersion: 'Версия Модели',
      method: 'Метод Оценки',
      failureReason: 'Причина Сбоя / Недостатка',
      close: 'Закрыть',
    },
    en: {
      title: 'Prediction Evaluation Audit & Ground-Truth Record',
      subtitle: 'Auditing forecast parameters, telemetry matching, and operational intervention',
      forecastSection: 'Forecast Parameters',
      outcomeSection: 'Observed Operational Reality',
      errorSection: 'Error & Tolerance Analysis',
      classificationSection: 'Classification & Confusion Matrix',
      operationalSection: 'Operational & Safety Context',
      humanSection: 'Human-in-the-Loop Audit',
      predicted: 'Forecasted',
      actual: 'Observed Fact',
      confidence: 'Confidence',
      bounds: 'Uncertainty Interval',
      observedAt: 'Observation Timestamp',
      absError: 'Absolute Error',
      relError: 'Relative Error (%)',
      dirError: 'Directional Error',
      withinBounds: 'Within Bounds',
      status: 'Status',
      modelVersion: 'Model Version',
      method: 'Evaluation Method',
      failureReason: 'Failure / Gating Reason',
      close: 'Close',
    },
  }[lang]

  if (!evaluation) return null

  const getClassificationBadge = (res: PredictionClassificationResult) => {
    switch (res) {
      case 'TRUE_POSITIVE':
        return <Badge className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/30">TRUE POSITIVE</Badge>
      case 'TRUE_NEGATIVE':
        return <Badge className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/30">TRUE NEGATIVE</Badge>
      case 'FALSE_POSITIVE':
        return <Badge className="bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/30">FALSE POSITIVE</Badge>
      case 'FALSE_NEGATIVE':
        return <Badge className="bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/30">FALSE NEGATIVE</Badge>
      case 'INTERRUPTED':
        return <Badge className="bg-purple-500/10 text-purple-600 dark:text-purple-400 border-purple-500/30">INTERRUPTED BY SAFETY</Badge>
      default:
        return <Badge variant="outline" className="text-muted-foreground">N/A</Badge>
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto" data-testid="evaluation-detail-modal">
        <DialogHeader className="border-b border-border pb-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Compass className="h-5 w-5 text-primary" />
              <DialogTitle className="text-base font-bold">{t.title}</DialogTitle>
            </div>
            {getClassificationBadge(evaluation.classificationResult)}
          </div>
          <DialogDescription className="text-xs text-muted-foreground mt-1">
            {t.subtitle}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-2">
          {/* Metadata Bar */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 p-2.5 rounded-lg bg-muted/40 border border-border/60 text-xs">
            <div>
              <span className="text-muted-foreground text-[10px] block">Turi:</span>
              <span className="font-semibold text-foreground">{evaluation.predictionType}</span>
            </div>
            <div>
              <span className="text-muted-foreground text-[10px] block">Gorizont:</span>
              <span className="font-semibold text-foreground">{evaluation.horizon}</span>
            </div>
            <div>
              <span className="text-muted-foreground text-[10px] block">{t.modelVersion}:</span>
              <span className="font-semibold font-mono text-foreground">{evaluation.modelVersion}</span>
            </div>
            <div>
              <span className="text-muted-foreground text-[10px] block">{t.status}:</span>
              <Badge variant="outline" className="text-[10px] font-mono mt-0.5">
                {evaluation.evaluationStatus}
              </Badge>
            </div>
          </div>

          {/* Forecast vs Outcome Comparison */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {/* Forecast Panel */}
            <div className="p-3.5 rounded-lg border border-border bg-card space-y-2">
              <span className="font-semibold text-xs text-primary uppercase tracking-wider block">
                {t.forecastSection}
              </span>
              <div className="space-y-1.5 text-xs">
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">{t.predicted}:</span>
                  <span className="font-mono font-bold text-foreground">
                    {evaluation.predictedValue !== null && evaluation.predictedValue !== undefined
                      ? evaluation.predictedValue
                      : evaluation.predictedClass || '—'}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">{t.confidence}:</span>
                  <span className="font-mono font-bold text-emerald-600 dark:text-emerald-400">
                    {evaluation.confidencePercent}%
                  </span>
                </div>
                {evaluation.lowerBound !== null && evaluation.upperBound !== null && (
                  <div className="flex justify-between items-center">
                    <span className="text-muted-foreground">{t.bounds}:</span>
                    <span className="font-mono text-muted-foreground text-[11px]">
                      [{evaluation.lowerBound} — {evaluation.upperBound}]
                    </span>
                  </div>
                )}
                <div className="flex justify-between items-center text-[11px] text-muted-foreground pt-1 border-t border-border/40">
                  <span>Nishon vaqti:</span>
                  <span className="font-mono">{new Date(evaluation.targetTime).toLocaleString()}</span>
                </div>
              </div>
            </div>

            {/* Actual Reality Panel */}
            <div className="p-3.5 rounded-lg border border-border bg-card space-y-2">
              <span className="font-semibold text-xs text-emerald-600 dark:text-emerald-400 uppercase tracking-wider block">
                {t.outcomeSection}
              </span>
              <div className="space-y-1.5 text-xs">
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">{t.actual}:</span>
                  <span className="font-mono font-bold text-foreground">
                    {evaluation.actualValue !== null && evaluation.actualValue !== undefined
                      ? evaluation.actualValue
                      : evaluation.actualClass || '—'}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">{t.observedAt}:</span>
                  <span className="font-mono text-muted-foreground text-[11px]">
                    {evaluation.actualObservedAt ? new Date(evaluation.actualObservedAt).toLocaleString() : '—'}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-muted-foreground">Telemetriya Sifati:</span>
                  <Badge variant="outline" className="font-mono text-[10px]">
                    {evaluation.dataQuality}
                  </Badge>
                </div>
                <div className="flex justify-between items-center text-[11px] text-muted-foreground pt-1 border-t border-border/40">
                  <span>Hodisa ID:</span>
                  <span className="font-mono text-[10px] truncate max-w-[140px]">
                    {evaluation.operationalEventId || 'Yo‘q'}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Numerical Regression Error & Bounds Check (if available) */}
          {(evaluation.absoluteError !== null || evaluation.withinBounds !== null) && (
            <div className="p-3.5 rounded-lg border border-border bg-card space-y-2">
              <span className="font-semibold text-xs text-foreground uppercase tracking-wider block">
                {t.errorSection}
              </span>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs">
                <div className="p-2 rounded bg-muted/40 border border-border/40">
                  <span className="text-[10px] text-muted-foreground block">{t.absError}</span>
                  <span className="font-mono font-bold text-foreground">
                    {evaluation.absoluteError !== null ? evaluation.absoluteError.toFixed(2) : '—'}
                  </span>
                </div>

                <div className="p-2 rounded bg-muted/40 border border-border/40">
                  <span className="text-[10px] text-muted-foreground block">{t.relError}</span>
                  <span className="font-mono font-bold text-foreground">
                    {evaluation.relativeErrorPercent !== null ? `${evaluation.relativeErrorPercent.toFixed(1)}%` : '—'}
                  </span>
                </div>

                <div className="p-2 rounded bg-muted/40 border border-border/40">
                  <span className="text-[10px] text-muted-foreground block">{t.dirError}</span>
                  <span className="font-mono font-bold text-foreground">
                    {evaluation.directionalError !== null
                      ? `${evaluation.directionalError > 0 ? '+' : ''}${evaluation.directionalError.toFixed(2)}`
                      : '—'}
                  </span>
                </div>

                <div className="p-2 rounded bg-muted/40 border border-border/40">
                  <span className="text-[10px] text-muted-foreground block">{t.withinBounds}</span>
                  <span className="font-mono font-bold text-foreground">
                    {evaluation.withinBounds === true ? (
                      <span className="text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
                        <CheckCircle2 className="h-3 w-3" /> HA
                      </span>
                    ) : evaluation.withinBounds === false ? (
                      <span className="text-rose-600 dark:text-rose-400 flex items-center gap-1">
                        <XCircle className="h-3 w-3" /> YO‘Q
                      </span>
                    ) : (
                      '—'
                    )}
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* Operational & Hydraulic Safety Context */}
          <div className="p-3.5 rounded-lg border border-border bg-card space-y-2">
            <span className="font-semibold text-xs text-foreground uppercase tracking-wider block">
              {t.operationalSection}
            </span>
            <div className="space-y-1.5 text-xs">
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">Gidravlik Blok (Safety Interlock):</span>
                {evaluation.safetyInterlockTriggered ? (
                  <Badge className="bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/30 gap-1 font-mono text-[10px]">
                    <ShieldAlert className="h-3 w-3" /> TRIGGERED
                  </Badge>
                ) : (
                  <Badge variant="outline" className="text-muted-foreground text-[10px] font-mono">
                    OFF
                  </Badge>
                )}
              </div>
              {evaluation.operationalNotes && (
                <div className="bg-muted/40 p-2 rounded text-[11px] text-muted-foreground border border-border/40">
                  <span className="font-semibold text-foreground mr-1">Operatsion izoh:</span>
                  <span>{evaluation.operationalNotes}</span>
                </div>
              )}
            </div>
          </div>

          {/* Human Decision Audit */}
          {(evaluation.recommendedValue !== null || evaluation.approvedValue !== null) && (
            <div className="p-3.5 rounded-lg border border-border bg-card space-y-2">
              <span className="font-semibold text-xs text-foreground uppercase tracking-wider block">
                {t.humanSection}
              </span>
              <div className="grid grid-cols-3 gap-2 text-xs">
                <div className="p-2 rounded bg-muted/40 border border-border/40">
                  <span className="text-[10px] text-muted-foreground block">AI Tavsiyasi:</span>
                  <span className="font-mono font-bold">{evaluation.recommendedValue ?? '—'}</span>
                </div>
                <div className="p-2 rounded bg-muted/40 border border-border/40">
                  <span className="text-[10px] text-muted-foreground block">Inson Qarori:</span>
                  <span className="font-mono font-bold">{evaluation.approvedValue ?? '—'}</span>
                </div>
                <div className="p-2 rounded bg-muted/40 border border-border/40">
                  <span className="text-[10px] text-muted-foreground block">Haqiqiy Sarf:</span>
                  <span className="font-mono font-bold">{evaluation.actualValue ?? '—'}</span>
                </div>
              </div>
            </div>
          )}

          {/* Diagnostic Failure Reason & Notes */}
          {evaluation.failureReason !== 'NONE' && (
            <div className="p-3 rounded-lg border border-amber-500/30 bg-amber-500/10 text-xs space-y-1">
              <span className="font-semibold text-amber-900 dark:text-amber-200">
                {t.failureReason}: {evaluation.failureReason}
              </span>
              {evaluation.notes && (
                <p className="text-amber-800 dark:text-amber-300 text-[11px]">{evaluation.notes}</p>
              )}
            </div>
          )}
        </div>

        <div className="pt-2 border-t border-border flex justify-end">
          <Button variant="outline" size="sm" onClick={onClose} className="text-xs">
            {t.close}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  )
}
