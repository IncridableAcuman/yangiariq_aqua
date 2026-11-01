import React from 'react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Anomaly } from '@/entities/anomaly'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { AnomalySeverityBadge } from './AnomalySeverityBadge'
import { AnomalyStatusBadge } from './AnomalyStatusBadge'
import {
  AlertTriangle,
  Clock,
  Cpu,
  FileText,
  Layers,
  Sparkles,
} from 'lucide-react'

interface AnomalyDetailsModalProps {
  isOpen: boolean
  onClose: () => void
  anomaly: Anomaly | null
  lang: SupportedLanguage
}

export const AnomalyDetailsModal: React.FC<AnomalyDetailsModalProps> = ({
  isOpen,
  onClose,
  anomaly,
  lang,
}) => {
  const t = translations[lang].anomalies

  if (!anomaly) return null

  const formatConfidence = (conf: number) => {
    return `${Math.round(conf * 100)}%`
  }

  const formatDate = (isoString: string | null | undefined) => {
    if (!isoString) return '-'
    try {
      const date = new Date(isoString)
      return date.toLocaleString(lang === 'uz' ? 'uz-UZ' : lang === 'ru' ? 'ru-RU' : 'en-US', {
        dateStyle: 'medium',
        timeStyle: 'short',
      })
    } catch {
      return isoString
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader className="space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <AnomalySeverityBadge severity={anomaly.severity} lang={lang} />
            <AnomalyStatusBadge status={anomaly.status} lang={lang} />
            {anomaly.isSimulated && (
              <Badge
                variant="outline"
                className="bg-indigo-50 text-indigo-700 border-indigo-200 dark:bg-indigo-950/40 dark:text-indigo-300 dark:border-indigo-800 gap-1"
              >
                <Sparkles className="w-3 h-3 text-indigo-500" />
                <span>{t.details.isSimulated}</span>
              </Badge>
            )}
          </div>
          <DialogTitle className="text-lg font-bold text-foreground">
            {t.types[anomaly.anomalyType]}
          </DialogTitle>
          <DialogDescription className="text-xs text-muted-foreground">
            {anomaly.description}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-2 text-xs">
          {/* Diagnostic Metrics Matrix */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 p-3 rounded-lg bg-muted/40 border border-border">
            <div>
              <p className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">
                {t.details.detectedValue}
              </p>
              <p className="text-sm font-bold text-foreground mt-0.5">
                {anomaly.detectedValue !== null && anomaly.detectedValue !== undefined
                  ? anomaly.detectedValue
                  : 'N/A'}
              </p>
            </div>
            <div>
              <p className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">
                {t.details.expectedValue}
              </p>
              <p className="text-sm font-semibold text-foreground mt-0.5">
                {anomaly.expectedValue !== null && anomaly.expectedValue !== undefined
                  ? anomaly.expectedValue
                  : 'N/A'}
              </p>
            </div>
            <div>
              <p className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">
                {t.details.deviation}
              </p>
              <p className="text-sm font-semibold text-amber-600 dark:text-amber-400 mt-0.5">
                {anomaly.deviation !== null && anomaly.deviation !== undefined
                  ? `${anomaly.deviation > 0 ? '+' : ''}${anomaly.deviation}`
                  : 'N/A'}
              </p>
            </div>
            <div>
              <p className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">
                {t.details.confidence}
              </p>
              <div className="flex items-center gap-1.5 mt-0.5">
                <div className="w-16 h-2 rounded-full bg-border overflow-hidden">
                  <div
                    className="h-full bg-primary rounded-full transition-all"
                    style={{ width: `${Math.min(100, Math.max(0, anomaly.confidence * 100))}%` }}
                  />
                </div>
                <span className="text-xs font-bold text-foreground">
                  {formatConfidence(anomaly.confidence)}
                </span>
              </div>
            </div>
          </div>

          {/* Explanation / Root Cause Interpretation */}
          <div className="p-3 rounded-lg bg-card border border-border space-y-1.5">
            <h4 className="font-semibold text-foreground flex items-center gap-1.5">
              <FileText className="w-3.5 h-3.5 text-primary" />
              <span>Texnik va Agrotexnik Tavsif</span>
            </h4>
            <p className="text-muted-foreground leading-relaxed">
              {anomaly.explanation}
            </p>
          </div>

          {/* Evidence Details */}
          <div className="space-y-2">
            <h4 className="font-semibold text-foreground flex items-center gap-1.5">
              <Layers className="w-3.5 h-3.5 text-primary" />
              <span>{t.details.evidence}</span>
            </h4>
            {anomaly.evidence && anomaly.evidence.length > 0 ? (
              <div className="border border-border rounded-lg overflow-hidden divide-y divide-border">
                {anomaly.evidence.map((ev) => (
                  <div key={ev.id} className="p-2.5 bg-card/50 flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                    <div>
                      <p className="font-semibold text-foreground">
                        {ev.metric} <span className="text-[10px] text-muted-foreground font-normal">({ev.evidenceType})</span>
                      </p>
                      {ev.metadata && (
                        <p className="text-[11px] text-muted-foreground mt-0.5">{ev.metadata}</p>
                      )}
                    </div>
                    <div className="flex items-center gap-4 text-right">
                      {ev.observedValue !== null && ev.observedValue !== undefined && (
                        <div>
                          <span className="text-[10px] text-muted-foreground block">{t.details.detectedValue}</span>
                          <span className="font-bold text-foreground">{ev.observedValue}</span>
                        </div>
                      )}
                      <div className="text-[10px] text-muted-foreground whitespace-nowrap">
                        {formatDate(ev.measuredAt)}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground italic">{t.details.noEvidence}</p>
            )}
          </div>

          {/* Operational Context & Audit History */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 p-3 rounded-lg bg-muted/20 border border-border">
            <div className="space-y-1">
              <div className="flex items-center gap-1.5 text-muted-foreground">
                <Clock className="w-3 h-3" />
                <span>{t.details.firstObserved}:</span>
                <span className="text-foreground font-medium">{formatDate(anomaly.firstObservedAt)}</span>
              </div>
              <div className="flex items-center gap-1.5 text-muted-foreground">
                <Clock className="w-3 h-3" />
                <span>{t.details.lastObserved}:</span>
                <span className="text-foreground font-medium">{formatDate(anomaly.lastObservedAt)}</span>
              </div>
              <div className="flex items-center gap-1.5 text-muted-foreground">
                <AlertTriangle className="w-3 h-3" />
                <span>{t.details.observations}:</span>
                <span className="text-foreground font-semibold">{anomaly.observationCount} marta</span>
              </div>
            </div>

            <div className="space-y-1">
              <div className="flex items-center gap-1.5 text-muted-foreground">
                <Cpu className="w-3 h-3" />
                <span>{t.details.source}:</span>
                <Badge variant="outline" className="text-[10px] py-0 px-1.5">
                  {anomaly.source}
                </Badge>
              </div>
              {anomaly.acknowledgedAt && (
                <div className="text-muted-foreground">
                  <span>Tasdiqlangan:</span>{' '}
                  <span className="text-foreground font-medium">{formatDate(anomaly.acknowledgedAt)}</span>
                </div>
              )}
              {anomaly.resolvedAt && (
                <div className="text-muted-foreground">
                  <span>Hal qilingan:</span>{' '}
                  <span className="text-foreground font-medium">{formatDate(anomaly.resolvedAt)}</span>
                </div>
              )}
            </div>
          </div>

          {/* Resolution Notes (if any) */}
          {anomaly.resolutionNotes && (
            <div className="p-3 rounded-lg bg-emerald-500/5 border border-emerald-500/20 space-y-1">
              <p className="font-semibold text-emerald-800 dark:text-emerald-300">
                {t.details.resolutionNotes}:
              </p>
              <p className="text-muted-foreground">{anomaly.resolutionNotes}</p>
            </div>
          )}
        </div>

        <div className="flex justify-end pt-2">
          <Button variant="outline" size="sm" onClick={onClose}>
            {translations[lang].common.close}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  )
}
