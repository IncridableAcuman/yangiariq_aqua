import React from 'react';

interface CanalCapacityIndicatorProps {
  currentFlowM3S: number;
  maxCapacityM3S: number;
  utilizationPercent?: number;
  isBlocked?: boolean;
  className?: string;
}

export const CanalCapacityIndicator: React.FC<CanalCapacityIndicatorProps> = ({
  currentFlowM3S,
  maxCapacityM3S,
  utilizationPercent,
  isBlocked = false,
  className = '',
}) => {
  const percent = utilizationPercent !== undefined
    ? utilizationPercent
    : maxCapacityM3S > 0
      ? Math.min(100, Math.round((currentFlowM3S / maxCapacityM3S) * 100))
      : 0;

  const barColor = isBlocked
    ? 'bg-rose-600 dark:bg-rose-500'
    : percent >= 90
      ? 'bg-rose-500'
      : percent >= 70
        ? 'bg-amber-500'
        : 'bg-emerald-500';

  return (
    <div className={`space-y-1.5 ${className}`}>
      <div className="flex items-center justify-between text-xs">
        <span className="text-muted-foreground font-medium">
          {currentFlowM3S.toFixed(2)} / {maxCapacityM3S.toFixed(2)} m³/s
        </span>
        <span
          className={`font-semibold ${
            isBlocked
              ? 'text-rose-600 dark:text-rose-400'
              : percent >= 90
                ? 'text-rose-600 dark:text-rose-400'
                : percent >= 70
                  ? 'text-amber-600 dark:text-amber-400'
                  : 'text-emerald-600 dark:text-emerald-400'
          }`}
        >
          {isBlocked ? 'BLOCKED' : `${percent}%`}
        </span>
      </div>
      <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
        <div
          className={`h-full transition-all duration-300 ${barColor}`}
          style={{ width: `${Math.min(100, percent)}%` }}
        />
      </div>
    </div>
  );
};
