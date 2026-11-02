import React from 'react'
import {
  X,
  MapPin,
  Waves,
  Droplets,
  AlertTriangle,
  ArrowRight,
  ExternalLink,
  Activity,
  ShieldAlert,
  Sparkles,
  BarChart3,
  Compass,
} from 'lucide-react'
import {
  GisField,
  GisWaterSource,
  GisFieldWaterStatus,
  GisAllocationLink,
} from '@/entities/gis'
import { FieldAnalyticsSummary } from '@/entities/sensor'
import { FieldAnomalySummary } from '@/entities/anomaly'
import { Recommendation } from '@/entities/recommendation'
import { SmartIrrigationPlan, WaterBudget } from '@/entities/irrigation'
import { WaterProgressBar } from '@/features/water/components/WaterProgressBar'
import { formatVolume, formatPercentage } from '@/features/water/lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { cn } from '@/shared/lib/utils'

interface FieldWaterSidePanelProps {
  selectedField: GisField | null
  selectedWaterSource: GisWaterSource | null
  waterStatus?: GisFieldWaterStatus | null
  allocations?: GisAllocationLink[]
  allFields?: GisField[]
  allWaterSources?: GisWaterSource[]
  telemetrySummary?: FieldAnalyticsSummary | null
  anomalySummary?: FieldAnomalySummary | null
  recommendations?: Recommendation[] | null
  activePlan?: SmartIrrigationPlan | null
  waterBudget?: WaterBudget | null
  onSelectField?: (field: GisField) => void
  onSelectWaterSource?: (source: GisWaterSource) => void
  onClose: () => void
  onNavigateToIrrigation?: () => void
  onNavigateToAnalytics?: (fieldId: string) => void
  onNavigateToPredictions?: (fieldId: string) => void
  userRole?: string
  lang: SupportedLanguage
  className?: string
}

