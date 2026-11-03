import React from 'react'
import { Card, CardContent } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import {
  Layers,
  Cpu,
  ShieldCheck,
  AlertTriangle,
  Compass,
  Database,
  CheckCircle2,
} from 'lucide-react'
import { AdaptiveOverviewResponse } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface AdaptiveHealthStripProps {
  overview?: AdaptiveOverviewResponse | null
  isLoading?: boolean
  lang: SupportedLanguage
}

export const AdaptiveHealthStrip: React.FC<AdaptiveHealthStripProps> = ({
  overview,
  isLoading,
  lang,
}) => {
  const t = {
    uz: {
      title: 'Kontekstga Moslashuvchi Intellekt Dvigateli',
      productPrinciple:
        'YANGIARIQ AQUA AI avtonom o‘zini-o‘zi o‘zgartirmaydi — tizim tekshirilgan tarixiy samaradorlik asosida joriy kontekstga eng mos keluvchi boshqariladigan usulni tanlaydi.',
      activeVersions: 'Faol Boshqariladigan Versiyalar',
      contextAwareSelections: 'Kontekst Asosida Tanlovlar',
      baselineFallbacks: 'Xavfsiz Baza Zaxiralari',
      insufficientContext: 'Yetarsiz Maʼlumot Zaxirasi',
      contextDriftAlert:
        'Muayyan ekin yoki sug‘orish usulida kontekstual xatolik pasayishi aniqlandi. Xavfsiz baza modeli himoyani taʼminlaydi.',
      healthyState: 'Kontekstual tanlovlar barqaror va tasdiqlangan meʼyorda ishlamoqda.',
    },
    ru: {
      title: 'Контекстно-Адаптивный Движок Интеллекта',
      productPrinciple:
        'YANGIARIQ AQUA AI не модифицирует себя автономно — система выбирает из утвержденных методов на основе проверенной исторической точности для текущего контекста.',
      activeVersions: 'Активные Утвержденные Версии',
      contextAwareSelections: 'Контекстных Выборов',
      baselineFallbacks: 'Безопасных Базовых Откатов',
      insufficientContext: 'Откатов при Нехватке Данных',
      contextDriftAlert:
        'Обнаружена контекстная деградация точности на отдельных культурах/почвах. Безопасная базовая модель обеспечивает защиту.',
      healthyState: 'Контекстный выбор моделей функционирует стабильно и в рамках нормы.',
    },
    en: {
      title: 'Context-Aware Adaptive Intelligence Engine',
      productPrinciple:
        'YANGIARIQ AQUA AI does not autonomously self-modify — the system selects among governed prediction methods using verified historical performance for the current context.',
      activeVersions: 'Governed Active Versions',
      contextAwareSelections: 'Context-Aware Selections',
      baselineFallbacks: 'Safe Baseline Fallbacks',
      insufficientContext: 'Insufficient Context Fallbacks',
      contextDriftAlert:
        'Contextual error degradation detected for specific crop or soil slices. Governed baseline preserves operational safety.',
      healthyState: 'Contextual selection matrix operating within verified empirical thresholds.',
    },
  }[lang]

  return (
    <div className="space-y-4">
      {/* Product Principle & System Guarantee Banner */}
      <Card className="border-emerald-500/30 bg-emerald-500/5 dark:bg-emerald-950/20 shadow-sm">
        <CardContent className="p-4 flex flex-col md:flex-row items-start md:items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-emerald-500/20 text-emerald-600 dark:text-emerald-400 shrink-0">
              <Compass className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-semibold text-sm text-foreground">{t.title}</span>
                <Badge variant="outline" className="text-[11px] bg-emerald-500/10 text-emerald-700 dark:text-emerald-300 border-emerald-500/30">
                  <ShieldCheck className="w-3 h-3 mr-1" />
                  Deterministic & Governed
                </Badge>
              </div>
              <p className="text-xs text-muted-foreground mt-0.5 max-w-3xl leading-relaxed">
                {t.productPrinciple}
              </p>
            </div>
          </div>
          {overview?.contextualDriftWarningActive ? (
            <Badge variant="destructive" className="shrink-0 flex items-center gap-1.5 px-3 py-1 text-xs">
              <AlertTriangle className="w-3.5 h-3.5" />
              Contextual Drift Active
            </Badge>
          ) : (
            <Badge variant="secondary" className="shrink-0 flex items-center gap-1.5 px-3 py-1 text-xs bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 border border-emerald-500/30">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
              Matrix Verified
            </Badge>
          )}
        </CardContent>
      </Card>

      {/* KPI Metrics Strip */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <Card className="border-border/60 bg-card/60 backdrop-blur-sm">
          <CardContent className="p-4 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-blue-500/10 text-blue-600 dark:text-blue-400 shrink-0">
              <Layers className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground font-medium">{t.activeVersions}</p>
              <div className="flex items-baseline gap-2 mt-0.5">
                <span className="text-xl font-bold tracking-tight text-foreground">
                  {isLoading ? '...' : overview?.activeVersionsCount ?? 4}
                </span>
                <span className="text-[11px] text-muted-foreground">
                  / {overview?.totalAdaptiveMethodsCount ?? 4}
                </span>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-border/60 bg-card/60 backdrop-blur-sm">
          <CardContent className="p-4 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 shrink-0">
              <Cpu className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground font-medium">{t.contextAwareSelections}</p>
              <div className="flex items-baseline gap-2 mt-0.5">
                <span className="text-xl font-bold tracking-tight text-emerald-600 dark:text-emerald-400">
                  {isLoading ? '...' : overview?.contextAwareSelectionsCount ?? 0}
                </span>
                <Badge variant="outline" className="text-[10px] px-1 py-0 border-emerald-500/30 text-emerald-600">
                  Tailored
                </Badge>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-border/60 bg-card/60 backdrop-blur-sm">
          <CardContent className="p-4 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-amber-500/10 text-amber-600 dark:text-amber-400 shrink-0">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground font-medium">{t.baselineFallbacks}</p>
              <div className="flex items-baseline gap-2 mt-0.5">
                <span className="text-xl font-bold tracking-tight text-foreground">
                  {isLoading ? '...' : overview?.baselineFallbackCount ?? 0}
                </span>
                <span className="text-[11px] text-muted-foreground">safe active</span>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-border/60 bg-card/60 backdrop-blur-sm">
          <CardContent className="p-4 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-slate-500/10 text-slate-600 dark:text-slate-400 shrink-0">
              <Database className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground font-medium">{t.insufficientContext}</p>
              <div className="flex items-baseline gap-2 mt-0.5">
                <span className="text-xl font-bold tracking-tight text-foreground">
                  {isLoading ? '...' : overview?.insufficientContextCount ?? 0}
                </span>
                <span className="text-[11px] text-muted-foreground">n &lt; 10</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Contextual Drift Alert Banner if active */}
      {overview?.contextualDriftWarningActive && (
        <Card className="border-amber-500/40 bg-amber-500/10 dark:bg-amber-950/20">
          <CardContent className="p-3.5 flex items-start gap-3">
            <AlertTriangle className="w-5 h-5 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
            <div className="text-xs text-foreground">
              <p className="font-semibold text-amber-800 dark:text-amber-300">{t.contextDriftAlert}</p>
              <p className="text-muted-foreground mt-0.5">
                {lang === 'uz' && 'Kontekstual degradatsiya tufayli nomzod modellar rad etildi va faqat barqaror bazaviy usul tanlanmoqda.'}
                {lang === 'ru' && 'Кандидаты с контекстной деградацией отклоняются, активирован безопасный базовый профиль.'}
                {lang === 'en' && 'Candidates with contextual degradation are rejected in favor of verified active baseline.'}
              </p>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
