import React from 'react'
import { TelemetryTrend } from '@/entities/intelligence'
import { TrendingDown, TrendingUp, Minus } from 'lucide-react'

interface MoistureTrendIndicatorProps {
  trend?: TelemetryTrend | null
  className?: string
}

export const MoistureTrendIndicator: React.FC<MoistureTrendIndicatorProps> = ({
  trend,
  className = '',
}) => {
  if (!trend) {
    return (
      <div className={`flex items-center gap-1 text-xs text-muted-foreground ${className}`}>
        <Minus className="w-3.5 h-3.5" />
        <span>No trend data</span>
      </div>
    )
  }

  const rate = trend.rateOfChangePerHour != null ? Number(trend.rateOfChangePerHour).toFixed(2) : '0.00'

  switch (trend.direction) {
    case 'DECREASING':
      return (
        <div
          className={`flex items-center gap-1.5 text-xs font-medium text-amber-600 dark:text-amber-400 ${className}`}
          title={`Soil moisture is decreasing at ${rate}% per hour`}
        >
          <TrendingDown className="w-4 h-4 text-amber-500" />
          <span>Drying ({rate}%/h)</span>
        </div>
      )
    case 'INCREASING':
      return (
        <div
          className={`flex items-center gap-1.5 text-xs font-medium text-blue-600 dark:text-blue-400 ${className}`}
          title={`Soil moisture is increasing at ${rate}% per hour`}
        >
          <TrendingUp className="w-4 h-4 text-blue-500" />
          <span>Wetting (+{rate}%/h)</span>
        </div>
      )
    case 'STABLE':
    default:
      return (
        <div
          className={`flex items-center gap-1.5 text-xs font-medium text-slate-600 dark:text-slate-400 ${className}`}
          title="Soil moisture level is stable"
        >
          <Minus className="w-4 h-4 text-slate-500" />
          <span>Stable ({rate}%/h)</span>
        </div>
      )
  }
}
