import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/ui/dialog';
import { Button } from '@/shared/ui/button';
import { Input } from '@/shared/ui/input';
import { Label } from '@/shared/ui/label';
import { useSimulateScenarioMutation, useNetworkTopologyQuery } from '../queries/useNetworkQueries';
import { Activity, AlertTriangle, ShieldAlert } from 'lucide-react';

interface ResilienceScenarioDialogProps {
  isOpen: boolean;
  onClose: () => void;
  presetEdgeId?: string;
  onSimulated?: () => void;
}

export const ResilienceScenarioDialog: React.FC<ResilienceScenarioDialogProps> = ({
  isOpen,
  onClose,
  presetEdgeId,
  onSimulated,
}) => {
  const { data: topology } = useNetworkTopologyQuery();
  const [scenarioName, setScenarioName] = useState('Magistral Kanal To\'xtalishi Ssenariysi');
  const [scenarioType, setScenarioType] = useState('CANAL_OUTAGE');
  const [selectedEdgeId, setSelectedEdgeId] = useState<string>(presetEdgeId || '');
  const [selectedNodeId, setSelectedNodeId] = useState<string>('');
  const [capacityReductionPercent, setCapacityReductionPercent] = useState('30.00');
  const [demandSurgePercent, setDemandSurgePercent] = useState('15.00');

  const simulateMutation = useSimulateScenarioMutation();

  const handleSimulate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await simulateMutation.mutateAsync({
        name: scenarioName,
        scenarioType,
        disabledEdgeIds: selectedEdgeId ? [selectedEdgeId] : [],
        disabledNodeIds: selectedNodeId ? [selectedNodeId] : [],
        capacityReductionPercent: parseFloat(capacityReductionPercent) || 0,
        demandSurgePercent: parseFloat(demandSurgePercent) || 0,
      });
      if (onSimulated) onSimulated();
      onClose();
    } catch (err) {
      console.error('Simulation failed', err);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-base font-bold text-foreground">
            <ShieldAlert className="h-5 w-5 text-amber-500" />
            Tarmoq Chidamliligi Simulyatsiyasi (What-If)
          </DialogTitle>
          <DialogDescription className="text-xs text-muted-foreground pt-1">
            Gipotetik avariya, qurg'oqchilik yoki nosozlikning past oqimga ta'sirini xavfsiz izolyatsiyada hisoblash.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSimulate} className="space-y-3.5 py-1 text-xs">
          <div className="space-y-1.5">
            <Label htmlFor="scenName" className="text-xs">Ssenariy Nomi</Label>
            <Input
              id="scenName"
              value={scenarioName}
              onChange={(e) => setScenarioName(e.target.value)}
              required
              className="text-xs"
            />
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="scenType" className="text-xs">Avariya Turi</Label>
            <select
              id="scenType"
              value={scenarioType}
              onChange={(e) => setScenarioType(e.target.value)}
              className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-xs shadow-xs focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
            >
              <option value="CANAL_OUTAGE">Kanal Yopilishi / Yoriq (Canal Rupture)</option>
              <option value="PUMP_FAILURE">Nasos Stansiyasi Nosozligi (Pump Failure)</option>
              <option value="SOURCE_DROUGHT_DEFICIT">Suv Manbai Qurg'oqchiligi (Source Deficit)</option>
              <option value="CAPACITY_RESTRICTION">O'tkazuvchanlikni Cheklash (Siltation/Repair)</option>
              <option value="DEMAND_SURGE">Issiqlik To'lqini Talab Keskin O'sishi</option>
            </select>
          </div>

          {scenarioType === 'CANAL_OUTAGE' && (
            <div className="space-y-1.5">
              <Label htmlFor="edgeSelect" className="text-xs">Ishdan Chiqqan Kanal Segments</Label>
              <select
                id="edgeSelect"
                value={selectedEdgeId}
                onChange={(e) => setSelectedEdgeId(e.target.value)}
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-xs shadow-xs focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
              >
                <option value="">Kanalni tanlang...</option>
                {topology?.edges.map((edge) => (
                  <option key={edge.id} value={edge.id}>
                    {edge.name} ({edge.edgeType})
                  </option>
                ))}
              </select>
            </div>
          )}

          {scenarioType === 'PUMP_FAILURE' && (
            <div className="space-y-1.5">
              <Label htmlFor="nodeSelect" className="text-xs">To'xtagan Nasos / Inshoot</Label>
              <select
                id="nodeSelect"
                value={selectedNodeId}
                onChange={(e) => setSelectedNodeId(e.target.value)}
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-xs shadow-xs focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
              >
                <option value="">Tugunni tanlang...</option>
                {topology?.nodes
                  .filter((n) => n.nodeType === 'PUMP_STATION' || n.nodeType === 'CONTROL_GATE')
                  .map((node) => (
                    <option key={node.id} value={node.id}>
                      {node.name} ({node.nodeType})
                    </option>
                  ))}
              </select>
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="capReduction" className="text-xs">Sig'im Kamayishi (%)</Label>
              <Input
                id="capReduction"
                type="number"
                step="1"
                min="0"
                max="100"
                value={capacityReductionPercent}
                onChange={(e) => setCapacityReductionPercent(e.target.value)}
                className="text-xs"
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="demSurge" className="text-xs">Talab O'sishi (%)</Label>
              <Input
                id="demSurge"
                type="number"
                step="1"
                min="0"
                max="100"
                value={demandSurgePercent}
                onChange={(e) => setDemandSurgePercent(e.target.value)}
                className="text-xs"
              />
            </div>
          </div>

          <div className="rounded-md border border-amber-500/20 bg-amber-50/50 dark:bg-amber-950/20 p-2 text-[11px] text-amber-900 dark:text-amber-200 flex items-start gap-1.5">
            <AlertTriangle className="h-4 w-4 shrink-0 text-amber-600 mt-0.5" />
            <span>
              <strong>Izolyatsiya kafolati:</strong> Ushbu simulyatsiya haqiqiy joriy operatsion holatga, tasdiqlangan jadvallarga yoki ma'lumotlar bazasidagi suv limitlariga aslo ta'sir qilmaydi.
            </span>
          </div>

          {simulateMutation.isError && (
            <p className="text-xs text-rose-600 font-medium">
              Xatolik: {(simulateMutation.error as Error).message}
            </p>
          )}

          <DialogFooter className="gap-2 sm:gap-0 pt-2">
            <Button type="button" variant="outline" size="sm" onClick={onClose} className="text-xs">
              Bekor Qilish
            </Button>
            <Button
              type="submit"
              size="sm"
              disabled={simulateMutation.isPending}
              className="bg-amber-600 hover:bg-amber-700 text-white text-xs gap-1.5"
            >
              <Activity className="h-3.5 w-3.5" />
              {simulateMutation.isPending ? 'Simulyatsiya hisoblanmoqda...' : 'Ssenariyni Hisoblash'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
