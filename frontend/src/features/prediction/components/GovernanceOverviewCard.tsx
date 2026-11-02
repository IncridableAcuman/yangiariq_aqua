import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  ShieldAlert,
  ShieldCheck,
  RotateCcw,
  Sliders,
  GitBranch,
  Layers,
  Sparkles,
  TrendingDown,
} from 'lucide-react'
import { GovernanceOverview } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface GovernanceOverviewCardProps {
  overview?: GovernanceOverview | null
  isLoading?: boolean
  onOpenRollback?: () => void
  onTriggerAnalysis?: () => void
  isAnalyzing?: boolean
  lang: SupportedLanguage
}

export const GovernanceOverviewCard: React.FC<GovernanceOverviewCardProps> = ({
  overview,
  isLoading,
  onOpenRollback,
  onTriggerAnalysis,
  isAnalyzing,
  lang,
}) => {
  const t = {
    uz: {
      title: 'Prediktiv Boshqaruv va Adaptiv Kalibratsiya',
      subtitle: 'Sunʼiy intellekt modellarining versiyalari, avtonom o‘zgarmaslik (Zero-Autonomous Self-Modification) va inson maʼqullash tizimi',
      activeVersions: 'Faol Modellar',
      candidates: 'Nomzod / Sinov Versiyalari',
      openProposals: 'Ochiq Kalibratsiya Takliflari',
      modelHealthSummary: 'Modellar Salomatligi',
      healthy: 'Sog‘lom',
      watch: 'Nazoratda',
      degraded: 'Degradatsiya',
      driftAlert: 'Ishlash Samarasining Pasayishi (Drift) Aniqlangan',
      rollbackBtn: '1-Bosishda Qaytarish (Rollback)',
      analyzeBtn: 'Adaptiv Kalibratsiya Tahlili',
      analyzing: 'Tahlil qilinmoqda...',
      safetyGuarantee: 'Qatʼiy Xavfsizlik Kafolati: Gidravlik xavfsizlik va favqulodda cheklovlar hech qachon avtomatik o‘zgartirilmaydi. Faqat yumshoq agronomik parametrlar inson tasdig‘i bilan kalibratsiyalanadi.',
    },
    ru: {
      title: 'Управление Моделями и Контролируемая Калибровка',
      subtitle: 'Версионирование моделей, запрет автономного самоизменения и обязательное утверждение человеком',
      activeVersions: 'Активные Модели',
      candidates: 'Кандидаты / Тестовые Версии',
      openProposals: 'Открытые Предложения',
      modelHealthSummary: 'Здоровье Моделей',
      healthy: 'В норме',
      watch: 'Внимание',
      degraded: 'Деградация',
      driftAlert: 'Обнаружено Снижение Точности (Дрейф)',
      rollbackBtn: 'Откат Версии (Rollback)',
      analyzeBtn: 'Анализ для Калибровки',
      analyzing: 'Анализ...',
      safetyGuarantee: 'Гарантия Безопасности: Гидравлические блокировки и аварийные пороги НИКОГДА не меняются автоматически. Только агрономические параметры подлежат калибровке после одобрения администратором.',
    },
    en: {
      title: 'Predictive Model Governance & Controlled Calibration',
      subtitle: 'Model versioning, zero-autonomous self-modification, and mandatory human-in-the-loop oversight',
      activeVersions: 'Active Production Models',
      candidates: 'Candidate / Shadow Versions',
      openProposals: 'Open Calibration Proposals',
      modelHealthSummary: 'Model Fleet Health',
      healthy: 'Healthy',
      watch: 'Watch',
      degraded: 'Degraded',
      driftAlert: 'Performance Drift Detected',
      rollbackBtn: '1-Click Rollback',
      analyzeBtn: 'Adaptive Calibration Analysis',
      analyzing: 'Analyzing...',
      safetyGuarantee: 'Strict Safety Guarantee: Hydraulic safety interlocks and emergency cutoffs can NEVER be altered automatically. Only soft agronomic parameters are calibrated subject to human administrator approval.',
    },
  }[lang]

  if (isLoading) {
    return (
      <Card className="animate-pulse">
        <CardHeader>
          <div className="h-6 w-1/3 bg-muted rounded" />
          <div className="h-4 w-1/2 bg-muted rounded mt-2" />
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-24 bg-muted rounded" />
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="border-emerald-500/30 bg-card/60 backdrop-blur-sm shadow-md">
      <CardHeader className="pb-3 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <ShieldCheck className="w-6 h-6 text-emerald-500" />
            <CardTitle className="text-xl font-bold tracking-tight">{t.title}</CardTitle>
            <Badge variant="outline" className="text-xs bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/30 font-medium">
              Human-in-the-loop Active
            </Badge>
          </div>
          <CardDescription className="text-xs mt-1 text-muted-foreground">
            {t.subtitle}
          </CardDescription>
        </div>
        <div className="flex items-center gap-2 flex-wrap">
          {onTriggerAnalysis && (
            <Button
              variant="outline"
              size="sm"
              onClick={onTriggerAnalysis}
              disabled={isAnalyzing}
              className="border-primary/40 text-xs flex items-center gap-1.5"
            >
              <Sparkles className="w-3.5 h-3.5 text-primary" />
              {isAnalyzing ? t.analyzing : t.analyzeBtn}
            </Button>
          )}
          {onOpenRollback && (
            <Button
              variant="destructive"
              size="sm"
              onClick={onOpenRollback}
              className="text-xs flex items-center gap-1.5"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              {t.rollbackBtn}
            </Button>
          )}
        </div>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Metric Cards Grid */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {/* Active Versions */}
          <div className="p-3.5 rounded-lg border bg-muted/20 flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-xs text-muted-foreground font-medium">{t.activeVersions}</span>
              <Layers className="w-4 h-4 text-emerald-500" />
            </div>
            <div className="mt-2 flex items-baseline gap-2">
              <span className="text-2xl font-bold">{overview?.activeVersionsCount ?? 0}</span>
              <span className="text-xs text-emerald-600 dark:text-emerald-400 font-medium">Production Locked</span>
            </div>
          </div>

          {/* Candidates */}
          <div className="p-3.5 rounded-lg border bg-muted/20 flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-xs text-muted-foreground font-medium">{t.candidates}</span>
              <GitBranch className="w-4 h-4 text-blue-500" />
            </div>
            <div className="mt-2 flex items-baseline gap-2">
              <span className="text-2xl font-bold text-blue-600 dark:text-blue-400">
                {overview?.candidateVersionsCount ?? 0}
              </span>
              <span className="text-xs text-muted-foreground">Shadow / Canary</span>
            </div>
          </div>

          {/* Open Proposals */}
          <div className="p-3.5 rounded-lg border bg-muted/20 flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-xs text-muted-foreground font-medium">{t.openProposals}</span>
              <Sliders className="w-4 h-4 text-amber-500" />
            </div>
            <div className="mt-2 flex items-baseline gap-2">
              <span className="text-2xl font-bold text-amber-600 dark:text-amber-400">
                {overview?.openProposalsCount ?? 0}
              </span>
              {(overview?.openProposalsCount ?? 0) > 0 ? (
                <Badge variant="outline" className="text-[10px] bg-amber-500/10 text-amber-600 border-amber-500/30">
                  Review Required
                </Badge>
              ) : (
                <span className="text-xs text-muted-foreground">No backlogs</span>
              )}
            </div>
          </div>

          {/* Model Health Summary */}
          <div className="p-3.5 rounded-lg border bg-muted/20 flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <span className="text-xs text-muted-foreground font-medium">{t.modelHealthSummary}</span>
              <ShieldAlert className="w-4 h-4 text-primary" />
            </div>
            <div className="mt-2 flex items-center gap-1.5 flex-wrap">
              <Badge variant="outline" className="text-xs bg-emerald-500/10 text-emerald-600 border-emerald-500/30">
                {overview?.healthyModelsCount ?? 0} {t.healthy}
              </Badge>
              <Badge variant="outline" className="text-xs bg-amber-500/10 text-amber-600 border-amber-500/30">
                {overview?.watchModelsCount ?? 0} {t.watch}
              </Badge>
              <Badge variant="outline" className="text-xs bg-red-500/10 text-red-600 border-red-500/30">
                {overview?.degradedModelsCount ?? 0} {t.degraded}
              </Badge>
            </div>
          </div>
        </div>

        {/* Performance Drift Alert Banner */}
        {overview?.performanceDriftDetected && (
          <div className="p-3.5 rounded-lg border border-amber-500/40 bg-amber-500/10 text-amber-900 dark:text-amber-200 flex items-start gap-3">
            <TrendingDown className="w-5 h-5 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
            <div className="space-y-1">
              <div className="text-sm font-semibold flex items-center gap-2">
                {t.driftAlert}
                <Badge variant="destructive" className="text-[10px] uppercase">Drift Alert</Badge>
              </div>
              <p className="text-xs text-muted-foreground dark:text-amber-300/80">
                {overview.driftSummary || 'Evaluation data demonstrates statistical performance degradation compared to previous observation window.'}
              </p>
            </div>
          </div>
        )}

        {/* Safety Guarantee Footer */}
        <div className="text-[11px] text-muted-foreground bg-muted/30 p-2.5 rounded border border-muted/50 flex items-center gap-2">
          <ShieldAlert className="w-4 h-4 text-emerald-500 shrink-0" />
          <span>{t.safetyGuarantee}</span>
        </div>
      </CardContent>
    </Card>
  )
}
