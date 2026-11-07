import React from 'react'
import { AlertCircle, AlertTriangle, CheckCircle2, HelpCircle, ShieldAlert } from 'lucide-react'
import { RiskSeverity } from '@/entities/risk'
import { Badge } from '@/shared/ui/badge'
import { cn } from '@/shared/lib/utils'

interface RiskSeverityBadgeProps {
  severity: RiskSeverity
  className?: string
  showIcon?: boolean
  size?: 'sm' | 'md' | 'lg'
}

export const RiskSeverityBadge: React.FC<RiskSeverityBadgeProps> = ({
  severity,
  className,
  showIcon = true,
  size = 'md',
}) => {
  const config = {
    CRITICAL: {
      label: 'CRITICAL',
      labelUz: 'OʻTA XAVFLI',
      labelRu: 'КРИТИЧЕСКИЙ',
      bg: 'bg-rose-500/15 text-rose-700 dark:text-rose-400 border-rose-300 dark:border-rose-900',
      icon: ShieldAlert,
    },
    HIGH: {
      label: 'HIGH',
      labelUz: 'YUQORI XAVF',
      labelRu: 'ВЫСОКИЙ',
      bg: 'bg-amber-500/15 text-amber-800 dark:text-amber-400 border-amber-300 dark:border-amber-900',
      icon: AlertTriangle,
    },
    MEDIUM: {
      label: 'MEDIUM',
      labelUz: 'OʻRTA XAVF',
      labelRu: 'СРЕДНИЙ',
      bg: 'bg-yellow-500/15 text-yellow-800 dark:text-yellow-400 border-yellow-300 dark:border-yellow-900',
      icon: AlertCircle,
    },
    LOW: {
      label: 'LOW',
      labelUz: 'PAST XAVF',
      labelRu: 'НИЗКИЙ',
      bg: 'bg-emerald-500/15 text-emerald-700 dark:text-emerald-400 border-emerald-300 dark:border-emerald-900',
      icon: CheckCircle2,
    },
    UNKNOWN: {
      label: 'UNKNOWN',
      labelUz: 'NOMAʻLUM',
      labelRu: 'НЕИЗВЕСТНО',
      bg: 'bg-slate-500/15 text-slate-700 dark:text-slate-400 border-slate-300 dark:border-slate-800',
      icon: HelpCircle,
    },
  }[severity] || {
    label: severity,
    bg: 'bg-muted text-muted-foreground border-border',
    icon: HelpCircle,
  }

  const Icon = config.icon

  const sizeClasses = {
    sm: 'text-[10px] px-1.5 py-0.5 gap-1 font-semibold',
    md: 'text-xs px-2 py-0.5 gap-1.5 font-semibold',
    lg: 'text-sm px-3 py-1 gap-2 font-bold',
  }[size]

  return (
    <Badge
      variant="outline"
      className={cn(
        'inline-flex items-center rounded-md border tracking-wide transition-colors',
        config.bg,
        sizeClasses,
        className
      )}
      data-testid={`risk-severity-badge-${severity.toLowerCase()}`}
    >
      {showIcon && <Icon className={size === 'sm' ? 'w-3 h-3 shrink-0' : size === 'lg' ? 'w-4 h-4 shrink-0' : 'w-3.5 h-3.5 shrink-0'} />}
      <span>{config.label}</span>
    </Badge>
  )
}
