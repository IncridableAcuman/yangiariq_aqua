import React from 'react'
import {
  AlertCircle,
  Clock,
  Compass,
  HelpCircle,
  ShieldCheck,
} from 'lucide-react'
import { RiskEvidence, RiskEvidenceType } from '@/entities/risk'
import { Badge } from '@/shared/ui/badge'
import { cn } from '@/shared/lib/utils'

interface RiskEvidencePanelProps {
  evidenceList?: RiskEvidence[]
  compact?: boolean
}

export const RiskEvidencePanel: React.FC<RiskEvidencePanelProps> = ({
  evidenceList,
  compact = false,
}) => {
  if (!evidenceList || evidenceList.length === 0) {
    return (
      <div className="p-4 text-center rounded-lg border border-dashed text-xs text-muted-foreground">
        No specific evidence records registered for this assessment factor.
      </div>
    )
  }

  const getTypeBadge = (type: RiskEvidenceType) => {
    switch (type) {
      case 'OBSERVED':
        return (
          <Badge
            variant="outline"
            className="text-[10px] font-bold bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-300 dark:border-emerald-800 gap-1"
          >
            <ShieldCheck className="w-3 h-3" />
            <span>OBSERVED (GROUND TRUTH)</span>
          </Badge>
        )
      case 'PREDICTED':
        return (
          <Badge
            variant="outline"
            className="text-[10px] font-bold bg-blue-500/10 text-blue-700 dark:text-blue-400 border-blue-300 dark:border-blue-800 gap-1"
          >
            <Compass className="w-3 h-3" />
            <span>PREDICTED (FORECAST)</span>
          </Badge>
        )
      case 'UNRELIABLE':
        return (
          <Badge
            variant="outline"
            className="text-[10px] font-bold bg-amber-500/10 text-amber-800 dark:text-amber-400 border-amber-300 dark:border-amber-800 gap-1"
          >
            <HelpCircle className="w-3 h-3" />
            <span>UNRELIABLE / STALE</span>
          </Badge>
        )
      case 'MISSING':
        return (
          <Badge
            variant="outline"
            className="text-[10px] font-bold bg-rose-500/10 text-rose-700 dark:text-rose-400 border-rose-300 dark:border-rose-800 gap-1"
          >
            <AlertCircle className="w-3 h-3" />
            <span>MISSING INPUT</span>
          </Badge>
        )
      default:
        return null
    }
  }

  const formatRecordedTime = (iso?: string) => {
    if (!iso) return ''
    try {
      return new Date(iso).toLocaleString([], {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    } catch {
      return iso
    }
  }

  return (
    <div className="space-y-2" data-testid="risk-evidence-panel">
      {evidenceList.map((ev) => (
        <div
          key={ev.id}
          className={cn(
            'rounded-lg border bg-card p-3 transition-colors',
            compact ? 'text-xs py-2 px-3' : 'p-3.5',
            ev.evidenceType === 'MISSING' ? 'border-rose-300/40 dark:border-rose-900/40' : ''
          )}
        >
          <div className="flex flex-wrap items-center justify-between gap-2 mb-1.5">
            <div className="flex items-center gap-2">
              <span className="font-bold text-xs text-foreground font-mono">
                {ev.metricName}
              </span>
              <span className="text-[10px] font-medium text-muted-foreground uppercase">
                [{ev.source.replace(/_/g, ' ')}]
              </span>
            </div>

            <div className="flex items-center gap-1.5">
              {getTypeBadge(ev.evidenceType)}
              {ev.isAuthoritative && (
                <span className="text-[10px] font-semibold text-emerald-600 dark:text-emerald-400">
                  ★ Authoritative
                </span>
              )}
            </div>
          </div>

          <p className="text-xs text-foreground/90 leading-relaxed mb-2">{ev.message}</p>

          <div className="flex flex-wrap items-center justify-between gap-2 pt-1 border-t border-border/40 text-[11px] text-muted-foreground">
            <div className="flex items-center gap-3">
              {ev.observedValue !== null && ev.observedValue !== undefined && (
                <span>
                  Observed:{' '}
                  <strong className="text-foreground font-mono">
                    {ev.observedValue} {ev.unit || ''}
                  </strong>
                </span>
              )}
              {ev.thresholdValue !== null && ev.thresholdValue !== undefined && (
                <span>
                  Threshold:{' '}
                  <strong className="text-muted-foreground font-mono">
                    {ev.thresholdValue} {ev.unit || ''}
                  </strong>
                </span>
              )}
            </div>

            {ev.recordedAt && (
              <div className="flex items-center gap-1">
                <Clock className="w-3 h-3" />
                <span>{formatRecordedTime(ev.recordedAt)}</span>
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  )
}
