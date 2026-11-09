import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, Badge, Button } from '@/shared/ui'
import {
  ShieldCheck,
  Server,
  Database,
  Users,
  Scale,
  RefreshCw,
  AlertTriangle,
  XCircle,
  CheckCircle2,
  HelpCircle,
} from 'lucide-react'
import { PilotReadinessAssessment, ReadinessResult, ReadinessCategory } from '@/entities/pilot'
import { translations, SupportedLang } from '@/shared/i18n'

interface PilotReadinessPanelProps {
  assessment?: PilotReadinessAssessment | null
  onTriggerAssessment: () => Promise<void>
  isAssessing: boolean
  canManage: boolean
  lang: SupportedLang
}

export const PilotReadinessPanel: React.FC<PilotReadinessPanelProps> = ({
  assessment,
  onTriggerAssessment,
  isAssessing,
  canManage,
  lang,
}) => {
  const t = translations[lang].pilot

  const getResultBadge = (res?: ReadinessResult) => {
    switch (res) {
      case 'PASS':
        return (
          <Badge className="bg-emerald-50 text-emerald-700 border-emerald-300 dark:bg-emerald-950/40 dark:text-emerald-300 flex items-center gap-1">
            <CheckCircle2 className="h-3 w-3" />
            PASS
          </Badge>
        )
      case 'WARNING':
        return (
          <Badge className="bg-amber-50 text-amber-700 border-amber-300 dark:bg-amber-950/40 dark:text-amber-300 flex items-center gap-1">
            <AlertTriangle className="h-3 w-3" />
            WARNING
          </Badge>
        )
      case 'BLOCKED':
        return (
          <Badge className="bg-rose-50 text-rose-700 border-rose-300 dark:bg-rose-950/40 dark:text-rose-300 flex items-center gap-1">
            <XCircle className="h-3 w-3" />
            BLOCKED
          </Badge>
        )
      default:
        return (
          <Badge className="bg-muted text-muted-foreground border-border flex items-center gap-1">
            <HelpCircle className="h-3 w-3" />
            NOT_ASSESSED
          </Badge>
        )
    }
  }

  const getCategoryIcon = (category: ReadinessCategory) => {
    switch (category) {
      case 'INFRASTRUCTURE':
        return <Server className="h-4 w-4 text-sky-500" />
      case 'DATA':
        return <Database className="h-4 w-4 text-purple-500" />
      case 'OPERATIONAL':
        return <Users className="h-4 w-4 text-emerald-500" />
      case 'GOVERNANCE':
        return <Scale className="h-4 w-4 text-amber-500" />
    }
  }

  if (!assessment) {
    return (
      <Card className="border border-border/60 bg-card/60 backdrop-blur-sm">
        <CardContent className="p-8 text-center space-y-3">
          <ShieldCheck className="h-10 w-10 text-muted-foreground mx-auto" />
          <div className="text-sm font-semibold text-foreground">{t.noReadinessAssessed}</div>
          <p className="text-xs text-muted-foreground max-w-md mx-auto">
            {t.readinessAssessmentNotice}
          </p>
          {canManage && (
            <Button
              size="sm"
              onClick={onTriggerAssessment}
              disabled={isAssessing}
              className="mt-2 text-xs"
            >
              {isAssessing ? <RefreshCw className="h-3.5 w-3.5 animate-spin mr-1.5" /> : null}
              {t.runReadinessAssessment}
            </Button>
          )}
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-4">
      {/* Category Status Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <Card className="border border-border/60 bg-card/60">
          <CardContent className="p-3.5">
            <div className="flex items-center justify-between mb-1">
              <span className="text-xs font-medium text-muted-foreground flex items-center gap-1.5">
                <Server className="h-3.5 w-3.5 text-sky-500" />
                {t.infrastructure}
              </span>
              {getResultBadge(assessment.infrastructureStatus)}
            </div>
            <p className="text-[11px] text-muted-foreground mt-1">{t.infraSummary}</p>
          </CardContent>
        </Card>

        <Card className="border border-border/60 bg-card/60">
          <CardContent className="p-3.5">
            <div className="flex items-center justify-between mb-1">
              <span className="text-xs font-medium text-muted-foreground flex items-center gap-1.5">
                <Database className="h-3.5 w-3.5 text-purple-500" />
                {t.dataReadiness}
              </span>
              {getResultBadge(assessment.dataStatus)}
            </div>
            <p className="text-[11px] text-muted-foreground mt-1">{t.dataSummary}</p>
          </CardContent>
        </Card>

        <Card className="border border-border/60 bg-card/60">
          <CardContent className="p-3.5">
            <div className="flex items-center justify-between mb-1">
              <span className="text-xs font-medium text-muted-foreground flex items-center gap-1.5">
                <Users className="h-3.5 w-3.5 text-emerald-500" />
                {t.operational}
              </span>
              {getResultBadge(assessment.operationalStatus)}
            </div>
            <p className="text-[11px] text-muted-foreground mt-1">{t.opsSummary}</p>
          </CardContent>
        </Card>

        <Card className="border border-border/60 bg-card/60">
          <CardContent className="p-3.5">
            <div className="flex items-center justify-between mb-1">
              <span className="text-xs font-medium text-muted-foreground flex items-center gap-1.5">
                <Scale className="h-3.5 w-3.5 text-amber-500" />
                {t.governance}
              </span>
              {getResultBadge(assessment.governanceStatus)}
            </div>
            <p className="text-[11px] text-muted-foreground mt-1">{t.govSummary}</p>
          </CardContent>
        </Card>
      </div>

      {/* Individual Checks Breakdown */}
      <Card className="border border-border/60 bg-card/60 backdrop-blur-sm">
        <CardHeader className="p-4 pb-2 flex flex-row items-center justify-between">
          <CardTitle className="text-sm font-semibold flex items-center gap-2">
            <ShieldCheck className="h-4 w-4 text-primary" />
            {t.detailedReadinessChecks}
          </CardTitle>
          <div className="flex items-center gap-2">
            <span className="text-xs text-muted-foreground">
              {t.overallGate}:
            </span>
            {getResultBadge(assessment.overallStatus)}
            {canManage && (
              <Button
                variant="outline"
                size="sm"
                className="h-7 text-xs ml-2"
                onClick={onTriggerAssessment}
                disabled={isAssessing}
              >
                <RefreshCw className={`h-3 w-3 mr-1 ${isAssessing ? 'animate-spin' : ''}`} />
                {t.reAssess}
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent className="p-4 pt-2 space-y-2">
          {assessment.checks.map((c, idx) => (
            <div
              key={idx}
              className="p-3 rounded-lg border border-border/50 bg-background/50 flex flex-col sm:flex-row sm:items-center justify-between gap-2"
            >
              <div className="space-y-0.5">
                <div className="flex items-center gap-2">
                  {getCategoryIcon(c.category)}
                  <span className="text-xs font-bold text-foreground">{c.name}</span>
                  {c.isMandatory && (
                    <Badge variant="outline" className="text-[9px] px-1.5 py-0 h-4 border-rose-300 text-rose-600">
                      {t.mandatory}
                    </Badge>
                  )}
                </div>
                <p className="text-xs text-muted-foreground">{c.reason}</p>
                {c.supportingEvidence && (
                  <p className="text-[10px] text-muted-foreground/75 font-mono">{t.evidence}: {c.supportingEvidence}</p>
                )}
              </div>
              <div className="shrink-0">{getResultBadge(c.result)}</div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  )
}
