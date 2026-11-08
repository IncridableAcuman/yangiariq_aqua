import React from 'react'
import { AccountabilityDashboardSummary } from '@/entities/accountability'
import { Card, CardContent } from '@/shared/ui/card'
import {
  AlertOctagon,
  AlertTriangle,
  Clock,
  Flame,
  RotateCcw,
  ShieldAlert,
  ShieldCheck,
} from 'lucide-react'

interface Props {
  summary?: AccountabilityDashboardSummary
  isLoading?: boolean
  onFilterClick?: (status: string) => void
}

export const AccountabilityMetricsCards: React.FC<Props> = ({
  summary,
  isLoading = false,
  onFilterClick,
}) => {
  if (isLoading) {
    return (
      <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-3 animate-pulse">
        {[...Array(7)].map((_, i) => (
          <div key={i} className="h-24 bg-card/60 border border-border/50 rounded-xl" />
        ))}
      </div>
    )
  }

  const cards = [
    {
      id: 'open',
      label: 'Ochiq hodisalar',
      sublabel: 'Dastlabki koʻrib chiqishda',
      value: summary?.openIncidents ?? 0,
      icon: AlertOctagon,
      color: 'text-indigo-500',
      bgColor: 'bg-indigo-500/10',
      borderColor: 'border-indigo-500/20',
      statusFilter: 'OPEN',
    },
    {
      id: 'critical',
      label: 'Kritik xavflar',
      sublabel: 'Gidravlik ustuvorlik',
      value: summary?.criticalIncidents ?? 0,
      icon: ShieldAlert,
      color: 'text-rose-500',
      bgColor: 'bg-rose-500/10',
      borderColor: 'border-rose-500/20',
      badge: '4h SLA',
      statusFilter: 'CRITICAL',
    },
    {
      id: 'investigating',
      label: 'Tekshirilmoqda',
      sublabel: 'Mutaxassis biriktirilgan',
      value: summary?.underInvestigation ?? 0,
      icon: Flame,
      color: 'text-amber-500',
      bgColor: 'bg-amber-500/10',
      borderColor: 'border-amber-500/20',
      statusFilter: 'INVESTIGATING',
    },
    {
      id: 'overdue',
      label: 'Muddat buzilgan',
      sublabel: 'SLA talabi oʻtgan',
      value: summary?.overdueIncidents ?? 0,
      icon: AlertTriangle,
      color: 'text-red-500',
      bgColor: 'bg-red-500/10',
      borderColor: 'border-red-500/20',
      statusFilter: 'OVERDUE',
    },
    {
      id: 'pending_verification',
      label: 'Tekshiruv kutmoqda',
      sublabel: 'Mustaqil baholash zarur',
      value: summary?.pendingVerification ?? 0,
      icon: Clock,
      color: 'text-orange-500',
      bgColor: 'bg-orange-500/10',
      borderColor: 'border-orange-500/20',
      statusFilter: 'PENDING_VERIFICATION',
    },
    {
      id: 'resolved',
      label: 'Hal etilgan',
      sublabel: 'Mustaqil tasdiqlangan',
      value: summary?.resolvedIncidents ?? 0,
      icon: ShieldCheck,
      color: 'text-emerald-500',
      bgColor: 'bg-emerald-500/10',
      borderColor: 'border-emerald-500/20',
      statusFilter: 'RESOLVED',
    },
    {
      id: 'reopened',
      label: 'Qayta ochilgan',
      sublabel: 'Muammo qaytalangan',
      value: summary?.reopenedIncidents ?? 0,
      icon: RotateCcw,
      color: 'text-purple-500',
      bgColor: 'bg-purple-500/10',
      borderColor: 'border-purple-500/20',
      statusFilter: 'REOPENED',
    },
  ]

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-7 gap-3">
      {cards.map((c) => {
        const Icon = c.icon
        return (
          <Card
            key={c.id}
            onClick={() => onFilterClick?.(c.statusFilter)}
            className={`cursor-pointer transition-all duration-200 hover:shadow-md hover:border-primary/40 border ${c.borderColor} bg-card/80 backdrop-blur-sm`}
          >
            <CardContent className="p-3 sm:p-3.5 flex flex-col justify-between h-full">
              <div className="flex items-center justify-between gap-1 mb-2">
                <span className="text-[11px] font-medium text-muted-foreground line-clamp-1">{c.label}</span>
                <div className={`p-1.5 rounded-lg ${c.bgColor} ${c.color} shrink-0`}>
                  <Icon className="w-3.5 h-3.5" />
                </div>
              </div>
              <div className="flex items-baseline justify-between mt-auto">
                <span className="text-xl sm:text-2xl font-bold tracking-tight text-foreground">{c.value}</span>
                {c.badge && (
                  <span className="text-[10px] font-semibold px-1.5 py-0.5 rounded bg-rose-500/10 text-rose-500 border border-rose-500/20">
                    {c.badge}
                  </span>
                )}
              </div>
            </CardContent>
          </Card>
        )
      })}
    </div>
  )
}
