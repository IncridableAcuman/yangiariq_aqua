import React from 'react';
import { Card, CardContent } from '@/shared/ui/card';
import { WaterNetworkOverview } from '@/entities/network';
import { Activity, AlertTriangle, Layers, Waves } from 'lucide-react';

interface NetworkOverviewCardsProps {
  overview?: WaterNetworkOverview | null;
  isLoading?: boolean;
}

export const NetworkOverviewCards: React.FC<NetworkOverviewCardsProps> = ({
  overview,
  isLoading = false,
}) => {
  if (isLoading || !overview) {
    return (
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {[1, 2, 3, 4].map((i) => (
          <Card key={i} className="animate-pulse">
            <CardContent className="p-4">
              <div className="h-4 w-24 bg-muted rounded mb-2" />
              <div className="h-8 w-16 bg-muted rounded" />
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  const utilization = overview.overallNetworkUtilizationPercent;

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
      {/* 1. Network Conveyance Capacity */}
      <Card className="border border-border/60 shadow-sm">
        <CardContent className="p-4">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium uppercase tracking-wider">Tizim O'tkazuvchanligi</span>
            <Waves className="h-4 w-4 text-sky-500" />
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-2xl font-bold tracking-tight text-foreground">
              {overview.totalCurrentFlowM3S.toFixed(2)}
            </span>
            <span className="text-sm font-medium text-muted-foreground">
              / {overview.totalConveyanceCapacityM3S.toFixed(2)} m³/s
            </span>
          </div>
          <p className="mt-1 text-xs text-muted-foreground">
            District conveyance capacity utilization: {utilization.toFixed(1)}%
          </p>
        </CardContent>
      </Card>

      {/* 2. Topology Infrastructure */}
      <Card className="border border-border/60 shadow-sm">
        <CardContent className="p-4">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium uppercase tracking-wider">Tarmoq Tuzilishi</span>
            <Layers className="h-4 w-4 text-emerald-500" />
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-2xl font-bold tracking-tight text-foreground">
              {overview.totalEdges}
            </span>
            <span className="text-sm font-medium text-muted-foreground">
              kanallar ({overview.totalNodes} tugun)
            </span>
          </div>
          <p className="mt-1 text-xs text-emerald-600 dark:text-emerald-400">
            {overview.operationalEdges} barqaror faoliyatda
          </p>
        </CardContent>
      </Card>

      {/* 3. Hydraulic Bottlenecks & Warnings */}
      <Card className="border border-border/60 shadow-sm">
        <CardContent className="p-4">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium uppercase tracking-wider">Cheklovlar & To'siqlar</span>
            <AlertTriangle className={`h-4 w-4 ${overview.blockedEdges > 0 ? 'text-rose-500' : 'text-amber-500'}`} />
          </div>
          <div className="mt-2 flex items-baseline gap-2">
            <span className={`text-2xl font-bold tracking-tight ${overview.blockedEdges > 0 ? 'text-rose-600 dark:text-rose-400' : 'text-foreground'}`}>
              {overview.constrainedEdges + overview.blockedEdges}
            </span>
            <span className="text-sm font-medium text-muted-foreground">
              ({overview.blockedEdges} to'siq, {overview.constrainedEdges} cheklov)
            </span>
          </div>
          <p className="mt-1 text-xs text-muted-foreground">
            {overview.recentSurgeRiskWarnings.length > 0
              ? `${overview.recentSurgeRiskWarnings.length} gidravlik to'lqin ogohlantirishi`
              : 'Gidravlik xavfsizlik normada'}
          </p>
        </CardContent>
      </Card>

      {/* 4. Active Dispatch Proposal */}
      <Card className="border border-border/60 shadow-sm">
        <CardContent className="p-4">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium uppercase tracking-wider">Navbatdagi Taqsimot</span>
            <Activity className="h-4 w-4 text-indigo-500" />
          </div>
          <div className="mt-2">
            <span className="text-lg font-bold tracking-tight text-foreground line-clamp-1">
              {overview.latestActiveScheduleTitle || 'Reja yo‘q'}
            </span>
          </div>
          <div className="mt-1 flex items-center gap-2">
            <span className="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold bg-indigo-500/10 text-indigo-600 dark:text-indigo-400">
              {overview.latestActiveScheduleStatus}
            </span>
            <span className="text-xs text-muted-foreground">
              {overview.activeScheduleCount} faol taqsimot
            </span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
