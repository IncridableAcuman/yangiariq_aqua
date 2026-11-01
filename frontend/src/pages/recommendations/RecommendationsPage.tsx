import React, { useState } from 'react'
import {
  AlertOctagon,
  AlertTriangle,
  BellRing,
  CheckCircle2,
  CheckSquare,
  Sparkles,
} from 'lucide-react'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { Card, CardContent } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { RecommendationFeed } from '@/features/recommendation/components/RecommendationFeed'
import {
  useAcceptRecommendation,
  useCompleteRecommendation,
  useDismissRecommendation,
  useGenerateRecommendation,
  useMarkRecommendationViewed,
  useRecommendations,
  useRecommendationSummary,
} from '@/features/recommendation/queries/useRecommendationQueries'
import { useFieldsQuery } from '@/features/field/queries/useFieldQueries'
import {
  AgronomicAssessmentCard,
  IntelligencePipelineFlow,
  useEvaluateFieldIntelligence,
  useFieldAgronomicAssessment,
} from '@/features/intelligence'

interface RecommendationsPageProps {
  lang: SupportedLanguage
}

export const RecommendationsPage: React.FC<RecommendationsPageProps> = ({ lang }) => {
  const t = translations[lang].recommendations || {}
  const statsT = t.stats || {
    totalActive: 'Active Recommendations',
    critical: 'Critical Action',
    high: 'High Priority',
    accepted: 'Accepted',
    completed: 'Completed',
  }

  const { data: summary } = useRecommendationSummary()
  const { data: pageData, isLoading } = useRecommendations({ size: 50 })
  const { data: fieldsData } = useFieldsQuery()

  const generateMutation = useGenerateRecommendation()
  const acceptMutation = useAcceptRecommendation()
  const dismissMutation = useDismissRecommendation()
  const completeMutation = useCompleteRecommendation()
  const markViewedMutation = useMarkRecommendationViewed()

  const [isGenerating, setIsGenerating] = useState(false)

  const fieldsList = fieldsData?.content || []
  const activeField = fieldsList.length > 0 ? fieldsList[0] : null

  const { data: assessment, isLoading: isAssessmentLoading } = useFieldAgronomicAssessment(activeField?.id)
  const evaluateMutation = useEvaluateFieldIntelligence()

  const handleGenerateOnDemand = async () => {
    if (!activeField) return

    try {
      setIsGenerating(true)
      await generateMutation.mutateAsync(activeField.id)
    } finally {
      setIsGenerating(false)
    }
  }

  const handleEvaluateTelemetry = async () => {
    if (!activeField) return
    await evaluateMutation.mutateAsync(activeField.id)
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-border/80 pb-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2.5">
            <Sparkles className="w-6 h-6 text-purple-600 dark:text-purple-400" />
            <span>{t.title || 'AI Agronomic Recommendations'}</span>
          </h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1 max-w-2xl">
            {t.subtitle ||
              'Holistic decision-support integrating telemetry, anomaly detection, weather forecast, and water allocations for Yangiariq parcels.'}
          </p>
        </div>

        {fieldsList.length > 0 && (
          <Button
            onClick={handleGenerateOnDemand}
            disabled={isGenerating}
            className="gap-2 bg-purple-600 hover:bg-purple-700 text-white font-medium shrink-0 self-start sm:self-auto"
          >
            <Sparkles className={`w-4 h-4 ${isGenerating ? 'animate-spin' : ''}`} />
            <span>{isGenerating ? t.generating || 'Evaluating...' : t.generateBtn || 'Evaluate & Generate'}</span>
          </Button>
        )}
      </div>

      {/* Decision-Support Intelligence Pipeline Flow */}
      <IntelligencePipelineFlow />

      {/* Deterministic Agronomic Assessment Card */}
      {activeField && (
        <AgronomicAssessmentCard
          assessment={assessment}
          isLoading={isAssessmentLoading}
          onEvaluate={handleEvaluateTelemetry}
          isEvaluating={evaluateMutation.isPending}
        />
      )}

      {/* KPI Overview Summary Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
        {/* Total Active */}
        <Card className="border-border bg-card">
          <CardContent className="p-3.5 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-primary/10 text-primary shrink-0">
              <BellRing className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[11px] text-muted-foreground font-medium">{statsT.totalActive}</p>
              <p className="text-lg font-bold text-foreground mt-0.5">
                {summary?.totalActive ?? 0}
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Critical Alerts */}
        <Card className="border-rose-300/60 dark:border-rose-900/60 bg-rose-500/5 dark:bg-rose-950/20">
          <CardContent className="p-3.5 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-rose-500/10 text-rose-600 dark:text-rose-400 shrink-0">
              <AlertOctagon className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[11px] text-rose-700 dark:text-rose-300 font-medium">{statsT.critical}</p>
              <p className="text-lg font-bold text-rose-700 dark:text-rose-300 mt-0.5">
                {summary?.criticalCount ?? 0}
              </p>
            </div>
          </CardContent>
        </Card>

        {/* High Priority */}
        <Card className="border-amber-300/60 dark:border-amber-900/60 bg-amber-500/5 dark:bg-amber-950/20">
          <CardContent className="p-3.5 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-amber-500/10 text-amber-600 dark:text-amber-400 shrink-0">
              <AlertTriangle className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[11px] text-amber-700 dark:text-amber-300 font-medium">{statsT.high}</p>
              <p className="text-lg font-bold text-amber-700 dark:text-amber-300 mt-0.5">
                {summary?.highCount ?? 0}
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Accepted Plans */}
        <Card className="border-emerald-300/60 dark:border-emerald-900/60 bg-emerald-500/5 dark:bg-emerald-950/20">
          <CardContent className="p-3.5 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 shrink-0">
              <CheckCircle2 className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[11px] text-emerald-700 dark:text-emerald-300 font-medium">{statsT.accepted}</p>
              <p className="text-lg font-bold text-emerald-700 dark:text-emerald-300 mt-0.5">
                {summary?.acceptedCount ?? 0}
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Completed Actions */}
        <Card className="border-teal-300/60 dark:border-teal-900/60 bg-teal-500/5 dark:bg-teal-950/20 col-span-2 sm:col-span-1">
          <CardContent className="p-3.5 flex items-center gap-3">
            <div className="p-2 rounded-lg bg-teal-500/10 text-teal-600 dark:text-teal-400 shrink-0">
              <CheckSquare className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[11px] text-teal-700 dark:text-teal-300 font-medium">{statsT.completed}</p>
              <p className="text-lg font-bold text-teal-700 dark:text-teal-300 mt-0.5">
                {summary?.completedCount ?? 0}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Main Feed Section */}
      <RecommendationFeed
        recommendations={pageData?.content ?? []}
        isLoading={isLoading}
        lang={lang}
        onAccept={async (id) => {
          await acceptMutation.mutateAsync(id)
        }}
        onDismiss={async (id, reason) => {
          await dismissMutation.mutateAsync({ id, reason })
        }}
        onComplete={async (id) => {
          await completeMutation.mutateAsync(id)
        }}
        onMarkViewed={async (id) => {
          await markViewedMutation.mutateAsync(id)
        }}
      />
    </div>
  )
}