export const FieldWaterSidePanel: React.FC<FieldWaterSidePanelProps> = ({
  selectedField,
  selectedWaterSource,
  waterStatus,
  allocations = [],
  allFields = [],
  allWaterSources = [],
  telemetrySummary,
  anomalySummary,
  recommendations = [],
  activePlan,
  waterBudget,
  onSelectField,
  onSelectWaterSource,
  onClose,
  onNavigateToIrrigation,
  onNavigateToAnalytics,
  onNavigateToPredictions,
  userRole,
  lang,
  className,
}) => {
  const t = translations[lang]
  const activeRecommendations = (recommendations || []).filter(
    (r) => r.status === 'GENERATED' || r.status === 'VIEWED' || r.status === 'ACCEPTED'
  )

  if (!selectedField && !selectedWaterSource) {
    return null
  }

  // -------------------------------------------------------------
  // Render Selected Water Source Details
  // -------------------------------------------------------------
  if (selectedWaterSource) {
    const sourceAllocations = allocations.filter(
      (a) => a.waterSourceId === selectedWaterSource.id
    )
    const totalAllocated = sourceAllocations.reduce(
      (acc, a) => acc + Number(a.allocatedVolumeM3 || 0),
      0
    )

    const connectedFieldIds = new Set(sourceAllocations.map((a) => a.fieldId))
    const connectedFields = allFields.filter((f) => connectedFieldIds.has(f.id))

    return (
      <div
        className={cn(
          'w-full md:w-80 lg:w-96 bg-card border border-border shadow-lg rounded-xl flex flex-col max-h-[85vh] overflow-hidden animate-in fade-in slide-in-from-right-4 duration-200',
          className
        )}
      >
        {/* Header */}
        <div className="p-4 border-b border-border/80 flex items-start justify-between gap-3 bg-muted/30">
          <div className="space-y-1">
            <div className="flex items-center gap-1.5 text-xs text-sky-600 dark:text-sky-400 font-semibold uppercase tracking-wider">
              <Waves className="w-3.5 h-3.5" />
              <span>{t.gis?.sidePanel?.waterSource || 'Suv manbasi'}</span>
            </div>
            <h3 className="font-bold text-base text-foreground leading-tight">
              {selectedWaterSource.name}
            </h3>
            <div className="flex items-center gap-2 pt-0.5">
              <Badge variant="outline" className="text-[10px] px-1.5 py-0">
                {t.water.sourceTypes[selectedWaterSource.type as keyof typeof t.water.sourceTypes] ||
                  selectedWaterSource.type}
              </Badge>
              <Badge
                variant={selectedWaterSource.status === 'ACTIVE' ? 'default' : 'secondary'}
                className="text-[10px] px-1.5 py-0"
              >
                {selectedWaterSource.status === 'ACTIVE'
                  ? t.gis?.status?.active || 'Faol'
                  : t.gis?.status?.inactive || 'Nofaol'}
              </Badge>
            </div>
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={onClose}
            className="h-7 w-7 p-0 rounded-full shrink-0"
            aria-label="Close side panel"
          >
            <X className="w-4 h-4" />
          </Button>
        </div>

        {/* Content Body */}
        <div className="p-4 overflow-y-auto space-y-4 text-xs">
          {/* Coordinates & Description */}
          {selectedWaterSource.latitude != null && selectedWaterSource.longitude != null && (
            <div className="flex items-center gap-2 p-2.5 rounded-lg bg-muted/40 font-mono text-muted-foreground">
              <MapPin className="w-3.5 h-3.5 text-sky-500 shrink-0" />
              <span>
                {Number(selectedWaterSource.latitude).toFixed(6)}°N,{' '}
                {Number(selectedWaterSource.longitude).toFixed(6)}°E
              </span>
            </div>
          )}

          {selectedWaterSource.description && (
            <div>
              <span className="text-[11px] font-semibold text-muted-foreground block mb-1">
                {t.water.sources.description || 'Tavsif'}:
              </span>
              <p className="text-foreground leading-relaxed">
                {selectedWaterSource.description}
              </p>
            </div>
          )}

          {/* Allocation Aggregate KPI */}
          <div className="grid grid-cols-2 gap-2 pt-2 border-t border-border/60">
            <div className="p-3 rounded-lg border border-border/60 bg-background space-y-1">
              <span className="text-[10px] font-medium text-muted-foreground block">
                {t.gis?.sidePanel?.connectedFields || 'Bogʻlangan dalalar'}
              </span>
              <span className="text-lg font-bold text-foreground">
                {connectedFields.length}
              </span>
            </div>
            <div className="p-3 rounded-lg border border-border/60 bg-background space-y-1">
              <span className="text-[10px] font-medium text-muted-foreground block">
                {t.gis?.sidePanel?.totalAllocated || 'Jami ajratilgan'}
              </span>
              <span className="text-base font-bold text-foreground">
                {formatVolume(totalAllocated)}
              </span>
            </div>
          </div>

          {/* Connected Fields List */}
          <div className="pt-2 border-t border-border/60 space-y-2">
            <span className="text-[11px] font-semibold text-muted-foreground block">
              {t.gis?.sidePanel?.servicedFields || 'Taʼminlanadigan dala maydonlari'}:
            </span>
            {connectedFields.length === 0 ? (
              <p className="text-muted-foreground italic text-[11px]">
                {t.gis?.sidePanel?.noConnectedFields || 'Hozircha limitlar biriktirilmagan'}
              </p>
            ) : (
              <div className="space-y-1.5 max-h-48 overflow-y-auto pr-1">
                {connectedFields.map((field) => {
                  const alloc = sourceAllocations.find((a) => a.fieldId === field.id)
                  return (
                    <div
                      key={field.id}
                      onClick={() => onSelectField?.(field)}
                      className="p-2 rounded-lg border border-border/60 hover:border-primary/60 bg-background/60 hover:bg-muted/40 cursor-pointer transition-all flex items-center justify-between gap-2"
                    >
                      <div>
                        <div className="font-semibold text-foreground">{field.name}</div>
                        <div className="text-[10px] text-muted-foreground">
                          {field.cropType} • {Number(field.areaHectares).toFixed(2)} ga
                        </div>
                      </div>
                      <div className="text-right">
                        <span className="text-xs font-bold text-primary block">
                          {alloc ? formatVolume(alloc.allocatedVolumeM3) : '—'}
                        </span>
                        <ArrowRight className="w-3 h-3 text-muted-foreground ml-auto" />
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        </div>
      </div>
    )
  }

  // -------------------------------------------------------------
  // Render Selected Field Details
  // -------------------------------------------------------------
  if (!selectedField) return null

  const isOver = waterStatus?.isOverConsumed || false
  const allocated = Number(waterStatus?.totalAllocatedVolumeM3 || 0)
  const consumed = Number(waterStatus?.totalConsumedVolumeM3 || 0)
  const remaining = Number(waterStatus?.totalRemainingVolumeM3 || 0)
  const overVolume = Number(waterStatus?.totalOverConsumedVolumeM3 || 0)
  const percent = Number(waterStatus?.consumptionPercentage || 0)

  // Find supplying water sources for this field
  const fieldAllocations = allocations.filter((a) => a.fieldId === selectedField.id)
  const supplyingSourceIds = new Set(fieldAllocations.map((a) => a.waterSourceId))
  const supplyingSources = allWaterSources.filter((s) => supplyingSourceIds.has(s.id))

  // Real-time telemetry indicators for selected parcel
  const latestMoisture = telemetrySummary?.latestReadings?.SOIL_MOISTURE
  const latestTemp = telemetrySummary?.latestReadings?.TEMPERATURE
  const latestFlow = telemetrySummary?.latestReadings?.WATER_FLOW
  const telemetryAvailability = telemetrySummary?.availability

  return (
    <div
      className={cn(
        'w-full md:w-80 lg:w-96 bg-card border border-border shadow-lg rounded-xl flex flex-col max-h-[85vh] overflow-hidden animate-in fade-in slide-in-from-right-4 duration-200',
        className
      )}
    >
      {/* Header */}
      <div className="p-4 border-b border-border/80 flex items-start justify-between gap-3 bg-muted/30">
        <div className="space-y-1">
          <div className="flex items-center gap-1.5 text-xs text-primary font-semibold uppercase tracking-wider">
            <MapPin className="w-3.5 h-3.5" />
            <span>{t.gis?.sidePanel?.field || 'Dala maydoni'}</span>
          </div>
          <h3 className="font-bold text-base text-foreground leading-tight">
            {selectedField.name}
          </h3>
          <div className="flex items-center gap-2 pt-0.5">
            <Badge variant="outline" className="text-[10px] px-1.5 py-0 font-medium">
              {selectedField.cropType}
            </Badge>
            <span className="text-xs text-muted-foreground font-semibold">
              {Number(selectedField.areaHectares).toFixed(2)} ga
            </span>
            <Badge
              variant={selectedField.status === 'ACTIVE' ? 'default' : 'secondary'}
              className="text-[10px] px-1.5 py-0"
            >
              {selectedField.status === 'ACTIVE'
                ? t.gis?.status?.active || 'Faol'
                : t.gis?.status?.inactive || 'Nofaol'}
            </Badge>
          </div>
        </div>
        <Button
          variant="ghost"
          size="sm"
          onClick={onClose}
          className="h-7 w-7 p-0 rounded-full shrink-0"
          aria-label="Close side panel"
        >
          <X className="w-4 h-4" />
        </Button>
      </div>

      {/* Content Body */}
      <div className="p-4 overflow-y-auto space-y-4 text-xs">
        {/* Over-Consumption Critical Alert Banner */}
        {isOver && (
          <div
            role="alert"
            className="p-3 rounded-lg border border-rose-300 dark:border-rose-900 bg-rose-50 dark:bg-rose-950/40 text-rose-800 dark:text-rose-200 space-y-1 animate-pulse"
          >
            <div className="flex items-center gap-1.5 font-bold text-xs text-rose-600 dark:text-rose-400">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              <span>{t.gis?.alerts?.overConsumedTitle || 'DIQQAT: SUV MEʼYORDAN ORTIQ SARFLANGAN'}</span>
            </div>
            <p className="text-[11px] leading-tight">
              {t.gis?.alerts?.overConsumedText || 'Ushbu dalada ajratilgan limitdan'}{' '}
              <strong className="underline">{formatVolume(overVolume)}</strong>{' '}
              {t.gis?.alerts?.overConsumedSuffix || 'koʻproq suv sarflangan.'}
            </p>
          </div>
        )}

        {/* Active Telemetry Anomaly Warning Banner */}
        {anomalySummary && anomalySummary.activeCount > 0 && (
          <div
            role="alert"
            className={`p-3 rounded-lg border space-y-1.5 ${
              anomalySummary.hasCritical
                ? 'border-rose-400 dark:border-rose-900 bg-rose-50 dark:bg-rose-950/40 text-rose-800 dark:text-rose-200'
                : anomalySummary.hasHigh
                ? 'border-amber-400 dark:border-amber-900 bg-amber-50 dark:bg-amber-950/40 text-amber-800 dark:text-amber-200'
                : 'border-blue-400 dark:border-blue-900 bg-blue-50 dark:bg-blue-950/40 text-blue-800 dark:text-blue-200'
            }`}
          >
            <div className="flex items-center gap-1.5 font-bold text-xs">
              <ShieldAlert className="w-4 h-4 shrink-0 text-rose-600 dark:text-rose-400 animate-pulse" />
              <span>
                {anomalySummary.hasCritical ? 'Kritik xavf aniqlangan' : 'Faol anomaliyalar'}: {anomalySummary.activeCount} ta
              </span>
            </div>
            <div className="space-y-0.5">
              {anomalySummary.activeAnomalies.slice(0, 2).map((a) => (
                <p key={a.id} className="text-[11px] leading-tight line-clamp-1">
                  • <strong>{t.anomalies?.types?.[a.anomalyType] || a.anomalyType}:</strong> {a.description}
                </p>
              ))}
            </div>
          </div>
        )}

        {/* Active AI Agronomic Recommendation Advisory Card */}
        {activeRecommendations.length > 0 && (
          <div
            className={cn(
              'p-3 rounded-lg border space-y-1.5',
              activeRecommendations[0].recommendationType === 'INSPECT_PIPE_BURST' ||
                activeRecommendations[0].priority === 'CRITICAL'
                ? 'border-rose-400 dark:border-rose-900 bg-rose-50 dark:bg-rose-950/40 text-rose-800 dark:text-rose-200'
                : 'border-purple-300 dark:border-purple-800 bg-purple-500/5 dark:bg-purple-950/20 text-foreground'
            )}
          >
            <div className="flex items-center justify-between">
              <span className="flex items-center gap-1.5 font-bold text-xs">
                <Sparkles className="w-3.5 h-3.5 text-purple-600 dark:text-purple-400" />
                <span>AI Agronomik Tavsiya</span>
              </span>
              <Badge
                variant="outline"
                className={cn(
                  'text-[10px] px-1.5 py-0 font-medium uppercase',
                  activeRecommendations[0].priority === 'CRITICAL'
                    ? 'border-rose-400 text-rose-600 bg-rose-500/10'
                    : 'border-purple-400 text-purple-600 bg-purple-500/10'
                )}
              >
                {activeRecommendations[0].priority}
              </Badge>
            </div>
            <p className="text-xs font-semibold leading-tight line-clamp-1">
              {activeRecommendations[0].title}
            </p>
            <p className="text-[11px] text-muted-foreground line-clamp-2">
              {activeRecommendations[0].summary}
            </p>
            {activeRecommendations[0].recommendationType === 'INSPECT_PIPE_BURST' && (
              <p className="text-[10px] font-bold text-rose-600 dark:text-rose-400 flex items-center gap-1">
                <ShieldAlert className="w-3 h-3" />
                Quvur tekshiruvi sababli sugʻorish toʻxtatilgan!
              </p>
            )}
          </div>
        )}

        {/* Active Smart Irrigation Plan & Safety Block Alerts */}
        {activePlan?.isBlocked && (
          <div
            role="alert"
            className="p-3 rounded-lg border border-rose-400 dark:border-rose-900 bg-rose-50 dark:bg-rose-950/40 text-rose-800 dark:text-rose-200 space-y-1.5 animate-pulse"
          >
            <div className="flex items-center gap-1.5 font-bold text-xs text-rose-600 dark:text-rose-400">
              <ShieldAlert className="w-4 h-4 shrink-0" />
              <span>Gidravlik xavfsizlik: Sugʻorish bloklangan!</span>
            </div>
            <p className="text-[11px] leading-tight">
              {activePlan.blockedReason || 'Gidravlik nosozlik sababli ushbu dalaga suv ochish qatʼiyan taqiqlangan.'}
            </p>
          </div>
        )}

        {activePlan && !activePlan.isBlocked && (
          <div className="p-3 rounded-lg border border-purple-200 dark:border-purple-900 bg-purple-500/5 space-y-2">
            <div className="flex items-center justify-between">
              <span className="flex items-center gap-1.5 font-bold text-xs text-foreground">
                <Sparkles className="w-3.5 h-3.5 text-purple-600 dark:text-purple-400" />
                <span>Smart Sugʻorish Rejasi</span>
              </span>
              <Badge
                variant="outline"
                className={cn(
                  'text-[10px] font-bold uppercase',
                  activePlan.priority === 'CRITICAL' ? 'border-rose-500 text-rose-600' : 'border-purple-400 text-purple-600'
                )}
              >
                {activePlan.status} • {activePlan.priority}
              </Badge>
            </div>
            <div className="grid grid-cols-2 gap-2 text-[11px]">
              <div>
                <span className="text-muted-foreground block">Tavsiya hajmi:</span>
                <span className="font-bold text-foreground">{formatVolume(activePlan.recommendedVolumeM3)}</span>
              </div>
              <div>
                <span className="text-muted-foreground block">Davomiyligi:</span>
                <span className="font-semibold text-foreground">
                  {(activePlan.estimatedDurationMinutes / 60).toFixed(1)} soat
                </span>
              </div>
            </div>
            <p className="text-[11px] text-muted-foreground line-clamp-2">
              {activePlan.reason}
            </p>
          </div>
        )}

        {/* Water Posture Volumetric Accounting */}
        <div className="space-y-2.5">
          <div className="flex items-center justify-between text-[11px] font-semibold text-muted-foreground uppercase tracking-wider">
            <span className="flex items-center gap-1">
              <Droplets className="w-3.5 h-3.5 text-sky-500" />
              {t.gis?.sidePanel?.waterBalance || 'Suv balansi va sarfi'}
            </span>
            <span className={cn('font-bold', isOver ? 'text-rose-600' : 'text-foreground')}>
              {formatPercentage(percent)}
            </span>
          </div>

          <WaterProgressBar
            consumed={consumed}
            allocated={allocated}
            isOverConsumed={isOver}
            showLabel={false}
          />

          <div className="grid grid-cols-2 gap-2 pt-1">
            <div className="p-2.5 rounded-lg border border-border/60 bg-background space-y-0.5">
              <span className="text-[10px] text-muted-foreground block">
                {t.water?.stats?.totalAllocated || 'Ajratilgan limit'}
              </span>
              <span className="font-bold text-xs text-foreground block">
                {formatVolume(allocated)}
              </span>
            </div>

            <div className="p-2.5 rounded-lg border border-border/60 bg-background space-y-0.5">
              <span className="text-[10px] text-muted-foreground block">
                {t.water?.stats?.totalConsumed || 'Haqiqiy sarf'}
              </span>
              <span className={cn('font-bold text-xs block', isOver ? 'text-rose-600' : 'text-foreground')}>
                {formatVolume(consumed)}
              </span>
            </div>

            <div className="p-2.5 rounded-lg border border-border/60 bg-background space-y-0.5">
              <span className="text-[10px] text-muted-foreground block">
                {t.water?.stats?.remainingBalance || 'Qolgan qoldiq'}
              </span>
              <span className="font-bold text-xs text-emerald-600 dark:text-emerald-400 block">
                {formatVolume(remaining)}
              </span>
            </div>

            <div
              className={cn(
                'p-2.5 rounded-lg border border-border/60 bg-background space-y-0.5',
                isOver && 'border-rose-300 dark:border-rose-900 bg-rose-50/20'
              )}
            >
              <span className="text-[10px] text-muted-foreground block">
                {t.water?.stats?.overConsumption || 'Ortiqcha sarf'}
              </span>
              <span
                className={cn(
                  'font-bold text-xs block',
                  isOver ? 'text-rose-600 dark:text-rose-400' : 'text-muted-foreground'
                )}
              >
                {formatVolume(overVolume)}
              </span>
            </div>
          </div>

          {waterBudget && (
            <div className="grid grid-cols-2 gap-2 pt-1">
              <div className="p-2 rounded-lg border border-border/60 bg-background/50">
                <span className="text-[10px] text-muted-foreground block">Rejalashtirilgan:</span>
                <span className="font-bold text-xs text-foreground block">
                  {formatVolume(Number(waterBudget.committedM3 || 0) + Number(waterBudget.reservedM3 || 0))}
                </span>
              </div>
              <div className="p-2 rounded-lg border border-border/60 bg-background/50">
                <span className="text-[10px] text-muted-foreground block">Rejalar uchun qoldiq:</span>
                <span className="font-bold text-xs text-emerald-600 dark:text-emerald-400 block">
                  {formatVolume(waterBudget.remainingAvailableM3)}
                </span>
              </div>
            </div>
          )}
        </div>

        {/* Supplying Water Sources */}
        <div className="pt-2 border-t border-border/60 space-y-2">
          <span className="text-[11px] font-semibold text-muted-foreground block">
            {t.gis?.sidePanel?.supplyingSources || 'Taʼminlovchi suv manbalari'}:
          </span>
          {supplyingSources.length === 0 ? (
            <p className="text-muted-foreground italic text-[11px]">
              {t.gis?.sidePanel?.noSourceAllocated || 'Ushbu dalaga suv manbasi biriktirilmagan'}
            </p>
          ) : (
            <div className="space-y-1.5">
              {supplyingSources.map((source) => {
                const alloc = fieldAllocations.find((a) => a.waterSourceId === source.id)
                return (
                  <div
                    key={source.id}
                    onClick={() => onSelectWaterSource?.(source)}
                    className="p-2 rounded-lg border border-border/60 hover:border-sky-500/60 bg-background/60 hover:bg-sky-50/20 dark:hover:bg-sky-950/20 cursor-pointer transition-all flex items-center justify-between gap-2"
                  >
                    <div className="flex items-center gap-2">
                      <Waves className="w-3.5 h-3.5 text-sky-500 shrink-0" />
                      <div>
                        <div className="font-semibold text-foreground">{source.name}</div>
                        <div className="text-[10px] text-muted-foreground">
                          {t.water.sourceTypes[source.type as keyof typeof t.water.sourceTypes] || source.type}
                        </div>
                      </div>
                    </div>
                    <div className="text-right">
                      <span className="text-xs font-bold text-sky-600 dark:text-sky-400 block">
                        {alloc ? formatVolume(alloc.allocatedVolumeM3) : '—'}
                      </span>
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </div>

        {/* Real-time Field Telemetry Section */}
        <div className="pt-2 border-t border-border/60 space-y-2">
          <div className="flex items-center justify-between text-[11px] font-semibold text-muted-foreground">
            <span className="flex items-center gap-1.5 text-foreground">
              <Activity className="w-3.5 h-3.5 text-primary" />
              <span>Dala Telemetriyasi</span>
            </span>
            {telemetryAvailability && (
              <Badge variant="outline" className="text-[10px] px-1 py-0">
                {telemetryAvailability.activeCount} datchik faol
              </Badge>
            )}
          </div>

          <div className="grid grid-cols-3 gap-1.5 text-center">
            <div className="p-2 rounded-lg bg-emerald-50/40 dark:bg-emerald-950/20 border border-emerald-500/20">
              <span className="text-[10px] text-muted-foreground block">Tuproq</span>
              <span className="text-xs font-bold text-emerald-600 dark:text-emerald-400">
                {latestMoisture ? `${Number(latestMoisture.value).toFixed(1)}%` : '—'}
              </span>
            </div>
            <div className="p-2 rounded-lg bg-amber-50/40 dark:bg-amber-950/20 border border-amber-500/20">
              <span className="text-[10px] text-muted-foreground block">Harorat</span>
              <span className="text-xs font-bold text-amber-600 dark:text-amber-400">
                {latestTemp ? `${Number(latestTemp.value).toFixed(0)}°C` : '—'}
              </span>
            </div>
            <div className="p-2 rounded-lg bg-sky-50/40 dark:bg-sky-950/20 border border-sky-500/20">
              <span className="text-[10px] text-muted-foreground block">Oqim</span>
              <span className="text-xs font-bold text-sky-600 dark:text-sky-400">
                {latestFlow ? `${Number(latestFlow.value).toFixed(1)} m³` : '—'}
              </span>
            </div>
          </div>
        </div>

        {/* Agronomic Attributes */}
        <div className="pt-2 border-t border-border/60 grid grid-cols-2 gap-2 text-[11px]">
          <div>
            <span className="text-muted-foreground block">{t.fields?.table?.soil || 'Tuproq turi'}:</span>
            <span className="font-medium text-foreground">{selectedField.soilType}</span>
          </div>
          <div>
            <span className="text-muted-foreground block">{t.fields?.table?.irrigation || 'Sugʻorish usuli'}:</span>
            <span className="font-medium text-foreground">{selectedField.irrigationMethod}</span>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="pt-3 border-t border-border/60 flex flex-col gap-2">
          {onNavigateToIrrigation && (
            <Button
              type="button"
              variant="default"
              size="sm"
              onClick={onNavigateToIrrigation}
              className="w-full justify-between text-xs h-8 bg-purple-600 hover:bg-purple-700 text-white"
            >
              <span className="flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5" />
                <span>Smart Rejalashtirish</span>
              </span>
              <ArrowRight className="w-3.5 h-3.5 ml-2" />
            </Button>
          )}

          {onNavigateToAnalytics && (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => onNavigateToAnalytics(selectedField.id)}
              className="w-full justify-between text-xs h-8 border-blue-200 dark:border-blue-900 text-blue-700 dark:text-blue-300 hover:bg-blue-50 dark:hover:bg-blue-950/40"
            >
              <span className="flex items-center gap-1.5">
                <BarChart3 className="w-3.5 h-3.5 text-blue-600 dark:text-blue-400" />
                <span>
                  {lang === 'uz'
                    ? 'Tahlil va Samaradorlik'
                    : lang === 'ru'
                    ? 'Аналитика и Эффективность'
                    : 'Performance Analytics'}
                </span>
              </span>
              <ArrowRight className="w-3.5 h-3.5 ml-2" />
            </Button>
          )}

          {onNavigateToPredictions && (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => onNavigateToPredictions(selectedField.id)}
              className="w-full justify-between text-xs h-8 border-primary/20 text-primary hover:bg-primary/5"
            >
              <span className="flex items-center gap-1.5">
                <Compass className="w-3.5 h-3.5 text-primary" />
                <span>
                  {lang === 'uz'
                    ? 'Prediktiv Prognozlar'
                    : lang === 'ru'
                    ? 'Предиктивные Прогнозы'
                    : 'Predictive Forecast'}
                </span>
              </span>
              <ArrowRight className="w-3.5 h-3.5 ml-2" />
            </Button>
          )}

          {onNavigateToIrrigation && userRole !== 'OPERATOR' && (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={onNavigateToIrrigation}
              className="w-full justify-between text-xs h-8"
            >
              <span>{t.gis?.sidePanel?.goToIrrigation || 'Sugʻorish jurnalini ochish'}</span>
              <ExternalLink className="w-3.5 h-3.5 ml-2" />
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}
