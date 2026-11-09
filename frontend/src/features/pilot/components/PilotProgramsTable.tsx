import React from 'react'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Badge,
  Button,
} from '@/shared/ui'
import { Eye, ShieldCheck, PlayCircle, Calendar, MapPin, Building } from 'lucide-react'
import { PilotProgram, PilotStatus } from '@/entities/pilot'
import { translations, SupportedLang } from '@/shared/i18n'

interface PilotProgramsTableProps {
  programs: PilotProgram[]
  selectedPilotId: string | null
  onSelectPilot: (pilot: PilotProgram) => void
  onViewDetails: (pilot: PilotProgram) => void
  onAssessReadiness: (pilot: PilotProgram) => void
  onActivatePilot: (pilot: PilotProgram) => void
  canManage: boolean
  lang: SupportedLang
}

export const PilotProgramsTable: React.FC<PilotProgramsTableProps> = ({
  programs,
  selectedPilotId,
  onSelectPilot,
  onViewDetails,
  onAssessReadiness,
  onActivatePilot,
  canManage,
  lang,
}) => {
  const t = translations[lang].pilot

  const getStatusBadge = (status: PilotStatus) => {
    switch (status) {
      case 'ACTIVE':
        return <Badge className="bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 border-emerald-500/30">{t.statusActive}</Badge>
      case 'READY':
        return <Badge className="bg-sky-500/15 text-sky-700 dark:text-sky-300 border-sky-500/30">{t.statusReady}</Badge>
      case 'APPROVED':
        return <Badge className="bg-blue-500/15 text-blue-700 dark:text-blue-300 border-blue-500/30">{t.statusApproved}</Badge>
      case 'DRAFT':
        return <Badge className="bg-amber-500/15 text-amber-700 dark:text-amber-300 border-amber-500/30">{t.statusDraft}</Badge>
      case 'PLANNED':
        return <Badge className="bg-indigo-500/15 text-indigo-700 dark:text-indigo-300 border-indigo-500/30">{t.statusPlanned}</Badge>
      case 'PAUSED':
        return <Badge className="bg-orange-500/15 text-orange-700 dark:text-orange-300 border-orange-500/30">{t.statusPaused}</Badge>
      case 'COMPLETED':
        return <Badge className="bg-purple-500/15 text-purple-700 dark:text-purple-300 border-purple-500/30">{t.statusCompleted}</Badge>
      case 'CANCELLED':
        return <Badge className="bg-rose-500/15 text-rose-700 dark:text-rose-300 border-rose-500/30">{t.statusCancelled}</Badge>
    }
  }

  if (programs.length === 0) {
    return (
      <div className="p-8 text-center text-muted-foreground border border-dashed rounded-lg bg-card/40">
        {t.noPilotsFound}
      </div>
    )
  }

  return (
    <div className="rounded-lg border border-border/70 overflow-hidden bg-card/60 backdrop-blur-sm">
      <Table>
        <TableHeader className="bg-muted/40">
          <TableRow>
            <TableHead>{t.code}</TableHead>
            <TableHead>{t.pilotName}</TableHead>
            <TableHead>{t.organization}</TableHead>
            <TableHead>{t.scopeAndDates}</TableHead>
            <TableHead>{t.status}</TableHead>
            <TableHead className="text-right">{t.actions}</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {programs.map((pilot) => {
            const isSelected = pilot.id === selectedPilotId
            return (
              <TableRow
                key={pilot.id}
                onClick={() => onSelectPilot(pilot)}
                className={`cursor-pointer transition-colors ${
                  isSelected ? 'bg-primary/10 border-l-4 border-l-primary' : 'hover:bg-muted/40'
                }`}
              >
                <TableCell className="font-mono text-xs font-semibold text-primary">
                  {pilot.code}
                </TableCell>
                <TableCell>
                  <div className="font-medium text-foreground text-sm">{pilot.name}</div>
                  <div className="text-xs text-muted-foreground line-clamp-1">{pilot.description || t.noDescription}</div>
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  <div className="flex items-center gap-1">
                    <Building className="h-3 w-3 text-muted-foreground/70" />
                    <span>{pilot.organization}</span>
                  </div>
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  <div className="flex items-center gap-1 text-[11px] mb-0.5">
                    <MapPin className="h-3 w-3" />
                    <span>{pilot.geographicScope}</span>
                  </div>
                  <div className="flex items-center gap-1 text-[10px] text-muted-foreground/80">
                    <Calendar className="h-3 w-3" />
                    <span>{pilot.startDate} → {pilot.endDate}</span>
                  </div>
                </TableCell>
                <TableCell>{getStatusBadge(pilot.status)}</TableCell>
                <TableCell className="text-right" onClick={(e) => e.stopPropagation()}>
                  <div className="flex items-center justify-end gap-1.5">
                    <Button
                      size="sm"
                      variant="ghost"
                      className="h-7 px-2 text-xs"
                      onClick={() => onViewDetails(pilot)}
                      title={t.viewDetails}
                    >
                      <Eye className="h-3.5 w-3.5" />
                    </Button>
                    {canManage && (
                      <>
                        <Button
                          size="sm"
                          variant="outline"
                          className="h-7 px-2 text-xs flex items-center gap-1"
                          onClick={() => onAssessReadiness(pilot)}
                          title={t.assessReadiness}
                        >
                          <ShieldCheck className="h-3.5 w-3.5 text-primary" />
                          <span className="hidden sm:inline">{t.readiness}</span>
                        </Button>
                        {(pilot.status === 'READY' || pilot.status === 'APPROVED' || pilot.status === 'PAUSED') && (
                          <Button
                            size="sm"
                            variant="default"
                            className="h-7 px-2 text-xs flex items-center gap-1 bg-emerald-600 hover:bg-emerald-700 text-white"
                            onClick={() => onActivatePilot(pilot)}
                            title={t.activatePilot}
                          >
                            <PlayCircle className="h-3.5 w-3.5" />
                            <span className="hidden sm:inline">{t.activate}</span>
                          </Button>
                        )}
                      </>
                    )}
                  </div>
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}
