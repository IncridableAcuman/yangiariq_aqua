import React from 'react'
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ReferenceLine,
} from 'recharts'
import { FieldRiskProfile } from '@/entities/risk'

interface RiskTrendChartProps {
  history?: FieldRiskProfile[]
}

export const RiskTrendChart: React.FC<RiskTrendChartProps> = ({
  history = [],
}) => {
  if (!history || history.length === 0) {
    return (
      <div className="h-48 flex flex-col items-center justify-center rounded-xl border border-dashed text-xs text-muted-foreground p-4">
        <span>No historical assessment trend available yet.</span>
        <span className="text-[11px] text-muted-foreground/80 pt-1">
          Historical scores are recorded as operational evidence and sensor telemetry accumulate.
        </span>
      </div>
    )
  }

  // Format history chronological (oldest to newest)
  const chartData = [...history]
    .reverse()
    .map((item) => {
      let timeLabel = ''
      try {
        timeLabel = new Date(item.evaluatedAt).toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit',
        })
      } catch {
        timeLabel = item.evaluatedAt
      }
      return {
        timestamp: timeLabel,
        fullTime: item.evaluatedAt,
        score: item.riskScore,
        severity: item.overallSeverity,
      }
    })

  return (
    <div className="space-y-2" data-testid="risk-trend-chart">
      <div className="flex items-center justify-between text-xs text-muted-foreground px-1">
        <span>Historical Composite Risk Score (0-100)</span>
        <div className="flex items-center gap-3 text-[11px]">
          <span className="flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-rose-500" />
            Critical &gt;75
          </span>
          <span className="flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-amber-500" />
            High &gt;50
          </span>
          <span className="flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-emerald-500" />
            Low &lt;25
          </span>
        </div>
      </div>

      <div className="h-48 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="hsl(var(--border))" />
            <XAxis
              dataKey="timestamp"
              tickLine={false}
              axisLine={false}
              tick={{ fontSize: 10, fill: 'hsl(var(--muted-foreground))' }}
            />
            <YAxis
              domain={[0, 100]}
              tickLine={false}
              axisLine={false}
              tick={{ fontSize: 10, fill: 'hsl(var(--muted-foreground))' }}
            />
            <ReferenceLine y={75} stroke="#ef4444" strokeDasharray="3 3" />
            <ReferenceLine y={50} stroke="#f59e0b" strokeDasharray="3 3" />
            <Tooltip
              content={({ active, payload }) => {
                if (active && payload && payload.length) {
                  const data = payload[0].payload
                  return (
                    <div className="rounded-lg border bg-popover p-2 shadow-md text-xs space-y-1">
                      <p className="font-semibold text-popover-foreground">{data.fullTime}</p>
                      <p className="text-muted-foreground">
                        Score:{' '}
                        <strong className="text-foreground font-mono">
                          {data.score.toFixed(1)}/100
                        </strong>
                      </p>
                      <p className="text-muted-foreground">
                        Severity: <strong className="text-foreground">{data.severity}</strong>
                      </p>
                    </div>
                  )
                }
                return null
              }}
            />
            <Line
              type="monotone"
              dataKey="score"
              stroke="hsl(var(--primary))"
              strokeWidth={2}
              dot={{ r: 3, fill: 'hsl(var(--primary))' }}
              activeDot={{ r: 5 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
