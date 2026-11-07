import React from 'react'
import {
  AlertOctagon,
  ArrowDownRight,
  ArrowUpRight,
  ChevronRight,
  Minus,
} from 'lucide-react'
import { FieldRiskProfile, RiskTrend } from '@/entities/risk'
import { Card, CardContent } from '@/shared/ui/card'
import { RiskSeverityBadge } from './RiskSeverityBadge'
import { RiskConfidenceIndicator } from './RiskConfidenceIndicator'
import { cn } from '@/shared/lib/utils'

interface FieldRiskCardProps {
  profile: FieldRiskProfile
  onSelect: (fieldId: string) => void
}

export const FieldRiskCard: React.FC<FieldRiskCardProps> = ({ profile, onSelect }) => {
  const getTrendIcon = (trend: RiskTrend) => {
    switch (trend) {
      case 'IMPROVING':
        return (
          <span title="Risk improving (declining)">
            <ArrowDownRight className="w-3.5 h-3.5 text-emerald-600" />
          </span>
        )
      case 'DEGRADING':
        return (
          <span title="Risk degrading (increasing)">
            <ArrowUpRight className="w-3.5 h-3.5 text-rose-600" />
          </span>
        )
      case 'STABLE':
        return (
          <span title="Risk stable">
            <Minus className="w-3.5 h-3.5 text-muted-foreground" />
          </span>
        )
      default:
        return null
    }
  }

  const formatEvaluatedAt = (iso: string) => {
    try {
      return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    } catch {
      return iso
    }
  }

  return (
    <Card
      className={cn(
        'group cursor-pointer border bg-card transition-all duration-200 hover:shadow-md hover:border-primary/40',
        profile.isHydraulicBlocked ? 'border-rose-400/60 dark:border-rose-800' : ''
      )}
      onClick={() => onSelect(profile.fieldId)}
      data-testid={`field-risk-card-${profile.fieldId}`}
    >
      <CardContent className="p-4 space-y-3">
        {/* Top Header: Field Name & Badges */}
        <div className="flex items-start justify-between gap-2">
          <div className="space-y-0.5">
            <h4 className="font-bold text-sm sm:text-base text-foreground group-hover:text-primary transition-colors line-clamp-1">
              {profile.fieldName}
            </h4>
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              {profile.cropType && <span>{profile.cropType}</span>}
              {profile.areaHectares && <span>• {profile.areaHectares} ha</span>}
              {profile.irrigationMethod && <span>• {profile.irrigationMethod}</span>}
            </div>
          </div>

          <div className="flex flex-col items-end gap-1 shrink-0">
            <RiskSeverityBadge severity={profile.overallSeverity} size="sm" />
            <RiskConfidenceIndicator confidence={profile.overallConfidence} size="sm" />
          </div>
        </div>

        {/* Priority Reason / Hydraulic Interlock Banner */}
        <div
          className={cn(
            'text-xs p-2 rounded-md font-medium line-clamp-2',
            profile.isHydraulicBlocked
              ? 'bg-rose-500/10 text-rose-700 dark:text-rose-400 border border-rose-200 dark:border-rose-900'
              : 'bg-muted/50 text-foreground'
          )}
        >
          {profile.isHydraulicBlocked ? (
            <span className="flex items-center gap-1.5 font-bold">
              <AlertOctagon className="w-3.5 h-3.5 shrink-0 text-rose-600" />
              {profile.hydraulicBlockReason || 'Hydraulic safety interlock active'}
            </span>
          ) : (
            profile.priorityReason
          )}
        </div>

        {/* Risk Score Progress & Trend */}
        <div className="space-y-1">
          <div className="flex items-center justify-between text-xs">
            <div className="flex items-center gap-1 text-muted-foreground">
              <span>Risk Score:</span>
              <span className="font-bold text-foreground">{profile.riskScore.toFixed(0)}/100</span>
              {getTrendIcon(profile.riskTrend)}
            </div>
            <span className="text-[11px] font-semibold text-muted-foreground">
              Priority Rank #{profile.priorityRank}
            </span>
          </div>

          <div className="h-2 w-full rounded-full bg-muted overflow-hidden">
            <div
              className={cn(
                'h-full rounded-full transition-all duration-500',
                profile.riskScore >= 75
                  ? 'bg-rose-500'
                  : profile.riskScore >= 50
                  ? 'bg-amber-500'
                  : profile.riskScore >= 25
                  ? 'bg-yellow-500'
                  : 'bg-emerald-500'
              )}
              style={{ width: `${Math.min(100, Math.max(0, profile.riskScore))}%` }}
            />
          </div>
        </div>

        {/* Bottom Footer: Recommendations & Evaluated At */}
        <div className="pt-1 flex items-center justify-between text-xs text-muted-foreground border-t border-border/60">
          <span className="font-medium text-foreground text-[11px]">
            {profile.recommendationCount} recommendation{profile.recommendationCount === 1 ? '' : 's'}
          </span>
          <div className="flex items-center gap-1 text-[11px]">
            <span>Updated {formatEvaluatedAt(profile.evaluatedAt)}</span>
            <ChevronRight className="w-3.5 h-3.5 group-hover:translate-x-0.5 transition-transform" />
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
