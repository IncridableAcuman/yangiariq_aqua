import React, { useState } from 'react'
import {
  Sparkles,
  Filter,
  AlertCircle,
  Plus,
  HelpCircle,
  FileCheck,
} from 'lucide-react'
import {
  ImpactAssessment,
} from '@/entities/performance'
import { EvidenceClassificationBadge } from './EvidenceClassificationBadge'
import { AttributionLevelBadge } from './AttributionLevelBadge'

interface Props {
  assessments: ImpactAssessment[]
  onCreateClick?: () => void
  onReviewClick?: (assessment: ImpactAssessment) => void
}

export const ImpactAssessmentWorkspace: React.FC<Props> = ({
  assessments,
  onCreateClick,
  onReviewClick,
}) => {
  const [filterType, setFilterType] = useState<string>('ALL')
  const [filterAttribution, setFilterAttribution] = useState<string>('ALL')

  const filtered = assessments.filter((a) => {
    const matchesType = filterType === 'ALL' || a.interventionType === filterType
    const matchesAttr = filterAttribution === 'ALL' || a.attributionLevel === filterAttribution
    return matchesType && matchesAttr
  })

  return (
    <div className="space-y-4">
      {/* Top action header */}
      <div className="p-5 rounded-xl bg-card border border-border shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <h3 className="font-semibold text-foreground flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-primary" />
            Amaliy Taʼsir va Natijadorlikni Baholash Markazi (Outcome & Impact Assessment)
          </h3>
          <p className="text-xs text-muted-foreground mt-0.5">
            Operatsion harakatlar (Output) emas, faqat haqiqiy empirik oʻzgarishlar (Outcome) va sababiy taʼsir (Impact)
          </p>
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <button
            onClick={onCreateClick}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-colors shadow-sm"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Yangi Taʼsir Baholash</span>
          </button>
        </div>
      </div>

      {/* Filter toolbar */}
      <div className="flex flex-wrap items-center gap-2 text-xs">
        <span className="text-muted-foreground font-medium flex items-center gap-1">
          <Filter className="w-3.5 h-3.5" /> Filtrlash:
        </span>
        <select
          value={filterType}
          onChange={(e) => setFilterType(e.target.value)}
          className="rounded-lg border border-border bg-card px-2.5 py-1 text-foreground"
        >
          <option value="ALL">Barcha intervensiyalar</option>
          <option value="CANAL_MAINTENANCE">Kanal taʼmiri & Oʻzan betonlash</option>
          <option value="DISPATCH_SCHEDULE">Dispecherlik jadvalini optimallash</option>
          <option value="FIELD_IRRIGATION_PLAN">Dala aniq sugʻorish rejasi</option>
          <option value="CORRECTIVE_ACTION">Tuzatish chorasi</option>
        </select>

        <select
          value={filterAttribution}
          onChange={(e) => setFilterAttribution(e.target.value)}
          className="rounded-lg border border-border bg-card px-2.5 py-1 text-foreground"
        >
          <option value="ALL">Barcha atributsiya darajalari</option>
          <option value="DIRECTLY_VERIFIED">Bevosita Tasdiqlangan (Directly Verified)</option>
          <option value="STRONGLY_ASSOCIATED">Kuchli Bogʻliqlik (Strongly Associated)</option>
          <option value="ASSOCIATED">Bogʻliq (Associated)</option>
          <option value="INCONCLUSIVE">Noaniq (Inconclusive)</option>
        </select>
      </div>

      {/* List of Assessments */}
      <div className="grid grid-cols-1 gap-3">
        {filtered.map((item) => {
          const isFavorable = item.changeDirection === 'FAVORABLE'
          return (
            <div
              key={item.id}
              className="p-4 rounded-xl bg-card border border-border hover:border-primary/40 transition-all shadow-sm space-y-3"
            >
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-2 border-b border-border/50 pb-3">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs text-primary font-semibold">{item.assessmentNumber}</span>
                    <span className="text-xs px-2 py-0.5 rounded-full bg-secondary text-secondary-foreground font-medium">
                      {item.interventionType}
                    </span>
                    <AttributionLevelBadge level={item.attributionLevel} size="sm" />
                    <EvidenceClassificationBadge classification={item.evidenceClassification} size="sm" />
                  </div>
                  <h4 className="text-sm font-semibold text-foreground">{item.title}</h4>
                  <p className="text-xs text-muted-foreground font-mono">
                    Intervensiya: {item.interventionReference} • {item.scopeName}
                  </p>
                </div>

                <div className="flex items-center gap-2 shrink-0">
                  <div className="text-right">
                    <span
                      className={`text-sm font-bold ${
                        isFavorable ? 'text-emerald-500' : 'text-rose-500'
                      }`}
                    >
                      {item.percentageChange > 0 ? `+${item.percentageChange}%` : `${item.percentageChange}%`}
                    </span>
                    <p className="text-[10px] text-muted-foreground">
                      Farq: {item.absoluteChange} {item.measurementUnit}
                    </p>
                  </div>

                  {item.reviewStatus === 'VERIFIED_OUTCOME' ? (
                    <span className="inline-flex items-center gap-1 text-[11px] font-medium text-emerald-600 bg-emerald-500/10 px-2.5 py-1 rounded-lg border border-emerald-500/30">
                      <FileCheck className="w-3.5 h-3.5" />
                      Tasdiqlangan
                    </span>
                  ) : (
                    <button
                      onClick={() => onReviewClick?.(item)}
                      className="px-2.5 py-1 text-xs font-medium rounded-lg bg-secondary text-secondary-foreground hover:bg-secondary/80 transition-colors"
                    >
                      Tekshirish
                    </button>
                  )}
                </div>
              </div>

              {/* Metric Comparison row */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs bg-muted/30 p-2.5 rounded-lg border border-border/40">
                <div>
                  <span className="text-muted-foreground text-[10px]">Tarixiy Baza:</span>
                  <p className="font-semibold text-foreground">
                    {item.baselineValue} {item.measurementUnit}
                  </p>
                  <p className="text-[10px] text-zinc-400 truncate">{item.baselineQuality}</p>
                </div>
                <div>
                  <span className="text-muted-foreground text-[10px]">Intervensiyadan Keyin:</span>
                  <p className="font-semibold text-foreground">
                    {item.postInterventionValue} {item.measurementUnit}
                  </p>
                  <p className="text-[10px] text-zinc-400">Oʻlchov yakuni: {item.evaluationPeriodEnd.slice(0, 10)}</p>
                </div>
                <div>
                  <span className="text-muted-foreground text-[10px]">Metodologiya:</span>
                  <p className="font-medium text-foreground text-[11px] truncate" title={item.methodologyNotes}>
                    {item.methodologyNotes}
                  </p>
                </div>
                <div>
                  <span className="text-muted-foreground text-[10px]">Auditor / Tekshiruvchi:</span>
                  <p className="font-medium text-foreground text-[11px] truncate">
                    {item.reviewerName || 'Tekshiruv kutilmoqda'}
                  </p>
                </div>
              </div>

              {/* Confounders / Limitations warnings */}
              {(item.confoundingFactors || item.dataLimitations) && (
                <div className="text-[11px] text-muted-foreground space-y-1 pt-1">
                  {item.confoundingFactors && (
                    <p className="flex items-center gap-1.5">
                      <AlertCircle className="w-3 h-3 text-amber-500 shrink-0" />
                      <span>
                        <strong className="text-foreground">Tashqi omillar (Confounders):</strong> {item.confoundingFactors}
                      </span>
                    </p>
                  )}
                  {item.dataLimitations && (
                    <p className="flex items-center gap-1.5">
                      <HelpCircle className="w-3 h-3 text-blue-500 shrink-0" />
                      <span>
                        <strong className="text-foreground">Cheklovlar (Limitations):</strong> {item.dataLimitations}
                      </span>
                    </p>
                  )}
                </div>
              )}
            </div>
          )
        })}

        {filtered.length === 0 && (
          <div className="text-center py-10 bg-card rounded-xl border border-border text-muted-foreground">
            Tanlangan filtrlarga mos taʼsir baholash yozuvlari topilmadi
          </div>
        )}
      </div>
    </div>
  )
}
