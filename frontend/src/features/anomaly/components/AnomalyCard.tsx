import React from 'react'
import { Card, CardContent } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Anomaly } from '@/entities/anomaly'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { AnomalySeverityBadge } from './AnomalySeverityBadge'
import { AnomalyStatusBadge } from './AnomalyStatusBadge'
import { AnomalyActionType } from './AnomalyActionDialog'
import {
  Clock,
  Eye,
  CheckCircle2,
  XCircle,
  Sparkles,
  Info,
} from 'lucide-react'

interface AnomalyCardProps {
  anomaly: Anomaly
  onAction: (anomaly: Anomaly, action: AnomalyActionType) => void
  onInspect: (anomaly: Anomaly) => void
  lang: SupportedLanguage
  className?: string
}

export const AnomalyCard: React.FC<AnomalyCardProps> = ({
  anomaly,
  onAction,
  onInspect,
  lang,
  className,
}) => {
  const t = translations[lang].anomalies

  const formatTime = (isoString: string) => {
    try {
      const date = new Date(isoString)
      return date.toLocaleTimeString(lang === 'uz' ? 'uz-UZ' : lang === 'ru' ? 'ru-RU' : 'en-US', {
        hour: '2-digit',
        minute: '2-digit',
      })
    } catch {
      return isoString
    }
  }

  const isTerminal = anomaly.status === 'RESOLVED' || anomaly.status === 'DISMISSED'

  return (
    <Card
      className={`border transition-all duration-200 hover:shadow-md bg-card ${
        anomaly.severity === 'CRITICAL' && anomaly.status === 'DETECTED'
          ? 'border-rose-400 dark:border-rose-900 bg-rose-500/[0.02]'
          : anomaly.severity === 'HIGH' && anomaly.status === 'DETECTED'
          ? 'border-amber-400 dark:border-amber-900 bg-amber-500/[0.02]'
          : 'border-border'
      } ${className || ''}`}
    >
      <CardContent className="p-4 space-y-3">
        {/* Header: Badges & Timestamp */}
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="flex flex-wrap items-center gap-1.5">
            <AnomalySeverityBadge severity={anomaly.severity} lang={lang} />
            <AnomalyStatusBadge status={anomaly.status} lang={lang} />
            {anomaly.isSimulated && (
              <Badge
                variant="outline"
                className="bg-indigo-50 text-indigo-700 border-indigo-200 dark:bg-indigo-950/40 dark:text-indigo-300 dark:border-indigo-800 gap-1 text-[10px] py-0 h-5"
              >
                <Sparkles className="w-2.5 h-2.5 text-indigo-500" />
                <span>Simulyatsiya</span>
              </Badge>
            )}
            {anomaly.observationCount > 1 && (
              <Badge variant="secondary" className="text-[10px] py-0 h-5">
                {anomaly.observationCount}×
              </Badge>
            )}
          </div>

          <div className="flex items-center gap-1 text-xs text-muted-foreground whitespace-nowrap">
            <Clock className="w-3.5 h-3.5" />
            <span>{formatTime(anomaly.lastObservedAt)}</span>
          </div>
        </div>

        {/* Title & Description */}
        <div>
          <h3 className="font-semibold text-sm text-foreground">
            {t.types[anomaly.anomalyType]}
          </h3>
          <p className="text-xs text-muted-foreground mt-0.5 line-clamp-2">
            {anomaly.description}
          </p>
        </div>

        {/* Diagnostic Metrics Preview */}
        <div className="flex flex-wrap items-center justify-between gap-2 pt-1 border-t border-border/60 text-xs">
          <div className="flex items-center gap-3">
            {anomaly.detectedValue !== null && anomaly.detectedValue !== undefined && (
              <div>
                <span className="text-[10px] text-muted-foreground block">{t.details.detectedValue}</span>
                <span className="font-bold text-foreground">{anomaly.detectedValue}</span>
              </div>
            )}
            {anomaly.expectedValue !== null && anomaly.expectedValue !== undefined && (
              <div>
                <span className="text-[10px] text-muted-foreground block">{t.details.expectedValue}</span>
                <span className="font-semibold text-muted-foreground">{anomaly.expectedValue}</span>
              </div>
            )}
            <div>
              <span className="text-[10px] text-muted-foreground block">{t.details.confidence}</span>
              <span className="font-bold text-primary">{Math.round(anomaly.confidence * 100)}%</span>
            </div>
          </div>

          <Button
            variant="ghost"
            size="sm"
            className="h-7 text-xs text-primary hover:text-primary hover:bg-primary/10 gap-1"
            onClick={() => onInspect(anomaly)}
          >
            <Info className="w-3.5 h-3.5" />
            <span>{t.actions.details}</span>
          </Button>
        </div>

        {/* Action Controls */}
        {!isTerminal && (
          <div className="flex flex-wrap items-center justify-end gap-1.5 pt-2 border-t border-border/40">
            {anomaly.status === 'DETECTED' && (
              <Button
                variant="outline"
                size="sm"
                className="h-7 text-xs gap-1 border-blue-300 text-blue-700 hover:bg-blue-50 dark:hover:bg-blue-950"
                onClick={() => onAction(anomaly, 'acknowledge')}
              >
                <CheckCircle2 className="w-3 h-3 text-blue-600" />
                <span>{t.actions.acknowledge}</span>
              </Button>
            )}

            {(anomaly.status === 'DETECTED' || anomaly.status === 'ACKNOWLEDGED') && (
              <Button
                variant="outline"
                size="sm"
                className="h-7 text-xs gap-1 border-purple-300 text-purple-700 hover:bg-purple-50 dark:hover:bg-purple-950"
                onClick={() => onAction(anomaly, 'investigate')}
              >
                <Eye className="w-3 h-3 text-purple-600" />
                <span>{t.actions.investigate}</span>
              </Button>
            )}

            <Button
              variant="outline"
              size="sm"
              className="h-7 text-xs gap-1 border-emerald-300 text-emerald-700 hover:bg-emerald-50 dark:hover:bg-emerald-950"
              onClick={() => onAction(anomaly, 'resolve')}
            >
              <CheckCircle2 className="w-3 h-3 text-emerald-600" />
              <span>{t.actions.resolve}</span>
            </Button>

            <Button
              variant="outline"
              size="sm"
              className="h-7 text-xs gap-1 border-slate-300 text-slate-700 hover:bg-slate-50 dark:hover:bg-slate-900"
              onClick={() => onAction(anomaly, 'dismiss')}
            >
              <XCircle className="w-3 h-3 text-slate-500" />
              <span>{t.actions.dismiss}</span>
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
