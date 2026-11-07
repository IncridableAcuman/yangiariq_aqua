import React from 'react'
import {
  ExternalLink,
} from 'lucide-react'
import { RiskRecommendation } from '@/entities/risk'
import { Card, CardContent } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { RiskSeverityBadge } from './RiskSeverityBadge'
import { RiskConfidenceIndicator } from './RiskConfidenceIndicator'

interface RiskRecommendationCardProps {
  recommendation: RiskRecommendation
  onNavigateRoute?: (route: string) => void
  showFieldBadge?: boolean
}

export const RiskRecommendationCard: React.FC<RiskRecommendationCardProps> = ({
  recommendation,
  onNavigateRoute,
  showFieldBadge = true,
}) => {
  const formatTime = (iso: string) => {
    try {
      return new Date(iso).toLocaleDateString([], {
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
    <Card
      className="border bg-card transition-all duration-200 hover:shadow-sm"
      data-testid={`risk-recommendation-${recommendation.id}`}
    >
      <CardContent className="p-4 space-y-3">
        {/* Header: Title, Field, and Severity Badges */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="space-y-1">
            <div className="flex flex-wrap items-center gap-2">
              <span className="inline-flex items-center justify-center px-1.5 py-0.5 rounded text-[10px] font-bold bg-primary/10 text-primary">
                Priority #{recommendation.priorityRank}
              </span>
              <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {recommendation.category.replace(/_/g, ' ')}
              </span>
              {showFieldBadge && recommendation.fieldName && (
                <Badge variant="secondary" className="text-[11px] font-semibold">
                  {recommendation.fieldName}
                </Badge>
              )}
            </div>

            <h4 className="text-sm sm:text-base font-bold text-foreground">
              {recommendation.title}
            </h4>
          </div>

          <div className="flex items-center gap-1.5 shrink-0">
            <RiskSeverityBadge severity={recommendation.severity} size="sm" />
            <RiskConfidenceIndicator confidence={recommendation.confidence} size="sm" />
          </div>
        </div>

        {/* Description */}
        <p className="text-xs text-muted-foreground leading-relaxed">
          {recommendation.description}
        </p>

        {/* Reason and Next Step Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-xs">
          <div className="p-2.5 rounded-lg bg-muted/40 space-y-1 border border-border/50">
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider block">
              Trigger Reason
            </span>
            <p className="text-foreground text-xs leading-relaxed">{recommendation.reason}</p>
          </div>

          <div className="p-2.5 rounded-lg bg-primary/5 space-y-1 border border-primary/20">
            <span className="text-[11px] font-semibold text-primary uppercase tracking-wider block">
              Suggested Next Action
            </span>
            <p className="text-foreground font-medium text-xs leading-relaxed">
              {recommendation.suggestedNextAction}
            </p>
          </div>
        </div>

        {/* Footer: Related Entity, Timestamp & Deep Link Navigation */}
        <div className="pt-2 flex flex-wrap items-center justify-between gap-2 border-t border-border/60 text-xs text-muted-foreground">
          <div className="flex items-center gap-2 text-[11px]">
            {recommendation.relatedOperationalEntity && (
              <span className="font-mono text-muted-foreground">
                Ref: {recommendation.relatedOperationalEntity}
              </span>
            )}
            <span>• Generated: {formatTime(recommendation.createdAt)}</span>
          </div>

          {recommendation.destinationRoute && onNavigateRoute && (
            <Button
              size="sm"
              variant="default"
              className="h-7 text-xs px-2.5 gap-1.5 font-semibold"
              onClick={() => onNavigateRoute(recommendation.destinationRoute)}
            >
              <span>Execute in Workflow</span>
              <ExternalLink className="w-3 h-3" />
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
