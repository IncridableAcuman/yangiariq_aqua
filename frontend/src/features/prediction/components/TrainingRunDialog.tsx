import React, { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/ui/dialog'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  Brain,
  Sparkles,
  ShieldCheck,
  AlertTriangle,
  CheckCircle2,
  RotateCcw,
} from 'lucide-react'
import {
  TrainPredictionModelRequest,
  TrainPredictionModelResponse,
} from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface TrainingRunDialogProps {
  isOpen: boolean
  onClose: () => void
  onStartTraining: (request: TrainPredictionModelRequest) => Promise<TrainPredictionModelResponse>
  isTraining: boolean
  lang: SupportedLanguage
}

export const TrainingRunDialog: React.FC<TrainingRunDialogProps> = ({
  isOpen,
  onClose,
  onStartTraining,
  isTraining,
  lang,
}) => {
  const [daysRange, setDaysRange] = useState<number>(90)
  const [l2Penalty, setL2Penalty] = useState<number>(1.0)
  const [result, setResult] = useState<TrainPredictionModelResponse | null>(null)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)

  const t = {
    uz: {
      dialogTitle: 'Boshqariladigan ML Model O‘qitish Jarayoni',
      dialogDescription: 'Tarixiy tasdiqlangan telemetriya maʼlumotlari asosida yangi model gipotezasini o‘qitish va sinash',
      targetType: 'Prognoz Turi',
      historicalWindow: 'Tarixiy Vaqt Oralig‘i',
      days30: 'Oxirgi 30 kun',
      days60: 'Oxirgi 60 kun',
      days90: 'Oxirgi 90 kun (Tavsiya)',
      regularization: 'L2 Regulyarizatsiya Jarimasi (λ)',
      preflightChecks: 'O‘qitishdan Oldingi Qatʼiy Qoidalar',
      rule1: 'Vaqt bo‘yicha ajratish: 60% Train, 20% Val, 20% Test (0% sizib chiqish).',
      rule2: 'Minimal namuna chegarasi: kamida 30 ta tasdiqlangan yer osti o‘lchovlari (N >= 30).',
      rule3: 'FEAT_SOIL_MOISTURE_V1: 14 ta standartlashtirilgan agronomik belgi.',
      rule4: 'Avtonom faollashuv taqiqlangan: faqat CANDIDATE maqomi beriladi.',
      startBtn: 'Boshqariladigan O‘qitishni Boshlash',
      trainingInProgress: 'Matritsalar hisoblanmoqda va o‘qitilmoqda...',
      successTitle: 'Model Nomzodi Muvaffaqiyatli Yaratildi',
      insufficientTitle: 'Yetarli O‘qitish Maʼlumotlari Mavjud Emas',
      insufficientBody: 'Platforma ushbu davr uchun ML model nomzodini xavfsiz o‘qitish yoki tasdiqlash imkoniyatiga ega emas. Deterministik agronomik baseline faol qoladi.',
      verifiedFound: 'Mavjud tasdiqlangan namunalar',
      requiredMin: 'Talab qilinadigan minimum',
      close: 'Yopish',
    },
    ru: {
      dialogTitle: 'Контролируемое Обучение ML Модели',
      dialogDescription: 'Обучение новой гипотезы модели на верифицированных исторических телеметрических данных',
      targetType: 'Целевой Прогноз',
      historicalWindow: 'Исторический Период',
      days30: 'Последние 30 дней',
      days60: 'Последние 60 дней',
      days90: 'Последние 90 дней (Рекомендовано)',
      regularization: 'L2 Регуляризация (λ)',
      preflightChecks: 'Обязательные Правила и Контроль Данных',
      rule1: 'Хронологическое разделение: 60% Train, 20% Val, 20% Test (0% утечки данных).',
      rule2: 'Порог минимальной выборки: не менее 30 верифицированных замеров (N >= 30).',
      rule3: 'FEAT_SOIL_MOISTURE_V1: 14 стандартизированных агрономических признаков.',
      rule4: 'Автономная активация запрещена: создается только статус CANDIDATE.',
      startBtn: 'Запустить Контролируемое Обучение',
      trainingInProgress: 'Обучение и расчет регрессионных матриц...',
      successTitle: 'Кандидат Модели Успешно Создан',
      insufficientTitle: 'Недостаточно Данных для Обучения',
      insufficientBody: 'Платформа не может безопасно обучить или валидировать ML кандидата. Детерминированный базис остается активным.',
      verifiedFound: 'Найдено верифицированных наблюдений',
      requiredMin: 'Требуемый минимум',
      close: 'Закрыть',
    },
    en: {
      dialogTitle: 'Controlled ML Model Training Run',
      dialogDescription: 'Initiate training for a reproducible regression candidate on verified historical ground-truth',
      targetType: 'Target Prediction Type',
      historicalWindow: 'Historical Timeline Window',
      days30: 'Past 30 Days',
      days60: 'Past 60 Days',
      days90: 'Past 90 Days (Recommended)',
      regularization: 'L2 Regularization Penalty (λ)',
      preflightChecks: 'Scientific Preflight Rules & Gates',
      rule1: 'Strict temporal partition: 60% Train, 20% Val, 20% Test (zero future leakage).',
      rule2: 'Minimum sample gate: requires at least 30 verified ground-truth pairs (N >= 30).',
      rule3: 'FEAT_SOIL_MOISTURE_V1: 14 standardized agronomic features.',
      rule4: 'Zero autonomous activation: produced strictly as an immutable CANDIDATE.',
      startBtn: 'Start Controlled Training',
      trainingInProgress: 'Fitting regularized regression model...',
      successTitle: 'Model Candidate Successfully Produced',
      insufficientTitle: 'Insufficient Training Data',
      insufficientBody: 'The platform cannot safely train or validate an ML candidate for this prediction window. The deterministic agronomic baseline remains active.',
      verifiedFound: 'Verified ground-truth samples found',
      requiredMin: 'Minimum required',
      close: 'Close',
    },
  }[lang]

  const handleRun = async () => {
    setErrorMsg(null)
    setResult(null)

    const now = new Date()
    const start = new Date(now.getTime() - daysRange * 86400 * 1000)

    try {
      const res = await onStartTraining({
        predictionType: 'SOIL_MOISTURE_FORECAST',
        periodStart: start.toISOString(),
        periodEnd: now.toISOString(),
        l2Penalty,
      })
      setResult(res)
    } catch (err: any) {
      setErrorMsg(err?.message || 'Training run failed unexpectedly')
    }
  }

  const handleReset = () => {
    setResult(null)
    setErrorMsg(null)
    onClose()
  }

  return (
    <Dialog open={isOpen} onOpenChange={handleReset}>
      <DialogContent className="max-w-xl p-5 sm:p-6" data-testid="training-run-dialog">
        <DialogHeader className="border-b border-border/60 pb-3">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-primary/10 text-primary">
              <Brain className="h-4 w-4" />
            </div>
            <DialogTitle className="text-base font-bold text-foreground">
              {t.dialogTitle}
            </DialogTitle>
          </div>
          <DialogDescription className="text-xs text-muted-foreground">
            {t.dialogDescription}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 pt-2">
          {/* Target Type */}
          <div className="space-y-1">
            <label className="text-xs font-semibold text-foreground">{t.targetType}</label>
            <div className="p-2.5 rounded-lg bg-muted/40 border border-border text-xs font-mono font-bold flex items-center justify-between">
              <span>SOIL_MOISTURE_FORECAST</span>
              <Badge variant="outline" className="text-[10px] bg-primary/5 text-primary border-primary/20">
                REGRESSION_RIDGE_HYBRID
              </Badge>
            </div>
          </div>

          {/* Historical Range Selector */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">{t.historicalWindow}</label>
            <div className="grid grid-cols-3 gap-2">
              {[
                { days: 30, label: t.days30 },
                { days: 60, label: t.days60 },
                { days: 90, label: t.days90 },
              ].map((opt) => (
                <button
                  key={opt.days}
                  type="button"
                  disabled={isTraining || !!result}
                  onClick={() => setDaysRange(opt.days)}
                  className={`p-2.5 rounded-lg text-xs font-medium border text-center transition-all ${
                    daysRange === opt.days
                      ? 'bg-primary/10 border-primary text-primary font-bold'
                      : 'bg-card border-border text-muted-foreground hover:bg-muted/40'
                  }`}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          </div>

          {/* Regularization Penalty */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">{t.regularization}</label>
            <div className="grid grid-cols-3 gap-2">
              {[0.1, 1.0, 10.0].map((val) => (
                <button
                  key={val}
                  type="button"
                  disabled={isTraining || !!result}
                  onClick={() => setL2Penalty(val)}
                  className={`p-2 rounded-lg text-xs font-mono font-medium border text-center transition-all ${
                    l2Penalty === val
                      ? 'bg-primary/10 border-primary text-primary font-bold'
                      : 'bg-card border-border text-muted-foreground hover:bg-muted/40'
                  }`}
                >
                  λ = {val}
                </button>
              ))}
            </div>
          </div>

          {/* Preflight Rules Box */}
          <div className="p-3.5 rounded-xl border border-border/80 bg-muted/20 text-xs space-y-2">
            <div className="font-semibold text-foreground text-[11px] flex items-center gap-1.5">
              <ShieldCheck className="h-4 w-4 text-emerald-500" />
              <span>{t.preflightChecks}</span>
            </div>
            <ul className="space-y-1 text-muted-foreground text-[11px] list-disc list-inside">
              <li>{t.rule1}</li>
              <li>{t.rule2}</li>
              <li>{t.rule3}</li>
              <li>{t.rule4}</li>
            </ul>
          </div>

          {/* Result: INSUFFICIENT DATA ALERT */}
          {result && result.status === 'INSUFFICIENT_TRAINING_DATA' && (
            <div className="p-4 rounded-xl border border-amber-500/30 bg-amber-500/10 text-xs space-y-2" data-testid="insufficient-data-alert">
              <div className="flex items-center gap-2 font-bold text-amber-700 dark:text-amber-300">
                <AlertTriangle className="h-4 w-4 shrink-0" />
                <span>{t.insufficientTitle}</span>
              </div>
              <p className="text-amber-800 dark:text-amber-200 text-[11px] leading-relaxed">
                {t.insufficientBody}
              </p>
              <div className="grid grid-cols-2 gap-2 pt-1 font-mono text-[11px]">
                <div className="p-2 rounded bg-card/80 border border-amber-500/20">
                  <div className="text-[10px] text-muted-foreground">{t.verifiedFound}</div>
                  <div className="font-bold text-foreground text-sm">{result.sampleCount}</div>
                </div>
                <div className="p-2 rounded bg-card/80 border border-amber-500/20">
                  <div className="text-[10px] text-muted-foreground">{t.requiredMin}</div>
                  <div className="font-bold text-foreground text-sm">{result.minRequiredSamples}</div>
                </div>
              </div>
            </div>
          )}

          {/* Result: SUCCESS ALERT */}
          {result && result.status === 'SUCCESS' && (
            <div className="p-4 rounded-xl border border-emerald-500/30 bg-emerald-500/10 text-xs space-y-2" data-testid="training-success-alert">
              <div className="flex items-center gap-2 font-bold text-emerald-700 dark:text-emerald-300">
                <CheckCircle2 className="h-4 w-4 shrink-0" />
                <span>{t.successTitle}: {result.versionTag}</span>
              </div>
              <p className="text-emerald-800 dark:text-emerald-200 text-[11px]">
                {result.message}
              </p>
              <div className="grid grid-cols-3 gap-2 pt-1 font-mono text-[11px]">
                <div className="p-2 rounded bg-card border border-emerald-500/20">
                  <div className="text-[10px] text-muted-foreground">ML Test MAE</div>
                  <div className="font-bold text-foreground">
                    {typeof result.candidateMae === 'number' ? `${result.candidateMae.toFixed(2)}%` : '—'}
                  </div>
                </div>
                <div className="p-2 rounded bg-card border border-emerald-500/20">
                  <div className="text-[10px] text-muted-foreground">Baseline MAE</div>
                  <div className="font-bold text-foreground">
                    {typeof result.baselineMae === 'number' ? `${result.baselineMae.toFixed(2)}%` : '—'}
                  </div>
                </div>
                <div className="p-2 rounded bg-card border border-emerald-500/20">
                  <div className="text-[10px] text-muted-foreground">Improvement</div>
                  <div className="font-bold text-emerald-600 dark:text-emerald-400">
                    {typeof result.improvementPercent === 'number' ? `+${result.improvementPercent.toFixed(1)}%` : '—'}
                  </div>
                </div>
              </div>
            </div>
          )}

          {errorMsg && (
            <div className="p-3 rounded-lg border border-rose-500/30 bg-rose-500/10 text-rose-600 dark:text-rose-400 text-xs">
              {errorMsg}
            </div>
          )}
        </div>

        <DialogFooter className="border-t border-border/60 pt-3 flex items-center justify-between gap-2">
          <Button variant="outline" size="sm" onClick={handleReset} className="h-8 text-xs">
            {t.close}
          </Button>

          {!result && (
            <Button
              size="sm"
              disabled={isTraining}
              onClick={handleRun}
              className="h-8 text-xs font-semibold gap-1.5 bg-primary text-primary-foreground hover:bg-primary/90"
            >
              {isTraining ? (
                <>
                  <RotateCcw className="h-3.5 w-3.5 animate-spin" />
                  <span>{t.trainingInProgress}</span>
                </>
              ) : (
                <>
                  <Sparkles className="h-3.5 w-3.5" />
                  <span>{t.startBtn}</span>
                </>
              )}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
