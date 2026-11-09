import React from 'react'
import { DataSource } from '@/entities/integration'
import { Card, CardContent } from '@/shared/ui/card'
import { Database, CheckCircle2, AlertTriangle, XCircle, RefreshCw, Activity } from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'

interface DataSourcesSummaryCardsProps {
  sources: DataSource[]
  lang: SupportedLanguage
}

export const DataSourcesSummaryCards: React.FC<DataSourcesSummaryCardsProps> = ({ sources, lang }) => {
  const t = translations[lang].dataIntegration

  const total = sources.length
  const active = sources.filter((s) => s.status === 'ACTIVE').length
  const degraded = sources.filter((s) => s.status === 'DEGRADED').length
  const notConfigured = sources.filter((s) => s.status === 'NOT_CONFIGURED').length
  const totalIngested = sources.reduce((acc, s) => acc + s.totalIngestedCount, 0)
  const totalRejected = sources.reduce((acc, s) => acc + s.totalRejectedCount, 0)
  const totalOps = totalIngested + totalRejected
  const overallReliability = totalOps > 0 ? ((totalIngested / totalOps) * 100).toFixed(1) : '100.0'

  return (
    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
      <Card className="border border-border/60 shadow-sm bg-card/60 backdrop-blur-sm">
        <CardContent className="p-4 flex flex-col justify-between h-full">
          <div className="flex items-center justify-between text-muted-foreground mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">{t.totalSources}</span>
            <Database className="h-4 w-4 text-primary" />
          </div>
          <div className="text-2xl font-bold text-foreground">{total}</div>
          <p className="text-[11px] text-muted-foreground mt-1">{t.configuredProviders}</p>
        </CardContent>
      </Card>

      <Card className="border border-border/60 shadow-sm bg-card/60 backdrop-blur-sm">
        <CardContent className="p-4 flex flex-col justify-between h-full">
          <div className="flex items-center justify-between text-muted-foreground mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">{t.activeSources}</span>
            <CheckCircle2 className="h-4 w-4 text-emerald-500" />
          </div>
          <div className="text-2xl font-bold text-emerald-600 dark:text-emerald-400">{active}</div>
          <p className="text-[11px] text-muted-foreground mt-1">{t.operational}</p>
        </CardContent>
      </Card>

      <Card className="border border-border/60 shadow-sm bg-card/60 backdrop-blur-sm">
        <CardContent className="p-4 flex flex-col justify-between h-full">
          <div className="flex items-center justify-between text-muted-foreground mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">{t.degradedSources}</span>
            <AlertTriangle className="h-4 w-4 text-amber-500" />
          </div>
          <div className="text-2xl font-bold text-amber-600 dark:text-amber-400">{degraded}</div>
          <p className="text-[11px] text-muted-foreground mt-1">{t.partialFailures}</p>
        </CardContent>
      </Card>

      <Card className="border border-border/60 shadow-sm bg-card/60 backdrop-blur-sm">
        <CardContent className="p-4 flex flex-col justify-between h-full">
          <div className="flex items-center justify-between text-muted-foreground mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">{t.notConfigured}</span>
            <XCircle className="h-4 w-4 text-rose-500" />
          </div>
          <div className="text-2xl font-bold text-muted-foreground">{notConfigured}</div>
          <p className="text-[11px] text-muted-foreground mt-1">{t.awaitingSetup}</p>
        </CardContent>
      </Card>

      <Card className="border border-border/60 shadow-sm bg-card/60 backdrop-blur-sm">
        <CardContent className="p-4 flex flex-col justify-between h-full">
          <div className="flex items-center justify-between text-muted-foreground mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">{t.ingestedTelemetry}</span>
            <Activity className="h-4 w-4 text-blue-500" />
          </div>
          <div className="text-2xl font-bold text-foreground">{totalIngested.toLocaleString()}</div>
          <p className="text-[11px] text-muted-foreground mt-1">{t.recordsAccepted}</p>
        </CardContent>
      </Card>

      <Card className="border border-border/60 shadow-sm bg-card/60 backdrop-blur-sm">
        <CardContent className="p-4 flex flex-col justify-between h-full">
          <div className="flex items-center justify-between text-muted-foreground mb-2">
            <span className="text-xs font-medium uppercase tracking-wider">{t.sourceReliability}</span>
            <RefreshCw className="h-4 w-4 text-indigo-500" />
          </div>
          <div className="text-2xl font-bold text-foreground">{overallReliability}%</div>
          <p className="text-[11px] text-muted-foreground mt-1">{t.qualityPassRate}</p>
        </CardContent>
      </Card>
    </div>
  )
}
