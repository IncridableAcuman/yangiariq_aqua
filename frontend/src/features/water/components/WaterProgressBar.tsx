import React from 'react'
import { calculateFillPercent, formatPercentage } from '../lib/waterFormatters'
import { cn } from '@/shared/lib/utils'

interface WaterProgressBarProps {
  consumed: number
  allocated: number
  isOverConsumed?: boolean
  showLabel?: boolean
  className?: string
}

export const WaterProgressBar: React.FC<WaterProgressBarProps> = ({
  consumed,
  allocated,
  isOverConsumed = false,
  showLabel = true,
  className,
}) => {
  const fillPercent = calculateFillPercent(consumed, allocated)
  const actualPercent = allocated > 0 ? (consumed / allocated) * 100 : 0
  const isOver = isOverConsumed || actualPercent > 100

  // Semantic color based on consumption threshold
  let barColor = 'bg-emerald-500 dark:bg-emerald-400'
  if (isOver) {
    barColor = 'bg-rose-500 dark:bg-rose-400'
  } else if (fillPercent >= 80) {
    barColor = 'bg-amber-500 dark:bg-amber-400'
  }

  return (
    <div className={cn('w-full flex flex-col gap-1', className)}>
      {showLabel && (
        <div className="flex justify-between items-center text-xs">
          <span className="text-muted-foreground font-medium">
            {isOver ? 'Over Quota' : 'Quota Used'}
          </span>
          <span
            className={cn(
              'font-semibold',
              isOver ? 'text-rose-600 dark:text-rose-400' : 'text-foreground'
            )}
          >
            {formatPercentage(actualPercent)}
          </span>
        </div>
      )}
      <div
        className="w-full h-2.5 bg-muted rounded-full overflow-hidden relative"
        role="progressbar"
        aria-valuenow={Math.round(actualPercent)}
        aria-valuemin={0}
        aria-valuemax={100}
      >
        <div
          className={cn('h-full transition-all duration-300 rounded-full', barColor)}
          style={{ width: `${fillPercent}%` }}
        />
      </div>
    </div>
  )
}
