import React, { useState } from 'react'
import { Card, CardContent, CardFooter, CardHeader } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Recommendation } from '@/entities/recommendation'
import { RecommendationPriorityBadge } from './RecommendationPriorityBadge'
import { RecommendationStatusBadge } from './RecommendationStatusBadge'
import { DismissRecommendationDialog } from './DismissRecommendationDialog'
import { SupportedLanguage, translations } from '@/shared/i18n'
import {
  CheckCircle2,
  Clock,
  Sparkles,
  Cpu,
  Layers,
  ChevronRight,
  ShieldAlert,
} from 'lucide-react'

interface RecommendationCardProps {
  recommendation: Recommendation
  lang: SupportedLanguage
  onViewDetails: (rec: Recommendation) => void
  onAccept?: (id: string) => Promise<void>
  onDismiss?: (id: string, reason: string) => Promise<void>
  onComplete?: (id: string) => Promise<void>
}

export const RecommendationCard: React.FC<RecommendationCardProps> = ({
  recommendation,
  lang,
  onViewDetails,
  onAccept,
  onDismiss,
  onComplete,
}) => {
  const [isDismissOpen, setIsDismissOpen] = useState(false)
  const [isProcessing, setIsProcessing] = useState(false)

  const t = translations[lang].recommendations || {}
  const actionLabels = t.actions || {
    viewDetails: 'View Details',
    accept: 'Accept',
    dismiss: 'Dismiss',
    complete: 'Mark Completed',
  }

  const confidencePercent = Math.round(recommendation.confidence * 100)
  const isPipeBurst = recommendation.recommendationType === 'INSPECT_PIPE_BURST'
  const isTerminal =
    recommendation.status === 'DISMISSED' ||
    recommendation.status === 'COMPLETED' ||
    recommendation.status === 'EXPIRED'

  const handleAccept = async (e: React.MouseEvent) => {
    e.stopPropagation()
    if (!onAccept) return
    try {
      setIsProcessing(true)
      await onAccept(recommendation.id)
    } finally {
      setIsProcessing(false)
    }
  }

  const handleComplete = async (e: React.MouseEvent) => {
    e.stopPropagation()
    if (!onComplete) return
    try {
      setIsProcessing(true)
      await onComplete(recommendation.id)
    } finally {
      setIsProcessing(false)
    }
  }

  const handleDismissConfirm = async (reason: string) => {
    if (!onDismiss) return
    try {
      setIsProcessing(true)
      await onDismiss(recommendation.id, reason)
    } finally {
      setIsProcessing(false)
    }
  }

  return (
    <>
      <Card
        onClick={() => onViewDetails(recommendation)}
        className={`cursor-pointer transition-all hover:shadow-md border-l-4 ${
          isPipeBurst || recommendation.priority === 'CRITICAL'
            ? 'border-l-rose-500 bg-rose-500/5 dark:bg-rose-950/20'
            : recommendation.priority === 'HIGH'
            ? 'border-l-amber-500 bg-amber-500/5 dark:bg-amber-950/20'
            : 'border-l-primary/40'
        }`}
      >
        <CardHeader className="pb-2">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="flex flex-wrap items-center gap-2">
              <RecommendationPriorityBadge priority={recommendation.priority} lang={lang} />
              <RecommendationStatusBadge status={recommendation.status} lang={lang} />
              <Badge variant="outline" className="text-xs gap-1 font-mono">
                {recommendation.source === 'HYBRID_AI' ? (
                  <>
                    <Sparkles className="w-3 h-3 text-purple-600 dark:text-purple-400" />
                    <span>Hybrid AI</span>
                  </>
                ) : (
                  <>
                    <Cpu className="w-3 h-3 text-blue-600 dark:text-blue-400" />
                    <span>Deterministic</span>
                  </>
                )}
              </Badge>
              {recommendation.isSimulated && (
                <Badge variant="secondary" className="text-xs bg-amber-500/10 text-amber-600 border-amber-300">
                  Simulated
                </Badge>
              )}
            </div>

            {/* Confidence indicator */}
            <div className="flex items-center gap-1.5 text-xs font-medium">
              <span className="text-muted-foreground">{t.confidence?.title || 'Confidence'}:</span>
              <span
                className={`font-semibold ${
                  confidencePercent >= 80
                    ? 'text-emerald-600 dark:text-emerald-400'
                    : confidencePercent >= 60
                    ? 'text-amber-600 dark:text-amber-400'
                    : 'text-rose-600 dark:text-rose-400'
                }`}
              >
                {confidencePercent}%
              </span>
            </div>
          </div>

          <h3 className="text-base font-semibold leading-tight pt-2 text-foreground flex items-center gap-2">
            {isPipeBurst && <ShieldAlert className="w-4 h-4 text-rose-600 animate-pulse shrink-0" />}
            {recommendation.title}
          </h3>
        </CardHeader>

        <CardContent className="pb-3 text-sm text-muted-foreground space-y-3">
          <p className="line-clamp-2">{recommendation.summary}</p>

          <div className="flex flex-wrap items-center gap-3 text-xs text-muted-foreground pt-1">
            {recommendation.actions && recommendation.actions.length > 0 && (
              <span className="flex items-center gap-1 bg-muted/60 px-2 py-0.5 rounded">
                <CheckCircle2 className="w-3 h-3 text-primary" />
                {recommendation.actions.length} {lang === 'uz' ? 'amal' : lang === 'ru' ? 'действий' : 'actions'}
              </span>
            )}

            {recommendation.evidence && recommendation.evidence.length > 0 && (
              <span className="flex items-center gap-1 bg-muted/60 px-2 py-0.5 rounded">
                <Layers className="w-3 h-3 text-primary" />
                {recommendation.evidence.length} {lang === 'uz' ? 'fakt' : lang === 'ru' ? 'показателей' : 'evidence'}
              </span>
            )}

            <span className="flex items-center gap-1 ml-auto">
              <Clock className="w-3 h-3" />
              {new Date(recommendation.generatedAt).toLocaleDateString(
                lang === 'uz' ? 'uz-UZ' : lang === 'ru' ? 'ru-RU' : 'en-US',
                { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }
              )}
            </span>
          </div>
        </CardContent>

        <CardFooter className="pt-0 flex flex-wrap items-center justify-between gap-2 border-t border-border/50 py-2.5">
          <div className="flex items-center gap-2">
            {!isTerminal && recommendation.status !== 'ACCEPTED' && onAccept && (
              <Button
                size="sm"
                variant="outline"
                className="h-8 text-xs border-emerald-500/40 text-emerald-700 hover:bg-emerald-500/10"
                onClick={handleAccept}
                disabled={isProcessing}
              >
                {actionLabels.accept}
              </Button>
            )}

            {recommendation.status === 'ACCEPTED' && onComplete && (
              <Button
                size="sm"
                variant="outline"
                className="h-8 text-xs border-teal-500/40 text-teal-700 hover:bg-teal-500/10 font-medium"
                onClick={handleComplete}
                disabled={isProcessing}
              >
                {actionLabels.complete}
              </Button>
            )}

            {!isTerminal && onDismiss && (
              <Button
                size="sm"
                variant="ghost"
                className="h-8 text-xs text-muted-foreground hover:text-rose-600"
                onClick={(e) => {
                  e.stopPropagation()
                  setIsDismissOpen(true)
                }}
                disabled={isProcessing}
              >
                {actionLabels.dismiss}
              </Button>
            )}
          </div>

          <Button
            size="sm"
            variant="ghost"
            className="h-8 text-xs gap-1 ml-auto"
            onClick={(e) => {
              e.stopPropagation()
              onViewDetails(recommendation)
            }}
          >
            <span>{actionLabels.viewDetails}</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </Button>
        </CardFooter>
      </Card>

      <DismissRecommendationDialog
        open={isDismissOpen}
        onOpenChange={setIsDismissOpen}
        onConfirm={handleDismissConfirm}
        isLoading={isProcessing}
        lang={lang}
      />
    </>
  )
}
