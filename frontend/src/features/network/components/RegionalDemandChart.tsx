import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card';
import { RegionalDemandResponse } from '@/entities/network';
import { BarChart3 } from 'lucide-react';

interface RegionalDemandChartProps {
  demandData?: RegionalDemandResponse | null;
  isLoading?: boolean;
}

export const RegionalDemandChart: React.FC<RegionalDemandChartProps> = ({
  demandData,
  isLoading = false,
}) => {
  if (isLoading || !demandData) {
    return (
      <Card className="animate-pulse">
        <CardHeader>
          <div className="h-5 w-40 bg-muted rounded" />
        </CardHeader>
        <CardContent>
          <div className="h-48 bg-muted rounded" />
        </CardContent>
      </Card>
    );
  }

  const { districtTotalRequestedM3, districtTotalAllocatedM3, districtTotalUnmetM3, wuaDemands, canalDemands } = demandData;

  return (
    <div className="space-y-4">
      {/* 1. Demand & Allocation Summary */}
      <Card className="border border-border/60 shadow-sm">
        <CardHeader className="pb-3">
          <CardTitle className="text-base font-semibold flex items-center justify-between">
            <span className="flex items-center gap-2">
              <BarChart3 className="h-4 w-4 text-primary" />
              Tuman Suv Talabi & Taqsimoti
            </span>
            <span className="text-xs text-muted-foreground font-normal">
              7 kunlik rejalashtirish davri
            </span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-3 gap-3 text-center">
            <div className="rounded-lg bg-sky-50 dark:bg-sky-950/40 p-3">
              <span className="text-xs text-sky-700 dark:text-sky-300 font-medium">Jami Talab</span>
              <p className="text-lg font-bold text-sky-900 dark:text-sky-100 mt-0.5">
                {districtTotalRequestedM3.toLocaleString()} m³
              </p>
            </div>
            <div className="rounded-lg bg-emerald-50 dark:bg-emerald-950/40 p-3">
              <span className="text-xs text-emerald-700 dark:text-emerald-300 font-medium">Ajratilgan</span>
              <p className="text-lg font-bold text-emerald-900 dark:text-emerald-100 mt-0.5">
                {districtTotalAllocatedM3.toLocaleString()} m³
              </p>
            </div>
            <div className="rounded-lg bg-amber-50 dark:bg-amber-950/40 p-3">
              <span className="text-xs text-amber-700 dark:text-amber-300 font-medium">Qondirilmagan Defitsit</span>
              <p className="text-lg font-bold text-amber-900 dark:text-amber-100 mt-0.5">
                {districtTotalUnmetM3.toLocaleString()} m³
              </p>
            </div>
          </div>

          {/* 2. WUA Breakdown */}
          <div className="space-y-3 pt-2">
            <h4 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Suv Iste'molchilari Uyushmalari (SIU) Bo'yicha
            </h4>
            {wuaDemands.length === 0 ? (
              <p className="text-xs text-muted-foreground italic">SIU ma'lumotlari mavjud emas</p>
            ) : (
              wuaDemands.map((wua) => {
                const ratio = wua.requestedM3 > 0
                  ? Math.round((wua.allocatedM3 / wua.requestedM3) * 100)
                  : 100;

                return (
                  <div key={wua.wuaId} className="space-y-1 rounded-md border border-border/40 p-3 bg-card">
                    <div className="flex items-center justify-between text-xs font-medium">
                      <span className="font-semibold text-foreground">{wua.wuaName}</span>
                      <span className="text-muted-foreground">
                        {wua.fieldCount} dala &bull; {ratio}% ta'minlangan
                      </span>
                    </div>
                    <div className="flex items-center justify-between text-xs text-muted-foreground">
                      <span>Talab: {wua.requestedM3.toLocaleString()} m³</span>
                      <span className="text-emerald-600 dark:text-emerald-400">Ajratildi: {wua.allocatedM3.toLocaleString()} m³</span>
                    </div>
                    <div className="h-1.5 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
                      <div
                        className={`h-full transition-all duration-300 ${
                          ratio >= 80 ? 'bg-emerald-500' : ratio >= 50 ? 'bg-amber-500' : 'bg-rose-500'
                        }`}
                        style={{ width: `${Math.min(100, ratio)}%` }}
                      />
                    </div>
                  </div>
                );
              })
            )}
          </div>

          {/* 3. Canal Demand Pressure */}
          <div className="space-y-2 pt-2">
            <h4 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Kanal O'tkazish Bosimi
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {canalDemands.slice(0, 6).map((canal) => (
                <div
                  key={canal.edgeId}
                  className={`flex items-center justify-between p-2 rounded text-xs border ${
                    canal.isConstrained
                      ? 'border-amber-500/30 bg-amber-50/50 dark:bg-amber-950/20 text-amber-900 dark:text-amber-200'
                      : 'border-border/40 bg-card text-foreground'
                  }`}
                >
                  <span className="font-medium line-clamp-1">{canal.canalName}</span>
                  <span className="shrink-0 text-muted-foreground ml-2">
                    {canal.totalDemandM3.toLocaleString()} m³
                  </span>
                </div>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
