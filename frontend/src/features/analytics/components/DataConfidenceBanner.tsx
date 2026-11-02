import React from 'react'
import { AlertTriangle, CheckCircle2, Info, XCircle } from 'lucide-react'
import { DataConfidenceDto, DataConfidenceLevel } from '@/entities/analytics'
import { SupportedLanguage } from '@/shared/i18n'

interface DataConfidenceBannerProps {
  confidence?: DataConfidenceDto
  lang: SupportedLanguage
}

export const DataConfidenceBanner: React.FC<DataConfidenceBannerProps> = ({ confidence, lang }) => {
  if (!confidence || (!confidence.hasIncompleteData && confidence.overall === 'COMPLETE')) {
    return null
  }

  const t = {
    uz: {
      title: 'Tahliliy maʼlumotlar toʻliqligi va ishonchliligi holati',
      complete: 'Toʻliq (Complete ≥85%)',
      partial: 'Qisman toʻliq (Partial 50-84%)',
      insufficient: 'Yetarli emas (Insufficient <50%)',
      water: 'Suv hisobi:',
      telemetry: 'Telemetriya:',
      verification: 'Natijani tekshirish:',
      warningDefault:
        'Sensor telemetriyasi yoki amallarni tekshirish maʼlumotlari toʻliq emas. Natijalar koʻrsatkichlariga taʼsir qilishi mumkin.',
    },
    ru: {
      title: 'Статус полноты и достоверности аналитических данных',
      complete: 'Полные (Complete ≥85%)',
      partial: 'Частичные (Partial 50-84%)',
      insufficient: 'Недостаточно (Insufficient <50%)',
      water: 'Учет воды:',
      telemetry: 'Телеметрия:',
      verification: 'Верификация:',
      warningDefault:
        'Данные телеметрии или верификации операций неполные. Это может влиять на точность расчетных показателей.',
    },
    en: {
      title: 'Analytical Data Confidence & Completeness Status',
      complete: 'Complete (≥85%)',
      partial: 'Partial (50-84%)',
      insufficient: 'Insufficient (<50%)',
      water: 'Water Accounting:',
      telemetry: 'Telemetry:',
      verification: 'Verification:',
      warningDefault:
        'Sensor telemetry or operation verification data is incomplete. Some performance scores may be impacted.',
    },
  }[lang]

  const getBadge = (level: DataConfidenceLevel) => {
    switch (level) {
      case 'COMPLETE':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300">
            <CheckCircle2 className="w-3 h-3" />
            {t.complete}
          </span>
        )
      case 'PARTIAL':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300">
            <AlertTriangle className="w-3 h-3" />
            {t.partial}
          </span>
        )
      case 'INSUFFICIENT':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-300">
            <XCircle className="w-3 h-3" />
            {t.insufficient}
          </span>
        )
    }
  }

  const isRose = confidence.overall === 'INSUFFICIENT'

  return (
    <div
      className={`rounded-xl p-4 border flex flex-col gap-2 ${
        isRose
          ? 'bg-rose-50/70 dark:bg-rose-950/20 border-rose-200 dark:border-rose-900/50'
          : 'bg-amber-50/70 dark:bg-amber-950/20 border-amber-200 dark:border-amber-900/50'
      }`}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-2.5">
          {isRose ? (
            <XCircle className="w-5 h-5 text-rose-600 dark:text-rose-400 shrink-0 mt-0.5" />
          ) : (
            <AlertTriangle className="w-5 h-5 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
          )}
          <div>
            <h4
              className={`text-sm font-semibold ${
                isRose ? 'text-rose-900 dark:text-rose-200' : 'text-amber-900 dark:text-amber-200'
              }`}
            >
              {t.title}
            </h4>
            <p
              className={`text-xs mt-0.5 ${
                isRose ? 'text-rose-700 dark:text-rose-300' : 'text-amber-700 dark:text-amber-300'
              }`}
            >
              {confidence.warningMessage || t.warningDefault}
            </p>
          </div>
        </div>
        <div>{getBadge(confidence.overall)}</div>
      </div>

      {/* Breakdown tags */}
      <div className="flex flex-wrap items-center gap-3 pt-2 text-xs border-t border-slate-200/60 dark:border-slate-800">
        <div className="flex items-center gap-1.5 text-slate-600 dark:text-slate-400">
          <Info className="w-3.5 h-3.5" />
          <span>{t.water}</span>
          <span className="font-medium text-slate-800 dark:text-slate-200">{confidence.waterConfidence}</span>
        </div>
        <div className="flex items-center gap-1.5 text-slate-600 dark:text-slate-400">
          <span>{t.telemetry}</span>
          <span className="font-medium text-slate-800 dark:text-slate-200">{confidence.telemetryConfidence}</span>
        </div>
        <div className="flex items-center gap-1.5 text-slate-600 dark:text-slate-400">
          <span>{t.verification}</span>
          <span className="font-medium text-slate-800 dark:text-slate-200">{confidence.verificationConfidence}</span>
        </div>
      </div>
    </div>
  )
}
