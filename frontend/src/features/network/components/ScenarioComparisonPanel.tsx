import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card';
import { Button } from '@/shared/ui/button';
import { Badge } from '@/shared/ui/badge';
import { useResilienceScenariosQuery } from '../queries/useNetworkQueries';
import { ResilienceScenarioDialog } from './ResilienceScenarioDialog';
import { Activity, Plus, ShieldCheck } from 'lucide-react';

export const ScenarioComparisonPanel: React.FC = () => {
  const { data: scenarios = [], isLoading, refetch } = useResilienceScenariosQuery();
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  return (
    <div className="space-y-4">
      <Card className="border border-border/60 shadow-sm">
        <CardHeader className="pb-3 flex flex-row items-center justify-between">
          <div>
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <Activity className="h-4 w-4 text-amber-500" />
              Chidamlilik Ssenariylari & Stress-Test Tahlili
            </CardTitle>
            <p className="text-xs text-muted-foreground mt-0.5">
              Gipotetik avariya holatlarining tuman sug'orish tizimiga ta'siri (izolyatsiyalangan hisob-kitoblar).
            </p>
          </div>
          <Button
            size="sm"
            onClick={() => setIsDialogOpen(true)}
            className="text-xs gap-1.5 bg-amber-600 hover:bg-amber-700 text-white"
          >
            <Plus className="h-3.5 w-3.5" />
            Yangi Stress-Test Simulyatsiyasi
          </Button>
        </CardHeader>
        <CardContent className="space-y-3">
          {isLoading ? (
            <div className="h-32 bg-muted/30 rounded animate-pulse" />
          ) : scenarios.length === 0 ? (
            <div className="p-8 text-center text-muted-foreground border border-dashed rounded-lg">
              <ShieldCheck className="h-8 w-8 mx-auto mb-2 text-emerald-500/80" />
              <p className="text-sm font-medium">Hali stress-test simulyatsiyalari o'tkazilmagan</p>
              <p className="text-xs mt-1">Yangi ssenariy yaratish tugmasini bosib kanal uzilishi yoki nasos nosozligini hisoblang.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {scenarios.map((scen) => (
                <div
                  key={scen.id}
                  className="rounded-lg border border-border/70 p-3.5 bg-card hover:border-amber-500/40 transition-colors space-y-2.5 text-xs"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="font-bold text-foreground text-sm block">{scen.name}</span>
                      <Badge variant="outline" className="text-[10px] mt-1 bg-amber-500/10 text-amber-700 border-amber-500/30">
                        {scen.scenarioType}
                      </Badge>
                    </div>
                    <span className="text-muted-foreground font-mono text-[10px]">
                      {new Date(scen.createdAt).toLocaleDateString()}
                    </span>
                  </div>

                  {/* Impact metrics */}
                  <div className="grid grid-cols-3 gap-2 rounded bg-muted/40 p-2 text-center">
                    <div>
                      <span className="text-[10px] text-muted-foreground block">Yo'qotilgan Sig'im</span>
                      <span className="font-bold text-rose-600 dark:text-rose-400">
                        {scen.lostCapacityM3S.toFixed(2)} m³/s
                      </span>
                    </div>
                    <div>
                      <span className="text-[10px] text-muted-foreground block">Ta'sirlangan Dala</span>
                      <span className="font-bold text-amber-600 dark:text-amber-400">
                        {scen.affectedFieldCount} ta
                      </span>
                    </div>
                    <div>
                      <span className="text-[10px] text-muted-foreground block">Unmet Defitsit</span>
                      <span className="font-bold text-foreground">
                        {scen.unmetDemandM3.toLocaleString()} m³
                      </span>
                    </div>
                  </div>

                  <p className="text-muted-foreground text-[11px] leading-relaxed">
                    {scen.summary}
                  </p>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <ResilienceScenarioDialog
        isOpen={isDialogOpen}
        onClose={() => setIsDialogOpen(false)}
        onSimulated={() => refetch()}
      />
    </div>
  );
};
