import React, { useState } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/ui/tabs';
import { Button } from '@/shared/ui/button';
import {
  NetworkOverviewCards,
  WaterNetworkMap,
  NetworkSegmentDetailsPanel,
  RegionalDemandChart,
  DispatchOptimizationPanel,
  DispatchScheduleTimeline,
  PlannedVsActualDeliveryChart,
  ScenarioComparisonPanel,
  ResilienceScenarioDialog,
} from '@/features/network/components';
import {
  useNetworkOverviewQuery,
  useNetworkTopologyQuery,
  useRegionalDemandQuery,
  useDispatchSchedulesQuery,
} from '@/features/network/queries/useNetworkQueries';
import { WaterNetworkEdge, WaterNetworkNode } from '@/entities/network';
import { Activity, GitMerge, MapPin, RefreshCw, ShieldAlert, Waves } from 'lucide-react';

export const RegionalOperationsPage: React.FC = () => {
  const [selectedEdge, setSelectedEdge] = useState<WaterNetworkEdge | null>(null);
  const [selectedNode, setSelectedNode] = useState<WaterNetworkNode | null>(null);
  const [simulateEdgeId, setSimulateEdgeId] = useState<string | undefined>(undefined);
  const [isSimulateDialogOpen, setIsSimulateDialogOpen] = useState(false);

  const { data: overview, isLoading: isOverviewLoading, refetch: refetchOverview } = useNetworkOverviewQuery();
  const { data: topology, refetch: refetchTopology } = useNetworkTopologyQuery();
  const { data: demandData, isLoading: isDemandLoading, refetch: refetchDemand } = useRegionalDemandQuery();
  const { data: schedules = [], refetch: refetchSchedules } = useDispatchSchedulesQuery();

  const handleRefreshAll = () => {
    refetchOverview();
    refetchTopology();
    refetchDemand();
    refetchSchedules();
  };

  const handleSimulateOutageFromPanel = (edgeId: string) => {
    setSimulateEdgeId(edgeId);
    setIsSimulateDialogOpen(true);
  };

  const activeSchedule = schedules.length > 0 ? schedules[0] : null;

  return (
    <div className="space-y-6 pb-12">
      {/* 1. Header & Live Status */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="inline-flex items-center rounded-md bg-sky-500/10 px-2 py-0.5 text-xs font-semibold text-sky-600 dark:text-sky-400">
              Yangiariq Tumani
            </span>
            <span className="text-xs text-muted-foreground font-medium">Milestone 08.14</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground mt-1 flex items-center gap-2">
            <Waves className="h-6 w-6 text-sky-600" />
            Suv Tarmog'i & Kanal Dispecherlik Markazi
          </h1>
          <p className="text-xs text-muted-foreground mt-0.5 max-w-2xl">
            Tuman darajasidagi magistral kanallar, nasos stansiyalari, SIU suv taqsimoti, ko'p dalali jadval muvofiqlashtiruvi va chidamlilik simulyatsiyasi.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={handleRefreshAll}
            className="text-xs gap-1.5"
          >
            <RefreshCw className="h-3.5 w-3.5" />
            Yangilash
          </Button>
        </div>
      </div>

      {/* 2. Overview Statistics Cards */}
      <NetworkOverviewCards overview={overview} isLoading={isOverviewLoading} />

      {/* 3. Main Navigation Tabs */}
      <Tabs defaultValue="topology" className="space-y-4">
        <TabsList className="grid grid-cols-2 md:grid-cols-4 w-full md:w-auto h-auto p-1 bg-muted/60">
          <TabsTrigger value="topology" className="text-xs gap-1.5 py-2">
            <MapPin className="h-3.5 w-3.5 text-sky-600" />
            Tarmoq Xaritasi & GIS
          </TabsTrigger>
          <TabsTrigger value="dispatch" className="text-xs gap-1.5 py-2">
            <GitMerge className="h-3.5 w-3.5 text-indigo-600" />
            Dispecherlik Optimizatsiyasi
          </TabsTrigger>
          <TabsTrigger value="resilience" className="text-xs gap-1.5 py-2">
            <ShieldAlert className="h-3.5 w-3.5 text-amber-600" />
            Chidamlilik & Stress-Test
          </TabsTrigger>
          <TabsTrigger value="audit" className="text-xs gap-1.5 py-2">
            <Activity className="h-3.5 w-3.5 text-emerald-600" />
            Yetkazish Auditi (Reja/Fakt)
          </TabsTrigger>
        </TabsList>

        {/* Tab 1: Topology & GIS */}
        <TabsContent value="topology" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
            <div className="lg:col-span-2 space-y-4">
              <WaterNetworkMap
                nodes={topology?.nodes}
                edges={topology?.edges}
                selectedEdge={selectedEdge}
                selectedNode={selectedNode}
                onSelectEdge={(edge) => {
                  setSelectedEdge(edge);
                  setSelectedNode(null);
                }}
                onSelectNode={(node) => {
                  setSelectedNode(node);
                  setSelectedEdge(null);
                }}
                height="580px"
              />
            </div>
            <div className="space-y-4">
              <NetworkSegmentDetailsPanel
                selectedEdge={selectedEdge}
                selectedNode={selectedNode}
                onClose={() => {
                  setSelectedEdge(null);
                  setSelectedNode(null);
                }}
                onSimulateOutage={handleSimulateOutageFromPanel}
              />
              <RegionalDemandChart demandData={demandData} isLoading={isDemandLoading} />
            </div>
          </div>
        </TabsContent>

        {/* Tab 2: Dispatch Optimization */}
        <TabsContent value="dispatch" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
            <div className="lg:col-span-1">
              <DispatchOptimizationPanel onSuccess={refetchSchedules} />
            </div>
            <div className="lg:col-span-2">
              <DispatchScheduleTimeline schedule={activeSchedule} onRefresh={refetchSchedules} />
            </div>
          </div>
        </TabsContent>

        {/* Tab 3: Resilience & Stress-Testing */}
        <TabsContent value="resilience" className="space-y-4">
          <ScenarioComparisonPanel />
        </TabsContent>

        {/* Tab 4: Delivery Variance Audit */}
        <TabsContent value="audit" className="space-y-4">
          <PlannedVsActualDeliveryChart scheduleId={activeSchedule?.id} />
        </TabsContent>
      </Tabs>

      {/* Outage Simulation Dialog */}
      <ResilienceScenarioDialog
        isOpen={isSimulateDialogOpen}
        onClose={() => {
          setIsSimulateDialogOpen(false);
          setSimulateEdgeId(undefined);
        }}
        presetEdgeId={simulateEdgeId}
      />
    </div>
  );
};

export default RegionalOperationsPage;
