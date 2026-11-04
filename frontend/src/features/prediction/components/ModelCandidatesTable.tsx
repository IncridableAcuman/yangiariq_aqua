import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  Brain,
  ShieldCheck,
  ShieldAlert,
  Eye,
  Layers,
} from 'lucide-react'
import { ModelCandidateDto, MethodVersionStatus } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface ModelCandidatesTableProps {
  candidates: ModelCandidateDto[]
  isLoading?: boolean
  onSelectCandidate: (candidate: ModelCandidateDto) => void
  onPromoteCandidate?: (candidateId: string, status: MethodVersionStatus) => void
  isPromoting?: boolean
  lang: SupportedLanguage
  userRole?: string
}

export const ModelCandidatesTable: React.FC<ModelCandidatesTableProps> = ({
  candidates,
  isLoading,
  onSelectCandidate,
  onPromoteCandidate,
  isPromoting,
  lang,
  userRole,
}) => {
  const t = {
    uz: {
      title: 'ML Model Nomzodlari va Gipotezalar Reyestri',
      subtitle: 'Tarixiy maʼlumotlar asosida o‘qitilgan, vaqt bo‘yicha tekshirilgan va boshqaruvdagi barcha model versiyalari',
      version: 'Versiya Tegi',
      type: 'Model Turi',
      samples: 'Namuna Hajmi',
      testMae: 'Test MAE',
      baselineMae: 'Baseline MAE',
      gain: 'Yaxshilanish',
      guard: 'Regressiya Himoyasi',
      status: 'Boshqaruv Holati',
      actions: 'Amallar',
      inspect: 'Dalillarni Ko‘rish',
      approve: 'Tasdiqlash',
      shadow: 'Soya (Shadow)',
      canary: 'Kanareyka (Canary)',
      activate: 'Faollashtirish',
      empty: 'Hozircha ML model nomzodlari mavjud emas. Boshqariladigan o‘qitishni boshlang.',
      passed: 'O‘tdi (Passed)',
      regression: 'Regressiya Aniqlangan',
    },
    ru: {
      sectionTitle: 'Реестр Кандидатов ML Моделей',
      subtitle: 'Все версии моделей, обученные на исторических данных с временным разделением выборок',
      version: 'Версия Модели',
      type: 'Тип Модели',
      samples: 'Выборка',
      testMae: 'Тест MAE',
      baselineMae: 'Базис MAE',
      gain: 'Прирост',
      guard: 'Защита от Регрессии',
      status: 'Статус Управления',
      actions: 'Действия',
      inspect: 'Свидетельства',
      approve: 'Одобрить',
      shadow: 'Теневой',
      canary: 'Канареечный',
      activate: 'Активировать',
      empty: 'Кандидаты ML моделей отсутствуют. Запустите контролируемое обучение.',
      passed: 'Пройдена',
      regression: 'Регрессия Обнаружена',
    },
    en: {
      title: 'ML Model Candidates & Evidence Registry',
      subtitle: 'Governed model versions trained on historical data with strict temporal cross-validation',
      version: 'Version Tag',
      type: 'Model Type',
      samples: 'Samples',
      testMae: 'Test MAE',
      baselineMae: 'Baseline MAE',
      gain: 'Improvement',
      guard: 'Regression Guard',
      status: 'Governance Status',
      actions: 'Actions',
      inspect: 'Inspect Evidence',
      approve: 'Approve',
      shadow: 'Shadow Rollout',
      canary: 'Canary Rollout',
      activate: 'Activate',
      empty: 'No ML model candidates available yet. Initiate controlled training to evaluate a candidate.',
      passed: 'Passed',
      regression: 'Regression Flagged',
    },
  }[lang]

  const isAdmin = userRole === 'ADMIN'

  const getStatusBadge = (status: MethodVersionStatus) => {
    switch (status) {
      case 'ACTIVE':
        return <Badge className="bg-emerald-600 text-white font-mono text-xs">ACTIVE</Badge>
      case 'CANARY':
        return <Badge className="bg-amber-600 text-white font-mono text-xs">CANARY</Badge>
      case 'SHADOW':
        return <Badge className="bg-purple-600 text-white font-mono text-xs">SHADOW</Badge>
      case 'APPROVED':
        return <Badge className="bg-sky-600 text-white font-mono text-xs">APPROVED</Badge>
      case 'CANDIDATE':
        return <Badge variant="outline" className="border-amber-500/50 text-amber-600 dark:text-amber-400 font-mono text-xs">CANDIDATE</Badge>
      case 'RETIRED':
        return <Badge variant="secondary" className="font-mono text-xs">RETIRED</Badge>
      default:
        return <Badge variant="outline" className="font-mono text-xs">{status}</Badge>
    }
  }

  return (
    <Card className="border border-border shadow-xs">
      <CardHeader className="p-4 sm:p-5 border-b border-border/50">
        <div className="flex items-center gap-2">
          <Layers className="h-4 w-4 text-primary" />
          <CardTitle className="text-base font-bold tracking-tight text-foreground">
            {t.title}
          </CardTitle>
          <Badge variant="outline" className="text-[11px] font-mono">
            {candidates.length} {lang === 'uz' ? 'ta nomzod' : 'candidates'}
          </Badge>
        </div>
        <CardDescription className="text-xs text-muted-foreground">
          {t.subtitle}
        </CardDescription>
      </CardHeader>

      <CardContent className="p-0">
        {isLoading ? (
          <div className="py-12 text-center text-xs text-muted-foreground animate-pulse">
            Loading ML model candidates...
          </div>
        ) : candidates.length === 0 ? (
          <div className="py-12 px-4 text-center">
            <Brain className="h-10 w-10 text-muted-foreground/30 mx-auto mb-2" />
            <p className="text-sm font-medium text-foreground">{t.empty}</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="bg-muted/40 text-muted-foreground text-[11px] font-semibold uppercase tracking-wider border-b border-border">
                <tr>
                  <th className="py-3 px-4">{t.version}</th>
                  <th className="py-3 px-3">{t.samples}</th>
                  <th className="py-3 px-3 text-right">{t.testMae}</th>
                  <th className="py-3 px-3 text-right">{t.gain}</th>
                  <th className="py-3 px-3 text-center">{t.guard}</th>
                  <th className="py-3 px-3">{t.status}</th>
                  <th className="py-3 px-4 text-right">{t.actions}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60">
                {candidates.map((cand) => (
                  <tr key={cand.id} className="hover:bg-muted/20 transition-colors">
                    <td className="py-3 px-4">
                      <div className="font-mono font-bold text-foreground">
                        {cand.versionTag}
                      </div>
                      <div className="text-[10px] text-muted-foreground">
                        {cand.modelType} • {new Date(cand.createdAt).toLocaleDateString()}
                      </div>
                    </td>

                    <td className="py-3 px-3 font-mono">
                      <span className="font-semibold">{cand.testSampleCount}</span>
                      <span className="text-[10px] text-muted-foreground ml-1">/ {cand.trainingSampleCount} train</span>
                    </td>

                    <td className="py-3 px-3 text-right font-mono font-bold">
                      {typeof cand.testMae === 'number' ? `${cand.testMae.toFixed(1)}%` : '—'}
                      <div className="text-[10px] text-muted-foreground font-normal">
                        base: {typeof cand.baselineMae === 'number' ? `${cand.baselineMae.toFixed(1)}%` : '—'}
                      </div>
                    </td>

                    <td className="py-3 px-3 text-right font-mono font-bold">
                      {typeof cand.maeImprovementPercent === 'number' ? (
                        <span className={cand.maeImprovementPercent > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-600 dark:text-rose-400'}>
                          {cand.maeImprovementPercent > 0 ? `+${cand.maeImprovementPercent.toFixed(1)}%` : `${cand.maeImprovementPercent.toFixed(1)}%`}
                        </span>
                      ) : (
                        '—'
                      )}
                    </td>

                    <td className="py-3 px-3 text-center">
                      {cand.regressionDetected ? (
                        <Badge variant="outline" className="bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/30 gap-1 text-[10px]">
                          <ShieldAlert className="h-3 w-3" />
                          <span>{t.regression}</span>
                        </Badge>
                      ) : (
                        <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/30 gap-1 text-[10px]">
                          <ShieldCheck className="h-3 w-3" />
                          <span>{t.passed}</span>
                        </Badge>
                      )}
                    </td>

                    <td className="py-3 px-3">
                      {getStatusBadge(cand.status)}
                    </td>

                    <td className="py-3 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5 flex-wrap">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => onSelectCandidate(cand)}
                          className="h-7 text-[11px] gap-1 px-2"
                        >
                          <Eye className="h-3 w-3" />
                          <span>{t.inspect}</span>
                        </Button>

                        {isAdmin && cand.status === 'CANDIDATE' && !cand.regressionDetected && (
                          <Button
                            variant="default"
                            size="sm"
                            disabled={isPromoting}
                            onClick={() => onPromoteCandidate?.(cand.id, 'APPROVED')}
                            className="h-7 text-[11px] px-2 bg-sky-600 hover:bg-sky-700 text-white"
                          >
                            {t.approve}
                          </Button>
                        )}

                        {isAdmin && cand.status === 'APPROVED' && (
                          <Button
                            variant="default"
                            size="sm"
                            disabled={isPromoting}
                            onClick={() => onPromoteCandidate?.(cand.id, 'SHADOW')}
                            className="h-7 text-[11px] px-2 bg-purple-600 hover:bg-purple-700 text-white"
                          >
                            {t.shadow}
                          </Button>
                        )}

                        {isAdmin && cand.status === 'SHADOW' && (
                          <Button
                            variant="default"
                            size="sm"
                            disabled={isPromoting}
                            onClick={() => onPromoteCandidate?.(cand.id, 'CANARY')}
                            className="h-7 text-[11px] px-2 bg-amber-600 hover:bg-amber-700 text-white"
                          >
                            {t.canary}
                          </Button>
                        )}

                        {isAdmin && (cand.status === 'CANARY' || cand.status === 'APPROVED') && (
                          <Button
                            variant="default"
                            size="sm"
                            disabled={isPromoting}
                            onClick={() => onPromoteCandidate?.(cand.id, 'ACTIVE')}
                            className="h-7 text-[11px] px-2 bg-emerald-600 hover:bg-emerald-700 text-white"
                          >
                            {t.activate}
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
