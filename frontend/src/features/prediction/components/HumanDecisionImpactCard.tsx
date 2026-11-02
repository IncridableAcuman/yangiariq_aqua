import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { UserCheck, CheckCircle2, Sliders, XCircle } from 'lucide-react'
import { HumanDecisionImpact } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface HumanDecisionImpactCardProps {
  humanImpacts?: HumanDecisionImpact[]
  lang: SupportedLanguage
}

export const HumanDecisionImpactCard: React.FC<HumanDecisionImpactCardProps> = ({
  humanImpacts = [],
  lang,
}) => {
  const t = {
    uz: {
      title: 'Insoniy Qarorlar va Model O‘zaro Ta’siri (Human-in-the-Loop Audit)',
      subtitle: 'AI tavsiyasi, operator qarori va yakuniy daladagi natija o‘rtasidagi farq tahlili',
      type: 'Prognoz Turi',
      cases: 'Jami Holatlar',
      approvedDirectly: 'To‘g‘ridan-to‘g‘ri Ma’qullangan',
      modified: 'Operator Tomonidan O‘zgartirilgan',
      rejected: 'Bekor Qilingan (Rad Etilgan)',
      accuracyWhenApproved: 'Ma’qullangandagi Aniqlik',
      impactAssessment: 'Inson Qarorining Ta’siri',
      auditQuote: '«AI tavsiya qiladi, agronom qaror qabul qiladi, haqiqiy dala natijasi baholaydi»',
      noData: 'Insoniy operatsion qarorlar bo‘yicha hozircha yetarli statistik ma’lumot to‘planmadi.',
    },
    ru: {
      title: 'Влияние Человеческих Решений на Итог (Human-in-the-Loop Audit)',
      subtitle: 'Анализ согласованности: рекомендация ИИ → решение оператора → реальный факт',
      type: 'Тип Прогноза',
      cases: 'Всего Случаев',
      approvedDirectly: 'Одобрено без изменений',
      modified: 'Скорректировано человеком',
      rejected: 'Отклонено оператором',
      accuracyWhenApproved: 'Точность при одобрении',
      impactAssessment: 'Эффект Вмешательства',
      auditQuote: '«ИИ советует, агроном решает, реальность выносит оценку»',
      noData: 'Недостаточно данных для статистического анализа человеческих решений.',
    },
    en: {
      title: 'Human-in-the-Loop Decision Impact & Audit',
      subtitle: 'Auditing alignment across Model Recommendation → Human Decision → Operational Reality',
      type: 'Forecast Type',
      cases: 'Total Cases',
      approvedDirectly: 'Approved Directly',
      modified: 'Modified by Operator',
      rejected: 'Rejected by Operator',
      accuracyWhenApproved: 'Accuracy When Approved',
      impactAssessment: 'Human Decision Impact',
      auditQuote: '“AI advises, agronomist decides, reality validates”',
      noData: 'Insufficient historical operator decision records for comparative audit.',
    },
  }[lang]

  if (!humanImpacts || humanImpacts.length === 0) {
    return null
  }

  return (
    <Card className="border-border shadow-xs" data-testid="human-decision-impact-card">
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <UserCheck className="h-4 w-4 text-primary" />
              <CardTitle className="text-base font-bold">{t.title}</CardTitle>
            </div>
            <CardDescription className="text-xs text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>
          <Badge variant="outline" className="text-[10px] text-muted-foreground italic shrink-0">
            {t.auditQuote}
          </Badge>
        </div>
      </CardHeader>

      <CardContent className="pt-4 space-y-3">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {humanImpacts.map((impact) => (
            <div
              key={impact.predictionType}
              className="p-3.5 rounded-lg border border-border bg-card space-y-2.5"
            >
              <div className="flex items-center justify-between">
                <span className="font-semibold text-xs text-foreground font-mono">
                  {impact.predictionType}
                </span>
                <span className="text-[11px] font-mono text-muted-foreground">
                  {t.cases}: {impact.totalPredictionsCount}
                </span>
              </div>

              {/* 3 Decision Buckets */}
              <div className="grid grid-cols-3 gap-2 text-center text-xs">
                <div className="p-2 rounded bg-emerald-500/5 border border-emerald-500/20 space-y-0.5">
                  <div className="flex items-center justify-center gap-1 text-emerald-600 dark:text-emerald-400">
                    <CheckCircle2 className="h-3 w-3" />
                    <span className="text-[10px] font-semibold">{t.approvedDirectly}</span>
                  </div>
                  <p className="text-base font-bold font-mono text-foreground">
                    {impact.approvedDirectlyCount}
                  </p>
                </div>

                <div className="p-2 rounded bg-amber-500/5 border border-amber-500/20 space-y-0.5">
                  <div className="flex items-center justify-center gap-1 text-amber-600 dark:text-amber-400">
                    <Sliders className="h-3 w-3" />
                    <span className="text-[10px] font-semibold">{t.modified}</span>
                  </div>
                  <p className="text-base font-bold font-mono text-foreground">
                    {impact.modifiedByHumanCount}
                  </p>
                </div>

                <div className="p-2 rounded bg-rose-500/5 border border-rose-500/20 space-y-0.5">
                  <div className="flex items-center justify-center gap-1 text-rose-600 dark:text-rose-400">
                    <XCircle className="h-3 w-3" />
                    <span className="text-[10px] font-semibold">{t.rejected}</span>
                  </div>
                  <p className="text-base font-bold font-mono text-foreground">
                    {impact.rejectedByHumanCount}
                  </p>
                </div>
              </div>

              <div className="pt-2 border-t border-border/60 flex items-center justify-between text-xs text-muted-foreground">
                <span>{t.accuracyWhenApproved}:</span>
                <span className="font-mono font-bold text-foreground">
                  {impact.directApprovalAccuracy !== null
                    ? `${impact.directApprovalAccuracy.toFixed(1)}%`
                    : '—'}
                </span>
              </div>

              {impact.humanOverrideImprovement && (
                <div className="bg-muted/40 p-2 rounded text-[11px] text-muted-foreground border border-border/40">
                  <span className="font-semibold text-foreground mr-1">{t.impactAssessment}:</span>
                  <span>{impact.humanOverrideImprovement}</span>
                </div>
              )}
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
