import React from 'react'
import {
  AlertOctagon,
  AlertTriangle,
  Cpu,
  Droplet,
  Layers,
  ShieldAlert,
} from 'lucide-react'
import { FarmRiskOverview } from '@/entities/risk'
import { Card, CardContent } from '@/shared/ui/card'
import { cn } from '@/shared/lib/utils'

interface RiskOverviewCardsProps {
  overview?: FarmRiskOverview | null
  activeFilter?: string | null
  onFilterChange?: (filterKey: string) => void
}

export const RiskOverviewCards: React.FC<RiskOverviewCardsProps> = ({
  overview,
  activeFilter,
  onFilterChange,
}) => {
  if (!overview) {
    return (
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 animate-pulse">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="h-24 rounded-xl bg-muted/60" />
        ))}
      </div>
    )
  }

  const items = [
    {
      key: 'ALL',
      title: 'Assessed Parcels',
      count: overview.totalAssessedFields,
      subtext: `${overview.activeRecommendationsCount} actions active`,
      icon: Layers,
      color: 'text-primary',
      borderColor: 'hover:border-primary/50',
      activeRing: 'ring-2 ring-primary/40',
    },
    {
      key: 'HYDRAULIC',
      title: 'Hydraulic Interlocks',
      count: overview.hydraulicEmergencyCount,
      subtext: overview.hydraulicEmergencyCount > 0 ? 'Strict safety block' : 'Nominal pressure',
      icon: AlertOctagon,
      color: overview.hydraulicEmergencyCount > 0 ? 'text-rose-600' : 'text-muted-foreground',
      bgColor: overview.hydraulicEmergencyCount > 0 ? 'bg-rose-500/10' : undefined,
      borderColor: 'hover:border-rose-400',
      activeRing: 'ring-2 ring-rose-500/40',
    },
    {
      key: 'CRITICAL',
      title: 'Critical Severity',
      count: overview.criticalRiskCount,
      subtext: 'Immediate intervention',
      icon: ShieldAlert,
      color: overview.criticalRiskCount > 0 ? 'text-rose-600' : 'text-muted-foreground',
      bgColor: overview.criticalRiskCount > 0 ? 'bg-rose-500/10' : undefined,
      borderColor: 'hover:border-rose-400',
      activeRing: 'ring-2 ring-rose-500/40',
    },
    {
      key: 'WATER_SHORTAGE',
      title: 'Water Quota Risks',
      count: overview.waterShortageCount,
      subtext: 'Deficit or exhausted',
      icon: Droplet,
      color: overview.waterShortageCount > 0 ? 'text-blue-600' : 'text-muted-foreground',
      bgColor: overview.waterShortageCount > 0 ? 'bg-blue-500/10' : undefined,
      borderColor: 'hover:border-blue-400',
      activeRing: 'ring-2 ring-blue-500/40',
    },
    {
      key: 'SOIL_MOISTURE',
      title: 'Soil Moisture Stress',
      count: overview.severeMoistureRiskCount,
      subtext: 'Wilting / drying trends',
      icon: AlertTriangle,
      color: overview.severeMoistureRiskCount > 0 ? 'text-amber-600' : 'text-muted-foreground',
      bgColor: overview.severeMoistureRiskCount > 0 ? 'bg-amber-500/10' : undefined,
      borderColor: 'hover:border-amber-400',
      activeRing: 'ring-2 ring-amber-500/40',
    },
    {
      key: 'TELEMETRY',
      title: 'Telemetry Gaps',
      count: overview.telemetryIssuesCount,
      subtext: overview.telemetryIssuesCount > 0 ? 'Offline / stale feeds' : 'All probes online',
      icon: Cpu,
      color: overview.telemetryIssuesCount > 0 ? 'text-purple-600' : 'text-muted-foreground',
      bgColor: overview.telemetryIssuesCount > 0 ? 'bg-purple-500/10' : undefined,
      borderColor: 'hover:border-purple-400',
      activeRing: 'ring-2 ring-purple-500/40',
    },
  ]

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3" data-testid="risk-overview-cards">
      {items.map((item) => {
        const Icon = item.icon
        const isSelected = activeFilter === item.key

        return (
          <Card
            key={item.key}
            className={cn(
              'cursor-pointer transition-all duration-200 border bg-card hover:shadow-sm select-none',
              item.bgColor,
              item.borderColor,
              isSelected ? item.activeRing : ''
            )}
            onClick={() => onFilterChange && onFilterChange(item.key)}
          >
            <CardContent className="p-3.5 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider truncate">
                  {item.title}
                </span>
                <Icon className={cn('w-4 h-4 shrink-0', item.color)} />
              </div>

              <div className="flex items-baseline justify-between gap-1">
                <span className={cn('text-2xl font-bold tracking-tight', item.color)}>
                  {item.count}
                </span>
              </div>

              <p className="text-[10px] text-muted-foreground truncate">
                {item.subtext}
              </p>
            </CardContent>
          </Card>
        )
      })}
    </div>
  )
}
