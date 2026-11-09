import React, { useState } from 'react'
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  Button,
} from '@/shared/ui'
import {
  FileText,
  FileBarChart,
  Calendar,
  User,
  AlertTriangle,
  Lightbulb,
  Droplets,
  Activity,
  Layers,
  Database,
} from 'lucide-react'
import { translations, SupportedLang } from '@/shared/i18n'
import {
  usePilotEvaluationReportQuery,
  useGenerateEvaluationReportMutation,
} from '../queries/usePilotQueries'
import { GenerateReportDialog } from './GenerateReportDialog'

interface PilotEvaluationReportViewProps {
  pilotId: string
  lang: SupportedLang
}

export const PilotEvaluationReportView: React.FC<PilotEvaluationReportViewProps> = ({
  pilotId,
  lang,
}) => {
  const t = translations[lang].pilot

  const [isGenerateDialogOpen, setIsGenerateDialogOpen] = useState(false)

  const {
    data: report,
    isLoading,
    refetch,
  } = usePilotEvaluationReportQuery(pilotId)

  const generateMutation = useGenerateEvaluationReportMutation()

  const parseJsonSafe = (jsonStr?: string) => {
    if (!jsonStr) return {}
    try {
      return JSON.parse(jsonStr)
    } catch {
      return {}
    }
  }

  const dataPerf = parseJsonSafe(report?.dataPerformanceJson)
  const predPerf = parseJsonSafe(report?.predictionPerformanceJson)
  const opsPerf = parseJsonSafe(report?.operationalPerformanceJson)
  const waterPerf = parseJsonSafe(report?.waterOutcomesJson)

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b">
          <div>
            <div className="flex items-center gap-2">
              <FileBarChart className="h-5 w-5 text-primary" />
              <CardTitle className="text-lg font-bold">{t.evaluationReport}</CardTitle>
            </div>
            <CardDescription className="text-xs mt-1">
              Evidence-based evaluation snapshot synthesizing ground-truth observations, model accuracy, and operational compliance.
            </CardDescription>
          </div>
          <Button size="sm" onClick={() => setIsGenerateDialogOpen(true)}>
            <FileText className="h-4 w-4 mr-1.5" />
            {t.generateSnapshot}
          </Button>
        </CardHeader>

        <CardContent className="pt-6 space-y-6">
          {isLoading ? (
            <div className="py-12 text-center text-xs text-muted-foreground">Loading report...</div>
          ) : !report ? (
            <div className="py-16 border border-dashed rounded-lg text-center space-y-3">
              <FileBarChart className="h-10 w-10 mx-auto text-muted-foreground opacity-50" />
              <div className="text-sm font-semibold">{t.noReportGenerated}</div>
              <p className="text-xs text-muted-foreground max-w-md mx-auto">
                Generate an evaluation snapshot to synthesize all pilot ground-truth observations, model validations, and operational KPIs.
              </p>
              <Button size="sm" onClick={() => setIsGenerateDialogOpen(true)} className="mt-2">
                {t.generateSnapshot}
              </Button>
            </div>
          ) : (
            <div className="space-y-6">
              {/* Report Header Metadata */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 p-4 bg-muted/30 rounded-lg text-xs">
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                  <div>
                    <span className="text-muted-foreground">{t.reportPeriod}:</span>
                    <div className="font-semibold mt-0.5">
                      {report.periodStart ? new Date(report.periodStart).toLocaleDateString() : 'N/A'} →{' '}
                      {report.periodEnd ? new Date(report.periodEnd).toLocaleDateString() : 'N/A'}
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <User className="h-4 w-4 text-muted-foreground" />
                  <div>
                    <span className="text-muted-foreground">Evaluator:</span>
                    <div className="font-mono font-semibold mt-0.5">{report.createdBy}</div>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <Activity className="h-4 w-4 text-muted-foreground" />
                  <div>
                    <span className="text-muted-foreground">Generated At:</span>
                    <div className="font-semibold mt-0.5">
                      {report.createdAt ? new Date(report.createdAt).toLocaleString() : 'N/A'}
                    </div>
                  </div>
                </div>
              </div>

              {/* 4 Quantitative Pillars */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Pillar 1: Data Performance */}
                <div className="border rounded-lg p-4 space-y-3 bg-card">
                  <div className="flex items-center gap-2 border-b pb-2">
                    <Database className="h-4 w-4 text-primary" />
                    <h4 className="text-xs font-bold uppercase tracking-wider">
                      {t.dataPerformance}
                    </h4>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <div className="p-2.5 bg-muted/40 rounded">
                      <span className="text-muted-foreground">Total Ground Truth</span>
                      <div className="text-base font-bold mt-1">
                        {dataPerf.totalGroundTruthCount ?? '0'}
                      </div>
                    </div>
                    <div className="p-2.5 bg-emerald-50 dark:bg-emerald-950/20 text-emerald-800 dark:text-emerald-300 rounded">
                      <span>Verified Observations</span>
                      <div className="text-base font-bold mt-1">
                        {dataPerf.verifiedCount ?? '0'}
                      </div>
                    </div>
                    <div className="p-2.5 bg-muted/40 rounded">
                      <span className="text-muted-foreground">Pending Four-Eyes</span>
                      <div className="text-base font-bold mt-1">
                        {dataPerf.pendingCount ?? '0'}
                      </div>
                    </div>
                    <div className="p-2.5 bg-muted/40 rounded">
                      <span className="text-muted-foreground">Verification Rate</span>
                      <div className="text-base font-bold mt-1">
                        {dataPerf.verificationRate != null
                          ? `${(dataPerf.verificationRate * 100).toFixed(1)}%`
                          : '—'}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Pillar 2: Predictive Performance */}
                <div className="border rounded-lg p-4 space-y-3 bg-card">
                  <div className="flex items-center gap-2 border-b pb-2">
                    <Activity className="h-4 w-4 text-blue-500" />
                    <h4 className="text-xs font-bold uppercase tracking-wider">
                      {t.predictionPerformance}
                    </h4>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <div className="p-2.5 bg-muted/40 rounded">
                      <span className="text-muted-foreground">Model Validations</span>
                      <div className="text-base font-bold mt-1">
                        {predPerf.totalValidationsCount ?? '0'}
                      </div>
                    </div>
                    <div className="p-2.5 bg-blue-50 dark:bg-blue-950/20 text-blue-800 dark:text-blue-300 rounded">
                      <span>Mean Absolute Error</span>
                      <div className="text-base font-bold mt-1">
                        {predPerf.meanAbsoluteError != null
                          ? predPerf.meanAbsoluteError.toFixed(2)
                          : '—'}
                      </div>
                    </div>
                    <div className="p-2.5 bg-muted/40 rounded">
                      <span className="text-muted-foreground">Mean Relative Error</span>
                      <div className="text-base font-bold mt-1">
                        {predPerf.meanRelativeErrorPercentage != null
                          ? `${predPerf.meanRelativeErrorPercentage.toFixed(1)}%`
                          : '—'}
                      </div>
                    </div>
                    <div className="p-2.5 bg-emerald-50 dark:bg-emerald-950/20 text-emerald-800 dark:text-emerald-300 rounded">
                      <span>Valid Comparisons</span>
                      <div className="text-base font-bold mt-1">
                        {predPerf.validatedCount ?? '0'}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Pillar 3: Operational Performance */}
                <div className="border rounded-lg p-4 space-y-3 bg-card">
                  <div className="flex items-center gap-2 border-b pb-2">
                    <Layers className="h-4 w-4 text-purple-500" />
                    <h4 className="text-xs font-bold uppercase tracking-wider">
                      {t.operationalPerformance}
                    </h4>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <div className="p-2.5 bg-muted/40 rounded">
                      <span className="text-muted-foreground">Logged Observations</span>
                      <div className="text-base font-bold mt-1">
                        {opsPerf.totalFeedbackCount ?? '0'}
                      </div>
                    </div>
                    <div className="p-2.5 bg-emerald-50 dark:bg-emerald-950/20 text-emerald-800 dark:text-emerald-300 rounded">
                      <span>Resolved Discrepancies</span>
                      <div className="text-base font-bold mt-1">
                        {opsPerf.resolvedCount ?? '0'}
                      </div>
                    </div>
                    <div className="p-2.5 bg-amber-50 dark:bg-amber-950/20 text-amber-800 dark:text-amber-300 rounded">
                      <span>Action Required</span>
                      <div className="text-base font-bold mt-1">
                        {opsPerf.actionRequiredCount ?? '0'}
                      </div>
                    </div>
                    <div className="p-2.5 bg-muted/40 rounded">
                      <span className="text-muted-foreground">Operational Gate</span>
                      <div className="text-base font-bold mt-1 text-emerald-600">
                        {opsPerf.operationalGateStatus ?? 'PASS'}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Pillar 4: Water Outcomes */}
                <div className="border rounded-lg p-4 space-y-3 bg-card">
                  <div className="flex items-center gap-2 border-b pb-2">
                    <Droplets className="h-4 w-4 text-cyan-500" />
                    <h4 className="text-xs font-bold uppercase tracking-wider">
                      {t.waterOutcomes}
                    </h4>
                  </div>
                  <div className="space-y-2 text-xs">
                    <div className="p-2.5 bg-cyan-50 dark:bg-cyan-950/20 text-cyan-800 dark:text-cyan-300 rounded flex justify-between items-center">
                      <span>Total Measured Irrigation Inflow</span>
                      <span className="font-mono font-bold text-sm">
                        {waterPerf.totalMeasuredVolumeM3 != null
                          ? `${waterPerf.totalMeasuredVolumeM3.toLocaleString()} m³`
                          : 'Baseline Pending'}
                      </span>
                    </div>
                    <div className="p-2.5 bg-muted/40 rounded text-muted-foreground text-[11px] leading-relaxed">
                      <strong>Governance Notice:</strong> Real water savings and efficiency claims require multi-season verified hydrological baselines. No unsupported claims are generated from single-flight tests.
                    </div>
                  </div>
                </div>
              </div>

              {/* Qualitative Sections: Limitations & Recommendations */}
              <div className="space-y-4 pt-2">
                <div className="border rounded-lg p-4 bg-amber-50/50 dark:bg-amber-950/20 border-amber-200 dark:border-amber-900/50 space-y-2">
                  <div className="flex items-center gap-2 text-amber-800 dark:text-amber-300 font-semibold text-xs uppercase tracking-wide">
                    <AlertTriangle className="h-4 w-4" />
                    {t.limitations}
                  </div>
                  <p className="text-xs text-amber-900/80 dark:text-amber-200/80 leading-relaxed whitespace-pre-wrap">
                    {report.limitationsText || 'None recorded.'}
                  </p>
                </div>

                <div className="border rounded-lg p-4 bg-emerald-50/50 dark:bg-emerald-950/20 border-emerald-200 dark:border-emerald-900/50 space-y-2">
                  <div className="flex items-center gap-2 text-emerald-800 dark:text-emerald-300 font-semibold text-xs uppercase tracking-wide">
                    <Lightbulb className="h-4 w-4" />
                    {t.recommendations}
                  </div>
                  <p className="text-xs text-emerald-900/80 dark:text-emerald-200/80 leading-relaxed whitespace-pre-wrap">
                    {report.recommendationsText || 'None recorded.'}
                  </p>
                </div>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Generate Report Dialog */}
      <GenerateReportDialog
        isOpen={isGenerateDialogOpen}
        onClose={() => setIsGenerateDialogOpen(false)}
        pilotId={pilotId}
        onSubmit={async (payload) => {
          await generateMutation.mutateAsync({ pilotId, payload })
          refetch()
        }}
        lang={lang}
      />
    </div>
  )
}
