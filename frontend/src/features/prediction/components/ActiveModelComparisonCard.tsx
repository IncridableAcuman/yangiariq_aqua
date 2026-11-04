import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  Brain,
  ShieldCheck,
  TrendingDown,
  Database,
  Layers,
  Sparkles,
  CheckCircle2,
} from 'lucide-react'
import { ActiveModelComparisonResponse } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface ActiveModelComparisonCardProps {
  comparison?: ActiveModelComparisonResponse | null
  isLoading?: boolean
  onOpenTrainingDialog: () => void
  lang: SupportedLanguage
  userRole?: string
}

export const ActiveModelComparisonCard: React.FC<ActiveModelComparisonCardProps> = ({
  comparison,
  isLoading,
  onOpenTrainingDialog,
  lang,
  userRole,
}) => {
  const t = {
    uz: {
      sectionTitle: 'Prediktiv Modellar va Gipotezalar',
      sectionSubtitle: 'Faol model strategiyasi, deterministic baseline va boshqariladigan ML gipotezalari qiyosi',
      activeModel: 'Faol Prognozlash Modeli',
      strategyType: 'Strategiya Turi',
      status: 'Boshqaruv Holati',
      baselineComparison: 'Deterministik Baseline Qiyosi',
      mae: 'O‘rtacha Xatolik (MAE)',
      baselineMae: 'Baseline MAE',
      relativeGain: 'Aniqlik Yaxshilanishi',
      sampleSize: 'Tasdiqlangan Namuna',
      contextHealth: 'Kontekst Salomatligi',
      dataQuality: 'Maʼlumotlar Sifati',
      trainBtn: 'Boshqariladigan ML O‘qitishni Boshlash',
      hybridActive: 'Gibrid ML Aktiv',
      deterministicBaseline: 'Faqat Deterministik Baseline',
      rationale: 'Ilmiy Asos',
    },
    ru: {
      sectionTitle: 'Предиктивные Модели и Обучение на Фактах',
      sectionSubtitle: 'Сравнение активной стратегии, детерминированного базиса и управляемых ML-кандидатов',
      activeModel: 'Активная Модель Прогнозирования',
      strategyType: 'Тип Стратегии',
      status: 'Статус Управления',
      baselineComparison: 'Сравнение с Детерминированным Базисом',
      mae: 'Средняя Ошибка (MAE)',
      baselineMae: 'Базисный MAE',
      relativeGain: 'Прирост Точности',
      sampleSize: 'Проверенная Выборка',
      contextHealth: 'Здоровье Контекста',
      dataQuality: 'Качество Данных',
      trainBtn: 'Запустить Контролируемое Обучение ML',
      hybridActive: 'Гибридный ML Активен',
      deterministicBaseline: 'Детерминированный Базис',
      rationale: 'Научное Обоснование',
    },
    en: {
      sectionTitle: 'Predictive Models & Evidence-Based Learning',
      sectionSubtitle: 'Comparison of active production strategy, agronomic baseline, and governed ML candidates',
      activeModel: 'Active Prediction Model',
      strategyType: 'Strategy Type',
      status: 'Governance Status',
      baselineComparison: 'Deterministic Baseline Comparison',
      mae: 'Mean Absolute Error (MAE)',
      baselineMae: 'Baseline MAE',
      relativeGain: 'Accuracy Improvement',
      sampleSize: 'Verified Samples',
      contextHealth: 'Context Health',
      dataQuality: 'Data Quality',
      trainBtn: 'Start Controlled ML Training',
      hybridActive: 'Hybrid ML Active',
      deterministicBaseline: 'Deterministic Baseline Only',
      rationale: 'Scientific Rationale',
    },
  }[lang]

  const isAdmin = userRole === 'ADMIN'

  return (
    <Card className="border border-border/80 shadow-xs bg-gradient-to-br from-card via-card to-primary/5 overflow-hidden">
      <CardHeader className="p-4 sm:p-5 border-b border-border/50 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-primary/10 text-primary">
              <Brain className="h-4 w-4" />
            </div>
            <CardTitle className="text-base font-bold tracking-tight text-foreground">
              {t.sectionTitle}
            </CardTitle>
            <Badge variant="outline" className="font-mono text-[10px] bg-primary/5 text-primary border-primary/20">
              MILESTONE 08.11
            </Badge>
          </div>
          <CardDescription className="text-xs text-muted-foreground">
            {t.sectionSubtitle}
          </CardDescription>
        </div>

        {isAdmin && (
          <Button
            size="sm"
            onClick={onOpenTrainingDialog}
            className="gap-1.5 text-xs font-semibold shadow-xs bg-primary text-primary-foreground hover:bg-primary/90 h-8 self-start sm:self-auto"
          >
            <Sparkles className="h-3.5 w-3.5" />
            <span>{t.trainBtn}</span>
          </Button>
        )}
      </CardHeader>

      <CardContent className="p-4 sm:p-5">
        {isLoading ? (
          <div className="py-8 text-center text-xs text-muted-foreground animate-pulse">
            Loading active predictive strategy evidence...
          </div>
        ) : comparison ? (
          <div className="space-y-4">
            {/* Top Identity & Status Row */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-3.5 rounded-xl bg-card border border-border/80">
              <div className="space-y-1">
                <div className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">
                  {t.activeModel}
                </div>
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-lg font-bold text-foreground font-mono">
                    {comparison.activeModelVersion}
                  </span>
                  <Badge
                    variant="outline"
                    className={
                      comparison.isHybridActive
                        ? 'bg-purple-500/10 text-purple-600 dark:text-purple-400 border-purple-500/30 font-semibold text-xs'
                        : 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/30 font-semibold text-xs'
                    }
                  >
                    {comparison.activeModelType}
                  </Badge>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="text-right">
                  <div className="text-[10px] text-muted-foreground uppercase tracking-wider">
                    {t.status}
                  </div>
                  <Badge className="bg-emerald-600 text-white font-mono text-xs font-bold mt-0.5">
                    {comparison.governanceStatus}
                  </Badge>
                </div>
              </div>
            </div>

            {/* Key Comparison Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <div className="p-3 rounded-xl bg-muted/30 border border-border/60">
                <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                  <TrendingDown className="h-3.5 w-3.5 text-emerald-500" />
                  <span>{t.mae}</span>
                </div>
                <div className="mt-1 flex items-baseline gap-1">
                  <span className="text-xl font-bold font-mono text-foreground">
                    {typeof comparison.activeMae === 'number' ? `${comparison.activeMae.toFixed(1)}%` : '—'}
                  </span>
                  {typeof comparison.baselineMae === 'number' && (
                    <span className="text-[11px] text-muted-foreground">
                      (vs {comparison.baselineMae.toFixed(1)}% {comparison.baselineModelVersion})
                    </span>
                  )}
                </div>
              </div>

              <div className="p-3 rounded-xl bg-muted/30 border border-border/60">
                <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                  <ShieldCheck className="h-3.5 w-3.5 text-sky-500" />
                  <span>{t.relativeGain}</span>
                </div>
                <div className="mt-1">
                  <span className="text-xl font-bold font-mono text-emerald-600 dark:text-emerald-400">
                    {typeof comparison.relativeImprovementPercent === 'number' && comparison.relativeImprovementPercent > 0
                      ? `+${comparison.relativeImprovementPercent.toFixed(1)}%`
                      : 'Baseline'}
                  </span>
                </div>
              </div>

              <div className="p-3 rounded-xl bg-muted/30 border border-border/60">
                <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                  <Database className="h-3.5 w-3.5 text-amber-500" />
                  <span>{t.sampleSize}</span>
                </div>
                <div className="mt-1 flex items-baseline gap-1">
                  <span className="text-xl font-bold font-mono text-foreground">
                    {comparison.sampleSize}
                  </span>
                  <span className="text-[11px] text-muted-foreground">obs (n &ge; 10)</span>
                </div>
              </div>

              <div className="p-3 rounded-xl bg-muted/30 border border-border/60">
                <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                  <CheckCircle2 className="h-3.5 w-3.5 text-purple-500" />
                  <span>{t.contextHealth}</span>
                </div>
                <div className="mt-1 flex items-center gap-1.5">
                  <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/30 text-xs">
                    {comparison.contextHealth}
                  </Badge>
                  <Badge variant="outline" className="bg-muted text-muted-foreground text-[10px]">
                    {comparison.dataQualityStatus}
                  </Badge>
                </div>
              </div>
            </div>

            {/* Rationale & Operational Footnote */}
            <div className="p-3 rounded-lg bg-card/60 border border-border/40 text-xs text-muted-foreground flex items-start gap-2">
              <Layers className="h-4 w-4 text-primary shrink-0 mt-0.5" />
              <div>
                <span className="font-semibold text-foreground mr-1">{t.rationale}:</span>
                {comparison.summaryRationale}
              </div>
            </div>
          </div>
        ) : null}
      </CardContent>
    </Card>
  )
}
