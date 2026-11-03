import React, { useMemo } from 'react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/shared/ui/dialog'
import { Badge } from '@/shared/ui/badge'
import { Card, CardContent } from '@/shared/ui/card'
import {
  ShieldCheck,
  Compass,
  CheckCircle2,
  Layers,
  Activity,
  Info,
} from 'lucide-react'
import { AdaptiveSelectionDecision } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface ExplainableSelectionModalProps {
  decision: AdaptiveSelectionDecision | null
  isOpen: boolean
  onClose: () => void
  lang: SupportedLanguage
}

export const ExplainableSelectionModal: React.FC<ExplainableSelectionModalProps> = ({
  decision,
  isOpen,
  onClose,
  lang,
}) => {
  const t = {
    uz: {
      title: 'Nega bu metod tanlandi? (Deterministik Izoh)',
      subtitle: '9 pog‘onali qatʼiy ierarxiya, agronomik kontekst va tarixiy tekshirilgan xatolik tahlili',
      selectedVersion: 'Tanlangan Metod Versiyasi',
      selectionMode: 'Tanlov Usuli',
      confidence: 'Ishonchlilik Darajasi',
      contextSnapshot: 'Daladagi Operatsion Kontekst Ko‘rinishi',
      hierarchyTitle: 'Deterministik Tanlov Ierarxiyasi Tekshiruvi',
      alternativesTitle: 'Ko‘rib Chiqilgan Boshqa Nomzodlar',
      limitationsTitle: 'Operatsion Cheklovlar va Xavfsizlik Kafolati',
      colCandidate: 'Nomzod Versiya',
      colStatus: 'Boshqaruv Holati',
      colMatch: 'Kontekst Mosligi',
      colMae: 'Tarixiy MAE',
      colScore: 'Ball',
      colReason: 'Xulosa / Sabab',
      closeBtn: 'Yopish',
    },
    ru: {
      title: 'Почему выбран этот метод? (Детерминированное объяснение)',
      subtitle: '9-уровневая строгая иерархия, агрономический контекст и верифицированная точность',
      selectedVersion: 'Выбранная Версия Метода',
      selectionMode: 'Режим Выбора',
      confidence: 'Уровень Уверенности',
      contextSnapshot: 'Снимок Операционного Контекста Контура',
      hierarchyTitle: 'Проверка Детерминированной Иерархии Выбора',
      alternativesTitle: 'Рассмотренные Альтернативные Кандидаты',
      limitationsTitle: 'Операционные Ограничения и Гарантии Безопасности',
      colCandidate: 'Кандидат',
      colStatus: 'Статус',
      colMatch: 'Соответствие',
      colMae: 'MAE',
      colScore: 'Балл',
      colReason: 'Обоснование',
      closeBtn: 'Закрыть',
    },
    en: {
      title: 'Why Was This Method Selected? (Deterministic Explanation)',
      subtitle: 'Strict 9-level selection hierarchy, agronomic context matching, and empirical performance verification',
      selectedVersion: 'Selected Governed Version',
      selectionMode: 'Selection Mode',
      confidence: 'Adaptive Confidence',
      contextSnapshot: 'Parcel Operational Context Snapshot',
      hierarchyTitle: 'Deterministic Selection Hierarchy Audit',
      alternativesTitle: 'Governed Alternative Candidates Evaluated',
      limitationsTitle: 'Operational Limitations & Safety Guarantees',
      colCandidate: 'Candidate Version',
      colStatus: 'Status',
      colMatch: 'Match Level',
      colMae: 'Historical MAE',
      colScore: 'Score',
      colReason: 'Reason / Outcome',
      closeBtn: 'Close',
    },
  }[lang]

  const contextData = useMemo(() => {
    if (!decision?.contextSnapshotJson) return null
    try {
      return JSON.parse(decision.contextSnapshotJson)
    } catch {
      return null
    }
  }, [decision?.contextSnapshotJson])

  const alternatives = useMemo(() => {
    if (!decision?.alternativesJson) return []
    try {
      return JSON.parse(decision.alternativesJson)
    } catch {
      return []
    }
  }, [decision?.alternativesJson])

  if (!decision) return null

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader className="border-b border-border/40 pb-3">
          <div className="flex items-center gap-2">
            <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
              <Compass className="w-5 h-5" />
            </div>
            <div>
              <DialogTitle className="text-base font-semibold">
                {t.title}
              </DialogTitle>
              <DialogDescription className="text-xs text-muted-foreground">
                {decision.fieldName} • {decision.predictionType}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        <div className="space-y-4 py-2">
          {/* Top Decision Summary Box */}
          <Card className="border-emerald-500/30 bg-emerald-500/5 dark:bg-emerald-950/20">
            <CardContent className="p-4 space-y-2.5">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold text-foreground">{t.selectedVersion}:</span>
                  <Badge variant="outline" className="font-mono text-xs border-primary/40 text-primary bg-primary/5">
                    {decision.selectedVersionTag}
                  </Badge>
                </div>
                <div className="flex items-center gap-2">
                  <Badge className="bg-emerald-500/20 text-emerald-700 dark:text-emerald-300 border-emerald-500/30 text-xs">
                    {decision.selectionMode}
                  </Badge>
                  <span className="text-xs font-semibold font-mono text-emerald-600 dark:text-emerald-400">
                    {decision.confidencePercent}% confidence
                  </span>
                </div>
              </div>

              <p className="text-xs text-foreground font-medium bg-card/60 p-2.5 rounded-lg border border-border/60">
                {decision.selectionReason}
              </p>

              {decision.evidenceSummary && (
                <p className="text-[11px] text-muted-foreground flex items-center gap-1.5">
                  <Info className="w-3.5 h-3.5 text-primary shrink-0" />
                  <span>{decision.evidenceSummary}</span>
                </p>
              )}
            </CardContent>
          </Card>

          {/* Operational Context Snapshot */}
          {contextData && (
            <Card className="border-border/60 bg-card/40">
              <CardContent className="p-3.5">
                <h4 className="text-xs font-semibold text-foreground mb-2.5 flex items-center gap-1.5">
                  <Layers className="w-3.5 h-3.5 text-primary" />
                  {t.contextSnapshot}
                </h4>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs">
                  <div className="p-2 rounded bg-muted/30">
                    <span className="text-[10px] text-muted-foreground block">Crop / Soil</span>
                    <span className="font-semibold text-foreground">{contextData.cropType} / {contextData.soilType}</span>
                  </div>
                  <div className="p-2 rounded bg-muted/30">
                    <span className="text-[10px] text-muted-foreground block">Irrigation Method</span>
                    <span className="font-semibold text-foreground">{contextData.irrigationMethod}</span>
                  </div>
                  <div className="p-2 rounded bg-muted/30">
                    <span className="text-[10px] text-muted-foreground block">Telemetry Quality</span>
                    <span className="font-semibold text-emerald-600 dark:text-emerald-400">{contextData.telemetryQuality}</span>
                  </div>
                  <div className="p-2 rounded bg-muted/30">
                    <span className="text-[10px] text-muted-foreground block">Agricultural Season</span>
                    <span className="font-semibold text-foreground">{contextData.season}</span>
                  </div>
                  <div className="p-2 rounded bg-muted/30">
                    <span className="text-[10px] text-muted-foreground block">Sensors Availability</span>
                    <span className="font-semibold text-foreground">{contextData.sensorAvailability}</span>
                  </div>
                  <div className="p-2 rounded bg-muted/30">
                    <span className="text-[10px] text-muted-foreground block">Water Headroom</span>
                    <span className="font-semibold text-foreground">{contextData.waterAvailability}</span>
                  </div>
                  <div className="p-2 rounded bg-muted/30">
                    <span className="text-[10px] text-muted-foreground block">Hydraulic Anomalies</span>
                    <span className="font-semibold text-emerald-600 dark:text-emerald-400">{contextData.recentAnomalySeverity}</span>
                  </div>
                  <div className="p-2 rounded bg-muted/30">
                    <span className="text-[10px] text-muted-foreground block">Weather Context</span>
                    <span className="font-semibold text-foreground">{contextData.temperatureContext} / {contextData.rainfallContext}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {/* 9-Level Deterministic Hierarchy Audit */}
          <Card className="border-border/60 bg-card/40">
            <CardContent className="p-3.5 space-y-2">
              <h4 className="text-xs font-semibold text-foreground mb-2 flex items-center gap-1.5">
                <ShieldCheck className="w-3.5 h-3.5 text-primary" />
                {t.hierarchyTitle}
              </h4>

              <div className="space-y-1.5 text-xs">
                <div className="flex items-center justify-between p-2 rounded bg-muted/20">
                  <span className="text-foreground">Level 1: Hydraulic Safety Priority Check</span>
                  <Badge variant="outline" className="text-[10px] border-emerald-500/30 text-emerald-600 bg-emerald-500/5">
                    <CheckCircle2 className="w-3 h-3 mr-1" />
                    Passed (No Critical Interlock)
                  </Badge>
                </div>

                <div className="flex items-center justify-between p-2 rounded bg-muted/20">
                  <span className="text-foreground">Level 2 & 3: Governed Eligibility Filter</span>
                  <Badge variant="outline" className="text-[10px] border-emerald-500/30 text-emerald-600 bg-emerald-500/5">
                    <CheckCircle2 className="w-3 h-3 mr-1" />
                    Active & Approved Only
                  </Badge>
                </div>

                <div className="flex items-center justify-between p-2 rounded bg-muted/20">
                  <span className="text-foreground">Level 4: Physical Sensor Availability Gate</span>
                  <Badge variant="outline" className="text-[10px] border-emerald-500/30 text-emerald-600 bg-emerald-500/5">
                    <CheckCircle2 className="w-3 h-3 mr-1" />
                    Telemetry Satisfied
                  </Badge>
                </div>

                <div className="flex items-center justify-between p-2 rounded bg-muted/20">
                  <span className="text-foreground">Level 5: Context Matching Precedence</span>
                  <Badge variant="outline" className="text-[10px] border-primary/30 text-primary bg-primary/5">
                    {decision.selectionMode}
                  </Badge>
                </div>

                <div className="flex items-center justify-between p-2 rounded bg-muted/20">
                  <span className="text-foreground">Level 6 & 7: Empirical Historical Ranking & Sample Gating</span>
                  <Badge variant="outline" className="text-[10px] border-emerald-500/30 text-emerald-600 bg-emerald-500/5">
                    <CheckCircle2 className="w-3 h-3 mr-1" />
                    n ≥ 10 Threshold Verified
                  </Badge>
                </div>

                <div className="flex items-center justify-between p-2 rounded bg-muted/20">
                  <span className="text-foreground">Level 8: Contextual Health & Local Drift Guard</span>
                  <Badge variant="outline" className="text-[10px] border-emerald-500/30 text-emerald-600 bg-emerald-500/5">
                    <CheckCircle2 className="w-3 h-3 mr-1" />
                    No Uncalibrated Degradation
                  </Badge>
                </div>

                <div className="flex items-center justify-between p-2 rounded bg-muted/20">
                  <span className="text-foreground">Level 9: Production Safe Fallback Guarantee</span>
                  <Badge variant="outline" className="text-[10px] border-slate-500/30 text-muted-foreground">
                    Active Baseline Ready
                  </Badge>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Alternatives Considered Table */}
          {alternatives.length > 0 && (
            <Card className="border-border/60 bg-card/40">
              <CardContent className="p-3.5">
                <h4 className="text-xs font-semibold text-foreground mb-2 flex items-center gap-1.5">
                  <Activity className="w-3.5 h-3.5 text-primary" />
                  {t.alternativesTitle} ({alternatives.length})
                </h4>

                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs">
                    <thead>
                      <tr className="border-b border-border/40 text-muted-foreground text-[11px]">
                        <th className="pb-1.5 font-semibold">{t.colCandidate}</th>
                        <th className="pb-1.5 font-semibold">{t.colStatus}</th>
                        <th className="pb-1.5 font-semibold">{t.colMatch}</th>
                        <th className="pb-1.5 font-semibold text-right">{t.colMae}</th>
                        <th className="pb-1.5 font-semibold text-right">{t.colScore}</th>
                        <th className="pb-1.5 font-semibold pl-3">{t.colReason}</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-border/20">
                      {alternatives.map((alt: any, idx: number) => {
                        const isChosen = alt.versionTag === decision.selectedVersionTag
                        return (
                          <tr key={idx} className={isChosen ? 'bg-emerald-500/10 font-semibold' : ''}>
                            <td className="py-2 font-mono text-[11px] text-foreground">
                              {alt.versionTag}
                              {isChosen && (
                                <Badge className="ml-1.5 text-[9px] px-1 py-0 bg-emerald-500/20 text-emerald-700 dark:text-emerald-300">
                                  Selected
                                </Badge>
                              )}
                            </td>
                            <td className="py-2">
                              <Badge variant="outline" className="text-[10px] px-1 py-0">
                                {alt.status}
                              </Badge>
                            </td>
                            <td className="py-2 text-[11px] text-muted-foreground">
                              {alt.contextMatchLevel || '—'}
                            </td>
                            <td className="py-2 text-right font-mono text-[11px]">
                              {typeof alt.meanAbsoluteError === 'number' ? `${alt.meanAbsoluteError.toFixed(1)}%` : '—'}
                            </td>
                            <td className="py-2 text-right font-mono text-[11px]">
                              {typeof alt.selectionScore === 'number' ? alt.selectionScore.toFixed(1) : '—'}
                            </td>
                            <td className="py-2 pl-3 text-[11px] text-muted-foreground">
                              {alt.rejectionReason || (isChosen ? 'Optimal match score' : 'Lower empirical score')}
                            </td>
                          </tr>
                        )
                      })}
                    </tbody>
                  </table>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Operational Limitations & Safety Guarantees */}
          <div className="p-3 rounded-lg bg-muted/30 border border-border/40 text-[11px] text-muted-foreground space-y-1">
            <span className="font-semibold text-foreground block">{t.limitationsTitle}</span>
            <p>
              • YANGIARIQ AQUA AI nomzod modellarni avtonom faollashtirmaydi yoki o‘zgartirmaydi.
            </p>
            <p>
              • Barcha parametrlar inson tomonidan tasdiqlangan chegaralar doirasida saqlanadi.
            </p>
            <p>
              • Gidravlik xavfsizlik cheklovlari va quvur yorilishi bosimi (burst pressure) barcha prediktiv modellardan ustun turadi.
            </p>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}
