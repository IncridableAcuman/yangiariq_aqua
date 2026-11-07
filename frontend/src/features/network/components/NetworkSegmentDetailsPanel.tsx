import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card';
import { Button } from '@/shared/ui/button';
import { WaterNetworkEdge, WaterNetworkNode } from '@/entities/network';
import { CanalStatusBadge } from './CanalStatusBadge';
import { CanalCapacityIndicator } from './CanalCapacityIndicator';
import { useSegmentCapacityQuery, useAffectedFieldsQuery } from '../queries/useNetworkQueries';
import { AlertOctagon, Droplets, Info, X } from 'lucide-react';

interface NetworkSegmentDetailsPanelProps {
  selectedEdge?: WaterNetworkEdge | null;
  selectedNode?: WaterNetworkNode | null;
  onClose: () => void;
  onSimulateOutage?: (edgeId: string) => void;
}

export const NetworkSegmentDetailsPanel: React.FC<NetworkSegmentDetailsPanelProps> = ({
  selectedEdge,
  selectedNode,
  onClose,
  onSimulateOutage,
}) => {
  const edgeId = selectedEdge?.id;
  const { data: capacity } = useSegmentCapacityQuery(edgeId);
  const { data: affected, isLoading: isAffectedLoading } = useAffectedFieldsQuery(edgeId);

  if (!selectedEdge && !selectedNode) {
    return (
      <Card className="border border-border/60 shadow-sm">
        <CardContent className="p-6 text-center text-muted-foreground">
          <Info className="h-8 w-8 mx-auto mb-2 text-muted-foreground/60" />
          <p className="text-sm font-medium">Kanal yoki tugun tanlanmagan</p>
          <p className="text-xs mt-1">Xarita ustidagi kanal chizig'i yoki gidrotexnik inshootni bosing.</p>
        </CardContent>
      </Card>
    );
  }

  if (selectedNode) {
    return (
      <Card className="border border-border/60 shadow-sm">
        <CardHeader className="pb-3 flex flex-row items-center justify-between">
          <div>
            <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              {selectedNode.nodeType}
            </span>
            <CardTitle className="text-base font-bold text-foreground mt-0.5">
              {selectedNode.name}
            </CardTitle>
          </div>
          <Button variant="ghost" size="icon" onClick={onClose} className="h-7 w-7">
            <X className="h-4 w-4" />
          </Button>
        </CardHeader>
        <CardContent className="space-y-3 text-xs">
          <div className="grid grid-cols-2 gap-2 p-2 rounded-lg bg-muted/40">
            <div>
              <span className="text-muted-foreground">Kod:</span>
              <p className="font-semibold">{selectedNode.code}</p>
            </div>
            <div>
              <span className="text-muted-foreground">Balandlik:</span>
              <p className="font-semibold">{selectedNode.elevationMeters ?? 0} m</p>
            </div>
            <div>
              <span className="text-muted-foreground">Kenglik (Lat):</span>
              <p className="font-mono">{selectedNode.latitude?.toFixed(5) ?? '-'}</p>
            </div>
            <div>
              <span className="text-muted-foreground">Uzunlik (Lon):</span>
              <p className="font-mono">{selectedNode.longitude?.toFixed(5) ?? '-'}</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-muted-foreground">Holati:</span>
            <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${
              selectedNode.isActive ? 'bg-emerald-500/10 text-emerald-600' : 'bg-rose-500/10 text-rose-600'
            }`}>
              {selectedNode.isActive ? 'Faol' : 'Nofaol'}
            </span>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="border border-border/60 shadow-sm">
      <CardHeader className="pb-3 flex flex-row items-start justify-between">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              {selectedEdge?.edgeType}
            </span>
            <CanalStatusBadge status={capacity?.status ?? selectedEdge!.operationalStatus} />
          </div>
          <CardTitle className="text-base font-bold text-foreground">
            {selectedEdge?.name}
          </CardTitle>
        </div>
        <Button variant="ghost" size="icon" onClick={onClose} className="h-7 w-7">
          <X className="h-4 w-4" />
        </Button>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Capacity Indicator */}
        <div className="rounded-lg border border-border/60 p-3 bg-muted/20">
          <span className="text-xs font-medium text-muted-foreground block mb-1">
            Gidravlik O'tkazuvchanlik:
          </span>
          <CanalCapacityIndicator
            currentFlowM3S={capacity?.currentFlowM3S ?? selectedEdge!.currentFlowM3S}
            maxCapacityM3S={capacity?.maxCapacityM3S ?? selectedEdge!.maxCapacityM3S}
            utilizationPercent={capacity?.utilizationPercent ?? selectedEdge!.utilizationPercent}
            isBlocked={capacity?.isBlocked}
          />
        </div>

        {/* Safety Warnings if Blocked */}
        {capacity?.isBlocked && (
          <div className="rounded-lg border border-rose-500/30 bg-rose-50 dark:bg-rose-950/30 p-3 text-xs text-rose-900 dark:text-rose-200 space-y-1">
            <div className="flex items-center gap-1.5 font-bold text-rose-700 dark:text-rose-300">
              <AlertOctagon className="h-4 w-4 shrink-0" />
              Gidravlik Xavfsizlik To'sig'i Faol
            </div>
            <p>{capacity.blockReason}</p>
            {capacity.safetyConstraintViolations.map((v, i) => (
              <p key={i} className="text-rose-600 dark:text-rose-400 font-mono text-[11px]">&bull; {v}</p>
            ))}
          </div>
        )}

        {/* Dependent Downstream Fields */}
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs font-semibold">
            <span className="flex items-center gap-1.5 text-foreground">
              <Droplets className="h-3.5 w-3.5 text-primary" />
              Bog'liq Dalalar ({affected?.dependentFieldCount ?? 0})
            </span>
            <span className="text-muted-foreground font-normal">
              Uzunligi: {(selectedEdge!.lengthMeters / 1000).toFixed(1)} km
            </span>
          </div>

          {isAffectedLoading ? (
            <div className="h-20 bg-muted/30 rounded animate-pulse" />
          ) : affected && affected.dependentFields.length > 0 ? (
            <div className="max-h-40 overflow-y-auto space-y-1.5 pr-1">
              {affected.dependentFields.map((f) => (
                <div
                  key={f.fieldId}
                  className="flex items-center justify-between rounded border border-border/40 p-2 text-xs bg-card"
                >
                  <div>
                    <span className="font-semibold text-foreground">{f.name}</span>
                    <span className="text-muted-foreground ml-2 text-[11px]">&bull; {f.cropType}</span>
                  </div>
                  <span className="text-muted-foreground font-medium">{f.areaHectares} ga</span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-muted-foreground italic">Past oqimda to'g'ridan-to'g'ri bog'liq dala yo'q</p>
          )}
        </div>

        {/* Action: Simulate Rupture / Outage */}
        {onSimulateOutage && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => onSimulateOutage(selectedEdge!.id)}
            className="w-full text-xs font-semibold text-amber-600 hover:text-amber-700 dark:text-amber-400 border-amber-500/40 hover:bg-amber-500/10"
          >
            Ushbu Kanal Bo'yicha Avariya Simulyatsiyasi &rarr;
          </Button>
        )}
      </CardContent>
    </Card>
  );
};
