import React from 'react'
import { AgronomicAssessment } from '@/entities/intelligence'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { TelemetryFreshnessBadge } from './TelemetryFreshnessBadge'
import { MoistureTrendIndicator } from './MoistureTrendIndicator'
import {
  ShieldAlert,
  ShieldCheck,
  Droplets,
  RefreshCw,
  Info,
  CheckCircle2,
  Sparkles,
} from 'lucide-react'

interface AgronomicAssessmentCardProps {
  assessment?: AgronomicAssessment | null
  isLoading?: boolean
  onEvaluate?: () => void
  isEvaluating?: boolean
}

export const AgronomicAssessmentCard: React.FC<AgronomicAssessmentCardProps> = ({
  assessment,
  isLoading,
  onEvaluate,
  isEvaluating,
}) => {
  if (isLoading) {
    return (
      <Card className="animate-pulse border-border/60">
        <CardHeader>
          <div className="h-5 w-48 bg-muted rounded mb-2" />
          <div className="h-4 w-72 bg-muted rounded" />
        </CardHeader>
        <CardContent>
          <div className="h-24 bg-muted/60 rounded" />
        </CardContent>
      </Card>
    )
  }

  if (!assessment) {
    return (
      <Card className="border-dashed border-border/80">
        <CardContent className="p-6 text-center">
          <Info className="w-8 h-8 mx-auto text-muted-foreground mb-2" />
          <div className="font-medium text-sm">No Agronomic Assessment Available</div>
          <p className="text-xs text-muted-foreground mt-1 mb-4">
            Trigger a deterministic telemetry evaluation to interpret crop and soil conditions.
          </p>
          {onEvaluate && (
            <Button
              size="sm"
              onClick={onEvaluate}
              disabled={isEvaluating}
              className="gap-2"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${isEvaluating ? 'animate-spin' : ''}`} />
              <span>Evaluate Telemetry</span>
            </Button>
          )}
        </CardContent>
      </Card>
    )
  }

  const confidenceColor =
    assessment.confidence === 'HIGH'
      ? 'bg-emerald-50 text-emerald-700 border-emerald-300 dark:bg-emerald-950/40 dark:text-emerald-300'
      : assessment.confidence === 'MEDIUM'
        ? 'bg-amber-50 text-amber-700 border-amber-300 dark:bg-amber-950/40 dark:text-amber-300'
        : 'bg-rose-50 text-rose-700 border-rose-300 dark:bg-rose-950/40 dark:text-rose-300'

  return (
    <Card className="border-border shadow-sm">
      <CardHeader className="pb-3 flex flex-row items-start justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1 flex-wrap">
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-indigo-500" />
              <span>Agronomic Telemetry Assessment</span>
            </CardTitle>
            <Badge variant="outline" className={confidenceColor}>
              {assessment.confidence} Confidence ({Math.round(assessment.confidenceScore * 100)}%)
            </Badge>
            <TelemetryFreshnessBadge freshness={assessment.telemetryFreshness} />
          </div>
          <CardDescription className="text-xs">
            Evaluated at {new Date(assessment.assessedAt).toLocaleString()}
          </CardDescription>
        </div>

        {onEvaluate && (
          <Button
            size="sm"
            variant="outline"
            onClick={onEvaluate}
            disabled={isEvaluating}
            className="gap-1.5 h-8 text-xs shrink-0"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isEvaluating ? 'animate-spin' : ''}`} />
            <span>Re-evaluate</span>
          </Button>
        )}
      </CardHeader>

      <CardContent className="space-y-4 pt-1">
        {/* Hydraulic Safety Interlock Alert */}
        {assessment.hydraulicSafetyBlocked ? (
          <div className="p-3 rounded-lg border border-rose-300 bg-rose-50 dark:bg-rose-950/30 text-rose-900 dark:text-rose-200 text-xs flex items-start gap-2.5">
            <ShieldAlert className="w-4 h-4 text-rose-600 dark:text-rose-400 shrink-0 mt-0.5" />
            <div>
              <span className="font-semibold">Hydraulic Safety Interlock Active:</span>{' '}
              {assessment.hydraulicSafetyReason || 'Irrigation blocked to protect pipeline integrity.'}
            </div>
          </div>
        ) : (
          <div className="p-2.5 rounded-lg border border-emerald-200/80 bg-emerald-50/50 dark:bg-emerald-950/20 text-emerald-800 dark:text-emerald-300 text-xs flex items-center justify-between">
            <div className="flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-emerald-600 dark:text-emerald-400 shrink-0" />
              <span className="font-medium">Hydraulic Pipeline Interlock: Normal (Clear to operate)</span>
            </div>
            <MoistureTrendIndicator trend={assessment.soilMoistureTrend} />
          </div>
        )}

        {/* Water Budget Constraint Alert */}
        {assessment.waterBudgetConstrained && (
          <div className="p-3 rounded-lg border border-amber-300 bg-amber-50 dark:bg-amber-950/30 text-amber-900 dark:text-amber-200 text-xs flex items-start gap-2.5">
            <Droplets className="w-4 h-4 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
            <div>
              <span className="font-semibold">Water Budget Exhausted:</span>{' '}
              {assessment.waterBudgetReason || 'Parcel allocation limit reached. Emergency irrigation only.'}
            </div>
          </div>
        )}

        {/* Summary text */}
        <div className="text-xs text-foreground/90 bg-slate-50 dark:bg-slate-900/50 p-3 rounded-lg border border-border/50">
          <div className="font-medium mb-1 text-slate-800 dark:text-slate-200">
            {assessment.recommendationsOverview}
          </div>
          <div className="text-muted-foreground leading-relaxed">
            {assessment.agronomicSummary}
          </div>
        </div>

        {/* Supporting Signals List */}
        {assessment.signals && assessment.signals.length > 0 && (
          <div>
            <div className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">
              Supporting Field Signals ({assessment.signals.length})
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {assessment.signals.map((sig) => (
                <div
                  key={sig.id}
                  className="p-2 rounded border border-border/70 bg-card text-xs flex items-start gap-2"
                >
                  <CheckCircle2 className="w-3.5 h-3.5 text-indigo-500 shrink-0 mt-0.5" />
                  <div className="min-w-0">
                    <div className="font-medium truncate">{sig.metric}: {sig.signalType}</div>
                    <div className="text-[11px] text-muted-foreground line-clamp-1">{sig.message}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
