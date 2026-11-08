import React from 'react'
import { IncidentStatus } from '@/entities/accountability'
import {
  AlertCircle,
  CheckCircle2,
  Clock,
  Eye,
  FileCheck,
  Flame,
  PlayCircle,
  RotateCcw,
  ShieldCheck,
  XCircle,
} from 'lucide-react'

interface Props {
  status: IncidentStatus
  className?: string
}

export const IncidentStatusBadge: React.FC<Props> = ({ status, className = '' }) => {
  switch (status) {
    case 'DETECTED':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-500/10 text-purple-600 dark:text-purple-400 border border-purple-500/20 ${className}`}>
          <Eye className="w-3 h-3 text-purple-500" />
          <span>Aniqlangan (Detected)</span>
        </span>
      )
    case 'UNDER_REVIEW':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-500/10 text-blue-600 dark:text-blue-400 border border-blue-500/20 ${className}`}>
          <Clock className="w-3 h-3 text-blue-500 animate-spin" />
          <span>Koʻrib chiqilmoqda</span>
        </span>
      )
    case 'OPEN':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 border border-indigo-500/20 ${className}`}>
          <AlertCircle className="w-3 h-3 text-indigo-500" />
          <span>Ochiq (Open)</span>
        </span>
      )
    case 'INVESTIGATING':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-700 dark:text-amber-400 border border-amber-500/20 ${className}`}>
          <Flame className="w-3 h-3 text-amber-500" />
          <span>Tekshirilmoqda</span>
        </span>
      )
    case 'ACTION_PROPOSED':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-sky-500/10 text-sky-600 dark:text-sky-400 border border-sky-500/20 ${className}`}>
          <FileCheck className="w-3 h-3 text-sky-500" />
          <span>Chora taklif etilgan</span>
        </span>
      )
    case 'ACTION_APPROVED':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-teal-500/10 text-teal-600 dark:text-teal-400 border border-teal-500/20 ${className}`}>
          <CheckCircle2 className="w-3 h-3 text-teal-500" />
          <span>Chora tasdiqlangan</span>
        </span>
      )
    case 'ACTION_IN_PROGRESS':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-cyan-500/10 text-cyan-600 dark:text-cyan-400 border border-cyan-500/20 ${className}`}>
          <PlayCircle className="w-3 h-3 text-cyan-500" />
          <span>Ijroda</span>
        </span>
      )
    case 'PENDING_VERIFICATION':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-orange-500/10 text-orange-600 dark:text-orange-400 border border-orange-500/20 ${className}`}>
          <Clock className="w-3 h-3 text-orange-500" />
          <span>Tekshiruv kutilmoqda</span>
        </span>
      )
    case 'RESOLVED':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20 ${className}`}>
          <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
          <span>Hal qilindi (Resolved)</span>
        </span>
      )
    case 'DISMISSED':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-500/10 text-slate-600 dark:text-slate-400 border border-slate-500/20 ${className}`}>
          <XCircle className="w-3 h-3 text-slate-400" />
          <span>Rad etilgan (Dismissed)</span>
        </span>
      )
    case 'REOPENED':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/20 ${className}`}>
          <RotateCcw className="w-3 h-3 text-rose-500" />
          <span>Qayta ochilgan</span>
        </span>
      )
    case 'CLOSED':
    default:
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-zinc-500/10 text-zinc-600 dark:text-zinc-400 border border-zinc-500/20 ${className}`}>
          <CheckCircle2 className="w-3 h-3 text-zinc-400" />
          <span>Yopilgan (Closed)</span>
        </span>
      )
  }
}
