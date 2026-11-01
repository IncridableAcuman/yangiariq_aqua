import React, { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/table'
import { Recommendation } from '@/entities/recommendation'
import { RecommendationPriorityBadge } from './RecommendationPriorityBadge'
import { RecommendationStatusBadge } from './RecommendationStatusBadge'
import { DismissRecommendationDialog } from './DismissRecommendationDialog'
import { SupportedLanguage, translations } from '@/shared/i18n'
import {
  AlertTriangle,
  CheckCircle2,
  Cpu,
  Info,
  ShieldAlert,
  Sparkles,
  XCircle,
} from 'lucide-react'

interface RecommendationDetailsModalProps {
  recommendation: Recommendation | null
  open: boolean
  onOpenChange: (open: boolean) => void
  lang: SupportedLanguage
  onAccept?: (id: string) => Promise<void>
  onDismiss?: (id: string, reason: string) => Promise<void>
  onComplete?: (id: string) => Promise<void>
}

export const RecommendationDetailsModal: React.FC<RecommendationDetailsModalProps> = ({
  recommendation,
  open,
  onOpenChange,
  lang,
  onAccept,
  onDismiss,
  onComplete,
}) => {
  const [isDismissOpen, setIsDismissOpen] = useState(false)
  const [isProcessing, setIsProcessing] = useState(false)

  if (!recommendation) return null

  const t = translations[lang].recommendations || {}
  const actionLabels = t.actions || {
    accept: 'Accept',
    dismiss: 'Dismiss',
    complete: 'Mark Completed',
    doNotTitle: 'CRITICAL DO NOT ACTIONS:',
    evidenceTitle: 'Corroborating Telemetry & Physical Evidence:',
    limitationsTitle: 'Limitations & Agronomic Disclaimer:',
  }
  const common = translations[lang].common

  const isPipeBurst = recommendation.recommendationType === 'INSPECT_PIPE_BURST'
  const confidencePercent = Math.round(recommendation.confidence * 100)
  const isTerminal =
    recommendation.status === 'DISMISSED' ||
    recommendation.status === 'COMPLETED' ||
    recommendation.status === 'EXPIRED'

  const doNotActions = recommendation.actions.filter((a) => a.isNegativeAction)
  const positiveActions = recommendation.actions.filter((a) => !a.isNegativeAction)

  const handleAccept = async () => {
    if (!onAccept) return
    try {
      setIsProcessing(true)
      await onAccept(recommendation.id)
    } finally {
      setIsProcessing(false)
    }
  }

  const handleComplete = async () => {
    if (!onComplete) return
    try {
      setIsProcessing(true)
      await onComplete(recommendation.id)
    } finally {
      setIsProcessing(false)
    }
  }

  const handleDismissConfirm = async (reason: string) => {
    if (!onDismiss) return
    try {
      setIsProcessing(true)
      await onDismiss(recommendation.id, reason)
      onOpenChange(false)
    } finally {
      setIsProcessing(false)
    }
  }

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
          <DialogHeader className="border-b pb-4">
            <div className="flex flex-wrap items-center gap-2 mb-2">
              <RecommendationPriorityBadge priority={recommendation.priority} lang={lang} />
              <RecommendationStatusBadge status={recommendation.status} lang={lang} />
              <Badge variant="outline" className="gap-1 font-mono text-xs">
                {recommendation.source === 'HYBRID_AI' ? (
                  <>
                    <Sparkles className="w-3 h-3 text-purple-600" />
                    <span>Hybrid AI</span>
                  </>
                ) : (
                  <>
                    <Cpu className="w-3 h-3 text-blue-600" />
                    <span>Deterministic</span>
                  </>
                )}
              </Badge>
              {recommendation.isSimulated && (
                <Badge variant="secondary" className="bg-amber-500/10 text-amber-600 border-amber-300 text-xs">
                  Simulated Telemetry
                </Badge>
              )}
            </div>

            <DialogTitle className="text-xl font-bold flex items-center gap-2">
              {isPipeBurst && <ShieldAlert className="w-5 h-5 text-rose-600 animate-pulse shrink-0" />}
              {recommendation.title}
            </DialogTitle>
            <DialogDescription className="text-sm pt-1">
              {recommendation.summary}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-6 py-4">
            {/* Hydraulic Pipe Burst Alert Banner */}
            {isPipeBurst && (
              <div className="rounded-lg border-2 border-rose-500 bg-rose-500/10 p-4 text-rose-800 dark:text-rose-200 space-y-1">
                <div className="flex items-center gap-2 font-semibold text-rose-700 dark:text-rose-300">
                  <ShieldAlert className="w-5 h-5 animate-pulse" />
                  <span>
                    {lang === 'uz'
                      ? 'Gidravlik xavfsizlik: Sugʻorish qatʼiyan toʻxtatildi!'
                      : lang === 'ru'
                      ? 'Гидравлическая безопасность: Подача воды категорически заблокирована!'
                      : 'Hydraulic Safety Interlock: Water delivery strictly suppressed!'}
                  </span>
                </div>
                <p className="text-xs">
                  {lang === 'uz'
                    ? 'Quvur yorilishi yoki bosim keskin tushishi aniqlandi. Suv yoʻqotish va tuproq eroziyasini oldini olish uchun zudlik bilan tarmoqni tekshiring.'
                    : lang === 'ru'
                    ? 'Обнаружен возможный прорыв трубопровода или падение давления. Немедленно проверьте сеть.'
                    : 'Suspected pipe burst or critical pressure drop detected. Inspect line immediately before resuming irrigation.'}
                </p>
              </div>
            )}

            {/* Confidence Calibration Box */}
            <div className="rounded-lg bg-muted/50 p-4 border flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div>
                <h4 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  {t.confidence?.title || 'Multi-Factor Calibrated Confidence'}
                </h4>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {lang === 'uz'
                    ? 'Faktlar kuchi (40%), maʼlumot sifati (25%), kontekst toʻliqligi (20%), model bahosi (15%)'
                    : lang === 'ru'
                    ? 'Сила доказательств (40%), качество данных (25%), полнота контекста (20%), оценка модели (15%)'
                    : 'Evidence strength (40%), data freshness (25%), context completeness (20%), model factor (15%)'}
                </p>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-24 bg-secondary rounded-full h-2.5 overflow-hidden">
                  <div
                    className={`h-2.5 rounded-full ${
                      confidencePercent >= 80 ? 'bg-emerald-500' : confidencePercent >= 60 ? 'bg-amber-500' : 'bg-rose-500'
                    }`}
                    style={{ width: `${confidencePercent}%` }}
                  />
                </div>
                <span className="font-bold text-sm">{confidencePercent}%</span>
              </div>
            </div>

            {/* Detailed Explanation */}
            <div>
              <h4 className="text-sm font-semibold mb-1.5 flex items-center gap-1.5">
                <Info className="w-4 h-4 text-primary" />
                <span>
                  {lang === 'uz' ? 'Agronomik asos va tahlil' : lang === 'ru' ? 'Агрономическое обоснование' : 'Agronomic Reasoning'}
                </span>
              </h4>
              <p className="text-sm text-foreground/90 leading-relaxed bg-card p-3 rounded-md border">
                {recommendation.explanation}
              </p>
            </div>

            {/* Recommended Action Steps */}
            {positiveActions.length > 0 && (
              <div>
                <h4 className="text-sm font-semibold mb-2 flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                  <span>
                    {lang === 'uz' ? 'Tavsiya etilgan amallar' : lang === 'ru' ? 'Рекомендуемые действия' : 'Recommended Actions'}
                  </span>
                </h4>
                <div className="space-y-2">
                  {positiveActions.map((action, idx) => (
                    <div
                      key={action.id || idx}
                      className="flex items-start gap-3 p-2.5 rounded-md border bg-card text-sm"
                    >
                      <span className="flex items-center justify-center w-6 h-6 rounded-full bg-primary/10 text-primary font-bold text-xs shrink-0 mt-0.5">
                        {action.actionOrder || idx + 1}
                      </span>
                      <div className="flex-1">
                        <p className="text-foreground">{action.actionText}</p>
                      </div>
                      <Badge variant="outline" className="text-xs uppercase shrink-0 font-mono">
                        {action.urgency}
                      </Badge>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* DO NOT Actions Alert Box */}
            {doNotActions.length > 0 && (
              <div className="rounded-lg border-2 border-amber-500/40 bg-amber-500/10 p-4 space-y-2">
                <h4 className="text-xs font-bold uppercase tracking-wider text-amber-800 dark:text-amber-300 flex items-center gap-1.5">
                  <AlertTriangle className="w-4 h-4 text-amber-600 dark:text-amber-400" />
                  <span>{actionLabels.doNotTitle}</span>
                </h4>
                <ul className="space-y-1.5 text-sm text-amber-900 dark:text-amber-200">
                  {doNotActions.map((action, idx) => (
                    <li key={action.id || idx} className="flex items-start gap-2">
                      <XCircle className="w-4 h-4 text-rose-500 shrink-0 mt-0.5" />
                      <span>{action.actionText}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Corroborating Physical Evidence Table */}
            {recommendation.evidence && recommendation.evidence.length > 0 && (
              <div>
                <h4 className="text-sm font-semibold mb-2">
                  {actionLabels.evidenceTitle}
                </h4>
                <div className="rounded-md border overflow-hidden">
                  <Table>
                    <TableHeader className="bg-muted/50">
                      <TableRow>
                        <TableHead className="text-xs">
                          {lang === 'uz' ? 'Metrika' : lang === 'ru' ? 'Метрика' : 'Metric'}
                        </TableHead>
                        <TableHead className="text-xs">
                          {lang === 'uz' ? 'Oʻlchangan qiymat' : lang === 'ru' ? 'Наблюдаемое' : 'Observed'}
                        </TableHead>
                        <TableHead className="text-xs">
                          {lang === 'uz' ? 'Bazaviy norma' : lang === 'ru' ? 'Базовое' : 'Baseline'}
                        </TableHead>
                        <TableHead className="text-xs">
                          {lang === 'uz' ? 'Xulosa' : lang === 'ru' ? 'Резюме' : 'Summary'}
                        </TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {recommendation.evidence.map((ev, idx) => (
                        <TableRow key={ev.id || idx}>
                          <TableCell className="text-xs font-mono font-medium">
                            {ev.metric}
                          </TableCell>
                          <TableCell className="text-xs font-semibold text-primary">
                            {ev.observedValue != null ? `${ev.observedValue} ${ev.unit || ''}` : '—'}
                          </TableCell>
                          <TableCell className="text-xs text-muted-foreground">
                            {ev.baselineValue != null ? `${ev.baselineValue} ${ev.unit || ''}` : '—'}
                          </TableCell>
                          <TableCell className="text-xs text-muted-foreground">
                            {ev.summary}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              </div>
            )}

            {/* Technical Limitations Disclaimer Box */}
            <div className="rounded-lg bg-muted/40 p-3 border text-xs text-muted-foreground space-y-1">
              <h5 className="font-semibold text-foreground flex items-center gap-1">
                <Info className="w-3.5 h-3.5" />
                <span>{actionLabels.limitationsTitle}</span>
              </h5>
              <p>
                {recommendation.limitations ||
                  (lang === 'uz'
                    ? 'Ushbu tavsiya Yangiariq tumani regional agronomik andozalari asosida avtomatlashtirilgan. On-field amaliyotidan oldin agronom tasdigʻi tavsiya etiladi.'
                    : lang === 'ru'
                    ? 'Данная рекомендация сформирована на базе региональных агрономических паттернов Янгиарыкского района. Рекомендуется верификация агрономом.'
                    : 'This recommendation is generated using regional agronomic models for Yangiariq. Field verification is advised.')}
              </p>
            </div>
          </div>

          <DialogFooter className="flex flex-wrap items-center justify-between gap-2 border-t pt-3">
            <div className="flex items-center gap-2">
              {!isTerminal && recommendation.status !== 'ACCEPTED' && onAccept && (
                <Button
                  variant="default"
                  className="bg-emerald-600 hover:bg-emerald-700 text-white"
                  onClick={handleAccept}
                  disabled={isProcessing}
                >
                  {actionLabels.accept}
                </Button>
              )}

              {recommendation.status === 'ACCEPTED' && onComplete && (
                <Button
                  variant="default"
                  className="bg-teal-600 hover:bg-teal-700 text-white font-semibold"
                  onClick={handleComplete}
                  disabled={isProcessing}
                >
                  {actionLabels.complete}
                </Button>
              )}

              {!isTerminal && onDismiss && (
                <Button
                  variant="outline"
                  className="text-rose-600 border-rose-300 hover:bg-rose-50 dark:hover:bg-rose-950/20"
                  onClick={() => setIsDismissOpen(true)}
                  disabled={isProcessing}
                >
                  {actionLabels.dismiss}
                </Button>
              )}
            </div>

            <Button variant="ghost" onClick={() => onOpenChange(false)}>
              {common.close}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <DismissRecommendationDialog
        open={isDismissOpen}
        onOpenChange={setIsDismissOpen}
        onConfirm={handleDismissConfirm}
        isLoading={isProcessing}
        lang={lang}
      />
    </>
  )
}
