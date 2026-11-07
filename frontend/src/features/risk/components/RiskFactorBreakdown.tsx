import React from 'react'
import { HelpCircle } from 'lucide-react'
import { RiskFactor } from '@/entities/risk'
import { RiskSeverityBadge } from './RiskSeverityBadge'
import { RiskConfidenceIndicator } from './RiskConfidenceIndicator'
import { RiskEvidencePanel } from './RiskEvidencePanel'

interface RiskFactorBreakdownProps {
  factors?: RiskFactor[]
}

export const RiskFactorBreakdown: React.FC<RiskFactorBreakdownProps> = ({ factors }) => {
  if (!factors || factors.length === 0) {
    return (
      <div className="p-6 text-center rounded-xl border border-dashed text-xs text-muted-foreground">
        No active risk factors identified for this parcel. All monitored parameters are within nominal safety thresholds.
      </div>
    )
  }

  return (
    <div className="space-y-3" data-testid="risk-factor-breakdown">
      {factors.map((factor) => (
        <div
          key={factor.id}
          className="rounded-xl border bg-card p-4 space-y-3 shadow-sm hover:border-border transition-colors"
        >
          {/* Factor Header */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <div className="space-y-0.5">
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  {factor.category.replace(/_/g, ' ')}
                </span>
                <span className="text-xs text-muted-foreground">• Contribution:</span>
                <span className="text-xs font-bold text-foreground">
                  {factor.contributionScore.toFixed(0)}%
                </span>
              </div>
              <h4 className="text-sm font-bold text-foreground">{factor.title}</h4>
            </div>

            <div className="flex items-center gap-1.5 shrink-0">
              <RiskSeverityBadge severity={factor.severity} size="sm" />
              <RiskConfidenceIndicator confidence={factor.confidence} size="sm" />
            </div>
          </div>

          {/* Description */}
          <p className="text-xs text-muted-foreground leading-relaxed">{factor.description}</p>

          {/* Cause and Projected Consequence / Potential Impact */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-xs">
            <div className="p-2.5 rounded-lg bg-rose-500/5 border border-rose-200/40 dark:border-rose-900/40 space-y-1">
              <span className="font-semibold text-rose-700 dark:text-rose-400 block text-[11px] uppercase tracking-wide">
                ⚠️ Potential Impact if Unresolved
              </span>
              <p className="text-foreground/90 text-xs">{factor.potentialImpact}</p>
            </div>

            <div className="p-2.5 rounded-lg bg-emerald-500/5 border border-emerald-200/40 dark:border-emerald-900/40 space-y-1">
              <span className="font-semibold text-emerald-700 dark:text-emerald-400 block text-[11px] uppercase tracking-wide">
                🛡️ Preventative Action
              </span>
              <p className="text-foreground/90 text-xs">{factor.suggestedAction}</p>
            </div>
          </div>

          {/* Missing Inputs Warning */}
          {factor.missingInputs && factor.missingInputs.length > 0 && (
            <div className="p-2 rounded-lg bg-amber-500/10 border border-amber-300 dark:border-amber-900 text-[11px] text-amber-800 dark:text-amber-300 flex items-center gap-1.5">
              <HelpCircle className="w-3.5 h-3.5 shrink-0" />
              <span>
                Missing domain inputs required for complete verification: {factor.missingInputs.join(', ')}
              </span>
            </div>
          )}

          {/* Supporting Evidence Sub-Panel */}
          {factor.evidenceList && factor.evidenceList.length > 0 && (
            <div className="pt-2 border-t border-border/50">
              <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider block mb-2">
                Identified Supporting Evidence ({factor.evidenceList.length})
              </span>
              <RiskEvidencePanel evidenceList={factor.evidenceList} compact />
            </div>
          )}
        </div>
      ))}
    </div>
  )
}
