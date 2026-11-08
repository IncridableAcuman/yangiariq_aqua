import React from 'react'
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts'
import { Calendar, HelpCircle, AlertTriangle } from 'lucide-react'
import { PerformanceTrendSeries } from '@/entities/performance'

interface Props {
  trendData?: PerformanceTrendSeries
  selectedInterval: string
  onIntervalChange: (interval: string) => void
  selectedIndicatorCode: string
  onIndicatorCodeChange: (code: string) => void
}

export const PerformanceTrendChart: React.FC<Props> = ({
  trendData,
  selectedInterval,
  onIntervalChange,
  selectedIndicatorCode,
  onIndicatorCodeChange,
}) => {

  const intervals = [
    { id: 'DAILY', label: 'Kunlik' },
    { id: 'WEEKLY', label: 'Haftalik' },
    { id: 'MONTHLY', label: 'Oylik' },
    { id: 'SEASONAL', label: 'Mavsumiy' },
  ]

  const indicators = [
    { code: 'WDP_DELIVERY_FULFILLMENT', name: 'Yetkazib berish rejasi ijrosi (%)' },
    { code: 'CCP_LOSS_PERCENTAGE', name: 'Kanal yoʻqotishi ulushi (%)' },
    { code: 'WAC_QUOTA_UTILIZATION', name: 'Kvota limiti sarfi (%)' },
    { code: 'DEO_EQUITY_CV', name: 'Taqsimot tengligi koeffitsiyenti (CV)' },
    { code: 'SDR_OBSERVED_COVERAGE', name: 'Datchik empirik qamrovi (%)' },
  ]

  const chartData = trendData?.dataPoints.map((pt) => ({
    label: pt.label,
    actual: pt.isMissing ? null : pt.actualValue,
    baseline: pt.baselineValue,
    target: pt.targetValue,
    isMissing: pt.isMissing,
    isSimulated: pt.isSimulated,
    classification: pt.evidenceClassification,
  })) || []

  const hasMissing = chartData.some((d) => d.isMissing)
  const hasSimulated = chartData.some((d) => d.isSimulated)

  return (
    <div className="p-5 rounded-xl bg-card border border-border shadow-sm space-y-4">
      {/* Header controls */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <h3 className="font-semibold text-foreground flex items-center gap-2">
            <Calendar className="w-4 h-4 text-primary" />
            Vaqt Kesimidagi Dinamika va Trend Tahlili
          </h3>
          <p className="text-xs text-muted-foreground mt-0.5">
            Bazaviy holat, tasdiqlangan meʼyor va haqiqiy koʻrsatkichlar solishtirmasi
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {/* Indicator selector */}
          <select
            value={selectedIndicatorCode}
            onChange={(e) => onIndicatorCodeChange(e.target.value)}
            className="text-xs rounded-lg border border-border bg-background px-2.5 py-1.5 text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
          >
            {indicators.map((ind) => (
              <option key={ind.code} value={ind.code}>
                {ind.name}
              </option>
            ))}
          </select>

          {/* Interval tabs */}
          <div className="flex items-center rounded-lg border border-border p-0.5 bg-muted/40">
            {intervals.map((int) => (
              <button
                key={int.id}
                onClick={() => onIntervalChange(int.id)}
                className={`px-2.5 py-1 text-xs font-medium rounded-md transition-all ${
                  selectedInterval === int.id
                    ? 'bg-background text-foreground shadow-sm'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                {int.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Warning indicators for missing/simulated intervals */}
      {(hasMissing || hasSimulated) && (
        <div className="flex flex-wrap items-center gap-3 text-xs bg-muted/40 px-3 py-2 rounded-lg border border-border/60">
          {hasMissing && (
            <div className="flex items-center gap-1.5 text-rose-500 font-medium">
              <AlertTriangle className="w-3.5 h-3.5" />
              <span>Uzilish: Ayrim oraliqlarda oʻlchov yetishmaydi (uzluksiz chiziq tortilmagan)</span>
            </div>
          )}
          {hasSimulated && (
            <div className="flex items-center gap-1.5 text-purple-500 font-medium">
              <HelpCircle className="w-3.5 h-3.5" />
              <span>Simulyatsiya: Rang bilan belgilangan oraliq ML/gidravlik model bahosi</span>
            </div>
          )}
        </div>
      )}

      {/* Chart visualization */}
      <div className="h-[280px] w-full pt-2">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={chartData} margin={{ top: 10, right: 20, left: -10, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" opacity={0.2} vertical={false} />
            <XAxis dataKey="label" stroke="#888888" fontSize={11} tickLine={false} />
            <YAxis stroke="#888888" fontSize={11} tickLine={false} />
            <Tooltip
              content={({ active, payload, label }) => {
                if (active && payload && payload.length) {
                  const data = payload[0].payload
                  return (
                    <div className="p-2.5 rounded-lg bg-popover text-popover-foreground border border-border shadow-md text-xs space-y-1">
                      <p className="font-semibold">{label}</p>
                      {data.isMissing ? (
                        <p className="text-rose-500 font-medium">Oʻlchov kiritilmagan (MISSING)</p>
                      ) : (
                        <>
                          <p className="text-emerald-500">
                            Amalda: {data.actual} {trendData?.measurementUnit}
                          </p>
                          <p className="text-blue-400">
                            Meʼyor: {data.target} {trendData?.measurementUnit}
                          </p>
                          <p className="text-zinc-400">
                            Baza: {data.baseline} {trendData?.measurementUnit}
                          </p>
                          {data.isSimulated && (
                            <p className="text-purple-400 text-[10px] italic">Simulyatsiya qilingan nuqta</p>
                          )}
                        </>
                      )}
                    </div>
                  )
                }
                return null
              }}
            />
            <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />

            {/* Target reference line */}
            <Line
              type="monotone"
              dataKey="target"
              name="Operatsion Meʼyor (Target)"
              stroke="#3b82f6"
              strokeDasharray="4 4"
              strokeWidth={2}
              dot={false}
            />

            {/* Baseline line */}
            <Line
              type="monotone"
              dataKey="baseline"
              name="Tarixiy Baza (Baseline)"
              stroke="#94a3b8"
              strokeDasharray="2 2"
              strokeWidth={1.5}
              dot={false}
            />

            {/* Actual line (connectNulls=false to never falsely bridge missing intervals!) */}
            <Line
              type="monotone"
              dataKey="actual"
              name="Haqiqiy Natija (Actual)"
              stroke="#10b981"
              strokeWidth={2.5}
              connectNulls={false}
              dot={(props: any) => {
                const isSim = props.payload?.isSimulated
                return (
                  <circle
                    cx={props.cx}
                    cy={props.cy}
                    r={isSim ? 5 : 4}
                    fill={isSim ? '#a855f7' : '#10b981'}
                    stroke={isSim ? '#ffffff' : '#10b981'}
                    strokeWidth={isSim ? 2 : 1}
                  />
                )
              }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
