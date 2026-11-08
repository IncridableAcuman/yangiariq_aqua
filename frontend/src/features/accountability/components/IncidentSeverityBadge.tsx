import React from 'react'
import { IncidentSeverity } from '@/entities/accountability'
import { AlertCircle, AlertTriangle, Info, ShieldAlert } from 'lucide-react'

interface Props {
  severity: IncidentSeverity
  className?: string
}

export const IncidentSeverityBadge: React.FC<Props> = ({ severity, className = '' }) => {
  switch (severity) {
    case 'CRITICAL':
      return (
        <span
          className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/20 ${className}`}
          title="Critical Severity: 4-Hour Investigation Target (Hydraulic/System priority)"
        >
          <ShieldAlert className="w-3.5 h-3.5 text-rose-500 animate-pulse" />
          <span>OʻTA SHOSHILINCH (CRITICAL)</span>
        </span>
      )
    case 'HIGH':
      return (
        <span
          className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/20 ${className}`}
          title="High Severity: 24-Hour Review Target"
        >
          <AlertTriangle className="w-3.5 h-3.5 text-amber-500" />
          <span>YUQORI (HIGH)</span>
        </span>
      )
    case 'MEDIUM':
      return (
        <span
          className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-500/10 text-yellow-700 dark:text-yellow-400 border border-yellow-500/20 ${className}`}
          title="Medium Severity: 72-Hour Target"
        >
          <AlertCircle className="w-3.5 h-3.5 text-yellow-500" />
          <span>OʻRTACHA (MEDIUM)</span>
        </span>
      )
    case 'LOW':
      return (
        <span
          className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-500/10 text-blue-600 dark:text-blue-400 border border-blue-500/20 ${className}`}
          title="Low Severity: 7-Day Target"
        >
          <Info className="w-3.5 h-3.5 text-blue-500" />
          <span>PAST (LOW)</span>
        </span>
      )
    case 'INFORMATIONAL':
    default:
      return (
        <span
          className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-500/10 text-slate-600 dark:text-slate-400 border border-slate-500/20 ${className}`}
          title="Informational Severity: No mandatory SLA deadline"
        >
          <Info className="w-3.5 h-3.5 text-slate-400" />
          <span>AXBOROT (INFO)</span>
        </span>
      )
  }
}
