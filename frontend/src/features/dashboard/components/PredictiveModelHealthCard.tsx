import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import {
  Activity,
  TrendingDown,
  ArrowRight,
} from 'lucide-react'
import { PredictiveModelHealthSummary } from '@/entities/dashboard'
import { SupportedLanguage } from '@/shared/i18n'

interface PredictiveModelHealthCardProps {
  predictiveHealth?: PredictiveModelHealthSummary | null
  lang: SupportedLanguage
  onOpenGovernance: () => void
}

export const PredictiveModelHealthCard: React.FC<PredictiveModelHealthCardProps> = ({
  predictiveHealth,
  lang,
  onOpenGovernance,
}) => {
  const t = {
    uz: {
      title: 'Prediktiv Salomatlik',
      subtitle: 'Prognozlash modellari, versiyalari va drift holati',
      activeVersions: 'Faol versiyalar',
      healthy: 'Barqaror',
      watch: 'Kuzatuvda',
      candidates: 'Nomzod',
      degraded: 'Og‘ish',
      driftDetected: 'Ogohlantirish: Prognoz xatoligi oshgan',
      openGovernance: 'Boshqaruv (Governance)',
    },
    ru: {
      title: 'Здоровье Моделей',
      subtitle: 'Состояние версий, точности и дрейфа прогнозов',
      activeVersions: 'Активные версии',
      healthy: 'В норме',
      watch: 'Наблюдение',
      candidates: 'Кандидаты',
      degraded: 'Деградация',
      driftDetected: 'Внимание: Зафиксирован дрейф точности',
      openGovernance: 'Управление Моделями',
    },
    en: {
      title: 'Predictive Health',
      subtitle: 'Model versioning, calibration status, and drift',
      activeVersions: 'Active versions',
      healthy: 'Healthy',
      watch: 'Watch',
      candidates: 'Candidate',
      degraded: 'Degraded',
      driftDetected: 'Warning: Performance drift detected',
      openGovernance: 'Open Governance',
    },
  }[lang]

  const health = predictiveHealth || {
    activeVersions: 4,
    healthy: 3,
    watch: 1,
    candidates: 1,
    degraded: 0,
    driftDetected: false,
  }

  return (
    <Card className="border border-border/80 shadow-xs hover:border-primary/30 transition-colors">
      <CardHeader className="pb-2.5 pt-4 px-4 border-b border-border/60">
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <div className="h-7 w-7 rounded-md bg-primary/10 text-primary flex items-center justify-center shrink-0">
              <Activity className="w-4 h-4" />
            </div>
            <div>
              <CardTitle className="text-sm font-bold text-foreground flex items-center gap-1.5">
                <span>{t.title}</span>
                {health.driftDetected && (
                  <Badge variant="outline" className="bg-amber-500/10 text-amber-600 border-amber-500/30 text-[10px] py-0 px-1.5">
                    Drift
                  </Badge>
                )}
              </CardTitle>
              <CardDescription className="text-[11px]">
                {t.subtitle}
              </CardDescription>
            </div>
          </div>

          <Button
            variant="ghost"
            size="sm"
            onClick={onOpenGovernance}
            className="text-xs h-7 gap-1 px-2 text-primary hover:text-primary hover:bg-primary/10"
          >
            <span>{t.openGovernance}</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Button>
        </div>
      </CardHeader>

      <CardContent className="p-3.5 space-y-2.5">
        {/* KPI Mini-Bar */}
        <div className="grid grid-cols-5 gap-1.5 text-center">
          {/* Active Versions */}
          <div className="p-1.5 rounded-md bg-muted/40 border border-border/60">
            <div className="text-[10px] text-muted-foreground font-medium truncate">
              {t.activeVersions}
            </div>
            <div className="font-mono text-sm font-bold text-foreground">
              {health.activeVersions}
            </div>
          </div>

          {/* Healthy */}
          <div className="p-1.5 rounded-md bg-emerald-500/10 border border-emerald-500/20 text-emerald-700 dark:text-emerald-300">
            <div className="text-[10px] font-medium truncate">
              {t.healthy}
            </div>
            <div className="font-mono text-sm font-bold">
              {health.healthy}
            </div>
          </div>

          {/* Watch */}
          <div className="p-1.5 rounded-md bg-amber-500/10 border border-amber-500/20 text-amber-700 dark:text-amber-300">
            <div className="text-[10px] font-medium truncate">
              {t.watch}
            </div>
            <div className="font-mono text-sm font-bold">
              {health.watch}
            </div>
          </div>

          {/* Candidate */}
          <div className="p-1.5 rounded-md bg-purple-500/10 border border-purple-500/20 text-purple-700 dark:text-purple-300">
            <div className="text-[10px] font-medium truncate">
              {t.candidates}
            </div>
            <div className="font-mono text-sm font-bold">
              {health.candidates}
            </div>
          </div>

          {/* Degraded */}
          <div className={`p-1.5 rounded-md border text-center ${
            health.degraded > 0
              ? 'bg-rose-500/10 border-rose-500/30 text-rose-700 dark:text-rose-300 animate-pulse'
              : 'bg-muted/30 border-border/60 text-muted-foreground'
          }`}>
            <div className="text-[10px] font-medium truncate">
              {t.degraded}
            </div>
            <div className="font-mono text-sm font-bold">
              {health.degraded}
            </div>
          </div>
        </div>

        {health.driftDetected && (
          <div className="flex items-center gap-1.5 text-[11px] text-amber-700 dark:text-amber-300 bg-amber-500/10 p-2 rounded border border-amber-500/20">
            <TrendingDown className="w-3.5 h-3.5 shrink-0" />
            <span>{t.driftDetected}</span>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
