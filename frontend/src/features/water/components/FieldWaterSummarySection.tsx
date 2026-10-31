import React, { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Label } from '@/shared/ui/label'
import { AlertTriangle, Droplets, BarChart3, ShieldCheck, Waves } from 'lucide-react'
import { Field } from '@/entities/field'
import { useFieldWaterSummaryQuery } from '../queries/useWaterQueries'
import { WaterProgressBar } from './WaterProgressBar'
import { formatVolume, formatPercentage } from '../lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { cn } from '@/shared/lib/utils'

interface FieldWaterSummarySectionProps {
  fields: Field[]
  lang: SupportedLanguage
}

export const FieldWaterSummarySection: React.FC<FieldWaterSummarySectionProps> = ({
  fields,
  lang,
}) => {
  const t = translations[lang]

  const [selectedFieldId, setSelectedFieldId] = useState<string>(fields[0]?.id || '')
  const [fromDate, setFromDate] = useState<string>('')
  const [toDate, setToDate] = useState<string>('')
  const [activeRange, setActiveRange] = useState<{ from?: string; to?: string }>({})

  const { data: summary, isLoading, error } = useFieldWaterSummaryQuery(
    selectedFieldId || null,
    activeRange
  )

  const handleApplyFilter = (e: React.FormEvent) => {
    e.preventDefault()
    setActiveRange({
      from: fromDate ? new Date(fromDate).toISOString() : undefined,
      to: toDate ? new Date(`${toDate}T23:59:59Z`).toISOString() : undefined,
    })
  }

  const handleResetFilter = () => {
    setFromDate('')
    setToDate('')
    setActiveRange({})
  }

  const selectedField = fields.find((f) => f.id === selectedFieldId)

  return (
    <div className="space-y-6">
      {/* Header & Parcel / Date Controls */}
      <Card className="border-border/60 shadow-sm">
        <CardHeader className="pb-4">
          <CardTitle className="text-lg font-semibold flex items-center gap-2">
            <BarChart3 className="w-5 h-5 text-primary" />
            {t.water.summary.title}
          </CardTitle>
          <CardDescription className="text-xs text-muted-foreground">
            {t.water.summary.subtitle}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form
            onSubmit={handleApplyFilter}
            className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 items-end"
          >
            {/* Field Selector */}
            <div className="space-y-1.5">
              <Label htmlFor="summary-field" className="text-xs font-semibold">
                {t.water.summary.selectField}
              </Label>
              <select
                id="summary-field"
                value={selectedFieldId}
                onChange={(e) => setSelectedFieldId(e.target.value)}
                className="w-full px-3 py-2 text-sm rounded-md border border-input bg-background focus:outline-none focus:ring-2 focus:ring-ring"
              >
                {fields.map((f) => (
                  <option key={f.id} value={f.id}>
                    {f.name} ({f.areaHectares} ha, {f.cropType})
                  </option>
                ))}
              </select>
            </div>

            {/* Date From */}
            <div className="space-y-1.5">
              <Label htmlFor="summary-from" className="text-xs font-semibold">
                {t.water.summary.dateFrom}
              </Label>
              <Input
                id="summary-from"
                type="date"
                value={fromDate}
                onChange={(e) => setFromDate(e.target.value)}
              />
            </div>

            {/* Date To */}
            <div className="space-y-1.5">
              <Label htmlFor="summary-to" className="text-xs font-semibold">
                {t.water.summary.dateTo}
              </Label>
              <Input
                id="summary-to"
                type="date"
                value={toDate}
                onChange={(e) => setToDate(e.target.value)}
              />
            </div>

            {/* Action Buttons */}
            <div className="flex gap-2">
              <Button type="submit" size="sm" className="flex-1 h-9 text-xs">
                {t.water.summary.filterBtn}
              </Button>
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={handleResetFilter}
                className="h-9 text-xs"
              >
                {t.water.summary.resetBtn}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Loading state */}
      {isLoading && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 animate-pulse">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="h-28 bg-muted/60 rounded-xl" />
          ))}
        </div>
      )}

      {/* Error state */}
      {error && !isLoading && (
        <div className="p-4 rounded-lg bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900 text-rose-700 dark:text-rose-300 text-sm">
          Failed to load periodic water summary: {error.message}
        </div>
      )}

      {/* Summary Content */}
      {summary && !isLoading && (
        <div className="space-y-6">
          {/* Over-Consumption Banner */}
          {summary.isOverConsumed && (
            <div
              role="alert"
              className="flex items-start gap-3 p-4 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-300 dark:border-rose-900 text-rose-900 dark:text-rose-200"
            >
              <AlertTriangle className="w-5 h-5 text-rose-600 dark:text-rose-400 shrink-0 mt-0.5" />
              <div className="space-y-1">
                <h5 className="font-semibold text-sm">
                  {t.water.stats.overQuotaAlert}
                </h5>
                <p className="text-xs text-rose-700 dark:text-rose-300">
                  {selectedField?.name} has exceeded its seasonal allocation quota by{' '}
                  <span className="font-bold underline">
                    {formatVolume(summary.totalOverConsumedVolumeM3)}
                  </span>
                  . Total water delivered:{' '}
                  <span className="font-semibold">
                    {formatVolume(summary.totalConsumedVolumeM3)}
                  </span>{' '}
                  vs allocated quota of{' '}
                  <span className="font-semibold">
                    {formatVolume(summary.totalAllocatedVolumeM3)}
                  </span>
                  .
                </p>
              </div>
            </div>
          )}

          {/* Metric Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {/* Total Allocated */}
            <Card className="border-border/60 shadow-sm bg-card">
              <CardContent className="p-5">
                <div className="flex justify-between items-center text-xs font-semibold text-muted-foreground uppercase">
                  <span>{t.water.stats.totalAllocated}</span>
                  <Droplets className="w-4 h-4 text-sky-500" />
                </div>
                <div className="text-2xl font-bold mt-2 text-foreground">
                  {formatVolume(summary.totalAllocatedVolumeM3)}
                </div>
                <div className="text-xs text-muted-foreground mt-1">
                  {summary.allocationCount} {t.water.summary.allocationCount}
                </div>
              </CardContent>
            </Card>

            {/* Total Consumed */}
            <Card className="border-border/60 shadow-sm bg-card">
              <CardContent className="p-5">
                <div className="flex justify-between items-center text-xs font-semibold text-muted-foreground uppercase">
                  <span>{t.water.stats.totalConsumed}</span>
                  <Waves className="w-4 h-4 text-indigo-500" />
                </div>
                <div className="text-2xl font-bold mt-2 text-foreground">
                  {formatVolume(summary.totalConsumedVolumeM3)}
                </div>
                <div className="text-xs text-muted-foreground mt-1">
                  {summary.irrigationEventCount} {t.water.summary.eventCount}
                </div>
              </CardContent>
            </Card>

            {/* Remaining Balance */}
            <Card className="border-border/60 shadow-sm bg-card">
              <CardContent className="p-5">
                <div className="flex justify-between items-center text-xs font-semibold text-muted-foreground uppercase">
                  <span>{t.water.stats.remainingBalance}</span>
                  <ShieldCheck className="w-4 h-4 text-emerald-500" />
                </div>
                <div className="text-2xl font-bold mt-2 text-emerald-600 dark:text-emerald-400">
                  {formatVolume(summary.totalRemainingVolumeM3)}
                </div>
                <div className="text-xs text-muted-foreground mt-1">
                  Utilization: {formatPercentage(summary.consumptionPercentage)}
                </div>
              </CardContent>
            </Card>

            {/* Over-Consumption */}
            <Card
              className={cn(
                'border-border/60 shadow-sm bg-card',
                summary.isOverConsumed && 'border-rose-300 dark:border-rose-900 bg-rose-50/20'
              )}
            >
              <CardContent className="p-5">
                <div className="flex justify-between items-center text-xs font-semibold text-muted-foreground uppercase">
                  <span>{t.water.stats.overConsumption}</span>
                  <AlertTriangle
                    className={cn(
                      'w-4 h-4',
                      summary.isOverConsumed ? 'text-rose-500' : 'text-teal-500'
                    )}
                  />
                </div>
                <div
                  className={cn(
                    'text-2xl font-bold mt-2',
                    summary.isOverConsumed
                      ? 'text-rose-600 dark:text-rose-400'
                      : 'text-foreground'
                  )}
                >
                  {formatVolume(summary.totalOverConsumedVolumeM3)}
                </div>
                <div className="text-xs mt-1">
                  {summary.isOverConsumed ? (
                    <span className="font-semibold text-rose-600">
                      {t.water.allocations.overQuotaBadge}
                    </span>
                  ) : (
                    <span className="text-emerald-600 font-medium">
                      {t.water.stats.withinQuota}
                    </span>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Volumetric Progress Bar */}
          <Card className="border-border/60 shadow-sm p-5 space-y-2">
            <h4 className="text-sm font-semibold text-foreground">
              Quota Consumption Bar
            </h4>
            <WaterProgressBar
              consumed={summary.totalConsumedVolumeM3}
              allocated={summary.totalAllocatedVolumeM3}
              isOverConsumed={summary.isOverConsumed}
            />
          </Card>
        </div>
      )}

      {fields.length === 0 && (
        <div className="text-center py-12 text-sm text-muted-foreground">
          {t.water.summary.noFieldSelected}
        </div>
      )}
    </div>
  )
}
