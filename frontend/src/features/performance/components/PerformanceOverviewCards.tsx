import React from 'react'
import {
  Droplets,
  TrendingUp,
  Percent,
  CheckCircle2,
  Scale,
  AlertOctagon,
  Cpu,
  Sparkles,
} from 'lucide-react'
import { PerformanceOverview } from '@/entities/performance'
import { EvidenceClassificationBadge } from './EvidenceClassificationBadge'

interface Props {
  overview: PerformanceOverview
  onSelectMetric?: (metricKey: string) => void
}

export const PerformanceOverviewCards: React.FC<Props> = ({ overview, onSelectMetric }) => {
  const cards = [
    {
      id: 'delivery',
      title: 'Yetkazib berish rejasi ijrosi',
      value: `${overview.deliveryFulfillmentPct.toFixed(1)}%`,
      subtitle: `Jami: ${overview.totalWaterDeliveredM3.toLocaleString()} m³`,
      evidence: overview.deliveryFulfillmentEvidence,
      icon: TrendingUp,
      statusColor: overview.deliveryFulfillmentPct >= 90 ? 'text-emerald-500' : 'text-amber-500',
      bgGlow: 'hover:border-emerald-500/40',
    },
    {
      id: 'conveyance',
      title: 'Magistral kanal yoʻqotishi',
      value: `${overview.averageConveyanceLossPct.toFixed(1)}%`,
      subtitle: `Filtrlash: ${overview.totalLossVolumeM3.toLocaleString()} m³`,
      evidence: overview.conveyanceLossEvidence,
      icon: Droplets,
      statusColor: overview.averageConveyanceLossPct <= 8.5 ? 'text-emerald-500' : 'text-rose-500',
      bgGlow: 'hover:border-blue-500/40',
    },
    {
      id: 'compliance',
      title: 'Kvota limiti intizomi',
      value: `${overview.quotaCompliancePct.toFixed(1)}%`,
      subtitle: 'Tuman boʻyicha fermer va WUA lar',
      evidence: overview.quotaComplianceEvidence,
      icon: Percent,
      statusColor: overview.quotaCompliancePct >= 90 ? 'text-emerald-500' : 'text-amber-500',
      bgGlow: 'hover:border-purple-500/40',
    },
    {
      id: 'equity',
      title: 'Taqsimot tengligi (CV)',
      value: overview.distributionEquityCv.toFixed(2),
      subtitle: `${overview.distributionEquityLevel} (Bosh va etak mutanosibligi)`,
      evidence: overview.distributionEquityEvidence,
      icon: Scale,
      statusColor: overview.distributionEquityCv <= 0.18 ? 'text-emerald-500' : 'text-amber-500',
      bgGlow: 'hover:border-indigo-500/40',
    },
    {
      id: 'accountability',
      title: 'Mustaqil tekshirilgan yechimlar',
      value: `${overview.verifiedResolutionRatePct.toFixed(1)}%`,
      subtitle: `${overview.verifiedResolvedIncidents} ta hal etildi / Qaytalanish: ${overview.incidentRecurrenceRatePct.toFixed(1)}%`,
      evidence: overview.accountabilityEvidence,
      icon: CheckCircle2,
      statusColor: 'text-emerald-500',
      bgGlow: 'hover:border-emerald-500/40',
    },
    {
      id: 'overdue',
      title: 'Kechikkan hodisalar (SLA)',
      value: `${overview.overdueIncidents} ta`,
      subtitle: `Jami ochiq hodisalar: ${overview.totalIncidents} ta`,
      evidence: overview.accountabilityEvidence,
      icon: AlertOctagon,
      statusColor: overview.overdueIncidents === 0 ? 'text-emerald-500' : 'text-rose-500',
      bgGlow: 'hover:border-rose-500/40',
    },
    {
      id: 'dataQuality',
      title: 'Datchiklar empirik qamrovi',
      value: `${overview.sensorObservedCoveragePct.toFixed(1)}%`,
      subtitle: `${overview.activeSensorsCount} ta faol telemetriya postlari`,
      evidence: overview.dataQualityEvidence,
      icon: Cpu,
      statusColor: overview.sensorObservedCoveragePct >= 90 ? 'text-emerald-500' : 'text-amber-500',
      bgGlow: 'hover:border-cyan-500/40',
    },
    {
      id: 'conservation',
      title: 'Tasdiqlangan suv tejamkorligi',
      value: `${overview.verifiedConservationSavingsM3.toLocaleString()} m³`,
      subtitle: 'Muhandislik va agrotexnik tejamkorlik',
      evidence: overview.conservationEvidence,
      icon: Sparkles,
      statusColor: 'text-emerald-500',
      bgGlow: 'hover:border-teal-500/40',
    },
  ]

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {cards.map((card) => {
        const Icon = card.icon
        return (
          <div
            key={card.id}
            onClick={() => onSelectMetric?.(card.id)}
            className={`p-4 rounded-xl bg-card border border-border shadow-sm transition-all duration-200 cursor-pointer ${card.bgGlow} flex flex-col justify-between space-y-3`}
          >
            <div className="flex items-start justify-between">
              <div className="p-2 rounded-lg bg-muted text-muted-foreground">
                <Icon className="w-5 h-5" />
              </div>
              <EvidenceClassificationBadge classification={card.evidence} size="sm" />
            </div>

            <div>
              <p className="text-xs text-muted-foreground font-medium">{card.title}</p>
              <h3 className={`text-2xl font-bold mt-1 tracking-tight ${card.statusColor}`}>{card.value}</h3>
              <p className="text-[11px] text-muted-foreground mt-1 truncate" title={card.subtitle}>
                {card.subtitle}
              </p>
            </div>
          </div>
        )
      })}
    </div>
  )
}
