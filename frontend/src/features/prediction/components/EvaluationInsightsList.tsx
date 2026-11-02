import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  Lightbulb,
  ShieldAlert,
  ArrowRight,
  TrendingDown,
  WifiOff,
} from 'lucide-react'
import { EvaluationInsight } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface EvaluationInsightsListProps {
  insights?: EvaluationInsight[]
  lang: SupportedLanguage
  onNavigateToAction?: (module: string) => void
}

export const EvaluationInsightsList: React.FC<EvaluationInsightsListProps> = ({
  insights = [],
  lang,
  onNavigateToAction,
}) => {
  const t = {
    uz: {
      title: 'Avtomatlashgan Diagnostika va Tavsiyalar (Continuous Intelligence)',
      subtitle: 'Xatoliklar tahlili va model parametrlarini yaxshilash bo‘yicha aniq agronomik tavsiyalar',
      evidence: 'Asos / Dalil',
      recommendation: 'Tavsiya etilgan tuzatish',
      takeAction: 'Ko‘rib chiqish',
      noInsights: 'Hozirda tizimda kritik model og‘ishlari yoki telemetriya to‘siqlari aniqlanmadi.',
    },
    ru: {
      title: 'Автоматическая Диагностика и Рекомендации (Continuous Intelligence)',
      subtitle: 'Детерминированные инсайты по улучшению калибровок и устранению систематических ошибок',
      evidence: 'Обоснование / Факт',
      recommendation: 'Рекомендованная корректировка',
      takeAction: 'Перейти к действию',
      noInsights: 'Критических отклонений моделей или блокировок телеметрии в данный момент не зафиксировано.',
    },
    en: {
      title: 'Deterministic Model Diagnostics & Actionable Insights',
      subtitle: 'Empirically grounded recommendations to remediate systematic biases and telemetry deficits',
      evidence: 'Empirical Evidence',
      recommendation: 'Recommended Adjustment',
      takeAction: 'Take Action',
      noInsights: 'No active systemic model anomalies or telemetry bottlenecks identified.',
    },
  }[lang]

  if (!insights || insights.length === 0) {
    return (
      <Card className="border-border shadow-xs">
        <CardContent className="py-6 text-center text-xs text-muted-foreground flex items-center justify-center gap-2">
          <Lightbulb className="h-4 w-4 text-emerald-500" />
          <span>{t.noInsights}</span>
        </CardContent>
      </Card>
    )
  }

  const getSeverityBadge = (severity: 'INFO' | 'WARNING' | 'CRITICAL') => {
    switch (severity) {
      case 'CRITICAL':
        return (
          <Badge className="bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/30 text-[10px] font-semibold">
            CRITICAL
          </Badge>
        )
      case 'WARNING':
        return (
          <Badge className="bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/30 text-[10px] font-semibold">
            WARNING
          </Badge>
        )
      default:
        return (
          <Badge className="bg-sky-500/10 text-sky-600 dark:text-sky-400 border-sky-500/30 text-[10px] font-semibold">
            INFO
          </Badge>
        )
    }
  }

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case 'SAFETY':
        return <ShieldAlert className="h-4 w-4 text-rose-500" />
      case 'TELEMETRY':
        return <WifiOff className="h-4 w-4 text-amber-500" />
      case 'BIAS':
        return <TrendingDown className="h-4 w-4 text-amber-500" />
      default:
        return <Lightbulb className="h-4 w-4 text-primary" />
    }
  }

  return (
    <Card className="border-border shadow-xs" data-testid="evaluation-insights-list">
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex items-center justify-between">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <Lightbulb className="h-4 w-4 text-primary" />
              <CardTitle className="text-base font-bold">{t.title}</CardTitle>
            </div>
            <CardDescription className="text-xs text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>
        </div>
      </CardHeader>

      <CardContent className="pt-4 space-y-3">
        {insights.map((insight) => (
          <div
            key={insight.id}
            className="p-3.5 rounded-lg border border-border bg-card space-y-2 hover:border-primary/40 transition-colors"
          >
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div className="flex items-center gap-2">
                {getCategoryIcon(insight.category)}
                <span className="font-semibold text-xs text-foreground">{insight.title}</span>
              </div>
              <div className="flex items-center gap-2">
                <Badge variant="outline" className="font-mono text-[10px]">
                  {insight.category}
                </Badge>
                {getSeverityBadge(insight.severity)}
              </div>
            </div>

            <p className="text-xs text-muted-foreground leading-relaxed">
              {insight.description}
            </p>

            <div className="pt-2 border-t border-border/40 grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
              <div className="bg-muted/30 p-2 rounded border border-border/30 space-y-0.5">
                <span className="font-semibold text-[11px] text-foreground">{t.evidence}:</span>
                <p className="text-[11px] text-muted-foreground font-mono">{insight.evidence}</p>
              </div>

              <div className="bg-primary/5 p-2 rounded border border-primary/20 space-y-0.5 flex flex-col justify-between">
                <div>
                  <span className="font-semibold text-[11px] text-primary">{t.recommendation}:</span>
                  <p className="text-[11px] text-foreground font-medium">{insight.recommendation}</p>
                </div>
                {onNavigateToAction && insight.actionTarget && (
                  <Button
                    variant="link"
                    size="sm"
                    className="h-5 p-0 text-[11px] text-primary justify-start gap-1 font-semibold mt-1"
                    onClick={() => onNavigateToAction(insight.actionTarget)}
                  >
                    <span>{t.takeAction}</span>
                    <ArrowRight className="h-3 w-3" />
                  </Button>
                )}
              </div>
            </div>
          </div>
        ))}
      </CardContent>
    </Card>
  )
}
