import React from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import {
  ArrowRight,
  Info,
  CheckCircle2,
  AlertTriangle,
  Compass,
} from 'lucide-react'
import { PredictionItem } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'
import { DataSufficiencyBadge } from './DataSufficiencyBadge'

interface PredictionExplanationModalProps {
  prediction: PredictionItem | null
  isOpen: boolean
  onClose: () => void
  lang: SupportedLanguage
  onNavigateToModule?: (module: string, fieldId?: string) => void
}

export const PredictionExplanationModal: React.FC<PredictionExplanationModalProps> = ({
  prediction,
  isOpen,
  onClose,
  lang,
  onNavigateToModule,
}) => {
  if (!prediction) return null

  const t = {
    uz: {
      title: 'Prognoz Asosi va Tushuntirishi (XAI)',
      subtitle: 'Deterministik algoritmik tahlil va dalillar zanjiri',
      whatTitle: 'KUTILAYOTGAN HOLAT (WHAT)',
      whyTitle: 'SABAB VA AGRONOMIK ASOS (WHY)',
      evidenceTitle: 'MIQDORIY DALILLAR (EVIDENCE)',
      assumptionsTitle: 'FARAZLAR VA CHEKLOVLAR (ASSUMPTIONS & LIMITATIONS)',
      actionTitle: 'TAVSIYA ETILGAN KEYINGI AMAL (NEXT STEP)',
      confidence: 'Ishonchlilik darajasi',
      risk: 'Xavf darajasi',
      horizon: 'Prognoz ufqi',
      close: 'Yopish',
      openModule: 'Operatsion bo‘limga o‘tish',
    },
    ru: {
      title: 'Обоснование и объяснение прогноза (XAI)',
      subtitle: 'Детерминированный алгоритмический анализ и цепочка доказательств',
      whatTitle: 'ЧТО ОЖИДАЕТСЯ (WHAT)',
      whyTitle: 'ПРИЧИНА И АГРОНОМИЧЕСКОЕ ОБОСНОВАНИЕ (WHY)',
      evidenceTitle: 'КОЛИЧЕСТВЕННЫЕ ДОКАЗАТЕЛЬСТВА (EVIDENCE)',
      assumptionsTitle: 'ПРЕДПОЛОЖЕНИЯ И ОГРАНИЧЕНИЯ (ASSUMPTIONS & LIMITATIONS)',
      actionTitle: 'РЕКОМЕНДУЕМОЕ ДЕЙСТВИЕ (NEXT STEP)',
      confidence: 'Уровень достоверности',
      risk: 'Уровень риска',
      horizon: 'Горизонт прогноза',
      close: 'Закрыть',
      openModule: 'Перейти в рабочий модуль',
    },
    en: {
      title: 'Forecast Evidence & Explanation (XAI)',
      subtitle: 'Deterministic agronomic reasoning and evidence chain',
      whatTitle: 'PROJECTED OUTCOME (WHAT)',
      whyTitle: 'AGRONOMIC CAUSE (WHY)',
      evidenceTitle: 'QUANTITATIVE EVIDENCE',
      assumptionsTitle: 'ASSUMPTIONS & LIMITATIONS',
      actionTitle: 'RECOMMENDED ACTION (NEXT STEP)',
      confidence: 'Confidence Score',
      risk: 'Risk Level',
      horizon: 'Forecast Horizon',
      close: 'Close',
      openModule: 'Open Operational Workspace',
    },
  }[lang]

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-xl sm:max-w-2xl bg-card border-border shadow-lg">
        <DialogHeader className="border-b border-border/60 pb-3">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-primary/10 text-primary">
              <Info className="h-5 w-5" />
            </div>
            <div>
              <DialogTitle className="text-base sm:text-lg font-bold text-foreground">
                {t.title}
              </DialogTitle>
              <DialogDescription className="text-xs text-muted-foreground">
                {t.subtitle}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        <div className="space-y-4 py-2">
          {/* Header Badges Strip */}
          <div className="flex flex-wrap items-center gap-2 text-xs">
            <Badge variant="outline" className="font-semibold bg-primary/5 text-primary">
              {prediction.predictionType.replace(/_/g, ' ')}
            </Badge>
            <Badge variant="outline" className="font-mono text-[11px]">
              {prediction.horizon.replace(/_/g, ' ')}
            </Badge>
            <DataSufficiencyBadge sufficiency={prediction.dataSufficiency} lang={lang} />
            <span className="text-xs text-muted-foreground ml-auto font-medium">
              {t.confidence}: <strong className="text-foreground">{prediction.confidencePercent}%</strong>
            </span>
          </div>

          {/* WHAT */}
          <div className="p-3.5 rounded-xl bg-muted/40 border border-border/60 space-y-1">
            <h4 className="text-[11px] font-bold tracking-wider text-muted-foreground uppercase flex items-center gap-1.5">
              <CheckCircle2 className="h-3.5 w-3.5 text-primary" />
              <span>{t.whatTitle}</span>
            </h4>
            <p className="text-sm font-semibold text-foreground">
              {prediction.predictedValue !== null
                ? `${prediction.predictedValue} ${prediction.unit || ''} (Kutilayotgan oraliq: ${prediction.lowerBound ?? 0} – ${prediction.upperBound ?? 0} ${prediction.unit || ''})`
                : 'Maʼlumot yetarli emasligi tufayli aniq qiymat hisoblanmadi (UNKNOWN)'}
            </p>
          </div>

          {/* WHY */}
          <div className="p-3.5 rounded-xl bg-purple-50/50 dark:bg-purple-950/20 border border-purple-200/50 dark:border-purple-900/50 space-y-1">
            <h4 className="text-[11px] font-bold tracking-wider text-purple-700 dark:text-purple-300 uppercase flex items-center gap-1.5">
              <Compass className="h-3.5 w-3.5" />
              <span>{t.whyTitle}</span>
            </h4>
            <p className="text-xs text-muted-foreground leading-relaxed">
              {prediction.riskLevel === 'BLOCKED'
                ? 'Gidravlik xavfsizlik cheklovi yoki avariya holati tufayli operatsiya bloklangan.'
                : prediction.riskLevel === 'CRITICAL' || prediction.riskLevel === 'HIGH'
                ? 'Tuproq namligi va suv balansi kritik chegaraga yaqinlashmoqda yoki suv tanqisligi xavfi yuqori.'
                : 'Ekinning suv isteʼmoli meʼyorda, gidravlik va telemetrik barqarorlik taʼminlangan.'}
            </p>
          </div>

          {/* EVIDENCE */}
          <div className="p-3.5 rounded-xl bg-blue-50/50 dark:bg-blue-950/20 border border-blue-200/50 dark:border-blue-900/50 space-y-1">
            <h4 className="text-[11px] font-bold tracking-wider text-blue-700 dark:text-blue-300 uppercase flex items-center gap-1.5">
              <Info className="h-3.5 w-3.5" />
              <span>{t.evidenceTitle}</span>
            </h4>
            <p className="text-xs text-muted-foreground leading-relaxed">
              {prediction.evidence || 'Kuzatuvlar ro‘yxati va datchik telemetriyasi barqaror.'}
            </p>
          </div>

          {/* ASSUMPTIONS & LIMITATIONS */}
          <div className="p-3.5 rounded-xl bg-amber-50/50 dark:bg-amber-950/20 border border-amber-200/50 dark:border-amber-900/50 space-y-1">
            <h4 className="text-[11px] font-bold tracking-wider text-amber-700 dark:text-amber-300 uppercase flex items-center gap-1.5">
              <AlertTriangle className="h-3.5 w-3.5" />
              <span>{t.assumptionsTitle}</span>
            </h4>
            <p className="text-xs text-muted-foreground leading-relaxed">
              {prediction.assumptions || 'Yog‘ingarchilik yoki favqulodda sug‘orish kuzatilmasligi faraz qilingan.'}
            </p>
          </div>

          {/* ACTION */}
          <div className="p-3.5 rounded-xl bg-emerald-50/50 dark:bg-emerald-950/20 border border-emerald-200/50 dark:border-emerald-900/50 space-y-2">
            <h4 className="text-[11px] font-bold tracking-wider text-emerald-700 dark:text-emerald-300 uppercase flex items-center gap-1.5">
              <ArrowRight className="h-3.5 w-3.5" />
              <span>{t.actionTitle}</span>
            </h4>
            <p className="text-xs font-medium text-foreground">
              {prediction.recommendedAction || 'Amaliy chora ko‘rish talab etilmaydi.'}
            </p>
            {prediction.actionTargetModule && onNavigateToModule && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  onClose()
                  onNavigateToModule(prediction.actionTargetModule || 'irrigation', prediction.fieldId)
                }}
                className="text-xs gap-1.5 border-emerald-300 dark:border-emerald-800 text-emerald-700 dark:text-emerald-300 hover:bg-emerald-100 dark:hover:bg-emerald-900/40"
              >
                <span>{t.openModule}</span>
                <ArrowRight className="h-3 w-3" />
              </Button>
            )}
          </div>
        </div>

        <div className="flex justify-end pt-2 border-t border-border/60">
          <Button variant="ghost" size="sm" onClick={onClose} className="text-xs">
            {t.close}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  )
}
