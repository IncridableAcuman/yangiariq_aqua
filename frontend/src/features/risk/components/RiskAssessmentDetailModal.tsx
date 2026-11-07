import React, { useState } from 'react'
import {
  AlertOctagon,
  AlertTriangle,
} from 'lucide-react'
import {
  useFieldRiskProfileQuery,
  useFieldRiskHistoryQuery,
  useAcknowledgeRiskMutation,
  useResolveRiskMutation,
} from '../queries/useRiskQueries'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/shared/ui/dialog'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/ui/tabs'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { RiskSeverityBadge } from './RiskSeverityBadge'
import { RiskConfidenceIndicator } from './RiskConfidenceIndicator'
import { RiskFactorBreakdown } from './RiskFactorBreakdown'
import { RiskRecommendationCard } from './RiskRecommendationCard'
import { RiskTrendChart } from './RiskTrendChart'
import { RiskHistoryTimeline } from './RiskHistoryTimeline'
import { RiskActionPanel } from './RiskActionPanel'
import { cn } from '@/shared/lib/utils'

interface RiskAssessmentDetailModalProps {
  fieldId: string | null
  isOpen: boolean
  onClose: () => void
  onNavigateRoute?: (route: string) => void
}

export const RiskAssessmentDetailModal: React.FC<RiskAssessmentDetailModalProps> = ({
  fieldId,
  isOpen,
  onClose,
  onNavigateRoute,
}) => {
  const [activeTab, setActiveTab] = useState<'factors' | 'recommendations' | 'history'>('factors')

  const {
    data: profile,
    isLoading: isProfileLoading,
    isError: isProfileError,
  } = useFieldRiskProfileQuery(fieldId)

  const {
    data: historyPage,
  } = useFieldRiskHistoryQuery(fieldId, { size: 10 })

  const ackMutation = useAcknowledgeRiskMutation()
  const resolveMutation = useResolveRiskMutation()

  if (!isOpen || !fieldId) {
    return null
  }

  const handleAcknowledge = async (notes?: string) => {
    if (!profile) return
    await ackMutation.mutateAsync({ id: profile.assessmentId, notes })
  }

  const handleResolve = async (resolutionNotes: string) => {
    if (!profile) return
    await resolveMutation.mutateAsync({ id: profile.assessmentId, resolutionNotes })
  }

  const formatEvaluatedTime = (iso?: string) => {
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
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent
        className="max-w-4xl max-h-[90vh] overflow-y-auto p-0 gap-0"
        data-testid="risk-assessment-detail-modal"
      >
        <DialogHeader className="sr-only">
          <DialogTitle>Parcel Risk Profile</DialogTitle>
        </DialogHeader>
        {isProfileLoading ? (
          <div className="p-12 flex flex-col items-center justify-center space-y-3">
            <span className="w-8 h-8 rounded-full border-2 border-primary border-t-transparent animate-spin" />
            <p className="text-xs text-muted-foreground font-medium">Loading parcel risk profile...</p>
          </div>
        ) : isProfileError || !profile ? (
          <div className="p-8 text-center space-y-3">
            <p className="text-sm font-semibold text-rose-600">Failed to load risk profile for this field.</p>
            <Button size="sm" variant="outline" onClick={onClose}>
              Close
            </Button>
          </div>
        ) : (
          <div className="flex flex-col">
            {/* Modal Header */}
            <div className="p-5 border-b border-border bg-card/80 backdrop-blur-sm sticky top-0 z-10 space-y-3">
              <div className="flex items-start justify-between gap-3">
                <div className="space-y-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
                      PARCEL RISK PROFILE
                    </span>
                    <RiskSeverityBadge severity={profile.overallSeverity} size="sm" />
                    <RiskConfidenceIndicator confidence={profile.overallConfidence} size="sm" />
                    <Badge variant="outline" className="text-[10px] font-semibold">
                      Rank #{profile.priorityRank}
                    </Badge>
                  </div>

                  <h2 className="text-xl font-bold text-foreground">{profile.fieldName}</h2>

                  <div className="flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
                    {profile.cropType && <span>Crop: <strong className="text-foreground">{profile.cropType}</strong></span>}
                    {profile.areaHectares && <span>Area: <strong className="text-foreground">{profile.areaHectares} ha</strong></span>}
                    {profile.soilType && <span>Soil: <strong className="text-foreground">{profile.soilType}</strong></span>}
                    {profile.irrigationMethod && <span>Method: <strong className="text-foreground">{profile.irrigationMethod}</strong></span>}
                    <span>• Evaluated: <strong>{formatEvaluatedTime(profile.evaluatedAt)}</strong></span>
                  </div>
                </div>

                <div className="text-right shrink-0">
                  <span className="text-[11px] font-semibold text-muted-foreground uppercase block">
                    Composite Score
                  </span>
                  <div className="flex items-baseline justify-end gap-1">
                    <span
                      className={cn(
                        'text-2xl font-bold font-mono',
                        profile.riskScore >= 75
                          ? 'text-rose-600'
                          : profile.riskScore >= 50
                          ? 'text-amber-600'
                          : 'text-emerald-600'
                      )}
                    >
                      {profile.riskScore.toFixed(0)}
                    </span>
                    <span className="text-xs text-muted-foreground">/100</span>
                  </div>
                </div>
              </div>

              {/* Hydraulic Safety Interlock Banner */}
              {profile.isHydraulicBlocked && (
                <div className="p-3 rounded-lg bg-rose-500/10 border border-rose-300 dark:border-rose-900 text-xs text-rose-800 dark:text-rose-300 flex items-start gap-2.5">
                  <AlertOctagon className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
                  <div>
                    <strong className="block font-semibold">HYDRAULIC SAFETY INTERLOCK ACTIVE</strong>
                    <span>{profile.hydraulicBlockReason}</span>
                  </div>
                </div>
              )}

              {/* Missing Data Warnings */}
              {profile.missingDataWarnings && profile.missingDataWarnings.length > 0 && (
                <div className="p-2.5 rounded-lg bg-amber-500/10 border border-amber-300 dark:border-amber-900 text-xs text-amber-800 dark:text-amber-300 flex items-start gap-2">
                  <AlertTriangle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
                  <div className="space-y-0.5">
                    <strong className="block font-semibold">Missing Telemetry or Baseline Configuration:</strong>
                    <ul className="list-disc list-inside space-y-0.5 text-[11px]">
                      {profile.missingDataWarnings.map((warning, idx) => (
                        <li key={idx}>{warning}</li>
                      ))}
                    </ul>
                  </div>
                </div>
              )}
            </div>

            {/* Modal Body: Tabs */}
            <div className="p-5 space-y-5">
              <Tabs
                value={activeTab}
                onValueChange={(val) => setActiveTab(val as any)}
                className="w-full"
              >
                <TabsList className="grid grid-cols-3 w-full mb-4">
                  <TabsTrigger value="factors" className="text-xs font-semibold">
                    Risk Factors & Evidence ({profile.riskFactors?.length || 0})
                  </TabsTrigger>
                  <TabsTrigger value="recommendations" className="text-xs font-semibold">
                    Actionable Recommendations ({profile.recommendations?.length || 0})
                  </TabsTrigger>
                  <TabsTrigger value="history" className="text-xs font-semibold">
                    Historical Trend & Timeline
                  </TabsTrigger>
                </TabsList>

                {/* Tab 1: Factors & Evidence */}
                <TabsContent value="factors" className="space-y-4">
                  <RiskFactorBreakdown factors={profile.riskFactors} />
                </TabsContent>

                {/* Tab 2: Recommendations */}
                <TabsContent value="recommendations" className="space-y-3">
                  {!profile.recommendations || profile.recommendations.length === 0 ? (
                    <div className="p-8 text-center rounded-xl border border-dashed text-xs text-muted-foreground">
                      No urgent recommendations. Operational conditions are within target limits.
                    </div>
                  ) : (
                    profile.recommendations.map((rec) => (
                      <RiskRecommendationCard
                        key={rec.id}
                        recommendation={rec}
                        onNavigateRoute={onNavigateRoute}
                        showFieldBadge={false}
                      />
                    ))
                  )}
                </TabsContent>

                {/* Tab 3: History & Trend */}
                <TabsContent value="history" className="space-y-6">
                  <div className="p-4 rounded-xl border bg-card space-y-3">
                    <h4 className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
                      Assessment Trajectory
                    </h4>
                    <RiskTrendChart
                      history={historyPage?.content || []}
                    />
                  </div>

                  <div className="space-y-3">
                    <h4 className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
                      Assessment Lifecycle Timeline
                    </h4>
                    <RiskHistoryTimeline history={historyPage?.content || []} />
                  </div>
                </TabsContent>
              </Tabs>

              {/* Bottom Action Panel */}
              <div className="pt-4 border-t border-border">
                <RiskActionPanel
                  profile={profile}
                  onAcknowledge={handleAcknowledge}
                  onResolve={handleResolve}
                  onNavigateRoute={onNavigateRoute}
                  isSubmitting={ackMutation.isPending || resolveMutation.isPending}
                />
              </div>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
