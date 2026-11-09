import React from 'react'
import { DataQualitySummary } from '@/entities/integration'
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card'
import { CheckCircle2, AlertTriangle, Copy, Clock, Ban, ShieldCheck, HelpCircle } from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'

interface DataQualityMetricsViewProps {
  summary: DataQualitySummary | undefined
  isLoading?: boolean
  lang: SupportedLanguage
}

export const DataQualityMetricsView: React.FC<DataQualityMetricsViewProps> = ({
  summary,
  isLoading,
  lang,
}) => {
  const t = translations[lang].dataIntegration

  if (isLoading || !summary) {
    return (
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[...Array(4)].map((_, i) => (
          <Card key={i} className="animate-pulse bg-muted/40 h-28" />
        ))}
      </div>
    )
  }

  const qualityScoreColor =
    summary.overallQualityScore >= 95
      ? 'text-emerald-500'
      : summary.overallQualityScore >= 80
      ? 'text-amber-500'
      : 'text-rose-500'

  return (
    <div className="space-y-4">
      {/* Top Quality Score Banner */}
      <Card className="border border-border/60 bg-card/60 backdrop-blur-sm">
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-semibold flex items-center justify-between">
            <span className="flex items-center gap-2">
              <ShieldCheck className="h-4 w-4 text-primary" />
              {t.overallDataQualityHealth}
            </span>
            <span className={`text-xl font-bold ${qualityScoreColor}`}>
              {summary.overallQualityScore}%
            </span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="w-full bg-muted rounded-full h-2 overflow-hidden mb-3">
            <div
              className={`h-full rounded-full transition-all duration-500 ${
                summary.overallQualityScore >= 95
                  ? 'bg-emerald-500'
                  : summary.overallQualityScore >= 80
                  ? 'bg-amber-500'
                  : 'bg-rose-500'
              }`}
              style={{ width: `${Math.max(5, summary.overallQualityScore)}%` }}
            />
          </div>
          <p className="text-xs text-muted-foreground">{t.qualityScoreExplanation}</p>
        </CardContent>
      </Card>

      {/* Grid of Quality Dimensions */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
        <Card className="border border-border/50 bg-card/40">
          <CardContent className="p-3">
            <div className="flex items-center justify-between text-muted-foreground mb-1">
              <span className="text-[11px] font-medium uppercase">{t.validReadings}</span>
              <CheckCircle2 className="h-3.5 w-3.5 text-emerald-500" />
            </div>
            <div className="text-xl font-bold text-emerald-600 dark:text-emerald-400">
              {summary.validCount.toLocaleString()}
            </div>
            <span className="text-[10px] text-muted-foreground">{t.passedAllChecks}</span>
          </CardContent>
        </Card>

        <Card className="border border-border/50 bg-card/40">
          <CardContent className="p-3">
            <div className="flex items-center justify-between text-muted-foreground mb-1">
              <span className="text-[11px] font-medium uppercase">{t.duplicateRecords}</span>
              <Copy className="h-3.5 w-3.5 text-amber-500" />
            </div>
            <div className="text-xl font-bold text-amber-600 dark:text-amber-400">
              {summary.duplicateCount.toLocaleString()}
            </div>
            <span className="text-[10px] text-muted-foreground">{t.idempotentDeduplicated}</span>
          </CardContent>
        </Card>

        <Card className="border border-border/50 bg-card/40">
          <CardContent className="p-3">
            <div className="flex items-center justify-between text-muted-foreground mb-1">
              <span className="text-[11px] font-medium uppercase">{t.outOfRange}</span>
              <AlertTriangle className="h-3.5 w-3.5 text-rose-500" />
            </div>
            <div className="text-xl font-bold text-rose-600 dark:text-rose-400">
              {summary.outOfRangeCount.toLocaleString()}
            </div>
            <span className="text-[10px] text-muted-foreground">{t.boundsViolations}</span>
          </CardContent>
        </Card>

        <Card className="border border-border/50 bg-card/40">
          <CardContent className="p-3">
            <div className="flex items-center justify-between text-muted-foreground mb-1">
              <span className="text-[11px] font-medium uppercase">{t.staleData}</span>
              <Clock className="h-3.5 w-3.5 text-blue-500" />
            </div>
            <div className="text-xl font-bold text-foreground">
              {summary.staleCount.toLocaleString()}
            </div>
            <span className="text-[10px] text-muted-foreground">{t.delayedLagging}</span>
          </CardContent>
        </Card>

        <Card className="border border-border/50 bg-card/40">
          <CardContent className="p-3">
            <div className="flex items-center justify-between text-muted-foreground mb-1">
              <span className="text-[11px] font-medium uppercase">{t.invalidIncomplete}</span>
              <Ban className="h-3.5 w-3.5 text-rose-500" />
            </div>
            <div className="text-xl font-bold text-rose-600 dark:text-rose-400">
              {summary.invalidCount.toLocaleString()}
            </div>
            <span className="text-[10px] text-muted-foreground">{t.rejectedPayloads}</span>
          </CardContent>
        </Card>

        <Card className="border border-border/50 bg-card/40">
          <CardContent className="p-3">
            <div className="flex items-center justify-between text-muted-foreground mb-1">
              <span className="text-[11px] font-medium uppercase">{t.unverifiedEntry}</span>
              <HelpCircle className="h-3.5 w-3.5 text-purple-500" />
            </div>
            <div className="text-xl font-bold text-foreground">
              {summary.unverifiedCount.toLocaleString()}
            </div>
            <span className="text-[10px] text-muted-foreground">{t.selfReported}</span>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
