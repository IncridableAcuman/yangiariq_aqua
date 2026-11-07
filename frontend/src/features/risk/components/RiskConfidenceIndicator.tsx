import React from 'react'
import { HelpCircle, Info, ShieldCheck, ShieldAlert } from 'lucide-react'
import { RiskConfidence } from '@/entities/risk'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/shared/ui/tooltip'
import { cn } from '@/shared/lib/utils'

interface RiskConfidenceIndicatorProps {
  confidence: RiskConfidence
  className?: string
  showLabel?: boolean
  showTooltip?: boolean
  size?: 'sm' | 'md'
}

export const RiskConfidenceIndicator: React.FC<RiskConfidenceIndicatorProps> = ({
  confidence,
  className,
  showLabel = true,
  showTooltip = true,
  size = 'md',
}) => {
  const meta = {
    HIGH: {
      label: 'High Confidence',
      description: 'Corroborated by fresh real-time sensor telemetry and multi-source observations.',
      color: 'text-emerald-600 dark:text-emerald-400',
      bgColor: 'bg-emerald-500/15 border-emerald-300 dark:border-emerald-800',
      bars: 3,
      icon: ShieldCheck,
    },
    MEDIUM: {
      label: 'Medium Confidence',
      description: 'Calculated using recent telemetry or calibrated predictive algorithms.',
      color: 'text-blue-600 dark:text-blue-400',
      bgColor: 'bg-blue-500/15 border-blue-300 dark:border-blue-800',
      bars: 2,
      icon: Info,
    },
    LOW: {
      label: 'Low Confidence',
      description: 'Telemetry is stale (>6h) or historical samples are limited. Treat as preliminary advisory.',
      color: 'text-amber-600 dark:text-amber-400',
      bgColor: 'bg-amber-500/15 border-amber-300 dark:border-amber-800',
      bars: 1,
      icon: HelpCircle,
    },
    INSUFFICIENT_DATA: {
      label: 'Insufficient Data',
      description: 'Sensors offline or vital readings missing. Does NOT imply parcel is healthy.',
      color: 'text-rose-600 dark:text-rose-400',
      bgColor: 'bg-rose-500/15 border-rose-300 dark:border-rose-800',
      bars: 0,
      icon: ShieldAlert,
    },
  }[confidence] || {
    label: confidence,
    description: 'Data reliability confidence unknown.',
    color: 'text-muted-foreground',
    bgColor: 'bg-muted border-border',
    bars: 0,
    icon: HelpCircle,
  }

  const content = (
    <div
      className={cn(
        'inline-flex items-center gap-1.5 px-2 py-0.5 rounded border text-xs font-medium cursor-default transition-colors',
        meta.bgColor,
        className
      )}
      data-testid={`risk-confidence-${confidence.toLowerCase()}`}
    >
      <div className="flex items-center gap-0.5" aria-hidden="true">
        {[1, 2, 3].map((barIndex) => (
          <div
            key={barIndex}
            className={cn(
              'w-1 rounded-full transition-all',
              size === 'sm' ? 'h-2' : 'h-2.5',
              barIndex <= meta.bars
                ? confidence === 'HIGH'
                  ? 'bg-emerald-600 dark:bg-emerald-400'
                  : confidence === 'MEDIUM'
                  ? 'bg-blue-600 dark:bg-blue-400'
                  : 'bg-amber-600 dark:bg-amber-400'
                : 'bg-muted-foreground/25'
            )}
          />
        ))}
      </div>
      {showLabel && (
        <span className={cn('text-[11px] font-semibold', meta.color)}>
          {meta.label}
        </span>
      )}
    </div>
  )

  if (!showTooltip) {
    return content
  }

  return (
    <TooltipProvider delayDuration={150}>
      <Tooltip>
        <TooltipTrigger asChild>{content}</TooltipTrigger>
        <TooltipContent side="top" className="max-w-xs text-xs space-y-1">
          <p className="font-semibold">{meta.label}</p>
          <p className="text-muted-foreground">{meta.description}</p>
          {confidence === 'INSUFFICIENT_DATA' && (
            <p className="text-rose-500 font-medium text-[11px]">
              ⚠️ Rule: Missing telemetry must never be interpreted as safe or healthy!
            </p>
          )}
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  )
}
