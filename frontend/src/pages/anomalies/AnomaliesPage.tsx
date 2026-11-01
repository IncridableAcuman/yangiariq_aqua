import React, { useState } from 'react'
import {
  AlertOctagon,
  AlertTriangle,
  BellRing,
  CheckCircle2,
  ListFilter,
  ShieldAlert,
  Sliders,
} from 'lucide-react'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { Card, CardContent } from '@/shared/ui/card'
import { Tabs, TabsList, TabsTrigger } from '@/shared/ui/tabs'
import { AnomalyFeed } from '@/features/anomaly/components/AnomalyFeed'
import { AnomalyRulesTable } from '@/features/anomaly/components/AnomalyRulesTable'
import { useAnomalySummary } from '@/features/anomaly/queries/useAnomalyQueries'

interface AnomaliesPageProps {
  lang: SupportedLanguage
}

export const AnomaliesPage: React.FC<AnomaliesPageProps> = ({ lang }) => {
  const t = translations[lang].anomalies
  const [activeTab, setActiveTab] = useState<'feed' | 'rules'>('feed')

  const { data: summary } = useAnomalySummary()

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-border/80 pb-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2.5">
            <ShieldAlert className="w-6 h-6 text-rose-500" />
            <span>{t.title}</span>
          </h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">
            {t.subtitle}
          </p>
        </div>

        {/* View Toggle Tabs */}
        <Tabs value={activeTab} onValueChange={(val) => setActiveTab(val as 'feed' | 'rules')}>
          <TabsList className="grid grid-cols-2 w-64 h-9">
            <TabsTrigger value="feed" className="text-xs gap-1.5">
              <ListFilter className="w-3.5 h-3.5" />
              <span>{t.tabs.feed}</span>
            </TabsTrigger>
            <TabsTrigger value="rules" className="text-xs gap-1.5">
              <Sliders className="w-3.5 h-3.5" />
              <span>{t.tabs.rules}</span>
            </TabsTrigger>
          </TabsList>
        </Tabs>
      </div>

      {/* KPI Overview Summary Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {/* Total Active */}
        <Card className="border-border bg-card">
          <CardContent className="p-3.5 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-primary/10 text-primary">
              <BellRing className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[11px] text-muted-foreground font-medium">{t.stats.totalActive}</p>
              <p className="text-lg font-bold text-foreground mt-0.5">
                {summary?.totalActive ?? 0}
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Critical Alerts */}
        <Card
          className={`border bg-card transition-all ${
            (summary?.criticalCount ?? 0) > 0
              ? 'border-rose-400 dark:border-rose-900 bg-rose-500/[0.04]'
              : 'border-border'
          }`}
        >
          <CardContent className="p-3.5 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-rose-500/10 text-rose-600 dark:text-rose-400">
              <AlertOctagon className={`w-4 h-4 ${(summary?.criticalCount ?? 0) > 0 ? 'animate-pulse' : ''}`} />
            </div>
            <div>
              <p className="text-[11px] text-muted-foreground font-medium">{t.stats.critical}</p>
              <p className="text-lg font-bold text-rose-600 dark:text-rose-400 mt-0.5">
                {summary?.criticalCount ?? 0}
              </p>
            </div>
          </CardContent>
        </Card>

        {/* High Severity */}
        <Card className="border-border bg-card">
          <CardContent className="p-3.5 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-amber-500/10 text-amber-600 dark:text-amber-400">
              <AlertTriangle className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[11px] text-muted-foreground font-medium">{t.stats.high}</p>
              <p className="text-lg font-bold text-amber-600 dark:text-amber-400 mt-0.5">
                {summary?.highCount ?? 0}
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Resolved */}
        <Card className="border-border bg-card">
          <CardContent className="p-3.5 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
              <CheckCircle2 className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[11px] text-muted-foreground font-medium">{t.stats.resolved}</p>
              <p className="text-lg font-bold text-emerald-600 dark:text-emerald-400 mt-0.5">
                {summary?.resolvedCount ?? 0}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Main Tab Content */}
      {activeTab === 'feed' ? (
        <AnomalyFeed lang={lang} />
      ) : (
        <AnomalyRulesTable lang={lang} />
      )}
    </div>
  )
}
