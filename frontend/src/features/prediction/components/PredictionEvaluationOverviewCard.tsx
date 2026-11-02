import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  HelpCircle,
  Play,
  RotateCw,
  TrendingDown,
} from 'lucide-react'
import {
  ModelHealthStatus,
  PredictionEvaluationOverview,
} from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface PredictionEvaluationOverviewCardProps {
  overview?: PredictionEvaluationOverview | null
  isLoading?: boolean
  isEvaluating?: boolean
  onTriggerEvaluation?: () => void
  lang: SupportedLanguage
}

export const PredictionEvaluationOverviewCard: React.FC<PredictionEvaluationOverviewCardProps> = ({
  overview,
  isLoading,
  isEvaluating,
  onTriggerEvaluation,
  lang,
}) => {
  const t = {
    uz: {
      title: 'Prediktiv Modellar Salomatligi va Aniqlik Holati',
      subtitle: 'Haqiqiy operatsion o‘lchovlar va natijalar asosidagi doimiy tekshiruv (Continuous Intelligence)',
      evaluateBtn: 'Kutilayotganlarni Baholash',
      evaluating: 'Baholanmoqda...',
      modelHealth: 'Model Salomatligi',
      coverage: 'Baholash Qamrovi',
      totalGenerated: 'Jami Prognozlar',
      totalEvaluated: 'Baholangan Natijalar',
      pending: 'Kutilayotgan (Kelajak)',
      insufficientData: 'Maʼlumot Yetarli Emas',
      safetyInterrupted: 'Gidravlik Blok (Xavfsizlik)',
      insufficientNotice: 'Statistik baholash uchun yetarli maʼlumot mavjud emas. Yangiariq dalalarida ishonchli xulosa chiqarish uchun kamida 10 ta tekshirilgan natija talab qilinadi.',
      driftAlert: 'Model Degratsiyasi Aniqlangan: so‘nggi davrdagi xatolik tarixiy ko‘rsatkichlardan sezilarli darajada yuqori.',
      healthyDesc: 'Barcha agronomik va gidravlik prognozlar ruxsat etilgan xatolik chegarasida.',
      watchDesc: 'Baʼzi turlarda o‘rtacha og‘ishish kuzatilmoqda, qayta kalibratsiya tavsiya etiladi.',
      degradedDesc: 'Prognoz xatoligi kritik chegaradan oshgan, operatsion qarorlarda ehtiyotkorlik zarur.',
      insufficientDataDesc: 'Namunalar soni yetarli emas (n < 10).',
    },
    ru: {
      title: 'Здоровье Предиктивных Моделей и Оценка Точности',
      subtitle: 'Непрерывная верификация на основе фактических полевых измерений (Continuous Intelligence)',
      evaluateBtn: 'Оценить Ожидающие',
      evaluating: 'Оценка...',
      modelHealth: 'Здоровье Модели',
      coverage: 'Охват Оценки',
      totalGenerated: 'Всего Прогнозов',
      totalEvaluated: 'Оценено Фактом',
      pending: 'В ожидании горизонта',
      insufficientData: 'Недостаточно данных',
      safetyInterrupted: 'Прервано безопасностью',
      insufficientNotice: 'Недостаточно данных для достоверной оценки. Для исключения ложной уверенности требуется минимум 10 завершенных измерений.',
      driftAlert: 'Обнаружен дрейф точности модели: ошибка прогнозов за недавний период превышает базовый порог.',
      healthyDesc: 'Все модели находятся в пределах допустимой погрешности.',
      watchDesc: 'Наблюдается умеренное смещение ошибки, рекомендуется мониторинг.',
      degradedDesc: 'Погрешность превысила критический порог, требуется калибровка коэффициентов.',
      insufficientDataDesc: 'Недостаточно валидных сопоставлений (n < 10).',
    },
    en: {
      title: 'Predictive Model Health & Continuous Verification',
      subtitle: 'Rigorous ground-truth verification against observed telemetry and executed operations',
      evaluateBtn: 'Run Pending Evaluation',
      evaluating: 'Evaluating...',
      modelHealth: 'Model Health',
      coverage: 'Evaluation Coverage',
      totalGenerated: 'Total Generated',
      totalEvaluated: 'Evaluated Outcomes',
      pending: 'Pending Horizon',
      insufficientData: 'Insufficient Data',
      safetyInterrupted: 'Safety Interrupted',
      insufficientNotice: 'Insufficient historical sample size for definitive evaluation (n < 10). Performance metrics are honestly gated to prevent false confidence.',
      driftAlert: 'Performance Drift Detected: recent error variance exceeds baseline historical threshold.',
      healthyDesc: 'All predictive models operating within authoritative agronomic error tolerances.',
      watchDesc: 'Moderate systematic bias or elevated variance observed; calibration recommended.',
      degradedDesc: 'Severe error deviation detected; operational decisions require human verification.',
      insufficientDataDesc: 'Sample size below minimum evaluation threshold (n < 10).',
    },
  }[lang]

  const getHealthBadge = (health?: ModelHealthStatus) => {
    switch (health) {
      case 'HEALTHY':
        return (
          <Badge className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30 gap-1.5 px-3 py-1 font-semibold">
            <CheckCircle2 className="h-4 w-4" />
            <span>HEALTHY</span>
          </Badge>
        )
      case 'WATCH':
        return (
          <Badge className="bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/30 gap-1.5 px-3 py-1 font-semibold">
            <AlertTriangle className="h-4 w-4" />
            <span>WATCH</span>
          </Badge>
        )
      case 'DEGRADED':
        return (
          <Badge className="bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/30 gap-1.5 px-3 py-1 font-semibold">
            <TrendingDown className="h-4 w-4" />
            <span>DEGRADED</span>
          </Badge>
        )
      default:
        return (
          <Badge variant="outline" className="bg-muted text-muted-foreground gap-1.5 px-3 py-1 font-semibold">
            <HelpCircle className="h-4 w-4" />
            <span>INSUFFICIENT DATA</span>
          </Badge>
        )
    }
  }

  const sampleSize = overview?.totalEvaluated ?? 0
  const hasSufficientSample = sampleSize >= 10

  return (
    <Card className="border-border shadow-xs overflow-hidden" data-testid="prediction-evaluation-overview">
      <CardHeader className="pb-3 border-b border-border/60 bg-muted/20">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <Activity className="h-4 w-4 text-primary" />
              <CardTitle className="text-base sm:text-lg font-bold">{t.title}</CardTitle>
            </div>
            <CardDescription className="text-xs text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>

          <div className="flex items-center gap-2">
            {getHealthBadge(overview?.overallModelHealth)}
            {onTriggerEvaluation && (
              <Button
                variant="outline"
                size="sm"
                onClick={onTriggerEvaluation}
                disabled={isEvaluating || isLoading}
                className="h-8 gap-1.5 text-xs font-medium"
                data-testid="trigger-evaluation-button"
              >
                {isEvaluating ? (
                  <RotateCw className="h-3.5 w-3.5 animate-spin" />
                ) : (
                  <Play className="h-3.5 w-3.5 fill-current" />
                )}
                <span>{isEvaluating ? t.evaluating : t.evaluateBtn}</span>
              </Button>
            )}
          </div>
        </div>
      </CardHeader>

      <CardContent className="pt-4 space-y-4">
        {/* Performance Drift Alert Banner */}
        {overview?.performanceDriftDetected && (
          <div className="rounded-lg border border-amber-500/30 bg-amber-500/10 p-3.5 flex items-start gap-3 text-amber-800 dark:text-amber-200" data-testid="drift-alert-banner">
            <AlertTriangle className="h-5 w-5 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
            <div className="text-xs space-y-1">
              <p className="font-semibold">{t.driftAlert}</p>
              <p className="opacity-90">{overview.driftSummary || overview.overallHealthReason}</p>
            </div>
          </div>
        )}

        {/* Insufficient Sample Size Banner */}
        {!hasSufficientSample && (
          <div className="rounded-lg border border-sky-500/30 bg-sky-500/5 p-3.5 flex items-start gap-3" data-testid="insufficient-data-banner">
            <HelpCircle className="h-5 w-5 text-sky-500 shrink-0 mt-0.5" />
            <div className="text-xs space-y-1">
              <p className="font-semibold text-foreground">
                {t.insufficientNotice}
              </p>
              <p className="text-muted-foreground">
                Hozirda baholangan namunalar: <span className="font-mono font-bold text-foreground">{sampleSize}</span> / 10 ta minimal kerak.
              </p>
            </div>
          </div>
        )}

        {/* Primary Health Reason */}
        {overview?.overallHealthReason && (
          <div className="text-xs text-muted-foreground bg-muted/40 p-2.5 rounded-md border border-border/40">
            <span className="font-semibold text-foreground mr-1.5">Diagnostika:</span>
            <span>{overview.overallHealthReason}</span>
          </div>
        )}

        {/* KPI Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
          <div className="p-3 rounded-lg border border-border/80 bg-card">
            <p className="text-[11px] font-medium text-muted-foreground">{t.totalGenerated}</p>
            <p className="text-xl font-bold font-mono text-foreground mt-0.5">
              {overview?.totalPredictionsGenerated ?? 0}
            </p>
          </div>

          <div className="p-3 rounded-lg border border-border/80 bg-card">
            <p className="text-[11px] font-medium text-muted-foreground">{t.totalEvaluated}</p>
            <p className="text-xl font-bold font-mono text-emerald-600 dark:text-emerald-400 mt-0.5">
              {overview?.totalEvaluated ?? 0}
            </p>
          </div>

          <div className="p-3 rounded-lg border border-border/80 bg-card">
            <p className="text-[11px] font-medium text-muted-foreground">{t.coverage}</p>
            <p className="text-xl font-bold font-mono text-foreground mt-0.5">
              {overview?.evaluationCoveragePercentage !== null && overview?.evaluationCoveragePercentage !== undefined
                ? `${overview.evaluationCoveragePercentage}%`
                : '—'}
            </p>
          </div>

          <div className="p-3 rounded-lg border border-border/80 bg-card">
            <p className="text-[11px] font-medium text-muted-foreground">{t.pending}</p>
            <p className="text-xl font-bold font-mono text-muted-foreground mt-0.5">
              {overview?.pendingCount ?? 0}
            </p>
          </div>

          <div className="p-3 rounded-lg border border-border/80 bg-card">
            <p className="text-[11px] font-medium text-muted-foreground">{t.insufficientData}</p>
            <p className="text-xl font-bold font-mono text-amber-600 dark:text-amber-400 mt-0.5">
              {overview?.insufficientDataCount ?? 0}
            </p>
          </div>

          <div className="p-3 rounded-lg border border-border/80 bg-card">
            <p className="text-[11px] font-medium text-muted-foreground">{t.safetyInterrupted}</p>
            <p className="text-xl font-bold font-mono text-rose-600 dark:text-rose-400 mt-0.5">
              {overview?.interruptedBySafetyCount ?? 0}
            </p>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
