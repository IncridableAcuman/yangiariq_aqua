import React, { useState, useMemo } from 'react'
import {
  Activity,
  Droplets,
  Thermometer,
  Waves,
  Zap,
  CheckCircle2,
  RefreshCw,
  Cpu,
  Layers,
} from 'lucide-react'
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts'
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { useFieldsQuery } from '@/features/field/queries/useFieldQueries'
import {
  useFieldSensorsQuery,
  useFieldAnalyticsQuery,
  useFieldAnalyticsSummaryQuery,
  useSimulateReadingMutation,
} from '@/features/sensor/queries/useSensorQueries'
import { SensorType } from '@/entities/sensor'

interface FieldTelemetryDashboardProps {
  lang: SupportedLanguage
  initialFieldId?: string
}

export const FieldTelemetryDashboard: React.FC<FieldTelemetryDashboardProps> = ({
  lang,
  initialFieldId,
}) => {
  const t = translations[lang]
  const [selectedFieldId, setSelectedFieldId] = useState<string>(initialFieldId || '')
  const [selectedModality, setSelectedModality] = useState<SensorType>('SOIL_MOISTURE')
  const [timeRange, setTimeRange] = useState<'24h' | '7d' | '30d'>('7d')
  const [interval, setInterval] = useState<'5m' | '15m' | '1h' | '1d'>('1h')

  // Fetch available fields
  const { data: fieldsPage, isLoading: fieldsLoading } = useFieldsQuery({ size: 100 })
  const fields = fieldsPage?.content || []

  // Default to first field if not set
  React.useEffect(() => {
    if (!selectedFieldId && fields.length > 0) {
      setSelectedFieldId(fields[0].id)
    }
  }, [fields, selectedFieldId])

  const selectedField = fields.find((f) => f.id === selectedFieldId)

  // Query time boundaries
  const { from, to } = useMemo(() => {
    const toTime = new Date()
    const fromTime = new Date()
    if (timeRange === '24h') {
      fromTime.setHours(toTime.getHours() - 24)
    } else if (timeRange === '7d') {
      fromTime.setDate(toTime.getDate() - 7)
    } else {
      fromTime.setDate(toTime.getDate() - 30)
    }
    return {
      from: fromTime.toISOString(),
      to: toTime.toISOString(),
    }
  }, [timeRange])

  // Queries
  const {
    data: summary,
    isLoading: summaryLoading,
    refetch: refetchSummary,
  } = useFieldAnalyticsSummaryQuery(selectedFieldId)

  const {
    data: seriesData,
    isLoading: seriesLoading,
    refetch: refetchSeries,
  } = useFieldAnalyticsQuery(selectedFieldId, {
    sensorType: selectedModality,
    from,
    to,
    interval,
  })

  const { data: sensors = [] } = useFieldSensorsQuery(selectedFieldId)
  const simulateMutation = useSimulateReadingMutation()

  // Format chart items
  const chartData = useMemo(() => {
    if (!seriesData || seriesData.length === 0) return []
    return seriesData.map((d) => {
      const date = new Date(d.bucketStartTime)
      let timeLabel = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      if (timeRange !== '24h') {
        timeLabel = `${date.getMonth() + 1}/${date.getDate()} ${timeLabel}`
      }
      return {
        timestamp: d.bucketStartTime,
        timeLabel,
        value: d.average != null ? Number(d.average) : d.sum != null ? Number(d.sum) : Number(d.maximum),
        min: Number(d.minimum),
        max: Number(d.maximum),
        count: d.readingCount,
        unit: d.unit,
      }
    })
  }, [seriesData, timeRange])

  // Active sensors on field
  const handleSimulate = (sensorId: string) => {
    simulateMutation.mutate(
      { sensorId, fieldId: selectedFieldId },
      {
        onSuccess: () => {
          refetchSummary()
          refetchSeries()
        },
      }
    )
  }

  const latestReadings = summary?.latestReadings || {}
  const past24h = summary?.past24hAggregates || {}
  const quality = summary?.past24hDataQuality
  const availability = summary?.availability

  return (
    <div className="space-y-6">
      {/* Top Controls: Field Selector & Refresh */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-4 rounded-xl border border-border bg-card shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center gap-3 w-full sm:w-auto">
          <label className="text-sm font-semibold text-foreground whitespace-nowrap">
            {t.telemetry.selectField}:
          </label>
          <select
            value={selectedFieldId}
            onChange={(e) => setSelectedFieldId(e.target.value)}
            disabled={fieldsLoading}
            className="w-full sm:w-72 h-9 rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
          >
            {fields.map((f) => (
              <option key={f.id} value={f.id}>
                {f.name} ({f.cropType} - {Number(f.areaHectares).toFixed(1)} ga)
              </option>
            ))}
          </select>
        </div>

        <div className="flex items-center gap-2 self-end sm:self-auto">
          <Button
            variant="outline"
            size="sm"
            onClick={() => {
              refetchSummary()
              refetchSeries()
            }}
            disabled={summaryLoading || seriesLoading}
            className="h-9 gap-1.5"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${summaryLoading ? 'animate-spin' : ''}`} />
            <span>{t.common.refresh}</span>
          </Button>
        </div>
      </div>

      {/* KPI Stat Cards: Soil, Weather, Water Delivery */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {/* Soil Moisture */}
        <Card className="border-border">
          <CardHeader className="p-4 pb-2 flex flex-row items-center justify-between">
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              {t.telemetry.modalities.SOIL_MOISTURE}
            </span>
            <Droplets className="w-4 h-4 text-emerald-500" />
          </CardHeader>
          <CardContent className="p-4 pt-0 space-y-1">
            <div className="flex items-baseline gap-1.5">
              <span className="text-2xl font-bold text-foreground">
                {latestReadings.SOIL_MOISTURE ? Number(latestReadings.SOIL_MOISTURE.value).toFixed(1) : '—'}
              </span>
              <span className="text-xs font-medium text-muted-foreground">%</span>
            </div>
            <div className="text-[11px] text-muted-foreground flex items-center justify-between">
              <span>24s oʻrtacha:</span>
              <span className="font-semibold text-foreground">
                {past24h.SOIL_MOISTURE?.average ? `${Number(past24h.SOIL_MOISTURE.average).toFixed(1)}%` : '—'}
              </span>
            </div>
          </CardContent>
        </Card>

        {/* Temperature */}
        <Card className="border-border">
          <CardHeader className="p-4 pb-2 flex flex-row items-center justify-between">
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              {t.telemetry.modalities.TEMPERATURE}
            </span>
            <Thermometer className="w-4 h-4 text-amber-500" />
          </CardHeader>
          <CardContent className="p-4 pt-0 space-y-1">
            <div className="flex items-baseline gap-1.5">
              <span className="text-2xl font-bold text-foreground">
                {latestReadings.TEMPERATURE ? Number(latestReadings.TEMPERATURE.value).toFixed(1) : '—'}
              </span>
              <span className="text-xs font-medium text-muted-foreground">°C</span>
            </div>
            <div className="text-[11px] text-muted-foreground flex items-center justify-between">
              <span>Min / Max:</span>
              <span className="font-semibold text-foreground">
                {past24h.TEMPERATURE
                  ? `${Number(past24h.TEMPERATURE.minimum).toFixed(0)}° / ${Number(past24h.TEMPERATURE.maximum).toFixed(0)}°`
                  : '—'}
              </span>
            </div>
          </CardContent>
        </Card>

        {/* Water Flow */}
        <Card className="border-border">
          <CardHeader className="p-4 pb-2 flex flex-row items-center justify-between">
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              {t.telemetry.modalities.WATER_FLOW}
            </span>
            <Waves className="w-4 h-4 text-sky-500" />
          </CardHeader>
          <CardContent className="p-4 pt-0 space-y-1">
            <div className="flex items-baseline gap-1.5">
              <span className="text-2xl font-bold text-foreground">
                {latestReadings.WATER_FLOW ? Number(latestReadings.WATER_FLOW.value).toFixed(1) : '—'}
              </span>
              <span className="text-xs font-medium text-muted-foreground">m³/h</span>
            </div>
            <div className="text-[11px] text-muted-foreground flex items-center justify-between">
              <span>Oqim holati:</span>
              <Badge variant="outline" className="text-[10px] px-1 py-0">
                {latestReadings.WATER_FLOW && Number(latestReadings.WATER_FLOW.value) > 0 ? 'Faol oqim' : 'Toʻxtatilgan'}
              </Badge>
            </div>
          </CardContent>
        </Card>

        {/* Soil Salinity */}
        <Card className="border-border">
          <CardHeader className="p-4 pb-2 flex flex-row items-center justify-between">
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              {t.telemetry.modalities.SALINITY}
            </span>
            <Zap className="w-4 h-4 text-purple-500" />
          </CardHeader>
          <CardContent className="p-4 pt-0 space-y-1">
            <div className="flex items-baseline gap-1.5">
              <span className="text-2xl font-bold text-foreground">
                {latestReadings.SALINITY ? Number(latestReadings.SALINITY.value).toFixed(2) : '—'}
              </span>
              <span className="text-xs font-medium text-muted-foreground">dS/m</span>
            </div>
            <div className="text-[11px] text-muted-foreground flex items-center justify-between">
              <span>Xavf darajasi:</span>
              <span className="font-semibold text-foreground">
                {latestReadings.SALINITY && Number(latestReadings.SALINITY.value) > 4.0 ? 'Yuqori shoʻrlanish' : 'Normal'}
              </span>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Hardware Fleet & Data Quality Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Availability */}
        <Card className="border-border">
          <CardHeader className="p-4 pb-2">
            <CardTitle className="text-sm font-bold flex items-center gap-2">
              <Cpu className="w-4 h-4 text-primary" />
              <span>{t.telemetry.availability.title}</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="p-4 pt-2">
            <div className="grid grid-cols-3 gap-2 text-center">
              <div className="p-2.5 rounded-lg bg-emerald-50/50 dark:bg-emerald-950/20 border border-emerald-500/20">
                <div className="text-lg font-bold text-emerald-600 dark:text-emerald-400">
                  {availability?.communicatingCount || 0}
                </div>
                <div className="text-[10px] text-muted-foreground">{t.telemetry.availability.communicating}</div>
              </div>
              <div className="p-2.5 rounded-lg bg-amber-50/50 dark:bg-amber-950/20 border border-amber-500/20">
                <div className="text-lg font-bold text-amber-600 dark:text-amber-400">
                  {availability?.unresponsiveCount || 0}
                </div>
                <div className="text-[10px] text-muted-foreground">{t.telemetry.availability.unresponsive}</div>
              </div>
              <div className="p-2.5 rounded-lg bg-muted/40 border border-border">
                <div className="text-lg font-bold text-foreground">{availability?.totalSensors || 0}</div>
                <div className="text-[10px] text-muted-foreground">{t.telemetry.availability.total}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Data Quality */}
        <Card className="border-border">
          <CardHeader className="p-4 pb-2">
            <CardTitle className="text-sm font-bold flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-500" />
              <span>{t.telemetry.quality.title}</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="p-4 pt-2">
            <div className="grid grid-cols-3 gap-2 text-center">
              <div className="p-2.5 rounded-lg bg-emerald-50/50 dark:bg-emerald-950/20 border border-emerald-500/20">
                <div className="text-lg font-bold text-emerald-600 dark:text-emerald-400">
                  {quality?.validPercentage != null ? `${quality.validPercentage}%` : '100%'}
                </div>
                <div className="text-[10px] text-muted-foreground">{t.telemetry.quality.validPct}</div>
              </div>
              <div className="p-2.5 rounded-lg bg-amber-50/50 dark:bg-amber-950/20 border border-amber-500/20">
                <div className="text-lg font-bold text-amber-600 dark:text-amber-400">
                  {quality?.suspectReadings || 0}
                </div>
                <div className="text-[10px] text-muted-foreground">{t.telemetry.quality.suspect}</div>
              </div>
              <div className="p-2.5 rounded-lg bg-rose-50/50 dark:bg-rose-950/20 border border-rose-500/20">
                <div className="text-lg font-bold text-rose-600 dark:text-rose-400">
                  {quality?.invalidReadings || 0}
                </div>
                <div className="text-[10px] text-muted-foreground">{t.telemetry.quality.invalid}</div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Historical Telemetry Chart Section */}
      <Card className="border-border">
        <CardHeader className="p-4 pb-3 flex flex-col md:flex-row md:items-center justify-between gap-3 border-b border-border/80">
          <div className="space-y-1">
            <CardTitle className="text-base font-bold flex items-center gap-2">
              <Activity className="w-4 h-4 text-primary" />
              <span>Vaqt Seriyalari Tahlili</span>
            </CardTitle>
            <p className="text-xs text-muted-foreground">
              {selectedField?.name} — {t.telemetry.modalities[selectedModality]} dinamikasi
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            {/* Interval Selector */}
            <div className="flex items-center gap-1 bg-muted/60 p-1 rounded-lg text-xs">
              {(['5m', '15m', '1h', '1d'] as const).map((code) => (
                <button
                  key={code}
                  type="button"
                  onClick={() => setInterval(code)}
                  className={`px-2 py-0.5 rounded font-medium transition-all ${
                    interval === code
                      ? 'bg-background text-foreground shadow-sm'
                      : 'text-muted-foreground hover:text-foreground'
                  }`}
                >
                  {t.telemetry.intervals[code]}
                </button>
              ))}
            </div>

            {/* Range Selector */}
            <div className="flex items-center gap-1 bg-muted/60 p-1 rounded-lg text-xs">
              {(['24h', '7d', '30d'] as const).map((r) => (
                <button
                  key={r}
                  type="button"
                  onClick={() => setTimeRange(r)}
                  className={`px-2 py-0.5 rounded font-medium transition-all ${
                    timeRange === r
                      ? 'bg-background text-foreground shadow-sm'
                      : 'text-muted-foreground hover:text-foreground'
                  }`}
                >
                  {t.telemetry.timeRange[r]}
                </button>
              ))}
            </div>
          </div>
        </CardHeader>

        <CardContent className="p-4">
          {/* Modality Tabs */}
          <div className="flex items-center gap-1 overflow-x-auto pb-3 mb-3 border-b border-border/60">
            {(
              [
                'SOIL_MOISTURE',
                'TEMPERATURE',
                'HUMIDITY',
                'WATER_FLOW',
                'WATER_LEVEL',
                'SALINITY',
                'RAINFALL',
              ] as SensorType[]
            ).map((mod) => (
              <button
                key={mod}
                type="button"
                onClick={() => setSelectedModality(mod)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-all ${
                  selectedModality === mod
                    ? 'bg-primary text-primary-foreground shadow-sm'
                    : 'bg-muted/40 hover:bg-muted text-muted-foreground hover:text-foreground'
                }`}
              >
                {t.telemetry.modalities[mod]}
              </button>
            ))}
          </div>

          {/* Chart Display */}
          {seriesLoading ? (
            <div className="h-64 flex items-center justify-center text-xs text-muted-foreground animate-pulse">
              {t.common.loading}
            </div>
          ) : chartData.length === 0 ? (
            <div className="h-64 flex flex-col items-center justify-center text-xs text-muted-foreground space-y-2 border border-dashed border-border rounded-xl">
              <Layers className="w-8 h-8 opacity-40" />
              <span>{t.telemetry.noReadings}</span>
            </div>
          ) : (
            <div className="h-72 w-full pt-2">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="telemetryGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="hsl(var(--primary))" stopOpacity={0.35} />
                      <stop offset="95%" stopColor="hsl(var(--primary))" stopOpacity={0.0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.25} />
                  <XAxis dataKey="timeLabel" tick={{ fontSize: 10 }} tickLine={false} axisLine={false} />
                  <YAxis tick={{ fontSize: 10 }} tickLine={false} axisLine={false} />
                  <Tooltip
                    content={({ active, payload }) => {
                      if (!active || !payload || payload.length === 0) return null
                      const d = payload[0].payload
                      return (
                        <div className="bg-popover border border-border p-2.5 rounded-lg shadow-lg text-xs space-y-1">
                          <div className="font-semibold text-foreground">{d.timeLabel}</div>
                          <div className="text-primary font-bold">
                            {t.telemetry.metrics.average}: {d.value} {d.unit}
                          </div>
                          <div className="text-[11px] text-muted-foreground flex justify-between gap-3">
                            <span>Min: {d.min}</span>
                            <span>Max: {d.max}</span>
                          </div>
                          <div className="text-[10px] text-muted-foreground pt-0.5">
                            {t.telemetry.metrics.count}: {d.count}
                          </div>
                        </div>
                      )
                    }}
                  />
                  <Area
                    type="monotone"
                    dataKey="value"
                    stroke="hsl(var(--primary))"
                    strokeWidth={2}
                    fillOpacity={1}
                    fill="url(#telemetryGradient)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Deployed Hardware Sensors List */}
      <Card className="border-border">
        <CardHeader className="p-4 pb-2">
          <CardTitle className="text-sm font-bold flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Cpu className="w-4 h-4 text-primary" />
              <span>Dala Datchiklari va Apparat Qurilmalari</span>
            </div>
            <Badge variant="outline" className="text-xs">
              {sensors.length} ta datchik
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="p-4 pt-2">
          {sensors.length === 0 ? (
            <div className="text-xs text-muted-foreground py-6 text-center">
              {t.telemetry.noSensors}
            </div>
          ) : (
            <div className="divide-y divide-border/60">
              {sensors.map((sensor) => (
                <div
                  key={sensor.id}
                  className="py-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-foreground text-sm">{sensor.name}</span>
                      <Badge variant="outline" className="text-[10px] font-mono">
                        {sensor.deviceIdentifier}
                      </Badge>
                      <Badge
                        variant={sensor.status === 'ACTIVE' ? 'default' : 'secondary'}
                        className="text-[10px]"
                      >
                        {sensor.status}
                      </Badge>
                    </div>
                    <div className="flex items-center gap-3 text-[11px] text-muted-foreground">
                      <span>{t.telemetry.modalities[sensor.type]}</span>
                      {sensor.lastSeenAt && (
                        <span>
                          Soʻnggi aloqa: {new Date(sensor.lastSeenAt).toLocaleTimeString()}
                        </span>
                      )}
                      {sensor.description && <span>• {sensor.description}</span>}
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => handleSimulate(sensor.id)}
                      disabled={simulateMutation.isPending}
                      className="h-8 text-xs gap-1.5"
                    >
                      <RefreshCw
                        className={`w-3 h-3 ${simulateMutation.isPending ? 'animate-spin' : ''}`}
                      />
                      <span>{t.telemetry.simulate}</span>
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
