import React from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  Cell,
} from 'recharts';
import { RegionalWaterBalanceResponse } from '@/entities/water-accounting';

interface WaterBalanceChartProps {
  balance: RegionalWaterBalanceResponse;
}

export const WaterBalanceChart: React.FC<WaterBalanceChartProps> = ({ balance }) => {
  const chartData = [
    {
      stage: 'Kiruvchi (Inflow)',
      volume: balance.inflowReceivedM3,
      fill: '#10b981', // emerald
      note: 'Tuyamuyun bosh suv olgich',
    },
    {
      stage: 'Kanal Isrofi (Loss)',
      volume: balance.verifiedLossesM3,
      fill: '#ef4444', // red
      note: 'Ochiq kanal oʻtkazish isrofi',
    },
    {
      stage: 'Yetkazilgan (Delivered)',
      volume: balance.waterDeliveredM3,
      fill: '#3b82f6', // blue
      note: 'Dala kirish qulogʻiga yetkazilgan',
    },
    {
      stage: 'Isteʼmol (Consumed)',
      volume: balance.waterConsumedM3,
      fill: '#6366f1', // indigo
      note: 'Ildiz qatlamida oʻzlashtirilgan',
    },
    {
      stage: 'Qaytarilgan (Return Flow)',
      volume: balance.recordedReturnFlowM3,
      fill: '#8b5cf6', // purple
      note: 'Kollektor-drenaj tarmogʻiga',
    },
  ];

  return (
    <div className="p-4 bg-card border rounded-xl shadow-xs space-y-3">
      <div className="flex items-center justify-between">
        <div>
          <h4 className="font-semibold text-sm text-foreground">
            Suv Taqsimoti Bosqichlari (Water Balance Stages)
          </h4>
          <p className="text-xs text-muted-foreground">
            Bosh suv olishdan dalagacha boʻlgan taqsimot hajmlari (m³)
          </p>
        </div>
      </div>

      <div className="h-64 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData} margin={{ top: 10, right: 10, left: 10, bottom: 25 }}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.3} />
            <XAxis
              dataKey="stage"
              tick={{ fontSize: 11 }}
              interval={0}
              angle={-15}
              textAnchor="end"
            />
            <YAxis
              tick={{ fontSize: 11 }}
              tickFormatter={(v: number) => `${(v / 1000).toFixed(0)}k`}
            />
            <Tooltip
              content={({ active, payload }) => {
                if (active && payload && payload.length) {
                  const data = payload[0].payload as (typeof chartData)[0];
                  return (
                    <div className="p-2.5 bg-popover border text-popover-foreground rounded-lg shadow-md text-xs space-y-1">
                      <p className="font-semibold">{data.stage}</p>
                      <p className="text-sm font-bold text-primary">
                        {data.volume.toLocaleString()} m³
                      </p>
                      <p className="text-[11px] text-muted-foreground">{data.note}</p>
                    </div>
                  );
                }
                return null;
              }}
            />
            <Bar dataKey="volume" radius={[4, 4, 0, 0]}>
              {chartData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.fill} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};
