import React from 'react'
import { CheckCircle2, Clock, UserCheck } from 'lucide-react'
import { FieldRiskProfile } from '@/entities/risk'
import { Badge } from '@/shared/ui/badge'
import { RiskSeverityBadge } from './RiskSeverityBadge'
import { cn } from '@/shared/lib/utils'

interface RiskHistoryTimelineProps {
  history?: FieldRiskProfile[]
  onSelectAssessment?: (assessmentId: string) => void
}

export const RiskHistoryTimeline: React.FC<RiskHistoryTimelineProps> = ({
  history = [],
}) => {
  if (!history || history.length === 0) {
    return (
      <div className="p-6 text-center rounded-xl border border-dashed text-xs text-muted-foreground">
        No prior assessment snapshots recorded for this parcel.
      </div>
    )
  }

  const formatDateTime = (iso: string) => {
    try {
      return new Date(iso).toLocaleString([], {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    } catch {
      return iso
    }
  }

  return (
    <div className="relative pl-6 space-y-4 border-l-2 border-border/80 ml-2" data-testid="risk-history-timeline">
      {history.map((item) => (
        <div key={item.assessmentId} className="relative group">
          {/* Dot on timeline */}
          <div
            className={cn(
              'absolute -left-[31px] top-1.5 w-3.5 h-3.5 rounded-full border-2 border-background',
              item.overallSeverity === 'CRITICAL'
                ? 'bg-rose-500'
                : item.overallSeverity === 'HIGH'
                ? 'bg-amber-500'
                : 'bg-emerald-500'
            )}
          />

          <div className="p-3.5 rounded-xl border bg-card space-y-2 shadow-sm transition-all group-hover:border-primary/40">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-foreground">
                  Score: {item.riskScore.toFixed(0)}/100
                </span>
                <RiskSeverityBadge severity={item.overallSeverity} size="sm" />
                <Badge variant="outline" className="text-[10px]">
                  {item.lifecycleStatus}
                </Badge>
              </div>

              <div className="flex items-center gap-1 text-[11px] text-muted-foreground">
                <Clock className="w-3 h-3" />
                <span>{formatDateTime(item.evaluatedAt)}</span>
              </div>
            </div>

            <p className="text-xs text-muted-foreground">{item.priorityReason}</p>

            {/* Acknowledgment & Resolution details */}
            {(item.acknowledgedAt || item.resolvedAt) && (
              <div className="pt-2 border-t border-border/50 text-xs space-y-1 text-muted-foreground">
                {item.acknowledgedAt && (
                  <div className="flex items-center gap-1.5 text-blue-700 dark:text-blue-400">
                    <UserCheck className="w-3.5 h-3.5" />
                    <span>Acknowledged on {formatDateTime(item.acknowledgedAt)}</span>
                  </div>
                )}
                {item.resolvedAt && (
                  <div className="flex items-start gap-1.5 text-emerald-700 dark:text-emerald-400">
                    <CheckCircle2 className="w-3.5 h-3.5 shrink-0 mt-0.5" />
                    <div>
                      <span>Resolved on {formatDateTime(item.resolvedAt)}</span>
                      {item.resolutionNotes && (
                        <p className="italic text-foreground/80 mt-0.5 font-normal">
                          &quot;{item.resolutionNotes}&quot;
                        </p>
                      )}
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  )
}
