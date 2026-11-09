import React from 'react'
import { Card, CardContent } from '@/shared/ui'
import { Rocket, ShieldCheck, CheckCircle2, TrendingUp, Layers, Users } from 'lucide-react'
import { PilotProgram, PilotOperationalMonitoring } from '@/entities/pilot'
import { translations, SupportedLang } from '@/shared/i18n'

interface PilotProgramsSummaryCardsProps {
  programs: PilotProgram[]
  monitoring?: PilotOperationalMonitoring | null
  lang: SupportedLang
}

export const PilotProgramsSummaryCards: React.FC<PilotProgramsSummaryCardsProps> = ({
  programs,
  monitoring,
  lang,
}) => {
  const t = translations[lang].pilot

  const totalPrograms = programs.length
  const activePrograms = programs.filter((p) => p.status === 'ACTIVE').length
  const readyOrApproved = programs.filter((p) => p.status === 'READY' || p.status === 'APPROVED').length
  const draftPrograms = programs.filter((p) => p.status === 'DRAFT' || p.status === 'PLANNED').length

  const verifiedGt = monitoring?.verifiedGroundTruths ?? 0
  const totalPreds = monitoring?.totalPredictionValidations ?? 0

  return (
    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
      <Card className="border border-border/60 bg-card/60 backdrop-blur-sm">
        <CardContent className="p-3.5">
          <div className="flex items-center justify-between text-muted-foreground mb-1">
            <span className="text-xs font-medium">{t.totalPrograms}</span>
            <Rocket className="h-4 w-4 text-primary" />
          </div>
          <div className="text-2xl font-bold text-foreground">{totalPrograms}</div>
          <span className="text-[10px] text-muted-foreground">{t.registeredPilots}</span>
        </CardContent>
      </Card>

      <Card className="border border-border/60 bg-card/60 backdrop-blur-sm">
        <CardContent className="p-3.5">
          <div className="flex items-center justify-between text-muted-foreground mb-1">
            <span className="text-xs font-medium">{t.activePilots}</span>
            <CheckCircle2 className="h-4 w-4 text-emerald-500" />
          </div>
          <div className="text-2xl font-bold text-emerald-600 dark:text-emerald-400">{activePrograms}</div>
          <span className="text-[10px] text-muted-foreground">{t.operationalPhase}</span>
        </CardContent>
      </Card>

      <Card className="border border-border/60 bg-card/60 backdrop-blur-sm">
        <CardContent className="p-3.5">
          <div className="flex items-center justify-between text-muted-foreground mb-1">
            <span className="text-xs font-medium">{t.readyApproved}</span>
            <ShieldCheck className="h-4 w-4 text-sky-500" />
          </div>
          <div className="text-2xl font-bold text-sky-600 dark:text-sky-400">{readyOrApproved}</div>
          <span className="text-[10px] text-muted-foreground">{t.preFlightCleared}</span>
        </CardContent>
      </Card>

      <Card className="border border-border/60 bg-card/60 backdrop-blur-sm">
        <CardContent className="p-3.5">
          <div className="flex items-center justify-between text-muted-foreground mb-1">
            <span className="text-xs font-medium">{t.draftPlanning}</span>
            <Layers className="h-4 w-4 text-amber-500" />
          </div>
          <div className="text-2xl font-bold text-amber-600 dark:text-amber-400">{draftPrograms}</div>
          <span className="text-[10px] text-muted-foreground">{t.awaitingApproval}</span>
        </CardContent>
      </Card>

      <Card className="border border-border/60 bg-card/60 backdrop-blur-sm">
        <CardContent className="p-3.5">
          <div className="flex items-center justify-between text-muted-foreground mb-1">
            <span className="text-xs font-medium">{t.verifiedGroundTruth}</span>
            <Users className="h-4 w-4 text-purple-500" />
          </div>
          <div className="text-2xl font-bold text-purple-600 dark:text-purple-400">{verifiedGt}</div>
          <span className="text-[10px] text-muted-foreground">{t.fourEyesVerified}</span>
        </CardContent>
      </Card>

      <Card className="border border-border/60 bg-card/60 backdrop-blur-sm">
        <CardContent className="p-3.5">
          <div className="flex items-center justify-between text-muted-foreground mb-1">
            <span className="text-xs font-medium">{t.modelValidations}</span>
            <TrendingUp className="h-4 w-4 text-blue-500" />
          </div>
          <div className="text-2xl font-bold text-blue-600 dark:text-blue-400">{totalPreds}</div>
          <span className="text-[10px] text-muted-foreground">{t.forecastMatches}</span>
        </CardContent>
      </Card>
    </div>
  )
}
