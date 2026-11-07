import React from 'react'
import { AlertOctagon, ArrowRight, ShieldAlert, Wrench } from 'lucide-react'
import { FieldRiskProfile } from '@/entities/risk'
import { Button } from '@/shared/ui/button'
import { RiskSeverityBadge } from './RiskSeverityBadge'
import { RiskConfidenceIndicator } from './RiskConfidenceIndicator'
import { cn } from '@/shared/lib/utils'

interface CriticalRiskBannerProps {
  emergencyFields: FieldRiskProfile[]
  onSelectField?: (fieldId: string) => void
  onNavigateRoute?: (route: string) => void
}

export const CriticalRiskBanner: React.FC<CriticalRiskBannerProps> = ({
  emergencyFields,
  onSelectField,
  onNavigateRoute,
}) => {
  if (!emergencyFields || emergencyFields.length === 0) {
    return null
  }

  const primaryEmergency = emergencyFields[0]
  const isHydraulic = primaryEmergency.isHydraulicBlocked

  return (
    <div
      className={cn(
        'relative overflow-hidden rounded-xl border p-4 sm:p-5 shadow-sm transition-all animate-in fade-in slide-in-from-top-2',
        isHydraulic
          ? 'bg-rose-950/15 border-rose-500/40 dark:bg-rose-950/40 dark:border-rose-700/60'
          : 'bg-amber-950/10 border-amber-500/40 dark:bg-amber-950/30 dark:border-amber-700/60'
      )}
      data-testid="critical-risk-banner"
      role="alert"
    >
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-start gap-3.5">
          <div
            className={cn(
              'p-2.5 rounded-xl shrink-0 text-white',
              isHydraulic ? 'bg-rose-600 dark:bg-rose-700' : 'bg-amber-600 dark:bg-amber-700'
            )}
          >
            {isHydraulic ? (
              <AlertOctagon className="w-6 h-6 animate-pulse" />
            ) : (
              <ShieldAlert className="w-6 h-6" />
            )}
          </div>

          <div className="space-y-1">
            <div className="flex flex-wrap items-center gap-2">
              <span className="text-xs font-bold uppercase tracking-wider text-rose-700 dark:text-rose-400">
                {isHydraulic ? '🚨 HYDRAULIC SAFETY INTERLOCK ACTIVE' : 'CRITICAL AGRICULTURAL EMERGENCY'}
              </span>
              <RiskSeverityBadge severity={primaryEmergency.overallSeverity} size="sm" />
              <RiskConfidenceIndicator confidence={primaryEmergency.overallConfidence} size="sm" />
            </div>

            <h3 className="text-base sm:text-lg font-bold text-foreground">
              {primaryEmergency.fieldName}: {isHydraulic ? 'Pressurization Strictly Blocked' : 'Immediate Action Required'}
            </h3>

            <p className="text-xs sm:text-sm text-muted-foreground leading-relaxed max-w-3xl">
              {primaryEmergency.hydraulicBlockReason || primaryEmergency.priorityReason}
            </p>

            {emergencyFields.length > 1 && (
              <p className="text-xs font-semibold text-rose-600 dark:text-rose-400 pt-0.5">
                +{emergencyFields.length - 1} additional parcel(s) also report critical alerts.
              </p>
            )}
          </div>
        </div>

        <div className="flex items-center gap-2 self-end md:self-center shrink-0">
          <Button
            size="sm"
            variant="destructive"
            className="gap-1.5 font-semibold text-xs shadow-sm"
            onClick={() => {
              if (onSelectField) onSelectField(primaryEmergency.fieldId)
              else if (onNavigateRoute) onNavigateRoute(`/anomalies?fieldId=${primaryEmergency.fieldId}`)
            }}
          >
            <Wrench className="w-3.5 h-3.5" />
            <span>Inspect Safety Block</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Button>
        </div>
      </div>
    </div>
  )
}
