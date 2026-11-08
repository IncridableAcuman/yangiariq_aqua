import React from 'react'
import { EvidenceConfidence } from '@/entities/accountability'
import { CheckCheck, Check, AlertCircle, Bot, HelpCircle } from 'lucide-react'

interface Props {
  confidence: EvidenceConfidence
  className?: string
}

export const AccountabilityEvidenceBadge: React.FC<Props> = ({ confidence, className = '' }) => {
  switch (confidence) {
    case 'VERIFIED':
      return (
        <span
          className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-semibold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20 ${className}`}
          title="Tasdiqlangan dalil (Fizik koʻrik yoki kalibrlangan oʻlchov bilan toʻliq tekshirilgan)"
        >
          <CheckCheck className="w-3 h-3 text-emerald-500" />
          <span>TASDIQLANGAN (VERIFIED)</span>
        </span>
      )
    case 'PARTIALLY_VERIFIED':
      return (
        <span
          className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-medium bg-teal-500/10 text-teal-600 dark:text-teal-400 border border-teal-500/20 ${className}`}
          title="Qisman tasdiqlangan (Telemetriya yoki hisob-kitob korrelyatsiyasi mavjud, toʻliq fizik koʻrik kutilmoqda)"
        >
          <Check className="w-3 h-3 text-teal-500" />
          <span>QISMAN TASDIQLANGAN</span>
        </span>
      )
    case 'UNVERIFIED':
      return (
        <span
          className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-medium bg-amber-500/10 text-amber-700 dark:text-amber-400 border border-amber-500/20 ${className}`}
          title="Tasdiqlanmagan dalil (Dastlabki hisobot yoki xabar, dalillar tekshirilishi kerak)"
        >
          <AlertCircle className="w-3 h-3 text-amber-500" />
          <span>TASDIQLANMAGAN</span>
        </span>
      )
    case 'SIMULATED':
      return (
        <span
          className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-medium bg-violet-500/10 text-violet-600 dark:text-violet-400 border border-violet-500/20 ${className}`}
          title="Simulyatsiya qilingan (Gidravlik yoki ML modeli bashorati - haqiqiy oʻlchov deb hisoblanmaydi)"
        >
          <Bot className="w-3 h-3 text-violet-500" />
          <span>SIMULYATSIYA</span>
        </span>
      )
    case 'INSUFFICIENT_DATA':
    default:
      return (
        <span
          className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-medium bg-slate-500/10 text-slate-600 dark:text-slate-400 border border-slate-500/20 ${className}`}
          title="Yetarli maʼlumot yoʻq (Datchik yoki jurnal yozuvlari yetishmaydi)"
        >
          <HelpCircle className="w-3 h-3 text-slate-400" />
          <span>YETARLI EMAS</span>
        </span>
      )
  }
}
